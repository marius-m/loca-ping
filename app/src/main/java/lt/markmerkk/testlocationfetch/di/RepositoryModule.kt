package lt.markmerkk.testlocationfetch.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lt.markmerkk.testlocationfetch.network.Api
import lt.markmerkk.testlocationfetch.network.NetworkErrorHandler
import lt.markmerkk.testlocationfetch.repositories.HomeRepository
import javax.inject.Singleton

@Module(includes = [NetworkModule::class, ServiceModule::class])
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    @Singleton
    fun provideHomeRepository(
        networkErrorHandler: NetworkErrorHandler,
        api: Api,
    ): HomeRepository {
        return HomeRepository(networkErrorHandler, api)
    }
}