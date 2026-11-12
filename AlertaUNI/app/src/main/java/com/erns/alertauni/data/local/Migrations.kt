package com.erns.alertauni.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** v2 -> v3: agrega borradores y estado local de anuncios sin tocar las notificaciones guardadas. */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `announcement_drafts` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`ownerUid` TEXT NOT NULL, " +
                    "`mode` TEXT NOT NULL, " +
                    "`courseCatalogId` TEXT, " +
                    "`courseCode` TEXT NOT NULL, " +
                    "`recipientStudentId` TEXT, " +
                    "`recipientName` TEXT, " +
                    "`priority` TEXT NOT NULL, " +
                    "`title` TEXT NOT NULL, " +
                    "`content` TEXT NOT NULL, " +
                    "`allowComments` INTEGER NOT NULL, " +
                    "`updatedAt` INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_announcement_drafts_ownerUid` " +
                    "ON `announcement_drafts` (`ownerUid`)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `post_local_state` (" +
                    "`ownerUid` TEXT NOT NULL, " +
                    "`postId` INTEGER NOT NULL, " +
                    "`priority` TEXT, " +
                    "`archived` INTEGER NOT NULL, " +
                    "`commentsEnabled` INTEGER, " +
                    "PRIMARY KEY(`ownerUid`, `postId`))"
        )
    }
}
