/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.completion;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.completion.debugger.CsmAutosProviderImpl;
import org.netbeans.modules.cnd.modelimpl.test.ProjectBasedTestCase;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 *
 */
public class AutosTestCase extends ProjectBasedTestCase {

    public AutosTestCase(String testName) {
        super(testName, false);
    }
    
    public void testAutosWrong() throws Exception {
        performTest("file.cc", -1); // should not fail
    }

    public void testAutosFirst() throws Exception {
        performTest("file.cc", 0);
    }

    public void testAutosMain() throws Exception {
        performTest("file.cc", 2);
    }

    public void testAutosBody() throws Exception {
        performTest("file.cc", 6);
    }

    public void testAutosInFunction() throws Exception {
        performTest("file.cc", 3);
    }

    public void testAutosStatement() throws Exception {
        performTest("file.cc", 7);
    }

    public void testAutosInStatement() throws Exception {
        performTest("file.cc", 8);
    }

    public void testAutosInFor() throws Exception {
        performTest("file.cc", 9);
    }

    public void testAutosIfEnd() throws Exception {
        performTest("file.cc", 11);
    }

    public void testAutosCompoundLine1() throws Exception {
        performTest("file.cc", 14);
    }

    public void testAutosCompoundLine2() throws Exception {
        performTest("file.cc", 15);
    }

    public void testAutosCompoundLine3() throws Exception {
        performTest("file.cc", 29);
    }

    public void testAutosEmptyLine() throws Exception {
        performTest("file.cc", 19);
    }

    public void testAutosMultiLine() throws Exception {
        performTest("file.cc", 23);
    }

    public void testAutosComment() throws Exception {
        performTest("file.cc", 27);
    }

    public void testAutosScope() throws Exception {
        performTest("file.cc", 31);
    }
    
    public void testAutosArrays() throws Exception {
        performTest("file.cc", 36);
    }
    
    public void testAutosArrays2() throws Exception {
        performTest("file.cc", 38);
    }
    
    public void testAutosNoCodeModel() throws Exception {
        performTest("file.cc", 38, true);
    }

    private void performTest(String source, int lineIndex) throws Exception {
        performTest(source, lineIndex, false);
    }
    private void performTest(String source, int lineIndex, boolean closeProject) throws Exception {
        File workDir = getWorkDir();
        File testFile = getDataFile(source);
        String goldenFileName = getName()+".ref"; //NOI18N

        File output = new File(workDir, goldenFileName); //NOI18N
        PrintStream streamOut = new PrintStream(output);

        FileObject testFileObject = getTestFile(testFile, logWriter);
        final DataObject testFileDO = DataObject.find(testFileObject);
        if (testFile == null) {
            throw new DataObjectNotFoundException(testFileObject);
        }

        final StyledDocument doc = (StyledDocument)CndCoreTestUtils.getBaseDocument(testFileDO);
        
        if (closeProject){
            closeProject(getProject().getName().toString());
        }
        
        Set<String> res = new CsmAutosProviderImpl().getAutos(doc, lineIndex-1);

        // sort results
        List<String> resList = new ArrayList<String>();
        if (res == null) {
            resList.add("null");
        } else {
            resList.addAll(res);
            Collections.sort(resList);
        }

        for (String val : resList) {
            streamOut.println(val);
        }
        streamOut.close();

        File goldenDataFile = getGoldenFile(goldenFileName);
        if (!goldenDataFile.exists()) {
            fail("No golden file " + goldenDataFile.getAbsolutePath() + "\n to check with output file " + output.getAbsolutePath());
        }

        if (CndCoreTestUtils.diff(output, goldenDataFile, null)) {
            // copy golden
            File goldenCopyFile = new File(workDir, goldenFileName + ".golden");
            CndCoreTestUtils.copyToWorkDir(goldenDataFile, goldenCopyFile); // NOI18N
            StringBuilder buf = new StringBuilder("OUTPUT Difference between diff " + output + " " + goldenCopyFile);
            File diffErrorFile = new File(output.getAbsolutePath() + ".diff");
            CndCoreTestUtils.diff(output, goldenDataFile, diffErrorFile);
            showDiff(diffErrorFile, buf);
            fail(buf.toString());
        }
    }

    //TODO: copied from CompletionTestPerformer
    private FileObject getTestFile(File testFile, PrintWriter log) throws IOException, InterruptedException, PropertyVetoException {
        FileObject test = CndFileUtils.toFileObject(testFile);
        CsmFile csmFile = CsmModelAccessor.getModel().findFile(FSPath.toFSPath(test), true, false);
        if (test == null || ! test.isValid() || csmFile == null) {
            throw new IllegalStateException("Given test file does not exist.");
        }
        log.println("File found: " + csmFile);
        return test;
    }
}
