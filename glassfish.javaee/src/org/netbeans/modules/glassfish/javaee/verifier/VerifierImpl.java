/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2013 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.glassfish.javaee.verifier;

import java.io.File;
import java.io.OutputStream;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.glassfish.common.GlassfishInstance;
import org.netbeans.modules.glassfish.common.ui.JavaSEPlatformPanel;
import org.netbeans.modules.glassfish.common.utils.JavaUtils;
import org.netbeans.modules.glassfish.eecommon.api.VerifierSupport;
import org.netbeans.modules.glassfish.javaee.Hk2DeploymentFactory;
import org.netbeans.modules.glassfish.javaee.Hk2DeploymentManager;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
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
                GlassfishModule gm = hk2dm.getCommonServerSupport();
                GlassFishServer server = gm.getInstance();
                String javaHome = gm.getInstanceProperties()
                        .get(GlassfishModule.JAVA_PLATFORM_ATTR);
                if (javaHome == null || javaHome.length() < 1) {
                    javaHome = JavaUtils.getDefaultJavaHome();
                }
                File javaHomeFile = new File(javaHome);
                if (!JavaUtils.isJavaPlatformSupported(
                        (GlassfishInstance)server, javaHomeFile)) {
                    FileObject javaFO
                            = JavaSEPlatformPanel.selectServerSEPlatform(
                            (GlassfishInstance)server, javaHomeFile);
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

