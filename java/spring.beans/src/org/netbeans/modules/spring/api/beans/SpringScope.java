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
package org.netbeans.modules.spring.api.beans;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
import org.netbeans.modules.spring.api.beans.model.SpringModel;
import org.netbeans.modules.spring.api.beans.model.SpringMetaModelSupport;
import org.netbeans.modules.spring.beans.ProjectSpringScopeProvider;
import org.netbeans.modules.spring.beans.SpringConfigModelAccessor;
import org.netbeans.modules.spring.beans.SpringScopeAccessor;
import org.netbeans.modules.spring.beans.model.SpringConfigFileModelManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 * Encapsulates the environment of Spring beans configuration files. It
 * comprises a list of related beans configuration files, and a model
 * which provides access to beans definitions in these files.
 *
 * @author Andrei Badea
 */
public final class SpringScope {

    // This class is also responsible for creating and maintaining
    // single-file models for Spring config files (that is, models that are created
    // for files not included in the config file group). But, in order to make
    // clients' life easier, they can obtain models through SpringConfigModel
    // (which calls back into this class).
    private final ConfigFileManager configFileManager;
    private final SpringConfigFileModelManager fileModelManager = new SpringConfigFileModelManager();
    private MetadataModel<SpringModel> springAnnotationModel = null;

    static {
        SpringScopeAccessor.setDefault(new SpringScopeAccessor() {

            @Override
            public SpringScope createSpringScope(ConfigFileManager configFileManager) {
                return new SpringScope(configFileManager);
            }

            @Override
            public SpringConfigModel getConfigModel(SpringScope scope, FileObject fo) {
                return scope.getConfigModel(fo);
            }
        });
    }

    private SpringScope(ConfigFileManager configFileManager) {
        this.configFileManager = configFileManager;
    }

    /**
     * Finds the Spring scope that contains (or could contain) a given file.
     *
     * @param  fo a file; never null.
     * @return the Spring scope or null.
     */
    public static SpringScope getSpringScope(FileObject fo) {
        Parameters.notNull("fo", fo);
        Project project = FileOwnerQuery.getOwner(fo);
        if (project == null) {
            return null;
        }
        ProjectSpringScopeProvider provider = project.getLookup().lookup(ProjectSpringScopeProvider.class);
        if (provider == null) {
            return null;
        }
        return provider.getSpringScope();
    }
    
    /**
     * Returns the config file groups for this Spring scope.
     *
     * @return the config file group; never null.
     */
    public ConfigFileManager getConfigFileManager() {
        return configFileManager;
    }

    /**
     * Returns the a list of Spring config models for all known configuration
     * file groups as well as for all files not contained in a group.
     *
     * @return the list of models; never null.
     */
    public List<SpringConfigModel> getAllConfigModels() {
        final List<ConfigFileGroup> groups = new ArrayList<ConfigFileGroup>();
        final List<File> files = new ArrayList<File>();
        // Avoid race conditions.
        configFileManager.mutex().readAccess(new Runnable() {
            public void run() {
                groups.addAll(configFileManager.getConfigFileGroups());
                files.addAll(configFileManager.getConfigFiles());
            }
        });
        List<SpringConfigModel> result = new ArrayList<SpringConfigModel>(groups.size());
        Set<File> modelFiles = new HashSet<File>(groups.size() * 2);
        // Create models for all config groups, and then for all known config files
        // not included in a group.
        for (ConfigFileGroup group : groups) {
            result.add(SpringConfigModelAccessor.getDefault().createSpringConfigModel(fileModelManager, group));
            modelFiles.addAll(group.getFiles());
        }
        // Using a TreeSet here in order to give deterministic results.
        Set<File> nonModelFiles = new TreeSet<File>(files);
        nonModelFiles.removeAll(modelFiles);
        for (File file : nonModelFiles) {
            ConfigFileGroup singleFileGroup = ConfigFileGroup.create(Collections.singletonList(file));
            result.add(SpringConfigModelAccessor.getDefault().createSpringConfigModel(fileModelManager, singleFileGroup));

        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns the reference to the {@code MetadataModel<SpringModel>} of Spring
     * annotation support.
     * 
     * @param fo any file inside project; never null.
     * @return {@code MetadataModel<SpringModel>} of annotation model; never null
     */
    public MetadataModel<SpringModel> getSpringAnnotationModel(FileObject fo) {
        if (springAnnotationModel == null) {
            Project project = getSpringProject(fo);
            if (project == null) {
                return null;
            }
            SpringMetaModelSupport metaModelSupport = new SpringMetaModelSupport(project);
            springAnnotationModel = metaModelSupport.getMetaModel();
        }
        return springAnnotationModel;
    }

    /**
     * Returns the model of the beans configuration files for the given file
     * (and any related files, if the files belongs to a
     * {@link ConfigFileGroup config file group}).
     *
     * @return the beans model; never null.
     */
    private SpringConfigModel getConfigModel(FileObject configFO) {
        File configFile = FileUtil.toFile(configFO);
        if (configFile == null) {
            return null;
        }
        // If the file is one contained in a config file group, return
        // the model for that whole config file group.
        SpringConfigModel model = getGroupConfigModel(configFile);
        if (model != null) {
            return model;
        }
        // Otherwise return a single-file model.
        return getFileConfigModel(configFO);
    }

    private SpringConfigModel getGroupConfigModel(File configFile) {
        for (ConfigFileGroup group : configFileManager.getConfigFileGroups()) {
            if (group.containsFile(configFile)) {
                return SpringConfigModelAccessor.getDefault().createSpringConfigModel(fileModelManager, group);
            }
        }
        return null;
    }

    private SpringConfigModel getFileConfigModel(FileObject configFO) {
        File configFile = FileUtil.toFile(configFO);
        if (configFile != null) {
            ConfigFileGroup singleFileGroup = ConfigFileGroup.create(Collections.singletonList(configFile));
            return SpringConfigModelAccessor.getDefault().createSpringConfigModel(fileModelManager, singleFileGroup);
        }
        return null;
    }
    
    private static Project getSpringProject(FileObject fo) {
        Parameters.notNull("fo", fo);
        Project project = FileOwnerQuery.getOwner(fo);
        if (project == null) {
            return null;
        }
        ProjectSpringScopeProvider provider = project.getLookup().lookup(ProjectSpringScopeProvider.class);
        if (provider == null) {
            return null;
        }
        return provider.getProject();
    }
}
