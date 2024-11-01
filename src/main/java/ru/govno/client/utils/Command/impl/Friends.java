package ru.govno.client.utils.Command.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketChatMessage;
import ru.govno.client.Client;
import ru.govno.client.friendsystem.Friend;
import ru.govno.client.module.modules.FreeCam;
import ru.govno.client.module.modules.Notifications;
import ru.govno.client.utils.Command.Command;

public class Friends extends Command {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public Friends() {
        super("Friends", new String[]{"friend", "friends", "f"});
    }

    @Override
    public void onCommand(String[] args) {
        try {
            if (args[1].equalsIgnoreCase("msg") || args[1].equalsIgnoreCase("tell")) {
                String massage = "";
                String tellType = "/" + (args[1].equalsIgnoreCase("tell") ? "tell " : "msg ");
                if (args[2].equalsIgnoreCase("coords")) {
                    EntityPlayer e = Minecraft.player;
                    if (args.length == 4) {
                        if (mc.world.getLoadedEntityList().size() <= 1) {
                            Client.msg("§e§lFriends:§r §7координаты игрока с ником [§l" + args[3] + "§r§7] недоступны.", false);
                            return;
                        }

                        for (Entity ent : mc.world.getLoadedEntityList()) {
                            if (ent != null && ent instanceof EntityOtherPlayerMP && ent.getName().equalsIgnoreCase(args[3])) {
                                e = (EntityPlayer)ent;
                            }
                        }

                        massage = "Игрок с ником " + e.getName() + " находится на координатах (X:" + (int)e.posX + " ,Y:" + (int)e.posY + " ,Z:" + (int)e.posZ + ")";
                    } else {
                        massage = "Я нахожусь на координатах (X:"
                                + (int)Minecraft.player.posX
                                + " ,Y:"
                                + (int)Minecraft.player.posY
                                + " ,Z:"
                                + (int)Minecraft.player.posZ
                                + ")";
                    }
                } else {
                    for (int i = 2; i < args.length; i++) {
                        massage = massage + args[i] + " ";
                    }
                }

                int fCount = 0;
                boolean hasFriend = false;
                String name = "";

                for (Friend f : Client.friendManager.getFriends()) {
                    if (f != null) {
                        hasFriend = true;
                        fCount++;
                        if (f.getName() != null && !f.getName().isEmpty()) {
                            name = f.getName().replace("§", "").replace(" ", "").trim() + " ";
                        }

                        if (hasFriend) {
                            Minecraft.player.connection.sendPacket(new CPacketChatMessage(tellType + name + massage));
                            Client.msg("§e§lFriends:§r §7CHAT: " + tellType + name + massage + "§b[§l" + fCount + "§r§7]§r", false);
                        }
                    }
                }
            }

            label326:
            if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("new")) {
                if (args[2].equalsIgnoreCase("near")) {
                    double range = args[3] != null ? Double.valueOf(args[3]) : 7.0;
                    Entity self = (Entity)(FreeCam.get.actived && FreeCam.fakePlayer != null ? FreeCam.fakePlayer : Minecraft.player);
                    List<String> toAddFriends = mc.world
                            .getLoadedEntityList()
                            .stream()
                            .<EntityLivingBase>map(Entity::getLivingBaseOf)
                            .filter(base -> base != self)
                            .filter(Objects::nonNull)
                            .filter(base -> (double)self.getDistanceToEntity(base) <= range)
                            .<String>map(Entity::getName)
                            .filter(name -> !Client.friendManager.isFriend(name))
                            .collect(Collectors.toList());
                    if (range <= 0.0) {
                        Client.msg("§e§lFriends:§r §7слишком мало дистанции, я никого не найду.", false);
                        return;
                    }

                    if (toAddFriends.isEmpty()) {
                        Client.msg("§e§lFriends:§r §7увы некого добавить в список.", false);
                        return;
                    }

                    int c = toAddFriends.size();
                    if (c != 1) {
                        Client.msg(
                                "§e§lFriends:§r §7в друзья "
                                        + (c != 1 && c != 2 && c != 3 && c != 4 ? "добавлены" : "добавлено")
                                        + " "
                                        + c
                                        + (c == 1 ? "существо" : (c != 2 && c != 3 && c != 4 ? "существ" : "существа"))
                                        + ":",
                                false
                        );
                    }

                    int addCounter = 0;
                    Iterator var32 = toAddFriends.iterator();

                    while (true) {
                        if (!var32.hasNext()) {
                            break label326;
                        }

                        String toAdd = (String)var32.next();
                        if (Client.friendManager.getFriend(toAdd) == null) {
                            addCounter++;
                            Client.friendManager.addFriend(toAdd);
                            Client.msg(
                                    "§e§lFriends:§r §7"
                                            + (c == 1 ? "найден 1 " : "новый друг")
                                            + " [§l"
                                            + toAdd
                                            + "§r§7]"
                                            + (c == 1 ? " и он" : "")
                                            + " добавлен в список"
                                            + (c == 1 ? "." : (c > 1 && addCounter >= c - 1 ? "." : ";")),
                                    false
                            );
                            if (Notifications.get.actived) {
                                Notifications.Notify.spawnNotify(toAdd, Notifications.type.FADD);
                            }
                        }
                    }
                }

                if (Client.friendManager.isFriend(args[2])) {
                    Client.msg("§e§lFriends:§r §7друг [§l" + args[2] + "§r§7] уже есть в списке.", false);
                    return;
                }

                Client.friendManager.addFriend(args[2].toString());
                Client.msg("§e§lFriends:§r §7друг [§l" + args[2] + "§r§7] добавлен в список.", false);
                if (Notifications.get.actived) {
                    Notifications.Notify.spawnNotify(args[2], Notifications.type.FADD);
                }

                return;
            }

            if (args[1].equalsIgnoreCase("replaceall") || args[1].equalsIgnoreCase("replall") || args[1].equalsIgnoreCase("ra")) {
                double rangex = args[2] != null ? Double.valueOf(args[2]) : 7.0;
                Entity selfx = (Entity)(FreeCam.get.actived && FreeCam.fakePlayer != null ? FreeCam.fakePlayer : Minecraft.player);
                List<String> toAddFriendsx = mc.world
                        .getLoadedEntityList()
                        .stream()
                        .<EntityLivingBase>map(Entity::getLivingBaseOf)
                        .filter(base -> base != selfx)
                        .filter(Objects::nonNull)
                        .filter(base -> (double)selfx.getDistanceToEntity(base) <= rangex)
                        .<String>map(Entity::getName)
                        .filter(name -> !Client.friendManager.isFriend(name))
                        .collect(Collectors.toList());
                List<Friend> toRemove = new ArrayList<>();

                for (Friend friend : Client.friendManager.getFriends()) {
                    if (!toAddFriendsx.stream().anyMatch(name -> name.equalsIgnoreCase(friend.getName()))) {
                        toRemove.add(friend);
                    }
                }

                if (!toRemove.isEmpty()) {
                    int c = toRemove.size();
                    if (c != 1) {
                        Client.msg(
                                "§e§lFriends:§r §7из друзей "
                                        + (c == 1 ? "удалено" : (c != 2 && c != 3 && c != 4 ? "удалены" : "удалены"))
                                        + " "
                                        + c
                                        + (c == 1 ? "существо" : (c != 2 && c != 3 && c != 4 ? "существ" : "существа"))
                                        + ":",
                                false
                        );
                    }

                    int removeCounter = 0;

                    for (Friend friendx : toRemove) {
                        if (Client.friendManager.getFriend(friendx.getName()) != null) {
                            removeCounter++;
                            Client.friendManager.removeFriend(friendx.getName());
                            Client.msg(
                                    "§e§lFriends:§r §7"
                                            + (c == 1 ? "удалён 1 " : "удалён друг")
                                            + " [§l"
                                            + friendx.getName()
                                            + "§r§7]"
                                            + (c == 1 ? " и он" : "")
                                            + " удалён из списка"
                                            + (c == 1 ? "." : (c > 1 && removeCounter >= c - 1 ? "." : ";")),
                                    false
                            );
                        }
                    }
                }

                if (rangex <= 0.0) {
                    Client.msg("§e§lFriends:§r §7слишком мало дистанции, я никого не найду.", false);
                    return;
                }

                if (toAddFriendsx.isEmpty()) {
                    Client.msg("§e§lFriends:§r §7увы некого добавить в список.", false);
                    return;
                }

                int c = toAddFriendsx.size();
                if (c != 1) {
                    Client.msg(
                            "§e§lFriends:§r §7в друзья "
                                    + (c != 1 && c != 2 && c != 3 && c != 4 ? "добавлены" : "добавлено")
                                    + " "
                                    + c
                                    + (c == 1 ? "существо" : (c != 2 && c != 3 && c != 4 ? "существ" : "существа"))
                                    + ":",
                            false
                    );
                }

                int addCounter = 0;

                for (String toAdd : toAddFriendsx) {
                    if (Client.friendManager.getFriend(toAdd) == null) {
                        addCounter++;
                        Client.friendManager.addFriend(toAdd);
                        Client.msg(
                                "§e§lFriends:§r §7"
                                        + (c == 1 ? "найден 1 " : "новый друг")
                                        + " [§l"
                                        + toAdd
                                        + "§r§7]"
                                        + (c == 1 ? " и он" : "")
                                        + " добавлен в список"
                                        + (c == 1 ? "." : (c > 1 && addCounter >= c - 1 ? "." : ";")),
                                false
                        );
                        if (Notifications.get.actived) {
                            Notifications.Notify.spawnNotify(toAdd, Notifications.type.FADD);
                        }
                    }
                }
            }

            if (args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("del")) {
                if (Client.friendManager.isFriend(args[2])) {
                    Client.friendManager.removeFriend(args[2]);
                    Client.msg("§e§lFriends:§r §7друг [§l" + args[2] + "§r§7] удалён из списка.", false);
                    if (Notifications.get.actived) {
                        Notifications.Notify.spawnNotify(args[2], Notifications.type.FDEL);
                    }

                    return;
                }

                Client.msg("§e§lFriends:§r §7друга [§l" + args[2] + "§r§7] нет в списке.", false);
                return;
            }

            if (args[1].equalsIgnoreCase("clear") || args[1].equalsIgnoreCase("ci")) {
                if (Notifications.get.actived && !Client.friendManager.getFriends().isEmpty()) {
                    Client.friendManager.getFriends().forEach(friendxx -> Notifications.Notify.spawnNotify(friendxx.getName(), Notifications.type.FDEL));
                }

                Client.friendManager.clearFriends();
                Client.msg("§e§lFriends:§r §7список друзей очищен.", false);
                return;
            }

            if (args[1].equalsIgnoreCase("list") || args[1].equalsIgnoreCase("see")) {
                if (Client.friendManager.getFriends().size() != 0) {
                    int counter = 0;

                    for (Friend friendxx : Client.friendManager.getFriends()) {
                        if (friendxx != null) {
                            Client.msg("§e§lFriends:§r §7друг №" + ++counter + " [§l" + friendxx.getName() + "§r§7].", false);
                        }
                    }
                } else {
                    Client.msg("§e§lFriends:§r §7список друзей пуст", false);
                }
            }
        } catch (Exception var11) {
            Client.msg("§e§lFriends:§r §7Комманда написана неверно.", false);
            Client.msg("§e§lFriends:§r §7add: add/new [§lNAME§r§7] / [§lNEAR | DST or null§r§7]", false);
            Client.msg("§e§lFriends:§r §7remove: remove/del [§lNAME§r§7]", false);
            Client.msg("§e§lFriends:§r §7clear: clear/ci", false);
            Client.msg("§e§lFriends:§r §7list: list/see", false);
            Client.msg("§e§lFriends:§r §7replaceall: replaceall/replall/ra [§lDST§r§7] or null", false);
            Client.msg("§e§lFriends:§r §7massage: msg/tell [§lTEXT / coords+[§lNAME§r§7] or null§r§7]", false);
            var11.printStackTrace();
        }
    }
}
