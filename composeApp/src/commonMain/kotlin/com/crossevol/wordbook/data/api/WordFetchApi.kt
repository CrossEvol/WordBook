package com.crossevol.wordbook.data.api
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.github.oshai.kotlinlogging.KotlinLogging
import com.crossevol.wordbook.data.model.WordFetchResultJson

private val logger = KotlinLogging.logger {}

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


/**
 * API client for fetching word details from the LLM.
 * Accepts a pre-configured HttpClient instance.
 */
class WordFetchApi(private val client: HttpClient) { // Accept HttpClient in constructor

    // Removed internal client creation

    /**
     * Fetches word details for a given query using a specific model and API key.
     *
     * @param query The word or phrase to fetch details for.
     * @param apiKey The API key to use for the request.
     * @param model The name of the LLM model to use (e.g., "gemini-1.5-flash-preview-04-17").
     * @return The parsed WordFetchResultJson object.
     * @throws Exception if the API call or parsing fails.
     */
    suspend fun fetchWordDetails(query: String, apiKey: String, model: String): WordFetchResultJson {
        if (apiKey.isBlank() || apiKey == "YOUR_API_KEY_HERE") {
             logger.error { "API Key is not configured." }
             throw IllegalStateException("API Key is not provided or is a placeholder.")
        }
        if (query.isBlank()) {
            logger.warn { "Attempted to fetch with empty query." }
            throw IllegalArgumentException("Search query cannot be empty.")
        }
        if (model.isBlank()) {
             logger.warn { "Attempted to fetch with empty model name." }
             throw IllegalArgumentException("Model name cannot be empty.")
        }


        logger.debug { "Fetching word details for query: '$query' using model '$model'." }

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

        // Construct the URL dynamically using the provided model name
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        try {
            val response: ApiResponse = client.post(url) { // Use the dynamic URL
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()

            // Extract the text containing the inner JSON
            val responseText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: run {
                    logger.error { "API response did not contain expected text content." }
                    throw Exception("API response did not contain expected text content.")
                }

            // Clean the text: remove ```json\n prefix and \n``` suffix
            val jsonString = responseText
                .removePrefix("```json\n")
                .removeSuffix("\n```")
                .trim() // Trim any extra whitespace

            logger.debug { "Received raw API response text:\n$responseText" }
            logger.debug { "Extracted JSON string:\n$jsonString" }

            // Parse the inner JSON string
            val result = Json.decodeFromString<WordFetchResultJson>(jsonString)
            logger.info { "Successfully parsed API response for query: '$query' using model '$model'." }
            return result

        } catch (e: Exception) {
            // Wrap specific Ktor exceptions or rethrow
            logger.error(e) { "API Fetch Error for query '$query' using model '$model': ${e.message}" }
            throw Exception("Failed to fetch word details: ${e.message}", e)
        }
    }
}
