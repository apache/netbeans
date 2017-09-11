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
