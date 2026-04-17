package git.pef.mendelu.cz.booknest.communication

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object BookApiProvider {
    fun create(): BookApi {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/books/v1/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(OkHttpClient.Builder().build())
            .build()

        return retrofit.create(BookApi::class.java)
    }
}
