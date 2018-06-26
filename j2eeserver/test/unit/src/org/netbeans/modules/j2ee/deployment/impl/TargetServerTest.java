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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
