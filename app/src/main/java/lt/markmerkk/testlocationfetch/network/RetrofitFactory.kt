package lt.markmerkk.testlocationfetch.network

import com.fasterxml.jackson.databind.ObjectMapper
import com.skydoves.sandwich.coroutines.CoroutinesResponseCallAdapterFactory
import lt.markmerkk.testlocationfetch.Tags
import java.util.concurrent.TimeUnit
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import timber.log.Timber

object RetrofitFactory {

    private const val MAX_IDLE_CONNECTIONS = 0
    private const val KEEP_ALIVE_DURATION_NANOSECONDS = 1L

    fun createUnauthorizedClientBuilder(
        isDebug: Boolean
    ): OkHttpClient.Builder {
        val clientBuilder = OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .connectionPool(
                ConnectionPool(
                    MAX_IDLE_CONNECTIONS,
                    KEEP_ALIVE_DURATION_NANOSECONDS,
                    TimeUnit.NANOSECONDS
                )
            )
        if (isDebug) {
            val loggingInterceptor = HttpLoggingInterceptor { message ->
                Timber.tag(Tags.NETWORK).i(message)
            }
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            clientBuilder.addInterceptor(loggingInterceptor)
        }
        return clientBuilder
    }

    fun createRetrofit(
        objectMapper: ObjectMapper,
        okHttpClient: OkHttpClient,
        url: String
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .addCallAdapterFactory(CoroutinesResponseCallAdapterFactory.create())
            .client(okHttpClient)
            .build()
    }
}
