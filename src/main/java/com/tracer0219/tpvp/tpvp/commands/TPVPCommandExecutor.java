package com.tracer0219.tpvp.tpvp.commands;

import com.tracer0219.tpvp.tpvp.TPVP;
import com.tracer0219.tpvp.tpvp.enums.PVPRegion;
import com.tracer0219.tpvp.tpvp.enums.PVPState;
import com.tracer0219.tpvp.tpvp.region.RegionHolder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class TPVPCommandExecutor implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (s.equalsIgnoreCase("tpvp")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§4§l只有玩家可以使用这个指令!");
                return true;
            }
            Player p = (Player) sender;
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("?")) {
                    sender.sendMessage("§r§8§l========§2§l▽§r§8§l=========");
                    sender.sendMessage("§2§l/tpvp p   §r§8§l进入和平模式");
                    sender.sendMessage("§6§l/tpvp pns §r§8§l进入善恶模式");
                    sender.sendMessage("§5§l/tpvp t   §r§8§l进入组队模式");
                    sender.sendMessage("§4§l/tpvp a   §r§8§l进入全体攻击");
                    sender.sendMessage("§r§8§l========§2§l△§r§8§l=========");
                    return true;
                } else if (args[0].equalsIgnoreCase("p")) {
                    if(!p.hasPermission("tpvp.admin")){
                        switch (RegionHolder.queryPlayerRegion(p)) {
                            case GREEN:
                            case YELLOW:
                            case OUTSIDE:
                                break;
                            case RED:
                                p.sendMessage("§r§4§l红区强制全体攻击模式!");
                                return true;
                        }
                    }
                    TPVP.changePlayerPVPState(p,PVPState.PEACE);
                    p.sendMessage("§r§8§l进入和平模式");
                    return true;

                } else if (args[0].equalsIgnoreCase("pns")) {
                    if(!p.hasPermission("tpvp.admin")&&RegionHolder.queryPlayerRegion(p)== PVPRegion.GREEN){
                        p.sendMessage("§r§4§l绿区强制和平模式!");
                        return true;
                    }
                    TPVP.changePlayerPVPState(p,PVPState.PEACE_AND_SIN);
                    p.sendMessage("§r§8§l进入善恶模式");
                    return true;

                } else if (args[0].equalsIgnoreCase("t")) {
                    if(!p.hasPermission("tpvp.admin")){
                        p.sendMessage("§r§8§l未开放");
                        return true;
                    }
                    if(!p.hasPermission("tpvp.admin")&&RegionHolder.queryPlayerRegion(p)== PVPRegion.GREEN){
                        p.sendMessage("§r§4§l绿区强制和平模式!");
                        return true;
                    }
                    TPVP.changePlayerPVPState(p,PVPState.TEAM);
                    p.sendMessage("§r§8§l进入组队模式");
                    return true;
                } else if (args[0].equalsIgnoreCase("a")) {
                    if(!p.hasPermission("tpvp.admin")&&RegionHolder.queryPlayerRegion(p)== PVPRegion.GREEN){
                        p.sendMessage("§r§4§l绿区强制和平模式!");
                        return true;
                    }
                    TPVP.changePlayerPVPState(p,PVPState.ALL);
                    p.sendMessage("§r§8§l进入全体攻击模式");
                    return true;

                }
            }
        }
        return false;
    }
}
