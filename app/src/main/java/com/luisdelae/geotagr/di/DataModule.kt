package com.luisdelae.geotagr.di

import android.content.Context
import com.luisdelae.geotagr.GeoTagrApplication
import com.luisdelae.geotagr.data.LocationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {

    @Provides
    @Singleton
    fun provideLocationManager(
        @ApplicationContext context: Context
    ): LocationManager {
        return LocationManager(context, (context.applicationContext as GeoTagrApplication).applicationScope)
    }
}