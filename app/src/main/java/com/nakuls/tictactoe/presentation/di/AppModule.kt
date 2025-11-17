package com.nakuls.tictactoe.presentation.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.nakuls.tictactoe.data.local.UserProfileLocal
import com.nakuls.tictactoe.data.local.UserProfileLocalDataSourceImpl
import com.nakuls.tictactoe.data.remote.ApiConfig
import com.nakuls.tictactoe.data.remote.GameAPI
import com.nakuls.tictactoe.data.remote.GameRemoteDataSourceImpl
import com.nakuls.tictactoe.data.remote.UserProfileAPI
import com.nakuls.tictactoe.data.remote.UserProfileRemoteDataSourceImpl
import com.nakuls.tictactoe.data.repository.GameRepositoryImpl
import com.nakuls.tictactoe.data.repository.UserRepositoryImpl
import com.nakuls.tictactoe.domain.repository.GameRepository
import com.nakuls.tictactoe.domain.repository.UserRepository
import com.nakuls.tictactoe.presentation.screens.home.HomeViewModel
import com.nakuls.tictactoe.presentation.screens.profile.ProfileViewModel
import com.nakuls.tictactoe.presentation.screens.splash.SplashViewModel
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val Context.userPreferencesDataStore: DataStore<Preferences> by
preferencesDataStore(name = "user_preferences")
val appModule = module {

    single<DataStore<Preferences>> {
        androidContext().userPreferencesDataStore
    }

    single { ApiConfig.httpClient }

    // --- ViewModels ---
    viewModel { SplashViewModel(userRepository = get()) }
    viewModel { ProfileViewModel(userRepository = get()) }
    viewModel { HomeViewModel(
        gameRepository = get(),
        get()
    ) }
    //viewModel { GameViewModel(/* add dependencies here */) }

    // --- Use Cases (from domain layer) ---
    // factory { GetUserProfileUseCase(get()) }
    // factory { CreateUserProfileUseCase(get()) }

    single<UserProfileAPI> {
        UserProfileRemoteDataSourceImpl(
            supabaseClient = get()
        )
    }

    single {
        createSupabaseClient(
            supabaseUrl = getProperty("SUPABASE_URL"),
            supabaseKey = getProperty("API_KEY")
        ) {
            install(Postgrest)
            install(Realtime)
        }
    }


    single<GameAPI> {
        GameRemoteDataSourceImpl(
            supabaseClient = get()
        )
    }

    single<UserProfileLocal> {
        UserProfileLocalDataSourceImpl(
            dataStore = get(),
        )
    }

    // --- Repositories (from data layer) ---
     single<UserRepository> {
         UserRepositoryImpl(
            get(),
            remoteSource = get()
     ) }
    single<GameRepository> {
        GameRepositoryImpl(
            get()
    ) }

    // --- Data Sources (from data layer) ---
    // single { createKtorClient() } // Ktor client
    // single { createDataStore(androidContext()) } // DataStore
}