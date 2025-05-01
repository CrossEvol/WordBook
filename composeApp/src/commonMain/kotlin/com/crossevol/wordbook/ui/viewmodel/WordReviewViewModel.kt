package com.crossevol.wordbook.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossevol.wordbook.data.WordRepository
import com.crossevol.wordbook.data.model.WordItemUI
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val logger = KotlinLogging.logger {}

/**
 * ViewModel for Word Review functionality, handling both the review summary page
 * and the review process.
 */
open class WordReviewViewModel(
    protected val wordRepository: WordRepository
) : ViewModel() {

    // UI state for the review summary
    sealed class ReviewState {
        data object Loading : ReviewState()
        data class Success(val words: List<WordItemUI>) : ReviewState()
        data class Error(val message: String) : ReviewState()
    }

    // State flow for the review process
    private val _reviewState = MutableStateFlow<ReviewState>(ReviewState.Loading)
    val reviewState: StateFlow<ReviewState> = _reviewState.asStateFlow()

    // The current language code being viewed
    private var languageCode by mutableStateOf("EN")
        private set

    // Current position in the review process
    private val _currentReviewIndex = MutableStateFlow(0)
    val currentReviewIndex = _currentReviewIndex.asStateFlow()
    
    // Words that need review
    private var wordsToReview = listOf<WordItemUI>()
    
    // Count of remaining words to review
    private val _remainingWordsCount = MutableStateFlow(0)
    val remainingWordsCount = _remainingWordsCount.asStateFlow()

    // Pending action to be applied when moveToNextWord is called
    private var pendingAction: PendingReviewAction? = null

    /**
     * Represents a pending action to be applied when moving to the next word
     */
    private data class PendingReviewAction(
        val wordId: Long,
        val remembered: Boolean?  // null means skip
    )

    /**
     * Get the current word being reviewed, or null if no words are available.
     */
    val currentWord: WordItemUI?
        get() = wordsToReview.getOrNull(_currentReviewIndex.value)

    /**
     * Load words that need review for the given language code.
     */
    open fun loadWordsForReview(languageCode: String = "EN") {
        this.languageCode = languageCode
        _reviewState.value = ReviewState.Loading
        
        viewModelScope.launch {
            try {
                withContext(Dispatchers.Default) {
                    // Use the repository method to get words needing review
                    wordsToReview = wordRepository.getWordsNeedingReview(languageCode)
                    logger.info { "Loaded ${wordsToReview.size} words that need review for language $languageCode" }
                }
                
                if (wordsToReview.isEmpty()) {
                    _reviewState.value = ReviewState.Error("No words found for review")
                    _remainingWordsCount.value = 0
                } else {
                    _reviewState.value = ReviewState.Success(wordsToReview)
                    _currentReviewIndex.value = 0
                    _remainingWordsCount.value = wordsToReview.size
                }
            } catch (e: Exception) {
                logger.error(e) { "Error loading words for review: ${e.message}" }
                _reviewState.value = ReviewState.Error("Failed to load words for review: ${e.message}")
                _remainingWordsCount.value = 0
            }
        }
    }

    /**
     * Advance to the next word in review and apply any pending action.
     * Returns true if successfully moved to next word, false if at the end.
     */
    fun moveToNextWord(): Boolean {
        // Apply any pending action first
        pendingAction?.let { action ->
            if (action.remembered != null) {
                applyWordReviewProgress(action.wordId, action.remembered)
            }
            pendingAction = null
        }

        if (_currentReviewIndex.value < wordsToReview.size - 1) {
            _currentReviewIndex.value += 1
            _remainingWordsCount.value = wordsToReview.size - _currentReviewIndex.value
            return true
        }
        _remainingWordsCount.value = 0
        return false
    }

    /**
     * Prepares a review action for a word (remembered or forgot).
     * This action will be applied when moveToNextWord is called.
     * 
     * @param wordId The ID of the word being reviewed
     * @param remembered Whether the user remembered the word (true) or forgot it (false)
     */
    open fun prepareWordReviewAction(wordId: Long, remembered: Boolean) {
        pendingAction = PendingReviewAction(wordId, remembered)
        logger.info { "Prepared review action for word ID $wordId (remembered: $remembered)" }
    }

    /**
     * Prepares to skip the current word without changing its review progress.
     * 
     * @param wordId The ID of the word being skipped
     */
    open fun prepareSkipWordReview(wordId: Long) {
        pendingAction = PendingReviewAction(wordId, null)
        logger.info { "Prepared skip action for word ID: $wordId" }
    }

    /**
     * Updates the review progress for a word directly.
     * This is called from the UI to mark a word as remembered or forgotten.
     * 
     * @param wordId The ID of the word being reviewed
     * @param remembered Whether the user remembered the word (true) or forgot it (false)
     * @return True if the update was successful, false otherwise
     */
    fun updateWordReviewProgress(wordId: Long, remembered: Boolean): Boolean {
        val word = wordsToReview.find { it.id == wordId }
        if (word == null) {
            logger.error { "Word with ID $wordId not found in review list" }
            return false
        }
        
        // Use the new updateWordReviewResult method that directly uses the ReviewCalculator
        val success = wordRepository.updateWordReviewResult(wordId, remembered)
        
        if (success) {
            // No need to calculate the new progress here as it's handled in the repository
            logger.info { "Updated word ${word.title} review result (remembered: $remembered)" }
        } else {
            logger.error { "Failed to update word ${word.title} review result" }
        }
        
        return success
    }
    
    /**
     * Skips a word in the review process without updating its progress.
     * 
     * @param wordId The ID of the word to skip
     * @return True if the word was found in the review list
     */
    fun skipWordReview(wordId: Long): Boolean {
        val word = wordsToReview.find { it.id == wordId }
        if (word == null) {
            logger.error { "Word with ID $wordId not found in review list" }
            return false
        }
        
        logger.info { "Skipped review for word: ${word.title}" }
        return true
    }
    
    /**
     * Actually applies the word review progress update in the repository.
     * This is called internally when moving to the next word.
     */
    private fun applyWordReviewProgress(wordId: Long, remembered: Boolean): Boolean {
        // Find the word in the list of words to review
        val word = wordsToReview.find { it.id == wordId } ?: return false
        
        return try {
            // Use the new updateWordReviewResult method that directly uses the ReviewCalculator
            val success = wordRepository.updateWordReviewResult(wordId, remembered)
            
            if (success) {
                logger.info { "Applied review result for word ${word.title} (remembered: $remembered)" }
            } else {
                logger.error { "Failed to apply review result for word ${word.title}" }
            }
            
            success
        } catch (e: Exception) {
            logger.error(e) { "Error applying word review result: ${e.message}" }
            false
        }
    }
    
    /**
     * Change the language code and reload words.
     */
    open fun changeLanguage(newLanguageCode: String) {
        if (languageCode != newLanguageCode) {
            loadWordsForReview(newLanguageCode)
        }
    }
}