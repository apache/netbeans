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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.web.jsf.refactoring;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
/**
 *
 * @author Petr Pisl
 */
public class JSFMoveClassPlugin extends JavaRefactoringPlugin {
    
    private Collection<TreePathHandle> treePathHandles;
    
    private static final Logger LOGGER = Logger.getLogger(JSFMoveClassPlugin.class.getName());
    
    private final MoveRefactoring refactoring;
    
    public JSFMoveClassPlugin(MoveRefactoring refactoring) {
        this.refactoring = refactoring;
    }
    
    @Override
    public Problem preCheck() {
        return null;
    }
    
    @Override
    public Problem checkParameters() {
        return null;
    }
    
    @Override
    public Problem fastCheckParameters() {
        return null;
    }

    @Override
    protected JavaSource getJavaSource(Phase p) {
        return null;
    }
    
    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        Collection<? extends FileObject> fileObjects = refactoring.getRefactoringSource().lookupAll(FileObject.class);    
        Collection treePathHandlesFromLookup = refactoring.getRefactoringSource().lookupAll(TreePathHandle.class);
        treePathHandles = new ArrayList(treePathHandlesFromLookup);
       
        if (fileObjects != null) {
            for (FileObject fileObject : fileObjects) {
                if (fileObject.isFolder() && (JSFRefactoringUtils.isJavaFile(fileObject) || fileObject.isFolder())) {
                    // moving folder
                    // find the old package name
                    ClassPath classPath = ClassPath.getClassPath(fileObject, ClassPath.SOURCE);
                    FileObject root = classPath.findOwnerRoot(fileObject);

                    String prefix = FileUtil.getRelativePath(root, fileObject.getParent()).replace('/','.');
                    String oldName = (prefix.length() == 0 ? fileObject.getName() : prefix + '.' + fileObject.getName());
                    // the new package name
                    String newPrefix = JSFRefactoringUtils.getPackageName(refactoring.getTarget().lookup(URL.class));
                    String newName = (newPrefix.length() == 0 ? fileObject.getName() : newPrefix + '.' + fileObject.getName());

                    JSFRefactoringUtils.renamePackage(refactoring, refactoringElements, fileObject, oldName, newName, true);
                }
                else {
                    if (JSFRefactoringUtils.isJavaFile(fileObject)){
                        JavaSource source = JavaSource.forFileObject(fileObject);
                        if (source != null) {
                            try {
                                source.runUserActionTask(new Task<CompilationController>() {
                                    public void run(CompilationController co) throws Exception {
                                        co.toPhase(JavaSource.Phase.RESOLVED);
                                        CompilationUnitTree cut = co.getCompilationUnit();
                                        List<? extends Tree> typeDecls = cut.getTypeDecls();
                                        if (!typeDecls.isEmpty()){
                                            treePathHandles.add(TreePathHandle.create(TreePath.getPath(cut, typeDecls.get(0)), co));
                                        }
                                    }
                                }, false);
                            } catch (IllegalArgumentException ex) {
                                LOGGER.log(Level.WARNING, "Exception in JSFMoveClassPlugin", ex); //NOI18N
                            } catch (IOException ex) {
                                LOGGER.log(Level.WARNING, "Exception in JSFMoveClassPlugin", ex); //NOI18N
                            }
                        }
                    }
                }
            }
       } 

       if (treePathHandles != null) { 
           for (TreePathHandle treePathHandle : treePathHandles) {
                if (treePathHandle != null && TreeUtilities.CLASS_TREE_KINDS.contains(treePathHandle.getKind())) {
                    Project project = FileOwnerQuery.getOwner(treePathHandle.getFileObject());
                    if (project != null) {
                        Element resElement = JSFRefactoringUtils.resolveElement(getClasspathInfo(refactoring), refactoring, treePathHandle);
                        TypeElement type = (TypeElement) resElement;
                        URL targetUrl = refactoring.getTarget().lookup(URL.class);
                        if (type != null && targetUrl != null) {
                            String oldFQN = type.getQualifiedName().toString();
                            String newPackageName = JSFRefactoringUtils.getPackageName(targetUrl);
                            String newFQN = newPackageName.length() == 0 ? type.getSimpleName().toString() : newPackageName + '.' + type.getSimpleName().toString();
                            if (isTargetOtherProject(treePathHandle.getFileObject(), refactoring)) {
                                List<Occurrences.OccurrenceItem> items = Occurrences.getAllOccurrences(project, oldFQN, newFQN);
                                for (Occurrences.OccurrenceItem item : items) {
                                    refactoringElements.add(refactoring, new JSFSafeDeletePlugin.JSFSafeDeleteClassElement(item));
                                }
                            } else {
                                List<Occurrences.OccurrenceItem> items = Occurrences.getAllOccurrences(project, oldFQN, newFQN);
                                Modifications modification = new Modifications();
                                for (Occurrences.OccurrenceItem item : items) {
                                    Modifications.Difference difference = new Modifications.Difference(Modifications.Difference.Kind.CHANGE, item.getChangePosition().getBegin(), item.getChangePosition().getEnd(), item.getOldValue(), item.getNewValue(), item.getRenamePackageMessage());
                                    modification.addDifference(item.getFacesConfig(), difference);
                                    refactoringElements.add(refactoring, new DiffElement.ChangeFQCNElement(difference, item, modification));
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private boolean isTargetOtherProject(FileObject localFileObject, MoveRefactoring ref) {
        boolean targetOtherProject = false;

        try {
            Project targetProject = FileOwnerQuery.getOwner(ref.getTarget().lookup(URL.class).toURI());
            Project srcProject = FileOwnerQuery.getOwner(localFileObject);
            targetOtherProject = !targetProject.equals(srcProject);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Exception in JSFMoveClassPlugin", e); //NOI18N
        }

        return targetOtherProject;
    }
}
