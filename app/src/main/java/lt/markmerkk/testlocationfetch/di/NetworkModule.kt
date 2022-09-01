package lt.markmerkk.testlocationfetch.di

import com.fasterxml.jackson.databind.ObjectMapper
import com.skydoves.sandwich.interceptors.EmptyBodyInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lt.markmerkk.testlocationfetch.BuildConfigData
import lt.markmerkk.testlocationfetch.network.RetrofitFactory
import lt.markmerkk.testlocationfetch.network.UnauthorizedClient
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    fun provideClientBuilder(
        objectMapper: ObjectMapper,
        buildConfigData: BuildConfigData
    ): OkHttpClient.Builder {
        val clientBuilder = RetrofitFactory
            .createUnauthorizedClientBuilder(isDebug = buildConfigData.isDebug)
        return clientBuilder
    }

    @UnauthorizedClient
    @Provides
    fun provideOkHttpClient(
        clientBuilder: OkHttpClient.Builder,
    ): OkHttpClient {
        return clientBuilder
            .addInterceptor(EmptyBodyInterceptor)
            .build()
    }

    @UnauthorizedClient
    @Provides
    fun provideRetrofit(
        @UnauthorizedClient okHttpClient: OkHttpClient,
        objectMapper: ObjectMapper,
        buildConfigData: BuildConfigData,
    ): Retrofit {
        return RetrofitFactory.createRetrofit(
            objectMapper,
            okHttpClient,
            buildConfigData.baseUrl,
        )
    }
}
