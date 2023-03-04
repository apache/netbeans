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

package org.netbeans.modules.xml.text.navigator;

/**
 *
 * @author mfukala@netbeans.org
 */
public class HTMLTextEncoder {
    
     /** Definitions for a limited subset of SGML character entities */
    private static final Object[] entities = new Object[] {
            new char[] { 'g', 't' }, new char[] { 'l', 't' }, //NOI18N
            new char[] { 'q', 'u', 'o', 't' }, new char[] { 'a', 'm', 'p' }, //NOI18N
            new char[] { 'l', 's', 'q', 'u', 'o' }, //NOI18N
            new char[] { 'r', 's', 'q', 'u', 'o' }, //NOI18N
            new char[] { 'l', 'd', 'q', 'u', 'o' }, //NOI18N
            new char[] { 'r', 'd', 'q', 'u', 'o' }, //NOI18N
            new char[] { 'n', 'd', 'a', 's', 'h' }, //NOI18N
            new char[] { 'm', 'd', 'a', 's', 'h' }, //NOI18N
            new char[] { 'n', 'e' }, //NOI18N
            new char[] { 'l', 'e' }, //NOI18N
            new char[] { 'g', 'e' }, //NOI18N
            new char[] { 'c', 'o', 'p', 'y' }, //NOI18N
            new char[] { 'r', 'e', 'g' }, //NOI18N
            new char[] { 't', 'r', 'a', 'd', 'e' }, //NOI18N
            new char[] { 'n', 'b', 's', 'p' //NOI18N
            }
        }; //NOI18N

    /** Mappings for the array of SGML character entities to characters */
    private static final char[] entitySubstitutions = new char[] {
            '>', '<', '"', '&', 8216, 8217, 8220, 8221, 8211, 8212, 8800, 8804, 8805, //NOI18N
            169, 174, 8482, ' '
        };
    
    /** Encodes the given string using the built-in translation table.*/
    static String encodeHTMLText(String content) {
        StringBuffer encoded = new StringBuffer();
        for(int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);
            encoded.append(encode(ch));
        }
        return encoded.toString();
    }
    
    /** Encodes the given character using the translation table. 
     * It returns the encoded HTML entities in the &ent; form.*/
    private static char[] encode(char ch) {
        for(int i=0; i < entitySubstitutions.length; i++) {
            if(entitySubstitutions[i] == ch) {
                char[] chs = (char[])entities[i];
                //add the ampersand char before and semicolon after the text
                char[] _chs = new char[chs.length + 2];
                _chs[0] = '&';_chs[_chs.length - 1] = ';'; //NOI18N
                System.arraycopy(chs, 0, _chs, 1, chs.length);

                return _chs;
            }
        }
        return new char[]{ch};
    }
    
}
