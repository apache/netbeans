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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.plugins.api.AppChangeDescriptor;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.tests.j2eeserver.plugin.jsr88.TestDeploymentManager;
import org.netbeans.tests.j2eeserver.plugin.jsr88.TestProgressObject;
import org.netbeans.tests.j2eeserver.plugin.jsr88.TestTargetModuleID;

/**
 *
 * @author  nn136682
 */
public class TestIncrementalDeployment extends IncrementalDeployment {

    private TestDeploymentManager dm;

    private File applicationsDir;

    public TestIncrementalDeployment(DeploymentManager manager) {
        assert manager != null;
        this.dm = (TestDeploymentManager) manager;
    }

    public void setDeploymentManager(DeploymentManager manager) {
        if (manager instanceof TestDeploymentManager) {
            dm = (TestDeploymentManager) manager;
        } else {
            throw new IllegalArgumentException("setDeploymentManager: Invalid manager type");
        }
    }

    File getApplicationsDir() {
        if (applicationsDir != null) {
            return applicationsDir;
        }

        String workDir = dm.getInstanceProperties().getProperty(TestDeploymentManager.WORK_DIR);
        if (workDir == null) {
            workDir = System.getProperty("java.io.tmpdir");
        }
        File userdir = new File(workDir);
        applicationsDir = new File(userdir, "testplugin/applications");
        if (!applicationsDir.exists()) {
            applicationsDir.mkdirs();
        }
        return applicationsDir;
    }

    static Map planFileNames = new HashMap();
    static {
        planFileNames.put(ModuleType.WAR, new String[] {"tpi-web.xml"});
        planFileNames.put(ModuleType.EJB, new String[] {"tpi-ejb-jar.xml"});
        planFileNames.put(ModuleType.EAR, new String[] {"tpi-application.xml"});
    }

    public File getDirectoryForModule (TargetModuleID module) {
        File appDir = new File(getApplicationsDir(), getModuleRelativePath((TestTargetModuleID)module));
        if (! appDir.exists())
            appDir.mkdirs();
        System.out.println("getDirectoryForModule("+module+") returned: "+appDir);
        return appDir;
    }

    String getModuleRelativePath(TestTargetModuleID module) {
        File path;
        if (module.getParent() != null)
            path = new File(module.getParent().getModuleID(), module.getModuleID());
        else
            path = new File(module.getModuleID());
        return path.getPath();
    }

    public String getModuleUrl(TargetModuleID module) {
        return ((TestTargetModuleID)module).getModuleUrl();
    }


    public ProgressObject incrementalDeploy (TargetModuleID module, AppChangeDescriptor changes) {
        return dm.incrementalDeploy(module, changes);
    }

    public boolean canFileDeploy (Target target, J2eeModule deployable) {
        return true;
    }

    public File getDirectoryForNewApplication (Target target, J2eeModule app, ModuleConfiguration configuration) {
        return null;
    }

    public File getDirectoryForNewModule (File appDir, String uri, J2eeModule module, ModuleConfiguration configuration) {
        return null;
    }

    public ProgressObject initialDeploy (Target target, J2eeModule app, ModuleConfiguration configuration, final File dir) {
        File webInf = new File(dir, "WEB-INF");
        final TestProgressObject po;
        if (webInf.exists() && webInf.isDirectory()) {
            po = new TestProgressObject(dm, new Target[] {target}, dir, null, ModuleType.WAR);
        } else {
            po = new TestProgressObject(dm, new Target[] {target}, dir, null);
        }

        Runnable r = new Runnable() {
            public void run() {
                try { Thread.sleep(200); //some latency
                } catch (InterruptedException e) {e.printStackTrace();}

                po.setStatusDistributeRunning("TestPluginDM: distributing "+ dir);

                try { Thread.sleep(500); //super server starting time
                } catch (InterruptedException e) {e.printStackTrace();}

                po.setStatusStartCompleted("TestPluginDM distribute finish");
            }
        };

        (new Thread(r)).start();
        return po;
    }

}
