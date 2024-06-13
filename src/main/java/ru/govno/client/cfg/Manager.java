package ru.govno.client.cfg;

import java.util.ArrayList;
import java.util.List;

public abstract class Manager<T> {
   private List<T> contents = new ArrayList<>();

   public List<T> getContents() {
      return this.contents;
   }

   public void setContents(ArrayList<T> contents) {
      this.contents = contents;
   }
}
