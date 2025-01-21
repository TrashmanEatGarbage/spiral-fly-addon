package com.example.addon.modules.utils;

import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileSaveUtils {
    public static final Path VENUS_FOLDER = FabricLoader.getInstance().getGameDir().resolve("afpios9dgh");

    public static void init() {
        if (Files.isDirectory(VENUS_FOLDER)) {
            try {
                Files.createDirectories(VENUS_FOLDER);
                LogUtils.getLogger().info("Created Venus folder.");
            } catch (IOException e) {
                LogUtils.getLogger().info("Failed to create Venus folder.");
            }
        }
    }

    public static void saveToFile(Serializable data, String fileName) throws Exception {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(VENUS_FOLDER.resolve(fileName)))) {
            oos.writeObject(data);
        }
    }

    public static Object load(String fileName) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(VENUS_FOLDER.resolve(fileName)))) {
            return ois.readObject();
        }
    }
}
