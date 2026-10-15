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
package org.netbeans.modules.docker.ui.build2;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.docker.api.BuildEvent;
import org.netbeans.modules.docker.api.DockerAction;
import org.netbeans.modules.docker.api.DockerImage;
import org.netbeans.modules.docker.api.DockerInstance;
import org.netbeans.modules.docker.ui.output.StatusOutputListener;
import org.openide.filesystems.FileObject;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.windows.FoldHandle;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOFolding;
import org.openide.windows.InputOutput;

/**
 *
 * @author Petr Hejl
 */
public class BuildTask implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(BuildTask.class.getName());

    private final WeakReference<DockerInstance> instance;

    private final WeakReference<InputOutput> inputOutput;

    private final Hook hook;

    private final FileObject buildContext;

    private final FileObject dockerfile;

    private final Map<String, String> buildargs;

    private final String repository;

    private final String tag;

    private final boolean pull;

    private final boolean noCache;

    public BuildTask(DockerInstance instance, InputOutput inputOutput, Hook hook, FileObject buildContext,
            FileObject dockerfile, Map<String, String> buildargs, String repository, String tag, boolean pull, boolean noCache) {
        this.instance = new WeakReference<>(instance);
        this.inputOutput = new WeakReference<>(inputOutput);
        this.hook = hook;
        this.buildContext = buildContext;
        this.dockerfile = dockerfile;
        this.repository = repository;
        this.tag = tag;
        this.pull = pull;
        this.noCache = noCache;
        this.buildargs = buildargs;
    }

    public WeakReference<DockerInstance> getInstance() {
        return instance;
    }

    public WeakReference<InputOutput> getInputOutput() {
        return inputOutput;
    }

    public FileObject getBuildContext() {
        return buildContext;
    }

    public FileObject getDockerfile() {
        return dockerfile;
    }

    public String getRepository() {
        return repository;
    }

    public String getTag() {
        return tag;
    }

    public boolean isPull() {
        return pull;
    }

    public boolean isNoCache() {
        return noCache;
    }

    @NbBundle.Messages({
        "# {0} - context",
        "MSG_Building=Building {0}",
        "# {0} - file",
        "MSG_Uploading=Sending file {0}",
        "MSG_BuildCancelled=Build cancelled"
    })
    @Override
    public void run() {
        final InputOutput io = inputOutput.get();
        if (io == null) {
            return;
        }
        final DockerInstance inst = instance.get();
        if (inst == null) {
            return;
        }
        DockerAction facade = new DockerAction(inst);
        final FutureTask<DockerImage> task = facade.createBuildTask(buildContext,
                dockerfile, buildargs, repository, tag, pull, noCache, new ListenerImpl(io), new StatusOutputListener(io));
        hook.onStart(task);
        ProgressHandle handle = ProgressHandle.createHandle(Bundle.MSG_Building(buildContext), new Cancellable() {
            @Override
            public boolean cancel() {
                return task.cancel(true);
            }
        }, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                io.select();
            }
        });
        handle.start();
        try {
            io.getOut().reset();
            io.select();
            task.run();
            if (!task.isCancelled()) {
                task.get();
            } else {
                io.getErr().println(Bundle.MSG_BuildCancelled());
            }
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause == null) {
                cause = ex;
            }
            LOGGER.log(Level.INFO, null, cause);
            io.getErr().println(cause.getMessage());
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.INFO, null, ex);
            Thread.currentThread().interrupt();
        } finally {
            io.getOut().close();
            handle.finish();
            hook.onFinish();
        }
    }

    public static interface Hook {

        void onStart(FutureTask<DockerImage> task);

        void onFinish();
    }

    private static class ListenerImpl implements BuildEvent.Listener {

        private final InputOutput io;
        private final boolean foldingSupported;
        private final boolean colorSupported;

        public ListenerImpl(InputOutput io) {
            this.io = io;
            this.foldingSupported = IOFolding.isSupported(io);
            this.colorSupported = IOColorLines.isSupported(io);
        }

        @Override
        public void onEvent(BuildEvent event) {
            String message = event.getMessage();
            if (event.isUpload()) {
                io.getOut().println(Bundle.MSG_Uploading(message));
            } else if (event.isError()) {
                // FIXME should we display more details ?
                io.getErr().println(message);
            } else {
                if (colorSupported && foldingSupported && message.startsWith("POST")) { // NOI18N
                    // Print first line of request unfolded
                    // Fold the rest of lines
                    String[] split = message.split("\n", 2); // NOI18N
                    println(io, split[0]);
                    if (split.length > 1 && !split[1].isEmpty()) {
                        FoldHandle fold = IOFolding.startFold(io, false);
                        println(io, split[1]);
                        fold.silentFinish();
                    }
                    io.getOut().println();
                } else {
                    io.getOut().println(event.getMessage());
                }
            }
        }

        private void println(InputOutput io, String message) {
            if (colorSupported) {
                try {
                    IOColorLines.println(io, message, Color.GRAY);
                } catch (IOException ex) {
                    // silent
                }
            } else {
                io.getOut().println(message);
            }
        }
    }
}
