/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.gsf.testrunner.ui.annotation;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.editor.AnnotationDesc;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ImplementationProvider;
import org.netbeans.editor.JumpList;
import org.netbeans.modules.gsf.testrunner.api.CommonUtils;
import org.netbeans.modules.gsf.testrunner.ui.annotation.SelectActionPopup.ActionDescription;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodController.TestMethod;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SingleMethod;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

@ActionID(id = "org.netbeans.modules.gsf.testrunner.ui.RunDebugTestGutterAction", category = "CommonTestRunner")
@ActionRegistration(displayName = "#NM_RunGutterAction", lazy = false)
@ActionReference(path = "Editors/GlyphGutterActions", position = 190)
@Messages("NM_RunGutterAction=Run")
public class RunDebugTestGutterAction extends AbstractAction {
    
    private static final Logger LOG = Logger.getLogger(RunDebugTestGutterAction.class.getName());
    private static final Set<String> fixableAnnotations = new HashSet<>();
    static {
        fixableAnnotations.add("org-netbeans-modules-gsf-testrunner-runnable-test-annotation"); // NOI18N
    }

    public RunDebugTestGutterAction() {
        putValue(NAME, Bundle.NM_RunGutterAction());
    }

    @Override
    public Object getValue(String key) {
        if ("supported-annotation-types".equals(key)) {//NOI18N
            return fixableAnnotations.toArray(new String[0]);
        }
        return super.getValue(key);
    }

    @Messages({
        "ERR_NoTestMethod=No Test Method",
        "# {0} - method name",
        "DN_run.single.method=Run {0} method",
        "# {0} - method name",
        "DN_debug.single.method=Debug {0} method",
        "CAP_SelectAction=Select Action",
    })
    public void actionPerformed(ActionEvent evt) {
        Object source = evt.getSource();

        if (!(source instanceof JTextComponent)) {
            StatusDisplayer.getDefault().setStatusText(Bundle.ERR_NoTestMethod());
            return ; //probably right click menu
        }

        JTextComponent comp = (JTextComponent) source;
        Document doc = comp.getDocument();
        int caretPos = comp.getCaretPosition();
        int line = NbDocument.findLineNumber((StyledDocument) doc, caretPos);
        AnnotationDesc activeAnnotation = ((BaseDocument) doc).getAnnotations().getActiveAnnotation(line);

        if (activeAnnotation != null && fixableAnnotations.contains(activeAnnotation.getAnnotationType())) {
            Map<Position, TestMethod> annotationLines = (Map<Position, TestMethod>) doc.getProperty(TestMethodAnnotation.DOCUMENT_ANNOTATION_LINES_KEY);
            int lineStartOffset = NbDocument.findLineOffset((StyledDocument) doc, line);
            int nextLineStartOffset = NbDocument.findLineOffset((StyledDocument) doc, line + 1);
            TestMethod testMethod = annotationLines.entrySet().stream().filter(e -> lineStartOffset <= e.getKey().getOffset() && e.getKey().getOffset() < nextLineStartOffset).map(e -> e.getValue()).findAny().orElse(null);

            if (testMethod != null) {
                SingleMethod singleMethod = testMethod.method();

                List<ActionDescription> actions = new ArrayList<>();
                ActionProvider ap = CommonUtils.getInstance().getActionProvider(singleMethod.getFile());
                if (ap != null) {
                    for (String command : new String[] {SingleMethod.COMMAND_RUN_SINGLE_METHOD,
                                                        SingleMethod.COMMAND_DEBUG_SINGLE_METHOD}) {
                        String displayName = NbBundle.getMessage(RunDebugTestGutterAction.class, "DN_" + command, singleMethod.getMethodName());
                        if (Arrays.asList(ap.getSupportedActions()).contains(command) && ap.isActionEnabled(command, Lookups.singleton(singleMethod))) {
                            actions.add(new ActionDescription(displayName, () -> ap.invokeAction(command, Lookups.singleton(singleMethod))));
                        }
                    }
                }

                if (actions.size() > 1) {
                    final Point[] p = new Point[1];

                    doc.render(new Runnable() {
                        public void run() {
                            try {
                                int startOffset = NbDocument.findLineOffset((StyledDocument) doc, line);
                                p[0] = comp.modelToView(startOffset).getLocation();
                            }  catch (BadLocationException ex) {
                                LOG.log(Level.WARNING, null, ex);
                            }
                        }
                    });
            
                    JumpList.checkAddEntry(comp, caretPos);

                    SwingUtilities.convertPointToScreen(p[0], comp);

                    PopupUtil.showPopup(new SelectActionPopup(Bundle.CAP_SelectAction(), actions), Bundle.CAP_SelectAction(), p[0].x, p[0].y, true, 0);
                } else if (actions.size() == 1) {
                    actions.get(0).action.run();
                }

                return ;
            }
        }

        Action actions[] = ImplementationProvider.getDefault().getGlyphGutterActions((JTextComponent) source);

        if (actions == null)
            return ;

        int nextAction = 0;

        while (nextAction < actions.length && actions[nextAction] != this)
            nextAction++;

        nextAction++;

        if (actions.length > nextAction) {
            Action a = actions[nextAction];
            if (a != null && a.isEnabled()){
                a.actionPerformed(evt);
            }
        }
    }

    @Override
    public boolean isEnabled() {
        TopComponent activetc = TopComponent.getRegistry().getActivated();
        if (activetc instanceof CloneableEditorSupport.Pane) {
            return true;
        }
        return false;
    }
}

