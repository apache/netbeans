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

package org.netbeans.modules.j2ee.genericserver.ide;

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.openide.WizardDescriptor.InstantiatingIterator;

/**
 *
 * @author Martin Adamek
 */
public class GSOptionalDeploymentManagerFactory extends OptionalDeploymentManagerFactory {
    
    // TODO: this is just temporary, to not show this instance in Registry
    // needs maybe option similar to is_it_bundled_tomcat to define visibility
    // current solution is only for EJB Freeform
    public StartServer getStartServer(DeploymentManager dm) {
        return null;//new GSStartServer();
    }

    public IncrementalDeployment getIncrementalDeployment(DeploymentManager dm) {
        return null;
    }

    public FindJSPServlet getFindJSPServlet(DeploymentManager dm) {
        return null;
    }

    // TODO: if returned value is null then this server in not displayed 
    // in Add server instance dialog. InstantiatingIterator should be returned
    // when whole functionality will be implemented. Current solution is only for 
    // EJB freeform project
    public InstantiatingIterator getAddInstanceIterator() {
        return null;//new GSInstantiatingIterator();
    }
    
    
}
