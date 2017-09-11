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

package org.netbeans.modules.languages.features;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtKit.CommentAction;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;

/**
 *
 * @author Dan
 */
public class CodeCommentAction extends CommentAction {
    
    public static final String COMMENT_LINE = "COMMENT_LINE";

    public CodeCommentAction() {
        super(""); // NOI18N
    }

    public void actionPerformed (final ActionEvent evt, final JTextComponent target) {
        if (target != null) {
            if (!target.isEditable() || !target.isEnabled()) {
                target.getToolkit().beep();
                return;
            }
            final BaseDocument doc = (BaseDocument)target.getDocument();
            final Caret caret = target.getCaret();
            final TokenHierarchy th = TokenHierarchy.get (doc);
            if (th == null) {
                return;
            }
            final TokenSequence ts = th.tokenSequence();
            try {
                if (caret.isSelectionVisible()) {
                    final int startPos = Utilities.getRowStart(doc, target.getSelectionStart());
                    final int endPos = target.getSelectionEnd();
                    doc.runAtomicAsUser (new Runnable () {
                        public void run () {
                            try {
                                int end = (endPos > 0 && Utilities.getRowStart(doc, endPos) == endPos) ?
                                    endPos-1 : endPos;
                                int lineCnt = Utilities.getRowCount(doc, startPos, end);
                                List mimeTypes = new ArrayList(lineCnt);
                                int pos = startPos;
                                for (int x = lineCnt ; x > 0; x--) {
                                    mimeTypes.add(getRealMimeType(ts, pos));
                                    pos = Utilities.getRowStart(doc, pos, 1);
                                }

                                pos = startPos;
                                for (Iterator iter = mimeTypes.iterator(); iter.hasNext(); ) {
                                    modifyLine(doc, (String)iter.next(), pos);
                                    pos = Utilities.getRowStart(doc, pos, 1);
                                }
                            } catch (BadLocationException e) {
                                target.getToolkit().beep();
                            }
                        }
                    });
                } else { // selection not visible
                    final int pos = Utilities.getRowStart(doc, target.getSelectionStart());
                    final String mt = getRealMimeType(ts, pos);
                    doc.runAtomicAsUser (new Runnable () {
                        public void run () {
                            try {
                                modifyLine(doc, mt, pos);
                            } catch (BadLocationException e) {
                                target.getToolkit().beep();
                            }
                        }
                    });
                }
            } catch (BadLocationException e) {
                target.getToolkit().beep();
            }
        }
    }
    
    private String getRealMimeType(TokenSequence ts, int offset) {
        while (true) {
            ts.move(offset);
            if (!ts.moveNext())
                break;
            offset = ts.offset();
            TokenSequence ts2 = ts.embedded();
            if (ts2 == null) break;
            ts = ts2;
        }
        return ts.language().mimeType();
    }
    
    private void modifyLine(BaseDocument doc, String mimeType, int offset) throws BadLocationException {
        Feature feature = null;
        try {
            Language language = LanguagesManager.getDefault().getLanguage(mimeType);
            feature = language.getFeatureList ().getFeature (COMMENT_LINE);
        } catch (LanguageDefinitionNotFoundException e) {
        }
        if (feature != null) {
            String prefix = (String) feature.getValue("prefix"); // NOI18N
            if (prefix == null) {
                return;
            }
            String suffix = (String) feature.getValue("suffix"); // NOI18N
            if (suffix != null) {
                int end = Utilities.getRowEnd(doc, offset);
                doc.insertString(end, suffix, null);
            }
            doc.insertString(offset, prefix, null);
        }
    }
    
}
