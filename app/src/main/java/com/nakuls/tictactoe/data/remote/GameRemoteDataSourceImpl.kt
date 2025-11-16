package com.nakuls.tictactoe.data.remote

import com.nakuls.tictactoe.data.remote.dto.GameCreationDTO
import com.nakuls.tictactoe.data.remote.dto.GameDTO
import com.nakuls.tictactoe.data.remote.dto.toGame
import com.nakuls.tictactoe.domain.model.Game
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class GameRemoteDataSourceImpl(
    private val supabaseClient: SupabaseClient
): GameAPI {

    private val TABLENAME = "game"

    override suspend fun createGame(gameCreationDTO: GameCreationDTO): Boolean {
        return try {
            // Attempt the RPC call
            supabaseClient.postgrest.rpc(
                function = "create_game_and_player",
                parameters = gameCreationDTO
            )
            true
        } catch (e: RestException) {
            // Handle database-related errors (constraints, RLS, etc.)
            println("Game creation failed due to Postgrest error: ${e.message}")
            false // Return false to indicate failure
        } catch (e: Exception) {
            // Handle network or other unexpected errors (e.g., decoding failure)
            println("An unexpected error occurred during game creation: ${e.message}")
            false
        }
    }

    override suspend fun fetchJoinableGames(currentUserId: Int): Flow<List<Game>> = flow {

        val columnsWithName = Columns.list("id", "status",
            "length", "created_at", "edited_at",
            "created_by!inner(name)")
        val initialGameDTOs = supabaseClient.postgrest[TABLENAME]
            .select(columns = columnsWithName) {
                filter { eq("status", 0) }
                filter { neq("created_by", currentUserId) }
            }
            .decodeList<GameDTO>()

        val initialGames: List<Game> = initialGameDTOs.map { it.toGame() }

        // Emit the initial list
        emit(initialGames)

        // --- 2. Realtime Subscription (The continuous flow) ---
        // Create a separate flow that handles the channel lifecycle and emissions
        val realtimeFlow = callbackFlow<List<Game>> {

            //if (!supabaseClient.realtime.) {
                //supabaseClient.realtime.connect()
            //}

            // Create a channel
            val channel = supabaseClient.realtime.channel("game_updates")

            launch {
                channel.status.collect { status ->
                    println("Realtime channel status: $status")
                }
            }

            // Define the listener that triggers a full re-fetch on ANY change
            val changesFlow = channel.postgresChangeFlow<PostgresAction>(
                schema = "public",
                filter = { "schema=public,table=$TABLENAME" }
            )



            // 2. Set up the collection logic (the re-fetch) using onEach
            // This sets up a side-effect without immediately collecting/suspending the main coroutine.
            changesFlow
                .onEach {
                    // Re-fetch the ENTIRE list whenever a change notification is received
                    val updatedGameDTOs = supabaseClient.postgrest[TABLENAME]
                        .select(columns = columnsWithName) {
                            filter { eq("status", 0) }
                        }
                        .decodeList<GameDTO>()

                    // Emit the new list to the outer flow
                    trySend(updatedGameDTOs.map { it.toGame() })
                }
                // 3. Start the collection in the current scope.
                // NOTE: This call is NOT suspending. It starts the collection in the background.
                .launchIn(this)

            // CRITICAL STEP: Call subscribe() here in the callbackFlow block
            channel.subscribe()

            // Lifecycle management: Close the channel when the flow is closed
            awaitClose {
                launch {
                    channel.unsubscribe()
                }
            }
        }


        // --- 3. Merge Flows ---
        // Emit all subsequent updates from the Realtime flow
        // This is now safe because the subscribe() call is inside the `realtimeFlow`'s setup
        emitAll(realtimeFlow)

    }
}