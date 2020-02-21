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
package org.netbeans.modules.cnd.refactoring.ui;

import java.io.IOException;
import java.text.MessageFormat;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUIBypass;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public class RenameRefactoringUI implements RefactoringUI, RefactoringUIBypass {
    private final AbstractRefactoring refactoring;
    private final String oldName;
    private final String dispOldName;
    private String newName;
    private RenamePanel panel;
    private final boolean fromListener;
//    private TreePathHandle handle;
    private final CsmObject origObject;
    private FileObject byPassFolder;
    private boolean byPassPakageRename;
    
    public RenameRefactoringUI(CsmObject csmObject, String newName) {
        String name = CsmRefactoringUtils.getSimpleText(csmObject);
        if ((csmObject instanceof CsmReference) &&
            (name.startsWith("\"") && name.endsWith("\"") || name.startsWith("<") && name.endsWith(">"))) { // NOI18N
            CsmObject referencedObject = ((CsmReference)csmObject).getReferencedObject();
            if (CsmKindUtilities.isFile(referencedObject)) {
                csmObject = referencedObject;
            }
        }
        this.origObject = csmObject;
        this.dispOldName = this.oldName = CsmRefactoringUtils.getSimpleText(this.origObject);
        this.newName = newName;
        this.fromListener = (newName != null);
        Lookup lkp = null;
        if (CsmKindUtilities.isFile(csmObject)) {
            // name is without extension, because later it will be passed to usual rename
            // refactoring as well which make real file rename and has ability to undo it
            FileObject fo = CsmUtilities.getFileObject(((CsmFile)csmObject));
            if (fo != null) {
                lkp = Lookups.fixed(csmObject, fo);
            }
        }
        if (lkp == null) {
           lkp = Lookups.singleton(csmObject);
        }
        this.refactoring = new RenameRefactoring(lkp);
    }
     
//    public RenameRefactoringUI(TreePathHandle handle, CompilationInfo info) {
//        this.handle = handle;
//        this.refactoring = new RenameRefactoring(Lookups.singleton(handle));
//        Element element = handle.resolveElement(info);
//        oldName = element.getSimpleName().toString();
//        if (element.getModifiers().contains(Modifier.PRIVATE)) {
//            refactoring.getContext().add(RetoucheUtils.getClasspathInfoFor(false, handle.getFileObject()));
//        } else {
//            refactoring.getContext().add(RetoucheUtils.getClasspathInfoFor(handle));
//        }
//        dispOldName = oldName;
//    }
    
//    public RenameRefactoringUI(FileObject file, TreePathHandle handle, CompilationInfo info) {
//        if (handle!=null) {
//            this.handle = handle;
//            this.refactoring = new RenameRefactoring(Lookups.fixed(file, handle));
//            oldName = handle.resolveElement(info).getSimpleName().toString();
//        } else {
//            this.refactoring = new RenameRefactoring(Lookups.fixed(file));
//            oldName = file.getName();
//        }
//        dispOldName = oldName;
//        ClasspathInfo cpInfo = handle==null?RetoucheUtils.getClasspathInfoFor(file):RetoucheUtils.getClasspathInfoFor(handle);
//        refactoring.getContext().add(cpInfo);
//    }

//    public RenameRefactoringUI(NonRecursiveFolder file) {
//        this.refactoring = new RenameRefactoring(Lookups.singleton(file));
//        oldName = RetoucheUtils.getPackageName(file.getFolder());
//        refactoring.getContext().add(RetoucheUtils.getClasspathInfoFor(file.getFolder()));
//        dispOldName = oldName;
//        pkgRename = true;
//    }

//    RenameRefactoringUI(FileObject jmiObject, String newName, TreePathHandle handle, CompilationInfo info) {
//        if (handle!=null) {
//            this.refactoring = new RenameRefactoring(Lookups.fixed(jmiObject, handle));
//        } else {
//            this.refactoring = new RenameRefactoring(Lookups.fixed(jmiObject));
//        }
//        
//        oldName = newName;
//        //[FIXME] this should be oldName of refactored object
//        this.dispOldName = newName;
//        ClasspathInfo cpInfo = handle==null?RetoucheUtils.getClasspathInfoFor(jmiObject):RetoucheUtils.getClasspathInfoFor(handle);
//        refactoring.getContext().add(cpInfo);
//        fromListener = true;
//    }
    
//    RenameRefactoringUI(NonRecursiveFolder jmiObject, String newName) {
//        this.refactoring = new RenameRefactoring(Lookups.singleton(jmiObject));
//        refactoring.getContext().add(RetoucheUtils.getClasspathInfoFor(jmiObject.getFolder()));
//        oldName = newName;
//        //[FIXME] this should be oldName of refactored object
//        this.dispOldName = newName;
//        fromListener = true;
//        pkgRename = true;
//    }
       
    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            String name = fromListener ? newName : oldName;
            String title = NbBundle.getMessage(RenamePanel.class, "LBL_RenamePanelTitle", "", oldName); // NOI18N
            panel = new RenamePanel(this.origObject, name, parent, title, !fromListener, fromListener && !byPassPakageRename);
        }
        return panel;
    }
    
    private static String getString(String key) {
        return NbBundle.getMessage(RenameRefactoringUI.class, key);
    }

    @Override
    public org.netbeans.modules.refactoring.api.Problem setParameters() {
        if (!panel.isUpdateReferences()) {
            return null;
        }
        newName = panel.getNameValue();
        if (refactoring instanceof RenameRefactoring) {
            ((RenameRefactoring) refactoring).setNewName(newName);
            ((RenameRefactoring) refactoring).setSearchInComments(panel.searchInComments());            
        }
        return refactoring.checkParameters();
    }
    
    @Override
    public org.netbeans.modules.refactoring.api.Problem checkParameters() {
        if (!panel.isUpdateReferences()) {
            return null;
        }
        newName = panel.getNameValue();
        if (refactoring instanceof RenameRefactoring) {
            ((RenameRefactoring) refactoring).setNewName(newName);
        }
        return refactoring.fastCheckParameters();
    }

    @Override
    public org.netbeans.modules.refactoring.api.AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    @Override
    public String getDescription() {
        return new MessageFormat(NbBundle.getMessage(RenamePanel.class, "DSC_Rename")).format (
                    new Object[] {dispOldName, newName}
                );
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(RenamePanel.class, "LBL_RefactoringRenameName", oldName, newName);
    }

    @Override
    public boolean hasParameters() {
        return true;
    }

    @Override
    public HelpCtx getHelpCtx() {
        String postfix = "";
//        if (handle==null) {
//            postfix = ".JavaPackage";//NOI18N
//        } else {
//            ElementKind k = RetoucheUtils.getElementKind(handle);
//            
//            if (k.isClass() || k.isInterface())
//                postfix = ".JavaClass";//NOI18N
//            else if (k == ElementKind.METHOD)
//                postfix = ".Method";//NOI18N
//            else if (k.isField())
//                postfix = ".Field";//NOI18N
//            else
//                postfix = "";
//        }
        
        return new HelpCtx(RenameRefactoringUI.class.getName() + postfix);
    }
    
    @Override
    public boolean isRefactoringBypassRequired() {
        return !panel.isUpdateReferences();
    }
    
    @Override
    public void doRefactoringBypass() throws IOException {
        DataObject dob = null;
        if (byPassFolder != null) {
            dob = DataFolder.findFolder(byPassFolder);
        } else {
            dob = DataObject.find(refactoring.getRefactoringSource().lookup(FileObject.class));
        }
        dob.rename(panel.getNameValue());
    }
}
