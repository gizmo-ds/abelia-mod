package dev.aika.abelia.config;

import dev.aika.abelia.Abelia;
import dev.aika.abelia.annotation.config.AbeliaConfig;
import dev.aika.abelia.annotation.config.Comment;
import dev.aika.abelia.annotation.config.LoaderSpecific;
import dev.aika.abelia.annotation.config.Range;
import dev.aika.abelia.annotation.config.gui.Category;
import dev.aika.abelia.annotation.config.gui.Tooltip;
import dev.aika.abelia.api.CaseFormat;
import dev.aika.abelia.api.LoaderType;
import dev.aika.abelia.config.codec.ResourceLocationCodec;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@AbeliaConfig(value = Abelia.ID, fieldsCaseFormat = CaseFormat.SNAKE_CASE)
public class ModConfig implements ConfigInitializer {
    @Override
    public @NotNull ConfigRegistry configure(ConfigRegistry registry) {
        return registry.registerCodec(ResourceLocation.class, String.class, new ResourceLocationCodec());
    }

    @Tooltip
    @Comment("Hide Realms Button")
    private boolean hideRealmsButton = false;
    @Tooltip
    @Comment("Hide fire overlay when fire resistance")
    private boolean hideFireOverlayWhenFireResistance = false;

    @Category("demo")
    private boolean booleanField = true;
    @Category("demo")
    private String stringField = "Gizmo";
    @Category("demo")
    @Range.Integer(min = 0, max = 255)
    private int intRangeField = 100;
    @Category("demo")
    private int intField = 123;
    @Category("demo")
    private long longField = 3L;
    @Category("demo")
    private float floatField = 3.1415f;
    @Category("demo")
    private double doubleField = 3.1415;

    @Category("demo")
    @LoaderSpecific(LoaderType.FABRIC)
    private boolean fabricOnlyField = true;
    @Category("demo")
    @LoaderSpecific(LoaderType.NEOFORGE)
    private boolean neoforgeOnlyField = true;
}
