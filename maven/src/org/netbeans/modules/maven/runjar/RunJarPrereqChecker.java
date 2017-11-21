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
package org.netbeans.modules.maven.runjar;

import java.awt.Dialog;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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

    private String mainClass;

    @Override public boolean checkRunConfig(RunConfig config) {
        String actionName = config.getActionName();
        for (Map.Entry<? extends String,? extends String> entry : config.getProperties().entrySet()) {
            if ("exec.executable".equals(entry.getKey())) { //NOI18N
                // check for "java" and replace it with absolute path to
                // project j2seplaform's java.exe
                if ("java".equals(entry.getValue())) { //NOI18N
                    //TODO somehow use the config.getMavenProject() call rather than looking up the
                    // ActiveJ2SEPlatformProvider from lookup. The loaded project can be different from the executed one.
                    ActiveJ2SEPlatformProvider plat = config.getProject().getLookup().lookup(ActiveJ2SEPlatformProvider.class);
                    assert plat != null;
                    FileObject fo = plat.getJavaPlatform().findTool(entry.getValue());
                    if (fo != null) {
                        File fl = FileUtil.toFile(fo);
                        config.setProperty("exec.executable", fl.getAbsolutePath()); //NOI18N
                    }
                }
            }
        }

        if ((ActionProvider.COMMAND_RUN.equals(actionName) ||
                ActionProvider.COMMAND_DEBUG.equals(actionName) ||
                ActionProvider.COMMAND_PROFILE.equals(actionName))) {
            String mc = null;
            for (Map.Entry<? extends String,? extends String> entry : config.getProperties().entrySet()) {
                if (entry.getValue().contains("${packageClassName}")) { //NOI18N
                    //show dialog to choose main class.
                    if (mc == null) {
                        mc = eventuallyShowDialog(config.getProject(), actionName);
                    }
                    if (mc == null) {
                        return false;
                    }
                    config.setProperty(entry.getKey(), entry.getValue().replace("${packageClassName}", mc)); // NOI18N
                }
            }
        }
        return true;
    }

    @Messages({
        "LBL_ChooseMainClass_Title=Select Main Class for Execution",
        "LBL_ChooseMainClass_OK=Select Main Class"
    })
    private String eventuallyShowDialog(Project project, String actionName) {
        if (mainClass != null) {
            return mainClass;
        }
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
