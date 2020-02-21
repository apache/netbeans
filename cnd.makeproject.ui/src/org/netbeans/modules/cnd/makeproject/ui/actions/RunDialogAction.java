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
package org.netbeans.modules.cnd.makeproject.ui.actions;

import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.launchers.Launcher;
import org.netbeans.modules.cnd.makeproject.api.launchers.LaunchersRegistry;
import org.netbeans.modules.cnd.makeproject.api.launchers.LaunchersRegistryFactory;
import org.netbeans.modules.cnd.makeproject.ui.launchers.actions.LauncherAction;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;
import org.openide.util.lookup.Lookups;
import org.openide.windows.WindowManager;

public class RunDialogAction extends NodeAction {

    protected JButton runButton = null;
    private Object options[];
    private FileObject contextFileObject;
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    private static final RequestProcessor RP = new RequestProcessor("RunDialogAction", 1); // NOI18N

    public RunDialogAction(){
    }

    private void init(boolean isRun) {
        if (runButton == null) {
            runButton = new JButton(getString("RunButtonText")); // NOI18N
            runButton.getAccessibleContext().setAccessibleDescription(getString("RunButtonAD"));// NOI18N
            options = new Object[]{
                        runButton,
                        DialogDescriptor.CANCEL_OPTION,};
        }
        if (isRun) {
            runButton.setText(getString("RunButtonText")); // NOI18N
            runButton.getAccessibleContext().setAccessibleDescription(getString("RunButtonAD"));// NOI18N
        } else {
            runButton.setText(getString("CreateButtonText")); // NOI18N
            runButton.getAccessibleContext().setAccessibleDescription(getString("CreateButtonAD"));// NOI18N
        }
    }

    @Override
    public String getName() {
        return getString("RUN_COMMAND"); // NOI18N
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        contextFileObject = actionContext.lookup(FileObject.class);
        return super.createContextAwareInstance(actionContext);
    }

    @Override
    protected void performAction(final Node[] activatedNodes) {
        FileObject executableFO = null;
        boolean isRun = true;
        if (activatedNodes != null && activatedNodes.length == 1) {
            DataObject dataObject = activatedNodes[0].getLookup().lookup(DataObject.class);
            String mime = getMime(dataObject);
            if (dataObject != null  && dataObject.isValid() && MIMENames.isBinary(mime)) {
                FileObject fo = dataObject.getPrimaryFile();
                if (fo != null) {
                    executableFO = fo;
                }
            }
        } else if (contextFileObject != null) {
            executableFO = contextFileObject;
            isRun = false;
        }
        if (executableFO != null) {
            perform(executableFO, isRun);
        }
    }

    protected String getMime(DataObject dob) {
        FileObject primaryFile = dob == null ? null : dob.getPrimaryFile();
        String mime = primaryFile == null ? "" : primaryFile.getMIMEType();// NOI18N
        return mime;
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes.length != 1) {
            return false;
        }
        DataObject dataObject = activatedNodes[0].getLookup().lookup(DataObject.class);
        String mime = getMime(dataObject);
        // disabled for core files, see issue 136696
        if (!MIMENames.isBinary(mime) || MIMENames.ELF_CORE_MIME_TYPE.equals(mime)) {
            return false;
        }
        return true;
    }

    public void perform(FileObject executableFO, boolean isRun) {
        init(isRun);
        try {
            perform(new RunDialogPanel(executableFO, runButton, isRun), isRun);
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void perform(final RunDialogPanel runDialogPanel, final boolean isRun) {
        if (WindowManager.getDefault().getRegistry().getOpened().isEmpty()){
            // It seems action was invoked from command line
            WindowManager.getDefault().invokeWhenUIReady(() -> {
                RP.post(() -> {
                    runDialogPanel.getSelectedProject(null);
                });
            });
            return;
        }

        SwingUtilities.invokeLater(() -> {
            if (running.get()) {
                return;
            }
            running.set(true);
            try {
                DialogDescriptor dialogDescriptor = new DialogDescriptor(
                        runDialogPanel,
                        isRun ? getString("RunDialogTitle") : getString("CreateDialogTitle"),// NOI18N
                        true,
                        options,
                        runButton,
                        DialogDescriptor.BOTTOM_ALIGN,
                        null,
                        null);
                Object ret = DialogDisplayer.getDefault().notify(dialogDescriptor);
                if (ret == runButton) {
                    runDialogPanel.getSelectedProject((Project project) -> {
                        performRun(project, runDialogPanel.getExecutablePath(), isRun);
                    });
                }
            } finally {
                running.set(false);
            }
        });
        
    }

    private void performRun(Project project, String executable, boolean isRun) {
        if (isRun) {
            final LaunchersRegistry registry = LaunchersRegistryFactory.getInstance(project.getProjectDirectory());
            Launcher launcher = null;
            if (registry.hasLaunchers()) {
                for (Launcher l : registry.getLaunchers()) {
                    if (executable.startsWith(l.getCommand())) {
                        launcher = l;
                        break;
                    }
                }
            }
            if (launcher == null) {
                launcher = new Launcher(executable, null);
                registry.add(launcher);
            }
            // we do not have API to "execute" launcher, so
            LauncherAction action = LauncherAction.runAsAction();
            action.createContextAwareInstance(Lookups.fixed(project));
            action.new LauncherExecutableAction(launcher).actionPerformed(null);
        }
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(RunDialogAction.class); // FIXUP ???
    }

    private String getString(String s) {
        return NbBundle.getMessage(RunDialogAction.class, s);
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    public final class SimpleRunActionProxy extends AbstractAction {

        private final Project project;
        private final String executable;

        public SimpleRunActionProxy(Project project, String executable) {
            this.project = project;
            this.executable = executable;
        }
               
        @Override
        public Object getValue(String key) {
            if (NAME.equals(key)) {
                return RunDialogAction.this.getName();
            }
            return super.getValue(key);
        }        
        
        @Override
        public void actionPerformed(ActionEvent e) {
            performRun(project, executable, true);
        }
        
    }
    
}
