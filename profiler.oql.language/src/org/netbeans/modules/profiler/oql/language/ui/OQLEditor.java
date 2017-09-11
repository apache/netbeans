/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
    final static private class TokenChangeListener implements TokenHierarchyListener {
        volatile boolean validFlag;
        final private Document document;
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

    synchronized public JEditorPane getEditorPane() {
        return createEditor();
    }
}
