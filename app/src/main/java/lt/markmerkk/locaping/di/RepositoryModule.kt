package lt.markmerkk.locaping.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lt.markmerkk.locaping.network.Api
import lt.markmerkk.locaping.network.NetworkErrorHandler
import lt.markmerkk.locaping.repositories.HomeRepository
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