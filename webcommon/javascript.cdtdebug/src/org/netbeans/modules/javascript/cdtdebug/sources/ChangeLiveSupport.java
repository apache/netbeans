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

package org.netbeans.modules.javascript.cdtdebug.sources;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Phaser;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.chrome_devtools_protocol.debugger.CallFrame;
import org.netbeans.lib.chrome_devtools_protocol.debugger.SetScriptSourceRequest;
import org.netbeans.modules.javascript.cdtdebug.CDTDebugger;
import org.netbeans.modules.javascript.cdtdebug.CDTScript;
import org.netbeans.modules.javascript.cdtdebug.ScriptsHandler;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

/**
 * Support for live application of saved files to the V8.
 */
public final class ChangeLiveSupport {

    private static final Logger LOG = Logger.getLogger(ChangeLiveSupport.class.getName());

    public static final String PROP_CHANGES = "changes";

    private final CDTDebugger dbg;
    private final FileChangeListener sourceChangeListener;
    private final FileChangeDelivery fileChangeDelivery = new FileChangeDelivery();
    private final File[] sourceChangeRoots;
    private final RequestProcessor rp = new RequestProcessor(ChangeLiveSupport.class);
    private final PropertyChangeSupport pcl = new PropertyChangeSupport(this);
    private volatile boolean haveChanges = false;

    public ChangeLiveSupport(CDTDebugger dbg) {
        this.dbg = dbg;
        this.sourceChangeListener = new SourceChangeListener();
        this.sourceChangeRoots = dbg.getScriptsHandler().getLocalRoots();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "new ChangeLiveSupport(), sourceChangeRoots = {0}", Arrays.toString(sourceChangeRoots));
        }
        if (sourceChangeRoots.length == 0) {
            FileUtil.addFileChangeListener(sourceChangeListener);
        } else {
            for (File root : sourceChangeRoots) {
                FileUtil.addRecursiveListener(sourceChangeListener, root);
            }
        }
        dbg.addListener(new CDTDebugger.Listener() {
            @Override public void notifySuspended(boolean suspended) {}

            @Override public void notifyCurrentFrame(CallFrame cf) {}

            @Override public void notifyFinished() {
                destroy();
            }
        });
    }

    public boolean hasChanges() {
        return haveChanges;
    }

    private void setHasChanges(boolean haveChanges) {
        if (haveChanges == this.haveChanges) {
            return ;
        }
        this.haveChanges = haveChanges;
        pcl.firePropertyChange(PROP_CHANGES, !haveChanges, haveChanges);
    }

    public void applyChanges() {
        fileChangeDelivery.applyChanges();
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcl.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcl.removePropertyChangeListener(l);
    }

    private void applyModifiedFiles(List<FileObject> modifiedFiles) {
        ScriptsHandler sh = dbg.getScriptsHandler();
        Collection<CDTScript> scripts = sh.getScripts();
        final Phaser phaser = new Phaser(1);
        LOG.log(Level.FINE, "applyModifiedFiles({0})", modifiedFiles);
        for (FileObject fo : modifiedFiles) {
            if (!sh.containsLocalFile(fo)) {
                continue;
            }
            String path = fo.getPath();
            String serverPath;
            try {
                serverPath = sh.getServerPath(path);
            } catch (ScriptsHandler.OutOfScope ex) {
                continue;
            }
            CDTScript script = null;
            for (CDTScript s : scripts) {
                if (serverPath.equals(s.getUrl().getPath())) {
                    script = s;
                    break;
                }
            }
            if (script == null) {
                // Not a loaded script
                continue;
            }
            String fileSource;
            try {
                fileSource = fo.asText();
            } catch (IOException ioex) {
                // Can not update scripts that can not be read.
                continue;
            }
            SetScriptSourceRequest request = new SetScriptSourceRequest();
            request.setScriptId(script.getScriptId());
            request.setScriptSource(fileSource);
            phaser.register();

            dbg.getConnection()
                    .getDebugger()
                    .setScriptSource(request)
                    .handle((res, thr) -> {
                        LOG.fine("A ChangeLive command finished.");
                        phaser.arriveAndDeregister();
                        return null;
                    });

            LOG.log(Level.FINE, "Running ChangeLive command for script {0}", script.getUrl().toASCIIString());
        }
        phaser.arriveAndAwaitAdvance();
    }


    private void destroy() {
        if (sourceChangeRoots.length == 0) {
            FileUtil.removeFileChangeListener(sourceChangeListener);
        } else {
            for (File root : sourceChangeRoots) {
                FileUtil.removeRecursiveListener(sourceChangeListener, root);
            }
        }
    }

    private final class SourceChangeListener implements FileChangeListener {

        @Override
        public void fileFolderCreated(FileEvent fe) {
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
        }

        @Override
        public void fileChanged(FileEvent fe) {
            fileChangeDelivery.add(fe.getFile());
            fe.runWhenDeliveryOver(fileChangeDelivery);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

    }

    private final class FileChangeDelivery implements Runnable {

        private final List<FileObject> changedFiles = new LinkedList<>();

        private void add(FileObject file) {
            synchronized (changedFiles) {
                changedFiles.add(file);
            }
            setHasChanges(true);
        }

        @Override
        public void run() {
            applyChanges();
        }

        void applyChanges() {
            final List<FileObject> modifiedFiles;
            synchronized (changedFiles) {
                modifiedFiles = new ArrayList<>(changedFiles);
                changedFiles.clear();
            }
            setHasChanges(false);
            rp.post(new Runnable() {
                @Override
                public void run() {
                    applyModifiedFiles(modifiedFiles);
                }
            });
        }

    }
}
