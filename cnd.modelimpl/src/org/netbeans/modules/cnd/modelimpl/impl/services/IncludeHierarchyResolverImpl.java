/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
