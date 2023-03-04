/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
