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
package org.netbeans.modules.jshell.parsing;

import org.netbeans.modules.jshell.model.Rng;
import org.netbeans.modules.jshell.model.ConsoleSection;
import org.netbeans.modules.jshell.model.ConsoleListener;
import org.netbeans.modules.jshell.model.ConsoleModel;
import org.netbeans.modules.jshell.model.ConsoleEvent;
import org.netbeans.modules.jshell.model.JShellToken;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;

/**
 *
 * @author sdedic
 */
public class LexerEmbeddingAdapter implements ConsoleListener {

    @Override
    public void sectionCreated(ConsoleEvent e) {
        process(e.getSource(), e.getAffectedSections());
    }

    @Override
    public void sectionUpdated(ConsoleEvent e) {
        process(e.getSource(), e.getAffectedSections());
    }
    
    private void process(ConsoleModel model, List<ConsoleSection> sections) {
        Document d = model.getDocument();
        AtomicLockDocument ald = LineDocumentUtils.as(d, AtomicLockDocument.class);
        Runnable r = () -> {
        TokenHierarchy h = TokenHierarchy.get(d);
        
        if (h == null) {
            return;
        }
        TokenSequence seq = h.tokenSequence();
        for (ConsoleSection s : sections) {
            if (s.getType().java) {
                defineEmbeddings(seq, model, s);
            }
            
        }};
        if (ald != null) {
            ald.runAtomicAsUser(r);
        } else {
            d.render(r);
        }
    }
    
    private void defineEmbeddings(TokenSequence seq, ConsoleModel model, ConsoleSection s) {
        F: for (Rng r : s.getPartRanges()) {
            seq.move(r.start);
            Token<JShellToken> tukac;
            
            W: while (seq.moveNext() && seq.offset() < r.end) {
                tukac = seq.token();
                switch (tukac.id()) {
                    
                    case JAVA:
                        seq.createEmbedding(JavaTokenId.language(), 0, 0);
                        
                        // fall through
                    case WHITESPACE: 
                        break;
                        
                    default:
                        break W;
                }
            }
        }
    }

    @Override
    public void executing(ConsoleEvent e) {
    }

    @Override
    public void closed(ConsoleEvent e) {
        
    }
}
