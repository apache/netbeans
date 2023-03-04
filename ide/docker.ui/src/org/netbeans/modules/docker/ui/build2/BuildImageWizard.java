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
package org.netbeans.modules.docker.ui.build2;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.docker.api.DockerInstance;
import org.netbeans.modules.docker.api.DockerAction;
import org.netbeans.modules.docker.api.DockerImage;
import org.netbeans.modules.docker.api.DockerSupport;
import org.netbeans.modules.docker.ui.build2.InputOutputCache.CachedInputOutput;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.InputOutput;

/**
 *
 * @author Petr Hejl
 */
public class BuildImageWizard {

    public static final String INSTANCE_PROPERTY = "instance";

    public static final String BUILD_CONTEXT_PROPERTY = "buildContext";

    public static final String REPOSITORY_PROPERTY = "repository";

    public static final String TAG_PROPERTY = "tag";

    public static final String DOCKERFILE_PROPERTY = "dockerfile";

    public static final String FILESYSTEM_PROPERTY = "filesystem";

    public static final String PULL_PROPERTY = "pull";

    public static final String NO_CACHE_PROPERTY = "noCache";

    public static final String BUILD_ARGUMENTS_PROPERTY = "buildArguments";

    public static final boolean PULL_DEFAULT = false;

    public static final boolean NO_CACHE_DEFAULT = false;

    private final RequestProcessor requestProcessor = new RequestProcessor(BuildImageWizard.class);

    private DockerInstance instance;

    private FileObject dockerfile;
    private FileSystem fileSystem;

    public DockerInstance getInstance() {
        return instance;
    }

    public void setInstance(DockerInstance instance) {
        try {
            this.instance = instance;
            File tmpFile = File.createTempFile("test", "test");
            this.fileSystem = FileUtil.toFileObject(tmpFile).getFileSystem();
            tmpFile.delete();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public FileObject getDockerfile() {
        return dockerfile;
    }

    public void setDockerfile(FileObject dockerfile) {
        this.dockerfile = dockerfile;
        try {
            this.fileSystem = dockerfile.getFileSystem();
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @NbBundle.Messages("LBL_BuildImage=Build Image")
    public void show() {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        if (instance == null) {
            panels.add(new BuildInstancePanel());
        }
        panels.add(new BuildContextPanel(fileSystem));
        panels.add(new BuildOptionsPanel());
        String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            JComponent c = (JComponent) panels.get(i).getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            c.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
            c.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            c.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
            c.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
            c.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
        }

        WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(panels));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(Bundle.LBL_BuildImage());

        if (instance != null) {
            wiz.putProperty(INSTANCE_PROPERTY, instance);
        }
        if (dockerfile != null && dockerfile.isData()) {
            wiz.putProperty(BUILD_CONTEXT_PROPERTY, dockerfile.getParent().getPath());
            wiz.putProperty(DOCKERFILE_PROPERTY, dockerfile.getName());
        }
        wiz.putProperty(FILESYSTEM_PROPERTY, fileSystem);

        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            Boolean pull = (Boolean) wiz.getProperty(PULL_PROPERTY);
            Boolean noCache = (Boolean) wiz.getProperty(NO_CACHE_PROPERTY);
            build((DockerInstance) wiz.getProperty(INSTANCE_PROPERTY),
                    (String) wiz.getProperty(BUILD_CONTEXT_PROPERTY),
                    (String) wiz.getProperty(DOCKERFILE_PROPERTY),
                    (Map<String, String>) wiz.getProperty(BUILD_ARGUMENTS_PROPERTY),
                    (String) wiz.getProperty(REPOSITORY_PROPERTY),
                    (String) wiz.getProperty(TAG_PROPERTY),
                    pull != null ? pull : PULL_DEFAULT,
                    noCache != null ? noCache : NO_CACHE_DEFAULT);
        }
    }

    static boolean isFinishable(FileSystem fs, String buildContext, String dockerfile) {
        if (buildContext == null) {
            return false;
        }
        String realDockerfile;
        if (dockerfile == null) {
            realDockerfile = buildContext + "/" + DockerAction.DOCKER_FILE;
        } else {
            realDockerfile = buildContext + "/" + dockerfile;
        }
        FileObject build = fs.getRoot().getFileObject(buildContext);
        FileObject fo = fs.getRoot().getFileObject(realDockerfile);
        // the last check avoids entires like Dockerfile/ to be considered valid files
        if (fo == null || !fo.isData() || !realDockerfile.endsWith(fo.getNameExt())) {
            return false;
        }
        if (build == null) {
            return false;
        }
        return FileUtil.isParentOf(build, fo);
    }

    private void build(final DockerInstance instance, final String buildContext,
            final String dockerfile, final Map<String, String> buildargs, final String repository, final String tag,
            final boolean pull, final boolean noCache) {

        assert SwingUtilities.isEventDispatchThread();

        RerunAction r = new RerunAction(requestProcessor);
        StopAction s = new StopAction();

        List<Action> actions = new ArrayList<>(2);
        Collections.addAll(actions, r, s);
        final CachedInputOutput ioData = InputOutputCache.get(Bundle.MSG_Building(buildContext), actions);
        r = (RerunAction) ioData.getActions().get(0);
        s = (StopAction) ioData.getActions().get(1);

        final InputOutput io = ioData.getInputOutput();
        final RerunAction rerun = r;
        final StopAction stop = s;

        stop.setEnabled(false);
        rerun.setEnabled(false);

        requestProcessor.post(new Runnable() {
            @Override
            public void run() {
                String file;
                if (dockerfile != null) {
                    file = buildContext + "/" + dockerfile;
                } else {
                    file = buildContext + "/" + DockerAction.DOCKER_FILE;
                }

                BuildTask.Hook hook = new BuildTask.Hook() {
                    @Override
                    public void onStart(FutureTask<DockerImage> task) {
                        stop.configure(task);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                stop.setEnabled(true);
                            }
                        });
                    }

                    @Override
                    public void onFinish() {
                        InputOutputCache.release(ioData);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                stop.setEnabled(false);
                                if (rerun.isAvailable()) {
                                    rerun.setEnabled(true);
                                }
                            }
                        });
                    }
                };

                BuildTask task = new BuildTask(instance, io, hook, fileSystem.getRoot().getFileObject(buildContext),
                        fileSystem.getRoot().getFileObject(file), buildargs, repository, tag, pull, noCache);
                rerun.configure(task);
                task.run();
            }
        });
    }

    private static class RerunAction extends AbstractAction {

        private final RequestProcessor requestProcessor;

        private BuildTask buildTask;

        private ActionStateListener listener;

        private boolean available = true;

        @NbBundle.Messages("LBL_Rerun=Rerun")
        public RerunAction(RequestProcessor requestProcessor) {
            this.requestProcessor = requestProcessor;

            setEnabled(false); // initially, until ready
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/docker/ui/resources/action_rerun.png", false)); // NOI18N
            putValue(Action.SHORT_DESCRIPTION, Bundle.LBL_Rerun());
        }

        public synchronized boolean isAvailable() {
            return available;
        }

        public synchronized void configure(BuildTask buildTask) {
            detach();
            this.buildTask = buildTask;
            this.available = true;
            attach(buildTask);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setEnabled(false); // discourage repeated clicking

            InputOutput io = buildTask.getInputOutput().get();
            if (io != null) {
                CachedInputOutput ioData = InputOutputCache.get(io);
                if (ioData != null) {
                    // should be always non null as the other allocation happens
                    // from EDT as well and thus one allocation only happens
                    requestProcessor.post(buildTask);
                }
            }
        }

        public synchronized void attach(BuildTask buildTask) {
            FileObject fo = buildTask.getDockerfile();

            this.buildTask = buildTask;
            this.listener = new ActionStateListener(this, buildTask.getInstance(), fo);

            fo.addFileChangeListener(listener);
            DockerSupport.getDefault().addChangeListener(listener);

            listener.refresh();
        }

        public synchronized void detach() {
            if (buildTask == null) {
                return;
            }

            FileObject fo = listener.getFileObject();

            fo.removeFileChangeListener(listener);
            DockerSupport.getDefault().removeChangeListener(listener);

            buildTask = null;
            listener = null;
            available = false;

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setEnabled(false);
                }
            });
        }
    }

    private static class StopAction extends AbstractAction {

        private FutureTask<DockerImage> task;

        @NbBundle.Messages("LBL_Stop=Stop")
        public StopAction() {
            setEnabled(false); // initially, until ready
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/docker/ui/resources/action_stop.png", false)); // NOI18N
            putValue(Action.SHORT_DESCRIPTION, Bundle.LBL_Stop());
        }

        public synchronized void configure(FutureTask<DockerImage> task) {
            this.task = task;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setEnabled(false); // discourage repeated clicking

            FutureTask<DockerImage> actionTask;
            synchronized (this) {
                actionTask = task;
            }

            if (actionTask != null) {
                actionTask.cancel(true);
            }
        }
    }

    private static class ActionStateListener implements ChangeListener, FileChangeListener {

        private final RerunAction action;

        private final WeakReference<DockerInstance> instance;

        private final FileObject fo;

        public ActionStateListener(RerunAction action, WeakReference<DockerInstance> instance, FileObject fo) {
            this.action = action;
            this.instance = instance;
            this.fo = fo;
        }

        public FileObject getFileObject() {
            return fo;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            refresh();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            refresh();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            refresh();
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            // noop
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            // noop
        }

        @Override
        public void fileChanged(FileEvent fe) {
            // noop
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // noop
        }

        private void refresh() {
            if (fo == null || !fo.isValid()) {
                action.detach();
            }
            DockerInstance inst = this.instance.get();
            DockerSupport integration = DockerSupport.getDefault();
            if (inst == null || !integration.getInstances().contains(inst)) {
                action.detach();
            }
        }

    }
}
