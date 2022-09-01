package lt.markmerkk.locaping.di

import android.content.Context
import com.danielceinos.cooper.CooperInterceptor
import com.fasterxml.jackson.databind.ObjectMapper
import com.skydoves.sandwich.interceptors.EmptyBodyInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lt.markmerkk.locaping.App
import lt.markmerkk.locaping.BuildConfigData
import lt.markmerkk.locaping.network.NetworkErrorHandler
import lt.markmerkk.locaping.network.RetrofitFactory
import lt.markmerkk.locaping.network.UnauthorizedClient
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    fun provideClientBuilder(
        app: App,
        objectMapper: ObjectMapper,
        buildConfigData: BuildConfigData
    ): OkHttpClient.Builder {
        val clientBuilder = RetrofitFactory
            .createUnauthorizedClientBuilder(isDebug = buildConfigData.isDebug)
            .addInterceptor(CooperInterceptor(context = app.applicationContext))
        return clientBuilder
    }

    @Provides
    fun provideNetworkErrorHandler(
        objectMapper: ObjectMapper,
    ): NetworkErrorHandler {
        return NetworkErrorHandler(objectMapper = objectMapper)
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
