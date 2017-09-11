/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.spring.beans.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

/**
 * XXX: Need to use the StringUtils from spring.jar instead of
 * duplication
 * 
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public final class StringUtils {

    private StringUtils() {
    }

    public static boolean hasText(String str) {
        if (!hasLength(str)) {
            return false;
        }

        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasLength(String str) {
        return (str != null && str.length() > 0);
    }

    /**
     * Tokenize the given String into a String array via a StringTokenizer.
     * Trims tokens and omits empty tokens.
     * <p>The given delimiters string is supposed to consist of any number of
     * delimiter characters. Each of those characters can be used to separate
     * tokens. A delimiter is always a single character; for multi-character
     * delimiters, consider using <code>delimitedListToStringArray</code>
     * @param str the String to tokenize
     * @param delimiters the delimiter characters, assembled as String
     * (each of those characters is individually considered as delimiter).
     * @return an array of the tokens
     * @see java.util.StringTokenizer
     * @see java.lang.String#trim()
     * @see #delimitedListToStringArray
     */
    public static List<String> tokenize(String str, String delimiters) {
        return tokenize(str, delimiters, true, true);
    }

    /**
     * Tokenize the given String into a String array via a StringTokenizer.
     * <p>The given delimiters string is supposed to consist of any number of
     * delimiter characters. Each of those characters can be used to separate
     * tokens. A delimiter is always a single character; for multi-character
     * delimiters, consider using <code>delimitedListToStringArray</code>
     * @param str the String to tokenize
     * @param delimiters the delimiter characters, assembled as String
     * (each of those characters is individually considered as delimiter)
     * @param trimTokens trim the tokens via String's <code>trim</code>
     * @param ignoreEmptyTokens omit empty tokens from the result array
     * (only applies to tokens that are empty after trimming; StringTokenizer
     * will not consider subsequent delimiters as token in the first place).
     * @return an array of the tokens (<code>null</code> if the input String
     * was <code>null</code>)
     * @see java.util.StringTokenizer
     * @see java.lang.String#trim()
     * @see #delimitedListToStringArray
     */
    private static List<String> tokenize(String str, String delimiters, boolean trimTokens, boolean ignoreEmptyTokens) {
        if (str == null) {
            return Collections.emptyList();
        }
        StringTokenizer st = new StringTokenizer(str, delimiters);
        List<String> tokens = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (trimTokens) {
                token = token.trim();
            }
            if (!ignoreEmptyTokens || token.length() > 0) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    /**
     * Joins a list of strings.
     *
     * @param strings the list of strings; can be empty, but not null.
     * @param separator a separator; can be null.
     * @return a string containing the joined strings; never null.
     */
    public static String join(List<String> strings, String separator) {
        int size = strings.size();
        if (size == 0) {
            return ""; // NOI18N
        }
        StringBuilder sb = new StringBuilder(strings.size() * strings.get(0).length());
        int index = 0;
        int lastIndex = size - 1;
        for (String string : strings) {
            sb.append(string);
            if (separator != null && index < lastIndex) {
                sb.append(separator);
            }
            index++;
        }
        return sb.toString();
    }

    /**
     * Converts the specified string (typically upper camel case) to 
     * lower camel case
     * 
     * @param str input string (typically upper camel case)
     * @return lower camel case representation of the input string
     */
    public static String toLowerCamelCase(String str) {
        char[] cs = str.toCharArray();
        cs[0] = Character.toLowerCase(cs[0]);
        return String.valueOf(cs);
    }

    /**
     * Overloaded Method. Method to search for fist occurrence of a delimiter in input string.
     * @param str - input string
     * @param fromIndex - start index in string to search for delimiter
     * @param delimiters - string of delimiters
     * @return
     */
    public static int indexOfAnyDelimiter(String str, int fromIndex, String delimiters) {
        if(!hasLength(str)) {
            return -1;
        }
        
        return indexOfAnyDelimiter(str, fromIndex, str.length(), delimiters);
    }
    
    /**
     * Method to search for first occurrence of a delimiter in input string between two indexes.
     * @param str - input string
     * @param fromIndex - start index in string to search for delimiter
     * @param toIndex - end index in string to search for delimiter
     * @param delimiters - string of delimiters
     * @return
     */
    public static int indexOfAnyDelimiter(String str, int fromIndex, int toIndex, String delimiters) {
        if(!hasLength(str) || !hasLength(delimiters)) {
            return -1;
        }
        
        if(toIndex <= 0 || fromIndex >= str.length()) {
            return -1;
        }
        
        fromIndex = Math.max(fromIndex, 0);
        toIndex = Math.min(toIndex, str.length());
        
        char[] charArray = str.toCharArray();
        char[] delims = delimiters.toCharArray();
        
        for (int i = fromIndex; i < toIndex; i++) {
            for (char d : delims) {
                if (d == charArray[i]) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    /**
     * Overloaded method. Method to search for last occurrence of a delimiter in input string
     * @param str - input string
     * @param fromIndex - start index in string to search for delimiter
     * @param delimiters - string of delimiters
     * @return
     */
    public static int lastIndexOfAnyDelimiter(String str, int fromIndex, String delimiters) {
        if(!hasLength(str)) {
            return -1;
        }
        return lastIndexOfAnyDelimiter(str, fromIndex, str.length(), delimiters);
    }
    
    /**
     * Method to search for last occurrence of a delimiter in input string between two indexes.
     * @param str - input string
     * @param fromIndex - start index in string to search for delimiter
     * @param toIndex - end index in string to search for delimiter
     * @param delimiters - string of delimiters
     * @return
     */
    public static int lastIndexOfAnyDelimiter(String str, int fromIndex, int toIndex, String delimiters) {
        if(!hasLength(str) || !hasLength(delimiters)) {
            return -1;
        }
        
        if(toIndex < 0 || fromIndex >= str.length()) {
            return -1;
        }
        
        fromIndex = Math.max(fromIndex, 0);
        toIndex = Math.min(toIndex, str.length());

        char[] charArray = str.toCharArray();
        char[] delims = delimiters.toCharArray();
        
        for (int i = toIndex - 1; i >= fromIndex; --i) {
            for (char d : delims) {
                if (d == charArray[i]) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Returns whether there is an occurrence of a specified substring in
     * the specified string after a specified index
     *
     * @param str input string
     * @param subString sub string to search for
     * @param fromIndex starting index (inclusive)
     *
     * @return true if there is an occurrence; false otherwise
     */
    public static boolean occurs(String str, String subString, int fromIndex) {
        return (str.indexOf(subString, fromIndex) == -1) ? false : true;
    }
}

