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

package org.netbeans.modules.refactoring.java.ui;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ResourceBundle;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.swing.event.ChangeListener;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.api.ui.ExplorerContext;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUIBypass;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.*;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * A CustomRefactoringUI subclass that represents Safe Delete
 * @author Bharath Ravikumar
 */
public class SafeDeleteUI implements RefactoringUI, RefactoringUIBypass, JavaRefactoringUIFactory {
    
    private SafeDeleteRefactoring refactoring;
    
    private Object[] elementsToDelete;
    
    private SafeDeletePanel panel;
    
    private ResourceBundle bundle;
    
    private boolean regulardelete = false;
    private Lookup lookup;
    /**
     * Creates a new instance of SafeDeleteUI
     * @param selectedFiles An array of selected FileObjects that need to be 
     * safely deleted
     * @param handles  
     */
    private SafeDeleteUI(FileObject[] selectedFiles, Collection<TreePathHandle> handles, boolean regulardelete) {
        this.elementsToDelete = selectedFiles;
        refactoring = new SafeDeleteRefactoring(new ProxyLookup(Lookups.fixed(elementsToDelete), Lookups.fixed(handles.toArray(new Object[0]))));
        refactoring.getContext().add(JavaRefactoringUtils.getClasspathInfoFor(selectedFiles));
        this.regulardelete = regulardelete;
    }

    /**
     * Creates a new instance of SafeDeleteUI
     * @param selectedElements An array of selected Elements that need to be 
     * safely deleted
     */
    private SafeDeleteUI(TreePathHandle[] selectedElements) {
        this.elementsToDelete = selectedElements;
        refactoring = new SafeDeleteRefactoring(Lookups.fixed(elementsToDelete));
        refactoring.getContext().add(RefactoringUtils.getClasspathInfoFor(selectedElements[0]));
    }

    private SafeDeleteUI(NonRecursiveFolder nonRecursiveFolder, boolean regulardelete) {
        refactoring = new SafeDeleteRefactoring(Lookups.fixed(nonRecursiveFolder));
        refactoring.getContext().add(JavaRefactoringUtils.getClasspathInfoFor(nonRecursiveFolder.getFolder()));
        this.regulardelete = regulardelete;
    }
    
    private SafeDeleteUI(Lookup lookup) {
        this.lookup = lookup;
    }
    
    /**
     * Delegates to the fastCheckParameters of the underlying
     * refactoring
     * @return Returns the result of fastCheckParameters of the
     * underlying refactoring
     */
    @Override
    public org.netbeans.modules.refactoring.api.Problem checkParameters() {
        refactoring.setCheckInComments(panel.isSearchInComments());
        return refactoring.fastCheckParameters();
    }
    
    @Override
    public String getDescription() {
        //TODO: Check bounds here. Might throw an OutofBoundsException otherwise.
//        if (elementsToDelete[0] instanceof JavaClass) {
//            return getString("DSC_SafeDelClasses", elementsToDelete);// NOI18N
//        } else {
//            if (elementsToDelete[0] instanceof ExecutableElement) {
//                if (elementsToDelete.length > 1) 
//                    return getString("DSC_SafeDelMethods");// NOI18N
//                else 
//                    return getString("DSC_SafeDelMethod", elementsToDelete[0]);// NOI18N
//            }
//            
//        }
//        if(elementsToDelete[0] instanceof Resource){
//                return NbBundle.getMessage(SafeDeleteUI.class, "DSC_SafeDel", 
//                        ((Resource)elementsToDelete[0]).getName()); // NOI18N
//        }
        NonRecursiveFolder folder = refactoring.getRefactoringSource().lookup(NonRecursiveFolder.class);
        if (folder != null) {
            return NbBundle.getMessage(SafeDeleteUI.class, "DSC_SafeDelPkg", folder.getFolder().getNameExt().replace('/', '.')); // NOI18N
        }
        
        return NbBundle.getMessage(SafeDeleteUI.class, "DSC_SafeDel", elementsToDelete); // NOI18N
    }
    
    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.refactoring.java.ui.SafeDeleteUI"); // NOI18N
    }
    
    @Override
    public String getName() {
        
        return NbBundle.getMessage(SafeDeleteUI.class, "LBL_SafeDel"); // NOI18N
    }
    
    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        //TODO:Do you want to just use Arrays.asList?
        if(panel == null) {
            panel = new SafeDeletePanel(refactoring, regulardelete, parent);
        }
        return panel;
    }
    
    @Override
    public AbstractRefactoring getRefactoring() {
        
        return refactoring;
    }
    
    @Override
    public boolean hasParameters() {
        
        return true;
    }
    /**
     * Returns false, since this refactoring is not a query.
     * @return false
     */
    @Override
    public boolean isQuery() {
        return false;
    }
    
    @Override
    public Problem setParameters() {
        refactoring.setCheckInComments(panel.isSearchInComments());
        return refactoring.checkParameters();
    }
    
    //Helper methods------------------
    
    @Override
    public boolean isRefactoringBypassRequired() {
        return panel.isRegularDelete();
    }

    @Override
    public void doRefactoringBypass() throws IOException {
        RequestProcessor.getDefault().post(new Runnable() {

            @Override
            public void run() {
                try {
                    // #172199
                    FileUtil.runAtomicAction(new FileSystem.AtomicAction() {

                        @Override
                        public void run() throws IOException {
                            for (FileObject file : getRefactoring().getRefactoringSource().lookupAll(FileObject.class)) {
                                if(file.isValid()) {
                                    DataObject.find(file).delete();
                                }
                            }
                            NonRecursiveFolder f = (NonRecursiveFolder) getRefactoring().getRefactoringSource().lookup(NonRecursiveFolder.class);
                            if (f != null) {
                                deletePackage(f.getFolder());
                            }
                        }
                    });
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            
        });

    }
    
    private void deletePackage(FileObject source) {
        ClassPath classPath = ClassPath.getClassPath(source, ClassPath.SOURCE);
        FileObject root = classPath != null ? classPath.findOwnerRoot(source) : null;

        DataFolder dataFolder = DataFolder.findFolder(source);

        FileObject parent = dataFolder.getPrimaryFile().getParent();
        // First; delete all files except packages

        try {
            DataObject ch[] = dataFolder.getChildren();
            boolean empty = true;
            for (int i = 0; ch != null && i < ch.length; i++) {
                if (!ch[i].getPrimaryFile().isFolder()) {
                    ch[i].delete();
                }
                else if (empty && VisibilityQuery.getDefault().isVisible(ch[i].getPrimaryFile())) {
                    // 156529: hidden folders should be considered as empty content
                    empty = false;
                }
            }

            // If empty delete itself
            if ( empty ) {
                dataFolder.delete();
            }

            // Second; delete empty super packages, or empty folders when there is not root
            while (!parent.equals(root) && parent.getChildren().length == 0) {
                FileObject newParent = parent.getParent();
                parent.delete();
                parent = newParent;
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    @Override
    public RefactoringUI create(CompilationInfo info, TreePathHandle[] handles, FileObject[] files, NonRecursiveFolder[] packages) {
        final boolean b = lookup.lookup(ExplorerContext.class)!=null;
        if (packages != null && packages.length == 1) {
            return new SafeDeleteUI(packages[0], b);
        }
        if (handles != null && handles.length == 0 || (files!=null && files.length > 1)) {
            return new SafeDeleteUI(files, Arrays.asList(handles), b);
        }
        
        if (b && files!=null && files.length == 1) {
            return new SafeDeleteUI(files, Arrays.asList(handles), b);
        }
        
        if (info == null) {
            return new SafeDeleteUI(handles);
        }
        
        TreePathHandle selectedElement = handles[0];
        Element selected = selectedElement.resolveElement(info);
        TreePath selectedTree = selectedElement.resolve(info);
        if (selected == null || selectedTree == null) {
            return null;
        }
        if (selected.getKind() == ElementKind.PACKAGE || selected.getEnclosingElement().getKind() == ElementKind.PACKAGE) {
            ElementHandle<Element> handle = ElementHandle.create(selected);
            FileObject file = SourceUtils.getFile(handle, info.getClasspathInfo());
            if (file == null) {
                return null;
            }
            if (file.getName().equals(selected.getSimpleName().toString())) {
                return new SafeDeleteUI(new FileObject[]{file}, Collections.singleton(selectedElement), b);
            }
        }
        if(!TreeUtilities.CLASS_TREE_KINDS.contains(selectedTree.getParentPath().getLeaf().getKind())
                && selectedTree.getParentPath().getLeaf().getKind() != Tree.Kind.COMPILATION_UNIT
                && selectedTree.getLeaf().getKind() == Tree.Kind.VARIABLE) {
            switch (selectedTree.getParentPath().getLeaf().getKind()) {
                case BLOCK:
                case METHOD:
                    break;
                default:
                    return null;
            }
        }
        return new SafeDeleteUI(new TreePathHandle[]{selectedElement});
    }
    
    public static JavaRefactoringUIFactory factory(Lookup lookup) {
        return new SafeDeleteUI(lookup);
    }
}
