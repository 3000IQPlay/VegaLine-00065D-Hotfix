package ru.govno.client.newfont;

import java.awt.Font;
import java.io.InputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class Fonts {
   public static CFontRenderer time_30 = new CFontRenderer(getFontTTF("tech", 30), true, true);
   public static CFontRenderer time_17 = new CFontRenderer(getFontTTF("tech", 17), true, true);
   public static CFontRenderer time_14 = new CFontRenderer(getFontTTF("tech", 14), true, true);
   public static CFontRenderer mntsb_10 = new CFontRenderer(getFontTTF("mntsb", 10), true, true);
   public static CFontRenderer mntsb_12 = new CFontRenderer(getFontTTF("mntsb", 12), true, true);
   public static CFontRenderer mntsb_13 = new CFontRenderer(getFontTTF("mntsb", 13), true, true);
   public static CFontRenderer mntsb_14 = new CFontRenderer(getFontTTF("mntsb", 14), true, true);
   public static CFontRenderer mntsb_15 = new CFontRenderer(getFontTTF("mntsb", 15), true, true);
   public static CFontRenderer mntsb_16 = new CFontRenderer(getFontTTF("mntsb", 16), true, true);
   public static CFontRenderer mntsb_18 = new CFontRenderer(getFontTTF("mntsb", 18), true, true);
   public static CFontRenderer mntsb_20 = new CFontRenderer(getFontTTF("mntsb", 20), true, true);
   public static CFontRenderer mntsb_36 = new CFontRenderer(getFontTTF("mntsb", 36), true, true);
   public static CFontRenderer iconswex_24 = new CFontRenderer(getFontTTF("iconswex", 24), true, true);
   public static CFontRenderer iconswex_36 = new CFontRenderer(getFontTTF("iconswex", 36), true, true);
   public static CFontRenderer roadrage_36 = new CFontRenderer(getFontTTF("roadrage", 36), true, true);
   public static CFontRenderer comfortaaRegular_12 = new CFontRenderer(getFontTTF("comfortaa-regular", 12), true, true);
   public static CFontRenderer comfortaaRegular_13 = new CFontRenderer(getFontTTF("comfortaa-regular", 13), true, true);
   public static CFontRenderer comfortaaRegular_14 = new CFontRenderer(getFontTTF("comfortaa-regular", 14), true, true);
   public static CFontRenderer comfortaaRegular_15 = new CFontRenderer(getFontTTF("comfortaa-regular", 15), true, true);
   public static CFontRenderer comfortaaRegular_16 = new CFontRenderer(getFontTTF("comfortaa-regular", 16), true, true);
   public static CFontRenderer comfortaaRegular_17 = new CFontRenderer(getFontTTF("comfortaa-regular", 17), true, true);
   public static CFontRenderer comfortaaRegular_18 = new CFontRenderer(getFontTTF("comfortaa-regular", 18), true, true);
   public static CFontRenderer comfortaaRegular_22 = new CFontRenderer(getFontTTF("comfortaa-regular", 22), true, true);
   public static CFontRenderer comfortaaBold_12 = new CFontRenderer(getFontTTF("comfortaa-bold", 12), true, true);
   public static CFontRenderer comfortaaBold_13 = new CFontRenderer(getFontTTF("comfortaa-bold", 13), true, true);
   public static CFontRenderer comfortaaBold_14 = new CFontRenderer(getFontTTF("comfortaa-bold", 14), true, true);
   public static CFontRenderer comfortaaBold_15 = new CFontRenderer(getFontTTF("comfortaa-bold", 15), true, true);
   public static CFontRenderer comfortaaBold_16 = new CFontRenderer(getFontTTF("comfortaa-bold", 16), true, true);
   public static CFontRenderer comfortaaBold_17 = new CFontRenderer(getFontTTF("comfortaa-bold", 17), true, true);
   public static CFontRenderer comfortaaBold_18 = new CFontRenderer(getFontTTF("comfortaa-bold", 18), true, true);
   public static CFontRenderer comfortaa_12 = new CFontRenderer(getFontTTF("comfortaa-light", 12), true, true);
   public static CFontRenderer comfortaa_18 = new CFontRenderer(getFontTTF("comfortaa-light", 18), true, true);
   public static CFontRenderer roboto_16 = new CFontRenderer(getFontTTF("roboto", 16), true, true);
   public static CFontRenderer roboto_13 = new CFontRenderer(getFontTTF("roboto", 13), true, true);
   public static CFontRenderer neverlose500_13 = new CFontRenderer(getFontTTF("neverlose500", 13), true, true);
   public static CFontRenderer neverlose500_15 = new CFontRenderer(getFontTTF("neverlose500", 15), true, true);
   public static CFontRenderer neverlose500_16 = new CFontRenderer(getFontTTF("neverlose500", 16), true, true);
   public static CFontRenderer neverlose500_17 = new CFontRenderer(getFontTTF("neverlose500", 17), true, true);
   public static CFontRenderer neverlose500_18 = new CFontRenderer(getFontTTF("neverlose500", 18), true, true);
   public static CFontRenderer smallestpixel_16 = new CFontRenderer(getFontTTF("smallpixel", 16), true, true);
   public static CFontRenderer smallestpixel_20 = new CFontRenderer(getFontTTF("smallpixel", 20), true, true);
   public static CFontRenderer smallestpixel_24 = new CFontRenderer(getFontTTF("smallpixel", 24), true, true);
   public static CFontRenderer stylesicons_18 = new CFontRenderer(getFontTTF("stylesicons", 18), true, true);
   public static CFontRenderer stylesicons_20 = new CFontRenderer(getFontTTF("stylesicons", 20), true, true);
   public static CFontRenderer stylesicons_24 = new CFontRenderer(getFontTTF("stylesicons", 24), true, true);
   public static CFontRenderer noise_14 = new CFontRenderer(getFontTTF("noise", 14), true, true);
   public static CFontRenderer noise_15 = new CFontRenderer(getFontTTF("noise", 15), true, true);
   public static CFontRenderer noise_16 = new CFontRenderer(getFontTTF("noise", 16), true, true);
   public static CFontRenderer noise_17 = new CFontRenderer(getFontTTF("noise", 17), true, true);
   public static CFontRenderer noise_18 = new CFontRenderer(getFontTTF("noise", 18), true, true);
   public static CFontRenderer noise_20 = new CFontRenderer(getFontTTF("noise", 20), true, true);
   public static CFontRenderer noise_24 = new CFontRenderer(getFontTTF("noise", 24), true, true);
   public static CFontRenderer minecraftia_14 = new CFontRenderer(getFontTTF("minecraftia", 14), true, true);
   public static CFontRenderer minecraftia_16 = new CFontRenderer(getFontTTF("minecraftia", 16), true, true);
   public static CFontRenderer minecraftia_18 = new CFontRenderer(getFontTTF("minecraftia", 18), true, true);
   public static CFontRenderer minecraftia_20 = new CFontRenderer(getFontTTF("minecraftia", 20), true, true);

   public static Font getFontTTF(String name, int size) {
      Font font;
      try {
         InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("font/" + name + ".ttf")).getInputStream();
         font = Font.createFont(0, is);
         font = font.deriveFont(0, (float)size);
      } catch (Exception var4) {
         System.out.println("Error loading font");
         font = new Font("default", 0, size);
      }

      return font;
   }
}
