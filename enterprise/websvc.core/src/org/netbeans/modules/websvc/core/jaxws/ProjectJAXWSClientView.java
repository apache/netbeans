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

package org.netbeans.modules.websvc.core.jaxws;

import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.core.ProjectClientViewProvider;
import org.netbeans.modules.websvc.core.jaxws.nodes.JaxWsClientRootNode;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientViewImpl;
import org.openide.nodes.Node;

/**
 *
 * @author mkuchtiak
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.core.ProjectClientViewProvider.class)
public class ProjectJAXWSClientView implements JAXWSClientViewImpl, ProjectClientViewProvider {
    
    /** Creates a new instance of ProjectJAXWSView */
    public ProjectJAXWSClientView() {
    }

    public Node createJAXWSClientView(Project project) {
        if (project != null) {
            JaxWsModel model = (JaxWsModel) project.getLookup().lookup(JaxWsModel.class);
            
            if (model != null) {
                return new JaxWsClientRootNode(model,project.getProjectDirectory());
            }
        }
        return null;
    }
    
    public Node createClientView(Project project) {
        JAXWSClientSupport support = JAXWSClientSupport.getJaxWsClientSupport(project.getProjectDirectory());
        if (support!=null && support.getServiceClients().size()>0) {
            return createJAXWSClientView(project);
        }
        return null;
    }
    
}
