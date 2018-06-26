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
