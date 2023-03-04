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

package org.netbeans.modules.gradle.persistence;

import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.java.api.GradleJavaProject;
import org.netbeans.modules.gradle.java.api.GradleJavaSourceSet;
import static org.netbeans.modules.gradle.java.api.GradleJavaSourceSet.SourceType.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceLocationProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Gradle Implementation of
 * <CODE>org.netbeans.modules.j2ee.persistence.spi.PersistenceLocationProvider</CODE>
 * also implements PropertyChangeListener to watch for changes on the
 * persistence.xml file
 *
 * @author Daniel Mohni
 */
public class PersistenceLocationProviderImpl implements PersistenceLocationProvider, PropertyChangeListener {

    static final String REL_PERSISTENCE = "META-INF/persistence.xml";//NOI18N
    static final String REL_LOCATION = "META-INF";//NOI18N
    private final Project project;
    private FileObject location = null;
    private File persistenceXml = null;
    private boolean initialized = false;

    /**
     * Creates a new instance of PersistenceLocationProviderImpl
     *
     * @param proj reference to the NbGradleProject
     */
    public PersistenceLocationProviderImpl(Project proj) {
        project = proj;
    }

    /**
     * property access to the persistence location
     *
     * @return FileObject representing the location (eg. parent folder) of the
     * persistence.xml file
     */
    @Override
    public FileObject getLocation() {
        initPXmlLocation(false);
        return location;
    }

    /**
     * creates a new persistence location using the Gradle resource folder ->
     * /src/main/resources/META-INF
     *
     * @return the newly created FileObject the location (eg. parent folder) of
     * the persistence.xml file
     * @throws java.io.IOException if location can not be created
     */
    @Override
    public FileObject createLocation() throws IOException {
        FileObject ret = null;
        GradleJavaProject gjp = GradleJavaProject.get(project);
        if (gjp != null && gjp.getMainSourceSet() != null) {
            GradleJavaSourceSet main = gjp.getMainSourceSet();
            File metaInf = main.findResource(REL_LOCATION, false, RESOURCES, JAVA, GROOVY, SCALA);
            if (metaInf == null) {
                if (!main.getResourcesDirs().isEmpty()) {
                    metaInf = new File(main.getResourcesDirs().iterator().next(), REL_LOCATION);
                } else if (!main.getGroovyDirs().isEmpty()) {
                    metaInf = new File(main.getGroovyDirs().iterator().next(), REL_LOCATION);
                } else if (!main.getScalaDirs().isEmpty()) {
                    metaInf = new File(main.getScalaDirs().iterator().next(), REL_LOCATION);
                }
            }
            if (metaInf != null) {
                ret = metaInf.exists() ? FileUtil.toFileObject(metaInf) : FileUtil.createFolder(metaInf);
            }
        }

        return ret;
    }

    /**
     * Protected method used by GradlePersistenceSupport to create a file
     * listener
     *
     * @return property access to the current persistence.xml file
     */
    protected File getPersistenceXml() {
        initPXmlLocation(false);
        return persistenceXml;
    }

    private void initPXmlLocation(boolean forced) {
        if (!initialized || forced) {
            persistenceXml = findPersistenceXml();
            location = persistenceXml != null ? FileUtil.toFileObject(persistenceXml.getParentFile()) : null;
            initialized = true;
        }
    }

    private File findPersistenceXml() {
        File ret = null;

        GradleJavaProject gjp = GradleJavaProject.get(project);
        if (gjp != null && gjp.getMainSourceSet() != null) {
            ret = gjp.getMainSourceSet().findResource(REL_PERSISTENCE, false, RESOURCES, JAVA, GROOVY, SCALA);
            if (ret == null && !gjp.getMainSourceSet().getResourcesDirs().isEmpty()) {
                File resDir = gjp.getMainSourceSet().getResourcesDirs().iterator().next();
                ret = new File(resDir, REL_PERSISTENCE);
            }
        }
        return ret;
    }

    /**
     * watches for creation and deletion of the persistence.xml file
     *
     * @param evt the change event to process
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (GradlePersistenceProvider.PROP_PERSISTENCE.equals(evt.getPropertyName())) {
            initPXmlLocation(true);
        }
    }
}
