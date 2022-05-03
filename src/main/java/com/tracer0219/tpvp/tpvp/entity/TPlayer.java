package com.tracer0219.tpvp.tpvp.entity;

import com.tracer0219.tpvp.tpvp.enums.PVPState;
import com.tracer0219.tpvp.tpvp.enums.PlayerState;
import org.bukkit.entity.Player;

public class TPlayer {
    public Player p;
    public PlayerState playerState;
    public PVPState pvpState;

    public boolean readyGod;
    public boolean god;

    public TPlayer(Player p) {
        this.p = p;
        this.pvpState = PVPState.PEACE;
        this.playerState=PlayerState.WHITE;
        this.god=false;
        this.readyGod=false;
    }


}



