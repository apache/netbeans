/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject.api.ui;

import java.beans.PropertyChangeEvent;
import java.util.logging.Level;
import org.netbeans.modules.cnd.api.project.NativeFileItemSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.MIMESupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 */
public class ItemEx extends Item {
    
    @org.openide.util.lookup.ServiceProvider(service=Item.ItemFactory.class)
    public static final class ItemFactoryEx extends ItemFactory {

        @Override
        public Item createInBaseDir(FileObject baseDirFileObject, String path) {
            return new ItemEx(baseDirFileObject, path);
        }

        @Override
        public Item createInFileSystem(FileSystem fileSystem, String path) {
            return new ItemEx(fileSystem, path);
        }

        @Override
        public Item createDetachedViewItem(FileSystem fileSystem, String path) {
            CndUtils.assertNonUiThread();
            ItemEx out = new ItemEx(fileSystem, path);
            // This method is executed in not EDT and first call to getDataObject() is quite expensive operation.
            // If we call this method here then result will be calculated and cached. So cached version will be
            // used in createNodes and won't freeze EDT.
            // See Bug 221962 - [73cat] 3.s - Blocked by cnd.makeproject.ui.LogicalViewChildren.createNodes().
            DataObject dobj = out.getDataObject();
            // detach resources to prevent memory leaks
            out.detachFrom(dobj);
            CndUtils.assertTrueInConsole(out.lastDataObject == dobj, "data object should stay the same ", out.lastDataObject);
            return out;
        }
    }
    
    private DataObject lastDataObject = null;

    private ItemEx(FileObject baseDirFileObject, String path) {
        super(baseDirFileObject, path);
    }

    // XXX:fullRemote deprecate and remove!
    private ItemEx(FileSystem fileSystem, String path) {
        super(fileSystem, path);
    }

    @Override
    public void setFolder(Folder folder) {
        if (folder == null && canonicalFileObject == null) {
            // store file in field. method getFile() will works after removing item
            ensureFileNotNull();
        }
        // leave folder if it is remove
        if (folder == null) { // Item is removed, let's clean up.
            synchronized (this) {
                detachFrom(lastDataObject);
                lastDataObject = null;
            }
        } else {
            this.folder = folder;
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("name")) { // NOI18N
            // File has been renamed.
            String newName = (String) evt.getNewValue();
            boolean nameWithoutExtension = true;
            Object o = evt.getSource();
            // New name is unpedictable. It can contain or not contain extension.
            // Detect true name with extension.
            if (o instanceof DataObject) {
                FileObject fo = ((DataObject) o).getPrimaryFile();
                if (fo != null) {
                    newName = fo.getNameExt();
                    nameWithoutExtension = false;
                }
            }
            rename(newName, nameWithoutExtension);
        } else if (evt.getPropertyName().equals("valid")) { // NOI18N
            if (!((Boolean) evt.getNewValue())) {
                // Data object invalid.
                // It may be renaming.
                Object o = evt.getSource();
                if (o instanceof DataObject) {
                    DataObject dao = ((DataObject) o);
                    FileObject fo = dao.getPrimaryFile();
                    if (fo != null && fo.isValid()) {
                        // Old data object invalid and valid file object.
                        // Rename and attach new data object.
                        rename(fo.getNameExt(), false);
                        DataObject dataObject;
                        try {
                            dataObject = DataObject.find(fo);
                            synchronized (this) {
                                if (dataObject != lastDataObject) {
                                    detachFrom(lastDataObject);
                                    attachTo(dataObject);
                                }
                            }
                        } catch (DataObjectNotFoundException ex) {
                            LOG.log(Level.FINE, "Can not find data object", ex); //NOI18N
                        }
                        return;
                    }
                }
                // File has been deleted.
                // Refresh folder. See also IZ 87557 and IZ 94935
                Folder containingFolder = getFolder();
                if (containingFolder != null) {
                    containingFolder.refresh(this);
                }
            } else {
                // Data object valid.
                // Attach new data object.
                Object o = evt.getSource();
                if (o instanceof DataObject) {
                    DataObject dao = ((DataObject) o);
                    synchronized (this) {
                        if (lastDataObject != null) {
                            detachFrom(lastDataObject);
                        }
                        lastDataObject = dao;
                        attachTo(lastDataObject);
                    }
                }
            }
        } else if (evt.getPropertyName().equals("primaryFile")) { // NOI18N
            // File has been moved.
            if (getFolder() != null) {
                FileObject fo = (FileObject) evt.getNewValue();
                String newPath = fo.getPath();
                if (!CndPathUtilities.isPathAbsolute(fileSystem, getPath())) {
                    newPath = CndPathUtilities.toRelativePath(getFolder().getConfigurationDescriptor().getBaseDirFileObject(), newPath);
                }
                newPath = CndPathUtilities.normalizeSlashes(newPath);
                renameTo(newPath);
            }
        }
    }

    public DataObject getDataObject() {
        synchronized (this) {
            if (lastDataObject != null && lastDataObject.isValid()) {
                return lastDataObject;
            }
        }
        DataObject dataObject = null;
        FileObject fo = getFileObjectImpl();
        if (fo != null && fo.isValid()) {
            try {
                dataObject = DataObject.find(fo);
            } catch (DataObjectNotFoundException e) {
                // that's normal, for example, "myfile.xyz" won't have data object
                // ErrorManager.getDefault().notify(e);
                LOG.log(Level.FINE, "Can not find data object", e); //NOI18N
            }
        }
        synchronized (this) {
            if (dataObject != lastDataObject) {
                // DataObject can change without notification. We need to track this
                // and properly attach/detach listeners.
                detachFrom(lastDataObject);
                attachTo(dataObject);
            }
        }
        return dataObject;
    }
    
    private void attachTo(DataObject dataObject) {
        if (dataObject != null) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "attaching {0} to {1}", new Object[]{System.identityHashCode(this), dataObject});
            }
            dataObject.removePropertyChangeListener(this);
            dataObject.addPropertyChangeListener(this);
            NativeFileItemSet set = dataObject.getLookup().lookup(NativeFileItemSet.class);
            if (set != null) {
                set.add(this);
            }
        }
        lastDataObject = dataObject;
    }
    
    @Override
    public final void onOpen() {
        synchronized (this) {
            // attach only if was initialized
            attachTo(lastDataObject);
        }
    }
    
    private void detachFrom(DataObject dao) {
        if (dao != null) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "detaching {0} from {1}", new Object[]{System.identityHashCode(this), dao});
            }
            dao.removePropertyChangeListener(this);
            NativeFileItemSet set = dao.getLookup().lookup(NativeFileItemSet.class);
            if (set != null) {
                set.remove(this);
            }
        }
    }
    
    @Override
    protected void onClose() {
        synchronized (this) {
            // detach but leave object reference for further possible reopen
            detachFrom(lastDataObject);
        }
    }
    
    @Override
    public String getMIMEType() {
        // use file object of this item
        return getMIMETypeImpl(this.getDataObject(), this);
    }
    
    private static String getMIMETypeImpl(DataObject dataObject, ItemEx item) {
        FileObject fobj = dataObject == null ? null : dataObject.getPrimaryFile();
        if (fobj == null) {
            fobj = item.getFileObjectImpl();
        }
        String mimeType;
        if (fobj == null || ! fobj.isValid()) {
            mimeType = MIMESupport.getKnownSourceFileMIMETypeByExtension(item.getName());
        } else {
            mimeType = MIMESupport.getSourceFileMIMEType(fobj);
        }
        return mimeType;
    }

    /*package*/ static PredefinedToolKind getDefaultToolForItem(DataObject dataObject, ItemEx item) {
        PredefinedToolKind tool;
        // use mime type of passed data object
        String mimeType = getMIMETypeImpl(dataObject, item);
        if (MIMENames.C_MIME_TYPE.equals(mimeType)) {
//            DataObject dataObject = getDataObject();
//            FileObject fo = dataObject == null ? null : dataObject.getPrimaryFile();
//            // Do not use C for .pc files
//            if (fo != null && "pc".equals(fo.getExt())) { //NOI18N
//                tool = PredefinedToolKind.CustomTool;
//            } else {
            tool = PredefinedToolKind.CCompiler;
//            }
        } else if (MIMENames.HEADER_MIME_TYPE.equals(mimeType)) {
            tool = PredefinedToolKind.CustomTool;
        } else if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mimeType)) {
            tool = PredefinedToolKind.CCCompiler;
        } else if (MIMENames.FORTRAN_MIME_TYPE.equals(mimeType)) {
            tool = PredefinedToolKind.FortranCompiler;
        } else if (MIMENames.ASM_MIME_TYPE.equals(mimeType)) {
            FileObject fobj = dataObject == null ? null : dataObject.getPrimaryFile();
            if (fobj == null) {
                fobj = item.getFileObjectImpl();
            }
            // Do not use assembler for .il files
            if (fobj != null && "il".equals(fobj.getExt())) { //NOI18N
                tool = PredefinedToolKind.CustomTool;
            } else {
                tool = PredefinedToolKind.Assembler;
            }
        } else {
            tool = PredefinedToolKind.CustomTool;
        }
        return tool;
    }
    
    @Override
    public PredefinedToolKind getDefaultTool() {
        // use data object of this item
        return getDefaultToolForItem(this.getDataObject(), this);
    }

    @Override
    protected void onAddedToFolder(Folder folder) {
        DataObject dao = this.getDataObject();
        NativeFileItemSet myNativeFileItemSet = (dao == null) ? null : dao.getLookup().lookup(NativeFileItemSet.class);
        if (myNativeFileItemSet != null) {
            myNativeFileItemSet.add(this);
        } else {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "can not add NativeFileItem for folder''s {0} item {1} using {2}", new Object[]{folder, this, dao}); // NOI18N
            }
        }
    }

}
