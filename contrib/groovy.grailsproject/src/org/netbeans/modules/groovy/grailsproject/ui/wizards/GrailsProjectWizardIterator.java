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
package org.netbeans.modules.groovy.grailsproject.ui.wizards;

import org.netbeans.modules.groovy.grailsproject.ui.wizards.impl.ProgressLineProcessor;
import org.netbeans.modules.groovy.grailsproject.ui.wizards.impl.PanelConfigureProject;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionDescriptor.InputProcessorFactory;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.groovy.grails.api.ExecutionSupport;
import org.netbeans.modules.groovy.grailsproject.GrailsProjectSettings;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author schmidtm
 */
public class GrailsProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {

    private static final Logger LOGGER = Logger.getLogger(GrailsProjectWizardIterator.class.getName());

    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    private PanelConfigureProject pls;
    private int baseCount;


    private WizardDescriptor.Panel[] createPanels() {
        pls = new PanelConfigureProject();
        return new WizardDescriptor.Panel[]{pls};
    }

    private String[] createSteps() {
        return new String[] {NbBundle.getMessage(GrailsProjectWizardIterator.class, "LAB_ConfigureProject")};
    }

    @Override
    public Set instantiate(final ProgressHandle handle) throws IOException {
        Set<FileObject> resultSet = new HashSet<FileObject>();

        handle.start(100);
        try {
            String displayName = "<new project> (create-app)"; // NOI18N
            Callable<Process> callable = ExecutionSupport.getInstance().createCreateApp(
                    (File) wiz.getProperty("projectFolder")); // NOI18N

            // we need a special descriptor here
            ExecutionDescriptor descriptor = new ExecutionDescriptor().frontWindow(true).inputVisible(true);
            descriptor = descriptor.outProcessorFactory(new InputProcessorFactory() {

                @Override
                public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                    return InputProcessors.proxy(defaultProcessor, InputProcessors.bridge(new ProgressLineProcessor(handle, 100, 2)));
                }
            });
            // TODO refresh

            ExecutionService service = ExecutionService.newService(callable, descriptor, displayName);
            Future<Integer> future = service.run();
            try {
                Integer ret = future.get();
                if (ret.intValue() != 0) {
                    String msg = NbBundle.getMessage(GrailsProjectWizardIterator.class, "WIZARD_ERROR_MESSAGE_APPLICATION");
                    DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE));
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex.getCause());
            }
        } finally {
            handle.progress(100);
            handle.finish();
        }

        File dirF = (File) wiz.getProperty("projectFolder");

        if (dirF != null) {
            dirF = FileUtil.normalizeFile(dirF);
            FileObject dir = FileUtil.toFileObject(dirF);

            if (dir == null) {
                LOGGER.log(Level.WARNING, "Folder was expected, but not found: {0}", dirF.getCanonicalPath());
            } else {
                resultSet.add(dir);
                GrailsProjectSettings.getDefault().setNewProjectCount(baseCount);

                File parentDir = dirF.getParentFile();

                if (parentDir != null && parentDir.exists() && parentDir.isDirectory()) {
                    GrailsProjectSettings.getDefault().setLastUsedArtifactFolder(dirF.getParentFile());
                }
            }
            dirF = dirF.getParentFile();
            if (dirF != null && dirF.exists()) {
                ProjectChooser.setProjectsFolder(dirF);
            }
        }

        return resultSet;
    }

    @Override
    public Set instantiate() throws IOException {
        Set<FileObject> resultSet = new HashSet<FileObject>();
        return resultSet;
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wiz = wizard;
        index = 0;

        // get project counter from GrailsConfiguration
        baseCount = GrailsProjectSettings.getDefault().getNewProjectCount() + 1;
        wizard.putProperty("WizardPanel_GrailsProjectCounter", Integer.valueOf(baseCount));

        panels = createPanels();
        String[] steps = createSteps();

        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            }
        }
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
    }

    @Override
    public Panel current() {
        return panels[index];
    }

    @Override
    public String name() {
        return NbBundle.getMessage(GrailsProjectWizardIterator.class, "LAB_IteratorName", Integer.valueOf(index + 1), Integer.valueOf(panels.length));
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }
}
