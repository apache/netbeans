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

package org.netbeans.modules.j2ee.deployment.impl;

import junit.framework.*;
import org.netbeans.junit.*;
import java.io.*;

/**
 *
 * @author nn136682
 */
public class TargetServerTest extends NbTestCase {

    public TargetServerTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(TargetServerTest.class);
        //suite.addTest(new TargetServerTest("testDistributeSuccess"));
        //suite.addTest(new TargetServerTest("testDistributeFailed"));
        //suite.addTest(new TargetServerTest("testRedeploySuccess"));
        //suite.addTest(new TargetServerTest("testRedeployFailed"));
        //suite.addTest(new TargetServerTest("testDeployWhenServerDown"));
        //suite.addTest(new TargetServerTest("testDeployWhenStartServerFailed"));
        return suite;
    }

    ServerString server;
    ServerString getSuiteTargetServer() {
        return getTargetServer(null);
    }
    ServerString getTargetServer(String name){
        if (server != null)
            return server;

        ServerRegistry registry = ServerRegistry.getInstance();
        String url = "fooservice:TargetServerTest";
        if (name != null)
            url += "_"+name;
        try {
            registry.addInstance(url, "user", "password", "TestInstance", true, false, null);
        } catch (IOException ioe) { throw new RuntimeException(ioe); }
        
        server = new ServerString(registry.getServerInstance(url).getServerTarget("Target 1"));
        return server;
    }
        
    /** Test of processLastTargetModules method, of class org.netbeans.modules.j2ee.deployment.impl.TargetServer. */
    /*
    public void testDistributeSuccess() {
        System.out.println("testDistributeSuccess");
        
        ServerInstance instance = getSuiteTargetServer().getServerInstance();
        DepManager dm = (DepManager) instance.getDeploymentManager();
        boolean started = instance.start();
        if (! started || dm.getState() != DepManager.RUNNING)
            fail("Failed to start: state="+dm.getState());
        try {Thread.sleep(2000); } catch(Exception e) {}
        TargetModule[] modules = getSuiteDeployTarget().getTargetModules();
        assertTrue(modules == null || modules.length == 0);
        DeploymentTarget dt = getSuiteDeployTarget();
        ServerExecutor.instance().deploy(dt);
        //FIXME: this.assertTrue(dm.hasDistributed(dt.getTargetModules()[0].getId()));
    }
    
    // Precondtion: testDistributeSuccess
    public void testRedeploySuccess() {
        System.out.println("testRedeploySuccess");
        DepManager dm = (DepManager) getSuiteTargetServer().getServerInstance().getDeploymentManager();
        //FIXME: this.assertFalse(dm.hasRedeployed(getSuiteDeployTarget().getTargetModules()[0].toString()));
        ServerExecutor.instance().deploy(getSuiteDeployTarget());
        //FIXME: this.assertTrue(dm.hasRedeployed(getSuiteDeployTarget().getTargetModules()[0].toString()));
    }
    */
    
    /*public void testNoChangesRedeploy() {
        System.out.println("testRedeployFailed");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }*/
    
    /*public void testDistributeFailed() {
        System.out.println("testDistributeFailed");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }*/
    
    /*public void testDeployWhenServerDown() {
        System.out.println("testDeployWhenServerDown");
        // Make sure server is down
        
        // deploy or redeploy
        
        // make sure server is up
        fail("The test case is empty.");
    }*/
    
    /*public void testDeployWhenStartServerFailed() {
        System.out.println("testDeployWhenStartServerFailed");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }*/
    
    public void testWebContextRoot() {
        
    }
    
}
