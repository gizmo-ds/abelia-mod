package dev.aika.abelia.api;

import lombok.Getter;

public class Donation {
    @Getter
    private final String key;
    @Getter
    private final String url;

    public Donation(String key, String url) {
        this.key = key;
        this.url = url;
    }

    public Donation(DonationPlatform platform, String url) {
        this(platform.toString(), url);
    }
}
