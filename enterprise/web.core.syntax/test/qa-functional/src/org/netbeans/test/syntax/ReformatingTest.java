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

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import junit.framework.Test;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.AssertionFailedErrorException;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.web.FileObjectFilter;
import org.netbeans.test.web.RecurrentSuiteFactory;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jindrich Sedek
 */
public class ReformatingTest extends CompletionTest {

    private static final String reformatSimplePrefix = "reformatTest";
    private static final String reformatSelection = "reformatSelection";
    private static final String reformatTwice = "reformatTwice";

    /** Creates a new instance of IndentationTest */
    public ReformatingTest(String name, FileObject testFileObj) {
        super(name, testFileObj);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.emptyConfiguration();
        addServerTests(conf, new String[0]);//register server
        conf = conf.enableModules(".*").clusters(".*");
        return NbModuleSuite.create(conf.addTest(SuiteCreator.class));
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

    public static final class SuiteCreator extends NbTestSuite {

        public SuiteCreator() {
            super();
            File datadir = new IndentationTest(null, null).getDataDir();
            File projectsDir = new File(datadir, "IndentationTestProjects");
            FileObjectFilter filter = new FileObjectFilter() {

                public boolean accept(FileObject fObject) {
                    String ext = fObject.getExt();
                    String name = fObject.getName();
                    return (name.startsWith(reformatSimplePrefix) || name.startsWith(reformatSelection) || name.startsWith(reformatTwice)) && (XML_EXTS.contains(ext) || JSP_EXTS.contains(ext));
                }
            };
            addTest(RecurrentSuiteFactory.createSuite(ReformatingTest.class, projectsDir, filter));
        }
    }

    @Override
    public void runTest() throws Exception {
        try {
            BaseDocument doc = openFile(testFileObj);
            String fileName = testFileObj.getNameExt();
            EditorOperator eOperator = new EditorOperator(fileName);
            waitTypingFinished(doc);
            if (fileName.startsWith(reformatSimplePrefix)) {
                eOperator.pushKey(KeyEvent.VK_F, InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
            } else if (fileName.startsWith(reformatTwice)) {
                eOperator.pushKey(KeyEvent.VK_F, InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
                eOperator.waitModified(true);
                String str = eOperator.getText();
                eOperator.pushKey(KeyEvent.VK_F, InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
                assertEquals(eOperator.getText(), str);//no change should be done during second reformating
            } else {// reformat selection
                String text = eOperator.getText();
                int firstIndex = text.indexOf(':');
                String first = text.substring(0, firstIndex);
                String second = text.substring(firstIndex + 1, text.indexOf(':', firstIndex + 1));
                eOperator.select(Integer.parseInt(first), Integer.parseInt(second));
                waitTypingFinished(doc);
                eOperator.pushKey(KeyEvent.VK_F, InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
            }
            eOperator.waitModified(true);
            ref(eOperator.getText());
        } catch (Exception ex) {
            throw new AssertionFailedErrorException(ex);
        }
        ending();
    }
}
