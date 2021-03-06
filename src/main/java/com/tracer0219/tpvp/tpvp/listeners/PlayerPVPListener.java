package com.tracer0219.tpvp.tpvp.listeners;

import com.tracer0219.tpvp.tpvp.TPVP;
import com.tracer0219.tpvp.tpvp.configuration.ConfigManager;
import com.tracer0219.tpvp.tpvp.database.TPVPSQLManager;
import com.tracer0219.tpvp.tpvp.enums.PVPRegion;
import com.tracer0219.tpvp.tpvp.enums.PVPState;
import com.tracer0219.tpvp.tpvp.enums.PlayerState;
import com.tracer0219.tpvp.tpvp.region.RegionHolder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.graalvm.compiler.hotspot.phases.AheadOfTimeVerificationPhase;

import java.util.*;

import static com.tracer0219.tpvp.tpvp.TPVP.*;
import static com.tracer0219.tpvp.tpvp.enums.PVPRegion.GREEN;
import static com.tracer0219.tpvp.tpvp.enums.PVPRegion.RED;


public class PlayerPVPListener implements Listener {
    private static TPVP instance;
    private static String PREFIX;
    private static TPVPSQLManager sql;

    public PlayerPVPListener() {
        instance = TPVP.getInstance();
        PREFIX = ConfigManager.getMessagePrefix();
        sql = TPVP.getSqlManager();
    }

    private static void msg(CommandSender s, String msg) {
        if (s == null) {
            return;
        }
        s.sendMessage(PREFIX + ChatColor.translateAlternateColorCodes('&', msg));
    }

    private static void msgWithoutPrefix(CommandSender s, String msg) {
        if (s == null) {
            return;
        }
        s.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        TPVP.initTPlayer(p);


        String uuid = p.getUniqueId().toString();


        int sin = sql.getSin(uuid);
        int time = sql.getTime(uuid);
        TPVP.initSinState(p, sin, time);

        if (sin > 0) {
            turnRed(p);
            msg(p, "&4&l???????????????????????????: &c&l&n" + sin);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        String uuid = p.getUniqueId().toString();

        //?????????????????????)
        int sin = sinValue(p);
        int time = sinDecreaseTimeRemain(p);

        sql.setSin(uuid, sin);
        sql.setTime(uuid, time);

        if (isInBattle(p) || sin > 0) {
            forceKillPlayer(p);
            event.setQuitMessage(p.getName() + " ???????????????, ????????????");
        }

        TPVP.removeTPlayer(p);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGetInBattle(EntityDamageByEntityEvent evt) {
        if (evt.isCancelled()) {
            return;
        }
        if (evt.getDamager() instanceof Player) {
            Player p = (Player) evt.getDamager();
            if (!isInBattle(p)) {
                msg(p, "&4&l&n??????????????????!????????????????????????!");
            }
            getInBattleState(p);
        } else if (evt.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) evt.getDamager();
            if (arrow.getShooter() != null && arrow.getShooter() instanceof Player) {
                Player p = (Player) arrow.getShooter();
                if (!isInBattle(p)) {
                    msg(p, "&4&l&n??????????????????!????????????????????????!");
                }
                getInBattleState(p);

            }
        }

        if (evt.getEntity() instanceof Player) {
            Player p = (Player) evt.getEntity();
            if (!isInBattle(p)) {
                msg(p, "&4&l&n??????????????????!????????????????????????!");
            }
            getInBattleState(p);
        }

    }

    @EventHandler
    public void onGold(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (isGod(p)) {
                e.setCancelled(true);
                msg(p, "&4&l??????????????????");
            }
        }
    }

    @EventHandler
    public void onKillPlayer(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        if (TPVP.getPlayerState(victim) != PlayerState.WHITE) {
            return;
        }
        EntityDamageEvent lastDamageCause = victim.getLastDamageCause();
        if (lastDamageCause instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent de = (EntityDamageByEntityEvent) lastDamageCause;
            if (victim.getKiller() != null) {
                Player attacker = victim.getKiller();
                TPVP.makePlayerRedWithSinIncrease(attacker);
                TPVP.outOfBattle(victim);
            }
            if (de.getDamager() instanceof Arrow || de.getDamager() instanceof Snowball) {
                Projectile projectile = (Projectile) de.getDamager();
                if (projectile.getShooter() != null && projectile.getShooter() instanceof Player) {
                    Player kill = (Player) projectile.getShooter();
                    TPVP.makePlayerWhite(kill);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        if (victim.hasPermission("tpvp.admin")) {
            return;
        }
        if (TPVP.getPlayerRegion(victim) != RED)
            switch (TPVP.getPlayerState(victim)) {

                case GRAY:
                    List<ItemStack> droppedGray = DropInventoryContent(victim, ConfigManager.getItemDropGray());
                    msg(victim, "&7&l??????????????????????????????????????????????????????&r&e&l&n" + droppedGray.size() + "&r&4&l?????????");
                    for (ItemStack stack : droppedGray) {
                        msgWithoutPrefix(victim, "  &7- &l" + getItemDisplayName(stack));
                    }
                    break;
                case RED:
                    List<ItemStack> droppedRed = DropInventoryContent(victim, ConfigManager.getItemDropRed());
                    msg(victim, "&4&l??????????????????????????????????????????????????????&r&e&l&n" + droppedRed.size() + "&r&4&l?????????");
                    for (ItemStack stack : droppedRed) {
                        msgWithoutPrefix(victim, "  &7- &l" + getItemDisplayName(stack));
                    }
                    break;

            }
    }

    @EventHandler
    public void onRedDeath(PlayerDeathEvent event) {
        Player p = event.getEntity();
        if (TPVP.getPlayerRegion(p) == RED) {
            DropInventoryContent(p, p.getInventory().getContents().length);
            msg(p, "&4&l&n???????????????????????????! ?????????????????????!");
        }
    }

    public static String getItemDisplayName(ItemStack stack) {

        int amount = stack.getAmount();
        if (!stack.hasItemMeta() || !stack.getItemMeta().hasDisplayName()) {
            return stack.getType().name() + " &r&7&lx &r&e&l" + amount;
        }
        return stack.getItemMeta().getDisplayName() + " &r&7&lx &r&e&l" + amount;
    }

    public static List<ItemStack> DropInventoryContent(Player p, int amount) {
        ItemStack[] contents = p.getInventory().getContents();
        List<ItemStack> result = new ArrayList<>();
        List<Integer> idxToDrop = new ArrayList<>();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null && contents[i].getType() != Material.AIR) {
                idxToDrop.add(i);

            }
        }
        for (int i = 0; i < amount; i++) {
            if (idxToDrop.size() < 1) break;
            int j = getRandom(0, idxToDrop.size() - 1);
            int k = idxToDrop.get(j);
            ItemStack drop = contents[k];

            if (allowDrop(drop)) {
                p.getInventory().setItem(k, new ItemStack(Material.AIR));
                Item item = p.getWorld().dropItem(p.getLocation(), drop);
                item.setGlowing(true);
                item.setThrower(p.getUniqueId());
                result.add(drop);
            }
            idxToDrop.remove(j);
        }
        return result;
    }

    public static boolean allowDrop(ItemStack stack) {
        return true;
    }

    public static int getRandom(int min, int max) {
        if (min > max) {
            int s = min;
            min = max;
            max = s;
        }
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    @EventHandler
    public void onAttackPlayer(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player attacker = null;
        if (event.getDamager() instanceof Player)
            attacker = (Player) event.getDamager();
        if (event.getDamager() instanceof Projectile)
            if (((Projectile) event.getDamager()).getShooter() instanceof Player)
                attacker = (Player) ((Projectile) event.getDamager()).getShooter();
        if (attacker == null)
            return;
        Player victim = (Player) event.getEntity();
        if (!doAttack(attacker, victim)) {
            event.setCancelled(true);
        }

    }

    /**
     * @param attacker
     * @param victim
     * @return ????????????????????????
     */
    public static boolean doAttack(Player attacker, Player victim) {
        if (attacker.hasPermission("tpvp.admin")) {
            return true;
        }
        if (RegionHolder.queryPlayerRegion(attacker) != RegionHolder.queryPlayerRegion(victim)) {
            msg(attacker, "&7&l????????????????????????!");
            return false;
        }
        if (TPVP.getPVPState(attacker) == PVPState.PEACE) {
            msg(attacker, "&e&l???????????????????????????????????????!");
            return false;
        } else if (TPVP.getPVPState(attacker) == PVPState.PEACE_AND_SIN && TPVP.getPlayerState(victim) == PlayerState.WHITE) {
            msg(attacker, "&e&l???????????????????????????????????????????????????!");
            return false;
        }

        //??????????????????????????????
        if (TPVP.getPlayerState(victim) != PlayerState.WHITE && TPVP.getPlayerState(attacker) == PlayerState.WHITE) {
            return true;
        }


        //???????????????????????????
        if (TPVP.getPlayerState(attacker) == PlayerState.RED) {
            return true;
        }
        //???????????????????????????
        if (TPVP.getPlayerState(attacker) != PlayerState.WHITE && TPVP.getPlayerState(victim) != PlayerState.WHITE) {
            return true;
        }

        switch (TPVP.getPlayerState(attacker)) {

            case WHITE: //?????????????????? ??????
                TPVP.makePlayerGray(attacker);
            case GRAY: //?????????????????? ??????
                TPVP.makePlayerGray(attacker);
                break;
            case RED:
                break;
        }
        return true;
    }


    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        doPlayerMove(e.getPlayer());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        doPlayerMove(e.getPlayer());
    }


    public static void doPlayerMove(Player p) {
        if (p.hasPermission("tpvp.admin")) {
            return;
        }
        PVPRegion to = RegionHolder.queryPlayerRegion(p);
        PVPRegion from = TPVP.changePlayerRegion(p, to);
        if (to == from) {
            return; //?????????
        }

        if (to != GREEN) {
            TPVP.clearForceKill(p);
        }

        if (to == GREEN && from == RED) {
            forceKillPlayer(p);
            msg(p, "&c&l??????????????????!");
            return;
        }


        if (to == RED && from != RED) {
            msg(p, "&4&l????????????????????????????????????????????????PVP??????!");
            TPVP.changePlayerPVPState(p, PVPState.ALL);
            return;
        }

        //????????????
        if (to == GREEN && from != GREEN) {
            switch (getPlayerState(p)) {
                case WHITE:
                case GRAY:
                    //????????????
                    TPVP.changePlayerPVPState(p, PVPState.PEACE);
                    //???????????????????????????
                    TPVP.startGreenProtectionRefreshCountDown(p);
                    msg(p, "&e&l?????????????????????????????????! ??????????????????????????????! 30????????????????????????");
                    return;
                case RED:
                    msg(p, "&4&l???????????????????????????????????????! ??????&e&l&n" + ConfigManager.getGreenForceKillTime() + "&r&4&l????????????!");
                    TPVP.forceKillStart(p);
                    return;
            }
        }

        //????????????
        if (from == GREEN && to != GREEN) {
            int d = leaveGreen(p);
            if (d >= 0) {
                msg(p, "&4&l???????????????????????????,??????" + d + "???????????????!");
                return;
            }
        }


    }


}