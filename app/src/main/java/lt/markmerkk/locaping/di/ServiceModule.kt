package lt.markmerkk.locaping.di

import android.app.Application
import com.fasterxml.jackson.databind.ObjectMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lt.markmerkk.locaping.network.Api
import lt.markmerkk.locaping.network.UnauthorizedClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module(includes = [NetworkModule::class])
@InstallIn(SingletonComponent::class)
class ServiceModule {

    @Provides
    @Singleton
    fun provideApi(
        app: Application,
        objectMapper: ObjectMapper,
        @UnauthorizedClient retrofit: Retrofit,
    ): Api {
        return retrofit.create(Api::class.java)
    }
}
