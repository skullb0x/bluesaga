package utils;

public class TextFormater {

	public static void formatStatInfo(StringBuilder sb, String label, int statValue){
		if(statValue != 0){
			if(statValue < 0){
				sb.append("190,60,60;");
			}else{
				sb.append("255,255,255;");
			}
			sb.append(label).append(statValue).append('/');
		}
	}

	
	public static void formatBonusInfo(StringBuilder sb, String label, int statValue){
		if(statValue != 0){
			if(statValue < 0){
				sb.append("190,60,60;")
				  .append(label)
				  .append(statValue).append('/');
			}else{
				sb.append("0,180,0;")
				  .append(label).append('+')
				  .append(statValue).append('/');
			}
		}
	}
	
	public static void formatReqInfo(StringBuilder sb, String label, int statValue, int characterValue){
		if(statValue != 0){
			if(statValue <= characterValue){
				sb.append("0,180,0;");
			}else{
				sb.append("190,60,60;");
			}
			sb.append(label).append(statValue).append('/');
		}
	}

	public static void formatConditionInfo(StringBuilder sb, String label, boolean hasReq){
		if(hasReq){
			sb.append("0,180,0;");
		}else{
			sb.append("190,60,60;");
		}
		sb.append(label).append('/');
	}
	
	public static void formatValueInfo(StringBuilder sb, String label, int value){
		sb.append("255,234,116;").append(label).append(value).append("c/");
	}
	
	public static void formatPriceInfo(StringBuilder sb, String label, int price, boolean canAfford){
		if(canAfford){
			sb.append("255,234,116;");
		}else{
			sb.append("190,60,60;");
		}
		sb.append(label).append(price).append("c/");
	}
	
	public static void formatInfo(StringBuilder sb, String label){
		sb.append("255,255,255;").append(label).append('/');
	}
}
