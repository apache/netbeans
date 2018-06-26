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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.tomcat5.config;

import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfigurationFactory2;
import org.netbeans.modules.tomcat5.TomcatFactory;
import org.netbeans.modules.tomcat5.deploy.TomcatManager;
import org.netbeans.modules.tomcat5.deploy.TomcatManager.TomcatVersion;

/**
 * Tomcat implementation of the ModuleConfigurationFactory.
 * 
 * @author sherold
 */
public class TomcatModuleConfigurationFactory implements ModuleConfigurationFactory2 {
    
    private final TomcatVersion version;

    public TomcatModuleConfigurationFactory() {
        this(null);
    }

    private TomcatModuleConfigurationFactory(TomcatVersion version) {
        this.version = version;
    }
    
    public static TomcatModuleConfigurationFactory create50() {
        return new TomcatModuleConfigurationFactory(TomcatVersion.TOMCAT_50);
    }
    
    public ModuleConfiguration create(J2eeModule j2eeModule) throws ConfigurationException {
        // XXX is there a better value for unknown tomcat ?
        return new TomcatModuleConfiguration(j2eeModule,
                version != null ? version : TomcatVersion.TOMCAT_90, null);
    }

    @Override
    public ModuleConfiguration create(J2eeModule j2eeModule, String instanceUrl) throws ConfigurationException {
        try {
            TomcatManager manager = (TomcatManager) TomcatFactory.getInstance().getDisconnectedDeploymentManager(instanceUrl);
            return new TomcatModuleConfiguration(j2eeModule, manager.getTomcatVersion(), manager.getTomEEVersion());
        } catch (DeploymentManagerCreationException ex) {
            return create(j2eeModule);
        }
    }
}
