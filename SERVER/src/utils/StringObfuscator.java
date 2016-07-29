package utils;

public class StringObfuscator {
	// Note: You can add 1 more char to this if you want to
	static String chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ.";

	
	public StringObfuscator(){
		
	}
	
	
	public static String decodeToken(String encoded) {
	    // Lay out the bits 5 at a time
	    StringBuilder sb = new StringBuilder();
	    for (byte b : encoded.toLowerCase().getBytes())
	        sb.append(asBits(chars.indexOf(b), 5));

	    sb.setLength(sb.length() - (sb.length() % 6));

	    // Consume it 6 bits at a time
	    int length = sb.length();
	    StringBuilder result = new StringBuilder();
	    for (int i = 0; i < length; i += 6)
	        result.append(chars.charAt(Integer.parseInt(sb.substring(i, i + 6), 2)));

	    return result.toString();
	}

	public static String generateToken(String x) {
	    StringBuilder sb = new StringBuilder();
	    for (byte b : x.getBytes())
	        sb.append(asBits(chars.indexOf(b), 6));

	    // Round up to 5 bit multiple
	    // Consume it 5 bits at a time
	    int length = sb.length();
	    sb.append("00000".substring(0, length % 5));
	    StringBuilder result = new StringBuilder();
	    for (int i = 0; i < length; i += 5)
	        result.append(chars.charAt(Integer.parseInt(sb.substring(i, i + 5), 2)));

	    return result.toString();
	}

	private static String asBits(int index, int width) {
	    String bits = "000000" + Integer.toBinaryString(index);
	    return bits.substring(bits.length() - width);
	}

}
