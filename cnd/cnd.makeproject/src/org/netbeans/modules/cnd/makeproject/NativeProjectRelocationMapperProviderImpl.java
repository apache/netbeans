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

package org.netbeans.modules.cnd.makeproject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectRegistry;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.spi.project.NativeProjectRelocationMapperProvider;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.CharSequences;
import org.openide.util.lookup.ServiceProvider;

/**
 * This service works for NativeProject implementation of CND : NativeProjectProvider
 * project1.source_name=/export1/tmp/LLVM33
 * project1.dest_name=/export/home/masha/ssd/llvm/LLVM33
 * project1.source_root=/export1/tmp/LLVM33
 * project1.dest_root=/export/home/masha/ssd/llvm/LLVM33
 */
@ServiceProvider(service = NativeProjectRelocationMapperProvider.class, position = 100)
public class NativeProjectRelocationMapperProviderImpl implements NativeProjectRelocationMapperProvider{
    public static final String NAME = "path_mapper.properties"; //NOI18N
    private static final String PROJECT_TAG = "project";//NOI18N
    private static final String SOURCE_NAME = "source_name";//NOI18N
    private static final String DESTINATION_NAME = "dest_name";//NOI18N
    private static final String SOURCE_ROOT = "source_root";//NOI18N
    private static final String DESTINATION_ROOT = "dest_root";//NOI18N
    private static final HashMap<FileObject, ProjectMapper> projectMappers = new HashMap<>();    

    @Override
    public CharSequence getDestinationPath(NativeProject project, CharSequence sourceFilePath) {
        if (!(project instanceof NativeProjectProvider)) {
            return null;
        }
        /*
        */        
        //here we are
        //what we can have here:
        //1. project is dest project already and SourceFilePath is /export1/tmp/LLVM33
//parse path_mapper.properties file to get map
        FileObject projectDir = CndFileUtils.toFileObject(project.getFileSystem(), project.getProjectRoot());
        ProjectMapper projectMapper = get(projectDir);
        if (projectMapper == null || projectMapper.getDestinationFilePath(sourceFilePath) == null) {
            return sourceFilePath;
        }
        return projectMapper.getDestinationFilePath(sourceFilePath);
    }


    @Override
    public CharSequence getSourceProjectName(NativeProject project) {
        if (!(project instanceof NativeProjectProvider)) {
            return null;
        }        
        //parse path_mapper.properties file to get map
        FileObject projectDir = CndFileUtils.toFileObject(project.getFileSystem(), project.getProjectRoot());
        ProjectMapper projectMapper = get(projectDir);
        if (projectMapper == null || projectMapper.getSourceProjectName() == null) {
            return project.getProjectRoot();
        }        
        //here we are
        return projectMapper.getSourceProjectName();
    }

    @Override
    public NativeProject findDestinationProject(CharSequence sourceProjectName) {      
        //go through all NativeProjects and find the one where source is sourceProjectName
        Collection<NativeProject> openProjects = NativeProjectRegistry.getDefault().getOpenProjects();
        for (NativeProject project : openProjects) {
            CharSequence sourceProjectName1 = getSourceProjectName(project);
            if (sourceProjectName1 != null && sourceProjectName1.equals(sourceProjectName)) {
                return project;
            }
        }
        return null;
        
    }
    
    
    /*package*/ static synchronized  ProjectMapper get(FileObject projectDir) {
        ProjectMapper mapper = projectMappers.get(projectDir);
        if (mapper == null) {
            mapper = ProjectMapper.create(projectDir);
            projectMappers.put(projectDir, mapper);
        }
        return mapper;
    }
    
    /*package*/static class ProjectMapper {
        private CharSequence sourceProjectName;
        private CharSequence destinationProjectName;
        private final HashMap<CharSequence, CharSequence> sourceRoots = 
                new HashMap<>();
        
        static  ProjectMapper create(FileObject projectDir) {
            Properties properties = new Properties();
            final FileObject nbProjectFolder = projectDir.getFileObject(MakeConfiguration.NBPROJECT_FOLDER);
            ProjectMapper mapper = new ProjectMapper();            
            if (nbProjectFolder == null) {  // LaunchersRegistry shouldn't be updated in case the project has been deleted.
                return mapper;
            }
            FileObject publicLaunchers = nbProjectFolder.getFileObject(NAME);
            final FileObject privateNbFolder = projectDir.getFileObject(MakeConfiguration.NBPROJECT_PRIVATE_FOLDER);
            FileObject privateLaunchers = null;
            if (privateNbFolder != null && privateNbFolder.isValid()) {
                privateLaunchers = privateNbFolder.getFileObject(NAME);
            }
            try {
                if (publicLaunchers != null && publicLaunchers.isValid()) {
                    final InputStream inputStream = publicLaunchers.getInputStream();
                    properties.load(inputStream);
                    inputStream.close();
                }
                if (privateLaunchers != null && privateLaunchers.isValid()) {
                    final InputStream inputStream = privateLaunchers.getInputStream();
                    properties.load(inputStream);
                    inputStream.close();
                }
            } catch (IOException ex) {
                //Exceptions.printStackTrace(ex);
            }  
            if (properties.isEmpty()) {
                return mapper;
            }
            
            mapper.load(properties, projectDir);
            return mapper;
        }

        private ProjectMapper() {
        }
        
        void load(Properties properties, FileObject projectDir) {
            try {
                sourceRoots.clear();
                this.sourceProjectName = CharSequences.create(properties.getProperty(PROJECT_TAG + "." + SOURCE_NAME));
                final String destName = properties.getProperty(PROJECT_TAG + "." + DESTINATION_NAME);
                this.destinationProjectName = CharSequences.create(destName == null ?
                        CndFileUtils.getCanonicalPath(projectDir) : destName);
                for (String key : properties.stringPropertyNames()) {
                    if (key.matches(PROJECT_TAG + "\\d*[.]" + SOURCE_ROOT)) { //NOI18N
                        String keyValue = key.substring(0, key.indexOf("." + SOURCE_ROOT)); //NOI18N
                        String sourceRoot = properties.getProperty(key);
                        String destRoot = properties.getProperty(keyValue + "." + DESTINATION_ROOT);
                        addMapping(CharSequences.create(sourceRoot), CharSequences.create(destRoot));
                    }
                }
            } catch (IOException ex) {
                //log an ignore
                //Exceptions.printStackTrace(ex);
            }
        }
        
        CharSequence getSourceProjectName() {
            return sourceProjectName;
        }
        
        CharSequence getDestinationProjectName() {
            return destinationProjectName;
        }        
        
        private void addMapping(CharSequence sourceRoot, CharSequence destRoot) {
            sourceRoots.put(sourceRoot, destRoot);
        }

        CharSequence getDestinationFilePath(CharSequence sourceFilePath) {
            Set<CharSequence> keySet = sourceRoots.keySet();
            for (CharSequence sourceRoot : keySet) {
                //if (sourceFilePath.toString().startsWith(sourceRoot.toString())) {
                if(CharSequenceUtils.startsWith(sourceFilePath, sourceRoot)) {
                    //return sourceFilePath.toString().replace(sourceRoot, destRoot);
                    StringBuilder sb = new StringBuilder(sourceRoots.get(sourceRoot));
                    sb.append(sourceFilePath.subSequence(sourceRoot.length(), sourceFilePath.length()));
                    return sb;
                }
            }                        
            return sourceFilePath;
        }
        
    }
}
