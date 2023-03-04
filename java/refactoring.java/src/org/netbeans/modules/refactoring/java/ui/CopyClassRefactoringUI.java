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
package org.netbeans.modules.refactoring.java.ui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.swing.event.ChangeListener;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.SingleCopyRefactoring;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUIBypass;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.*;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

/**
 * Refactoring UI object for Copy Class refactoring.
 *
 * @author Jan Becicka
 */
public class CopyClassRefactoringUI implements RefactoringUI, RefactoringUIBypass, JavaRefactoringUIFactory {
    // reference to pull up refactoring this UI object corresponds to

    private SingleCopyRefactoring refactoring;
    // UI panel for collecting parameters
    private MoveClassPanel panel;
    private FileObject resource;
    private FileObject targetFolder;
    private Lookup lookup;
    private boolean needsByPass;

    private CopyClassRefactoringUI(FileObject resource, FileObject target) {
        this(resource, target, false);
    }

    private CopyClassRefactoringUI(FileObject resource, FileObject target, boolean needsByPass) {
        refactoring = new SingleCopyRefactoring(Lookups.singleton(resource));
        this.resource = resource;
        this.targetFolder = target;
        this.needsByPass = needsByPass;
    }
    
    private CopyClassRefactoringUI(Lookup lookup) {
        this.lookup = lookup;
    }

    // --- IMPLEMENTATION OF RefactoringUI INTERFACE ---------------------------
    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            FileObject target = targetFolder != null ? targetFolder : resource.getParent();
            panel = new MoveClassPanel(parent,
                    RefactoringUtils.getPackageName(target),
                    getName() + " - " + resource.getName(), // NOI18N
                    NbBundle.getMessage(CopyClassRefactoringUI.class, "LBL_CopyWithoutRefactoring"),
                    target, resource.getName(), false);
            panel.setCombosEnabled(!(targetFolder != null));
            panel.setRefactoringBypassRequired(needsByPass);
        }
        return panel;
    }

    @Override
    public Problem setParameters() {
        setupRefactoring();
        return refactoring.checkParameters();
    }

    @Override
    public Problem checkParameters() {
        if (panel == null) {
            return null;
        }
        setupRefactoring();
        return refactoring.fastCheckParameters();
    }

    private void setupRefactoring() {
        refactoring.setNewName(panel.getNewName());
        FileObject rootFolder = panel.getRootFolder();
        Lookup target = Lookup.EMPTY;
        if (rootFolder != null) {
            try {
                URL url = URLMapper.findURL(rootFolder, URLMapper.EXTERNAL);
                URL targetURL = new URL(url.toExternalForm() + panel.getPackageName().replace('.', '/')); // NOI18N
                target = Lookups.singleton(targetURL);
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        refactoring.setTarget(target);
    }

    @Override
    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(CopyClassRefactoringUI.class, "DSC_CopyClass", refactoring.getNewName()); // NOI18N
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(CopyClassRefactoringUI.class, "LBL_CopyClass"); // NOI18N
    }

    @Override
    public boolean hasParameters() {
        return true;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.refactoring.java.ui.CopyClassRefactoringUI"); // NOI18N
    }

    @Override
    public boolean isRefactoringBypassRequired() {
        return needsByPass || panel != null && panel.isRefactoringBypassRequired();
    }

    @Override
    public void doRefactoringBypass() throws IOException {
        RequestProcessor.getDefault().post(new Runnable() {

            @Override
            public void run() {
                try {
                    //Transferable t = paste.paste();
                    FileObject source = refactoring.getRefactoringSource().lookup(FileObject.class);
                    if (source != null) {
                        DataObject sourceDo = DataObject.find(source);
                        DataFolder targetDataFolder = DataFolder.findFolder(targetFolder);
                        sourceDo.copy(targetDataFolder).rename(panel.getNewName());
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }

    @Override
    public RefactoringUI create(CompilationInfo info, TreePathHandle[] handles, FileObject[] files, NonRecursiveFolder[] packages) {

        PasteType paste = RefactoringActionsProvider.getPaste(lookup);
        FileObject tar = RefactoringActionsProvider.getTarget(lookup);
        if (files != null && (files.length > 1 || files.length == 1 && files[0].isFolder())) {
            Set<FileObject> s = new HashSet<FileObject>();
            s.addAll(Arrays.asList(files));
            return new CopyClassesUI(s, tar, paste);
        }
        if(handles.length < 1) {
            if(tar != null) {
                assert files.length > 0;
                return new CopyClassRefactoringUI(files[0], tar, !files[0].getNameExt().equals("package-info.java"));
            } else {
                return null;
            }
        }
        
        if(info == null) {
            return new CopyClassRefactoringUI(handles[0].getFileObject(), tar);
        }
        
        TreePathHandle selectedElement = handles[0];
        Element e = selectedElement.resolveElement(info);
        if (e == null) {
            return null;
        }
        if ((e.getKind().isClass() || e.getKind().isInterface())
                && SourceUtils.getOutermostEnclosingTypeElement(e) == e) {
            try {
                FileObject fo = SourceUtils.getFile(e, info.getClasspathInfo());
                if (fo != null) {
                    DataObject d = DataObject.find(SourceUtils.getFile(e, info.getClasspathInfo()));
                    if (d.getName().equals(e.getSimpleName().toString())) {
                        return new CopyClassRefactoringUI(d.getPrimaryFile(), tar);
                    }
                }
            } catch (DataObjectNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        }
        return new CopyClassRefactoringUI(info.getFileObject(), tar);

    }
    
    public static JavaRefactoringUIFactory factory(Lookup lookup) {
        return new CopyClassRefactoringUI(lookup);
    }
}
