/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.properties;

/**
 * Contains conversion utilities which allow reading and storing a properties file
 * while preserving formatting and comments that user may have entered.
 * <p>
 * Huge portions of this class marked by <i>// prj40 trunk compatability</i>
 * are there only for being able to run unmodified i18n module code in
 * prj40_prototype branch and trunk. Properties module is not
 * prj40 branched and it is updated by <tt>cvs up -f -rprj40_prototype</tt>.
 * <br>
 * 28th March 2003 Petr Kuzel
 *
 * @author Petr Jiricka
 * @author Petr Kuzel - simplification
 */
public class UtilConvert {

    private UtilConvert() {
    }

    /**
     * These characters play role as key-value sepsrators
     */
    public  static final String keyValueSeparators = "=: \t\r\n\f";

    public  static final String strictKeyValueSeparators = "=:";

    /** Differs from JDK's implementation in that it does not save ' ' as '\ '. */
    private static final String specialSaveChars = "=:\t\r\n\f#!";

    public  static final String whiteSpaceChars = " \t\r\n\f";


    /**
     * Escape key value. Converts string to one with escaped ' ','=',':', and last '\\'
     * in case they are not escaped already. Used for formating user input.
     * //!!! wrong semantics what does it mean if not escaped already?
     */
    public static String escapePropertiesSpecialChars (String source) {
        return source; // prj40 trunk compatability
//        if(source == null) return null;
//        StringBuffer result = new StringBuffer();
//        for(int i=0; i<source.length(); i++) {
//            char x = source.charAt(i);
//            if(x == ' ' || x == '=' || x == ':') {
//                if( i==0 || (i>0 && source.charAt(i-1) != '\\'))
//                    result.append('\\');
//            }
//            // last char == '\\'
//            if(i==source.length()-1 && x == '\\') {
//                if( i>0 && source.charAt(i-1)!='\\')
//                    result.append('\\');
//            }
//            result.append(x);
//        }
//        return result.toString();
    }
    
    /** Checks whether the string contains only spaces */
    private static boolean onlySpaces(String s){
        for (int i = 0; i<s.length(); i++){
            if (s.charAt(i) != ' ') return false;
        }
        return true;
    }
    
    /** Escapes spaces in outer part of string. */
    public static String escapeOutsideSpaces(String source){
        return source; // prj40 trunk compatability
//        if (source == null || source.length() == 0) return source;
//        StringBuffer result = new StringBuffer();
//        int i = 0;
//        while (source.charAt(i) == ' '){
//            result.append('\\');
//            result.append(' ');
//            if ((i+1) == source.length()) return result.toString();
//            i++;
//        }
//        while (!onlySpaces(source.substring(i))){
//            result.append(source.charAt(i));
//            if ((i+1) == source.length()) return result.toString();
//            i++;
//        }
//        while (i < source.length()){
//            result.append('\\');
//            result.append(' ');
//            i++;
//        }
//        return result.toString();
    }

    /**
     * Escape user's value. It escapes last '\\' character  only
     * to prevent user to wrongly create continuation line.
     */
    public static String escapeLineContinuationChar(String source) {
        return source; // prj40 trunk compatability
//        if(source == null) return null;
//        if(source.endsWith("\\")) { //NOI18N
//            if(source.length()>1 && source.charAt(source.length()-2)!='\\')
//                return new String(new StringBuffer(source).append('\\'));
//        }
//        return source;
    }

    /**
     * Converts these java special chars ('\t', '\n', '\b', '\r', '\f') to encoded escapes.
     * Note there are not converted unicode chars.
     */
    public static String escapeJavaSpecialChars(String source) {
        return source;  // prj40 trunk compatability
//        if(source == null) return null;
//        StringBuffer result = new StringBuffer();
//        for (int i=0; i<source.length(); i++) {
//            char ch = source.charAt(i);
//            switch(ch) {
//                case '\t':
//                    result.append("\\t"); //NOI18N
//                    break;
//                case '\n':
//                    result.append("\\n"); //NOI18N
//                    break;
//                case '\b':
//                    result.append("\\b"); //NOI18N
//                    break;
//                case '\r':
//                    result.append("\\r"); //NOI18N
//                    break;
//                case '\f':
//                    result.append("\\f"); //NOI18N
//                    break;
//                default:
//                    result.append(ch);
//            }
//        }
//        return result.toString();
    }


    /**
     * Converts encoded '\\uxxxx' to chars.
     * Note there are not converted '\\"', '\\'', '\\ ', '\\\\' and java special chars escapes.
     */
    public static String unicodesToChars (String theString) {
        return theString; // prj40 trunk compatability
//        if (theString == null) return null;
//        char aChar;
//        char next;
//        int len = theString.length();
//        StringBuffer outBuffer = new StringBuffer(len);
//
//        for(int x=0; x<len; x++) {
//            aChar = theString.charAt(x);
//            if(x+5 < len) { // if there is space for uXXXX chars enough
//                next = theString.charAt(x+1);
//                try {
//                    if (aChar == '\\' && next == 'u') {
//                        // Read the xxxx
//                        int value=0;
//                        for (int i=0; i<4; i++) {
//                            next = theString.charAt(x+1+i+1);
//                            switch (next) {
//                                case '0': case '1': case '2': case '3': case '4':
//                                case '5': case '6': case '7': case '8': case '9':
//                                    value = (value << 4) + next - '0';
//                                    break;
//                                case 'a': case 'b': case 'c':
//                                case 'd': case 'e': case 'f':
//                                    value = (value << 4) + 10 + next - 'a';
//                                    break;
//                                case 'A': case 'B': case 'C':
//                                case 'D': case 'E': case 'F':
//                                    value = (value << 4) + 10 + next - 'A';
//                                    break;
//                                default:
//                                    throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
//                            }
//                        }
//                        outBuffer.append((char)value);
//                        x += 5;
//                    } else
//                        outBuffer.append(aChar);
//                } catch (IllegalArgumentException iae) {
//                    outBuffer.append(aChar); // not unicode -> interpret as a normal char
//                }
//            } else
//                outBuffer.append(aChar);
//        }
//        return outBuffer.toString();
    }

    /**
     * Convert chars to encoded '\\uxxxx' using comment escaping rules.
     * @param commentString
     * @return escaped comment
     */
    public static String escapeComment(String commentString) {
        return charsToUnicodes(commentString, true);
    }

    /**
    * Converts chars to encoded '\\uxxxx'.
     * Note there are not converted '\\"', '\\'', '\\ ', '\\\\' and java special chars escapes.
    */
    public static String charsToUnicodes(String s){
        return charsToUnicodes(s, false);
    }
    
    
    /**
    * Converts chars to encoded '\\uxxxx'. If skipWhiteSpaces is true, then white spaces won't be converted
     * Note there are not converted '\\"', '\\'', '\\ ', '\\\\' and java special chars escapes.
    */
    public static String charsToUnicodes(String theString, boolean skipWhiteSpaces) {
        return theString; // prj40 trunk compatability
//        if(theString == null) return null;
//        char aChar;
//        int len = theString.length();
//        StringBuffer outBuffer = new StringBuffer(len*2);
//
//        for(int x=0; x<len; ) {
//            aChar = theString.charAt(x++);
//            if ((aChar < 20) || (aChar > 127) ) {
//
//                if (skipWhiteSpaces && Character.isWhitespace(aChar)){
//                    // do not convert white spaces
//                    outBuffer.append(aChar);
//                    continue;
//                }
//
//                outBuffer.append('\\');
//                outBuffer.append('u');
//                outBuffer.append(toHex((aChar >> 12) & 0xF));
//                outBuffer.append(toHex((aChar >> 8) & 0xF));
//                outBuffer.append(toHex((aChar >> 4) & 0xF));
//                outBuffer.append(toHex((aChar >> 0) & 0xF));
//            } else {
//                outBuffer.append(aChar);
//            }
//        }
//        return outBuffer.toString();
    }


    /**
     * Converts encoded \\uxxxx to unicode chars
     * and changes special saved chars to their original forms
     */
    public static String loadConvert (String theString) {
        char aChar;
        final int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len);

        main:
        for(int x=0; x<len; ) {
            aChar = theString.charAt(x++);
            if (aChar == '\\' && x != len) {
                aChar = theString.charAt(x++);
                if(aChar == 'u') {
                    if (x > len - 4) {
                        outBuffer.append('\\').append('u');
                        continue main;
                    }
                    // Read the xxxx
                    int value=0;
                    for (int i=0; i<4; i++) {
                        aChar = theString.charAt(x++);
                        switch (aChar) {
                        case '0': case '1': case '2': case '3': case '4':
                        case '5': case '6': case '7': case '8': case '9':
                            value = (value << 4) + aChar - '0';
                            break;
                        case 'a': case 'b': case 'c':
                        case 'd': case 'e': case 'f':
                            value = (value << 4) + 10 + aChar - 'a';
                            break;
                        case 'A': case 'B': case 'C':
                        case 'D': case 'E': case 'F':
                            value = (value << 4) + 10 + aChar - 'A';
                            break;
                        default:
                            /*
                             * Handle a malformed \\uxxxx encoding:
                             *
                             * We want to print "\\u" plus all the hexadecimal
                             * digits that passed the above switch.
                             * To achieve it, print ("\\u")...,
                             */
                            outBuffer.append('\\').append('u');

                            /* ... move 'x' back to character after "u"... */
                            x -= i + 1;

                            /* ... and continue with the main loop. */
                            continue main;
                        }
                    }
                    outBuffer.append((char)value);
                } else {
                    if (aChar == 't') aChar = '\t';
                    else if (aChar == 'r') aChar = '\r';
                    else if (aChar == 'n') aChar = '\n';
                    else if (aChar == 'f') aChar = '\f';
                    outBuffer.append(aChar);
                }
            } else
                outBuffer.append(aChar);
        }
        return outBuffer.toString();
    }

    /**
     * Converts unicodes to encoded \\uxxxx
     * and writes out any of the characters in specialSaveChars
     * with a preceding slash.
     * Differs from Sun's implementation in that it does not save ' ' as '\ '.
     */
    public static String saveConvert(String theString) {
        char aChar;
        int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len*2);

        for(int x=0; x<len; ) {
            aChar = theString.charAt(x++);
            switch(aChar) {
            case '\\':outBuffer.append('\\'); outBuffer.append('\\');
                continue;
            case '\t':outBuffer.append('\\'); outBuffer.append('t');
                continue;
            case '\n':outBuffer.append('\\'); outBuffer.append('n');
                continue;
            case '\r':outBuffer.append('\\'); outBuffer.append('r');
                continue;
            case '\f':outBuffer.append('\\'); outBuffer.append('f');
                continue;
            default:
                if ((aChar < 20) || (aChar > 127)) {
                    outBuffer.append('\\');
                    outBuffer.append('u');
                    outBuffer.append(toHex((aChar >> 12) & 0xF));
                    outBuffer.append(toHex((aChar >> 8) & 0xF));
                    outBuffer.append(toHex((aChar >> 4) & 0xF));
                    outBuffer.append(toHex((aChar >> 0) & 0xF));
                } else {
                    if (specialSaveChars.indexOf(aChar) != -1)
                        outBuffer.append('\\');
                    outBuffer.append(aChar);
                }
            }
        }
        return outBuffer.toString();
    }

    /**
     * Convert a nibble to a hex character
     * @param	nibble	the nibble to convert.
     */
    private static char toHex(int nibble) {
        return (char)hexDigit[(nibble & 0xF)];
    }

    /** A table of hex digits */
    static final byte[] hexDigit = {
        '0','1','2','3','4','5','6','7','8','9',
        'a','b','c','d','e','f'}; // #168629
}
