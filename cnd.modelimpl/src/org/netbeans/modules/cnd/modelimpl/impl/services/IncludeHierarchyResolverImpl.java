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

package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.xref.CsmIncludeHierarchyResolver;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceSupport;
import org.netbeans.modules.cnd.modelimpl.accessors.CsmCorePackageAccessor;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.model.xref.CsmIncludeHierarchyResolver.class)
public final class IncludeHierarchyResolverImpl extends CsmIncludeHierarchyResolver {

    /** Creates a new instance of FriendResolverImpl */
    public IncludeHierarchyResolverImpl() {
    }

    @Override
    public Collection<CsmFile> getFiles(CsmFile referencedFile) {
        CsmProject project = referencedFile.getProject();
        if (project instanceof ProjectBase) {
            return getReferences((ProjectBase)project, referencedFile);
        }
        return Collections.<CsmFile>emptyList();
    }

    @Override
    public Collection<CsmFile> getAllFiles(CsmFile referencedFile) {
        CsmProject project = referencedFile.getProject();
        if (project instanceof ProjectBase) {
            return getAllReferences((ProjectBase)project, referencedFile);
        }
        return Collections.<CsmFile>emptyList();
    }

    @Override
    public Collection<CsmReference> getIncludes(CsmFile referencedFile) {
        CsmProject project = referencedFile.getProject();
        if (project instanceof ProjectBase) {
            List<CsmReference> res = new ArrayList<>();
            for (CsmFile file : getReferences((ProjectBase)project, referencedFile)){
                for (CsmInclude include : file.getIncludes()){
                    if (referencedFile.equals(include.getIncludeFile())){
                        res.add(CsmReferenceSupport.createObjectReference(include.getIncludeFile(), include));
                    }
                }
            }
            return res;
        }
        return Collections.<CsmReference>emptyList();
    }

    private Collection<CsmFile> getReferences(ProjectBase project, CsmFile referencedFile){
        Set<CsmFile> res = CsmCorePackageAccessor.get().getGraph(project).getInLinks(referencedFile);
        for(ProjectBase dependent : project.getDependentProjects()){
            res.addAll(CsmCorePackageAccessor.get().getGraph(dependent).getInLinks(referencedFile));
        }
        return res;
    }

    private Collection<CsmFile> getAllReferences(ProjectBase project, CsmFile referencedFile){
        Set<CsmFile> res = CsmCorePackageAccessor.get().getGraph(project).getInLinks(referencedFile);
        for(ProjectBase dependent : project.getDependentProjects()){
            res.addAll(CsmCorePackageAccessor.get().getGraph(dependent).getInLinks(referencedFile));
            for(CsmProject lib : dependent.getLibraries()) {
                if (lib instanceof ProjectBase) {
                    res.addAll(CsmCorePackageAccessor.get().getGraph((ProjectBase) lib).getInLinks(referencedFile));
                }
            }
        }
        return res;
    }
    
}
