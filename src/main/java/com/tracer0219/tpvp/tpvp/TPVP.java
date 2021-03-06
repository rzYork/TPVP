package com.tracer0219.tpvp.tpvp;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.tracer0219.tpvp.tpvp.commands.TPVPCommandExecutor;
import com.tracer0219.tpvp.tpvp.configuration.ConfigManager;
import com.tracer0219.tpvp.tpvp.database.TPVPSQLManager;
import com.tracer0219.tpvp.tpvp.entity.TPlayer;
import com.tracer0219.tpvp.tpvp.enums.PVPRegion;
import com.tracer0219.tpvp.tpvp.enums.PVPState;
import com.tracer0219.tpvp.tpvp.enums.PlayerState;
import com.tracer0219.tpvp.tpvp.listeners.PlayerPVPListener;


import com.tracer0219.tpvp.tpvp.placeholder.PVPExpansion;
import com.tracer0219.tpvp.tpvp.region.RegionHolder;
import mc.obliviate.bloksqliteapi.sqlutils.SQLTable;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;


import java.util.HashMap;

public class TPVP extends JavaPlugin {

    private static int BATTLE_TIME;
    private static TPVP instance;
    private ConfigManager configManager;
    private PlayerPVPListener listener;
    private static String PREFIX;
    private static BukkitRunnable timeCalculatorRunnable;

    private static HashMap<Player, TPlayer> TPlayerHolder = new HashMap<>();
    private static HashMap<TPlayer, Integer> grayTimeHolder = new HashMap<>();
    private static HashMap<TPlayer, Integer> sinHolder = new HashMap<>();
    private static HashMap<TPlayer, Integer> sinTimeHolder = new HashMap<>();
    private static HashMap<TPlayer, PVPRegion> playerCurrentRegion = new HashMap<>();
    private static HashMap<TPlayer, Integer> playerGodRefreshCountDown = new HashMap<>();
    private static HashMap<TPlayer, Integer> playerGodTimeCountDown = new HashMap<>();
    private static HashMap<TPlayer, Integer> forceKillCountDown = new HashMap<>();
    private static HashMap<TPlayer, Integer> battleState = new HashMap<>();

    private static TPVPSQLManager sqlManager;

    public static TPVPSQLManager getSqlManager() {
        return sqlManager;
    }

    private static int GOD_REFRESH_TIME;

    private static int GOD_DURATION;

    private static int GREEN_FORCE_KILL_TIME;


    private static RegionHolder regionHolder;

    public static TPVP getInstance() {
        return TPVP.instance;
    }

    public static void initTPlayer(Player player) {
        TPlayer tP = getTPlayer(player);
        if (grayTimeHolder.get(tP) == null) {
            grayTimeHolder.put(tP, 0);
        }
        if (sinHolder.get(tP) == null) {
            sinHolder.put(tP, 0);
        }
        if (sinTimeHolder.get(tP) == null) {
            sinTimeHolder.put(tP, 0);
        }
        if (playerCurrentRegion.get(tP) == null) {
            playerCurrentRegion.put(tP, PVPRegion.OUTSIDE);
        }
        if (playerGodRefreshCountDown.get(tP) == null) {
            playerGodRefreshCountDown.put(tP, -1);
        }
        if (playerGodTimeCountDown.get(tP) == null) {
            playerGodTimeCountDown.put(tP, -1);
        }
        if (forceKillCountDown.get(tP) == null) {
            forceKillCountDown.put(tP, -1);
        }
        if (battleState.get(tP) == null) {
            battleState.put(tP, -1);
        }

    }


    public static int getBattleTimeRemaining(Player p) {
        return battleState.get(getTPlayer(p));
    }

    public static int getInBattleState(Player p) {
        return battleState.put(getTPlayer(p), BATTLE_TIME);
    }

    public static void forceKillStart(Player p) {
        forceKillCountDown.put(getTPlayer(p), GREEN_FORCE_KILL_TIME);
    }

    public static void clearForceKill(Player p) {
        forceKillCountDown.put(getTPlayer(p), -1);
    }

    public static void initSinState(Player p, int sin, int time) {
        TPlayer tP = getTPlayer(p);
        sinHolder.put(tP, sin);
        sinTimeHolder.put(tP, time);
    }


    private void checkDependency() {
        PluginManager pm = getServer().getPluginManager();
        if (pm.getPlugin("PlaceholderAPI") == null) {
            getLogger().info("PlaceholderAPI?????????!");
            pm.disablePlugin(this);
            return;
        }

        if (pm.getPlugin("PlayerPoints") == null) {
            getLogger().info("PlayerPoints?????????!");
            pm.disablePlugin(this);
            return;
        }

        if (pm.getPlugin("WorldGuard") == null) {
            getLogger().info("WorldGuard?????????!");
            pm.disablePlugin(this);
            return;
        }

        if (pm.getPlugin("Vault") == null) {
            getLogger().info("Vault?????????!");
            pm.disablePlugin(this);
            return;
        }
    }


    @Override
    public void onEnable() {

        //?????????????????????
        instance = this;

        //????????????
        checkDependency();


        //??????????????????????????????
        configManager = new ConfigManager();
        configManager.loadConfig();

        PREFIX = ConfigManager.getMessagePrefix();

        //???????????????
        GOD_REFRESH_TIME = ConfigManager.getGodRefreshTime();
        GOD_DURATION = ConfigManager.getGodDuration();
        GREEN_FORCE_KILL_TIME = ConfigManager.getGreenForceKillTime();
        BATTLE_TIME = ConfigManager.getBattleTime();

        //??????????????????
        sqlManager = new TPVPSQLManager(this);


        //??????????????????
        listener = new PlayerPVPListener();
        getServer().getPluginManager().registerEvents(listener, this);


        //????????????
        TPVPCommandExecutor executor = new TPVPCommandExecutor();
        getServer().getPluginCommand("tpvp").setExecutor(executor);

        //??????????????????
        regionHolder = new RegionHolder();


        //???????????????????????????????????????
        TPlayerHolder.clear();
        getServer().getOnlinePlayers().stream().forEach(p -> {
            initTPlayer(p);
        });

        //???????????????
        startTimeCalculator();


        //??????PlaceholderAPI ??????
        PVPExpansion pvpExpansion = new PVPExpansion();
        pvpExpansion.register();

        WorldGuardPlugin.inst();


    }

    @Override
    public void onDisable() {
        TPlayerHolder.entrySet().stream().forEach(e->
        {
            int sin=sinHolder.get(e.getValue());
            int time=sinTimeHolder.get(e.getValue());
            sqlManager.setTime(e.getKey().getUniqueId().toString(),time);
            sqlManager.setSin(e.getKey().getUniqueId().toString(),sin);
        });
        instance = null;
    }

    public static PVPState getPVPState(Player p) {
        return getTPlayer(p).pvpState;
    }

    public static PlayerState getPlayerState(Player p) {
        return getTPlayer(p).playerState;
    }

    public static boolean isInBattle(Player p) {
        return getBattleTimeRemaining(p) >= 0;
    }

    private void startTimeCalculator() {
        timeCalculatorRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                grayTimeHolder.entrySet().stream().forEach(e -> {
                    if (e.getValue() == null) {
                        e.setValue(0);
                    }

                    if (e.getValue() > 0) {
                        e.setValue(e.getValue() - 1);
                        if (e.getValue() <= 0) {
                            makePlayerWhite(e.getKey().p);
                        }
                    }
                });
                sinTimeHolder.entrySet().stream().forEach(e ->
                {
                    e.setValue(e.getValue() == null ? 0 : e.getValue());
                    if (e.getValue() > 0) {
                        e.setValue(e.getValue() - 1);
                    } else {
                        if (sinHolder.get(e.getKey()) == null)
                            sinHolder.put(e.getKey(), 0);

                        if (sinHolder.get(e.getKey()) > 0) {
                            sinHolder.put(e.getKey(), sinHolder.get(e.getKey()) - 1);
                            e.setValue(ConfigManager.getTimeSinDecrease());
                            if (sinHolder.get(e.getKey()) <= 0) {
                                makePlayerWhite(e.getKey().p);
                                sinTimeHolder.put(e.getKey(), 0);
                            }
                        }
                    }
                });

                playerGodRefreshCountDown.entrySet().stream().forEach(e -> {
                    if (e.getValue() == null) {
                        e.setValue(-1);
                    }

                    if (e.getValue() == 0) {
                        e.getKey().readyGod = true;
                    }

                    if (e.getValue() >= 0) {
                        e.setValue(e.getValue() - 1);
                    }

                });

                playerGodTimeCountDown.entrySet().stream().forEach(e -> {
                    if (e.getValue() == null) {
                        e.setValue(-1);
                    }
                    if (e.getValue() == 0) {
                        e.getKey().god = false;
                    }
                    if (e.getValue() >= 0) {
                        e.setValue(e.getValue() - 1);
                    }

                });

                forceKillCountDown.entrySet().stream().forEach(e -> {
                    if (e.getValue() == null)
                        e.setValue(-1);
                    if (e.getValue() == 0) {
                        greenForceKill(e.getKey());
                    }
                    if (e.getValue() >= 0) {
                        e.setValue(e.getValue() - 1);
                        e.getKey().p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1, false, true, true));
                    }

                });

                battleState.entrySet().stream().forEach(e -> {
                    if (e.getValue() == null)
                        e.setValue(-1);
                    if (e.getValue() == 0) {
                        outOfBattle(e.getKey().p);
                    }
                    if (e.getValue() >= 0) {
                        e.setValue(e.getValue() - 1);
                    }
                });

                sinHolder.entrySet().stream().forEach(e->{
                    if(e.getValue()>0)
                        e.getKey().playerState=PlayerState.RED;
                });


            }
        };

        timeCalculatorRunnable.runTaskTimer(instance, 0L, 20L);
    }

    public static void outOfBattle(Player p) {
        battleState.put(getTPlayer(p),0);
        msg(p, "&4&l????????????????????????!");
    }

    private static void greenForceKill(TPlayer tP) {
        forceKillPlayer(tP.p);
        msg(tP.p, "&4&l??????????????????");
    }

    public static void forceKillPlayer(Player p) {
        double h = p.getHealth();
        p.setHealth(0);
        p.setLastDamageCause(new EntityDamageEvent(p, EntityDamageEvent.DamageCause.FALL, h));
    }

    public static PVPRegion getPlayerRegion(Player p) {
        return playerCurrentRegion.get(getTPlayer(p));
    }

    /**
     * @param p
     * @param newRegion
     * @return oldRegion
     */
    public static PVPRegion changePlayerRegion(Player p, PVPRegion newRegion) {

        return playerCurrentRegion.put(getTPlayer(p), newRegion);
    }

    public static int getGreenForceKillTime(Player p) {
        return forceKillCountDown.get(getTPlayer(p));
    }


    /**
     * ??????????????????????????????buff??????
     *
     * @param p
     */
    public static void startGreenProtectionRefreshCountDown(Player p) {
        playerGodRefreshCountDown.put(getTPlayer(p), GOD_REFRESH_TIME);
    }


    /**
     * ????????????????????????
     */
    public static void clearGodRefresh(Player p) {
        playerGodRefreshCountDown.put(getTPlayer(p), -1);
    }

    /**
     * ????????????
     *
     * @param p
     * @return ??????????????????????????????????????????-1
     */
    public static int leaveGreen(Player p) {
        TPlayer tP = getTPlayer(p);
        if (tP.readyGod == true) {
            playerGodTimeCountDown.put(getTPlayer(p), GOD_DURATION);
            tP.god = true;
            tP.readyGod = false;
            return GOD_DURATION;
        }
        clearGodRefresh(p);
        return -1;
    }

    public static int getGodRefreshRemaining(Player p) {
        return playerGodRefreshCountDown.get(getTPlayer(p));
    }

    public static int getGodRemaining(Player p) {
        return playerGodTimeCountDown.get(getTPlayer(p));
    }

    public static boolean isGod(Player p) {
        return getTPlayer(p).god;
    }

    public static boolean isReadGod(Player p) {
        return getTPlayer(p).readyGod;
    }

    public static PVPState changePlayerPVPState(Player p, PVPState state) {
        getTPlayer(p).pvpState = state;
        return getTPlayer(p).pvpState;
    }

    private static TPlayer getTPlayer(Player p) {
        if (p == null || !p.isOnline()) {
            removeTPlayer(p);
            return null;
        }
        TPlayer tP = TPlayerHolder.get(p);
        if (tP == null) {
            TPlayerHolder.put(p, new TPlayer(p));
            tP = TPlayerHolder.get(p);
        }
        return tP;
    }

    public static void removeTPlayer(Player p) {
        if (p != null) {
            TPlayerHolder.remove(p);
            playerCurrentRegion.remove(p);
            playerGodTimeCountDown.remove(p);
            playerGodRefreshCountDown.remove(p);
            forceKillCountDown.remove(p);
            sinHolder.remove(p);
            sinTimeHolder.remove(p);
            grayTimeHolder.remove(p);
        }
    }


    /**
     * ?????????????????????????????????????????????????????????
     *
     * @param p
     */
    public static void makePlayerGray(Player p) {
        if (p.hasPermission("tpvp.admin")) {
            return;
        }
        TPlayer tP = getTPlayer(p);
        boolean grayFistTime = tP.playerState == PlayerState.WHITE;
        tP.playerState = PlayerState.GRAY;
        int grayTime = ConfigManager.getTimeGrayDisappear();
        grayTimeHolder.put(tP, grayTime);
        if (grayFistTime)
            msg(p, "???????????????????????????! " + grayTime + "?????????????????????????????????????????????????????????!");

    }


    /**
     * ?????????????????????
     *
     * @param p
     */
    public static void makePlayerWhite(Player p) {
        TPlayer tP = getTPlayer(p);
        tP.playerState = PlayerState.WHITE;
        msg(p, "&7&l??????????????????????????????!");
    }


    public static void makePlayerRedWithSinIncrease(Player p) {
        if (p.hasPermission("tpvp.admin")) {
            return;
        }
        TPlayer tP = getTPlayer(p);
        boolean firstTimeGetInRed = tP.playerState != PlayerState.RED;
        tP.playerState = PlayerState.RED;
        int sinTime = ConfigManager.getTimeSinDecrease();
        int sinToAdd = ConfigManager.getSinGainPerKill();
        if (sinHolder.get(tP) == null)
            sinHolder.put(tP, 0);
        sinHolder.put(tP, sinHolder.get(tP) + sinToAdd);
        sinTimeHolder.put(tP, sinTime);
        if (firstTimeGetInRed) {
            msg(p, PlaceholderAPI.setPlaceholders(p, ConfigManager.getMessageGetRed()));
        } else {
            msg(p, PlaceholderAPI.setPlaceholders(p, ConfigManager.getMessageGetKill()));
        }
        grayTimeHolder.put(tP, 0);

    }

    public static void turnRed(Player p) {
        getTPlayer(p).playerState = PlayerState.RED;
    }


    public static int grayTimeRemain(Player p) {
        TPlayer tP = getTPlayer(p);
        Integer i = grayTimeHolder.get(tP);
        return i == null ? 0 : i;
    }

    public static int sinDecreaseTimeRemain(Player p) {
        TPlayer tP = getTPlayer(p);
        Integer i = sinTimeHolder.get(tP);
        return i == null ? 0 : i;
    }

    public static int sinValue(Player p) {
        TPlayer tP = getTPlayer(p);
        Integer i = sinHolder.get(tP);
        return i == null ? 0 : i;
    }

    public static String playerState(Player p) {
        TPlayer tp = getTPlayer(p);
        switch (tp.playerState) {

            case WHITE:
                return "???";

            case GRAY:
                return "???";

            case RED:
                return "???";

        }
        return "N/A";
    }

    public static String playerPVPState(Player p) {
        TPlayer tPlayer = getTPlayer(p);
        switch (tPlayer.pvpState) {

            case PEACE:
                return "??????";
            case PEACE_AND_SIN:
                return "??????";
            case TEAM:
                return "??????";
            case ALL:
                return "??????";
        }
        return "N/A";
    }

    public static void msg(CommandSender s, String msg) {
        if (s == null) {
            return;
        }
        s.sendMessage(PREFIX + ChatColor.translateAlternateColorCodes('&', msg));
    }
}
