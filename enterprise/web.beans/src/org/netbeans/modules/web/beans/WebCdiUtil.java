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
package org.netbeans.modules.web.beans;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.project.Project;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 * @author ads
 *
 */
@ProjectServiceProvider(service={CdiUtil.class}, projectType = {
    "org-netbeans-modules-web-project", "org-netbeans-modules-maven/war"})
public class WebCdiUtil extends CdiUtil {
    
    public WebCdiUtil( Project project ) {
        super(project);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.CdiUtil#getBeansTargetFolder(boolean)
     */
    @Override
    public Collection<FileObject> getBeansTargetFolder( boolean create ) {
        Project project = getProject();
        if ( project == null ){
            return Collections.emptyList();
        }
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm != null && wm.getDocumentBase() != null) {
            FileObject webInf = wm.getWebInf();
            if (webInf == null && create ) {
                try {
                    webInf = FileUtil.createFolder(wm.getDocumentBase(), WEB_INF); 
                } catch (IOException ex) {
                    Logger.getLogger( WebCdiUtil.class.getName() ).log( 
                            Level.WARNING, null, ex );
                }
            }
            return Collections.singleton(webInf);
        } 
        return super.getBeansTargetFolder(create);
    }
    
}
