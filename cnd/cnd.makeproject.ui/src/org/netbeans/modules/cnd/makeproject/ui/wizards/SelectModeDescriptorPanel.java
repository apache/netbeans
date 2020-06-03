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
package org.netbeans.modules.cnd.makeproject.ui.wizards;

import org.netbeans.modules.cnd.makeproject.api.ui.wizard.WizardConstants;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.wizards.BuildSupport;
import org.netbeans.modules.cnd.makeproject.api.wizards.BuildSupport.BuildFile;
import org.netbeans.modules.cnd.makeproject.api.wizards.PreBuildSupport;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.ProjectWizardPanels;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.ProjectWizardPanels.NamedPanel;
import org.netbeans.modules.cnd.makeproject.api.wizards.PreBuildSupport.PreBuildArtifact;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 */
public class SelectModeDescriptorPanel implements ProjectWizardPanels.MakeModePanel<WizardDescriptor>, NamedPanel, ChangeListener {

    private WizardDescriptor wizardDescriptor;
    private SelectModePanel component;
    private final String name;
    private final MyWizardStorage wizardStorage;
    private boolean isValid = false;
    private int generation = 0;
    private final Object lock = new Object();

    public SelectModeDescriptorPanel() {
        name = NbBundle.getMessage(SelectModePanel.class, "SelectModeName"); // NOI18N
        wizardStorage = new MyWizardStorage();
    }

    @Override
    public String getName() {
        return name;
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public SelectModePanel getComponent() {
        if (component == null) {
            component = new SelectModePanel(this);
      	    component.setName(name);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("NewMakeWizardP0"); // NOI18N
    }

    @Override
    public boolean isValid() {
        synchronized (lock) {
            return isValid;
        }
    }

    protected boolean validate(){
        int gen;
        synchronized (lock) {
            gen = generation;
        }
        boolean tmpValid = component.valid();
        boolean fire = false;
        synchronized (lock) {
            if (generation == gen) {
                isValid = tmpValid;
                fire = true;
            }
        }
        if (fire) {
            if (SwingUtilities.isEventDispatchThread()) {
                fireChangeEvent();
            } else {
                SwingUtilities.invokeLater(() -> {
                    fireChangeEvent();
                });
            }
        }
        return tmpValid;
    }

    private void setMode(boolean isSimple) {
        WizardConstants.PROPERTY_SIMPLE_MODE.put(wizardDescriptor, isSimple);
    }

    private final Set<ChangeListener> listeners = new HashSet<>(1);
    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    @Override
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    protected final void invalidate() {
        synchronized (lock) {
            isValid = false;
            generation++;
        }
        fireChangeEvent();
    }

    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }

    WizardDescriptor getWizardDescriptor(){
        return wizardDescriptor;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        String[] res;
        Object o = component.getClientProperty(WizardDescriptor.PROP_CONTENT_DATA);
        String[] names = (String[]) o;
        if (Boolean.TRUE.equals(WizardConstants.PROPERTY_SIMPLE_MODE.get(wizardDescriptor))){
            res = new String[]{names[0]};
        } else {
            res = new String[]{names[0], "..."}; // NOI18N
        }
        component.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, res);
      	fireChangeEvent();
    }

    @Override
    public boolean isFinishPanel() {
        return  Boolean.TRUE.equals(WizardConstants.PROPERTY_SIMPLE_MODE.get(wizardDescriptor));
    }

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    @Override
    public void readSettings(WizardDescriptor settings) {
        wizardDescriptor = settings;
        if (WizardConstants.PROPERTY_SIMPLE_MODE.get(wizardDescriptor) == null) {
            WizardConstants.PROPERTY_SIMPLE_MODE.put(wizardDescriptor, Boolean.TRUE);
        }
        getComponent().read(wizardDescriptor);
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        getComponent().store(settings);
         if (Boolean.TRUE.equals(WizardConstants.PROPERTY_SIMPLE_MODE.get(settings))) {
             wizardStorage.finishWizard(wizardDescriptor);
         }
    }

    public WizardStorage getWizardStorage(){
        return wizardStorage;
    }

    @Override
    public void setFinishPanel(boolean isFinishPanel) {
    }

    boolean isFullRemote() {
        return WizardConstants.PROPERTY_REMOTE_FILE_SYSTEM_ENV.get(wizardDescriptor) != null;
    }

    private class MyWizardStorage implements WizardStorage {
        volatile String projectPath = ""; // NOI18N
        volatile FileObject sourceFileObject;
        volatile String flags = ""; // NOI18N
        volatile boolean setMain = true;
        volatile boolean buildProject = true;
        volatile CompilerSet cs;
        volatile boolean defaultCompilerSet;
        volatile ExecutionEnvironment buildEnv;
        volatile ExecutionEnvironment sourceEnv;
        volatile ExecutionEnvironment fullRemoteEnv;
        volatile FileObject makefileFO;

        public MyWizardStorage() {
            buildEnv = ServerList.getDefaultRecord().getExecutionEnvironment();
            sourceEnv = NewProjectWizardUtils.getDefaultSourceEnvironment();
        }

        void finishWizard(WizardDescriptor settings) {
            if (getSourcesFileObject() == null) {
                // called from WizardDescriptor.resetWizard()
                return;
            }
            WizardConstants.PROPERTY_HOST_UID.put(settings, ExecutionEnvironmentFactory.toUniqueID(getExecutionEnvironment()));
            WizardConstants.PROPERTY_SOURCE_HOST_ENV.put(settings, getSourceExecutionEnvironment());
            WizardConstants.PROPERTY_TOOLCHAIN_DEFAULT.put(settings, isDefaultCompilerSet());
            WizardConstants.PROPERTY_TOOLCHAIN.put(settings, getCompilerSet());
            WizardConstants.PROPERTY_NATIVE_PROJ_DIR.put(settings, getSourcesFileObject().getPath());
            WizardConstants.PROPERTY_NATIVE_PROJ_FO.put(settings, getSourcesFileObject());
            WizardConstants.PROPERTY_SIMPLE_MODE.put(settings, Boolean.TRUE);
            try {
                WizardConstants.PROPERTY_PROJECT_FOLDER.put(settings, new FSPath(getSourcesFileObject().getFileSystem(), getSourcesFileObject().getPath()));
            } catch (FileStateInvalidException ex) {
            }
            PreBuildArtifact scriptArtifact = getScriptArtifact();
            BuildFile makeArtifact = null;
            if (scriptArtifact != null) {
                WizardConstants.PROPERTY_RUN_CONFIGURE.put(settings, Boolean.TRUE);
                FileObject script = scriptArtifact.getScript();
                WizardConstants.PROPERTY_CONFIGURE_RUN_FOLDER.put(settings, script.getParent().getPath());
                WizardConstants.PROPERTY_CONFIGURE_SCRIPT_PATH.put(settings, script.getPath());
                String args = scriptArtifact.getArguments(buildEnv, cs, flags);
                WizardConstants.PROPERTY_CONFIGURE_SCRIPT_ARGS.put(settings, args);
                String command = scriptArtifact.getCommandLine(args, script.getParent().getPath());
                WizardConstants.PROPERTY_CONFIGURE_COMMAND.put(settings, command);
                
                String makefile = script.getParent().getPath()+"/Makefile"; //NOI18N
                ExecutionEnvironment env = WizardConstants.PROPERTY_REMOTE_FILE_SYSTEM_ENV.get(wizardDescriptor);
                if (env != null) {
                    makefile = RemoteFileUtil.normalizeAbsolutePath(makefile, env);
                }
                makeArtifact = BuildSupport.scriptToBuildFile(makefile);
            }
            if (makeArtifact == null) {
                makeArtifact = getMakeArtifact();
            }
            if (makeArtifact != null) {
                WizardConstants.PROPERTY_RUN_REBUILD.put(settings, Boolean.TRUE);
                WizardConstants.PROPERTY_USER_MAKEFILE_PATH.put(settings, makeArtifact.getFile());
                WizardConstants.PROPERTY_WORKING_DIR.put(settings, CndPathUtilities.getDirName(makeArtifact.getFile()));
                WizardConstants.PROPERTY_BUILD_COMMAND.put(settings, makeArtifact.getBuildCommandLine(null, CndPathUtilities.getDirName(makeArtifact.getFile())));
                WizardConstants.PROPERTY_CLEAN_COMMAND.put(settings, makeArtifact.getCleanCommandLine(null, CndPathUtilities.getDirName(makeArtifact.getFile())));
            }
        }
        
        /**
         * @return the path
         */
        @Override
        public void setMode(boolean isSimple) {
            SelectModeDescriptorPanel.this.setMode(isSimple);
        }

        /**
         * @return the path
         */
        @Override
        public String getProjectPath() {
            return projectPath;
        }

        @Override
        public FileObject getSourcesFileObject() {
            return sourceFileObject;
        }

        /**
         * @param path the path to set
         */
        @Override
        public void setProjectPath(String path) {
            this.projectPath = path.trim();
        }

        @Override
        public void setSourcesFileObject(FileObject fileObject) {
            if (sourceFileObject != null && sourceFileObject.equals(fileObject)) {
                return;
            }
            this.sourceFileObject = fileObject;
        }

        @Override
        public String getConfigure(){
            PreBuildArtifact scriptArtifact = getScriptArtifact();
            if (scriptArtifact != null) {
                return scriptArtifact.getScript().getPath();
            }
            return null;
        }

        private PreBuildArtifact getScriptArtifact(){
            if (sourceFileObject != null) {
                return PreBuildSupport.findArtifactInFolder(sourceFileObject, sourceEnv, cs);
            } else {
                if (wizardDescriptor != null) {
                    ExecutionEnvironment env = WizardConstants.PROPERTY_REMOTE_FILE_SYSTEM_ENV.get(wizardDescriptor);
                    if (env == null) {
                        env = ExecutionEnvironmentFactory.getLocal();
                    }
                    FileSystem fileSystem = FileSystemProvider.getFileSystem(env);
                     return PreBuildSupport.findArtifactInFolder(fileSystem.findResource(projectPath), sourceEnv, cs);
                }
            }
            return null;
        }

        private BuildFile getMakeArtifact() {
            if (makefileFO != null) {
                return BuildSupport.scriptToBuildFile(makefileFO.getPath());
            }
            return null;
        }

        
        @Override
        public String getMake(){
            return (makefileFO == null) ? null : makefileFO.getPath();
        }

        @Override
        public void setMake(FileObject makefileFO) {
            this.makefileFO = makefileFO;
        }

        /**
         * @return the flags
         */
        @Override
        public String getFlags() {
            return flags;
        }

        /**
         * @param flags the flags to set
         */
        @Override
        public void setFlags(String flags) {
            this.flags = flags;
        }

        /**
         * @return the arguments
         */
        @Override
        public String getRealFlags() {
            PreBuildArtifact scriptArtifact = getScriptArtifact();
            if (scriptArtifact != null) {
                return scriptArtifact.getArguments(buildEnv, cs, flags);
            }
            return null;
        }

        @Override
        public String getRealCommand() {
            PreBuildArtifact scriptArtifact = getScriptArtifact();
            if (scriptArtifact != null) {
                String args = scriptArtifact.getArguments(buildEnv, cs, flags);
                return scriptArtifact.getCommandLine(args, scriptArtifact.getScript().getParent().getPath());
            }
            return null;
        }

        /**
         * @return the setMain
         */
        @Override
        public boolean isSetMain() {
            return setMain;
        }

        /**
         * @param setMain the setMain to set
         */
        @Override
        public void setSetMain(boolean setMain) {
            this.setMain = setMain;
        }

        /**
         * @return the buildProject
         */
        @Override
        public boolean isBuildProject() {
            return buildProject;
        }

        /**
         * @param buildProject the buildProject to set
         */
        @Override
        public void setBuildProject(boolean buildProject) {
            this.buildProject = buildProject;
        }

        @Override
        public void setCompilerSet(CompilerSet cs) {
            this.cs = cs;
        }

        @Override
        public CompilerSet getCompilerSet() {
            return cs;
        }

        @Override
        public void setExecutionEnvironment(ExecutionEnvironment ee) {
            this.buildEnv = ee;
        }

        @Override
        public ExecutionEnvironment getExecutionEnvironment() {
            return buildEnv;
        }

        @Override
        public ExecutionEnvironment getSourceExecutionEnvironment() {
            return sourceEnv;
        }

        @Override
        public void setSourceExecutionEnvironment(ExecutionEnvironment sourceEnv) {
            this.sourceEnv = sourceEnv;
        }

        @Override
        public void setDefaultCompilerSet(boolean defaultCompilerSet) {
            this.defaultCompilerSet = defaultCompilerSet;
        }

        @Override
        public boolean isDefaultCompilerSet() {
            return defaultCompilerSet;
        }

        @Override
        public void setFullRemoteEnv(ExecutionEnvironment fullRemoteEnv) {
            this.fullRemoteEnv = fullRemoteEnv;
        }

        @Override
        public ExecutionEnvironment getFullRemoteEnv() {
            return fullRemoteEnv;
        }
    }
}
