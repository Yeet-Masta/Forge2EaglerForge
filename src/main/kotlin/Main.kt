import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.AnnotationExpr
import com.github.javaparser.ast.expr.MemberValuePair
import com.github.javaparser.ast.visitor.VoidVisitorAdapter

/**
 * Main converter class for transforming Forge mods to EaglerForge format
 */
import java.net.URL
import java.net.HttpURLConnection

class ForgeToEaglerConverter {
    /**
     * Simple JavaScript code formatter
     */
    private fun formatJavaScript(code: String): String {
        // First clean up any malformed segments
        var cleanCode = processStringContent(code)
            .replace("(event)=> {", "(event) => {") // Fix arrow function spacing
            .replace(Regex("\\{\\s*\\{"), "{") // Remove duplicate braces
            .replace(Regex("\\}\\s*\\}\\);"), "}\n});") // Fix closing braces

        // Split into sections
        val sections = cleanCode.split("\n").map { it.trim() }
        val output = StringBuilder()
        var insideEventHandler = false
        var indentLevel = 0

        sections.forEach { line ->
            when {
                // Handle start of event handler
                line.contains("addEventListener") -> {
                    output.append("${line}\n")
                    insideEventHandler = true
                    indentLevel = 1
                }

                // Handle end of event handler
                line.contains("});") -> {
                    insideEventHandler = false
                    indentLevel = 0
                    output.append("});\n\n")
                }

                // Handle empty lines
                line.isBlank() -> {
                    if (!insideEventHandler) {
                        output.append("\n")
                    }
                }

                // Handle normal lines
                else -> {
                    if (line.endsWith("{")) indentLevel++
                    if (line.startsWith("}")) indentLevel--

                    val indent = "  ".repeat(indentLevel)
                    output.append("$indent$line\n")

                    if (line.endsWith("{")) indentLevel++
                }
            }
        }

        return output.toString()
            .replace(Regex("\n{3,}"), "\n\n") // Remove extra blank lines
            .trim()
    }
    private val requiredModules = mutableSetOf<String>()
    private val mappings = ForgeMappings

    private fun processStringContent(content: String): String {
        if (!content.contains("ModAPI.util.str")) return content

        return content.replace(
            Regex("ModAPI\\.util\\.str\\(\"([^\"]+)\"\\)"),
        ) { matchResult ->
            val stringContent = matchResult.groupValues[1]
            // Don't replace ModAPI references inside the actual string content
            "ModAPI.util.str(\"${stringContent}\")"
                .replace("ModAPI.", "") // Remove accidental ModAPI. references in strings
        }
    }
    /**
     * Converts a Forge mod file to EaglerForge format
     */
    fun convert(sourceCode: String): String {
        val parser = JavaParser()
        val result = parser.parse(sourceCode)

        if (!result.isSuccessful) {
            throw IllegalArgumentException("Failed to parse Java source code")
        }

        val cu = result.result.get()
        val output = StringBuilder()

        // Process @Mod annotation
        val modMetadata = extractModMetadata(cu)
        addModMetadata(output, modMetadata)

        // Process class contents including event handlers
        val eventHandlers = extractEventHandlers(cu)
        detectRequiredModules(sourceCode)

        // Add required modules
        requiredModules.forEach { module ->
            output.appendLine("ModAPI.require('$module');")
        }
        output.appendLine()

        // Convert event handlers
        eventHandlers.forEach { handler ->
            val convertedHandler = convertEventHandler(handler)
            output.append(convertedHandler)
            output.appendLine()
        }

        // Format the final JavaScript code
        val formattedCode = formatJavaScript(output.toString())
        return formattedCode
    }

    private data class EventHandler(
        val methodName: String,
        val eventType: String,
        val body: String
    )

    private data class ModMetadata(
        val modId: String?,
        val version: String?,
        val name: String?,
        val description: String?
    )

    /**
     * Extracts metadata from @Mod annotation
     */
    private fun extractModMetadata(cu: CompilationUnit): ModMetadata {
        var modId: String? = null
        var version: String? = null
        var name: String? = null
        var description: String? = null

        cu.findAll(ClassOrInterfaceDeclaration::class.java).forEach { classDecl ->
            classDecl.annotations.forEach { annotation ->
                if (annotation.nameAsString == "Mod") {
                    annotation.childNodes.forEach { node ->
                        if (node is MemberValuePair) {
                            when (node.nameAsString) {
                                "modid" -> modId = node.value.toString().removeSurrounding("\"")
                                "version" -> version = node.value.toString().removeSurrounding("\"")
                                "name" -> name = node.value.toString().removeSurrounding("\"")
                                "description" -> description = node.value.toString().removeSurrounding("\"")
                            }
                        }
                    }
                }
            }
        }

        return ModMetadata(modId, version, name, description)
    }

    /**
     * Adds mod metadata to output
     */
    private fun addModMetadata(output: StringBuilder, metadata: ModMetadata) {
        metadata.modId?.let { output.appendLine("ModAPI.meta.title(\"$it\");") }
        metadata.version?.let { output.appendLine("ModAPI.meta.version(\"$it\");") }
        metadata.name?.let { output.appendLine("ModAPI.meta.description(\"$it\");") }
        metadata.description?.let {
            val desc = if (metadata.name != null) "$it - ${metadata.name}" else it
            output.appendLine("ModAPI.meta.description(\"$desc\");")
        }
        output.appendLine()
    }

    /**
     * Extracts event handlers from the source code
     */
    private fun extractEventHandlers(cu: CompilationUnit): List<EventHandler> {
        val handlers = mutableListOf<EventHandler>()

        class MethodVisitor : VoidVisitorAdapter<Void>() {
            override fun visit(method: MethodDeclaration, arg: Void?) {
                method.annotations.forEach { annotation ->
                    if (annotation.nameAsString == "SubscribeEvent") {
                        val eventType = method.parameters[0].type.toString()
                        handlers.add(EventHandler(
                            methodName = method.nameAsString,
                            eventType = eventType,
                            body = method.body.get().toString()
                        ))
                    }
                }
                super.visit(method, arg)
            }
        }

        cu.accept(MethodVisitor(), null)
        return handlers
    }

    /**
     * Converts Java code to JavaScript with comprehensive Forge -> EaglerForge mappings
     */
    private fun convertToJavaScript(code: String): String {
        return code
            // Remove Java type declarations
            .replace(Regex("(\\w+)\\s+(\\w+)\\s*="), "const $2 =")

            // Convert Java null checks
            .replace(".equals(null)", "=== null")
            .replace("== null", "=== null")
            .replace("!= null", "!== null")

            // Convert common Forge patterns
            .replace("@SideOnly(Side.CLIENT)", "")  // Not needed in EaglerForge
            .replace("@Mod.EventHandler", "")       // Not needed in EaglerForge
            .replace("FMLCommonHandler.instance()", "ModAPI")
            .replace("Loader.isModLoaded", "ModAPI.isModLoaded")

            // Convert property accessors
            .replace(".getItem()", ".item")
            .replace(".getBlock()", ".block")
            .replace(".getMeta()", ".meta")
            .replace(".getUnlocalizedName()", ".unlocalizedName")
            .replace(".getDisplayName()", ".displayName")
            .replace(".getStack()", ".stack")

            // Convert method calls
            .replace(".sendToServer()", ".addToSendQueue()")
            .replace(".getMinecraft()", ".minecraft")
            .replace(".getWorld()", ".world")
            .replace(".getPlayer()", ".player")
            .replace(".markDirty()", ".markBlockForUpdate()")

            // Convert rendering hooks
            .replace("RenderHelper.disableStandardItemLighting()", "ModAPI.GlStateManager.disableLighting()")
            .replace("RenderHelper.enableStandardItemLighting()", "ModAPI.GlStateManager.enableLighting()")
            .replace("RenderHelper.enableGUIStandardItemLighting()", "ModAPI.GlStateManager.enableGUILighting()")

            // Convert GUI hooks
            .replace("Gui.drawModalRectWithCustomSizedTexture", "ModAPI.minecraft.currentScreen.drawModalRectWithCustomSizedTexture")
            .replace("Gui.drawRect", "ModAPI.minecraft.currentScreen.drawRect")
            .replace("fontRendererObj", "ModAPI.minecraft.fontRenderer")
            .replace("mc.fontRenderer", "ModAPI.minecraft.fontRenderer")

            // Convert keybinding
            .replace("Keyboard.isKeyDown", "ModAPI.keyboard.isKeyDown")
            .replace("Mouse.isButtonDown", "ModAPI.mouse.isButtonDown")
            .replace("new KeyBinding", "ModAPI.minecraft.gameSettings.keyBindings.add")

            // Convert resource loading
            .replace("Minecraft.getMinecraft().getResourceManager()", "ModAPI.minecraft.resourceManager")
            .replace("new ResourceLocation", "ModAPI.util.resourceLocation")

            // Convert sound system
            .replace("mc.getSoundHandler()", "ModAPI.minecraft.soundHandler")
            .replace("new PositionedSoundRecord", "ModAPI.util.sound")

            // Convert inventory/container hooks
            .replace("InventoryPlayer", "ModAPI.player.inventory")
            .replace("Container", "ModAPI.minecraft.currentScreen")
            .replace("Slot", "ModAPI.container.Slot")

            // Convert creative tab hooks
            .replace("CreativeTabs", "ModAPI.creativeTabs")
            .replace("getCreativeTab()", "creativeTab")

            // Convert particle effects
            .replace("EffectRenderer", "ModAPI.minecraft.effectRenderer")
            .replace("EntityFX", "ModAPI.particle")

            // Convert entity AI
            .replace("EntityAIBase", "ModAPI.entity.ai.base")
            .replace("PathNavigate", "ModAPI.entity.navigator")

            // Convert network packets
            .replace("SimpleNetworkWrapper", "ModAPI.network")
            .replace("PacketBuffer", "ModAPI.network.PacketBuffer")
            .replace("IMessage", "ModAPI.network.Packet")

            // Convert config system
            .replace("Configuration", "ModAPI.config")
            .replace("Property", "ModAPI.config.Property")

            // Convert base Minecraft classes
            .replace("Minecraft.getMinecraft()", "ModAPI.minecraft")
            .replace("Minecraft.minecraft()", "ModAPI.minecraft")
            .replace("mc.thePlayer", "ModAPI.minecraft.player")
            .replace("mc.theWorld", "ModAPI.minecraft.world")
            .replace(".thePlayer", ".player")
            .replace(".theWorld", ".world")

            // Convert GUI references
            .replace("GuiScreen", "ModAPI.minecraft.currentScreen")
            .replace("GuiMainMenu", "ModAPI.minecraft.GuiMainMenu")
            .replace("GuiChat", "ModAPI.minecraft.GuiChat")
            .replace("GuiContainer", "ModAPI.minecraft.GuiContainer")
            .replace("GuiNewChat", "ModAPI.minecraft.ingameGUI.persistantChatGUI")

            // Convert renderer references
            .replace("EntityRenderer", "ModAPI.minecraft.entityRenderer")
            .replace("RenderGlobal", "ModAPI.minecraft.renderGlobal")
            .replace("RenderManager", "ModAPI.minecraft.renderManager")
            .replace("GlStateManager", "ModAPI.GlStateManager")

            // Convert entity references
            .replace("EntityPlayerSP", "ModAPI.player")
            .replace("EntityPlayerMP", "ModAPI.server.player")

            // Convert world references
            .replace("World", "ModAPI.world")
            .replace("WorldProvider", "ModAPI.world.provider")
            .replace("getChunkFromBlockCoords", "ModAPI.world.getChunkFromBlockCoords")

            // Convert item/block references
            .replace("Items.", "ModAPI.items.")
            .replace("Blocks.", "ModAPI.blocks.")
            .replace("Item.", "ModAPI.items.")
            .replace("Block.", "ModAPI.blocks.")
            .replace("Material.", "ModAPI.materials.")

            // Convert NBT references
            .replace("new NBTTagCompound()", "ModAPI.util.makeArray(ModAPI.nbt.NBTTagCompound)")
            .replace("new NBTTagList()", "ModAPI.util.makeArray(ModAPI.nbt.NBTTagList)")

            // Convert network references
            .replace("NetworkManager", "ModAPI.network")
            .replace("C01PacketChatMessage", "ModAPI.network.packets.C01PacketChatMessage")

            // Convert server references
            .replace("MinecraftServer", "ModAPI.server")
            .replace("CommandHandler", "ModAPI.server.commandManager")

            // Convert utility methods
            .replace("MathHelper", "ModAPI.util.math")
            .replace("BlockPos", "ModAPI.util.BlockPos")
            .replace("EnumFacing", "ModAPI.util.EnumFacing")
            .replace("new Vec3", "ModAPI.util.vec3")
            .replace("MovingObjectPosition", "ModAPI.util.MovingObjectPosition")

            // new shit!
            // Handle null comparisons
        
            .replace(".equals(null)", "=== null")
            .replace("== null", "=== null")
            .replace("!= null", "!== null")

            // Replace type casts
        
            .replace(Regex("\\(int\\)\\s*"), "Math.floor(")
            .replace(Regex("\\(float\\)\\s*"), "")
            .replace(Regex("\\(double\\)\\s*"), "")

            // Replace Java collections
        
            .replace("List<Entity>", "const")
            .replace(".size()", ".\$size()")
            .replace(".get(", ".\$get(")
            .replace(".add(", ".\$add(")

            // Replace string operations
        
            .replace("\"", "'")
            .replace(".toString()", "")
            .replace("new ChatComponentText(", "ModAPI.util.str(")

            // Replace Minecraft specific code
        
            .replace("Minecraft.getMinecraft()", "ModAPI.minecraft")
            .replace("mc.thePlayer", "ModAPI.player")
            .replace("mc.theWorld", "ModAPI.world")
            .replace("player.worldObj", "ModAPI.world")
            .replace(".fontRendererObj", ".\$fontRendererObj")

            // Add proper referencing
        
            .replace(".getRef().", ".")
            .replace("instanceof", "getRef() instanceof ModAPI.reflect.getClassById(\"")
            .replace(")", "\").class)")

            // Handle events
        
            .replace("event.setCanceled(true)", "event.preventDefault = true")
            .replace("event.isCanceled()", "event.preventDefault")

            // Convert KeyBinding creation
            .replace(
                Regex("new KeyBinding\\(\"([^\"]+)\",\\s*(\\d+),\\s*\"([^\"]+)\"\\)"),
                "ModAPI.KeyBinding.create('$1', $2, '$3')"
            )
            .replace("Keyboard.KEY_", "ModAPI.KeyBinding.KEY_")
            .replace(".isPressed()", ".isPressed()")
            .replace(".getIsKeyPressed()", ".getIsKeyPressed()")
            .replace(".setKeyBindState(", ".setKeyBindState(")

            // Clean up the code
            .replace(Regex("\\{\\s*([^}]+)\\s*\\}"), "{ $1 }")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * Converts a single event handler to EaglerForge format
     */
    private fun convertEventHandler(handler: EventHandler): String {
        val eaglerEvent = mapEventType(handler.eventType)
        var convertedBody = handler.body

        // Convert class references
        mappings.coreMappings.forEach { (forge, eagler) ->
            convertedBody = convertedBody.replace(forge, eagler)
        }

        // Convert method calls
        mappings.methodMappings.forEach { (forge, eagler) ->
            convertedBody = convertedBody.replace(".$forge(", ".$eagler(")
        }

        // Handle special cases
        convertedBody = handleSpecialCases(convertedBody)

        var jsBody = convertToJavaScript(convertedBody)
        jsBody = jsBody.replace(Regex("^\\s*\\{|\\}\\s*$"), "") // Remove outer braces

        return """
            ModAPI.addEventListener('$eaglerEvent', (event) => {
                if (!ModAPI.minecraft || !ModAPI.player) return;
                
                try {
                    $jsBody
                } catch (error) {
                    console.error('[${handler.methodName}] Error:', error);
                }
            });
        """.trimIndent()
    }

    private fun mapEventType(forgeEvent: String): String {
        // Direct event mappings from ForgeMappings
        val directMappings = mapOf(
            "TickEvent.PlayerTickEvent" to "tick",
            "TickEvent.ClientTickEvent" to "tick",
            "RenderWorldLastEvent" to "render",
            "RenderGameOverlayEvent" to "render",
            "ClientChatReceivedEvent" to "receivechatmessage",
            "ClientChatEvent" to "sendchatmessage",
            "GuiScreenEvent" to "frame",
            "RenderHandEvent" to "render",
            "RenderLivingEvent" to "render",
            "RenderPlayerEvent" to "render",
            "FOVUpdateEvent" to "frame",
            "MouseEvent" to "frame",
            "WorldEvent" to "load",
            "ChunkEvent" to "load",
            "BlockEvent" to "tick",
            "ChunkWatchEvent" to "tick",
            "FMLServerStartingEvent" to "serverstart",
            "FMLServerStoppingEvent" to "serverstop",
            "FMLServerStartedEvent" to "serverstart",
            "ServerChatEvent" to "receivechatmessage",
            "TickEvent.ServerTickEvent" to "tick",
            "PlayerEvent" to "tick",
            "PlayerInteractEvent" to "tick",
            "PlayerUseItemEvent" to "tick",
            "PlayerDropsEvent" to "tick",
            "PlayerSleepInBedEvent" to "tick",
            "PlayerWakeUpEvent" to "tick",
            "CommandEvent" to "processcommand",
            "FMLPreInitializationEvent" to "bootstrap",
            "FMLInitializationEvent" to "load",
            "FMLPostInitializationEvent" to "load"
        )

        // Try direct mapping first
        return directMappings[forgeEvent] ?: when {
            // Handle special cases
            forgeEvent.contains("Render") -> "render"
            forgeEvent.contains("Tick") -> "tick"
            forgeEvent.contains("Chat") -> "receivechatmessage"
            forgeEvent.contains("Player") -> "tick"
            forgeEvent.contains("World") -> "load"
            forgeEvent.contains("Server") -> "serverstart"
            forgeEvent.contains("Command") -> "processcommand"
            forgeEvent.contains("InputEvent.KeyInputEvent") -> "frame"
            forgeEvent.contains("RenderGameOverlay") -> "render"
            forgeEvent.contains("PlayerTick") -> "tick"
            forgeEvent.contains("ClientTick") -> "tick"
            forgeEvent.contains("RenderWorld") -> "render"
            forgeEvent.contains("ChatReceived") -> "receivechatmessage"
            forgeEvent.contains("ChatSent") -> "sendchatmessage"
            forgeEvent.contains("GuiScreen") -> "frame"
            forgeEvent.contains("RenderHand") -> "render"
            forgeEvent.contains("WorldLoad") -> "load"
            forgeEvent.contains("ServerStart") -> "serverstart"
            forgeEvent.contains("ServerStop") -> "serverstop"
            else -> "custom:${forgeEvent.toLowerCase()}"
        }
    }

    /**
     * Handles special cases in code conversion
     */
    private fun handleSpecialCases(code: String): String {
        return code
            .replace("Minecraft.getMinecraft()", "ModAPI.minecraft")
            .replace("mc.thePlayer", "ModAPI.player")
            .replace("mc.theWorld", "ModAPI.world")
            .replace("event.setCanceled(true)", "event.preventDefault = true")
            .replace("event.isCanceled()", "event.preventDefault")
            .replace("new NBTTagCompound()", "ModAPI.util.makeArray(ModAPI.nbt.NBTTagCompound)")
            .replace("new ChatComponentText(", "ModAPI.util.str(")
            .replace(".toString()", ").toString()")
            // Handle KeyBinding cases
            .replace("new KeyBinding", "ModAPI.KeyBinding.create")
            .replace("Keyboard.isKeyDown", "ModAPI.KeyBinding.isKeyDown")
            .replace("keyBindings.add", "keyBindings.\$add")
            .replace("keyBindings.get", "keyBindings.\$get")
    }

    /**
     * Detects required modules based on code content
     */
    private fun detectRequiredModules(code: String) {
        mappings.modulePatterns.forEach { (module, patterns) ->
            if (patterns.any { pattern -> code.contains(pattern) }) {
                requiredModules.add(module)
            }
        }
    }
}

/**
 * Example usage
 */
fun main() {
    val converter = ForgeToEaglerConverter()

    val forgeCode = """
        @Mod(modid = "combathelper", version = "2.1", name = "Combat Helper")
        public class CombatHelperMod {
            private static final KeyBinding TOGGLE_KEY = new KeyBinding("key.combathelper.toggle", Keyboard.KEY_P, "key.categories.combat");
            private boolean isEnabled = false;
            
            @SubscribeEvent
            public void onRender(RenderGameOverlayEvent event) {
                if (event.type != RenderGameOverlayEvent.ElementType.TEXT) {
                    return;
                }
                
                EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
                if (player == null) return;
                
                ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
                FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
                
                fr.drawString("Combat Helper: " + (isEnabled ? "§aEnabled" : "§cDisabled"), 5, 5, 0xFFFFFF);
                fr.drawString("Health: §c" + (int)player.getHealth() + "§f/§c" + (int)player.getMaxHealth(), 5, 15, 0xFFFFFF);
                
                ItemStack held = player.getHeldItem();
                if (held != null && held.getItem() instanceof ItemSword) {
                    fr.drawString("Sword Damage: §e+" + getItemDamage(held), 5, 25, 0xFFFFFF);
                }
            }
            
            @SubscribeEvent
            public void onTick(TickEvent.PlayerTickEvent event) {
                if (!isEnabled) return;
                
                EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
                if (player == null) return;
                
                
                if (player.getHealth() < 10.0f) {
                    for (int i = 0; i < 9; i++) {
                        ItemStack stack = player.inventory.getStackInSlot(i);
                        if (stack != null && stack.getItem() == Items.potionitem &&
                            stack.getMetadata() == 16389) { 
                            
                            int prevSlot = player.inventory.currentItem;
                            player.inventory.currentItem = i;
                            Minecraft.getMinecraft().rightClickMouse();
                            player.inventory.currentItem = prevSlot;
                            break;
                        }
                    }
                }
                
                
                List<Entity> entities = player.worldObj.loadedEntityList;
                for (Entity entity : entities) {
                    if (entity instanceof EntityPlayer && !entity.equals(player)) {
                        double distance = player.getDistanceToEntity(entity);
                        if (distance < 4.0 && player.getHeldItem() != null && 
                            player.getHeldItem().getItem() instanceof ItemSword) {
                            player.setItemInUse(player.getHeldItem(), 72000);
                        }
                    }
                }
            }
            
            @SubscribeEvent
            public void onKey(InputEvent.KeyInputEvent event) {
                if (TOGGLE_KEY.isPressed()) {
                    isEnabled = !isEnabled;
                    String message = isEnabled ? "§aCombat Helper Enabled" : "§cCombat Helper Disabled";
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(message));
                }
            }
            
            private double getItemDamage(ItemStack stack) {
                double damage = 0.0;
                if (stack.getItem() instanceof ItemSword) {
                    damage += ((ItemSword)stack.getItem()).getDamageVsEntity();
                    if (stack.isItemEnchanted()) {
                        damage += EnchantmentHelper.getEnchantmentLevel(Enchantments.sharpness.effectId, stack) * 1.25;
                    }
                }
                return damage;
            }
        }
    """.trimIndent()

    try {
        val eaglerCode = converter.convert(forgeCode)
        println("Converted Code:")
        println(eaglerCode)
    } catch (e: Exception) {
        println("Conversion failed: ${e.message}")
        e.printStackTrace()
    }
}