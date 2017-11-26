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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.hibernate.refactoring.HibernateRefactoringUtil.OccurrenceItem;
import org.netbeans.modules.hibernate.service.api.HibernateEnvironment;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Dongmei Cao
 */
public class HibernateMovePlugin implements RefactoringPlugin {

    private MoveRefactoring refactoring;
    private Project project;

    public HibernateMovePlugin(MoveRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    public Problem preCheck() {
        return null;
    }

    public Problem checkParameters() {
        return null;
    }

    public Problem fastCheckParameters() {
        return null;
    }

    public void cancelRequest() {
    }

    public Problem prepare(RefactoringElementsBag refactoringElements) {
        try {
            URL targetURL = refactoring.getTarget().lookup(URL.class);
            if (targetURL == null) {
                // TODO: return a Problem
                return null;
            }
            
            // See issue 138950
            if(refactoring.getRefactoringSource().lookupAll(FileObject.class).isEmpty()) {
                return null;
            }

            String targetPackageName = HibernateRefactoringUtil.getPackageName(targetURL);
            if (targetPackageName == null) {
                // TODO: return a Problem
                return null;
            }

            // Find out the classes or/and packages to be refactored
            List<String> oldPackageNames = new ArrayList<String>();
            List<String> oldClassNames = new ArrayList<String>();
            for (FileObject fo : refactoring.getRefactoringSource().lookupAll(FileObject.class)) {
                if (project == null) {
                    project = FileOwnerQuery.getOwner(fo);
                }

                if (fo.isFolder()) {
                    String oldPackageName = HibernateRefactoringUtil.getPackageName(fo);
                    if (oldPackageName == null) {
                        continue;
                    } else {
                        oldPackageNames.add(oldPackageName);
                    }

                } else if (HibernateRefactoringUtil.isJavaFile(fo)) {
                    List<String> classNames = HibernateRefactoringUtil.getTopLevelClassNames(fo);
                    oldClassNames.addAll(classNames);
                }
            }

            // Find the mapping files in this project
            HibernateEnvironment env = project.getLookup().lookup(HibernateEnvironment.class);
            if(env == null) {
                // The project does not support Hibernate framework
                return null;
            }
            List<FileObject> mappingFileObjs = env.getAllHibernateMappingFileObjects();
            if (mappingFileObjs == null || mappingFileObjs.size() == 0) {
                // OK, no mapping files at all. 
                return null;
            }

            // Pacakges
            // TODO: is it possible to just move the packages????. Need to find out

            // Class names
            // TODO: have all the modifications in one transaction
            for (String oldBinaryName : oldClassNames) {

                String simpleClassName = HibernateRefactoringUtil.getSimpleElementName(oldBinaryName);
                String newBinaryName = HibernateRefactoringUtil.createQualifiedName(targetPackageName, simpleClassName);

                if (newBinaryName != null) {

                    Map<FileObject, List<OccurrenceItem>> occurrences =
                            HibernateRefactoringUtil.getJavaClassOccurrences(mappingFileObjs, oldBinaryName);

                    for (FileObject mFileObj : occurrences.keySet()) {
                        List<OccurrenceItem> foundPlaces = occurrences.get(mFileObj);
                        for (OccurrenceItem foundPlace : foundPlaces) {
                            HibernateRenameRefactoringElement elem = new HibernateRenameRefactoringElement(mFileObj,
                                    oldBinaryName,
                                    foundPlace.getMatching(),
                                    newBinaryName,
                                    foundPlace.getLocation(),
                                    foundPlace.getText());
                            refactoringElements.add(refactoring, elem);
                        }
                    }

                    refactoringElements.registerTransaction(new JavaClassRenameTransaction(occurrences.keySet(), oldBinaryName, newBinaryName));
                }
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        }

        return null;
    }
}
