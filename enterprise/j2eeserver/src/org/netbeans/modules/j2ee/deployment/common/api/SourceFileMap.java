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

package org.netbeans.modules.j2ee.deployment.common.api;

import java.io.File;
import javax.enterprise.deploy.model.DDBean;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.config.J2eeModuleAccessor;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * Extra file mapping service for each j2ee module.  This service cover irregular source to
 * distribution file mapping.  Users of the mapping could update the mapping.  Provider of
 * the mapping has to ensure the mapping persistence.
 *
 * Note: the initial design is for non-static configuration file such as schema file used in
 * CMP mapping, but it could be used to expose any kind of source-to-distribution mapping.
 *
 * @author  nn136682
 */
public abstract class SourceFileMap {
    
    /**
     * Returns the concrete file for the given distribution path.
     * @param distributionPath distribution path for to find source file for.
     */
    public abstract FileObject[] findSourceFile(String distributionPath);

    /**
     * Returns the relative path in distribution of the given concrete source file.
     * @param sourceFile source file for to find the distribution path.
     */
    public abstract File getDistributionPath(FileObject sourceFile);
    
    /**
     * Return source roots this file mapping is operate on.
     */
    public abstract FileObject[] getSourceRoots();

    /**
     * Return context name, typically the J2EE module project name.
     */
    public abstract String getContextName();
    
    /**
     * Returns directory paths to repository of enterprise resource definition files.
     * If the directories pointed to by the returned path does not exists writing user
     * of the method call could attempt to create it.
     */
    public abstract File getEnterpriseResourceDir();
    
    /**
     * Returns directory paths to repository of enterprise resource definition files.
     * If the directories pointed to by the returned path does not exists writing user
     * of the method call could attempt to create it.
     * For a stand-alone J2EE module, the returned list should contain only one path as
     * returned by getEnterpriseResourceDir.
     *
     * For J2EE application module, the list contains resource directory paths of all child modules.
     */
    public abstract File[] getEnterpriseResourceDirs();
    
    /**
     * Add new mapping or update existing mapping of the given distribution path.
     * Provider of the mapping needs to extract and persist the relative path to
     * ensure the mapping is in project sharable data.  The mapping would be
     * used in ensuring that during build time the source file is put at the 
     * right relative path in the distribution content.
     *
     * @param distributionPath file path in the distribution content
     * @param sourceFile souce concrete file object.
     * @return true if added successfully; false if source file is out of this mapping scope.
     */
    public abstract boolean add(String distributionPath, FileObject sourceFile);

    /**
     * Remove mapping for the given distribution path.
     * @param distributionPath file path in the distribution content
     */
    public abstract FileObject remove(String distributionPath);

    /**
     * Returns a source file map for the module, or null if none can be identified.
     *
     * @param source A non-null source file (java, descriptor or dbschema) to establish mapping context.
     * @return SourceFileMap for the project, may return <code>null</code>
     */
    public static final SourceFileMap findSourceMap(FileObject source) {
        Project owner = FileOwnerQuery.getOwner(source);
        if (owner != null) {
            Lookup l = owner.getLookup();
            J2eeModuleProvider projectModule = l.lookup(J2eeModuleProvider.class);
            if (projectModule != null) {
                return projectModule.getSourceFileMap();
            }
        }
        return null;
    }

    /**
     * Returns a source file map for the module, or null if none can be identified.
     *
     * @param j2eeModule module for which the source file map will be returned.
     * @return SourceFileMap for the project, may return <code>null</code>
     */
    public static final SourceFileMap findSourceMap(J2eeModule j2eeModule) {
        Parameters.notNull("j2eeModule", j2eeModule);
        J2eeModuleProvider projectModule = J2eeModuleAccessor.getDefault().getJ2eeModuleProvider(j2eeModule);
        if (projectModule != null) {
            return projectModule.getSourceFileMap();
        }
        return null;
    }
}
