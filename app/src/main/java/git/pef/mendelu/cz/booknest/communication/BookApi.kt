package git.pef.mendelu.cz.booknest.communication

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface BookApi {
    @Headers("Content-Type: application/json")
    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int = 10
    ): Response<GoogleBooksResponse>

    @Headers("Content-Type: application/json")
    @GET("volumes/{id}")
    suspend fun getBookById(
        @Path("id") id: String
    ): Response<GoogleBookItem>
}
