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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.cnd.test.CndBaseTestSuite;

/**
 * Test that goes forever
 * (Created for the sake of investigation IZ #106124)
 */
public class TestEndless extends CndBaseTestSuite {

    public TestEndless() {
        super("C/C++ Endless Test");
//	this.addTestSuite(TestEndless.Worker.class);
        for (int i = 0; i < 1000; i++) {
            this.addTest(new TestEndless.Worker());
        }
        System.setProperty("cnd.modelimpl.parser.threads", "4");
    }

    public static Test suite() {
        TestSuite suite = new TestEndless();
        return suite;
    }

    public static class Worker extends TraceModelTestBase {

        public Worker() {
            super("testEndless");
        }

        @Override
        protected void setUp() throws Exception {
            System.setProperty("parser.report.errors", "true");
            super.setUp();
        }

        @Override
        protected void postSetUp() {
            // init flags needed for file model tests
            getTraceModel().setDumpModel(true);
            getTraceModel().setDumpPPState(true);
        }

        public void testEndless() throws Exception {
//	    int pass = 0;
//	    do {
//		//System.out.printf("Pass %d\n", ++pass);
//		justParse(++pass); // NOI18N
//	    }
//	    while( ! new File("/tmp/stop-endless-test").exists());
            for (int pass = 0; pass < 10; pass++) {
                justParse(pass); // NOI18N
            }
        }

        private void justParse(int pass) throws Exception {
            String source = "file_" + pass;
            File workDir = getWorkDir();
            workDir.mkdirs();
            File testFile = new File(workDir, source);
            writeSource(testFile, pass);
            File output = new File(workDir, source + ".dat");
            PrintStream streamOut = new PrintStream(output);
            PrintStream oldOut = System.out;
            File error = new File(workDir, source + ".err");
            PrintStream streamErr = new PrintStream(error);
            PrintStream oldErr = System.err;

            try {
                System.out.println("testing " + testFile);
                // redirect output and err
                System.setOut(streamOut);
                System.setErr(streamErr);
                performModelTest(testFile, streamOut, streamErr);
            } finally {
                // restore err and out
                streamOut.close();
                streamErr.close();
                System.setOut(oldOut);
                System.setErr(oldErr);
            }
        }

        private void writeSource(File file, int pass) throws FileNotFoundException {
            PrintStream ps = null;
            try {
                //file.getParentFile().mkdirs();
                ps = new PrintStream(file);
                ps.printf("class TheSame {};\n");
                ps.printf("class Different%d {};\n", pass);
            } finally {
                if (ps != null) {
                    ps.close();
                }
            }
        }
    }
}
