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

package org.netbeans.modules.tomcat5;

import org.netbeans.modules.tomcat5.deploy.TomcatManager;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.tomcat5.util.TestBase;

/**
 *
 * @author Radim Kubacki
 */
public class TomcatManagerTest extends TestBase {
    
    public TomcatManagerTest (String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp () throws Exception {
        super.setUp ();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new TomcatManagerTest("testGetPorts"));
        return suite;
    }
    
    public void testGetPorts() throws Exception {
        clearWorkDir();
        
        String home = getDataDir().getAbsolutePath() + "/server/home0";
        String base = getWorkDir().getAbsolutePath() + "/base_dir";
        
        String url = TomcatFactory.TOMCAT_URI_PREFIX_55;        
        url += "home=" + home + ":base=" + base;
        
        // register the test tomcat instance
        InstanceProperties ip = InstanceProperties.createInstanceProperties(
                url, "", "", "Test Tomcat");
        
        TomcatManager manager = (TomcatManager) TomcatFactory.getInstance().getDeploymentManager(url, null, null);
        
        assertEquals(9999, manager.getServerPort());
        assertEquals(7777, manager.getShutdownPort());
        
        manager.ensureCatalinaBaseReady();
        
        assertEquals(9999, manager.getServerPort());
        assertEquals(7777, manager.getShutdownPort());
        
    }
    
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    public void testIsHigherThanTomcat70() {
        TomcatManager.TomcatVersion tomcatVersion = TomcatManager.TomcatVersion.TOMCAT_70;
        
        assertFalse(tomcatVersion.isAtLeast(TomcatManager.TomcatVersion.TOMCAT_110));
        assertFalse(tomcatVersion.isAtLeast(TomcatManager.TomcatVersion.TOMCAT_101));
        assertFalse(tomcatVersion.isAtLeast(TomcatManager.TomcatVersion.TOMCAT_100));
        assertFalse(tomcatVersion.isAtLeast(TomcatManager.TomcatVersion.TOMCAT_90));
        assertFalse(tomcatVersion.isAtLeast(TomcatManager.TomcatVersion.TOMCAT_80));
        assertTrue(tomcatVersion.isAtLeast(TomcatManager.TomcatVersion.TOMCAT_60));
        assertTrue(tomcatVersion.isAtLeast(TomcatManager.TomcatVersion.TOMCAT_55));
        assertTrue(tomcatVersion.isAtLeast(TomcatManager.TomcatVersion.TOMCAT_50));
        
    }
    
    public void testIsHigherThanTomee70() {
        TomcatManager.TomEEVersion tomEEVersion = TomcatManager.TomEEVersion.TOMEE_70;
        
        assertFalse(tomEEVersion.isAtLeast(TomcatManager.TomEEVersion.TOMEE_90));
        assertFalse(tomEEVersion.isAtLeast(TomcatManager.TomEEVersion.TOMEE_80));
        assertFalse(tomEEVersion.isAtLeast(TomcatManager.TomEEVersion.TOMEE_71));
        assertTrue(tomEEVersion.isAtLeast(TomcatManager.TomEEVersion.TOMEE_70));
        assertTrue(tomEEVersion.isAtLeast(TomcatManager.TomEEVersion.TOMEE_17));
        assertTrue(tomEEVersion.isAtLeast(TomcatManager.TomEEVersion.TOMEE_16));
        assertTrue(tomEEVersion.isAtLeast(TomcatManager.TomEEVersion.TOMEE_15));
        
    }
    
}
