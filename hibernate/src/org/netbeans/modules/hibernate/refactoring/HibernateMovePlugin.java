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
