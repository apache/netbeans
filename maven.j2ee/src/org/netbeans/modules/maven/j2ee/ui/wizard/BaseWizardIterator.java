/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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

        if (j2eeVersion != null && j2eeVersion.contains("1.7")) { //NOI18N
            JavaPlatform platform = findJDK7Platform();

            AuxiliaryProperties properties = project.getLookup().lookup(AuxiliaryProperties.class);

            if (platform == null || platform.equals(JavaPlatformManager.getDefault().getDefaultPlatform())) {
                properties.put(Constants.HINT_JDK_PLATFORM, null, true);
            } else {
                properties.put(Constants.HINT_JDK_PLATFORM, platform.getDisplayName(), true);
            }
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