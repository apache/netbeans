/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.j2seproject.ui.wizards;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 *
 * @author Dusan Balek
 */
public final class MoveToModulePathPanel implements WizardDescriptor.Panel<WizardDescriptor>, ChangeListener {

    public static final String CP_ITEMS_TO_MOVE = "cp_items_to_move"; //NOI18N

    private final J2SEProject project;
    private final EditableProperties ep;
    private final List<ChangeListener> listeners = new ArrayList<>();
    private MoveToModulePathPanelGUI gui;

    public MoveToModulePathPanel(J2SEProject project, EditableProperties ep) {
        this.project = project;
        this.ep = ep;
    }

    @Override
    public Component getComponent() {
        if (gui == null) {
            gui = new MoveToModulePathPanelGUI(project);
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
        if (gui != null) {
            ClassPathSupport cs = new ClassPathSupport(project.evaluator(), project.getReferenceHelper(), project.getAntProjectHelper(), project.getUpdateHelper(), null);
            gui.setCPItems(cs.itemsList(ep.get(ProjectProperties.JAVAC_CLASSPATH)));
        }
    }

    @Override
    public void storeSettings(WizardDescriptor wizard) {
        Object value = wizard.getValue();
        if (WizardDescriptor.PREVIOUS_OPTION.equals(value) || WizardDescriptor.CANCEL_OPTION.equals(value)
                || WizardDescriptor.CLOSED_OPTION.equals(value)) {
            return;
        }
        if (gui != null) {
            wizard.putProperty(CP_ITEMS_TO_MOVE, gui.getCPItemsToMove());
        }
        wizard.putProperty("NewFileWizard_Title", null); // NOI18N
    }

    @Override
    public boolean isValid() {
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

    @Override
    public void stateChanged(ChangeEvent e) {
        fireChange();
    }

    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener listener : listeners) {
            listener.stateChanged(e);
        }
    }
}
