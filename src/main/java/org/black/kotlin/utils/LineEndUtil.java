package org.black.kotlin.utils;

/**
 *
 * @author Александр
 */
public class LineEndUtil {
    public static final char CARRIAGE_RETURN_CHAR = '\r';
    public static final String CARRIAGE_RETURN_STRING = Character.toString(CARRIAGE_RETURN_CHAR);
    public static final char NEW_LINE_CHAR = '\n';
    public static final String NEW_LINE_STRING = Character.toString(NEW_LINE_CHAR);

    public static int convertCrToDocumentOffset(String crText, int crOffset) {
        return crOffset - countCrToLineNumber(crText, crOffset);
    }
    
    public static int countCrToLineNumber(String lfText, int offset) {
        int countCR = 0;
        int curOffset = 0;
        
        while (curOffset < offset) {
            if (curOffset == lfText.length()) {
                break;
            }
            
            char c = lfText.charAt(curOffset);
            if (c == '\r') {
                countCR++;
            } 
            
            curOffset++;
        }
        
        return countCR;  
    }

}
