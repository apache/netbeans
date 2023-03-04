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
import org.netbeans.lib.profiler.ui.components.JExtendedSpinner;
import org.netbeans.modules.profiler.ppoints.CodeProfilingPoint;
import org.netbeans.modules.profiler.ppoints.Utils;
import org.openide.util.NbBundle;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.openide.util.Utilities;


/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "LocationCustomizer_FileLabelText=F&ile:",
    "LocationCustomizer_BrowseButtonText=Bro&wse...",
    "LocationCustomizer_CurrentLineButtonText=&Current\nLine",
    "LocationCustomizer_LineLabelText=&Line:",
    "LocationCustomizer_BeginRadioText=Be&gin",
    "LocationCustomizer_EndRadioText=&End",
    "LocationCustomizer_OffsetRadioText=&Offset:",
    "LocationCustomizer_ChooseFileDialogCaption=Choose Java Source File",
    "LocationCustomizer_FileDialogFilterName=Java Sources (*.java)"
})
public class LocationCustomizer extends ValidityAwarePanel implements ActionListener, ChangeListener, DocumentListener,
                                                                      HierarchyListener {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private static class HTMLButton extends JButton {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public void setEnabled(boolean enabled) {
            setForeground(enabled ? SystemColor.textText : SystemColor.textInactiveText);
            super.setEnabled(enabled);
        }

        //    public HTMLButton(String text) {
        //      super("<html><center>" + text.replaceAll("\\n", "<br>") + "</center></html>"); // NOI18N
        //      getAccessibleContext().setAccessibleName(text);
        //    }
        public void setText(String value) {
            super.setText("<html><center><nobr>" + value.replace("\\n", "<br>") + "</nobr></center></html>"); // NOI18N
            getAccessibleContext().setAccessibleName(value);
        }
    }

    // --- Implementation --------------------------------------------------------
    private static int defaultTextComponentHeight = -1;
    private static JFileChooser fileChooser;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private JButton fileButton;
    private JButton fromEditorButton;
    private JLabel fileLabel;
    private JLabel lineLabel;
    private JPanel firstLineCaptionSpacer = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
    private JPanel secondLineCaptionSpacer = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
    private JRadioButton lineBeginRadio;
    private JRadioButton lineEndRadio;
    private JRadioButton lineOffsetRadio;
    private JSeparator fromEditorSeparator;
    private JSpinner lineNumberSpinner;
    private JSpinner lineOffsetSpinner;
    private JTextField fileTextField;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public LocationCustomizer() {
        initComponents();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        fileLabel.setEnabled(enabled);
        fileTextField.setEnabled(enabled);
        fileButton.setEnabled(enabled);
        lineLabel.setEnabled(enabled);
        lineNumberSpinner.setEnabled(enabled);
        lineBeginRadio.setEnabled(enabled);
        lineEndRadio.setEnabled(enabled);
        lineOffsetRadio.setEnabled(enabled);
        lineOffsetSpinner.setEnabled((enabled == false) ? false : lineOffsetRadio.isSelected());
        fromEditorButton.setEnabled(enabled);
    }

    public void setPPLocation(CodeProfilingPoint.Location location) {
        fileTextField.setText(location.getFile());
        lineNumberSpinner.setValue(location.getLine());

        int offset = location.getOffset();

        if (offset == CodeProfilingPoint.Location.OFFSET_START) {
            lineBeginRadio.setSelected(true);
        } else if (offset == CodeProfilingPoint.Location.OFFSET_END) {
            lineEndRadio.setSelected(true);
        } else {
            lineOffsetRadio.setSelected(true);
            lineOffsetSpinner.setValue(offset);
        }
    }

    public CodeProfilingPoint.Location getPPLocation() {
        int offset = ((Integer) lineOffsetSpinner.getValue()).intValue();

        if (lineBeginRadio.isSelected()) {
            offset = CodeProfilingPoint.Location.OFFSET_START;
        } else if (lineEndRadio.isSelected()) {
            offset = CodeProfilingPoint.Location.OFFSET_END;
        }

        return new CodeProfilingPoint.Location(fileTextField.getText(), ((Integer) lineNumberSpinner.getValue()).intValue(),
                                               offset);
    }

    public int getPreferredCaptionAreaWidth() {
        return Math.max(fileLabel.getPreferredSize().width, lineLabel.getPreferredSize().width);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == fileButton) {
            JFileChooser fileChooser = getFileChooser();
            fileChooser.setCurrentDirectory(new File(fileTextField.getText()));

            if (fileChooser.showOpenDialog(Utilities.findDialogParent()) == JFileChooser.APPROVE_OPTION) {
                fileTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        } else if (e.getSource() == fromEditorButton) {
            if (lineBeginRadio.isSelected()) {
                setPPLocation(Utils.getCurrentLocation(CodeProfilingPoint.Location.OFFSET_START));
            } else if (lineEndRadio.isSelected()) {
                setPPLocation(Utils.getCurrentLocation(CodeProfilingPoint.Location.OFFSET_END));
            } else {
                setPPLocation(Utils.getCurrentLocation(((Integer) lineOffsetSpinner.getValue()).intValue()));
            }
        }
    }

    public void changedUpdate(DocumentEvent e) {
        updateValidity();
    }

    public void hierarchyChanged(HierarchyEvent e) {
        if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
            Window window = SwingUtilities.getWindowAncestor(this);

            if (window instanceof Dialog && !((Dialog) window).isModal()) {
                showFromEditor();
            } else {
                hideFromEditor();
            }
        }
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

        LocationCustomizer main = new LocationCustomizer();

        //    main.addValidityListener(new ValidityListener() {
        //      public void validityChanged(boolean isValid) { System.err.println(">>> Validity changed to " + isValid); }
        //    });
        JFrame frame = new JFrame("Customize Profiling Point"); // NOI18N
        frame.getContentPane().add(main);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public void normalizeCaptionAreaWidth(int captionAreaWidth) {
        int requiredCaptionAreaWidth = getPreferredCaptionAreaWidth();
        int diffCaptionAreaWidth = captionAreaWidth - requiredCaptionAreaWidth;
        int normalizedCaptionAreaWidth = (diffCaptionAreaWidth > 0) ? diffCaptionAreaWidth : 0;

        firstLineCaptionSpacer.setBorder(BorderFactory.createEmptyBorder(0, normalizedCaptionAreaWidth, 0, 0));
        secondLineCaptionSpacer.setBorder(BorderFactory.createEmptyBorder(0, normalizedCaptionAreaWidth, 0, 0));
    }

    public void removeUpdate(DocumentEvent e) {
        updateValidity();
    }

    public void resetMnemonic() {
        fileLabel.setDisplayedMnemonic(0);
        fileLabel.setDisplayedMnemonicIndex(-1);

        fileButton.setMnemonic(0);
        fileButton.setDisplayedMnemonicIndex(-1);

        fromEditorButton.setMnemonic(0);
        fromEditorButton.setDisplayedMnemonicIndex(-1);

        lineLabel.setDisplayedMnemonic(0);
        lineLabel.setDisplayedMnemonicIndex(-1);

        lineBeginRadio.setMnemonic(0);
        lineBeginRadio.setDisplayedMnemonicIndex(-1);

        lineEndRadio.setMnemonic(0);
        lineEndRadio.setDisplayedMnemonicIndex(-1);

        lineOffsetRadio.setMnemonic(0);
        lineOffsetRadio.setDisplayedMnemonicIndex(-1);
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == lineOffsetRadio) {
            lineOffsetSpinner.setEnabled(lineOffsetRadio.isSelected());
        }
    }

    private JFileChooser getFileChooser() {
        if (fileChooser == null) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setMultiSelectionEnabled(false);
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
            chooser.setDialogTitle(Bundle.LocationCustomizer_ChooseFileDialogCaption());
            chooser.setFileFilter(new FileFilter() {
                    public boolean accept(File f) {
                        return f.isDirectory() || f.getName().toLowerCase().endsWith(".java");
                    } // NOI18N

                    public String getDescription() {
                        return Bundle.LocationCustomizer_FileDialogFilterName();
                    }
                });
            fileChooser = chooser;
        }

        return fileChooser;
    }

    private boolean isFileValid() {
        File file = new File(fileTextField.getText());

        return file.exists() && file.isFile();
    }

    private void hideFromEditor() {
        fromEditorSeparator.setVisible(false);
        fromEditorButton.setVisible(false);
    }

    //  private int getDefaultTextComponentHeight() {
    //    if (defaultTextComponentHeight == -1) defaultTextComponentHeight = new JComboBox().getPreferredSize().height;
    //    return defaultTextComponentHeight;
    //  }
    private void initComponents() {
        setLayout(new GridBagLayout());

        GridBagConstraints constraints;

        // fileLabel
        fileLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(fileLabel, Bundle.LocationCustomizer_FileLabelText());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 5, 5);
        add(fileLabel, constraints);

        // firstLineCaptionSpacer
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 0);
        add(firstLineCaptionSpacer, constraints);

        // fileTextField
        fileTextField = new JTextField("") { // NOI18N
                public Dimension getPreferredSize() {
                    return new Dimension(super.getMinimumSize().width, super.getPreferredSize().height);
                }

                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
        fileLabel.setLabelFor(fileTextField);
        fileTextField.getDocument().addDocumentListener(this);
        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 5, 5);
        add(fileTextField, constraints);

        // fileButton
        fileButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(fileButton, Bundle.LocationCustomizer_BrowseButtonText());
        fileButton.addActionListener(this);
        constraints = new GridBagConstraints();
        constraints.gridx = 3;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 5, 0);
        add(fileButton, constraints);

        // fromEditorSeparator
        fromEditorSeparator = new JSeparator(SwingConstants.VERTICAL) {
                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
        constraints = new GridBagConstraints();
        constraints.gridx = 4;
        constraints.gridy = 0;
        constraints.gridheight = 2;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.VERTICAL;
        constraints.insets = new Insets(0, 8, 0, 0);
        add(fromEditorSeparator, constraints);

        // fromEditorButton
        fromEditorButton = new HTMLButton();
        org.openide.awt.Mnemonics.setLocalizedText(fromEditorButton, Bundle.LocationCustomizer_CurrentLineButtonText());
        fromEditorButton.addActionListener(this);
        constraints = new GridBagConstraints();
        constraints.gridx = 5;
        constraints.gridy = 0;
        constraints.gridheight = 2;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.VERTICAL;
        constraints.insets = new Insets(0, 8, 0, 0);
        add(fromEditorButton, constraints);

        // --- next row ----------------------------------------------------------
        ButtonGroup lineRadiosGroup = new ButtonGroup();

        // lineLabel
        lineLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(lineLabel, Bundle.LocationCustomizer_LineLabelText());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 5);
        add(lineLabel, constraints);

        // secondLineCaptionSpacer
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 0);
        add(secondLineCaptionSpacer, constraints);

        JPanel lineSettingsContainer = new JPanel(new GridBagLayout());

        // lineNumberSpinner
        SpinnerNumberModel lineNumberModel = new SpinnerNumberModel();
        lineNumberModel.setMinimum(1);
        lineNumberSpinner = new JExtendedSpinner(lineNumberModel) {
                public Dimension getPreferredSize() {
                    return new Dimension(Math.max(super.getPreferredSize().width, 55),
                                         getDefaultSpinnerHeight());
                }

                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
        lineLabel.setLabelFor(lineNumberSpinner);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 10);
        lineSettingsContainer.add(lineNumberSpinner, constraints);

        // lineBeginRadio
        lineBeginRadio = new JRadioButton();
        org.openide.awt.Mnemonics.setLocalizedText(lineBeginRadio, Bundle.LocationCustomizer_BeginRadioText());
        lineBeginRadio.getAccessibleContext().setAccessibleDescription(Bundle.LocationCustomizer_LineLabelText() + Bundle.LocationCustomizer_BeginRadioText());
        lineRadiosGroup.add(lineBeginRadio);
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 3);
        lineSettingsContainer.add(lineBeginRadio, constraints);

        // lineEndRadio
        lineEndRadio = new JRadioButton();
        org.openide.awt.Mnemonics.setLocalizedText(lineEndRadio, Bundle.LocationCustomizer_EndRadioText());
        lineEndRadio.getAccessibleContext().setAccessibleDescription(Bundle.LocationCustomizer_LineLabelText() + Bundle.LocationCustomizer_EndRadioText());
        lineRadiosGroup.add(lineEndRadio);
        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 3);
        lineSettingsContainer.add(lineEndRadio, constraints);

        // lineOffsetRadio
        lineOffsetRadio = new JRadioButton();
        org.openide.awt.Mnemonics.setLocalizedText(lineOffsetRadio, Bundle.LocationCustomizer_OffsetRadioText());
        lineOffsetRadio.getAccessibleContext().setAccessibleDescription(Bundle.LocationCustomizer_LineLabelText() + Bundle.LocationCustomizer_OffsetRadioText());
        lineRadiosGroup.add(lineOffsetRadio);
        lineOffsetRadio.addChangeListener(this);
        constraints = new GridBagConstraints();
        constraints.gridx = 3;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 0);
        //      lineSettingsContainer.add(lineOffsetRadio, constraints);

        // Placeholder for lineOffsetRadio and lineOffsetSpinner
        lineSettingsContainer.add(new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0)) {
                public Dimension getPreferredSize() {
                    return new Dimension(lineOffsetRadio.getPreferredSize().width + lineOffsetSpinner.getPreferredSize().width,
                                         Math.max(lineOffsetRadio.getPreferredSize().height,
                                                  lineOffsetSpinner.getPreferredSize().height));
                }
            }, constraints);

        // lineOffsetSpinner
        lineOffsetSpinner = new JExtendedSpinner() {
                public Dimension getPreferredSize() {
                    return new Dimension(Math.max(super.getPreferredSize().width, 55),
                                         getDefaultSpinnerHeight());
                }

                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
        constraints = new GridBagConstraints();
        constraints.gridx = 4;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 0);
        //      lineSettingsContainer.add(lineOffsetSpinner, constraints);
        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 0);
        add(lineSettingsContainer, constraints);

        // --- next row ----------------------------------------------------------
        JPanel fillerPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.weighty = 1;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0, 0, 0, 0);
        add(fillerPanel, constraints);

        addHierarchyListener(this);
    }

    private void showFromEditor() {
        fromEditorSeparator.setVisible(true);
        fromEditorButton.setVisible(true);
    }

    private void updateValidity() {
        boolean isValid = isFileValid();

        fileTextField.setForeground(isValid ? UIManager.getColor("TextField.foreground") : Color.RED); // NOI18N

        if (isValid != LocationCustomizer.this.areSettingsValid()) {
            fireValidityChanged(isValid);
        }
    }
}
