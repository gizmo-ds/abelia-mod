package dev.aika.abelia.api;

import dev.aika.abelia.AbeliaConstants;
import dev.aika.abelia.annotation.config.*;
import dev.aika.abelia.annotation.config.Comment;
import dev.aika.abelia.annotation.config.gui.Category;
import dev.aika.abelia.annotation.config.gui.ConfigEntry;
import dev.aika.abelia.annotation.config.gui.Tooltip;
import dev.aika.abelia.client.config.ClientConfigManager;
import dev.aika.abelia.config.*;
import dev.aika.abelia.client.config.NameableEnum;
import dev.aika.abelia.config.codec.ResourceLocationCodec;
import dev.aika.abelia.config.codec.TextColorCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {
    @Test
    void testLoad() {
        var conf = ConfigAPI.create(TestConfig.class, Path.of("src/test/resources"));
        assertTrue(conf.load());
        System.out.println(conf);
    }

    @Test
    void testDump() {
        var conf = ConfigAPI.create(TestConfig2.class, Path.of("src/test/resources"));
        conf.get().name = "Banana";
        conf.get().theSwitch = true;
        String content = conf.dump();
        System.out.println(content);
    }

    @Test
    void testSave() {
        var conf = ConfigAPI.create(TestConfig.class, Path.of("src/test/resources"));
        conf.setConfig(new TestConfig());
        conf.get().set = new LinkedHashSet<>();
        conf.get().set.add("Jinhsi");
        conf.get().set.add("Calcharo");
        conf.get().set.add("Zani");
        conf.get().range = 5;
        assertTrue(conf.save());
        System.out.println(conf);
    }

    @Test
    void testConfigScreen() {
        var conf = ConfigAPI.create(TestConfig2.class, Path.of("src/test/resources"));
        conf.setConfig(new TestConfig2());
        var ccm = new ClientConfigManager<>(conf)
                .setDonation(DonationPlatform.AFDIAN, AbeliaConstants.DonateUrl);
//        var screen = ccm.generateScreen(null);
        System.out.println(conf);
    }

    @SuppressWarnings("unused")
    @AbeliaConfig(value = "test_mod", comment = {"这里也能添加注释"}, fieldsCaseFormat = CaseFormat.SNAKE_CASE)
    public static class TestConfig implements ConfigInitializer {
        @Override
        public @NotNull ConfigRegistry configure(ConfigRegistry registry) {
            return registry
                    .registerCodec(ResourceLocation.class, String.class, new ResourceLocationCodec())
                    .registerCodec(TextColor.class, String.class, new TextColorCodec());
        }

        @Comment({"注释支持多行", "这是第二行❤️"})
        @SerializedKey("username")
        @LoaderSpecific({LoaderType.NEOFORGE, LoaderType.FABRIC})
        String name = "Gizmo";

        @LoaderSpecific(LoaderType.NEOFORGE)
        ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath("abelia", "apple");

        @Range.Integer(min = 0, max = 255)
        int range = 111;

        @Ignored
        int asdf = 123;

        @Comment({"这是一个开关"})
        @ConfigEntry(ConfigEntry.Type.TOGGLE)
        boolean theSwitch = true;

        @Category(value = "test", order = 2)
        float floatValue = 3.14f;

        @Comment("This is a List :)")
        List<String> list = List.of("Carlotta", "Encore", "Camellya", "Phoebe");

        @CollectionImpl(HashSet.class)
        @SerializedKey("hash_set")
        Set<String> set;

        @Category(value = "banana", order = 1)
        @Comment("This is an Array :)")
        String[] arr = new String[]{"Carlotta", "Encore", "Camellya", "Phoebe"};

        @Comment("Hehe")
        @Category("sub_config")
//        SubConfig subConfig = new SubConfig();
        SubConfig subConfig;

        @Tooltip
        @ConfigEntry(ConfigEntry.Type.COLOR)
        TextColor color = TextColor.fromRgb(0xFF0000);

        @Comment("test")
        @SerializedKey("enum")
        EnumTest enumTest = EnumTest.APPLE;

        @ConfigCaseFormat(CaseFormat.SNAKE_CASE)
        public static class SubConfig implements ConfigInitializer {
            @Override
            public @NotNull ConfigRegistry configure(ConfigRegistry registry) {
                return registry
                        .registerCodec(ResourceLocation.class, String.class, new ResourceLocationCodec())
                        .registerCodec(TextColor.class, String.class, new TextColorCodec());
            }

            @Comment("Hello")
            String name = "is sub_config";
            boolean theSwitch = true;

            List<String> list = List.of("Carlotta", "Encore", "Camellya", "Phoebe");

            ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath("abelia", "banana");
        }

        public enum EnumTest implements NameableEnum {
            APPLE, BANANA;

            @Override
            public Component getDisplayName() {
                return Component.literal(this.name().toLowerCase(Locale.ENGLISH));
            }
        }
    }

    @AbeliaConfig("test_mod")
    public static class TestConfig2 implements ConfigInitializer {
        @Override
        public @NotNull ConfigRegistry configure(ConfigRegistry registry) {
            return registry
                    .registerCodec(ResourceLocation.class, String.class, new ResourceLocationCodec())
                    .registerCodec(TextColor.class, String.class, new TextColorCodec());
        }

        boolean theSwitch = false;
        String name = "apple";
    }
}