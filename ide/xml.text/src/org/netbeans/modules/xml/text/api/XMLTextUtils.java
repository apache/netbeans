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
package org.netbeans.modules.xml.text.api;

import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.modules.xml.text.indent.XMLLexerFormatter;

/**
 * Miscellaneous utilities for working with XML in text form.  This class
 * replaces the old Util class which contains references to old lexer and was
 * removed to the {@code xml.text.obsolete90} module.
 * 
 * @author sdedic
 * @since 1.60
 */
public final class XMLTextUtils {
    private XMLTextUtils() {}
    
    /**
     * MIME type for the text/xml documents.
     */
    public static final String XML_MIME = "text/xml";

    static String[] knownEntityStrings = {"&lt;", "&gt;", "&apos;", "&quot;", "&amp;"};

    static char[] knownEntityChars = {'<', '>', '\'', '"', '&'};

    /**
     * Handle fuzziness of attribute end detection. Advances the passed TokenSequence
     * to the token <b>after</b> attribute value end delimiter. The delimiter (quote, doublequote)
     * is passed as a parameter. The method returns the token after the attribute value if the delimiter is
     * found and positions the TokenSequence to the returned token. If there's no delimiter,
     * the method returns {@code null} and the TokenSequence position/state is not defined.
     * 
     * @return Token after attribute value or null.
     */
    public static Token<XMLTokenId> skipAttributeValue(TokenSequence ts, char delim) {
        boolean ok = true;
        for (; ok; ok = ts.moveNext()) {
            Token<XMLTokenId> next = ts.token();
            CharSequence cs = next.text();
            if (cs.charAt(cs.length() - 1) == delim) {
                ts.moveNext();
                return ts.token();
            }
        }
        return null;
    }
    
    /**
     * This method looks for '<' and '>' characters in attributes values and
     * returns whitespace-stripped substring which does not contain '<' or '>'.
     * This method should be used to calculate an attribute value which has
     * not currently been closed.
     * @param attributeValue an original attribute value
     * @return the same value of stripped substring of it.
     */
    public static String actualAttributeValue(String attributeValue) {
        int ltIndex = attributeValue.indexOf('<'); // NOI18N
        int gtIndex = attributeValue.indexOf('>'); // NOI18N
        int firstUnwantedIndex = -1;
        if (gtIndex != -1) {
            if (ltIndex != -1 && ltIndex < gtIndex) {
                firstUnwantedIndex = ltIndex;
            } else {
                firstUnwantedIndex = gtIndex;
            }
        } else {
            firstUnwantedIndex = ltIndex;
        }
        
        if (firstUnwantedIndex != -1) {
            char charAtIndex = attributeValue.charAt(firstUnwantedIndex);
            while (charAtIndex == ' ' || charAtIndex == '\t' || charAtIndex  == '\n' ||
            charAtIndex == '\r' || charAtIndex == '<' || charAtIndex == '>') {
                firstUnwantedIndex--;
                if (firstUnwantedIndex < 0) {
                    break;
                }
                charAtIndex = attributeValue.charAt(firstUnwantedIndex);
            }
            
            return attributeValue.substring(0, firstUnwantedIndex + 1);
        } else {
            return attributeValue;
        }
    }
    
    /**
     * Replaces "&lt;", "&gt;", "&apos;", "&quot;", "&amp;" with
     * '<', '>', '\'', '"', '&'.
     * @param a string that may contain &lt;", "&gt;", "&apos;", "&quot;" and "&amp;"
     * @return a string that may contain '<', '>', '\'', '"', '&'.
     */
    public static String replaceEntityStringsWithChars(String value) {
        StringBuffer buf = new StringBuffer(value);
        for (int entity = 0; entity < knownEntityStrings.length; entity++) {
            String curEntityString = knownEntityStrings[entity];
            int indexOfEntity = buf.toString().indexOf(curEntityString);
            while (indexOfEntity != -1) {
                buf.replace(indexOfEntity, indexOfEntity + curEntityString.length(),
                new String(new char[]{knownEntityChars[entity]}));
                indexOfEntity = buf.toString().indexOf(curEntityString);
            }
        }
        
        return buf.toString();
    }
    
    /**
     * Replaces '<', '>', '\'', '"', '&' with
     * "&lt;", "&gt;", "&apos;", "&quot;", "&amp;".
     * @param a string that may contain '<', '>', '\'', '"', '&'.
     * @return a string that may contain &lt;", "&gt;", "&apos;", "&quot;" and "&amp;"
     */
    public static String replaceCharsWithEntityStrings(String value) {
    	if (value == null) {
    		return null;
    	}
        StringBuffer replBuf = new StringBuffer(value.length());
        for (int ind = 0; ind < value.length(); ind++) {
            boolean charReplaced = false;
            char curChar = value.charAt(ind);
            for (int entity = 0; entity < knownEntityChars.length; entity++) {
                if (curChar == knownEntityChars[entity]) {
                    replBuf.append(knownEntityStrings[entity]);
                    charReplaced = true;
                    break;
                }
            }
            
            if (!charReplaced) {
                replBuf.append(curChar);
            }
        }
        
        return replBuf.toString();
    }


    /**
     * Convenience method to reformat portion of document using XML reformatter.
     * @param doc
     * @param startOffset
     * @param endOffset 
     * @throws IllegalArgumentException if the document implementation is not compatible
     */
    public static void reformat(final LineDocument doc, final int startOffset, final int endOffset) {
        final XMLLexerFormatter formatter = new XMLLexerFormatter(null);
        AtomicLockDocument ald = LineDocumentUtils.asRequired(doc, AtomicLockDocument.class);
        ald.runAtomic(new Runnable() {
            public void run() {
                formatter.doReformat(doc, startOffset, endOffset);
            }
        });
    }
    
}

