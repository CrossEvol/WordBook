-- Table to store the core word entry and its general metadata
CREATE TABLE words (
    id INTEGER PRIMARY KEY AUTOINCREMENT, -- Unique identifier for the word
    text TEXT NOT NULL UNIQUE,            -- The word or phrase being learned
    create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL -- When the word was added
);

-- Optional: Index on text for faster lookup by word
CREATE INDEX idx_words_text ON words(text);

-- Table to store language-specific details for each word
CREATE TABLE word_details (
    id INTEGER PRIMARY KEY AUTOINCREMENT,   -- Unique identifier for the detail entry
    word_id INTEGER NOT NULL,               -- Foreign key linking to the words table
    language_code VARCHAR(5) NOT NULL,      -- Language identifier (e.g., 'en', 'ja', 'zh')

    -- Language-specific content
    explanation TEXT,                       -- Translation/definition in this language
    sentences TEXT,                         -- Example sentences (consider JSON or separate table if complex)
    related_words TEXT,                     -- Related words/phrases (consider JSON or separate table if complex)
    pronunciation VARCHAR(255),            -- Pronunciation information

    -- Review progress for this specific language detail
    last_review_at TIMESTAMP,               -- When this specific language detail was last reviewed
    review_progress INTEGER DEFAULT 0 NOT NULL, -- Progress metric (e.g., level in spaced repetition)

    -- Constraints
    FOREIGN KEY (word_id) REFERENCES words(id) ON DELETE CASCADE, -- If a word is deleted, delete its details
    UNIQUE (word_id, language_code)         -- Ensure only one detail entry per word per language
);

-- Optional: Indexes for faster joins and lookups
CREATE INDEX idx_word_details_word_id ON word_details(word_id);
CREATE INDEX idx_word_details_language_code ON word_details(language_code);
CREATE INDEX idx_word_details_review ON word_details(last_review_at, review_progress); -- Useful for querying review items

-- Table to store API key configurations submitted from the ApiKeyEditingPage
CREATE TABLE api_key_configurations (
    id INTEGER PRIMARY KEY AUTOINCREMENT, -- Unique identifier for the API key configuration
    alias TEXT NOT NULL UNIQUE,           -- User-friendly name for the key configuration
    apiKey TEXT NOT NULL,                 -- The actual API key string (sensitive data)
    provider TEXT NOT NULL CHECK (provider IN ('Google', 'Anthropic', 'OpenAI', 'Mistral AI', 'Meta', 'Cohere', 'DeepSeek', 'AI21 Labs', 'Perplexity')), -- The LLM provider name (constrained to known providers)
    model TEXT NOT NULL                   -- The specific model name (e.g., 'gemini-1.5-pro', 'gpt-4o')
);

-- Optional: Index on alias for faster lookup by alias
CREATE INDEX idx_api_key_configurations_alias ON api_key_configurations(alias);
