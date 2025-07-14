package dev.loveeev.townyPainter

import com.google.gson.Gson
import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.TownyUniverse
import com.palmergames.bukkit.towny.`object`.Town
import com.palmergames.bukkit.towny.`object`.TownBlock
import com.palmergames.bukkit.towny.`object`.WorldCoord
import com.palmergames.bukkit.towny.utils.ResidentUtil
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bukkit.Bukkit

class MyHttpServer {
    val gson = Gson()
    var lastTown: Town? = null

    fun Application.module() {
        install(ContentNegotiation) {
            gson {  }
        }
        install(CORS) {
            anyHost()
            allowCredentials = true
            allowMethod(HttpMethod.Options)  // Разрешить запросы OPTIONS
            allowMethod(HttpMethod.Get)      // Разрешить запросы GET
            allowMethod(HttpMethod.Post)     // Разрешить запросы POST
            allowHeader(HttpHeaders.ContentType) // Разрешить заголовок Content-Type
            allowHeader(HttpHeaders.Authorization) // Разрешить заголовок Authorization
        }

    }

    fun startServer() {
        embeddedServer(Netty, port = 5029, host = "0.0.0.0") {
            module()
            routing {
                route("/selector") {
                    post("/fill") {
                        call.processFill()
                    }
                    post("/create") {
                        call.processCreate()
                    }
                    post("/claim") {
                        call.processClaim()
                    }
                    post("/delete") {
                        call.processDelete()
                    }
                }
            }
        }.start(wait = false)
    }

    private suspend fun ApplicationCall.processFill() {
        val chunkData = receiveAndParse<ChunkFill>() ?: return
        if (lastTown == null) {
            respondBadRequest("Missing town reference")
            return
        }
        TownyUtil.getTownClaimSelectionOrThrow(WorldCoord(chunkData.chunk.world, chunkData.chunk.x, chunkData.chunk.z), lastTown!!)
            .forEach { coord ->
                TownBlock(coord.x, coord.z, TownyAPI.getInstance().getTownyWorld(coord.bukkitWorld)).apply {
                    town = lastTown
                    save()
                }
            }
        lastTown!!.save()
        respondOk()
    }

    private suspend fun ApplicationCall.processCreate() {
        val townRequest = receiveAndParse<TownRequest>() ?: return
        val townyUniverse = TownyUniverse.getInstance()

        if (!townyUniverse.hasTown(townRequest.townName)) {
            val town = TownyUtil.newTown(townRequest.townName, ResidentUtil.createAndGetNPCResident(), townRequest.selection)
            townRequest.nationName.let { nationName ->
                TownyUtil.getAndAddOrCreateNation(nationName, town)
            }
        }
        respondOk()
    }

    private suspend fun ApplicationCall.processClaim() {
        val townRequest = receiveAndParse<TownClaims>() ?: return
        lastTown = TownyAPI.getInstance().getTown(townRequest.townName)
        lastTown?.let { town ->
            townRequest.selections.forEach { selection ->
                Bukkit.getWorld(selection.world)?.let { world ->
                    TownBlock(WorldCoord(world, selection.x, selection.z)).apply {
                        this.town = town
                        save()
                    }
                }
            }
            town.save()
        }
        respondOk("Claim processed successfully")
    }

    private suspend fun ApplicationCall.processDelete() {
        val townRequest = receiveAndParse<TownRequestDelete>() ?: return
        if (TownyUtil.deleteTown(townRequest)) {
            respondOk()
        } else {
            respondBadRequest("Invalid request")
        }
    }

    private suspend inline fun <reified T> ApplicationCall.receiveAndParse(): T? {
        return try {
            gson.fromJson(receive<String>(), T::class.java)
        } catch (e: Exception) {
            respondBadRequest("Invalid request format")
            null
        }
    }

    private suspend fun ApplicationCall.respondOk(message: String = "{\"success\": true}") {
        respond(HttpStatusCode.OK, message)
    }

    private suspend fun ApplicationCall.respondBadRequest(message: String) {
        respond(HttpStatusCode.BadRequest, "{\"error\": \"$message\"}")
    }
}

data class TownRequestDelete(val townName: String)
data class TownRequest(val townName: String, val nationName: String, val selection: ChunkPosition)
data class TownClaims(val townName: String, val selections: List<ChunkPosition>)
data class ChunkPosition(val x: Int, val z: Int, val world: String, val prevState: Int, val newState: Int)
data class ChunkFill(val townName: String, val chunk: ChunkPosition)


