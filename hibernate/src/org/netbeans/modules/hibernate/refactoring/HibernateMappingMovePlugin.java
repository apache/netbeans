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
package org.netbeans.modules.hibernate.refactoring;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.hibernate.loaders.mapping.HibernateMappingDataLoader;
import org.netbeans.modules.hibernate.refactoring.HibernateRefactoringUtil.OccurrenceItem;
import org.netbeans.modules.hibernate.service.api.HibernateEnvironment;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Plugin for refactor/move Hibernate mapping files
 * 
 * @author Dongmei Cao
 */
public class HibernateMappingMovePlugin implements RefactoringPlugin {

    private MoveRefactoring refactoring;
    private Project project;
    private String srcRoot;
    private ArrayList<MappingFileData> toBeMovedMappingFiles;

    public HibernateMappingMovePlugin(MoveRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    public Problem preCheck() {
        return null;
    }

    public Problem checkParameters() {
        return null;
    }

    public Problem fastCheckParameters() {

        URL targetURL = refactoring.getTarget().lookup(URL.class);
        if (targetURL == null) {
            // TODO: return a Problem
            return null;
        }

        String targetPackageName = HibernateRefactoringUtil.getPackageName(targetURL);
        if (targetPackageName == null) {
            // TODO: return a Problem
            return null;
        }
        
        // The files to be moved. Can be multiple of them
        toBeMovedMappingFiles = getToBeMovedMappingFileData(targetPackageName);

        // The mapping files in the target package
        ArrayList<MappingFileData> mappingFilesInTargetPkg = getMappingFileDataInTargetPkg(targetPackageName);

        // Make sure the to be moved mapping does not exist in the target package
        Problem fastCheckProblem = null;
        for (MappingFileData toBeMovedFo : toBeMovedMappingFiles) {

            for (MappingFileData existingFo : mappingFilesInTargetPkg) {
                if (!toBeMovedFo.getFo().equals(existingFo.getFo()) && toBeMovedFo.getNewResourcename().equals(existingFo.getResourceName())) {

                    fastCheckProblem = HibernateRefactoringUtil.createProblem(fastCheckProblem,
                            true, NbBundle.getMessage(HibernateMappingRenamePlugin.class, "MSG_Name_Exists", toBeMovedFo.getFo().getNameExt()));

                    return fastCheckProblem;
                }
            }
        }
        return fastCheckProblem;
    }

    public void cancelRequest() {
    }

    public Problem prepare(RefactoringElementsBag refactoringElements) {

        // Get the configuration files
        HibernateEnvironment env = project.getLookup().lookup(HibernateEnvironment.class);
        if (env == null) {
            // The project does not support Hibernate framework
            return null;
        }
        List<FileObject> configFiles = env.getAllHibernateConfigFileObjects();
        if (configFiles.isEmpty()) {
            return null;
        }

        // TODO: have all the modifications in one transaction
        for(MappingFileData tobemoved : toBeMovedMappingFiles) {
            Map<FileObject, List<OccurrenceItem>> occurrences =
                    HibernateRefactoringUtil.getMappingResourceOccurrences(configFiles, tobemoved.getResourceName(), false);

            for (FileObject configFile : occurrences.keySet()) {
                List<OccurrenceItem> foundPlaces = occurrences.get(configFile);
                for (OccurrenceItem foundPlace : foundPlaces) {
                    HibernateRenameRefactoringElement elem = new HibernateRenameRefactoringElement(configFile,
                            tobemoved.getResourceName(),
                            tobemoved.getNewResourcename(),
                            foundPlace.getLocation(),
                            foundPlace.getText());
                    refactoringElements.add(refactoring, elem);
                }
            }
            refactoringElements.registerTransaction(new HibernateMappingRenameTransaction(
                    occurrences.keySet(), tobemoved.getResourceName(), tobemoved.getNewResourcename()));
        }

        return null;
    }

    private ArrayList<MappingFileData> getToBeMovedMappingFileData(String targetPkgName) {

        ArrayList<MappingFileData> fileData = new ArrayList<MappingFileData>();
        for (FileObject fo : refactoring.getRefactoringSource().lookupAll(FileObject.class)) {
            if(!fo.getMIMEType().equals(HibernateMappingDataLoader.REQUIRED_MIME)) {
                continue;
            }
            
            if (project == null) {
                project = FileOwnerQuery.getOwner(fo);
            }

            if(srcRoot == null) {
                SourceGroup[] grp = SourceGroups.getJavaSourceGroups(project);
                if (grp.length == 0) {
                    return null;
                }

                srcRoot = grp[0].getRootFolder().getPath();
            }
            String oldPath = fo.getPath();
            String oldResource = oldPath.substring(srcRoot.length() + 1);
            String fileName = oldResource.substring(oldResource.lastIndexOf("/") + 1);
            String newResource = (fileName.endsWith(".hbm.xml") ? targetPkgName.replace('.', '/') : targetPkgName) + "/" + fileName;//NOI18N, safe fix for 242415 in case if this data is used in more places

            fileData.add(new MappingFileData(fo, oldResource, newResource));
        }

        return fileData;
    }

    private ArrayList<MappingFileData> getMappingFileDataInTargetPkg(String targetPkgName) {

        ArrayList<MappingFileData> fileData = new ArrayList<MappingFileData>();
        HibernateEnvironment hibernateEnv = (HibernateEnvironment) project.getLookup().lookup(HibernateEnvironment.class);
        if (hibernateEnv == null) {
            // The project does not support Hibernate framework
            return fileData;
        }
        List<FileObject> mappingFiles = hibernateEnv.getAllHibernateMappingFileObjects();
        for (FileObject fo : mappingFiles) {
            String path = fo.getPath();
            String resourceName = path.substring(srcRoot.length() + 1);
            int separator = resourceName.lastIndexOf("/");
            if (separator > 0) {
                String packageName = resourceName.substring(0, separator);
                if (packageName.equals(targetPkgName)) {
                    fileData.add(new MappingFileData(fo, resourceName, null));
                }
            }            
        }

        return fileData;
    }

    private class MappingFileData {

        private FileObject fo;
        private String resourceName;
        private String newResourcename;

        public MappingFileData(FileObject fo, String resourceName, String newResourcename) {
            this.fo = fo;
            this.resourceName = resourceName;
            this.newResourcename = newResourcename;
        }

        public FileObject getFo() {
            return fo;
        }

        public String getNewResourcename() {
            return newResourcename;
        }

        public String getResourceName() {
            return resourceName;
        }
    }
}
