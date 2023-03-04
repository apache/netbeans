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

package org.netbeans.modules.j2ee.genericserver;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Adamek
 */
public class GSDeploymentFactory implements DeploymentFactory {

    public static final String GENERIC_SERVER_PREFIX = "generic"; // NOI18N
    
    private static DeploymentFactory instance;
    
    private static Logger err = Logger.getLogger("org.netbeans.modules.j2ee.genericserver");  // NOI18N
    
    public static synchronized DeploymentFactory create() {
        if (instance == null) {
            if (err.isLoggable(Level.FINE)) {
                err.log(Level.FINE, "Creating Generic Server Factory"); // NOI18N
            }
            instance = new GSDeploymentFactory();
            DeploymentFactoryManager.getInstance().registerDeploymentFactory(instance);
        }
        return instance;
    }
    
    public boolean handlesURI(String str) {
        return str != null && str.startsWith(GENERIC_SERVER_PREFIX);
    }
    
    public DeploymentManager getDeploymentManager(String uri, String uname, String passwd) throws DeploymentManagerCreationException {
        if (!handlesURI(uri)) {
            throw new DeploymentManagerCreationException("Invalid URI:" + uri); // NOI18N
        }
        return new GSDeploymentManager();
    }
    
    public DeploymentManager getDisconnectedDeploymentManager(String str) throws DeploymentManagerCreationException {
        return new GSDeploymentManager();
    }
    
    public String getProductVersion() {
        return "0.1"; // NOI18N
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(GSDeploymentFactory.class, "TXT_DisplayName"); // NOI18N
    }
    
}
