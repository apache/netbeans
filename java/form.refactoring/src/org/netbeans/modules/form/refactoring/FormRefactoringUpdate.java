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
package org.netbeans.modules.form.refactoring;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.form.FormDataObject;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.form.PersistenceException;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.RenameSupport;
import org.netbeans.modules.form.ResourceSupport;
import org.netbeans.modules.nbform.FormEditorSupport;
import org.netbeans.modules.refactoring.api.SingleCopyRefactoring;
import org.netbeans.modules.refactoring.spi.BackupFacility;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * This class does the actual refactoring changes for one form - updates the
 * form, regenerates code, updates properties files for i18n, etc. Multiple
 * different instances (updates) can be created and executed for one refactoring
 * (all kept in RefacoringInfo).
 * 
 * @author Tomas Pavek
 */
public class FormRefactoringUpdate extends SimpleRefactoringElementImplementation implements Transaction {

    /**
     * Information about the performed refactoring.
     */
    private RefactoringInfo refInfo;

    /**
     * RefactoringElement used in the preview, but doing nothing.
     */
    private RefactoringElementImplementation previewElement;

    /**
     * Java file of a form affected by the refactoring.
     */
    private FileObject changingFile;

    /**
     * DataObject of the changed file. Has changedFile as primary file at the
     * beginning, but may get a different one later (e.g. if moved).
     */
    private FormDataObject formDataObject;

    /**
     * FormEditor of the updated form. Either taken from the FormDataObject
     * (typically when already opened), or created temporarily just to do the
     * update. See prepareForm method.
     */
    private FormEditor formEditor;

    private boolean loadingFailed;

    /**
     * Whether a change in guarded code was requested by java refactoring.
     */
    private boolean guardedCodeChanging;

    private boolean transactionDone;

    private boolean formFileRenameDone;

    private List<BackupFacility.Handle> backups;

    // -----

    public FormRefactoringUpdate(RefactoringInfo refInfo, FileObject changingFile) {
        this.refInfo = refInfo;
        this.changingFile = changingFile;
        try {
            DataObject dobj = DataObject.find(changingFile);
            if (dobj instanceof FormDataObject) {
                formDataObject = (FormDataObject) dobj;
            }
        } catch(DataObjectNotFoundException ex) {
            assert false;
        }
    }

    FormDataObject getFormDataObject() {
        return formDataObject;
    }

    RefactoringElementImplementation getPreviewElement(/*String displayText*/) {
        if (previewElement == null) {
            previewElement = new PreviewElement(changingFile/*, displayText*/);
        }
        return previewElement;
    }

    void setGaurdedCodeChanging(boolean b) {
        guardedCodeChanging = b;
    }

    boolean isGuardedCodeChanging() {
        return guardedCodeChanging;
    }

    // -----

    // Transaction (registered via RefactoringElementsBag.registerTransaction)
    @Override
    public void commit() {
        if (previewElement != null && !previewElement.isEnabled()) {
            return;
        }

        // As "transactions" we do updates for changes affecting only the
        // content of the source file, not changing the file's name or location.
        // Our transaction is called after retouche commits its changes to the
        // source. After all transactions are done, the source file is saved
        // automatically.

        for (FileObject originalFile : refInfo.getOriginalFiles()) {
            // (Actually more original files make only sense for this form if
            // they represent components used in this form and moved...)
            switch (refInfo.getChangeType()) {
            case VARIABLE_RENAME: // renaming a variable (just one)
                if (originalFile.equals(changingFile)) {
                    renameMetaComponent(refInfo.getOldName(originalFile), refInfo.getNewName());
                    transactionDone = true;
                }
                break;
            case CLASS_RENAME: // renaming a component class used in the form (just one)
                if (!originalFile.equals(changingFile)) {
                    componentClassRename(originalFile);
                    transactionDone = true;
                }
                break;
            case CLASS_MOVE: // moving a class used in the form (there can be more of them)
                if (!originalFile.equals(changingFile) && isGuardedCodeChanging()) {
                    componentChange(refInfo.getOldName(originalFile), refInfo.getNewName(originalFile));
                    transactionDone = !refInfo.containsOriginalFile(changingFile);
                    // If a form is moved together with other java classes, it needs
                    // to be checked here but also processed later in performChange
                    // method. If it contained some of the moved components, we will
                    // not be able to load it. But that is not for sure, so will try
                    // anyway, at worst it won't be processed.
                }
                break;
            case CLASS_DELETE: // deleting form (more can be deleted, but here we only care about this form)
                if (originalFile.equals(changingFile)) {
                    saveFormForUndo(); // we only need to backup the form file for undo
                    transactionDone = true;
                }
                break;
            case PACKAGE_RENAME:
            case FOLDER_RENAME:  // renaming package of a component used in the form,
                                 // but not the package of the form itself
                                 // (just one package renamed)
                if (!changingFile.getParent().equals(originalFile)) {
                    packageRename(originalFile);
                    transactionDone = true;
                }
                break;
            default:
                // do nothing otherwise - could be just redundantly registered by the guarded handler
                return;
            }
        }
    }

    // Transaction (registered via RefactoringElementsBag.registerTransaction)
    @Override
    public void rollback() {
        if (previewElement != null && !previewElement.isEnabled()) {
            return;
        }
        undoFromBackups();
/*        switch (refInfo.getChangeType()) {
        case VARIABLE_RENAME:
            renameMetaComponent(refInfo.getNewName(), refInfo.getOldName());
            break;
        case CLASS_RENAME: // renaming a component class used in the form
            if (!refInfo.getPrimaryFile().equals(changingFile)) {
                componentClassRename(refInfo.getNewName(), refInfo.getOldName());
            }
            break;
        case CLASS_MOVE: // moving a component class used in the form
            if (!refInfo.getPrimaryFile().equals(changingFile)) {
                componentChange(refInfo.getNewName(), refInfo.getOldName());
            }
            break;
        } */
    }

    // RefactoringElementImplementation (registered via RefactoringElementsBag.addFileChange)
    @Override
    public void performChange() {
        if (previewElement != null && !previewElement.isEnabled()) {
            return;
        }
        if (transactionDone) { // could be registered redundantly as file change
            processCustomCode();
            return;
        }

        // As "file changes" we do updates that react on changes of the source
        // file's name or location. We need the source file to be already
        // renamed/moved. The file changes are run after the "transactions".

        for (FileObject originalFile : refInfo.getOriginalFiles()) {
            // Looking through if this form is among original files - i.e. the
            // one being changed.
            switch (refInfo.getChangeType()) {
            case CLASS_RENAME: // renaming the form itself
                if (originalFile.equals(changingFile)) {
                    formRename();
                }
                break;
            case CLASS_MOVE: // moving the form itself
                if (originalFile.equals(changingFile) && prepareForm(false)) {
                    formMove();
                }
                break;
            case CLASS_COPY: // copying the form itslef
                if (originalFile.equals(changingFile) && prepareForm(false)) {
                    formCopy();
                }
                break;
            case PACKAGE_RENAME: // renaming package of the form (just one)
            case FOLDER_RENAME:
                packageRename(originalFile);
                break;
            }
        }

        processCustomCode();
    }

    // RefactoringElementImplementation (registered via RefactoringElementsBag.addFileChange)
    @Override
    public void undoChange() {
        if (previewElement != null && !previewElement.isEnabled()) {
            return;
        }
        if (transactionDone) { // could be registered redundantly as file change
            return;
        }

        undoFromBackups();
    }

    // -----

    private void renameMetaComponent(String oldName, String newName) {
        if (prepareForm(true)) {
            RADComponent metacomp = formEditor.getFormModel().findRADComponent(oldName);
            if (metacomp != null) {
                saveFormForUndo();
                saveResourcesForContentChangeUndo();
                metacomp.setName(newName);
                updateForm(false);
            }
        }
    }

    private void formRename() {
        if (prepareForm(true)) {
            saveFormForUndo();
            saveResourcesForFormRenameUndo();
            ResourceSupport.formMoved(formEditor.getFormModel(), null, refInfo.getOldName(changingFile), false);
            updateForm(true);
        }
    }

    private void componentClassRename(FileObject originalFile) {
        String oldName = refInfo.getOldName(originalFile);
        String newName = refInfo.getNewName();
        String pkg = ClassPath.getClassPath(originalFile, ClassPath.SOURCE)
                .getResourceName(originalFile.getParent(), '.', false);
        String oldClassName = (pkg != null && pkg.length() > 0)
                ? pkg + "." + oldName : oldName; // NOI18N
        String newClassName = (pkg != null && pkg.length() > 0)
                ? pkg + "." + newName : newName; // NOI18N
        componentChange(oldClassName, newClassName);
    }

    private FormEditorSupport getFormEditorSupport() {
        return (FormEditorSupport)formDataObject.getFormEditorSupport();
    }

    private void formMove(/*final boolean saveAll*/) {
        final FormEditorSupport fes = getFormEditorSupport();
        if (fes.isOpened()) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    formEditor = fes.reloadFormEditor();
                    formMove2(/*saveAll*/);
                }
            });
        } else {
            assert !formEditor.isFormLoaded();
            formMove2(/*saveAll*/);
        }
    }

    private void formMove2(/*boolean saveAll*/) {
        if (prepareForm(true)) {
            saveFormForUndo();
            FileObject oldFolder = changingFile.getParent();
            saveResourcesForFormMoveUndo(oldFolder);
            String oldFormName = refInfo.getOldName(changingFile);
            oldFormName = oldFormName.substring(oldFormName.lastIndexOf('.')+1); // should be a short name
            ResourceSupport.formMoved(formEditor.getFormModel(), oldFolder, oldFormName, false);
            updateForm(true);
        }
    }

    private void formCopy() {
        if (refInfo.getRefactoring() instanceof SingleCopyRefactoring) {
            FileObject oldFile = changingFile;
            FormDataObject oldForm = formDataObject;
            FileObject oldFolder = changingFile.getParent();

            SingleCopyRefactoring copyRef = (SingleCopyRefactoring)refInfo.getRefactoring();
            String newName = copyRef.getNewName(); // short name without extension
            Lookup target = copyRef.getTarget();
            FileObject targetFolder = URLMapper.findFileObject((URL)target.lookup(URL.class));
            // will process the new copy - update changingFile and formDataObject fields
            changingFile = targetFolder.getFileObject(newName, "java"); // NOI18N
            try {
                DataObject dobj = DataObject.find(changingFile);
                if (dobj instanceof FormDataObject) {
                    formDataObject = (FormDataObject) dobj;
                }
            } catch(DataObjectNotFoundException ex) {
                assert false;
            }
            formEditor = null;
            if (prepareForm(true)) {
                saveResourcesForFormRenameUndo(); // same set of files like if the new form was renamed
                if (oldFolder == targetFolder) {
                    oldFolder = null;
                }
                ResourceSupport.formMoved(formEditor.getFormModel(), oldFolder, oldFile.getName(), true);
                updateForm(true);
            }
            // set back to original so the operation can be repeated in redo
            changingFile = oldFile;
            formDataObject = oldForm;
        }
    }

    private void componentChange(String oldClassName, String newClassName) {
        if (oldClassName == null || newClassName == null) {
            return; // for unknown reason 'newClassName' is sometimes null during move refactoring, issue 174136
        }

        FormEditorSupport fes = getFormEditorSupport();
        if (fes.isOpened()) {
            fes.closeFormEditor();
        }
        String[] oldNames = new String[] { oldClassName };
        String[] newNames = new String[] { newClassName };
        replaceClassOrPkgName(oldNames, newNames, false);
        replaceShortClassName(oldNames, newNames);
        // (Only updating form file, java code gets updated via GuardedBlockUpdate)
    }

    private boolean replaceShortClassName(String[] oldNames, String[] newNames) {
        List<String> oldList = new LinkedList<String>();
        List<String> newList = new LinkedList<String>();
        for (int i=0; i < oldNames.length; i++) {
            if (oldNames[i].contains(".")) { // NOI18N
                String shortOldName = oldNames[i].substring(oldNames[i].lastIndexOf('.')+1);
                String shortNewName = newNames[i].substring(newNames[i].lastIndexOf('.')+1);
                if (!shortNewName.equals(shortOldName)) {
                    oldList.add(shortOldName);
                    newList.add(newNames[i]); // intentionally replace with FQN
                }
            }
        }
        if (!oldList.isEmpty()) {
            oldNames = oldList.toArray(new String[0]);
            newNames = newList.toArray(new String[0]);
            return replaceClassOrPkgName(oldNames, newNames, false);
        }
        return false;
    }

    private void packageRename(FileObject originalPkgFile) {
        FormEditorSupport fes = getFormEditorSupport();
        if (fes.isOpened()) {
            fes.closeFormEditor();
        }
        String oldName = refInfo.getOldName(originalPkgFile);
        String newName = refInfo.getNewName();
        if (refInfo.getChangeType() == RefactoringInfo.ChangeType.FOLDER_RENAME) {
            // determine full package name for renamed folder
            ClassPath cp = ClassPath.getClassPath(originalPkgFile, ClassPath.SOURCE);
            FileObject parent = originalPkgFile.getParent();
            if (cp != null && cp.contains(parent)) {
                String parentPkgName = cp.getResourceName(parent, '.', false);
                if (parentPkgName != null && parentPkgName.length() > 0) {
                    oldName = parentPkgName + "." + oldName; // NOI18N
                    newName = parentPkgName + "." + newName; // NOI18N
                }
            }
        }
        if (replaceClassOrPkgName(new String[] { oldName },
                                  new String[] { newName },
                                  true)
                && !isGuardedCodeChanging()) {
            // some package references in resource were changed in the form file
            // (not class names since no change in guarded code came from java
            // refactoring) and because no component has changed we can load the
            // form and regenerate to get the new resource names into code
            updateForm(true);
        }
    }

    /**
     * Tries to update the fragments of custom code in the .form file according
     * to the refactoring change. The implementation is quite simple and 
     * super-ugly. It goes through the form file, finds relevant attributes,
     * and blindly replaces given "old name" with a "new name". Should mostly
     * work when a component variable or class is renamed. Should be enough
     * though, since the usage of custom code is quite limited.
     */
    private void processCustomCode() {
        if (isGuardedCodeChanging() && !formFileRenameDone) {
            boolean replaced = false;
            List<String> oldList = new LinkedList<String>();
            List<String> newList = new LinkedList<String>();
            for (FileObject originalFile : refInfo.getOriginalFiles()) {
                String oldName = refInfo.getOldName(originalFile);
                String newName = refInfo.getNewName(originalFile);
                if (oldName != null && newName != null) {
                    oldList.add(oldName);
                    newList.add(newName);
                }
            }
            if (!oldList.isEmpty()) {
                String[] oldNames = oldList.toArray(new String[0]);
                String[] newNames = newList.toArray(new String[0]);
                replaced |= replaceClassOrPkgName(oldNames, newNames, false);
               // also try to replace short class name
                switch (refInfo.getChangeType()) {
                case CLASS_RENAME:
                case CLASS_MOVE:
                    replaced |= replaceShortClassName(oldNames, newNames);
                    break;
                }
            }
            if (replaced) { // regenerate the code
                // need to reload the form from file
                final FormEditorSupport fes = getFormEditorSupport();
                if (fes.isOpened()) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            formEditor = fes.reloadFormEditor();
                            updateForm(true);
                        }
                    });
                } else {
                    if  (formEditor != null && formEditor.isFormLoaded()) {
                        formEditor.closeForm();
                    }
                    if (prepareForm(true)) {
                        updateForm(true);
                    }
                }
            }
            formFileRenameDone = false; // not to block redo
        }
    }

    // -----

    /**
     * Regenerate code and save.
     */
    private void updateForm(boolean saveAll) {
        if (!prepareForm(true)) {
            return;
        }
        // hack: regenerate code immediately
        formEditor.getFormModel().fireFormChanged(true);
        FormEditorSupport fes = getFormEditorSupport();
        try {
            if (!fes.isOpened()) {
                // the form is not opened, just loaded aside to do this refactoring
                // update (not held from FormEditorSupport); so we must save the
                // form always - it would not get save with refactoring
                formEditor.saveFormData(); // TODO should save form only if there was a change
                if (saveAll) { // a post-refactoring change that would not be saved by refactoring
                    fes.saveSourceOnly();
                }
                formEditor.closeForm();
            } else if (saveAll) { // a post-refactoring change that would not be saved
                fes.saveDocument();
            }
        } catch (PersistenceException pex) {
            Exceptions.printStackTrace(pex);
        } catch (IOException ioex) {
            Exceptions.printStackTrace(ioex);
        }
    }

    boolean prepareForm(boolean load) {
        if (formDataObject != null) {
            FormEditor fe = getFormEditorSupport().getFormEditor();
            if (fe != null) { // use the current FormEditor (might change due to reload after undo)
                formEditor = fe;
            } else if (formEditor == null) { // create a disconnected form editor
                formEditor = new FormEditor(formDataObject, formDataObject.getFormEditorSupport());
            }
        }
        if (formEditor != null) {
            if (formEditor.isFormLoaded() || !load) {
                return true;
            } else if (!loadingFailed) {
                if (formEditor.loadForm()) {
                    if (formEditor.anyPersistenceError()) { // Issue 128504
                        formEditor.closeForm();
                        loadingFailed = true;
                    } else {
                        return true;
                    }
                } else {
                    loadingFailed = true;
                }
            }
        }
        return false;
    }

    private void saveFormForUndo() {
        if (!formDataObject.isValid()) {
            // 210787: Refresh formDataObject if it became obsolete
            FileObject fob = formDataObject.getPrimaryFile();
            if (!fob.isValid()) {
                File file = FileUtil.toFile(fob);
                fob = FileUtil.toFileObject(file);
            }
            try {
                formDataObject = (FormDataObject)DataObject.find(fob);
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        saveForUndo(formDataObject.getFormFile());
        // java file is backed up by java refactoring
    }

    private void saveResourcesForContentChangeUndo() {
        for (URL url : ResourceSupport.getFilesForContentChangeBackup(formEditor.getFormModel())) {
            saveForUndo(url);
        }
    }

    private void saveResourcesForFormRenameUndo() {
        for (URL url : ResourceSupport.getFilesForFormRenameBackup(formEditor.getFormModel())) {
            saveForUndo(url);
        }
    }

    private void saveResourcesForFormMoveUndo(FileObject oldFolder) {
        for (URL url : ResourceSupport.getFilesForFormMoveBackup(formEditor.getFormModel(), oldFolder)) {
            saveForUndo(url);
        }
    }

    private void saveForUndo(final URL url) {
        FileObject file = URLMapper.findFileObject(url);
        BackupFacility.Handle id;
        if (file != null) {
            try {
                id = BackupFacility.getDefault().backup(file);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return;
            }
        } else { // file does not exist - will be created; to undo we must delete it
           id = new BackupFacility.Handle() {
                @Override
                public void restore() throws IOException {
                    FileObject file = URLMapper.findFileObject(url);
                    if (file != null) {
                        file.delete();
                    }
                }
           };
        }
        if (backups == null) {
            backups = new ArrayList<BackupFacility.Handle>();
        }
        backups.add(id);
    }

    private void saveForUndo(FileObject file) {
        try {
            BackupFacility.Handle id = BackupFacility.getDefault().backup(file);
            if (backups == null) {
                backups = new ArrayList<BackupFacility.Handle>();
            }
            backups.add(id);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void undoFromBackups() {
        if (backups != null) {
            try {
                for (BackupFacility.Handle id : backups) {
                    id.restore();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            backups.clear();
        }
    }

    // -----

    private static class PreviewElement extends SimpleRefactoringElementImplementation {
        private FileObject file;

        PreviewElement(FileObject file) {
            this.file = file;
        }

        @Override
        public String getText() {
            return "GUI form update"; // NOI18N
        }

        @Override
        public String getDisplayText() {
            return NbBundle.getMessage(FormRefactoringUpdate.class, "CTL_RefactoringUpdate1"); // NOI18N
        }

        @Override
        public void performChange() {
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @Override
        public FileObject getParentFile() {
            return file;
        }

        @Override
        public PositionBounds getPosition() {
            return null;
        }
    }

    // -----

    // RefactoringElementImplementation
    @Override
    public String getText() {
        return "GUI form update";
    }

    // RefactoringElementImplementation
    @Override
    public String getDisplayText() {
        return NbBundle.getMessage(FormRefactoringUpdate.class, "CTL_RefactoringUpdate2"); // NOI18N
    }

    // RefactoringElementImplementation
    @Override
    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

    // RefactoringElementImplementation
    @Override
    public FileObject getParentFile() {
        return changingFile;
    }

    // RefactoringElementImplementation
    @Override
    public PositionBounds getPosition() {
        return null;
    }

   // -----

    private boolean replaceClassOrPkgName(String[] oldNames, String[] newNames, boolean pkgName) {
        FileObject formFile = formDataObject.getFormFile();
        FileLock lock = null;
        OutputStream os = null;
        try {
            lock = formFile.lock();
            String outString = RenameSupport.renameInFormFile(formFile, oldNames, newNames, pkgName);
            if (outString != null) {
                saveForUndo(formFile);
                os = formFile.getOutputStream(lock);
                os.write(outString.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException ex) { // ignore
            }
            if (lock != null) {
                lock.releaseLock();
            }
        }
        formFileRenameDone = true; // we don't need to do processCustomCode
        return true;
    }
}
