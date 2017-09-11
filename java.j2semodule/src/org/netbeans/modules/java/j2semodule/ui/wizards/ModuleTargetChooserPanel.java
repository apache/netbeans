/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.java.j2semodule.ui.wizards;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Dusan Balek
 */
public class ModuleTargetChooserPanel implements WizardDescriptor.Panel<WizardDescriptor>, ChangeListener {

    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private ModuleTargetChooserPanelGUI gui;
    private WizardDescriptor wizard;

    private Project project;
    private SourceGroup folders[];
    
    public ModuleTargetChooserPanel(Project project, SourceGroup folders[]) {
        this.project = project;
        this.folders = folders;
    }

    @Override
    public Component getComponent() {
        if (gui == null) {
            gui = new ModuleTargetChooserPanelGUI(project, folders, null);
            gui.addChangeListener(this);
        }
        return gui;
    }

    @Override
    public HelpCtx getHelp() {
        return null;
    }

    @Override
    public void readSettings(WizardDescriptor wizard) {
        this.wizard = wizard;
        if (gui != null) {
            // Try to preselect a folder
            FileObject preselectedFolder = Templates.getTargetFolder(wizard);            
            // Init values
            gui.initValues(Templates.getTemplate(wizard), preselectedFolder);
        }
        
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewFileWizard to modify the title
        if (gui != null) {
            Object substitute = gui.getClientProperty ("NewFileWizard_Title"); // NOI18N
            if (substitute != null) {
                wizard.putProperty ("NewFileWizard_Title", substitute); // NOI18N
            }
        }
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        Object value = wizard.getValue();
        if (WizardDescriptor.PREVIOUS_OPTION.equals(value) || WizardDescriptor.CANCEL_OPTION.equals(value) ||
                WizardDescriptor.CLOSED_OPTION.equals(value)) {
            return;
        }
        if (isValid()) {
            assert gui != null;
            FileObject rootFolder = gui.getRootFolder();
            Templates.setTargetFolder(wizard, rootFolder.isValid() ? rootFolder : null);
            Templates.setTargetName(wizard, gui.getTargetName());
        }        
        if (WizardDescriptor.FINISH_OPTION.equals(value)) {
            wizard.putProperty("NewFileWizard_Title", null); // NOI18N
        }
    }

    @Override
    public boolean isValid() {
        setErrorMessage( null );
        if (gui == null) {
            return false;
        }        
        if (gui.getTargetName() == null) {
            setErrorMessage("INFO_ModuleTargetChooser_ProvideModuleName"); // NOI18N
            return false;
        }
        if (!isValidModuleName(gui.getTargetName())) {
            setErrorMessage( "ERR_ModuleTargetChooser_InvalidModule" ); // NOI18N
            return false;
        }
        if (!isValidModule(gui.getRootFolder(), gui.getTargetName())) {
            setErrorMessage("ERR_ModuleTargetChooser_InvalidFolder"); // NOI18N
            return false;
        }
        return true;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChange() {
        ChangeListener[] ls;
        synchronized (listeners) {
            ls = listeners.toArray(new ChangeListener[listeners.size()]);
        }
        ChangeEvent ev = new ChangeEvent(this);
        for (ChangeListener l : ls) {
            l.stateChanged(ev);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireChange();
    }
    
    private boolean isValidModuleName(String str) {
        if (str.length() > 0 && str.charAt(0) == '.') {
            return false;
        }
        StringTokenizer st = new StringTokenizer(str, ".");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if ("".equals(token))
                return false;
            if (!Utilities.isJavaIdentifier(token))
                return false;
        }
        return true;
    }
    
    private boolean isValidModule (FileObject root, final String path) {
        //May be null when nothing selected in the GUI.
        if (root == null) {
            return false;
        }
        if (path == null) {
            return false;
        }
        final StringTokenizer st = new StringTokenizer(path,".");   //NOI18N
        while (st.hasMoreTokens()) {
            root = root.getFileObject(st.nextToken());
            if (root == null) {
                return true;
            }
            else if (root.isData()) {
                return false;
            }
        }
        return true;
    }

    private void setErrorMessage(String key) {
        if (key == null) {
            wizard.getNotificationLineSupport().clearMessages();
        } else {
            wizard.getNotificationLineSupport().setErrorMessage(NbBundle.getMessage(ModuleTargetChooserPanelGUI.class, key));
        }
    }
}
