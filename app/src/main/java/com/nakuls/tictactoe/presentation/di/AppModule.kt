package com.nakuls.tictactoe.presentation.di

import com.nakuls.tictactoe.data.UserRepositoryImpl
import com.nakuls.tictactoe.domain.repository.UserRepository
import com.nakuls.tictactoe.presentation.screens.splash.SplashViewModel
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModel

val appModule = module {

    // --- ViewModels ---
    viewModel { SplashViewModel(userRepository = get()) }
    //viewModel { ProfileViewModel(/* add dependencies here */) }
    //viewModel { HomeViewModel(/* add dependencies here */) }
    //viewModel { GameViewModel(/* add dependencies here */) }

    // --- Use Cases (from domain layer) ---
    // factory { GetUserProfileUseCase(get()) }
    // factory { CreateUserProfileUseCase(get()) }

    // --- Repositories (from data layer) ---
     single<UserRepository> { UserRepositoryImpl(get()) }
    // single<GameRepository> { GameRepositoryImpl(get()) }

    // --- Data Sources (from data layer) ---
    // single { createKtorClient() } // Ktor client
    // single { createDataStore(androidContext()) } // DataStore
}