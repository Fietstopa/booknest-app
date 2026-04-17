package git.pef.mendelu.cz.booknest.ui.screens.MapScreen.components.bottomsheet

import git.pef.mendelu.cz.booknest.communication.GoogleBookItem

internal data class MapBottomSheetUiState(
    val city: String? = null,
    val street: String? = null,
    val isAddressLoading: Boolean = false,
    val searchResults: List<GoogleBookItem> = emptyList(),
    val isSearching: Boolean = false,
    val searchError: String? = null,
    val saveError: String? = null,
    val saveSuccess: Boolean = false,
    val comments: List<LibraryComment> = emptyList(),
    val commentText: String = "",
    val isCommentSending: Boolean = false,
    val commentError: String? = null,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val likeError: String? = null
)

internal data class LibraryComment(
    val id: String,
    val text: String,
    val authorName: String,
    val authorUid: String?,
    val createdAtMillis: Long?
)
