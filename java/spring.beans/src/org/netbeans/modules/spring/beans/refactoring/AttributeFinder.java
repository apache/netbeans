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
package org.netbeans.modules.spring.beans.refactoring;

import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;

/**
 *
 * @author Rohan Ranade
 */
public class AttributeFinder {

    private final XMLSyntaxSupport syntaxSupport;
    private final int start;
    private int foundOffset = -1;

    public AttributeFinder(XMLSyntaxSupport syntaxSupport, int start) {
        this.syntaxSupport = syntaxSupport;
        this.start = start;
    }

    public boolean find(String attrName) throws BadLocationException {
        foundOffset = -1;
        Token<XMLTokenId> item = syntaxSupport.getNextToken(start);
        if (item == null || item.id() != XMLTokenId.TAG) {
            return false;
        }
        return syntaxSupport.runWithSequence(start, (TokenSequence s) -> {
            String currentAttrName = null;
            while (s.moveNext()) {
                Token<XMLTokenId> t = s.token();
                XMLTokenId id = t.id();
                if (id == XMLTokenId.ARGUMENT) {
                    currentAttrName = t.text().toString();
                    if (currentAttrName != null && currentAttrName.equals(attrName)) {
                        foundOffset = s.offset();
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
}
