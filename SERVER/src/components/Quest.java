package components;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import data_handlers.item_handler.Item;
import utils.ServerGameInfo;

public class Quest {

	private int Id;
	private int Status; // 0 = new, 1 = accepted, 2 = get reward, 3 = completed

	private int ParentQuestId;

	private String Name;
	private String QuestMessage;
	private String RewardMessage;

	private int Level;
	private String Type;
	private int TargetNumber;
	private String TargetType;
	private int TargetId;
	private int NextQuestId;

	private Vector<Item> questItems;
	private int questAbilityId;


	private int tempTextLines;
	private int NrQuestLines;
	private int NrRewardLines;

	private boolean ReturnForReward;


	public Quest(int newId) {
		Id = newId;
		questItems = new Vector<Item>();
		setQuestAbilityId(0);
	}

	public Quest(Quest questDef){
		Id = questDef.getId();
		
		Name = questDef.getName();

		QuestMessage = questDef.getQuestMessage();
		RewardMessage = questDef.getRewardMessage();
		
		
		Level = questDef.getLevel();
		Type = questDef.getType();
		TargetNumber = questDef.getTargetNumber();
		TargetType = questDef.getTargetType();
		TargetId = questDef.getTargetId();

		NrQuestLines = tempTextLines;
		
		NrRewardLines = tempTextLines;

		questItems = new Vector<Item>();
		
		questItems = questDef.questItems;
		
		questAbilityId = questDef.getQuestAbilityId();
		
		setNextQuestId(questDef.getNextQuestId());

		setReturnForReward(questDef.isReturnForReward());
		
		ParentQuestId = questDef.getParentQuestId();
	}
	
	
	public String justifyLeft( int width,String st) {
		if(!st.equals("")){
			StringBuffer buf = new StringBuffer(st);
			int lastspace = -1;
			int linestart = 0;
			int i = 0;

			tempTextLines = 1;
			while (i < buf.length()) {
				if ( buf.charAt(i) == ' ' ) lastspace = i;
				if ( buf.charAt(i) == '\n' ) {
					lastspace = -1;
					linestart = i+1;
					tempTextLines++;
				}
				if (i > linestart + width - 1 ) {
					if (lastspace != -1) {
						buf.setCharAt(lastspace,'\n');
						linestart = lastspace+1;
						lastspace = -1;
						tempTextLines++;
					}
					else {
						buf.insert(i,'\n');
						linestart = i+1;
						tempTextLines++;
					}
				}
				i++;
			}

			return buf.toString();
		}
		return st;
	}


	public void loadQuest(ResultSet rs){
		try {
			Name = rs.getString("Name");

			QuestMessage = rs.getString("QuestMessage");
			RewardMessage = rs.getString("RewardMessage");

			Level = rs.getInt("Level");
			Type = rs.getString("Type");
			TargetNumber = rs.getInt("TargetNumber");
			TargetType = rs.getString("TargetType");
			TargetId = rs.getInt("TargetId");

			if(!QuestMessage.equals("")){
				QuestMessage = justifyLeft(42,QuestMessage);
			}

			NrQuestLines = tempTextLines;
			if(!RewardMessage.equals("")){
				RewardMessage = justifyLeft(42,RewardMessage);
			}
			NrRewardLines = tempTextLines;

			// Add quest items
			if(!rs.getString("QuestItems").equals("None")){
				String questItemsInfo[] = rs.getString("QuestItems").split(";");
				
				for(String questItemInfo: questItemsInfo){
					String questItemIdNr[] = questItemInfo.split(",");
						
					int questItemId = Integer.parseInt(questItemIdNr[0]);
					int questItemNr = Integer.parseInt(questItemIdNr[1]);

					Item questItem = new Item(ServerGameInfo.itemDef.get(questItemId));
					questItem.setStacked(questItemNr);
					questItems.add(questItem);
				}
			}
			
			questAbilityId = rs.getInt("QuestAbilityId");
			
			setNextQuestId(rs.getInt("NextQuestId"));

			if(rs.getInt("ReturnForReward") == 0){
				setReturnForReward(false);
			}else{
				setReturnForReward(true);
			}

			ParentQuestId = rs.getInt("ParentQuestId");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/****************************************
	 *                                      *
	 *             GETTER/SETTER            *
	 *                                      *
	 *                                      *
	 ****************************************/

	public String getName() {
		return Name;
	}


	public int getId() {
		return Id;
	}



	public int getLevel() {
		return Level;
	}

	public String getType() {
		return Type;
	}

	public int getTargetNumber() {
		return TargetNumber;
	}

	public String getTargetType() {
		return TargetType;
	}

	public int getTargetId() {
		return TargetId;
	}

	public int getNrQuestLines() {
		return NrQuestLines;
	}

	public int getNrRewardLines() {
		return NrRewardLines;
	}

	public String getQuestMessage() {
		return QuestMessage;
	}

	public String getRewardMessage() {
		return RewardMessage;
	}


	public void setTargetNumber(int newNr) {
		TargetNumber = newNr;
	}

	public void setStatus(int newStatus) {
		Status = newStatus;
	}

	public int getStatus() {
		return Status;
	}	

	public int getParentQuestId(){
		return ParentQuestId;
	}

	public int getNextQuestId() {
		return NextQuestId;
	}

	public void setNextQuestId(int nextQuestId) {
		NextQuestId = nextQuestId;
	}

	public Vector<Item> getQuestItems() {
		return questItems;
	}

	public boolean isReturnForReward() {
		return ReturnForReward;
	}

	public void setReturnForReward(boolean returnForReward) {
		ReturnForReward = returnForReward;
	}

	public int getQuestAbilityId() {
		return questAbilityId;
	}

	public void setQuestAbilityId(int questAbilityId) {
		this.questAbilityId = questAbilityId;
	}


}