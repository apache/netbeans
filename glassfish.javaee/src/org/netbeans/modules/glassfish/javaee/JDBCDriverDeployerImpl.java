/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.glassfish.javaee;

import java.io.File;
import java.util.List;
import java.util.Set;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.glassfish.tooling.TaskEvent;
import org.netbeans.modules.glassfish.tooling.TaskState;
import org.netbeans.modules.glassfish.eecommon.api.JDBCDriverDeployHelper;
import org.netbeans.modules.glassfish.javaee.ide.MonitorProgressObject;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.plugins.spi.JDBCDriverDeployer;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.openide.util.RequestProcessor;

public class JDBCDriverDeployerImpl implements JDBCDriverDeployer {

    final private File driverLoc;
    final private GlassfishModule commonSupport;
    final private boolean isLocal;
    private Hk2DeploymentManager dm;

    public JDBCDriverDeployerImpl(Hk2DeploymentManager dm, OptionalDeploymentManagerFactory odmf) {
        this.dm = dm;
        commonSupport = dm.getCommonServerSupport();
        String domainDir = commonSupport.getInstanceProperties().get(GlassfishModule.DOMAINS_FOLDER_ATTR);
        if (null == domainDir || domainDir.trim().length() < 1) {
            isLocal = false;
        } else {
            isLocal = true;
        }        
        String domain = commonSupport.getInstanceProperties().get(GlassfishModule.DOMAIN_NAME_ATTR);
        driverLoc = new File(domainDir + File.separator + domain + File.separator + "lib");
    }

    @Override
    public boolean supportsDeployJDBCDrivers(Target target) {
        boolean supported = isLocal;
        // todo -- allow the user to turn this deployment operation OFF.
        if (supported) {
            supported = Boolean.parseBoolean(commonSupport.getInstanceProperties().get(GlassfishModule.DRIVER_DEPLOY_FLAG));
        }
        return supported;
    }

    @Override
    public ProgressObject deployJDBCDrivers(Target target, Set<Datasource> datasources) {
        List urls = JDBCDriverDeployHelper.getMissingDrivers(getDriverLocations(), datasources);
        final MonitorProgressObject startProgress = new MonitorProgressObject(dm,null);
        ProgressObject retVal = JDBCDriverDeployHelper.getProgressObject(driverLoc, urls);
        if (urls.size() > 0) {
            retVal.addProgressListener(new ProgressListener() {

                @Override
                public void handleProgressEvent(ProgressEvent event) {
                    // todo -- enable when this is ready
                    if (event.getDeploymentStatus().isCompleted()) {
                        commonSupport.restartServer(startProgress);
                    } else {
                        startProgress.fireHandleProgressEvent(event.getDeploymentStatus());
                    }
                }
            });
        } else {
            startProgress.operationStateChanged(TaskState.COMPLETED,
                    TaskEvent.CMD_COMPLETED, "no deployment necessary");
        }
        RequestProcessor.getDefault().post((Runnable) retVal);
        return startProgress; // new JDBCDriverDeployHelper.getProgressObject(dmp.getDriverLocation(), datasources);
    }

    private File[] getDriverLocations(){
        String installLoc = commonSupport.getInstanceProperties().get(GlassfishModule.GLASSFISH_FOLDER_ATTR);
        File installLib = new File (installLoc + File.separator + "lib"); //NOI18N
        File[] locs = {driverLoc, installLib};
        return locs;
    }
}
