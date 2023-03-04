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

package org.netbeans.modules.websvc.core.client.wizard;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.support.ClientCreator;
import org.netbeans.modules.websvc.spi.support.ClientCreatorProvider;
import org.netbeans.modules.websvc.core.ClientWizardProperties;
import org.netbeans.modules.websvc.core.ServerType;
import org.netbeans.modules.websvc.core.WSStackUtils;
import org.openide.WizardDescriptor;

/**
 *
 * @author Milan Kuchtiak
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.spi.support.ClientCreatorProvider.class)
public class JaxWsClientCreatorProvider implements ClientCreatorProvider {

    public JaxWsClientCreatorProvider() {
    }
    
    public ClientCreator getClientCreator(Project project, WizardDescriptor wiz) {
        String jaxVersion = (String) wiz.getProperty(ClientWizardProperties.JAX_VERSION);
        if (JAXWSClientSupport.getJaxWsClientSupport(project.getProjectDirectory()) != null) {
            if (jaxVersion.equals(ClientWizardProperties.JAX_WS)) {
                return new JaxWsClientCreator(project, wiz);
            }
    //        if (JaxWsUtils.isEjbJavaEE5orHigher(project)) {
    //            return new JaxWsClientCreator(project, wiz);
    //        }

            if (ServerType.JBOSS == WSStackUtils.getServerType(project)) {
                return new JaxWsClientCreator(project, wiz);
            }
        }
        return null;
    }

}
