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
package org.netbeans.modules.versioning.core.filesystems;

import java.awt.Image;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.modules.versioning.core.APIAccessor;
import org.netbeans.modules.versioning.core.VersioningAnnotationProvider;
import org.netbeans.modules.versioning.core.VersioningManager;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSInterceptor;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider.VersioningSystem;
import org.openide.filesystems.*;

/**
 * Entry point for calls from a filesystem. 
 * 
 * @author Tomas Stupka
 */
public final class VCSFilesystemInterceptor {
    private static final Logger LOG = VersioningManager.LOG;
    private static final VersioningManager master = VersioningManager.getInstance();
    /**
     * Delete interceptor: holds files and folders that we do not want to delete
     * but must pretend that they were deleted.
     */
    private static final Set<VCSFileProxy> deletedFiles = new HashSet<VCSFileProxy>(5);
    
    /**
     * A versioned files remote repository or origin.
     */
    private static final String ATTRIBUTE_REMOTE_LOCATION = VersioningManager.ATTRIBUTE_REMOTE_LOCATION;

    /**
     * A Runnable to refresh the file given in {@link #getAttribute()}.
     */
    private static final String ATTRIBUTE_REFRESH = "ProvidedExtensions.Refresh";

    /**
     * A o.n.m.versioning.util.SearchHistorySupport instance
     */
    private static final String ATTRIBUTE_SEARCH_HISTORY = "ProvidedExtensions.SearchHistorySupport";

    /**
     * A Boolean specifying if a VCS marks the file as modified or up to date.
     */
    private static final String ATTRIBUTE_IS_MODIFIED = "ProvidedExtensions.VCSIsModified";

    private VCSFilesystemInterceptor() {
    }
    
    /** 
     * Listener for changes in annotations of files.
     */
    public static interface VCSAnnotationListener {
        /** Notifies listener about change in annotataion of a few files.
        * @param ev event describing the change
        */
        public void annotationChanged(VCSAnnotationEvent ev);
    }
    
    /**
     * Event describing a change in annotation of files.
     */
    public static final class VCSAnnotationEvent {

        /** changed files */
        private Set<? extends FileObject> files;

        /** icon changed? */
        private boolean icon;

        /** name changed? */
        private boolean name;

        /** Creates new VCSAnnotationEvent
        * @param files set of FileObjects that has been changed
        * @param icon has icon changed?
        * @param name has name changed?
        */
        public VCSAnnotationEvent(Set<? extends FileObject> files, boolean icon, boolean name) {
            this.files = files;
            this.icon = icon;
            this.name = name;
        }

        /** Creates new VCSAnnotationEvent
        * @param file file object that has been changed
        * @param icon has icon changed?
        * @param name has name changed?
        */
        public VCSAnnotationEvent(FileObject file, boolean icon, boolean name) {
            this(Collections.singleton(file), icon, name);
        }

        /** Creates new VCSAnnotationEvent. This does not specify the
        * file that changed annotation, assuming that everyone should update
        * its annotation. Please notice that this can be time consuming
        * and should be fired only when really necessary.
        *
        * @param icon has icon changed?
        * @param name has name changed?
        */
        public VCSAnnotationEvent(boolean icon, boolean name) {
            this((Set<FileObject>) null, icon, name);
        }

        /** 
         * Is the change a change in the name?
         */
        public boolean isNameChange() {
            return name;
        }

        /** 
         * Did the files changed their icons?
         */
        public boolean isIconChange() {
            return icon;
        }
        
        /**
         * Files with a changed annotation
         * 
         * @return 
         */
        public Set<? extends FileObject> getFiles() {
            return files;
        }
    }    


    // ==================================================================================================
    // ANNOTATIONS
    // ==================================================================================================

    /** Listeners are held weakly, and can GC if nobody else holds them */
    public static void registerFileStatusListener(VCSAnnotationListener listener) {
        VersioningManager.statusListener(listener, true);
    }
    
    public static Image annotateIcon(Image icon, int iconType, Set<? extends FileObject> files) {
        return VersioningAnnotationProvider.getDefault().annotateIcon(icon, iconType, files);
    }
    
    public static String annotateNameHtml(String name, Set<? extends FileObject> files) {
        return VersioningAnnotationProvider.getDefault().annotateNameHtml(name, files);
    }
    
    public static Action[] actions(Set<? extends FileObject> files) {
        return VersioningAnnotationProvider.getDefault().actions(files);
    }

    
    // ==================================================================================================
    // QUERIES
    // ==================================================================================================

    /**
     * Determines if the given file should be considered writable by the IDE even if it isn't in 
     * means of the relevant filesystem. Useful in cases when a file is locked by VCS but can be
     * unlocked on write demand - e.g. by refactoring or editor access.
     * 
     * @param file
     * @return true if a relevant VCS system considers that the file should be handled as 
     * writable by the IDE, otherwise false.
     */
    public static boolean canWriteReadonlyFile(VCSFileProxy file) {
        LOG.log(Level.FINE, "canWrite {0}", file);
        // can be optimized by taking out local history from the search
        return getInterceptor(file, false, "isMutable").isMutable(file); // NOI18N
    }

    /**
     * Returns the given files files attribute
     * @param file
     * @param attrName
     * @return 
     */
    public static Object getAttribute(VCSFileProxy file, String attrName) {
        LOG.log(Level.FINE, "getAttribute {0}, {1}", new Object[] {file, attrName});
        if (ATTRIBUTE_REMOTE_LOCATION.equals(attrName)
                || ATTRIBUTE_REFRESH.equals(attrName)
                || ATTRIBUTE_IS_MODIFIED.equals(attrName)
                || ATTRIBUTE_SEARCH_HISTORY.equals(attrName)) {
            return getInterceptor(file, file.isDirectory(), "getAttribute").getAttribute(attrName); // NOI18N
        } else {
            return null;
        }
    }
    
    // ==================================================================================================
    // CHANGE
    // ==================================================================================================

    public static void beforeChange(VCSFileProxy file) {
        LOG.log(Level.FINE, "beforeChange {0}", file);
        getInterceptor(file, file.isDirectory(), "beforeChange").beforeChange(); // NOI18N
    }
    
    public static void fileChanged(VCSFileProxy file) {
        LOG.log(Level.FINE, "fileChanged {0}", file);
        removeFromDeletedFiles(file);
        getInterceptor(file, "afterChange").afterChange();
    }

    // ==================================================================================================
    // DELETE
    // ==================================================================================================

    private static void removeFromDeletedFiles(VCSFileProxy file) {
        synchronized(deletedFiles) {
            deletedFiles.remove(file);
        }
    }

    public static IOHandler getDeleteHandler(VCSFileProxy file) {
        LOG.log(Level.FINE, "getDeleteHandler {0}", file);
        removeFromDeletedFiles(file);
        DelegatingInterceptor dic = getInterceptor(file, (Boolean) null, "beforeDelete", "doDelete"); // NOI18N
        return dic.beforeDelete() ? dic : null;
    }

    public static void deleteSuccess(VCSFileProxy file) {
        LOG.log(Level.FINE, "deleteSuccess {0}", file);
        removeFromDeletedFiles(file);
        getInterceptor(file, "afterDelete").afterDelete(); // NOI18N
    }

    public static void deletedExternally(VCSFileProxy file) {
        LOG.log(Level.FINE, "deletedExternally {0}", file);
        removeFromDeletedFiles(file);
        getInterceptor(file, "afterDelete").afterDelete(); // NOI18N
    }
   
    // ==================================================================================================
    // CREATE
    // ==================================================================================================

    public static void beforeCreate(VCSFileProxy parent, String name, boolean isFolder) {
        LOG.log(Level.FINE, "beforeCreate {0}, {1}, {2} ", new Object[] {parent, name, isFolder});
        if (parent == null) return;
        VCSFileProxy file = APIAccessor.IMPL.createFileProxy(parent, name, isFolder);
        DelegatingInterceptor dic = getInterceptor(file, isFolder, "beforeCreate"); // NOI18N
        if (dic.beforeCreate()) {
            filesBeingCreated.put(new FileEx(parent, name, isFolder), dic);
        }
    }

    public static void createFailure(VCSFileProxy parent, String name, boolean isFolder) {
        LOG.log(Level.FINE, "createFailure {0}, {1}, {2} ", new Object[] {parent, name, isFolder});
        filesBeingCreated.remove(new FileEx(parent, name, isFolder));
    }

    public static void createSuccess(VCSFileProxy file) {
        LOG.log(Level.FINE, "createSuccess {0}", new Object[] {file});
        createSuccessImpl(file);
    }

    public static void createdExternally(VCSFileProxy file) {
        LOG.log(Level.FINE, "createdExternally {0}", new Object[] {file});
        createSuccessImpl(file);
    }

    private static void createSuccessImpl (VCSFileProxy file) {
        FileEx fileEx = new FileEx(file.getParentFile(), file.getName(), file.isDirectory());
        DelegatingInterceptor interceptor = filesBeingCreated.remove(fileEx);
        if (interceptor != null) {
            try {
                interceptor.doCreate();
            } catch (Exception e) {
                // ignore errors, the file is already created anyway
            }
        }
        removeFromDeletedFiles(file);
        if (interceptor == null) {
            interceptor = getInterceptor(file, "afterCreate"); // NOI18N
        }
        interceptor.afterCreate();
    }
    
    // ==================================================================================================
    // MOVE
    // ==================================================================================================

    public static IOHandler getMoveHandler(VCSFileProxy from, VCSFileProxy to) {
        LOG.log(Level.FINE, "getMoveHandler {0}, {1}", new Object[]{from, to});
        return getMoveHandlerIntern(from, to);
    }

    public static IOHandler getRenameHandler(VCSFileProxy from, String newName) {
        LOG.log(Level.FINE, "getRenameHandler {0}, {1}", new Object[] {from, newName});
        return getMoveHandlerIntern(from, VCSFileProxy.createFileProxy(from.getParentFile(), newName));
    }

    private static IOHandler getMoveHandlerIntern(VCSFileProxy from, VCSFileProxy to) {
        DelegatingInterceptor dic = getInterceptor(from, to, "beforeMove", "doMove"); // NOI18N
        return dic.beforeMove() ? dic.getMoveHandler() : null;
    }

    public static void afterMove(VCSFileProxy from, VCSFileProxy to) {
        LOG.log(Level.FINE, "afterMove {0}, {1}", new Object[] {from, to});
        removeFromDeletedFiles(from);
        getInterceptor(from, to, "afterMove").afterMove();
    }

    // ==================================================================================================
    // COPY
    // ==================================================================================================

    public static IOHandler getCopyHandler(VCSFileProxy from, VCSFileProxy to) {
        LOG.log(Level.FINE, "getCopyHandler {0}, {1}", new Object[]{from, to});
        DelegatingInterceptor dic = getInterceptor(from, to, "beforeCopy", "doCopy"); // NOI18N
        return dic.beforeCopy() ? dic.getCopyHandler() : null;
    }

    public static void beforeCopy(VCSFileProxy from, VCSFileProxy to) {
        // XXX and what is getCopyHandler good for?
    }
    
    public static void copySuccess(VCSFileProxy from, VCSFileProxy to) {
        LOG.log(Level.FINE, "copySuccess {0}, {1}", new Object[] {from, to});
        getInterceptor(from, to, "afterCopy").afterCopy();
    }

    // ==================================================================================================
    // MISC
    // ==================================================================================================    
    
    /**
     * There is a contract that says that when a file is locked, it is expected to be changed. This is what openide/text
     * does when it creates a Document. A versioning system is expected to make the file r/w.
     *
     * @param fo a VCSFileProxy
     */
    public static void fileLocked(final VCSFileProxy fo) throws IOException {
        LOG.log(Level.FINE, "fileLocked {0}", fo);
        getInterceptor(fo, "beforeEdit").beforeEdit();           // NOI18N
    }

    public static long listFiles(VCSFileProxy dir, long lastTimeStamp, List<? super VCSFileProxy> children) {
        LOG.log(Level.FINE, "listFiles {0}, {1}", new Object[]{dir, lastTimeStamp});
        if(LOG.isLoggable(Level.FINER)) {
            for (Object f : children) {
                LOG.log(Level.FINE, "  listFiles child {1}", f);
            }
        }
        DelegatingInterceptor interceptor = getRefreshInterceptor(dir);
        return interceptor.refreshRecursively(dir, lastTimeStamp, children);
    }
    
    // ==================================================================================================
    // HANDLERS
    // ==================================================================================================
    
    /** Handle to perform an I/O operation. Its presence indicates that
     * the system is capable to handle given I/O operation (like
     * {@link #getMoveHandler(org.netbeans.modules.versioning.core.api.VCSFileProxy, org.netbeans.modules.versioning.core.api.VCSFileProxy)}
     * or 
     * {@link #getRenameHandler(org.netbeans.modules.versioning.core.api.VCSFileProxy, java.lang.String)}, etc.).
     * 
     */
    public interface IOHandler {
        /**
         * @throws java.io.IOException if handled operation isn't successful
         */
        void handle() throws IOException;
    }
    
    // private methods
    

    private static boolean needsLH(String... methodNames) {
        if(Boolean.getBoolean("versioning.no.localhistory.interceptor")) {
            // do not intercept file events in LH
            return false;
        }
        for (String methodName : methodNames) {
            if(master.needsLocalHistory(methodName)) {
                return true;
            }
        }
        return false;
    }
    /**
     * Stores files that are being created inside the IDE and the owner interceptor wants to handle the creation. Entries
     * are added in beforeCreate() and removed in fileDataCreated() or createFailure().
     */
    private static final Map<FileEx, DelegatingInterceptor> filesBeingCreated = new HashMap<FileEx, DelegatingInterceptor>(10);
    private static class FileEx {
        final VCSFileProxy parent;
        final String name;
        final boolean isFolder;

        public FileEx(VCSFileProxy parent, String name, boolean folder) {
            this.parent = parent;
            this.name = name;
            isFolder = folder;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FileEx)) return false;
            FileEx fileEx = (FileEx) o;
            return isFolder == fileEx.isFolder && name.equals(fileEx.name) && parent.equals(fileEx.parent);
        }

        @Override
        public int hashCode() {
            int result = parent.hashCode();
            result = 17 * result + name.hashCode();
            result = 17 * result + (isFolder ? 1 : 0);
            return result;
        }
    }

    private static DelegatingInterceptor getInterceptor(VCSFileProxy file, String... forMethods) {
        return getInterceptor(file, file.isDirectory(), forMethods);
    }
    
    private static DelegatingInterceptor getInterceptor(VCSFileProxy file, Boolean isDirectory, String... forMethods) {
        if (file == null || master == null) return nullDelegatingInterceptor;

        Boolean isFile = isDirectory != null ? !isDirectory : null;
        isDirectory = isDirectory != null ? isDirectory : false;
        
        VersioningSystem vs = master.getOwner(file, isFile);
        VCSInterceptor vsInterceptor = vs != null ? vs.getVCSInterceptor() : nullInterceptor;

        VersioningSystem lhvs = needsLH(forMethods) ? master.getLocalHistory(file, isFile) : null;
        VCSInterceptor localHistoryInterceptor = lhvs != null ? lhvs.getVCSInterceptor() : nullInterceptor;

        return new DelegatingInterceptor(vsInterceptor, localHistoryInterceptor, file, null, isDirectory);
    }

    private static DelegatingInterceptor getInterceptor(VCSFileProxy from, VCSFileProxy to, String... forMethods) {
        if (from == null || to == null) return nullDelegatingInterceptor;

        VersioningSystem vs = master.getOwner(from);
        VCSInterceptor vsInterceptor = vs != null ? vs.getVCSInterceptor() : nullInterceptor;

        VersioningSystem lhvs = needsLH(forMethods) ? master.getLocalHistory(from) : null;
        VCSInterceptor localHistoryInterceptor = lhvs != null ? lhvs.getVCSInterceptor() : nullInterceptor;

        return new DelegatingInterceptor(vsInterceptor, localHistoryInterceptor, from, to, false);
    }

    private static DelegatingInterceptor getRefreshInterceptor (VCSFileProxy dir) {
        if (dir == null) return nullDelegatingInterceptor;
        VersioningSystem vs = master.getOwner(dir);
        VCSInterceptor Interceptor = vs != null ? vs.getVCSInterceptor() : nullInterceptor;
        return new DelegatingInterceptor(Interceptor, nullInterceptor, dir, null, true);
    }

    private static final DelegatingInterceptor nullDelegatingInterceptor = new DelegatingInterceptor() {
        public boolean beforeDelete() { return false; }
        public void doDelete() throws IOException {  }
        public void afterDelete() { }
        public boolean beforeMove() { return false; }
        public void doMove() throws IOException {  }
        public boolean beforeCreate() { return false; }
        public void doCreate() throws IOException {  }
        public void afterCreate() {  }
        public void beforeChange() {  }
        public void beforeEdit() { }
        public void afterChange() {  }
        public void afterMove() {  }
    };

    private static final VCSInterceptor nullInterceptor = new VCSInterceptor() {

        @Override
        public boolean isMutable(VCSFileProxy file) {
            return false;
        }

        @Override
        public Object getAttribute(VCSFileProxy file, String attrName) {
            return null;
        }

        @Override
        public boolean beforeDelete(VCSFileProxy file) {
            return false;
        }

        @Override
        public void doDelete(VCSFileProxy file) throws IOException { }

        @Override
        public void afterDelete(VCSFileProxy file) {}

        @Override
        public boolean beforeMove(VCSFileProxy from, VCSFileProxy to) {
            return false;
        }

        @Override
        public void doMove(VCSFileProxy from, VCSFileProxy to) throws IOException {}

        @Override
        public void afterMove(VCSFileProxy from, VCSFileProxy to) {}

        @Override
        public boolean beforeCopy(VCSFileProxy from, VCSFileProxy to) {
            return false;
        }

        @Override
        public void doCopy(VCSFileProxy from, VCSFileProxy to) throws IOException {}

        @Override
        public void afterCopy(VCSFileProxy from, VCSFileProxy to) {}

        @Override
        public boolean beforeCreate(VCSFileProxy file, boolean isDirectory) {
            return false;
        }

        @Override
        public void doCreate(VCSFileProxy file, boolean isDirectory) throws IOException {}

        @Override
        public void afterCreate(VCSFileProxy file) {}

        @Override
        public void afterChange(VCSFileProxy file) {}

        @Override
        public void beforeChange(VCSFileProxy file) {}

        @Override
        public void beforeEdit(VCSFileProxy file) {}

        @Override
        public long refreshRecursively(VCSFileProxy dir, long lastTimeStamp, List<? super VCSFileProxy> children) {
            return -1;
        }
    };

    private static class DelegatingInterceptor implements IOHandler {
        final Collection<VCSInterceptor> interceptors;
        final VCSInterceptor interceptor;
        final VCSInterceptor lhInterceptor;
        final VCSFileProxy file;
        final VCSFileProxy to;
        private final boolean isDirectory;
        private IOHandler moveHandler;
        private IOHandler copyHandler;

        private DelegatingInterceptor() {
            this((VCSInterceptor) null, null, null, null, false);
        }

        public DelegatingInterceptor(VCSInterceptor interceptor, VCSInterceptor lhInterceptor, VCSFileProxy file, VCSFileProxy to, boolean isDirectory) {
            this.interceptor = interceptor != null ? interceptor : nullInterceptor;
            this.interceptors = Collections.singleton(this.interceptor);
            this.lhInterceptor = lhInterceptor != null ? lhInterceptor : nullInterceptor;
            this.file = file;
            this.to = to;
            this.isDirectory = isDirectory;
        }

        // TODO: special hotfix for #95243
        public DelegatingInterceptor(Collection<VCSInterceptor> interceptors, VCSInterceptor lhInterceptor, VCSFileProxy file, VCSFileProxy to, boolean isDirectory) {
            this.interceptors = interceptors != null && interceptors.size() > 0 ? interceptors : Collections.singleton(nullInterceptor);
            this.interceptor = this.interceptors.iterator().next();
            this.lhInterceptor = lhInterceptor != null ? lhInterceptor : nullInterceptor;
            this.file = file;
            this.to = to;
            this.isDirectory = isDirectory;
        }

        public boolean isMutable(VCSFileProxy file) {
            return interceptor.isMutable(file);
        }

        private Object getAttribute(String attrName) {
            return interceptor.getAttribute(file, attrName);
        }

        public boolean beforeDelete() {
            lhInterceptor.beforeDelete(file);
            return interceptor.beforeDelete(file);
        }

        public void doDelete() throws IOException {
            lhInterceptor.doDelete(file);
            interceptor.doDelete(file);
        }

        public void afterDelete() {
            lhInterceptor.afterDelete(file);
            interceptor.afterDelete(file);
        }

        public boolean beforeMove() {
            lhInterceptor.beforeMove(file, to);
            return interceptor.beforeMove(file, to);
        }

        public void doMove() throws IOException {
            lhInterceptor.doMove(file, to);
            interceptor.doMove(file, to);
        }

        public void afterMove() {
            lhInterceptor.afterMove(file, to);
            interceptor.afterMove(file, to);
        }

        public boolean beforeCopy() {
            lhInterceptor.beforeCopy(file, to);
            return interceptor.beforeCopy(file, to);
        }

        public void doCopy() throws IOException {
            lhInterceptor.doCopy(file, to);
            interceptor.doCopy(file, to);
        }

        public void afterCopy() {
            lhInterceptor.afterCopy(file, to);
            interceptor.afterCopy(file, to);
        }

        public boolean beforeCreate() {
            lhInterceptor.beforeCreate(file, isDirectory);
            return interceptor.beforeCreate(file, isDirectory);
        }

        public void doCreate() throws IOException {
            lhInterceptor.doCreate(file, isDirectory);
            interceptor.doCreate(file, isDirectory);
        }

        public void afterCreate() {
            lhInterceptor.afterCreate(file);
            interceptor.afterCreate(file);
        }

        public void afterChange() {
            lhInterceptor.afterChange(file);
            interceptor.afterChange(file);
        }

        public void beforeChange() {
            lhInterceptor.beforeChange(file);
            interceptor.beforeChange(file);
        }

        public void beforeEdit() throws IOException {
            lhInterceptor.beforeEdit(file);
            interceptor.beforeEdit(file);
        }

        private IOHandler getMoveHandler() {
            if (moveHandler == null) {
                moveHandler = new IOHandler() {

                    @Override
                    public void handle() throws IOException {
                        LOG.log(Level.FINE, "move handle {0}", new Object[]{file, to});
                        doMove();
                    }
                };
            }
            return moveHandler;
        }

        private IOHandler getCopyHandler() {
            if (copyHandler == null) {
                copyHandler = new IOHandler() {

                    @Override
                    public void handle() throws IOException {
                        LOG.log(Level.FINE, "copy handle {0}", new Object[]{file, to});
                        doCopy();
                    }
                };
            }
            return copyHandler;
        }

        /**
         * This must act EXACTLY like java.io.File.delete(). This means:
         *
         * 1.1 if the file is a file and was deleted, return true 1.2 if the
         * file is a file and was NOT deleted because we want to keep it (is
         * part of versioning metadata), also return true this is done this way
         * to enable bottom-up recursive file deletion 1.3 if the file is a file
         * that should be deleted but the operation failed (the file is locked,
         * for example), return false
         *
         * 2.1 if the file is an empty directory that was deleted, return true
         * 2.2 if the file is a NON-empty directory that was NOT deleted because
         * it contains files that were NOT deleted in step 1.2, return true 2.3
         * if the file is a NON-empty directory that was NOT deleted because it
         * contains some files that were not previously deleted, return false
         *
         * @param file file or folder to delete
         * @return true if the file was successfully deleted (event virtually
         * deleted), false otherwise
         */
        @Override
        public void handle() throws IOException {
            LOG.log(Level.FINE, "delete handle {0}", new Object[]{file});
            lhInterceptor.doDelete(file);
            interceptor.doDelete(file);
            synchronized (deletedFiles) {
                if (file.isDirectory()) {
                    // the directory was virtually deleted, we can forget about its children
                    for (Iterator<VCSFileProxy> i = deletedFiles.iterator(); i.hasNext();) {
                        VCSFileProxy fakedFile = i.next();
                        if (file.equals(fakedFile.getParentFile())) {
                            i.remove();
                        }
                    }
                }
                if (file.exists()) {
                    deletedFiles.add(file);
                } else {
                    deletedFiles.remove(file);
                }
            }
        }
        public long refreshRecursively(VCSFileProxy dir, long lastTimeStamp, List<? super VCSFileProxy> children) {
            return interceptor.refreshRecursively(dir, lastTimeStamp, children);
        }
    }
    
}
