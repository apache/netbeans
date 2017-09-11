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
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
/*
 * Contributor(s): Daniel Mohni
 */
package org.netbeans.modules.maven.persistence;

import org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceLocationProvider;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeImplementation;
import org.netbeans.modules.j2ee.persistence.spi.support.EntityMappingsMetadataModelHelper;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Maven2 Implementation of 
 * <CODE>org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeImplementation</CODE> 
 * @author Daniel Mohni
 */
public class PersistenceScopeImpl implements PersistenceScopeImplementation
{
    private PersistenceLocationProvider  locationProvider  = null;
    private final EntityMappingsMetadataModelHelper modelHelper;
    private ProjectSourcesClassPathProvider cpProvider;
    private ClassPath projectSourcesClassPath;
    
    
    /**
     * Creates a new instance of PersistenceScopeImpl
     * @param locProvider the PersistenceLocationProvider instance to use for lookups
     * @param cpProvider the PersistenceClasspathProvider instance to use for lookups
     */
    public PersistenceScopeImpl(PersistenceLocationProvider locProvider,
            ProjectSourcesClassPathProvider imp)
    {
        this.locationProvider  = locProvider;
        cpProvider = imp;
        modelHelper = createEntityMappingsHelper();
    }
    
    /**
     * property access to the project's persistence.xml
     * @return the persistence.xml file used in this project or null
     */
    @Override
    public FileObject getPersistenceXml()
    {
        FileObject location = locationProvider.getLocation();
        if (location == null)
        {
            return null;
        }
        return location.getFileObject("persistence.xml"); // NOI18N
        
    }
    
    /**
     * property access to the persistence project classpath
     * @return the classpath provided by the PersistenceClasspathProvider
     */
    @Override
    public ClassPath getClassPath()
    {
        return getProjectSourcesClassPath();
    }
    
    private ClassPath getProjectSourcesClassPath() {
        synchronized (this) {
            if (projectSourcesClassPath == null) {
                projectSourcesClassPath = ClassPathSupport.createProxyClassPath(new ClassPath[] {
                    cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE),
                    cpProvider.getProjectSourcesClassPath(ClassPath.COMPILE),
                });
            }
            return projectSourcesClassPath;
        }
    }
    

    @Override
    public MetadataModel<EntityMappingsMetadata> getEntityMappingsModel(String persistenceUnitName) {
        MetadataModel<EntityMappingsMetadata> metadataModel = modelHelper.getEntityMappingsModel(persistenceUnitName);
        if (metadataModel == null) {
            FileObject puFo = getPersistenceXml();
            if (puFo != null) {
                modelHelper.changePersistenceXml(FileUtil.toFile(puFo));
                metadataModel = modelHelper.getEntityMappingsModel(persistenceUnitName);
            }
        }
        return metadataModel;
    }
    
    private EntityMappingsMetadataModelHelper createEntityMappingsHelper() {
        return EntityMappingsMetadataModelHelper.create(
            cpProvider.getProjectSourcesClassPath(ClassPath.BOOT),
            cpProvider.getProjectSourcesClassPath(ClassPath.COMPILE),
            cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE));
    }
    
    
}
