package dev.aika.abelia.api;

public enum DonationPlatform {
    PATREON("patreon"),
    AFDIAN("afdian"),
    GITHUB_SPONSORS("github_sponsors"),
    BUY_ME_A_COFFEE("buy_me_a_coffee"),
    KOFI("kofi");

    private final String key;

    DonationPlatform(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return this.key;
    }
}
