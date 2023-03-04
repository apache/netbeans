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

package org.netbeans.modules.maven.newproject;

import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.maven.api.archetype.ArchetypeWizards;
import static org.netbeans.modules.maven.newproject.Bundle.*;
import org.openide.WizardDescriptor;
import org.openide.awt.Actions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 *
 *@author mkleint
 */
@TemplateRegistration(folder=ArchetypeWizards.TEMPLATE_FOLDER, position=1000, displayName="#template.existing", iconBase="org/netbeans/modules/maven/resources/Maven2Icon.gif", description="ExistingDescription.html")
@Messages("template.existing=Project with Existing POM")
public class ExistingWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {
    
    private static final long serialVersionUID = 1L;
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    
    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new UseOpenWizardPanel()
        };
    }
    
    @Messages("LBL_UseOpenStep=Existing Project")
    private String[] createSteps() {
        return new String[] {
            LBL_UseOpenStep(),
        };
    }
    
    @Override
    public Set/*<FileObject>*/ instantiate() throws IOException {
        assert false : "Cannot call this method if implements WizardDescriptor.ProgressInstantiatingIterator."; //NOI18N
        return null;
    }
    
    @Override
    public Set instantiate(ProgressHandle handle) throws IOException {
        try {
            handle.start(2);
            handle.progress(1);
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    tryOpenProject();
                }
            });
            return Collections.EMPTY_SET;
        } finally {
            handle.finish();
            
        }
    }
    
    private void tryOpenProject() {
        final Action act = Actions.forID("Project", "org.netbeans.modules.project.ui.OpenProject"); //NOI18N
        if (act != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    act.actionPerformed(null);
                }
            });
        }
    }
    
    @Override
    public void initialize(WizardDescriptor wiz) {
        index = 0;
        wiz.putProperty ("NewProjectWizard_Title", template_existing()); // NOI18N
        
        panels = createPanels();
        // Make sure list of steps is accurate.
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
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); //NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); //NOI18N
            }
        }
    }
    
    @Override
    public void uninitialize(WizardDescriptor wiz) {
        panels = null;
    }
    
    @Messages({"# {0} - index", "# {1} - length", "MSG_One_of_Many={0} of {1}"})
    @Override
    public String name() {
        return MSG_One_of_Many(index + 1, panels.length);
    }
    
    @Override
    public boolean hasNext() {
        return false;
    }
    
    @Override
    public boolean hasPrevious() {
        return false;
    }
    
    @Override
    public void nextPanel() {
    }
    
    @Override
    public void previousPanel() {
    }
    
    @Override
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public final void addChangeListener(ChangeListener l) {}
    @Override
    public final void removeChangeListener(ChangeListener l) {}
    
}
