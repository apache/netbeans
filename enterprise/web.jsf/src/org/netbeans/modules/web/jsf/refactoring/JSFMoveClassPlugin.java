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
