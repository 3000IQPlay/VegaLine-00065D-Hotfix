package ru.govno.client.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtils {
   public static String readFile(File file) {
      StringBuilder stringBuilder = new StringBuilder();

      try {
         FileInputStream fileInputStream = new FileInputStream(file);

         String line;
         try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream))) {
            while ((line = bufferedReader.readLine()) != null) {
               stringBuilder.append(line).append('\n');
            }
         }
      } catch (Exception var8) {
         var8.printStackTrace();
      }

      return stringBuilder.toString();
   }

   public static String readInputStream(InputStream inputStream) {
      StringBuilder stringBuilder = new StringBuilder();

      try {
         BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

         String line;
         while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line).append('\n');
         }
      } catch (Exception var4) {
         var4.printStackTrace();
      }

      return stringBuilder.toString();
   }
}
