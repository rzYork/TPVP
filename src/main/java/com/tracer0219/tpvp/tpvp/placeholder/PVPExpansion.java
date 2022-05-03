package com.tracer0219.tpvp.tpvp.placeholder;

import com.tracer0219.tpvp.tpvp.TPVP;
import com.tracer0219.tpvp.tpvp.configuration.ConfigManager;
import com.tracer0219.tpvp.tpvp.entity.TPlayer;
import com.tracer0219.tpvp.tpvp.region.RegionHolder;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public class PVPExpansion extends PlaceholderExpansion {


    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if(player.isOnline()){
            Player p= Bukkit.getPlayer(player.getUniqueId());
            if(params.equalsIgnoreCase("gray_time")){
                return String.valueOf(TPVP.grayTimeRemain(p));
            }else if(params.equalsIgnoreCase("red_time")){
                return String.valueOf(TPVP.sinDecreaseTimeRemain(p));
            }else  if(params.equalsIgnoreCase("red_sin")){
                return String.valueOf(TPVP.sinValue(p));
            }else if(params.equalsIgnoreCase("state")){
                return TPVP.playerState(p);
            }else if(params.equalsIgnoreCase("time_gray_disappear")){
                return String.valueOf(ConfigManager.getTimeGrayDisappear());
            }else if(params.equalsIgnoreCase("sin_gain_per_kill")){
                return String.valueOf(ConfigManager.getSinGainPerKill());
            }else if(params.equalsIgnoreCase("time_sin_decrease")){
                return String.valueOf(ConfigManager.getTimeSinDecrease());
            }else if(params.equalsIgnoreCase("item_drop_gray")){
                return String.valueOf(ConfigManager.getItemDropGray());
            }else if(params.equalsIgnoreCase("item_drop_red")){
                return String.valueOf(ConfigManager.getItemDropRed());
            }else if(params.equalsIgnoreCase("pvp_state")){
                return TPVP.playerPVPState(p);
            }else if(params.equalsIgnoreCase("pvp_area")){
                return RegionHolder.queryPlayerRegion(p).displayName();
            }else if(params.equalsIgnoreCase("god_refresh")){
                return TPVP.getGodRefreshRemaining(p)+"s";
            }else if(params.equalsIgnoreCase("god")){
                return TPVP.isGod(p)?"是":"否";
            }else if(params.equalsIgnoreCase("is_ready_god")){
                return TPVP.isReadGod(p)?"是":"否";
            }else if(params.equalsIgnoreCase("god_duration")){
                return TPVP.getGodRemaining(p)+"s";
            }else if(params.equalsIgnoreCase("force_kill")){
                return TPVP.getGreenForceKillTime(p)+"s";
            }else if(params.equalsIgnoreCase("is_in_battle")){
                return TPVP.isInBattle(p)?"是":"否";
            } else if (params.equalsIgnoreCase("battle_time")) {
                return TPVP.getBattleTimeRemaining(p)+"s";
            }
        }
        return super.onRequest(player, params);

        /**
         * %tpvp_god_refresh% 无敌模式刷新时间剩余
         *
         * %tpvp_god% 是否开启了无敌
         *
         * %tpvp_god_duration% 剩余无敌时间
         *
         * %tpvp_is_ready_god% 是否已刷新好出区保护
         */

    }
    @Override
    public @NotNull String getIdentifier() {
        return "tpvp";
    }

    @Override
    public @NotNull String getAuthor() {
        return "tracer";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }
}
