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
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.hibernate.refactoring.HibernateRefactoringUtil.OccurrenceItem;
import org.netbeans.modules.hibernate.service.api.HibernateEnvironment;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 * Plugin to find usage of Java class or Java class field
 * 
 * @author Dongmei Cao
 */
public class HibernateFindUsagesPlugin implements RefactoringPlugin {

    private final WhereUsedQuery query;
    private List<FileObject> mappingFileObjs;

    public HibernateFindUsagesPlugin(WhereUsedQuery query) {
        this.query = query;
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

        if (query.getBooleanValue(WhereUsedQuery.FIND_REFERENCES)) {

            final TreePathHandle treePathHandle = query.getRefactoringSource().lookup(TreePathHandle.class);
            FileObject fo = null;
            if (treePathHandle != null &&
                    (TreeUtilities.CLASS_TREE_KINDS.contains(treePathHandle.getKind()) ||
                    treePathHandle.getKind() == Kind.VARIABLE ||
                    treePathHandle.getKind() == Kind.MEMBER_SELECT ||
                    treePathHandle.getKind() == Kind.IDENTIFIER)) {

                fo = treePathHandle.getFileObject();
            }

            if (fo == null) {
                // TODO: return a Problem
                return null;
            }

            // Find the mapping files in this project
            Project proj = org.netbeans.api.project.FileOwnerQuery.getOwner(fo);
            if(proj == null) {
                // See issue 141117
                return null;
            }
            HibernateEnvironment env = proj.getLookup().lookup(HibernateEnvironment.class);
            if(env == null) {
                // The project does not have Hibernate framework support
                return null;
            }
            mappingFileObjs = env.getAllHibernateMappingFileObjects();
            if (mappingFileObjs == null || mappingFileObjs.size() == 0) {
                // OK, no mapping files at all. 
                return null;
            }

            try {
                if (treePathHandle != null) {
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(treePathHandle.getKind())) {
                        findJavaClassUsage(treePathHandle, refactoringElements, fo);
                    } else /*if (treePathHandle.getKind() == Kind.VARIABLE ||
                            treePathHandle.getKind() == Kind.MEMBER_SELECT ||
                            treePathHandle.getKind() == Kind.IDENTIFIER)*/ {
                        findJavaClassFieldUsage(treePathHandle, refactoringElements, fo);

                    }
                }
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            }
        }

        return null;
    }

    private void findJavaClassUsage(final TreePathHandle treePathHandle,
            RefactoringElementsBag refactoringElements, FileObject classFile) throws IOException {

        // Figure out the class binary name
        final String[] binaryClassName = new String[]{null};
        JavaSource javaSource = JavaSource.forFileObject(classFile);
        if (javaSource == null) {
            return;
        }

        javaSource.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController cc) throws IOException {
                cc.toPhase(Phase.ELEMENTS_RESOLVED);
                Element element = treePathHandle.resolveElement(cc);
                if (element == null || element.getKind() != ElementKind.CLASS) {
                    return;
                }
                binaryClassName[0] = ElementUtilities.getBinaryName((TypeElement) element);
            }
        }, true);

        String className = binaryClassName[0];
        if (className != null) {
            Map<FileObject, List<OccurrenceItem>> occurrences =
                    HibernateRefactoringUtil.getJavaClassOccurrences(mappingFileObjs, className);

            for (FileObject mFileObj : occurrences.keySet()) {
                List<OccurrenceItem> foundPlaces = occurrences.get(mFileObj);
                for( OccurrenceItem foundPlace : foundPlaces ) {
                    HibernateRefactoringElement elem = new HibernateRefactoringElement(mFileObj,
                            className,
                            foundPlace.getMatching(),
                            foundPlace.getLocation(),
                            foundPlace.getText());
                    refactoringElements.add(query, elem);
                }
            }
        }
    }
    
    private void findJavaClassFieldUsage(final TreePathHandle treePathHandle,
            RefactoringElementsBag refactoringElements, FileObject classFile) throws IOException {

        // Figure out the class binary name and field name
        final String[] classAndFieldName = new String[]{null, null};
        JavaSource javaSource = JavaSource.forFileObject(classFile);
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
                classAndFieldName[0] = ElementUtilities.getBinaryName((TypeElement) element.getEnclosingElement());
                classAndFieldName[1] = element.getSimpleName().toString();
            }
        }, true);

        String className = classAndFieldName[0];
        String fieldName = classAndFieldName[1];
        
        if (className != null && fieldName != null) {

            Map<FileObject, List<OccurrenceItem>> occurrences =
                    HibernateRefactoringUtil.getJavaFieldOccurrences(mappingFileObjs, className, fieldName);

            for (FileObject mFileObj : occurrences.keySet()) {
                List<OccurrenceItem> foundPlaces = occurrences.get(mFileObj);
                for (OccurrenceItem foundPlace : foundPlaces) {
                    HibernateRefactoringElement elem = new HibernateRefactoringElement(mFileObj,
                            fieldName,
                            foundPlace.getLocation(),
                            foundPlace.getText());
                    refactoringElements.add(query, elem);
                }
            }
        }
    }
}
