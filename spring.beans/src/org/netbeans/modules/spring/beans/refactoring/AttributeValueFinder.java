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

package org.netbeans.modules.spring.beans.refactoring;

import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;

/**
 *
 * @author abadea
 */
public class AttributeValueFinder {

    private final XMLSyntaxSupport xmlSupport;
    private final int start;

    private int foundOffset = -1;
    private String foundValue;

    public AttributeValueFinder(SyntaxSupport syntaxSupport, int start) {
        this.start = start;
        this.xmlSupport = XMLSyntaxSupport.getSyntaxSupport(syntaxSupport.getDocument());
    }

    public AttributeValueFinder(XMLSyntaxSupport syntaxSupport, int start) {
        this.start = start;
        this.xmlSupport = syntaxSupport;
    }

    public boolean find(String attrName) throws BadLocationException {
        foundOffset = -1;
        foundValue = null;
        if (xmlSupport == null) {
            return false;
        }
        Token<XMLTokenId> item = xmlSupport.getNextToken(start);
        if (item == null || item.id() != XMLTokenId.TAG) {
            return false;
        }
        return xmlSupport.runWithSequence(start, (TokenSequence s) -> {
            String currentAttrName = null;
            while (s.moveNext()) {
                Token<XMLTokenId> t = s.token();
                XMLTokenId id = t.id();
                if (id == XMLTokenId.ARGUMENT) {
                    currentAttrName = t.text().toString();
                } else if (id == XMLTokenId.VALUE) {
                    if (currentAttrName != null && currentAttrName.equals(attrName)) {
                        foundOffset = s.offset();
                        foundValue = t.text().toString();
                        return true;
                    }
                } else if (id == XMLTokenId.TAG) {
                    break;
                }
            }
            return false;
        });
    }

    public int getFoundOffset() {
        return foundOffset;
    }

    public String getValue() {
        return foundValue;
    }
}
