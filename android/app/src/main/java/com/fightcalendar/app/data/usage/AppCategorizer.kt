package com.fightcalendar.app.data.usage

import com.fightcalendar.app.domain.model.Category
import javax.inject.Inject
import javax.inject.Singleton

/**
 * アプリのカテゴリ自動分類を行うクラス
 * パッケージ名とアプリ名から適切なカテゴリを推定する
 */
@Singleton
class AppCategorizer @Inject constructor() {

    /**
     * 仕事関連のキーワード
     */
    private val workKeywords = setOf(
        // 一般的なオフィスアプリ
        "office", "word", "excel", "powerpoint", "outlook", "teams", "slack",
        "zoom", "webex", "meet", "calendar", "mail", "email", "gmail",
        "drive", "dropbox", "onedrive", "box", "notion", "evernote",
        "trello", "asana", "jira", "confluence", "salesforce",
        
        // 開発・技術
        "android", "studio", "github", "gitlab", "bitbucket", "vscode",
        "terminal", "ssh", "putty", "docker", "aws", "azure", "gcp",
        
        // 日本企業アプリ
        "chatwork", "cybozu", "kintone", "line works", "teams",
        
        // パッケージ名パターン
        "microsoft", "google.android.gm", "com.slack", "zoom.us",
        "com.github", "atlassian"
    )

    /**
     * 学習関連のキーワード
     */
    private val studyKeywords = setOf(
        // 語学学習
        "duolingo", "rosetta", "babbel", "memrise", "anki", "quizlet",
        "english", "toeic", "toefl", "ielts", "cambridge",
        
        // オンライン学習
        "coursera", "udemy", "khan", "edx", "lynda", "pluralsight",
        "codecademy", "freecodecamp", "udacity",
        
        // 読書・学習
        "kindle", "book", "reader", "library", "wikipedia", "dictionary",
        "translator", "translate", "education", "learning", "study",
        
        // 日本の学習アプリ
        "studyplus", "zuknow", "polyglots", "iknow", "abceed",
        "schoo", "gacco", "jmooc",
        
        // 資格・試験
        "certification", "exam", "test", "quiz", "practice"
    )

    /**
     * 娯楽・SNS関連のキーワード
     */
    private val entertainmentKeywords = setOf(
        // SNS
        "facebook", "twitter", "instagram", "tiktok", "snapchat",
        "pinterest", "linkedin", "reddit", "discord", "telegram",
        "line", "whatsapp", "messenger", "wechat",
        
        // 動画・音楽
        "youtube", "netflix", "amazon.prime", "hulu", "disney",
        "spotify", "apple.music", "amazon.music", "tidal",
        "twitch", "niconico", "abema", "gyao",
        
        // ゲーム
        "game", "puzzle", "rpg", "strategy", "action", "adventure",
        "pokemon", "nintendo", "sony", "square.enix", "capcom",
        "minecraft", "fortnite", "pubg", "among.us",
        
        // 娯楽
        "entertainment", "fun", "social", "media", "photo", "camera",
        "dating", "match", "tinder", "bumble",
        
        // 日本の娯楽アプリ
        "rakuten.tv", "u.next", "dazn", "paravi", "fod"
    )

    /**
     * ツール関連のキーワード
     */
    private val toolKeywords = setOf(
        // システム・ユーティリティ
        "settings", "system", "launcher", "keyboard", "file", "manager",
        "browser", "chrome", "firefox", "safari", "edge", "opera",
        "calculator", "calendar", "clock", "alarm", "timer", "weather",
        "map", "navigation", "gps", "camera", "gallery", "photos",
        
        // 通信・接続
        "wifi", "bluetooth", "vpn", "network", "internet", "data",
        "phone", "call", "contacts", "sms", "message",
        
        // セキュリティ・バックアップ
        "antivirus", "security", "backup", "sync", "cleaner", "optimizer",
        "password", "authenticator", "2fa",
        
        // 開発・技術ツール
        "terminal", "console", "editor", "ide", "debugger", "compiler",
        
        // 日本のツールアプリ
        "yahoo", "rakuten", "amazon", "mercari", "paypay", "suica"
    )

    /**
     * パッケージ名とアプリ名からカテゴリを推定
     */
    fun assignCategory(packageName: String, appLabel: String): Category {
        val combined = "$packageName $appLabel".lowercase()
        
        // 各カテゴリのスコアを計算
        val workScore = calculateScore(combined, workKeywords)
        val studyScore = calculateScore(combined, studyKeywords)
        val entertainmentScore = calculateScore(combined, entertainmentKeywords)
        val toolsScore = calculateScore(combined, toolKeywords)
        
        // 最高スコアのカテゴリを返す
        val maxScore = maxOf(workScore, studyScore, entertainmentScore, toolsScore)
        
        return when (maxScore) {
            workScore -> Category.WORK
            studyScore -> Category.STUDY
            entertainmentScore -> Category.ENTERTAINMENT
            else -> Category.TOOLS
        }
    }

    /**
     * キーワードマッチングスコアを計算
     */
    private fun calculateScore(text: String, keywords: Set<String>): Int {
        var score = 0
        
        for (keyword in keywords) {
            if (text.contains(keyword)) {
                // 完全一致は高得点
                score += if (text == keyword) 10 else 5
                // 複数マッチでボーナス
                score += text.split(keyword).size - 1
            }
        }
        
        return score
    }

    /**
     * 手動でアプリのカテゴリを更新
     */
    suspend fun updateAppCategory(packageName: String, category: Category, appDao: com.fightcalendar.app.data.db.dao.AppDao) {
        appDao.updateAppCategory(packageName, category.id)
    }

    /**
     * カテゴリの説明を取得
     */
    fun getCategoryDescription(category: Category): String {
        return when (category) {
            Category.WORK -> "仕事・ビジネス関連のアプリ。オフィスアプリ、コミュニケーションツール、会議アプリなど。"
            Category.STUDY -> "学習・教育関連のアプリ。語学学習、オンライン講座、読書、資格試験対策など。"
            Category.ENTERTAINMENT -> "娯楽・SNS関連のアプリ。ソーシャルメディア、動画配信、音楽、ゲーム、写真など。"
            Category.TOOLS -> "ツール・ユーティリティアプリ。ブラウザ、カメラ、マップ、設定、システムアプリなど。"
        }
    }

    /**
     * 推定精度を向上させるためのサジェスト機能
     */
    fun getSuggestedCategory(packageName: String, appLabel: String): Pair<Category, Float> {
        val category = assignCategory(packageName, appLabel)
        val combined = "$packageName $appLabel".lowercase()
        
        // 信頼度を計算（0.0 - 1.0）
        val confidence = when (category) {
            Category.WORK -> calculateConfidence(combined, workKeywords)
            Category.STUDY -> calculateConfidence(combined, studyKeywords)
            Category.ENTERTAINMENT -> calculateConfidence(combined, entertainmentKeywords)
            Category.TOOLS -> calculateConfidence(combined, toolKeywords)
        }
        
        return category to confidence
    }

    /**
     * カテゴリ推定の信頼度を計算
     */
    private fun calculateConfidence(text: String, keywords: Set<String>): Float {
        val matches = keywords.count { text.contains(it) }
        return minOf(matches.toFloat() / 5f, 1f) // 最大5個のキーワードマッチで100%
    }
}