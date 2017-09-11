/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.hints.other;

import java.util.Collections;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.editor.BaseAction;
import org.netbeans.modules.csl.api.CslActions;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.csl.api.RuleContext;
import org.openide.util.NbBundle;

/**
 *
 * @author mfukala@netbeans.org
 */
@NbBundle.Messages("MSG_SurroundWithTag=Surround With Tag")
public class SurroundWithTag extends Hint {

    private static final Rule RULE = new SurroundWithTagRule();

    public SurroundWithTag(RuleContext context, OffsetRange range) {
        super(RULE,
                Bundle.MSG_SurroundWithTag(),
                context.parserResult.getSnapshot().getSource().getFileObject(),
                range,
                Collections.<HintFix>singletonList(new SurroundWithTagHintFix(context)),
                10);
    }

    private static class SurroundWithTagHintFix implements HintFix {

        private static final String OPEN_TAG = "<div>"; //NOI18N
        private static final String CLOSE_TAG = "</div>"; //NOI18N
        RuleContext context;

        public SurroundWithTagHintFix(RuleContext context) {
            this.context = context;
        }

        @Override
        public String getDescription() {
            return Bundle.MSG_SurroundWithTag();
        }

        @Override
        public void implement() throws Exception {
            context.doc.runAtomic(new Runnable() {

                @Override
                public void run() {
                    try {
                        //I need to insert the <div> tag instead of just empty <> tag from two reasons:
                        //1) the InstantRenameAction from CSL does a pre check before calling the
                        //   InstantRenamer if the text under the caret represents an identifier
                        //2) simplier implementation of the HtmlRenameHandler so it finds a paired tags
                        //   at the caret which wouldn't happen for the empty tags: <>...</> 
                        //
                        //I may possibly fix both issues later if one complains

                        context.doc.insertString(context.selectionStart, OPEN_TAG, null);
                        context.doc.insertString(context.selectionEnd + OPEN_TAG.length(), CLOSE_TAG, null);

                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                JTextComponent pane = EditorRegistry.focusedComponent();

                                //select the "p" char so it is overwritten once user starts typing
                                pane.select(context.selectionStart + 1, context.selectionStart + 2);

                                //set caret after the opening tag delimiter
                                pane.setCaretPosition(context.selectionStart + 1);
                                
                                //invoke instant rename
                                BaseAction instantRenameAction = (BaseAction) CslActions.createInstantRenameAction();
                                instantRenameAction.actionPerformed(null, pane);
                            }
                        });

                    } catch (BadLocationException ex) {
                        //ignore
                    }

                }
            });
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }
    }

    private static class SurroundWithTagRule implements Rule {

        @Override
        public boolean appliesTo(RuleContext context) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Bundle.MSG_SurroundWithTag();
        }

        @Override
        public boolean showInTasklist() {
            return false;
        }

        @Override
        public HintSeverity getDefaultSeverity() {
            return HintSeverity.INFO;
        }
    }
}
