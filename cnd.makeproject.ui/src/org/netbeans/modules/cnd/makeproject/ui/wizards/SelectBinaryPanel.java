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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.util.ArrayList;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.WizardConstants;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.ProjectWizardPanels.NamedPanel;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileSystem;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 */
public class SelectBinaryPanel implements WizardDescriptor.FinishablePanel<WizardDescriptor>, NamedPanel, ChangeListener {
    private WizardDescriptor wizardDescriptor;
    private SelectBinaryPanelVisual component;
    private final String name;
    private boolean isValid = false;
    private final BinaryWizardStorage wizardStorage;
    private final Set<ChangeListener> listeners = new HashSet<>(1);

    public SelectBinaryPanel(){
        name = NbBundle.getMessage(SelectBinaryPanel.class, "SelectBinaryPanelVisual.Title"); // NOI18N
        wizardStorage = new BinaryWizardStorage(this);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isFinishPanel() {
        return  Boolean.TRUE.equals(WizardConstants.PROPERTY_SIMPLE_MODE.get(wizardDescriptor));
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        String[] res;
        Object o = component.getClientProperty(WizardDescriptor.PROP_CONTENT_DATA);
        String[] names = (String[]) o;
        if (Boolean.TRUE.equals(WizardConstants.PROPERTY_SIMPLE_MODE.get(wizardDescriptor))){
            res = new String[]{names[0]};
        } else {
            res = new String[]{names[0], "..."}; // NOI18N
        }
        component.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, res);
      	fireChangeEvent();
    }

    @Override
    public SelectBinaryPanelVisual getComponent() {
        if (component == null) {
            component = new SelectBinaryPanelVisual(this);
      	    component.setName(name);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("NewBinaryWizardP1"); // NOI18N
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        wizardDescriptor = settings;
        getComponent().read(wizardDescriptor);
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        getComponent().store(settings);
    }

    @Override
    public boolean isValid() {
        return isValid;
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

    private void validate(){
        isValid = component.valid();
        if (SwingUtilities.isEventDispatchThread()) {
            fireChangeEvent();
        } else {
            SwingUtilities.invokeLater(() -> {
                fireChangeEvent();
            });
        }
    }

    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }

    WizardDescriptor getWizardDescriptor(){
        return wizardDescriptor;
    }

    public BinaryWizardStorage getWizardStorage(){
        return wizardStorage;
    }

    public static class BinaryWizardStorage {
        private List<FSPath> binaryPath;
        private FSPath sourceFolderPath;
        private final SelectBinaryPanel controller;

        public BinaryWizardStorage(SelectBinaryPanel controller) {
            this.controller = controller;
        }

        public List<FSPath> getBinaryPath() {
            return binaryPath;
        }

        public void setBinaryPath(FileSystem fs, String path) {
            binaryPath = new ArrayList<>();
            for(String s : path.split(";")) { //NOI18N
                String p = s.trim();
                if (!p.isEmpty()) {
                    binaryPath.add(new FSPath(fs, p));
                }
            }
            controller.validate();
        }

        public FSPath getSourceFolderPath() {
            return sourceFolderPath;
        }

        public void setSourceFolderPath(FSPath path) {
            this.sourceFolderPath = path;
            controller.validate();
        }

        public void validate() {
            controller.validate();
        }
    }
}
