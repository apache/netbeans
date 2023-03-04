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
