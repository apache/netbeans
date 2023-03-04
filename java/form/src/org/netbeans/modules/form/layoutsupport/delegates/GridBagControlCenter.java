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

package org.netbeans.modules.form.layoutsupport.delegates;

import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import org.openide.util.ImageUtilities;

/**
 * @author  phrebejk
 */

public class GridBagControlCenter extends javax.swing.JPanel {

    private GridBagCustomizer customizer;

    private ResourceBundle bundle;

    private ActionL actionLsnr;

    static final long serialVersionUID =-3758289034173265028L;
    /** Creates new form GridBagControlCenter */
    public GridBagControlCenter(GridBagCustomizer customizer) {
        bundle = GridBagLayoutSupport.getBundleHack();

        initComponents();

        ButtonGroup anchorGroup = new ButtonGroup();

        anchorGroup.add(nwButton);
        anchorGroup.add(nButton);
        anchorGroup.add(neButton);
        anchorGroup.add(wButton);
        anchorGroup.add(cButton);
        anchorGroup.add(eButton);
        anchorGroup.add(swButton);
        anchorGroup.add(sButton);
        anchorGroup.add(seButton);


        // i18n

        ((TitledBorder)anchorPanel.getBorder()).setTitle(bundle.getString("CTL_GBC_anchorPanel"));
        ((TitledBorder)fillPanel.getBorder()).setTitle(bundle.getString("CTL_GBC_fillPanel"));
        ((TitledBorder)ipadPanel.getBorder()).setTitle(bundle.getString("CTL_GBC_ipadPanel"));
        ((TitledBorder)insetsPanel.getBorder()).setTitle(bundle.getString("CTL_GBC_insetsPanel"));
        ((TitledBorder)gridSizePanel.getBorder()).setTitle(bundle.getString("CTL_GBC_gridSizePanel"));


        nwButton.setToolTipText(bundle.getString("CTL_GBC_anchor.NW"));
        nButton.setToolTipText(bundle.getString("CTL_GBC_anchor.N"));
        neButton.setToolTipText(bundle.getString("CTL_GBC_anchor.NE"));
        wButton.setToolTipText(bundle.getString("CTL_GBC_anchor.W"));
        cButton.setToolTipText(bundle.getString("CTL_GBC_anchor.C"));
        eButton.setToolTipText(bundle.getString("CTL_GBC_anchor.E"));
        swButton.setToolTipText(bundle.getString("CTL_GBC_anchor.SW"));
        sButton.setToolTipText(bundle.getString("CTL_GBC_anchor.S"));
        seButton.setToolTipText(bundle.getString("CTL_GBC_anchor.SE"));

        horizontalFillButton.setToolTipText(bundle.getString("CTL_GBC_fill.H"));
        verticalFillButton.setToolTipText(bundle.getString("CTL_GBC_fill.V"));

        ipadHMButton.setToolTipText(bundle.getString("CTL_GBC_ipad.HM"));
        ipadHPButton.setToolTipText(bundle.getString("CTL_GBC_ipad.HP"));
        ipadVMButton.setToolTipText(bundle.getString("CTL_GBC_ipad.VM"));
        ipadVPButton.setToolTipText(bundle.getString("CTL_GBC_ipad.VP"));

        topMButton.setToolTipText(bundle.getString("CTL_GBC_insets.topM"));
        topPButton.setToolTipText(bundle.getString("CTL_GBC_insets.topP"));
        leftPButton.setToolTipText(bundle.getString("CTL_GBC_insets.leftP"));
        leftMButton.setToolTipText(bundle.getString("CTL_GBC_insets.leftM"));
        rightMButton.setToolTipText(bundle.getString("CTL_GBC_insets.rightM"));
        rightPButton.setToolTipText(bundle.getString("CTL_GBC_insets.rightP"));
        bottomMButton.setToolTipText(bundle.getString("CTL_GBC_insets.bottomM"));
        bottomPButton.setToolTipText(bundle.getString("CTL_GBC_insets.bottomP"));
        HMButton.setToolTipText(bundle.getString("CTL_GBC_insets.HM"));
        HPButton.setToolTipText(bundle.getString("CTL_GBC_insets.HP"));
        VMButton.setToolTipText(bundle.getString("CTL_GBC_insets.VM"));
        VPButton.setToolTipText(bundle.getString("CTL_GBC_insets.VP"));
        BMButton.setToolTipText(bundle.getString("CTL_GBC_insets.BM"));
        BPButton.setToolTipText(bundle.getString("CTL_GBC_insets.BP"));

        gridSizeHMButton.setToolTipText(bundle.getString("CTL_GBC_gridSize.HM"));
        gridSizeHPButton.setToolTipText(bundle.getString("CTL_GBC_gridSize.HP"));
        gsRHButton.setToolTipText(bundle.getString("CTL_GBC_gridSize.RH"));
        gridSizeVMButton.setToolTipText(bundle.getString("CTL_GBC_gridSize.VM"));
        gridSizeVPButton.setToolTipText(bundle.getString("CTL_GBC_gridSize.VP"));
        gsRVButton.setToolTipText(bundle.getString("CTL_GBC_gridSize.RV"));

        this.customizer = customizer;
    }

    private void initComponents() {
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;

        actionLsnr = new ActionL ();

        anchorPanel = new javax.swing.JPanel();
        anchorPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints2;
        anchorPanel.setBorder(new javax.swing.border.TitledBorder(
            new javax.swing.border.EtchedBorder(), "anchorPanel")); // NOI18N

        nwButton = new javax.swing.JToggleButton();
        nwButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/nw.gif", false)); // NOI18N
        nwButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        nwButton.setActionCommand("NW"); // NOI18N
        nwButton.addActionListener(actionLsnr);

        gridBagConstraints2 = new java.awt.GridBagConstraints();
        anchorPanel.add(nwButton, gridBagConstraints2);

        nButton = new javax.swing.JToggleButton();
        nButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/n.gif", false)); // NOI18N
        nButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        nButton.setActionCommand("N"); // NOI18N
        nButton.addActionListener(actionLsnr);

        gridBagConstraints2 = new java.awt.GridBagConstraints();
        anchorPanel.add(nButton, gridBagConstraints2);

        neButton = new javax.swing.JToggleButton();
        neButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/ne.gif", false)); // NOI18N
        neButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        neButton.setActionCommand("NE"); // NOI18N
        neButton.addActionListener(actionLsnr);

        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.gridwidth = 0;
        anchorPanel.add(neButton, gridBagConstraints2);

        wButton = new javax.swing.JToggleButton();
        wButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/w.gif", false)); // NOI18N
        wButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        wButton.setActionCommand("W"); // NOI18N
        wButton.addActionListener(actionLsnr);

        gridBagConstraints2 = new java.awt.GridBagConstraints();
        anchorPanel.add(wButton, gridBagConstraints2);

        cButton = new javax.swing.JToggleButton();
        cButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/c.gif", false)); // NOI18N
        cButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        cButton.setActionCommand("C"); // NOI18N
        cButton.addActionListener(actionLsnr);

        gridBagConstraints2 = new java.awt.GridBagConstraints();
        anchorPanel.add(cButton, gridBagConstraints2);

        eButton = new javax.swing.JToggleButton();
        eButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/e.gif", false)); // NOI18N
        eButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        eButton.setActionCommand("E"); // NOI18N
        eButton.addActionListener(actionLsnr);

        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.gridwidth = 0;
        anchorPanel.add(eButton, gridBagConstraints2);

        swButton = new javax.swing.JToggleButton();
        swButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/sw.gif", false)); // NOI18N
        swButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        swButton.setActionCommand("SW"); // NOI18N
        swButton.addActionListener(actionLsnr);

        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.insets = new java.awt.Insets(0, 0, 4, 0);
        anchorPanel.add(swButton, gridBagConstraints2);

        sButton = new javax.swing.JToggleButton();
        sButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/s.gif", false)); // NOI18N
        sButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        sButton.setActionCommand("S"); // NOI18N
        sButton.addActionListener(actionLsnr);

        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.insets = new java.awt.Insets(0, 0, 4, 0);
        anchorPanel.add(sButton, gridBagConstraints2);

        seButton = new javax.swing.JToggleButton();
        seButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/se.gif", false)); // NOI18N
        seButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        seButton.setActionCommand("SE"); // NOI18N
        seButton.addActionListener(actionLsnr);

        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.insets = new java.awt.Insets(0, 0, 4, 0);
        anchorPanel.add(seButton, gridBagConstraints2);


        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add(anchorPanel, gridBagConstraints1);

        fillPanel = new javax.swing.JPanel();
        fillPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints3;
        fillPanel.setBorder(new javax.swing.border.TitledBorder(
            new javax.swing.border.EtchedBorder(), "fillPanel")); // NOI18N

        horizontalFillButton = new javax.swing.JToggleButton();
        horizontalFillButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/horizontal.gif", false)); // NOI18N
        horizontalFillButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        horizontalFillButton.addActionListener(actionLsnr);

        gridBagConstraints3 = new java.awt.GridBagConstraints();
        gridBagConstraints3.insets = new java.awt.Insets(0, 0, 4, 0);
        fillPanel.add(horizontalFillButton, gridBagConstraints3);

        verticalFillButton = new javax.swing.JToggleButton();
        verticalFillButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/vertical.gif", false)); // NOI18N
        verticalFillButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        verticalFillButton.addActionListener(actionLsnr);

        gridBagConstraints3 = new java.awt.GridBagConstraints();
        gridBagConstraints3.insets = new java.awt.Insets(0, 0, 4, 0);
        fillPanel.add(verticalFillButton, gridBagConstraints3);


        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add(fillPanel, gridBagConstraints1);

        ipadPanel = new javax.swing.JPanel();
        ipadPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints4;
        ipadPanel.setBorder(new javax.swing.border.TitledBorder(
            new javax.swing.border.EtchedBorder(), "iPaddingPanel")); // NOI18N

        jLabel1 = new javax.swing.JLabel();
        jLabel1.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/horizontalGr.gif", false)); // NOI18N

        gridBagConstraints4 = new java.awt.GridBagConstraints();
        gridBagConstraints4.insets = new java.awt.Insets(0, 0, 0, 5);
        ipadPanel.add(jLabel1, gridBagConstraints4);

        ipadHMButton = new javax.swing.JButton();
        ipadHMButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/minus.gif", false)); // NOI18N
        ipadHMButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        ipadHMButton.setActionCommand("HM"); // NOI18N
        ipadHMButton.addActionListener(actionLsnr);

        gridBagConstraints4 = new java.awt.GridBagConstraints();
        ipadPanel.add(ipadHMButton, gridBagConstraints4);

        ipadHPButton = new javax.swing.JButton();
        ipadHPButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/plus.gif", false)); // NOI18N
        ipadHPButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        ipadHPButton.setActionCommand("HP"); // NOI18N
        ipadHPButton.addActionListener(actionLsnr);

        gridBagConstraints4 = new java.awt.GridBagConstraints();
        gridBagConstraints4.gridwidth = 0;
        ipadPanel.add(ipadHPButton, gridBagConstraints4);

        jLabel2 = new javax.swing.JLabel();
        jLabel2.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/verticalGr.gif", false)); // NOI18N

        gridBagConstraints4 = new java.awt.GridBagConstraints();
        gridBagConstraints4.insets = new java.awt.Insets(0, 0, 4, 5);
        ipadPanel.add(jLabel2, gridBagConstraints4);

        ipadVMButton = new javax.swing.JButton();
        ipadVMButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/minus.gif", false)); // NOI18N
        ipadVMButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        ipadVMButton.setActionCommand("VM"); // NOI18N
        ipadVMButton.addActionListener(actionLsnr);

        gridBagConstraints4 = new java.awt.GridBagConstraints();
        gridBagConstraints4.insets = new java.awt.Insets(0, 0, 4, 0);
        ipadPanel.add(ipadVMButton, gridBagConstraints4);

        ipadVPButton = new javax.swing.JButton();
        ipadVPButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/plus.gif", false)); // NOI18N
        ipadVPButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        ipadVPButton.setActionCommand("VP"); // NOI18N
        ipadVPButton.addActionListener(actionLsnr);

        gridBagConstraints4 = new java.awt.GridBagConstraints();
        gridBagConstraints4.insets = new java.awt.Insets(0, 0, 4, 0);
        ipadPanel.add(ipadVPButton, gridBagConstraints4);


        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add(ipadPanel, gridBagConstraints1);

        insetsPanel = new javax.swing.JPanel();
        insetsPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints5;
        insetsPanel.setBorder(new javax.swing.border.TitledBorder(
            new javax.swing.border.EtchedBorder(), "insetsPanel")); // NOI18N

        topMButton = new javax.swing.JButton();
        topMButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/minus.gif", false)); // NOI18N
        topMButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        topMButton.setActionCommand("tM"); // NOI18N
        topMButton.addActionListener(actionLsnr);

        gridBagConstraints5 = new java.awt.GridBagConstraints();
        gridBagConstraints5.gridx = 1;
        gridBagConstraints5.gridy = 0;
        gridBagConstraints5.insets = new java.awt.Insets(0, 0, 8, 0);
        gridBagConstraints5.anchor = java.awt.GridBagConstraints.NORTH;
        insetsPanel.add(topMButton, gridBagConstraints5);

        topPButton = new javax.swing.JButton();
        topPButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/plus.gif", false)); // NOI18N
        topPButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        topPButton.setActionCommand("tP"); // NOI18N
        topPButton.addActionListener(actionLsnr);

        gridBagConstraints5 = new java.awt.GridBagConstraints();
        gridBagConstraints5.gridx = 2;
        gridBagConstraints5.gridy = 0;
        gridBagConstraints5.insets = new java.awt.Insets(0, 0, 8, 0);
        gridBagConstraints5.anchor = java.awt.GridBagConstraints.NORTH;
        insetsPanel.add(topPButton, gridBagConstraints5);

        leftPButton = new javax.swing.JButton();
        leftPButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/plus.gif", false)); // NOI18N
        leftPButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        leftPButton.setActionCommand("lP"); // NOI18N
        leftPButton.addActionListener(actionLsnr);

        gridBagConstraints5 = new java.awt.GridBagConstraints();
        gridBagConstraints5.gridx = 0;
        gridBagConstraints5.gridy = 0;
        gridBagConstraints5.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        insetsPanel.add(leftPButton, gridBagConstraints5);

        leftMButton = new javax.swing.JButton();
        leftMButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/minus.gif", false)); // NOI18N
        leftMButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        leftMButton.setActionCommand("lM"); // NOI18N
        leftMButton.addActionListener(actionLsnr);

        gridBagConstraints5 = new java.awt.GridBagConstraints();
        gridBagConstraints5.gridx = 0;
        gridBagConstraints5.gridy = 1;
        gridBagConstraints5.anchor = java.awt.GridBagConstraints.NORTHEAST;
        insetsPanel.add(leftMButton, gridBagConstraints5);

        rightPButton = new javax.swing.JButton();
        rightPButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/plus.gif", false)); // NOI18N
        rightPButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        rightPButton.setActionCommand("rP"); // NOI18N
        rightPButton.addActionListener(actionLsnr);

        gridBagConstraints5 = new java.awt.GridBagConstraints();
        gridBagConstraints5.gridx = 3;
        gridBagConstraints5.gridy = 0;
        gridBagConstraints5.anchor = java.awt.GridBagConstraints.SOUTH;
        insetsPanel.add(rightPButton, gridBagConstraints5);

        rightMButton = new javax.swing.JButton();
        rightMButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/minus.gif", false)); // NOI18N
        rightMButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        rightMButton.setActionCommand("rM"); // NOI18N
        rightMButton.addActionListener(actionLsnr);

        gridBagConstraints5 = new java.awt.GridBagConstraints();
        gridBagConstraints5.gridx = 3;
        gridBagConstraints5.gridy = 1;
        gridBagConstraints5.anchor = java.awt.GridBagConstraints.NORTH;
        insetsPanel.add(rightMButton, gridBagConstraints5);

        bottomMButton = new javax.swing.JButton();
        bottomMButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/minus.gif", false)); // NOI18N
        bottomMButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        bottomMButton.setActionCommand("bM"); // NOI18N
        bottomMButton.addActionListener(actionLsnr);

        gridBagConstraints5 = new java.awt.GridBagConstraints();
        gridBagConstraints5.gridx = 1;
        gridBagConstraints5.gridy = 1;
        gridBagConstraints5.insets = new java.awt.Insets(8, 0, 0, 0);
        insetsPanel.add(bottomMButton, gridBagConstraints5);

        bottomPButton = new javax.swing.JButton();
        bottomPButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/plus.gif", false)); // NOI18N
        bottomPButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        bottomPButton.setActionCommand("bP"); // NOI18N
        bottomPButton.addActionListener(actionLsnr);

        gridBagConstraints5 = new java.awt.GridBagConstraints();
        gridBagConstraints5.gridx = 2;
        gridBagConstraints5.gridy = 1;
        gridBagConstraints5.insets = new java.awt.Insets(8, 0, 0, 0);
        insetsPanel.add(bottomPButton, gridBagConstraints5);

        jLabel3 = new javax.swing.JLabel();
        jLabel3.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/horizontalGr.gif", false)); // NOI18N

        gridBagConstraints5 = new java.awt.GridBagConstraints();
        gridBagConstraints5.gridx = 0;
        gridBagConstraints5.gridy = 2;
        gridBagConstraints5.insets = new java.awt.Insets(8, 0, 0, 5);
        insetsPanel.add(jLabel3, gridBagConstraints5);

        HMButton = new javax.swing.JButton();
        HMButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/minus.gif", false)); // NOI18N
        HMButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        HMButton.setActionCommand("HM"); // NOI18N
        HMButton.addActionListener(actionLsnr);

        gridBagConstraints5 = new java.awt.GridBagConstraints();
        gridBagConstraints5.gridx = 1;
        gridBagConstraints5.gridy = 2;
        gridBagConstraints5.insets = new java.awt.Insets(8, 0, 0, 0);
        insetsPanel.add(HMButton, gridBagConstraints5);

        HPButton = new javax.swing.JButton();
        HPButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/plus.gif", false)); // NOI18N
        HPButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        HPButton.setActionCommand("HP"); // NOI18N
        HPButton.addActionListener(actionLsnr);

        gridBagConstraints5 = new java.awt.GridBagConstraints();
        gridBagConstraints5.gridx = 2;
        gridBagConstraints5.gridy = 2;
        gridBagConstraints5.insets = new java.awt.Insets(8, 0, 0, 0);
        insetsPanel.add(HPButton, gridBagConstraints5);

        jLabel4 = new javax.swing.JLabel();
        jLabel4.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/verticalGr.gif", false)); // NOI18N

        gridBagConstraints5 = new java.awt.GridBagConstraints();
        gridBagConstraints5.gridx = 0;
        gridBagConstraints5.gridy = 3;
        gridBagConstraints5.insets = new java.awt.Insets(0, 0, 0, 5);
        insetsPanel.add(jLabel4, gridBagConstraints5);

        VMButton = new javax.swing.JButton();
        VMButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/minus.gif", false)); // NOI18N
        VMButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        VMButton.setActionCommand("VM"); // NOI18N
        VMButton.addActionListener(actionLsnr);

        gridBagConstraints5 = new java.awt.GridBagConstraints();
        gridBagConstraints5.gridx = 1;
        gridBagConstraints5.gridy = 3;
        insetsPanel.add(VMButton, gridBagConstraints5);

        VPButton = new javax.swing.JButton();
        VPButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/plus.gif", false)); // NOI18N
        VPButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        VPButton.setActionCommand("VP"); // NOI18N
        VPButton.addActionListener(actionLsnr);

        gridBagConstraints5 = new java.awt.GridBagConstraints();
        gridBagConstraints5.gridx = 2;
        gridBagConstraints5.gridy = 3;
        insetsPanel.add(VPButton, gridBagConstraints5);

        jLabel5 = new javax.swing.JLabel();
        jLabel5.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/bothGr.gif", false)); // NOI18N

        gridBagConstraints5 = new java.awt.GridBagConstraints();
        gridBagConstraints5.gridx = 0;
        gridBagConstraints5.gridy = 4;
        gridBagConstraints5.insets = new java.awt.Insets(0, 0, 4, 5);
        insetsPanel.add(jLabel5, gridBagConstraints5);

        BMButton = new javax.swing.JButton();
        BMButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/minus.gif", false)); // NOI18N
        BMButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        BMButton.setActionCommand("BM"); // NOI18N
        BMButton.addActionListener(actionLsnr);

        gridBagConstraints5 = new java.awt.GridBagConstraints();
        gridBagConstraints5.gridx = 1;
        gridBagConstraints5.gridy = 4;
        gridBagConstraints5.insets = new java.awt.Insets(0, 0, 4, 0);
        insetsPanel.add(BMButton, gridBagConstraints5);

        BPButton = new javax.swing.JButton();
        BPButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/plus.gif", false)); // NOI18N
        BPButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        BPButton.setActionCommand("BP"); // NOI18N
        BPButton.addActionListener(actionLsnr);

        gridBagConstraints5 = new java.awt.GridBagConstraints();
        gridBagConstraints5.gridx = 2;
        gridBagConstraints5.gridy = 4;
        gridBagConstraints5.insets = new java.awt.Insets(0, 0, 4, 0);
        insetsPanel.add(BPButton, gridBagConstraints5);


        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridheight = 2;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add(insetsPanel, gridBagConstraints1);

        gridSizePanel = new javax.swing.JPanel();
        gridSizePanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints6;
        gridSizePanel.setBorder(new javax.swing.border.TitledBorder(
            new javax.swing.border.EtchedBorder(), "panelSizePanel")); // NOI18N

        jLabel6 = new javax.swing.JLabel();
        jLabel6.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/horizontalGr.gif", false)); // NOI18N

        gridBagConstraints6 = new java.awt.GridBagConstraints();
        gridBagConstraints6.insets = new java.awt.Insets(0, 0, 0, 5);
        gridSizePanel.add(jLabel6, gridBagConstraints6);

        gridSizeHMButton = new javax.swing.JButton();
        gridSizeHMButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/minus.gif", false)); // NOI18N
        gridSizeHMButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        gridSizeHMButton.setActionCommand("HM"); // NOI18N
        gridSizeHMButton.addActionListener(actionLsnr);

        gridBagConstraints6 = new java.awt.GridBagConstraints();
        gridSizePanel.add(gridSizeHMButton, gridBagConstraints6);

        gridSizeHPButton = new javax.swing.JButton();
        gridSizeHPButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/plus.gif", false)); // NOI18N
        gridSizeHPButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        gridSizeHPButton.setActionCommand("HP"); // NOI18N
        gridSizeHPButton.addActionListener(actionLsnr);

        gridBagConstraints6 = new java.awt.GridBagConstraints();
        gridSizePanel.add(gridSizeHPButton, gridBagConstraints6);

        gsRHButton = new javax.swing.JToggleButton();
        gsRHButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/remainder.gif", false)); // NOI18N
        gsRHButton.setMargin(new java.awt.Insets(5, 5, 5, 5));
        gsRHButton.setActionCommand("HR"); // NOI18N
        gsRHButton.addActionListener(actionLsnr);

        gridBagConstraints6 = new java.awt.GridBagConstraints();
        gridBagConstraints6.gridwidth = 0;
        gridSizePanel.add(gsRHButton, gridBagConstraints6);

        jLabel8 = new javax.swing.JLabel();
        jLabel8.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/verticalGr.gif", false)); // NOI18N

        gridBagConstraints6 = new java.awt.GridBagConstraints();
        gridBagConstraints6.insets = new java.awt.Insets(0, 0, 4, 5);
        gridSizePanel.add(jLabel8, gridBagConstraints6);

        gridSizeVMButton = new javax.swing.JButton();
        gridSizeVMButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/minus.gif", false)); // NOI18N
        gridSizeVMButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        gridSizeVMButton.setActionCommand("VM"); // NOI18N
        gridSizeVMButton.addActionListener(actionLsnr);

        gridBagConstraints6 = new java.awt.GridBagConstraints();
        gridBagConstraints6.insets = new java.awt.Insets(0, 0, 4, 0);
        gridSizePanel.add(gridSizeVMButton, gridBagConstraints6);

        gridSizeVPButton = new javax.swing.JButton();
        gridSizeVPButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/plus.gif", false)); // NOI18N
        gridSizeVPButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        gridSizeVPButton.setActionCommand("VP"); // NOI18N
        gridSizeVPButton.addActionListener(actionLsnr);

        gridBagConstraints6 = new java.awt.GridBagConstraints();
        gridBagConstraints6.insets = new java.awt.Insets(0, 0, 4, 0);
        gridSizePanel.add(gridSizeVPButton, gridBagConstraints6);

        gsRVButton = new javax.swing.JToggleButton();
        gsRVButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/resources/remainder.gif", false)); // NOI18N
        gsRVButton.setMargin(new java.awt.Insets(5, 5, 5, 5));
        gsRVButton.setActionCommand("VR"); // NOI18N
        gsRVButton.addActionListener(actionLsnr);

        gridBagConstraints6 = new java.awt.GridBagConstraints();
        gridBagConstraints6.gridwidth = 0;
        gridBagConstraints6.insets = new java.awt.Insets(0, 0, 4, 0);
        gridSizePanel.add(gsRVButton, gridBagConstraints6);


        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add(gridSizePanel, gridBagConstraints1);

    }

    private void gridSizeButtonAction(java.awt.event.ActionEvent evt) {
        int what = evt.getActionCommand().charAt(0) == 'H' ? GridBagCustomizer.HORIZONTAL : GridBagCustomizer.VERTICAL;
        int action = 0;

        switch (evt.getActionCommand().charAt(1)) {
            case 'P':
                action = GridBagCustomizer.PLUS;
                break;
            case 'M':
                action = GridBagCustomizer.MINUS;
                break;
            case 'R':
                action = 0;
                break;
            default:
                return;
        }
        customizer.modifyGridSize(action, what);
    }

    private void insetsButtonAction(java.awt.event.ActionEvent evt) {
        int action = evt.getActionCommand().charAt(1) == 'P' ? GridBagCustomizer.PLUS : GridBagCustomizer.MINUS;
        int what = 0;

        switch (evt.getActionCommand().charAt(0)) {
            case 't':
                what = GridBagCustomizer.TOP;
                break;
            case 'l':
                what = GridBagCustomizer.LEFT;
                break;
            case 'b':
                what = GridBagCustomizer.BOTTOM;
                break;
            case 'r':
                what = GridBagCustomizer.RIGHT;
                break;
            case 'H':
                what = GridBagCustomizer.HORIZONTAL;
                break;
            case 'V':
                what = GridBagCustomizer.VERTICAL;
                break;
            case 'B':
                what = GridBagCustomizer.HORIZONTAL + GridBagCustomizer.VERTICAL;
                break;
        }
        customizer.modifyInsets(action, what);
    }

    private void ipadButtonAction(java.awt.event.ActionEvent evt) {
        if (evt.getActionCommand().equals("HM")) // NOI18N
            customizer.modifyIPad(GridBagCustomizer.MINUS, GridBagCustomizer.HORIZONTAL);
        else if (evt.getActionCommand().equals("HP")) // NOI18N
            customizer.modifyIPad(GridBagCustomizer.PLUS, GridBagCustomizer.HORIZONTAL);
        else if (evt.getActionCommand().equals("VM")) // NOI18N
            customizer.modifyIPad(GridBagCustomizer.MINUS, GridBagCustomizer.VERTICAL);
        else if (evt.getActionCommand().equals("VP")) // NOI18N
            customizer.modifyIPad(GridBagCustomizer.PLUS, GridBagCustomizer.VERTICAL);
    }

    private void fillButtonAction(java.awt.event.ActionEvent evt) {
        if (horizontalFillButton.isSelected() && verticalFillButton.isSelected())
            customizer.setFill(java.awt.GridBagConstraints.BOTH);
        else if (horizontalFillButton.isSelected())
            customizer.setFill(java.awt.GridBagConstraints.HORIZONTAL);
        else if (verticalFillButton.isSelected())
            customizer.setFill(java.awt.GridBagConstraints.VERTICAL);
        else
            customizer.setFill(java.awt.GridBagConstraints.NONE);
    }

    private void anchorButtonAction(java.awt.event.ActionEvent evt) {
        String command = evt.getActionCommand();

        if (command.equals("NW")) // NOI18N
            customizer.setAnchor(java.awt.GridBagConstraints.NORTHWEST);
        else if (command.equals("N")) // NOI18N
            customizer.setAnchor(java.awt.GridBagConstraints.NORTH);
        else if (command.equals("NE")) // NOI18N
            customizer.setAnchor(java.awt.GridBagConstraints.NORTHEAST);
        else if (command.equals("W")) // NOI18N
            customizer.setAnchor(java.awt.GridBagConstraints.WEST);
        else if (command.equals("C")) // NOI18N
            customizer.setAnchor(java.awt.GridBagConstraints.CENTER);
        else if (command.equals("E")) // NOI18N
            customizer.setAnchor(java.awt.GridBagConstraints.EAST);
        else if (command.equals("SW")) // NOI18N
            customizer.setAnchor(java.awt.GridBagConstraints.SOUTHWEST);
        else if (command.equals("S")) // NOI18N
            customizer.setAnchor(java.awt.GridBagConstraints.SOUTH);
        else if (command.equals("SE")) // NOI18N
            customizer.setAnchor(java.awt.GridBagConstraints.SOUTHEAST);
    }


    private javax.swing.JPanel anchorPanel;
    private javax.swing.JToggleButton nwButton;
    private javax.swing.JToggleButton nButton;
    private javax.swing.JToggleButton neButton;
    private javax.swing.JToggleButton wButton;
    private javax.swing.JToggleButton cButton;
    private javax.swing.JToggleButton eButton;
    private javax.swing.JToggleButton swButton;
    private javax.swing.JToggleButton sButton;
    private javax.swing.JToggleButton seButton;
    private javax.swing.JPanel fillPanel;
    private javax.swing.JToggleButton horizontalFillButton;
    private javax.swing.JToggleButton verticalFillButton;
    private javax.swing.JPanel ipadPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton ipadHMButton;
    private javax.swing.JButton ipadHPButton;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JButton ipadVMButton;
    private javax.swing.JButton ipadVPButton;
    private javax.swing.JPanel insetsPanel;
    private javax.swing.JButton topMButton;
    private javax.swing.JButton topPButton;
    private javax.swing.JButton leftPButton;
    private javax.swing.JButton leftMButton;
    private javax.swing.JButton rightPButton;
    private javax.swing.JButton rightMButton;
    private javax.swing.JButton bottomMButton;
    private javax.swing.JButton bottomPButton;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JButton HMButton;
    private javax.swing.JButton HPButton;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JButton VMButton;
    private javax.swing.JButton VPButton;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JButton BMButton;
    private javax.swing.JButton BPButton;
    private javax.swing.JPanel gridSizePanel;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JButton gridSizeHMButton;
    private javax.swing.JButton gridSizeHPButton;
    private javax.swing.JToggleButton gsRHButton;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JButton gridSizeVMButton;
    private javax.swing.JButton gridSizeVPButton;
    private javax.swing.JToggleButton gsRVButton;


    void newSelection(java.util.List proxies) {

        int anchor = -1;
        boolean noAnchor = false;
        int fill = -1;
        boolean noFill = false;

        boolean remainderH = true;
        boolean remainderV = true;

        java.util.Iterator it = proxies.iterator();


        for (int i = 0; it.hasNext(); i++) {
            GridBagCustomizer.GBComponentProxy p =(GridBagCustomizer.GBComponentProxy) it.next();
            java.awt.GridBagConstraints con = p.getRealConstraints();

            if (i == 0) {
                anchor = con.anchor;
                fill = con.fill;
            }
            else {
                if (con.anchor != anchor)
                    noAnchor = true;
                if (con.fill != fill)
                    noFill = true;
            }

            if (con.gridwidth != java.awt.GridBagConstraints.REMAINDER)
                remainderH = false;

            if (con.gridheight != java.awt.GridBagConstraints.REMAINDER)
                remainderV = false;

        }

        // Set anchor buttons

        nwButton.setSelected((!noAnchor) && anchor == java.awt.GridBagConstraints.NORTHWEST);
        nButton.setSelected((!noAnchor) && anchor == java.awt.GridBagConstraints.NORTH);
        neButton.setSelected((!noAnchor) && anchor == java.awt.GridBagConstraints.NORTHEAST);
        wButton.setSelected((!noAnchor) && anchor == java.awt.GridBagConstraints.WEST);
        cButton.setSelected((!noAnchor) && anchor == java.awt.GridBagConstraints.CENTER);
        eButton.setSelected((!noAnchor) && anchor == java.awt.GridBagConstraints.EAST);
        swButton.setSelected((!noAnchor) && anchor == java.awt.GridBagConstraints.SOUTHWEST);
        sButton.setSelected((!noAnchor) && anchor == java.awt.GridBagConstraints.SOUTH);
        seButton.setSelected((!noAnchor) && anchor == java.awt.GridBagConstraints.SOUTHEAST);

        // Set fill buttons
        horizontalFillButton.setSelected(!noFill &&
                                         (fill == java.awt.GridBagConstraints.HORIZONTAL || fill == java.awt.GridBagConstraints.BOTH));

        verticalFillButton.setSelected(!noFill &&
                                       (fill == java.awt.GridBagConstraints.VERTICAL || fill == java.awt.GridBagConstraints.BOTH));

        // Set remainder buttons
        gsRHButton.setSelected(remainderH);
        gsRVButton.setSelected(remainderV);
    }

    private class ActionL implements java.awt.event.ActionListener {
        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            Object src = evt.getSource();
	    if (nwButton.equals (src)
		|| nButton.equals (src)
		|| neButton.equals (src)
		|| wButton.equals (src)
		|| cButton.equals (src)
		|| eButton.equals (src)
		|| swButton.equals (src)
		|| sButton.equals (src)
		|| seButton.equals (src)
		) {
                anchorButtonAction(evt);
	    }
            else if (horizontalFillButton.equals(src)
		     || verticalFillButton.equals(src)
		     )
	    {
                fillButtonAction(evt);
            }
            else if (ipadHMButton.equals(src)
		     || ipadHPButton.equals(src)
		     || ipadVMButton.equals(src)
		     || ipadVPButton.equals(src)
		     )
	    {
                ipadButtonAction(evt);
            }
            else if (BMButton.equals(src)
		|| BPButton.equals(src)
		|| HMButton.equals(src)
		|| HPButton.equals(src)
		|| VMButton.equals(src)
		|| VPButton.equals(src)
		|| topPButton.equals(src)
		|| topMButton.equals(src)
		|| leftPButton.equals(src)
		|| leftMButton.equals(src)
		|| rightPButton.equals(src)
		|| rightMButton.equals(src)
		|| bottomPButton.equals(src)
		|| bottomMButton.equals(src)
		)
	    {
                insetsButtonAction(evt);
            }
            else if (gridSizeHMButton.equals(src)
		     || gridSizeHPButton.equals(src)
		     || gsRHButton.equals(src)
		     || gridSizeVMButton.equals(src)
		     || gridSizeVPButton.equals(src)
		     || gsRVButton.equals(src)
		     )
	    {
                gridSizeButtonAction(evt);
            }
        }
    }
}
