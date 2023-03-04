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

package org.netbeans.modules.profiler.ppoints.ui;

import java.awt.Color;
import org.openide.util.NbBundle;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.Utilities;


/**
 *
 * @author Tomas Hurka
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "SnapshotCustomizer_TakeLabelText=Take:",
    "SnapshotCustomizer_ProfilingDataRadioText=Profiling &data snapshot",
    "SnapshotCustomizer_HeapDumpRadioText=He&ap dump",
    "SnapshotCustomizer_SaveLabelText=Save:",
    "SnapshotCustomizer_ToProjectRadioText=To &project",
    "SnapshotCustomizer_ToDirectoryRadioText=To direc&tory:",
    "SnapshotCustomizer_BrowseButtonText=Brow&se...",
    "SnapshotCustomizer_ResetResultsCheckboxText=&Reset results after taking snapshot",
    "SnapshotCustomizer_SelectSnapshotDialogCaption=Select Snapshot Directory",
    "SnapshotCustomizer_SaveFieldAccessName=Directory where snapshot should be saved"
})
public class SnapshotCustomizer extends ValidityAwarePanel implements ActionListener, ChangeListener, DocumentListener {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static int defaultTextComponentHeight = -1;
    private static JFileChooser fileChooser;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private JButton saveToFileButton;
    private JCheckBox resetResultsCheckbox;
    private JLabel saveLabel;
    private JLabel takeLabel;
    private JPanel firstLineCaptionSpacer = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
    private JPanel secondLineCaptionSpacer = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
    private JPanel thirdLineCaptionSpacer = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
    private JRadioButton saveToFileRadio;
    private JRadioButton saveToProjectRadio;
    private JRadioButton takeHeapdumpRadio;
    private JRadioButton takeSnapshotRadio;
    private JTextField saveToFileField;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public SnapshotCustomizer() {
        initComponents();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public int getPreferredCaptionAreaWidth() {
        return Math.max(takeLabel.getPreferredSize().width, saveLabel.getPreferredSize().width);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == saveToFileButton) {
            JFileChooser fileChooser = getFileChooser();
            fileChooser.setCurrentDirectory(new File(saveToFileField.getText()).getParentFile());

            if (fileChooser.showOpenDialog(Utilities.findDialogParent()) == JFileChooser.APPROVE_OPTION) {
                saveToFileField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        }
    }

    public void changedUpdate(DocumentEvent e) {
        updateValidity();
    }

    public void insertUpdate(DocumentEvent e) {
        updateValidity();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"); //NOI18N
                                                                                            //      UIManager.setLookAndFeel("plaf.metal.MetalLookAndFeel"); //NOI18N
                                                                                            //      UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel"); //NOI18N
                                                                                            //      UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel"); //NOI18N
        } catch (Exception e) {
        }

        ;

        SnapshotCustomizer main = new SnapshotCustomizer();

        //    main.addValidityListener(new ValidityListener() {
        //      public void validityChanged(boolean isValid) { System.err.println(">>> Validity changed to " + isValid); }
        //    });

        //    main.normalizeCaptionAreaWidth(20);
        JFrame frame = new JFrame("Customize Profiling Point"); // NOI18N
        frame.getContentPane().add(main);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        //    LocationCustomizer main2 = new LocationCustomizer();
        //
        //    JFrame frame2 = new JFrame("Customizer Frame 2");
        //    frame2.getContentPane().add(main2.getPanel(70));
        //    frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //    frame2.pack();
        //    frame2.setVisible(true);
    }

    public void normalizeCaptionAreaWidth(int captionAreaWidth) {
        int requiredCaptionAreaWidth = getPreferredCaptionAreaWidth();
        int diffCaptionAreaWidth = captionAreaWidth - requiredCaptionAreaWidth;
        int normalizedCaptionAreaWidth = (diffCaptionAreaWidth > 0) ? diffCaptionAreaWidth : 0;

        firstLineCaptionSpacer.setBorder(BorderFactory.createEmptyBorder(0, normalizedCaptionAreaWidth, 0, 0));
        secondLineCaptionSpacer.setBorder(BorderFactory.createEmptyBorder(0, normalizedCaptionAreaWidth, 0, 0));
        thirdLineCaptionSpacer.setBorder(BorderFactory.createEmptyBorder(0, normalizedCaptionAreaWidth, 0, 0));
    }

    public void removeUpdate(DocumentEvent e) {
        updateValidity();
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == saveToProjectRadio) {
            updateValidity();
        } else if (e.getSource() == saveToFileRadio) {
            saveToFileField.setEnabled(saveToFileRadio.isSelected());
            saveToFileButton.setEnabled(saveToFileRadio.isSelected());
            updateValidity();
        }
    }

    void setPPFile(String fileName) {
        if (fileName == null) {
            saveToFileField.setText(""); // NOI18N
        } else {
            saveToFileField.setText(fileName);
        }
    }

    String getPPFile() {
        String text = saveToFileField.getText().trim();

        if (text.length() == 0) {
            return null;
        } else {
            return text;
        }
    }

    void setPPResetResults(boolean resetResults) {
        resetResultsCheckbox.setSelected(resetResults);
    }

    boolean getPPResetResults() {
        return resetResultsCheckbox.isSelected();
    }

    void setPPTarget(boolean target) {
        saveToProjectRadio.setSelected(target);
        saveToFileRadio.setSelected(!target);
    }

    boolean getPPTarget() {
        return saveToProjectRadio.isSelected();
    }

    void setPPType(boolean type) {
        takeSnapshotRadio.setSelected(type);
        takeHeapdumpRadio.setSelected(!type);
        resetResultsCheckbox.setEnabled(type);
    }

    boolean getPPType() {
        return takeSnapshotRadio.isSelected();
    }

    private boolean isDestinationDirectoryValid() {
        File file = new File(saveToFileField.getText());

        return file.exists() && file.isDirectory();
    }

    private JFileChooser getFileChooser() {
        if (fileChooser == null) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setMultiSelectionEnabled(false);
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
            chooser.setDialogTitle(Bundle.SnapshotCustomizer_SelectSnapshotDialogCaption());
            fileChooser = chooser;
        }

        return fileChooser;
    }

    //  private int getDefaultTextComponentHeight() {
    //    if (defaultTextComponentHeight == -1) defaultTextComponentHeight = new JComboBox().getPreferredSize().height;
    //    return defaultTextComponentHeight;
    //  }
    private void initComponents() {
        setLayout(new GridBagLayout());

        GridBagConstraints constraints;

        // takeLabel
        takeLabel = new JLabel(Bundle.SnapshotCustomizer_TakeLabelText());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 5, 5);
        add(takeLabel, constraints);

        // firstLineCaptionSpacer
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 0);
        add(firstLineCaptionSpacer, constraints);

        ButtonGroup takeRadiosGroup = new ButtonGroup();
        JPanel takeRadiosContainer = new JPanel(new GridBagLayout());

        // takeSnapshotRadio
        takeSnapshotRadio = new JRadioButton();
        org.openide.awt.Mnemonics.setLocalizedText(takeSnapshotRadio, Bundle.SnapshotCustomizer_ProfilingDataRadioText());
        takeRadiosGroup.add(takeSnapshotRadio);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 5);
        takeRadiosContainer.add(takeSnapshotRadio, constraints);
        takeSnapshotRadio.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    resetResultsCheckbox.setEnabled(takeSnapshotRadio.isSelected());
                }
            });

        // takeHeapdumpRadio
        takeHeapdumpRadio = new JRadioButton();
        org.openide.awt.Mnemonics.setLocalizedText(takeHeapdumpRadio, Bundle.SnapshotCustomizer_HeapDumpRadioText());
        takeRadiosGroup.add(takeHeapdumpRadio);
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 0);
        takeRadiosContainer.add(takeHeapdumpRadio, constraints);

        JPanel takeRadiosSpacer = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 0, 0);
        takeRadiosContainer.add(takeRadiosSpacer, constraints);

        // takeRadiosContainer
        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 5, 0);
        add(takeRadiosContainer, constraints);

        //    return new Dimension(super.getMinimumSize().width, getDefaultTextComponentHeight());
        // --- next row ----------------------------------------------------------
        ButtonGroup saveRadiosGroup = new ButtonGroup();

        // lineLabel
        saveLabel = new JLabel(Bundle.SnapshotCustomizer_SaveLabelText());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 5);
        add(saveLabel, constraints);

        // secondLineCaptionSpacer
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 0);
        add(secondLineCaptionSpacer, constraints);

        // saveToProjectRadio
        saveToProjectRadio = new JRadioButton();
        org.openide.awt.Mnemonics.setLocalizedText(saveToProjectRadio, Bundle.SnapshotCustomizer_ToProjectRadioText());
        saveRadiosGroup.add(saveToProjectRadio);
        saveToProjectRadio.addChangeListener(this);
        constraints.gridx = 2;
        constraints.gridy = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 0);
        add(saveToProjectRadio, constraints);

        // --- next row ----------------------------------------------------------
        JPanel saveToFileSettingsContainer = new JPanel(new GridBagLayout());

        JPanel saveToFileSpacer = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 0, 0);
        takeRadiosContainer.add(saveToFileSpacer, constraints);

        // thirdLineCaptionSpacer
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 0);
        add(thirdLineCaptionSpacer, constraints);

        // saveToFileRadio
        saveToFileRadio = new JRadioButton();
        org.openide.awt.Mnemonics.setLocalizedText(saveToFileRadio, Bundle.SnapshotCustomizer_ToDirectoryRadioText());
        saveRadiosGroup.add(saveToFileRadio);
        saveToFileRadio.setSelected(true);
        saveToFileRadio.addChangeListener(this);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 5);
        saveToFileSettingsContainer.add(saveToFileRadio, constraints);

        // saveToFileField
        saveToFileField = new JTextField("") { // NOI18N
                public Dimension getPreferredSize() {
                    return new Dimension(super.getMinimumSize().width, super.getPreferredSize().height);
                }

                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
        saveToFileField.getAccessibleContext().setAccessibleName(Bundle.SnapshotCustomizer_ToDirectoryRadioText());
        saveToFileField.getAccessibleContext().setAccessibleDescription(Bundle.SnapshotCustomizer_SaveFieldAccessName());
        saveToFileField.getDocument().addDocumentListener(this);
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 0, 5);
        saveToFileSettingsContainer.add(saveToFileField, constraints);

        // saveToFileButton
        saveToFileButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(saveToFileButton, Bundle.SnapshotCustomizer_BrowseButtonText());
        saveToFileButton.addActionListener(this);
        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 0);
        saveToFileSettingsContainer.add(saveToFileButton, constraints);

        // saveToFileSettingsContainer
        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 2;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 8, 0);
        add(saveToFileSettingsContainer, constraints);

        // --- next row ----------------------------------------------------------
        resetResultsCheckbox = new JCheckBox();
        org.openide.awt.Mnemonics.setLocalizedText(resetResultsCheckbox, Bundle.SnapshotCustomizer_ResetResultsCheckboxText());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 0);
        add(resetResultsCheckbox, constraints);

        // --- next row ----------------------------------------------------------
        JPanel fillerPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.weighty = 1;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0, 0, 0, 0);
        add(fillerPanel, constraints);
    }

    private void updateValidity() {
        boolean isDirectoryValid = isDestinationDirectoryValid();

        saveToFileField.setForeground(isDirectoryValid ? UIManager.getColor("TextField.foreground") : Color.RED); // NOI18N

        boolean isValid = saveToProjectRadio.isSelected() || isDirectoryValid;

        if (isValid != areSettingsValid()) {
            fireValidityChanged(isValid);
        }
    }
}
