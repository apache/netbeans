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
package org.netbeans.modules.javafx2.project;

import java.awt.Dialog;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.scripting.Scripting;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.j2seproject.api.J2SEBuildPropertiesProvider;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.modules.javafx2.project.ui.JFXApplicationClassChooser;
import org.netbeans.modules.javafx2.project.ui.JFXRunPanel;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.MouseUtils;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 * JFX Action Provider
 */
@ProjectServiceProvider(
    service=ActionProvider.class,
    projectTypes={@LookupProvider.Registration.ProjectType(id="org-netbeans-modules-java-j2seproject",position=90)})
public class JFXActionProvider implements ActionProvider {

    private static final Logger LOG = Logger.getLogger(JFXActionProvider.class.getName());

    private final Project prj;
    private boolean isJSAvailable = true;
    private boolean isJSAvailableChecked = false;

    private static final Map<String,String> ACTIONS = new HashMap<String,String>(){
        {
            put(COMMAND_BUILD,"jfx-build"); //NOI18N
            put(COMMAND_REBUILD,"jfx-rebuild"); //NOI18N
            put(COMMAND_RUN,"run"); //NOI18N
            put(COMMAND_DEBUG,"debug"); //NOI18N
            put(COMMAND_PROFILE,"profile"); //NOI18N
        }
    };

    private static final Map<String,String> RUN_ACTIONS = new HashMap<String,String>(){
        {
            put(COMMAND_RUN,"run"); //NOI18N
            put(COMMAND_DEBUG,"debug"); //NOI18N
            put(COMMAND_PROFILE,"profile"); //NOI18N
        }
    };
    
    public JFXActionProvider(@NonNull final Project project) {
        Parameters.notNull("project", project); // NOI18N
        this.prj = project;
    }

    @Override
    @NonNull
    public String[] getSupportedActions() {
        return ACTIONS.keySet().toArray(new String[ACTIONS.size()]);
    }

    @Override
    public void invokeAction(@NonNull String command, @NonNull Lookup context) throws IllegalArgumentException {
        if (command != null) {
            if(RUN_ACTIONS.containsKey(command) && JFXProjectUtils.isFXPreloaderProject(prj)) {
                NotifyDescriptor d =
                    new NotifyDescriptor.Message(NbBundle.getMessage(JFXActionProvider.class,"WARN_PreloaderExecutionUnsupported", // NOI18N
                        ProjectUtils.getInformation(prj).getDisplayName()), NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                return;
            }
            FileObject buildFo = findBuildXml();
            assert buildFo != null && buildFo.isValid();
            String noScript = isJavaScriptAvailable() ? "" : "-noscript"; // NOI18N
            String runAs = JFXProjectUtils.getFXProjectRunAs(prj);
            if(runAs == null) {
                runAs = JFXProjectProperties.RunAsType.STANDALONE.getString();
            }
            
            Properties props = verifyApplicationClass();
            if(props != null) {
                final ActionProgress listener = ActionProgress.start(context);
                try {
                    List<String> targets;
                    final Map<String,List<String>> targetReplacements = loadTargetsFromConfig(prj);
                    targets = targetReplacements.get(command);
                    if (targets == null) {
                        if(command.equalsIgnoreCase(COMMAND_BUILD) || command.equalsIgnoreCase(COMMAND_REBUILD)) {
                            targets = Collections.singletonList(ACTIONS.get(command).concat(noScript)); // NOI18N
                        } else {
                            if(runAs.equalsIgnoreCase(JFXProjectProperties.RunAsType.STANDALONE.getString())) {
                                targets = Collections.singletonList("jfxsa-".concat(ACTIONS.get(command)).concat(noScript)); //NOI18N
                            } else {
                                if(runAs.equalsIgnoreCase(JFXProjectProperties.RunAsType.ASWEBSTART.getString())) {
                                    targets = Collections.singletonList("jfxws-".concat(ACTIONS.get(command)).concat(noScript)); //NOI18N
                                } else { //JFXProjectProperties.RunAsType.INBROWSER
                                    targets = Collections.singletonList("jfxbe-".concat(ACTIONS.get(command)).concat(noScript)); //NOI18N
                                }
                            }
                        }
                    }

                    collectStartupExtenderArgs(props, command, context);
                    final Set<String> concealedProperties = collectAdditionalBuildProperties(props, command, context);
                    ActionUtils.runTarget(buildFo, targets.toArray(new String[0]), props, concealedProperties)
                               .addTaskListener(task -> listener.finished(((ExecutorTask) task).result() == 0));
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    listener.finished(false);
                }
            }
        } else {
            throw new IllegalArgumentException(command);
        }
    }

    @Override
    public boolean isActionEnabled(@NonNull String command, @NonNull Lookup context) throws IllegalArgumentException {
        if (isFXProject(prj)) {
            if (findBuildXml() == null) {
                return false;
            }
            return findTarget(command) != null;
        }
        return false;
    }

    @NonNull
    private static String getBuildXmlName (@NonNull final PropertyEvaluator evaluator) {
        String buildScriptPath = evaluator.getProperty("buildfile");    //NOI18N
        if (buildScriptPath == null) {
            buildScriptPath = GeneratedFilesHelper.BUILD_XML_PATH;
        }
        return buildScriptPath;
    }

    @NonNull
    private static HashMap<String,List<String>> loadTargetsFromConfig(
            @NonNull final Project project) {
        HashMap<String,List<String>> targets = new HashMap<>(6);
        final J2SEPropertyEvaluator ep = project.getLookup().lookup(J2SEPropertyEvaluator.class);
        final PropertyEvaluator evaluator = ep.evaluator();
        String config = evaluator.getProperty(ProjectProperties.PROP_PROJECT_CONFIGURATION_CONFIG);
        // load targets from shared config
        FileObject propFO = project.getProjectDirectory().getFileObject("nbproject/configs/" + config + ".properties");
        if (propFO == null) {
            return targets;
        }
        Properties props = new Properties();
        try (final InputStream is = propFO.getInputStream()) {
            props.load(is);
        } catch (IOException ex) {
            LOG.warning(ex.getMessage());
            return targets;
        }
        Enumeration<?> propNames = props.propertyNames();
        while (propNames.hasMoreElements()) {
            String propName = (String) propNames.nextElement();
            if (propName.startsWith("$target.")) {
                String tNameVal = props.getProperty(propName);
                if (tNameVal != null && !tNameVal.equals("")) {
                    String cmdNameKey = propName.substring("$target.".length());
                    StringTokenizer stok = new StringTokenizer(tNameVal.trim(), " ");
                    List<String> targetNames = new ArrayList<String>(3);
                    while (stok.hasMoreTokens()) {
                        targetNames.add(stok.nextToken());
                    }
                    targets.put(
                        cmdNameKey,
                        targetNames.isEmpty() ? null : targetNames);
                }
            }
        }
        return targets;
    }

    @CheckForNull
    private FileObject findBuildXml () {
        final J2SEPropertyEvaluator ep = prj.getLookup().lookup(J2SEPropertyEvaluator.class);
        assert ep != null;
        return prj.getProjectDirectory().getFileObject (getBuildXmlName(ep.evaluator()));
    }

    @CheckForNull
    private static String findTarget(@NonNull final String command) {
        return ACTIONS.get(command);
    }
    
    private void collectStartupExtenderArgs(Map<? super String,? super String> p, String command, Lookup context) {
        StringBuilder b = new StringBuilder();
        for (String arg : runJvmargsIde(command, context)) {
            b.append(' ').append(arg); // NOI18N
        }
        if (b.length() > 0) {
            p.put("run.jvmargs.ide", b.toString()); // NOI18N
        }
    }

    @NonNull
    private Set<String> collectAdditionalBuildProperties(
        @NonNull final Map<? super String, ? super String> p,
        @NonNull final String command,
        @NonNull final Lookup context) {
        final Set<String> concealedProperties = new HashSet<>();
        for (J2SEBuildPropertiesProvider pp : prj.getLookup().lookupAll(J2SEBuildPropertiesProvider.class)) {
            final Map<String,String> contrib = pp.createAdditionalProperties(command, context);
            assert contrib != null;
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(
                    Level.FINE,
                    "J2SEBuildPropertiesProvider: {0} added following build properties: {1}",   //NOI18N
                    new Object[]{
                        pp.getClass(),
                        contrib
                    });
            }
            p.putAll(contrib);
            final Set<String> concealedContrib = pp.createConcealedProperties(command, context);
            assert concealedContrib != null;
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(
                    Level.FINE,
                    "J2SEBuildPropertiesProvider: {0} added following concealed properties: {1}",   //NOI18N
                    new Object[]{
                        pp.getClass(),
                        concealedContrib
                    });
            }
            concealedProperties.addAll(concealedContrib);
        }
        return Collections.unmodifiableSet(concealedProperties);
    }
    
    private List<String> runJvmargsIde(String command, Lookup context) {
        StartupExtender.StartMode mode;
        if (command.equals(COMMAND_RUN) || command.equals(COMMAND_RUN_SINGLE)) {
            mode = StartupExtender.StartMode.NORMAL;
        } else if (command.equals(COMMAND_DEBUG) || command.equals(COMMAND_DEBUG_SINGLE) || command.equals(COMMAND_DEBUG_STEP_INTO)) {
            mode = StartupExtender.StartMode.DEBUG;
        } else if (command.equals(COMMAND_PROFILE) || command.equals(COMMAND_PROFILE_SINGLE)) {
            mode = StartupExtender.StartMode.PROFILE;
        } else if (command.equals(COMMAND_TEST) || command.equals(COMMAND_TEST_SINGLE)) {
            mode = StartupExtender.StartMode.TEST_NORMAL;
        } else if (command.equals(COMMAND_DEBUG_TEST_SINGLE)) {
            mode = StartupExtender.StartMode.TEST_DEBUG;
        } else if (command.equals(COMMAND_PROFILE_TEST_SINGLE)) {
            mode = StartupExtender.StartMode.TEST_PROFILE;
        } else {
            return Collections.emptyList();
        }
        List<String> args = new ArrayList<String>();

        for (StartupExtender group : StartupExtender.getExtenders(context, mode)) {
            args.addAll(group.getArguments());
        }
        return args;
    }
    
    private boolean isJavaScriptAvailable() {
        if(isJSAvailableChecked) {
            return isJSAvailable;
        }
        ScriptEngineManager mgr = Scripting.createManager();
        List<ScriptEngineFactory> factories = mgr.getEngineFactories();
        for (ScriptEngineFactory factory: factories) {
            List<String> engNames = factory.getNames();
            for(String name: engNames) {
                if(name.equalsIgnoreCase("js") || name.equalsIgnoreCase("javascript")) { //NOI18N
                    isJSAvailableChecked = true;
                    isJSAvailable = true;
                    return isJSAvailable;
                }
            }
        }
        isJSAvailableChecked = true;
        isJSAvailable = false;
        return isJSAvailable;
    }
    
    /**
     * Verify that the currently selected Application class exists. If not,
     * offer a chooser dialog to select among existing ones. If no valid choice
     * is made, return false. Otherwise, continue with the current choice.
     * @return true if current Application class is valid, false otherwise
     */
    private Properties verifyApplicationClass() {
        final JButton okButton = new JButton (NbBundle.getMessage (JFXRunPanel.class, "LBL_ChooseMainClass_OK")); // NOI18N
        okButton.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage (JFXRunPanel.class, "AD_ChooseMainClass_OK"));  // NOI18N
        final boolean FXinSwing = JFXProjectUtils.isFXinSwingProject(prj);
        final Collection<? extends FileObject> roots = JFXProjectUtils.getClassPathMap(prj).keySet();
        final Set<String> appClassNames;
        if (FXinSwing) {
            appClassNames = JFXProjectUtils.getMainClassNames(prj);
        } else {
            final AtomicBoolean cancel = new AtomicBoolean();
            final AtomicReference<Set<String>> result = new AtomicReference<>(Collections.<String>emptySet());
            try {
            BaseProgressUtils.runOffEventDispatchThread(new Runnable() {
                @Override
                public void run() {
                    if (!cancel.get()) {
                        final Set<String> appClasses = JFXProjectUtils.getAppClassNames(roots, "javafx.application.Application"); //NOI18N
                        result.set(appClasses);
                    }
                }
            }, NbBundle.getMessage(JFXActionProvider.class, "TXT_ActionInProgress"), cancel, true); //NOI18N
            } catch (IllegalStateException ex) {
                LOG.log(Level.INFO, "Canceled operation did not finish in time.", ex); //NOI18N
            }
            appClassNames = result.get();
        }
        final PropertyEvaluator eval = prj.getLookup().lookup(J2SEPropertyEvaluator.class).evaluator();
        
        String appClassName = eval.getProperty(FXinSwing ? ProjectProperties.MAIN_CLASS : JFXProjectProperties.MAIN_CLASS);
        Properties props = new Properties();
        if (!JFXProjectUtils.isFXPreloaderProject(prj) && (appClassName == null || !appClassNames.contains(appClassName))) {
            final JFXApplicationClassChooser panel = new JFXApplicationClassChooser(prj, eval);
            Object[] options = new Object[] {
                okButton,
                DialogDescriptor.CANCEL_OPTION
            };
            panel.addChangeListener (new ChangeListener () {
               @Override
               public void stateChanged(ChangeEvent e) {
                   if (e.getSource () instanceof MouseEvent && MouseUtils.isDoubleClick (((MouseEvent)e.getSource ()))) {
                       // click button and finish the dialog with selected class
                       okButton.doClick ();
                   } else {
                       okButton.setEnabled (panel.getSelectedClass () != null);
                   }
               }
            });
            okButton.setEnabled (false);
            DialogDescriptor desc = new DialogDescriptor (
                panel,
                NbBundle.getMessage (JFXRunPanel.class, FXinSwing ? "LBL_ChooseMainClass_Title_Swing" : "LBL_ChooseMainClass_Title" ),  // NOI18N
                true, 
                options, 
                options[0], 
                DialogDescriptor.BOTTOM_ALIGN, 
                null, 
                null);
            //desc.setMessageType (DialogDescriptor.INFORMATION_MESSAGE);
            Dialog dlg = DialogDisplayer.getDefault ().createDialog (desc);
            dlg.setVisible (true);
            if (desc.getValue() == options[0]) {
                try {
                    JFXProjectUtils.updatePropertyInActiveConfig(prj, FXinSwing ? ProjectProperties.MAIN_CLASS : JFXProjectProperties.MAIN_CLASS, panel.getSelectedClass());
                    props.setProperty(FXinSwing ? ProjectProperties.MAIN_CLASS : JFXProjectProperties.MAIN_CLASS, panel.getSelectedClass() );
                } catch(IOException e) {
                    props = null;
                }
            } else {
                props = null;
            }
            dlg.dispose();
        }
        return props;
    }

    private static boolean isFXProject(@NonNull final Project prj) {
        final J2SEPropertyEvaluator eval = prj.getLookup().lookup(J2SEPropertyEvaluator.class);
        if (eval == null) {
            return false;
        }
        //Don't use JFXProjectProperties.isTrue to prevent JFXProjectProperties from being loaded
        //JFXProjectProperties.JAVAFX_ENABLED is inlined by compliler
        return isTrue(eval.evaluator().getProperty(JFXProjectProperties.JAVAFX_ENABLED));
    }

    private static boolean isTrue(@NullAllowed final String value) {
        return  value != null && (
           "true".equalsIgnoreCase(value) ||    //NOI18N
           "yes".equalsIgnoreCase(value) ||     //NOI18N
           "on".equalsIgnoreCase(value));       //NOI18N
    }
}
