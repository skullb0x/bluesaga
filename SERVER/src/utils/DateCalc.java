package utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateCalc {
	
    public DateCalc(){
		
    }
    
    public static int daysBetween(String start, String end){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	
        int diff = -9999;
        
        try {
        	
        	Date startDate = sdf.parse(start);
        	Date endDate = sdf.parse(end);
			
			diff = (int)( (endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    return diff;
    }
    
    
    public int secondsBetween(String start, String end){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	
        int diff = -9999;
        
        try {
        	
        	Date startDate = sdf.parse(start);
        	Date endDate = sdf.parse(end);
			
			diff = (int)( (endDate.getTime() - startDate.getTime()) / (1000));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    return diff;
    }
    
    
}
