package dev.loveeev.townyPainter

import com.palmergames.bukkit.towny.*
import com.palmergames.bukkit.towny.command.NationCommand
import com.palmergames.bukkit.towny.command.TownCommand
import com.palmergames.bukkit.towny.event.*
import com.palmergames.bukkit.towny.exceptions.TownyException
import com.palmergames.bukkit.towny.`object`.*
import com.palmergames.bukkit.towny.utils.BorderUtil
import org.bukkit.Bukkit


class TownyUtil {
    companion object {

        fun deleteTown(requestData: TownRequestDelete): Boolean {
            val town = TownyAPI.getInstance().getTown(requestData.townName) ?: return false
            println("Deleting town: ${requestData.townName}")
            TownyUniverse.getInstance().dataSource.removeTown(town, DeleteTownEvent.Cause.ADMIN_COMMAND)
            return true
        }

        fun getTownClaimSelectionOrThrow(coord: WorldCoord, town: Town): Collection<WorldCoord> {
            return coord.bukkitWorld?.let { BorderUtil.getFloodFillableCoords(town, coord).coords() } ?: emptyList()
        }

        @Throws(TownyException::class)
        fun newTown(name: String, resident: Resident, key: ChunkPosition): Town {
            val world = TownyAPI.getInstance().getTownyWorld(Bukkit.getWorld("world")) ?: throw TownyException("World not found")

            val location = Bukkit.getWorld(key.world)?.getChunkAt(key.x, key.z)
                ?.getBlock(8, 64, 8)?.location?.apply { y = world.bukkitWorld!!.getHighestBlockAt(this).y.toDouble() }


            return TownCommand.newTown(TownyAPI.getInstance().getTownyWorld(key.world),name,resident,Coord(key.x,key.z), location,null)
        }

        @Throws(TownyException::class)
        fun getAndAddOrCreateNation(nationName: String?, town: Town?): Nation? {
            requireNotNull(nationName) { "Название нации не может быть null" }
            requireNotNull(town) { "Город не может быть null" }

            val nation = TownyUniverse.getInstance().getNation(nationName)
                ?: NationCommand.newNation(nationName, town).apply { capital = town }

            NationCommand.nationAdd(nation, town)
            nation.save()

            return nation
        }
    }
}
