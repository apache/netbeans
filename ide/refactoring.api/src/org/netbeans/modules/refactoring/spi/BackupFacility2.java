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

import java.io.*;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.refactoring.spi.impl.UndoableWrapper;
import org.netbeans.modules.refactoring.spi.impl.UndoableWrapper.UndoableEditDelegate;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Simple backup facility can be used to backup files and implement undo For
 * instance Java Refactoring module implements undo this way:
 *
 * public Problem prepare(RefactoringElementsBag elements) { . .
 * elements.registerTransaction(new RetoucheCommit(results)); }
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
 *
 * @see Transaction
 * @see RefactoringElementImplementation#performChange
 * @see RefactoringElementImplementation#undoChange
 * @see RefactoringElementsBag#registerTransaction
 * @see RefactoringElementsBag#addFileChange
 * @see BackupFacility.Handle
 * @author Jan Becicka
 */
abstract class BackupFacility2 {
    
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.refactoring.Undo");

    private BackupFacility2() {
    }
    private static BackupFacility2 defaultInstance;

    /**
     * does backup
     *
     * @param file file(s) to backup
     * @return handle which can be used to restore files
     * @throws java.io.IOException if backup failed
     */
    public abstract Handle backup(FileObject... file) throws IOException;

    public abstract Handle backup(File...files) throws IOException;
    
    /**
     * does backup
     *
     * @param fileObjects FileObjects to backup
     * @return handle which can be used to restore files
     * @throws java.io.IOException
     */
    public final Handle backup(Collection<? extends FileObject> fileObjects) throws IOException {
        return backup(fileObjects.toArray(new FileObject[0]));
    }

    /**
     * do cleanup all backup files are deleted all internal structures cleared
     * default implemntation
     */
    public abstract void clear();

    /**
     * @return default instance of this class. If there is instance of this
     * class in META-INF services -> this class is returned. Otherwise default
     * implementation is used.
     */
    public static BackupFacility2 getDefault() {
        BackupFacility2 instance = Lookup.getDefault().lookup(BackupFacility2.class);
        return (instance != null) ? instance : getDefaultInstance();
    }

    private static synchronized BackupFacility2 getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new DefaultImpl();
        }

        return defaultInstance;
    }

    /**
     * Handle class representing handle to file(s), which were backuped by {@link  org.netbeans.modules.refactoring.spi.BackupFacility#backup()}
     */
    public interface Handle {

        /**
         * restore file(s), which was stored by {@link  org.netbeans.modules.refactoring.spi.BackupFacility#backup()}
         *
         * @throws java.io.IOException if restore failed.
         */
        public abstract void restore() throws java.io.IOException;

        void storeChecksum() throws IOException;

        public Collection<String> checkChecksum(boolean undo) throws IOException;
    }

    private static class DefaultHandle implements Handle {

        private List<Long> handle;
        private DefaultImpl instance;

        private DefaultHandle(DefaultImpl instance, List<Long> handles) {
            this.handle = handles;
            this.instance = instance;
        }

        @Override
        public void restore() throws IOException {
            for (long l : handle) {
                instance.restore(l);
            }
        }

        @Override
        public void storeChecksum() throws IOException {
            for (long l : handle) {
                instance.storeChecksum(l);
            }
        }

        @Override
        public Collection<String> checkChecksum(boolean undo) throws IOException {
            Collection<String> result = new LinkedList<>();
            for (long l : handle) {
                String checkChecksum = instance.checkChecksum(l, undo);
                if (checkChecksum !=null) {
                    result.add(checkChecksum);
                }
            }
            return result;

        }
    }

    private static class DefaultImpl extends BackupFacility2 {

        private long currentId = 0;
        private Map<Long, BackupEntry> map = new HashMap<Long, BackupEntry>();

        private String MD5toString(byte[] digest) {
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < digest.length; i++) {
                b.append(Integer.toHexString(0xFF & digest[i]));
            }
            return b.toString();
        }

        private void storeChecksum(long l) throws IOException {
            BackupEntry backup = map.get(l);
            if(backup.orig == null) { // Find fileobject for newly created file
                backup.orig = FileUtil.toFileObject(backup.origFile);
                backup.origFile = null;
            }
            FileObject fo = backup.orig;
            if (!fo.isValid()) {
                //deleted
                backup.checkSum = new byte[16];
                Arrays.fill(backup.checkSum, (byte)0);
                return;
            }
            DataObject dob = DataObject.find(fo);
            if (dob != null) {
                CloneableEditorSupport ces = dob.getLookup().lookup(CloneableEditorSupport.class);
                final BaseDocument doc = (BaseDocument) ces.getDocument();
                if (doc !=null && doc.isAtomicLock()) {
                    //workaround to avoid deadlock
                    return;
                }
            }
            LOG.log(Level.FINE, "Storing MD5 for {0}", backup.orig);
            backup.checkSum = getMD5(getInputStream(backup.orig));
            LOG.log(Level.FINE, "MD5 is: {0}", MD5toString(backup.checkSum));
        }

        private String checkChecksum(long l, boolean undo) {

            try {
                BackupEntry backup = map.get(l);
                FileObject fo = backup.orig;
                if (!fo.isValid()) {
                    //file does not exist. No conflict
                    return null;
                }
                DataObject dob = DataObject.find(fo);
                if (dob != null) {
                    CloneableEditorSupport ces = dob.getLookup().lookup(CloneableEditorSupport.class);

                    final BaseDocument doc = (BaseDocument) ces.getDocument();
                    if (doc != null && doc.isAtomicLock()) {
                        //workaround to avoid deadlock
                        return null;
                    } else {
                        EditorCookie editor = dob.getLookup().lookup(EditorCookie.class);
                        if (editor != null  && doc!=null && editor.isModified()) {
                            UndoableEditDelegate edit = undo
                                    ? NbDocument.getEditToBeUndoneOfType(editor, UndoableWrapper.UndoableEditDelegate.class)
                                    : NbDocument.getEditToBeRedoneOfType(editor, UndoableWrapper.UndoableEditDelegate.class);
                            if (edit == null) {
                                LOG.fine("Editor Undo Different");
                                return backup.orig.getPath();
                            }
                        }

                    }
                }

                try {
                    LOG.log(Level.FINE, "Checking MD5 for {0}", backup.orig);
                    byte[] ts = getMD5(getInputStream(backup.orig));
                    if (!Arrays.equals(backup.checkSum, ts)) {
                        LOG.fine("MD5 check failed");
                        return backup.orig.getPath();
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }

        private InputStream getInputStream(FileObject fo) throws IOException {
            DataObject dob = DataObject.find(fo);
            CloneableEditorSupport ces = dob.getLookup().lookup(CloneableEditorSupport.class);
            if (ces != null && ces.isModified()) {
                LOG.fine("Editor Input Stream");
                return ces.getInputStream();
            }
            LOG.fine("File Input Stream");
            return fo.getInputStream();
        }

        private class BackupEntry {

            private File file;
            private FileObject orig;
            private File origFile;
            private byte[] checkSum;
            private boolean undo = true;
            private boolean exists = true;

            public BackupEntry() {
            }

            public boolean isUndo() {
                return undo;
            }

            public void setUndo(boolean undo) {
                this.undo = undo;
            }
        }

        /**
         * Creates a new instance of BackupFacility
         */
        private DefaultImpl() {
        }

        @Override
        public Handle backup(FileObject... file) throws IOException {
            ArrayList<Long> list = new ArrayList<>();
            for (FileObject f : file) {
                list.add(backup(f));
            }
            return new DefaultHandle(this, list);
        }
        
        @Override
        public Handle backup(File... files) throws IOException {
            ArrayList<Long> list = new ArrayList<>();
            for (File f : files) {
                list.add(backup(f));
            }
            return new DefaultHandle(this, list);
        }
        

        /**
         * does backup
         *
         * @param file to backup
         * @return id of backup file
         * @throws java.io.IOException if backup failed
         */
        public long backup(FileObject file) throws IOException {
            BackupEntry entry = new BackupEntry();
            entry.file = Files.createTempFile("nbbackup", null).toFile(); //NOI18N
            copy(file, entry.file);
            entry.orig = file;
            map.put(currentId, entry);
            entry.file.deleteOnExit();
            return currentId++;
        }
        
        /**
         * does backup
         *
         * @param file to backup
         * @return id of backup file
         * @throws java.io.IOException if backup failed
         */
        public long backup(File file) throws IOException {
            BackupEntry entry = new BackupEntry();
            entry.file = Files.createTempFile("nbbackup", null).toFile(); //NOI18N
            entry.exists = file.exists();
            if(entry.exists) {
                FileObject fo = FileUtil.toFileObject(file);
                entry.orig = fo;
            } else {
                // Temporarily store the file, will be changed to fileobject when store checksum is called
                entry.origFile = file;
            }
            map.put(currentId, entry);
            entry.file.deleteOnExit();
            if (entry.exists)
                copy(file, entry.file);
            return currentId++;
        }
        

        private byte[] getMD5(InputStream is) throws IOException {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                try {
                    is = new DigestInputStream(is, md);
                    readFully(is, -1, true);
                } finally {
                    is.close();
                }
                return md.digest();
            } catch (NoSuchAlgorithmException ex) {
                throw new IOException(ex);
            }
        }
        
        /**
         * Read up to
         * <code>length</code> of bytes from
         * <code>in</code> until EOF is detected.
         *
         * @param in input stream, must not be null
         * @param length number of bytes to read, -1 or Integer.MAX_VALUE means
         * read as much as possible
         * @param readAll if true, an EOFException will be thrown if not enough
         * bytes are read. Ignored when length is -1 or Integer.MAX_VALUE
         * @return bytes read
         * @throws IOException Any IO error or a premature EOF is detected
         */
        private static byte[] readFully(InputStream is, int length, boolean readAll)
                throws IOException {
            byte[] output = {};
            if (length == -1) {
                length = Integer.MAX_VALUE;
            }
            int pos = 0;
            while (pos < length) {
                int bytesToRead;
                if (pos >= output.length) { // Only expand when there's no room
                    bytesToRead = Math.min(length - pos, output.length + 1024);
                    if (output.length < pos + bytesToRead) {
                        output = Arrays.copyOf(output, pos + bytesToRead);
                    }
                } else {
                    bytesToRead = output.length - pos;
                }
                int cc = is.read(output, pos, bytesToRead);
                if (cc < 0) {
                    if (readAll && length != Integer.MAX_VALUE) {
                        throw new EOFException("Detect premature EOF");
                    } else {
                        if (output.length != pos) {
                            output = Arrays.copyOf(output, pos);
                        }
                        break;
                    }
                }
                pos += cc;
            }
            return output;
        }    

        private static java.lang.reflect.Field undoRedo;

        static {
            try {
                //obviously hack. See 108616 and 48427
                undoRedo = org.openide.text.CloneableEditorSupport.class.getDeclaredField("undoRedo"); //NOI18N
                undoRedo.setAccessible(true);
            } catch (NoSuchFieldException | SecurityException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        /**
         * restore file, which was stored by backup(file)
         *
         * @param id identification of backup transaction
         * @throws java.io.IOException if restore failed.
         */
        void restore(long id) throws IOException {
            BackupEntry entry = map.get(id);
            if (entry == null) {
                throw new IllegalArgumentException("Backup with id " + id + "does not exist"); // NOI18N
            }
            File backup = Files.createTempFile("nbbackup", null).toFile(); //NOI18N
            backup.deleteOnExit();
            boolean exists = false;
            FileObject fo = entry.orig;
            if(!fo.isValid()) { // Try to restore FO
                FileObject file = FileUtil.toFileObject(FileUtil.toFile(fo));
                fo = file == null? fo : file;
            }
            if (exists = fo.isValid()) {
                backup.createNewFile();
                copy(fo, backup);
            } else {
                fo = createNewFile(fo);
            }
            if (entry.exists) {
                if (!tryUndoOrRedo(fo, entry)) {
                    copy(entry.file, fo);
                }
            } else {
                fo.delete();
            }
            entry.exists = exists;
            entry.file.delete();
            if (backup.exists()) {
                entry.file = backup;
            } else {
                map.remove(id);
            }
        }

        private boolean tryUndoOrRedo(@NonNull final FileObject fileObj, @NonNull final BackupEntry entry) throws DataObjectNotFoundException {
            DataObject dob = DataObject.find(fileObj);
            if (dob != null) {
                CloneableEditorSupport ces = dob.getLookup().lookup(CloneableEditorSupport.class);
                if(ces == null) {
                    return false;
                }
                final org.openide.awt.UndoRedo.Manager manager;
                try {
                    manager = (org.openide.awt.UndoRedo.Manager) undoRedo.get(ces);
                    final BaseDocument doc = (BaseDocument) ces.getDocument();
                    if (doc==null) {
                        return false;
                    }
                    if (doc.isAtomicLock() || fileObj.isLocked()) {
                        //undo already performed
                        if (entry.isUndo()) {
                            entry.setUndo(false);
                        } else {
                            entry.setUndo(true);
                        }
                    } else {
                        if ((entry.isUndo() && manager.canUndo()) || (!entry.isUndo() && manager.canRedo())) {
                            doc.runAtomic(new Runnable() {

                                @Override
                                public void run() {
                                    if (entry.isUndo()) {
                                        manager.undo();
                                        entry.setUndo(false);
                                    } else {
                                        manager.redo();
                                        entry.setUndo(true);
                                    }
                                }
                            });
                        } else {
                            return false;
                        }
                    }
                    return true;
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return false;
        }

        /**
         * workaround for #93390
         */
        private FileObject createNewFile(FileObject fo) throws IOException {
            if (fo.isValid()) {
                return fo;
            }
            File file = FileUtil.toFile(fo);
            if(file != null && file.exists()) {
                return FileUtil.toFileObject(file);
            }
            FileObject parent = fo.getParent();
            if (parent != null) {
                createNewFolder(parent);
            }
            return FileUtil.createData(parent, fo.getNameExt());
        }

        private void createNewFolder(FileObject fo) throws IOException {
            if (!fo.isValid()) {
                FileObject parent = fo.getParent();
                if (parent != null) {
                    createNewFolder(parent);
                    FileUtil.createFolder(parent, fo.getNameExt());
                }
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
            for (BackupEntry entry : map.values()) {
                entry.file.delete();
            }
            map.clear();
        }
    }
}
