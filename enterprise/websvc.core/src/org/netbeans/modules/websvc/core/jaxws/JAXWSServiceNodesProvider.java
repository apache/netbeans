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
import org.netbeans.modules.websvc.core.ServiceNodesProvider;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.jaxws.api.JAXWSView;
import org.openide.nodes.Node;
/**
 *
 * @author rico
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.core.ServiceNodesProvider.class)
public class JAXWSServiceNodesProvider implements ServiceNodesProvider{
    
    public JAXWSServiceNodesProvider() {
    }
    
    public Node[] getServiceNodes(Project project) {
        JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
        if(jaxWsSupport != null){
            if (jaxWsSupport.getServices().size() >0) {
                Node servicesNode = JAXWSView.getJAXWSView().createJAXWSView(project);
                if (servicesNode!=null) {
                    return servicesNode.getChildren().getNodes();
                }
            }
        }
        return null;
    }
}
