package com.medilabosolutions.model;

public enum ExpectedRisk {
    NONE("None"), BORDERLINE("Borderline"), INDANGER("In Danger"), EARLYONSET("Early onset");

    private String abreviation;

    private ExpectedRisk(String abreviation) {
        this.abreviation = abreviation;
    }

    public String getAbreviation() {
        return this.abreviation;
    }


}
