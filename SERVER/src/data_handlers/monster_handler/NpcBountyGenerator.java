package data_handlers.monster_handler;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import utils.ServerGameInfo;
import utils.RandomUtils;
import utils.TimeUtils;
import creature.NameGenerator;
import creature.Npc;
import data_handlers.item_handler.Item;
import network.Server;

public class NpcBountyGenerator {

	public static void loadBounties(){
		for(int level = 10; level < 50; level += 10){
			ResultSet bountyInfo = Server.userDB.askDB("select * from bounty where Level = "+level+" and KillerType = 'None'");
			try {
				if(bountyInfo.next()){
					Npc m = Server.WORLD_MAP.getMonster(bountyInfo.getInt("AreaCreatureId"));
					m.getCustomization().setMouthFeatureId(bountyInfo.getInt("MouthFeatureId"));
					m.getCustomization().setAccessoriesId(bountyInfo.getInt("AccessoriesId"));
					
					if(ServerGameInfo.itemDef.containsKey(bountyInfo.getInt("WeaponId"))){
						m.equipItem(new Item(ServerGameInfo.itemDef.get(bountyInfo.getInt("WeaponId"))));
					}
					
					if(ServerGameInfo.itemDef.containsKey(bountyInfo.getInt("OffHandId"))){
						m.equipItem(new Item(ServerGameInfo.itemDef.get(bountyInfo.getInt("OffHandId"))));
					}
					
					if(ServerGameInfo.itemDef.containsKey(bountyInfo.getInt("HeadId"))){
						m.equipItem(new Item(ServerGameInfo.itemDef.get(bountyInfo.getInt("HeadId"))));
					}
					
					if(ServerGameInfo.itemDef.containsKey(bountyInfo.getInt("AmuletId"))){
						m.equipItem(new Item(ServerGameInfo.itemDef.get(bountyInfo.getInt("AmuletId"))));
					}
					
					if(ServerGameInfo.itemDef.containsKey(bountyInfo.getInt("ArtifactId"))){
						m.equipItem(new Item(ServerGameInfo.itemDef.get(bountyInfo.getInt("ArtifactId"))));
					}
					
					m.setAggroType(1);
					m.setName(bountyInfo.getString("Name"));

					m.setupMonsterBounty(level);

				}else {
					generateNewBounty(level);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static void generateNewBounty(int level){
		ResultSet mobInfo = Server.mapDB.askDB("select Id from area_creature where AggroType = 2 and MobLevel > "+(level-4)+" and MobLevel < "+(level + 4)+" order by random() limit 1");
		try {
			if(mobInfo.next()){
				Npc m = Server.WORLD_MAP.getMonster(mobInfo.getInt("Id"));

				int MouthFeatureId = 0;
				int AccessoriesId = 0;
				int bounty = level*50;

				int chanceOfFeature = RandomUtils.getInt(0,2);

				if(chanceOfFeature == 0){
					ResultSet accessoriesInfo = Server.gameDB.askDB("select Id from item where Type = 'Customization' and SubType = 'Accessories' order by random() limit 1");
					if(accessoriesInfo.next()){
						AccessoriesId = accessoriesInfo.getInt("Id");
						m.getCustomization().setAccessoriesId(accessoriesInfo.getInt("Id"));
					}
					accessoriesInfo.close();
				}

				chanceOfFeature = RandomUtils.getInt(0,2);

				if(chanceOfFeature == 0){
					ResultSet accessoriesInfo = Server.gameDB.askDB("select Id from item where Type = 'Customization' and SubType = 'Mouth Feature' order by random() limit 1");
					if(accessoriesInfo.next()){
						MouthFeatureId = accessoriesInfo.getInt("Id");
						m.getCustomization().setMouthFeatureId(accessoriesInfo.getInt("Id"));
					}
					accessoriesInfo.close();
				}

				m.getCustomization().setAccessoriesId(AccessoriesId);
				m.getCustomization().setMouthFeatureId(MouthFeatureId);

				int weaponItem = 0;
				ResultSet weaponInfo = Server.gameDB.askDB("select Id from item where (Type = 'Weapon' and ReqLevel < "+(level+5)+" and ReqLevel > "+(level - 5)+") or SubType = 'Weapon' order by random() limit 1");
				if(weaponInfo.next()){
					weaponItem = weaponInfo.getInt("Id");
				}
				weaponInfo.close();

				int offHandItem = 0;
				ResultSet offHandInfo = Server.gameDB.askDB("select Id from item where (Type = 'OffHand' and ReqLevel < "+(level+5)+" and ReqLevel > "+(level - 5)+") or SubType = 'OffHand' order by random() limit 1");
				if(offHandInfo.next()){
					offHandItem = offHandInfo.getInt("Id");
				}
				offHandInfo.close();

				int headItem = 0;
				ResultSet headInfo = Server.gameDB.askDB("select Id from item where (Type = 'Head' and ReqLevel < "+(level+5)+" and ReqLevel > "+(level - 5)+") or SubType = 'Head' order by random() limit 1");
				if(headInfo.next()){
					headItem = headInfo.getInt("Id");
				}
				headInfo.close();

				int amuletItem = 0;
				ResultSet amuletInfo = Server.gameDB.askDB("select Id from item where (Type = 'Amulet' and ReqLevel < "+(level+5)+" and ReqLevel > "+(level - 5)+") or SubType = 'Amulet' order by random() limit 1");
				if(amuletInfo.next()){
					amuletItem = amuletInfo.getInt("Id");
				}
				amuletInfo.close();

				int artifactItem = 0;
				ResultSet artifactInfo = Server.gameDB.askDB("select Id from item where (Type = 'Artifact' and ReqLevel < "+(level+5)+" and ReqLevel > "+(level - 5)+") or SubType = 'Artifact' order by random() limit 1");
				if(artifactInfo.next()){
					artifactItem = artifactInfo.getInt("Id");
				}
				artifactInfo.close();

				if(weaponItem > 0){
					if(ServerGameInfo.itemDef.containsKey(weaponItem)){
						m.equipItem(new Item(ServerGameInfo.itemDef.get(weaponItem)));
					}
				}

				if(headItem > 0){
					if(ServerGameInfo.itemDef.containsKey(headItem)){
						m.equipItem(new Item(ServerGameInfo.itemDef.get(headItem)));
					}
				}

				if(offHandItem > 0){
					if(ServerGameInfo.itemDef.containsKey(offHandItem)){
						m.equipItem(new Item(ServerGameInfo.itemDef.get(offHandItem)));
					}
				}

				if(artifactItem > 0){
					if(ServerGameInfo.itemDef.containsKey(offHandItem)){
						m.equipItem(new Item(ServerGameInfo.itemDef.get(offHandItem)));
					}
				}

				if(amuletItem > 0){
					if(ServerGameInfo.itemDef.containsKey(amuletItem)){
						m.equipItem(new Item(ServerGameInfo.itemDef.get(amuletItem)));
					}
				}



				/*
				int HeadSkinId = 0;
				int WeaponSkinId = 0;
				int OffHandSkinId = 0;
				int AmuletSkinId = 0;
				int ArtifactSkinId = 0;
				 */

				int nameType = RandomUtils.getInt(0,2);
				String filename = "roman";
				if(nameType == 1){
					filename = "elven";
				}else if(nameType == 2){
					filename = "goblin";
				}

				String mobName = m.getName();

				try {
					NameGenerator ng = new NameGenerator(filename+".txt");
					int nrSyl = RandomUtils.getInt(1,2);
					mobName = ng.compose(nrSyl);
					nrSyl = RandomUtils.getInt(1,2);
					mobName += " "+ng.compose(nrSyl);

					m.setName(mobName);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				m.setAggroType(1);


				// SAVE BOUNTY
				Server.userDB.updateDB("insert into bounty (AreaCreatureId, WeaponId, OffHandId, HeadId, AmuletId, ArtifactId, MouthFeatureId, AccessoriesId, Name, Level, Bounty, KillerType, KillerId, KilledDate, CreatedDate) values ("+m.getDBId()+","+weaponItem+","+offHandItem+","+headItem+","+amuletItem+","+artifactItem+","+MouthFeatureId+","+AccessoriesId+",'"+mobName+"',"+level+","+bounty+",'None',0,'0000-00-00 00:00:00','"+TimeUtils.now()+"')");

				m.setupMonsterBounty(level);

			}
			mobInfo.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
