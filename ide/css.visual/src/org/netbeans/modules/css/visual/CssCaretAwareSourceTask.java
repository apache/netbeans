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
