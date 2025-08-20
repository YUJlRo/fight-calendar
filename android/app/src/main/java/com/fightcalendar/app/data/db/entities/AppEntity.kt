package com.fightcalendar.app.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fightcalendar.app.domain.model.Category

/**
 * アプリ情報エンティティ
 */
@Entity(tableName = "apps")
data class AppEntity(
    @PrimaryKey
    val appId: String,
    val packageName: String,
    val label: String,
    val category: String, // Category.id
    val lastUsed: Long = 0L, // 最後に使用した時刻（ソート用）
    val totalUsageMs: Long = 0L, // 総使用時間（統計用）
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * カテゴリEnum を取得
     */
    fun getCategoryEnum(): Category = Category.fromId(category) ?: Category.TOOLS
}