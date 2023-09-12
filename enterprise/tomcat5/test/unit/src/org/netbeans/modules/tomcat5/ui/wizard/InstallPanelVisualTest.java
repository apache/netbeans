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

package org.netbeans.modules.tomcat5.ui.wizard;

import java.io.File;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.tomcat5.deploy.TomcatManager;
import org.netbeans.modules.tomcat5.deploy.TomcatManager.TomcatVersion;

/**
 *
 * @author sherold
 */
public class InstallPanelVisualTest extends NbTestCase {

    private File datadir;

    public InstallPanelVisualTest(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new InstallPanelVisualTest("testIsServerXmlValid"));
        return suite;
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp ();
        datadir = getDataDir();
    }
    
    public void testIsServerXmlValid() {
        InstallPanelVisual inst = new InstallPanelVisual();
        for (int i = 0; true; i++) {
            File serverXml = new File(datadir, "conf/valid/server_" + i + ".xml");
            if (!serverXml.exists()) {
                break;
            }
            assertTrue("Tomcat configuration file " + serverXml.getAbsolutePath() + " is supposed to be valid", 
                       inst.isServerXmlValid(serverXml));
        }

        for (int i = 0; true; i++) {
            File serverXml = new File(datadir, "conf/invalid/server_" + i + ".xml");
            if (!serverXml.exists()) {
                break;
            }
            assertFalse("Tomcat configuration file " + serverXml.getAbsolutePath() + " is supposed to be invalid", 
                        inst.isServerXmlValid(serverXml));
        }
    }
    
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
}
