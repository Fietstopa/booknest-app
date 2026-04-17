package git.pef.mendelu.cz.booknest.communication

interface IBooksRemoteRepository : IBaseRemoteRepository {

    suspend fun searchBooks(query: String): CommunicationResult<GoogleBooksResponse>
    suspend fun getBookById(id: String): CommunicationResult<GoogleBookItem>
}
