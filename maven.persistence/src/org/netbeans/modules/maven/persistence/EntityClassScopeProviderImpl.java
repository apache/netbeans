/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.persistence;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.spi.EntityClassScopeFactory;
import org.netbeans.modules.j2ee.persistence.spi.EntityClassScopeImplementation;
import org.netbeans.modules.j2ee.persistence.spi.EntityClassScopeProvider;
import org.netbeans.modules.j2ee.persistence.spi.support.EntityMappingsMetadataModelHelper;
import org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mkleint
 */
@ProjectServiceProvider(service=EntityClassScopeProvider.class, projectType="org-netbeans-modules-maven")
public class EntityClassScopeProviderImpl implements EntityClassScopeProvider {
    
    Project project;
    EntityMappingsMetadataModelHelper helper;
    EntityClassScope scope;
    
    public EntityClassScopeProviderImpl(Project prj) {
        project = prj;
    }
    
    private synchronized EntityMappingsMetadataModelHelper getHelper() {
        if (helper == null) {
            ProjectSourcesClassPathProvider cp = project.getLookup().lookup(ProjectSourcesClassPathProvider.class);
            helper = EntityMappingsMetadataModelHelper.create(
                    cp.getProjectSourcesClassPath(ClassPath.BOOT),
                    cp.getProjectSourcesClassPath(ClassPath.COMPILE),
                    cp.getProjectSourcesClassPath(ClassPath.SOURCE));
        }
        return helper;
    }
    
    
    @Override
    public synchronized EntityClassScope findEntityClassScope(FileObject fo) {
        if (scope == null) {
            scope = EntityClassScopeFactory.createEntityClassScope(new ScopeImpl());
        }
        return scope;
    }

    private class ScopeImpl implements EntityClassScopeImplementation {

        @Override
        public MetadataModel<EntityMappingsMetadata> getEntityMappingsModel(boolean withDeps) {
            return getHelper().getDefaultEntityMappingsModel(withDeps);
        }

    }


}
