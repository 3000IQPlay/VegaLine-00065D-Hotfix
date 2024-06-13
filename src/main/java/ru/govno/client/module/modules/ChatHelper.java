package ru.govno.client.module.modules;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Arrays;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.TextComponentString;
import org.apache.commons.lang3.RandomStringUtils;
import ru.govno.client.Client;
import ru.govno.client.event.EventTarget;
import ru.govno.client.event.events.EventReceivePacket;
import ru.govno.client.event.events.EventSendPacket;
import ru.govno.client.module.Module;
import ru.govno.client.module.settings.BoolSettings;
import ru.govno.client.module.settings.FloatSettings;
import ru.govno.client.module.settings.ModeSettings;
import ru.govno.client.utils.Math.TimerHelper;

public class ChatHelper extends Module {
   public static ChatHelper get;
   public BoolSettings CensureText;
   public BoolSettings NoExtraCopy;
   public BoolSettings AntiLog4j;
   public BoolSettings AutoLogin;
   public BoolSettings AutoTpaAccept;
   public BoolSettings OnlyFriends;
   public BoolSettings ChatSpammer;
   public BoolSettings InGlobalChat;
   public BoolSettings SpamBypassTexts;
   public BoolSettings SelfNameHighlight;
   public BoolSettings EndlessStory;
   public BoolSettings ChatPrintSuffix;
   public ModeSettings LoginPass;
   public ModeSettings SpamMode;
   public ModeSettings ChatSuffix;
   public FloatSettings SpamDelay;
   boolean accept;
   boolean isReg;
   boolean toReg;
   boolean gotoSend = true;
   TimerHelper times = new TimerHelper();
   private boolean send;
   TimerHelper timer = new TimerHelper();

   public ChatHelper() {
      super("ChatHelper", 0, Module.Category.MISC);
      this.settings.add(this.CensureText = new BoolSettings("CensureText", true, this));
      this.settings.add(this.NoExtraCopy = new BoolSettings("NoExtraCopy", true, this));
      this.settings.add(this.AntiLog4j = new BoolSettings("AntiLog4j", true, this));
      this.settings.add(this.AutoLogin = new BoolSettings("AutoLogin", true, this));
      this.settings
         .add(
            this.LoginPass = new ModeSettings(
               "LoginPass", "123123123", this, new String[]{"123123123", "Mam6a_xu9imba", "zalupa228"}, () -> this.AutoLogin.getBool()
            )
         );
      this.settings.add(this.AutoTpaAccept = new BoolSettings("AutoTpaAccept", true, this));
      this.settings.add(this.OnlyFriends = new BoolSettings("OnlyFriends", true, this, () -> this.AutoTpaAccept.getBool()));
      this.settings.add(this.ChatSpammer = new BoolSettings("ChatSpammer", false, this));
      this.settings.add(this.InGlobalChat = new BoolSettings("InGlobalChat", false, this, () -> this.ChatSpammer.getBool()));
      this.settings.add(this.SpamBypassTexts = new BoolSettings("SpamBypassTexts", true, this, () -> this.ChatSpammer.getBool()));
      this.settings.add(this.SpamDelay = new FloatSettings("SpamDelay", 4000.0F, 10000.0F, 100.0F, this, () -> this.ChatSpammer.getBool()));
      this.settings
         .add(
            this.SpamMode = new ModeSettings(
               "SpamMode", "Bulling", this, new String[]{"WarpHVH", "Client", "/tpahere", "Bulling", "Citations"}, () -> this.ChatSpammer.getBool()
            )
         );
      this.settings.add(this.SelfNameHighlight = new BoolSettings("SelfNameHighlight", true, this));
      this.settings.add(this.EndlessStory = new BoolSettings("EndlessStory", false, this));
      this.settings.add(this.ChatPrintSuffix = new BoolSettings("ChatPrintSuffix", false, this));
      this.settings
         .add(
            this.ChatSuffix = new ModeSettings(
               "ChatSuffix", "Vegaline", this, new String[]{"Vegaline", "VL", "RandomSmile"}, () -> this.ChatPrintSuffix.getBool()
            )
         );
      get = this;
   }

   public boolean isInfiniteChatHistory() {
      return this.actived && this.EndlessStory.getBool();
   }

   public boolean highlightSelf(String byChat) {
      return get != null
         && get.actived
         && (
            byChat.toLowerCase().contains(mc.session.getUsername().toLowerCase())
               || byChat.toLowerCase().contains(Minecraft.player.getDisplayName().getUnformattedText().toLowerCase())
         )
         && this.SelfNameHighlight.getBool();
   }

   @EventTarget
   public void onPacketReceive(EventReceivePacket event) {
      if (this.AntiLog4j.getBool()
         && event.getPacket() instanceof SPacketChat packet
         && (packet.chatComponent.getFormattedText().startsWith("${") || packet.chatComponent.getFormattedText().contains("}"))) {
         event.setCancelled(true);
         Minecraft.player.addChatMessage(new TextComponentString("Log4jFixer удалил опасное сообщение."));
      }

      if (this.AutoLogin.getBool() && event.getPacket() instanceof SPacketChat packet) {
         if (packet.chatComponent.getFormattedText().contains("/reg") || packet.chatComponent.getFormattedText().contains("/register")) {
            this.times.reset();
            this.isReg = true;
            this.toReg = true;
         }

         if (packet.chatComponent.getFormattedText().contains("/l") || packet.chatComponent.getFormattedText().contains("/login")) {
            this.times.reset();
            this.isReg = false;
            this.toReg = true;
         }
      }

      if (this.AutoTpaAccept.getBool() && event.getPacket() instanceof SPacketChat packet) {
         String msg = packet.getChatComponent().getFormattedText();
         msg = msg.replace("  ", " ")
            .replace("§l", "")
            .replace("[]", "")
            .replace("§k", "")
            .replace("§m", "")
            .replace("§n", "")
            .replace("§o", "")
            .replace("§c", "")
            .replace("§a", "")
            .replace("§e", "")
            .replace("§d", "")
            .replace("§b", "")
            .replace("§1", "")
            .replace("§2", "")
            .replace("§3", "")
            .replace("§4", "")
            .replace("§5", "")
            .replace("§6", "")
            .replace("§7", "")
            .replace("§8", "")
            .replace("§9", "")
            .replace("§0", "")
            .replace("§o", "")
            .replace("§r", "");
         String msg2 = msg.toLowerCase();
         String[] types = new String[]{"/tpaccept", "/tpdeny", "/tpyes", "/tpno", "120 seconds", "[принять]"};
         boolean canAccept = !this.OnlyFriends.getBool()
            || Client.friendManager.getFriends().stream().map(friend -> friend.getName()).anyMatch(name -> msg2.contains(name));
         boolean hasSlashed = false;

         for (String type : types) {
            if (msg2.contains(type)) {
               hasSlashed = true;
               break;
            }
         }

         if (hasSlashed || msg2.contains("просит телепортироваться к Вам.") || msg2.contains("has requested to teleport to you")) {
            this.accept = canAccept;
         }
      }
   }

   @Override
   public void onUpdate() {
      if (this.ChatPrintSuffix.getBool() && !this.gotoSend) {
         this.gotoSend = true;
      }

      if (this.AutoLogin.getBool() && this.toReg && !this.times.hasReached(150.0)) {
         String pass = this.LoginPass.currentMode;
         if (this.isReg) {
            Minecraft.player.sendChatMessage("/register " + pass + " " + pass);
            Client.msg("§f§lModules:§r §7[§lChatHelper§r§7]: Регистрирую аккаунт.", false);
            StringSelection selection = new StringSelection(pass);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
            Client.msg("§f§lModules:§r §7[§lChatHelper§r§7]: Пароль скопирован в буффер обмена.", false);
         } else {
            Minecraft.player.sendChatMessage("/login " + pass);
            Client.msg("§f§lModules:§r §7[§lChatHelper§r§7]: Авторизовываю аккаунт.", false);
         }

         this.toReg = false;
      }

      if (this.accept && this.AutoTpaAccept.getBool()) {
         Minecraft.player.sendChatMessage("/tpaccept");
         Client.msg("§f§lModules:§r §7[§lChatHelper§r§7]: Принимаю телепорт.", false);
         this.accept = false;
      }

      if (this.ChatSpammer.getBool() && this.timer.hasReached((double)((int)this.SpamDelay.getFloat()))) {
         String spamMode = this.SpamMode.currentMode;
         if (spamMode.equalsIgnoreCase("WarpHVH")) {
            if (this.SpamBypassTexts.getBool()) {
               Minecraft.player
                  .connection
                  .sendPacket(
                     new CPacketChatMessage(
                        (this.InGlobalChat.getBool() ? "!" : "")
                           + RandomStringUtils.randomAlphanumeric(5)
                           + " all warp hvh zames z4 ezzz go hvh"
                           + RandomStringUtils.randomAlphanumeric(5)
                     )
                  );
            } else {
               Minecraft.player.connection.sendPacket(new CPacketChatMessage((this.InGlobalChat.getBool() ? "!" : "") + "all warp hvh zames z4 ezzz go hvh"));
            }
         } else if (spamMode.equalsIgnoreCase("Client")) {
            if (this.SpamBypassTexts.getBool()) {
               Minecraft.player
                  .connection
                  .sendPacket(
                     new CPacketChatMessage(
                        (this.InGlobalChat.getBool() ? "!" : "")
                           + RandomStringUtils.randomAlphanumeric(5)
                           + " VegaLineClient is a client that pisses your shit clients"
                           + RandomStringUtils.randomAlphanumeric(5)
                     )
                  );
            } else {
               Minecraft.player
                  .connection
                  .sendPacket(new CPacketChatMessage((this.InGlobalChat.getBool() ? "!" : "") + "vegaline client is a client that pisses your shit clients"));
            }
         } else if (spamMode.equalsIgnoreCase("/tpahere")) {
            for (EntityPlayer e : GuiPlayerTabOverlay.getPlayers2()) {
               if (e != null && e != Minecraft.player) {
                  Minecraft.player.connection.sendPacket(new CPacketChatMessage("/tpahere " + e.getName()));
               }
            }
         } else if (spamMode.equalsIgnoreCase("Citations")) {
            String[] citates = new String[]{
               "Почему женщины много говорят, а мужчины много думают. У женщин двое губ, а у мужчин две головы.",
               "Гиппопотам — это бегемот или просто очень крутой опотам?",
               "Я не разбрасываюсь словами, мне потом их трудно подбирать.",
               "Чем старше человек, тем больше ему лет.",
               "От короновируса умирали даже те, кто раньше никогда не умирал.",
               "С помощью дверей можно зайти домой.",
               "Ещё вчера я думал, завтра будет сегодня.",
               "Если чего-то не знаешь, просто спроси.",
               "Если вы провалились в яму, то идите домой за лестницей.",
               "Если почувствуете, что тонете, просто плывите к берегу.",
               "Ты не сможешь ничего сказать, кроме слов.",
               "Левый глаз левее правого.",
               "Я не настолько глуп, как вы думаете. Просто, у меня столько мыслей в голове, что рот не успевает за ними.",
               "У мужчины две головы. Одной он думает, а вторая у него на плечах.",
               "Одна ошибка - и ты ошибся.",
               "Волк - это не работа... Работа - это ворк, а волк - это ходить.",
               "Если тебя мучает жажда, то борис лов.",
               "Порхай как бабочка. Жаль, что твоя мать сдохла.",
               "В мире есть много типов животных. Простейшие, губки, черви, хордовые, членистоногие, моллюски и целепукстиал юзеры.",
               "В мире есть много типов животных. Простейшие, губки, черви, хордовые, членистоногие, моллюски и обосрансив юзеры.",
               "В мире есть много типов животных. Простейшие, губки, черви, хордовые, членистоногие, моллюски и насрултан юзеры.",
               "Иди хоть что-то нормальное посмотри кроме ютуба.",
               "Спи там, где волки ссать боятся.",
               "Если пошёл дождь и не где укрыться, а ты боишься промокнуть - заставь дождь промокнуть вместо тебя.",
               "Если на тебя напали и воткнули нож - пропиши двоечку ножу чтоб не втыкал.",
               "Чувствуешь, что время быстро идёт? Догони его и попроси идти немного помедленее.",
               "Запомни и не забудь - на березах яблоки не растут! Там только бананы.",
               "Увидел бабушку и хочешь перевести её через дорогу? Запомни, бабки - в жизни не главное.",
               "Не имей сто друзей, а имей их подруг.",
               "Любишь срать, люби и унитаз смывать.",
               "Если тебе больно - не болей.",
               "Лучше жопой съехать с терки, чем учиться на пятерки.",
               "Дыши там, где воздух есть и сможешь дышать.",
               "Если захотелось одновременно и ссать, и пить - считай обе проблемы уже решены.",
               "Кто не воин, тот не воин.",
               "Чтобы холодная вода стала горячей, ее нужно подогреть.",
               "Я не разбрасываюсь словами, мне потом их трудно подбирать.",
               "Чем старше человек, тем больше ему лет.",
               "Они хотели обосрать нас, но забыл снять штаны.",
               "Если закрыть глаза, становится темно.",
               "Пока не доказано - не ебёт что сказано.",
               "Не опоздал, а задержался по семейным обстоятельствам.",
               "Хороший человек плохой воздух в себе держать не будет.",
               "Пей там где конь пьёт, ведь паразиты не убивают, они делают нас сильнее.",
               "Обидно что я живу в мире, где у огурца есть горькая попка, но нет сладкой письки.",
               "Э... Эм... бля опять забыл что такое альцгеймер.",
               "Лучше посрать и опоздать, чем прийти и обосраться.",
               "Если тебе отрубили ногу, приклей её клеем.",
               "Глупый человек жалуется на дырку в кармане. Умный использует ее, чтобы почесать себе яйца.",
               "Один мужчина сказал очень мудрую вещь, но я ее забыл.",
               "Если ты заблудился в лесу, иди домой.",
               "Никогда не сдавайся, ты же не квартира.",
               "Если ты хочешь пить, а твой друг ссать, то держитесь друг от друга подальше.",
               "Чтобы сон прошел удачно, не забудь подергать смачно.",
               "Если не можешь уснуть, просто спи.",
               "Когда комар сядет тебе на яйцо, лишь тогда ты поймешь что точность важнее силы.",
               "Мне даже играть не нужно, я знаю что ты, кал, допустил критическую ошибку - запустился на кристальный кит.",
               "Новости: найден труп очередной жертвы насилия юзера клиента vegaline.",
               "Внимание: на сервере обнаружен крайне опасный серийный киллер, и это я.",
               "Я тут щас всех жахну и вновь уйду спать довольным собой.",
               "Буду ебать всех кто движется а кто не движется, того подвину.",
               "Буду пялить всех кто движется а кто не движется, того подвину и продолжу пялить.",
               "Чем кормить курицу что-бы она сносила не яйца а ебальники?",
               "Что бы я дал человеку, у которого все есть? Я дал бы ему в челюсть.",
               "Вот так всегда: хорошо скажешь – сглазишь, плохо – накаркаешь!",
               "Самая красивая месть недоброжелателю, это – забыть о его существовании.",
               "От того, как ты посмотришь, зависит то, что ты увидишь.",
               "Сегодняшний прогноз: 100% шанс на победу.",
               "Я не сошел с ума.. Я просто продал его в Интернете.",
               "Надежда есть всегда, она придёт и задавит тебя пузом.",
               "Никто не идеален. Но это не про меня!",
               "Хочешь конфетку? На бери... глк глк глк...",
               "Если ты это читаешь, пошёл нахуй.",
               "Если хочешь срать и тебе лень снимать портки, просто сри.",
               "Я все еще играю в игры и получаю удовольствие, а ты скучный аморал.",
               "Ты вышел в топ мира.. по наличию хромосом.",
               "Пиздец. Одним словом описал ситуацию в стране и в мире, рассказал о проблемах.",
               "И пизда раз в год стреляет.",
               "Военком прислал повестку, пошёл на почту отправил назад.",
               "Не можешь срать не мучай жопу.",
               "Смотри в пол когда я перед тобой.",
               "Пивка для рывка, водочки для блатной походочки.",
               "Матернул училку, её лицо набухло. Ща как бабахнет.",
               "Выебал медузу + познал морской мир.",
               "Играл в гольф, твоя мама лежала и вдруг дёрнулась.",
               "Делай как надо, как не надо не делай.",
               "Сходил в качалку, позанимался.. Тренажёр стал сильнее.",
               "Жи Ши пиши от души.",
               "Когда я иду даже воздух отходит.",
               "Мёртвое море знаешь? Это я убил!",
               "Не откладывай на завтра, то, что можно сделать после завтра.",
               "Я бью два раза, один в ебало, другой по крышке гроба.",
               "Без бумажки ты какашка, а с бумажкой какашка с бумажкой.",
               "Фуух! Пробурил пещеру, бля эт плева была. Андрей??"
            };
            String msg = citates[(int)(((float)citates.length - 1.0F) * (float)Math.random() * (((float)citates.length - 1.0F) / (float)citates.length))];
            Minecraft.player.connection.sendPacket(new CPacketChatMessage((this.InGlobalChat.getBool() ? "!" : "") + msg));
         }

         if (spamMode.equalsIgnoreCase("Bulling") && !this.send) {
            this.send = true;
         }

         this.timer.reset();
      }
   }

   @EventTarget
   public void onPacketSend(EventSendPacket event) {
      String[] messages = new String[]{
         "не чувствую",
         "ха eбaть ты слабый",
         "чё с ебалом сын шлюхи",
         "купи вегалайн читикс реал а то сосёшь",
         "у тебя в очке мой меч хых",
         "кто из нас умрёт тот пидор",
         "скули псина глотай мои криты сын урны",
         "чё по хп xyйня нищaя",
         "cоси мне старательнее пж",
         "eбaть твой пиздaк сыпется",
         "на нaxyй сын блядиHы",
         "ебу тебя как твою мамку прям",
         "чё ебaть попасть не можешь cвинья бляTь7",
         "oткиcaй бoмжиха eбaнaя",
         "сын шлюхи сын шлюхииии",
         "твоя maть у меня заперта в туалете ободок облизывает чтоб я её покормил",
         "твоя maть в лаве купается",
         "ты умер ёптa",
         "у тебя пиcьки нет",
         "твой тампон провалился слишком глубоко",
         "цветочный лox толстый сиськастый  хyй",
         "ты думал что закинул загубу насвай а закинул мой xyй",
         "ты был создан чтоб сoсать мне",
         "я вaxye какой ты бич",
         "oтxвaтывай nиздюлей шeгол eбyчий",
         "твoя мaмaша тaкая жиpная что её хoдьба зacтaвляет зeмлю крyтиться",
         "oтпинал твoю мaть и тeбя oтпинaю сyчкa",
         "мамку твою хуем рублю",
         "мать твою пнул хуем",
         "мать твою хуем вскрыл",
         "хехе сосешь мой хуй талантливо",
         "мать твоя на хую тухнет",
         "сосешь аналом",
         "сосешь как можешь",
         "твоя мать чавкает мой хуй",
         "твоя мамаша уехала нахуй и больше не вернулась",
         "твоя мама такая жирная что чёрное море её тень",
         "как сосётся а хач7",
         "отмудохаю тебя хуесос как некеда",
         "на по ебалу чубрик",
         "заглаатывай мою жирную елдину"
      };
      String text = messages[new Random().nextInt(messages.length)];
      if (event.getPacket() instanceof CPacketUseEntity packet && mc.world != null && packet.getAction() == CPacketUseEntity.Action.ATTACK) {
         if (packet.getEntityFromWorld(mc.world) == null) {
            return;
         }

         String nick = packet.getEntityFromWorld(mc.world).getName().replace("entity.", "").replace(".name", "");
         if (this.send) {
            if (this.SpamBypassTexts.getBool()) {
               Minecraft.player
                  .connection
                  .sendPacket(
                     new CPacketChatMessage(
                        (this.InGlobalChat.getBool() ? "!" : "")
                           + RandomStringUtils.randomAlphanumeric(5)
                           + " "
                           + nick
                           + " "
                           + text
                           + " "
                           + RandomStringUtils.randomAlphanumeric(5)
                     )
                  );
               this.send = false;
            } else {
               Minecraft.player.connection.sendPacket(new CPacketChatMessage((this.InGlobalChat.getBool() ? "!" : "") + nick + " " + text));
               this.send = false;
            }
         }
      }

      if (event.getPacket() instanceof CPacketChatMessage massagePacket && this.ChatPrintSuffix.getBool() && this.gotoSend) {
         String massage;
         if ((massage = massagePacket.getMessage()) != null
            && !massage.isEmpty()
            && !Arrays.asList("/", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "#")
               .stream()
               .anyMatch(badStart -> massage.replace(" ", "").startsWith(badStart))) {
            String[] apps = new String[]{" ", "[", "]"};
            String var7 = this.ChatSuffix.currentMode;
            switch (var7) {
               case "Vegaline":
                  massagePacket.appendMessage(apps[0] + apps[1] + this.ChatSuffix.currentMode + apps[2]);
                  break;
               case "VL":
                  massagePacket.appendMessage(apps[0] + apps[1] + this.ChatSuffix.currentMode + apps[2]);
                  break;
               case "RandomSmile":
                  String[] smiles = new String[]{
                     "(-_-)",
                     "(8_8)",
                     "(>-<)",
                     ":3",
                     ":D",
                     ":-|",
                     "(x.x)",
                     "Zzzz",
                     "(O_o)",
                     "(o_O)",
                     "(-.-)",
                     "(^_~)",
                     "(u_u)",
                     "(>__<)",
                     "(^_-)",
                     "(^^)",
                     "-_-;",
                     "(^-^)",
                     "(^3^)",
                     "^o^",
                     "u_u",
                     "n_n",
                     "＄=＄",
                     "'-'",
                     "X-X",
                     "T_T",
                     "<0_0>"
                  };
                  String smile = smiles[(int)(((float)smiles.length - 1.0F) * (float)Math.random() * (((float)smiles.length - 1.0F) / (float)smiles.length))];
                  massagePacket.appendMessage(apps[0] + smile);
            }
         }

         this.gotoSend = false;
      }
   }
}
