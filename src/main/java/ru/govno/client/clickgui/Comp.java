package ru.govno.client.clickgui;

public class Comp {
   public void drawScreen(int x, int y, int step, int mouseX, int mouseY, float partialTicks) {
   }

   public void initGui() {
   }

   public void reset() {
   }

   public void mouseClicked(int x, int y, int mouseX, int mouseY, int mouseButton) {
   }

   public float getHeight() {
      return 10.0F;
   }

   public float getWidth() {
      return 100.0F;
   }

   public boolean ishover(float x1, float y1, float x2, float y2, int mouseX, int mouseY) {
      return (float)mouseX >= x1 && (float)mouseX <= x2 && (float)mouseY >= y1 && (float)mouseY <= y2;
   }

   public void mouseScrolled(int x, int y, int mouseX, int mouseY, double scroll) {
   }

   public void keyPressed(int key) {
   }

   public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
   }

   public void drawScreen(float x, float y, int step, int mouseX, int mouseY, float partialTicks) {
   }
}