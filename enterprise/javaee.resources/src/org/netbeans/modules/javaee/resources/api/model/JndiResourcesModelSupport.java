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
package org.netbeans.modules.javaee.resources.api.model;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;

/**
 * Helper class for getting JndiResources model related data.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public final class JndiResourcesModelSupport {

    private static final Logger LOGGER = Logger.getLogger(JndiResourcesModelSupport.class.getName());

    private JndiResourcesModelSupport() {
    }

    public static final Map<Project, WeakReference<MetadataModel<JndiResourcesModel>>> MODELS =
            new WeakHashMap<Project, WeakReference<MetadataModel<JndiResourcesModel>>>();

    /**
     * Return JndiResourcesModel for current project.
     * @return JndiResourcesModel
     */
    public static MetadataModel<JndiResourcesModel> getModel(Project project) {
        WeakReference<MetadataModel<JndiResourcesModel>> reference = MODELS.get(project);
        MetadataModel<JndiResourcesModel> metadataModel = null;
        if (reference != null) {
            metadataModel = reference.get();
        }
        if (metadataModel == null) {
            JndiResourcesModelUnit modelUnit = getModelUnit(project);
            LOGGER.log(Level.FINE, "Metadata model not found in cache for model unit: {0}, reference: {1}",new Object[]{modelUnit, reference});
            if (modelUnit == null) {
                return null;
            }
            metadataModel = JndiResourcesModelFactory.createMetaModel(modelUnit);
            reference = new WeakReference<MetadataModel<JndiResourcesModel>>(metadataModel);
            MODELS.put(project, reference);
        }
        return metadataModel;
    }

    /**
     * Returns JndiResourcesModelUnit for current project.
     * @return JndiResourcesModelUnit holding all ClassPaths
     */
    private static JndiResourcesModelUnit getModelUnit(Project project) {
        if (project == null) {
            return null;
        }
        ClassPath boot = getClassPath(project, ClassPath.BOOT);
        ClassPath compile = getClassPath(project, ClassPath.COMPILE);
        ClassPath src = getClassPath(project, ClassPath.SOURCE);
        return JndiResourcesModelUnit.create(boot, compile, src);
    }

    /**
     * Returns merged classpath for given type.
     * @param type a classpath type such as {@link ClassPath#COMPILE}
     * @return generated read-only project's classpath of given type
     */
    private static ClassPath getClassPath(Project project, String type) {
        ClassPathProvider provider = project.getLookup().lookup(ClassPathProvider.class);
        if (provider == null) {
            return null;
        }

        Sources sources = ProjectUtils.getSources(project);
        if (sources == null) {
            return null;
        }

        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        ClassPath[] paths = new ClassPath[sourceGroups.length];
        int i = 0;
        for (SourceGroup sourceGroup : sourceGroups) {
            FileObject rootFolder = sourceGroup.getRootFolder();
            paths[i] = provider.findClassPath(rootFolder, type);
            i++;
        }
        return ClassPathSupport.createProxyClassPath(paths);
    }

}
