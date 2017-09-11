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
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceLocationProvider;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeFactory;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeImplementation;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeProvider;
import org.openide.filesystems.FileObject;


/**
 * Maven2 Implementation of 
 * <CODE>org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeProvider</CODE> 
 * @author Daniel Mohni
 */
public class PersistenceScopeProviderImpl implements PersistenceScopeProvider 
{

    private final PersistenceLocationProvider locProvider;
    private final Project project;
    private PersistenceScope persistenceScope = null;

    /**
     * Creates a new instance of PersistenceScopeProviderImpl
     * @param locProvider the PersistenceLocationProvider instance to use for lookups
     * @param cpProvider the PersistenceClasspathProvider instance to use for lookups
     */
    public PersistenceScopeProviderImpl(PersistenceLocationProvider locProvider,
            Project project)
    {
        this.locProvider = locProvider;
        this.project = project;
    }

    /**
     * validated access to the current PersistenceScope instance by checking
     * the presence of a persistence.xml file
     * @param fileObject file to check for persistence scope, not used !
     * @return a valid PersistenceScope instance or null
     */
    @Override public synchronized PersistenceScope findPersistenceScope(FileObject fileObject)
    {
        if (persistenceScope == null) {
            ProjectSourcesClassPathProvider classpath = project.getLookup().lookup(ProjectSourcesClassPathProvider.class);
            PersistenceScopeImplementation persistenceScopeImpl = new PersistenceScopeImpl(locProvider, classpath);
            persistenceScope = PersistenceScopeFactory.createPersistenceScope(persistenceScopeImpl);
        }
        FileObject persistenceXml = persistenceScope.getPersistenceXml();
        if (persistenceXml != null && persistenceXml.isValid()) {
            return persistenceScope;
        }
        return null;
    }
}
