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
