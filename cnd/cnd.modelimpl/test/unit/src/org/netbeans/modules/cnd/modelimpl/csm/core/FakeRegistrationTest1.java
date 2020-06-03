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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.File;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.test.ModelImplBaseTestCase;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelBase;

/**
 * Test for #131967
 */
public class FakeRegistrationTest1 extends ModelImplBaseTestCase  {

    private final static boolean verbose;
    static {
        verbose = Boolean.getBoolean("test.fake.reg.verbose");
        if( verbose ) {
            System.setProperty("cnd.modelimpl.timing", "true");
            System.setProperty("cnd.modelimpl.timing.per.file.flat", "true");
        }
        
        System.setProperty("cnd.modelimpl.parser.threads", "1");
    }    
    
    public FakeRegistrationTest1(String testName) {
        super(testName);
    }
    
    public void testSimple() throws Exception {

        File workDir = getWorkDir();
        
        File sourceFile = new File(workDir, "fake.cc");
        File dummyFile1 = new File(workDir, "dummy1.cc");
        File dummyFile2 = new File(workDir, "dummy2.cc");
        File headerFile = new File(workDir, "fake.h");
        
        writeFile(sourceFile, " #include \"fake.h\"\n BEGIN\n int x;\n void QNAME () {}\n END \n");
        writeFile(headerFile, " #define QNAME Qwe::foo\n  #define BEGIN\n #define END\n");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("#include \"fake.h\"\n");
        }
        writeFile(dummyFile1, sb.toString());
        writeFile(dummyFile2, sb.toString());

        TraceModelBase traceModel = new  TraceModelBase(true);

	traceModel.processArguments(dummyFile1.getAbsolutePath(), sourceFile.getAbsolutePath(), dummyFile2.getAbsolutePath(), headerFile.getAbsolutePath());
        
	ModelImpl model = traceModel.getModel();
	final CsmProject project = traceModel.getProject();
        
        FileImpl csmSource = (FileImpl) project.findFile(sourceFile.getAbsolutePath(), true, false);
        assert csmSource != null;

        FileImpl csmHeader = (FileImpl) project.findFile(headerFile.getAbsolutePath(), true, false);
        assert csmHeader != null;

        csmSource.scheduleParsing(true);
        
        writeFile(headerFile, " #define QNAME foo\n #define BEGIN class C {\n #define END };\n");
        sleep(500);
        csmHeader.markReparseNeeded(true);
        csmSource.markReparseNeeded(true);
        csmSource.scheduleParsing(true);
        
        project.waitParse();
        sleep(500);
        
        assertNoExceptions();
        
        clearWorkDir();
    }

}
