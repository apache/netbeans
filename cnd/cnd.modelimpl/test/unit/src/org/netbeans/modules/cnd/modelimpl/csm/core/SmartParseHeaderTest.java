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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelTestBase;

/**
 * A suite for testing smart header parsing (iz #142107)
 */
public class SmartParseHeaderTest extends TraceModelTestBase {


    public SmartParseHeaderTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("cnd.modelimpl.tracemodel.project.name", "DummyProject"); // NOI18N
        System.setProperty("parser.report.errors", "true");
        System.setProperty("cnd.smart.parse", "true");
        System.setProperty("antlr.exceptions.hideExpectedTokens", "true");
        System.setProperty("cnd.modelimpl.parser.threads", "1");
        System.setProperty("cnd.cache.file.state","false");
//        System.setProperty("cnd.trace.schedule.parsing","true");
//        System.setProperty("cnd.parser.queue.trace","true");
//        System.setProperty("cnd.parser.queue.trace.poll","true");
        ParseStatistics.getInstance().setEnabled(true);
        if (false) {
            Logger logger = ProjectBase.WAIT_PARSE_LOGGER;
            logger.setLevel(Level.ALL);
            TestLogHandler.attach(logger);
        }
        super.setUp();
    }

    @Override
    protected void postSetUp() {
        // init flags needed for file model tests
        getTraceModel().setDumpModel(true);
        getTraceModel().setDumpPPState(true);
        ParseStatistics.getInstance().clear();
    }

    private void assertParseCount(String fileName, int expectedParseCount) throws Exception {
        FileImpl fileImpl = findFile(fileName);
        //assertNotNull("file " + fileName + " not found", fileImpl);
        int actualParseCount = ParseStatistics.getInstance().getParseCount(fileImpl);
        assertEquals("Unexpected parse count for " + fileName, expectedParseCount, actualParseCount);
    }
    
    private void performTrivialTest(String fileToParse, String headerToCheck, int expectedParseCount, int exprectedReparseCount)
        throws Exception {
        performTrivialTest(new String[] { fileToParse }, fileToParse, headerToCheck, expectedParseCount, exprectedReparseCount);
    }

    private void performTrivialTest(String[] filesToParse, String goldensNameBase, 
            String headerToCheck, int expectedParseCount, int exprectedReparseCount)
            throws Exception {
        performTest(filesToParse, goldensNameBase);
        // check current parse count
        assertParseCount(headerToCheck, expectedParseCount);
        if (exprectedReparseCount >= 0) {
            ParseStatistics.getInstance().clear();
            ProjectBase project = getProject();
            // suspend/resume parser queue is needed to have stable results or reparsing counters,
            // although it's not the real situation when work from IDE, but we check only logic correctness
            try {
                ParserQueue.instance().suspend();
                for (int i = 0; i < filesToParse.length; i++) {
                    FileImpl fileImpl = findFile(filesToParse[i]);
                    fileImpl.markReparseNeeded(false);
                    DeepReparsingUtils.fullReparseOnChangedFile(project, fileImpl);
                }
            } finally {
                ParserQueue.instance().resume();
            }
            getProject().waitParse();
            assertParseCount(headerToCheck, exprectedReparseCount);
        }
    }

    public void testSimple_1a() throws Exception {
        performTrivialTest("smart_headers_simple_1a.cc", "smart_headers_simple_1.h", 3, -1);
    }

    public void testSimple_1b() throws Exception {
        performTrivialTest("smart_headers_simple_1b.cc", "smart_headers_simple_1.h", 1, -1);
    }

    public void testSimple_1c() throws Exception {
        performTrivialTest("smart_headers_simple_1c.cc", "smart_headers_simple_1.h", 1, -1);
    }

    public void testSimple_1d() throws Exception {
        performTrivialTest("smart_headers_simple_1d.cc", "smart_headers_simple_1.h", 1, -1);
    }

    public void testSimple_1e() throws Exception {
        performTrivialTest("smart_headers_simple_1e.cc", "smart_headers_simple_1.h", 4, -1);
    }

    public void testSimple_1f() throws Exception {
        performTrivialTest("smart_headers_simple_1f.cc", "smart_headers_simple_1.h", 1, -1);
    }

    public void testSimpleReparse_1a() throws Exception {
        performTrivialTest("smart_headers_simple_1a.cc", "smart_headers_simple_1.h", 3, 3);
    }
    
    public void testSimpleReparse_1b() throws Exception {
        performTrivialTest("smart_headers_simple_1b.cc", "smart_headers_simple_1.h", 1, 1);
    }

    public void testSimpleReparse_1c() throws Exception {
        performTrivialTest("smart_headers_simple_1c.cc", "smart_headers_simple_1.h", 1, 1);
    }

    public void testSimpleReparse_1d() throws Exception {
        performTrivialTest("smart_headers_simple_1d.cc", "smart_headers_simple_1.h", 1, 1);
    }

    public void testSimpleReparse_1e() throws Exception {
        performTrivialTest("smart_headers_simple_1e.cc", "smart_headers_simple_1.h", 4, 4);
    }

    public void testSimpleReparse_1f() throws Exception {
        performTrivialTest("smart_headers_simple_1f.cc", "smart_headers_simple_1.h", 1, 1);
    }

    public void testSimple_1_multy() throws Exception {
        performTrivialTest(new String[] {
                "smart_headers_simple_1a.cc",
                "smart_headers_simple_1b.cc",
                //"smart_headers_simple_1c.cc",
                "smart_headers_simple_1d.cc",
                "smart_headers_simple_1e.cc",
                "smart_headers_simple_1f.cc"
            }, 
            "smart_headers_simple_1_multy", "smart_headers_simple_1.h", 5, -1);
    }

    public void testSimpleReparse_1_multy() throws Exception {
        performTrivialTest(new String[] {
                "smart_headers_simple_1a.cc",
                "smart_headers_simple_1b.cc",
                //"smart_headers_simple_1c.cc",
                "smart_headers_simple_1d.cc",
                "smart_headers_simple_1e.cc",
                "smart_headers_simple_1f.cc"
            }, 
            "smart_headers_simple_1_multy", "smart_headers_simple_1.h", 5, 1);
    }

    public void testMixed_1() throws Exception {
        performTrivialTest(new String[] {
                "mixed_c.c",
                "mixed_cpp.cc",
            }, 
            "mixed_1", "mixed_header.h", 2, 2);
    }

    public void testElifElse() throws Exception {
        performTrivialTest("elif_else_simple.cc", "elif_else_simple.h", 5, 5);
    }

    public void testElifElseModel() throws Exception {
        performTest(new String[] {"elif_else_simple.cc", "elif_else_simple.h"}, "elif_else_simple_model");
    }

    /////////////////////////////////////////////////////////////////////
    // FAILURES
    
    public static class Failed extends TraceModelTestBase {
	
        public Failed(String testName) {
            super(testName);
        }

	@Override
	protected void setUp() throws Exception {
	    System.setProperty("parser.report.errors", "true");
	    super.setUp();
	}
	
        @Override
	protected Class<?> getTestCaseDataClass() {
	    return SmartParseHeaderTest.class;
	}
	
        @Override
	protected void postSetUp() {
	    // init flags needed for file model tests
	    getTraceModel().setDumpModel(true);
	    getTraceModel().setDumpPPState(true);
	}
   }
    
}
