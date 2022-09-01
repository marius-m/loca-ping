package lt.markmerkk.locaping.di

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lt.markmerkk.locaping.AppTimeProvider
import lt.markmerkk.locaping.AppTimeProviderAndroid
import lt.markmerkk.locaping.BuildConfig
import lt.markmerkk.locaping.BuildConfigData
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideTimeProvider(): AppTimeProvider {
        return AppTimeProviderAndroid()
    }

    @Provides
    @Singleton
    fun provideObjectMapper(): ObjectMapper {
        return ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
            .registerKotlinModule()
    }

    @Provides
    @Singleton
    fun provideBuildConfigData(): BuildConfigData {
        return BuildConfigData(
            applicationId = BuildConfig.APPLICATION_ID,
            isDebug = BuildConfig.DEBUG,
            baseUrl = "http://app.marius-m.lt",
            versionName = BuildConfig.VERSION_NAME,
            versionCode = BuildConfig.VERSION_CODE
        )
    }
}
