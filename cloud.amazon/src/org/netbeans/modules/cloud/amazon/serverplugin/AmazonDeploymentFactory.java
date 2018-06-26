/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cloud.amazon.serverplugin;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.util.NbBundle;

/**
 *
 */
public class AmazonDeploymentFactory implements DeploymentFactory {

    public static final String AMAZON_URI = "amazon:";  // NOI18N

    // some instance properties:
    public static final String IP_APPLICATION_NAME = "application-name";  // NOI18N
    public static final String IP_ENVIRONMENT_ID = "environment--id";  // NOI18N
    public static final String IP_KEY_ID = "access-key-id";  // NOI18N
    public static final String IP_KEY = "access-key";  // NOI18N
    public static final String IP_CONTAINER_TYPE = "container-type";  // NOI18N
    public static final String IP_REGION_URL = "region-url";  // NOI18N
    
    @Override
    public boolean handlesURI(String string) {
        return string.startsWith(AMAZON_URI);
    }

    @Override
    public DeploymentManager getDeploymentManager(String uri, String username,
            String password) throws DeploymentManagerCreationException {
        InstanceProperties props = InstanceProperties.getInstanceProperties(uri);
        return new AmazonDeploymentManager(props.getProperty(IP_APPLICATION_NAME), props.getProperty(IP_ENVIRONMENT_ID), 
                props.getProperty(IP_KEY_ID), props.getProperty(IP_KEY), props.getProperty(IP_CONTAINER_TYPE), props.getProperty(IP_REGION_URL));
    }

    @Override
    public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
        InstanceProperties props = InstanceProperties.getInstanceProperties(uri);
        return new AmazonDeploymentManager(props.getProperty(IP_APPLICATION_NAME), props.getProperty(IP_ENVIRONMENT_ID), 
                props.getProperty(IP_KEY_ID), props.getProperty(IP_KEY), props.getProperty(IP_CONTAINER_TYPE), props.getProperty(IP_REGION_URL));
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(AmazonDeploymentFactory.class, "AmazonDeploymentFactory.displayName");
    }

    @Override
    public String getProductVersion() {
        return "1.0"; // NOI18N
    }
    
}
