package com.medilabosolutions.dto;

public enum ExpectedRisk {
    NONE("None"), BORDERLINE("Bordeline"), INDANGER("InDanger"), EARLYONSET("EarlyOnset");

    private String abreviation;

    private ExpectedRisk(String abreviation) {
        this.abreviation = abreviation;
    }

    public String getAbreviation() {
        return this.abreviation;
    }


}
