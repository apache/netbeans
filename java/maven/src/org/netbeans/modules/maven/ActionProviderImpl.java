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
package org.netbeans.modules.maven;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import static org.netbeans.modules.maven.Bundle.*;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.MavenConfiguration;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.api.ModuleInfoUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.netbeans.modules.maven.customizer.ActionMappings;
import org.netbeans.modules.maven.customizer.WarnPanel;
import org.netbeans.modules.maven.execute.ActionToGoalUtils;
import org.netbeans.modules.maven.execute.BeanRunConfig;
import org.netbeans.modules.maven.execute.ModelRunConfig;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.execute.ui.RunGoalsPanel;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.DependencyManagement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.operations.Operations;
import org.netbeans.modules.maven.options.MavenSettings;
import org.netbeans.modules.maven.spi.actions.AbstractMavenActionsProvider;
import org.netbeans.modules.maven.spi.actions.ActionConvertor;
import org.netbeans.modules.maven.spi.actions.MavenActionsProvider;
import org.netbeans.modules.maven.spi.actions.ReplaceTokenProvider;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.SingleMethod;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.StatusDisplayer;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author  Milos Kleint
 */
@ProjectServiceProvider(service={ActionProvider.class, ActionProviderImpl.class}, projectType="org-netbeans-modules-maven")
public class ActionProviderImpl implements ActionProvider {

    public static final String BUILD_WITH_DEPENDENCIES = "build-with-dependencies"; // NOI18N

    private final Project proj;

    public static final String COMMAND_RUN_MAIN = ActionProvider.COMMAND_RUN_SINGLE + ".main"; // NOI18N
    public static final String COMMAND_DEBUG_MAIN = ActionProvider.COMMAND_DEBUG_SINGLE + ".main"; // NOI18N
    public static final String COMMAND_PROFILE_MAIN = ActionProvider.COMMAND_PROFILE_SINGLE + ".main"; // NOI18N
    public static final String COMMAND_INTEGRATION_TEST_SINGLE = "integration-test.single"; // NOI18N
    public static final String COMMAND_DEBUG_INTEGRATION_TEST_SINGLE = "debug.integration-test.single"; // NOI18N

    private static final String[] supported = new String[]{
        COMMAND_BUILD,
        BUILD_WITH_DEPENDENCIES,
        COMMAND_CLEAN,
        COMMAND_REBUILD,
        "javadoc", //NOI18N
        COMMAND_TEST,
        COMMAND_TEST_SINGLE,
        SingleMethod.COMMAND_RUN_SINGLE_METHOD,
        SingleMethod.COMMAND_DEBUG_SINGLE_METHOD,
            
        COMMAND_RUN,
        COMMAND_RUN_SINGLE,
        COMMAND_DEBUG,
        COMMAND_DEBUG_SINGLE,
        COMMAND_DEBUG_TEST_SINGLE,
        "debug.fix", //NOI18N
        COMMAND_PROFILE,
        COMMAND_PROFILE_SINGLE,
        COMMAND_PROFILE_TEST_SINGLE,
        
        //operations
        COMMAND_DELETE,
        COMMAND_RENAME,
        COMMAND_MOVE,
        COMMAND_COPY,
        
        // infrastructure
        COMMAND_PRIME
    };
    
    private static final RequestProcessor RP = new RequestProcessor(ActionProviderImpl.class.getName(), 3);
    private static final Logger LOG = Logger.getLogger(ActionProviderImpl.class.getName());

    public ActionProviderImpl(Project proj) {
        this.proj = proj;
    }
    
    protected M2Configuration usedConfiguration(boolean useActive, Lookup ctx) {
        ProjectConfiguration selected = ctx.lookup(ProjectConfiguration.class);
        M2ConfigProvider configs = proj.getLookup().lookup(M2ConfigProvider.class);
        ProjectConfiguration toFind;
        
        if (selected == null) {
            if (useActive) {
                selected = configs.getActiveConfiguration();
                if (selected == null) {
                    return null;
                }
                toFind = selected;
            } else {
                return null;
            }
        } else {
            toFind = selected;
        }
        // documentation says the configuration may not be != and should be compared by equals.
        return configs.getConfigurations().stream().filter(c -> toFind.equals(c)).findFirst().orElse(null);
    }

    @Override
    public String[] getSupportedActions() {
        Set<String> supp = new HashSet<String>();
        supp.addAll( Arrays.asList(supported));
        
        M2ConfigProvider configs = proj.getLookup().lookup(M2ConfigProvider.class);
        configs.getConfigurations().forEach(c -> supp.addAll(c.getSupportedDefaultActions()));
        for (MavenActionsProvider add : ActionToGoalUtils.actionProviders(proj)) {
            Set<String> added = add.getSupportedDefaultActions();
            if (added != null) {
                supp.addAll( added);
            }
        }
        return supp.toArray(new String[0]);
    }

    private boolean usingSurefire28() {
        String v = PluginPropertyUtils.getPluginVersion(proj.getLookup().lookup(NbMavenProject.class).getMavenProject(), Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_SUREFIRE);
        return v != null && new ComparableVersion(v).compareTo(new ComparableVersion("2.8")) >= 0;
    }
    
    private boolean usingSurefire2_22() {
        String v = PluginPropertyUtils.getPluginVersion(proj.getLookup().lookup(NbMavenProject.class).getMavenProject(), Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_SUREFIRE);
        return v != null && new ComparableVersion(v).compareTo(new ComparableVersion("2.22.0")) >= 0;
    }

    private boolean usingJUnit4() { // SUREFIRE-724
        for (Artifact a : proj.getLookup().lookup(NbMavenProject.class).getMavenProject().getArtifacts()) {
            if ("junit".equals(a.getGroupId()) && ("junit".equals(a.getArtifactId()) || "junit-dep".equals(a.getArtifactId()))) { //junit-dep  see #214238
                String version = a.getVersion();
                if (version != null && new ComparableVersion(version).compareTo(new ComparableVersion("4.8")) >= 0) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean usingJUnit5() {
        return proj.getLookup().lookup(NbMavenProject.class).getMavenProject().getArtifacts()
                .stream()
                .anyMatch((a) -> ("org.junit.jupiter".equals(a.getGroupId()) && "junit-jupiter-engine".equals(a.getArtifactId()) ||
                        "org.junit.platform".equals(a.getGroupId()) && "junit-platform-engine".equals(a.getArtifactId())));
    }

    private boolean usingTestNG() {
        for (Artifact a : proj.getLookup().lookup(NbMavenProject.class).getMavenProject().getArtifacts()) {
            if ("org.testng".equals(a.getGroupId()) && "testng".equals(a.getArtifactId())) {
                return true;
            }
        }
        return false;
    }

    boolean runSingleMethodEnabled() {
        return (usingSurefire28() && (usingJUnit4() || usingTestNG())) || (usingSurefire2_22() && usingJUnit5());
    }
    
    //TODO these effectively need updating once in a while
    private static final String SUREFIRE_VERSION_SAFE = "2.15"; //2.16 is broken
    // surefire 2.22 is needed for JUnit 5
    private static final String SUREFIRE_VERSION_SAFE_5 = "2.22.0";
    private static final String JUNIT_VERSION_SAFE = "4.11";

    @Override public void invokeAction(final String action, final Lookup lookup) {
        invokeAction(action, lookup, true);
    }
    
    private void invokeAction(final String action, final Lookup lookup, boolean checkCompiler) {
        if (!checkSurefire(action) || (checkCompiler && !checkCompilerPlugin(action))) {
            return;
        }
        
        if (COMMAND_DELETE.equals(action)) {
            DefaultProjectOperations.performDefaultDeleteOperation(proj);
            return;
        }
        if (COMMAND_COPY.equals(action)) {
            DefaultProjectOperations.performDefaultCopyOperation(proj);
            return;
        }
        if (COMMAND_MOVE.equals(action)) {
            DefaultProjectOperations.performDefaultMoveOperation(proj);
            return;
        }

        if (COMMAND_RENAME.equals(action)) {
            Operations.renameProject(proj.getLookup().lookup(NbMavenProjectImpl.class));
            return;
        }
        
        if (SwingUtilities.isEventDispatchThread()) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    invokeAction(action, lookup, false);
                }
            });
            return;
        }
        //TODO if order is important, use the lookupmerger
        Collection<? extends ActionConvertor> convertors = proj.getLookup().lookupAll(ActionConvertor.class);
        String convertedAction = null;
        for (ActionConvertor convertor : convertors) {
            convertedAction = convertor.convert(action, lookup);
            if (convertedAction != null) {
                break;
            }
        }
        if (convertedAction == null) {
            convertedAction = action;
        }
        
        for (InternalActionDelegate del : proj.getLookup().lookupAll(InternalActionDelegate.class)) {
            ActionProvider ap = del.getActionProvider();
            if (Arrays.asList(ap.getSupportedActions()).contains(action)) {
                LOG.log(Level.FINE, "Runnign action {0} through provider {1}", new Object[] {
                    action, ap
                });
                ap.invokeAction(action, lookup);
                return;
            }
        }
        Lookup enhanced = new ProxyLookup(lookup, Lookups.fixed(replacements(proj, convertedAction, lookup)));

        RunConfig rc = ActionToGoalUtils.createRunConfig(convertedAction, proj.getLookup().lookup(NbMavenProjectImpl.class), 
                usedConfiguration(true, lookup), enhanced);
        if (rc == null) {
            Logger.getLogger(ActionProviderImpl.class.getName()).log(Level.INFO, "No handling for action: {0}. Ignoring.", action); //NOI18N

        } else {
            setupTaskName(action, rc, lookup);
            final ActionProgress listener = ActionProgress.start(lookup);
            final ExecutorTask task = RunUtils.run(rc);
            if (task != null) {
                task.addTaskListener((Task t) -> {
                    listener.finished(task.result() == 0);
                });
            } else {
                listener.finished(false);
            }
        }
    }
    
    private static final String SHOW_SUREFIRE_WARNING = "showSurefireWarning";

    @Messages({
        "run_single_method_disabled=Surefire 2.8+ with JUnit 4.8+ or TestNG needed to run a single test method.",
        "run_single_method_disabled5=Surefire 2.22.0 is required to run a single test method with JUnit5.",
        "TIT_RequiresUpdateOfPOM=Feature requires update of POM",
        "TXT_Run_Single_method=<html>Executing single test method requires Surefire 2.8+ and JUnit in version 4.8 and bigger. <br/><br/>Update your pom.xml?</html>",
        "TXT_Run_Single_method5=<html>Executing single test method with JUnit 5 requires Surefire 2.22.0. <br/><br/>Update your pom.xml?</html>"
    })    
    private boolean checkSurefire(final String action) {
        if (action.equals(SingleMethod.COMMAND_RUN_SINGLE_METHOD) || action.equals(SingleMethod.COMMAND_DEBUG_SINGLE_METHOD)) {
            if (!runSingleMethodEnabled()) {
                boolean ju5 = usingJUnit5();
                if (NbPreferences.forModule(ActionProviderImpl.class).getBoolean(SHOW_SUREFIRE_WARNING, true)) {
                    WarnPanel pnl = new WarnPanel(ju5 ? TXT_Run_Single_method5() : TXT_Run_Single_method());
                    Object o = DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(pnl, TIT_RequiresUpdateOfPOM(), NotifyDescriptor.YES_NO_OPTION));
                    if (pnl.disabledWarning()) {
                        NbPreferences.forModule(ActionProviderImpl.class).putBoolean(SHOW_SUREFIRE_WARNING, false);
                    }
                    if (o == NotifyDescriptor.YES_OPTION) {
                        RequestProcessor.getDefault().post(new Runnable() {
                            @Override
                            public void run() {
                                String surefireVersion = null;
                                String junitVersion = null;
                                
                                if (ju5 && !usingSurefire2_22()) {
                                    surefireVersion = SUREFIRE_VERSION_SAFE_5;
                                } else if (!usingSurefire28()) {
                                    surefireVersion = SUREFIRE_VERSION_SAFE;
                                }
                                if (!ju5) {
                                    junitVersion = usingJUnit4() || usingTestNG() ? null : JUNIT_VERSION_SAFE;
                                }
                                
                                Utilities.performPOMModelOperations(
                                        proj.getProjectDirectory().getFileObject("pom.xml"),
                                        Collections.singletonList(new UpdateSurefireOperation(
                                                surefireVersion, junitVersion
                                        ))
                                );
                                //this appears to run too fast, before the resolved model is updated.
//                                SwingUtilities.invokeLater(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        invokeAction(action, lookup);
//                                    }
//                                });
                            }
                        });
                        return false;
                    }
                }
                StatusDisplayer.getDefault().setStatusText(
                        ju5 ? run_single_method_disabled5() : run_single_method_disabled());
                return false;
            }
        }
        return true;
    }
    
    private static final String SHOW_COMPILER_TOO_OLD_WARNING = "showCompilerTooOldWarning";
    @Messages({                
        "# {0} - project display name", "TXT_CompilerTooOld=<html>Project {0} contains module-info.java, but modules need maven-compiler-plugin >= 3.6.<br/>Update your pom.xml?</html>"
    })
    private boolean checkCompilerPlugin(final String action) {
        if (action.equals(ActionProvider.COMMAND_BUILD) || 
            action.equals(ActionProvider.COMMAND_DEBUG) ||
            action.equals(ActionProvider.COMMAND_PROFILE) ||
            action.equals(ActionProvider.COMMAND_REBUILD) ||
            action.equals(ActionProvider.COMMAND_RUN) ||
            action.equals(ActionProvider.COMMAND_TEST)) 
        {
            if (!ModuleInfoUtils.checkModuleInfoAndCompilerFit(proj)) {
                if (NbPreferences.forModule(ActionProviderImpl.class).getBoolean(SHOW_COMPILER_TOO_OLD_WARNING, true)) {
                    ProjectInformation info = ProjectUtils.getInformation(proj);
                    WarnPanel pnl = new WarnPanel(TXT_CompilerTooOld(info.getDisplayName()));
                    Object o = DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(pnl, TIT_RequiresUpdateOfPOM(), NotifyDescriptor.YES_NO_OPTION));
                    if (pnl.disabledWarning()) {
                        NbPreferences.forModule(ActionProviderImpl.class).putBoolean(SHOW_COMPILER_TOO_OLD_WARNING, false);
                    }
                    if (o == NotifyDescriptor.YES_OPTION) {
                        RequestProcessor.getDefault().post(() -> {
                            Utilities.performPOMModelOperations(
                                    proj.getProjectDirectory().getFileObject("pom.xml"),
                                    Collections.singletonList(new UpdateCompilerOperation()));                            
                        });
                        return false; // false means do not continue
                    }
                }                
            }
        }
        return true;
    }
    
    public static Map<String,String> replacements(Project proj, String action, Lookup lookup) {
        Map<String,String> replacements = new HashMap<String,String>();
        for (ReplaceTokenProvider prov : proj.getLookup().lookupAll(ReplaceTokenProvider.class)) {
            replacements.putAll(prov.createReplacements(action, lookup));
        }
        return replacements;
    }

    @Messages({
        "# {0} - artifactId", "TXT_Run=Run ({0})",
        "# {0} - artifactId", "TXT_Debug=Debug ({0})",
        "# {0} - artifactId", "TXT_ApplyCodeChanges=Apply Code Changes ({0})",
        "# {0} - artifactId", "TXT_Profile=Profile ({0})",
        "# {0} - artifactId", "TXT_Test=Test ({0})",
        "# {0} - artifactId", "TXT_Build=Build ({0})",
        "# {0} - action name", "# {1} - project name", "TXT_CustomNamed={0} ({1})"
    })
    private static void setupTaskName(String action, RunConfig config, Lookup lkp) {
        assert config instanceof BeanRunConfig;
        BeanRunConfig bc = (BeanRunConfig) config;
        String title;
        DataObject dobj = lkp.lookup(DataObject.class);
        NbMavenProject prj = bc.getProject().getLookup().lookup(NbMavenProject.class);
        //#118926 prevent NPE, how come the dobj is null?
        String dobjName = dobj != null ? dobj.getName() : ""; //NOI18N
        String prjLabel = MavenSettings.OutputTabName.PROJECT_NAME.equals(MavenSettings.getDefault().getOutputTabName()) 
                ? ProjectUtils.getInformation(bc.getProject()).getDisplayName()
                : prj.getMavenProject().getArtifactId();
        if (MavenSettings.getDefault().isOutputTabShowConfig()) {
            prjLabel = prjLabel + ", " + bc.getProject().getLookup().lookup(M2ConfigProvider.class).getActiveConfiguration().getDisplayName();
        }
        if (ActionProvider.COMMAND_RUN.equals(action)) {
            title = TXT_Run(prjLabel);
        } else if (ActionProvider.COMMAND_DEBUG.equals(action)) {
            title = TXT_Debug(prjLabel);
        } else if (ActionProvider.COMMAND_PROFILE.equals(action)) {
            title = TXT_Profile(prjLabel);
        } else if (ActionProvider.COMMAND_TEST.equals(action)) {
            title = TXT_Test(prjLabel);
        } else if (action.startsWith(ActionProvider.COMMAND_RUN_SINGLE)) {
            title = TXT_Run(dobjName);
        } else if (action.startsWith(ActionProvider.COMMAND_DEBUG_SINGLE) || ActionProvider.COMMAND_DEBUG_TEST_SINGLE.equals(action)) {
            title = TXT_Debug(dobjName);
        } else if (action.startsWith(ActionProvider.COMMAND_PROFILE_SINGLE) || ActionProvider.COMMAND_PROFILE_TEST_SINGLE.equals(action)) {
            title = TXT_Profile(dobjName);
        } else if (ActionProvider.COMMAND_TEST_SINGLE.equals(action)) {
            title = TXT_Test(dobjName);
        } else if ("debug.fix".equals(action)) {
            title = TXT_ApplyCodeChanges(prjLabel);
        } else {
            if("custom".equals(action)) {
                String name = config.getActionName();
                if(name != null && name.startsWith(ActionMappings.CUSTOM_ACTION_PREFIX)) {
                    name = name.substring(ActionMappings.CUSTOM_ACTION_PREFIX.length());
                } 
                title = name != null ? TXT_CustomNamed(name, prjLabel) : TXT_Build(prjLabel);                    
            } else {
                title = TXT_Build(prjLabel);                                                                    
            }                          
        }
        bc.setTaskDisplayName(title);
        bc.setExecutionName(title);
    }

    @Override
    public boolean isActionEnabled(String action, Lookup lookup) {
        if (COMMAND_DELETE.equals(action) ||
                COMMAND_RENAME.equals(action) ||
                COMMAND_COPY.equals(action) ||
                COMMAND_MOVE.equals(action)) {
            return true;
        }
        if (action.equals(SingleMethod.COMMAND_RUN_SINGLE_METHOD) || action.equals(SingleMethod.COMMAND_DEBUG_SINGLE_METHOD)) {
            return true;
        }
        //TODO if order is important, use the lookupmerger
        Collection<? extends ActionConvertor> convertors = proj.getLookup().lookupAll(ActionConvertor.class);
        String convertedAction = null;
        for (ActionConvertor convertor : convertors) {
            convertedAction = convertor.convert(action, lookup);
            if (convertedAction != null) {
                break;
            }
        }
        if (convertedAction == null) {
            convertedAction = action;
        }
        
        for (InternalActionDelegate ap : proj.getLookup().lookupAll(InternalActionDelegate.class)) {
            if (ap.getActionProvider().isActionEnabled(action, lookup)) {
                return true;
            }
        }

        return ActionToGoalUtils.isActionEnable(convertedAction, proj.getLookup().lookup(NbMavenProjectImpl.class), 
                usedConfiguration(false, lookup), lookup);
    }

    public static Action createCustomMavenAction(String name, NetbeansActionMapping mapping, boolean showUI, Lookup context, Project project) {
        return new CustomAction(name, mapping, showUI, context, project);
    }

    private static final class CustomAction extends AbstractAction {

        private final NetbeansActionMapping mapping;
        private final boolean showUI;
        private final Lookup context;
        private final Project proj;

        private CustomAction(String name, NetbeansActionMapping mapp, boolean showUI, Lookup context, Project project) {
            mapping = mapp;
            putValue(Action.NAME, name);
            this.showUI = showUI;
            this.context = context;
            this.proj = project;
        }

        @Messages("TIT_Run_Maven=Run Maven")
        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            Map<String,String> replacements = replacements(proj, (String) getValue(Action.NAME), context);
            for (Map.Entry<String,String> entry : mapping.getProperties().entrySet()) {
                entry.setValue(AbstractMavenActionsProvider.dynamicSubstitutions(replacements, entry.getValue()));
            }

            if (!showUI) {
                final M2ConfigProvider conf = proj.getLookup().lookup(M2ConfigProvider.class);
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        ModelRunConfig rc = createCustomRunConfig(conf);
                        setupTaskName("custom", rc, context);
                        RunUtils.run(rc);
                    }
                });
                return;
            }
            RunGoalsPanel pnl = new RunGoalsPanel();
            DialogDescriptor dd = new DialogDescriptor(pnl, TIT_Run_Maven());
            final ActionToGoalMapping maps = ActionToGoalUtils.readMappingsFromFileAttributes(proj.getProjectDirectory());
            pnl.readMapping(mapping, proj.getLookup().lookup(NbMavenProjectImpl.class), maps);
            pnl.setShowDebug(MavenSettings.getDefault().isShowDebug());
            pnl.setOffline(MavenSettings.getDefault().isOffline() != null ? MavenSettings.getDefault().isOffline() : false);
            pnl.setRecursive(true);
            Object retValue = DialogDisplayer.getDefault().notify(dd);
            if (retValue == DialogDescriptor.OK_OPTION) {
                pnl.applyValues(mapping);
                if (maps.getActions().size() > 10) {
                    maps.getActions().remove(0);
                }
                maps.getActions().add(mapping);
                final String remembered = pnl.isRememberedAs();
                final Boolean offline = Boolean.valueOf(pnl.isOffline());
                final boolean debug = pnl.isShowDebug();
                final boolean recursive = pnl.isRecursive();
                final boolean updateSnapshots = pnl.isUpdateSnapshots();
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        M2ConfigProvider conf = proj.getLookup().lookup(M2ConfigProvider.class);
                        ActionToGoalUtils.writeMappingsToFileAttributes(proj.getProjectDirectory(), maps);
                        if (remembered != null) {
                            try {

                                String tit = "CUSTOM-" + remembered; //NOI18N
                                mapping.setActionName(tit);
                                mapping.setDisplayName(remembered);
                                //TODO shall we write to configuration based files or not?
                                ModelHandle2.putMapping(mapping, proj, conf.getDefaultConfig());
                            } catch (IOException ex) {
                                LOG.log(Level.INFO, "Cannot write custom action mapping", ex);
                            }
                        }
                        ModelRunConfig rc = createCustomRunConfig(conf);
                        rc.setOffline(offline);
                        rc.setShowDebug(debug);
                        rc.setRecursive(recursive);
                        rc.setUpdateSnapshots(updateSnapshots);

                        setupTaskName("custom", rc, Lookup.EMPTY); //NOI18N
                        RunUtils.run(rc);
                    }
                    
                });
                

            }
        }

        private ModelRunConfig createCustomRunConfig(M2ConfigProvider conf) {
            ModelRunConfig rc = new ModelRunConfig(proj, mapping, mapping.getActionName(), null, Lookup.EMPTY, false);

            //#171086 also inject profiles from currently selected configuratiin
            List<String> acts = new ArrayList<String>();
            acts.addAll(rc.getActivatedProfiles());
            acts.addAll(conf.getActiveConfiguration().getActivatedProfiles());
            rc.setActivatedProfiles(acts);
            Map<String, String> props = new HashMap<String, String>(rc.getProperties());
            props.putAll(conf.getActiveConfiguration().getProperties());
            rc.addProperties(props);
            rc.setTaskDisplayName(TXT_Build(proj.getLookup().lookup(NbMavenProject.class).getMavenProject().getArtifactId()));
            return rc;
        }
    }

    // XXX should this be an API somewhere?
    private abstract static class ConditionallyShownAction extends AbstractAction implements ContextAwareAction {
        protected boolean triggeredOnFile = false;
        protected boolean triggeredOnPom = false;
        
        protected ConditionallyShownAction() {
            setEnabled(false);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        }

        public final @Override void actionPerformed(ActionEvent e) {
            assert false;
        }

        protected abstract Action forProject(@NonNull Project p, @NullAllowed FileObject file);

        public final @Override Action createContextAwareInstance(Lookup actionContext) {
            triggeredOnFile = false;
            triggeredOnPom = false;
            Collection<? extends Project> projects = actionContext.lookupAll(Project.class);
            if (projects.size() != 1) {
                Collection<? extends FileObject> fobs = actionContext.lookupAll(FileObject.class);
                if (fobs.size() == 1) {
                    FileObject fo = fobs.iterator().next();
                    if ("pom.xml".equals(fo.getNameExt())) {
                        Project p = null;
                        // issue #262651 FileOwnerQuery.getOwner() might block, so lets see first
                        // if the projects we got in the lookup do not own the pom.xml
                        FileObject parent = fo.getParent();
                        for (Project prj : projects) {
                            if(prj.getProjectDirectory().equals(parent)) {
                                p = prj;
                                break;
                            }
                        }
                        if(p == null) {
                            p = FileOwnerQuery.getOwner(fo);
                        }
                        if (p != null) {
                             triggeredOnFile = true;
                             triggeredOnPom = true;
                             Action a = forProject(p, null);
                             return a != null ? a : this;
                        }
                    } else {
                        //other non-pom files
                        
                        // issue #262651 FileOwnerQuery.getOwner() might block, so lets see first
                        // if the projects we got in the lookup do not own the pom.xml
                        Project p = findOwner(projects, fo);
                        if(p == null) {
                            p = FileOwnerQuery.getOwner(fo);                            
                        }                        
                        if (p != null) {
                             triggeredOnFile = true;
                             Action a = forProject(p, fo);
                             return a != null ? a : this;
                        }
                    }
                }
                return this;
            }
            Action a = forProject(projects.iterator().next(), null);
            return a != null ? a : this;
        }

        private Project findOwner(Collection<? extends Project> projects, FileObject fo) {
            FileObject parent = fo.getParent();
            if(parent == null) {
                return null;
            }
            for (Project prj : projects) {
                if(prj.getProjectDirectory().equals(fo.getParent())) {
                    return prj;
                }
            }
            return null;
        }
    }

    @ActionID(id = "org.netbeans.modules.maven.customPopup", category = "Project")
    @ActionRegistration(displayName = "#LBL_Custom_Run", lazy=false)
    @ActionReferences({
        @ActionReference(position = 1400, path = "Projects/org-netbeans-modules-maven/Actions"),
        @ActionReference(position = 250, path = "Loaders/text/x-maven-pom+xml/Actions"),
        @ActionReference(position = 1296, path = "Loaders/text/x-java/Actions"),
        @ActionReference(position = 1821, path = "Editors/text/x-java/Popup")
    })
    @Messages({"LBL_Custom_Run=Run Maven", "LBL_Custom_Run_File=Run Maven"})
    public static ContextAwareAction customPopupActions() {
        return new ConditionallyShownAction() {
            
            protected @Override Action forProject(Project p, FileObject fo) {
                ActionProviderImpl ap = p.getLookup().lookup(ActionProviderImpl.class);
                return ap != null ? ap.new CustomPopupActions(triggeredOnFile, triggeredOnPom, fo) : null;
            }
        };
    }
    private final class CustomPopupActions extends AbstractAction implements Presenter.Popup {
        private final boolean onFile;
        private final boolean onPom;
        private final Lookup lookup;

        private CustomPopupActions(boolean onFile, boolean onPomFile, FileObject fo) {
            putValue(Action.NAME, onFile ? LBL_Custom_Run_File() : LBL_Custom_Run());
            this.onFile = onFile;
            this.onPom = onPomFile;
            this.lookup = fo != null ? Lookups.singleton(fo) : Lookup.EMPTY;
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
        }

        
        @Messages({
            "LBL_Loading=Loading...",
            "LBL_Custom_run_goals=Goals..."
        })
        @Override public JMenuItem getPopupPresenter() {

            final JMenu menu = new JMenu(onFile ? LBL_Custom_Run_File() : LBL_Custom_Run());
            final JMenuItem loading = new JMenuItem(LBL_Loading());

            menu.add(loading);
            /*using lazy construction strategy*/
            RP.post(new Runnable() {

                @Override
                public void run() {
                    NetbeansActionMapping[] maps;
                    if (onFile && !onPom) {
                      maps = ActionToGoalUtils.getActiveCustomMappingsForFile(proj.getLookup().lookup(NbMavenProjectImpl.class));
                    } else {
                      maps = ActionToGoalUtils.getActiveCustomMappings(proj.getLookup().lookup(NbMavenProjectImpl.class));
                    }
                    final List<Action> acts = new ArrayList<Action>();
                    for (NetbeansActionMapping mapp : maps) {
                        Action act = createCustomMavenAction(mapp.getActionName(), mapp, false, lookup, proj);
                        act.putValue(NAME, mapp.getDisplayName() == null ? mapp.getActionName() : mapp.getDisplayName());
                        acts.add(act);
                    }
                    acts.add(createCustomMavenAction(LBL_Custom_run_goals(), new NetbeansActionMapping(), true, lookup, proj));
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            boolean selected = menu.isSelected();
                            menu.remove(loading);
                            for (Action a : acts) {
                                menu.add(new JMenuItem(a));
                            }
                            menu.getPopupMenu().pack();
                            menu.repaint();
                            menu.updateUI();
                            menu.setSelected(selected);
                        }
                    });
                }
            }, 100);
            return menu;
        }
    }

    @ActionID(id = "org.netbeans.modules.maven.closeSubprojects", category = "Project")
    @ActionRegistration(displayName = "#ACT_CloseRequired", lazy=false)
    @ActionReference(position = 2000, path = "Projects/org-netbeans-modules-maven/Actions")
    @Messages("ACT_CloseRequired=Close Required Projects")
    public static ContextAwareAction closeSubprojectsAction() {
        return new ConditionallyShownAction() {
            protected @Override Action forProject(Project p, FileObject fo) {
                NbMavenProjectImpl project = p.getLookup().lookup(NbMavenProjectImpl.class);
                if (project != null && NbMavenProject.TYPE_POM.equalsIgnoreCase(project.getProjectWatcher().getPackagingType())) {
                    return new CloseSubprojectsAction(project);
                } else {
                    return null;
                }
            }
        };
    }
    private static class CloseSubprojectsAction extends AbstractAction {
        private final NbMavenProjectImpl project;
        public CloseSubprojectsAction(NbMavenProjectImpl project) {
            this.project = project;
            putValue(Action.NAME, ACT_CloseRequired());
        }
        public @Override void actionPerformed(ActionEvent e) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    Set<Project> res = ProjectUtils.getContainedProjects(project, true);
                    Project[] arr = res.toArray(new Project[0]);
                    OpenProjects.getDefault().close(arr);
                }
            });
            
        }
    }

    @ActionID(id = "org.netbeans.modules.maven.buildWithDependencies", category = "Project")
    @ActionRegistration(displayName = "#ACT_Build_Deps", lazy=false)
    @ActionReference(position = 500, path = "Projects/org-netbeans-modules-maven/Actions")
    @Messages("ACT_Build_Deps=Build with Dependencies")
    public static ContextAwareAction buildWithDependenciesAction() {
        return (ContextAwareAction) ProjectSensitiveActions.projectCommandAction(BUILD_WITH_DEPENDENCIES, ACT_Build_Deps(), null);
    }

    private static class UpdateSurefireOperation implements ModelOperation<POMModel> {
        private final String newJUnit;
        private final String newSurefirePluginVersion;

        public UpdateSurefireOperation(@NullAllowed String newSurefirePluginVersion, @NullAllowed String newJUnit) {
            this.newSurefirePluginVersion = newSurefirePluginVersion;
            this.newJUnit = newJUnit;
        }

        
        
        @Override
        public void performOperation(POMModel model) {
            org.netbeans.modules.maven.model.pom.Project prj = model.getProject();
            if (newJUnit != null) {
                findDependency("junit", "junit", newJUnit, prj);
            }
            if (newSurefirePluginVersion != null) {
                ModelUtils.updatePluginVersion(Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_SUREFIRE, newSurefirePluginVersion, prj);
            }
            ModelUtils.openAtPlugin(model, Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_SUREFIRE);
        }

        private void findDependency(String groupId, String artifactID, String newVersion, org.netbeans.modules.maven.model.pom.Project prj) {
            DependencyManagement dm = prj.getDependencyManagement();
            boolean setInDM = false;
            boolean setInDeps = false;
            if (dm != null) {
                Dependency dep = dm.findDependencyById(groupId, artifactID, null);
                if (dep != null) {
                    dep.setVersion(newVersion);
                    setInDM = true;
                }
            }
            //TODO search profiles?
            Dependency dep = prj.findDependencyById(groupId, artifactID, null);
            if (dep != null) {
                if (dep.getVersion() != null) {
                    dep.setVersion(newVersion);
                    setInDeps = true;
                } else {
                    if (!setInDM) { //dm in parent maybe? set here
                        dep.setVersion(newVersion);
                        setInDeps = true;
                    }
                }
            }
            if (!setInDM && !setInDeps) {
                //not found in current project (likely in one of parents), we need to insert here.
                Dependency d = prj.getModel().getFactory().createDependency();
                d.setArtifactId(artifactID);
                d.setGroupId(groupId);
                d.setVersion(newVersion);
                prj.addDependency(d);
            }
        }
    }
    
    private static class UpdateCompilerOperation implements ModelOperation<POMModel> {
        @Override
        public void performOperation(POMModel model) {
            org.netbeans.modules.maven.model.pom.Project prj = model.getProject();
            ModelUtils.updatePluginVersion(Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, "3.6.1", prj);
            ModelUtils.openAtPlugin(model, Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER);
        }
    }
}
