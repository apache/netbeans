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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
            J2eeModuleProvider projectModule = (J2eeModuleProvider) l.lookup(J2eeModuleProvider.class);
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
