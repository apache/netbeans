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

package org.netbeans.modules.javascript.v8debug.sources;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Phaser;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.lib.v8debug.PropertyBoolean;
import org.netbeans.lib.v8debug.V8Arguments;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Request;
import org.netbeans.lib.v8debug.V8Response;
import org.netbeans.lib.v8debug.V8Script;
import org.netbeans.lib.v8debug.V8StepAction;
import org.netbeans.lib.v8debug.commands.ChangeLive;
import org.netbeans.lib.v8debug.commands.ChangeLive.ChangeLog.BreakpointUpdate;
import org.netbeans.lib.v8debug.commands.ChangeLive.ChangeLog.BreakpointUpdate.Position;
import org.netbeans.lib.v8debug.commands.Continue;
import org.netbeans.lib.v8debug.commands.Source;
import org.netbeans.modules.javascript.v8debug.ScriptsHandler;
import org.netbeans.modules.javascript.v8debug.V8Debugger;
import org.netbeans.modules.javascript.v8debug.V8DebuggerEngineProvider;
import org.netbeans.modules.javascript.v8debug.api.DebuggerOptions;
import org.netbeans.modules.javascript.v8debug.frames.CallFrame;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * Support for live application of saved files to the V8.
 * 
 * @author Martin Entlicher
 */
public final class ChangeLiveSupport {
    
    private static final Logger LOG = Logger.getLogger(ChangeLiveSupport.class.getName());
    
    private static final String PREP_REGEX = "^(\\(function.*\\(.*\\).*\\{ ).*";
    private static final Pattern PREP_PATTERN = Pattern.compile(PREP_REGEX, Pattern.MULTILINE | Pattern.DOTALL);
    
    //private static final String PREP_TEXT = "(function (exports, require, module, __filename, __dirname) { ";
    private static final String APP_TEXT = "})();";
    
    public static final String PROP_CHANGES = "changes";
    
    private final V8Debugger dbg;
    private final FileChangeListener sourceChangeListener;
    private FileChangeDelivery fileChangeDelivery = new FileChangeDelivery();
    private final File[] sourceChangeRoots;
    private final RequestProcessor rp = new RequestProcessor(ChangeLiveSupport.class);
    private final PropertyChangeSupport pcl = new PropertyChangeSupport(this);
    private volatile boolean haveChanges = false;
    
    public ChangeLiveSupport(V8Debugger dbg) {
        this.dbg = dbg;
        this.sourceChangeListener = new SourceChangeListener();
        this.sourceChangeRoots = dbg.getScriptsHandler().getLocalRoots();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("new ChangeLiveSupport(), sourceChangeRoots = "+Arrays.toString(sourceChangeRoots));
        }
        if (sourceChangeRoots.length == 0) {
            FileUtil.addFileChangeListener(sourceChangeListener);
        } else {
            for (File root : sourceChangeRoots) {
                FileUtil.addRecursiveListener(sourceChangeListener, root);
            }
        }
        dbg.addListener(new V8Debugger.Listener() {
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
        Collection<V8Script> scripts = sh.getScripts();
        final AtomicBoolean doStepInto = new AtomicBoolean(false);
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
            V8Script script = null;
            for (V8Script s : scripts) {
                if (serverPath.equals(s.getName())) {
                    script = s;
                    break;
                }
            }
            if (script == null) {
                // Not a loaded script
                continue;
            }
            String origScriptSource = script.getSource();
            if (origScriptSource == null) {
                origScriptSource = script.getSourceStart();
            }
            String prependedText = null;
            Matcher matcher = PREP_PATTERN.matcher(origScriptSource);
            if (matcher.matches()) {
                int gc = matcher.groupCount();
                if (gc > 0) {
                    prependedText = matcher.group(1);
                }
            }
            String fileSource;
            try {
                fileSource = fo.asText();
            } catch (IOException ioex) {
                // Can not update scripts that can not be read.
                continue;
            }
            LOG.fine("Identified changed script "+script.getName());
            if (prependedText != null) {
                if (!fileSource.startsWith(prependedText)) {
                    // It's not there already
                    fileSource = prependedText + fileSource + APP_TEXT;
                    LOG.log(Level.FINE,"Header text added: ''{0}"+"'', appended: ''"+APP_TEXT+"''", prependedText);
                }
            }
            V8Arguments changeLiveArgs = new ChangeLive.Arguments(
                    script.getId(),
                    fileSource,
                    Boolean.FALSE
                    );
            phaser.register();
            LOG.log(Level.FINE, "Running ChangeLive command for script {0}", script.getName());
            V8Request sendCLRequest = dbg.sendCommandRequest(V8Command.Changelive, changeLiveArgs, new V8Debugger.CommandResponseCallback() {
                @Override
                public void notifyResponse(V8Request request, V8Response response) {
                    try {
                        if (response != null) {
                            ChangeLive.ResponseBody clrb = (ChangeLive.ResponseBody) response.getBody();
                            if (clrb != null) {
                                ChangeLive.ChangeLog changeLog = clrb.getChangeLog();
                                if (changeLog != null) {
                                    updateBreakpoints(changeLog.getBreakpointsUpdate());
                                }
                                PropertyBoolean doStepIn = clrb.getStepInRecommended();
                                ChangeLive.Result result = clrb.getResult();
                                if (result != null) {
                                    if (!doStepIn.hasValue()) {
                                        doStepIn = result.getStackUpdateNeedsStepIn();
                                    }
                                }
                                if (doStepIn.getValue()) {
                                    doStepInto.set(true);
                                }
                            }
                        }
                    } finally {
                        LOG.fine("A ChangeLive command finished.");
                        phaser.arriveAndDeregister();
                    }
                }
            });
            if (sendCLRequest == null) {
                phaser.arriveAndDeregister();
            }
        }
        phaser.arriveAndAwaitAdvance();
        boolean doStepIn = doStepInto.get();
        doStepIn = doStepIn && dbg.isSuspended();
        LOG.log(Level.FINE, "ALl ChangeLive commands processed. Will step into = {0}", doStepIn);
        if (doStepIn) {
            final CountDownLatch cdl = new CountDownLatch(1);
            Continue.Arguments ca = new Continue.Arguments(V8StepAction.in);
            V8Request stepInRequest = dbg.sendCommandRequest(V8Command.Continue, ca, new V8Debugger.CommandResponseCallback() {
                @Override public void notifyResponse(V8Request request, V8Response response) {
                    if (response != null) {
                        dbg.addListener(new V8Debugger.Listener() {

                            @Override public void notifySuspended(boolean suspended) {
                                if (suspended) {
                                    cdl.countDown();
                                    dbg.removeListener(this);
                                }
                            }

                            @Override public void notifyCurrentFrame(CallFrame cf) {}

                            @Override public void notifyFinished() {
                                cdl.countDown();
                                dbg.removeListener(this);
                            }
                        });
                    } else {
                        cdl.countDown();
                    }
                }
            });
            if (stepInRequest != null) {
                try {
                    cdl.await();
                } catch (InterruptedException ex) {}
            }
        }
    }

    private void updateBreakpoints(BreakpointUpdate[] breakpointsUpdate) {
        for (BreakpointUpdate bu : breakpointsUpdate) {
            long bpId = bu.getId();
            BreakpointUpdate.Type type = bu.getType();
            LOG.fine("updateBreakpoint id = "+bpId+", type = "+type);
            switch (type) {
                case CopiedToOld:
                    
                    break;
                case PositionChanged:
                    Position oldPos = bu.getOldPositions();
                    Position newPos = bu.getNewPositions();
                    dbg.getBreakpointsHandler().positionChanged(bpId,
                            newPos.getLine(), newPos.getColumn());
                    break;
            }
        }
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
            if (DebuggerOptions.getInstance().isLiveEdit()) {
                applyChanges();
            }
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
