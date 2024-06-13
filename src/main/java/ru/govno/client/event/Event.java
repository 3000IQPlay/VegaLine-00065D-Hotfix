package ru.govno.client.event;

import java.lang.reflect.InvocationTargetException;
import ru.govno.client.utils.Command.impl.Panic;

public abstract class Event {
   private boolean cancelled;

   public Event call() {
      this.cancelled = false;
      call(this);
      return this;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   public void cancel() {
      this.cancelled = true;
   }

   private static final void call(Event event) {
      if (!Panic.stop) {
         ArrayHelper<Data> dataList = EventManager.get((Class<? extends Event>)event.getClass());
         if (dataList != null) {
            for (Data data : dataList) {
               try {
                  data.target.invoke(data.source, event);
               } catch (IllegalAccessException var5) {
                  var5.printStackTrace();
               } catch (InvocationTargetException var6) {
                  var6.printStackTrace();
               }
            }
         }
      }
   }

   public static enum State {
      PRE("PRE", 0),
      POST("POST", 1);

      private State(String string, int number) {
      }
   }
}
