package git.pef.mendelu.cz.booknest.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_books")
data class PendingBookEntity(
    @PrimaryKey
    val localId: String,
    val libraryId: String,
    val bookId: String,
    val title: String,
    val authors: List<String>,
    val thumbnail: String?,
    val addedByUid: String?,
    val addedByName: String?,
    val createdAtMillis: Long
)
