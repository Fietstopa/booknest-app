package git.pef.mendelu.cz.booknest.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nearby_libraries")
data class NearbyLibraryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val size: String,
    val imageUrl: String?,
    val imageLocalPath: String?,
    val createdByName: String?,
    val distanceMeters: Double,
    val updatedAtMillis: Long
)
