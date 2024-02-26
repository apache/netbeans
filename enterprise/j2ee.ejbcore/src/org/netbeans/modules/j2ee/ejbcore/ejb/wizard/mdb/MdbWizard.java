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
package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.mdb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettings;
import org.netbeans.modules.j2ee.common.ProjectUtil;
import org.netbeans.modules.j2ee.common.ServerUtil;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.j2ee.core.api.support.wizard.DelegatingWizardDescriptorPanel;
import org.netbeans.modules.j2ee.core.api.support.wizard.Wizards;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.netbeans.modules.j2ee.ejbcore.api.codegeneration.MessageGenerator;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.MultiTargetChooserPanel;
import org.netbeans.modules.javaee.specs.support.api.JmsSupport;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public final class MdbWizard implements WizardDescriptor.InstantiatingIterator {

    private static final Logger LOG = Logger.getLogger(MdbWizard.class.getName());
    public static final String PROP_DESTINATION_TYPE = "DESTINATION_TYPE";
    private WizardDescriptor.Panel[] panels;
    private int index = 0;
    private MdbLocationPanel ejbPanel;
    private MdbPropertiesPanel propertiesPanel;
    private WizardDescriptor wiz;
    private static final Map<Profile, String> MAVEN_JAVAEE_API_LIBS = new HashMap<>(6);
    private static final String[] MAVEN_JAVAEE_WEB_API_LIBS = new String[]{
        "javaee-web-api-6.0", //NOI18N
        "javaee-web-api-7.0", //NOI18N
        "javaee-web-api-8.0", //NOI18N
        "jakarta.jakartaee-web-api-8.0.0", //NOI18N
        "jakarta.jakartaee-web-api-9.0.0", //NOI18N
        "jakarta.jakartaee-web-api-9.1.0", //NOI18N
        "jakarta.jakartaee-web-api-10.0.0", //NOI18N
        "jakarta.jakartaee-web-api-11.0.0" //NOI18N
    };
    private static final String[] SESSION_STEPS = new String[]{
        NbBundle.getMessage(MdbWizard.class, "LBL_SpecifyEJBInfo"), //NOI18N
        NbBundle.getMessage(MdbWizard.class, "LBL_SpecifyActivationProperties") //NOI18N
    };

    static {
        MAVEN_JAVAEE_API_LIBS.put(Profile.JAVA_EE_6_FULL, "javaee-api-6.0"); //NOI18N
        MAVEN_JAVAEE_API_LIBS.put(Profile.JAVA_EE_7_FULL, "javaee-api-7.0"); //NOI18N
        MAVEN_JAVAEE_API_LIBS.put(Profile.JAVA_EE_8_FULL, "javaee-api-8.0"); //NOI18N
        MAVEN_JAVAEE_API_LIBS.put(Profile.JAKARTA_EE_8_FULL, "jakarta.jakartaee-api-8.0.0"); //NOI18N
        MAVEN_JAVAEE_API_LIBS.put(Profile.JAKARTA_EE_9_FULL, "jakarta.jakartaee-api-9.0.0"); //NOI18N
        MAVEN_JAVAEE_API_LIBS.put(Profile.JAKARTA_EE_9_1_FULL, "jakarta.jakartaee-api-9.1.0"); //NOI18N
        MAVEN_JAVAEE_API_LIBS.put(Profile.JAKARTA_EE_10_FULL, "jakarta.jakartaee-api-10.0.0"); //NOI18N
        MAVEN_JAVAEE_API_LIBS.put(Profile.JAKARTA_EE_11_FULL, "jakarta.jakartaee-api-11.0.0"); //NOI18N
    }

    @Override
    public String name() {
        return NbBundle.getMessage(MdbWizard.class, "LBL_MessageEJBWizardTitle"); //NOI18N
    }

    @Override
    public void uninitialize(WizardDescriptor wiz) {
    }

    @Override
    public void initialize(WizardDescriptor wizardDescriptor) {
        wiz = wizardDescriptor;
        Project project = Templates.getProject(wiz);
        SourceGroup[] sourceGroups = SourceGroups.getJavaSourceGroups(project);
        ejbPanel = new MdbLocationPanel(wiz);
        WizardDescriptor.Panel locationPanel = new ValidatingPanel(
                new MultiTargetChooserPanel(project, sourceGroups, ejbPanel, true));
        propertiesPanel = new MdbPropertiesPanel(wizardDescriptor);
        panels = new WizardDescriptor.Panel[]{locationPanel, propertiesPanel};
        Wizards.mergeSteps(wiz, panels, SESSION_STEPS);
    }

    @Override
    public Set instantiate() throws IOException {
        FileObject pkg = Templates.getTargetFolder(wiz);
        EjbJar ejbModule = EjbJar.getEjbJar(pkg);

        Profile profile = ejbModule.getJ2eeProfile();
        boolean isSimplified = profile != null && profile.isAtLeast(Profile.JAVA_EE_5);
        MessageGenerator generator = MessageGenerator.create(
                profile,
                Templates.getTargetName(wiz),
                pkg,
                ejbPanel.getDestination(),
                isSimplified,
                propertiesPanel.getProperties(),
                JmsSupport.getInstance(ProjectUtil.getPlatform(Templates.getProject(wiz))));
        FileObject result = generator.generate();

        // see issue #230021 - update Maven project dependencies if necessary
        updateProjectJavaEESupport();

        return result == null ? Collections.EMPTY_SET : Collections.singleton(result);
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
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

    private boolean isJmsOnClasspath() throws IOException {
        ClassPath classPath = Utils.getCompileClassPath(Templates.getProject(wiz));
        if (classPath != null && classPath.findResource("javax/jms") == null) {  //NOI18N
            return false;
        }
        return true;
    }

    private Profile getTargetFullProfile() {
        Profile profile = JavaEEProjectSettings.getProfile(Templates.getProject(wiz));
        if (profile != null) {
            if (profile.isAtLeast(Profile.JAKARTA_EE_11_WEB)) {
              return Profile.JAKARTA_EE_11_FULL;
            } else if (profile.isAtLeast(Profile.JAKARTA_EE_10_WEB)) {
              return Profile.JAKARTA_EE_10_FULL;
            } else if (profile.isAtLeast(Profile.JAKARTA_EE_9_1_WEB)) {
              return Profile.JAKARTA_EE_9_1_FULL;
            } else if (profile.isAtLeast(Profile.JAKARTA_EE_9_WEB)) {
              return Profile.JAKARTA_EE_9_FULL;
            } else if (profile.isAtLeast(Profile.JAKARTA_EE_8_WEB)) {
              return Profile.JAKARTA_EE_8_FULL;
            } else if (profile.isAtLeast(Profile.JAVA_EE_8_WEB)) {
                return Profile.JAVA_EE_8_FULL;
            } else if (profile.isAtLeast(Profile.JAVA_EE_7_WEB)) {
                return Profile.JAVA_EE_7_FULL;
            } else if (profile.isAtLeast(Profile.JAVA_EE_6_WEB)) {
                return Profile.JAVA_EE_6_FULL;
            } else {
                LOG.severe("Unknown JavaEE web profile.");
            }
        } else {
            LOG.severe("Project profile was not recognized correctly.");
        }
        return null;
    }

    private boolean removeWebApiJarsFromClasspath() throws IOException {
        List<Library> toRemove = new ArrayList<>();
        for (String libraryName : MAVEN_JAVAEE_WEB_API_LIBS) {
            Library library = LibraryManager.getDefault().getLibrary(libraryName);
            if (library != null) {
                toRemove.add(library);
            }
        }
        return Utils.removeLibraryFromClasspath(Templates.getProject(wiz), toRemove.toArray(new Library[0]));
    }

    private void enhanceProjectClasspath(Profile targetProfile) throws IOException {
        Project project = Templates.getProject(wiz);
        String fullLibraryName = MAVEN_JAVAEE_API_LIBS.get(targetProfile);
        Library targetLibrary = LibraryManager.getDefault().getLibrary(fullLibraryName);
        if (targetLibrary != null) {
            Utils.addLibraryToClasspath(project, targetLibrary);
        }
    }

    private void updateProjectJavaEESupport() {
        Project project = Templates.getProject(wiz);
        try {
            AntBasedProjectType abpt = project.getLookup().lookup(AntBasedProjectType.class);
            // in cases of Maven project without JMS on classpath
            if (abpt == null && !isJmsOnClasspath()) {
                Profile targetFullProfile = getTargetFullProfile();
                if (targetFullProfile != null) {
                    // remove all web API from the classpath, if the CP change proceed to CP upgrade
                    if (removeWebApiJarsFromClasspath()) {
                        // add API jar matching the targeted project ee level
                        enhanceProjectClasspath(targetFullProfile);
                        // update target EE level
                        JavaEEProjectSettings.setProfile(project, targetFullProfile);
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * A panel which checks whether the target project has a valid server set, otherwise it delegates to another panel.
     */
    private static final class ValidatingPanel extends DelegatingWizardDescriptorPanel {

        public ValidatingPanel(WizardDescriptor.Panel delegate) {
            super(delegate);
        }

        @Override
        public boolean isValid() {
            if (!ServerUtil.isValidServerInstance(getProject())) {
                getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(MdbWizard.class, "ERR_MissingServer")); // NOI18N
                return false;
            }
            return super.isValid();
        }
    }
}
