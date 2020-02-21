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
package org.netbeans.modules.cnd.makeproject.ui;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.Action;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.RemoteProject;
import org.netbeans.modules.cnd.api.remote.RemoteSyncSupport;
import org.netbeans.modules.cnd.api.remote.RemoteSyncSupport.PathMapperException;
import org.netbeans.modules.cnd.api.remote.RemoteSyncSupport.Worker;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.cnd.utils.NamedRunnable;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.spi.search.SearchInfoDefinition;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.NodeAction;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

class RemoteSyncActions {

    private static UploadAction uploadAction;
    private static DownloadAction downloadAction;

    /* A common active nodes cache to be used by all actions */
    private static final AtomicReference<Node[]> activatedNodesCache = new AtomicReference<>();
    
    private static final RequestProcessor RP = new RequestProcessor("RemoteSyncActions", 1); // NOI18N

    /** A task that activatedNodesCache */
    private static final RequestProcessor.Task clearCacheTask = RP.create(() -> {
        activatedNodesCache.set(null);
    });
    
    /** prevents instance creation */
    private RemoteSyncActions() {
    }

    public static Action createUploadAction() {
        if (uploadAction == null) {
            uploadAction = new UploadAction();
        }
        return uploadAction;
    }

    public static Action createDownloadAction() {
        if (downloadAction == null) {
            downloadAction = new DownloadAction();
        }
        return downloadAction;
    }

    private static void cacheActiveNodes(Node[] activatedNodes)  {
        activatedNodesCache.set(activatedNodes);
        clearCacheTask.schedule(5000);
    }

    /** contains upload / download similarites */
    private static abstract class UpDownLoader implements Cancellable {

        protected final ExecutionEnvironment execEnv;
        protected final String envName;
        protected final InputOutput tab;

        private boolean cancelled = false;
        private final Node[] nodes;
        private volatile Thread workingThread;

        public UpDownLoader(ExecutionEnvironment execEnv, Node[] nodes, InputOutput tab) {
            this.execEnv = execEnv;
            this.nodes = nodes;
            this.tab = tab;
            envName = ServerList.get(execEnv).getDisplayName();
        }

        public void work() {
            workingThread = Thread.currentThread();
            String title = getProgressTitle();
            tab.getOut().println(title);
            int errCnt = 0;
            int okCnt = 0;
            ProgressHandle progressHandle = ProgressHandleFactory.createHandle(title, this);
            progressHandle.start();
            try {
                if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
                    ConnectionManager.getInstance().connectTo(execEnv);
                }                
                Map<Project, Set<File>> filesMap = gatherFiles(nodes);
                int cnt = 0;
                int total = 0;
                for (Set<File> files : filesMap.values()) {
                    total += files.size();
                }
                progressHandle.switchToDeterminate(total);
                for (Map.Entry<Project, Set<File>> entry : filesMap.entrySet()) {
                    RemoteSyncSupport.Worker worker = createWorker(entry.getKey(), execEnv);
                    try {
                        for (File file : entry.getValue()) {
                            if (cancelled) {
                                break;
                            }
                            String progressMessage = getFileProgressMessage(file);
                            tab.getOut().println(progressMessage);
                            try {
                                worker.process(file, tab.getErr());
                                okCnt++;
                            } catch (InterruptedException ex) {
                                break;
                            } catch (ExecutionException ex) {
                                tab.getErr().println(NbBundle.getMessage(RemoteSyncActions.class, "ERR_FILE", file.getAbsolutePath(), ex.getMessage()));
                                errCnt++;
                            } catch (IOException ex) {
                                tab.getErr().println(NbBundle.getMessage(RemoteSyncActions.class, "ERR_FILE", file.getAbsolutePath(), ex.getMessage()));
                                errCnt++;
                            }
                            progressHandle.progress(progressMessage, cnt++);
                        }
                    } finally {
                        worker.close();
                    }
                }
            } catch (PathMapperException ex) {
                tab.getErr().println(NbBundle.getMessage(RemoteSyncActions.class, "ERR_MAPPING", ex.getFile().getAbsolutePath()));
                errCnt++;
            } catch (CancellationException ex) {
                cancelled = true;
            } catch (InterruptedIOException ex) {
                cancelled = true;
            } catch (IOException ex) {
                tab.getErr().println(NbBundle.getMessage(RemoteSyncActions.class, "ERR_CONNECT", envName));
                errCnt++;
            } finally {
                progressHandle.finish();
            }
            workingThread = null;
            if (errCnt == 0) {
                tab.getOut().println(NbBundle.getMessage(RemoteSyncActions.class, "SUMMARY_SUCCESS", okCnt));
            } else if (cancelled) {
                tab.getOut().println(NbBundle.getMessage(RemoteSyncActions.class, "SUMMARY_CANCELED", okCnt));
            } else {
                tab.getErr().println(NbBundle.getMessage(RemoteSyncActions.class, "SUMMARY_ERROR", okCnt));
            }
        }

        @Override
        public boolean cancel() {
            cancelled = true;
            Thread thread = workingThread;
            if (thread != null) {
                thread.interrupt();
            }
            return true;
        }

        protected abstract String getProgressTitle();
        protected abstract String getFileProgressMessage(File file);
        protected abstract RemoteSyncSupport.Worker createWorker(Project project, ExecutionEnvironment execEnv) throws IOException;
    }

    private static class Uploader extends UpDownLoader {

        public Uploader(ExecutionEnvironment execEnv, Node[] nodes, InputOutput tab) {
            super(execEnv, nodes, tab);
        }

        @Override
        protected String getFileProgressMessage(File file) {
            return NbBundle.getMessage(RemoteSyncActions.class, "MSG_UPLOAD_FILE", file.getAbsolutePath());
        }

        @Override
        protected String getProgressTitle() {
            return NbBundle.getMessage(RemoteSyncActions.class, "PROGRESS_TITLE_UPLOAD", envName);
        }

        @Override
        protected Worker createWorker(Project project, ExecutionEnvironment execEnv) throws IOException {
            return RemoteSyncSupport.createUploader(project, execEnv);
        }
    }

    private static class Downloader extends UpDownLoader {

        public Downloader(ExecutionEnvironment execEnv, Node[] nodes, InputOutput tab) {
            super(execEnv, nodes, tab);
        }

        @Override
        protected String getFileProgressMessage(File file) {
            return NbBundle.getMessage(RemoteSyncActions.class, "MSG_DOWNLOAD_FILE", file.getAbsolutePath());
        }

        @Override
        protected String getProgressTitle() {
            return NbBundle.getMessage(RemoteSyncActions.class, "PROGRESS_TITLE_DOWNLOAD", envName);
        }

        @Override
        protected Worker createWorker(final Project project, final ExecutionEnvironment execEnv) {
            return new Worker() {
                private final PathMap pathMap = RemoteSyncSupport.getPathMap(execEnv, project);
                @Override
                public void process(File file, Writer err) throws PathMapperException, InterruptedException, ExecutionException, IOException {
                    String remotePath = pathMap.getRemotePath(file.getAbsolutePath(), false);
                    if (remotePath == null) {
                        throw new RemoteSyncSupport.PathMapperException(file);
                    }
                    Future<Integer> task = CommonTasksSupport.downloadFile(remotePath, execEnv, file.getAbsolutePath(), err);
                    int rc = task.get();
                    if (rc != 0) {
                        throw new IOException(NbBundle.getMessage(RemoteSyncActions.class, "ERR_RC", rc));
                    }
                }
                @Override
                public void close() {}
            };
        }
    }

    private static abstract class BaseAction extends NodeAction {

        private boolean enabled;

        protected abstract void performAction(ExecutionEnvironment execEnv, Node[] activatedNodes);
        protected abstract String getDummyItemText();
        protected abstract String getItemText(String hostName);
        
        public BaseAction() {
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, Boolean.TRUE);
        }

        @Override
        protected boolean enable(Node[] activatedNodes) {
            boolean isSyncActionsEnabled = true;
            if (hasRootNode(activatedNodes)) {
                isSyncActionsEnabled = Boolean.getBoolean("cnd.remote.sync.project.action");
            }
            if (!isSyncActionsEnabled) {
                return false;
            }
            cacheActiveNodes(activatedNodes);
            Pair<ExecutionEnvironment, RemoteSyncFactory> p = getEnv(activatedNodes);
            if(p != null && p.first() != null && p.first().isRemote()) {
                RemoteSyncFactory sync = p.second();
                if (sync == null) {
                    sync = ServerList.get(p.first()).getSyncFactory();
                }
                enabled = (sync == null) ? false : sync.isCopying();
            } else {
                enabled = false;
            }
            
            return enabled;
        }

        protected boolean wasEnabled() {
            return enabled;
        }


        @Override
        protected void performAction(Node[] activatedNodes) {
            Pair<ExecutionEnvironment, RemoteSyncFactory> p = getEnv(activatedNodes);
            ExecutionEnvironment execEnv = (p == null) ? null : p.first();
            if (execEnv != null && execEnv.isRemote()) {
                performAction(execEnv, activatedNodes);
            }
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }

        @Override
        public String getName() {
            if (!wasEnabled()) {
                return getDummyItemText();
            }
            final Node[] activatedNodes = activatedNodesCache.get();
            if (activatedNodes == null || activatedNodes.length == 0) {
                return getDummyItemText();
            }

            Pair<ExecutionEnvironment, RemoteSyncFactory> p = getEnv(activatedNodes);
            ExecutionEnvironment execEnv = (p == null) ? null : p.first();
            if (execEnv == null || execEnv.isLocal()) {
                return getDummyItemText();
            }
            final String hostName = ServerList.get(execEnv).getDisplayName();
            return getItemText(hostName);
        }
    }

    private static class UploadAction extends BaseAction  {

        @Override
        protected String getDummyItemText() {
            return NbBundle.getMessage(RemoteSyncActions.class, "LBL_UploadAction_Name_0");
        }

        @Override
        protected String getItemText(String hostName) {
            return NbBundle.getMessage(RemoteSyncActions.class, "LBL_UploadAction_Name_1", hostName);
        }

        @Override
        protected void performAction(final ExecutionEnvironment execEnv, final Node[] activatedNodes) {
            RP.post(new NamedRunnable("Uploading to " + ServerList.get(execEnv).getDisplayName()) { // NOI18N
                @Override
                protected void runImpl() {
                    upload(execEnv, activatedNodes);
                }
            });
        }

    }

    private static class DownloadAction extends BaseAction{

        @Override
        protected String getDummyItemText() {
            return NbBundle.getMessage(RemoteSyncActions.class, "LBL_DownloadAction_Name_0");
        }

        @Override
        protected String getItemText(String hostName) {
            return NbBundle.getMessage(RemoteSyncActions.class, "LBL_DownloadAction_Name_1", hostName);
        }

        @Override
        protected void performAction(final ExecutionEnvironment execEnv, final Node[] activatedNodes) {
            RP.post(new NamedRunnable("Uploading to " + ServerList.get(execEnv).getDisplayName()) { // NOI18N
                @Override
                protected void runImpl() {
                    download(execEnv, activatedNodes);
                }
            });
        }

        @Override
        protected boolean enable(Node[] activatedNodes) {
            boolean isEnabled =  super.enable(activatedNodes); //To change body of generated methods, choose Tools | Templates.
            if (isEnabled) {
                return Boolean.getBoolean("cnd.remote.download.project.action");
            }
            return isEnabled;
        }
        
        
    }

    private static Pair<ExecutionEnvironment, RemoteSyncFactory> getEnv(Node[] activatedNodes) {
        Pair<ExecutionEnvironment, RemoteSyncFactory> result = null;
        for (Node node : activatedNodes) {
            Project project = getNodeProject(node);
            Pair<ExecutionEnvironment, RemoteSyncFactory> curr = getEnv(project);
            if (curr != null) {
                if (result == null) {
                    result = curr;
                } else {
                    if (!result.equals(curr)) { // Pair.equals compares both
                        return null;
                    }
                }
            }
        }
        return result;
    }

    private static Pair<ExecutionEnvironment, RemoteSyncFactory> getEnv(Project project) {        
        if (project != null) {
            RemoteProject info = project.getLookup().lookup(RemoteProject.class);
            if (info != null) {
                ExecutionEnvironment dh = info.getDevelopmentHost();
                if (dh != null) {
                    return Pair.of(dh, info.getSyncFactory());
                }
            }
        }
        ServerRecord rec = ServerList.getDefaultRecord();
        return Pair.of(rec.getExecutionEnvironment(), rec.getSyncFactory());
    }

    private static Project getNodeProject(Node node) {
        if (node == null) {
            return null;
        }
        Project project = node.getLookup().lookup(Project.class);
        if (project != null) {
            return project;
        } else {
            return getNodeProject(node.getParentNode());
        }
    }

    private static InputOutput getTab(String name, boolean reuse) {
        InputOutput tab;
        if (reuse) {
            tab = IOProvider.getDefault().getIO(name, false); // This will (sometimes!) find an existing one.
            tab.closeInputOutput(); // Close it...
        }
        tab = IOProvider.getDefault().getIO(name, true); // Create a new ...
        try {
            tab.getOut().reset();
        } catch (IOException ex) {
        }
        tab.select();
        return tab;
    }

    private static void upload(ExecutionEnvironment execEnv, Node[] nodes) {
        InputOutput tab = getTab(NbBundle.getMessage(RemoteSyncActions.class, "LBL_UploadTab_Name", execEnv), true);
        Uploader worker = new Uploader(execEnv, nodes, tab);
        worker.work();
    }

    private static void download(ExecutionEnvironment execEnv, Node[] nodes) {
        InputOutput tab = getTab(NbBundle.getMessage(RemoteSyncActions.class, "LBL_DownloadTab_Name", execEnv), true);
        Downloader worker = new Downloader(execEnv, nodes, tab);
        worker.work();
    }

    private static Map<Project, Set<File>> gatherFiles(Node[] nodes) {
        Map<Project, Set<File>> result = new HashMap<>();
        for (Node node : nodes) {
            Project project = getNodeProject(node);
            Set<File> files = result.get(project);
            if (files == null) {
                files = new HashSet<>();
                result.put(project, files);
            }
            gatherFiles(files, node);
        }
        return result;
    }

    private static void gatherFiles(Set<File> files, Node node) {
        DataObject dataObject = node.getLookup().lookup(DataObject.class);
        if (dataObject != null) {
            FileObject fo = dataObject.getPrimaryFile();
            if (fo != null) {
                File file = FileUtil.toFile(fo); // XXX:fullRemote
                if (file != null && !file.isDirectory()) {
                    files.add(file);
                    return;
                }
            }
        }
        Folder folder = node.getLookup().lookup(Folder.class);
        if (folder != null) {
            gatherFiles(files, folder);
            return;
        }
        SearchInfoDefinition searchInfo = node.getLookup().lookup(SearchInfoDefinition.class);
        if (searchInfo != null && searchInfo.canSearch()) {
            Iterator<FileObject> filesToSearch = searchInfo.filesToSearch(SearchScopeOptions.create(), new SearchListener() {}, new AtomicBoolean());
            while (filesToSearch.hasNext()) {
                FileObject fo = filesToSearch.next();
                File file = FileUtil.toFile(fo);
                if (file != null && !file.isDirectory()) {
                    files.add(file);
                }
            }
        }
    }

    private static void gatherFiles(Set<File> files, Folder folder) {
        for (Item item : folder.getItemsAsArray()) {
            FileObject fo = item.getFileObject();            
            File file = FileUtil.toFile(fo);
            if (file != null && !file.isDirectory()) {
                files.add(file);
            }
        }
        folder.getFolders().forEach((subfolder) -> {
            gatherFiles(files, subfolder);
        });
    }

    private static boolean hasRootNode(Node[] activatedNodes) {
        for (Node node : activatedNodes) {
            Folder folder = node.getLookup().lookup(Folder.class);
            if (folder != null && folder.getKind() == Folder.Kind.ROOT) {
                return true;
            }
        }
        return false;
    }
}
