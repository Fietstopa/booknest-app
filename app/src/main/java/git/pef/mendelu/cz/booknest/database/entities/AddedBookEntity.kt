package git.pef.mendelu.cz.booknest.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "added_books")
data class AddedBookEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val authors: List<String>,
    val thumbnail: String?,
    val libraryId: String?,
    val addedByUid: String?,
    val createdAtMillis: Long?
)
