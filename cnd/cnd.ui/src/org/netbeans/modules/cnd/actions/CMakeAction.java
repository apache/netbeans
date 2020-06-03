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
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.remote.RemoteSyncSupport;
import org.netbeans.modules.cnd.api.remote.RemoteSyncWorker;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.utils.ImportUtils;
import org.netbeans.modules.cnd.loaders.CMakeDataObject;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.ui.ModalMessageDlg;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionService;
import org.netbeans.modules.nativeexecution.api.execution.PostMessageDisplayer;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Utilities;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.WindowManager;

/**
 *
 */
public class CMakeAction extends AbstractExecutorRunAction {

    @Override
    public String getName () {
        return getString("BTN_Cmake"); // NOI18N
    }

    @Override
    protected boolean accept(DataObject object) {
        return object instanceof CMakeDataObject;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        for (int i = 0; i < activatedNodes.length; i++){
            performAction(activatedNodes[i]);
        }
    }

    protected void performAction(Node node) {
        performAction(node, null, null, getProject(node), null);
    }

    public static Future<Integer> performAction(final Node node, final ExecutionListener listener, final Writer outputListener, final Project project, final InputOutput inputOutput) {
        if (SwingUtilities.isEventDispatchThread()) {
            final ModalMessageDlg.LongWorker runner = new ModalMessageDlg.LongWorker() {
                private NativeExecutionService es;
                @Override
                public void doWork() {
                    es = CMakeAction.prepare(node, listener, outputListener, project, inputOutput);
                }
                @Override
                public void doPostRunInEDT() {
                    if (es != null) {
                        es.run();
                    }
                }
            };
            Frame mainWindow = WindowManager.getDefault().getMainWindow();
            String title = getString("DLG_TITLE_Prepare", "cmake"); // NOI18N
            String msg = getString("MSG_TITLE_Prepare", "cmake"); // NOI18N
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
        //Save file
        saveNode(node);
        DataObject dataObject = node.getLookup().lookup(DataObject.class);
        FileObject fileObject = dataObject.getPrimaryFile();
        // Build directory
        FileObject buildDirFileObject = getBuildDirectory(node,PredefinedToolKind.CMakeTool);
        if (buildDirFileObject == null) {
            trace("Run folder folder is null"); //NOI18N
            return null;
        }
        String buildDir = buildDirFileObject.getPath();
        // Executable
        String executable = getCommand(node, project, PredefinedToolKind.CMakeTool, "cmake"); // NOI18N
        // Arguments
        //String arguments = proFile.getName();
        String[] arguments =  getArguments(node, PredefinedToolKind.CMakeTool); // NOI18N
        ExecutionEnvironment execEnv = getExecutionEnvironment(fileObject, project);
        if (FileSystemProvider.getExecutionEnvironment(buildDirFileObject).isLocal()) {
            buildDir = convertToRemoteIfNeeded(execEnv, buildDir, project);
        }
        if (buildDir == null) {
            trace("Run folder folder is null"); //NOI18N
            return null;
        }
        Map<String, String> envMap = getEnv(execEnv, node, project, null);
        StringBuilder argsFlat = new StringBuilder();
        for (int i = 0; i < arguments.length; i++) {
            argsFlat.append(" "); // NOI18N
            argsFlat.append(arguments[i]);
        }
        String scriptPath = fileObject.getParent().getPath();
        if (FileSystemProvider.getExecutionEnvironment(buildDirFileObject).isLocal()) {
            scriptPath = convertToRemoteIfNeeded(execEnv, scriptPath, project);
        }
        String relativePathToScript = CndPathUtilities.toRelativePath(buildDir, scriptPath);
        if (relativePathToScript.length()>1) {
            argsFlat.append(" "); // NOI18N
            argsFlat.append(relativePathToScript);
        }
        if (inputOutput == null) {
            // Tab Name
            String tabName = execEnv.isLocal() ? getString("CMAKE_LABEL", node.getName()) : getString("CMAKE_REMOTE_LABEL", node.getName(), execEnv.getDisplayName()); // NOI18N
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
        
        traceExecutable(executable, buildDir, argsFlat, execEnv.toString(), mm.toMap());

        ProcessChangeListener processChangeListener = new ProcessChangeListener(listener, outputListener, null, syncWorker);

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv).
                setWorkingDirectory(buildDir).
                unbufferOutput(false).
                addNativeProcessListener(processChangeListener);
        
        npb.getEnvironment().putAll(mm);
        npb.redirectError();
        List<String> list = ImportUtils.parseArgs(argsFlat.toString());
        list = ImportUtils.normalizeParameters(list);
        npb.setExecutable(executable);
        npb.setArguments(list.toArray(new String[list.size()]));

        NativeExecutionDescriptor descr = new NativeExecutionDescriptor().controllable(true).
                frontWindow(true).
                inputVisible(true).
                inputOutput(inputOutput).
                showProgress(!CndUtils.isStandalone()).
                postExecution(processChangeListener).
                postMessageDisplayer(new PostMessageDisplayer.Default("CMake")). // NOI18N
                outConvertorFactory(processChangeListener);

        descr.noReset(true);
        inputOutput.getOut().println("cd '"+buildDir+"'"); //NOI18N
        inputOutput.getOut().println(executable+" "+argsFlat); //NOI18N
        return NativeExecutionService.newService(npb, descr, "cmake"); // NOI18N
    }
}
