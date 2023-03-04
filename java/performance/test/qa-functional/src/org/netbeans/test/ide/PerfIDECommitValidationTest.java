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

package org.netbeans.test.ide;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;

/**
 * Overall sanity check suite for IDE before commit.<br>
 * Look at IDEValidation.java for test specification and implementation.
 *
 * @author Jiri Skrivanek, mrkam@netbeans.org
 */
public class PerfIDECommitValidationTest extends PerfIDEValidation {

    /** Need to be defined because of JUnit */
    public PerfIDECommitValidationTest(String name) {
        super(name);
    }
    
    public static Test suite() throws IOException {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(
            PerfIDECommitValidationTest.class
        ).clusters(".*").enableModules(".*").honorAutoloadEager(true)
        .failOnException(Level.WARNING)
        .failOnMessage(Level.SEVERE);
        
        
        PerfCountingSecurityManager.initWrites();
        
        /* too easy to break:
        conf = conf.addTest("testReflectionUsage");
         */
        conf = conf.addTest("testWriteAccess");
        //conf = conf.addTest("testInitGC");
        conf = conf.addTest("testMainMenu");
        conf = conf.addTest("testHelp");
        conf = conf.addTest("testOptions");
        conf = conf.addTest("testNewProject");
        conf = conf.addTest("testShortcuts"); // sample project must exist before testShortcuts
        conf = conf.addTest("testNewFile");
        conf = conf.addTest("testProjectsView");
        conf = conf.addTest("testFilesView");
        conf = conf.addTest("testEditor");
        conf = conf.addTest("testBuildAndRun");
        conf = conf.addTest("testDebugging");
        //conf = conf.addTest("testPlugins"); //not in commit suite because it needs net connectivity
        //conf = conf.addTest("testJUnit");  //needs JUnit installed in testPlugins
        conf = conf.addTest("testXML");
        conf = conf.addTest("testDb");
        conf = conf.addTest("testWindowSystem");
//        conf = conf.addTest("testGCDocuments");
//        conf = conf.addTest("testGCProjects");
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(conf));
        suite.addTest(new PerfIDECommitValidationTest("testPostRunCheck"));
        return suite;
    }

    public void testPostRunCheck() throws Exception {
        String ud = System.getProperty("netbeans.user");
        assertNotNull("User dir is provided", ud);

        File loaders = new File(new File(new File(ud), "config"), "loaders.ser");
        if (loaders.exists()) {
            fail("loaders.ser file shall not be created, as loaders shall now be " +
                "defined using layers:\n" + loaders);
        }
    }
}
