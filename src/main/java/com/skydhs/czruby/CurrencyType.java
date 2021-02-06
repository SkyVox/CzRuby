package com.skydhs.czruby;

public enum CurrencyType {
    FRAGMENT("Fragmento"),
    RUBY("Rubi");

    private String name;

    CurrencyType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}