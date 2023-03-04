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
package org.netbeans.modules.spellchecker.bindings.htmlxml;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 * Tokenize RHTML for spell checking: Spell check Ruby comments AND HTML text content!
 *
 * @author Tor Norbye
 */
public class XmlTokenList extends AbstractTokenList {


    private boolean hidden = false;

    public XmlTokenList(BaseDocument doc) {
        super(doc);
    }

    @Override
    public void setStartOffset(int offset) {
        super.setStartOffset (offset);
        FileObject fileObject = FileUtil.getConfigFile ("Spellcheckers/XML");
        Boolean b = (Boolean) fileObject.getAttribute ("Hidden");
        hidden = Boolean.TRUE.equals (b);
    }

    protected int[] findNextSpellSpan() throws BadLocationException {
        TokenHierarchy<Document> h = TokenHierarchy.get((Document) doc);
        TokenSequence<?> ts = h.tokenSequence();
        if (ts == null || hidden) {
            return new int[]{-1, -1};
        }

        ts.move(nextSearchOffset);

        while (ts.moveNext()) {
            TokenId id = ts.token().id();

            if (id == XMLTokenId.BLOCK_COMMENT || id == XMLTokenId.TEXT) {
                return new int[]{ts.offset(), ts.offset() + ts.token().length()};
            }
        }
        return new int[]{-1, -1};
    }
}
