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

package org.netbeans.modules.java.j2semodule.ui.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.platform.JavaPlatform;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.java.j2semodule.J2SEModularProjectGenerator;
import org.netbeans.spi.java.project.support.ui.SharableLibrariesUtils;
import org.netbeans.spi.project.ui.support.ProjectChooser;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 * Wizard to create a new J2SE modular project.
 */
public class NewJ2SEModularProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {

    static final String PROP_NAME_INDEX = "nameIndex";      //NOI18N
    static final String PROP_BUILD_SCRIPT_NAME = "buildScriptName"; //NOI18N
    static final String PROP_DIST_FOLDER = "distFolder";    //NOI18N

    private static final Logger LOG = Logger.getLogger(NewJ2SEModularProjectWizardIterator.class.getName());
    private static final long serialVersionUID = 1L;

    private NewJ2SEModularProjectWizardIterator() {
    }

    @TemplateRegistration(folder="Project/Standard", position=350, displayName="#template_multimodule", iconBase="org/netbeans/modules/java/j2semodule/ui/resources/j2seModuleProject.png", description="../resources/emptyModuleProject.html")
    @Messages("template_multimodule=Java Modular Project")
    public static NewJ2SEModularProjectWizardIterator multiModule() {
        return new NewJ2SEModularProjectWizardIterator();
    }

    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new PanelConfigureProject()
        };
    }

    private String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(NewJ2SEModularProjectWizardIterator.class,"LAB_ConfigureProject"),
        };
    }


    @Override
    public Set<?> instantiate() throws IOException {
        assert false : "Cannot call this method if implements WizardDescriptor.ProgressInstantiatingIterator."; //NOI18N
        return null;
    }

    @Override
    public Set<FileObject> instantiate (ProgressHandle handle) throws IOException {
        final WizardDescriptor myWiz = this.wiz;
        if (myWiz == null) {
            LOG.warning("The uninitialize called before instantiate."); //NOI18N
            return Collections.emptySet();
        }
        handle.start (3);
        Set<FileObject> resultSet = new HashSet<>();
        File dirF = (File)myWiz.getProperty("projdir"); //NOI18N
        if (dirF == null) {
            throw new NullPointerException ("projdir == null, props:" + myWiz.getProperties()); //NOI18N
        }
        dirF = FileUtil.normalizeFile(dirF);
        String name = (String)myWiz.getProperty("name"); //NOI18N
        JavaPlatform platform = (JavaPlatform)myWiz.getProperty("javaPlatform"); //NOI18N
        handle.progress (NbBundle.getMessage (NewJ2SEModularProjectWizardIterator.class, "LBL_NewJ2SEModuleProjectWizardIterator_WizardProgress_CreatingProject"), 1);
        J2SEModularProjectGenerator.createProject(dirF, name, platform);
        handle.progress (2);
        FileObject dir = FileUtil.toFileObject(dirF);

        // Returning FileObject of project diretory.
        // Project will be open and set as main
        final Integer ind = (Integer) myWiz.getProperty(PROP_NAME_INDEX);
        if (ind != null) {
            WizardSettings.setNewProjectCount(ind);
        }
        resultSet.add (dir);
        handle.progress (NbBundle.getMessage (NewJ2SEModularProjectWizardIterator.class, "LBL_NewJ2SEModuleProjectWizardIterator_WizardProgress_PreparingToOpen"), 3);
        dirF = (dirF != null) ? dirF.getParentFile() : null;
        if (dirF != null && dirF.exists()) {
            ProjectChooser.setProjectsFolder (dirF);
        }

        SharableLibrariesUtils.setLastProjectSharable(false);
        return resultSet;
    }

    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient volatile WizardDescriptor wiz;

    @Override
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
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
                JComponent jc = (JComponent)c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            }
        }
        //set the default values of the sourceRoot and the testRoot properties
        this.wiz.putProperty("sourceRoot", new File[0]);    //NOI18N
        this.wiz.putProperty("testRoot", new File[0]);      //NOI18N
    }

    @Override
    public void uninitialize(WizardDescriptor wiz) {
        this.wiz = null;
        this.panels = null;
    }

    @Override
    public String name() {
        return NbBundle.getMessage(NewJ2SEModularProjectWizardIterator.class, "LAB_IteratorName", index + 1, panels.length);
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
    public WizardDescriptor.Panel current () {
        return panels[index];
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public final void addChangeListener(ChangeListener l) {}
    @Override
    public final void removeChangeListener(ChangeListener l) {}

}
