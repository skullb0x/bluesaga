package utils;

public class TextFormater {

	
	
	public static String formatStatInfo(String label, int statValue){
		String formatedInfo = "";
		if(statValue != 0){
			if(statValue < 0){
				formatedInfo += "190,60,60;";
			}else{
				formatedInfo += "255,255,255;";
			}
			
			formatedInfo += label+statValue+"/";
		}
		return formatedInfo;
	}

	
	public static String formatBonusInfo(String label, int statValue){
		String formatedInfo = "";
		if(statValue != 0){
			if(statValue < 0){
				formatedInfo += "190,60,60;";
				formatedInfo += label+""+statValue+"/";
			}else{
				formatedInfo += "0,180,0;";
				formatedInfo += label+"+"+statValue+"/";
			}
		}
		return formatedInfo;
	}
	
	public static String formatReqInfo(String label, int statValue, int characterValue){
		String formatedInfo = "";
		if(statValue != 0){
			if(statValue <= characterValue){
				formatedInfo += "0,180,0;";
			}else{
				formatedInfo += "190,60,60;";
			}
			formatedInfo += label+statValue+"/";
		}
		return formatedInfo;
	}

	public static String formatConditionInfo(String label, boolean hasReq){
		String formatedInfo = "";
		if(!hasReq){
			formatedInfo += "190,60,60;";
		}else{
			formatedInfo += "0,180,0;";
		}
		formatedInfo += label+"/";
		return formatedInfo;
	}
	
	public static String formatValueInfo(String label, int value){
		String formatedInfo = "";
		formatedInfo += "255,234,116;";
		formatedInfo += label+value+"c/";
		return formatedInfo;
	}
	
	public static String formatPriceInfo(String label, int price, boolean canAfford){
		String formatedInfo = "";
			if(canAfford){
				formatedInfo += "255,234,116;";
			}else{
				formatedInfo += "190,60,60;";
			}
			formatedInfo += label+price+"c/";
		return formatedInfo;
	}
	
	public static String formatInfo(String label){
		String formatedInfo = "";
		formatedInfo += "255,255,255;";
		formatedInfo += label+"/";
		
		return formatedInfo;
	}
}
