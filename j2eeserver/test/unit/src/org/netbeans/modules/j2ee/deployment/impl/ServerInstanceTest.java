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

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerProgress;
import org.netbeans.tests.j2eeserver.plugin.jsr88.*;
import org.netbeans.modules.j2ee.deployment.impl.ui.*;
import org.openide.filesystems.*;

/**
 *
 * @author nn136682
 */
public class ServerInstanceTest extends ServerRegistryTestBase {

    public ServerInstanceTest(java.lang.String testName) {
        super(testName);
    }
    
    /**
     * Test start instance make sure its running.
     * Test stop instance make sure its stopped.
     */
    public void testStartStopInstance()  throws java.io.IOException {
        // setup
        ServerRegistry registry = ServerRegistry.getInstance();
        String url = "fooservice:testStartStopInstance";
        registry.addInstance(url, "user", "password", "TestInstance", true, false, null);
        ServerInstance instance = registry.getServerInstance(url);
        ServerTarget target = instance.getServerTarget("Target 1");

        //fail("");
    }
    
    /**
     * Test start target, case admin is also target make sure its started.
     * Test stop target.
     */
//    public void testStartStopTarget() throws java.io.IOException {
//        // setup
//        ServerRegistry registry = ServerRegistry.getInstance();
//        String url = "fooservice:testStartStopTarget";
//        registry.addInstance(url, "user", "password", "TestInstance");
//        ServerInstance instance = registry.getServerInstance(url);
//        ServerTarget target = instance.getServerTarget("Target 1");
//        
//        // start target
//        DeployProgressUI ui = new DeployProgressMonitor(false, true);
//        ui.startProgressUI(10);
//        boolean success = instance.startTarget(target.getTarget(), ui);
//        ui.recordWork(10);
//        if (! success)
//            fail("Failed to start 'Target 1'");
//        DepManager dm = (DepManager) instance.getDeploymentManager();
//        if (dm.getState() != DepManager.RUNNING)
//            fail("DepManager is not running after ServerInstance.startTarget() call!");
//        
//        /*
//        // stop target
//        ui = new DeployProgressMonitor(false, true);
//        ui.startProgressUI(10);
//        success = instance._test_stop(target.getTarget(), ui);
//        ui.recordWork(10);
//        if (! success)
//            fail("Failed to stop target 'Target 1'");
//        if (dm.getState() != DepManager.STOPPED)
//            fail("DepManager is not stopped after ServerInstance.stopTarget() call");
//        */
//        // cleanup
//        instance.remove();
//    }
}
