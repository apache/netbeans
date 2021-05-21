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
package org.netbeans.modules.maven.j2ee.ui.wizard;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.validation.adapters.WizardDescriptorAdapter;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettings;
import org.netbeans.modules.javaee.project.api.ui.UserProjectSettings;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.j2ee.MavenJavaEEConstants;
import static org.netbeans.modules.maven.j2ee.ui.wizard.Bundle.*;
import org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.openide.WizardDescriptor;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle.Messages;

/**
 * Base abstract class for all types of Maven enterprise projects.
 * Encapsulates some Wizard related stuffs and few methods common for every project type
 *
 * @author Martin Janicek
 */
public abstract class BaseWizardIterator implements WizardDescriptor.BackgroundInstantiatingIterator {

    protected transient WizardDescriptor wiz;
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private final List<ChangeListener> listeners;


    public BaseWizardIterator() {
        listeners = new ArrayList<ChangeListener>();
    }

    protected abstract WizardDescriptor.Panel[] createPanels(ValidationGroup vg);


    protected void saveSettingsToNbConfiguration(Project project) throws IOException {
        // Getting properties saved in ServerSelectionHelper.storeServerSettings
        String instanceID = (String) wiz.getProperty(MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER_ID);
        String serverID = (String) wiz.getProperty(MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER);
        String j2eeVersion = (String) wiz.getProperty(MavenJavaEEConstants.HINT_J2EE_VERSION);
        Profile j2eeProfile = Profile.fromPropertiesString(j2eeVersion);

        // Saving server information for project
        if (j2eeProfile != null) {
            JavaEEProjectSettings.setProfile(project, j2eeProfile);
        }

        // Store last used server for later usage --> #244534
        if (instanceID != null) {
            UserProjectSettings.getDefault().setLastUsedServer(instanceID);
        }

        MavenProjectSupport.setServerID(project, serverID);
        JavaEEProjectSettings.setServerInstanceID(project, instanceID);
        MavenProjectSupport.createWebXMLIfRequired(project, serverID);

        if (j2eeVersion != null && j2eeVersion.contains("1.8")) { //NOI18N
            JavaPlatform platform = findJDK8Platform();
            loadAuxillaryProps(platform,project);
        } else if (j2eeVersion != null && j2eeVersion.contains("1.7")) { //NOI18N
            JavaPlatform platform = findJDK7Platform();
            loadAuxillaryProps(platform,project);
        }
    }
    
    private void loadAuxillaryProps(JavaPlatform platform, Project project){
        AuxiliaryProperties properties = project.getLookup().lookup(AuxiliaryProperties.class);

            if (platform == null || platform.equals(JavaPlatformManager.getDefault().getDefaultPlatform())) {
                properties.put(Constants.HINT_JDK_PLATFORM, null, true);
            } else {
                properties.put(Constants.HINT_JDK_PLATFORM, platform.getDisplayName(), true);
            }
    }

    private JavaPlatform findJDK7Platform() {
        JavaPlatform defaultPlatform = JavaPlatformManager.getDefault().getDefaultPlatform();
        List<JavaPlatform> jdk7Platforms = getJdk7Platforms();

        // If the default platform support JDK 7 then choose it
        if (jdk7Platforms.contains(defaultPlatform)) {
            return defaultPlatform;
        }

        // Otherwise take one of the JDK 7 complient platforms
        for (JavaPlatform platform : jdk7Platforms) {
            return platform;
        }
        return null;
    }

    private JavaPlatform findJDK8Platform() {
        JavaPlatform defaultPlatform = JavaPlatformManager.getDefault().getDefaultPlatform();
        List<JavaPlatform> jdk8Platforms = getJdk8Platforms();

        // If the default platform support JDK 7 then choose it
        if (jdk8Platforms.contains(defaultPlatform)) {
            return defaultPlatform;
        }

        // Otherwise take one of the JDK 8 complient platforms
        for (JavaPlatform platform : jdk8Platforms) {
            return platform;
        }
        return null;
    }

    private List<JavaPlatform> getJdk7Platforms() {
        List<JavaPlatform> jdk7platforms = new ArrayList<JavaPlatform>();

        for (JavaPlatform platform : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
            SpecificationVersion version = platform.getSpecification().getVersion();
            if ("1.7".equals(version.toString())) { //NOI18N
                jdk7platforms.add(platform);
            }
        }
        return jdk7platforms;
    }

    private List<JavaPlatform> getJdk8Platforms() {
        List<JavaPlatform> jdk8platforms = new ArrayList<JavaPlatform>();

        for (JavaPlatform platform : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
            SpecificationVersion version = platform.getSpecification().getVersion();
            if ("1.8".equals(version.toString())) { //NOI18N
                jdk8platforms.add(platform);
            }
        }
        return jdk8platforms;
    }

    @Override
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        this.index = 0;
        ValidationGroup vg = ValidationGroup.create(new WizardDescriptorAdapter(wiz));
        panels = createPanels(vg);
        updateSteps();
    }


    @Override
    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty("projdir", null); //NOI18N
        this.wiz.putProperty("name", null); //NOI18N
        this.wiz = null;
        panels = null;
        listeners.clear();
    }

    @Override
    @Messages("NameFormat={0} of {1}")
    public String name() {
        return NameFormat(index + 1, panels.length);
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
    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    private void fireChange() {
        synchronized (listeners) {
            for (ChangeListener list : listeners) {
                list.stateChanged(new ChangeEvent(this));
            }
        }
    }

    private void updateSteps() {
        // Make sure list of steps is accurate.
        String[] steps = new String[panels.length];
        String[] basicOnes = createSteps();
        System.arraycopy(basicOnes, 0, steps, 0, basicOnes.length);
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (i >= basicOnes.length || steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) {
                // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", Integer.valueOf(i)); //NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); //NOI18N
            }
        }
    }

    @Messages("LBL_CreateProjectStep2ee=Name and Location")
    private String[] createSteps() {
        return new String[] {
            LBL_CreateProjectStep2ee(),
            LBL_EESettings()
        };
    }
}