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
package org.netbeans.modules.java.disco;

import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.checkerframework.checker.guieffect.qual.UIEffect;
import org.netbeans.api.java.platform.JavaPlatform;
import org.openide.WizardDescriptor;

@SuppressWarnings("initialization")
public final class FoojayPlatformIt implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {

    public static final String PROP_DOWNLOAD = "download"; //NOI18N
    public static final String PROP_DOWNLOAD_FOLDER = "downloadFolder"; //NOI18N
    
    private int index;

    private List<WizardDescriptor.Panel<WizardDescriptor>> panels;
    private WizardDescriptor wizard;
    private WizardState state = new WizardState();
    private String[] names;
    private final List<ChangeListener> listeners = new ArrayList<>();

    @SuppressWarnings("call.invalid.ui") //TODO: Remove this and fix the underlying warning
    private List<WizardDescriptor.Panel<WizardDescriptor>> getPanels() {
        if (panels == null) {
            panels = new ArrayList<>();
            panels.add(new SelectPackageWizardPanel(state));
            panels.add(new BrowseWizardPanel(state));
            panels.add(new DownloadWizardPanel(state));
            String[] steps = new String[panels.size()];
            for (int i = 0; i < panels.size(); i++) {
                Component c = panels.get(i).getComponent();
                // Default step name to component name of panel.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
                }
            }
        }
        return panels;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return getPanels().get(index);
    }

    @Override
    public String name() {
        return index + 1 + ". from " + getPanels().size();
    }

    @Override
    public boolean hasNext() {
        return index < getPanels().size() - 1;
    }

    @Override
    public boolean hasPrevious() {
        //can't go back from the download panel
        //TODO: this API does not work since TemplateWizardIterImpl.hasPrevious never delegates
        return index == 1;
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
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    @UIEffect
    protected void fireChangeListeners() {
        ChangeEvent ce = new ChangeEvent(this);
        for(ChangeListener l : listeners) {
            l.stateChanged(ce);
        }
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        getPanels();
    }

    @Override
    public Set<JavaPlatform> instantiate() throws IOException {
        //TODO: Download (in background?)
        String downloadedFolder = (String) wizard.getProperty(FoojayPlatformIt.PROP_DOWNLOAD);
        if (downloadedFolder != null) {
            File f = new File(downloadedFolder);
            if (!f.isDirectory()) {
                //open the file manager for the parent folder
                Desktop.getDesktop().open(f.getParentFile());
                return Collections.EMPTY_SET;
            }

            String name = state.selection.getJavaPlatformDisplayName();
            return Collections.singleton(J2SEPlatformUtils.register(new File(downloadedFolder), name));
        } else {
            //TODO: notifcation?
            return Collections.EMPTY_SET;
        }
    }
    
    @Override
    public void uninitialize(WizardDescriptor wd) {
        //TODO: cancel download here?
    }

}
