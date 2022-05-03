package com.tracer0219.tpvp.tpvp.configuration;

import com.tracer0219.tpvp.tpvp.TPVP;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class ConfigManager {
    private static TPVP instance;
    private static FileConfiguration config;
    public static boolean initialized = false;

    public static int getTimeGrayDisappear() {
        return TIME_GRAY_DISAPPEAR;
    }

    public static int getSinGainPerKill() {
        return SIN_GAIN_PER_KILL;
    }

    public static int getTimeSinDecrease() {
        return TIME_SIN_DECREASE;
    }

    public static int getItemDropGray() {
        return ITEM_DROP_GRAY;
    }

    public static int getItemDropRed() {
        return ITEM_DROP_RED;
    }

    public static String getMessagePrefix() {
        return MESSAGE_PREFIX;
    }

    public static String getGreenAreaPrefix() {
        return GREEN_AREA_PREFIX;
    }

    public static String getYellowAreaPrefix() {
        return YELLOW_AREA_PREFIX;
    }

    public static String getRedAreaPrefix() {
        return RED_AREA_PREFIX;
    }

    public static int getGodRefreshTime() {
        return GOD_REFRESH_TIME;
    }

    public static int getGodDuration() {
        return GOD_DURATION;
    }

    public static int getGreenForceKillTime() {
        return GREEN_FORCE_KILL_TIME;
    }

    public static String getMessageGetRed() {
        return MESSAGE_GET_RED;
    }

    public static String getMessageGetKill() {
        return MESSAGE_GET_KILL;
    }

    public static int getBattleTime() {
        return BATTLE_TIME;
    }

    private static int TIME_GRAY_DISAPPEAR;
    private static int SIN_GAIN_PER_KILL;
    private static int TIME_SIN_DECREASE;
    private static int ITEM_DROP_GRAY;
    private static int ITEM_DROP_RED;
    private static String MESSAGE_PREFIX;

    private static String MESSAGE_GET_RED;
    private static String MESSAGE_GET_KILL;

    private static String GREEN_AREA_PREFIX;
    private static String YELLOW_AREA_PREFIX;
    private static String RED_AREA_PREFIX;

    private static int GOD_REFRESH_TIME, GOD_DURATION, GREEN_FORCE_KILL_TIME;

    private static int BATTLE_TIME;


    public ConfigManager() {
        instance = TPVP.getInstance();
        File file = new File(instance.getDataFolder(), "config.yml");
        if (!file.exists()) {
            instance.saveDefaultConfig();
        }
        config = instance.getConfig();
    }

    public void loadConfig() {
        MESSAGE_PREFIX = ChatColor.translateAlternateColorCodes('&', config.getString("message_prefix"));
        MESSAGE_GET_RED = ChatColor.translateAlternateColorCodes('&', config.getString("message_get_red"));
        MESSAGE_GET_KILL = ChatColor.translateAlternateColorCodes('&', config.getString("message_get_kill"));

        TIME_GRAY_DISAPPEAR = config.getInt("pvp.time_gray_disappear");
        SIN_GAIN_PER_KILL = config.getInt("pvp.sin_gain_per_kill");
        TIME_SIN_DECREASE = config.getInt("pvp.time_sin_decrease");
        ITEM_DROP_GRAY = config.getInt("pvp.item_drop_gray");
        ITEM_DROP_RED = config.getInt("pvp.item_drop_red");
        BATTLE_TIME=config.getInt("pvp.battle_time");

        GREEN_AREA_PREFIX=config.getString("area.green_prefix");
        YELLOW_AREA_PREFIX=config.getString("area.yellow_prefix");
        RED_AREA_PREFIX=config.getString("area.red_prefix");

        GOD_REFRESH_TIME=config.getInt("area.god_refresh_time");
        GOD_DURATION=config.getInt("area.god_duration");
        GREEN_FORCE_KILL_TIME=config.getInt("area.green_force_kill_time");

        ConfigManager.initialized = true;
    }

}
