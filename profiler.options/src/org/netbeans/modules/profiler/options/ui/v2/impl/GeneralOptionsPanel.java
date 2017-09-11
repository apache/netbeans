/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2014 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.profiler.options.ui.v2.impl;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.components.JExtendedSpinner;
import org.netbeans.modules.profiler.actions.RunCalibrationAction;
import org.netbeans.modules.profiler.api.ProfilerIDESettings;
import org.netbeans.modules.profiler.options.ui.v2.ProfilerOptionsContainer;
import org.netbeans.modules.profiler.options.ui.v2.ProfilerOptionsPanel;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "GeneralOptionsPanel_Name=General",
    "GeneralOptionsPanel_CatWindow=Profiler Window",
    "GeneralOptionsPanel_NoDataHint=&Show 'No data collected yet' hint before first profiling session",
    "GeneralOptionsPanel_ProfilerStatus=&Display profiling session status when window is active",
//    "GeneralOptionsPanel_ExpertSetup=&Enable manual setup for Methods and Objects (expert users)",
    "GeneralOptionsPanel_CatProfiling=Profiling",
    "GeneralOptionsPanel_ProfilingPort=&Profiling port:",
    "GeneralOptionsPanel_ManageCalibration=Manage calibration data:",
    "GeneralOptionsPanel_ManageButton=&Manage",
    "GeneralOptionsPanel_CatMiscellaneous=Miscellaneous",
    "GeneralOptionsPanel_ResetDNSA=Reset all 'Do not show again' confirmations:",
    "GeneralOptionsPanel_ResetButton=&Reset"
})
@ServiceProvider( service = ProfilerOptionsPanel.class, position = 10 )
public final class GeneralOptionsPanel extends ProfilerOptionsPanel {
    
    private JCheckBox noDataHintChoice;
    private JCheckBox profilerStatusChoice;
//    private JCheckBox expertConfigChoice;
    private JSpinner portSpinner;
    private JButton resetDNSAButton;
    
    
    public GeneralOptionsPanel() {
        initUI();
    }
    
    
    public String getDisplayName() {
        return Bundle.GeneralOptionsPanel_Name();
    }

    public void storeTo(ProfilerIDESettings settings) {
        settings.setShowNoDataHint(noDataHintChoice.isSelected());
        settings.setLogProfilerStatus(profilerStatusChoice.isSelected());
//        settings.setEnableExpertSettings(expertConfigChoice.isSelected());
        settings.setPortNo((Integer)portSpinner.getValue());
    }

    public void loadFrom(ProfilerIDESettings settings) {
        noDataHintChoice.setSelected(settings.getShowNoDataHint());
        profilerStatusChoice.setSelected(settings.getLogProfilerStatus());
//        expertConfigChoice.setSelected(settings.getEnableExpertSettings());
        portSpinner.setValue(settings.getPortNo());
        resetDNSAButton.setEnabled(true);
    }

    public boolean equalsTo(ProfilerIDESettings settings) {
        if (noDataHintChoice.isSelected() != settings.getShowNoDataHint()) return false;
        if (profilerStatusChoice.isSelected() != settings.getLogProfilerStatus()) return false;
//        if (expertConfigChoice.isSelected() != settings.getEnableExpertSettings()) return false;
        if (!Objects.equals(portSpinner.getValue(), settings.getPortNo())) return false;
        return true;
    }
    
    
    private void initUI() {
        setLayout(new GridBagLayout());
        
        GridBagConstraints c;
        int y = 0;
        int htab = 8;
        int hgap = 10;
        int vgap = 5;
        
        Separator dataTransferSeparator = new Separator(Bundle.GeneralOptionsPanel_CatWindow());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, vgap * 2, 0);
        add(dataTransferSeparator, c);
        
        noDataHintChoice = new JCheckBox();
        Mnemonics.setLocalizedText(noDataHintChoice, Bundle.GeneralOptionsPanel_NoDataHint());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, htab, vgap, 0);
        add(noDataHintChoice, c);
        
        profilerStatusChoice = new JCheckBox();
        Mnemonics.setLocalizedText(profilerStatusChoice, Bundle.GeneralOptionsPanel_ProfilerStatus());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(vgap, htab, vgap, 0);
        add(profilerStatusChoice, c);
        
//        expertConfigChoice = new JCheckBox();
//        Mnemonics.setLocalizedText(expertConfigChoice, Bundle.GeneralOptionsPanel_ExpertSetup());
//        c = new GridBagConstraints();
//        c.gridx = 0;
//        c.gridy = y++;
//        c.gridwidth = GridBagConstraints.REMAINDER;
//        c.anchor = GridBagConstraints.WEST;
//        c.fill = GridBagConstraints.NONE;
//        c.insets = new Insets(vgap, htab, vgap, 0);
//        add(expertConfigChoice, c);
        
        Separator profilingSeparator = new Separator(Bundle.GeneralOptionsPanel_CatProfiling());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(vgap * 4, 0, vgap * 2, 0);
        add(profilingSeparator, c);
        
        JLabel portLabel = new JLabel();
        Mnemonics.setLocalizedText(portLabel, Bundle.GeneralOptionsPanel_ProfilingPort());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, htab, vgap, 0);
        add(portLabel, c);
        
        portSpinner = new JExtendedSpinner(new SpinnerNumberModel(5140, 1, 65535, 1));
        portLabel.setLabelFor(portSpinner);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = y++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, hgap, vgap, 0);
        add(portSpinner, c);
        
        JLabel manageCalibrationLabel = new JLabel();
        Mnemonics.setLocalizedText(manageCalibrationLabel, Bundle.GeneralOptionsPanel_ManageCalibration());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 3;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, htab, vgap, 0);
        add(manageCalibrationLabel, c);
        
        JButton manageCalibrationButton = new JButton() {
            protected void fireActionPerformed(final ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() { new RunCalibrationAction().actionPerformed(e); }
                });
            }
        };
        Mnemonics.setLocalizedText(manageCalibrationButton, Bundle.GeneralOptionsPanel_ManageButton());
        manageCalibrationLabel.setLabelFor(manageCalibrationButton);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = y++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, hgap, vgap, 0);
        add(manageCalibrationButton, c);
        
        Separator miscSeparator = new Separator(Bundle.GeneralOptionsPanel_CatMiscellaneous());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(vgap * 4, 0, vgap * 2, 0);
        add(miscSeparator, c);
        
        JLabel resetDNSALabel = new JLabel();
        Mnemonics.setLocalizedText(resetDNSALabel, Bundle.GeneralOptionsPanel_ResetDNSA());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, htab, vgap, 0);
        add(resetDNSALabel, c);
        
        resetDNSAButton = new JButton() {
            protected void fireActionPerformed(ActionEvent e) {
                super.fireActionPerformed(e);
                ProfilerIDESettings.getInstance().clearDoNotShowAgainMap();
                reloadAllPanels();
                setEnabled(false);
            }
        };
        Mnemonics.setLocalizedText(resetDNSAButton, Bundle.GeneralOptionsPanel_ResetButton());
        resetDNSALabel.setLabelFor(resetDNSAButton);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = y++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, hgap, vgap, 0);
        add(resetDNSAButton, c);
        
        JPanel filler = UIUtils.createFillerPanel();
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        add(filler, c);
        
        Dimension dim1 = manageCalibrationButton.getPreferredSize();
        Dimension dim2 = resetDNSAButton.getPreferredSize();
        Dimension dim3 = portSpinner.getPreferredSize();
        
        int width = manageCalibrationButton.getPreferredSize().width;
        width = Math.max(dim1.width, dim2.width);
        width = Math.max(width, dim3.width);
        
        dim1.width = width;
        dim2.width = width;
        dim3.width = width;
        
        manageCalibrationButton.setPreferredSize(dim1);
        resetDNSAButton.setPreferredSize(dim2);
        portSpinner.setPreferredSize(dim3);
        
    }
    
    
    private void reloadAllPanels() {
        Container parent = getParent();
        while (parent != null) {
            if (parent instanceof ProfilerOptionsContainer) {
                ProfilerIDESettings settings = ProfilerIDESettings.getInstance();
                ((ProfilerOptionsContainer)parent).loadFrom(settings);
                return;
            }
            parent = parent.getParent();
        }
    }
    
}
