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

package org.netbeans.modules.spring.beans.model;

import java.io.File;
import java.util.logging.Logger;
import org.netbeans.modules.spring.beans.model.ExclusiveAccess.AsyncTask;
import org.netbeans.modules.spring.beans.model.impl.ConfigFileSpringBeanSource;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import javax.swing.text.Position.Bias;
import org.netbeans.editor.BaseDocument;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;

/**
 * Handles the lifecycle of a single config file. Can be notified of external changes
 * to the file through the {@link #change} method. Also provides access
 * to the beans for a single file through the {@link #getBeanSource} method.
 *
 * @author Andrei Badea
 */
public class SpringConfigFileModelController {

    private static final Logger LOGGER = Logger.getLogger(SpringConfigFileModelController.class.getName());
    private static final int DELAY = 500;

    private final ConfigFileSpringBeanSource beanSource;
    private final File file;

    // @GuardedBy("this")
    private boolean parsedAtLeastOnce;
    // @GuardedBy("this")
    private AsyncTask currentUpdateTask;
    // @GuardedBy("this")
    private FileObject currentFile;

    public SpringConfigFileModelController(File file, ConfigFileSpringBeanSource beanSource) {
        this.file = file;
        this.beanSource = beanSource;
    }

    public SpringBeanSource getUpToDateBeanSource() throws IOException {
        assert ExclusiveAccess.getInstance().isCurrentThreadAccess();
        FileObject fo = getFileToMakeUpToDate();
        if (fo != null) {
            doParse(fo, false);
        }
        return beanSource;
    }

    public LockedDocument getLockedDocument() throws IOException {
        assert ExclusiveAccess.getInstance().isCurrentThreadAccess();
        FileObject fo = getFileToMakeUpToDate();
        if (fo == null) {
            fo = FileUtil.toFileObject(file);
        }
        if (fo != null) {
            return new LockedDocument(fo);
        }
        return null;
    }

    /**
     * Makes the beans up to date, that is, if there has previously been
     * an external change and the config file hasn't been parsed yet,
     * it is parsed now. This method needs to be called under exclusive
     * access.
     */
    private FileObject getFileToMakeUpToDate() throws IOException {
        assert ExclusiveAccess.getInstance().isCurrentThreadAccess();
        FileObject fileToParse = null;
        synchronized (this) {
            if (currentUpdateTask == null || currentUpdateTask.isFinished()) {
                // No update scheduled.
                if (!parsedAtLeastOnce) {
                    // Moreover, not parsed yet, so will parse now.
                    fileToParse = FileUtil.toFileObject(file);
                }
            } else {
                // An update is scheduled, so will perform it now.
                fileToParse = currentFile;
            }
        }
        return fileToParse;
    }

    private void doParse(FileObject fo, boolean updateTask) throws IOException {
        assert ExclusiveAccess.getInstance().isCurrentThreadAccess();
        BaseDocument document = (BaseDocument)getEditorCookie(fo).openDocument();
        document.readLock();
        try {
            doParse(fo, document, updateTask);
        } finally {
            document.readUnlock();
        }
    }

    private void doParse(FileObject fo, BaseDocument document, boolean updateTask) throws IOException {
        assert ExclusiveAccess.getInstance().isCurrentThreadAccess();
        beanSource.parse(document);
        synchronized (this) {
            if (!parsedAtLeastOnce) {
                parsedAtLeastOnce = true;
            }
            if (!updateTask && fo.equals(currentFile)) {
                // We were not invoked from an update task. By parsing the file,
                // we have just processed the scheduled update, so
                // it can be cancelled now.
                LOGGER.log(Level.FINE, "Canceling update task for " + currentFile);
                currentUpdateTask.cancel();
            }
        }
    }

    public void notifyChange(FileObject configFO) {
        assert configFO != null;
        LOGGER.log(Level.FINE, "Scheduling update for {0}", configFO);
        synchronized (this) {
            if (configFO != currentFile) {
                // We are going to parse another FileObject (for example, because the
                // original one has been renamed).
                if (currentUpdateTask != null) {
                    currentUpdateTask.cancel();
                }
                currentFile = configFO;
                currentUpdateTask = ExclusiveAccess.getInstance().createAsyncTask(new Updater(configFO));
            }
            currentUpdateTask.schedule(DELAY);
        }
    }

    private static EditorCookie getEditorCookie(FileObject fo) throws IOException {
        DataObject dataObject = DataObject.find(fo);
        EditorCookie result = dataObject.getCookie(EditorCookie.class);
        if (result == null) {
            throw new IllegalStateException("File " + fo + " does not have an EditorCookie.");
        }
        return result;
    }

    public final class LockedDocument {

        private final FileObject fo;
        private final CloneableEditorSupport editor;
        final BaseDocument document;
        // Although this class is single-threaded, better to have these thread-safe,
        // since they are guarding the document locking, and that needs to be right
        // even if when the class is misused.
        private final AtomicBoolean locked = new AtomicBoolean();
        private final AtomicBoolean unlocked = new AtomicBoolean();

        public LockedDocument(FileObject fo) throws IOException {
            this.fo = fo;
            editor = (CloneableEditorSupport)getEditorCookie(fo);
            document = (BaseDocument)editor.openDocument();
        }

        public void lock() throws IOException {
            if (!locked.getAndSet(true)) {
                document.atomicLock();
                boolean success = false;
                try {
                    doParse(fo, document, false);
                    success = true;
                } finally {
                    if (!success) {
                        document.atomicUnlock();
                    }
                }
            }
        }

        public void unlock() throws IOException {
            assert locked.get();
            if (!unlocked.getAndSet(true)) {
                document.atomicUnlock();
            }
        }

        public BaseDocument getDocument() {
            assert locked.get();
            return document;
        }

        public SpringBeanSource getBeanSource() throws IOException {
            assert locked.get();
            return beanSource;
        }

        public PositionRef createPositionRef(int offset, Bias bias) {
            assert locked.get();
            return editor.createPositionRef(offset, bias);
        }
    }

    private final class Updater implements Runnable {

        private final FileObject configFile;

        public Updater(FileObject configFile) {
            this.configFile = configFile;
        }

        public void run() {
            LOGGER.log(Level.FINE, "Running scheduled update for file {0}", configFile);
            assert ExclusiveAccess.getInstance().isCurrentThreadAccess();
            try {
                doParse(configFile, true);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }
}
