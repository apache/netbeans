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
package org.netbeans.modules.cnd.actions;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.ExecutionDescriptor.LineConvertorFactory;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.remote.*;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetUtils;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.builds.CMakeExecSupport;
import org.netbeans.modules.cnd.builds.MakeExecSupport;
import org.netbeans.modules.cnd.builds.QMakeExecSupport;
import org.netbeans.modules.cnd.execution.ExecutionSupport;
import org.netbeans.modules.cnd.spi.toolchain.ToolchainProject;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessChangeEvent;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.LinkSupport;
import org.netbeans.modules.nativeexecution.api.util.Path;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.NodeAction;

/**
 *
 */
public abstract class AbstractExecutorRunAction extends NodeAction {

    private static final boolean TRACE = Boolean.getBoolean("cnd.discovery.trace.projectimport"); // NOI18N
    private static final Logger logger = Logger.getLogger("org.netbeans.modules.cnd.actions.AbstractExecutorRunAction"); // NOI18N

    static {
        if (TRACE) {
            logger.setLevel(Level.ALL);
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        boolean enabled;

        if (activatedNodes == null || activatedNodes.length == 0 || activatedNodes.length > 1) {
            enabled = false;
        } else {
            DataObject dataObject = activatedNodes[0].getLookup().lookup(DataObject.class);
            if (accept(dataObject)) {
                enabled = true;
            } else {
                enabled = false;
            }
        }
        return enabled;
    }

    protected abstract boolean accept(DataObject object);

    protected static Project getProject(Node node) {
        DataObject dataObject = node.getLookup().lookup(DataObject.class);
        if (dataObject != null) {
            FileObject fileObject = dataObject.getPrimaryFile();
            if (fileObject != null) {
                return FileOwnerQuery.getOwner(fileObject);
            }
        }
        return null;
    }

    protected static ExecutionEnvironment getExecutionEnvironment(FileObject fileObject, Project project) {
        if (project == null) {
            project = FileOwnerQuery.getOwner(fileObject);
        }
        ExecutionEnvironment developmentHost = null;
        if (project != null) {
            RemoteProject info = project.getLookup().lookup(RemoteProject.class);
            if (info != null) {
                ExecutionEnvironment dh = info.getDevelopmentHost();
                if (dh != null) {
                    developmentHost = dh;
                }
            }
        }
        if (developmentHost == null) {
            developmentHost = FileSystemProvider.getExecutionEnvironment(fileObject);
            if (developmentHost == null || developmentHost.isLocal()) {
                developmentHost = ServerList.getDefaultRecord().getExecutionEnvironment();
            }
        }
        return developmentHost;
    }

    private static Project findProject(Node node) {
        Node parent = node;
        while (true) {
            Project project = parent.getLookup().lookup(Project.class);
            if (project != null) {
                return project;
            }
            Node p = parent.getParentNode();
            if (p != null && p != parent) {
                parent = p;
            } else {
                return null;
            }
        }
    }

    protected static boolean isSunStudio(Node node, Project project) {
        CompilerSet set = getCompilerSet(node, project);
        if (set == null) {
            return false;
        }
        return set.getCompilerFlavor().isSunStudioCompiler();
    }

    protected static CompilerSet getCompilerSet(Node node, Project project) {
        DataObject dataObject = node.getLookup().lookup(DataObject.class);
        FileObject fileObject = dataObject.getPrimaryFile();
        if (project == null) {
            project = findProject(node);
        }
        if (project == null) {
            project = FileOwnerQuery.getOwner(fileObject);
        }
        CompilerSet set = null;
        if (project != null) {
            ToolchainProject toolchain = project.getLookup().lookup(ToolchainProject.class);
            if (toolchain != null) {
                set = toolchain.getCompilerSet();
            }
        }
        if (set == null) {
            ExecutionEnvironment executionEnvironment = FileSystemProvider.getExecutionEnvironment(fileObject);
            if (!executionEnvironment.isLocal()) {
                set = CompilerSetManager.get(executionEnvironment).getDefaultCompilerSet();
            }
            if (set == null) {
                set = CompilerSetManager.get(ExecutionEnvironmentFactory.getLocal()).getDefaultCompilerSet();
            }
        }
        return set;
    }

    protected static String getCommand(Node node, Project project, PredefinedToolKind tool, String defaultName) {
        CompilerSet set = getCompilerSet(node, project);
        String command = null;
        if (set != null) {
            Tool aTool = set.findTool(tool);
            if (aTool != null) {
                command = aTool.getPath();
            }
        }
        if (command == null || command.length() == 0) {
            if (tool == PredefinedToolKind.MakeTool) {
                MakeExecSupport mes = node.getLookup().lookup(MakeExecSupport.class);
                if (mes != null) {
                    command = mes.getMakeCommand();
                }
            } else if (tool == PredefinedToolKind.QMakeTool) {
                QMakeExecSupport mes = node.getLookup().lookup(QMakeExecSupport.class);
                if (mes != null) {
                    command = mes.getQMakeCommand();
                }
            } else if (tool == PredefinedToolKind.CMakeTool) {
                CMakeExecSupport mes = node.getLookup().lookup(CMakeExecSupport.class);
                if (mes != null) {
                    command = mes.getCMakeCommand();
                }
            }
        }
        if (command == null || command.length() == 0) {
            command = findTools(defaultName);
        }
        return command;
    }

    protected static FileObject getBuildDirectory(Node node, PredefinedToolKind tool) {
        DataObject dataObject = node.getLookup().lookup(DataObject.class);
        FileObject makeFileDir = dataObject.getPrimaryFile().getParent();
        // Build directory
        String bdir = null;
        if (tool == PredefinedToolKind.MakeTool) {
            MakeExecSupport mes = node.getLookup().lookup(MakeExecSupport.class);
            if (mes != null) {
                bdir = mes.getBuildDirectory();
            }
        } else if (tool == PredefinedToolKind.QMakeTool) {
            QMakeExecSupport mes = node.getLookup().lookup(QMakeExecSupport.class);
            if (mes != null) {
                bdir = mes.getRunDirectory();
            }
        } else if (tool == PredefinedToolKind.CMakeTool) {
            CMakeExecSupport mes = node.getLookup().lookup(CMakeExecSupport.class);
            if (mes != null) {
                bdir = mes.getRunDirectory();
            }
        }
        if (bdir == null) {
            return makeFileDir;
        } else {
            return getAbsolutePath(bdir, makeFileDir);
        }
    }

    protected static String[] getArguments(Node node, PredefinedToolKind tool) {
        String[] args = null;
        if (tool == PredefinedToolKind.QMakeTool) {
            QMakeExecSupport mes = node.getLookup().lookup(QMakeExecSupport.class);
            if (mes != null) {
                args = mes.getArguments();
            }
        } else if (tool == PredefinedToolKind.CMakeTool) {
            CMakeExecSupport mes = node.getLookup().lookup(CMakeExecSupport.class);
            if (mes != null) {
                args = mes.getArguments();
            }
        }
        if (args == null) {
            args = new String[0];
        }
        return args;
    }

    public static String findTools(String toolName) {
        for (String path : Path.getPath()) {
            String task = path + File.separatorChar + toolName;
            File tool = new File(task);
            if (tool.exists() && tool.isFile()) {
                return tool.getAbsolutePath();
            } else if (Utilities.isWindows()) {
                task = task + ".exe"; // NOI18N
                tool = new File(task);
                if (tool.exists() && tool.isFile()) {
                    return tool.getAbsolutePath();
                }
            }
        }
        return toolName;
    }

    private static Map<String, String> parseEnvironmentVariables(Collection<String> vars) {
        if (vars.isEmpty()) {
            return Collections.emptyMap();
        } else {
            Map<String, String> envMap = new HashMap<String, String>();
            for (String s : vars) {
                int i = s.indexOf('='); // NOI18N
                if (i > 0) {
                    String key = s.substring(0, i);
                    String value = s.substring(i + 1).trim();
                    if (value.length() > 1 && (value.startsWith("\"") && value.endsWith("\"") || // NOI18N
                            value.startsWith("'") && value.endsWith("'"))) { // NOI18N
                        value = value.substring(1, value.length() - 1);
                    }
                    envMap.put(key, value);
                }
            }
            return envMap;
        }
    }

    private static Map<String, String> getDefaultEnvironment(ExecutionEnvironment execEnv, Node node, Project project) {
        PlatformInfo pi = PlatformInfo.getDefault(execEnv);
        String defaultPath = pi.getPathAsString();
        CompilerSet cs = getCompilerSet(node, project);
        if (cs != null) {
            defaultPath = cs.getDirectory() + pi.pathSeparator() + defaultPath;
            // TODO Provide platform info
            String cmdDir = CompilerSetUtils.getCommandFolder(cs);
            if (cmdDir != null && 0 < cmdDir.length()) {
                // Also add msys to path. Thet's where sh, mkdir, ... are.
                defaultPath = cmdDir + pi.pathSeparator() + defaultPath;
            }
        }
        return Collections.singletonMap(pi.getPathName(), defaultPath);
    }

    private static Map<String, String> getExecCookieEnvironment(Node node) {
        ExecutionSupport execSupport = node.getLookup().lookup(ExecutionSupport.class);
        if (execSupport == null) {
            return Collections.emptyMap();
        } else {
            return parseEnvironmentVariables(Arrays.asList(execSupport.getEnvironmentVariables()));
        }
    }

    protected static Map<String, String> getEnv(ExecutionEnvironment execEnv, Node node, Project project, List<String> additionalEnvironment) {
        Map<String, String> envMap = new HashMap<String, String>(getDefaultEnvironment(execEnv, node, project));
        envMap.putAll(getExecCookieEnvironment(node));
        if (additionalEnvironment != null) {
            envMap.putAll(parseEnvironmentVariables(additionalEnvironment));
        }
        return envMap;
    }
    
    // resolve windows links
    protected static String convertPath(String path, ExecutionEnvironment execEnv) {
        if (execEnv.isLocal()) {
            return LinkSupport.resolveWindowsLink(path);
        }
        return path;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP; // FIXUP ???
    }

    protected static String getString(String key) {
        return NbBundle.getMessage(AbstractExecutorRunAction.class, key);
    }

    protected static String getString(String key, String... a1) {
        return NbBundle.getMessage(AbstractExecutorRunAction.class, key, a1);
    }

    protected static String quoteExecutable(String orig) {
        StringBuilder sb = new StringBuilder();
        String escapeChars = Utilities.isWindows() ? " \"'()" : " \"'()!"; // NOI18N

        for (char c : orig.toCharArray()) {
            if (escapeChars.indexOf(c) >= 0) { // NOI18N
                sb.append('\\');
            }
            sb.append(c);
        }

        return sb.toString();
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    private static FileObject getAbsolutePath(String bdir, FileObject relativeDirFO) {
        if (bdir.length() == 0 || bdir.equals(".")) { // NOI18N
            return relativeDirFO;
        } else if (CndPathUtilities.isPathAbsolute(bdir)) {
            try {
                FileObject res = relativeDirFO.getFileSystem().findResource(bdir);
                if (res != null && res.isValid()) {
                    res.refresh();
                }
                if (res == null || !res.isValid()) {
                    res = FileUtil.createFolder(relativeDirFO.getFileSystem().getRoot(), bdir);
                }
                return res;
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            FileObject buildDirFO = relativeDirFO.getFileObject(bdir);
            if (buildDirFO == null) {
                return null;
            } else {
                return buildDirFO;
            }
        }
        return null;
        // Canonical path not appropriate here.
        // We must emulate command line behaviour hence absolute normalized path is more appropriate here.
        // See IZ#157677:LiteSQL is not configurable in case of symlinks.
    }

    protected static void saveNode(Node node) {
        //Save file
        SaveCookie save = node.getLookup().lookup(SaveCookie.class);
        if (save != null) {
            try {
                save.save();
            } catch (IOException ex) {
            }
        }
    }

    protected static void traceExecutable(String executable, String buildDir, StringBuilder argsFlat, String host, Map<String, String> envMap) {
        if (TRACE) {
            StringBuilder buf = new StringBuilder("Run " + executable); // NOI18N
            buf.append("\n\tin folder   ").append(buildDir); // NOI18N
            buf.append("\n\targuments   ").append(argsFlat); // NOI18N
            buf.append("\n\thost        ").append(host); // NOI18N
            buf.append("\n\tenvironment "); // NOI18N
            for (Map.Entry<String, String> v : envMap.entrySet()) {
                buf.append("\n\t\t").append(v.getKey()).append("=").append(v.getValue()); // NOI18N
            }
            buf.append("\n"); // NOI18N
            logger.log(Level.INFO, buf.toString());
        }
    }

    protected static void traceExecutable(String executable, String buildDir, String[] args, String host, Map<String, String> envMap) {
        if (TRACE) {
            StringBuilder argsFlat = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                argsFlat.append(" "); // NOI18N
                argsFlat.append(args[i]);
            }
            traceExecutable(executable, buildDir, argsFlat, host, envMap);
        }
    }

    protected static void trace(String message) {
        if (TRACE) {
            logger.log(Level.INFO, message);
        }
    }

    protected static String convertToRemoteIfNeeded(ExecutionEnvironment execEnv, String localDir, Project project) {
        if (!checkConnection(execEnv)) {
            return null;
        }
        if (execEnv.isRemote()) {
            final PathMap pathMap = RemoteSyncSupport.getPathMap(execEnv, project);
            String remotePath = pathMap.getRemotePath(localDir, false);
            if (remotePath == null) {
                if (!pathMap.checkRemotePaths(new File[]{new File(localDir)}, true)) {
                    return null;
                }
                remotePath = pathMap.getRemotePath(localDir, false);
            }
            return remotePath;
        }
        return localDir;
    }

    protected static String convertToRemoveSeparatorsIfNeeded(ExecutionEnvironment execEnv, String localPath) {
        if (execEnv.isRemote()) {
            // on remote we always have Unix
            return localPath.replace("\\", "/"); // NOI18N
        } else {
            return localPath;
        }
    }

    protected static boolean checkConnection(ExecutionEnvironment execEnv) {
        if (execEnv.isRemote()) {
            try {
                ConnectionManager.getInstance().connectTo(execEnv);
                ServerRecord record = ServerList.get(execEnv);
                if (record.isOffline()) {
                    record.validate(true);
                }
                return record.isOnline();
            } catch (IOException ex) {
                return false;
            } catch (CancellationException ex) {
                return false;
            }
        } else {
            return true;
        }
    }

    public static final class ProcessChangeListener implements ChangeListener, Runnable, LineConvertorFactory {

        private final AtomicReference<NativeProcess> processRef = new AtomicReference<NativeProcess>();
        private final ExecutionListener listener;
        private Writer outputListener;
        private final LineConvertor lineConvertor;
        private final RemoteSyncWorker syncWorker;

        public ProcessChangeListener(ExecutionListener listener, Writer outputListener, LineConvertor lineConvertor, RemoteSyncWorker syncWorker) {
            this.listener = listener;
            this.outputListener = outputListener;
            this.lineConvertor = lineConvertor;
            this.syncWorker = syncWorker;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            if (!(e instanceof NativeProcessChangeEvent)) {
                return;
            }

            final NativeProcessChangeEvent event = (NativeProcessChangeEvent) e;
            processRef.compareAndSet(null, (NativeProcess) event.getSource());

            if (NativeProcess.State.RUNNING == event.state) {
                if (listener != null) {
                    listener.executionStarted(event.pid);
                }
            }
        }

        @Override
        public void run() {
            closeOutputListener();

            NativeProcess process = processRef.get();
            try {
                if (syncWorker != null) {
                    syncWorker.shutdown();
                }
            } finally {
                if (process != null && listener != null) {
                    listener.executionFinished(process.exitValue());
                }
            }
        }

        @Override
        public LineConvertor newLineConvertor() {
            return new LineConvertor() {

                @Override
                public List<ConvertedLine> convert(String line) {
                    return ProcessChangeListener.this.convert(line);
                }
            };
        }

        private synchronized void closeOutputListener() {
            if (outputListener != null) {
                try {
                    outputListener.flush();
                    outputListener.close();
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
                outputListener = null;
            }
            if (lineConvertor instanceof ChangeListener) {
                ((ChangeListener)lineConvertor).stateChanged(new ChangeEvent(this));
            }
        }

        private synchronized List<ConvertedLine> convert(String line) {
            if (outputListener != null) {
                try {
                    outputListener.write(line);
                    outputListener.write("\n"); // NOI18N
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (lineConvertor != null) {
                return lineConvertor.convert(line);
            }
            return null;
        }
    }
}
