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

/**
 *
 */
public class FileModelNewGrammarTest extends TraceModelTestBase {
    
    public FileModelNewGrammarTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("cnd.modelimpl.tracemodel.project.name", "DummyProject"); // NOI18N
        System.setProperty("parser.report.errors", "true");
        System.setProperty("antlr.exceptions.hideExpectedTokens", "true");
        System.setProperty("cnd.language.flavor.cpp11", "true");
        System.setProperty("cnd.modelimpl.cpp.parser.new.grammar", "true");
        System.setProperty("cnd.modelimpl.parse.headers.with.sources", "true");
        super.setUp();
    }

    @Override
    protected void postSetUp() {
        // init flags needed for file model tests
        getTraceModel().setDumpModel(true);
        getTraceModel().setDumpPPState(true);
    }

    @Override
    protected void postTest(String[] args, Object... params) throws Exception {
        System.setProperty("cnd.language.flavor.cpp11", "false"); 
    }
    
    public void testTypedefEnum() throws Exception {
        performTest("typedefEnum.cpp");
    }
    
    public void testDestructor() throws Exception {
        performTest("destructor.cpp");
    }
    
    public void testConstMethod() throws Exception {
        performTest("constMethod.cpp");
    }
    
    public void testFriendFunction() throws Exception {
        performTest("friendFunction.cpp");
    }
    
    public void testParameters() throws Exception {
        performTest("parameters.cpp");
    }
    
    public void testAccessModifiers() throws Exception {
        performTest("accessModifiers.cpp");
    }
    
    public void testClassForwardScope() throws Exception { 
        performTest("classForwardScope.cpp");
    }
    
    public void testConstructorImportSymbols() throws Exception {
        performTest("constructorImportSymbols.cpp");
    }
    
    public void testClearingImportedSymbols() throws Exception {
        performTest("clearingImportedSymbols.cpp");
    }    
    
    public void testMultipleDeclarativeRegions() throws Exception {
        performTest("multipleDeclarativeRegions.cpp");
    }
    
    public void testNestedClassScope() throws Exception {
        performTest("nestedClassScope.cpp");
    }    
    
    public void testConstructorInitializerListScope() throws Exception {
        performTest("constructorInitializerListScope.cpp");
    }    
    
    public void testBitFields() throws Exception {
        performTest("bitFields.cpp");
    }    

    public void testFunctionParametersInFunctionBody() throws Exception {
        performTest("functionParametersInFunctionBody.cpp");
    }    
    
//    public void testMultipleMemberDeclaration() throws Exception {
//        performTest("typedefEnum.cpp");
//    } 
    
}
