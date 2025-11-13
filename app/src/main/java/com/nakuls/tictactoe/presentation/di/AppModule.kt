package com.nakuls.tictactoe.presentation.di

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
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.WebSockets
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val appModule = module {

    single { ApiConfig.httpClient }

    // --- ViewModels ---
    viewModel { SplashViewModel(userRepository = get()) }
    viewModel { ProfileViewModel(userRepository = get()) }
    viewModel { HomeViewModel(gameRepository = get()) }
    //viewModel { GameViewModel(/* add dependencies here */) }

    // --- Use Cases (from domain layer) ---
    // factory { GetUserProfileUseCase(get()) }
    // factory { CreateUserProfileUseCase(get()) }

    single<UserProfileAPI> {
        UserProfileRemoteDataSourceImpl(
            client = get(),
            auth_key = getProperty("AUTH_KEY"),
            apiKey = getProperty("API_KEY")
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
            context = get(),
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