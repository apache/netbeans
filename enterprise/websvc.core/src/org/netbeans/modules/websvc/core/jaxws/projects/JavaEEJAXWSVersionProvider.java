
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

/*
 * WebProjectJAXWSVersionProvider.java
 *
 * Created on March 21, 2007, 3:21 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.core.jaxws.projects;

import org.netbeans.api.project.Project;
import org.netbeans.modules.javaee.specs.support.api.JaxWs;
import org.netbeans.modules.websvc.api.jaxws.project.JAXWSVersionProvider;
import org.netbeans.modules.websvc.core.WSStackUtils;
import org.netbeans.modules.websvc.wsstack.api.WSStack;
import org.netbeans.spi.project.ProjectServiceProvider;

/**
 *
 * @author rico
 */
@ProjectServiceProvider(service=JAXWSVersionProvider.class, projectType={
    "org-netbeans-modules-web-project",
    "org-netbeans-modules-j2ee-ejbjarproject",
    "org-netbeans-modules-j2ee-clientproject"
})
public class JavaEEJAXWSVersionProvider implements JAXWSVersionProvider{
    
    private Project project;
    /** Creates a new instance of WebProjectJAXWSVersionProvider */
    public JavaEEJAXWSVersionProvider(Project project) {
        this.project = project;
    }
    
    public String getJAXWSVersion() {
        WSStackUtils stackUtils = new WSStackUtils(project);
        WSStack wsStack = stackUtils.getWsStack(JaxWs.class);
        return wsStack == null ? null:wsStack.getVersion().toString();
    }
}
