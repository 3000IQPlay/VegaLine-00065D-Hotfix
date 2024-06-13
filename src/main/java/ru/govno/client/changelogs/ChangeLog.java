package ru.govno.client.changelogs;

public class ChangeLog {
   private String changeName;
   private final ChangelogType type;

   public ChangeLog(String name, ChangelogType type) {
      this.changeName = name;
      this.type = type;
      switch (type) {
         case NONE:
            this.changeName = ": " + this.changeName;
            break;
         case ADD:
            this.changeName = "§r added §7" + this.changeName;
            break;
         case DELETE:
            this.changeName = "§r deleted §7" + this.changeName;
            break;
         case IMPROVED:
            this.changeName = "§r improved §7" + this.changeName;
            break;
         case FIXED:
            this.changeName = "§r fixed §7" + this.changeName;
            break;
         case PROTOTYPE:
            this.changeName = "§r prototype §7" + this.changeName;
            break;
         case NEW:
            this.changeName = "§r new §7" + this.changeName;
      }
   }

   public String getChangeName() {
      return this.changeName;
   }

   public ChangelogType getType() {
      return this.type;
   }
}
