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
package org.netbeans.modules.java.source;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.tools.Diagnostic;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lahvac
 */
public class PostFlowAnalysisTest extends NbTestCase {
    
    public PostFlowAnalysisTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
    }

    public void test225887() throws Exception {
        performErrorsCorrectTest("package test; public class Test implements I { public void test() { I.super.test(); } } interface I { public default void test() { } }",
                                 "1.8");
    }
    
    private void performErrorsCorrectTest(String code, String sourceLevel, String... errors) throws Exception {
        prepareTest();

        FileObject src = FileUtil.createData(sourceRoot, "test/Test.java");

        TestUtilities.copyStringToFile(src, code);
        SourceUtilsTestUtil.setSourceLevel(src, sourceLevel);
        JavaSource javaSource = JavaSource.forFileObject(src);
        final Set<String> actual = new HashSet<String>();
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(Phase.RESOLVED);
                for (Diagnostic<?> d : controller.getDiagnostics()) {
                    actual.add(d.getPosition() + ":" + d.getCode());
                }
            }
        }, true);
        
        assertEquals(new HashSet<String>(Arrays.asList(errors)), actual);
    }

    private FileObject sourceRoot;

    private void prepareTest() throws Exception {
        File work = getWorkDir();
        FileObject workFO = FileUtil.toFileObject(work);

        assertNotNull(workFO);

        sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");

        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);
    }
    
    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }
}
