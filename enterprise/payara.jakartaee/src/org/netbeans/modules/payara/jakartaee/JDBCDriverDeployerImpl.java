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

package org.netbeans.modules.payara.jakartaee;

import java.io.File;
import java.util.List;
import java.util.Set;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.payara.tooling.TaskEvent;
import org.netbeans.modules.payara.tooling.TaskState;
import org.netbeans.modules.payara.eecommon.api.JDBCDriverDeployHelper;
import org.netbeans.modules.payara.jakartaee.ide.MonitorProgressObject;
import org.netbeans.modules.payara.spi.PayaraModule;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.plugins.spi.JDBCDriverDeployer;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.openide.util.RequestProcessor;

public class JDBCDriverDeployerImpl implements JDBCDriverDeployer {

    final private File driverLoc;
    final private PayaraModule commonSupport;
    final private boolean isLocal;
    private Hk2DeploymentManager dm;

    public JDBCDriverDeployerImpl(Hk2DeploymentManager dm, OptionalDeploymentManagerFactory odmf) {
        this.dm = dm;
        commonSupport = dm.getCommonServerSupport();
        String domainDir = commonSupport.getInstanceProperties().get(PayaraModule.DOMAINS_FOLDER_ATTR);
        if (null == domainDir || domainDir.trim().length() < 1) {
            isLocal = false;
        } else {
            isLocal = true;
        }        
        String domain = commonSupport.getInstanceProperties().get(PayaraModule.DOMAIN_NAME_ATTR);
        driverLoc = new File(domainDir + File.separator + domain + File.separator + "lib");
    }

    @Override
    public boolean supportsDeployJDBCDrivers(Target target) {
        boolean supported = isLocal;
        // todo -- allow the user to turn this deployment operation OFF.
        if (supported) {
            supported = Boolean.parseBoolean(commonSupport.getInstanceProperties().get(PayaraModule.DRIVER_DEPLOY_FLAG));
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
        String installLoc = commonSupport.getInstanceProperties().get(PayaraModule.PAYARA_FOLDER_ATTR);
        File installLib = new File (installLoc + File.separator + "lib"); //NOI18N
        File[] locs = {driverLoc, installLib};
        return locs;
    }
}
