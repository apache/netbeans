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
package org.netbeans.modules.glassfish.javaee.templates;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.glassfish.eecommon.api.Utils;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

import org.netbeans.modules.glassfish.eecommon.api.XmlFileCreator;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;


/*
 * Adapted from SunDDWizardIterator
 * @author Vince Kraemer
 */
public final class WebLogicDDWizardIterator implements WizardDescriptor.InstantiatingIterator {
    
    private int index;
    
    private WizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels;
    
    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[] {
                new WebLogicDDWizardPanel()
            };
            String[] steps = createSteps();
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                if (steps[i] == null) {
                    // Default step name to component name of panel. Mainly
                    // useful for getting the name of the target chooser to
                    // appear in the list of steps.
                    steps[i] = c.getName();
                }
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(i)); // NOI18N
                    // Sets steps names for a panel
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
                    // Turn on subtitle creation on each step
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE); // NOI18N
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE); // NOI18N
                    // Turn on numbering of all steps
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE); // NOI18N
                }
            }
        }
        return panels;
    }
    
    @Override
    public Set instantiate() throws IOException {
        Set result = Collections.emptySet();
        WebLogicDDWizardPanel wizardPanel = (WebLogicDDWizardPanel) panels[0];
        
        File configDir = wizardPanel.getSelectedLocation();
        FileObject configFolder = FileUtil.createFolder(configDir);
        if(configFolder != null) {
            String sunDDFileName = wizardPanel.getFileName();
            Project p = wizardPanel.getProject();
            J2eeModuleProvider mod = p.getLookup().lookup(J2eeModuleProvider.class);
            if (null != mod) {
                FileObject sunDDTemplate = Utils.getSunDDFromProjectsModuleVersion(mod.getJ2eeModule(), sunDDFileName);
            if(sunDDTemplate != null) {
                FileSystem fs = configFolder.getFileSystem();
                XmlFileCreator creator = new XmlFileCreator(sunDDTemplate, configFolder, 
                        sunDDTemplate.getName(), sunDDTemplate.getExt());
                fs.runAtomicAction(creator);
                FileObject sunDDFO = creator.getResult();
                if(sunDDFO != null) {
//                    GlassfishConfiguration config =
//                            GlassfishConfiguration.getConfiguration(FileUtil.toFile(sunDDFO));
//                    if(config != null) {
//                        // Set version of target configuration file we just saved to maximum supported version.
//                        config.setAppServerVersion(config.getMaxASVersion());
//                    } else {
//                        NotifyDescriptor nd = new NotifyDescriptor.Message(
//                                NbBundle.getMessage(WebLogicDDWizardIterator.class,"ERR_NoDeploymentConfiguration"), // NOI18N
//                                NotifyDescriptor.ERROR_MESSAGE);
//                        DialogDisplayer.getDefault().notify(nd);
//                    }
                    result = Collections.singleton(creator.getResult());
                } else {
                    NotifyDescriptor nd = new NotifyDescriptor.Message(
                            NbBundle.getMessage(WebLogicDDWizardIterator.class,"ERR_FileCreationFailed", sunDDFileName), // NOI18N
                            NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                }
            }
        }
        } else {
            NotifyDescriptor nd = new NotifyDescriptor.Message(
                    NbBundle.getMessage(WebLogicDDWizardIterator.class,"ERR_LocationNotFound", configDir.getAbsolutePath()), // NOI18N
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
        return result;
    }
    
    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
    }
    
    @Override
    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }
    
    @Override
    public WizardDescriptor.Panel current() {
        return getPanels()[index];
    }
    
    @Override
    public String name() {
        return index + 1 + ". from " + getPanels().length;
    }
    
    @Override
    public boolean hasNext() {
        return index < getPanels().length - 1;
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
    
    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public void addChangeListener(ChangeListener l) {}
    @Override
    public void removeChangeListener(ChangeListener l) {}
    
    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then uncomment
    // the following and call when needed: fireChangeEvent();
    /*
    private Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }
     */
    
    // You could safely ignore this method. Is is here to keep steps which were
    // there before this wizard was instantiated. It should be better handled
    // by NetBeans Wizard API itself rather than needed to be implemented by a
    // client code.
    private String[] createSteps() {
        String[] beforeSteps = null;
        Object prop = wizard.getProperty(WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }
        
        if (beforeSteps == null) {
            beforeSteps = new String[0];
        }
        
        String[] res = new String[(beforeSteps.length - 1) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (beforeSteps.length - 1)) {
                res[i] = beforeSteps[i];
            } else {
                res[i] = panels[i - beforeSteps.length + 1].getComponent().getName();
            }
        }
        return res;
    }
}
