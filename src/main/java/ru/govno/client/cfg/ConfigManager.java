package ru.govno.client.cfg;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.FilenameUtils;
import ru.govno.client.Client;

public final class ConfigManager extends Manager<Config> {
   public static final File configDirectory = new File(new File(Minecraft.getMinecraft().mcDataDir, "saves"), "configurations");
   public static final String format = "vls";
   private static final ArrayList<Config> loadedConfigs = new ArrayList<>();

   public ConfigManager() {
      this.setContents(loadConfigs());
      configDirectory.mkdirs();
   }

   private static ArrayList<Config> loadConfigs() {
      File[] files = configDirectory.listFiles();
      if (files != null) {
         for (File file : files) {
            if (FilenameUtils.getExtension(file.getName()).equals("vls") && !FilenameUtils.removeExtension(file.getName()).equalsIgnoreCase("nulled")) {
               loadedConfigs.add(new Config(FilenameUtils.removeExtension(file.getName())));
            }
         }
      }

      return loadedConfigs;
   }

   public static ArrayList<Config> getLoadedConfigs() {
      return loadedConfigs;
   }

   public void load() {
      if (!configDirectory.exists()) {
         configDirectory.mkdirs();
      }

      if (configDirectory != null) {
         File[] files = configDirectory.listFiles(fx -> !fx.isDirectory() && FilenameUtils.getExtension(fx.getName()).equals("vls"));

         for (File f : files) {
            Config config = new Config(FilenameUtils.removeExtension(f.getName()).replace(" ", ""));
            loadedConfigs.add(config);
         }
      }
   }

   public boolean loadConfig(String configName) {
      if (configName == null) {
         return false;
      } else {
         Config config = this.findConfig(configName);
         if (config == null) {
            return false;
         } else {
            try {
               FileReader reader = new FileReader(config.getFile());
               JsonParser parser = new JsonParser();
               JsonObject object = (JsonObject)parser.parse(reader);
               config.load(object);
               return true;
            } catch (FileNotFoundException var6) {
               return false;
            }
         }
      }
   }

   public boolean saveConfig(String configName) {
      if (configName == null) {
         return false;
      } else if (configName.equalsIgnoreCase("Default") && Client.moduleManager.getEnabledModulesCount() < 3) {
         return false;
      } else {
         Config config;
         if ((config = this.findConfig(configName)) == null) {
            Config newConfig = config = new Config(configName);
            this.getContents().add(newConfig);
         }

         String contentPrettyPrint = new GsonBuilder().setPrettyPrinting().create().toJson((JsonElement)config.save());

         try {
            FileWriter writer = new FileWriter(config.getFile());
            writer.write(contentPrettyPrint);
            writer.close();
            Minecraft.getMinecraft().entityRenderer.runCfgSaveAnim();
            return true;
         } catch (IOException var5) {
            return false;
         }
      }
   }

   public Config findConfig(String configName) {
      if (configName == null) {
         return null;
      } else {
         for (Config config : this.getContents()) {
            if (config.getName().equalsIgnoreCase(configName)) {
               return config;
            }
         }

         return new File(configDirectory, configName + ".vls").exists() ? new Config(configName) : null;
      }
   }

   public boolean deleteConfig(String configName) {
      if (configName == null) {
         return false;
      } else {
         Config config;
         if ((config = this.findConfig(configName)) == null) {
            return false;
         } else {
            File f = config.getFile();
            this.getContents().remove(config);
            return f.exists() && f.delete();
         }
      }
   }
}
