/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.mercurial.remote.versioning.util;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.openide.filesystems.FileObject;

/**
 * clone missed methods from org.netbeans.modules.versioning.util.Utils
 *
 * 
 */
public class Utils {
    private static final Logger LOG = Logger.getLogger(Utils.class.getName());
    
    /**
     * Returns the {@link Project} {@link File} for the given context
     *
     * @param VCSContext
     * @return File of Project Directory
     */
    public static VCSFileProxy getProjectFile(VCSContext context){
        return getProjectFile(getProject(context));
    }
    
    /**
     * Returns the {@link Project} {@link File} for the given {@link Project}
     * 
     * @param project
     * @return 
     */
    public static VCSFileProxy getProjectFile(Project project){
        if (project == null) {
            return null;
        }

        return VCSFileProxy.createFileProxy(project.getProjectDirectory());
    }
    
    /**
     * Returns {@link Project} for the given context
     * 
     * @param context
     * @return 
     */
    public static Project getProject(VCSContext context){
        if (context == null) {
            return null;
        }
        return getProject(context.getRootFiles().toArray(new VCSFileProxy[context.getRootFiles().size()]));
    }
    
    public static Project getProject (VCSFileProxy[] files) {
        for (VCSFileProxy file : files) {
            /* We may be committing a LocallyDeleted file */
            if (!file.exists()) {
                file = file.getParentFile();
            }
            FileObject fo =file.toFileObject();
            if(fo == null) {
                LOG.log(Level.FINE, "Utils.getProjectFile(): No FileObject for {0}", file); // NOI18N
            } else {
                Project p = FileOwnerQuery.getOwner(fo);
                if (p != null) {
                    return p;
                } else {
                    LOG.log(Level.FINE, "Utils.getProjectFile(): No project for {0}", file); // NOI18N
                }
            }
        }
        return null;
    }
    
    /**
     * Returns all root files for the given {@link Project}
     * 
     * @param project
     * @return 
     */
    public static VCSFileProxy[] getProjectRootFiles(Project project){
        if (project == null) {
            return null;
        }
        Set<VCSFileProxy> set = new HashSet<>();

        Sources sources = ProjectUtils.getSources(project);
        SourceGroup [] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        for (int j = 0; j < sourceGroups.length; j++) {
            SourceGroup sourceGroup = sourceGroups[j];
            FileObject srcRootFo = sourceGroup.getRootFolder();
            VCSFileProxy rootFile = VCSFileProxy.createFileProxy(srcRootFo);
            set.add(rootFile);
        }
        return set.toArray(new VCSFileProxy[set.size()]);
    }    
   
}
