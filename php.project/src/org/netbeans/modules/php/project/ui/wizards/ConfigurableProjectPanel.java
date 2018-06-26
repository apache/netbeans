/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.wizards;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import javax.swing.JPanel;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.project.ui.LocalServer;
import org.netbeans.modules.php.project.ui.ProjectNameProvider;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;

/**
 * @author Tomas Mysik
 */
public abstract class ConfigurableProjectPanel extends JPanel implements ProjectNameProvider, DocumentListener, ChangeListener, ActionListener {

    private static final int STEP_INDEX = 0;

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    protected final ProjectFolder projectFolderComponent;

    public ConfigurableProjectPanel(ConfigureProjectPanel wizardPanel) {
        projectFolderComponent = new ProjectFolder(this, wizardPanel);
        projectFolderComponent.addProjectFolderListener(this);

        setName(wizardPanel.getSteps()[STEP_INDEX]);
        putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, STEP_INDEX);
        // Step name (actually the whole list for reference).
        putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, wizardPanel.getSteps());
    }

    // abstract methods
    public abstract void setProjectName(String projectName);
    public abstract String getSourcesFolder();
    public abstract LocalServer getSourcesLocation();
    public abstract void selectSourcesLocation(LocalServer localServer);
    public abstract MutableComboBoxModel<LocalServer> getLocalServerModel();
    public abstract void setLocalServerModel(MutableComboBoxModel<LocalServer> localServers);
    public abstract PhpVersion getPhpVersion();
    public abstract void setPhpVersion(PhpVersion phpVersion);
    public abstract Charset getEncoding();
    public abstract void setEncoding(Charset encoding);
    public abstract void setState(boolean enabled);
    public abstract boolean getState();

    public String getProjectFolder() {
        return projectFolderComponent.getProjectFolder();
    }

    public void setProjectFolder(String projectFolder) {
        projectFolderComponent.setProjectFolder(projectFolder);
    }

    public boolean isProjectFolderUsed() {
        return projectFolderComponent.isProjectFolderUsed();
    }

    public void setProjectFolderUsed(boolean used) {
        projectFolderComponent.setProjectFolderUsed(used);
    }

    public void addConfigureProjectListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeConfigureProjectListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    // listeners
    @Override
    public void insertUpdate(DocumentEvent e) {
        processUpdate();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        processUpdate();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        processUpdate();
    }

    private void processUpdate() {
        changeSupport.fireChange();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        changeSupport.fireChange();
    }
}
