package fishcute.breakblocker;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.command.CommandException;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Language;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

@Environment(EnvType.CLIENT)
public class BreakBlocker implements ClientModInitializer {
    public static ArrayList<String> blockedList = new ArrayList<>();
    public static ArrayList<String> entityBlockedList = new ArrayList<>();
    static final ArrayList<String> bannedBlocks = new ArrayList<>(
            Arrays.asList(
                    "water",
                    "lava",
                    "air",
                    "cave_air",
                    "void_air"
            )
    );
    public static final Logger LOGGER = LogManager.getLogger();
    public static Config CONFIG = new Config();
    private static KeyBinding addBlock;
    private static KeyBinding toggleMod;
    private static int breakAnimation = 0;
    public static boolean enabled = true;
    public static boolean disabledBlock() {
        if (!enabled)
            return false;
        else if (CONFIG.blacklistBlocks()) {
            return (MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult && blockedList.contains(getId(((BlockHitResult) MinecraftClient.getInstance().crosshairTarget).getBlockPos())));
        }
        else {
            return (MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult && !blockedList.contains(getId(((BlockHitResult) MinecraftClient.getInstance().crosshairTarget).getBlockPos())));
        }
        }
    static boolean bannedBlock(String i) {
        return bannedBlocks.contains(i);
    }
    public static String blockType() {
        if (!(MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult))
            return "air";
        return getId(((BlockHitResult)MinecraftClient.getInstance().crosshairTarget).getBlockPos());
    }
    public static String getId(BlockPos pos) {
        return MinecraftClient.getInstance().world.getBlockState(pos)
                .getBlock().getName().getString().replaceAll(" ", "_").toLowerCase();
    }
    public static String getId(Block item) {
        return item.getName().getString().replaceAll(" ", "_").toLowerCase();
    }
    static void updateBlocked() {
        String type = blockType();
        if (bannedBlock(type))
            return;
        if (!blockedList.contains(blockType())) {
            CONFIG.addEntryToDisabled(type);
            sendMessage(translateMessage(Formatting.YELLOW, "message.breakblocker.add_block", "%b", type.replaceAll("_", " ")), true);
        }
        else {
            CONFIG.removeEntryFromDisabled(type);
            sendMessage(translateMessage(Formatting.YELLOW, "message.breakblocker.remove_block", "%b", type.replaceAll("_", " ")), true);
        }
    }
    static void updateBlocked(String type) {
        if (bannedBlock(type)) {
            sendMessage(translateMessage(Formatting.RED,"message.breakblocker.cannot_add", "%b", type.replaceAll("_", " ")), true);
        }
        else if (!blockedList.contains(type)) {
            CONFIG.addEntryToDisabled(type);
            sendMessage(translateMessage(Formatting.YELLOW,"message.breakblocker.add_block", "%b", type.replaceAll("_", " ")), true);
        }
        else {
            CONFIG.removeEntryFromDisabled(type);
            sendMessage(translateMessage(Formatting.YELLOW, "message.breakblocker.remove_block", "%b", type.replaceAll("_", " ")), true);
        }
    }
    public static void playMiningAnimation(BlockHitResult blockHitResult) {
        if (CONFIG.shouldPlayMiningAnimation()&&!(MinecraftClient.getInstance().world.getBlockState(blockHitResult.getBlockPos()).isAir())) {
            MinecraftClient.getInstance().particleManager.addBlockBreakingParticles(
                blockHitResult.getBlockPos(),
                blockHitResult.getSide());
            MinecraftClient.getInstance().player.swingHand(Hand.MAIN_HAND);
        }
    }
    static void toggleMod() {
        enabled = !enabled;
        if (enabled)
            sendMessage(Formatting.YELLOW + "Enabled " + name, true);
        else
            sendMessage(Formatting.YELLOW + "Disabled " + name, true);
    }
    public static final String name = "BreakBlocker";
    static void sendHelpMessage() {
        sendMessage(Formatting.YELLOW + "" + Formatting.BOLD + name, false);
        if (CONFIG.blacklistBlocks())
            sendMessage(Formatting.YELLOW + "Current mode: " + Formatting.GREEN + "Blacklist Blocks", false);
        else
            sendMessage(Formatting.YELLOW + "Current mode: " + Formatting.RED + "Whitelist Blocks", false);
        sendMessage(Formatting.YELLOW + "" + Formatting.BOLD + "Available Commands", false);
        sendMessage(Formatting.YELLOW + "- list : Lists all disabled blocks", false);
        sendMessage(Formatting.YELLOW + "- toggle (block_id) : Disables/enables a specific block", false);
        sendMessage(Formatting.YELLOW + "- disable : Disables/enables the mod", false);
        sendMessage(Formatting.YELLOW + "- clear : Clears all entries in the disabled blocks list", false);
        sendMessage(Formatting.YELLOW + "- help : Shows available commands", false);
        sendMessage(Formatting.GRAY + "Created by fishcute", false);
    }
    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing " + name);
        ClientTickEvents.END_WORLD_TICK.register((client) -> {
            while (addBlock.wasPressed()) {
                updateBlocked();
            }
            while (toggleMod.wasPressed()) {
                toggleMod();
            }
        });
        Config.attemptLoadConfig();
        Config.TEXTURE = CONFIG.getIconTexture();
        blockedList = CONFIG.disabledBlocks;

        addBlock = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.breakblocker.toggleblock",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_GRAVE_ACCENT,
                "key.categories.breakblocker"
        ));
        toggleMod = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.breakblocker.togglemod",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                "key.categories.breakblocker"
        ));
    }
    public static int runCommand(StringReader reader) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        try {
            return player.networkHandler.getCommandDispatcher().execute(reader, new ClientCommandSource(player));
        } catch (CommandException e) {
            sendMessage(Formatting.RED + e.getMessage(), false);
        } catch (CommandSyntaxException e) {
            sendMessage(Formatting.RED + Texts.toText(e.getRawMessage()).getString(), false);
        }
        return 1;
    }

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("breakblocker").executes(context -> {
                            sendHelpMessage();
                            return 1;
                        })
                                .then(CommandManager.literal("list").executes(context -> {
                                    if (blockedList.size()>0) {
                                        sendMessage(Formatting.YELLOW + "" + Formatting.BOLD + "Disabled Blocks:", false);
                                        for (String blockType : blockedList) {
                                            sendMessage(Formatting.YELLOW + "- " + blockType, false);
                                        }
                                    }
                                    else
                                        sendMessage(translateMessage(Formatting.RED, "message.breakblocker.no_blocks_disabled"), false);
                                    return 1;
                                }))
                                .then(CommandManager.literal("clear").executes(context -> {
                                    for (String i : blockedList)
                                        CONFIG.removeEntryFromDisabled(i);
                                    sendMessage(translateMessage(Formatting.YELLOW, "message.breakblocker.cleared_list"), false);
                                    return 1;
                                }))
                                .then(CommandManager.literal("help").executes(context -> {
                                    sendHelpMessage();
                                    return 1;
                                }))
                                .then(CommandManager.literal("disable").executes(context -> {
                                    toggleMod();
                                    return 1;
                                }))
                                .then(CommandManager.literal("toggle")
                                        .then(CommandManager.argument("block", new BlockArgumentType())
                                                .executes(context -> {
                                                    updateBlocked(getId(BlockArgumentType.getItemStackArgument(context, "block").getBlock()));
                                                    return 1;
                                                }))
                ));
    }

    static String translateMessage(Formatting format, String key, String from, String to) {
        return format + Language.getInstance().get(key).replaceAll(from, to);
    }
    static String translateMessage(Formatting format, String key) {
        return format + Language.getInstance().get(key);
    }
    public static void sendMessage(String message, boolean actionBar) {
        MinecraftClient.getInstance().player.sendMessage(Text.of(message), actionBar);
    }
    static class ClientCommandSource extends ServerCommandSource {
        public ClientCommandSource(ClientPlayerEntity player) {
            super(player, player.getPos(), player.getRotationClient(), null, 0, player.getEntityName(), player.getName(), null, player);
        }
    }
    //Somehow this works
    static class BlockArgumentType implements ArgumentType<BlockArgument> {
        private static final Collection<String> EXAMPLES = Arrays.asList("cobblestone", "minecraft:cobblestone");

        public BlockArgumentType() {
        }

        public BlockArgument parse(StringReader stringReader) throws CommandSyntaxException {
            BlockArgumentParser itemStringReader = (new BlockArgumentParser(stringReader, false)).parse(false);
            return new BlockArgument(itemStringReader.getBlockState().getBlock());
        }

        public static <S> BlockArgument getItemStackArgument(CommandContext<S> context, String name) {
            return context.getArgument(name, BlockArgument.class);
        }

        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            StringReader stringReader = new StringReader(builder.getInput());
            stringReader.setCursor(builder.getStart());
            BlockArgumentParser blockStringReader = new BlockArgumentParser(stringReader, false);

            try {
                blockStringReader.parse(false);
            } catch (CommandSyntaxException ignored) {
            }

            return blockStringReader.getSuggestions(builder, BlockTags.getTagGroup());
        }

        public Collection<String> getExamples() {
            return EXAMPLES;
        }
    }
    static class BlockArgument implements Predicate<Block> {
        private final Block item;

        public BlockArgument(Block item) {
            this.item = item;
        }

        public Block getBlock() {
            return this.item;
        }

        public boolean test(Block block) {
            return block.equals(this.item);
        }
    }
}
