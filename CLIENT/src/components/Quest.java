package components;


public class Quest {

	private int Id;
	private int Status;
	
	private String Name;
	private String Message;
	private String Description;
	
	
	private int Level;
	private String Type;
	private int ActiveNumber;
	private int TargetNumber;
	private String TargetType;
	private int TargetId;
	
	private int NrQuestLines;
	private int NrRewardLines;
	
	private int NpcId;
	
	
	public Quest() {
	}
	
	public static String justifyLeft( int width,String st) {
	    StringBuffer buf = new StringBuffer(st);
	    int lastspace = -1;
	    int linestart = 0;
	    int i = 0;

	    while (i < buf.length()) {
	       if ( buf.charAt(i) == ' ' ) lastspace = i;
	       if ( buf.charAt(i) == '\n' ) {
	          lastspace = -1;
	          linestart = i+1;
	       }
	       if (i > linestart + width - 1 ) {
	          if (lastspace != -1) {
	             buf.setCharAt(lastspace,'\n');
	             linestart = lastspace+1;
	             lastspace = -1;
	             }
	          else {
	             buf.insert(i,'\n');
	             linestart = i+1;
	             }
	          }
	        i++;
	       }
	    
	    return buf.toString();
	 }
	
	
	
    /****************************************
     *                                      *
     *             GETTER/SETTER            *
     *                                      *
     *                                      *
     ****************************************/
	
	public int getId() {
		return Id;
	}

	public void setId(int newId){
		Id = newId;
	}


	public void setLevel(int newLevel){
		Level = newLevel;
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

	
	public void setActiveNumber(int newNumber) {
		ActiveNumber = newNumber;
	}
	
	public int getActiveNumber() {
		return ActiveNumber;
	}
	
	
	public String getMessage() {
		return Message;
	}
	
	public void setMessage(String newMessage){
		Message = newMessage;
		
	}
	
	
	public void setType(String newType){
		Type = newType;
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
	
	public int getNpcId(){
		return NpcId;
	}
	
	public void setName(String newName){
		Name = newName;
	}
	
	public String getName() {
		return Name;
	}
	
	public void setDescription(String newDesc){
		Description = justifyLeft(37,newDesc);
	}
	
	public String getDescription(){
		return Description;
	}
		
	
}