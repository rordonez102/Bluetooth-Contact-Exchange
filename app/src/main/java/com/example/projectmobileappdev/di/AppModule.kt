package com.example.projectmobileappdev.di

import android.content.Context
import com.example.projectmobileappdev.data.chat.AndroidBluetoothController
import com.example.projectmobileappdev.data.chat.PhoneNumberHelper
import com.example.projectmobileappdev.domain.chat.BluetoothController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideBluetoothController(@ApplicationContext context: Context): BluetoothController {
        return AndroidBluetoothController(context)
    }

    @Provides
    @Singleton
    fun providePhoneNumberHelper(@ApplicationContext context: Context): PhoneNumberHelper {
        return PhoneNumberHelper(context)
    }
}