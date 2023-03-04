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
package org.netbeans.modules.maven.runjar;

import java.awt.Dialog;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.api.execute.ActiveJ2SEPlatformProvider;
import org.netbeans.modules.maven.api.execute.PrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.classpath.MavenSourcesImpl;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import static org.netbeans.modules.maven.runjar.Bundle.*;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.MouseUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

/**
 * @author mkleint
 */
@ProjectServiceProvider(service=PrerequisitesChecker.class, projectType="org-netbeans-modules-maven/" + NbMavenProject.TYPE_JAR)
public class RunJarPrereqChecker implements PrerequisitesChecker {

    private static final String[] MAIN_CLASS_PROPERTIES = {"mainClass", "exec.mainClass", "project.mainClass", "project.mainclass"}; // NOI18N

    private String mainClass;
    
    private static String testedMainClass;
    
    /**
     * For testing purposes only
     * @param mainClass 
     */
    public static void setMainClass(String mainClass) {
        testedMainClass = mainClass;
    }

    @Override public boolean checkRunConfig(RunConfig config) {
        String actionName = config.getActionName();

        // check for "java" and replace it with absolute path to
        // project j2seplaform's java.exe
        String tool = config.getProperties().get("exec.executable"); //NOI18N
        if ("java".equals(tool)) { //NOI18N
            //TODO somehow use the config.getMavenProject() call rather than looking up the
            // ActiveJ2SEPlatformProvider from lookup. The loaded project can be different from the executed one.
            ActiveJ2SEPlatformProvider plat = config.getProject().getLookup().lookup(ActiveJ2SEPlatformProvider.class);
            assert plat != null;
            FileObject fo = plat.getJavaPlatform().findTool(tool);
            if (fo != null) {
                File fl = FileUtil.toFile(fo);
                config.setProperty("exec.executable", fl.getAbsolutePath()); //NOI18N
            }
        }

        if ((ActionProvider.COMMAND_RUN.equals(actionName) ||
                ActionProvider.COMMAND_DEBUG.equals(actionName) ||
                ActionProvider.COMMAND_PROFILE.equals(actionName))) {
            String mc = findMainClass(config);
            for (Map.Entry<? extends String,? extends String> entry : config.getProperties().entrySet()) {
                if (entry.getValue().contains("${packageClassName}")) { //NOI18N
                    //show dialog to choose main class.
                    if (mc == null) {
                        if (mainClass != null) {
                            mc = mainClass;
                        } else if (testedMainClass != null) {
                            mainClass = testedMainClass;
                            mc = mainClass;
                        } else if (!GraphicsEnvironment.isHeadless()) {
                            mc = showMainClassDialog(config.getProject(), actionName);
                            if (mc == null) {
                                return false;
                            }
                        }
                    }
                    if (mc != null) {
                        config.setProperty(entry.getKey(), entry.getValue().replace("${packageClassName}", mc)); // NOI18N
                        // send a note to RunJarStartupArgs
                        config.setProperty(MavenExecuteUtils.RUN_MAIN_CLASS, mc); // NOI18N
                    }
                }
            }
        }
        return true;
    }

    private static String findMainClass(RunConfig config) {
        // Read main class from the manifest property:
        String mainClass = getConfiguration(config, "maven-jar-plugin", "archive", "manifest", "mainClass"); // NOI18N
        if (mainClass != null) {
            return mainClass;
        }
        // Read main class from exec-maven-plugin configuration:
        mainClass = getConfiguration(config, "exec-maven-plugin", "mainClass"); // NOI18N
        if (mainClass != null) {
            return mainClass;
        }
        // Check pom's properties:
        Properties properties = config.getMavenProject().getProperties();
        for (String name : MAIN_CLASS_PROPERTIES) {
            String mc = properties.getProperty(name);
            if (mc != null) {
                return mc;
            }
        }
        return null;
    }

    private static Plugin findPlugin(RunConfig config, String name) {
        List<Plugin> plugins = config.getMavenProject().getBuild().getPlugins();
        for (Plugin p : plugins) {
            if (name.equals(p.getArtifactId())) {
                return p;
            }
        }
        return null;
    }

    private static String getConfiguration(RunConfig config, String pluginId, String... configs) {
        Plugin plugin = findPlugin(config, pluginId);
        if (plugin != null) {
            Xpp3Dom configuration = (Xpp3Dom) plugin.getConfiguration();
            if (configuration != null) {
                Xpp3Dom child = configuration;
                for (String c : configs) {
                    child = child.getChild(c);
                    if (child == null) {
                        break;
                    }
                }
                if (child != null) {
                    return child.getValue();
                }
            }
        }
        return null;
    }

    @Messages({
        "LBL_ChooseMainClass_Title=Select Main Class for Execution",
        "LBL_ChooseMainClass_OK=Select Main Class"
    })
    private String showMainClassDialog(Project project, String actionName) {
        List<FileObject> roots = new ArrayList<FileObject>();
        Sources srcs = ProjectUtils.getSources(project);
        for (SourceGroup sourceGroup : srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            if (MavenSourcesImpl.NAME_SOURCE.equals(sourceGroup.getName())) {
                roots.add(sourceGroup.getRootFolder());
            }
        }
        for (SourceGroup sourceGroup : srcs.getSourceGroups(MavenSourcesImpl.TYPE_GEN_SOURCES)) {
            roots.add(sourceGroup.getRootFolder());
        }
        final JButton okButton = new JButton(LBL_ChooseMainClass_OK());
        final MainClassChooser panel = new MainClassChooser(roots.toArray(new FileObject[0]));
        Object[] options = new Object[]{
            okButton,
            DialogDescriptor.CANCEL_OPTION
        };
        panel.addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                if (e.getSource() instanceof MouseEvent && MouseUtils.isDoubleClick(((MouseEvent) e.getSource()))) {
                    // click button and finish the dialog with selected class
                    okButton.doClick();
                } else {
                    okButton.setEnabled(panel.getSelectedMainClass() != null);
                }
            }
        });
        panel.rbSession.setSelected(true);
        okButton.setEnabled(false);
        DialogDescriptor desc = new DialogDescriptor(
                panel,
                LBL_ChooseMainClass_Title(),
                true,
                options,
                options[0],
                DialogDescriptor.BOTTOM_ALIGN,
                null,
                null);
        //desc.setMessageType (DialogDescriptor.INFORMATION_MESSAGE);
        Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
        dlg.setVisible(true);
        if (okButton == desc.getValue()) {
            if (panel.rbSession.isSelected()) {
                mainClass = panel.getSelectedMainClass();
            } else if (panel.rbPermanent.isSelected()) {
                writeMapping(actionName, project, panel.getSelectedMainClass());
            }
            return panel.getSelectedMainClass();
        }
        return null;
    }

    static void writeMapping(String actionName, Project project, String clazz) {
        try {
            M2ConfigProvider usr = project.getLookup().lookup(M2ConfigProvider.class);
            NetbeansActionMapping mapp = ModelHandle2.getMapping(actionName, project, usr.getActiveConfiguration());
            if (mapp == null) {
                mapp = ModelHandle2.getDefaultMapping(actionName, project);
            }
            // XXX should this rather run on _all_ actions that reference ${packageClassName}?
            for (Map.Entry<String,String> e : mapp.getProperties().entrySet()) {
                String val = e.getValue();
                if (val.contains("${packageClassName}")) { //NOI18N
                    //show dialog to choose main class.
                    e.setValue(val.replace("${packageClassName}", clazz)); // NOI18N
                }
            }
            //TODO we should definitely write to the mappings of active configuration here..
            ModelHandle2.putMapping(mapp, project, usr.getActiveConfiguration());
        } catch (Exception e) {
            Exceptions.attachMessage(e, "Cannot persist action configuration.");
            Exceptions.printStackTrace(e);
        }
    }
}
