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

package org.netbeans.modules.cnd.modelimpl.trace;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;

/**
 *
 */
public class FileModelTest3 extends TraceModelTestBase {

    public FileModelTest3(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("cnd.modelimpl.tracemodel.project.name", "DummyProject"); // NOI18N
        System.setProperty("parser.report.errors", "true");
        System.setProperty("antlr.exceptions.hideExpectedTokens", "true");
//        System.setProperty("cnd.modelimpl.trace.registration", "true");
//        System.setProperty("cnd.modelimpl.parser.threads", "1");
        super.setUp();
    }

    @Override
    protected List<Class<?>> getServices() {
        List<Class<?>> list = new ArrayList<>();
        list.add(FortranFileModelTest.FileEncodingQueryImplementationImpl.class);
        list.addAll(super.getServices());
        return list;
    }


    @Override
    protected void postSetUp() {
        // init flags needed for file model tests
        getTraceModel().setDumpModel(true);
        getTraceModel().setDumpPPState(true);
    }

    // it behaved differently on 1-st and subsequent runs
    public void testResolverClassString_01() throws Exception {
        performTest("resolver_class_string.cc"); // NOI18N
    }

    // it behaved differently on 1-st and subsequent runs
    public void testResolverClassString_02() throws Exception {
        performTest("resolver_class_string.cc"); // NOI18N
    }
    
    public void testBug242674() throws Exception {
        performTest("bug242674.cpp"); // NOI18N
    }    
    
    public void testBug242861() throws Exception {
        performTest("bug242861.cpp");
    }
    
    public void testBug243546() throws Exception {
        performTest("bug243546.cpp");
    }
    
    public void testBug248661() throws Exception {
        performTest("bug248661.cpp");
    }
    
    public void testBug249746() throws Exception {
        performTest("bug249746.cpp");
    }
    
    public void testBug250243() throws Exception {
        performTest("bug250243.cpp");
    }
    
    public void testBug250270() throws Exception {
        performTest("bug250270.cpp");
    }
    
    public void testBug250324() throws Exception {
        performTest("bug250324.cpp");
    }
    
    public void testBug250325() throws Exception {
        performTest("bug250325.cpp");
    }
    
    public void testBug251621() throws Exception {
        performTest("bug251621.cpp");
    }
    
    public void testBug252427() throws Exception {
        performTest("bug252427.cpp");
    }
    
    public void testBug252425() throws Exception {
        performTest("bug252425.cpp");
    }
    
    public void testBug252875() throws Exception {
        performTest("bug252875.c");
    }

    public void testBug252875_UTF() throws Exception {
        performTest("bug252875_1.c");
    }

    @Override
    protected Class<?> getTestCaseDataClass() {
        return FileModelTest.class;
    }

    public static class FileEncodingQueryImplementationImpl extends FileEncodingQueryImplementation {

        public FileEncodingQueryImplementationImpl() {
        }

        @Override
        public Charset getEncoding(FileObject file) {
            return Charset.forName("UTF-8");
        }
    }
}
