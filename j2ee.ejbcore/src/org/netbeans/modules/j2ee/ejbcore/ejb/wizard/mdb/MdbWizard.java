/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
    private static final Map<Profile, String> MAVEN_JAVAEE_API_LIBS = new HashMap<>(2);
    private static final String[] MAVEN_JAVAEE_WEB_API_LIBS = new String[]{
        "javaee-web-api-6.0", //NOI18N
        "javaee-web-api-7.0", //NOI18N
    };
    private static final String[] SESSION_STEPS = new String[]{
        NbBundle.getMessage(MdbWizard.class, "LBL_SpecifyEJBInfo"), //NOI18N
        NbBundle.getMessage(MdbWizard.class, "LBL_SpecifyActivationProperties") //NOI18N
    };

    static {
        MAVEN_JAVAEE_API_LIBS.put(Profile.JAVA_EE_6_FULL, "javaee-api-6.0"); //NOI18N
        MAVEN_JAVAEE_API_LIBS.put(Profile.JAVA_EE_7_FULL, "javaee-api-7.0"); //NOI18N
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
            if (profile.isAtLeast(Profile.JAVA_EE_7_WEB)) {
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
        return Utils.removeLibraryFromClasspath(Templates.getProject(wiz), toRemove.toArray(new Library[toRemove.size()]));
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
