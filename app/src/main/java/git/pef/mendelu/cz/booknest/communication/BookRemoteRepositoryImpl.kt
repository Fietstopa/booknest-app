package git.pef.mendelu.cz.booknest.communication

import javax.inject.Inject

class BookRemoteRepositoryImpl @Inject constructor(
    private val api: BookApi
) : IBooksRemoteRepository {

    override suspend fun searchBooks(query: String): CommunicationResult<GoogleBooksResponse> {
        return processResponse {
            api.searchBooks(query = query)
        }
    }

    override suspend fun getBookById(id: String): CommunicationResult<GoogleBookItem> {
        return processResponse {
            api.getBookById(id = id)
        }
    }
}
