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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceLocationProvider;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 * Maven2 Implementation of 
 * <CODE>org.netbeans.modules.j2ee.persistence.spi.PersistenceLocationProvider</CODE> 
 * also implements PropertyChangeListener to watch for changes on the persistence.xml file
 * @author Daniel Mohni
 */
public class PersistenceLocationProviderImpl implements PersistenceLocationProvider, PropertyChangeListener {

    static final String DEF_LOCATION = "src/main/resources/META-INF"; //NOI18N
    static final String DEF_PERSISTENCE = "src/main/resources/META-INF/persistence.xml"; //NOI18N
    static final String ALT_PERSISTENCE = "src/main/java/META-INF/persistence.xml"; //NOI18N
    static final String REL_PERSISTENCE = "META-INF/persistence.xml";//NOI18N
    static final String REL_LOCATION = "META-INF";//NOI18N
    private Project project = null;
    private FileObject location = null;
    private File projectDir = null;
    private File persistenceXml = null;
    private boolean initialized = false;
    private final NbMavenProject mproject;

    /**
     * Creates a new instance of PersistenceLocationProviderImpl
     * @param proj reference to the NbMavenProject provider
     */
    public PersistenceLocationProviderImpl(Project proj, NbMavenProject mProj) {
        project = proj;
        mproject = mProj;
        projectDir = FileUtil.toFile(proj.getProjectDirectory());
    }

    /**
     * property access to the persistence location
     * @return FileObject representing the location (eg. parent folder) 
     * of the persistence.xml file
     */
    @Override
    public FileObject getLocation() {
        initPXmlLocation(false);
        return location;
    }

    /**
     * creates a new persistence location using the maven resource folder
     * -> /src/main/resources/META-INF
     * @return the newly created FileObject the location (eg. parent folder) 
     * of the persistence.xml file
     * @throws java.io.IOException if location can not be created
     */
    @Override
    public FileObject createLocation() throws IOException {
        FileObject retVal = null;
        URI[] resources = mproject!=null ? mproject.getResources(false) : null;
        if(resources!=null && resources.length>0) {
            try {
                FileObject res = URLMapper.findFileObject(resources[0].toURL());
                retVal = res.getFileObject(REL_LOCATION);
                if(retVal == null){
                    retVal = res.createFolder(REL_LOCATION);
                }
            } catch (Exception ex) {

            }
        } 
        if(retVal == null) {
            File defaultLocation = FileUtilities.resolveFilePath(
                    projectDir, DEF_LOCATION);

            if (!defaultLocation.exists()) {
                retVal = FileUtil.createFolder(
                        project.getProjectDirectory(), DEF_LOCATION);
            }

            retVal = FileUtil.toFileObject(defaultLocation);

            location = retVal;
        }

        return retVal;
    }

    /**
     * Protected method used by MavenPersistenceSupport to create a file listener
     * @return property access to the current persistence.xml file
     */
    protected File getPersistenceXml() {
        initPXmlLocation(false);
        return persistenceXml;
    }
    
    private void initPXmlLocation(boolean forced) {
        if(!initialized || forced) {
            persistenceXml = findPersistenceXml();
            location = FileUtil.toFileObject(persistenceXml.getParentFile());
            initialized = true;
        }
    }

    /**
     * called by constructor to check if there is a persistence.xml available,
     * it checks in /src/main/java/META-INF and /src/main/resources/META-INF
     * @return File object with the current persistence.xml or null
     */
    private File findPersistenceXml() {
        File retVal = null;

        // check if persistence.xml is in src/main/resources/META-INF
        File defaultLocation = FileUtilities.resolveFilePath(
                projectDir, DEF_PERSISTENCE);

        if (defaultLocation.exists()) {
            retVal = defaultLocation;
        } else {
            // check if persistence.xml is in src/main/java/META-INF
            File altLocation = FileUtilities.resolveFilePath(
                    projectDir, ALT_PERSISTENCE);
            if (altLocation.exists()) {
                retVal = altLocation;
            } else {
                URI[] resources = mproject!=null ? mproject.getResources(false) : null;
                if(resources!=null && resources.length>0) {
                    try {
                        FileObject res = URLMapper.findFileObject(resources[0].toURL());
                        retVal = res!=null ? FileUtilities.resolveFilePath(FileUtil.toFile(res), REL_PERSISTENCE) : null;
                        if(retVal == null || !retVal.exists()) {
                            retVal = defaultLocation;
                        }
                    } catch (MalformedURLException ex) {
                        retVal = defaultLocation;
                    }
                } else {
                    retVal = defaultLocation;
                }
            }
        }

        return retVal;
    }

    /**
     * watches for creation and deletion of the persistence.xml file
     * @param evt the change event to process
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (MavenPersistenceProvider.PROP_PERSISTENCE.equals(evt.getPropertyName())) {
            initPXmlLocation(true);
        }
    }
}
