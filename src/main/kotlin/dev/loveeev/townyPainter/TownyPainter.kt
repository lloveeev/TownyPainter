package dev.loveeev.townyPainter

import org.bukkit.plugin.java.JavaPlugin


class TownyPainter : JavaPlugin() {

    private var server: MyHttpServer? = null

    override fun onEnable() {
        server = MyHttpServer()
        try {
            server?.startServer()
            logger.info("Сервер запущен")
        } catch (e: Exception) {
            logger.severe("Не удалось запустить сервер: ${e.message}")
        }
    }

    override fun onDisable() {
        // Плагин завершает свою работу
    }
}

