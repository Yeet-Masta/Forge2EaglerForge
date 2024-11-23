/**
 * Complete Forge to EaglerForge API mappings
 */
object ForgeMappings {
    /**
     * Core class mappings for major Minecraft packages
     */
    val coreMappings = mapOf(
        // Client
        "net.minecraft.client.Minecraft" to "ModAPI.minecraft",
        "net.minecraft.client.entity.EntityPlayerSP" to "ModAPI.player",
        "net.minecraft.client.multiplayer.WorldClient" to "ModAPI.world",
        "net.minecraft.client.settings.GameSettings" to "ModAPI.settings",
        "net.minecraft.client.network.NetHandlerPlayClient" to "ModAPI.network",
        "net.minecraft.client.gui.ScaledResolution" to "ModAPI.resolution",

        // GUI
        "net.minecraft.client.gui.GuiScreen" to "ModAPI.minecraft.currentScreen",
        "net.minecraft.client.gui.GuiMainMenu" to "nmcg_GuiMainMenu",
        "net.minecraft.client.gui.GuiChat" to "nmcg_GuiChat",
        "net.minecraft.client.gui.inventory.GuiContainer" to "nmcg_GuiContainer",
        "net.minecraft.client.gui.GuiNewChat" to "ModAPI.minecraft.ingameGUI.persistantChatGUI",

        // Renderer
        "net.minecraft.client.renderer.EntityRenderer" to "nmcr_EntityRenderer",
        "net.minecraft.client.renderer.RenderGlobal" to "nmcr_RenderGlobal",
        "net.minecraft.client.renderer.entity.RenderManager" to "ModAPI.minecraft.renderManager",
        "net.minecraft.client.renderer.GlStateManager" to "nlevo_GlStateManager",

        // Entity
        "net.minecraft.entity.Entity" to "ModAPI.entity",
        "net.minecraft.entity.player.EntityPlayer" to "ModAPI.player",
        "net.minecraft.entity.player.EntityPlayerMP" to "ModAPI.server.player",

        // World
        "net.minecraft.world.World" to "ModAPI.world",
        "net.minecraft.world.chunk.Chunk" to "ModAPI.world.getChunkFromBlockCoords",
        "net.minecraft.world.WorldProvider" to "ModAPI.world.provider",

        // Items/Blocks
        "net.minecraft.init.Items" to "ModAPI.items",
        "net.minecraft.init.Blocks" to "ModAPI.blocks",
        "net.minecraft.item.Item" to "ModAPI.items",
        "net.minecraft.block.Block" to "ModAPI.blocks",
        "net.minecraft.block.material.Material" to "ModAPI.materials",

        // NBT
        "net.minecraft.nbt.NBTTagCompound" to "ModAPI.util.makeArray(ModAPI.nbt.NBTTagCompound)",
        "net.minecraft.nbt.NBTTagList" to "ModAPI.util.makeArray(ModAPI.nbt.NBTTagList)",

        // Network
        "net.minecraft.network.NetworkManager" to "ModAPI.network",
        "net.minecraft.network.play.client.C01PacketChatMessage" to "ModAPI.network.packets.C01PacketChatMessage",

        // Server
        "net.minecraft.server.MinecraftServer" to "ModAPI.server",
        "net.minecraft.command.CommandHandler" to "ModAPI.server.commandManager",

        "net.minecraft.client.settings.KeyBinding" to "ModAPI.KeyBinding",
        "net.minecraft.client.settings.GameSettings" to "ModAPI.minecraft.gameSettings",
        "org.lwjgl.input.Keyboard" to "ModAPI.KeyBinding",
        "org.lwjgl.input.Mouse" to "ModAPI.mouse"
    )

    /**
     * Event mappings from Forge events to EaglerForge events
     */
    val eventMappings = mapOf(
        // Client events
        "net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent" to "tick",
        "net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent" to "tick",
        "net.minecraftforge.client.event.RenderWorldLastEvent" to "render",
        "net.minecraftforge.client.event.RenderGameOverlayEvent" to "render",
        "net.minecraftforge.client.event.ClientChatReceivedEvent" to "receivechatmessage",
        "net.minecraftforge.client.event.ClientChatEvent" to "sendchatmessage",
        "net.minecraftforge.client.event.GuiScreenEvent" to "frame",
        "net.minecraftforge.client.event.RenderHandEvent" to "render",
        "net.minecraftforge.client.event.RenderLivingEvent" to "render",
        "net.minecraftforge.client.event.RenderPlayerEvent" to "render",
        "net.minecraftforge.client.event.FOVUpdateEvent" to "frame",
        "net.minecraftforge.client.event.MouseEvent" to "frame",

        // World events
        "net.minecraftforge.event.world.WorldEvent" to "load",
        "net.minecraftforge.event.world.ChunkEvent" to "load",
        "net.minecraftforge.event.world.BlockEvent" to "tick",
        "net.minecraftforge.event.world.ChunkWatchEvent" to "tick",

        // Server events
        "net.minecraftforge.fml.common.event.FMLServerStartingEvent" to "serverstart",
        "net.minecraftforge.fml.common.event.FMLServerStoppingEvent" to "serverstop",
        "net.minecraftforge.fml.common.event.FMLServerStartedEvent" to "serverstart",
        "net.minecraftforge.event.ServerChatEvent" to "receivechatmessage",
        "net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent" to "tick",

        // Player events
        "net.minecraftforge.event.entity.player.PlayerEvent" to "tick",
        "net.minecraftforge.event.entity.player.PlayerInteractEvent" to "tick",
        "net.minecraftforge.event.entity.player.PlayerUseItemEvent" to "tick",
        "net.minecraftforge.event.entity.player.PlayerDropsEvent" to "tick",
        "net.minecraftforge.event.entity.player.PlayerSleepInBedEvent" to "tick",
        "net.minecraftforge.event.entity.player.PlayerWakeUpEvent" to "tick",

        // Command events
        "net.minecraftforge.event.CommandEvent" to "processcommand",

        // Bootstrap/Init events
        "net.minecraftforge.fml.common.event.FMLPreInitializationEvent" to "bootstrap",
        "net.minecraftforge.fml.common.event.FMLInitializationEvent" to "load",
        "net.minecraftforge.fml.common.event.FMLPostInitializationEvent" to "load",

        // Custom events
        "custom:asyncsink_reloaded" to "custom:asyncsink_reloaded",
        "lib:asyncsink" to "lib:asyncsink",
        "lib:libcustomitems:loaded" to "lib:libcustomitems:loaded"
    )

    /**
     * Method name mappings
     */
    val methodMappings = mapOf(
        // Minecraft methods
        "getMinecraft" to "minecraft",
        "displayGuiScreen" to "displayGuiScreen",
        "loadWorld" to "loadWorld",
        "setWorldAndResolution" to "setWorldAndResolution",
        "refreshResources" to "refreshResources",
        "getDebugFPS" to "getFPS",

        // Player methods
        "sendChatMessage" to "sendChatMessage",
        "addChatMessage" to "addChatMessage",
        "getHeldItem" to "getHeldItem",
        "swingItem" to "swingItem",
        "getPosition" to "getPosition",
        "setPosition" to "setPosition",
        "getDisplayName" to "getDisplayName",

        // World methods
        "getBlockState" to "getBlockState",
        "setBlockState" to "setBlockState",
        "markBlockForUpdate" to "markBlockForUpdate",
        "playSound" to "playSound",
        "spawnParticle" to "spawnParticle",
        "getChunkFromBlockCoords" to "getChunkFromBlockCoords",
        "isAirBlock" to "isAirBlock",

        // NBT methods
        "setString" to "setString",
        "getString" to "getString",
        "setInteger" to "setInteger",
        "getInteger" to "getInteger",
        "setTag" to "setTag",
        "getTag" to "getTag",
        "hasKey" to "hasKey",

        // Network methods
        "sendPacket" to "addToSendQueue",
        "addToSendQueue" to "addToSendQueue",
        "sendToServer" to "addToSendQueue",
        "sendToAll" to "addToSendQueue",

        // Server methods
        "startServer" to "startServer",
        "stopServer" to "stopServer",
        "getServer" to "getServer",
        "registerCommand" to "registerCommand",
        "executeCommand" to "executeCommand",

        "drawString" to "\$drawStringWithShadow",
        "getHeldItem" to "\$getHeldItem",
        "getHealth" to "\$getHealth",
        "getMaxHealth" to "\$getMaxHealth",
        "getCurrentItem" to "\$currentItem",
        "getStackInSlot" to "\$getStackInSlot",
        "getItem" to "\$getItem",
        "getMetadata" to "\$getMetadata",
        "setItemInUse" to "\$setItemInUse",
        "getDistanceToEntity" to "\$getDistanceToEntity",
        "addChatMessage" to "\$addChatComponentMessage",

        // Key Binding methods
        "isKeyDown" to "isKeyDown",
        "isPressed" to "isPressed",
        "getKeyCode" to "getKeyCode",
        "getKeyDescription" to "getKeyDescription",
        "getKeyCategory" to "getKeyCategory",
        "setKeyBindState" to "setKeyBindState",
        "getIsKeyPressed" to "getIsKeyPressed"
    )

    private val classMappings = mapOf(
        "EntityPlayerSP" to "net.minecraft.client.entity.EntityPlayerSP",
        "ItemSword" to "net.minecraft.item.ItemSword",
        "EntityPlayer" to "net.minecraft.entity.player.EntityPlayer",
        "FontRenderer" to "net.minecraft.client.gui.FontRenderer",
        "Minecraft" to "net.minecraft.client.Minecraft",
        "Items" to "net.minecraft.init.Items"
    )

    /**
     * Required module patterns to detect when modules need to be required
     */
    val modulePatterns = mapOf(
        "player" to listOf(
            "EntityPlayerSP", "thePlayer", "player", "EntityPlayer",
            "sendChatMessage", "inventory", "getHeldItem",
            "PlayerEvent", "PlayerInteractEvent", "PlayerCapabilities"
        ),
        "world" to listOf(
            "World", "WorldClient", "theWorld", "Chunk", "WorldProvider",
            "setBlock", "getBlock", "loadChunk", "WorldEvent",
            "BlockPos", "IBlockAccess", "WorldType"
        ),
        "network" to listOf(
            "NetHandlerPlayClient", "sendPacket", "NetworkManager",
            "addToSendQueue", "Packet", "NetworkRegistry",
            "SimpleNetworkWrapper", "FMLEventChannel"
        ),
        "resolution" to listOf(
            "ScaledResolution", "displayWidth", "displayHeight",
            "getScaledWidth", "getScaledHeight", "getScaleFactor",
            "GuiScreen", "displayGuiScreen"
        )
    )

    /**
     * Package prefix mappings
     */
    val packagePrefixes = mapOf(
        "net.minecraft.client" to "nmc",
        "net.minecraft.client.gui" to "nmcg",
        "net.minecraft.client.entity" to "nmce",
        "net.minecraft.client.renderer" to "nmcr",
        "net.minecraft.init" to "nmi",
        "net.minecraft.enchantment" to "nme",
        "net.minecraft.block.material" to "nbm",
        "net.minecraft.server" to "nms",
        "net.minecraft.command" to "nmc",
        "net.minecraft.network.play.client" to "nnpc",
        "net.minecraft.entity.player" to "nep",
        "net.minecraft.world" to "nmw",
        "net.minecraft.util" to "nmu",
        "net.minecraft.inventory" to "nmi"
    )
}