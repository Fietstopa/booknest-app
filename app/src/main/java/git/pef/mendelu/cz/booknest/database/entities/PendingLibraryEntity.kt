package git.pef.mendelu.cz.booknest.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_libraries")
data class PendingLibraryEntity(
    @PrimaryKey
    val localId: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val size: String,
    val imageLocalPath: String,
    val createdByUid: String?,
    val createdByName: String?,
    val createdAtMillis: Long
)
