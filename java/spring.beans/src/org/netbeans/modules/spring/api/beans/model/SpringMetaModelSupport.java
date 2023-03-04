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
package org.netbeans.modules.spring.api.beans.model;

import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;

/**
 * Like helper class for getting Spring model related inner, outer data.
 * 
 * @author Martin Fousek <marfous@netbeans.org>
 */
public final class SpringMetaModelSupport {

    private final Project project;
    // copied from WebProjectConstants.TYPE_WEB_INF to save one dependency
    private static final String TYPE_WEB_INF = "web_inf"; //NOI18N

    public SpringMetaModelSupport(Project project) {
        this.project = project;
    }

    /**
     * Return {@link MetadataModel} for current project.
     * @return meta model
     */
    public MetadataModel<SpringModel> getMetaModel() {
        ModelUnit modelUnit = getModelUnit();
        if (modelUnit == null) {
            return null;
        }
        return SpringModelFactory.getMetaModel(modelUnit);
    }

    /**
     * Returns {@link ModelUnit} for current project.
     * @return {@code ModelUnit} holding all {@code ClassPath}es
     */
    public ModelUnit getModelUnit() {
        if (project == null) {
            return null;
        }
        ClassPath boot = getClassPath(ClassPath.BOOT);
        ClassPath compile = getClassPath(ClassPath.COMPILE);
        ClassPath src = getClassPath(ClassPath.SOURCE);
        return ModelUnit.create(boot, compile, src);
    }

    /**
     * Returns merged {@link ClassPath} for given type.
     * @param type a classpath type such as {@link ClassPath#COMPILE}
     * @return generated read-only project's classpath of given type
     */
    public ClassPath getClassPath(String type) {
        ClassPathProvider provider = project.getLookup().lookup(
                ClassPathProvider.class);
        if (provider == null) {
            return null;
        }
        Sources sources = project.getLookup().lookup(Sources.class);
        if (sources == null) {
            return null;
        }
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        SourceGroup[] webGroup = sources.getSourceGroups(TYPE_WEB_INF);
        ClassPath[] paths = new ClassPath[sourceGroups.length + webGroup.length];
        int i = 0;
        for (SourceGroup sourceGroup : sourceGroups) {
            FileObject rootFolder = sourceGroup.getRootFolder();
            paths[i] = provider.findClassPath(rootFolder, type);
            i++;
        }
        for (SourceGroup sourceGroup : webGroup) {
            FileObject rootFolder = sourceGroup.getRootFolder();
            paths[i] = provider.findClassPath(rootFolder, type);
            i++;
        }
        return ClassPathSupport.createProxyClassPath(paths);
    }
}
