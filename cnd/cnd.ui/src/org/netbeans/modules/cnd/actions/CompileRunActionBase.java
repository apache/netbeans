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
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.remote.RemoteSyncSupport;
import org.netbeans.modules.cnd.api.remote.RemoteSyncWorker;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.api.utils.ImportUtils;
import org.netbeans.modules.cnd.execution.CompileExecSupport;
import org.netbeans.modules.cnd.spi.toolchain.CompilerLineConvertor;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.ui.ModalMessageDlg;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionService;
import org.netbeans.modules.nativeexecution.api.execution.PostMessageDisplayer;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.WindowManager;

/**
 *
 */
public abstract class CompileRunActionBase extends AbstractExecutorRunAction {

    public CompileRunActionBase() {
    }
    
    @Override
    protected boolean accept(DataObject object) {
        return object != null && object.getLookup().lookup(CompileExecSupport.class) != null;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        // Save everything first
        LifecycleManager.getDefault().saveAll();

        for (int i = 0; i < activatedNodes.length; i++) {
            performAction(activatedNodes[i]);
        }
    }


    public void performAction(Node node) {
        performAction(node, getProject(node));
    }

    private Future<Integer> performAction(final Node node, final Project project) {
        if (SwingUtilities.isEventDispatchThread()){
            final ModalMessageDlg.LongWorker runner = new ModalMessageDlg.LongWorker() {
                private NativeExecutionService es;
                @Override
                public void doWork() {
                    es = prepare(node, project);
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
            NativeExecutionService es = prepare(node, project);
            if (es != null) {
                return es.run();
            }
        }
        return null;
    }

    private NativeExecutionService prepare(Node node, Project project) {
        final Writer outputListener = null;
        CompileExecSupport ces = node.getLookup().lookup(CompileExecSupport.class);
        if (ces == null) {
            trace("Node "+node+" does not have CompileExecSupport"); //NOI18N
            return null;
        }
        //Save file
        saveNode(node);
        DataObject dataObject = node.getLookup().lookup(DataObject.class);
        FileObject fileObject = dataObject.getPrimaryFile();
        
        // Build directory
        String bdir = ces.getRunDirectory();
        FileObject compileDirObject = RemoteFileUtil.getFileObject(fileObject.getParent(), bdir);
        if (compileDirObject == null) {
            trace("Run folder folder is null"); //NOI18N
            return null;
        }
        String compileDir = compileDirObject.getPath();
        
        ExecutionEnvironment execEnv = getExecutionEnvironment(fileObject, project);
        if (FileSystemProvider.getExecutionEnvironment(compileDirObject).isLocal()) {
            compileDir = convertToRemoteIfNeeded(execEnv, compileDir, project);
        }
        if (compileDir == null) {
            trace("Compile folder folder is null"); //NOI18N
            return null;
        }
        CompilerSet compilerSet = getCompilerSet(node, project);
        if (compilerSet == null) {
            trace("Not found tool collection"); //NOI18N
            return null;
        }
        String mimeType = fileObject.getMIMEType();
        Tool tool = null;
        if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mimeType)) {
            tool = compilerSet.findTool(PredefinedToolKind.CCCompiler);
        } else if (MIMENames.C_MIME_TYPE.equals(mimeType)) {
            tool = compilerSet.findTool(PredefinedToolKind.CCompiler);
        } else if (MIMENames.FORTRAN_MIME_TYPE.equals(mimeType)) {
            tool = compilerSet.findTool(PredefinedToolKind.FortranCompiler);
        }
        if (tool == null || tool.getPath() == null) {
            trace("Not found compiler"); //NOI18N
            return null;
        }
        
        final String compilerPath = tool.getPath();
        final StringBuilder argsFlat = new StringBuilder();
        argsFlat.append(ces.getCompileFlags()).append(' ');// NOI18N
        argsFlat.append(fileObject.getNameExt()).append(' ');// NOI18N
        argsFlat.append("-o ").append(fileObject.getName()).append(' ');// NOI18N
        argsFlat.append(ces.getLinkFlags());
        Map<String, String> envMap = getEnv(execEnv, node, project, null);
        // Tab Name
        String tabName = getTabName(node, execEnv);
        InputOutput _tab = IOProvider.getDefault().getIO(tabName, false); // This will (sometimes!) find an existing one.
        _tab.closeInputOutput(); // Close it...
        final InputOutput inputOutput = IOProvider.getDefault().getIO(tabName, true); // Create a new ...
        try {
            inputOutput.getOut().reset();
        } catch (IOException ioe) {
        }
        RemoteSyncWorker syncWorker = RemoteSyncSupport.createSyncWorker(project, inputOutput.getOut(), inputOutput.getErr());
        if (syncWorker != null) {
            if (!syncWorker.startup(envMap)) {
                trace("RemoteSyncWorker is not started up"); //NOI18N
                return null;
            }
        }
        
        MacroMap mm = MacroMap.forExecEnv(execEnv);
        mm.putAll(envMap);
        
        traceExecutable(compilerPath, compileDir, argsFlat, execEnv.toString(), mm.toMap());

        final Runnable runContext = getRunnable(tabName, inputOutput, execEnv, compileDir, fileObject.getName(), ces);
        ExecutionListener listener = new  ExecutionListener() {
            @Override
            public void executionStarted(int pid) {
                inputOutput.getOut().println(compilerPath+" "+argsFlat.toString()); //NOI18N
            }
            @Override
            public void executionFinished(int rc) {
                if (rc == 0) {
                    runContext.run();
                }
            }
        };
        CompilerLineConvertor compilerLineConvertor = new CompilerLineConvertor(project, compilerSet, execEnv, compileDirObject, inputOutput);
        AbstractExecutorRunAction.ProcessChangeListener processChangeListener = new AbstractExecutorRunAction.ProcessChangeListener(listener, outputListener, compilerLineConvertor, syncWorker);

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv).
                setWorkingDirectory(compileDir).
                unbufferOutput(false).
                addNativeProcessListener(processChangeListener);

        npb.getEnvironment().putAll(mm);
        npb.redirectError();

        List<String> list = ImportUtils.parseArgs(argsFlat.toString());
        list = ImportUtils.normalizeParameters(list);
        npb.setExecutable(compilerPath);
        npb.setArguments(list.toArray(new String[list.size()]));

        NativeExecutionDescriptor descr = new NativeExecutionDescriptor().controllable(true).
                frontWindow(true).
                inputVisible(true).
                inputOutput(inputOutput).
                outLineBased(true).
                showProgress(!CndUtils.isStandalone()).
                postExecution(processChangeListener).
                postMessageDisplayer(new PostMessageDisplayer.Default("Compile")). // NOI18N
                errConvertorFactory(processChangeListener).
                outConvertorFactory(processChangeListener);

        // Execute the shellfile
        return NativeExecutionService.newService(npb, descr, "Compile"); // NOI18N
    }
    
    protected abstract String getTabName(Node node, ExecutionEnvironment execEnv);
    
    protected abstract Runnable getRunnable(String tabName, InputOutput tab, ExecutionEnvironment execEnv, String buildDir, String executable, CompileExecSupport ces);
}
