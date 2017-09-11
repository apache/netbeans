/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.commons;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author Marian Petras
 */
public class TextUtils {

    private TextUtils() {}

    /**
     * Shortens the given text to the given number of characters and adds
     * an ellipsis if appropriate. If the given string is too long to fit the
     * limit, it is shortened and an ellipsis is appended to signal that it
     * was shortened. Length of the resulting {@code String} thus can by longer
     * than the given limit because of the ellipsis and possible an extra space
     * before the ellipsis.
     *
     * @param  text  text to be shortened to fit into the given number
     *               of characters
     * @param  minWords  try to output at least the given number of words,
     *                   even if the last word should be truncated
     * @param  limit  maximum number of characters of the trimmed message
     *
     * @return
     */
    public static String shortenText(String text,
                                     final int minWords,
                                     final int limit) {
        if (text == null) {
            throw new IllegalArgumentException("text must be non-null");//NOI18N
        }
        if (minWords < 1) {
            throw new IllegalArgumentException(
                    "minimum number of words must be positive");        //NOI18N
        }
        if (limit < 1) {
            throw new IllegalArgumentException(
                    "limit must be positive - was: " + limit);          //NOI18N
        }

        text = trimSpecial(text);

        int length = text.length();
        if (length <= limit) {
            return text;
        }

        int wordCount = 0;
        int lastWordEndIndex = -1;
        boolean lastWasSpace = false;
        for (int i = 1; i < limit; i++) {
            boolean isSpace = isSpace(text.charAt(i));
            if (isSpace && !lastWasSpace) {
                lastWordEndIndex = i;
                wordCount++;
            }
            lastWasSpace = isSpace;
        }

        int endIndex;
        boolean wholeWords;

        if (wordCount >= minWords) {
            endIndex = lastWordEndIndex;
            wholeWords = true;
        } else if (lastWasSpace) {
            /* the for-cycle ended in a space between the first two words */
            endIndex = lastWordEndIndex;
            wholeWords = true;
        } else {
            endIndex = limit;
            if (isSpace(text.charAt(limit))) {
                /* the for-cycle ended just after the second word */
                wholeWords = true;
            } else {
                /* the for-cycle ended in the middle of the second word */
                wholeWords = false;
            }
        }

        StringBuilder buf = new StringBuilder(endIndex + 4);
        buf.append(text.substring(0, endIndex));
        if (wholeWords) {
            buf.append(' ');
        }
        buf.append("...");                                              //NOI18N
        return buf.toString();
    }

    /**
     * Trims the given text by removing all leading and trailing <em>space
     * characters</em>. Unlike the known method {@link java.lang.String#trim},
     * this method removes also tabs and all characters for which method
     * {@link java.lang.Character#isSpaceChar(char)} returns {@code true}.
     * @param  str  string to be trimmed
     * @return  the trimmed string
     *          (may be the original string if no trimming was needed)
     */
    public static String trimSpecial(String str) {
        if (str == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }

        int length = str.length();

        int beginIndex, endIndex;

        int index;

        index = 0;
        while ((index < length) && isSpace(str.charAt(index))) {
            index++;
        }

        if (index == length) {
            /* there were just space characters in the string */
            return "";                                                  //NOI18N
        }

        beginIndex = index;

        index = length - 1;
        while (isSpace(str.charAt(index))) {
            index--;
        }

        endIndex = index + 1;

        return str.substring(beginIndex, endIndex);
    }

    private static boolean isSpace(char ch) {
        return (ch == '\t') || Character.isSpaceChar(ch);
    }

    /**
     * Replaces problematic characters by escape sequences.
     * This method is designed for text that should appear
     * in HTML label.
     *
     * @param text text to process.
     * @return text with correct escape sequences.
     */
    public static String escapeForHTMLLabel(String text) {
        if(text == null) {
            return "";                              // NOI18N
        }
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '<': sb.append("&lt;"); break; // NOI18N
                case '>': sb.append("&gt;"); break; // NOI18N
                default: sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Encodes URL by encoding to %XX escape sequences.
     *
     * @param url url to decode
     * @return decoded url
     */
    public static String encodeURL(String url) {
        if (url == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer(url.length());

        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            if (!isAlowedChar(c)) {
                sb.append('%');                                                 // NOI18N
                sb.append(Integer.toHexString(c).toUpperCase());
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Decodes URL by decoding from %XX escape sequences.
     *
     * @param encoded url to decode
     * @return decoded url
     */
    public static String decodeURL(String encoded) {
        if (encoded == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(encoded.length());

        for (int i = 0; i < encoded.length(); i++) {
            char c = encoded.charAt(i);
            if (c == '%') {
                String code = encoded.substring(i + 1, i + 3);
                char decode = (char) Integer.parseInt(code, 16);
                sb.append(decode);
                i += 2;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static boolean isAlowedChar(char c) {
        return c >= '0' && c <= '9' ||                                          // NOI18N
               c >= 'A' && c <= 'Z' ||                                          // NOI18N
               c >= 'a' && c <= 'z' ||                                          // NOI18N
               c == '.' ||                                                      // NOI18N
               c == '_';                                                        // NOI18N
    }
    
    public static String getMD5(String name) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");                          // NOI18N
        } catch (NoSuchAlgorithmException e) {
            // should not happen
            return null;
        }
        digest.update(name.getBytes());
        byte[] hash = digest.digest();
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(hash[i] & 0x000000FF);
            if(hex.length()==1) {
                hex = "0" + hex;                                                // NOI18N
            }
            ret.append(hex);
        }
        return ret.toString();
    }    
}
