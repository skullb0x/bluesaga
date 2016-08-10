package data_handlers;

import game.ServerSettings;
import login.WebsiteServerStatus;
import network.Client;
import network.Server;

import java.io.IOException;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import data_handlers.ability_handler.AbilityHandler;
import data_handlers.ability_handler.StatusEffectHandler;
import data_handlers.battle_handler.BattleHandler;
import data_handlers.battle_handler.PvpHandler;
import data_handlers.card_handler.CardHandler;
import data_handlers.chat_handler.ChatHandler;
import data_handlers.crafting_handler.CraftingHandler;
import data_handlers.item_handler.ContainerHandler;
import data_handlers.item_handler.ActionbarHandler;
import data_handlers.item_handler.EquipHandler;
import data_handlers.item_handler.InventoryHandler;
import data_handlers.item_handler.ItemHandler;
import data_handlers.monster_handler.MonsterHandler;
import data_handlers.party_handler.PartyHandler;
import utils.CrashLogger;
import utils.ServerMessage;

public class DataHandlers {
  private static Map<String, Consumer<Message>> dispatch = new HashMap<>();

  private static ConcurrentLinkedQueue<Message> incomingMessages;
  private static ConcurrentLinkedQueue<Message> outgoingMessages;

  public static void init() {
    incomingMessages = new ConcurrentLinkedQueue<Message>();
    outgoingMessages = new ConcurrentLinkedQueue<Message>();

    MonsterHandler.init();
    AbilityHandler.init();
    ContainerHandler.init();
    ChatHandler.init();
    FishingHandler.init();
    TrapHandler.init();
    CraftingHandler.init();
    PartyHandler.init();
    CardHandler.init();

    ConnectHandler.init();
    LoginHandler.init();
    WalkHandler.init();
    SkillHandler.init();
    ItemHandler.init();
    MapHandler.init();
    BattleHandler.init();
    BountyHandler.init();
    FriendsHandler.init();
    QuestHandler.init();
    ShopHandler.init();
    MusicHandler.init();
    GatheringHandler.init();
    SkinHandler.init();
    TutorialHandler.init();

    InventoryHandler.init();
    EquipHandler.init();
    ActionbarHandler.init();
  }

  public static void register(String type, Consumer<Message> handle) {
    dispatch.put(type, handle);
  }

  public static void update(long tick) {

    // Every 50 ms
    MonsterHandler.update(tick);
    BattleHandler.updateRangedCooldowns();

    AbilityHandler.updateProjectiles();
    AbilityHandler.updateAbilityEvents();
    AbilityHandler.updatePlayerCasting();

    // Every 200 ms
    if (tick % 4 == 0) {
      BattleHandler.update();
      AbilityHandler.updateCooldowns();
    }

    // Every 1000 ms
    if (tick % 20 == 0) {
      TrapHandler.update();
      StatusEffectHandler.updateStatusEffects();
      StatusEffectHandler.updateTileStatusEffects();
      PvpHandler.updatePKTimers();
      MapHandler.updateNightTime();
    }

    // Every 10000 ms
    if (tick % 200 == 0) {
      ContainerHandler.checkContainerRespawn();
    }

    // Every minute
    if (tick % 1200 == 0) {
      if (!ServerSettings.DEV_MODE) {
        int nrPlayers = Server.clients.size();
        WebsiteServerStatus.UpdateServerStatus(ServerSettings.SERVER_ID, nrPlayers);
      }
    }
  }

  public static void addIncomingMessage(Message message) {
    incomingMessages.add(message);
  }

  //public static void handleData(Client client, String message){

  public static void processIncomingData() {
    for (Iterator<Message> i = incomingMessages.iterator(); i.hasNext(); ) {
      Message m = i.next();
      Consumer<Message> handle = dispatch.get(m.type);
      if (handle != null) {
        handle.accept(m);
      } else {
        ServerMessage.printMessage("WARNING - Unknown message type: " + m.type, false);
      }
      i.remove();
    }
  }

  public static void addOutgoingMessage(Message message) {
    outgoingMessages.add(message);
  }

  public static void processOutgoingData() {
    for (Iterator<Message> i = outgoingMessages.iterator(); i.hasNext(); ) {
      Message m = i.next();

      sendMessage(m);

      i.remove();
    }
  }

  private static boolean sendMessage(Message message) {
    boolean sendOk = true;
    Client client = message.client;

    try {
      try {
        String dataToSend = "<" + message.type + ">" + message.message;

        byte[] byteMsg = (dataToSend).getBytes();
        if (client.out != null) {
          client.out.writeObject(byteMsg);
          client.out.reset();
          client.out.flush();
        }

      } catch (SocketException e) {
        sendOk = false;
      }
    } catch (IOException ioException) {
      CrashLogger.uncaughtException(ioException);
      sendOk = false;
    }
    return sendOk;
  }
}
