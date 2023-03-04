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

package org.netbeans.modules.xml.wsdl.validator.visitor;

/**
 * Copied from castor validation utils.
 *
 *
 */
public class ValidationUtils {
    
    
    //----------------/
    //- Constructors -/
    //----------------/
    
    private ValidationUtils() {
        super();
    }
    
    //------------------/
    //- Public Methods -/
    //------------------/
    
    
    /**
     * Checks the given character to determine if it is a valid
     * CombiningChar as defined by the W3C XML 1.0 Recommendation
     * @return true if the given character is a CombiningChar
     **/
    public static boolean isCombiningChar(char ch) {
        
        //-- NOTE: THIS METHOD IS NOT COMPLETE
        
        return false;
        
    } //-- isCombiningChar
    
    /**
     * @param ch the character to check
     * @return true if the given character is a digit
     **/
    public static boolean isDigit(char ch) {
        return Character.isDigit(ch);
    } //-- isDigit
    
    
    /**
     * @param ch the character to check
     * @return true if the given character is a letter
     **/
    public static boolean isLetter(char ch) {
        return Character.isLetter(ch);
    } //-- isLetter
    
    /**
     * Checks the characters of the given String to determine if they
     * syntactically match the production of an NCName as defined
     * by the W3C XML Namespaces recommendation
     * @param str the String to check
     * @return true if the given String follows the Syntax of an NCName
     **/
    public static boolean isNCName(String str) {
        
        if ((str == null) || (str.length() == 0)) return false;
        
        
        char[] chars = str.toCharArray();
        
        char ch = chars[0];
        
        //-- make sure String starts with a letter or '_'
        if ((!isLetter(ch)) && (ch != '_'))
            return false;
        
        for (int i = 1; i < chars.length; i++) {
            if (!isNCNameChar(chars[i])) return false;
        }
        return true;
    } //-- isNCName
    
    /**
     * Checks the the given character to determine if it is
     * a valid NCNameChar as defined by the W3C XML
     * Namespaces recommendation
     * @param ch the char to check
     * @return true if the given char is an NCNameChar
     **/
    public static boolean isNCNameChar(char ch) {
        if (isLetter(ch) || isDigit(ch)) return true;
        if (isExtender(ch) || isCombiningChar(ch)) return true;
        switch(ch) {
        case '.':
        case '-':
        case '_':
            return true;
        default:
            return false;
        }
    } //-- isNCNameChar
    
    
    /**
     * Checks the characters of the given String to determine if they
     * syntactically match the production of an NMToken
     * @param str the String to check
     * @return true if the given String follows the Syntax of an NMToken
     **/
    public static boolean isNMToken(String str) {
        
        if (str == null) return false;
        char[] chars = str.toCharArray();
        
        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];
            if (isLetter(ch) || isDigit(ch)) continue;
            if (isExtender(ch) || isCombiningChar(ch)) continue;
            switch(ch) {
            case '.':
            case '-':
            case '_':
            case ':':
                break;
            default:
                return false;
            }
        }
        return true;
    } //-- isNMToken
    
    /**
     * Checks the characters of the given String to determine if they
     * syntactically match the production of a CDATA
     * @param str the String to check
     * @return true if the given String follows the Syntax of an NMToken
     **/
    public static boolean isCDATA(String str) {
        
        if (str == null) return false;
        char[] chars = str.toCharArray();
        
        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];
            switch(ch) {
            case '\r':
            case '\n':
            case '\t':
                return false;
            default:
                continue;
            }
        }
        return true;
    } //-- isCDATA
    
    /**
     * Returns true if the given character is a valid XML Extender
     * character, according to the XML 1.0 specification
     * @param ch the character to check
     * @return true if the character is a valid XML Extender character
     **/
    public static boolean isExtender(char ch) {
        
        if ((ch >= 0x3031) && (ch <= 0x3035)) return true;
        if ((ch >= 0x30FC) && (ch <= 0x30FE)) return true;
        
        switch(ch) {
        case 0x00B7:
        case 0x02D0:
        case 0x02D1:
        case 0x0387:
        case 0x0640:
        case 0x0E46:
        case 0x0EC6:
        case 0x3005:
        case 0x309D:
        case 0x309E:
            return true;
        default:
            break;
        }
        return false;
    } //-- isExtender
    
    /**
     * Checks the characters of the given String to determine if they
     * syntactically match the production of an QName as defined
     * by the W3C XML Namespaces recommendation
     * @param str the String to check
     * @return true if the given String follows the Syntax of an QName
     **/
    public static boolean isQName(String str) {
        
        if ((str == null) || (str.length() == 0)) return false;
        
        
        char[] chars = str.toCharArray();
        
        char ch = chars[0];
        
        //-- make sure String starts with a letter or '_'
        if ((!isLetter(ch)) && (ch != '_'))
            return false;
        
        for (int i = 1; i < chars.length; i++) {
            if (chars[i] == ':') continue;
            if (!isNCNameChar(chars[i])) return false;
        }
        return true;
    } //-- isQName
    
    /**
     * Test
     **
     public static void main(String[] args) {
     System.out.println("0x00B7: " + (char)0x00B7);
     }
     /* */
    /** Test if two strings are equal in XML.
     * @param   s1  First string.
     * @param   s2  Second string.
     * @return  <code>true</code> if strings are equal.
     */
    public static boolean areEqualXMLValues(String s1, String s2) {

        return ((s1 == null && s2 == null)
            || (s1 == null && isEmpty(s2))
            || (isEmpty(s1) && s2 == null)
            || (s1 != null && s1.equals(s2)));
    }
    
    /** Test if a string is empty.
     * @param   s   String to test
     * @return  <code>true</code> if string is empty
     */
    public static boolean isEmpty(String s) {
        return ((null == s) || (s.trim().length() == 0));
    }
}

