package data_handlers;

public class DataHandlers {

  public static void init() {
    ItemHandler.init();
    MonsterHandler.init();
  }

  public static void handleData(String serverData) {
    LoginHandler.handleData(serverData);

    BattleHandler.handleData(serverData);

    ItemHandler.handleData(serverData);

    ConnectHandler.handleData(serverData);

    AbilityHandler.handleData(serverData);

    SkillHandler.handleData(serverData);

    ShopHandler.handleData(serverData);

    QuestHandler.handleData(serverData);

    MapHandler.handleData(serverData);

    MonsterHandler.handleData(serverData);

    BountyHandler.handleData(serverData);

    WalkHandler.handleData(serverData);

    CreatureHandler.handleData(serverData);

    ChatHandler.handleData(serverData);

    ContainerHandler.handleData(serverData);

    FriendsHandler.handleData(serverData);

    BoatHandler.handleData(serverData);

    AreaEffectHandler.handleData(serverData);

    TrapHandler.handleData(serverData);

    MusicHandler.handleData(serverData);

    FishingHandler.handleData(serverData);

    GatheringHandler.handleData(serverData);

    SkinHandler.handleData(serverData);

    MovieHandler.handleData(serverData);

    CraftingHandler.handleData(serverData);

    TutorialHandler.handleData(serverData);

    PartyHandler.handleData(serverData);

    ClassHandler.handleData(serverData);

    CardHandler.handleData(serverData);
  }
}
