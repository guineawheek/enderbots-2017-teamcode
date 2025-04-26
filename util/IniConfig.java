package org.firstinspires.ftc.teamcode.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

public class IniConfig {

	/**
	 * BUGS:
	 * 
	 * Parsing invalid octal strings (leading zeros) like 08 as an integer will crash the code 
	 * Comments need seperate lines to work
	 * putHexInteger has mild overflow issues
	 * 
	 */
	// static methods
	/**
	 * returns -1 if negative, 0 if invalid, and 1 if positive
	 * @param str
	 * @return
	 */
	public static int validNegative(String str) {
		// we americans here
		char localeMinusSign = '-';
		boolean negative = false;

		if (!Character.isDigit(str.charAt(0)) && !(negative = (str.charAt(0) == localeMinusSign)))
			return 0;
		
		return (negative) ? -1 : 1;
	}

	/**
	 * Returns if a hex value
	 * @param str
	 * @param negative has leading -
     * @return
     */
	public static boolean isHex(String str, boolean negative) {
		int negativeOffset = 0;
		if (negative) negativeOffset = 1;
		// too short to be hex
		if (str.length() <= 2 + negativeOffset)
			return false;
		
		if (!str.substring(negativeOffset, negativeOffset + 2).equalsIgnoreCase("0x"))
			return false;
		
		for (char c: str.substring(negativeOffset + 2).toCharArray()) {
			if (!Character.isDigit(c)) {
				switch (Character.toLowerCase(c)) {
				case 'a':
				case 'b':
				case 'c':
				case 'd':
				case 'e':
				case 'f':
					break;
				default:
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Returns if valid integer
	 * @param str
	 * @param negative
     * @return
     */
	public static boolean isInteger(String str, boolean negative) {
		int negativeOffset = 0;
		if (negative) negativeOffset = 1;
		for (char c: str.substring(negativeOffset).toCharArray()) {
			if (!Character.isDigit(c))
				return false;
		}
		return true;
	}

	/**
	 * Returns what type of number the string is, if it's a number
	 * @param str
	 * @return
     */
	public static NumberType isNumeric(String str) {
		int negativeStatus = validNegative(str);
		if (negativeStatus == 0) 
			return NumberType.INVALID;
		boolean negative = negativeStatus == -1;
		
		if (isHex(str, negative))
			return NumberType.HEXADECIMAL;
		
		boolean isDecimalSeparatorFound = false;
		char decimalSeparator = '.';

		for (char c : str.substring(1).toCharArray()) {
			if (!Character.isDigit(c)) {
				if (c == decimalSeparator && !isDecimalSeparatorFound) {
					isDecimalSeparatorFound = true;
					continue;
				}
				return NumberType.INVALID;
			}
		}
		return (isDecimalSeparatorFound) ? NumberType.DECIMAL : NumberType.INTEGER;
	}
	
	// used in returning
	public static final String NEWLINE = "\r\n";
	// helper classes and enums
	public enum NumberType {
			INVALID,
			INTEGER,
			HEXADECIMAL,
			DECIMAL,
	}

	public static class ConfigReadException extends RuntimeException {
		// shut up about warnings
		private static final long serialVersionUID = -163869913406984454L;

		public ConfigReadException(String message) {
			super(message);
		}
	}
	
	public static class ConfigSection {
		private String name;
		private LinkedHashMap<String, String> map;
		private boolean isPermissive;
		public ConfigSection(String name) {
			this.name = name;
			this.map = new LinkedHashMap<>();
			this.isPermissive = true;
		}

		
		private void error(String message) {
			// Log.e("iniParser", message);
			System.err.println("iniParser: " + message);
			if (!this.isPermissive)
				throw new ConfigReadException(message);
		}
		private boolean containsKey(String key) {
			if (!map.containsKey(key)) {
				error(String.format("key %s not in section [%s]", key, name));
				return false;
			}
			return true;
		}

		// set if to throw exceptions on errors
		public void setPermissive(boolean permissive) {
			this.isPermissive = permissive;
		}
		
		public boolean getPermissive() {
			return this.isPermissive;
		}
		
		public String getName() {
			return name;
		}
		
		public Map<String, String> getMap() {
			return map;
		}

		/**
		 * Gets the raw string value corresponding to a key in a section
 		 * @param key the key to try to read a string value from
		 * @param fallback bacup value if key doesn't exist
         * @return the string value associated with the key, or the fallback if the key doesn't exist
         */
		public String get(String key, String fallback) {
			if (!containsKey(key))
				return fallback;
			return map.get(key);
		}

        /**
         * Gets the double value corresponding the a key in a section
         * @param key the key to try and read a double value from
         * @param fallback a fallback value to return if a double value cannot be read
         * @return the double value associated with the key, or the fallback if the key can't be read
         */
		public double getNumber(String key, double fallback) {
			if (!containsKey(key))
				return fallback;
			
			String str = map.get(key);
			NumberType numberType = isNumeric(str);
			if (numberType == NumberType.INVALID) {
				error(String.format("[%s]: value %s for key %s is not a valid number", name, str, key));
				return fallback;
			}
			if (numberType == NumberType.HEXADECIMAL)
				return (double) Integer.decode(str);
				
			return Double.parseDouble(str);
		}

		/**
         * Shorthand for {@link #getNumber(String)}
         * @param key the key to try and read a double value from
         * @return the double value associated with the key, or the fallback if the key can't be read
         */
		public double getd(String key) {
			return getNumber(key);
		}

        /**
         * Shorthand for {@link #getNumber(String, double)}
         * @param key the key to try and read a double value from
         * @param fallback a fallback value to return if a double value cannot be read
         * @return the double value associated with the key, or the fallback if the key can't be read
         */
		public double getd(String key, double fallback) {
			return getNumber(key, fallback);
		}

        /**
         * Shorthand for {@link #getInteger(String)}
         * @param key the key to try and read an integer value from
         * @return the integer value associated with the key, or 0 if the key can't be read
         */
		public int geti(String key) {
            return getInteger(key);
        }

        /**
         * Shorthand for {@link #getInteger(String, int)}
         * @param key the key to try and read a double value from
         * @param fallback a fallback value to return if a double value cannot be read
         * @return the integer value associated with the key, or the fallback if the key can't be read
         */
        public int geti(String key, int fallback) {
            return getInteger(key, fallback);
        }

        /**
         * Gets the integer value corresponding the a key in a section
         * @param key the key to try and read an integer value from
         * @param fallback a fallback value to return if an integer value cannot be read
         * @return the double value associated with the key, or the fallback if the key can't be read
         */
		public int getInteger(String key, int fallback) {
			if (!containsKey(key))
				return fallback;
			
			String str = map.get(key);
			NumberType numberType = isNumeric(str);
			if (numberType == NumberType.INVALID) {
				error(String.format("[%s]: value %s for key %s is not a valid integer", name, str, key));
				return fallback;
			} else if (numberType == NumberType.DECIMAL) {
				error(String.format("[%s]: value %s for key %s is a decimal and must be coerced", name, str, key));
				return (int) Double.parseDouble(str);
			} else {
				return Integer.decode(str);
			}
				
		}

        /**
         * Gets the raw string value corresponding to a key in a section, or an empty string if none can be found
         * @param key the key to try to read a string value from
         * @return the string value associated with the key, or an empty string if the key doesn't exist
         */
		public String get(String key) {
			return get(key, "");
		}

        /**
         * Shorthand for {@link #getNumber(String)}
         * @param key the key to try and read a double value from
         * @return the double value associated with the key, or the fallback if the key can't be read
         */
		public double getNumber(String key) {
			return getNumber(key, 0);
		}

        /**
         * Shorthand for {@link #getInteger(String)}
         * @param key the key to try and read an integer value from
         * @return the integer value associated with the key, or 0 if the key can't be read
         */
		public int getInteger(String key) {
			return getInteger(key, 0);
		}
		
		public void put(String key, String value) {
			map.put(key, value);
		}
		
		public void putNumber(String key, double number) {
			map.put(key, Double.toString(number));
		}
		
		public void putInteger(String key, int number) {
			map.put(key,Integer.toString(number));
		}
		
		public void putHexInteger(String key, int number) {
			String sign = (number < 0) ? "-" : "";
			map.put(key, sign + "0x" + Integer.toHexString(Math.abs(number)));
		}
		
		public void putComment(String comment) {
			map.put("[comment - " + UUID.randomUUID().toString() + "]", "# " + comment);
		}
		
		@Override
		public String toString() {
			StringBuilder ret = new StringBuilder();
			if (!name.equals("default")) {
				ret.append(String.format("%s[%s]%s", NEWLINE, name, NEWLINE + NEWLINE));
			}
			for (Map.Entry<String, String> entry : map.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				if (key.isEmpty()) {
					error("empty key detected - should not happen!");
				}
				
				if (key.charAt(0) == '[') {
					// looks like a comment - append it wholesale
					ret.append(value + NEWLINE);
				} else {
					ret.append(String.format("%s = %s%s", key, value, NEWLINE));
				}
			}
			return ret.toString();
		}

	}

	// class definition
	File iniFile;
	boolean isPermissive;
	LinkedHashMap<String, ConfigSection> sections;
	String lastError;
	public IniConfig(File iniFile) {
		this.iniFile = iniFile;
		this.isPermissive = true;
		this.lastError = "";
		sections = new LinkedHashMap<>();
	}
	
	public void setPermissive(boolean permissive) {
		this.isPermissive = permissive;
		for (ConfigSection section : sections.values()) {
			section.setPermissive(permissive);
		}
	}
	
	public HashMap<String, ConfigSection> getMap() {
		return sections;
	}
	
	public boolean getPermissive() {
		return isPermissive;
	}
	
	private void error(String message) {
		Log.e("iniParser", message);
		lastError = message;
		//System.err.println("iniParser: " + message);
		if (!isPermissive)
			throw new ConfigReadException(message);
	}
	
	public boolean readConfig() {
		// clear old values, but store old sections for atomicity
		LinkedHashMap<String, ConfigSection> oldSections = sections;
		sections = new LinkedHashMap<>();
		ConfigSection currentSection = new ConfigSection("default");
		sections.put("default", currentSection);
		try (BufferedReader br = new BufferedReader(new FileReader(iniFile))) {
			int lineCount = 0;
			String rawLine;
		    String line;
		    while ((rawLine = br.readLine()) != null) {
		    	line = rawLine.trim();
		    	lineCount++;
		    	if (line.length() < 1)
		    		continue;
		    	
		    	
		    	if (line.charAt(0) == '[') {
		    		// new section
		    		// as malformed sections can cause really bad corruption, we will abort a bad config read
		    		int endIndex = line.indexOf(']');
		    		if (endIndex == -1) {
                        if (!isPermissive)
                            sections = oldSections;
		    			error(String.format("%s: expected ']' for section header at line %d", iniFile.getName(), lineCount));
						return false;
		    		}
		    		String name = line.substring(1, endIndex);
		    		currentSection = new ConfigSection(name);
		    		// new sections with same name overwrite old sections
		    		sections.put(name, currentSection);
		    	} else if (line.charAt(0) == '#') {
		    		// looks like a comment
		    		// we use [ because it will never be used for a key
		    		currentSection.put(String.format("[comment line %d]", lineCount), line);
		    	} else {
		    		// just looks like regular old values
		    		int delimiterIndex = line.indexOf('=');
		    		if (delimiterIndex == -1) {
		    			if (!isPermissive) 
		    				sections = oldSections;
		    			error(String.format("%s: expected '=' at line %d", iniFile.getName(), lineCount));
                        continue; // skip the read
		    		}
		    		String key = line.substring(0, delimiterIndex).trim();
		    		String value = line.substring(delimiterIndex + 1).trim();
		    		currentSection.put(key, value);
		    	}
		    	
		    }
		} catch (IOException e) {
			error("caught IOException on read: " + e.getMessage());
			return false;
		}
		return true;
	}

    /**
     * Attempts to write out the current config values to the set file path.
     * Useful for configuration opmodes.
     * @return true if successful, false if error
     */
	public boolean writeConfig() {
		try(PrintWriter out = new PrintWriter(iniFile)) {
			out.write(toString());
		} catch (IOException e) {
			error("caught IOException on write: " + e.getMessage());
			return false;
		}
		return true;
	}
	
	public ConfigSection getSection(String name) {
		ConfigSection ret = sections.get(name);
		if (ret == null) {
			error(String.format("%s: no section named %s exists", iniFile.getName(), name));
			// if permissive just return a blank config
			ret = new ConfigSection("name");
		}
		return ret;
	}

    public String getLastError() {
        return lastError;
    }
	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		for (ConfigSection section : sections.values()) {
			ret.append(section.toString());
		}
		return ret.toString();
	}

}
