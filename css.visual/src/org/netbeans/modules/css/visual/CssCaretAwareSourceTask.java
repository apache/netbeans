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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.css.visual;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.ModelVisitor;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.css.visual.api.CssStylesTC;
import org.netbeans.modules.css.visual.api.RuleEditorController;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.*;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.NbDocument;
import org.openide.windows.WindowManager;

/**
 *
 * @author mfukala@netbeans.org
 */
public final class CssCaretAwareSourceTask extends ParserResultTask<CssParserResult> {

    private static final Logger LOG = Logger.getLogger("rule.editor");
    private static final String CSS_MIMETYPE = "text/css"; //NOI18N
    private boolean cancelled;

    @Override
    public int getPriority() {
        return 5000; //low priority
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.CURSOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

    @Override
    public void run(final CssParserResult result, SchedulerEvent event) {
        final FileObject file = result.getSnapshot().getSource().getFileObject();
        if (file == null) {
            LOG.log(Level.FINE, "run() -  file is null."); // NOI18N
            return ;
        }
        final String mimeType = file.getMIMEType();

        LOG.log(Level.FINER, "run(), file: {0}", new Object[]{file});
        
        if(!mimeType.equals("text/css")) {
            LOG.log(Level.FINE, "ignoring non-pure (text/css) file, actual mimetype is {0}", mimeType);
            return ;
        }

        cancelled = false;

        final int caretOffset;
        if (event == null) {
            caretOffset = -1;
        } else {
            if (event instanceof CursorMovedSchedulerEvent) {
                caretOffset = ((CursorMovedSchedulerEvent) event).getCaretOffset();
            } else {
                LOG.log(Level.FINE, "run() - !(event instanceof CursorMovedSchedulerEvent)");
                caretOffset = -1;
            }
        }

        final Model model = Model.getModel(result); //do this outside EDT
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                runInEDT(result.getSnapshot(), model, file, mimeType, caretOffset);
            }
        });
    }

    private void runInEDT(Snapshot snapshot, Model model, final FileObject file, String mimeType, int caretOffset) {
        LOG.log(Level.FINER, "runInEDT(), file: {0}, caret: {1}", new Object[]{file, caretOffset});

        if (cancelled) {
            LOG.log(Level.FINER, "cancelled");
            return;
        }

        if (caretOffset == -1) {
            try {
                //dirty workaround
                DataObject dobj = DataObject.find(file);
                EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
                if (ec != null) {
                    JEditorPane pane = NbDocument.findRecentEditorPane(ec);
                    if (pane != null) {
                        caretOffset = pane.getCaretPosition();
                    }
                }

            } catch (DataObjectNotFoundException ex) {
                //possibly deleted file, give up
                return;

            }
            
            LOG.log(Level.INFO, "workarounded caret offset: {0}", caretOffset);
        }


        //find rule corresponding to the offset
        Rule rule = findRuleAtOffset(snapshot, model, caretOffset);
        //>>hack, remove once the css.lib css grammar gets finalized
        if(rule != null && rule.getSelectorsGroup() == null) {
            rule = null;
        }
        //<<hack
        
        
        if(rule != null) {
            //check whether the rule is virtual
            if(snapshot.getOriginalOffset(rule.getSelectorsGroup().getStartOffset()) == -1) {
                //virtual selector created for html source element with class or id attribute
                LOG.log(Level.FINER, "the found rule is virtual, exiting w/o change of the RuleEditor", caretOffset);
                return ;
            }
        }
        
        if (!mimeType.equals("text/css")) {
            //if not a css file, 
            //update the rule editor only if there's a rule in an embedded css code
            if (rule == null) {
                LOG.log(Level.FINER, "not a css file and rule not found at {0} offset, exiting w/o change of the RuleEditor", caretOffset);
                return;
            }
        }

        final CssStylesTC cssStylesTC = (CssStylesTC) WindowManager.getDefault().findTopComponent(CssStylesTC.ID);
        if (cssStylesTC == null) {
            return;
        }
        
        //update the RuleEditor TC name
        RuleEditorController controller = cssStylesTC.getRuleEditorController();
        LOG.log(Level.FINER, "SourceTask: calling controller.setModel({0})", model);
        controller.setModel(model);
        
        if (rule == null) {
            controller.setNoRuleState();
        } else {
            controller.setRule(rule);
        }
    }

    private Rule findRuleAtOffset(final Snapshot snapshot, Model model, int documentOffset) {
        final int astOffset = snapshot.getEmbeddedOffset(documentOffset);
        if (astOffset == -1) {
            return null;
        }
        final AtomicReference<Rule> ruleRef = new AtomicReference<Rule>();
        model.runReadTask(new Model.ModelTask() {
            @Override
            public void run(StyleSheet styleSheet) {
                styleSheet.accept(new ModelVisitor.Adapter() {
                    @Override
                    public void visitRule(Rule rule) {
                        if (cancelled) {
                            return;
                        }
                        if (astOffset >= rule.getStartOffset()) {
                            if(astOffset <= rule.getEndOffset()) {
                                //exactly in the rule or at its boundaries
                                ruleRef.set(rule);
                            } else {
                                //behind the rule close curly bracket.
                                //if the offset is at the same line as the rule
                                //end and there're only WS between,
                                //activate the rule
                                CharSequence source = snapshot.getText();
                                
                                //weird - looks like parsing.api bug - the astoffset may point beond the snapshot's length?!?!?
                                int adjustedAstOffset = Math.min(source.length(), astOffset);
                                for(int i = adjustedAstOffset - 1; i >= 0; i--) {
                                    if(i == rule.getEndOffset()) {
                                        ruleRef.set(rule);
                                        return ;
                                    }
                                    char c = source.charAt(i);
                                    if(!(c == ' ' || c == '\t')) {
                                        break; //some non-ws text //todo fix comments
                                    }
                                    if(c == '\n') {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                });

            }
        });
        return ruleRef.get();

    }

    @MimeRegistration(mimeType = "text/css", service = TaskFactory.class)
    public static class Factory extends TaskFactory {

        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            String mimeType = snapshot.getMimeType();

            if (mimeType.equals(CSS_MIMETYPE)) { //NOI18N
                return Collections.singletonList(new CssCaretAwareSourceTask());
            } else {
                return Collections.emptyList();
            }
        }
    }
}
