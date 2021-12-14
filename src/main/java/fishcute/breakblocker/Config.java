package fishcute.breakblocker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.io.*;
import java.util.ArrayList;

public class Config {
    static File configFile = FabricLoader.getInstance().getConfigDir().resolve("blockblocker_config.json").toFile();
    public static Identifier TEXTURE;
    public static void attemptLoadConfig() {
        if (configFile.exists()) {
            try {
                ConfigData data = Config.loadConfigFile(configFile);
                BreakBlocker.CONFIG = new Config(data);
                Config.writeConfigFile(configFile, data);
            } catch (Exception ex) {
                BreakBlocker.LOGGER.error("Something went wrong while loading the config file, using default config file");
                ex.printStackTrace();
            }
        } else {
            try {
                Config.writeConfigFile(configFile, new ConfigData());
            } catch (Exception ex) {
                BreakBlocker.LOGGER.error("Something went wrong while creating a default config. Please report this to the mod author");
                ex.printStackTrace();
            }
        }
    }
    public void addEntryToDisabled(String entry) {
        try {
            ArrayList<String> entryAdded = (ArrayList<String>) disabledBlocks.clone();
            entryAdded.add(entry);
            writeConfigFile(configFile, new ConfigData(entryAdded));
            attemptLoadConfig();
            BreakBlocker.blockedList = BreakBlocker.CONFIG.disabledBlocks;
        }
        catch (Exception ignored) {}
    }
    public void removeEntryFromDisabled(String entry) {
        try {
            ArrayList<String> entryAdded = (ArrayList<String>) disabledBlocks.clone();
            entryAdded.remove(entry);
            writeConfigFile(configFile, new ConfigData(entryAdded));
            attemptLoadConfig();
            BreakBlocker.blockedList = BreakBlocker.CONFIG.disabledBlocks;
        }
        catch (Exception ignored) {}
    }
    private String indicatorType;
    private int indicatorX;
    private int indicatorY;
    private float r;
    private float g;
    private float b;
    private float a;
    private int size;
    private String iconTexture;
    public ArrayList<String> disabledBlocks;
    private boolean blackListBlocks;
    private boolean miningAnimation;
    private int cooldownDisabledBlock;
    public Config(ConfigData confileFileFormat) {
        indicatorType = confileFileFormat.indicatorType;
        indicatorX = confileFileFormat.iconX;
        indicatorY = confileFileFormat.iconY;
        disabledBlocks = confileFileFormat.disabledBlocks;
        r = confileFileFormat.crosshairRed;
        g = confileFileFormat.crosshairGreen;
        b = confileFileFormat.crosshairBlue;
        a = confileFileFormat.crosshairAlpha;
        size = confileFileFormat.iconSize;
        iconTexture = confileFileFormat.iconType;
        blackListBlocks = confileFileFormat.blackListBlocks;
        miningAnimation = confileFileFormat.playMiningAnimation;
        cooldownDisabledBlock = confileFileFormat.cooldownAfterMiningDisabledBlock;
    }

    public Config() {
        this(new ConfigData());
    }

    public Indicator getIndicatorType() {
        switch (this.indicatorType) {
            case "crosshair":
                return Indicator.CROSSHAIR;
            case "icon":
                return Indicator.ICON;
            case "outline":
                return Indicator.OUTLINE;
            case "none":
                return Indicator.NONE;
        }
        return Indicator.NONE;
    }
    public int getCooldown() {
        return this.cooldownDisabledBlock;
    }
    public int getIndicatorX() {
        return this.indicatorX;
    }
    public int getIndicatorY() {
        return this.indicatorY;
    }
    public int getIndicatorSize() { return this.size; }
    public float getR() {
        return this.r;
    }
    public float getG() {
        return this.g;
    }
    public float getB() {
        return this.b;
    }
    public float getA() {
        return this.a;
    }
    public boolean shouldPlayMiningAnimation() {
        return this.miningAnimation;
    }
    public boolean blacklistBlocks() {
        return this.blackListBlocks;
    }
    public Identifier getIconTexture() {
        switch (iconTexture) {
            case "red_x":
                return new Identifier("breakblocker:textures/icon_red_x.png");
            case "pickaxe":
                return new Identifier("breakblocker:textures/icon_pickaxe.png");
            case "x":
                return new Identifier("breakblocker:textures/icon_x.png");
            case "alert":
                return new Identifier("breakblocker:textures/icon_alert.png");
            default:
                return new Identifier("breakblocker:textures/icon_star.png");
        }
    }

    public static ConfigData loadConfigFile(File configFile) throws IOException {
        FileReader reader = null;
        try {
            Gson gson = new Gson();
            reader = new FileReader(configFile);
            return gson.fromJson(reader, ConfigData.class);
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    public static void writeConfigFile(File configFile, ConfigData data) throws IOException {
        FileWriter writer = null;
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            writer = new FileWriter(configFile);
            writer.write(gson.toJson(data));
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public static class ConfigData implements Serializable {
        @Expose
        private boolean playMiningAnimation = true;
        @Expose
        private String indicatorType = "icon";
        @Expose
        private int iconX = -4;
        @Expose
        private int iconY = 8;
        @Expose
        private int iconSize = 9;
        @Expose
        private String iconType = "star";
        @Expose
        private ArrayList<String> disabledBlocks = new ArrayList<>();
        @Expose
        private boolean blackListBlocks = true;
        @Expose
        private float crosshairRed = 1F;
        @Expose
        private float crosshairGreen = 0F;
        @Expose
        private float crosshairBlue = 0F;
        @Expose
        private float crosshairAlpha = 1F;
        @Expose
        private int cooldownAfterMiningDisabledBlock = 3;
        public ConfigData(ArrayList<String> disabledBlocks) {
            this.disabledBlocks = disabledBlocks;
            this.indicatorType = BreakBlocker.CONFIG.getIndicatorType().toString().toLowerCase();
            this.iconX = BreakBlocker.CONFIG.getIndicatorX();
            this.iconY = BreakBlocker.CONFIG.getIndicatorY();
            this.iconSize = BreakBlocker.CONFIG.getIndicatorSize();
            this.iconType = BreakBlocker.CONFIG.iconTexture;
            this.crosshairRed = BreakBlocker.CONFIG.getR();
            this.crosshairGreen = BreakBlocker.CONFIG.getG();
            this.crosshairBlue = BreakBlocker.CONFIG.getB();
            this.crosshairAlpha = BreakBlocker.CONFIG.getA();
            this.playMiningAnimation = BreakBlocker.CONFIG.miningAnimation;
            this.cooldownAfterMiningDisabledBlock = BreakBlocker.CONFIG.cooldownDisabledBlock;
        }
        public ConfigData() {}
    }
    public enum Indicator {
        CROSSHAIR,
        ICON,
        OUTLINE,
        NONE
    }
}
