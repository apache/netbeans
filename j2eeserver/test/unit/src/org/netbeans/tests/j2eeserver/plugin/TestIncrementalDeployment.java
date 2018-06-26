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
