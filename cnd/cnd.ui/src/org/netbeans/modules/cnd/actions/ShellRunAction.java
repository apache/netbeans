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

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.remote.RemoteSyncSupport;
import org.netbeans.modules.cnd.api.remote.RemoteSyncWorker;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetUtils;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.api.utils.ImportUtils;
import org.netbeans.modules.cnd.execution.ShellExecSupport;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.ui.ModalMessageDlg;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionService;
import org.netbeans.modules.nativeexecution.api.execution.PostMessageDisplayer;
import org.netbeans.modules.nativeexecution.api.util.LinkSupport;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Utilities;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.WindowManager;

/**
 * Base class for Make Actions ...
 */
public class ShellRunAction extends AbstractExecutorRunAction {

    @Override
    public String getName() {
        return getString("BTN_Run"); // NOI18N
    }

    @Override
    protected boolean accept(DataObject object) {
        return object != null && object.getLookup().lookup(ShellExecSupport.class) != null;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        // Save everything first
        LifecycleManager.getDefault().saveAll();

        for (int i = 0; i < activatedNodes.length; i++) {
            performAction(activatedNodes[i]);
        }
    }


    public static void performAction(Node node) {
        performAction(node, null, null, getProject(node), null);
    }

    public static Future<Integer> performAction(final Node node, final ExecutionListener listener, final Writer outputListener, final Project project, final InputOutput inputOutput) {
        if (SwingUtilities.isEventDispatchThread()){
            final ModalMessageDlg.LongWorker runner = new ModalMessageDlg.LongWorker() {
                private NativeExecutionService es;
                @Override
                public void doWork() {
                    es = prepare(node, listener, outputListener, project, inputOutput);
                }
                @Override
                public void doPostRunInEDT() {
                    if (es != null) {
                        es.run();
                    }
                }
            };
            Frame mainWindow = WindowManager.getDefault().getMainWindow();
            String title = getString("DLG_TITLE_Prepare",node.getName()); // NOI18N
            String msg = getString("MSG_TITLE_Prepare",node.getName()); // NOI18N
            ModalMessageDlg.runLongTask(mainWindow, title, msg, runner, null);
        } else {
            NativeExecutionService es = prepare(node, listener, outputListener, project, inputOutput);
            if (es != null) {
                return es.run();
            }
        }
        return null;
    }

    private static NativeExecutionService prepare(Node node, final ExecutionListener listener, final Writer outputListener, Project project, InputOutput inputOutput) {
        ShellExecSupport bes = node.getLookup().lookup(ShellExecSupport.class);
        if (bes == null) {
            trace("Node "+node+" does not have ShellExecSupport"); //NOI18N
            return null;
        }
        //Save file
        saveNode(node);
        DataObject dataObject = node.getLookup().lookup(DataObject.class);
        FileObject fileObject = dataObject.getPrimaryFile();
        
        // Build directory
        String bdir = bes.getRunDirectory();
        FileObject buildDirObject = RemoteFileUtil.getFileObject(fileObject.getParent(), bdir);
        if (buildDirObject == null) {
            trace("Run folder folder is null"); //NOI18N
            return null;
        }
        String buildDir = buildDirObject.getPath();
        
        String[] shellCommandAndArgs = bes.getShellCommandAndArgs(fileObject); // from inside shell file or properties
        String shellCommand = shellCommandAndArgs[0];
        String shellFilePath = CndPathUtilities.toRelativePath(buildDirObject, fileObject);
        if (shellFilePath.equals(fileObject.getNameExt())) {
            shellFilePath = "./" + shellFilePath; //NOI18N
        }
        shellFilePath = shellFilePath.replace('\\', '/');
        String[] args = bes.getArguments(); // from properties

        ExecutionEnvironment execEnv = getExecutionEnvironment(fileObject, project);
        if (FileSystemProvider.getExecutionEnvironment(buildDirObject).isLocal()) {
            buildDir = convertToRemoteIfNeeded(execEnv, buildDir, project);
        }
        if (buildDir == null) {
            trace("Run folder folder is null"); //NOI18N
            return null;
        }
        shellFilePath = convertToRemoveSeparatorsIfNeeded(execEnv, shellFilePath);
        // Windows: The command is usually of the from "/bin/sh", but this
        // doesn't work here, so extract the 'sh' part and use that instead. 
        // FIXUP: This is not entirely correct though.
        if (PlatformInfo.getDefault(execEnv).isWindows() && shellCommand.length() > 0) {
            shellCommand = findWindowsShell(shellCommand, execEnv, node);
            shellCommand = LinkSupport.resolveWindowsLink(shellCommand);
        }
        
        StringBuilder argsFlat = new StringBuilder();
        if (shellCommandAndArgs[0].length() > 0) {
            for (int i = 1; i < shellCommandAndArgs.length; i++) {
                argsFlat.append(" "); // NOI18N
                argsFlat.append(shellCommandAndArgs[i]);
            }
        }
        if (shellCommand.length() == 0) {
            shellCommand = fileObject.getPath();
        } else {
            argsFlat.append(shellFilePath);
        }
        for (int i = 0; i < args.length; i++) {
            argsFlat.append(" "); // NOI18N
            argsFlat.append(args[i]);
        }
        Map<String, String> envMap = getEnv(execEnv, node, project, null);
        if (inputOutput == null) {
            // Tab Name
            String tabName = execEnv.isLocal() ? getString("RUN_LABEL", node.getName()) : getString("RUN_REMOTE_LABEL", node.getName(), execEnv.getDisplayName()); // NOI18N
            InputOutput _tab = IOProvider.getDefault().getIO(tabName, false); // This will (sometimes!) find an existing one.
            _tab.closeInputOutput(); // Close it...
            final InputOutput tab = IOProvider.getDefault().getIO(tabName, true); // Create a new ...
            try {
                tab.getOut().reset();
            } catch (IOException ioe) {
            }
            inputOutput = tab;
        }
        RemoteSyncWorker syncWorker = RemoteSyncSupport.createSyncWorker(project, inputOutput.getOut(), inputOutput.getErr());
        if (syncWorker != null) {
            if (!syncWorker.startup(envMap)) {
                trace("RemoteSyncWorker is not started up"); //NOI18N
                return null;
            }
        }
        String wrapper = envMap.get("__CND_TOOL_WRAPPER__"); //NOI18N
        if (wrapper != null) {
            for(Map.Entry<String,String> e : envMap.entrySet()) {
                if ("PATH".equals(e.getKey().toUpperCase())) { //NOI18N
                    if (execEnv.isLocal() && Utilities.isWindows()) {
                        envMap.put(e.getKey(), wrapper+";"+e.getValue()); //NOI18N
                    } else {
                        envMap.put(e.getKey(), wrapper+":"+e.getValue()); //NOI18N
                    }
                    break;
                }
            }
        }
        
        MacroMap mm = MacroMap.forExecEnv(execEnv);
        mm.putAll(envMap);
        
        traceExecutable(shellCommand, buildDir, argsFlat, execEnv.toString(), mm.toMap());

        ProcessChangeListener processChangeListener = new ProcessChangeListener(listener, outputListener, null, syncWorker);

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv).
                setWorkingDirectory(buildDir).
                unbufferOutput(false).
                addNativeProcessListener(processChangeListener);

        npb.getEnvironment().putAll(mm);
        npb.redirectError();

        List<String> list = ImportUtils.parseArgs(argsFlat.toString());
        list = ImportUtils.normalizeParameters(list);
        npb.setExecutable(shellCommand);
        npb.setArguments(list.toArray(new String[list.size()]));

        NativeExecutionDescriptor descr = new NativeExecutionDescriptor().controllable(true).
                frontWindow(true).
                inputVisible(true).
                inputOutput(inputOutput).
                outLineBased(true).
                showProgress(!CndUtils.isStandalone()).
                postExecution(processChangeListener).
                postMessageDisplayer(new PostMessageDisplayer.Default("Run")). // NOI18N
                errConvertorFactory(processChangeListener).
                outConvertorFactory(processChangeListener);

        descr.noReset(true);
        inputOutput.getOut().println("cd '"+buildDir+"'"); //NOI18N
        inputOutput.getOut().println(shellCommand+" "+argsFlat); //NOI18N
        return NativeExecutionService.newService(npb, descr, "Run"); // NOI18N
    }

    private static String findWindowsShell(String shellCommand, ExecutionEnvironment execEnv, Node node) {
        int i = shellCommand.lastIndexOf('/'); // UNIX PATH // NOI18N
        if (i >= 0) {
            shellCommand = shellCommand.substring(i + 1);
        }
        File sc = new File(shellCommand);
        if (sc.exists()) {
            return shellCommand;
        }
        PlatformInfo pi = PlatformInfo.getDefault(execEnv);
        String newShellCommand = pi.findCommand(shellCommand);
        if (newShellCommand != null) {
            return newShellCommand;
        }
        List<CompilerSet> list = new ArrayList<CompilerSet>();
        CompilerSet set = getCompilerSet(node, null);
        if (set != null) {
            list.add(set);
        }
        CompilerSetManager csm = CompilerSetManager.get(execEnv);
        if (csm != null) {
            set = csm.getDefaultCompilerSet();
            if (set != null && !list.contains(set)) {
                list.add(set);
            }
            for (CompilerSet aSet : csm.getCompilerSets()) {
                if (aSet != null && !list.contains(aSet)) {
                    list.add(aSet);
                }
            }
        }
        String folder;
        for (CompilerSet aSet : list) {
            folder = CompilerSetUtils.getCommandFolder(aSet);
            if (folder != null) {
                newShellCommand = pi.findCommand(folder, shellCommand);
                if (newShellCommand != null) {
                    return newShellCommand;
                }
            } else {
                folder = aSet.getDirectory();
                if (folder != null) {
                    newShellCommand = pi.findCommand(folder, shellCommand);
                    if (newShellCommand != null) {
                        return newShellCommand;
                    }
                }
            }
        }
        folder = CompilerSetUtils.getCygwinBase();
        if (folder != null) {
            newShellCommand = pi.findCommand(folder + "/bin", shellCommand); // NOI18N
            if (newShellCommand != null) {
                return newShellCommand;
            }
        }
        folder = CompilerSetUtils.getCommandFolder(null);
        if (folder != null) {
            newShellCommand = pi.findCommand(folder, shellCommand);
            if (newShellCommand != null) {
                return newShellCommand;
            }
        }
        return shellCommand;
    }
}
