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

package org.netbeans.modules.gradle.spi.newproject;

import org.netbeans.modules.gradle.newproject.ProjectAttriburesPanel;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.gradle.api.GradleProjects;
import org.netbeans.modules.gradle.newproject.GradleProjectFromTemplateHandler;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Laszlo Kishalmi
 */
@NbBundle.Messages({
    "TITLE_CreatingNewProject=Creating new project"
})
public abstract class BaseGradleWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor> {

    ArrayList<? extends WizardDescriptor.Panel<WizardDescriptor>> panels;

    private int index;
    private transient WizardDescriptor data;

    public static final String PROP_INIT_WRAPPER = "initWrapper"; //NOI18N
    public static final String PROP_NAME = "name";                //NOI18N
    public static final String PROP_PACKAGE_BASE = "packageBase"; //NOI18N
    public static final String PROP_GROUP = "group";              //NOI18N
    public static final String PROP_VERSION = "version";          //NOI18N
    public static final String PROP_DESCRIPTION = "description";  //NOI18N

    @Override
    @SuppressWarnings("rawtypes")
    public final Set instantiate() throws IOException {
        assert false : "Cannot call this method if implements WizardDescriptor.ProgressInstantiatingIterator."; //NOI18N
        return null;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public final Set instantiate(ProgressHandle handle) throws IOException {
        TemplateOperation ops = new TemplateOperation(handle);
        HashMap<String, Object> params = new HashMap<>(getData().getProperties());

        collectOperations(ops, params);

        ops.run();

        return ops.getImportantFiles();
    }

    @Override
    public final void initialize(WizardDescriptor wizard) {
        index = 0;
        wizard.putProperty ("NewProjectWizard_Title", getTitle()); // NOI18N

        data = initData(wizard);

        panels = new ArrayList<>(createPanels());
        String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            Component c = panels.get(i).getComponent();
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", i); //NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); //NOI18N
            }
        }
    }

    protected WizardDescriptor initData(WizardDescriptor data) {
        return data;
    }

    protected abstract List<? extends WizardDescriptor.Panel<WizardDescriptor>> createPanels();
    protected abstract String getTitle();
    protected abstract void collectOperations(final TemplateOperation ops, final Map<String, Object> params);

    static {
        // register collectOperations for a callback from a friend package
        GradleProjectFromTemplateHandler.register((baseIterator, params) -> {
            ProgressHandle handle = ProgressHandle.createHandle(Bundle.TITLE_CreatingNewProject());
            TemplateOperation ops = new TemplateOperation(handle);
            baseIterator.collectOperations(ops, params);
            return ops;
        });
    }

    protected final WizardDescriptor getData() {
        return data;
    }

    @Override
    public final void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }

    protected final File assumedRoot() {
        File ret = null;

        File loc = data == null ? null : (File) data.getProperty(CommonProjectActions.PROJECT_PARENT_FOLDER);
        if (loc != null) {
            //TODO: Better evaluate possible roots.
            ret = GradleProjects.testForRootProject(loc) ? loc : null;
        }
        return ret;
    }

    @Override
    public String name() {
        return "Some name";
    }

    @Override
    public final boolean hasNext() {
        return index < panels.size() - 1;
    }

    @Override
    public final boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public final void nextPanel() {
        index++;
    }

    @Override
    public final void previousPanel() {
        index--;
    }

    @Override
    public final WizardDescriptor.Panel<WizardDescriptor> current() {
        return panels.get(index);
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    protected static final WizardDescriptor.Panel<WizardDescriptor> createProjectAttributesPanel(WizardDescriptor.Panel<WizardDescriptor> bottom) {
        return new ProjectAttriburesPanel(bottom);
    }
}
