package git.pef.mendelu.cz.booknest.communication

data class CommunicationError(
    val code: Int,
    val message: String? = null
)
