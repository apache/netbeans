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

import org.netbeans.modules.profiler.ppoints.CodeProfilingPoint;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.lib.profiler.ui.UIUtils;


/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "StopwatchCustomizer_NameLabelText=&Name:",
    "StopwatchCustomizer_SettingsLabelText=Settings",
    "StopwatchCustomizer_MeasureLabelText=Measure:",
    "StopwatchCustomizer_TimestampRadioText=&Timestamp",
    "StopwatchCustomizer_DurationRadioText=Timestamp and &duration",
    "StopwatchCustomizer_BeginLocationLabelText=Location (start)",
    "StopwatchCustomizer_EndLocationLabelText=Location (stop)"
})
public class StopwatchCustomizer extends ValidityAwarePanel implements DocumentListener, ChangeListener, ValidityListener,
                                                                       HelpCtx.Provider {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final String HELP_CTX_KEY = "StopwatchCustomizer.HelpCtx"; // NOI18N
    private static final HelpCtx HELP_CTX = new HelpCtx(HELP_CTX_KEY);
    private static int defaultTextComponentHeight = -1;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private JLabel captionLabel;
    private JLabel locationBeginHeaderLabel;
    private JLabel locationEndHeaderLabel;
    private JLabel measureLabel;
    private JLabel nameLabel;
    private JLabel settingsHeaderLabel;
    private JPanel captionPanel;
    private JPanel firstLineCaptionSpacer = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
    private JRadioButton measureTimestampDurationRadio;
    private JRadioButton measureTimestampRadio;
    private JSeparator locationBeginHeaderSeparator;
    private JSeparator locationEndHeaderSeparator;
    private JSeparator settingsHeaderSeparator;
    private JTextField nameTextField;
    private LocationCustomizer locationBeginCustomizer;
    private LocationCustomizer locationEndCustomizer;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public StopwatchCustomizer(String caption, Icon icon) {
        initComponents(caption, icon);
        normalizeCaptionAreaWidth();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public HelpCtx getHelpCtx() {
        return HELP_CTX;
    }

    public Component getInitialFocusTarget() {
        return nameTextField;
    }

    public void setPPEndLocation(CodeProfilingPoint.Location location) {
        if (location == null) {
            measureTimestampRadio.setSelected(true);
            locationEndCustomizer.setPPLocation(CodeProfilingPoint.Location.EMPTY);
        } else {
            measureTimestampDurationRadio.setSelected(true);
            locationEndCustomizer.setPPLocation(location);
        }
    }

    public CodeProfilingPoint.Location getPPEndLocation() {
        if (measureTimestampRadio.isSelected()) {
            return null;
        } else {
            return locationEndCustomizer.getPPLocation();
        }
    }

    public void setPPName(String name) {
        nameTextField.setText(name);
    }

    public String getPPName() {
        return nameTextField.getText();
    }

    public void setPPStartLocation(CodeProfilingPoint.Location location) {
        locationBeginCustomizer.setPPLocation(location);
    }

    public CodeProfilingPoint.Location getPPStartLocation() {
        return locationBeginCustomizer.getPPLocation();
    }

    public int getPreferredCaptionAreaWidth() {
        int ownCaptionAreaWidth = nameLabel.getPreferredSize().width - 12; // nameLabel starts at 8, locationCustomizer at 20 => -12

        return Math.max(ownCaptionAreaWidth, locationBeginCustomizer.getPreferredCaptionAreaWidth());
    }

    public void changedUpdate(DocumentEvent e) {
        updateValidity();
    }

    //  private int getDefaultTextComponentHeight() {
    //    if (defaultTextComponentHeight == -1) defaultTextComponentHeight = new JComboBox().getPreferredSize().height;
    //    return defaultTextComponentHeight;
    //  }
    public void initComponents(String caption, Icon icon) {
        setLayout(new GridBagLayout());

        GridBagConstraints constraints;

        // captionPanel
        captionPanel = new JPanel(new BorderLayout(0, 0));
        captionPanel.setOpaque(true);
        captionPanel.setBackground(UIUtils.getProfilerResultsBackground());

        // captionLabel
        captionLabel = new JLabel(caption, icon, SwingConstants.LEADING);
        captionLabel.setFont(captionLabel.getFont().deriveFont(Font.BOLD));
        captionLabel.setOpaque(false);
        captionLabel.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 6));
        captionPanel.add(captionLabel, BorderLayout.WEST);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0, 0, 16, 0);
        add(captionPanel, constraints);

        // --- next row ----------------------------------------------------------

        // nameLabel
        nameLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, Bundle.StopwatchCustomizer_NameLabelText());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 8, 10, 5);
        add(nameLabel, constraints);

        // firstLineCaptionSpacer
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 0);
        add(firstLineCaptionSpacer, constraints);

        // nameTextField
        nameTextField = new JTextField("") { // NOI18N
                public Dimension getPreferredSize() {
                    return (StopwatchCustomizer.this.getParent() instanceof JViewport) ? getMinimumSize()
                                                                                       : new Dimension(400,
                                                                                                       super.getPreferredSize().height);
                }

                public Dimension getMinimumSize() {
                    return new Dimension(super.getMinimumSize().width, super.getPreferredSize().height);
                }
            };
        nameLabel.setLabelFor(nameTextField);
        nameTextField.getDocument().addDocumentListener(this);
        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 10, 13);
        add(nameTextField, constraints);

        // --- next row ----------------------------------------------------------
        JPanel settingsHeaderContainer = new JPanel(new GridBagLayout());

        // settingsHeaderLabel
        settingsHeaderLabel = new JLabel(Bundle.StopwatchCustomizer_SettingsLabelText());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 5);
        settingsHeaderContainer.add(settingsHeaderLabel, constraints);

        // settingsHeaderSeparator
        settingsHeaderSeparator = new JSeparator() {
                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 0, 0);
        settingsHeaderContainer.add(settingsHeaderSeparator, constraints);

        // locationHeaderContainer
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 8, 5, 8);
        add(settingsHeaderContainer, constraints);

        // --- next row ----------------------------------------------------------
        ButtonGroup measureRadiosGroup = new ButtonGroup();
        JPanel measureSettingsContainer = new JPanel(new GridBagLayout());

        // measureLabel
        measureLabel = new JLabel(Bundle.StopwatchCustomizer_MeasureLabelText());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 8);
        measureSettingsContainer.add(measureLabel, constraints);

        // measureTimestampRadio
        measureTimestampRadio = new JRadioButton();
        org.openide.awt.Mnemonics.setLocalizedText(measureTimestampRadio, Bundle.StopwatchCustomizer_TimestampRadioText());
        measureRadiosGroup.add(measureTimestampRadio);
        measureTimestampRadio.addChangeListener(this);
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 3);
        measureSettingsContainer.add(measureTimestampRadio, constraints);

        // measureTimestampDurationRadio
        measureTimestampDurationRadio = new JRadioButton();
        org.openide.awt.Mnemonics.setLocalizedText(measureTimestampDurationRadio, Bundle.StopwatchCustomizer_DurationRadioText());
        measureRadiosGroup.add(measureTimestampDurationRadio);
        measureTimestampDurationRadio.setSelected(true);
        measureTimestampDurationRadio.addChangeListener(this);
        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 0);
        measureSettingsContainer.add(measureTimestampDurationRadio, constraints);

        // measureSettingsContainer
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 20, 10, 13);
        add(measureSettingsContainer, constraints);

        // --- next row ----------------------------------------------------------
        JPanel locationBeginHeaderContainer = new JPanel(new GridBagLayout());

        // locationBeginHeaderLabel
        locationBeginHeaderLabel = new JLabel(Bundle.StopwatchCustomizer_BeginLocationLabelText());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 5);
        locationBeginHeaderContainer.add(locationBeginHeaderLabel, constraints);

        // locationBeginHeaderSeparator
        locationBeginHeaderSeparator = new JSeparator() {
                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 0, 0);
        locationBeginHeaderContainer.add(locationBeginHeaderSeparator, constraints);

        // locationBeginHeaderContainer
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 8, 5, 8);
        add(locationBeginHeaderContainer, constraints);

        // --- next row ----------------------------------------------------------

        // locationBeginCustomizer
        locationBeginCustomizer = new LocationCustomizer();
        locationBeginCustomizer.addValidityListener(this);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 20, 12, 13);
        add(locationBeginCustomizer, constraints);

        // --- next row ----------------------------------------------------------
        JPanel locationEndHeaderContainer = new JPanel(new GridBagLayout());

        // locationEndHeaderLabel
        locationEndHeaderLabel = new JLabel(Bundle.StopwatchCustomizer_EndLocationLabelText());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 5);
        locationEndHeaderContainer.add(locationEndHeaderLabel, constraints);

        // locationEndHeaderSeparator
        locationEndHeaderSeparator = new JSeparator() {
                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 0, 0);
        locationEndHeaderContainer.add(locationEndHeaderSeparator, constraints);

        // locationEndHeaderContainer
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 6;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 8, 5, 8);
        add(locationEndHeaderContainer, constraints);

        // --- next row ----------------------------------------------------------

        // locationEndCustomizer
        locationEndCustomizer = new LocationCustomizer();
        locationEndCustomizer.resetMnemonic();
        locationEndCustomizer.addValidityListener(this);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 7;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 20, 0, 13);
        add(locationEndCustomizer, constraints);

        // --- next row ----------------------------------------------------------
        JPanel fillerPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 8;
        constraints.weighty = 1;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0, 0, 0, 0);
        add(fillerPanel, constraints);
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

        StopwatchCustomizer main = new StopwatchCustomizer("Stopwatch", null); // NOI18N
        main.addValidityListener(new ValidityListener() {
                public void validityChanged(boolean isValid) {
                    System.err.println(">>> Validity changed to " + isValid);
                } // NOI18N
            });

        JFrame frame = new JFrame("Customize Profiling Point"); // NOI18N
        frame.getContentPane().add(main);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public void normalizeCaptionAreaWidth() {
        int requiredCaptionAreaWidth = nameLabel.getPreferredSize().width - 12; // nameLabel starts at 8, locationCustomizer at 20 => -12
        int diffCaptionAreaWidth = getPreferredCaptionAreaWidth() - requiredCaptionAreaWidth;
        int normalizedCaptionAreaWidth = (diffCaptionAreaWidth > 0) ? diffCaptionAreaWidth : 0;

        firstLineCaptionSpacer.setBorder(BorderFactory.createEmptyBorder(0, normalizedCaptionAreaWidth, 0, 0));
        locationBeginCustomizer.normalizeCaptionAreaWidth(getPreferredCaptionAreaWidth());
        locationEndCustomizer.normalizeCaptionAreaWidth(getPreferredCaptionAreaWidth());
    }

    public void removeUpdate(DocumentEvent e) {
        updateValidity();
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == measureTimestampRadio) {
            updateValidity();
        }

        if (e.getSource() == measureTimestampDurationRadio) {
            boolean selected = measureTimestampDurationRadio.isSelected();
            locationEndCustomizer.setEnabled(selected);
            locationEndHeaderLabel.setEnabled(selected);
            locationEndHeaderSeparator.setEnabled(selected);

            CodeProfilingPoint.Location endLocation = getPPEndLocation();

            if (selected && (endLocation != null) && (endLocation.getFile().trim().length() == 0)) {
                CodeProfilingPoint.Location startLocation = getPPStartLocation();
                CodeProfilingPoint.Location newEndLocation = new CodeProfilingPoint.Location(startLocation.getFile(),
                                                                                             startLocation.getLine() + 1,
                                                                                             CodeProfilingPoint.Location.OFFSET_END);
                locationEndCustomizer.setPPLocation(newEndLocation);
            }

            updateValidity();
        }
    }

    public void validityChanged(boolean isValid) {
        updateValidity();
    }

    private boolean isNameEmpty() {
        return nameTextField.getText().trim().length() == 0;
    }

    private boolean areEndLocationSettingsValid() {
        return measureTimestampRadio.isSelected() || locationEndCustomizer.areSettingsValid();
    }

    private void updateValidity() {
        boolean isValid = !isNameEmpty() && locationBeginCustomizer.areSettingsValid() && areEndLocationSettingsValid();

        if (isValid != areSettingsValid()) {
            fireValidityChanged(isValid);
        }
    }
}
