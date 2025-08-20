package com.fightcalendar.app.domain.model

/**
 * アプリカテゴリ（4カテゴリ固定）
 */
enum class Category(
    val id: String,
    val displayName: String,
    val color: String,
    val isProductive: Boolean
) {
    WORK("work", "仕事", "#2BB673", true),
    STUDY("study", "学習", "#2F6DF6", true),
    ENTERTAINMENT("entertainment", "娯楽・SNS", "#FF7A59", false),
    TOOLS("tools", "ツール", "#7E57C2", false);

    companion object {
        /**
         * ID文字列からカテゴリを取得
         */
        fun fromId(id: String): Category? = values().find { it.id == id }

        /**
         * 生産的なカテゴリのリストを取得
         */
        fun getProductiveCategories(): List<Category> = values().filter { it.isProductive }

        /**
         * 非生産的なカテゴリのリストを取得
         */
        fun getNonProductiveCategories(): List<Category> = values().filter { !it.isProductive }
    }
}