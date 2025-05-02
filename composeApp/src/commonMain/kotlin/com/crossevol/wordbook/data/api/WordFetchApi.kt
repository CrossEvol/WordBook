package com.crossevol.wordbook.data.api
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.SerialName
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.github.oshai.kotlinlogging.KotlinLogging // Import KotlinLogging
import io.ktor.client.plugins.HttpTimeout

private val logger = KotlinLogging.logger {} // Add logger instance

// --- Data classes for API Request Body (based on solicit.ps1) ---

@Serializable
data class ApiRequestBody(
    val contents: List<Content>
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String
)

// --- Data classes for API Response (based on solicit-response.json) ---

@Serializable
data class ApiResponse(
    val candidates: List<Candidate>,
    // Add other fields like usageMetadata, modelVersion if needed
)

@Serializable
data class Candidate(
    val content: Content, // Reusing Content structure
    // Add other fields like finishReason, index if needed
)

// --- Data class for the INNER JSON string extracted from the response ---

@Serializable
data class WordFetchResultJson(
    val text: String,
    @SerialName("en_explanation") val enExplanation: String,
    @SerialName("en_sentences") val enSentences: String, // Sentences are semicolon-separated
    @SerialName("en_related_words") val enRelatedWords: String, // Words are semicolon-separated
    @SerialName("en_pronunciation") val enPronunciation: String,
    @SerialName("ja_explanation") val jaExplanation: String,
    @SerialName("ja_sentences") val jaSentences: String,
    @SerialName("ja_related_words") val jaRelatedWords: String,
    @SerialName("ja_pronunciation") val jaPronunciation: String,
    @SerialName("zh_explanation") val zhExplanation: String,
    @SerialName("zh_sentences") val zhSentences: String,
    @SerialName("zh_related_words") val zhRelatedWords: String,
    @SerialName("zh_pronunciation") val zhPronunciation: String,
) {
    // Helper function to split semicolon-separated strings into lists
    fun getEnSentencesList(): List<String> = enSentences.split(";").filter { it.isNotBlank() }
    fun getJaSentencesList(): List<String> = jaSentences.split(";").filter { it.isNotBlank() }
    fun getZhSentencesList(): List<String> = zhSentences.split(";").filter { it.isNotBlank() }

    fun getEnRelatedWordsList(): List<String> = enRelatedWords.split(";").filter { it.isNotBlank() }
    fun getJaRelatedWordsList(): List<String> = jaRelatedWords.split(";").filter { it.isNotBlank() }
    fun getZhRelatedWordsList(): List<String> = zhRelatedWords.split(";").filter { it.isNotBlank() }
}


/**
 * API client for fetching word details from the LLM.
 */
class WordFetchApi() { // Removed apiKey from constructor

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true // Ignore extra fields in the response if any
                prettyPrint = true
                isLenient = true // Be lenient with JSON parsing
            })
        }
        // Add HttpTimeout plugin
        install(HttpTimeout) {
            requestTimeoutMillis = 10000 // Set request timeout to 10 seconds
            // You can also set connectTimeoutMillis and socketTimeoutMillis if needed
        }
        // Optional: Add logging plugin for debugging
        // install(Logging) {
        //     logger = Logger.DEFAULT
        //     level = LogLevel.INFO
        // }
    }

    // Base URL for the API (model and key are part of the URL in the PS script)
    // In a real app, you might want to make the model configurable.
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-04-17:generateContent"

    /**
     * Fetches word details for a given query.
     *
     * @param query The word or phrase to fetch details for.
     * @param apiKey The API key to use for the request.
     * @return The parsed WordFetchResultJson object.
     * @throws Exception if the API call or parsing fails.
     */
    suspend fun fetchWordDetails(query: String, apiKey: String): WordFetchResultJson { // Added apiKey parameter
        if (apiKey.isBlank() || apiKey == "YOUR_API_KEY_HERE") {
             logger.error { "API Key is not configured." } // Replaced println
             throw IllegalStateException("API Key is not provided or is a placeholder.")
        }
        if (query.isBlank()) {
            logger.warn { "Attempted to fetch with empty query." } // Replaced println
            throw IllegalArgumentException("Search query cannot be empty.")
        }

        logger.debug { "Fetching word details for query: '$query' from API." } // Replaced println

        // Construct the prompt text including the instructions and the query
        // This should match the structure in solicit.ps1
        val promptText = """
            You are a multilingual translation and linguistic analysis AI. Your task is to process an input word or phrase, which can be in English, Japanese, or Chinese, and generate a detailed JSON output containing its translations, explanations, example sentences, related words/phrases, and pronunciation guides for all three languages: English (en), Japanese (ja), and Chinese (zh).

            **Input:**
            You will receive a single word or short phrase in plain text. This input could be in English, Japanese, or Chinese.

            **Output Requirements:**
            Your response MUST be a single, valid JSON object and nothing else (no introductory text, no explanations outside the JSON). The JSON object must strictly adhere to the following structure:

            ```json
            {
            	"text": "ORIGINAL_INPUT_TEXT",
            	"en_explanation": "CONCISE_ENGLISH_EXPLANATION_OF_THE_WORD/PHRASE",
            	"en_sentences": "FIRST_ENGLISH_SENTENCE_USING_WORD;SECOND_ENGLISH_SENTENCE_USING_WORD",
            	"en_related_words": "ENGLISH_RELATED_WORD1;ENGLISH_RELATED_WORD2;ENGLISH_RELATED_WORD3",
            	"en_pronunciation": "ENGLISH_PRONUNCIATION_GUIDE_(IPA_PREFERRED)",
            	"ja_explanation": "CONCISE_JAPANESE_EXPLANATION_OF_THE_WORD/PHRASE",
            	"ja_sentences": "FIRST_JAPANESE_SENTENCE_USING_WORD;SECOND_JAPANESE_SENTENCE_USING_WORD",
            	"ja_related_words": "JAPANESE_RELATED_WORD1;JAPANESE_RELATED_WORD2;JAPANESE_RELATED_WORD3",
            	"ja_pronunciation": "JAPANESE_PRONUNCIATION_GUIDE_(ROMAJI_AND/OR_KANA)",
            	"zh_explanation": "CONCISE_CHINESE_EXPLANATION_OF_THE_WORD/PHRASE",
            	"zh_sentences": "FIRST_CHINESE_SENTENCE_USING_WORD;SECOND_CHINESE_SENTENCE_USING_WORD",
            	"zh_related_words": "CHINESE_RELATED_WORD1;CHINESE_RELATED_WORD2;CHINESE_RELATED_WORD3",
            	"zh_pronunciation": "CHINESE_PRONUNCIATION_GUIDE_(PINYIN_WITH_TONE_MARKS)"
            }

             Now Input is $query
        """.trimIndent() // Use trimIndent to remove leading whitespace

        val requestBody = ApiRequestBody(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = promptText)
                    )
                )
            )
        )

        try {
            val response: ApiResponse = client.post("$baseUrl?key=$apiKey") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()

            // Extract the text containing the inner JSON
            val responseText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: run {
                    logger.error { "API response did not contain expected text content." } // Replaced println
                    throw Exception("API response did not contain expected text content.")
                }

            // Clean the text: remove ```json\n prefix and \n``` suffix
            val jsonString = responseText
                .removePrefix("```json\n")
                .removeSuffix("\n```")
                .trim() // Trim any extra whitespace

            logger.debug { "Received raw API response text:\n$responseText" } // Replaced println
            logger.debug { "Extracted JSON string:\n$jsonString" } // Replaced println

            // Parse the inner JSON string
            val result = Json.decodeFromString<WordFetchResultJson>(jsonString)
            logger.info { "Successfully parsed API response for query: '$query'" } // Replaced println
            return result

        } catch (e: Exception) {
            // Wrap specific Ktor exceptions or rethrow
            logger.error(e) { "API Fetch Error for query '$query': ${e.message}" } // Replaced println, added exception
            throw Exception("Failed to fetch word details: ${e.message}", e)
        }
    }
}
