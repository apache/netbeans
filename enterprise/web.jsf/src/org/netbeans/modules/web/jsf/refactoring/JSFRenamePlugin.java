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
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.PositionBounds;

/**
 *
 * @author Petr Pisl
 */

public class JSFRenamePlugin extends JavaRefactoringPlugin {
    
    /** This one is important creature - makes sure that cycles between plugins won't appear */
    private static ThreadLocal semafor = new ThreadLocal();
    private TreePathHandle treePathHandle = null;
    
    private static final Logger LOGGER = Logger.getLogger(JSFRenamePlugin.class.getName());
    
    private final RenameRefactoring refactoring;
    
    /** Creates a new instance of WicketRenameRefactoringPlugin */
    public JSFRenamePlugin(RenameRefactoring refactoring) {
        this.refactoring = refactoring;
    }
    
    @Override
    public Problem preCheck() {
        LOGGER.fine("preCheck() called.");                                      //NOI18N
        return null;
    }
    
    @Override
    public Problem checkParameters() {
        LOGGER.fine("checkParameters() called.");                               //NOI18N
        return null;
    }
    
    @Override
    public Problem fastCheckParameters() {
        LOGGER.fine("fastCheckParameters() called.");                           //NOI18N
        return null;
    }

    @Override
    protected JavaSource getJavaSource(Phase p) {
        return null;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        if (semafor.get() == null) {
            semafor.set(new Object());
            
            FileObject fileObject = refactoring.getRefactoringSource().lookup(FileObject.class);
            NonRecursiveFolder nonRecursivefolder = refactoring.getRefactoringSource().lookup(NonRecursiveFolder.class);
            treePathHandle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
            
            if (fileObject != null && (JSFRefactoringUtils.isJavaFile(fileObject) || fileObject.isFolder())) {
                if (fileObject.isFolder()){
                    // renaming folder -> recursively
                    
                    // find the old package name
                    ClassPath classPath = ClassPath.getClassPath(fileObject, ClassPath.SOURCE);
                    if(classPath == null){
                        return null;//it may happens for folders in php and similar projects, see #181611
                    }
                    FileObject root = classPath.findOwnerRoot(fileObject);
                    String relativePath = FileUtil.getRelativePath(root, fileObject.getParent());
                    // change of the one of project source roots - issue #222730
                    if (relativePath == null) { relativePath = ""; } //NOI18N
                    String prefix = relativePath.replace('/','.'); //NOI18N
                    String oldName = (prefix.length() == 0 ? fileObject.getName() : prefix + "." + fileObject.getName());
                    // the new package name
                    String newName = (prefix.length() == 0 ? refactoring.getNewName() : prefix + "." + refactoring.getNewName());
                    
                    JSFRefactoringUtils.renamePackage(refactoring, refactoringElements, fileObject, oldName, newName, true);
                }
                else {
                    if (JSFRefactoringUtils.isJavaFile(fileObject)){
                        JavaSource source = JavaSource.forFileObject(fileObject);
                        // Can be null, if it is just folder. 
                        if (source != null){
                            try {
                                source.runUserActionTask(new Task<CompilationController>() {
                                    public void run(CompilationController co) throws Exception {
                                        co.toPhase(JavaSource.Phase.RESOLVED);
                                        CompilationUnitTree cut = co.getCompilationUnit();
                                        if(!cut.getTypeDecls().isEmpty()){
                                            treePathHandle = TreePathHandle.create(TreePath.getPath(cut, cut.getTypeDecls().get(0)), co);
                                        }
                                    }
                                }, false);
                            } catch (IllegalArgumentException ex) {
                                LOGGER.log(Level.WARNING, "Exception in JSFRenamePlugin", ex);  //NOI18N
                            } catch (IOException ex) {
                                LOGGER.log(Level.WARNING, "Exception in JSFRenamePlugin", ex);  //NOI18N
                            }
                        }
                    }
                }
            }
            if (nonRecursivefolder != null){
                // non recursive package renaming
                String oldName = JSFRefactoringUtils.getPackageName(nonRecursivefolder.getFolder());
                String newName = refactoring.getNewName();
                    
                JSFRefactoringUtils.renamePackage(refactoring, refactoringElements, nonRecursivefolder.getFolder(), oldName, newName, false);
            }
            
            if (treePathHandle != null && TreeUtilities.CLASS_TREE_KINDS.contains(treePathHandle.getKind())){
                //renaming a class
                Project project = FileOwnerQuery.getOwner(treePathHandle.getFileObject());
                if (project != null){
                    Element resElement = JSFRefactoringUtils.resolveElement(getClasspathInfo(refactoring), refactoring, treePathHandle);
                    // issue #242249
                    if (resElement == null) {
                        return null;
                    }
                    TypeElement type = (TypeElement) resElement;
                    String oldFQN = type.getQualifiedName().toString();
                    String newFQN = renameClass(oldFQN, refactoring.getNewName());
                    List <Occurrences.OccurrenceItem> items = Occurrences.getAllOccurrences(project, oldFQN, newFQN);
                    Modifications modification = new Modifications();
                    for (Occurrences.OccurrenceItem item : items) {
                       // refactoringElements.add(refactoring, new JSFConfigRenameClassElement(item));
                        PositionBounds position = item.getChangePosition();
                        Modifications.Difference difference = new Modifications.Difference(
                                Modifications.Difference.Kind.CHANGE, position.getBegin(),
                                position.getEnd(), oldFQN, newFQN, item.getChangeMessage());
                        modification.addDifference(item.getFacesConfig(), difference);
                        refactoringElements.add(refactoring, new DiffElement.ChangeFQCNElement(difference, item, modification));
                    }
                }
            }
            
            semafor.set(null);
        }
        return null;
    }
    
    /**
     * @return true if given str is null or empty.
     */
    private static boolean isEmpty(String str){
        return str == null || "".equals(str.trim());
    }
    
    /**
     * Constructs new name for given class.
     * @param originalFullyQualifiedName old fully qualified name of the class.
     * @param newName new unqualified name of the class.
     * @return new fully qualified name of the class.
     */
    private static String renameClass(String originalFullyQualifiedName, String newName){
        if (isEmpty(originalFullyQualifiedName) || isEmpty(newName)){
            throw new IllegalArgumentException("Old and new name of the class must be given."); //NOI18N
        }
        int lastDot = originalFullyQualifiedName.lastIndexOf('.');
        if (lastDot <= 0){
            // no package
            return newName;
        }
        return originalFullyQualifiedName.substring(0, lastDot + 1) + newName;
    }
    
    
   
    
}
