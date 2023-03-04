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

package org.netbeans.modules.websvc.core.jaxws.projects;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.openide.filesystems.FileObject;

import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportProvider;
import org.netbeans.spi.project.ProjectServiceProvider;


/** Provider object to locate web service client support for j2se project.
 *
 * @author Milan Kuchtiak
 */
@ProjectServiceProvider(service=WebServicesClientSupportProvider.class, projectType="org-netbeans-modules-java-j2seproject")
public class J2SEProjectWSClientSupportProvider implements WebServicesClientSupportProvider {

    public J2SEProjectWSClientSupportProvider () {
    }

    public WebServicesClientSupport findWebServicesClientSupport (FileObject file) {
        Project project = FileOwnerQuery.getOwner (file);        
        return project.getLookup().lookup(WebServicesClientSupport.class);
    }

    public JAXWSClientSupport findJAXWSClientSupport(FileObject file) {        
        Project project = FileOwnerQuery.getOwner (file);
        return project.getLookup().lookup(JAXWSClientSupport.class);
    }
}
