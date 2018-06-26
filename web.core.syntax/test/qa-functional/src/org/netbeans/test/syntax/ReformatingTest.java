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
