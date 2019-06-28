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

package org.netbeans.modules.payara.jakartaee.verifier;

import java.io.File;
import java.io.OutputStream;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import org.netbeans.modules.payara.tooling.data.PayaraServer;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.payara.common.PayaraInstance;
import org.netbeans.modules.payara.common.ui.JavaSEPlatformPanel;
import org.netbeans.modules.payara.common.utils.JavaUtils;
import org.netbeans.modules.payara.eecommon.api.VerifierSupport;
import org.netbeans.modules.payara.jakartaee.Hk2DeploymentFactory;
import org.netbeans.modules.payara.jakartaee.Hk2DeploymentManager;
import org.netbeans.modules.payara.spi.PayaraModule;
import org.netbeans.modules.j2ee.deployment.common.api.ValidationException;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


public  class VerifierImpl extends org.netbeans.modules.j2ee.deployment.plugins.spi.VerifierSupport {
    
    /** Creates a new instance of VerifierImpl */
    public VerifierImpl() {
    }

    /**
     * Verify the provided target J2EE module or application, including both
     * standard J2EE and platform specific deployment info.  The provided
     * service could include invoking its own specific UI displaying of verification
     * result. In this case, the service could have limited or no output to logger stream.
     *
     * @param target The an archive, directory or file to verify.
     * @param logger Log stream to write verification output to.
     * @exception ValidationException if the target fails the validation.
     */
    @Override
    public void verify(FileObject target, OutputStream logger) throws ValidationException {
        final String jname = FileUtil.toFile(target).getAbsolutePath();
        DeploymentManager dm;
        try {
            dm = getAssociatedSunDM(target);
            if (dm instanceof Hk2DeploymentManager) {
                Hk2DeploymentManager hk2dm = (Hk2DeploymentManager) dm;
                PayaraModule gm = hk2dm.getCommonServerSupport();
                PayaraServer server = gm.getInstance();
                String javaHome = gm.getInstanceProperties()
                        .get(PayaraModule.JAVA_PLATFORM_ATTR);
                if (javaHome == null || javaHome.length() < 1) {
                    javaHome = JavaUtils.getDefaultJavaHome();
                }
                File javaHomeFile = new File(javaHome);
                if (!JavaUtils.isJavaPlatformSupported(
                        (PayaraInstance)server, javaHomeFile)) {
                    FileObject javaFO
                            = JavaSEPlatformPanel.selectServerSEPlatform(
                            (PayaraInstance)server, javaHomeFile);
                    javaHome = FileUtil.toFile(javaFO).getAbsolutePath();
                }
                VerifierSupport.launchVerifier(jname, logger, server, javaHome);
            }
        } catch (DeploymentManagerCreationException ex) {
            ValidationException ve = new ValidationException("Bad DM");
            ve.initCause(ex);
            throw ve;
        }
    }
    
    private DeploymentManager getAssociatedSunDM(FileObject target) throws DeploymentManagerCreationException{
        DeploymentManager dm = null;
        J2eeModuleProvider modProvider = getModuleProvider(target);
        if (modProvider != null){
            InstanceProperties serverName = modProvider.getInstanceProperties();
            dm = Hk2DeploymentFactory.createEe6().getDisconnectedDeploymentManager(serverName.getProperty(InstanceProperties.URL_ATTR));
        }
        return dm;
    }

    private J2eeModuleProvider getModuleProvider(FileObject target){
        J2eeModuleProvider modProvider = null;
        Project holdingProj = FileOwnerQuery.getOwner(target);
        if (holdingProj != null){
            modProvider = (J2eeModuleProvider) holdingProj.getLookup().lookup(J2eeModuleProvider.class);
        }
        return modProvider;
    }

}

