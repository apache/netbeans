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
package org.netbeans.test.syntax;

import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.JEditorPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Caret;
import junit.framework.Test;
import org.netbeans.editor.Utilities;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.editor.BaseDocument;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.web.FileObjectFilter;
import org.netbeans.test.web.RecurrentSuiteFactory;
import org.netbeans.test.syntax.CompletionTest.TestStep;
import org.openide.actions.UndoAction;
import org.openide.filesystems.FileObject;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Jindrich Sedek
 */
public class AutoCompletionTest extends CompletionTest {
    
    /** Creates a new instance of AutoCompletionTest */
    public AutoCompletionTest(String name, FileObject testFileObj) {
        super(name, testFileObj);
    }
    
    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.emptyConfiguration();
        addServerTests(Server.GLASSFISH, conf, new String[0]);//register server
        conf = conf.enableModules(".*").clusters(".*");
        return NbModuleSuite.create(conf.addTest(SuiteCreator.class));
    }

    public static final class SuiteCreator extends NbTestSuite {

        public SuiteCreator() {
            File datadir = new AutoCompletionTest(null, null).getDataDir();
            File projectsDir = new File(datadir, "AutoCompletionTestProjects");
            FileObjectFilter filter = new FileObjectFilter() {

                public boolean accept(FileObject fo) {
                    String ext = fo.getExt();
                    String name = fo.getName();
                    return (name.startsWith("test") || name.startsWith("Test")) && (XML_EXTS.contains(ext) || JSP_EXTS.contains(ext) || ext.equals("java"));
                }
            };
            addTest(RecurrentSuiteFactory.createSuite(AutoCompletionTest.class, projectsDir, filter));
        }
    }

    @Override
    protected File getProjectsDir() {
        File datadir = new CompletionTest().getDataDir();
        return new File(datadir, "AutoCompletionTestProjects");
    }

    @Override
    protected void exec(JEditorPane editor, TestStep step) throws Exception {
        try {
            final BaseDocument doc = (BaseDocument) editor.getDocument();
            ref(step.toString());
            Caret caret = editor.getCaret();
            caret.setDot(step.getOffset() + 1);
            EditorOperator eo = new EditorOperator(testFileObj.getNameExt());
            eo.insert(step.getPrefix());
            waitScanFinished();
            caret.setDot(step.getCursorPos());
            eo.save();
            waitTypingFinished(doc);
            final Object lock = new Object();
            synchronized (lock) {
                doc.addDocumentListener(new DocumentListener() {

                    public void insertUpdate(DocumentEvent e) {
                        synchronized (lock) {
                            lock.notifyAll();
                        }
                    }

                    public void removeUpdate(DocumentEvent e) {
                    }

                    public void changedUpdate(DocumentEvent e) {
                    }
                });
                CompletionJListOperator.hideAll();
                eo.txtEditorPane().pressKey(KeyEvent.VK_SPACE, KeyEvent.CTRL_MASK);
                lock.wait(10000);
            }
            waitTypingFinished(doc);
            waitTypingFinished(doc);
            int rowStart = Utilities.getRowStart(doc, step.getOffset() + 1);
            int rowEnd = Utilities.getRowEnd(doc, step.getOffset() + 1);
            String result = doc.getText(new int[]{rowStart, rowEnd}).trim();
            if (!result.equals(step.getResult())) {
                ref("EE: unexpected CC result:\n< " + result + "\n> " + step.getResult());
            }
        } finally {
            // undo all changes
            final UndoAction ua = SystemAction.get(UndoAction.class);
            assertNotNull("Cannot obtain UndoAction", ua);
            while (ua.isEnabled()) {
                runInAWT(new Runnable() {

                    public void run() {
                        ua.performAction();
                    }
                });
            }
        }
    }
}
