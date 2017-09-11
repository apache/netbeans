/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
