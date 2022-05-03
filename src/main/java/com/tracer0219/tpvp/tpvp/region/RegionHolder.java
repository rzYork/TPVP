package com.tracer0219.tpvp.tpvp.region;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.tracer0219.tpvp.tpvp.TPVP;
import com.tracer0219.tpvp.tpvp.configuration.ConfigManager;
import com.tracer0219.tpvp.tpvp.enums.PVPRegion;
import org.bukkit.entity.Player;


public class RegionHolder {
    private ProtectedCuboidRegion region;
    private static WorldGuard wgInstance;
    private static TPVP instance;
    private static RegionContainer rc ;
    private static RegionQuery query ;
    private static String GREEN_REGION_PREFIX,YELLOW_REGION_PREFIX,RED_REGION_PREFIX;

    public RegionHolder() {
        wgInstance= WorldGuard.getInstance();
        instance=TPVP.getInstance();
        rc= wgInstance.getPlatform().getRegionContainer();;
        query= rc.createQuery();

        GREEN_REGION_PREFIX=ConfigManager.getGreenAreaPrefix();
        YELLOW_REGION_PREFIX=ConfigManager.getYellowAreaPrefix();
        RED_REGION_PREFIX= ConfigManager.getRedAreaPrefix();
    }


    public static PVPRegion queryPlayerRegion(Player p){

        if(query==null){
            new NullPointerException("RegionHolder is not initialized!").printStackTrace();
            return PVPRegion.OUTSIDE;
        }

        ApplicableRegionSet regions = query.getApplicableRegions(BukkitAdapter.adapt(p.getLocation()));
        int level=0;
        for (ProtectedRegion region : regions) {
            if(region.getId().startsWith(GREEN_REGION_PREFIX)){
                if(level<1)
                    level=1;
            }else  if(region.getId().startsWith(YELLOW_REGION_PREFIX)){
                if(level<2)
                    level=2;
            }else if(region.getId().startsWith(RED_REGION_PREFIX)){
                if(level<3)
                    level=3;
            }
        }
        switch (level){
            case 0:
                return PVPRegion.OUTSIDE;
            case 1:
                return PVPRegion.GREEN;
            case 2:
                return PVPRegion.YELLOW;
            case 3:
                return PVPRegion.RED;
            default:
                throw new IllegalStateException("Unexpected Level value: " + level);
        }

    }




}