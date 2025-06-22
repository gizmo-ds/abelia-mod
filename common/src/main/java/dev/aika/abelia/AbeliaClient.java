package dev.aika.abelia;

import dev.aika.abelia.api.DonationPlatform;
import dev.aika.abelia.client.config.ClientConfigManager;
import dev.aika.abelia.config.ModConfig;

public final class AbeliaClient {
    public static final ClientConfigManager<ModConfig> CLOTH_CONFIG_MANAGER = new ClientConfigManager<>(Abelia.CONFIG)
            .setDonation(DonationPlatform.AFDIAN, AbeliaConstants.DonateUrl);
}
