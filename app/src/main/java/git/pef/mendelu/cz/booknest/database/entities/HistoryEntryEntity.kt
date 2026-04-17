package git.pef.mendelu.cz.booknest.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_entries")
data class HistoryEntryEntity(
    @PrimaryKey
    val id: String,
    val type: String,
    val bookId: String?,
    val bookTitle: String?,
    val libraryId: String?,
    val libraryName: String?,
    val createdAtMillis: Long?
)
