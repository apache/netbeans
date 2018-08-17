package org.netbeans.modules.javascript2.jade.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.javascript2.jade.editor.lexer.JadeTokenId;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
/**
 *
 * @author Petr Pisl
 */
@EmbeddingProvider.Registration(
        mimeType="text/jade",
        targetMimeType="text/javascript"
)

public class JadeJsEmbeddingProvider extends EmbeddingProvider {
    
    private static final Logger LOGGER = Logger.getLogger(JadeJsEmbeddingProvider.class.getName());
    private static final String JS_MIME_TYPE = "text/javascript"; //NOI18N
    
    private static final String SEMICOLON_EOL = ";\n"; //NOI18N
    private static final String EOL = "\n"; //NOI18N
    private static final String SCRIPT_TAG_NAME = "script";     // NOI18N
    
    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        TokenHierarchy<?> th = snapshot.getTokenHierarchy();
        TokenSequence<JadeTokenId> ts = th.tokenSequence(JadeTokenId.jadeLanguage());
        
        if (ts == null) {
            LOGGER.log(
                    Level.WARNING,
                    "TokenHierarchy.tokenSequence(JadeTokenId.jadeLanguage()) == null " + "for static immutable Jade TokenHierarchy!\nFile = ''{0}'' ;snapshot mimepath=''{1}''",
                    new Object[]{snapshot.getSource().getFileObject().getPath(), snapshot.getMimePath()});

            return Collections.emptyList();
        }
        
        ts.moveStart();
        
        List<Embedding> embeddings = new ArrayList<>();
        
        int from = -1;
        int len = 0;
        Token<JadeTokenId> lastTag = null;
        Token<JadeTokenId> lastMixing = null;
        
        while (ts.moveNext()) {
            Token<JadeTokenId> token = ts.token();
            if (token.id() == JadeTokenId.JAVASCRIPT) {
                if (from < 0) {
                    from = ts.offset();
                }
                len += token.length();
            } else {
                if (from >= 0) {
                    if (lastMixing != null) {
                        wrapAsFnParameter(snapshot, embeddings, lastMixing.text().toString(), from, len);
                    } else {
                        embeddings.add(snapshot.create(from, len, JS_MIME_TYPE));
                        addNewLine(snapshot, embeddings, from + len - 1);
                    }
                } 
                from = -1;
                len = 0;
            }
            if (token.id() == JadeTokenId.TAG) {
                lastTag = token;
                lastMixing = null;
            }
            if (token.id() == JadeTokenId.MIXIN_NAME) {
                lastTag = null;
                lastMixing = token;
            }
            if (token.id() == JadeTokenId.PLAIN_TEXT_DELIMITER) {
                // check whether there is not 
                if (lastTag != null && SCRIPT_TAG_NAME.equals(lastTag.text().toString().toLowerCase()) && ts.moveNext()) {
                    token = ts.token();
                    while (token.id() == JadeTokenId.EOL && ts.moveNext()) {
                        token = ts.token();
                    }
                    if (token.id() == JadeTokenId.PLAIN_TEXT || token.id() == JadeTokenId.JAVASCRIPT) {
                        embeddings.add(snapshot.create(ts.offset(), token.length(), JS_MIME_TYPE));
                    }
                }
            }
        }
        if (from >= 0) {
            if (lastMixing != null) {
                wrapAsFnParameter(snapshot, embeddings, lastMixing.text().toString(), from, len);
            } else {
                embeddings.add(snapshot.create(from, len, JS_MIME_TYPE));
                addNewLine(snapshot, embeddings, from + len - 1);
            }
        }
        if (embeddings.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(Embedding.create(embeddings));
    }

    private void addNewLine(Snapshot snapshot, List<Embedding> embeddings, int origOffset) {
        CharSequence text = snapshot.getText();
        int offset = origOffset;
        while (offset > 0 && Character.isWhitespace(text.charAt(offset))) {
            offset--;
        }
        if (offset > 0 ) {
            char ch = text.charAt(offset);
            if (ch == '{' || ch == '}' || ch == '[') {
                embeddings.add(snapshot.create(EOL, JS_MIME_TYPE));
            } else {
                embeddings.add(snapshot.create(SEMICOLON_EOL, JS_MIME_TYPE));
            }
        }
    }
    
    private void wrapAsFnParameter(Snapshot snapshot, List<Embedding> embeddings, String fnName, int from, int len) {
        embeddings.add(snapshot.create(fnName + "(", JS_MIME_TYPE));    //NOI18N
        embeddings.add(snapshot.create(from, len, JS_MIME_TYPE));
        embeddings.add(snapshot.create(");\n", JS_MIME_TYPE)); //NOI18N
    }
    
    @Override
    public int getPriority() {
        return 202;
    }

    @Override
    public void cancel() {
        // do nothig for now
    }
    
}
