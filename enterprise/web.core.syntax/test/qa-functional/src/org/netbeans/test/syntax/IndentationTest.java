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
import junit.framework.Test;
import org.netbeans.junit.AssertionFailedErrorException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.web.FileObjectFilter;
import org.netbeans.test.web.RecurrentSuiteFactory;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jindrich Sedek
 */
public class IndentationTest extends CompletionTest {

    private static final String[] START_STEPS = {"<!--CC", "/*CC"};
    private static final String[] END_STEPS = {"-->", "*/"};
    private boolean debugMode = false;

    /** Creates a new instance of IndentationTest */
    public IndentationTest(String name, FileObject testFileObj) {
        super(name, testFileObj);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.emptyConfiguration();
        addServerTests(conf, new String[0]);//register server
        conf = conf.enableModules(".*").clusters(".*");
        return NbModuleSuite.create(conf.addTest(SuiteCreator.class));
    }

    public static final class SuiteCreator extends NbTestSuite {

        public SuiteCreator() {
            super();
            // find folder with test projects and define file objects filter
            File datadir = new IndentationTest(null, null).getDataDir();
            File projectsDir = new File(datadir, "IndentationTestProjects");
            FileObjectFilter filter = new FileObjectFilter() {

                public boolean accept(FileObject fObject) {
                    String ext = fObject.getExt();
                    String name = fObject.getName();
                    return name.startsWith("test") && (XML_EXTS.contains(ext) || JSP_EXTS.contains(ext));
                }
            };
            addTest(RecurrentSuiteFactory.createSuite(IndentationTest.class, projectsDir, filter));
        }
    }

    @Override
    public File getProjectsDir(){
        File datadir = new CompletionTest().getDataDir();
        return new File(datadir, "IndentationTestProjects");
    }

    @Override
    protected void finalizeProjectsOpening() {
        IndentCasesTest.setIndent(2);
        IndentCasesTest.setIndent(4);
    }

    @Override
    public void runTest() throws Exception {
        try {
            BaseDocument doc = openFile(testFileObj);
            String text = doc.getText(0, doc.getLength());
            Possition actualPossition = getNextPossition(text, 0);
            EditorOperator eOperator;
            while (actualPossition != null) {//go through all cases
                doc.remove(actualPossition.start, actualPossition.len);
                eOperator = new EditorOperator(testFileObj.getName());
                eOperator.txtEditorPane().getCaret().setDot(actualPossition.start);
                eOperator.save();
                CompletionJListOperator.hideAll();
                int invocationPossition = eOperator.txtEditorPane().getCaret().getDot();
                eOperator.pushKey(KeyEvent.VK_ENTER);
                eOperator.waitModified(true);              
                Thread.sleep(1000);
                CompletionJListOperator.hideAll();
                int shift = eOperator.txtEditorPane().getCaret().getDot() - invocationPossition;
                ref("line " + eOperator.getLineNumber() + ": " + shift);
                if (debugMode) {
                    doc.insertString(eOperator.txtEditorPane().getCaret().getDot(), "|", null);
                    ref(actualPossition.start + " -> " + eOperator.txtEditorPane().getCaretPosition());
                    Thread.sleep(2000);
                }
                actualPossition = getNextPossition(doc.getText(0, doc.getLength()), actualPossition.start + 1); // modified, previous version: actualPossition = getNextPossition(eOperator.getText(), actualPossition.start + 1);
            }
            if (debugMode) {
                ref(new EditorOperator(testFileObj.getName()).getText());
            }
        } catch (Exception ex) {
            throw new AssertionFailedErrorException(ex);
        }
        ending();
    }

    private Possition getNextPossition(String text, int actual) {
        int minStart = Integer.MAX_VALUE,
                len = -1;
        for (int i = 0; i < START_STEPS.length; i++) {
            int pos = text.indexOf(START_STEPS[i], actual);
            if ((pos != -1) && (pos < minStart)) {
                minStart = pos;
                int minEnd = text.indexOf(END_STEPS[i], actual);
                len = minEnd - minStart + END_STEPS[i].length();
            }
        }
        if (minStart != Integer.MAX_VALUE) {
            return new Possition(minStart, len);
        } else {
            return null;
        }
    }

    private class Possition {

        public int start,  len;

        Possition(int start, int len) {
            this.start = start;
            this.len = len;
        }
    }
}
