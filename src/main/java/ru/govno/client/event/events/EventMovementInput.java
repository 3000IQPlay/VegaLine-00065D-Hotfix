package ru.govno.client.event.events;

import ru.govno.client.event.Event;

public class EventMovementInput extends Event {
   private float forward;
   private float strafe;
   private boolean jump;
   private boolean sneaking;
   private double sneakSlowDownMultiplier;

   public float getForward() {
      return this.forward;
   }

   public float getStrafe() {
      return this.strafe;
   }

   public boolean isJump() {
      return this.jump;
   }

   public boolean isSneaking() {
      return this.sneaking;
   }

   public double getSneakSlowDownMultiplier() {
      return this.sneakSlowDownMultiplier;
   }

   public void setForward(float forward) {
      this.forward = forward;
   }

   public void setStrafe(float strafe) {
      this.strafe = strafe;
   }

   public void setJump(boolean jump) {
      this.jump = jump;
   }

   public void setSneaking(boolean sneaking) {
      this.sneaking = sneaking;
   }

   public void setSneakSlowDownMultiplier(double sneakSlowDownMultiplier) {
      this.sneakSlowDownMultiplier = sneakSlowDownMultiplier;
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof EventMovementInput other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else if (Float.compare(this.getForward(), other.getForward()) != 0) {
         return false;
      } else if (Float.compare(this.getStrafe(), other.getStrafe()) != 0) {
         return false;
      } else if (this.isJump() != other.isJump()) {
         return false;
      } else {
         return this.isSneaking() != other.isSneaking() ? false : Double.compare(this.getSneakSlowDownMultiplier(), other.getSneakSlowDownMultiplier()) == 0;
      }
   }

   protected boolean canEqual(Object other) {
      return other instanceof EventMovementInput;
   }

   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + Float.floatToIntBits(this.getForward());
      result = result * 59 + Float.floatToIntBits(this.getStrafe());
      result = result * 59 + (this.isJump() ? 79 : 97);
      result = result * 59 + (this.isSneaking() ? 79 : 97);
      long $sneakSlowDownMultiplier = Double.doubleToLongBits(this.getSneakSlowDownMultiplier());
      return result * 59 + (int)($sneakSlowDownMultiplier >>> 32 ^ $sneakSlowDownMultiplier);
   }

   @Override
   public String toString() {
      return "EventMovementInput(forward="
         + this.getForward()
         + ", strafe="
         + this.getStrafe()
         + ", jump="
         + this.isJump()
         + ", sneaking="
         + this.isSneaking()
         + ", sneakSlowDownMultiplier="
         + this.getSneakSlowDownMultiplier()
         + ")";
   }

   public EventMovementInput(float forward, float strafe, boolean jump, boolean sneaking, double sneakSlowDownMultiplier) {
      this.forward = forward;
      this.strafe = strafe;
      this.jump = jump;
      this.sneaking = sneaking;
      this.sneakSlowDownMultiplier = sneakSlowDownMultiplier;
   }
}
