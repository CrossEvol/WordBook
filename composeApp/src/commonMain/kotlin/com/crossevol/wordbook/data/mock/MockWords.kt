package com.crossevol.wordbook.data.mock

import com.crossevol.wordbook.data.model.WordItemUI

// Update sample data to use UiWordItem
val sampleWordItem = WordItemUI( // Changed from WordItem
    id = 1L,
    title = "Hello World",
    explanation = "Say welcome to the world.",
    pronunciation = "[ Nǐ hǎo shìjiè ]",
    rating = 3,
    sentences = listOf(
        "The bright sun shines over the vast ocean, casting a golden glow on the waves today.",
        "The bright sun shines over the vast ocean, casting a golden glow on the waves today.",
    ),
    relatedWords = listOf(
        "welcome",
        "greeting"
    )
)

// Sample data for the home page list (Manually extracted from JSONs)
val sampleWordListEN = listOf(
    WordItemUI(
        id = 1L,
        title = "同人志二次創作", // Original text as title
        explanation = "Fan-created doujinshi or secondary creation doujinshi; refers to self-published works (often comics or novels) that are creative adaptations or extensions of existing intellectual property (manga, anime, games, etc.), typically produced by fans.",
        sentences = listOf(
            "I found a fascinating fan-created doujinshi at the convention.",
            "Many artists specialize in doujinshi secondary creation based on popular series."
        ),
        relatedWords = listOf(
            "doujinshi",
            "fan work",
            "fan fiction",
            "self-publishing",
            "circle"
        ),
        pronunciation = "/doʊˈdʒɪnʃi ˌsɛkənˌdɛri kriˈeɪʃən/ (Concept pronunciation)",
        rating = 3L // Sample rating 0-5
    ),
    WordItemUI(
        id = 2L,
        title = "轻小说", // Original text as title
        explanation = "A style of Japanese novel primarily targeting young adults, often featuring illustrations and a simpler writing style.",
        sentences = listOf(
            "I enjoy reading light novels in my spare time.",
            "Many popular anime series are adapted from light novels."
        ),
        relatedWords = listOf(
            "manga",
            "anime",
            "novel",
            "young adult fiction",
            "genre fiction"
        ),
        pronunciation = "/laɪt ˈnɒvəl/",
        rating = 5L // Sample rating 0-5
    ),
    WordItemUI(
        id = 3L,
        title = "视觉小说", // Original text as title
        explanation = "An interactive fiction video game genre, primarily text-based with static or animated visuals, often featuring branching narratives.",
        sentences = listOf(
            "Visual novels are known for their strong focus on story and characters.",
            "I spent hours playing that new visual novel with multiple endings."
        ),
        relatedWords = listOf(
            "interactive fiction",
            "dating sim",
            "adventure game",
            "story game",
            "VN"
        ),
        pronunciation = "/ˈvɪʒuəl ˈnɒvəl/",
        rating = 1L // Sample rating 0-5
    )
)

val sampleWordListJA = listOf(
    WordItemUI(
        id = 1L,
        title = "同人志二次創作", // Original text as title
        explanation = "既存の作品を基にファンが制作し、同人誌の形式で発表される二次創作物のこと。漫画、小説、イラスト集など。",
        sentences = listOf(
            "彼女は同人誌二次創作で有名なサークルに所属しています。",
            "このイベントでは様々なジャンルの同人誌二次創作が頒布されています。"
        ),
        relatedWords = listOf(
            "同人誌",
            "二次創作",
            "同人",
            "サークル",
            "ファンアート"
        ),
        pronunciation = "どうじんし にじそうさく (dōjinshi nijisōsaku)",
        rating = 3L // Same sample rating
    ),
    WordItemUI(
        id = 2L,
        title = "轻小说", // Original text as title
        explanation = "イラストを多用し、主に中高生を対象とした小説のジャンル。",
        sentences = listOf(
            "週末は家でライトノベルを読んで過ごすのが好きです。",
            "そのアニメは有名なライトノベルシリーズが原作です。"
        ),
        relatedWords = listOf(
            "漫画",
            "アニメ",
            "小説",
            "ラノベ",
            "文芸"
        ),
        pronunciation = "ライトノベル (raito noberu)",
        rating = 5L // Same sample rating
    ),
    WordItemUI(
        id = 3L,
        title = "视觉小说", // Original text as title
        explanation = "静止画や動画、テキストを用いてストーリーを進める形式のゲームジャンル。プレイヤーの選択によって展開が変化するものが多い。",
        sentences = listOf(
            "最近、感動的なビジュアルノベルをプレイして泣きました。",
            "このビジュアルノベルは選択肢が多くて面白いです。"
        ),
        relatedWords = listOf(
            "ノベルゲーム",
            "アドベンチャーゲーム",
            "ゲーム",
            "インタラクティブフィクション",
            "AVG"
        ),
        pronunciation = "ビジュアルノベル (bijuaru noberu)",
        rating = 1L // Same sample rating
    )
)

val sampleWordListZH = listOf(
    WordItemUI(
        id = 1L,
        title = "同人志二次創作", // Original text as title
        explanation = "基于现有作品（如动漫、游戏）进行创作并以同人志形式出版或分享的作品。特指粉丝创作的、非官方出版的二次创作物。",
        sentences = listOf(
            "他创作了很多优秀的同人志二次创作作品。",
            "这本漫展上有很多关于热门IP的同人志二次创作。"
        ),
        relatedWords = listOf(
            "动漫",
            "漫画",
            "小说",
            "二次元",
            "文学"
        ),
        pronunciation = "tóngrénzhì èrcì chuàngzuò",
        rating = 3L // Same sample rating
    ),
    WordItemUI(
        id = 2L,
        title = "轻小说", // Original text as title
        explanation = "一种源自日本的小说类型，主要面向青少年读者，常包含插图且文风相对轻松。",
        sentences = listOf(
            "我喜欢在空闲时间读轻小说。",
            "很多受欢迎的动漫都是由轻小说改编的。",
            "另一个例子。"
        ), // Added another example sentence
        relatedWords = listOf(
            "动漫",
            "漫画",
            "小说",
            "二次元",
            "文学"
        ),
        pronunciation = "qīng xiǎo shuō",
        rating = 5L // Sample rating 0-5
    ),
    WordItemUI(
        id = 3L,
        title = "视觉小说", // Original text as title
        explanation = "一种电子游戏类型，以文字叙述为主要表现形式，并搭配大量静态或动态图像，玩家通过选项影响剧情走向。",
        sentences = listOf(
            "我喜欢玩剧情深度很高的视觉小说。",
            "这部视觉小说有精美的画面和动听的配乐。"
        ),
        relatedWords = listOf(
            "文字冒险游戏",
            "电子游戏",
            "互动小说",
            "AVG",
            "galgame"
        ),
        pronunciation = "shì jué xiǎo shuō",
        rating = 1L // Sample rating 0-5
    )
)