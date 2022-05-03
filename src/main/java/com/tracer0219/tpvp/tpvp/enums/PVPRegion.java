package com.tracer0219.tpvp.tpvp.enums;

public enum PVPRegion {
    GREEN("绿区"),YELLOW("黄区"),RED("红区"),OUTSIDE("区外");

    private String display;

    private PVPRegion(String display){
        this.display=display;
    }
    private PVPRegion(){
        this("N/A");
    }
    public String displayName() {
        return display;
    }
}
