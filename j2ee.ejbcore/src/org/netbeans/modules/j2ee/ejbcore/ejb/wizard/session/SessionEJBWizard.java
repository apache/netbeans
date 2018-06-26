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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
                 Profile.JAVA_EE_7_FULL.equals(profile) || Profile.JAVA_EE_7_WEB.equals(profile);
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

