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

package org.netbeans.tests.j2eeserver.plugin;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.Target;

import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerProgress;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.tests.j2eeserver.plugin.jsr88.TestDeploymentManager;

/**
 *
 * @author  nn136682
 */
public class TestStartServer extends StartServer {

    private TestDeploymentManager dm;

    public TestStartServer(DeploymentManager dm) {
        this.dm = (TestDeploymentManager) dm;
    }
    
    public ServerDebugInfo getDebugInfo(Target target) {
        return null;
    }
    
    public boolean isAlsoTargetServer(Target target) {
        return true;
    }
    
    public boolean isDebuggable(Target target) {
        return false; //target.getName().equals("Target 1");
    }
    
    public boolean isRunning() {
        return dm.getState() == TestDeploymentManager.RUNNING;
    }
    
    public boolean needsStartForConfigure() {
        return false;
    }
    
    public void setDeploymentManager(DeploymentManager manager) {
        this.dm = (TestDeploymentManager) manager;
    }
    
    public ProgressObject startDebugging(Target target) {
        return dm.createServerProgress();
    }
    
    public ProgressObject startDeploymentManager() {
        final ServerProgress sp = dm.createServerProgress();
        Runnable r = new Runnable() {
            public void run() {
                try { Thread.sleep(500); //latency
                } catch (Exception e) {}
                dm.setState(TestDeploymentManager.STARTING);
                sp.setStatusStartRunning("TestPluginDM: "+dm.getName()+" is starting.");
                try { Thread.sleep(2000); //super server starting time
                } catch (Exception e) {}
                if (dm.getTestBehavior() == TestDeploymentManager.START_FAILED) {
                    dm.setState(TestDeploymentManager.FAILED);
                    sp.setStatusStartFailed("TestPluginDM: "+dm.getName()+" startup failed");
                } else {
                    dm.setState(TestDeploymentManager.RUNNING);
                    sp.setStatusStartCompleted("TestPluginDM "+dm.getName()+" startup finished");
                }
            }
        };
        
        (new Thread(r)).start();
        return sp;
    }
    
    public ProgressObject stopDeploymentManager() {
        final ServerProgress sp = dm.createServerProgress();
        Runnable r = new Runnable() {
            public void run() {
                try { Thread.sleep(500); //latency
                } catch (Exception e) {}
                dm.setState(TestDeploymentManager.STOPPING);
                sp.setStatusStopRunning("TestPluginDM is preparing to stop "+dm.getName()+"...");
                try { Thread.sleep(2000); //super server stop time
                } catch (Exception e) {}
                if (dm.getTestBehavior() == TestDeploymentManager.STOP_FAILED) {
                    dm.setState(TestDeploymentManager.FAILED);
                    sp.setStatusStopFailed("TestPluginDM stop "+dm.getName()+" failed");
                } else {
                    dm.setState(TestDeploymentManager.STOPPED);
                    sp.setStatusStopCompleted("TestPluginDM startup "+dm.getName()+" finished");
                }
            }
        };

        (new Thread(r)).start();
        return sp;
    }
    
    public boolean supportsStartDeploymentManager() {
        return true;
    }
    
    public boolean needsStartForAdminConfig() {
        return true;
    }
    
    public boolean needsStartForTargetList() {
        return true;
    }
    
}
