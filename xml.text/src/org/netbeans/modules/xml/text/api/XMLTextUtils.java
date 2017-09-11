/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
     * @throw IllegalArgumentException if the document implementation is not compatible
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

