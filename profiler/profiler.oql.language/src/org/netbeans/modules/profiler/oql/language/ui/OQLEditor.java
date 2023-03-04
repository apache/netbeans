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

package org.netbeans.modules.profiler.oql.language.ui;

import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.profiler.oql.language.OQLTokenId;
import org.netbeans.modules.profiler.oql.spi.OQLEditorImpl;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Bachorik
 */
@ServiceProvider(service=OQLEditorImpl.class)
public class OQLEditor extends OQLEditorImpl{
    private static final class TokenChangeListener implements TokenHierarchyListener {
        volatile boolean validFlag;
        private final Document document;
        public TokenChangeListener(Document document) {
            this.document = document;
        }
        public void tokenHierarchyChanged(TokenHierarchyEvent evt) {
            TokenSequence ts = evt.tokenHierarchy().tokenSequence();
            validFlag = true;
            if (ts.tokenCount() == 0) {
                validFlag = false;
            } else {
                while (ts.moveNext()) {
                    if (ts.token().id() == OQLTokenId.ERROR) {
                        validFlag = false;
                        break;
                    }
                }
            }
            getValidationCallback(document).callback(validFlag);
//            parent.firePropertyChange(VALIDITY_PROPERTY, oldValidity, validFlag);
        }
    };

    private JEditorPane createEditor() {
        String mimeType = "text/x-oql"; // NOI18N
        JEditorPane editorPane = new JEditorPane();

        editorPane.setEditorKit(MimeLookup.getLookup(mimeType).lookup(EditorKit.class));
        TokenHierarchy th = TokenHierarchy.get(editorPane.getDocument());
        th.addTokenHierarchyListener(new TokenChangeListener(editorPane.getDocument()));
        return editorPane;
    }

    public synchronized JEditorPane getEditorPane() {
        return createEditor();
    }
}
