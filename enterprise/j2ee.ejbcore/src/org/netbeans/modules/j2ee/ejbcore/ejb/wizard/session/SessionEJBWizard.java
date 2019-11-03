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

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.session;

import java.io.IOException;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.ejbcore.api.codegeneration.SessionGenerator;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.j2ee.core.api.support.wizard.Wizards;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.MultiTargetChooserPanel;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public final class SessionEJBWizard implements WizardDescriptor.AsynchronousInstantiatingIterator {

    private WizardDescriptor.Panel[] panels;
    private int index = 0;
    private SessionEJBWizardDescriptor ejbPanel;
    private WizardDescriptor wiz;
    private TimerOptions timerOptions;
    private String resourceWizardName;

    public SessionEJBWizard(String resourceWizardName, TimerOptions timerOptions) {
        this.resourceWizardName = resourceWizardName;
        this.timerOptions = timerOptions;
    }

    public static SessionEJBWizard createSession() {
        return new SessionEJBWizard("LBL_SessionEJBWizardTitle", null); //NOI18N
    }

    public static SessionEJBWizard createTimerSession() {
        return new SessionEJBWizard("LBL_TimerSessionEJBWizardTitle", new TimerOptions()); //NOI18N
    }

    public String name() {
        return NbBundle.getMessage (SessionEJBWizard.class, resourceWizardName);
    }

    public void uninitialize(WizardDescriptor wiz) {
    }

    public void initialize(WizardDescriptor wizardDescriptor) {
        wiz = wizardDescriptor;
        Project project = Templates.getProject(wiz);
        SourceGroup[] sourceGroups = SourceGroups.getJavaSourceGroups(project);
        ejbPanel = new SessionEJBWizardDescriptor(project, timerOptions);
        WizardDescriptor.Panel wizardDescriptorPanel = new MultiTargetChooserPanel(project, sourceGroups, ejbPanel, true);

        panels = new WizardDescriptor.Panel[] {wizardDescriptorPanel};
        Wizards.mergeSteps(wiz, panels, null);
    }

    public Set instantiate () {
        FileObject pkg = Templates.getTargetFolder(wiz);
        EjbJar ejbModule = EjbJar.getEjbJar(pkg);
        // TODO: UI - add checkbox for Java EE 5 to create also EJB 2.1 style EJBs
        Profile profile = ejbModule.getJ2eeProfile();
        boolean isSimplified = Profile.JAVA_EE_5.equals(profile) || Profile.JAVA_EE_6_FULL.equals(profile) || Profile.JAVA_EE_6_WEB.equals(profile) ||
                 Profile.JAVA_EE_7_FULL.equals(profile) || Profile.JAVA_EE_7_WEB.equals(profile) ||
                 Profile.JAVA_EE_8_FULL.equals(profile) || Profile.JAVA_EE_8_WEB.equals(profile);
        SessionGenerator sessionGenerator = SessionGenerator.create(
                Templates.getTargetName(wiz), 
                pkg, 
                ejbPanel.hasRemote(), 
                ejbPanel.hasLocal(), 
                ejbPanel.getSessionType(),
                isSimplified, 
                true, // TODO: UI - add checkbox for creation of business interface
                !isSimplified, // TODO: UI - add checkbox for option XML (not annotation) usage
                ejbPanel.getTimerOptions(),
                ejbPanel.exposeTimerMethod(),
                ejbPanel.nonPersistentTimer()
                );
        FileObject result = null;
        try {
            if (ejbPanel.hasRemote() && ejbPanel.getRemoteInterfaceProject() != null) {
                String packageName = (String)wiz.getProperty(MultiTargetChooserPanel.TARGET_PACKAGE);
                sessionGenerator.initRemoteInterfacePackage(ejbPanel.getRemoteInterfaceProject(), packageName, pkg);
            }
            result = sessionGenerator.generate();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return result == null ? Collections.<FileObject>emptySet() : Collections.singleton(result);
    }

    public void addChangeListener(ChangeListener listener) {
    }

    public void removeChangeListener(ChangeListener listener) {
    }

    public boolean hasPrevious () {
        return index > 0;
    }

    public boolean hasNext () {
    return index < panels.length - 1;
    }

    public void nextPanel () {
        if (! hasNext ()) {
            throw new NoSuchElementException ();
        }
        index++;
    }
    public void previousPanel () {
        if (! hasPrevious ()) {
            throw new NoSuchElementException ();
        }
        index--;
    }
    public WizardDescriptor.Panel current () {
        return panels[index];
    }

}

