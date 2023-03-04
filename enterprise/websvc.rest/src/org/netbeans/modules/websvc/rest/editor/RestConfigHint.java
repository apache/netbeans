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
package org.netbeans.modules.websvc.rest.editor;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;

import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
class RestConfigHint extends BaseRestConfigurationFix  {
    
    private RestConfigHint(Project project , FileObject fileObject , 
            RestConfigurationEditorAwareTaskFactory factory, 
            ClasspathInfo cpInfo, boolean jersey)
    {
        super(project, fileObject, factory, cpInfo );
        isJersey = jersey;
    }
    
    public static List<Fix> getConfigHints(Project project , FileObject fileObject , 
            RestConfigurationEditorAwareTaskFactory factory, ClasspathInfo cpInfo)
    {
        List<Fix> result = new ArrayList<Fix>(2);
        result.add( new RestConfigHint(project, fileObject, factory, cpInfo, false));
        result.add( new RestConfigHint(project, fileObject, factory, cpInfo, true));
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.editor.hints.Fix#getText()
     */
    @Override
    public String getText() {
        if ( isJersey ){
            return NbBundle.getMessage( RestConfigHint.class, "MSG_HintJerseyServlet");    // NOI18N
        }
        else {
            return NbBundle.getMessage( RestConfigHint.class, "MSG_HintApplicationClass");    // NOI18N
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.editor.hints.Fix#implement()
     */
    @Override
    public ChangeInfo implement() throws Exception {
        RestSupport restSupport = getSupport();
        if ( isJersey ){
            restSupport.ensureRestDevelopmentReady(RestSupport.RestConfig.DD);
        } else {
            restSupport.ensureRestDevelopmentReady(RestSupport.RestConfig.IDE);
            // XXX : package and Application class is subject to configure via UI
            SourceGroup[] groups = ProjectUtils.getSources(getProject()).getSourceGroups(
                    JavaProjectConstants.SOURCES_TYPE_JAVA);
            if ( groups.length == 0){
                return null;
            }
            FileObject folder = SourceGroupSupport.getFolderForPackage(groups[0], 
                    "org.netbeans.rest.application.config", true);
            RestUtils.createApplicationConfigClass(restSupport, folder, "ApplicationConfig");   // NOI18N
        }
        
        super.implement();
        return null;
    }

    
    private boolean isJersey;

}
