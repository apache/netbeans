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
    public static final String IP_REGION_CODE = "region-code"; // NOI18N
    
    @Override
    public boolean handlesURI(String string) {
        return string.startsWith(AMAZON_URI);
    }

    @Override
    public DeploymentManager getDeploymentManager(String uri, String username,
            String password) throws DeploymentManagerCreationException {
        InstanceProperties props = InstanceProperties.getInstanceProperties(uri);
        return new AmazonDeploymentManager(props.getProperty(IP_APPLICATION_NAME), props.getProperty(IP_ENVIRONMENT_ID), 
                props.getProperty(IP_KEY_ID), props.getProperty(IP_KEY), props.getProperty(IP_CONTAINER_TYPE), props.getProperty(IP_REGION_URL),
                props.getProperty(IP_REGION_CODE));
    }

    @Override
    public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
        InstanceProperties props = InstanceProperties.getInstanceProperties(uri);
        return new AmazonDeploymentManager(props.getProperty(IP_APPLICATION_NAME), props.getProperty(IP_ENVIRONMENT_ID), 
                props.getProperty(IP_KEY_ID), props.getProperty(IP_KEY), props.getProperty(IP_CONTAINER_TYPE), props.getProperty(IP_REGION_URL),
                props.getProperty(IP_REGION_CODE));
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
