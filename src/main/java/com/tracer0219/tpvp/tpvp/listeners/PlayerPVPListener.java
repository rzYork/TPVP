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
            msg(p, "&4&l您的罪恶值已然恢复: &c&l&n" + sin);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        String uuid = p.getUniqueId().toString();

        //惩罚罪恶（红名)
        int sin = sinValue(p);
        int time = sinDecreaseTimeRemain(p);

        sql.setSin(uuid, sin);
        sql.setTime(uuid, time);

        if (isInBattle(p) || sin > 0) {
            forceKillPlayer(p);
            event.setQuitMessage(p.getName() + " 逃离了战斗, 已被处死");
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
                msg(p, "&4&l&n您发起了攻击!已经进入战斗状态!");
            }
            getInBattleState(p);
        } else if (evt.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) evt.getDamager();
            if (arrow.getShooter() != null && arrow.getShooter() instanceof Player) {
                Player p = (Player) arrow.getShooter();
                if (!isInBattle(p)) {
                    msg(p, "&4&l&n您发起了攻击!已经进入战斗状态!");
                }
                getInBattleState(p);

            }
        }

        if (evt.getEntity() instanceof Player) {
            Player p = (Player) evt.getEntity();
            if (!isInBattle(p)) {
                msg(p, "&4&l&n您受到了攻击!已经进入战斗状态!");
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
                msg(p, "&4&l受到无敌保护");
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
                    msg(victim, "&7&l由于您处于灰名的死亡，你丢失以下共计&r&e&l&n" + droppedGray.size() + "&r&4&l件物品");
                    for (ItemStack stack : droppedGray) {
                        msgWithoutPrefix(victim, "  &7- &l" + getItemDisplayName(stack));
                    }
                    break;
                case RED:
                    List<ItemStack> droppedRed = DropInventoryContent(victim, ConfigManager.getItemDropRed());
                    msg(victim, "&4&l由于您处于红名的死亡，你丢失以下共计&r&e&l&n" + droppedRed.size() + "&r&4&l件物品");
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
            msg(p, "&4&l&n由于你在红区的死亡! 全部物品已掉落!");
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
     * @return 是否允许攻击发生
     */
    public static boolean doAttack(Player attacker, Player victim) {
        if (attacker.hasPermission("tpvp.admin")) {
            return true;
        }
        if (RegionHolder.queryPlayerRegion(attacker) != RegionHolder.queryPlayerRegion(victim)) {
            msg(attacker, "&7&l无法发起跨区攻击!");
            return false;
        }
        if (TPVP.getPVPState(attacker) == PVPState.PEACE) {
            msg(attacker, "&e&l您处于和平状态无法发起攻击!");
            return false;
        } else if (TPVP.getPVPState(attacker) == PVPState.PEACE_AND_SIN && TPVP.getPlayerState(victim) == PlayerState.WHITE) {
            msg(attacker, "&e&l在善恶模式下您无法攻击一位白名玩家!");
            return false;
        }

        //白名攻击非白名无处理
        if (TPVP.getPlayerState(victim) != PlayerState.WHITE && TPVP.getPlayerState(attacker) == PlayerState.WHITE) {
            return true;
        }


        //红名攻击玩家无处理
        if (TPVP.getPlayerState(attacker) == PlayerState.RED) {
            return true;
        }
        //非白名互殴不做处理
        if (TPVP.getPlayerState(attacker) != PlayerState.WHITE && TPVP.getPlayerState(victim) != PlayerState.WHITE) {
            return true;
        }

        switch (TPVP.getPlayerState(attacker)) {

            case WHITE: //白名攻击白名 变灰
                TPVP.makePlayerGray(attacker);
            case GRAY: //灰名攻击白名 变灰
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
            return; //未换区
        }

        if (to != GREEN) {
            TPVP.clearForceKill(p);
        }

        if (to == GREEN && from == RED) {
            forceKillPlayer(p);
            msg(p, "&c&l非法红区跨越!");
            return;
        }


        if (to == RED && from != RED) {
            msg(p, "&4&l您已经进入红区，已经强制进入全体PVP模式!");
            TPVP.changePlayerPVPState(p, PVPState.ALL);
            return;
        }

        //进入绿区
        if (to == GREEN && from != GREEN) {
            switch (getPlayerState(p)) {
                case WHITE:
                case GRAY:
                    //强制和平
                    TPVP.changePlayerPVPState(p, PVPState.PEACE);
                    //出区保护开启倒计时
                    TPVP.startGreenProtectionRefreshCountDown(p);
                    msg(p, "&e&l您已经进入绿区安全区域! 已经强制开启和平模式! 30秒后获得出区保护");
                    return;
                case RED:
                    msg(p, "&4&l您已经在红名状态下进入绿区! 请在&e&l&n" + ConfigManager.getGreenForceKillTime() + "&r&4&l秒内离开!");
                    TPVP.forceKillStart(p);
                    return;
            }
        }

        //离开绿区
        if (from == GREEN && to != GREEN) {
            int d = leaveGreen(p);
            if (d >= 0) {
                msg(p, "&4&l您已离开绿区安全区,共有" + d + "秒无敌时间!");
                return;
            }
        }


    }


}