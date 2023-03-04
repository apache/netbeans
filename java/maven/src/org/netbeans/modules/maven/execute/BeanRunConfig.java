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
package org.netbeans.modules.maven.execute;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusContainerException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.options.MavenSettings;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;



/**
 *
 * @author mkleint
 */
public class BeanRunConfig implements RunConfig {
    
    private File executionDirectory;
    private WeakReference<Project> project;
    private FileObject projectDirectory;
    private List<String> goals;
    private String executionName;
    private Map<String,String> properties;
    private Map<String,Object> internalProperties;
    //for these delegate to default options for defaults.
    private boolean showDebug = MavenSettings.getDefault().isShowDebug();
    private boolean showError = MavenSettings.getDefault().isShowErrors();
    private Boolean offline = MavenSettings.getDefault().isOffline();
    private boolean updateSnapshots = MavenSettings.getDefault().isUpdateSnapshots();
    private boolean interactive = MavenSettings.getDefault().isInteractive();
    private List<String> activate;
    private boolean recursive = true;
    private String taskName;
    private RunConfig parent;
    private String actionName;
    private FileObject selectedFO;
    private MavenProject mp;
    private RunConfig preexecution;
    private ReactorStyle reactor = ReactorStyle.NONE;
    private Lookup actionContext = Lookup.EMPTY;
    
    /** Creates a new instance of BeanRunConfig */
    public BeanRunConfig() {
    }

    @Override
    public Lookup getActionContext() {
        return actionContext;
    }
    
    /**
     * create a new instance that wraps around the parent instance, allowing
     * to change values while delegating to originals if not changed.
     * @param parent
     */
    public BeanRunConfig(RunConfig parent) {
        this.parent = parent;
        //boolean props need to be caried over
        setRecursive(parent.isRecursive());
        setInteractive(parent.isInteractive());
        setOffline(parent.isOffline());
        setShowDebug(parent.isShowDebug());
        setShowError(parent.isShowError());
        setUpdateSnapshots(parent.isUpdateSnapshots());
        setReactorStyle(parent.getReactorStyle());
        setActionContext(parent.getActionContext());
    }
    
    public void setActionContext(Lookup actionContext) {
        this.actionContext = actionContext;
    }
    
    //#243897 MavenCommoandLineExecutor needs to reuse the maven project from the parent config to prevent repoading MP many times during one execution..
    public void reassignMavenProjectFromParent() {
        if (parent instanceof BeanRunConfig) {
            this.mp = ((BeanRunConfig)parent).mp;
        }
    }

    @Override
    public final File getExecutionDirectory() {
        if (parent != null && executionDirectory == null) {
            return parent.getExecutionDirectory();
        }
        return executionDirectory;
    }

    @Override
    public final void setExecutionDirectory(File executionDirectory) {
        assert executionDirectory != null : "Please reopen issue 239540 - https://netbeans.org/bugzilla/show_bug.cgi?id=239540";
        this.executionDirectory = executionDirectory;
    }

    @Override
    public final Project getProject() {
        if (parent != null && project == null) {
            return parent.getProject();
        }
        if (project != null) {
            Project prj = project.get();
            if (prj == null && projectDirectory.isValid()) {
                try {
                    prj = ProjectManager.getDefault().findProject(projectDirectory);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IllegalArgumentException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return prj;
        }
        return null;
    }

    @Override
    public final synchronized MavenProject getMavenProject() {
        if (mp != null) {
            return mp;
        }
        Project prj = getProject();
        if (prj != null) {
            NbMavenProjectImpl impl = prj.getLookup().lookup(NbMavenProjectImpl.class);
            List<String> profiles = new ArrayList<String>();
            profiles.addAll(impl.getCurrentActiveProfiles());
            if (getActivatedProfiles() != null) {
                profiles.addAll(getActivatedProfiles());
            }
            Properties props = new Properties();
            if (getProperties() != null) {
                props.putAll(getProperties());
            }
            mp = impl.loadMavenProject(EmbedderFactory.getProjectEmbedder(), profiles, props);
        }
        return mp;
     }

    public final synchronized void setProject(Project project) {
        if (project != null) {
            this.project = new WeakReference<Project>(project);
            projectDirectory  = project.getProjectDirectory();
        } else {
            this.project = null;
            projectDirectory = null;
        }
        mp = null;
    }

    @Override
    public final List<String> getGoals() {
        if (parent != null && goals == null) {
            return parent.getGoals();
        }
        return goals;
    }

    public final void setGoals(List<String> goals) {
        this.goals = goals;
    }

    @Override
    public final String getExecutionName() {
        if (parent != null && executionName == null) {
            return parent.getExecutionName();
        }
        return executionName;
    }

    public final void setExecutionName(String executionName) {
        this.executionName = executionName;
    }

    @Override public final Map<? extends String,? extends String> getProperties() {
        if (properties == null) {
            return parent != null ? parent.getProperties() : Collections.<String,String>emptyMap();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<String,String>(properties));
    }

    @Override public final void setProperty(@NonNull String key, @NullAllowed String value) {
        if (properties == null) {
            properties = new LinkedHashMap<String,String>();
            if (parent != null) {
                properties.putAll(parent.getProperties());
            }
        }
        if (value != null) {
            properties.put(key, value);
        } else {
            properties.remove(key);
        }
        //#243897 let's assume that all significant properties were set before the getMavenProject() method was called.
//        synchronized (this) {
//            mp = null;
//        }
    }
    
    @Override public final Map<? extends String,? extends Object> getInternalProperties() {
        if (internalProperties == null) {
            return parent != null ? parent.getInternalProperties() : Collections.<String, Object>emptyMap();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<String,Object>(internalProperties));
    }

    @Override public final void setInternalProperty(@NonNull String key, @NullAllowed Object value) {
        if (internalProperties == null) {
            internalProperties = new LinkedHashMap<String,Object>();
            if (parent != null) {
                internalProperties.putAll(parent.getInternalProperties());
            }
        }
        if (value != null) {
            internalProperties.put(key, value);
        } else {
            internalProperties.remove(key);
        }
    }
    
    
    @Override public final void addProperties(Map<String, String> props) {
         if (properties == null) {
            properties = new LinkedHashMap<String,String>();
            if (parent != null) {
                properties.putAll(parent.getProperties());
            }
        }
        properties.putAll(props);
        //#243897 let's assume that all significant properties were set before the getMavenProject() method was called.
//        synchronized (this) {
//            mp = null;
//        }         
    }

    @Override
    public final boolean isShowDebug() {
        return showDebug;
    }

    public final void setShowDebug(boolean showDebug) {
        this.showDebug = showDebug;
    }

    @Override
    public final boolean isShowError() {
        return showError;
    }

    public final void setShowError(boolean showError) {
        this.showError = showError;
    }

    @Override
    public final Boolean isOffline() {
        return offline;
    }

    @Override
    public final void setOffline(Boolean offline) {
        this.offline = offline;
    }

    @Override
    public final List<String> getActivatedProfiles() {
        if (parent != null && activate == null) {
            return parent.getActivatedProfiles();
        }
        if (activate != null) {
            return Collections.unmodifiableList(activate);
        }
        return Collections.<String>emptyList();
    }

    @Override
    public final void setActivatedProfiles(List<String> activeteProfiles) {
        activate = new ArrayList<String>();
        activate.addAll(activeteProfiles);
        //#243897 let's assume that all profiles were set before the getMavenProject() method was called.
//        synchronized (this) {
//            mp = null;
//        }
    }

    @Override
    public final boolean isRecursive() {
        return recursive;
    }
    
    public final void setRecursive(boolean rec) {
        recursive = rec;
    }

    @Override
    public final boolean isUpdateSnapshots() {
        return updateSnapshots;
    }
    
    public final void setUpdateSnapshots(boolean set) {
        updateSnapshots = set;
    }

    @Override
    public final String getTaskDisplayName() {
        if (parent != null && taskName == null) {
            return parent.getTaskDisplayName();
        }
        return taskName;
    }
    
    public final void setTaskDisplayName(String name) {
        taskName = name;
    }

    @Override
    public final boolean isInteractive() {
        return interactive;
    }
    
    public final void setInteractive(boolean ia) {
        interactive = ia;
    }


    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    @Override
    public String getActionName()
    {
        if (parent != null && actionName == null) {
            return parent.getActionName();
        }
        return actionName;
    }

    @Override
    public FileObject getSelectedFileObject() {
        if (parent != null && selectedFO == null) {
            return parent.getSelectedFileObject();
        }
        return selectedFO;
    }

    public void setFileObject(FileObject selectedFile) {
        this.selectedFO = selectedFile;
    }

    @Override
    public RunConfig getPreExecution() {
        if (parent != null && preexecution == null) {
            return parent.getPreExecution();
        }
        return preexecution;
    }

    public @Override void setPreExecution(RunConfig config) {
        preexecution = config;
    }

    @Override
    public final ReactorStyle getReactorStyle() {
        return reactor;
    }

    public final void setReactorStyle(ReactorStyle style) {
        reactor = style;
    }
}

