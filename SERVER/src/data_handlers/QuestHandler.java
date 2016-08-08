package data_handlers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CopyOnWriteArrayList;

import utils.ServerGameInfo;
import components.Quest;
import components.Ship;
import creature.Creature;
import creature.Npc;
import creature.Creature.CreatureType;
import data_handlers.ability_handler.Ability;
import data_handlers.battle_handler.BattleHandler;
import data_handlers.item_handler.CoinConverter;
import data_handlers.item_handler.EquipHandler;
import data_handlers.item_handler.InventoryHandler;
import data_handlers.item_handler.Item;
import map.Tile;
import network.Client;
import network.Server;

public class QuestHandler extends Handler {

	public static void init() {
		DataHandlers.register("talknpc", m -> handleTalkNpc(m));
		DataHandlers.register("quest", m -> handleQuest(m));
		DataHandlers.register("questdescr", m -> handleQuestDescription(m));
		DataHandlers.register("myquests", m -> handleMyQuests(m));
		DataHandlers.register("checkin", m -> handleCheckIn(m));
		DataHandlers.register("learn_class", m -> handleLearnClass(m));
	}
	
	public static void handleTalkNpc(Message m) {
		if (m.client.playerCharacter == null) return;
		Client client = m.client;
		String talkInfo[] = m.message.split(";");

		int tileX = Integer.parseInt(talkInfo[0]);
		int tileY = Integer.parseInt(talkInfo[1]);
		int tileZ = client.playerCharacter.getZ();

		getNpcDialog(client, tileX,tileY,tileZ);
	}

	public static void handleQuest(Message m) {
		if (m.client.playerCharacter == null) return;
		Client client = m.client;
		String questInfo[] = m.message.split(";");

		String action = questInfo[0];
		int questId = Integer.parseInt(questInfo[1]);

		// ACTION, QUEST ID
		if(action.equals("info")){
			getQuestInfo(client,questId);
		}else if(action.equals("add")){
			addQuest(client,questId);
		}
	}

	public static void handleQuestDescription(Message m) {
		if (m.client.playerCharacter == null) return;
		Client client = m.client;
		int questId = Integer.parseInt(m.message);

		ResultSet questInfo = Server.mapDB.askDB("select Description from quest where Id = "+questId);

		String questText = "";

		try {
			if(questInfo.next()){
				questText = questInfo.getString("Description");
			}
			questInfo.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		addOutGoingMessage(client, "questdesc", questText);
	}

	public static void handleMyQuests(Message m) {
		if (m.client.playerCharacter == null) return;
		Client client = m.client;
		ResultSet myQuestInfo = Server.userDB.askDB("select QuestId, Status from character_quest where CharacterId = "+client.playerCharacter.getDBId()+" and Status > 0 and Status < 3");

		StringBuilder questData = new StringBuilder(1000);

		try {
			while(myQuestInfo.next()){
				ResultSet questInfo = Server.mapDB.askDB("select Name, Type, Level from quest where Id = "+myQuestInfo.getInt("QuestId"));

				if(questInfo.next()){
					questData.append(myQuestInfo.getInt("QuestId")).append(',')
									 .append(questInfo.getString("Name")).append(',')
									 .append(questInfo.getString("Type")).append(',')
									 .append(myQuestInfo.getString("Status")).append(',')
									 .append(questInfo.getInt("Level")).append(';');
				}
				questInfo.close();
			}
			myQuestInfo.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if(questData.length() == 0){
			questData.append("None");
		}
		addOutGoingMessage(client, "myquests", questData.toString());
	}

	public static void handleCheckIn(Message m) {
		if (m.client.playerCharacter == null) return;
		Client client = m.client;
		int checkInId = Integer.parseInt(m.message);

		Server.userDB.updateDB("update user_character set CheckpointId = "+checkInId+" where Id = "+client.playerCharacter.getDBId());
		addOutGoingMessage(client,"message","#messages.quest.checked_in");
	}

	public static void handleLearnClass(Message m) {
		if (m.client.playerCharacter == null) return;
		Client client = m.client;
		String classInfo[] = m.message.split(",");
		int questId = Integer.parseInt(classInfo[0]);
		int classType = Integer.parseInt(classInfo[1]);

		// Get class that is learnt from quest
		ResultSet questInfo = Server.mapDB.askDB("select LearnClassId from quest where Id = "+questId);
		try {
			if(questInfo.next()){
				int classId = questInfo.getInt("LearnClassId");
				if(ClassHandler.learnClass(client,classId,classType)){
					EquipHandler.checkRequirements(client);
					rewardQuest(client,questId);
				}
			}
			questInfo.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void getNpcDialog(Client client, int tileX, int tileY, int tileZ){
		// CHECK IF THERE IS NPC ON TILE
		Tile TILE = Server.WORLD_MAP.getTile(tileX, tileY, tileZ);

		if(TILE.getOccupantType() == CreatureType.Monster){
			Npc NPC = Server.WORLD_MAP.getMonster(TILE.getOccupant().getDBId());

			// NPC IS FRIENDLY
			if(NPC.getAggroType() == 3){
				// SEND TALK TUTORIAL
				TutorialHandler.updateTutorials(2, client);

				updateTalkToQuests(client, NPC.getDBId());

				// NpcName / QuestId, QuestName, QuestStatus; QuestId2, QuestName2, QuestStatus2 / ShopOrNot

				StringBuilder npcInfo = new StringBuilder(1000);
				npcInfo.append(NPC.getDBId()).append(';')
				       .append(NPC.getX()).append(';')
				       .append(NPC.getY()).append(';')
				       .append(NPC.getName()).append('/');

				// GET QUESTS FROM NPC
				ResultSet rs = Server.mapDB.askDB("select Id, Name, Type, ParentQuestId, Level from quest where NpcId = "+NPC.getDBId()+" order by OrderNr asc");

				try {
					int nrQuests = 0;

					while(rs.next()){
						boolean showOk = true; 

						// IF QUEST HAS PARENT QUEST
						if(rs.getInt("ParentQuestId") > 0){
							showOk = false;
							// CHECK IF PARENT QUEST IS COMPLETED
							ResultSet checkParentQ = Server.userDB.askDB("select Status from character_quest where QuestId = "+rs.getInt("ParentQuestId")+" and CharacterId = "+client.playerCharacter.getDBId());
							if(checkParentQ.next()){
								if(checkParentQ.getInt("Status") == 3){
									showOk = true;
								}
							}
							checkParentQ.close();
						}

						// NOT SHOW COMPLETED QUESTS
						ResultSet qStatus = Server.userDB.askDB("select Status from character_quest where QuestId = "+rs.getInt("Id")+" and CharacterId = "+client.playerCharacter.getDBId());

						int questStatus = 0; // 0 = new, 1 = accepted, 2 = get reward, 3 = completed

						if(qStatus.next()){
							questStatus = qStatus.getInt("Status");
						}
						qStatus.close();

						if(questStatus == 3){
							showOk = false;
						}

						if(showOk){
							// SEND QUEST IF NOT COMPLETED
							
							npcInfo.append(rs.getInt("Id")).append(',')
							       .append(rs.getString("Name")).append(',')
							       .append(rs.getString("Type")).append(',')
							       .append(rs.getInt("Level")).append(',')
							       .append(questStatus).append(';');

							nrQuests++;
						}
					}
					rs.close();

					if(nrQuests == 0){
						npcInfo.append("None");
					}

					// GET SHOP FROM NPC

					int shopId = 0;

					rs = Server.gameDB.askDB("select Id from shop where NpcId = "+NPC.getDBId());

					if(rs.next()){
						shopId = rs.getInt("Id");
					}
					rs.close();

					npcInfo.append('/').append(shopId);

					// GET CHECKIN FROM NPC
					int checkInId = 0;

					rs = Server.mapDB.askDB("select Id from checkpoint where NpcId = "+NPC.getDBId());

					if(rs.next()){
						checkInId = rs.getInt("Id");
					}
					rs.close();

					npcInfo.append('/').append(checkInId);

					// GET BOUNTY MERCHANT

					int bountyId = 0;
/*
					rs = Server.gameDB.askDB("select Id from bountyhut where NpcId = "+NPC.getDBId());

					if(rs.next()){
						bountyId = rs.getInt("Id");
					}
					rs.close();
*/
					npcInfo.append('/').append(bountyId);


				} catch (SQLException e) {
					e.printStackTrace();
				}

				addOutGoingMessage(client,"talknpc",npcInfo.toString());

			}
		}

	}


	public static void addQuest(Client client, int questId){
		// CHECK IF PLAYER ALREADY HAVE QUEST
		// AND IF HAVE REQUIREMENTS

		ResultSet checkQuest = Server.mapDB.askDB("select Level, Type, Name, ParentQuestId from quest where Id = "+questId);

		boolean addOk = true;

		try {
			if(checkQuest.next()){
				if(checkQuest.getString("Type").equals("Instructions") || checkQuest.getString("Type").equals("Story")){
					addOk = false;
				}else{
					// CHECK IF COMPLETED PARENT QUEST
					if(checkQuest.getInt("ParentQuestId") > 0){
						int parentQuestStatus = 0;
						ResultSet parentQuestInfo = Server.userDB.askDB("select Status from character_quest where QuestId = "+checkQuest.getInt("ParentQuestId")+" and CharacterId = "+client.playerCharacter.getDBId());

						if(parentQuestInfo.next()){
							parentQuestStatus = parentQuestInfo.getInt("Status");
						}
						parentQuestInfo.close();

						if(parentQuestStatus < 3){
							addOk = false;
						}
					}
					// CHECK IF ALREADY HAVE QUEST
					ResultSet haveQuest = Server.userDB.askDB("select Status from character_quest where QuestId = "+questId+" and CharacterId = "+client.playerCharacter.getDBId());
					if(haveQuest.next()){
						addOk = false;
					}
					haveQuest.close();
				}
				// IF ALL IS FINE, ADD QUEST
				if(addOk){
					checkQuestItem(client, questId);
					checkQuestAbility(client, questId);

					addOutGoingMessage(client,"quest", "add;"+checkQuest.getString("Name"));
					client.playerCharacter.addQuest(questId);
				}

			}
			checkQuest.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void checkQuestAbility(Client client, int questId){
		// CHECK IF THERE IS A QUEST ITEM
		Quest addedQuest = new Quest(ServerGameInfo.questDef.get(questId));
	
		// Check if quest gives ability
		if(addedQuest.getQuestAbilityId() != 0){
			// Check if player has ability already
			if(client.playerCharacter != null){
				if(client.playerCharacter.getAbilityById(addedQuest.getQuestAbilityId()) == null){
					client.playerCharacter.addAbility(new Ability(ServerGameInfo.abilityDef.get(addedQuest.getQuestAbilityId())));
					Server.userDB.updateDB("insert into character_ability (CharacterId, AbilityId, CooldownLeft) values ("+client.playerCharacter.getDBId()+","+addedQuest.getQuestAbilityId()+",0)");
					addOutGoingMessage(client,"abilitydata","0/"+client.playerCharacter.getAbilitiesAsString());
					addOutGoingMessage(client,"message","#messages.quest.gained"+" '"+ServerGameInfo.abilityDef.get(addedQuest.getQuestAbilityId()).getName()+"' #messages.quest.ability");
				}
			}
		}
	}

	// Check if there is a quest item to be given before doing the quest
	public static void checkQuestItem(Client client, int questId){
		// CHECK IF THERE IS A QUEST ITEM
		Quest addedQuest = new Quest(ServerGameInfo.questDef.get(questId));
	
		for(Item questItem: addedQuest.getQuestItems()){
			InventoryHandler.addItemToInventory(client, questItem);
		}
	}

	public static void getQuestInfo(Client client, int questId){
		ResultSet questInfo = Server.mapDB.askDB("select QuestMessage, RewardMessage, RewardXp, RewardItemId, RewardCopper, Type, NextQuestId from quest where Id = "+questId);

		try {
			if(questInfo.next()){

				String questMessage = "";
				int questStatus = 0;

				String questType = questInfo.getString("Type");

				// GET QUEST STATUS
				ResultSet questStatusInfo = Server.userDB.askDB("select Status from character_quest where QuestId = "+questId+" and CharacterId = "+client.playerCharacter.getDBId());

				if(questStatusInfo.next()){
					questStatus = questStatusInfo.getInt("Status");
				}
				questStatusInfo.close();

				

				/*
				// IF GET ITEM X QUEST, REMOVE ITEM AND COMPLETE QUEST
				if(questStatus == 1 && questInfo.getString("Type").equals("Get X item X")){
					int itemId = questInfo.getInt("TargetId");
					int targetNr = questInfo.getInt("TargetNr");

					//CHECK IF HAS CORRECT NR OF ITEMS
					ResultSet itemInfo = Server.userDB.askDB("select Id from character_item where CharacterId = "+client.playerCharacter.getDBId()+" and ItemId = "+itemId+" and InventoryPos <> 'None' and InventoryPos <> 'Mouse'  and InventoryPos <> 'Actionbar'");

					int nrRows = 0;
					while(itemInfo.next()){
						nrRows++;
					}
					itemInfo.close();

					if(targetNr <= nrRows){
						itemInfo = Server.userDB.askDB("select Id from character_item where CharacterId = "+client.playerCharacter.getDBId()+" and ItemId = "+itemId+" and InventoryPos <> 'None' and InventoryPos <> 'Mouse'  and InventoryPos <> 'Actionbar'");
						while(targetNr > 0 && itemInfo.next()){
							Server.userDB.updateDB("delete from character_item where Id = "+itemInfo.getInt("Id"));
							targetNr--;
						}
						itemInfo.close();
						questStatus = 2;
					}
				}
*/

				// COMPLETE QUESTS OF "STORY" TYPE DIRECTLY 
				if(questInfo.getString("Type").equals("Story")){
					// GIVE QUEST ITEM
					checkQuestItem(client,questId);
					Server.userDB.updateDB("insert into character_quest (QuestId, CharacterId, Status) values ("+questId+","+client.playerCharacter.getDBId()+",3)");

					// CHECK IF QUEST TRIGGERS NEW QUEST
					if(questInfo.getInt("NextQuestId") > 0){
						addQuest(client,questInfo.getInt("NextQuestId"));
					}
				}

				// IF COMPLETED, FINISH QUEST, GIVE REWARD
				boolean completedQuest = false;
				
				if(questStatus == 2){
					TutorialHandler.updateTutorials(6, client);
					completedQuest = rewardQuest(client,questId);

					questStatus = 3;

				}
				
				// Quest dialog
				if(completedQuest){
					questMessage = questInfo.getString("RewardMessage");
				}else{
					questMessage = questInfo.getString("QuestMessage");
				}
				
				addOutGoingMessage(client,"questinfo",questId+";"+questType+";"+questMessage+";"+questStatus);
			}
			questInfo.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}


	}

	/*
	 * 
	 * 	DIFFERENT QUEST TYPES
	 * 
	 */

	// USE X ITEM X
	public static void updateUseItemQuests(Client client, int itemId){
		for(Quest q: client.playerCharacter.getQuests()){
			if(q.getType().toLowerCase().equals("use item x") && q.getStatus() == 1 && q.getTargetId() == itemId){
				// COMPLETE QUEST
				q.setStatus(2);
				Server.userDB.updateDB("update character_quest set Status = 2 where QuestId = "+q.getId()+" and CharacterId = "+client.playerCharacter.getDBId());
				addOutGoingMessage(client,"quest","complete;"+q.getName());
				if(!q.isReturnForReward()){
					rewardQuest(client, q.getId());
				}
			}
		}
	}

	// KILL X CREATURE X
	public static void updateKills(Client client, Creature victim){

		// CHECK IF KILLED CREATURE BEFORE
		int nrKills = 0;
		ResultSet rs = Server.userDB.askDB("select Kills from character_kills where CreatureId = "+victim.getCreatureId()+" and CharacterId = "+client.playerCharacter.getDBId());
		try {
			if(rs.next()){
				nrKills = rs.getInt("Kills") + 1;
				Server.userDB.updateDB("update character_kills set Kills = Kills + 1  where CreatureId = "+victim.getCreatureId()+" and CharacterId = "+client.playerCharacter.getDBId());
			}else{
				nrKills = 1;
				Server.userDB.updateDB("insert into character_kills (CharacterId, CreatureId, Kills) values ("+client.playerCharacter.getDBId()+","+victim.getCreatureId()+",1)");
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		CopyOnWriteArrayList<Quest> playerQuests = client.playerCharacter.getQuests();
		for(Quest q: playerQuests){
			if(q != null){
				// CHECK IF COMPLETED ANY "KILL X CREATURE X" - QUESTS
				if(q.getType().equals("Kill X creature X") && q.getStatus() == 1 && q.getTargetId() == victim.getCreatureId()){
					addOutGoingMessage(client,"quest","update;"+q.getName()+";"+nrKills+" / "+q.getTargetNumber()+" killed");

					if(nrKills >= q.getTargetNumber()){
						q.setStatus(2);

						// COMPLETED QUEST
						Server.userDB.updateDB("update character_quest set Status = 2 where QuestId = "+q.getId()+" and CharacterId = "+client.playerCharacter.getDBId());
						addOutGoingMessage(client,"quest","complete;"+q.getName());

						// CHECK KILL PRACTICE TARGET TUTORIAL
						if(q.getId() == 36){
							TutorialHandler.updateTutorials(5, client);
						}

						if(!q.isReturnForReward()){
							rewardQuest(client, q.getId());
						}
						checkQuestEvents(client, q.getId());
					}
				}
				// CHECK IF COMPLETED ANY "KILL DIFFERENT CREATURES X" - QUESTS
				if(q.getType().equals("Kill different creatures") && q.getStatus() == 1){
					String targetIds[] = q.getTargetType().split(",");
					boolean completedDiffKills = true;
					int totalNrTargets = targetIds.length;
					int nrTargetsKilled = 0;
					boolean updateQuest = false;

					for(String targetId: targetIds){
						if(victim.getCreatureId() == Integer.parseInt(targetId)){
							updateQuest = true;
						}
					}

					if(updateQuest){
						for(String targetId : targetIds){
							ResultSet killInfo = Server.userDB.askDB("select Kills from character_kills where CreatureId = "+targetId);
							try {
								if(killInfo.next()){
									if(killInfo.getInt("Kills") == 0){
										completedDiffKills = false;
									}else{
										nrTargetsKilled++;
									}	
								}else{
									completedDiffKills = false;
								}
								killInfo.close();
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}

						if(nrTargetsKilled > 0){
							addOutGoingMessage(client,"quest","update;"+q.getName()+";"+nrTargetsKilled+" / "+totalNrTargets+" targets killed");
						}

						if(completedDiffKills){
							q.setStatus(2);
							// COMPLETED QUEST
							Server.userDB.updateDB("update character_quest set Status = 2 where QuestId = "+q.getId()+" and CharacterId = "+client.playerCharacter.getDBId());
							addOutGoingMessage(client,"quest","complete;"+q.getName());
						}
					}
				}
			}
		}
	}

	// "FIND AREA X" - QUESTS
	public static void updateFindAreaQuests(Client client, int areaEffectId){
		for(Quest q: client.playerCharacter.getQuests()){
			if(q.getType().toLowerCase().equals("go to area x") && q.getStatus() == 1 && q.getTargetId() == areaEffectId){
				q.setStatus(2);
				// COMPLETED QUEST
				Server.userDB.updateDB("update character_quest set Status = 3 where QuestId = "+q.getId()+" and CharacterId = "+client.playerCharacter.getDBId());
				addOutGoingMessage(client,"quest","complete;"+q.getName());
				if(!q.isReturnForReward()){
					rewardQuest(client, q.getId());
				}
				checkQuestEvents(client, q.getId());
			}
		}
	}

	// "TALK TO CREATURE X" - QUESTS
	public static void updateTalkToQuests(Client client, int npcId){
		for(Quest q: client.playerCharacter.getQuests()){
			if(q.getType().toLowerCase().equals("talk to creature x") && q.getStatus() == 1 && q.getTargetId() == npcId){
				// GIVE SECOND SHIP
				if(q.getId() == 170){
					client.playerCharacter.setShip(new Ship(2));
					Server.userDB.updateDB("update user_character set ShipId = 2 where Id = "+client.playerCharacter.getDBId());
					addOutGoingMessage(client,"getboat","rowboat,2");
					addOutGoingMessage(client,"message","#messages.quest.travel_deep_water");

				}
				q.setStatus(2);
				if(!q.isReturnForReward()){
					rewardQuest(client, q.getId());
				}
				addOutGoingMessage(client,"quest","complete;"+q.getName());
			}
		}
	}

	// "GET X ITEMS X" - QUESTS
	public static void updateItemQuests(Client client, int itemId){
		Item itemToCheck = ServerGameInfo.itemDef.get(itemId);
		
		for(Quest q: client.playerCharacter.getQuests()){
			if(q.getType().toLowerCase().equals("get x items x") && q.getStatus() == 1 && q.getTargetId() == itemId){
				boolean questCompleted = false;
				
				if(itemToCheck.getType().equals("Key")){
					// If item is a key, check keychain
					ResultSet keyInfo = Server.userDB.askDB("select Id from character_key where KeyId = "+itemId+" and CharacterId = "+client.playerCharacter.getDBId());
					
					try {
						if(keyInfo.next()){
							questCompleted = true;
						}
						keyInfo.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}else{
					// CHECK IF RIGHT NR
					int nrItems = 0;
					ResultSet itemInfo = Server.userDB.askDB("select Nr from character_item where InventoryPos <> 'Mouse' and Equipped = 0 and ItemId = "+itemId+" and CharacterId = "+client.playerCharacter.getDBId());
					try {
						while(itemInfo.next() && nrItems < q.getTargetNumber()){
							nrItems += itemInfo.getInt("Nr");
						}
						itemInfo.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
					
					if(nrItems >= q.getTargetNumber()){
						questCompleted = true;
					}else{
						Item questItem = new Item(ServerGameInfo.itemDef.get(itemId));
						addOutGoingMessage(client,"quest","update;"+q.getName()+";"+nrItems+" / "+q.getTargetNumber()+" "+questItem.getName()+" collected");
					}
				}
				
				if(questCompleted){
					// COMPLETE QUEST
					q.setStatus(2);
					Server.userDB.updateDB("update character_quest set Status = 2 where QuestId = "+q.getId()+" and CharacterId = "+client.playerCharacter.getDBId());
					addOutGoingMessage(client,"quest","complete;"+q.getName());
					if(!q.isReturnForReward()){
						rewardQuest(client, q.getId());
					}
				}
			}
		}
	}

	/**
	 * 
	 * QUEST COMPLETION/REWARD
	 *
	 **/
	
	public static boolean rewardQuest(Client client, int questId){
		boolean rewardOk = true;

		ResultSet questInfo = Server.mapDB.askDB("select * from quest where Id = "+questId);

		// GIVE REWARD FOR COMPLETING QUEST
		try {
			if(questInfo.next()){
			

				// CHECK IF ITEM QUEST
				if(questInfo.getString("Type").toLowerCase().equals("get x items x")){
					// CHECK THAT PLAYER HAS ITEMS IN HIS INVENTORY
					
					int checkItemId = questInfo.getInt("TargetId");
					int checkItemNr = questInfo.getInt("TargetNumber");

					Item itemToCheck = ServerGameInfo.itemDef.get(checkItemId);
					if(!itemToCheck.getType().equals("Key")){
						int nrItems = InventoryHandler.countPlayerItem(client,checkItemId);
	
						if(nrItems >= checkItemNr){
							// HAS ITEMS, REMOVE THEM FROM INVENTORY
							InventoryHandler.removeNumberOfItems(client,checkItemId, checkItemNr);
						}else{
							addOutGoingMessage(client,"message","#messages.quest.missing_items");
							rewardOk = false;
						}
					}
				}

				if(rewardOk){

					Server.userDB.updateDB("update character_quest set Status = 3 where QuestId = "+questId+" and CharacterId = "+client.playerCharacter.getDBId());

					int rewardCopper = questInfo.getInt("RewardCopper");
					int rewardXp = questInfo.getInt("RewardXp");
					int rewardItemId = questInfo.getInt("RewardItemId");
					int rewardAbilityId = questInfo.getInt("RewardAbilityId");

					if(rewardItemId > 0){
						InventoryHandler.addItemToInventory(client, new Item(ServerGameInfo.itemDef.get(rewardItemId)));
					}

					if(rewardCopper > 0){
						CoinConverter cc = new CoinConverter(rewardCopper);

						if(cc.getGold() > 0){
							Item GoldItem = new Item(ServerGameInfo.itemDef.get(34));
							GoldItem.setStacked(cc.getGold());
							InventoryHandler.addItemToInventory(client, GoldItem);
						}
						if(cc.getSilver() > 0){
							Item SilverItem = new Item(ServerGameInfo.itemDef.get(35));
							SilverItem.setStacked(cc.getSilver());
							InventoryHandler.addItemToInventory(client, SilverItem);
						}
						if(cc.getCopper() > 0){
							Item CopperItem = new Item(ServerGameInfo.itemDef.get(36));
							CopperItem.setStacked(cc.getCopper());
							InventoryHandler.addItemToInventory(client, CopperItem);
						}
					}

					if(rewardXp > 0){
						BattleHandler.addXP(client, rewardXp);
					}

					if(rewardAbilityId > 0){
						// Check if player has ability
						Ability checkAbility = client.playerCharacter.getAbilityById(rewardAbilityId);
						if(checkAbility == null){
							// Add ability
							Ability newAbility = new Ability(ServerGameInfo.abilityDef.get(rewardAbilityId));
							Server.userDB.updateDB("insert into character_ability (CharacterId, AbilityId, CooldownLeft) values ("+client.playerCharacter.getDBId()+","+rewardAbilityId+",0)");
							client.playerCharacter.addAbility(newAbility);
							addOutGoingMessage(client,"message", "#messages.quest.gained# '"+newAbility.getName()+"' #messages.quest.ability");
							addOutGoingMessage(client, "abilitydata", "0/"+client.playerCharacter.getAbilitiesAsString());
						}else{
							addOutGoingMessage(client,"message", "#messages.quest.already_have# '"+checkAbility.getName()+"' #messages.quest.ability");
						}
					}



					// SPECIAL OCCASION
					// GET THE RAFT
					if(questId == 6){
						Server.userDB.updateDB("update user_character set ShipId = 1 where Id = "+client.playerCharacter.getDBId());
						client.playerCharacter.setShip(new Ship(1));
						addOutGoingMessage(client,"getboat","raft,1");
					}

					Quest myQuest = client.playerCharacter.getQuestById(questId); 
					if(myQuest != null){
						myQuest.setStatus(3);

						// CHECK IF QUEST TRIGGERS NEW QUEST
						if(myQuest.getNextQuestId() > 0){
							addQuest(client,myQuest.getNextQuestId());
						}
					}
				}
			}
			questInfo.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rewardOk;
	}


	

	public static void checkQuestEvents(Client client, int questId){
		ResultSet questInfo = Server.mapDB.askDB("select EventId from quest where Id = "+questId);
		try {
			if(questInfo.next()){
				if(questInfo.getInt("EventId") > 0){
					addOutGoingMessage(client,"cutscene",""+questInfo.getInt("EventId"));
				}
			}
			questInfo.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}