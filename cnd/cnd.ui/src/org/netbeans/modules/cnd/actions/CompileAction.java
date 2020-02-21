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
import javax.swing.Action;
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
import org.netbeans.modules.cnd.utils.CndLanguageStandards;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.ui.ModalMessageDlg;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionService;
import org.netbeans.modules.nativeexecution.api.execution.PostMessageDisplayer;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.FileSensitiveActions;
import org.openide.LifecycleManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.FileEntry.Folder;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.WindowManager;

/**
 * Project less compile action
 * 
 */
public class CompileAction extends AbstractExecutorRunAction {
    
    @ActionID(id = "org.netbeans.modules.cnd.actions.impl.CompileAction.compile", category = "Build")
    @ActionRegistration(lazy = false, displayName = "#BTN_Compile_File")
    @ActionReference(path = "Loaders/text/x-cnd+sourcefile/Actions", name = "CompileAction", position = 950)
    public static Action compile() {
        final Action fileCommandAction = FileSensitiveActions.fileCommandAction(
                ActionProvider.COMMAND_COMPILE_SINGLE,
                getString("BTN_Compile_File"),
                null);
        fileCommandAction.putValue("key", "CndCompileAction");// NOI18N
        return fileCommandAction;
    }
    
    public CompileAction() {
        super.putValue("key", "CndCompileAction");// NOI18N
    }
    
    @Override
    public String getName() {
        return getString("BTN_Compile_File"); // NOI18N
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


    static void performAction(Node node) {
        final Project project = getProject(node);
        if (project != null) {
            ActionProvider ap = project.getLookup().lookup(ActionProvider.class);
            if (ap != null) {
                InstanceContent ic = new InstanceContent();
                ic.add(project);
                Folder targetFolder = (Folder) node.getValue("Folder"); // NOI18N
                if (targetFolder != null) {
                    ic.add(targetFolder);
                }
                DataObject d = node.getLookup().lookup(DataObject.class);
                if (d != null) {
                    ic.add(d.getPrimaryFile());
                }
                Lookup lookup = new AbstractLookup(ic);
                if (ap.isActionEnabled(ActionProvider.COMMAND_COMPILE_SINGLE, lookup)) {
                    ap.invokeAction(ActionProvider.COMMAND_COMPILE_SINGLE, lookup);
                    return;
                }
            }
        }
        performAction(node, project);
    }

    private static Future<Integer> performAction(final Node node, final Project project) {
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
            String title = getString("DLG_TITLE_PrepareToCompile",node.getDisplayName()); // NOI18N
            String msg = getString("MSG_TITLE_PrepareToCompile",node.getDisplayName()); // NOI18N
            ModalMessageDlg.runLongTask(mainWindow, title, msg, runner, null);
        } else {
            NativeExecutionService es = prepare(node, project);
            if (es != null) {
                return es.run();
            }
        }
        return null;
    }

    private static NativeExecutionService prepare(Node node, Project project) {
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
        } else {
            try {
                ConnectionManager.getInstance().connectTo(execEnv);
            } catch (IOException ex) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(CompileAction.class, "Status.Error", execEnv.getDisplayName(), ex.getLocalizedMessage()));
                return null;
            } catch (CancellationException ex) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(CompileAction.class, "Status.Connection.Canceled", execEnv.getDisplayName()));
                return null;
            }
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
        String[] cStandardFlags = compilerSet.getCompilerFlavor().getToolchainDescriptor().getC().getCStandardFlags();
        String[] cppStandardFlags = compilerSet.getCompilerFlavor().getToolchainDescriptor().getCpp().getCppStandardFlags();
        final String compilerPath = convertPath(tool.getPath(), execEnv);
        final StringBuilder argsFlat = new StringBuilder();
        CndLanguageStandards.CndLanguageStandard standard = ces.getStandard();
        if (standard != null) {
            switch (standard) {
                case C89:
                    if (cStandardFlags != null && cStandardFlags.length > 1) {
                        argsFlat.append(cStandardFlags[1]).append(' ');
                    }
                    break;
                case C99:
                    if (cStandardFlags != null && cStandardFlags.length > 2) {
                        argsFlat.append(cStandardFlags[2]).append(' ');
                    }
                    break;
                case C11:
                    if (cStandardFlags != null && cStandardFlags.length > 3) {
                        argsFlat.append(cStandardFlags[3]).append(' ');
                    }
                    break;
                case CPP98:
                    if (cppStandardFlags != null && cppStandardFlags.length > 1) {
                        argsFlat.append(cppStandardFlags[1]).append(' ');
                    }
                    break;
                case CPP11:
                    if (cppStandardFlags != null && cppStandardFlags.length > 2) {
                        argsFlat.append(cppStandardFlags[2]).append(' ');
                    }
                    break;
                case CPP14:
                    if (cppStandardFlags != null && cppStandardFlags.length > 3) {
                        argsFlat.append(cppStandardFlags[3]).append(' ');
                    }
                    break;
                case CPP17:
                    if (cppStandardFlags != null && cppStandardFlags.length > 4) {
                        argsFlat.append(cppStandardFlags[4]).append(' ');
                    }
                    break;
            }
        }
        argsFlat.append(ces.getCompileFlags()).append(' ');// NOI18N
        argsFlat.append("-c").append(' ');// NOI18N
        argsFlat.append(fileObject.getNameExt()).append(' ');// NOI18N
        argsFlat.append("-o ").append(getDevNull(execEnv, compilerSet));// NOI18N
        Map<String, String> envMap = getEnv(execEnv, node, project, null);
        // Tab Name
        String tabName = execEnv.isLocal() ? getString("COMPILE_LABEL", node.getDisplayName()) : getString("COMPILE_REMOTE_LABEL", node.getDisplayName(), execEnv.getDisplayName()); // NOI18N
        InputOutput _tab = IOProvider.getDefault().getIO(tabName, false); // This will (sometimes!) find an existing one.
        _tab.closeInputOutput(); // Close it...
        final InputOutput tab = IOProvider.getDefault().getIO(tabName, true); // Create a new ...
        try {
            tab.getOut().reset();
        } catch (IOException ioe) {
        }
        final InputOutput inputOutput = tab;
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
        final String finalCompileDir = compileDir;
        ExecutionListener listener = new  ExecutionListener() {
            @Override
            public void executionStarted(int pid) {
                inputOutput.getOut().println("cd '"+finalCompileDir+"'"); //NOI18N
                inputOutput.getOut().println(compilerPath+" "+argsFlat.toString()); //NOI18N
            }
            @Override
            public void executionFinished(int rc) {
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
        inputOutput.getOut().println(compilerPath+" "+argsFlat.toString()); // NOI18N
        inputOutput.getOut().flush();

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

        return NativeExecutionService.newService(npb, descr, "Compile"); // NOI18N
    }

    // find out right /dev/null
    private static String getDevNull(ExecutionEnvironment execEnv, CompilerSet compilerSet) {
        if (execEnv.isLocal() && Utilities.isWindows()){
            if (!compilerSet.getCompilerFlavor().isCygwinCompiler()) {
                return "NUL"; // NOI18N
            }
        }
        return "/dev/null"; // NOI18N
    }
}
