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
package org.netbeans.modules.php.spi.phpmodule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.ChangeSupport;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 * Support for {@link ImportantFilesImplementation}.
 * @since 2.50
 */
public final class ImportantFilesSupport {

    private final FileObject directory;
    final List<String> fileNames;
    final FileChangeListener fileChangeListener = new FilesListener();
    private final ChangeSupport changeSupport = new ChangeSupport(this);


    private ImportantFilesSupport(FileObject directory, String... fileNames) {
        assert directory != null;
        assert fileNames != null;
        this.directory = directory;
        this.fileNames = new CopyOnWriteArrayList<>(fileNames);
    }

    /**
     * Creates new support for the given directory and file name(s).
     * @param directory directory
     * @param fileNames file name(s)
     * @return new support
     */
    public static ImportantFilesSupport create(FileObject directory, String... fileNames) {
        Parameters.notNull("directory", directory); // NOI18N
        Parameters.notNull("fileNames", fileNames); // NOI18N
        ImportantFilesSupport support = new ImportantFilesSupport(directory, fileNames);
        directory.addFileChangeListener(WeakListeners.create(FileChangeListener.class, support.fileChangeListener, directory));
        return support;
    }

    /**
     * Gets information about all important files.
     * @param fileInfoCreator custom {@link FileInfoCreator}, can be {@code null}
     *        (in such case, {@link ImportantFilesImplementation.FileInfo#FileInfo(FileObject)} is used)
     * @return information about all important files; can be empty but never {@code null}
     */
    public Collection<ImportantFilesImplementation.FileInfo> getFiles(@NullAllowed FileInfoCreator fileInfoCreator) {
        List<ImportantFilesImplementation.FileInfo> files = new ArrayList<>();
        for (String name : fileNames) {
            FileObject fo = directory.getFileObject(name);
            if (fo != null) {
                ImportantFilesImplementation.FileInfo info = null;
                if (fileInfoCreator != null) {
                    info = fileInfoCreator.create(fo);
                }
                if (info == null) {
                    info = new ImportantFilesImplementation.FileInfo(fo);
                }
                files.add(info);
            }
        }
        if (files.isEmpty()) {
            return Collections.emptyList();
        }
        return files;
    }

    /**
     * Adds listener to be notified when important files change.
     * @param listener listener to be notified when important files change
     */
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    /**
     * Removes listener.
     * @param listener listener
     */
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    //~ Inner classes

    /**
     * {@link ImportantFilesImplementation.FileInfo} creator for the given {@link FileObject}.
     */
    public interface FileInfoCreator {

        /**
         * Creates {@link ImportantFilesImplementation.FileInfo} for the given {@link FileObject}.
         * @param fileObject FileObject to be used
         * @return {@link ImportantFilesImplementation.FileInfo} for the given {@link FileObject}, can be {@null}
         */
        @CheckForNull
        ImportantFilesImplementation.FileInfo create(FileObject fileObject);

    }

    private final class FilesListener extends FileChangeAdapter {

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            check(fe.getFile().getNameExt());
            check(fe.getName() + "." + fe.getExt()); // NOI18N
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            check(fe.getFile().getNameExt());
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            check(fe.getFile().getNameExt());
        }

        private void check(String filename) {
            if (fileNames.contains(filename)) {
                fireChange();
            }
        }

    }

}
