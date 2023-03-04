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

import java.util.Iterator;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 * Tokenize RHTML for spell checking: Spell check Ruby comments AND HTML text content!
 *
 * @author Tor Norbye, Marek Fukala
 */
public class HtmlTokenList extends AbstractTokenList {

    private String fileType;
    private boolean hidden = false;
    private Iterator<TokenSequence<?>> tss;
    private TokenSequence<?> ts;

    public HtmlTokenList(BaseDocument doc, String fileType) {
        super(doc);
        this.fileType = fileType;
    }

    @Override
    public void setStartOffset(int offset) {
        super.setStartOffset (offset);

        if(fileType != null) {
            FileObject fileObject = FileUtil.getConfigFile ("Spellcheckers/" + fileType);
            Boolean b = (Boolean) fileObject.getAttribute ("Hidden");
            hidden = Boolean.TRUE.equals (b);
        }

        //find top most html embedding token sequence list
        TokenHierarchy<?> th = TokenHierarchy.get(doc);
        LanguagePath htmlPath = null;
        Set<LanguagePath> paths = th.languagePaths();
        for(LanguagePath path : paths) {
            if(path.innerLanguage() == HTMLTokenId.language()) {
                if(htmlPath == null) {
                    htmlPath = path;
                } else {
                    if(htmlPath.size() > path.size()) {
                        //current path is more shallow
                        htmlPath = path;
                    }
                }
            }
        }

        assert htmlPath != null;

        tss = th.tokenSequenceList(htmlPath, offset, Integer.MAX_VALUE).iterator(); //no range provided by the API
        if(tss.hasNext()) {
            ts = tss.next(); //position to the first sequence
            ts.move(offset);
        } else {
           //no html code in the file
        }
        
    }

    //fast hack for making the spellchecking embedding aware, should be fixed properly
    //performance: the current approach is wrong since a new token sequence is obtained
    //and positioned for each search offset!
    @Override
    protected int[] findNextSpellSpan() throws BadLocationException {
        if (ts == null || !ts.isValid() || hidden) {
            return new int[]{-1, -1};
        }

        ts.move(nextSearchOffset);

        while (ts.moveNext()) {
            TokenId id = ts.token().id();

            if (id == HTMLTokenId.SGML_COMMENT || id == HTMLTokenId.BLOCK_COMMENT || id == HTMLTokenId.TEXT) {
                return new int[]{ts.offset(), ts.offset() + ts.token().length()};
            }
        }

        //we are out of the token sequence, try another one
        if(tss.hasNext()) {
            ts = tss.next();
            return findNextSpellSpan();
        }

        return new int[]{-1, -1};
    }


}
