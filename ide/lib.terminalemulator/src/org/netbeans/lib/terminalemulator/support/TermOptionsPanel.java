/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.lib.terminalemulator.support;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.border.Border;
import org.netbeans.lib.terminalemulator.LineDiscipline;
import org.netbeans.lib.terminalemulator.Term;

// TODO seems to be the older copy of ide/terminal.nb/src/org/netbeans/modules/terminal/nb/TermOptionsPanel.java
// public via friend dependencies.
public final class TermOptionsPanel extends javax.swing.JPanel {

    private TermOptions termOptions;
    private final Term term;

    // Why variables???
    // These are used in gui-builder-generated code
    // If we use automatic bundle usage in the gui-builder it will
    // use o.n.openide.NbBundel but we're ina "pure" module and don't
    // want to be polluted with NB dependencies
    // If we use cusom code and inline the Catalog.get() then there's
    // no opportunity to insert a // NOI18N.
    //static final String LBL_ChooseForegroundColor = Catalog.get("LBL_ChooseForegroundColor");	// NOI18N
    //static final String LBL_ChooseBackgroundColor = Catalog.get("LBL_ChooseBackgroundColor");	// NOI18N
    //static final String LBL_ChooseSelectionBackgroundColor = Catalog.get("LBL_ChooseSelectionBackgroundColor");	// NOI18N
    static final String LBL_Options = Catalog.get("LBL_Options");	// NOI18N
    static final String CTL_Restore = Catalog.get("CTL_Restore");	// NOI18N
    static final String LBL_Font = Catalog.get("LBL_Font");	// NOI18N
    static final String CTL_Ellipsis = Catalog.get("CTL_Ellipsis");	// NOI18N
    static final String LBL_FontSize = Catalog.get("LBL_FontSize");	// NOI18N
    static final String LBL_ForegroundColor = Catalog.get("LBL_ForegroundColor");	// NOI18N
    static final String LBL_BackgroundColor = Catalog.get("LBL_BackgroundColor");	// NOI18N
    static final String LBL_SelectionBackgroundColor = Catalog.get("LBL_SelectionBackgroundColor");	// NOI18N
    static final String LBL_HistorySize = Catalog.get("LBL_HistorySize");	// NOI18N
    static final String LBL_TabSize = Catalog.get("LBL_TabSize");	// NOI18N
    static final String CTL_ClickToType = Catalog.get("CTL_ClickToType");	// NOI18N
    static final String CTL_ScrollOnInput = Catalog.get("CTL_ScrollOnInput");	// NOI18N
    static final String CTL_ScrollOnOutput = Catalog.get("CTL_ScrollOnOutput");	// NOI18N
    static final String LBL_WrapLines = Catalog.get("LBL_WrapLines");	// NOI18N
    static final String LBL_Preview = Catalog.get("LBL_Preview");	// NOI18N
    static final String LBL_IgnoreKeymap = Catalog.get("LBL_IgnoreKeymap");	// NOI18N
    
    private static final int MNM_Restore = Catalog.mnemonic("MNM_Restore"); //NOI18N
    private static final int MNM_Font = Catalog.mnemonic("MNM_Font"); //NOI18N
    private static final int MNM_FontSize = Catalog.mnemonic("MNM_FontSize"); //NOI18N
    private static final int MNM_ForegroundColor = Catalog.mnemonic("MNM_ForegroundColor"); //NOI18N
    private static final int MNM_BackgroundColor = Catalog.mnemonic("MNM_BackgroundColor"); //NOI18N
    private static final int MNM_SelectionBackgroundColor = Catalog.mnemonic("MNM_SelectionBackgroundColor"); //NOI18N
    private static final int MNM_HistorySize = Catalog.mnemonic("MNM_HistorySize"); //NOI18N
    private static final int MNM_TabSize = Catalog.mnemonic("MNM_TabSize"); //NOI18N
    private static final int MNM_ClickToType = Catalog.mnemonic("MNM_ClickToType"); //NOI18N
    private static final int MNM_ScrollOnInput = Catalog.mnemonic("MNM_ScrollOnInput"); //NOI18N
    private static final int MNM_ScrollOnOutput = Catalog.mnemonic("MNM_ScrollOnOutput"); //NOI18N
    private static final int MNM_WrapLines = Catalog.mnemonic("MNM_WrapLines"); //NOI18N
    private static final int MNM_IgnoreKeymap = Catalog.mnemonic("MNM_IgnoreKeymap"); //NOI18N
    private static final int MNM_Preview = Catalog.mnemonic("MNM_Preview"); //NOI18N
    private boolean inApplyingModel;

    /** Creates new form TermOptionsPanel */
    public TermOptionsPanel() {
        initComponents();        
        
	ColorComboBox.init (foregroundComboBox);
        ColorComboBox.init (backgroundComboBox);
        ColorComboBox.init (selectionComboBox);

        term = new Term();
        final String line1String = Catalog.get("MSG_Hello") + "\r\n";	// NOI18N
        final char line1[] = line1String.toCharArray();
        term.putChars(line1, 0, line1.length);

        Border termBorder = BorderFactory.createLoweredBevelBorder();
        term.setBorder(termBorder);
        term.pushStream(new LineDiscipline());
        term.setRowsColumns(7, 60);
        term.setClickToType(true);

        previewPanel.add(term, BorderLayout.CENTER);

    }

    private final PropertyChangeListener propertyListener =
        new PropertyChangeListener() {
	@Override
            public void propertyChange(PropertyChangeEvent e) {
		refreshView();
            }
        };

    /**
     * Set the model for this view.
     * Changes in the panel are directly reflected in this model which may be
     * {@link TermOptions#assign}ed later.
     * @param termOptions
     */
    public void setTermOptions(TermOptions termOptions) {

        if (this.termOptions != null)
            this.termOptions.removePropertyChangeListener(propertyListener);

        this.termOptions = termOptions;

        if (this.termOptions != null)
            this.termOptions.addPropertyChangeListener(propertyListener);

	refreshView();

    }

    private void refreshView() {
        if (termOptions != null)
            termOptions.removePropertyChangeListener(propertyListener);

	try {
	    applyTermOptions();
	} finally {
	    if (termOptions != null)
		termOptions.addPropertyChangeListener(propertyListener);
	}
        previewTermOptions();
    }

    /**
     * Transfer model values to view widgets.
     */
    private void applyTermOptions() {
        inApplyingModel = true;
        try {
            fontSizeSpinner.setValue(termOptions.getFontSize());
            fontText.setText(termOptions.getFont().getFamily()
                    + " " + // NOI18N
                    termOptions.getFont().getSize());
            ColorComboBox.setColor(foregroundComboBox, termOptions.getForeground());
            ColorComboBox.setColor(backgroundComboBox, termOptions.getBackground());
            ColorComboBox.setColor(selectionComboBox, termOptions.getSelectionBackground());
            historySizeSpinner.setValue(termOptions.getHistorySize());
            tabSizeSpinner.setValue(termOptions.getTabSize());
            clickToTypeCheckBox.setSelected(termOptions.getClickToType());
            scrollOnInputCheckBox.setSelected(termOptions.getScrollOnInput());
            scrollOnOutputCheckBox.setSelected(termOptions.getScrollOnOutput());
            lineWrapCheckBox.setSelected(termOptions.getLineWrap());
            ignoreKeymapCheckBox.setSelected(termOptions.getIgnoreKeymap());
        } finally {
            inApplyingModel = false;
        }
    }

    /**
     * Adjust dialog size and layout.
     * *
     * If the chosen font size is >= 14 the term preview area grows too
     * large for the dialog (I think). The result is that the term preview
     * area, button sizes and the font name textarea all shrink to a point.
     *
     * This is an attemt to force the dialog to resize itself but it doesn't work.
     */
    private void patchSizes() {
        term.invalidate();
        previewPanel.validate();

        previewPanel.invalidate();
        this.validate();

        this.invalidate();

//        Component p = getParent();
//        while (p != null) {
//            if (p instanceof JDialog) {
//                ((JDialog) p).pack();
//                break;
//            }
//            p = p.getParent();
//        }
    }

    /**
     * Apply current models values to the preview area Term.
     */
    private void previewTermOptions() {
        if (term == null)
            return;

        term.setFixedFont(true);
        term.setFont(termOptions.getFont());

        term.setBackground(termOptions.getBackground());
        term.setForeground(termOptions.getForeground());
        term.setHighlightColor(termOptions.getSelectionBackground());
        term.setHistorySize(termOptions.getHistorySize());
        term.setTabSize(termOptions.getTabSize());

        term.setClickToType(termOptions.getClickToType());
        term.setScrollOnInput(termOptions.getScrollOnInput());
        term.setScrollOnOutput(termOptions.getScrollOnOutput());
        term.setHorizontallyScrollable(!termOptions.getLineWrap());

        term.setRowsColumns(7, 60);

        patchSizes();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        descriptionLabel = new javax.swing.JLabel();
        restoreButton = new javax.swing.JButton();
        fontLabel = new javax.swing.JLabel();
        fontText = new javax.swing.JTextField();
        fontButton = new javax.swing.JButton();
        fontSizeLabel = new javax.swing.JLabel();
        fontSizeSpinner = new javax.swing.JSpinner();
        foregroundLabel = new javax.swing.JLabel();
        foregroundComboBox = new javax.swing.JComboBox<ColorValue>();
        backgroundLabel = new javax.swing.JLabel();
        backgroundComboBox = new javax.swing.JComboBox<ColorValue>();
        selectionLabel = new javax.swing.JLabel();
        selectionComboBox = new javax.swing.JComboBox<ColorValue>();
        historySizeLabel = new javax.swing.JLabel();
        historySizeSpinner = new javax.swing.JSpinner();
        tabSizeLabel = new javax.swing.JLabel();
        tabSizeSpinner = new javax.swing.JSpinner();
        clickToTypeCheckBox = new javax.swing.JCheckBox();
        scrollOnInputCheckBox = new javax.swing.JCheckBox();
        scrollOnOutputCheckBox = new javax.swing.JCheckBox();
        lineWrapCheckBox = new javax.swing.JCheckBox();
        ignoreKeymapCheckBox = new javax.swing.JCheckBox();
        previewLabel = new javax.swing.JLabel();
        previewPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        descriptionLabel.setText(LBL_Options);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
        add(descriptionLabel, gridBagConstraints);

        restoreButton.setMnemonic(MNM_Restore);
        restoreButton.setText(CTL_Restore);
        restoreButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                restoreActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
        add(restoreButton, gridBagConstraints);

        fontLabel.setDisplayedMnemonic(MNM_Font);
        fontLabel.setText(LBL_Font);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
        add(fontLabel, gridBagConstraints);

        fontText.setColumns(20);
        fontText.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
        add(fontText, gridBagConstraints);

        fontButton.setText(CTL_Ellipsis);
        fontButton.setMaximumSize(new java.awt.Dimension(20, 20));
        fontButton.setMinimumSize(new java.awt.Dimension(20, 20));
        fontButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseFont(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
        add(fontButton, gridBagConstraints);

        fontSizeLabel.setDisplayedMnemonic(MNM_FontSize);
        fontSizeLabel.setLabelFor(fontSizeSpinner);
        fontSizeLabel.setText(LBL_FontSize);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
        add(fontSizeLabel, gridBagConstraints);

        fontSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(12, 8, 48, 1));
        fontSizeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fontSizeSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
        add(fontSizeSpinner, gridBagConstraints);

        foregroundLabel.setDisplayedMnemonic(MNM_ForegroundColor);
        foregroundLabel.setLabelFor(foregroundComboBox);
        foregroundLabel.setText(LBL_ForegroundColor);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
        add(foregroundLabel, gridBagConstraints);

        foregroundComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                foregroundComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
        add(foregroundComboBox, gridBagConstraints);

        backgroundLabel.setDisplayedMnemonic(MNM_BackgroundColor);
        backgroundLabel.setLabelFor(backgroundComboBox);
        backgroundLabel.setText(LBL_BackgroundColor);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
        add(backgroundLabel, gridBagConstraints);

        backgroundComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backgroundComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
        add(backgroundComboBox, gridBagConstraints);

        selectionLabel.setDisplayedMnemonic(MNM_SelectionBackgroundColor);
        selectionLabel.setLabelFor(selectionComboBox);
        selectionLabel.setText(LBL_SelectionBackgroundColor);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
        add(selectionLabel, gridBagConstraints);

        selectionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectionComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
        add(selectionComboBox, gridBagConstraints);

        historySizeLabel.setDisplayedMnemonic(MNM_HistorySize);
        historySizeLabel.setLabelFor(historySizeSpinner);
        historySizeLabel.setText(LBL_HistorySize);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
        add(historySizeLabel, gridBagConstraints);

        historySizeSpinner.setModel(new javax.swing.SpinnerNumberModel(4000, 0, 50000, 10));
        historySizeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                historySizeSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
        add(historySizeSpinner, gridBagConstraints);

        tabSizeLabel.setDisplayedMnemonic(MNM_TabSize);
        tabSizeLabel.setLabelFor(tabSizeSpinner);
        tabSizeLabel.setText(LBL_TabSize);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
        add(tabSizeLabel, gridBagConstraints);

        tabSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(5, 1, 16, 1));
        tabSizeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabSizeSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
        add(tabSizeSpinner, gridBagConstraints);

        clickToTypeCheckBox.setMnemonic(MNM_ClickToType);
        clickToTypeCheckBox.setText(CTL_ClickToType);
        clickToTypeCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clickToTypeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
        add(clickToTypeCheckBox, gridBagConstraints);

        scrollOnInputCheckBox.setMnemonic(MNM_ScrollOnInput);
        scrollOnInputCheckBox.setText(CTL_ScrollOnInput);
        scrollOnInputCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scrollOnInputActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
        add(scrollOnInputCheckBox, gridBagConstraints);

        scrollOnOutputCheckBox.setMnemonic(MNM_ScrollOnOutput);
        scrollOnOutputCheckBox.setText(CTL_ScrollOnOutput);
        scrollOnOutputCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scrollOnOutputActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
        add(scrollOnOutputCheckBox, gridBagConstraints);

        lineWrapCheckBox.setMnemonic(MNM_WrapLines);
        lineWrapCheckBox.setText(LBL_WrapLines);
        lineWrapCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lineWrapActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
        add(lineWrapCheckBox, gridBagConstraints);

        ignoreKeymapCheckBox.setMnemonic(MNM_IgnoreKeymap);
        ignoreKeymapCheckBox.setText(LBL_IgnoreKeymap);
        ignoreKeymapCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ignoreKeymapCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
        add(ignoreKeymapCheckBox, gridBagConstraints);

        previewLabel.setDisplayedMnemonic(MNM_Preview);
        previewLabel.setLabelFor(previewPanel);
        previewLabel.setText(LBL_Preview);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(previewLabel, gridBagConstraints);

        previewPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        previewPanel.setPreferredSize(new java.awt.Dimension(400, 50));
        previewPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
        add(previewPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void restoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restoreActionPerformed
        termOptions.resetToDefault();
    }//GEN-LAST:event_restoreActionPerformed

    private void fontSizeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fontSizeSpinnerStateChanged
        if (inApplyingModel) {
            return;
        }
        Object fontSizeObj = fontSizeSpinner.getValue();
        if (fontSizeObj instanceof Integer) {
            int fontSize = ((Integer) fontSizeObj).intValue();
            termOptions.setFontSize(fontSize);
        }
    }//GEN-LAST:event_fontSizeSpinnerStateChanged

    private void historySizeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_historySizeSpinnerStateChanged
        if (inApplyingModel) {
            return;
        }
        Object historySizeObj = historySizeSpinner.getValue();
        if (historySizeObj instanceof Integer) {
            int historySize = ((Integer) historySizeObj).intValue();
            termOptions.setHistorySize(historySize);
        }
}//GEN-LAST:event_historySizeSpinnerStateChanged

    private void tabSizeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabSizeSpinnerStateChanged
        if (inApplyingModel) {
            return;
        }
        Object tabSizeObj = tabSizeSpinner.getValue();
        if (tabSizeObj instanceof Integer) {
            int tabSize = ((Integer) tabSizeObj).intValue();
            termOptions.setTabSize(tabSize);
        }
}//GEN-LAST:event_tabSizeSpinnerStateChanged

    private void clickToTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clickToTypeActionPerformed
        if (inApplyingModel) {
            return;
        }
        termOptions.setClickToType(clickToTypeCheckBox.isSelected());
    }//GEN-LAST:event_clickToTypeActionPerformed

    private void scrollOnInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scrollOnInputActionPerformed
        if (inApplyingModel) {
            return;
        }
        termOptions.setScrollOnInput(scrollOnInputCheckBox.isSelected());
}//GEN-LAST:event_scrollOnInputActionPerformed

    private void scrollOnOutputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scrollOnOutputActionPerformed
        if (inApplyingModel) {
            return;
        }
        termOptions.setScrollOnOutput(scrollOnOutputCheckBox.isSelected());
}//GEN-LAST:event_scrollOnOutputActionPerformed

    private void lineWrapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lineWrapActionPerformed
        if (inApplyingModel) {
            return;
        }
        termOptions.setLineWrap(lineWrapCheckBox.isSelected());
}//GEN-LAST:event_lineWrapActionPerformed

    String getStyleName (int i) {
        if ((i & Font.BOLD) > 0)
            if ((i & Font.ITALIC) > 0) return "CTL_BoldItalic";	// NOI18N
            else return "CTL_Bold";				// NOI18N
        else
            if ((i & Font.ITALIC) > 0) return "CTL_Italic";	// NOI18N
            else return "CTL_Plain";				// NOI18N
    }


    private void chooseFont(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseFont
        /*
        PropertyEditor pe = PropertyEditorManager.findEditor(Font.class);
        JOptionPane.showOptionDialog(previewPanel,
                                     pe.getCustomEditor(),
                                     "TITLE",
                                     JOptionPane.OK_CANCEL_OPTION,
                                     JOptionPane.QUESTION_MESSAGE, null, null, null);
         */
        FontPanel panel = new FontPanel(termOptions.getFont(),this);
        int choice = JOptionPane.showOptionDialog(previewPanel,
                                                  panel,
                                                  Catalog.get("LBL_Title"),	// NOI18N
                                                  JOptionPane.OK_CANCEL_OPTION,
                                                  JOptionPane.PLAIN_MESSAGE, null, null, null);
        if (choice == JOptionPane.OK_OPTION) {
            termOptions.setFont(panel.font());
            applyTermOptions();
        }
    }//GEN-LAST:event_chooseFont

    private void ignoreKeymapCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ignoreKeymapCheckBoxActionPerformed
        if (inApplyingModel) {
            return;
        }
        termOptions.setIgnoreKeymap(ignoreKeymapCheckBox.isSelected());
    }//GEN-LAST:event_ignoreKeymapCheckBoxActionPerformed

    private void foregroundComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_foregroundComboBoxActionPerformed
        if (inApplyingModel) {
            return;
        }
        Color c = ColorComboBox.getColor(foregroundComboBox);
	if (c != null) {
	    termOptions.setForeground(c);
	}
    }//GEN-LAST:event_foregroundComboBoxActionPerformed

    private void backgroundComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backgroundComboBoxActionPerformed
        if (inApplyingModel) {
            return;
        }
        Color c = ColorComboBox.getColor(backgroundComboBox);
	if (c != null) {
	    termOptions.setBackground(c);
	}
    }//GEN-LAST:event_backgroundComboBoxActionPerformed

    private void selectionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectionComboBoxActionPerformed
        if (inApplyingModel) {
            return;
        }
        Color c = ColorComboBox.getColor(selectionComboBox);
	if (c != null) {
	    termOptions.setSelectionBackground(c);
	}
    }//GEN-LAST:event_selectionComboBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<ColorValue> backgroundComboBox;
    private javax.swing.JLabel backgroundLabel;
    private javax.swing.JCheckBox clickToTypeCheckBox;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JButton fontButton;
    private javax.swing.JLabel fontLabel;
    private javax.swing.JLabel fontSizeLabel;
    private javax.swing.JSpinner fontSizeSpinner;
    private javax.swing.JTextField fontText;
    private javax.swing.JComboBox<ColorValue> foregroundComboBox;
    private javax.swing.JLabel foregroundLabel;
    private javax.swing.JLabel historySizeLabel;
    private javax.swing.JSpinner historySizeSpinner;
    private javax.swing.JCheckBox ignoreKeymapCheckBox;
    private javax.swing.JCheckBox lineWrapCheckBox;
    private javax.swing.JLabel previewLabel;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JButton restoreButton;
    private javax.swing.JCheckBox scrollOnInputCheckBox;
    private javax.swing.JCheckBox scrollOnOutputCheckBox;
    private javax.swing.JComboBox<ColorValue> selectionComboBox;
    private javax.swing.JLabel selectionLabel;
    private javax.swing.JLabel tabSizeLabel;
    private javax.swing.JSpinner tabSizeSpinner;
    // End of variables declaration//GEN-END:variables

}
