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
package org.netbeans.modules.web.jsf.impl.metamodel;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelFactory;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModel;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModelProvider;
import org.netbeans.modules.web.jsf.api.metamodel.ModelUnit;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;

/**
 * @author ads
 * @author Martin Fousek <marfous@netbeans.org>
 */
@ProjectServiceProvider(service = JsfModelProvider.class, projectType = {
    "org-netbeans-modules-maven",
    "org-netbeans-modules-java-j2seproject",
    "org-netbeans-modules-web-project"})
public final class JsfModelProviderImpl implements JsfModelProvider {

    private static final Logger LOG = Logger.getLogger(JsfModelProviderImpl.class.getName());

    //@GuardedBy(this)
    private MetadataModel<JsfModel> model = null;

    private final Project project;

    public JsfModelProviderImpl(Project project) {
        this.project = project;
    }

    @Override
    public synchronized MetadataModel<JsfModel> getModel() {
        if (model == null) {
            LOG.log(Level.FINEST, "JsfModel requested and not created yet, initializing it.");
            long startTime = System.currentTimeMillis();
            ModelUnit unit = getUnit(project);
            if (unit == null) {
                return null;
            }
            model = JsfModelProviderImpl.createMetaModel(unit);
            LOG.log(Level.FINEST, "JsfModel created in {0} ms.", (System.currentTimeMillis() - startTime));
        }
        return model;
    }

    public static MetadataModel<JsfModel> createMetaModel(ModelUnit unit) {
        return MetadataModelFactory.createMetadataModel(JsfModelImplementation.create(unit));
    }

    private static ModelUnit getUnit(Project project) {
        if (project == null) {
            return null;
        }
        ClassPath boot = getClassPath(project, ClassPath.BOOT);
        ClassPath compile = getClassPath(project, ClassPath.COMPILE);
        ClassPath src = getClassPath(project, ClassPath.SOURCE);
        return (src == null) ? null : ModelUnit.create(boot, compile, src, project);
    }

    private static ClassPath getClassPath(Project project, String type) {
        ClassPathProvider provider = project.getLookup().lookup(
                ClassPathProvider.class);
        if (provider == null) {
            return null;
        }
        Sources sources = ProjectUtils.getSources(project);
        if (sources == null) {
            return null;
        }
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (SourceGroup sourceGroup : sourceGroups) {
            FileObject rootFolder = sourceGroup.getRootFolder();
            ClassPath path = provider.findClassPath(rootFolder, type);
            // return classpath of the first source group, that is ignore test source roots:
            return ClassPathSupport.createProxyClassPath(path);
        }
        return null;
    }
}
