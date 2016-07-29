package utils;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CrashLogger {


	public static void uncaughtException(Throwable e) {
		System.out.println("Server crashed! ");

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");

		String filename = "crashlogs/"+sdf.format(cal.getTime())+".txt";
		
		PrintStream writer;
		try {
			writer = new PrintStream(filename, "UTF-8");
			writer.println(e.getClass() + ": " + e.getMessage());
			for (int i = 0; i < e.getStackTrace().length; i++) {
				writer.println(e.getStackTrace()[i].toString());
			}
			System.out.println("Error written to file: "+filename);
		} catch (FileNotFoundException | UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("Error writing exception to file!");
		}
	}
}
