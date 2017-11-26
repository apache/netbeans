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

import com.sun.source.tree.Tree.Kind;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.TreeUtilities;
import org.openide.filesystems.FileObject;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.hibernate.refactoring.HibernateRefactoringUtil.OccurrenceItem;
import org.netbeans.modules.hibernate.refactoring.HibernateRefactoringUtil.RenamedClassName;
import org.netbeans.modules.hibernate.service.api.HibernateEnvironment;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.ErrorManager;

/**
 * This plugin modifies the Hibernate mapping files accordingly when the referenced
 * Java class or/and package names are changed
 * 
 * @author Dongmei Cao
 */
public class HibernateRenamePlugin implements RefactoringPlugin {

    private RenameRefactoring refactoring;
    private List<FileObject> mFileObjs;

    public HibernateRenamePlugin(RenameRefactoring refactoring) {
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
        return;
    }

    public Problem prepare(RefactoringElementsBag refactoringElements) {
        final TreePathHandle treePathHandle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
        FileObject fo = null;
        if (treePathHandle != null &&
                (TreeUtilities.CLASS_TREE_KINDS.contains(treePathHandle.getKind()) ||
                treePathHandle.getKind() == Kind.VARIABLE ||
                treePathHandle.getKind() == Kind.MEMBER_SELECT ||
                treePathHandle.getKind() == Kind.IDENTIFIER)) {
            fo = treePathHandle.getFileObject();
        }
        if (fo == null) {
            fo = refactoring.getRefactoringSource().lookup(FileObject.class);
        }
        boolean recursive = true;
        if (fo == null) {
            NonRecursiveFolder folder = refactoring.getRefactoringSource().lookup(NonRecursiveFolder.class);
            if (folder != null) {
                recursive = false;
                fo = folder.getFolder();
            }
        }
        if (fo == null) {
            return null;
        }

        // Find the mapping files in this project
        Project proj = org.netbeans.api.project.FileOwnerQuery.getOwner(fo);
        if(proj == null){
            //file is not part of any project
            return null;
        }
        HibernateEnvironment env = proj.getLookup().lookup(HibernateEnvironment.class);
        if (env == null) {
            // The project does not support Hibernate framework
            return null;
        }
        mFileObjs = env.getAllHibernateMappingFileObjects();
        if (mFileObjs == null || mFileObjs.size() == 0) {
            // OK, no mapping files at all. 
            return null;
        }

        try {
            if (treePathHandle != null) {
                if (TreeUtilities.CLASS_TREE_KINDS.contains(treePathHandle.getKind())) {
                    // A Java class is being renamed
                    renameJavaClass(refactoringElements, treePathHandle, fo);
                } else if (treePathHandle.getKind() == Kind.VARIABLE ||
                        treePathHandle.getKind() == Kind.MEMBER_SELECT ||
                        treePathHandle.getKind() == Kind.IDENTIFIER) {
                    // A Java field is being renamed
                    renameJavaField(refactoringElements, treePathHandle, fo);
                }
            } else if (fo.isFolder()) {
                // A Java package is being renamed
                renameJavaPackage(refactoringElements, treePathHandle, fo, recursive);
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        }
        return null;
    }

    private void renameJavaClass(RefactoringElementsBag refactoringElements, TreePathHandle treePathHandle,
            FileObject fo) throws IOException {

        RenamedClassName clazz = null;

        // Figure out the old name and new name
        JavaSource js = JavaSource.forFileObject(fo);
        if (js != null) {
            clazz = HibernateRefactoringUtil.getRenamedClassName(treePathHandle, js, refactoring.getNewName());
        }

        if (clazz != null) {
            String oldBinaryName = clazz.getOldBinaryName();
            String newBinaryName = clazz.getNewBinaryName();
            if (oldBinaryName != null && newBinaryName != null) {

                Map<FileObject, List<OccurrenceItem>> occurrences =
                        HibernateRefactoringUtil.getJavaClassOccurrences(mFileObjs, oldBinaryName);

                for (FileObject mFileObj : occurrences.keySet()) {
                    List<OccurrenceItem> foundPlaces = occurrences.get(mFileObj);
                    for( OccurrenceItem foundPlace : foundPlaces) {
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
    }

    private void renameJavaField(RefactoringElementsBag refactoringElements, final TreePathHandle treePathHandle,
            FileObject fo) throws IOException {

        final String[] classAndVariableNames = new String[]{null, null};
        JavaSource javaSource = JavaSource.forFileObject(fo);
        if (javaSource == null) {
            return;
        }

        javaSource.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController cc) throws IOException {
                cc.toPhase(Phase.ELEMENTS_RESOLVED);
                Element element = treePathHandle.resolveElement(cc);
                if (element == null || element.getKind() != ElementKind.FIELD) {
                    return;
                }
                classAndVariableNames[0] = ElementUtilities.getBinaryName((TypeElement) element.getEnclosingElement());
                classAndVariableNames[1] = element.getSimpleName().toString();
            }
        }, true);
        
        String className = classAndVariableNames[0];
        String oldVariableName = classAndVariableNames[1];
        String newVariableName = refactoring.getNewName();
        if(oldVariableName != null && newVariableName != null) {
        
            Map<FileObject, List<OccurrenceItem>> occurrences =
                        HibernateRefactoringUtil.getJavaFieldOccurrences(mFileObjs, className, oldVariableName);

                for (FileObject mFileObj : occurrences.keySet()) {
                    List<OccurrenceItem> foundPlaces = occurrences.get(mFileObj);
                    for (OccurrenceItem foundPlace : foundPlaces) {
                        HibernateRenameRefactoringElement elem = new HibernateRenameRefactoringElement(mFileObj,
                                oldVariableName,
                                foundPlace.getMatching(),
                                newVariableName,
                                foundPlace.getLocation(),
                                foundPlace.getText());
                        refactoringElements.add(refactoring, elem);
                    }
                }

                refactoringElements.registerTransaction(new JavaFieldRenameTransaction(occurrences.keySet(), className, oldVariableName, newVariableName));
        }
    }

    private void renameJavaPackage(RefactoringElementsBag refactoringElements, final TreePathHandle treePathHandle,
            FileObject fo, boolean recursive) throws IOException {
        
        // First, find all the occurrences of the affected Java classes in the mapping files
        
        String oldPackageName = HibernateRefactoringUtil.getPackageName(fo);
        // If the rename is not recursive (e.g, "a.b.c" -> "x.b.c"), the new name is the whole package name.
        String newPackageName = recursive ? HibernateRefactoringUtil.getRenamedPackageName(fo, refactoring.getNewName()) : refactoring.getNewName();
        if (oldPackageName != null && newPackageName != null) {
            Map<FileObject, List<OccurrenceItem>> occurrences =
                    HibernateRefactoringUtil.getJavaPackageOccurrences(mFileObjs, oldPackageName);

            for (FileObject mFileObj : occurrences.keySet()) {
                List<OccurrenceItem> foundPlaces = occurrences.get(mFileObj);

                for (OccurrenceItem foundPlace : foundPlaces) {
                    HibernateRenameRefactoringElement elem = new HibernateRenameRefactoringElement(mFileObj,
                            oldPackageName,
                            newPackageName,
                            foundPlace.getLocation(),
                            foundPlace.getText());
                    refactoringElements.add(refactoring, elem);
                }
            }

            refactoringElements.registerTransaction(new JavaPackageRenameTransaction(occurrences.keySet(), oldPackageName, newPackageName));
        }
        
        // Second, find all the occurrences of the affected the mapping file in the tobe-renamed pacakge in the configuration files
        String oldResourcePath = oldPackageName.replace('.', '/');
        String newResourcePath = newPackageName.replace('.', '/');
        
        // Get the configuration files
        Project proj = FileOwnerQuery.getOwner(fo);
        HibernateEnvironment env = proj.getLookup().lookup(HibernateEnvironment.class);
        if (env == null) {
            // The project does not support Hibernate framework
            return;
        }
        List<FileObject> configFiles = env.getAllHibernateConfigFileObjects();
        if(configFiles.isEmpty())
            return;
        
        Map<FileObject, List<OccurrenceItem>> occurrences =
                HibernateRefactoringUtil.getMappingResourceOccurrences(configFiles, oldResourcePath, true);

        for (FileObject configFile : occurrences.keySet()) {
            List<OccurrenceItem> foundPlaces = occurrences.get(configFile);
            for (OccurrenceItem foundPlace : foundPlaces) {
                HibernateRenameRefactoringElement elem = new HibernateRenameRefactoringElement(configFile,
                        oldResourcePath,
                        newResourcePath,
                        foundPlace.getLocation(),
                        foundPlace.getText());
                refactoringElements.add(refactoring, elem);
            }
        }
        refactoringElements.registerTransaction(new HibernateMappingRenameTransaction(
                occurrences.keySet(), oldResourcePath, newResourcePath, true));
    }
}
