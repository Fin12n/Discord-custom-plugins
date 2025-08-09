package com.quang1807.discordbot

import com.yourname.discordbot.commands.CommandManager
import com.yourname.discordbot.listeners.MinecraftEventListener
import com.yourname.discordbot.managers.BotManager
import com.yourname.discordbot.managers.ConfigManager
import com.yourname.discordbot.managers.VoiceChannelManager
import com.yourname.discordbot.utils.Logger
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

/**
 * Main Discord Bot Plugin Class
 * 
 * @author YourName
 * @version 1.0.0
 */
class DiscordBotPlugin : JavaPlugin() {
    
    companion object {
        lateinit var instance: DiscordBotPlugin
            private set
        
        const val PLUGIN_NAME = "DiscordBotPlugin"
        const val VERSION = "1.0.0"
        const val AUTHOR = "YourName"
    }
    
    // Managers
    private lateinit var configManager: ConfigManager
    private lateinit var botManager: BotManager
    private lateinit var voiceChannelManager: VoiceChannelManager
    private lateinit var commandManager: CommandManager
    private lateinit var pluginLogger: Logger
    
    // Plugin lifecycle
    override fun onLoad() {
        instance = this
        
        // Initialize logger first
        pluginLogger = Logger(this)
        
        pluginLogger.info("Loading $PLUGIN_NAME v$VERSION by $AUTHOR")
    }
    
    override fun onEnable() {
        try {
            // Print banner
            printBanner()
            
            // Initialize components
            if (!initializePlugin()) {
                pluginLogger.severe("Failed to initialize plugin! Disabling...")
                server.pluginManager.disablePlugin(this)
                return
            }
            
            pluginLogger.info("✅ $PLUGIN_NAME has been enabled successfully!")
            pluginLogger.info("🤖 Bot Status: ${if (botManager.isConnected()) "Connected" else "Disconnected"}")
            pluginLogger.info("📢 Voice Channel: ${if (voiceChannelManager.isEnabled()) "Active" else "Disabled"}")
            
        } catch (e: Exception) {
            pluginLogger.severe("Critical error during plugin initialization: ${e.message}")
            e.printStackTrace()
            server.pluginManager.disablePlugin(this)
        }
    }
    
    override fun onDisable() {
        try {
            pluginLogger.info("🔄 Shutting down $PLUGIN_NAME...")
            
            // Shutdown managers in reverse order
            if (::voiceChannelManager.isInitialized) {
                voiceChannelManager.shutdown()
            }
            
            if (::commandManager.isInitialized) {
                commandManager.shutdown()
            }
            
            if (::botManager.isInitialized) {
                botManager.shutdown()
            }
            
            pluginLogger.info("✅ $PLUGIN_NAME has been disabled successfully!")
            
        } catch (e: Exception) {
            pluginLogger.severe("Error during plugin shutdown: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Initialize all plugin components
     */
    private fun initializePlugin(): Boolean {
        try {
            // 1. Initialize config manager
            pluginLogger.info("📝 Loading configuration...")
            configManager = ConfigManager(this)
            if (!configManager.initialize()) {
                pluginLogger.severe("Failed to initialize configuration!")
                return false
            }
            
            // 2. Initialize bot manager
            pluginLogger.info("🤖 Initializing Discord Bot...")
            botManager = BotManager(this)
            if (!botManager.initialize()) {
                pluginLogger.warning("Discord Bot initialization failed - some features will be disabled!")
                // Don't return false here, allow plugin to continue without Discord
            }
            
            // 3. Initialize voice channel manager
            pluginLogger.info("📢 Setting up Voice Channel Manager...")
            voiceChannelManager = VoiceChannelManager(this)
            voiceChannelManager.initialize()
            
            // 4. Initialize command manager
            pluginLogger.info("⚡ Setting up Command Manager...")
            commandManager = CommandManager(this)
            commandManager.initialize()
            
            // 5. Register Minecraft event listeners
            pluginLogger.info("👂 Registering event listeners...")
            server.pluginManager.registerEvents(MinecraftEventListener(this), this)
            
            return true
            
        } catch (e: Exception) {
            pluginLogger.severe("Error during plugin initialization: ${e.message}")
            e.printStackTrace()
            return false
        }
    }
    
    /**
     * Print plugin banner
     */
    private fun printBanner() {
        val banner = listOf(
            "§b╔══════════════════════════════════════╗",
            "§b║        §eDiscord Bot Plugin v1.0.0    §b║",
            "§b║                                      §b║", 
            "§b║  §a✅ Server Management Commands       §b║",
            "§b║  §a📢 Voice Channel Real-time Update   §b║",
            "§b║  §a🤡 Cringe Member Detection          §b║",
            "§b║  §a📊 Rich Server Information          §b║",
            "§b║  §a🔐 Advanced Permission System       §b║",
            "§b║                                      §b║",
            "§b║        §dBy: §f$AUTHOR                 §b║",
            "§b╚══════════════════════════════════════╝"
        )
        
        banner.forEach { line ->
            server.consoleSender.sendMessage(line)
        }
    }
    
    // Getter methods for managers
    fun getConfigManager(): ConfigManager = configManager
    fun getBotManager(): BotManager = botManager
    fun getVoiceChannelManager(): VoiceChannelManager = voiceChannelManager
    fun getCommandManager(): CommandManager = commandManager
    fun getPluginLogger(): Logger = pluginLogger
    
    /**
     * Reload plugin configuration and reinitialize components
     */
    fun reloadPlugin(): Boolean {
        return try {
            pluginLogger.info("🔄 Reloading plugin...")
            
            // Reload config
            reloadConfig()
            configManager.reload()
            
            // Restart voice channel manager
            voiceChannelManager.shutdown()
            voiceChannelManager.initialize()
            
            // Reconnect bot if needed
            if (!botManager.isConnected()) {
                botManager.reconnect()
            }
            
            pluginLogger.info("✅ Plugin reloaded successfully!")
            true
            
        } catch (e: Exception) {
            pluginLogger.severe("Failed to reload plugin: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Get plugin data folder
     */
    fun getPluginDataFolder(): File = dataFolder
    
    /**
     * Check if plugin is fully initialized and working
     */
    fun isFullyInitialized(): Boolean {
        return ::configManager.isInitialized &&
               ::botManager.isInitialized &&
               ::voiceChannelManager.isInitialized &&
               ::commandManager.isInitialized &&
               isEnabled
    }
    
    /**
     * Get plugin statistics
     */
    fun getPluginStats(): Map<String, Any> {
        return mapOf(
            "version" to VERSION,
            "enabled" to isEnabled,
            "botConnected" to if (::botManager.isInitialized) botManager.isConnected() else false,
            "voiceChannelActive" to if (::voiceChannelManager.isInitialized) voiceChannelManager.isEnabled() else false,
            "uptime" to (System.currentTimeMillis() - server.startTime),
            "javaVersion" to System.getProperty("java.version"),
            "bukkitVersion" to server.version
        )
    }
}