package git.pef.mendelu.cz.booknest.communication

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GoogleBooksResponse(
    val items: List<GoogleBookItem>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class GoogleBookItem(
    val id: String,
    val volumeInfo: VolumeInfo
)

@JsonClass(generateAdapter = true)
data class VolumeInfo(
    val title: String? = null,
    val subtitle: String? = null,
    val authors: List<String>? = emptyList(),
    val description: String? = null,
    val imageLinks: ImageLinks? = null
)

@JsonClass(generateAdapter = true)
data class ImageLinks(
    val thumbnail: String? = null
)
