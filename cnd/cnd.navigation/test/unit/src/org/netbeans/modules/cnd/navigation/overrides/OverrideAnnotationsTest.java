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

package org.netbeans.modules.cnd.navigation.overrides;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.modelimpl.test.ProjectBasedTestCase;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 */
public class OverrideAnnotationsTest extends ProjectBasedTestCase {

    public OverrideAnnotationsTest(String testName) {
        super(testName);
    }

    public void testPrimitiveOverrides() throws Exception {
        performTest("primitive.cc", "\\d*:OVERRID.*", ".overrides.ref");
    }
    public void testPrimitiveClasses() throws Exception {
        performTest("primitive.cc", "\\d*:INHERIT.*", ".extends.ref");
    }

    public void testProperOverridesParentSingle() throws Exception {
        performTest("proper_parent_single.cc", "\\d*:OVERRID.*", ".overrides.ref");
    }
    public void testClassesSingleInh() throws Exception {
        performTest("proper_parent_single.cc", "\\d*:INHERIT.*", ".extends.ref");
    }


    public void testProperOverridesParentMulty() throws Exception {
        performTest("proper_parent_multy.cc", "\\d*:OVERRID.*", ".overrides.ref");
    }
    public void testClassesMultyInh() throws Exception {
        performTest("proper_parent_multy.cc", "\\d*:INHERIT.*", ".extends.ref");
    }

    public void testSingleOverrides() throws Exception {
        performTest("single_inh_tree.cc", "\\d*:OVERRID.*", ".overrides.ref");
    }
    public void testSingleInhTree() throws Exception {
        performTest("single_inh_tree.cc", "\\d*:INHERIT.*", ".extends.ref");
    }

    public void testRombOverrides() throws Exception {
        performTest("romb_half_virtual.cc", "\\d*:OVERRID.*", ".overrides.ref");
    }
    public void testRombInhTree() throws Exception {
        performTest("romb_half_virtual.cc", "\\d*:INHERIT.*", ".extends.ref");
    }
    
    public void testBug252147Inh() throws Exception {
        performTest("bug252147.cpp", "\\d*:INHERIT.*", ".extends.ref");
    }
    public void testBug252147Overrides() throws Exception {
        performTest("bug252147.cpp", "\\d*:OVERRID.*", ".overrides.ref");
    }
    
    public void testBug269823Inh() throws Exception {
        performTest("bug269823.cpp", "\\d*:INHERIT.*", ".extends.ref");
    }

    public void testBug269823Overrides() throws Exception {
        performTest("bug269823.cpp", "\\d*:OVERRID.*", ".overrides.ref");
    }
    
    private void performTest(String sourceFileName, String patternString, String refPostfix) throws Exception {
        File testSourceFile = getDataFile(sourceFileName);
        assertNotNull(testSourceFile);
        StyledDocument doc = (StyledDocument) getBaseDocument(testSourceFile);
        assertNotNull(doc);
        FileObject fo = CndFileUtils.toFileObject(testSourceFile);
        assertNotNull(fo);
        DataObject dao = DataObject.find(fo);
        assertNotNull(dao);
        CsmFile csmFile = getCsmFile(testSourceFile);
        assertNotNull(csmFile);
        List<BaseAnnotation> annotations = new ArrayList<BaseAnnotation>();
        ComputeAnnotations.getInstance(csmFile, doc, new AtomicBoolean()).computeAnnotations(annotations);
        Collections.sort(annotations, new Comparator<BaseAnnotation>() {
            @Override
            public int compare(BaseAnnotation o1, BaseAnnotation o2) {
                return o1.getPosition().getOffset() - o2.getPosition().getOffset();
            }
        });

        String goldenFileName = sourceFileName + refPostfix;
        String dataFileName = sourceFileName + ".dat";
        File workDir = getWorkDir();
        File output = new File(workDir, dataFileName); //NOI18N
        PrintStream streamOut = new PrintStream(output);
        dumpAnnotations(annotations, doc, streamOut, patternString);
        streamOut.close();

        File goldenDataFile = getGoldenFile(goldenFileName);
        if (!goldenDataFile.exists()) {
            fail("No golden file " + goldenDataFile.getAbsolutePath() + "\n to check with output file " + output.getAbsolutePath());
        }
        if (CndCoreTestUtils.diff(output, goldenDataFile, null)) {
            System.err.printf("---------- Annotations dump for failed %s.%s ----------\n", getClass().getSimpleName(), getName());
            dumpAnnotations(annotations, doc, System.err, patternString);
            System.err.println();
            // copy golden
            File goldenCopyFile = new File(workDir, goldenFileName + ".golden");
            CndCoreTestUtils.copyToWorkDir(goldenDataFile, goldenCopyFile); // NOI18N
            StringBuilder buf = new StringBuilder("OUTPUT Difference between diff " + output + " " + goldenCopyFile); // NOI18N
            File diffErrorFile = new File(output.getAbsolutePath() + ".diff"); // NOI18N
            CndCoreTestUtils.diff(output, goldenDataFile, diffErrorFile);
            showDiff(diffErrorFile, buf);
            fail(buf.toString());
        }
    }

    private void dumpAnnotations(Collection<BaseAnnotation> annotations, StyledDocument doc, PrintStream ps, String patternString) {
        Pattern pattern = (patternString == null) ? null : Pattern.compile(patternString);
        for (BaseAnnotation anno : annotations) {
            CharSequence sb = anno.debugDump();
            if (pattern == null || pattern.matcher(sb).matches()) {
                ps.printf("%s\n", sb);
            }
        }
    }
}
