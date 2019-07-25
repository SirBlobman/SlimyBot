package com.SirBlobman.discord.utility;

/**
 * Utils for casting number types to other number types
 */
public final class NumberConversions {
	private NumberConversions() {}
	
	public static int floor(double number) {
	    int floor = (int) number;
	    return (floor == number ? floor : (floor - (int) (Double.doubleToRawLongBits(number) >>> 63)));
	}
	
	public static int ceil(double number) {
	    int ceil = (int) number;
	    return (ceil == number ? ceil : (ceil + (int) (~Double.doubleToRawLongBits(number) >>> 63)));
	}
	
	public static int round(double number) {
	    double plus = (number + 0.5D);
	    return floor(plus);
	}
	
	public static double square(double number) {
	    return (number * number);
	}
	
	public static int toInt(Object object) {
	    if(object == null) return 0;
	    if(object instanceof Number) {
	        Number number = (Number) object;
	        return number.intValue();
	    }
	    
	    try {
	        String string = object.toString();
	        return Integer.valueOf(string);
	    } catch(NumberFormatException | NullPointerException ex) {
	        return 0;
	    }
	}
	
	public static float toFloat(Object object) {
        if(object == null) return 0.0F;
        if(object instanceof Number) {
            Number number = (Number) object;
            return number.floatValue();
        }
        
        try {
            String string = object.toString();
            return Float.valueOf(string);
        } catch(NumberFormatException | NullPointerException ex) {
            return 0.0F;
        }
	}
	
	public static double toDouble(Object object) {
        if(object == null) return 0.0D;
        if(object instanceof Number) {
            Number number = (Number) object;
            return number.doubleValue();
        }
        
        try {
            String string = object.toString();
            return Double.valueOf(string);
        } catch(NumberFormatException | NullPointerException ex) {
            return 0.0D;
        }
	}

	public static long toLong(Object object) {
        if(object == null) return 0L;
        if(object instanceof Number) {
            Number number = (Number) object;
            return number.longValue();
        }
        
        try {
            String string = object.toString();
            return Long.valueOf(string);
        } catch(NumberFormatException | NullPointerException ex) {
            return 0L;
        }
	}

	public static short toShort( Object object ) {
        if(object == null) return 0;
        if(object instanceof Number) {
            Number number = (Number) object;
            return number.shortValue();
        }
        
        try {
            String string = object.toString();
            return Short.valueOf(string);
        } catch(NumberFormatException | NullPointerException ex) {
            return 0;
        }
	}

	public static byte toByte( Object object ) {
        if(object == null) return 0;
        if(object instanceof Number) {
            Number number = (Number) object;
            return number.byteValue();
        }
        
        try {
            String string = object.toString();
            return Byte.valueOf(string);
        } catch(NumberFormatException | NullPointerException ex) {
            return 0;
        }
	}
	
	public static boolean isFinite(double number) {
	    double abs = Math.abs(number);
	    return (abs <= Double.MAX_VALUE);
	}
	
	public static boolean isFinite(float number) {
	    float abs = Math.abs(number);
	    return (abs <= Float.MAX_VALUE);
	}
    
    public static void checkFinite(double number, String message) {
        if(!isFinite(number)) throw new IllegalArgumentException(message);
    }
    
    public static void checkFinite(float number, String message) {
        if(!isFinite(number)) throw new IllegalArgumentException(message);
    }
}