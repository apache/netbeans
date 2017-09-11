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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2008 Sun
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

package org.netbeans.modules.junit.ui.wizards;

import java.awt.Component;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.java.testrunner.GuiUtils;
import org.netbeans.modules.gsf.testrunner.api.SelfResizingPanel;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Marian Petras
 */
class EmptyTestStepLocation implements WizardDescriptor.Panel<WizardDescriptor> {

    private Component visualComp;
    private JCheckBox chkSetUp;
    private JCheckBox chkTearDown;
    private JCheckBox chkBeforeClass;
    private JCheckBox chkAfterClass;
    private JCheckBox chkCodeHints;

    EmptyTestStepLocation() {
        super();
        visualComp = createVisualComp();
    }

    private Component createVisualComp() {
        JCheckBox[] chkBoxes;
        
        JComponent optCode = GuiUtils.createChkBoxGroup(
                NbBundle.getMessage(
                        GuiUtils.class,
                        "CommonTestsCfgOfCreate.groupOptCode"),               //NOI18N
                chkBoxes = GuiUtils.createCheckBoxes(new String[] {
                        GuiUtils.CHK_SETUP,
                        GuiUtils.CHK_TEARDOWN,
                        GuiUtils.CHK_BEFORE_CLASS,
                        GuiUtils.CHK_AFTER_CLASS}));
        chkSetUp = chkBoxes[0];
        chkTearDown = chkBoxes[1];
        chkBeforeClass = chkBoxes[2];
        chkAfterClass = chkBoxes[3];
        
        JComponent optComments = GuiUtils.createChkBoxGroup(
                NbBundle.getMessage(
                        GuiUtils.class,
                        "CommonTestsCfgOfCreate.groupOptComments"),           //NOI18N
                chkBoxes = GuiUtils.createCheckBoxes(new String[] {
                        GuiUtils.CHK_HINTS}));
        chkCodeHints = chkBoxes[0];

        JComponent box = new SelfResizingPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.X_AXIS));
        box.add(optCode);
        box.add(Box.createHorizontalStrut(18));
        box.add(optComments);

        /* tune layout of the components within the box: */
        optCode.setAlignmentY(0.0f);
        optComments.setAlignmentY(0.0f);

        return box;
    }

    public void addChangeListener(ChangeListener l) {
         // no listeners needed - the panel is always valid
    }

    public void removeChangeListener(ChangeListener l) {
         // no listeners needed - the panel is always valid
    }

    public Component getComponent() {
        return visualComp;
    }

    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.junit.wizards.EmptyTest");//NOI18N
    }

    public boolean isValid() {
        return true;
    }

    public void readSettings(WizardDescriptor settings) {
        chkSetUp.setSelected(
                Boolean.TRUE.equals(settings.getProperty(GuiUtils.CHK_SETUP)));
        chkTearDown.setSelected(
                Boolean.TRUE.equals(settings.getProperty(GuiUtils.CHK_TEARDOWN)));
        chkBeforeClass.setSelected(
                Boolean.TRUE.equals(settings.getProperty(GuiUtils.CHK_BEFORE_CLASS)));
        chkAfterClass.setSelected(
                Boolean.TRUE.equals(settings.getProperty(GuiUtils.CHK_AFTER_CLASS)));
        chkCodeHints.setSelected(
                Boolean.TRUE.equals(settings.getProperty(GuiUtils.CHK_HINTS)));
    }

    public void storeSettings(WizardDescriptor settings) {
        settings.putProperty(GuiUtils.CHK_SETUP,
                           Boolean.valueOf(chkSetUp.isSelected()));
        settings.putProperty(GuiUtils.CHK_TEARDOWN,
                           Boolean.valueOf(chkTearDown.isSelected()));
        settings.putProperty(GuiUtils.CHK_BEFORE_CLASS,
                           Boolean.valueOf(chkBeforeClass.isSelected()));
        settings.putProperty(GuiUtils.CHK_AFTER_CLASS,
                           Boolean.valueOf(chkAfterClass.isSelected()));
        settings.putProperty(GuiUtils.CHK_HINTS,
                           Boolean.valueOf(chkCodeHints.isSelected()));
    }

}
