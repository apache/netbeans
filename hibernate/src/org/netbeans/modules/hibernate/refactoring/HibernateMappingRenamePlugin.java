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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.hibernate.loaders.mapping.HibernateMappingDataLoader;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.hibernate.refactoring.HibernateRefactoringUtil.OccurrenceItem;
import org.netbeans.modules.hibernate.service.api.HibernateEnvironment;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.util.NbBundle;

/**
 * This plugin modifies the Hibernate configuration files accordingly when the referenced
 * mapping files are renamed
 * 
 * @author Dongmei Cao
 */
public class HibernateMappingRenamePlugin implements RefactoringPlugin {

    private RenameRefactoring refactoring;
    private FileObject fo;
    private String oldResourceName, newResourceName;

    public HibernateMappingRenamePlugin(RenameRefactoring refactoring) {
        this.refactoring = refactoring;
        fo = refactoring.getRefactoringSource().lookup(FileObject.class);
    }

    public Problem preCheck() {
        return null;
    }

    public Problem checkParameters() {
        return null;
    }

    public Problem fastCheckParameters() {
        Problem fastCheckProblem = null;
    
        String oldName = fo.getName();
        String newName = refactoring.getNewName();
        if(oldName.equals(newName)) {
            fastCheckProblem = HibernateRefactoringUtil.createProblem(fastCheckProblem, 
                    true,  NbBundle.getMessage(HibernateMappingRenamePlugin.class,"MSG_NameNotChanged"));
            return fastCheckProblem;
        }
        
         if(!HibernateRefactoringUtil.isValidMappingFileName(newName)) {
             fastCheckProblem = HibernateRefactoringUtil.createProblem(fastCheckProblem, 
                     true, NbBundle.getMessage(HibernateMappingRenamePlugin.class,"MSG_Invalid_Name"));
         }
        
         // Get the old and new resource name
        getOldNewResourceName();
        if(oldResourceName == null || newResourceName == null) {
            fastCheckProblem = HibernateRefactoringUtil.createProblem(fastCheckProblem, 
                    true, NbBundle.getMessage(HibernateMappingRenamePlugin.class, "MSG_Invalid_Name"));
        }
        
        // Make sure the name is unique
        Project project = org.netbeans.api.project.FileOwnerQuery.getOwner(fo);
        HibernateEnvironment hibernateEnv = (HibernateEnvironment) project.getLookup().lookup(HibernateEnvironment.class);
        List<String> mappingFiles = hibernateEnv.getAllHibernateMappings();
        if(mappingFiles.contains(newResourceName)){
            fastCheckProblem = HibernateRefactoringUtil.createProblem(fastCheckProblem, 
                    true, NbBundle.getMessage(HibernateMappingRenamePlugin.class, "MSG_NameNotUnique", newName));
        } 

        return fastCheckProblem;
    }

    public void cancelRequest() {
        return;
    }

    public Problem prepare(RefactoringElementsBag refactoringElements) {

        if (fo == null || !fo.getMIMEType().equals(HibernateMappingDataLoader.REQUIRED_MIME)) {
            // Nothing needs to be done
            return null;
        }

        // Get the configuration files
        Project proj = FileOwnerQuery.getOwner(fo);
        HibernateEnvironment env = proj.getLookup().lookup(HibernateEnvironment.class);
        List<FileObject> configFiles = env.getAllHibernateConfigFileObjects();
        if(configFiles.isEmpty())
            return null;
        
        Map<FileObject, List<OccurrenceItem>> occurrences =
                HibernateRefactoringUtil.getMappingResourceOccurrences(configFiles, oldResourceName, false);

        for (FileObject configFile : occurrences.keySet()) {
            List<OccurrenceItem> foundPlaces = occurrences.get(configFile);
            for (OccurrenceItem foundPlace : foundPlaces) {
                HibernateRenameRefactoringElement elem = new HibernateRenameRefactoringElement(configFile,
                        oldResourceName,
                        newResourceName,
                        foundPlace.getLocation(),
                        foundPlace.getText());
                refactoringElements.add(refactoring, elem);
            }
        }
        refactoringElements.registerTransaction(new HibernateMappingRenameTransaction(
                occurrences.keySet(), oldResourceName, newResourceName));

        return null;
    }

    private void getOldNewResourceName() {
        Project project = FileOwnerQuery.getOwner(fo);
        SourceGroup[] grp = SourceGroups.getJavaSourceGroups(project);
        if (grp.length == 0) {
            return;
        }

        String srcRoot = grp[0].getRootFolder().getPath();
        String oldPath = fo.getPath();
        oldResourceName = oldPath.substring(srcRoot.length() + 1);
        String pkgPath = oldResourceName.substring(0, oldResourceName.lastIndexOf("/") + 1);
        newResourceName = pkgPath + refactoring.getNewName() + ".xml"; // NOI18N
    }
}
