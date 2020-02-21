/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
