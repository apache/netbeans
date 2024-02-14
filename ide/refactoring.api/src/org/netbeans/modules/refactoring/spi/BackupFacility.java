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

package org.netbeans.modules.refactoring.spi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * Simple backup facility
 * can be used to backup files and implement undo
 * For instance Java Refactoring module implements undo this way:
 *
 * public Problem prepare(RefactoringElementsBag elements) {
 * .
 * .
 *   elements.registerTransaction(new RetoucheCommit(results));
 * }
 * 
 * where RetoucheCommit is Transaction:
 * <pre>
 * BackupFacility.Handle handle;
 * public void commit() {
 *   FileObject[] files;
 *   .
 *   .
 *   handle = BackupFacility.getDefault().backup(files);
 *   doCommit();
 * }
 * public void rollback() {
 *   //rollback all files
 *   handle.restore();
 * }
 * </pre>
 * 
 * You can register your own implementation via META-INF services.
 * @see Transaction
 * @see RefactoringElementImplementation#performChange
 * @see RefactoringElementImplementation#undoChange
 * @see RefactoringElementsBag#registerTransaction
 * @see RefactoringElementsBag#addFileChange
 * @see BackupFacility.Handle
 * @author Jan Becicka
 * @deprecated 
 */
@Deprecated
public abstract class BackupFacility {
    
    private BackupFacility() {
    }
    
    private static BackupFacility defaultInstance;
    
    /**
     * does beckup
     * @param file file(s) to backup
     * @return handle which can be used to restore files
     * @throws java.io.IOException if backup failed
     */
    public abstract Handle backup(FileObject... file) throws IOException;
    
    /**
     * does backup
     * @param fileObjects FileObjects to backup
     * @return handle which can be used to restore files
     * @throws java.io.IOException 
     */
    public final Handle backup(Collection<? extends FileObject> fileObjects) throws IOException {
        return backup(fileObjects.toArray(new FileObject[0]));
    }
    
    /**
     * do cleanup
     * all backup files are deleted
     * all internal structures cleared
     * default implementa
     */
    public abstract void clear();
    
    /**
     * @return default instance of this class. If there is instance of this 
     * class in META-INF services -> this class is returned. Otherwise default 
     * implementation is used.
     */
    public static BackupFacility getDefault() {
        BackupFacility instance = Lookup.getDefault().lookup(BackupFacility.class);
        return (instance != null) ? instance : getDefaultInstance();
    }
    
    private static synchronized BackupFacility getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new DefaultImpl();
        }
        
        return defaultInstance;
    }
    
    /**
     * Handle class representing handle to file(s), which were backuped
     * by
     * {@link  org.netbeans.modules.refactoring.spi.BackupFacility#backup(FileObject...)}
     */
    public interface Handle {
        /**
         * restore file(s), which was stored by  {@link  org.netbeans.modules.refactoring.spi.BackupFacility#backup(FileObject...)}
         * @throws java.io.IOException if restore failed.
         */
        void restore() throws IOException;
    }
    
    private static class DefaultHandle implements Handle {
        List<Long> handle;
        DefaultImpl instance;
        private DefaultHandle(DefaultImpl instance, List<Long> handles) {
            this.handle = handles;
            this.instance = instance;
        }
        @Override
        public void restore() throws IOException {
            for (long l:handle) {
                instance.restore(l);
            }
        }
    }
    
    private static class DefaultImpl extends BackupFacility {
        
        private long currentId = 0;
        private Map<Long, BackupEntry> map = new HashMap<Long, BackupEntry>();
        
        private class BackupEntry {
            private File file;
            private URI path;
        }
        
        /** Creates a new instance of BackupFacility */
        private DefaultImpl() {
        }
        
        @Override
        public Handle backup(FileObject ... file) throws IOException {
            ArrayList<Long> list = new ArrayList<Long>();
            for (FileObject f:file) {
                list.add(backup(f));
            }
            return new DefaultHandle(this, list);
        }
        /**
         * does beckup
         * @param file to backup
         * @return id of backup file
         * @throws java.io.IOException if backup failed
         */
        public long backup(FileObject file) throws IOException {
            try {
                BackupEntry entry = new BackupEntry();
                entry.file = Files.createTempFile("nbbackup", null).toFile(); //NOI18N
                copy(file, entry.file);
                entry.path = file.getURL().toURI();
                map.put(currentId, entry);
                entry.file.deleteOnExit();
                return currentId++;
            } catch (URISyntaxException ex) {
                throw (IOException) new IOException(file.toString()).initCause(ex);
            }
        }
        /**
         * restore file, which was stored by backup(file)
         * @param id identification of backup transaction
         * @throws java.io.IOException if restore failed.
         */
        void restore(long id) throws IOException {
            BackupEntry entry = map.get(id);
            if(entry==null) {
                throw new IllegalArgumentException("Backup with id " + id + "does not exist"); // NOI18N
            }
            File backup = Files.createTempFile("nbbackup", null).toFile(); //NOI18N
            backup.deleteOnExit();
            File f = new File(entry.path);
            if (createNewFile(f)) {
                backup.createNewFile();
                copy(f,backup);
            }
            FileObject fileObj = FileUtil.toFileObject(f);
            copy(entry.file,fileObj);
            entry.file.delete();
            if (backup.exists()) {
                entry.file = backup;
            } else {
                map.remove(id);
            }
        }
        
        /**
         * workaround for #93390
         */
        private boolean createNewFile(File f) throws IOException {
            if (f.exists())
                return true;
            File parent = f.getParentFile();
            if (parent!=null) {
                createNewFolder(parent);
            }
            FileUtil.createData(f);
            return false;
        }
        
        private void createNewFolder(File f) throws IOException {
            if (!f.exists()) {
                File parent = f.getParentFile();
                if (parent != null) {
                    createNewFolder(parent);
                }
                 FileUtil.createFolder(f);
            }
        }
        
        private void copy(FileObject a, File b) throws IOException {
            InputStream fs = a.getInputStream();
            FileOutputStream fo = new FileOutputStream(b);
            copy(fs, fo);
        }
        
        private void copy(File a, File b) throws IOException {
            FileInputStream fs = new FileInputStream(a);
            FileOutputStream fo = new FileOutputStream(b);
            copy(fs, fo);
        }
        
        private void copy(File a, FileObject b) throws IOException {
            FileInputStream fs = new FileInputStream(a);
            OutputStream fo = b.getOutputStream();
            copy(fs, fo);
        }

        private void copy(InputStream is, OutputStream os) throws IOException {
            try {
                FileUtil.copy(is, os);
            } finally {
                is.close();
                os.close();
            }
        }
        
        @Override
        public void clear() {
            for(BackupEntry entry: map.values()) {
                entry.file.delete();
            }
            map.clear();
        }
    }
}
