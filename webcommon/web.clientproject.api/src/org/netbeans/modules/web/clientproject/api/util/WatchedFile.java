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
package org.netbeans.modules.web.clientproject.api.util;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 * File that we can listen on, it does not need to exist. It works even if the directory is renamed.
 * @since 1.90
 */
public final class WatchedFile {

    private static final Logger LOGGER = Logger.getLogger(WatchedFile.class.getName());

    final FileObject directory;
    private final String filename;
    final FileChangeListener directoryListener = new DirectoryListener();
    private final FileChangeListener fileListener = new FileListener();
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    // @GuardedBy("this")
    private File file = null;


    private WatchedFile(String filename, FileObject directory) {
        Parameters.notNull("filename", filename); // NOI18N
        Parameters.notNull("directory", directory); // NOI18N
        if (!directory.isFolder()) {
            throw new IllegalArgumentException("Directory must be given, got " + directory);
        }
        if (!directory.isValid()) {
            // try to recover
            File testFile = FileUtil.toFile(directory);
            if (testFile != null) {
                FileObject testFo = FileUtil.toFileObject(testFile);
                if (testFo != null) {
                    directory = testFo;
                }
            }
        }
        this.filename = filename;
        this.directory = directory;
    }

    /**
     * Creates new watched file.
     * @param filename name of the file
     * @param directory directory where the file is to be found
     * @return watched file
     */
    public static WatchedFile create(String filename, FileObject directory) {
        WatchedFile watchedFile = new WatchedFile(filename, directory);
        watchedFile.directory.addFileChangeListener(WeakListeners.create(FileChangeListener.class, watchedFile.directoryListener, watchedFile.directory));
        return watchedFile;
    }

    /**
     * Checks whether the file exists.
     * @return {@code true} if the file exists, {@code false} otherwise.
     */
    public boolean exists() {
        return getWatchedFile().exists();
    }

    /**
     * Gets the file, does not need to exist.
     * @return the file.
     */
    public File getFile() {
        return getWatchedFile();
    }

    /**
     * Adds file change listener.
     * @param listener listener to be added
     */
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    /**
     * Removes file change listener.
     * @param listener listener to be removed
     */
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    private synchronized File getWatchedFile() {
        if (file == null) {
            file = FileUtil.normalizeFile(new File(FileUtil.toFile(directory), filename)); // #254561
            try {
                FileUtil.addFileChangeListener(fileListener, file);
                LOGGER.log(Level.FINE, "Started listening to {0}", file);
            } catch (IllegalArgumentException ex) {
                // ignore, already listening
                LOGGER.log(Level.FINE, "Already listening to {0}", file);
            }
        }
        return file;
    }

    void clear(boolean newFile) {
        if (newFile) {
            synchronized (this) {
                if (file != null) {
                    try {
                        FileUtil.removeFileChangeListener(fileListener, file);
                        LOGGER.log(Level.FINE, "Stopped listening to {0}", file);
                    } catch (IllegalArgumentException ex) {
                        // not listeneing yet, ignore
                        LOGGER.log(Level.FINE, "Not listening yet to {0}", file);
                    }
                    LOGGER.log(Level.FINE, "Clearing cached file path {0}", file);
                    file = null;
                }
            }
        }
        fireChange();
    }

    @Override
    public String toString() {
        return "WatchedFile{" + "directory=" + directory + ", filename=" + filename + '}'; // NOI18N
    }

    //~ Inner classes

    private final class DirectoryListener extends FileChangeAdapter {

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            clear(true);
        }

    }

    private final class FileListener extends FileChangeAdapter {

        @Override
        public void fileDataCreated(FileEvent fe) {
            clear(false);
        }

        @Override
        public void fileChanged(FileEvent fe) {
            clear(false);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            clear(false);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            clear(true);
        }

    }

}
