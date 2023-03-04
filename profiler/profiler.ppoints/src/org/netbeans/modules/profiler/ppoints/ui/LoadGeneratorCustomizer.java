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
import org.netbeans.modules.profiler.spi.LoadGenPlugin;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
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
import javax.swing.filechooser.FileFilter;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.modules.profiler.api.ProjectUtilities;
import org.openide.util.Utilities;


/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "LoadGeneratorCustomizer_NameLabelText=&Name:",
    "LoadGeneratorCustomizer_SettingsLabelText=Settings",
    "LoadGeneratorCustomizer_ScriptLabelText=&Script:",
    "LoadGeneratorCustomizer_BrowseButtonText=Br&owse...",
    "LoadGeneratorCustomizer_StopLabelText=Stop:",
    "LoadGeneratorCustomizer_DefineRadioText=&Define",
    "LoadGeneratorCustomizer_StopRadioText=W&hen profiling stops",
    "LoadGeneratorCustomizer_LocationBeginLabelText=Location (start)",
    "LoadGeneratorCustomizer_LocationEndLabelText=Location (stop)",
    "LoadGeneratorCustomizer_ChooseScriptDialogCaption=Choose Load Generator Script",
    "LoadGeneratorCustomizer_ScriptFieldAccessDescr=Script to be executed in Load Generator",
    "LoadGeneratorCustomizer_SupportedFiles=Load Generator Scripts"
})
public class LoadGeneratorCustomizer extends ValidityAwarePanel implements ActionListener, ChangeListener, DocumentListener,
                                                                           ValidityListener, HelpCtx.Provider {
    private static final String HELP_CTX_KEY = "LoadGeneratorCustomizer.HelpCtx"; // NOI18N
    private static final HelpCtx HELP_CTX = new HelpCtx(HELP_CTX_KEY);
    private static int defaultTextComponentHeight = -1;
    private static JFileChooser fileChooser;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private JButton scriptButton;
    private JLabel captionLabel;
    private JLabel locationBeginHeaderLabel;
    private JLabel locationEndHeaderLabel;
    private JLabel nameLabel;
    private JLabel scriptLabel;
    private JLabel settingsHeaderLabel;
    private JLabel stopLabel;
    private JPanel captionPanel;
    private JPanel firstLineCaptionSpacer = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
    private JPanel secondLineCaptionSpacer = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
    private JPanel thirdLineCaptionSpacer = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
    private JRadioButton stopDefineRadio;
    private JRadioButton stopOnStopRadio;
    private JSeparator locationBeginHeaderSeparator;
    private JSeparator locationEndHeaderSeparator;
    private JSeparator settingsHeaderSeparator;
    private JTextField nameTextField;
    private JTextField scriptTextField;
    private LocationCustomizer locationBeginCustomizer;
    private LocationCustomizer locationEndCustomizer;
    private Lookup.Provider project;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public LoadGeneratorCustomizer(String caption, Icon icon) {
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
            stopOnStopRadio.setSelected(true);
            locationEndCustomizer.setPPLocation(CodeProfilingPoint.Location.EMPTY);
        } else {
            stopDefineRadio.setSelected(true);
            locationEndCustomizer.setPPLocation(location);
        }
    }

    public CodeProfilingPoint.Location getPPEndLocation() {
        if (stopOnStopRadio.isSelected()) {
            return null;
        } else {
            return locationEndCustomizer.getPPLocation();
        }
    }

    public void setPPName(String ppName) {
        nameTextField.setText(ppName);
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
        ownCaptionAreaWidth = Math.max(ownCaptionAreaWidth, scriptLabel.getPreferredSize().width);
        ownCaptionAreaWidth = Math.max(ownCaptionAreaWidth, stopLabel.getPreferredSize().width);

        return Math.max(ownCaptionAreaWidth, locationBeginCustomizer.getPreferredCaptionAreaWidth());
    }

    public void setProject(Lookup.Provider aProject) {
        project = aProject;
    }

    public void setScriptFile(String fileName) {
        scriptTextField.setText(fileName);
    }

    public String getScriptFile() {
        return scriptTextField.getText();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == scriptButton) {
            JFileChooser fileChooser = getFileChooser();
            File currentDir = null;
            String scriptPath = scriptTextField.getText();

            if (scriptPath.length() > 0) {
                fileChooser.setCurrentDirectory(new File(scriptPath).getParentFile());
            } else {
                fileChooser.setCurrentDirectory(FileUtil.toFile(ProjectUtilities.getProjectDirectory(project)));
            }

            if (fileChooser.showOpenDialog(Utilities.findDialogParent()) == JFileChooser.APPROVE_OPTION) {
                scriptTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        }
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
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, Bundle.LoadGeneratorCustomizer_NameLabelText());
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
                    return (LoadGeneratorCustomizer.this.getParent() instanceof JViewport) ? getMinimumSize()
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
        settingsHeaderLabel = new JLabel(Bundle.LoadGeneratorCustomizer_SettingsLabelText());
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
        JPanel scriptSettingsContainer = new JPanel(new GridBagLayout());

        // scriptLabel
        scriptLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(scriptLabel, Bundle.LoadGeneratorCustomizer_ScriptLabelText());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 5);
        scriptSettingsContainer.add(scriptLabel, constraints);

        // secondLineCaptionSpacer
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 0);
        scriptSettingsContainer.add(secondLineCaptionSpacer, constraints);

        // scriptTextField
        scriptTextField = new JTextField("") { // NOI18N
                public Dimension getPreferredSize() {
                    return new Dimension(super.getMinimumSize().width, super.getPreferredSize().height);
                }

                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
        scriptLabel.setLabelFor(scriptTextField);
        scriptTextField.getAccessibleContext().setAccessibleDescription(Bundle.LoadGeneratorCustomizer_ScriptFieldAccessDescr());
        scriptTextField.getDocument().addDocumentListener(this);
        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 0, 5);
        scriptSettingsContainer.add(scriptTextField, constraints);

        // scriptButton
        scriptButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(scriptButton, Bundle.LoadGeneratorCustomizer_BrowseButtonText());
        scriptButton.addActionListener(this);
        constraints = new GridBagConstraints();
        constraints.gridx = 3;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 0);
        scriptSettingsContainer.add(scriptButton, constraints);

        // scriptSettingsContainer
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 20, 5, 13);
        add(scriptSettingsContainer, constraints);

        // --- next row ----------------------------------------------------------
        ButtonGroup stopRadiosGroup = new ButtonGroup();
        JPanel stopSettingsContainer = new JPanel(new GridBagLayout());

        // stopLabel
        stopLabel = new JLabel(Bundle.LoadGeneratorCustomizer_StopLabelText());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 5);
        stopSettingsContainer.add(stopLabel, constraints);

        // thirdLineCaptionSpacer
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 0);
        stopSettingsContainer.add(thirdLineCaptionSpacer, constraints);

        // stopDefineRadio
        stopDefineRadio = new JRadioButton();
        org.openide.awt.Mnemonics.setLocalizedText(stopDefineRadio, Bundle.LoadGeneratorCustomizer_DefineRadioText());
        stopRadiosGroup.add(stopDefineRadio);
        stopDefineRadio.addChangeListener(this);
        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 5);
        stopSettingsContainer.add(stopDefineRadio, constraints);

        // stopOnStopRadio
        stopOnStopRadio = new JRadioButton();
        org.openide.awt.Mnemonics.setLocalizedText(stopOnStopRadio, Bundle.LoadGeneratorCustomizer_StopRadioText());
        stopOnStopRadio.addChangeListener(this);
        stopRadiosGroup.add(stopOnStopRadio);
        constraints = new GridBagConstraints();
        constraints.gridx = 3;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 0);
        stopSettingsContainer.add(stopOnStopRadio, constraints);

        // measureSettingsContainer
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 20, 10, 13);
        add(stopSettingsContainer, constraints);

        // --- next row ----------------------------------------------------------
        JPanel locationBeginHeaderContainer = new JPanel(new GridBagLayout());

        // locationBeginHeaderLabel
        locationBeginHeaderLabel = new JLabel(Bundle.LoadGeneratorCustomizer_LocationBeginLabelText());
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
        constraints.gridy = 5;
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
        constraints.gridy = 6;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 20, 12, 13);
        add(locationBeginCustomizer, constraints);

        // --- next row ----------------------------------------------------------
        JPanel locationEndHeaderContainer = new JPanel(new GridBagLayout());

        // locationEndHeaderLabel
        locationEndHeaderLabel = new JLabel(Bundle.LoadGeneratorCustomizer_LocationEndLabelText());
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
        constraints.gridy = 7;
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
        constraints.gridy = 8;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 20, 20, 13);
        add(locationEndCustomizer, constraints);

        // --- next row ----------------------------------------------------------
        JPanel fillerPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 9;
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

        LoadGeneratorCustomizer main = new LoadGeneratorCustomizer("Load Generator", null); // NOI18N
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
        int requiredCaptionAreaWidth1 = nameLabel.getPreferredSize().width - 12; // nameLabel starts at 8, locationCustomizer at 20 => -12
        int diffCaptionAreaWidth1 = getPreferredCaptionAreaWidth() - requiredCaptionAreaWidth1;
        int normalizedCaptionAreaWidth1 = (diffCaptionAreaWidth1 > 0) ? diffCaptionAreaWidth1 : 0;

        int requiredCaptionAreaWidth2 = scriptLabel.getPreferredSize().width;
        int diffCaptionAreaWidth2 = getPreferredCaptionAreaWidth() - requiredCaptionAreaWidth2;
        int normalizedCaptionAreaWidth2 = (diffCaptionAreaWidth2 > 0) ? diffCaptionAreaWidth2 : 0;

        int requiredCaptionAreaWidth3 = stopLabel.getPreferredSize().width;
        int diffCaptionAreaWidth3 = getPreferredCaptionAreaWidth() - requiredCaptionAreaWidth3;
        int normalizedCaptionAreaWidth3 = (diffCaptionAreaWidth3 > 0) ? diffCaptionAreaWidth3 : 0;

        firstLineCaptionSpacer.setBorder(BorderFactory.createEmptyBorder(0, normalizedCaptionAreaWidth1, 0, 0));
        secondLineCaptionSpacer.setBorder(BorderFactory.createEmptyBorder(0, normalizedCaptionAreaWidth2, 0, 0));
        thirdLineCaptionSpacer.setBorder(BorderFactory.createEmptyBorder(0, normalizedCaptionAreaWidth3, 0, 0));
        locationBeginCustomizer.normalizeCaptionAreaWidth(getPreferredCaptionAreaWidth());
        locationEndCustomizer.normalizeCaptionAreaWidth(getPreferredCaptionAreaWidth());
    }

    public void removeUpdate(DocumentEvent e) {
        updateValidity();
    }

    public void stateChanged(ChangeEvent e) {
        if ((e.getSource() == stopDefineRadio) || (e.getSource() == stopOnStopRadio)) {
            boolean selected = stopDefineRadio.isSelected();
            locationEndCustomizer.setEnabled(selected);
            locationEndHeaderLabel.setEnabled(selected);
            locationEndHeaderSeparator.setEnabled(selected);
            updateValidity();
        }
    }

    public void validityChanged(boolean isValid) {
        updateValidity();
    }

    private JFileChooser getFileChooser() {
        if (fileChooser == null) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setMultiSelectionEnabled(false);
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
            chooser.setDialogTitle(Bundle.LoadGeneratorCustomizer_ChooseScriptDialogCaption());
    
            chooser.setFileFilter(new FileFilter() {
                    private Set<String> extensions = new HashSet<String>();

                    {
                        LoadGenPlugin lg = Lookup.getDefault().lookup(LoadGenPlugin.class);
                        extensions = lg.getSupportedExtensions();
                    }

                    public boolean accept(File f) {
                        return f.isDirectory() || extensions.contains(FileUtil.getExtension(f.getPath()));
                    }

                    public String getDescription() {
                        return Bundle.LoadGeneratorCustomizer_SupportedFiles();
                    }
                });

            fileChooser = chooser;
        }

        return fileChooser;
    }

    private boolean isNameEmpty() {
        return nameTextField.getText().trim().length() == 0;
    }

    private boolean isScriptValid() {
        String fileName = scriptTextField.getText();
        File file = new File(fileName);

        if (file.exists() && file.isFile()) {
            LoadGenPlugin lg = Lookup.getDefault().lookup(LoadGenPlugin.class);

            if (lg == null) {
                return false;
            }

            String ext = FileUtil.getExtension(scriptTextField.getText());

            return lg.getSupportedExtensions().contains(ext);
        }

        return false;
    }

    private boolean areEndLocationSettingsValid() {
        return stopOnStopRadio.isSelected() || locationEndCustomizer.areSettingsValid();
    }

    private void updateValidity() {
        boolean isValid = !isNameEmpty() && isScriptValid() && locationBeginCustomizer.areSettingsValid()
                          && areEndLocationSettingsValid();

        if (isValid != areSettingsValid()) {
            fireValidityChanged(isValid);
        }
    }
}
