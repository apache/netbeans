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

package org.netbeans.modules.websvc.rest.codegen;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.codegen.model.EntityClassInfo;
import org.netbeans.modules.websvc.rest.support.PersistenceHelper;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.netbeans.modules.websvc.rest.wizard.Util;
import org.openide.filesystems.FileObject;

/**
 *
 * @author ads
 */
public class JavaEE6EntityResourcesGenerator extends EntityResourcesGenerator {
    
    @Override
    public Set<FileObject> generate(ProgressHandle pHandle) throws IOException {
        if (pHandle != null) {
            initProgressReporting(pHandle);
        }

        createFolders( false);

        //Make necessary changes to the persistence.xml
        new PersistenceHelper(getProject()).configure(getModel().getBuilder().
                getAllEntityNames(),!RestUtils.hasJTASupport(getProject()));
        
        Set<String> entities = new HashSet<String>();
        for (EntityClassInfo info : getModel().getEntityInfos()) {
            String entity = info.getEntityFqn();
            entities.add( entity );
            Util.modifyEntity( entity , getProject());
        }
        
        FileObject targetResourceFolder = null;
        SourceGroup[] sourceGroups = SourceGroupSupport.getJavaSourceGroups(getProject());
        SourceGroup targetSourceGroup = SourceGroupSupport.findSourceGroupForFile(
                sourceGroups, getTargetFolder());
        if (targetSourceGroup != null) {
            targetResourceFolder = SourceGroupSupport.getFolderForPackage(
                    targetSourceGroup, getResourcePackageName(), true);
        }
        if (targetResourceFolder == null) {
            targetResourceFolder = getTargetFolder();
        }
        
        Util.generateRESTFacades(getProject(), entities, getModel(), 
                targetResourceFolder, getResourcePackageName());
        
        finishProgressReporting();

        return new HashSet<FileObject>();
    }

}
