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
package org.netbeans.api.java.source;

import com.sun.source.tree.CompilationUnitTree;
import java.io.File;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lahvac
 */
public class TranslateIdentifierTest extends NbTestCase {
    
    public TranslateIdentifierTest(String name) {
        super(name);
    }
    
    public void test219539() throws Exception {
        runTest("test/Test.java", "package test; @Generate(\"foo\")class Test {}", "package test; @Generate(\"foo\")class Test {}");
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        super.setUp();
    }
    
    private void runTest(String filename, String code, String golden) throws Exception {
        File work = getWorkDir();
        FileObject workFO = FileUtil.toFileObject(work);
        
        assertNotNull(workFO);
        
        FileObject sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");
        FileObject packageRoot = sourceRoot.createFolder("test");
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);
        
        FileObject testSource = FileUtil.createData(packageRoot, filename);
        
        assertNotNull(testSource);
        
        TestUtilities.copyStringToFile(FileUtil.toFile(testSource), code);
        
        JavaSource js = JavaSource.forFileObject(testSource);
        
        assertNotNull(js);
        ModificationResult mr = js.runModificationTask(new Task<WorkingCopy>() {
             @Override public void run(WorkingCopy parameter) throws Exception {
                 parameter.toPhase(Phase.RESOLVED);

                 CompilationUnitTree cut = GeneratorUtilities.get(parameter).importFQNs(parameter.getCompilationUnit());

                 parameter.rewrite(parameter.getCompilationUnit(), cut);
             }
         });
        
        mr.commit();
        
        assertEquals(golden, testSource.asText());
    }
}
