package org.dynmap.mobs;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.Configuration;
import org.dynmap.markers.MarkerAPI;

import java.lang.reflect.Method;

public class Utils {

    public static final int TYPE_MOB = 100;
    public static final int TYPE_VEHICLE = 101;

    private static String sOBCPackage;
    private static String sNMSPackage;

    public static final MobConfig CONFIG_MOBS;
    public static MobConfig CONFIG_VEHICLES;

    static {
        CONFIG_MOBS = new MobConfig("mobs.", new MobMapping[]{
                // Mo'Creatures
                new MobMapping("horse", "org.bukkit.entity.Animals", "Horse", "net.minecraft.server.MoCEntityHorse"),
                new MobMapping("fireogre", "org.bukkit.entity.Monster", "Fire Ogre", "net.minecraft.server.MoCEntityFireOgre"),
                new MobMapping("caveogre", "org.bukkit.entity.Monster", "Cave Ogre", "net.minecraft.server.MoCEntityCaveOgre"),
                new MobMapping("ogre", "org.bukkit.entity.Monster", "Ogre", "net.minecraft.server.MoCEntityOgre"),
                new MobMapping("boar", "org.bukkit.entity.Pig", "Boar", "net.minecraft.server.MoCEntityBoar"),
                new MobMapping("polarbear", "org.bukkit.entity.Animals", "Polar Bear", "net.minecraft.server.MoCEntityPolarBear"),
                new MobMapping("bear", "org.bukkit.entity.Animals", "Bear", "net.minecraft.server.MoCEntityBear"),
                new MobMapping("duck", "org.bukkit.entity.Chicken", "Duck", "net.minecraft.server.MoCEntityDuck"),
                new MobMapping("bigcat", "org.bukkit.entity.Animals", "Big Cat", "net.minecraft.server.MoCEntityBigCat"),
                new MobMapping("deer", "org.bukkit.entity.Animals", "Deer", "net.minecraft.server.MoCEntityDeer"),
                new MobMapping("wildwolf", "org.bukkit.entity.Monster", "Wild Wolf", "net.minecraft.server.MoCEntityWWolf"),
                new MobMapping("flamewraith", "org.bukkit.entity.Monster", "Wraith", "net.minecraft.server.MoCEntityFlameWraith"),
                new MobMapping("wraith", "org.bukkit.entity.Monster", "Wraith", "net.minecraft.server.MoCEntityWraith"),
                new MobMapping("bunny", "org.bukkit.entity.Animals", "Bunny", "net.minecraft.server.MoCEntityBunny"),
                new MobMapping("bird", "org.bukkit.entity.Animals", "Bird", "net.minecraft.server.MoCEntityBird"),
                new MobMapping("fox", "org.bukkit.entity.Animals", "Bird", "net.minecraft.server.MoCEntityFox"),
                new MobMapping("werewolf", "org.bukkit.entity.Monster", "Werewolf", "net.minecraft.server.MoCEntityWerewolf"),
                new MobMapping("shark", "org.bukkit.entity.WaterMob", "Shark", "net.minecraft.server.MoCEntityShark"),
                new MobMapping("dolphin", "org.bukkit.entity.WaterMob", "Shark", "net.minecraft.server.MoCEntityDolphin"),
                new MobMapping("fishy", "org.bukkit.entity.WaterMob", "Fishy", "net.minecraft.server.MoCEntityFishy"),
                new MobMapping("kitty", "org.bukkit.entity.Animals", "Kitty", "net.minecraft.server.MoCEntityKitty"),
                new MobMapping("hellrat", "org.bukkit.entity.Monster", "Hell Rat", "net.minecraft.server.MoCEntityHellRat"),
                new MobMapping("rat", "org.bukkit.entity.Monster", "Rat", "net.minecraft.server.MoCEntityRat"),
                new MobMapping("mouse", "org.bukkit.entity.Animals", "Mouse", "net.minecraft.server.MoCEntityMouse"),
                new MobMapping("scorpion", "org.bukkit.entity.Monster", "Scorpion", "net.minecraft.server.MoCEntityScorpion"),
                new MobMapping("turtle", "org.bukkit.entity.Animals", "Turtle", "net.minecraft.server.MoCEntityTurtle"),
                new MobMapping("crocodile", "org.bukkit.entity.Animals", "Crocodile", "net.minecraft.server.MoCEntityCrocodile"),
                new MobMapping("ray", "org.bukkit.entity.WaterMob", "Ray", "net.minecraft.server.MoCEntityRay"),
                new MobMapping("jellyfish", "org.bukkit.entity.WaterMob", "Jelly Fish", "net.minecraft.server.MoCEntityJellyFish"),
                new MobMapping("goat", "org.bukkit.entity.Animals", "Goat", "net.minecraft.server.MoCEntityGoat"),
                new MobMapping("snake", "org.bukkit.entity.Animals", "Snake", "net.minecraft.server.MoCEntitySnake"),
                new MobMapping("ostrich", "org.bukkit.entity.Animals", "Ostrich", "net.minecraft.server.MoCEntityOstrich"),
                // Standard
                new MobMapping("bat", "org.bukkit.entity.Bat", "Bat"),
                new MobMapping("witch", "org.bukkit.entity.Witch", "Witch"),
                new MobMapping("wither", "org.bukkit.entity.Wither", "Wither"),
                new MobMapping("blaze", "org.bukkit.entity.Blaze", "Blaze"),
                new MobMapping("enderdragon", "org.bukkit.entity.EnderDragon", "Ender Dragon"),
                new MobMapping("ghast", "org.bukkit.entity.EnderDragon", "Ghast"),
                new MobMapping("mooshroom", "org.bukkit.entity.MushroomCow", "Mooshroom"),
                new MobMapping("cow", "org.bukkit.entity.Cow", "Cow"),
                new MobMapping("silverfish", "org.bukkit.entity.Silverfish", "Silverfish"),
                new MobMapping("magmacube", "org.bukkit.entity.MagmaCube", "Magma Cube"),
                new MobMapping("slime", "org.bukkit.entity.Slime", "Slime"),
                new MobMapping("snowgolem", "org.bukkit.entity.Snowman", "Snow Golem"),
                new MobMapping("cavespider", "org.bukkit.entity.CaveSpider", "Cave Spider"),
                new MobMapping("spider", "org.bukkit.entity.Spider", "Spider"),
                new MobMapping("spiderjockey", "org.bukkit.entity.Spider", "Spider Jockey"), /* Must be just after "spider" */
                new MobMapping("wolf", "org.bukkit.entity.Wolf", "Wolf"),
                new MobMapping("tamedwolf", "org.bukkit.entity.Wolf", "Wolf"), /* Must be just after wolf */
                new MobMapping("ocelot", "org.bukkit.entity.Ocelot", "Ocelot"),
                new MobMapping("cat", "org.bukkit.entity.Ocelot", "Cat"), /* Must be just after ocelot */
                new MobMapping("zombiepigman", "org.bukkit.entity.PigZombie", "Zombie Pigman"),
                new MobMapping("creeper", "org.bukkit.entity.Creeper", "Creeper"),
                new MobMapping("skeleton", "org.bukkit.entity.Skeleton", "Skeleton"),
                new MobMapping("witherskeleton", "org.bukkit.entity.Skeleton", "Wither Skeleton"), /* Must be just after "skeleton" */
                new MobMapping("enderman", "org.bukkit.entity.Enderman", "Enderman"),
                new MobMapping("zombie", "org.bukkit.entity.Zombie", "Zombie"),
                new MobMapping("zombievilager", "org.bukkit.entity.Zombie", "Zombie Villager"), /* Must be just after "zomnie" */
                new MobMapping("giant", "org.bukkit.entity.Giant", "Giant"),
                new MobMapping("chicken", "org.bukkit.entity.Chicken", "Chicken"),
                new MobMapping("pig", "org.bukkit.entity.Pig", "Pig"),
                new MobMapping("sheep", "org.bukkit.entity.Sheep", "Sheep"),
                new MobMapping("squid", "org.bukkit.entity.Squid", "Squid"),
                new MobMapping("villager", "org.bukkit.entity.Villager", "Villager"),
                new MobMapping("golem", "org.bukkit.entity.IronGolem", "Iron Golem"),
                new MobMapping("vanillahorse", "org.bukkit.entity.Horse", "Horse"),
                new MobMapping("rabbit", "org.bukkit.entity.Rabbit", "Rabbit"),
                new MobMapping("endermite", "org.bukkit.entity.Endermite", "Endermite"),
                new MobMapping("guardian", "org.bukkit.entity.Guardian", "Guardian")
        }, new StaticMobConfig(
                "layer.tinyicons",
                "mobs.markerset",
                "layer.name",
                "Mobs",
                "layer.layerprio",
                "layer.hidebydefault",
                "layer.minzoom",
                "layer.nolabels",
                "layer.inc-coord",
                "update.period",
                "update.mobs-per-tick"
        ));

        CONFIG_VEHICLES = new MobConfig("vehicles.", new MobMapping[]{
                // Explosive Minecart
                new MobMapping("explosive-minecart", "org.bukkit.entity.minecart.ExplosiveMinecart", "Explosive Minecart"),
                // Hopper Minecart
                new MobMapping("hopper-minecart", "org.bukkit.entity.minecart.HopperMinecart", "Hopper Minecart"),
                // Powered Minecart
                new MobMapping("powered-minecart", "org.bukkit.entity.minecart.PoweredMinecart", "Powered Minecart"),
                // Rideable Minecart
                new MobMapping("minecart", "org.bukkit.entity.minecart.RideableMinecart", "Minecart"),
                // Spawner Minecart
                new MobMapping("spawner-minecart", "org.bukkit.entity.minecart.SpawnerMinecart", "Spawner Minecart"),
                // Storage Minecart
                new MobMapping("storage-minecart", "org.bukkit.entity.minecart.StorageMinecart", "Storage Minecart"),
                // Boat
                new MobMapping("boat", "org.bukkit.entity.Boat", "Boat")
        }, new StaticMobConfig(
                "vehiclelayer.tinyicons",
                "vehicles.markerset",
                "vehiclelayer.name",
                "Vehicles",
                "vehiclelayer.layerprio",
                "vehiclelayer.hidebydefault",
                "vehiclelayer.minzoom",
                "vehiclelayer.nolabels",
                "vehiclelayer.inc-coord",
                "update.vehicleperiod",
                "update.vehicles-per-tick"
        ));
    }

    private static MobConfig getConfigByType(int type) {
        switch (type) {
            case TYPE_MOB: {
                return CONFIG_MOBS;
            }
            case TYPE_VEHICLE: {
                return CONFIG_VEHICLES;
            }
            default: {
                throw new RuntimeException("Type: " + type + " NOT support.");
            }
        }
    }

    private synchronized static String getOBCPackage() {
        if (sOBCPackage == null) {
            sOBCPackage = Bukkit.getServer().getClass().getPackage().getName();
        }
        return sOBCPackage;
    }

    public static String mapClassName(String n) {
        if (n.startsWith("org.bukkit.craftbukkit")) {
            n = getOBCPackage() + n.substring("org.bukkit.craftbukkit".length());
        } else if (n.startsWith("net.minecraft.server")) {
            n = getNMSPackage() + n.substring("net.minecraft.server".length());
        }
        return n;
    }

    public static MobGroup newMobGroup(int type, Configuration cfg, MarkerAPI markerapi) {
        MobConfig config = getConfigByType(type);
        return new MobGroup(type, config, cfg, markerapi);
    }

    private static String getNMSPackage() {
        if (sNMSPackage == null) {
            Server srv = Bukkit.getServer();
            /* Get getHandle() method */
            try {
                Method m = srv.getClass().getMethod("getHandle");
                Object scm = m.invoke(srv); /* And use it to get SCM (nms object) */
                sNMSPackage = scm.getClass().getPackage().getName();
            } catch (Exception x) {
                sNMSPackage = "net.minecraft.server";
            }
        }
        return sNMSPackage;
    }

}
