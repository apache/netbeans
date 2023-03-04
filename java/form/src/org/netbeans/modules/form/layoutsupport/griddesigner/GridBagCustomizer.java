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

package org.netbeans.modules.form.layoutsupport.griddesigner;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.Insets;
import javax.swing.AbstractButton;
import java.util.ResourceBundle;
import java.util.Set;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.AbstractGridAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.GridActionPerformer;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.GridBoundsChange;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Stola
 * @author Petr Somol
 */
public class GridBagCustomizer implements GridCustomizer {

    private GridActionPerformer performer;
    private GridBagManager manager;

    public GridBagCustomizer(GridBagManager manager, GridActionPerformer performer) {
        this.manager = manager;
        this.performer = performer;
        initComponents();
        bidiCenter = true; // default to promote bidirectional anchors over absolute ones
    }
    
    @Override
    public Component getComponent() {
        return customizer;
    }

    /**
     * Step size in component size change
     */
    public static final int STANDARD_SIZE_CHANGE = 1;

    /**
     * Step size in component size change (with Ctrl key pressed)
     */
    public static final int ACCELERATED_SIZE_CHANGE = 5;

    /**
     * Step size in component weight change (with Ctrl key pressed)
     */
    public static final double STANDARD_WEIGHT_CHANGE = 0.1d;

    /**
     * Step size in component weight change (with Ctrl key pressed)
     */
    public static final double ACCELERATED_WEIGHT_CHANGE = 0.5d;

    /**
     * Maximum number of decimal places to be preserved in weights; 10^k here means k decimal places
     */
    public static final double WEIGHT_NUMERIC_PRECISION = 100.0d;

    /**
     * Marker to make parameter passing more readable
     */
    public static final byte NO_INDIRECT_CHANGE = 0;

    /**
     * Marker to make parameter passing more readable
     */
    public static final byte CHANGE_RELATIVE = 1;

    /**
     * Marker to make parameter passing more readable
     */
    public static final byte CHANGE_REMAINDER = 2;

    /**
     * Determines that weights will be equalized across current selection
     */
    public static final byte WEIGHTS_EQUALIZE = 4;

    /**
     * Determines that anchor buttons represent absolute positioning using {@code GridBagConstraints} constants:
     * <code>CENTER</code>, <code>NORTH</code>, <code>NORTHEAST</code>,
     * <code>EAST</code>, <code>SOUTHEAST</code>, <code>SOUTH</code>,
     * <code>SOUTHWEST</code>, <code>WEST</code>, and <code>NORTHWEST</code>.
     */
    public static final int ANCHOR_ABSOLUTE = 1;

    /**
     * Determines that anchor buttons represent bi-directional-aware positioning using {@code GridBagConstraints} constants
     * <code>PAGE_END</code>,
     * <code>LINE_START</code>, <code>LINE_END</code>, 
     * <code>FIRST_LINE_START</code>, <code>FIRST_LINE_END</code>, 
     * <code>LAST_LINE_START</code> and <code>LAST_LINE_END</code>.  The
     */
    public static final int ANCHOR_BIDI = 2;

    /**
     * Determines that anchor buttons represent baseline-relative positioning using {@code GridBagConstraints} constants
     * <code>BASELINE</code>, <code>BASELINE_LEADING</code>,
     * <code>BASELINE_TRAILING</code>,
     * <code>ABOVE_BASELINE</code>, <code>ABOVE_BASELINE_LEADING</code>,
     * <code>ABOVE_BASELINE_TRAILING</code>,
     * <code>BELOW_BASELINE</code>, <code>BELOW_BASELINE_LEADING</code>,
     * and <code>BELOW_BASELINE_TRAILING</code>.
     */
    public static final int ANCHOR_BASELINE = 4;
    
    /**
     * bidiCenter remembers the last used anchor type;
     * this is a workaround to deal with the ambiguous meaning
     * of {@code GridBagConstraints.CENTER} which is not sufficient
     * to distinguish among ANCHOR_ABSOLUTE and ANCHOR_BIDI anchor types
     */
    private boolean bidiCenter;

    /** 
     * Parameter passing structure 
     */
    private class PaddingChange {
        PaddingChange(int xDiff, int yDiff, boolean reset) {
            this.xDiff = xDiff;
            this.yDiff = yDiff;
            this.reset = reset;
        }
        public final int xDiff; /** horizontal padding increase or decrease (0 = no change) */
        public final int yDiff; /** vertical padding increase or decrease (0 = no change) */
        public final boolean reset; /** changes meaning of preceeding diff values: nonzero = reset request, 0 = no change */
        private PaddingChange() {xDiff = yDiff = 0; reset = false;}
    }

    /** 
     * Parameter passing structure 
     */
    private class GridSizeChange {
        GridSizeChange(int wDiff, byte wRelRem, int hDiff, byte hRelRem, boolean reset) {
            this.wDiff = wDiff;
            this.wRelRem = wRelRem;
            this.hDiff = hDiff;
            this.hRelRem = hRelRem;
            this.reset = reset;
        }
        public final int wDiff; /** absolute width in grid, increase or decrease (0 = no change) */
        public final byte wRelRem; /** relative width, CHANGE_RELATIVE = set RELATIVE, -CHANGE_RELATIVE = unset RELATIVE, NO_INDIRECT_CHANGE = keep unchanged, CHANGE_REMAINDER = set REMAINDER, -CHANGE_REMAINDER = unset REMAINDER */
        public final int hDiff; /** absolute height in grid, increase or decrease (0 = no change) */
        public final byte hRelRem; /** relative height, CHANGE_RELATIVE = set RELATIVE, -CHANGE_RELATIVE = unset RELATIVE, NO_INDIRECT_CHANGE = keep unchanged, CHANGE_REMAINDER = set REMAINDER, -CHANGE_REMAINDER = unset REMAINDER */
        public final boolean reset; /** changes meaning of preceeding diff values: nonzero = reset request, 0 = no change */
        private GridSizeChange() {wDiff = hDiff = wRelRem = hRelRem = 0; reset = false;}
    }

    /** 
     * Parameter passing structure 
     */
    private class GridPositionChange {
        GridPositionChange(int xDiff, byte xRelative, int yDiff, byte yRelative, boolean reset) {
            this.xDiff = xDiff;
            this.xRelative = xRelative;
            this.yDiff = yDiff;
            this.yRelative = yRelative;
            this.reset = reset;
        }
        public final int xDiff; /** absolute horizontal position in grid increase or decrease (0 = no change) */
        public final byte xRelative; /** relative horizontal position, CHANGE_RELATIVE = set RELATIVE, -CHANGE_RELATIVE = unset RELATIVE, NO_INDIRECT_CHANGE = keep unchanged */
        public final int yDiff; /** absolute vertical position in grid increase or decrease (0 = no change) */
        public final byte yRelative; /** relative vertical position, CHANGE_RELATIVE = set RELATIVE, -CHANGE_RELATIVE = unset RELATIVE, NO_INDIRECT_CHANGE = keep unchanged */
        public final boolean reset; /** changes meaning of preceeding diff values: nonzero = reset request, 0 = no change */
        private GridPositionChange() {xDiff = yDiff = 0; xRelative = yRelative = 0; reset = false;}
    }

    /** 
     * Parameter passing structure 
     */
    private class InsetsChange {
        InsetsChange(int top, int left, int bottom, int right, boolean reset) {
            this.iDiff = new Insets(top,left,bottom,right);
            this.reset = reset;
        }
        public final Insets iDiff; /** absolute inset increase or decrease (0 = no change) */
        public final boolean reset; /** changes meaning of preceeding diff values: nonzero = reset request, 0 = no change */
        private InsetsChange() {iDiff = new Insets(0,0,0,0); reset = false;}
    }

    /** 
     * Parameter passing structure 
     */
    private class FillChange {
        FillChange(int hFill, int vFill) {
            this.hFill = hFill;
            this.vFill = vFill;
        }
        public final int hFill; /** -1 = no change, 0 = NONE/VERTICAL, 1 = HORIZONTAL/BOTH */
        public final int vFill; /** -1 = no change, 0 = NONE/HORIZONTAL, 1= VERTICAL/BOTH */
        private FillChange() {hFill = vFill = -1;}
    }

    /** 
     * Parameter passing structure 
     */
    private class WeightChange {
        WeightChange(double xDiff, byte xNorm, double yDiff, byte yNorm, boolean reset) {
            this.xDiff = xDiff;
            this.xNorm = xNorm;
            this.yDiff = yDiff;
            this.yNorm = yNorm;
            this.reset = reset;
        }
        public final double xDiff; /** horizontal weight increase or decrease (0 = no change) */
        public final byte xNorm; /**  NO_INDIRECT_CHANGE, or WEIGHTS_EQUALIZE */
        public final double yDiff; /** vertical weight increase or decrease (0 = no change) */
        public final byte yNorm; /**  NO_INDIRECT_CHANGE, or WEIGHTS_EQUALIZE */
        public final boolean reset; /** changes meaning of preceeding diff values: nonzero = reset request, 0 = no change */
        private WeightChange() {xDiff = yDiff = 0.0d; xNorm = yNorm = 0; reset = false;}
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        customizer = new javax.swing.JPanel();
        leftPanel = new javax.swing.JPanel();
        anchorToolGroup = new javax.swing.JPanel();
        anchorPanel = new javax.swing.JPanel();
        cAnchorButton = new javax.swing.JToggleButton();
        nAnchorButton = new javax.swing.JToggleButton();
        nwAnchorButton = new javax.swing.JToggleButton();
        eAnchorButton = new javax.swing.JToggleButton();
        neAnchorButton = new javax.swing.JToggleButton();
        sAnchorButton = new javax.swing.JToggleButton();
        seAnchorButton = new javax.swing.JToggleButton();
        wAnchorButton = new javax.swing.JToggleButton();
        swAnchorButton = new javax.swing.JToggleButton();
        anchorTypePanel = new javax.swing.JPanel();
        baselineAnchorButton = new javax.swing.JToggleButton();
        bidiAnchorButton = new javax.swing.JToggleButton();
        paddingToolGroup = new javax.swing.JPanel();
        paddingPanel = new javax.swing.JPanel();
        vMinusPaddingButton = new javax.swing.JButton();
        vPadLabel = new javax.swing.JLabel();
        vPlusPaddingButton = new javax.swing.JButton();
        hPlusPaddingButton = new javax.swing.JButton();
        hPadLabel = new javax.swing.JLabel();
        hMinusPaddingButton = new javax.swing.JButton();
        bMinusPaddingButton = new javax.swing.JButton();
        bPadLabel = new javax.swing.JLabel();
        bPlusPaddingButton = new javax.swing.JButton();
        gridSizeToolGroup = new javax.swing.JPanel();
        gridSizePanel = new javax.swing.JPanel();
        vGridRelativeButton = new javax.swing.JToggleButton();
        vGridRemainderButton = new javax.swing.JToggleButton();
        vGridMinusButton = new javax.swing.JButton();
        vGridPlusButton = new javax.swing.JButton();
        vGridLabel = new javax.swing.JLabel();
        hGridMinusButton = new javax.swing.JButton();
        hGridPlusButton = new javax.swing.JButton();
        hGridRelativeButton = new javax.swing.JToggleButton();
        hGridRemainderButton = new javax.swing.JToggleButton();
        hGridLabel = new javax.swing.JLabel();
        gridPositionToolGroup = new javax.swing.JPanel();
        gridPositionPanel = new javax.swing.JPanel();
        xGridMinusButton = new javax.swing.JButton();
        yGridMinusButton = new javax.swing.JButton();
        yGridPlusButton = new javax.swing.JButton();
        xGridPlusButton = new javax.swing.JButton();
        xGridRelativeButton = new javax.swing.JToggleButton();
        yGridRelativeButton = new javax.swing.JToggleButton();
        rightPanel = new javax.swing.JPanel();
        insetsToolGroup = new javax.swing.JPanel();
        insetsPanel = new javax.swing.JPanel();
        vInsetLabel = new javax.swing.JLabel();
        vMinusInsetButton = new javax.swing.JButton();
        vPlusInsetButton = new javax.swing.JButton();
        hInsetLabel = new javax.swing.JLabel();
        hMinusInsetButton = new javax.swing.JButton();
        hPlusInsetButton = new javax.swing.JButton();
        bInsetLabel = new javax.swing.JLabel();
        bMinusInsetButton = new javax.swing.JButton();
        bPlusInsetButton = new javax.swing.JButton();
        insetsCross = new javax.swing.JPanel();
        topLeftCorner = new javax.swing.JLabel();
        vPlusTopInsetButton = new javax.swing.JButton();
        vMinusTopInsetButton = new javax.swing.JButton();
        topRightCorner = new javax.swing.JLabel();
        hPlusLeftInsetButton = new javax.swing.JButton();
        hMinusLeftInsetButton = new javax.swing.JButton();
        hMinusRightInsetButton = new javax.swing.JButton();
        hPlusRightInsetButton = new javax.swing.JButton();
        bottomLeftCorner = new javax.swing.JLabel();
        vMinusBottomInsetButton = new javax.swing.JButton();
        vPlusBottomInsetButton = new javax.swing.JButton();
        bottomRightCorner = new javax.swing.JLabel();
        crossCenter = new javax.swing.Box.Filler(new java.awt.Dimension(20, 20), new java.awt.Dimension(20, 20), new java.awt.Dimension(20, 20));
        fillToolGroup = new javax.swing.JPanel();
        fillPanel = new javax.swing.JPanel();
        hFillButton = new javax.swing.JToggleButton();
        vFillButton = new javax.swing.JToggleButton();
        weightsToolGroup = new javax.swing.JPanel();
        weightsPanel = new javax.swing.JPanel();
        vWeightLabel = new javax.swing.JLabel();
        vMinusWeightButton = new javax.swing.JButton();
        vPlusWeightButton = new javax.swing.JButton();
        hWeightLabel = new javax.swing.JLabel();
        hMinusWeightButton = new javax.swing.JButton();
        hPlusWeightButton = new javax.swing.JButton();
        hWeightEqualizeButton = new javax.swing.JButton();
        vWeightEqualizeButton = new javax.swing.JButton();

        FormListener formListener = new FormListener();

        anchorToolGroup.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.anchorToolGroup.border.title"))); // NOI18N

        anchorPanel.setOpaque(false);

        cAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/anchor_c.png"))); // NOI18N
        cAnchorButton.setEnabled(false);
        cAnchorButton.setFocusPainted(false);
        cAnchorButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        cAnchorButton.setPreferredSize(new java.awt.Dimension(22, 22));
        cAnchorButton.addActionListener(formListener);

        nAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/anchor_n.png"))); // NOI18N
        nAnchorButton.setEnabled(false);
        nAnchorButton.setFocusPainted(false);
        nAnchorButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        nAnchorButton.setPreferredSize(new java.awt.Dimension(22, 22));
        nAnchorButton.addActionListener(formListener);

        nwAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/anchor_nw.png"))); // NOI18N
        nwAnchorButton.setEnabled(false);
        nwAnchorButton.setFocusPainted(false);
        nwAnchorButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        nwAnchorButton.setPreferredSize(new java.awt.Dimension(22, 22));
        nwAnchorButton.addActionListener(formListener);

        eAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/anchor_e.png"))); // NOI18N
        eAnchorButton.setEnabled(false);
        eAnchorButton.setFocusPainted(false);
        eAnchorButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        eAnchorButton.setPreferredSize(new java.awt.Dimension(22, 22));
        eAnchorButton.addActionListener(formListener);

        neAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/anchor_ne.png"))); // NOI18N
        neAnchorButton.setEnabled(false);
        neAnchorButton.setFocusPainted(false);
        neAnchorButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        neAnchorButton.setPreferredSize(new java.awt.Dimension(22, 22));
        neAnchorButton.addActionListener(formListener);

        sAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/anchor_s.png"))); // NOI18N
        sAnchorButton.setEnabled(false);
        sAnchorButton.setFocusPainted(false);
        sAnchorButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        sAnchorButton.setPreferredSize(new java.awt.Dimension(22, 22));
        sAnchorButton.addActionListener(formListener);

        seAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/anchor_se.png"))); // NOI18N
        seAnchorButton.setEnabled(false);
        seAnchorButton.setFocusPainted(false);
        seAnchorButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        seAnchorButton.setPreferredSize(new java.awt.Dimension(22, 22));
        seAnchorButton.addActionListener(formListener);

        wAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/anchor_w.png"))); // NOI18N
        wAnchorButton.setEnabled(false);
        wAnchorButton.setFocusPainted(false);
        wAnchorButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        wAnchorButton.setPreferredSize(new java.awt.Dimension(22, 22));
        wAnchorButton.addActionListener(formListener);

        swAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/anchor_sw.png"))); // NOI18N
        swAnchorButton.setEnabled(false);
        swAnchorButton.setFocusPainted(false);
        swAnchorButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        swAnchorButton.setPreferredSize(new java.awt.Dimension(22, 22));
        swAnchorButton.addActionListener(formListener);

        anchorTypePanel.setOpaque(false);

        baselineAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/baseline.png"))); // NOI18N
        baselineAnchorButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.anchor.baselineRelated")); // NOI18N
        baselineAnchorButton.setEnabled(false);
        baselineAnchorButton.setFocusPainted(false);
        baselineAnchorButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        baselineAnchorButton.setPreferredSize(new java.awt.Dimension(22, 22));
        baselineAnchorButton.addActionListener(formListener);

        bidiAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/bidi.png"))); // NOI18N
        bidiAnchorButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.anchor.bidiAware")); // NOI18N
        bidiAnchorButton.setEnabled(false);
        bidiAnchorButton.setFocusPainted(false);
        bidiAnchorButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        bidiAnchorButton.setPreferredSize(new java.awt.Dimension(22, 22));
        bidiAnchorButton.addActionListener(formListener);

        javax.swing.GroupLayout anchorTypePanelLayout = new javax.swing.GroupLayout(anchorTypePanel);
        anchorTypePanel.setLayout(anchorTypePanelLayout);
        anchorTypePanelLayout.setHorizontalGroup(
            anchorTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(anchorTypePanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(anchorTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bidiAnchorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(baselineAnchorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );
        anchorTypePanelLayout.setVerticalGroup(
            anchorTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(anchorTypePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bidiAnchorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(baselineAnchorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout anchorPanelLayout = new javax.swing.GroupLayout(anchorPanel);
        anchorPanel.setLayout(anchorPanelLayout);
        anchorPanelLayout.setHorizontalGroup(
            anchorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(anchorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(anchorTypePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(anchorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addGroup(anchorPanelLayout.createSequentialGroup()
                        .addComponent(swAnchorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(sAnchorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(seAnchorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(anchorPanelLayout.createSequentialGroup()
                        .addComponent(wAnchorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(cAnchorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(eAnchorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(anchorPanelLayout.createSequentialGroup()
                        .addComponent(nwAnchorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(nAnchorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(neAnchorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(14, Short.MAX_VALUE))
        );
        anchorPanelLayout.setVerticalGroup(
            anchorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(anchorPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(anchorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(anchorTypePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(anchorPanelLayout.createSequentialGroup()
                        .addGroup(anchorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(nwAnchorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(nAnchorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(neAnchorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, 0)
                        .addGroup(anchorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(wAnchorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cAnchorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(eAnchorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, 0)
                        .addGroup(anchorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(swAnchorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(sAnchorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(seAnchorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout anchorToolGroupLayout = new javax.swing.GroupLayout(anchorToolGroup);
        anchorToolGroup.setLayout(anchorToolGroupLayout);
        anchorToolGroupLayout.setHorizontalGroup(
            anchorToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(anchorToolGroupLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(anchorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        anchorToolGroupLayout.setVerticalGroup(
            anchorToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(anchorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        paddingToolGroup.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.paddingToolGroup.border.title"))); // NOI18N

        paddingPanel.setOpaque(false);

        vMinusPaddingButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        vMinusPaddingButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.padding.VMinus.toolTipText")); // NOI18N
        vMinusPaddingButton.setEnabled(false);
        vMinusPaddingButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vMinusPaddingButton.setPreferredSize(new java.awt.Dimension(22, 22));
        vMinusPaddingButton.addActionListener(formListener);

        vPadLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        vPadLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/vertical.png"))); // NOI18N
        vPadLabel.setPreferredSize(new java.awt.Dimension(10, 15));

        vPlusPaddingButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        vPlusPaddingButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.padding.VPlus.toolTipText")); // NOI18N
        vPlusPaddingButton.setEnabled(false);
        vPlusPaddingButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vPlusPaddingButton.setPreferredSize(new java.awt.Dimension(22, 22));
        vPlusPaddingButton.addActionListener(formListener);

        hPlusPaddingButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        hPlusPaddingButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.padding.HPlus.toolTipText")); // NOI18N
        hPlusPaddingButton.setEnabled(false);
        hPlusPaddingButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hPlusPaddingButton.setPreferredSize(new java.awt.Dimension(22, 22));
        hPlusPaddingButton.addActionListener(formListener);

        hPadLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/horizontal.png"))); // NOI18N
        hPadLabel.setPreferredSize(new java.awt.Dimension(15, 10));

        hMinusPaddingButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        hMinusPaddingButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.padding.HMinus.toolTipText")); // NOI18N
        hMinusPaddingButton.setEnabled(false);
        hMinusPaddingButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hMinusPaddingButton.setPreferredSize(new java.awt.Dimension(22, 22));
        hMinusPaddingButton.addActionListener(formListener);

        bMinusPaddingButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        bMinusPaddingButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.padding.BMinus.toolTipText")); // NOI18N
        bMinusPaddingButton.setEnabled(false);
        bMinusPaddingButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        bMinusPaddingButton.setPreferredSize(new java.awt.Dimension(22, 22));
        bMinusPaddingButton.addActionListener(formListener);

        bPadLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        bPadLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/both.png"))); // NOI18N

        bPlusPaddingButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        bPlusPaddingButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.padding.BPlus.toolTipText")); // NOI18N
        bPlusPaddingButton.setEnabled(false);
        bPlusPaddingButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        bPlusPaddingButton.setPreferredSize(new java.awt.Dimension(22, 22));
        bPlusPaddingButton.addActionListener(formListener);

        javax.swing.GroupLayout paddingPanelLayout = new javax.swing.GroupLayout(paddingPanel);
        paddingPanel.setLayout(paddingPanelLayout);
        paddingPanelLayout.setHorizontalGroup(
            paddingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(paddingPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(vPadLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addGroup(paddingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(vPlusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vMinusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(paddingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(hPadLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(paddingPanelLayout.createSequentialGroup()
                        .addComponent(hMinusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(hPlusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bPadLabel)
                .addGap(0, 0, 0)
                .addGroup(paddingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(bPlusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bMinusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(17, Short.MAX_VALUE))
        );
        paddingPanelLayout.setVerticalGroup(
            paddingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(paddingPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(paddingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(paddingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(bPadLabel)
                        .addGroup(paddingPanelLayout.createSequentialGroup()
                            .addComponent(bPlusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 0, 0)
                            .addComponent(bMinusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(paddingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(vPadLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(paddingPanelLayout.createSequentialGroup()
                            .addComponent(vPlusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 0, 0)
                            .addComponent(vMinusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, paddingPanelLayout.createSequentialGroup()
                            .addGroup(paddingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(hPlusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(hMinusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(0, 0, 0)
                            .addComponent(hPadLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout paddingToolGroupLayout = new javax.swing.GroupLayout(paddingToolGroup);
        paddingToolGroup.setLayout(paddingToolGroupLayout);
        paddingToolGroupLayout.setHorizontalGroup(
            paddingToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(paddingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        paddingToolGroupLayout.setVerticalGroup(
            paddingToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(paddingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        gridSizeToolGroup.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.gridSizeToolGroup.border.title"))); // NOI18N

        gridSizePanel.setOpaque(false);

        vGridRelativeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/gridheight_relative.png"))); // NOI18N
        vGridRelativeButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vGridRelativeButton.toolTipText")); // NOI18N
        vGridRelativeButton.setEnabled(false);
        vGridRelativeButton.setPreferredSize(new java.awt.Dimension(22, 22));
        vGridRelativeButton.addActionListener(formListener);

        vGridRemainderButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/gridheight_remainder.png"))); // NOI18N
        vGridRemainderButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vGridRemainderButton.toolTipText")); // NOI18N
        vGridRemainderButton.setEnabled(false);
        vGridRemainderButton.setPreferredSize(new java.awt.Dimension(22, 22));
        vGridRemainderButton.addActionListener(formListener);

        vGridMinusButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        vGridMinusButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vGridMinusButton.toolTipText")); // NOI18N
        vGridMinusButton.setEnabled(false);
        vGridMinusButton.setPreferredSize(new java.awt.Dimension(22, 22));
        vGridMinusButton.addActionListener(formListener);

        vGridPlusButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        vGridPlusButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vGridPlusButton.toolTipText")); // NOI18N
        vGridPlusButton.setEnabled(false);
        vGridPlusButton.setPreferredSize(new java.awt.Dimension(22, 22));
        vGridPlusButton.addActionListener(formListener);

        vGridLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        vGridLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/gridheight.png"))); // NOI18N
        vGridLabel.setPreferredSize(new java.awt.Dimension(10, 15));

        hGridMinusButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        hGridMinusButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hGridMinusButton.toolTipText")); // NOI18N
        hGridMinusButton.setEnabled(false);
        hGridMinusButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hGridMinusButton.setPreferredSize(new java.awt.Dimension(22, 22));
        hGridMinusButton.addActionListener(formListener);

        hGridPlusButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        hGridPlusButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hGridPlusButton.toolTipText")); // NOI18N
        hGridPlusButton.setEnabled(false);
        hGridPlusButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hGridPlusButton.setPreferredSize(new java.awt.Dimension(22, 22));
        hGridPlusButton.addActionListener(formListener);

        hGridRelativeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/gridwidth_relative.png"))); // NOI18N
        hGridRelativeButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hGridRelativeButton.toolTipText")); // NOI18N
        hGridRelativeButton.setEnabled(false);
        hGridRelativeButton.setPreferredSize(new java.awt.Dimension(22, 22));
        hGridRelativeButton.addActionListener(formListener);

        hGridRemainderButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/gridwidth_remainder.png"))); // NOI18N
        hGridRemainderButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hGridRemainderButton.toolTipText")); // NOI18N
        hGridRemainderButton.setEnabled(false);
        hGridRemainderButton.setPreferredSize(new java.awt.Dimension(22, 22));
        hGridRemainderButton.addActionListener(formListener);

        hGridLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/gridwidth.png"))); // NOI18N
        hGridLabel.setPreferredSize(new java.awt.Dimension(15, 10));

        javax.swing.GroupLayout gridSizePanelLayout = new javax.swing.GroupLayout(gridSizePanel);
        gridSizePanel.setLayout(gridSizePanelLayout);
        gridSizePanelLayout.setHorizontalGroup(
            gridSizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gridSizePanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(vGridLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addGroup(gridSizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(vGridMinusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vGridPlusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(gridSizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addGroup(gridSizePanelLayout.createSequentialGroup()
                        .addComponent(hGridMinusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(hGridPlusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(hGridLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(gridSizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(vGridRelativeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hGridRelativeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(gridSizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(vGridRemainderButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hGridRemainderButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        gridSizePanelLayout.setVerticalGroup(
            gridSizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gridSizePanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(gridSizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(gridSizePanelLayout.createSequentialGroup()
                        .addComponent(hGridRemainderButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(vGridRemainderButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(gridSizePanelLayout.createSequentialGroup()
                        .addComponent(hGridRelativeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(vGridRelativeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(gridSizePanelLayout.createSequentialGroup()
                        .addGroup(gridSizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(hGridPlusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(hGridMinusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, 0)
                        .addComponent(hGridLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(gridSizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(vGridLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(gridSizePanelLayout.createSequentialGroup()
                            .addComponent(vGridMinusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 0, 0)
                            .addComponent(vGridPlusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout gridSizeToolGroupLayout = new javax.swing.GroupLayout(gridSizeToolGroup);
        gridSizeToolGroup.setLayout(gridSizeToolGroupLayout);
        gridSizeToolGroupLayout.setHorizontalGroup(
            gridSizeToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(gridSizePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        gridSizeToolGroupLayout.setVerticalGroup(
            gridSizeToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gridSizeToolGroupLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(gridSizePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        gridPositionToolGroup.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.gridPositionToolGroup.border.title"))); // NOI18N

        gridPositionPanel.setOpaque(false);

        xGridMinusButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/anchor_w.png"))); // NOI18N
        xGridMinusButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.xGridMinusButton.toolTipText")); // NOI18N
        xGridMinusButton.setEnabled(false);
        xGridMinusButton.setPreferredSize(new java.awt.Dimension(22, 22));
        xGridMinusButton.addActionListener(formListener);

        yGridMinusButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/anchor_n.png"))); // NOI18N
        yGridMinusButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.yGridMinusButton.toolTipText")); // NOI18N
        yGridMinusButton.setEnabled(false);
        yGridMinusButton.setPreferredSize(new java.awt.Dimension(22, 22));
        yGridMinusButton.addActionListener(formListener);

        yGridPlusButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/anchor_s.png"))); // NOI18N
        yGridPlusButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.yGridPlusButton.toolTipText")); // NOI18N
        yGridPlusButton.setEnabled(false);
        yGridPlusButton.setPreferredSize(new java.awt.Dimension(22, 22));
        yGridPlusButton.addActionListener(formListener);

        xGridPlusButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/anchor_e.png"))); // NOI18N
        xGridPlusButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.xGridPlusButton.toolTipText")); // NOI18N
        xGridPlusButton.setEnabled(false);
        xGridPlusButton.setPreferredSize(new java.awt.Dimension(22, 22));
        xGridPlusButton.addActionListener(formListener);

        xGridRelativeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/gridx_relative.png"))); // NOI18N
        xGridRelativeButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.xGridRelativeButton.toolTipText")); // NOI18N
        xGridRelativeButton.setEnabled(false);
        xGridRelativeButton.setPreferredSize(new java.awt.Dimension(22, 22));
        xGridRelativeButton.addActionListener(formListener);

        yGridRelativeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/gridy_relative.png"))); // NOI18N
        yGridRelativeButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.yGridRelativeButton.toolTipText")); // NOI18N
        yGridRelativeButton.setEnabled(false);
        yGridRelativeButton.setPreferredSize(new java.awt.Dimension(22, 22));
        yGridRelativeButton.addActionListener(formListener);

        javax.swing.GroupLayout gridPositionPanelLayout = new javax.swing.GroupLayout(gridPositionPanel);
        gridPositionPanel.setLayout(gridPositionPanelLayout);
        gridPositionPanelLayout.setHorizontalGroup(
            gridPositionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gridPositionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(xGridMinusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addGroup(gridPositionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(yGridMinusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(yGridPlusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addComponent(xGridPlusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(gridPositionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(yGridRelativeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(xGridRelativeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );
        gridPositionPanelLayout.setVerticalGroup(
            gridPositionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gridPositionPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(gridPositionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(gridPositionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(xGridMinusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(xGridPlusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(gridPositionPanelLayout.createSequentialGroup()
                            .addComponent(yGridMinusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 0, 0)
                            .addComponent(yGridPlusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.CENTER, gridPositionPanelLayout.createSequentialGroup()
                        .addComponent(xGridRelativeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(yGridRelativeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout gridPositionToolGroupLayout = new javax.swing.GroupLayout(gridPositionToolGroup);
        gridPositionToolGroup.setLayout(gridPositionToolGroupLayout);
        gridPositionToolGroupLayout.setHorizontalGroup(
            gridPositionToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gridPositionToolGroupLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(gridPositionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        gridPositionToolGroupLayout.setVerticalGroup(
            gridPositionToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, gridPositionToolGroupLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(gridPositionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout leftPanelLayout = new javax.swing.GroupLayout(leftPanel);
        leftPanel.setLayout(leftPanelLayout);
        leftPanelLayout.setHorizontalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leftPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(anchorToolGroup, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(gridPositionToolGroup, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(gridSizeToolGroup, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(paddingToolGroup, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        leftPanelLayout.setVerticalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, leftPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(anchorToolGroup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(paddingToolGroup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gridSizeToolGroup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gridPositionToolGroup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        insetsToolGroup.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.insetsToolGroup.border.title"))); // NOI18N

        insetsPanel.setOpaque(false);

        vInsetLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        vInsetLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/inset_v.png"))); // NOI18N
        vInsetLabel.setMaximumSize(new java.awt.Dimension(10, 15));
        vInsetLabel.setMinimumSize(new java.awt.Dimension(10, 15));
        vInsetLabel.setPreferredSize(new java.awt.Dimension(10, 15));

        vMinusInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        vMinusInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vMinusInsetButton.toolTipText")); // NOI18N
        vMinusInsetButton.setEnabled(false);
        vMinusInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vMinusInsetButton.setPreferredSize(new java.awt.Dimension(22, 22));
        vMinusInsetButton.addActionListener(formListener);

        vPlusInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        vPlusInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vPlusInsetButton.toolTipText")); // NOI18N
        vPlusInsetButton.setEnabled(false);
        vPlusInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vPlusInsetButton.setPreferredSize(new java.awt.Dimension(22, 22));
        vPlusInsetButton.addActionListener(formListener);

        hInsetLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/inset_h.png"))); // NOI18N
        hInsetLabel.setMaximumSize(new java.awt.Dimension(15, 10));
        hInsetLabel.setMinimumSize(new java.awt.Dimension(15, 10));
        hInsetLabel.setPreferredSize(new java.awt.Dimension(15, 10));

        hMinusInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        hMinusInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hMinusInsetButton.toolTipText")); // NOI18N
        hMinusInsetButton.setEnabled(false);
        hMinusInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hMinusInsetButton.setPreferredSize(new java.awt.Dimension(22, 22));
        hMinusInsetButton.addActionListener(formListener);

        hPlusInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        hPlusInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hPlusInsetButton.toolTipText")); // NOI18N
        hPlusInsetButton.setEnabled(false);
        hPlusInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hPlusInsetButton.setPreferredSize(new java.awt.Dimension(22, 22));
        hPlusInsetButton.addActionListener(formListener);

        bInsetLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        bInsetLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/inset_both.png"))); // NOI18N

        bMinusInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        bMinusInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.bMinusInsetButton.toolTipText")); // NOI18N
        bMinusInsetButton.setEnabled(false);
        bMinusInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        bMinusInsetButton.setPreferredSize(new java.awt.Dimension(22, 22));
        bMinusInsetButton.addActionListener(formListener);

        bPlusInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        bPlusInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.bPlusInsetButton.toolTipText")); // NOI18N
        bPlusInsetButton.setEnabled(false);
        bPlusInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        bPlusInsetButton.setPreferredSize(new java.awt.Dimension(22, 22));
        bPlusInsetButton.addActionListener(formListener);

        javax.swing.GroupLayout insetsPanelLayout = new javax.swing.GroupLayout(insetsPanel);
        insetsPanel.setLayout(insetsPanelLayout);
        insetsPanelLayout.setHorizontalGroup(
            insetsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(insetsPanelLayout.createSequentialGroup()
                .addGroup(insetsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(vPlusInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(insetsPanelLayout.createSequentialGroup()
                        .addComponent(vInsetLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(vMinusInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(insetsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(hInsetLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(insetsPanelLayout.createSequentialGroup()
                        .addComponent(hMinusInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(hPlusInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bInsetLabel)
                .addGap(0, 0, 0)
                .addGroup(insetsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bPlusInsetButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bMinusInsetButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        insetsPanelLayout.setVerticalGroup(
            insetsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(insetsPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(insetsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(insetsPanelLayout.createSequentialGroup()
                        .addGroup(insetsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(hPlusInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(hMinusInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, 0)
                        .addComponent(hInsetLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(insetsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(bInsetLabel)
                        .addGroup(insetsPanelLayout.createSequentialGroup()
                            .addComponent(bPlusInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 0, 0)
                            .addComponent(bMinusInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(vInsetLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(insetsPanelLayout.createSequentialGroup()
                            .addComponent(vPlusInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(vMinusInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        insetsCross.setOpaque(false);

        topLeftCorner.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/corner_tl.png"))); // NOI18N

        vPlusTopInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        vPlusTopInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vPlusTopInsetButton.toolTipText")); // NOI18N
        vPlusTopInsetButton.setEnabled(false);
        vPlusTopInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vPlusTopInsetButton.setPreferredSize(new java.awt.Dimension(22, 22));
        vPlusTopInsetButton.addActionListener(formListener);

        vMinusTopInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        vMinusTopInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vMinusTopInsetButton.toolTipText")); // NOI18N
        vMinusTopInsetButton.setEnabled(false);
        vMinusTopInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vMinusTopInsetButton.setPreferredSize(new java.awt.Dimension(22, 22));
        vMinusTopInsetButton.addActionListener(formListener);

        topRightCorner.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/corner_tr.png"))); // NOI18N

        hPlusLeftInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        hPlusLeftInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hPlusLeftInsetButton.toolTipText")); // NOI18N
        hPlusLeftInsetButton.setEnabled(false);
        hPlusLeftInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hPlusLeftInsetButton.setPreferredSize(new java.awt.Dimension(22, 22));
        hPlusLeftInsetButton.addActionListener(formListener);

        hMinusLeftInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        hMinusLeftInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hMinusLeftInsetButton.toolTipText")); // NOI18N
        hMinusLeftInsetButton.setEnabled(false);
        hMinusLeftInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hMinusLeftInsetButton.setPreferredSize(new java.awt.Dimension(22, 22));
        hMinusLeftInsetButton.addActionListener(formListener);

        hMinusRightInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        hMinusRightInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hMinusRightInsetButton.toolTipText")); // NOI18N
        hMinusRightInsetButton.setEnabled(false);
        hMinusRightInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hMinusRightInsetButton.setPreferredSize(new java.awt.Dimension(22, 22));
        hMinusRightInsetButton.addActionListener(formListener);

        hPlusRightInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        hPlusRightInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hPlusRightInsetButton.toolTipText")); // NOI18N
        hPlusRightInsetButton.setEnabled(false);
        hPlusRightInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hPlusRightInsetButton.setPreferredSize(new java.awt.Dimension(22, 22));
        hPlusRightInsetButton.addActionListener(formListener);

        bottomLeftCorner.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/corner_bl.png"))); // NOI18N

        vMinusBottomInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        vMinusBottomInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vMinusBottomInsetButton.toolTipText")); // NOI18N
        vMinusBottomInsetButton.setEnabled(false);
        vMinusBottomInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vMinusBottomInsetButton.setPreferredSize(new java.awt.Dimension(22, 22));
        vMinusBottomInsetButton.addActionListener(formListener);

        vPlusBottomInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        vPlusBottomInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vPlusBottomInsetButton.toolTipText")); // NOI18N
        vPlusBottomInsetButton.setEnabled(false);
        vPlusBottomInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vPlusBottomInsetButton.setPreferredSize(new java.awt.Dimension(22, 22));
        vPlusBottomInsetButton.addActionListener(formListener);

        bottomRightCorner.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/corner_br.png"))); // NOI18N

        javax.swing.GroupLayout insetsCrossLayout = new javax.swing.GroupLayout(insetsCross);
        insetsCross.setLayout(insetsCrossLayout);
        insetsCrossLayout.setHorizontalGroup(
            insetsCrossLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(insetsCrossLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(insetsCrossLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(insetsCrossLayout.createSequentialGroup()
                        .addComponent(topLeftCorner)
                        .addGap(0, 0, 0))
                    .addGroup(insetsCrossLayout.createSequentialGroup()
                        .addComponent(hPlusLeftInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(hMinusLeftInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(insetsCrossLayout.createSequentialGroup()
                        .addComponent(bottomLeftCorner)
                        .addGap(0, 0, 0)))
                .addGroup(insetsCrossLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(vPlusTopInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vMinusTopInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(crossCenter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vMinusBottomInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vPlusBottomInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(insetsCrossLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bottomRightCorner)
                    .addGroup(insetsCrossLayout.createSequentialGroup()
                        .addComponent(hMinusRightInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(hPlusRightInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(topRightCorner))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        insetsCrossLayout.setVerticalGroup(
            insetsCrossLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(insetsCrossLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(insetsCrossLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(topLeftCorner)
                    .addGroup(insetsCrossLayout.createSequentialGroup()
                        .addComponent(vPlusTopInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addGroup(insetsCrossLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(vMinusTopInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(topRightCorner))))
                .addGap(0, 0, 0)
                .addGroup(insetsCrossLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(hPlusLeftInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hMinusLeftInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hMinusRightInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(crossCenter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hPlusRightInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(insetsCrossLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bottomRightCorner)
                    .addGroup(insetsCrossLayout.createSequentialGroup()
                        .addComponent(vMinusBottomInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(vPlusBottomInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(bottomLeftCorner))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout insetsToolGroupLayout = new javax.swing.GroupLayout(insetsToolGroup);
        insetsToolGroup.setLayout(insetsToolGroupLayout);
        insetsToolGroupLayout.setHorizontalGroup(
            insetsToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(insetsToolGroupLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(insetsToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(insetsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(insetsCross, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        insetsToolGroupLayout.setVerticalGroup(
            insetsToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, insetsToolGroupLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(insetsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(insetsCross, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        fillToolGroup.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.fillToolGroup.border.title"))); // NOI18N

        fillPanel.setOpaque(false);

        hFillButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/resize_h.png"))); // NOI18N
        hFillButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.fill.horizontal")); // NOI18N
        hFillButton.setEnabled(false);
        hFillButton.setFocusPainted(false);
        hFillButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hFillButton.setPreferredSize(new java.awt.Dimension(22, 22));
        hFillButton.addActionListener(formListener);

        vFillButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/resize_v.png"))); // NOI18N
        vFillButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.fill.vertical")); // NOI18N
        vFillButton.setEnabled(false);
        vFillButton.setFocusPainted(false);
        vFillButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vFillButton.setPreferredSize(new java.awt.Dimension(22, 22));
        vFillButton.addActionListener(formListener);

        javax.swing.GroupLayout fillPanelLayout = new javax.swing.GroupLayout(fillPanel);
        fillPanel.setLayout(fillPanelLayout);
        fillPanelLayout.setHorizontalGroup(
            fillPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fillPanelLayout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addComponent(hFillButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(vFillButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        fillPanelLayout.setVerticalGroup(
            fillPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fillPanelLayout.createSequentialGroup()
                .addGroup(fillPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(hFillButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vFillButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout fillToolGroupLayout = new javax.swing.GroupLayout(fillToolGroup);
        fillToolGroup.setLayout(fillToolGroupLayout);
        fillToolGroupLayout.setHorizontalGroup(
            fillToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fillToolGroupLayout.createSequentialGroup()
                .addComponent(fillPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap(49, Short.MAX_VALUE))
        );
        fillToolGroupLayout.setVerticalGroup(
            fillToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fillToolGroupLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(fillPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        weightsToolGroup.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.weightsToolGroup.border.title"))); // NOI18N

        weightsPanel.setOpaque(false);

        vWeightLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        vWeightLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/weight_vertical.png"))); // NOI18N

        vMinusWeightButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        vMinusWeightButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vMinusWeightButton.toolTipText")); // NOI18N
        vMinusWeightButton.setEnabled(false);
        vMinusWeightButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vMinusWeightButton.setPreferredSize(new java.awt.Dimension(22, 22));
        vMinusWeightButton.addActionListener(formListener);

        vPlusWeightButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        vPlusWeightButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vPlusWeightButton.toolTipText")); // NOI18N
        vPlusWeightButton.setEnabled(false);
        vPlusWeightButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vPlusWeightButton.setPreferredSize(new java.awt.Dimension(22, 22));
        vPlusWeightButton.addActionListener(formListener);

        hWeightLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/weight_horizontal.png"))); // NOI18N

        hMinusWeightButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        hMinusWeightButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hMinusWeightButton.toolTipText")); // NOI18N
        hMinusWeightButton.setEnabled(false);
        hMinusWeightButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hMinusWeightButton.setPreferredSize(new java.awt.Dimension(22, 22));
        hMinusWeightButton.addActionListener(formListener);

        hPlusWeightButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        hPlusWeightButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hPlusWeightButton.toolTipText")); // NOI18N
        hPlusWeightButton.setEnabled(false);
        hPlusWeightButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hPlusWeightButton.setPreferredSize(new java.awt.Dimension(22, 22));
        hPlusWeightButton.addActionListener(formListener);

        hWeightEqualizeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/weight_equal_horizontal.png"))); // NOI18N
        hWeightEqualizeButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hWeightEqualizeButton.toolTipText")); // NOI18N
        hWeightEqualizeButton.setEnabled(false);
        hWeightEqualizeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hWeightEqualizeButton.setPreferredSize(new java.awt.Dimension(22, 22));
        hWeightEqualizeButton.addActionListener(formListener);

        vWeightEqualizeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/weight_equal_vertical.png"))); // NOI18N
        vWeightEqualizeButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vWeightEqualizeButton.toolTipText")); // NOI18N
        vWeightEqualizeButton.setEnabled(false);
        vWeightEqualizeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vWeightEqualizeButton.setPreferredSize(new java.awt.Dimension(22, 22));
        vWeightEqualizeButton.addActionListener(formListener);

        javax.swing.GroupLayout weightsPanelLayout = new javax.swing.GroupLayout(weightsPanel);
        weightsPanel.setLayout(weightsPanelLayout);
        weightsPanelLayout.setHorizontalGroup(
            weightsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(weightsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(vWeightLabel)
                .addGap(0, 0, 0)
                .addGroup(weightsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(vPlusWeightButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vMinusWeightButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(weightsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(hWeightLabel)
                    .addGroup(weightsPanelLayout.createSequentialGroup()
                        .addComponent(hMinusWeightButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(hPlusWeightButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(weightsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(hWeightEqualizeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vWeightEqualizeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        weightsPanelLayout.setVerticalGroup(
            weightsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(weightsPanelLayout.createSequentialGroup()
                .addGroup(weightsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(vWeightLabel)
                    .addGroup(weightsPanelLayout.createSequentialGroup()
                        .addComponent(vPlusWeightButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(vMinusWeightButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(weightsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(weightsPanelLayout.createSequentialGroup()
                            .addComponent(hWeightEqualizeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 0, 0)
                            .addComponent(vWeightEqualizeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(weightsPanelLayout.createSequentialGroup()
                            .addGroup(weightsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(hPlusWeightButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(hMinusWeightButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(0, 0, 0)
                            .addComponent(hWeightLabel))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout weightsToolGroupLayout = new javax.swing.GroupLayout(weightsToolGroup);
        weightsToolGroup.setLayout(weightsToolGroupLayout);
        weightsToolGroupLayout.setHorizontalGroup(
            weightsToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(weightsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        weightsToolGroupLayout.setVerticalGroup(
            weightsToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(weightsToolGroupLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(weightsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout rightPanelLayout = new javax.swing.GroupLayout(rightPanel);
        rightPanel.setLayout(rightPanelLayout);
        rightPanelLayout.setHorizontalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(weightsToolGroup, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(fillToolGroup, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(insetsToolGroup, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        rightPanelLayout.setVerticalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(insetsToolGroup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fillToolGroup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(weightsToolGroup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout customizerLayout = new javax.swing.GroupLayout(customizer);
        customizer.setLayout(customizerLayout);
        customizerLayout.setHorizontalGroup(
            customizerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(customizerLayout.createSequentialGroup()
                .addComponent(leftPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(rightPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        customizerLayout.setVerticalGroup(
            customizerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(leftPanel, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(rightPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == cAnchorButton) {
                GridBagCustomizer.this.cAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == nAnchorButton) {
                GridBagCustomizer.this.nAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == nwAnchorButton) {
                GridBagCustomizer.this.nwAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == eAnchorButton) {
                GridBagCustomizer.this.eAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == neAnchorButton) {
                GridBagCustomizer.this.neAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == sAnchorButton) {
                GridBagCustomizer.this.sAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == seAnchorButton) {
                GridBagCustomizer.this.seAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == wAnchorButton) {
                GridBagCustomizer.this.wAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == swAnchorButton) {
                GridBagCustomizer.this.swAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == baselineAnchorButton) {
                GridBagCustomizer.this.baselineAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == bidiAnchorButton) {
                GridBagCustomizer.this.bidiAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == vMinusPaddingButton) {
                GridBagCustomizer.this.vMinusPaddingButtonActionPerformed(evt);
            }
            else if (evt.getSource() == vPlusPaddingButton) {
                GridBagCustomizer.this.vPlusPaddingButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hPlusPaddingButton) {
                GridBagCustomizer.this.hPlusPaddingButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hMinusPaddingButton) {
                GridBagCustomizer.this.hMinusPaddingButtonActionPerformed(evt);
            }
            else if (evt.getSource() == bMinusPaddingButton) {
                GridBagCustomizer.this.bMinusPaddingButtonActionPerformed(evt);
            }
            else if (evt.getSource() == bPlusPaddingButton) {
                GridBagCustomizer.this.bPlusPaddingButtonActionPerformed(evt);
            }
            else if (evt.getSource() == vGridRelativeButton) {
                GridBagCustomizer.this.vGridRelativeButtonActionPerformed(evt);
            }
            else if (evt.getSource() == vGridRemainderButton) {
                GridBagCustomizer.this.vGridRemainderButtonActionPerformed(evt);
            }
            else if (evt.getSource() == vGridMinusButton) {
                GridBagCustomizer.this.vGridMinusButtonActionPerformed(evt);
            }
            else if (evt.getSource() == vGridPlusButton) {
                GridBagCustomizer.this.vGridPlusButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hGridMinusButton) {
                GridBagCustomizer.this.hGridMinusButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hGridPlusButton) {
                GridBagCustomizer.this.hGridPlusButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hGridRelativeButton) {
                GridBagCustomizer.this.hGridRelativeButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hGridRemainderButton) {
                GridBagCustomizer.this.hGridRemainderButtonActionPerformed(evt);
            }
            else if (evt.getSource() == xGridMinusButton) {
                GridBagCustomizer.this.xGridMinusButtonActionPerformed(evt);
            }
            else if (evt.getSource() == yGridMinusButton) {
                GridBagCustomizer.this.yGridMinusButtonActionPerformed(evt);
            }
            else if (evt.getSource() == yGridPlusButton) {
                GridBagCustomizer.this.yGridPlusButtonActionPerformed(evt);
            }
            else if (evt.getSource() == xGridPlusButton) {
                GridBagCustomizer.this.xGridPlusButtonActionPerformed(evt);
            }
            else if (evt.getSource() == xGridRelativeButton) {
                GridBagCustomizer.this.xGridRelativeButtonActionPerformed(evt);
            }
            else if (evt.getSource() == yGridRelativeButton) {
                GridBagCustomizer.this.yGridRelativeButtonActionPerformed(evt);
            }
            else if (evt.getSource() == vMinusInsetButton) {
                GridBagCustomizer.this.vMinusInsetButtonActionPerformed(evt);
            }
            else if (evt.getSource() == vPlusInsetButton) {
                GridBagCustomizer.this.vPlusInsetButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hMinusInsetButton) {
                GridBagCustomizer.this.hMinusInsetButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hPlusInsetButton) {
                GridBagCustomizer.this.hPlusInsetButtonActionPerformed(evt);
            }
            else if (evt.getSource() == bMinusInsetButton) {
                GridBagCustomizer.this.bMinusInsetButtonActionPerformed(evt);
            }
            else if (evt.getSource() == bPlusInsetButton) {
                GridBagCustomizer.this.bPlusInsetButtonActionPerformed(evt);
            }
            else if (evt.getSource() == vPlusTopInsetButton) {
                GridBagCustomizer.this.vPlusTopInsetButtonActionPerformed(evt);
            }
            else if (evt.getSource() == vMinusTopInsetButton) {
                GridBagCustomizer.this.vMinusTopInsetButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hPlusLeftInsetButton) {
                GridBagCustomizer.this.hPlusLeftInsetButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hMinusLeftInsetButton) {
                GridBagCustomizer.this.hMinusLeftInsetButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hMinusRightInsetButton) {
                GridBagCustomizer.this.hMinusRightInsetButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hPlusRightInsetButton) {
                GridBagCustomizer.this.hPlusRightInsetButtonActionPerformed(evt);
            }
            else if (evt.getSource() == vMinusBottomInsetButton) {
                GridBagCustomizer.this.vMinusBottomInsetButtonActionPerformed(evt);
            }
            else if (evt.getSource() == vPlusBottomInsetButton) {
                GridBagCustomizer.this.vPlusBottomInsetButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hFillButton) {
                GridBagCustomizer.this.hFillButtonActionPerformed(evt);
            }
            else if (evt.getSource() == vFillButton) {
                GridBagCustomizer.this.vFillButtonActionPerformed(evt);
            }
            else if (evt.getSource() == vMinusWeightButton) {
                GridBagCustomizer.this.vMinusWeightButtonActionPerformed(evt);
            }
            else if (evt.getSource() == vPlusWeightButton) {
                GridBagCustomizer.this.vPlusWeightButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hMinusWeightButton) {
                GridBagCustomizer.this.hMinusWeightButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hPlusWeightButton) {
                GridBagCustomizer.this.hPlusWeightButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hWeightEqualizeButton) {
                GridBagCustomizer.this.hWeightEqualizeButtonActionPerformed(evt);
            }
            else if (evt.getSource() == vWeightEqualizeButton) {
                GridBagCustomizer.this.vWeightEqualizeButtonActionPerformed(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void nwAnchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nwAnchorButtonActionPerformed
        int anchor = currentAnchorSpecialization(GridBagConstraints.NORTHWEST);
        selectAnchorButtons(anchor);
        update(-1, anchor, null, null, null, null, null, null);
    }//GEN-LAST:event_nwAnchorButtonActionPerformed

    private void nAnchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nAnchorButtonActionPerformed
        int anchor = currentAnchorSpecialization(GridBagConstraints.NORTH);
        selectAnchorButtons(anchor);
        update(-1, anchor, null, null, null, null, null, null);
    }//GEN-LAST:event_nAnchorButtonActionPerformed

    private void neAnchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_neAnchorButtonActionPerformed
        int anchor = currentAnchorSpecialization(GridBagConstraints.NORTHEAST);
        selectAnchorButtons(anchor);
        update(-1, anchor, null, null, null, null, null, null);
    }//GEN-LAST:event_neAnchorButtonActionPerformed

    private void wAnchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wAnchorButtonActionPerformed
        int anchor = currentAnchorSpecialization(GridBagConstraints.WEST);
        selectAnchorButtons(anchor);
        update(-1, anchor, null, null, null, null, null, null);
    }//GEN-LAST:event_wAnchorButtonActionPerformed

    private void cAnchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cAnchorButtonActionPerformed
        int anchor = currentAnchorSpecialization(GridBagConstraints.CENTER);
        selectAnchorButtons(anchor);
        update(-1, anchor, null, null, null, null, null, null);
    }//GEN-LAST:event_cAnchorButtonActionPerformed

    private void eAnchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eAnchorButtonActionPerformed
        int anchor = currentAnchorSpecialization(GridBagConstraints.EAST);
        selectAnchorButtons(anchor);
        update(-1, anchor, null, null, null, null, null, null);
    }//GEN-LAST:event_eAnchorButtonActionPerformed

    private void swAnchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_swAnchorButtonActionPerformed
        int anchor = currentAnchorSpecialization(GridBagConstraints.SOUTHWEST);
        selectAnchorButtons(anchor);
        update(-1, anchor, null, null, null, null, null, null);
    }//GEN-LAST:event_swAnchorButtonActionPerformed

    private void sAnchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sAnchorButtonActionPerformed
        int anchor = currentAnchorSpecialization(GridBagConstraints.SOUTH);
        selectAnchorButtons(anchor);
        update(-1, anchor, null, null, null, null, null, null);
    }//GEN-LAST:event_sAnchorButtonActionPerformed

    private void seAnchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seAnchorButtonActionPerformed
        int anchor = currentAnchorSpecialization(GridBagConstraints.SOUTHEAST);
        selectAnchorButtons(anchor);
        update(-1, anchor, null, null, null, null, null, null);
    }//GEN-LAST:event_seAnchorButtonActionPerformed

    private void hFillButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hFillButtonActionPerformed
        boolean hFill = hFillButton.isSelected();
        update(-1, -1, new FillChange(hFill ? 1 : 0, -1), null, null, null, null, null);
    }//GEN-LAST:event_hFillButtonActionPerformed

    private void vFillButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vFillButtonActionPerformed
        boolean vFill = vFillButton.isSelected();
        update(-1, -1, new FillChange(-1, vFill ? 1 : 0), null, null, null, null, null);
    }//GEN-LAST:event_vFillButtonActionPerformed

    private void baselineAnchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_baselineAnchorButtonActionPerformed
        boolean baseline = baselineAnchorButton.isSelected();
        boolean bidi;
        if (baseline) {
            // Baseline anchors are bidi-aware
            bidiAnchorButton.setSelected(true);
            bidi = true;
        } else
            bidi = bidiAnchorButton.isSelected();
        update(baseline ? ANCHOR_BASELINE : (bidi ? ANCHOR_BIDI : ANCHOR_ABSOLUTE), -1, null, null, null, null, null, null);
        updateAnchorToolTips();
    }//GEN-LAST:event_baselineAnchorButtonActionPerformed

    private void bidiAnchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bidiAnchorButtonActionPerformed
        boolean bidi = bidiAnchorButton.isSelected();
        bidiCenter = bidi;
        boolean baseline;
        if (!bidi) {
            // Baseline anchors are bidi-aware
            baselineAnchorButton.setSelected(false);
            baseline = false;
        } else
            baseline = baselineAnchorButton.isSelected();
        update(baseline ? ANCHOR_BASELINE : (bidi ? ANCHOR_BIDI : ANCHOR_ABSOLUTE), -1, null, null, null, null, null, null);
        updateAnchorToolTips();
    }//GEN-LAST:event_bidiAnchorButtonActionPerformed

    private void hMinusPaddingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hMinusPaddingButtonActionPerformed
        int changeBy = STANDARD_SIZE_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_SIZE_CHANGE;
        boolean shift = ( evt.getModifiers() & ActionEvent.SHIFT_MASK ) != 0;        
        update(-1, -1, null, new PaddingChange(-changeBy, 0, shift), null, null, null, null);
    }//GEN-LAST:event_hMinusPaddingButtonActionPerformed

    private void hPlusPaddingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hPlusPaddingButtonActionPerformed
        int changeBy = STANDARD_SIZE_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, new PaddingChange(changeBy, 0, false), null, null, null, null);
    }//GEN-LAST:event_hPlusPaddingButtonActionPerformed

    private void bMinusPaddingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bMinusPaddingButtonActionPerformed
        int changeBy = STANDARD_SIZE_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_SIZE_CHANGE;
        boolean shift = ( evt.getModifiers() & ActionEvent.SHIFT_MASK ) != 0;
        update(-1, -1, null, new PaddingChange(-changeBy, -changeBy, shift), null, null, null, null);
    }//GEN-LAST:event_bMinusPaddingButtonActionPerformed

    private void bPlusPaddingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bPlusPaddingButtonActionPerformed
        int changeBy = STANDARD_SIZE_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, new PaddingChange(changeBy, changeBy, false), null, null, null, null);
    }//GEN-LAST:event_bPlusPaddingButtonActionPerformed

    private void bPlusInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bPlusInsetButtonActionPerformed
        int changeBy = STANDARD_SIZE_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new InsetsChange(changeBy, changeBy, changeBy, changeBy, false), null, null, null);
    }//GEN-LAST:event_bPlusInsetButtonActionPerformed

    private void bMinusInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bMinusInsetButtonActionPerformed
        int changeBy = STANDARD_SIZE_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_SIZE_CHANGE;
        boolean shift = ( evt.getModifiers() & ActionEvent.SHIFT_MASK ) != 0;
        update(-1, -1, null, null, new InsetsChange(-changeBy, -changeBy, -changeBy, -changeBy, shift), null, null, null);
    }//GEN-LAST:event_bMinusInsetButtonActionPerformed

    private void hMinusInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hMinusInsetButtonActionPerformed
        int changeBy = STANDARD_SIZE_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_SIZE_CHANGE;
        boolean shift = ( evt.getModifiers() & ActionEvent.SHIFT_MASK ) != 0;
        update(-1, -1, null, null, new InsetsChange(0, -changeBy, 0, -changeBy, shift), null, null, null);
    }//GEN-LAST:event_hMinusInsetButtonActionPerformed

    private void hPlusInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hPlusInsetButtonActionPerformed
        int changeBy = STANDARD_SIZE_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new InsetsChange(0, changeBy, 0, changeBy, false), null, null, null);
    }//GEN-LAST:event_hPlusInsetButtonActionPerformed

    private void vMinusInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vMinusInsetButtonActionPerformed
        int changeBy = STANDARD_SIZE_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_SIZE_CHANGE;
        boolean shift = ( evt.getModifiers() & ActionEvent.SHIFT_MASK ) != 0;
        update(-1, -1, null, null, new InsetsChange(-changeBy, 0, -changeBy, 0, shift), null, null, null);
    }//GEN-LAST:event_vMinusInsetButtonActionPerformed

    private void vPlusInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vPlusInsetButtonActionPerformed
        int changeBy = STANDARD_SIZE_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new InsetsChange(changeBy, 0, changeBy, 0, false), null, null, null);
    }//GEN-LAST:event_vPlusInsetButtonActionPerformed

    private void xGridMinusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xGridMinusButtonActionPerformed
        int changeBy = STANDARD_SIZE_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_SIZE_CHANGE;
        boolean shift = ( evt.getModifiers() & ActionEvent.SHIFT_MASK ) != 0;
        update(-1, -1, null, null, null, new GridPositionChange(-changeBy, NO_INDIRECT_CHANGE, 0, NO_INDIRECT_CHANGE, shift), null, null);
    }//GEN-LAST:event_xGridMinusButtonActionPerformed

    private void xGridPlusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xGridPlusButtonActionPerformed
        int changeBy = STANDARD_SIZE_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, null, new GridPositionChange(changeBy, NO_INDIRECT_CHANGE, 0, NO_INDIRECT_CHANGE, false), null, null);
    }//GEN-LAST:event_xGridPlusButtonActionPerformed

    private void yGridPlusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yGridPlusButtonActionPerformed
        int changeBy = STANDARD_SIZE_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, null, new GridPositionChange(0, NO_INDIRECT_CHANGE, changeBy, NO_INDIRECT_CHANGE, false), null, null);
    }//GEN-LAST:event_yGridPlusButtonActionPerformed

    private void yGridMinusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yGridMinusButtonActionPerformed
        int changeBy = STANDARD_SIZE_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_SIZE_CHANGE;
        boolean shift = ( evt.getModifiers() & ActionEvent.SHIFT_MASK ) != 0;
        update(-1, -1, null, null, null, new GridPositionChange(0, NO_INDIRECT_CHANGE, -changeBy, NO_INDIRECT_CHANGE, shift), null, null);
    }//GEN-LAST:event_yGridMinusButtonActionPerformed

    private void hMinusWeightButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hMinusWeightButtonActionPerformed
        double changeBy = STANDARD_WEIGHT_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_WEIGHT_CHANGE;
        boolean shift = ( evt.getModifiers() & ActionEvent.SHIFT_MASK ) != 0;
        update(-1, -1, null, null, null, null, null, new WeightChange(-changeBy, NO_INDIRECT_CHANGE, 0.0d, NO_INDIRECT_CHANGE, shift));
    }//GEN-LAST:event_hMinusWeightButtonActionPerformed

    private void hPlusWeightButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hPlusWeightButtonActionPerformed
        double changeBy = STANDARD_WEIGHT_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_WEIGHT_CHANGE;
        update(-1, -1, null, null, null, null, null, new WeightChange(changeBy, NO_INDIRECT_CHANGE, 0.0d, NO_INDIRECT_CHANGE, false));
    }//GEN-LAST:event_hPlusWeightButtonActionPerformed

    private void vMinusWeightButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vMinusWeightButtonActionPerformed
        double changeBy = STANDARD_WEIGHT_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_WEIGHT_CHANGE;
        boolean shift = ( evt.getModifiers() & ActionEvent.SHIFT_MASK ) != 0;
        update(-1, -1, null, null, null, null, null, new WeightChange(0.0d, NO_INDIRECT_CHANGE, -changeBy, NO_INDIRECT_CHANGE, shift));
    }//GEN-LAST:event_vMinusWeightButtonActionPerformed

    private void vPlusWeightButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vPlusWeightButtonActionPerformed
        double changeBy = STANDARD_WEIGHT_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_WEIGHT_CHANGE;
        update(-1, -1, null, null, null, null, null, new WeightChange(0.0d, NO_INDIRECT_CHANGE, changeBy, NO_INDIRECT_CHANGE, false));
    }//GEN-LAST:event_vPlusWeightButtonActionPerformed

    private void vWeightEqualizeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vWeightEqualizeButtonActionPerformed
        update(-1, -1, null, null, null, null, null, new WeightChange(0.0d, NO_INDIRECT_CHANGE, 0.0d, WEIGHTS_EQUALIZE, false));
    }//GEN-LAST:event_vWeightEqualizeButtonActionPerformed

    private void hWeightEqualizeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hWeightEqualizeButtonActionPerformed
        update(-1, -1, null, null, null, null, null, new WeightChange(0.0d, WEIGHTS_EQUALIZE, 0.0d, NO_INDIRECT_CHANGE, false));
    }//GEN-LAST:event_hWeightEqualizeButtonActionPerformed

    private void hGridPlusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hGridPlusButtonActionPerformed
        int changeBy = STANDARD_SIZE_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, null, null, new GridSizeChange(changeBy, NO_INDIRECT_CHANGE, 0, NO_INDIRECT_CHANGE, false), null);
    }//GEN-LAST:event_hGridPlusButtonActionPerformed

    private void hGridMinusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hGridMinusButtonActionPerformed
        int changeBy = STANDARD_SIZE_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_SIZE_CHANGE;
        boolean shift = ( evt.getModifiers() & ActionEvent.SHIFT_MASK ) != 0;
        update(-1, -1, null, null, null, null, new GridSizeChange(-changeBy, NO_INDIRECT_CHANGE, 0, NO_INDIRECT_CHANGE, shift), null);
    }//GEN-LAST:event_hGridMinusButtonActionPerformed

    private void vGridMinusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vGridMinusButtonActionPerformed
        int changeBy = STANDARD_SIZE_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_SIZE_CHANGE;
        boolean shift = ( evt.getModifiers() & ActionEvent.SHIFT_MASK ) != 0;
        update(-1, -1, null, null, null, null, new GridSizeChange(0, NO_INDIRECT_CHANGE, -changeBy, NO_INDIRECT_CHANGE, shift), null);
    }//GEN-LAST:event_vGridMinusButtonActionPerformed

    private void vGridPlusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vGridPlusButtonActionPerformed
        int changeBy = STANDARD_SIZE_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, null, null, new GridSizeChange(0, NO_INDIRECT_CHANGE, changeBy, NO_INDIRECT_CHANGE, false), null);
    }//GEN-LAST:event_vGridPlusButtonActionPerformed

    private void vPlusPaddingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vPlusPaddingButtonActionPerformed
        int changeBy = STANDARD_SIZE_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, new PaddingChange(0, changeBy, false), null, null, null, null);
    }//GEN-LAST:event_vPlusPaddingButtonActionPerformed

    private void vMinusPaddingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vMinusPaddingButtonActionPerformed
        int changeBy = STANDARD_SIZE_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_SIZE_CHANGE;
        boolean shift = ( evt.getModifiers() & ActionEvent.SHIFT_MASK ) != 0;
        update(-1, -1, null, new PaddingChange(0, -changeBy, shift), null, null, null, null);
    }//GEN-LAST:event_vMinusPaddingButtonActionPerformed

    private void vPlusTopInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vPlusTopInsetButtonActionPerformed
        int changeBy = STANDARD_SIZE_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new InsetsChange(changeBy, 0, 0, 0, false), null, null, null);
    }//GEN-LAST:event_vPlusTopInsetButtonActionPerformed

    private void vMinusTopInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vMinusTopInsetButtonActionPerformed
        int changeBy = STANDARD_SIZE_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_SIZE_CHANGE;
        boolean shift = ( evt.getModifiers() & ActionEvent.SHIFT_MASK ) != 0;
        update(-1, -1, null, null, new InsetsChange(-changeBy, 0, 0, 0, shift), null, null, null);
    }//GEN-LAST:event_vMinusTopInsetButtonActionPerformed

    private void hPlusLeftInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hPlusLeftInsetButtonActionPerformed
        int changeBy = STANDARD_SIZE_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new InsetsChange(0, changeBy, 0, 0, false), null, null, null);
    }//GEN-LAST:event_hPlusLeftInsetButtonActionPerformed

    private void hMinusLeftInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hMinusLeftInsetButtonActionPerformed
        int changeBy = STANDARD_SIZE_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_SIZE_CHANGE;
        boolean shift = ( evt.getModifiers() & ActionEvent.SHIFT_MASK ) != 0;
        update(-1, -1, null, null, new InsetsChange(0, -changeBy, 0, 0, shift), null, null, null);
    }//GEN-LAST:event_hMinusLeftInsetButtonActionPerformed

    private void hMinusRightInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hMinusRightInsetButtonActionPerformed
        int changeBy = STANDARD_SIZE_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_SIZE_CHANGE;
        boolean shift = ( evt.getModifiers() & ActionEvent.SHIFT_MASK ) != 0;
        update(-1, -1, null, null, new InsetsChange(0, 0, 0, -changeBy, shift), null, null, null);
    }//GEN-LAST:event_hMinusRightInsetButtonActionPerformed

    private void hPlusRightInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hPlusRightInsetButtonActionPerformed
        int changeBy = STANDARD_SIZE_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new InsetsChange(0, 0, 0, changeBy, false), null, null, null);
    }//GEN-LAST:event_hPlusRightInsetButtonActionPerformed

    private void vMinusBottomInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vMinusBottomInsetButtonActionPerformed
        int changeBy = STANDARD_SIZE_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_SIZE_CHANGE;
        boolean shift = ( evt.getModifiers() & ActionEvent.SHIFT_MASK ) != 0;
        update(-1, -1, null, null, new InsetsChange(0, 0, -changeBy, 0, shift), null, null, null);
    }//GEN-LAST:event_vMinusBottomInsetButtonActionPerformed

    private void vPlusBottomInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vPlusBottomInsetButtonActionPerformed
        int changeBy = STANDARD_SIZE_CHANGE;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeBy = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new InsetsChange(0, 0, changeBy, 0, false), null, null, null);
    }//GEN-LAST:event_vPlusBottomInsetButtonActionPerformed

    private void hGridRelativeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hGridRelativeButtonActionPerformed
        boolean hGridRelative = hGridRelativeButton.isSelected();
        update(-1, -1, null, null, null, null, new GridSizeChange(0, hGridRelative ? CHANGE_RELATIVE : -CHANGE_RELATIVE, 0, NO_INDIRECT_CHANGE, false), null);
    }//GEN-LAST:event_hGridRelativeButtonActionPerformed

    private void hGridRemainderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hGridRemainderButtonActionPerformed
        boolean hGridRemainder = hGridRemainderButton.isSelected();
        update(-1, -1, null, null, null, null, new GridSizeChange(0, hGridRemainder ? CHANGE_REMAINDER : -CHANGE_REMAINDER, 0, NO_INDIRECT_CHANGE, false), null);
    }//GEN-LAST:event_hGridRemainderButtonActionPerformed

    private void vGridRelativeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vGridRelativeButtonActionPerformed
        boolean vGridRelative = vGridRelativeButton.isSelected();
        update(-1, -1, null, null, null, null, new GridSizeChange(0, NO_INDIRECT_CHANGE, 0, vGridRelative ? CHANGE_RELATIVE : -CHANGE_RELATIVE, false), null);
    }//GEN-LAST:event_vGridRelativeButtonActionPerformed

    private void vGridRemainderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vGridRemainderButtonActionPerformed
        boolean vGridRemainder = vGridRemainderButton.isSelected();
        update(-1, -1, null, null, null, null, new GridSizeChange(0, NO_INDIRECT_CHANGE, 0, vGridRemainder ? CHANGE_REMAINDER : -CHANGE_REMAINDER, false), null);
    }//GEN-LAST:event_vGridRemainderButtonActionPerformed

    private void xGridRelativeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xGridRelativeButtonActionPerformed
        boolean xGridRelative = xGridRelativeButton.isSelected();
        update(-1, -1, null, null, null, new GridPositionChange(0, xGridRelative ? CHANGE_RELATIVE : -CHANGE_RELATIVE, 0, NO_INDIRECT_CHANGE, false), null, null);
    }//GEN-LAST:event_xGridRelativeButtonActionPerformed

    private void yGridRelativeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yGridRelativeButtonActionPerformed
        boolean yGridRelative = yGridRelativeButton.isSelected();
        update(-1, -1, null, null, null, new GridPositionChange(0, NO_INDIRECT_CHANGE, 0, yGridRelative ? CHANGE_RELATIVE : -CHANGE_RELATIVE, false), null, null);
    }//GEN-LAST:event_yGridRelativeButtonActionPerformed

    /** converts anchor of any type to the currently selected type
     * (baseline, bi-directional or absolute)
     * 
     * @param anchorButton anchor to be converted.
     * @return converted anchor
     */
    private int currentAnchorSpecialization(int anchorButton) {
        boolean baseline = baselineAnchorButton.isSelected();
        boolean bidi = bidiAnchorButton.isSelected();
        return convertAnchorType(baseline ? ANCHOR_BASELINE : (bidi ? ANCHOR_BIDI : ANCHOR_ABSOLUTE),anchorButton);
    }
    
    /** converts anchor of any type to any other type
     * (baseline, bi-directional or absolute)
     * 
     * @param currentAnchor anchor to be converted.
     * @param type one of ANCHOR_BASELINE, ANCHOR_BIDI, ANCHOR_ABSOLUTE
     * @return equivalent anchor of the requested type
     */
    private int convertAnchorType(int type, int currentAnchor) {
        switch(type) {
            case ANCHOR_BASELINE: {
                switch(currentAnchor) {
                    case GridBagConstraints.NORTHWEST: 
                    case GridBagConstraints.ABOVE_BASELINE_LEADING:
                    case GridBagConstraints.FIRST_LINE_START: return GridBagConstraints.ABOVE_BASELINE_LEADING;
                    case GridBagConstraints.NORTH:
                    case GridBagConstraints.ABOVE_BASELINE:
                    case GridBagConstraints.PAGE_START: return GridBagConstraints.ABOVE_BASELINE;
                    case GridBagConstraints.NORTHEAST:
                    case GridBagConstraints.ABOVE_BASELINE_TRAILING:
                    case GridBagConstraints.FIRST_LINE_END: return GridBagConstraints.ABOVE_BASELINE_TRAILING;
                    case GridBagConstraints.WEST:
                    case GridBagConstraints.BASELINE_LEADING:
                    case GridBagConstraints.LINE_START: return GridBagConstraints.BASELINE_LEADING;
                    case GridBagConstraints.CENTER:
                    case GridBagConstraints.BASELINE: return GridBagConstraints.BASELINE;
                    case GridBagConstraints.EAST:
                    case GridBagConstraints.BASELINE_TRAILING:
                    case GridBagConstraints.LINE_END: return GridBagConstraints.BASELINE_TRAILING;
                    case GridBagConstraints.SOUTHWEST:
                    case GridBagConstraints.BELOW_BASELINE_LEADING:
                    case GridBagConstraints.LAST_LINE_START: return GridBagConstraints.BELOW_BASELINE_LEADING;
                    case GridBagConstraints.SOUTH:
                    case GridBagConstraints.BELOW_BASELINE:
                    case GridBagConstraints.PAGE_END: return GridBagConstraints.BELOW_BASELINE;
                    case GridBagConstraints.SOUTHEAST:
                    case GridBagConstraints.BELOW_BASELINE_TRAILING:
                    case GridBagConstraints.LAST_LINE_END: return GridBagConstraints.BELOW_BASELINE_TRAILING;
                }
                break;
            }
            case ANCHOR_BIDI: {
                switch(currentAnchor) {
                    case GridBagConstraints.NORTHWEST: 
                    case GridBagConstraints.ABOVE_BASELINE_LEADING:
                    case GridBagConstraints.FIRST_LINE_START: return GridBagConstraints.FIRST_LINE_START;
                    case GridBagConstraints.NORTH:
                    case GridBagConstraints.ABOVE_BASELINE:
                    case GridBagConstraints.PAGE_START: return GridBagConstraints.PAGE_START;
                    case GridBagConstraints.NORTHEAST:
                    case GridBagConstraints.ABOVE_BASELINE_TRAILING:
                    case GridBagConstraints.FIRST_LINE_END: return GridBagConstraints.FIRST_LINE_END;
                    case GridBagConstraints.WEST:
                    case GridBagConstraints.BASELINE_LEADING:
                    case GridBagConstraints.LINE_START: return GridBagConstraints.LINE_START;
                    case GridBagConstraints.CENTER:
                    case GridBagConstraints.BASELINE: return GridBagConstraints.CENTER;
                    case GridBagConstraints.EAST:
                    case GridBagConstraints.BASELINE_TRAILING:
                    case GridBagConstraints.LINE_END: return GridBagConstraints.LINE_END;
                    case GridBagConstraints.SOUTHWEST:
                    case GridBagConstraints.BELOW_BASELINE_LEADING:
                    case GridBagConstraints.LAST_LINE_START: return GridBagConstraints.LAST_LINE_START;
                    case GridBagConstraints.SOUTH:
                    case GridBagConstraints.BELOW_BASELINE:
                    case GridBagConstraints.PAGE_END: return GridBagConstraints.PAGE_END;
                    case GridBagConstraints.SOUTHEAST:
                    case GridBagConstraints.BELOW_BASELINE_TRAILING:
                    case GridBagConstraints.LAST_LINE_END: return GridBagConstraints.LAST_LINE_END;
                }
                break;
            }
            case ANCHOR_ABSOLUTE: {
                switch(currentAnchor) {
                    case GridBagConstraints.NORTHWEST: 
                    case GridBagConstraints.ABOVE_BASELINE_LEADING:
                    case GridBagConstraints.FIRST_LINE_START: return GridBagConstraints.NORTHWEST;
                    case GridBagConstraints.NORTH:
                    case GridBagConstraints.ABOVE_BASELINE:
                    case GridBagConstraints.PAGE_START: return GridBagConstraints.NORTH;
                    case GridBagConstraints.NORTHEAST:
                    case GridBagConstraints.ABOVE_BASELINE_TRAILING:
                    case GridBagConstraints.FIRST_LINE_END: return GridBagConstraints.NORTHEAST;
                    case GridBagConstraints.WEST:
                    case GridBagConstraints.BASELINE_LEADING:
                    case GridBagConstraints.LINE_START: return GridBagConstraints.WEST;
                    case GridBagConstraints.CENTER:
                    case GridBagConstraints.BASELINE: return GridBagConstraints.CENTER;
                    case GridBagConstraints.EAST:
                    case GridBagConstraints.BASELINE_TRAILING:
                    case GridBagConstraints.LINE_END: return GridBagConstraints.EAST;
                    case GridBagConstraints.SOUTHWEST:
                    case GridBagConstraints.BELOW_BASELINE_LEADING:
                    case GridBagConstraints.LAST_LINE_START: return GridBagConstraints.SOUTHWEST;
                    case GridBagConstraints.SOUTH:
                    case GridBagConstraints.BELOW_BASELINE:
                    case GridBagConstraints.PAGE_END: return GridBagConstraints.SOUTH;
                    case GridBagConstraints.SOUTHEAST:
                    case GridBagConstraints.BELOW_BASELINE_TRAILING:
                    case GridBagConstraints.LAST_LINE_END: return GridBagConstraints.SOUTHEAST;
                }
                break;
            }
        }        
        return GridBagConstraints.NONE;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel anchorPanel;
    private javax.swing.JPanel anchorToolGroup;
    private javax.swing.JPanel anchorTypePanel;
    private javax.swing.JLabel bInsetLabel;
    private javax.swing.JButton bMinusInsetButton;
    private javax.swing.JButton bMinusPaddingButton;
    private javax.swing.JLabel bPadLabel;
    private javax.swing.JButton bPlusInsetButton;
    private javax.swing.JButton bPlusPaddingButton;
    private javax.swing.JToggleButton baselineAnchorButton;
    private javax.swing.JToggleButton bidiAnchorButton;
    private javax.swing.JLabel bottomLeftCorner;
    private javax.swing.JLabel bottomRightCorner;
    private javax.swing.JToggleButton cAnchorButton;
    private javax.swing.Box.Filler crossCenter;
    private javax.swing.JPanel customizer;
    private javax.swing.JToggleButton eAnchorButton;
    private javax.swing.JPanel fillPanel;
    private javax.swing.JPanel fillToolGroup;
    private javax.swing.JPanel gridPositionPanel;
    private javax.swing.JPanel gridPositionToolGroup;
    private javax.swing.JPanel gridSizePanel;
    private javax.swing.JPanel gridSizeToolGroup;
    private javax.swing.JToggleButton hFillButton;
    private javax.swing.JLabel hGridLabel;
    private javax.swing.JButton hGridMinusButton;
    private javax.swing.JButton hGridPlusButton;
    private javax.swing.JToggleButton hGridRelativeButton;
    private javax.swing.JToggleButton hGridRemainderButton;
    private javax.swing.JLabel hInsetLabel;
    private javax.swing.JButton hMinusInsetButton;
    private javax.swing.JButton hMinusLeftInsetButton;
    private javax.swing.JButton hMinusPaddingButton;
    private javax.swing.JButton hMinusRightInsetButton;
    private javax.swing.JButton hMinusWeightButton;
    private javax.swing.JLabel hPadLabel;
    private javax.swing.JButton hPlusInsetButton;
    private javax.swing.JButton hPlusLeftInsetButton;
    private javax.swing.JButton hPlusPaddingButton;
    private javax.swing.JButton hPlusRightInsetButton;
    private javax.swing.JButton hPlusWeightButton;
    private javax.swing.JButton hWeightEqualizeButton;
    private javax.swing.JLabel hWeightLabel;
    private javax.swing.JPanel insetsCross;
    private javax.swing.JPanel insetsPanel;
    private javax.swing.JPanel insetsToolGroup;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JToggleButton nAnchorButton;
    private javax.swing.JToggleButton neAnchorButton;
    private javax.swing.JToggleButton nwAnchorButton;
    private javax.swing.JPanel paddingPanel;
    private javax.swing.JPanel paddingToolGroup;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JToggleButton sAnchorButton;
    private javax.swing.JToggleButton seAnchorButton;
    private javax.swing.JToggleButton swAnchorButton;
    private javax.swing.JLabel topLeftCorner;
    private javax.swing.JLabel topRightCorner;
    private javax.swing.JToggleButton vFillButton;
    private javax.swing.JLabel vGridLabel;
    private javax.swing.JButton vGridMinusButton;
    private javax.swing.JButton vGridPlusButton;
    private javax.swing.JToggleButton vGridRelativeButton;
    private javax.swing.JToggleButton vGridRemainderButton;
    private javax.swing.JLabel vInsetLabel;
    private javax.swing.JButton vMinusBottomInsetButton;
    private javax.swing.JButton vMinusInsetButton;
    private javax.swing.JButton vMinusPaddingButton;
    private javax.swing.JButton vMinusTopInsetButton;
    private javax.swing.JButton vMinusWeightButton;
    private javax.swing.JLabel vPadLabel;
    private javax.swing.JButton vPlusBottomInsetButton;
    private javax.swing.JButton vPlusInsetButton;
    private javax.swing.JButton vPlusPaddingButton;
    private javax.swing.JButton vPlusTopInsetButton;
    private javax.swing.JButton vPlusWeightButton;
    private javax.swing.JButton vWeightEqualizeButton;
    private javax.swing.JLabel vWeightLabel;
    private javax.swing.JToggleButton wAnchorButton;
    private javax.swing.JPanel weightsPanel;
    private javax.swing.JPanel weightsToolGroup;
    private javax.swing.JButton xGridMinusButton;
    private javax.swing.JButton xGridPlusButton;
    private javax.swing.JToggleButton xGridRelativeButton;
    private javax.swing.JButton yGridMinusButton;
    private javax.swing.JButton yGridPlusButton;
    private javax.swing.JToggleButton yGridRelativeButton;
    // End of variables declaration//GEN-END:variables

    /** Modifies Anchor pushbuttons selection state to show
     * which anchor has been set for the currently selected components
     */
    public void selectAnchorButtons(int anchor)
    {
        nwAnchorButton.setSelected(anchor == GridBagConstraints.NORTHWEST
                || anchor == GridBagConstraints.FIRST_LINE_START
                || anchor == GridBagConstraints.ABOVE_BASELINE_LEADING);
        nAnchorButton.setSelected(anchor == GridBagConstraints.NORTH
                || anchor == GridBagConstraints.PAGE_START
                || anchor == GridBagConstraints.ABOVE_BASELINE);
        neAnchorButton.setSelected(anchor == GridBagConstraints.NORTHEAST
                || anchor == GridBagConstraints.FIRST_LINE_END
                || anchor == GridBagConstraints.ABOVE_BASELINE_TRAILING);
        wAnchorButton.setSelected(anchor == GridBagConstraints.WEST
                || anchor == GridBagConstraints.LINE_START
                || anchor == GridBagConstraints.BASELINE_LEADING);
        cAnchorButton.setSelected(anchor == GridBagConstraints.CENTER
                || anchor == GridBagConstraints.BASELINE);
        eAnchorButton.setSelected(anchor == GridBagConstraints.EAST
                || anchor == GridBagConstraints.LINE_END
                || anchor == GridBagConstraints.BASELINE_TRAILING);
        swAnchorButton.setSelected(anchor == GridBagConstraints.SOUTHWEST
                || anchor == GridBagConstraints.LAST_LINE_START
                || anchor == GridBagConstraints.BELOW_BASELINE_LEADING);
        sAnchorButton.setSelected(anchor == GridBagConstraints.SOUTH
                || anchor == GridBagConstraints.PAGE_END
                || anchor == GridBagConstraints.BELOW_BASELINE);
        seAnchorButton.setSelected(anchor == GridBagConstraints.SOUTHEAST
                || anchor == GridBagConstraints.LAST_LINE_END
                || anchor == GridBagConstraints.BELOW_BASELINE_TRAILING);
    }
       
    /** Updates anchor button ToolTips depending on currently active
     * anchor type: baseline, bi-directional or absolute
     */
    private void updateAnchorToolTips() {
        boolean baseline = baselineAnchorButton.isSelected();
        boolean bidi = bidiAnchorButton.isSelected();
        ResourceBundle bundle = NbBundle.getBundle(GridBagCustomizer.class);
        String key = (baseline ? "GridBagCustomizer.anchor.aboveBaselineLeading" // NOI18N
                : (bidi ? "GridBagCustomizer.anchor.firstLineStart" // NOI18N
                    : "GridBagCustomizer.anchor.northWest")); // NOI18N
        nwAnchorButton.setToolTipText(bundle.getString(key));
        key = (baseline ? "GridBagCustomizer.anchor.aboveBaseline" // NOI18N
                : (bidi ? "GridBagCustomizer.anchor.pageStart" // NOI18N
                    : "GridBagCustomizer.anchor.north")); // NOI18N
        nAnchorButton.setToolTipText(bundle.getString(key));
        key = (baseline ? "GridBagCustomizer.anchor.aboveBaselineTrailing" // NOI18N
                : (bidi ? "GridBagCustomizer.anchor.firstLineEnd" // NOI18N
                    : "GridBagCustomizer.anchor.northEast")); // NOI18N
        neAnchorButton.setToolTipText(bundle.getString(key));
        key = (baseline ? "GridBagCustomizer.anchor.baselineLeading" // NOI18N
                : (bidi ? "GridBagCustomizer.anchor.lineStart" // NOI18N
                    : "GridBagCustomizer.anchor.west")); // NOI18N
        wAnchorButton.setToolTipText(bundle.getString(key));
        key = (baseline ? "GridBagCustomizer.anchor.baseline" // NOI18N
                : "GridBagCustomizer.anchor.center"); // NOI18N
        cAnchorButton.setToolTipText(bundle.getString(key));
        key = (baseline ? "GridBagCustomizer.anchor.baselineTrailing" // NOI18N
                : (bidi ? "GridBagCustomizer.anchor.lineEnd" // NOI18N
                    : "GridBagCustomizer.anchor.east")); // NOI18N
        eAnchorButton.setToolTipText(bundle.getString(key));
        key = (baseline ? "GridBagCustomizer.anchor.belowBaselineLeading" // NOI18N
                : (bidi ? "GridBagCustomizer.anchor.lastLineStart" // NOI18N
                    : "GridBagCustomizer.anchor.southWest")); // NOI18N
        swAnchorButton.setToolTipText(bundle.getString(key));
        key = (baseline ? "GridBagCustomizer.anchor.belowBaseline" // NOI18N
                : (bidi ? "GridBagCustomizer.anchor.pageEnd" // NOI18N
                    : "GridBagCustomizer.anchor.south")); // NOI18N
        sAnchorButton.setToolTipText(bundle.getString(key));
        key = (baseline ? "GridBagCustomizer.anchor.belowBaselineTrailing" // NOI18N
                : (bidi ? "GridBagCustomizer.anchor.lastLineEnd" // NOI18N
                    : "GridBagCustomizer.anchor.southEast")); // NOI18N
        seAnchorButton.setToolTipText(bundle.getString(key));
    }
    
    /** Updates the state of those pushbuttons in the left
     * vertical tool box that need to indicate the special "ambiguous"
     * state marking that some of the currently selected components
     * have the respective property set while some do not.
     */
    private void updateButton(AbstractButton button, boolean nonEmptySelection, boolean allSelectedUnambiguous, String iconWarning, String toolTipWarning, String iconNormal, String toolTipNormal) {
        button.setSelected(allSelectedUnambiguous);
        if(nonEmptySelection && !allSelectedUnambiguous) {
            button.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/griddesigner/resources/" + iconWarning, false)); // NOI18N
            button.setToolTipText(NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer." + toolTipWarning)); // NOI18N
        } else {
            button.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/griddesigner/resources/" + iconNormal, false)); // NOI18N
            button.setToolTipText(NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer." + toolTipNormal)); // NOI18N
        }
    }

    /** Updates the state of buttons in the left
     * vertical tool box to reflect the current component(s) selection.
     */
    @Override
    public void setContext(DesignerContext context) {
        GridBagInfoProvider info = manager.getGridInfo();
        Set<Component> components = context.getSelectedComponents();

        boolean enableButtons = !components.isEmpty();
        boolean multiple = components.size() > 1;
        if(!enableButtons) {
            bidiAnchorButton.setSelected(false);
            bidiAnchorButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/griddesigner/resources/bidi.png", false)); // NOI18N
            bidiAnchorButton.setToolTipText(NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.anchor.bidiAware")); // NOI18N
            baselineAnchorButton.setSelected(false);
            baselineAnchorButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/griddesigner/resources/baseline.png", false)); // NOI18N
            baselineAnchorButton.setToolTipText(NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.anchor.baselineRelated")); // NOI18N
            selectAnchorButtons(GridBagConstraints.NONE);
            hFillButton.setSelected(false);
            hFillButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/griddesigner/resources/resize_h.png", false)); // NOI18N
            hFillButton.setToolTipText(NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.fill.horizontal")); // NOI18N
            vFillButton.setSelected(false);
            vFillButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/griddesigner/resources/resize_v.png", false)); // NOI18N
            vFillButton.setToolTipText(NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.fill.vertical")); // NOI18N
            xGridRelativeButton.setSelected(false);
            xGridRelativeButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/griddesigner/resources/gridx_relative.png", false)); // NOI18N
            xGridRelativeButton.setToolTipText(NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.xGridRelativeButton.toolTipText")); // NOI18N
            yGridRelativeButton.setSelected(false);
            yGridRelativeButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/griddesigner/resources/gridy_relative.png", false)); // NOI18N
            yGridRelativeButton.setToolTipText(NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.yGridRelativeButton.toolTipText")); // NOI18N
            hGridRelativeButton.setSelected(false);
            hGridRelativeButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/griddesigner/resources/gridwidth_relative.png", false)); // NOI18N
            hGridRelativeButton.setToolTipText(NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hGridRelativeButton.toolTipText")); // NOI18N
            vGridRelativeButton.setSelected(false);
            vGridRelativeButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/griddesigner/resources/gridheight_relative.png", false)); // NOI18N
            vGridRelativeButton.setToolTipText(NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vGridRelativeButton.toolTipText")); // NOI18N
            hGridRemainderButton.setSelected(false);
            hGridRemainderButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/griddesigner/resources/gridwidth_remainder.png", false)); // NOI18N
            hGridRemainderButton.setToolTipText(NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hGridRemainderButton.toolTipText")); // NOI18N
            vGridRemainderButton.setSelected(false);
            vGridRemainderButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/griddesigner/resources/gridheight_remainder.png", false)); // NOI18N
            vGridRemainderButton.setToolTipText(NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vGridRemainderButton.toolTipText")); // NOI18N
        }
        bidiAnchorButton.setEnabled(enableButtons);
        baselineAnchorButton.setEnabled(enableButtons);

        nwAnchorButton.setEnabled(enableButtons);
        nAnchorButton.setEnabled(enableButtons);
        neAnchorButton.setEnabled(enableButtons);
        wAnchorButton.setEnabled(enableButtons);
        cAnchorButton.setEnabled(enableButtons);
        eAnchorButton.setEnabled(enableButtons);
        swAnchorButton.setEnabled(enableButtons);
        sAnchorButton.setEnabled(enableButtons);
        seAnchorButton.setEnabled(enableButtons);

        hMinusPaddingButton.setEnabled(enableButtons);
        hPlusPaddingButton.setEnabled(enableButtons);
        vMinusPaddingButton.setEnabled(enableButtons);
        vPlusPaddingButton.setEnabled(enableButtons);
        bMinusPaddingButton.setEnabled(enableButtons);
        bPlusPaddingButton.setEnabled(enableButtons);

        vPlusInsetButton.setEnabled(enableButtons);
        vMinusInsetButton.setEnabled(enableButtons);
        hPlusInsetButton.setEnabled(enableButtons);
        hMinusInsetButton.setEnabled(enableButtons);
        bPlusInsetButton.setEnabled(enableButtons);
        bMinusInsetButton.setEnabled(enableButtons);
        vPlusTopInsetButton.setEnabled(enableButtons);
        vMinusTopInsetButton.setEnabled(enableButtons);
        vPlusBottomInsetButton.setEnabled(enableButtons);
        vMinusBottomInsetButton.setEnabled(enableButtons);
        hPlusLeftInsetButton.setEnabled(enableButtons);
        hMinusLeftInsetButton.setEnabled(enableButtons);
        hPlusRightInsetButton.setEnabled(enableButtons);
        hMinusRightInsetButton.setEnabled(enableButtons);

        hFillButton.setEnabled(enableButtons);
        vFillButton.setEnabled(enableButtons);
        
        xGridMinusButton.setEnabled(enableButtons);
        xGridPlusButton.setEnabled(enableButtons);
        yGridMinusButton.setEnabled(enableButtons);
        yGridPlusButton.setEnabled(enableButtons);
        
        if(info.hasGaps()) {
            xGridRelativeButton.setEnabled(false);
            yGridRelativeButton.setEnabled(false);
            vGridRelativeButton.setEnabled(false);
            hGridRelativeButton.setEnabled(false);
        } else {
            xGridRelativeButton.setEnabled(enableButtons);
            yGridRelativeButton.setEnabled(enableButtons);
            vGridRelativeButton.setEnabled(enableButtons);
            hGridRelativeButton.setEnabled(enableButtons);            
        }
        vGridRemainderButton.setEnabled(enableButtons);
        hGridRemainderButton.setEnabled(enableButtons);
        hGridMinusButton.setEnabled(enableButtons);
        hGridPlusButton.setEnabled(enableButtons);
        vGridMinusButton.setEnabled(enableButtons);
        vGridPlusButton.setEnabled(enableButtons);
        
        vPlusWeightButton.setEnabled(enableButtons);
        vMinusWeightButton.setEnabled(enableButtons);
        hPlusWeightButton.setEnabled(enableButtons);
        hMinusWeightButton.setEnabled(enableButtons);
        
        hWeightEqualizeButton.setEnabled(multiple);
        vWeightEqualizeButton.setEnabled(multiple);

        if(enableButtons) {
            /* set fill/bidi/base buttons as selected only if all selected components have the respective property set */
            int bidi = 0, baseline = 0, center = 0;
            boolean tlAnchor = false, tcAnchor = false, trAnchor = false;
            boolean lAnchor = false, cAnchor = false, rAnchor = false;
            boolean blAnchor = false, bcAnchor = false, brAnchor = false;
            int hFill = 0, vFill = 0;
            int gridXRelative = 0, gridYRelative = 0;
            int gridWRelative = 0, gridHRelative = 0;
            int gridWRemainder = 0, gridHRemainder = 0;
            for(Component component : components) {
                int fill = info.getFill(component);
                if(fill == GridBagConstraints.HORIZONTAL || fill == GridBagConstraints.BOTH) hFill++;
                if(fill == GridBagConstraints.VERTICAL || fill == GridBagConstraints.BOTH) vFill++;
                if(info.getGridXRelative(component)) gridXRelative++;
                if(info.getGridYRelative(component)) gridYRelative++;
                if(info.getGridWidthRelative(component)) gridWRelative++;
                if(info.getGridHeightRelative(component)) gridHRelative++;
                if(info.getGridWidthRemainder(component)) gridWRemainder++;
                if(info.getGridHeightRemainder(component)) gridHRemainder++;
                int anchor = info.getAnchor(component);
                switch(anchor) {
                    // baseline anchors
                    case GridBagConstraints.ABOVE_BASELINE_LEADING: tlAnchor = true; baseline++; break;
                    case GridBagConstraints.ABOVE_BASELINE: tcAnchor = true; baseline++; break;
                    case GridBagConstraints.ABOVE_BASELINE_TRAILING: trAnchor = true; baseline++; break;
                    case GridBagConstraints.BASELINE_LEADING: lAnchor = true; baseline++; break;
                    case GridBagConstraints.BASELINE: cAnchor = true; baseline++; break;
                    case GridBagConstraints.BASELINE_TRAILING: rAnchor = true; baseline++; break;
                    case GridBagConstraints.BELOW_BASELINE_LEADING: blAnchor = true; baseline++; break;
                    case GridBagConstraints.BELOW_BASELINE: bcAnchor = true; baseline++; break;
                    case GridBagConstraints.BELOW_BASELINE_TRAILING: brAnchor = true; baseline++; break;
                    // bidirectional anchors
                    case GridBagConstraints.FIRST_LINE_START: tlAnchor = true; bidi++; break;
                    case GridBagConstraints.PAGE_START: tcAnchor = true; bidi++; break;
                    case GridBagConstraints.FIRST_LINE_END: trAnchor = true; bidi++; break;
                    case GridBagConstraints.LINE_START: lAnchor = true; bidi++; break;
                    case GridBagConstraints.LINE_END: rAnchor = true; bidi++; break;
                    case GridBagConstraints.LAST_LINE_START: blAnchor = true; bidi++; break;
                    case GridBagConstraints.PAGE_END: bcAnchor = true; bidi++; break;
                    case GridBagConstraints.LAST_LINE_END: brAnchor = true; bidi++; break;
                    // absolute anchors
                    case GridBagConstraints.NORTHWEST: tlAnchor = true; break;
                    case GridBagConstraints.NORTH: tcAnchor = true; break;
                    case GridBagConstraints.NORTHEAST: trAnchor = true; break;
                    case GridBagConstraints.WEST: lAnchor = true; break;
                    case GridBagConstraints.CENTER: cAnchor = true; center++; break;
                    case GridBagConstraints.EAST: rAnchor = true; break;
                    case GridBagConstraints.SOUTHWEST: blAnchor = true; break;
                    case GridBagConstraints.SOUTH: bcAnchor = true; break;
                    case GridBagConstraints.SOUTHEAST: brAnchor = true; break;
                }
            }
            nwAnchorButton.setSelected(tlAnchor);
            nAnchorButton.setSelected(tcAnchor);
            neAnchorButton.setSelected(trAnchor);
            wAnchorButton.setSelected(lAnchor);
            cAnchorButton.setSelected(cAnchor);
            eAnchorButton.setSelected(rAnchor);
            swAnchorButton.setSelected(blAnchor);
            sAnchorButton.setSelected(bcAnchor);
            seAnchorButton.setSelected(brAnchor);

            if(center != components.size()) {
                if(baseline + bidi == 0) {
                    bidiCenter = false;
                } else {
                    bidiCenter = true;
                }
            }
            if(baseline + bidi > 0 && baseline + bidi + center != components.size()) {
                bidiAnchorButton.setSelected(false);
                bidiAnchorButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/griddesigner/resources/bidi_warning.png", false)); // NOI18N
                bidiAnchorButton.setToolTipText(NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.anchor.bidiAwareWarning")); // NOI18N
            } else {
                bidiAnchorButton.setSelected(bidiCenter);
                bidiAnchorButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/griddesigner/resources/bidi.png", false)); // NOI18N
                bidiAnchorButton.setToolTipText(NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.anchor.bidiAware")); // NOI18N
            }
            updateButton(baselineAnchorButton, baseline > 0, baseline == components.size(), 
                         "baseline_warning.png", "anchor.baselineRelatedWarning", 
                         "baseline.png", "anchor.baselineRelated"); // NOI18N
            updateButton(hFillButton, hFill > 0, hFill == components.size(), 
                         "resize_h_warning.png", "fill.horizontalWarning", 
                         "resize_h.png", "fill.horizontal"); // NOI18N
            updateButton(vFillButton, vFill > 0, vFill == components.size(), 
                         "resize_v_warning.png", "fill.verticalWarning", 
                         "resize_v.png", "fill.vertical"); // NOI18N
            updateButton(xGridRelativeButton, gridXRelative > 0, gridXRelative == components.size(), 
                         "gridx_relative_warning.png", "xGridRelativeButtonWarning.toolTipText", 
                         "gridx_relative.png", "xGridRelativeButton.toolTipText"); // NOI18N
            updateButton(yGridRelativeButton, gridYRelative > 0, gridYRelative == components.size(), 
                         "gridy_relative_warning.png", "yGridRelativeButtonWarning.toolTipText", 
                         "gridy_relative.png", "yGridRelativeButton.toolTipText"); // NOI18N
            updateButton(hGridRelativeButton, gridWRelative > 0, gridWRelative == components.size(), 
                         "gridwidth_relative_warning.png", "hGridRelativeButtonWarning.toolTipText", 
                         "gridwidth_relative.png", "hGridRelativeButton.toolTipText"); // NOI18N
            updateButton(vGridRelativeButton, gridHRelative > 0, gridHRelative == components.size(), 
                         "gridheight_relative_warning.png", "vGridRelativeButtonWarning.toolTipText", 
                         "gridheight_relative.png", "vGridRelativeButton.toolTipText"); // NOI18N
            updateButton(hGridRemainderButton, gridWRemainder > 0, gridWRemainder == components.size(), 
                         "gridwidth_remainder_warning.png", "hGridRemainderButtonWarning.toolTipText", 
                         "gridwidth_remainder.png", "hGridRemainderButton.toolTipText"); // NOI18N
            updateButton(vGridRemainderButton, gridHRemainder > 0, gridHRemainder == components.size(), 
                         "gridheight_remainder_warning.png", "vGridRemainderButtonWarning.toolTipText", 
                         "gridheight_remainder.png", "vGridRemainderButton.toolTipText"); // NOI18N
        }
        updateAnchorToolTips();
    }

    /** Updates property or multiple properties of currently selected components.
     * Called when a property affecting button is pressed.
     */
    private void update(final int anchorType, final int anchor, final FillChange fill, final PaddingChange iPad, final InsetsChange insets, 
                        final GridPositionChange gridPos, final GridSizeChange gridSize, final WeightChange weight) {
        performer.performAction(new AbstractGridAction() {
            @Override
            public GridBoundsChange performAction(GridManager gridManager, DesignerContext context) {
                GridBagManager gridBagManager = (GridBagManager) gridManager;
                GridBagInfoProvider info = gridBagManager.getGridInfo();
                int columns = info.getColumnCount();
                int rows = info.getRowCount();
                double avgX = 0.0d;
                double avgY = 0.0d;

                GridUtils.removePaddingComponents(gridManager);
                gridManager.updateLayout(false);
                GridUtils.revalidateGrid(gridManager);

                if ( weight != null && (weight.xNorm != 0 || weight.yNorm != 0) ) {
                    double noOfComponents = (double)context.getSelectedComponents().size();
                    if(noOfComponents > 0) {
                        for(Component component : context.getSelectedComponents()) {
                            avgX += info.getWeightX(component);
                            avgY += info.getWeightY(component);
                        }
                        avgX /= noOfComponents;
                        avgY /= noOfComponents;
                    }
                }
                // to preserve relative positions of components in group when shifting group up or left
                GridPositionChange gridPosCorrected = gridPos;
                if(gridPos != null &&
                        ((gridPos.xDiff < 0 && gridPos.xRelative == NO_INDIRECT_CHANGE) || 
                        (gridPos.yDiff < 0 && gridPos.yRelative == NO_INDIRECT_CHANGE)) ) {
                    int xDiff = gridPos.xDiff;
                    int yDiff = gridPos.yDiff;
                    for(Component component : context.getSelectedComponents()) {
                        int x = info.getGridX(component);
                        int y = info.getGridY(component);
                        if(x + xDiff < 0 && gridPos.xRelative == NO_INDIRECT_CHANGE) {
                            xDiff = -x;
                        }
                        if(y + yDiff < 0 && gridPos.yRelative == NO_INDIRECT_CHANGE) {
                            yDiff = -y;
                        }
                    }
                    if(xDiff != gridPos.xDiff || yDiff != gridPos.yDiff) {
                        gridPosCorrected = new GridPositionChange(xDiff, gridPos.xRelative, yDiff, gridPos.yRelative, gridPos.reset);
                    }
                }
                // now update component properties
                for(Component component : context.getSelectedComponents()) {
                    if (anchor != -1) {
                        gridBagManager.setAnchor(component, anchor);
                    }
                    if (anchorType != -1) {
                        int oldanchor = info.getAnchor(component);
                        int newanchor = convertAnchorType(anchorType,oldanchor);
                        if (newanchor != oldanchor) {
                            gridBagManager.setAnchor(component, newanchor);
                        }
                    }
                    if (iPad != null) {
                        updateIPadding(gridBagManager, component, iPad);
                    }
                    if (gridSize != null) {
                        updateGridSize(gridBagManager, info, component, gridSize);
                    }
                    if (gridPosCorrected != null) {
                        updateGridPosition(gridBagManager, info, component, gridPosCorrected);
                    }
                    if (insets != null) {
                        updateInsets(gridBagManager, component, insets);
                    }
                    if (fill != null) {
                        if (fill.hFill != -1) gridBagManager.setHorizontalFill(component, fill.hFill == 1);
                        if (fill.vFill != -1) gridBagManager.setVerticalFill(component, fill.vFill == 1);
                    }
                    if (weight != null) {
                        updateWeight(gridBagManager, info, component, weight, avgX, avgY);
                    }
                }
                gridManager.updateLayout(false);
                GridUtils.revalidateGrid(gridManager);
                gridManager.updateGaps(false);
                int newColumns = info.getColumnCount();
                int newRows = info.getRowCount();
                int padColumns = Math.max(columns, newColumns);
                int padRows = Math.max(rows, newRows);
                GridUtils.addPaddingComponents(gridManager, padColumns, padRows);
                GridUtils.revalidateGrid(gridManager);
                return null;
            }
        });
    }

    /** Updates component internal padding based on PaddingChange
     */
    private void updateIPadding(GridBagManager gridBagManager, Component component, PaddingChange iPad) {
        if (iPad.xDiff != 0) {
            if (iPad.reset) {
                gridBagManager.setIPadX(component, 0);
            } else {
                gridBagManager.updateIPadX(component, iPad.xDiff);
            }
        }
        if (iPad.yDiff != 0) {
            if (iPad.reset) {
                gridBagManager.setIPadY(component, 0);
            } else {
                gridBagManager.updateIPadY(component, iPad.yDiff);
            }
        }
    }
    
    /** Updates component grid size based on GridSizeChange
     */
    private void updateGridSize(GridBagManager gridBagManager, GridBagInfoProvider info, Component component, GridSizeChange gridSize) {
        boolean gapSupport = info.hasGaps();
        if (gridSize.wRelRem == NO_INDIRECT_CHANGE) {
            if (gridSize.wDiff != 0) {
                if (gridSize.reset) {
                    gridBagManager.setGridWidth(component, 1);
                } else {
                    int wDiff = gridSize.wDiff;
                    if(gapSupport) {
                        wDiff *= 2;
                        if(info.isGapColumn(info.getGridX(component) + info.getGridWidth(component) - 1 + wDiff)) {
                            if(wDiff < 0) {
                                wDiff++;
                            } else {
                                wDiff--;
                            }
                        }
                    }
                    gridBagManager.updateGridWidth(component, wDiff);
                }
            }
        } else {
            if (gridSize.wRelRem < 0) { // replace RELATIVE / REMAINDER by current absolute width
                gridBagManager.setGridWidth(component, info.getGridWidth(component));
            } else if (gridSize.wRelRem == CHANGE_RELATIVE) {
                gridBagManager.setGridWidth(component, GridBagConstraints.RELATIVE);
            } else { // gridSize.wrelrem == CHANGE_REMAINDER
                gridBagManager.setGridWidth(component, GridBagConstraints.REMAINDER);
            }
        }
        if (gridSize.hRelRem == NO_INDIRECT_CHANGE) {
            if (gridSize.hDiff != 0) {
                if (gridSize.reset) {
                    gridBagManager.setGridHeight(component, 1);
                } else {
                    int hDiff = gridSize.hDiff;
                    if(gapSupport) {
                        hDiff *= 2;
                        if(info.isGapRow(info.getGridY(component) + info.getGridHeight(component) - 1 + hDiff)) {
                            if(hDiff < 0) {
                                hDiff++;
                            } else {
                                hDiff--;
                            }
                        }
                    }
                    gridBagManager.updateGridHeight(component, hDiff);
                }
            }
        } else {
            if (gridSize.hRelRem < 0) { // replace RELATIVE / REMAINDER by current absolute height
                gridBagManager.setGridHeight(component, info.getGridHeight(component));
            } else if (gridSize.hRelRem == CHANGE_RELATIVE) {
                gridBagManager.setGridHeight(component, GridBagConstraints.RELATIVE);
            } else { // gridSize.hrelrem == CHANGE_REMAINDER
                gridBagManager.setGridHeight(component, GridBagConstraints.REMAINDER);
            }
        }
    }

    /** Updates component grid position based on GridPositionChange
     */
    private void updateGridPosition(GridBagManager gridBagManager, GridBagInfoProvider info, Component component, GridPositionChange gridPos) {
        boolean gapSupport = info.hasGaps();
        if (gridPos.xRelative == NO_INDIRECT_CHANGE) {
            if (gridPos.xDiff != 0) {
                if (gridPos.reset) {
                    gridBagManager.setGridX(component, 0);
                } else {
                    int xDiff = gridPos.xDiff;
                    if(gapSupport) {
                        xDiff *= 2;
                        if(info.isGapColumn(info.getGridX(component) + xDiff)) {
                            if(xDiff < 0) {
                                xDiff++;
                            } else {
                                xDiff--;
                            }
                        }
                    }
                    gridBagManager.updateGridX(component, xDiff);
                }
            }
        } else {
            if (gridPos.xRelative == -CHANGE_RELATIVE) { // replace RELATIVE by current absolute position
                gridBagManager.setGridX(component, info.getGridX(component));
            } else { // gridPos.xrelative == CHANGE_RELATIVE
                gridBagManager.setGridX(component, GridBagConstraints.RELATIVE);
            }
        }
        if (gridPos.yRelative == NO_INDIRECT_CHANGE) {
            if (gridPos.yDiff != 0) {
                if (gridPos.reset) {
                    gridBagManager.setGridY(component, 0);
                } else {
                    int yDiff = gridPos.yDiff;
                    if(gapSupport) {
                        yDiff *= 2;
                        if(info.isGapRow(info.getGridY(component) + yDiff)) {
                            if(yDiff < 0) {
                                yDiff++;
                            } else {
                                yDiff--;
                            }
                        }
                    }
                    gridBagManager.updateGridY(component, yDiff);
                }
            }
        } else {
            if (gridPos.yRelative == -CHANGE_RELATIVE) { // replace RELATIVE by current absolute position
                gridBagManager.setGridY(component, info.getGridY(component));
            } else { // gridPos.yrelative == CHANGE_RELATIVE
                gridBagManager.setGridY(component, GridBagConstraints.RELATIVE);
            }
        }
    }

    /** Updates component insets based on InsetsChange
     */
    private void updateInsets(GridBagManager gridBagManager, Component component, InsetsChange insets) {
        if (insets.iDiff.top != 0 || insets.iDiff.left != 0 || insets.iDiff.bottom != 0 || insets.iDiff.right != 0) {
            if (insets.reset) {
                gridBagManager.resetInsets(component, insets.iDiff.top != 0, insets.iDiff.left != 0, insets.iDiff.bottom != 0, insets.iDiff.right != 0);
            } else {
                gridBagManager.updateInsets(component, insets.iDiff);
            }
        }
    }

    /** Updates component weight based on WeightChange
     */
    private void updateWeight(GridBagManager gridBagManager, GridBagInfoProvider info, Component component, WeightChange weight, double avgX, double avgY) {
        if (weight.xNorm == NO_INDIRECT_CHANGE) {
            if (weight.xDiff != 0.0d) {
                if (weight.reset) {
                    gridBagManager.setWeightX(component, 0.0d);
                } else {
                    double xWeight = info.getWeightX(component) + weight.xDiff > 0.0d ? info.getWeightX(component) + weight.xDiff : 0.0d;
                    gridBagManager.setWeightX(component, Math.floor(WEIGHT_NUMERIC_PRECISION * xWeight + 0.5d) / WEIGHT_NUMERIC_PRECISION);
                }
            }
        } else {
            double xWeight = 0.0d;
            if (weight.xNorm == WEIGHTS_EQUALIZE) {
                xWeight = avgX;
            }
            gridBagManager.setWeightX(component, Math.floor(WEIGHT_NUMERIC_PRECISION * xWeight + 0.5d) / WEIGHT_NUMERIC_PRECISION);
        }
        if (weight.yNorm == NO_INDIRECT_CHANGE) {
            if (weight.yDiff != 0.0d) {
                if (weight.reset) {
                    gridBagManager.setWeightY(component, 0.0d);
                } else {
                    double yWeight = info.getWeightY(component) + weight.yDiff > 0.0d ? info.getWeightY(component) + weight.yDiff : 0.0d;
                    gridBagManager.setWeightY(component, Math.floor(WEIGHT_NUMERIC_PRECISION * yWeight + 0.5d) / WEIGHT_NUMERIC_PRECISION);
                }
            }
        } else {
            double yWeight = 0.0d;
            if (weight.yNorm == WEIGHTS_EQUALIZE) {
                yWeight = avgY;
            }
            gridBagManager.setWeightY(component, Math.floor(WEIGHT_NUMERIC_PRECISION * yWeight + 0.5d) / WEIGHT_NUMERIC_PRECISION);
        }
    }        

}
