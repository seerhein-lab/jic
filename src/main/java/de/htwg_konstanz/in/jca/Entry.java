package de.htwg_konstanz.in.jca;

public enum Entry { someByte, someShort, someInt, someLong, someFloat, someDouble, someChar, 
    someBoolean, someReference, thisReference, unknownValue;
        	
    public static Entry getInstance(String signature) {
	if ( signature.equals("I") )  return someInt;
	if ( signature.equals("J") )  return someLong;
	if ( signature.equals("C") )  return someChar;
	if ( signature.equals("B") )  return someByte;
	if ( signature.equals("Z") )  return someBoolean;
	if ( signature.equals("S") )  return someShort;
	if ( signature.equals("F") )  return someFloat;
	if ( signature.equals("D") )  return someDouble;
	return someReference;
    }
        
} 
    
