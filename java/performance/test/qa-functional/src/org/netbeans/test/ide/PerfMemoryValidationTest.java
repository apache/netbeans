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

import java.util.logging.Level;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;

public class PerfMemoryValidationTest extends PerfIDEValidation {
    /** Need to be defined because of JUnit */
    public PerfMemoryValidationTest(String name) {
        super(name);
    }

//    @Override
//    public void run(TestResult result) {
//        if (!getName().startsWith("testGC")) {
//            result = new TestResult();
//        }
//        super.run(result);
//    }

    protected @Override int timeOut() {
        return 1200000;
    }
    
    public static Test suite() {
        // XXX: supresses warning about jpda debugger using parsing API from AWT thread
        System.setProperty("org.netbeans.modules.parsing.impl.TaskProcessor.level", "OFF");

        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(
            PerfMemoryValidationTest.class
        ).clusters(".*").enableModules(".*").
        honorAutoloadEager(true).
        failOnException(Level.OFF)
        .failOnMessage(Level.OFF);

        //conf = conf.addTest("testWriteAccess");
        //conf = conf.addTest("testReflectionUsage");
        conf = conf.addTest("testInitGC");
        conf = conf.addTest("testMainMenu");
        conf = conf.addTest("testHelp");
        conf = conf.addTest("testOptions");
  //      conf = conf.addTest("testPlugins");
  //      conf = conf.addTest("testJUnit");        
        conf = conf.addTest("testNewJavaProject");
        conf = conf.addTest("testPrepareGC");
        // sample project must exist before testShortcuts
        conf = conf.addTest("testShortcuts");
        conf = conf.addTest("testNewFile");
        conf = conf.addTest("testProjectsView");
        conf = conf.addTest("testFilesView");
        conf = conf.addTest("testEditor");
        conf = conf.addTest("testBuildAndRun");
        conf = conf.addTest("testDebugging");
        conf = conf.addTest("testXML"); 
        conf = conf.addTest("testNewJavaScriptProject");
        
        conf = conf.addTest("testWindowSystem");
//        conf = conf.addTest("testDb");
        conf = conf.addTest("testGCDocuments");
        conf = conf.addTest("testAnalyzeGCDocuments");
        conf = conf.addTest("testGCProjects");
        conf = conf.addTest("testAnalyzeGCProjects");
        // not in commit suite because it needs net connectivity
        return NbModuleSuite.create(conf);
    }
}
