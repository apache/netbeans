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

package org.netbeans.modules.web.project;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.openide.filesystems.FileObject;

import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.websvc.spi.webservices.WebServicesSupportProvider;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.jaxws.spi.JAXWSSupportProvider;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportProvider;

/** Provider object to locate web services support or web service client support
 *  for this web project.
 *
 * @author Peter Williams, Milan Kuchtiak
 */
public class ProjectWebServicesSupportProvider implements WebServicesSupportProvider, 
        JAXWSSupportProvider, WebServicesClientSupportProvider {
	
    public ProjectWebServicesSupportProvider () {
    }

    public WebServicesSupport findWebServicesSupport (FileObject file) {
        Project project = FileOwnerQuery.getOwner (file);
        if (project instanceof WebProject) {
            return ((WebProject) project).getAPIWebServicesSupport();
        }
        return null;
    }

    public WebServicesClientSupport findWebServicesClientSupport (FileObject file) {
        Project project = FileOwnerQuery.getOwner (file);
        if (project instanceof WebProject) {
            return ((WebProject) project).getAPIWebServicesClientSupport();
        }
        return null;
    }

    public JAXWSSupport findJAXWSSupport(FileObject file) {
        Project project = FileOwnerQuery.getOwner (file);
        if (project instanceof WebProject) {
            return ((WebProject) project).getAPIJAXWSSupport();
        }
        return null;
    }
    
    public JAXWSClientSupport findJAXWSClientSupport(FileObject file) {
        Project project = FileOwnerQuery.getOwner(file);
        if (project instanceof WebProject) {
            return ((WebProject) project).getAPIJAXWSClientSupport();
        }
        return null;
    }
	
}
