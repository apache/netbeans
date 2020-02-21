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

package org.netbeans.modules.cnd.completion;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.completion.debugger.CsmFunctionCallsProviderImpl;
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
public class FunctionCallsTestCase extends ProjectBasedTestCase {
    public FunctionCallsTestCase(String testName) {
        super(testName, false);
    }

    public void testFunctionCallsWrong() throws Exception {
        performTest("file.cc", -1); // should not fail
    }

    public void testFunctionCallsMultiple() throws Exception {
        performTest("file.cc", 18); // should not fail
    }

    public void testFunctionCallsNone() throws Exception {
        performTest("file.cc", 25); // should not fail
    }

    public void testFunctionCallsArguments() throws Exception {
        performTest("file.cc", 19); // should not fail
    }

    public void testFunctionCallsNoStd() throws Exception {
        performTest("file.cc", 26); // should not fail
    }

    private void performTest(String source, int lineIndex) throws Exception {
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

        List<CsmReference> res = new CsmFunctionCallsProviderImpl().getFunctionCalls(doc, lineIndex-1);

        for (CsmReference fc : res) {
            CsmFunction function = (CsmFunction)fc.getReferencedObject();
            streamOut.println(function.getSignature());
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
            fail("OUTPUT Difference between diff " + output + " " + goldenCopyFile); // NOI18N
        }
    }

    //TODO: copied from CompletionTestPerformer
    private FileObject getTestFile(File testFile, PrintWriter log) throws IOException, InterruptedException, PropertyVetoException {
        FileObject test = CndFileUtils.toFileObject(testFile);
        CsmFile csmFile = CsmModelAccessor.getModel().findFile(FSPath.toFSPath(test), true, false);
        if (test == null || !test.isValid() || csmFile == null) {
            throw new IllegalStateException("Given test file does not exist.");
        }
        log.println("File found: " + csmFile);
        return test;
    }
}
