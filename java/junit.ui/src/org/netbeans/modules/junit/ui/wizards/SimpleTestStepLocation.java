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

package org.netbeans.modules.junit.ui.wizards;

import org.netbeans.modules.junit.api.JUnitUtils;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.java.testrunner.GuiUtils;
import org.netbeans.modules.gsf.testrunner.api.NamedObject;
import org.netbeans.modules.gsf.testrunner.api.SizeRestrictedPanel;
import org.netbeans.modules.java.testrunner.CommonSettings;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.nodes.NodeOperation;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.UserCancelException;

/**
 *
 * @author  Marian Petras
 */
public final class SimpleTestStepLocation implements WizardDescriptor.Panel<WizardDescriptor> {
    
    private final String testClassNameSuffix
            = NbBundle.getMessage(CommonSettings.class,
                                  "PROP_test_classname_suffix");        //NOI18N
    
    private Component visualComp;
    private List<ChangeListener> changeListeners;
    private JTextField tfClassToTest;
    private JButton btnBrowse;
    private JTextField tfTestClass;
    private JTextField tfProjectName;
    private JComboBox cboxLocation;
    private JTextField tfCreatedFile;
    
    private JCheckBox chkPublic;
    private JCheckBox chkProtected;
    private JCheckBox chkPackagePrivate;
    private JCheckBox chkSetUp;
    private JCheckBox chkTearDown;
    private JCheckBox chkBeforeClass;
    private JCheckBox chkAfterClass;
    private JCheckBox chkMethodBodies;
    private JCheckBox chkJavadoc;
    private JCheckBox chkHints;

    /** error message */
    private String errMsg;
    private String msgClassNameInvalid;
    private String msgClassToTestDoesNotExist;

    
    /**
     * project to create a test class in
     */
    private Project project;
    private WizardDescriptor wizard;
    

    // focus change detection mechanism
    
    /**
     * true, if the current chosen project have multiple testable SourceGroups.
     * If it does, class name entered in the Class to Test textfield must be
     * checked agains all of them (to detect ambiguity) and if there are
     * multiple classes matching, the user must be forced to choose one
     * before leaving the textfield.
     * <p>
     * The focus change detection mechanism is activated by the
     * {@link #hierarchyListener} after this wizard panel is added
     * to the wizard dialog. The listener activates the mechanism
     * only if the mechanism is
     * {@linkplain #focusChangeDetectionEnabled enabled}.
     *
     * @see  #setUp
     */
    private boolean multipleSourceRoots;
    /**
     * true if the focus change detection mechanism is enabled.
     * Being it enabled does not mean that it is activated
     * - it cannot be activated until the visual component is added
     * to the wizard dialog
     */
    private boolean interactionRestrictionsEnabled = false;
    /**
     * true if the focus change detection mechanism is active
     */
    private boolean interactionRestrictionsActive = false;
    /** <!-- PENDING --> */
    private boolean interactionRestrictionsSuspended = false;
    /** */
    private boolean mouseClicksBlocked = false;
    /**
     * hierarchy listener that detects when the visual component
     * is added to the wizard dialog. Once it is added to the dialog,
     * the focus change detection mechanism can be activated.
     *
     * @see  #focusChangeDetectionEnabled
     */
    private HierarchyListener displayabilityListener;
    /** root pane of the wizard dialog */
    private JRootPane rootPane;
    /**
     * default button of the wizard.
     * It is actually the default button of the dialog's {@link #rootPane}.
     */
    private JButton defaultButton;
    /**
     * action key of the root pane's original default action
     * <!-- PENDING -->
     */
    private String rootPaneDefaultActionKey;
    /**
     * root pane's original default action
     * <!-- PENDING -->
     */
    private Action rootPaneDefaultAction;
    /**
     * mouse listener of the wizard dialog's glass pane.
     * It is a part of the focus change detection mechanism.
     */
    private MouseInputListener glassPaneListener;
    /**
     * UI components on which mouse events are checked and evauluated.
     * The mouse events are checked only if there are
     * {@link #multipleSourceRoots}.
     */
    private Component[] mouseBlocked;
    /** 
     * UI components on which mnemonic activation is checked and evaluated.
     * Mnemonic activation events are checked only if there are
     * {@link #multipleSourceRoots}.
     */
    private JComponent[] mnemonicBlocked;
    /**
     * information about actions mapped to action keys of UI components
     * accessible using mnemonics.
     * This is used for blocking access to those components using
     * mnemonics and for restoring the UI components' action maps
     * to the original state.
     *
     * @see  #blockMnemonics
     * @see  #unblockMnemonics
     */
    private ActionMappingInfo[] actionMappingInfo;
    /**
     * component that is explicitely allowed to gain focus.
     * This is used when a button press event is about to be dispatched
     * to the button, so that the focus listener does not interrupt
     * focus transfer to the button.
     */
    private Component focusGainAllowedFor;
    

    // project structure (static)
    
    /**
     * <code>SourceGroups</code> that have at least one test
     * <code>SourceGroup</code> assigned. It is equal to set of keys
     * of the {@link #sourcesToTestsMap}.
     * It is updated whenever {@link #project} changes.
     *
     * @see  #setUp
     */
    private SourceGroup[] testableSourceGroups;
    /** root folders of {@link #testableSourceGroups} */
    private FileObject[] testableSourceGroupsRoots;
    /** <!-- PENDING --> */
    private SourceGroup[] allTestSourceGroups;
    /**
     * relation between <code>SourceGroup</code>s
     * and their respective test <code>SourceGroup</code>s.
     * It is updated whenever {@link #project} changes.
     *
     * @see  #setUp
     */
    private Map<SourceGroup,Object[]> sourcesToTestsMap;
    

    // entered and computed data
    
    /**
     * index of the first <code>SourceGroup</code> where a file named
     * according to contents of {@link #srcRelFileNameSys} was found.
     * The search is performed in {@link #testableSourceGroupsRoots}.
     * If such a file is not found in any of the source groups roots,
     * this variable is set to <code>-1</code>.
     *
     * @see  #classExists
     */
    private int sourceGroupParentIndex = -1;
    /** */
    private FileObject srcFile;
    private SourceGroup srcGroup = null;
    private String testsRootDirName = "";                               //NOI18N
    private String srcRelFileNameSys = "";                              //NOI18N
    private String testRelFileName = "";                                //NOI18N
    /** */
    private FileObject testRootFolder;
    /** */
    private int classNameLength = 0;
    /** length of the string denoting name of the selected SourceGroup */
    private boolean srcGroupNameDisplayed = false;
    /** <!-- PENDING --> */
    private boolean programmaticChange = false;
    /** <!-- PENDING --> */
    private boolean navigationFilterEnabled = false;
    /** <!-- PENDING --> */
    private ClsNameNavigationFilter clsNameNavigationFilter;
    /** <!-- PENDING --> */
    private ClsNameDocumentFilter clsNameDocumentFilter;
    
    /** */
    private boolean ignoreCboxItemChanges = false;
    /** */
    private boolean ignoreClsNameChanges = false;

    
    // validation of entered data
    
    /**
     * <code>true</code> if data entered in the form are valid.
     * The data are valid if the entered class name denotes an existing
     * class and at least one of the <em>Method Access Levels</em>
     * checkboxes is selected.
     */
    private boolean isValid = false;
    /** is the class name non-empty and valid? */
    private boolean classNameValid = false;
    /**
     * <code>true</code> if and only if a file named
     * according to contents of {@link #srcRelFileNameSys} was found.
     * The search is performed in {@link #testableSourceGroupsRoots}.
     * If this variable is <code>true</code>, variable
     * {@link #sourceGroupParentIndex} is set to a non-negative value.
     */
    private boolean classExists = false;

    
    //--------------------------------------------------------------------------
    
    public SimpleTestStepLocation() {
        visualComp = createVisualComp();
    }
    
    private Component createVisualComp() {
        JLabel lblClassToTest = new JLabel();
        JLabel lblCreatedTestClass = new JLabel();
        JLabel lblProject = new JLabel();
        JLabel lblLocation = new JLabel();
        JLabel lblFile = new JLabel();
        tfClassToTest = new JTextField(25);
        btnBrowse = new JButton();
        tfTestClass = new JTextField();
        tfProjectName = new JTextField();
        cboxLocation = new JComboBox();
        tfCreatedFile = new JTextField();
        
        ResourceBundle bundle
                = NbBundle.getBundle(SimpleTestStepLocation.class);
        
        Mnemonics.setLocalizedText(lblClassToTest,
                                   bundle.getString("LBL_ClassToTest"));//NOI18N
        Mnemonics.setLocalizedText(lblCreatedTestClass,
                                   bundle.getString("LBL_TestClass"));  //NOI18N
        Mnemonics.setLocalizedText(lblProject,
                                   bundle.getString("LBL_Project"));    //NOI18N
        Mnemonics.setLocalizedText(lblLocation,
                                   bundle.getString("LBL_Location"));   //NOI18N
        Mnemonics.setLocalizedText(lblFile,
                                   bundle.getString("LBL_CreatedFile"));//NOI18N
        Mnemonics.setLocalizedText(btnBrowse,
                                   bundle.getString("LBL_Browse"));     //NOI18N
        
        lblClassToTest.setLabelFor(tfClassToTest);
        lblCreatedTestClass.setLabelFor(tfTestClass);
        lblProject.setLabelFor(tfProjectName);
        lblFile.setLabelFor(tfCreatedFile);
        lblLocation.setLabelFor(cboxLocation);
        
        tfTestClass.setEditable(false);
        tfProjectName.setEditable(false);
        tfCreatedFile.setEditable(false);
        
        tfTestClass.setFocusable(false);
        tfProjectName.setFocusable(false);
        tfCreatedFile.setFocusable(false);
        
        cboxLocation.setEditable(false);
        
        JCheckBox[] chkBoxes;
        
        JComponent accessLevels = GuiUtils.createChkBoxGroup(
                NbBundle.getMessage(
                        GuiUtils.class,
                        "CommonTestsCfgOfCreate.groupAccessLevels"),          //NOI18N
                chkBoxes = GuiUtils.createCheckBoxes(new String[] {
                        GuiUtils.CHK_PUBLIC,
                        GuiUtils.CHK_PROTECTED,
                        GuiUtils.CHK_PACKAGE}));
        chkPublic = chkBoxes[0];
        chkProtected = chkBoxes[1];
        chkPackagePrivate = chkBoxes[2];
        
        JComponent optCode = GuiUtils.createChkBoxGroup(
                NbBundle.getMessage(
                        GuiUtils.class,
                        "CommonTestsCfgOfCreate.groupOptCode"),               //NOI18N
                chkBoxes = GuiUtils.createCheckBoxes(new String[] {
                        GuiUtils.CHK_SETUP,
                        GuiUtils.CHK_TEARDOWN,
                        GuiUtils.CHK_BEFORE_CLASS,
                        GuiUtils.CHK_AFTER_CLASS,
                        GuiUtils.CHK_METHOD_BODIES}));
        chkSetUp = chkBoxes[0];
        chkTearDown = chkBoxes[1];
        chkBeforeClass = chkBoxes[2];
        chkAfterClass = chkBoxes[3];
        chkMethodBodies = chkBoxes[4];
        
        JComponent optComments = GuiUtils.createChkBoxGroup(
                NbBundle.getMessage(
                        GuiUtils.class,
                        "CommonTestsCfgOfCreate.groupOptComments"),           //NOI18N
                chkBoxes = GuiUtils.createCheckBoxes(new String[] {
                        GuiUtils.CHK_JAVADOC,
                        GuiUtils.CHK_HINTS}));
        chkJavadoc = chkBoxes[0];
        chkHints = chkBoxes[1];
                        
        /* set layout of the components: */
        JPanel targetPanel
                = new SizeRestrictedPanel(new GridBagLayout(), false, true);
        
        GridBagConstraints gbcLeft = new GridBagConstraints();
        gbcLeft.anchor = GridBagConstraints.WEST;
        gbcLeft.gridwidth = 1;
        gbcLeft.insets = new Insets(0, 0, 6, 12);
        gbcLeft.fill = GridBagConstraints.NONE;
        gbcLeft.weightx = 0.0f;
        
        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.anchor = GridBagConstraints.WEST;
        gbcRight.gridwidth = GridBagConstraints.REMAINDER;
        gbcRight.insets = new Insets(0, 0, 6, 0);
        gbcRight.fill = GridBagConstraints.BOTH;
        gbcRight.weightx = 1.0f;
        
        // Class to Test:
        
        gbcRight.gridwidth = 1;
        
        GridBagConstraints gbcBrowse = new GridBagConstraints();
        gbcBrowse.insets = new Insets(0, 11, 6, 0);
        gbcBrowse.gridwidth = GridBagConstraints.REMAINDER;
        
        targetPanel.add(lblClassToTest, gbcLeft);
        targetPanel.add(tfClassToTest, gbcRight);
        targetPanel.add(btnBrowse, gbcBrowse);
        
        // Created Test Class:
        
        gbcLeft.insets.bottom = gbcRight.insets.bottom = 24;
        
        targetPanel.add(lblCreatedTestClass, gbcLeft);
        targetPanel.add(tfTestClass, gbcRight);
        targetPanel.add(new JPanel(), gbcBrowse);               //filler
        
        // Project:
        
        gbcRight.gridwidth = GridBagConstraints.REMAINDER;
        
        gbcLeft.insets.bottom = gbcRight.insets.bottom = 6;
        
        targetPanel.add(lblProject, gbcLeft);
        targetPanel.add(tfProjectName, gbcRight);
        
        // Location:
        
        gbcLeft.insets.bottom = gbcRight.insets.bottom = 12;
        
        targetPanel.add(lblLocation, gbcLeft);
        targetPanel.add(cboxLocation, gbcRight);
        
        // Created File:
        
        gbcLeft.insets.bottom = gbcRight.insets.bottom = 0;
        
        targetPanel.add(lblFile, gbcLeft);
        targetPanel.add(tfCreatedFile, gbcRight);
        
        JComponent optionsBox = new SizeRestrictedPanel(false, true);
        optionsBox.setLayout(
                new BoxLayout(optionsBox, BoxLayout.X_AXIS));
        optionsBox.add(accessLevels);
        optionsBox.add(Box.createHorizontalStrut(18));
        optionsBox.add(optCode);
        optionsBox.add(Box.createHorizontalStrut(18));
        optionsBox.add(optComments);
        //align groups of the checkboxes vertically to the top:
        accessLevels.setAlignmentY(0.0f);
        optCode.setAlignmentY(0.0f);
        optComments.setAlignmentY(0.0f);
        
        final Box result = Box.createVerticalBox();
        result.add(targetPanel);
        result.add(Box.createVerticalStrut(12));
            JPanel separatorPanel = new SizeRestrictedPanel(new GridLayout(),
                                                            false, true);
            separatorPanel.add(new JSeparator());
        result.add(separatorPanel);
        result.add(Box.createVerticalStrut(12));
        result.add(optionsBox);
        //result.add(Box.createVerticalGlue());  //not necessary
        
        /* tune layout of the components within the box: */
        targetPanel.setAlignmentX(0.0f);
        optionsBox.setAlignmentX(0.0f);
        optCode.setAlignmentX(0.0f);
        optComments.setAlignmentX(0.0f);
        
        result.setName(bundle.getString("LBL_panel_ChooseClass"));      //NOI18N
        
        addAccessibilityDescriptions(result);
        setUpInteraction();
        
        return result;
    }
    
    /**
     * Sets up tooltips and accessibility names and descriptions
     * for GUI elements of the wizard panel.
     *
     * @param  wizPanel  wizard panel whose elements need to be made accessible.
     */
    private void addAccessibilityDescriptions(Component wizPanel) {
        final ResourceBundle bundle
                = NbBundle.getBundle(SimpleTestStepLocation.class);
        
        tfClassToTest.setToolTipText(
                bundle.getString("SimpleTest.classToTest.toolTip"));    //NOI18N
        tfClassToTest.getAccessibleContext().setAccessibleName(
                bundle.getString("SimpleTest.classToTest.AN"));         //NOI18N
        tfClassToTest.getAccessibleContext().setAccessibleDescription(
                bundle.getString("SimpleTest.classToTest.AD"));         //NOI18N
        
        btnBrowse.setToolTipText(
                bundle.getString("SimpleTest.btnBrowse.toolTip"));      //NOI18N
        btnBrowse.getAccessibleContext().setAccessibleName(
                bundle.getString("SimpleTest.btnBrowse.AN"));           //NOI18N
        btnBrowse.getAccessibleContext().setAccessibleDescription(
                bundle.getString("SimpleTest.btnBrowse.AD"));           //NOI18N
        
        cboxLocation.setToolTipText(
                bundle.getString("SimpleTest.location.toolTip"));       //NOI18N
        cboxLocation.getAccessibleContext().setAccessibleName(
                bundle.getString("SimpleTest.location.AN"));            //NOI18N
        cboxLocation.getAccessibleContext().setAccessibleDescription(
                bundle.getString("SimpleTest.location.AD"));            //NOI18N
        
        wizPanel.getAccessibleContext().setAccessibleDescription(
                bundle.getString("SimpleTest.AD"));                     //NOI18N
    }
    
    /**
     * <!-- PENDING -->
     *
     * @return  <code>true</code> if the selected item has changed,
     *          <code>false</code> otherwise
     */
    private boolean updateLocationComboBox() {
        Object[] srcRootsToOffer;
        
        if ((allTestSourceGroups.length == 1) || (srcGroup == null)) {
            srcRootsToOffer = allTestSourceGroups;
        } else {
            srcRootsToOffer = sourcesToTestsMap.get(srcGroup);
        }
        
        Object previousSelectedItem = cboxLocation.getSelectedItem();
        
        ignoreCboxItemChanges = true;
        try {
            Object[] items = createNamedItems(srcRootsToOffer);
            cboxLocation.setModel(new DefaultComboBoxModel(items));
            if (previousSelectedItem != null) {
                cboxLocation.setSelectedItem(previousSelectedItem);//may not process
            }
        } finally {
            ignoreCboxItemChanges = false;
        }
        
        Object newSelectedItem = cboxLocation.getSelectedItem();
        
        return !newSelectedItem.equals(previousSelectedItem);
    }
    
    /**
     */
    private static NamedObject[] createNamedItems(final Object[] srcRoots) {
        
        //PENDING - should not the source groups be sorted (alphabetically)?
        NamedObject[] items = new NamedObject[srcRoots.length];
        for (int i = 0; i < srcRoots.length; i++) {
            String name = (srcRoots[i] instanceof SourceGroup)
                          ? ((SourceGroup) srcRoots[i]).getDisplayName()
                          : (srcRoots[i] instanceof FileObject)
                            ? FileUtil.getFileDisplayName((FileObject)
                                                          srcRoots[i])
                            : srcRoots[i].toString();
            items[i] = new NamedObject(srcRoots[i],
                                       name);
        }
        return items;
    }
    
    /**
     */
    private void setUpInteraction() {
        
        class UIListener implements ActionListener, DocumentListener,
                                    FocusListener, ItemListener {
            public void actionPerformed(ActionEvent e) {
                
                /* button Browse... pressed */
                
                chooseClass();
            }
            public void insertUpdate(DocumentEvent e) {
                classNameChanged();
            }
            public void removeUpdate(DocumentEvent e) {
                classNameChanged();
            }
            public void changedUpdate(DocumentEvent e) {
                classNameChanged();
            }
            public void focusGained(FocusEvent e) {
                Object source = e.getSource();
                if (source == tfClassToTest) {
                    //tfClassToTest.getDocument().addDocumentListener(this);
                }
            }
            public void focusLost(FocusEvent e) {
                Object source = e.getSource();
                if (source == tfClassToTest) {
                    //tfClassToTest.getDocument().removeDocumentListener(this);
                    if (!e.isTemporary()) {
                        tfClassToTestFocusLost(e);
                    }
                } else if ((source == btnBrowse) && !e.isTemporary()) {
                    btnBrowseFocusLost(e);
                }
            }
            public void itemStateChanged(ItemEvent e) {
                if (e.getSource() == cboxLocation) {
                    if (!ignoreCboxItemChanges) {
                        locationChanged();
                    }
                } else {
                    assert false;
                }
            }
        }
        
        final UIListener listener = new UIListener();
        
        btnBrowse.addActionListener(listener);
        tfClassToTest.addFocusListener(listener);
        btnBrowse.addFocusListener(listener);
        cboxLocation.addItemListener(listener);
        tfClassToTest.getDocument().addDocumentListener(listener);
    }
    
    /**
     */
    private void tfClassToTestFocusLost(FocusEvent e) {
        final Component allowFocusGain = focusGainAllowedFor;
        focusGainAllowedFor = null;
        
        if (multipleSourceRoots
                && interactionRestrictionsActive
                && !interactionRestrictionsSuspended) {

            final Component opposite = e.getOppositeComponent();

            if ((allowFocusGain != null) && (opposite == allowFocusGain)) {
                return;
            }
            if (opposite == btnBrowse) {
                return;
            }
            if ((opposite instanceof JLabel)
                    && (((JLabel) opposite).getLabelFor() == tfClassToTest)) {
                /*
                 * When a JLabel's mnemonic key is pressed, the JLabel gains focus
                 * until the key is released again. That's why we must ignore such
                 * focus transfers.
                 */
                return;
            }
            
            if (!maybeDisplaySourceGroupChooser()) {
                
                /* send the request back to the Test to Class textfield: */
                tfClassToTest.requestFocus();
            }
        }
    }
    
    /**
     */
    private void btnBrowseFocusLost(FocusEvent e) {
        final Component allowFocusGain = focusGainAllowedFor;
        focusGainAllowedFor = null;
        
        if (multipleSourceRoots
                && interactionRestrictionsActive
                && !interactionRestrictionsSuspended) {

            final Component opposite = e.getOppositeComponent();

            if ((allowFocusGain != null) && (opposite == allowFocusGain)) {
                return;
            }
            if (opposite == tfClassToTest) {
                return;
            }
            if ((opposite instanceof JLabel)
                    && (((JLabel) opposite).getLabelFor() == tfClassToTest)) {
                /*
                 * When a JLabel's mnemonic key is pressed, the JLabel gains focus
                 * until the key is released again. That's why we must ignore such
                 * focus transfers.
                 */
                return;
            }

            if (!maybeDisplaySourceGroupChooser()) {
                
                /* send the request back to the Browse... button: */
                btnBrowse.requestFocus();
            }
        }
    }
    
    /**
     * <!-- PENDING -->
     *
     * @return  <code>false</code> if the SourceGroup chooser was displayed
     *          and the user cancelled the choice; <code>true</code> otherwise
     */
    private boolean maybeDisplaySourceGroupChooser() {
        assert multipleSourceRoots;
        
        if (classExists && (srcGroup == null)) {
            SourceGroup[] candidates = findParentGroupCandidates();
            
            assert candidates.length != 0;      //because the class exists
            
            if (candidates.length == 1) {
                setSelectedSrcGroup(candidates[0]);
                return true;
            } else {
                SourceGroup chosenSrcGroup = chooseSrcGroup(candidates);
                if (chosenSrcGroup != null) {
                    setSelectedSrcGroup(chosenSrcGroup);
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            return true;
        }
    }
    
    /**
     * Displays a source root chooser which allows the user to choose
     * a parent source root for the entered class name.
     *
     * @param  candidates  source roots to be offered to the user
     * @return  the chosen source root,
     *          or <code>null</code> if the user cancelled the choice
     */
    private SourceGroup chooseSrcGroup(final SourceGroup[] candidates) {
        assert (candidates != null) && (candidates.length != 0);

        final String[] rootNames = new String[candidates.length];
        for (int i = 0; i < rootNames.length; i++) {
            rootNames[i] = candidates[i].getDisplayName();
        }
        
        final JButton btn = new JButton(
                NbBundle.getMessage(getClass(),
                                    "LBL_SelectBtn"));                  //NOI18N
        final JList list = new JList(rootNames);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                btn.setEnabled(!list.isSelectionEmpty());
            }
        });
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.add(list, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(12, 12, 0, 12),
                BorderFactory.createBevelBorder(BevelBorder.LOWERED)));
        
        String dialogTitle = NbBundle.getMessage(
                                    getClass(),
                                    "LBL_SourceRootChooserTitle");      //NOI18N
        DialogDescriptor descriptor
                = new DialogDescriptor(panel,                   //component
                                       dialogTitle,             //title
                                       true,                    //modal
                                       new Object[] {           //options
                                               btn,
                                               NotifyDescriptor.CANCEL_OPTION},
                                       btn,                     //default option
                                       DialogDescriptor.DEFAULT_ALIGN,
                                       (HelpCtx) null,
                                       (ActionListener) null);
        Object selected = DialogDisplayer.getDefault().notify(descriptor);
        return (selected == btn) ? candidates[list.getSelectedIndex()]
                                 : (SourceGroup) null;
    }
    
    /**
     */
    private void setSelectedSrcGroup(SourceGroup srcGroup) {
        setSelectedSrcGroup(srcGroup, true);
    }
    
    /**
     * <!-- PENDING -->
     */
    private void setSelectedSrcGroup(SourceGroup srcGroup, boolean updateDisp) {
        assert multipleSourceRoots
               && ((srcGroup == null) || (classNameValid && classExists));
        
        if (!checkObjChanged(this.srcGroup, srcGroup)) {
            return;
        }
        
        this.srcGroup = srcGroup;
        
        if (updateDisp) {
            
            /* update the display: */
            try {
                programmaticChange = true;

                String className = tfClassToTest.getText()
                                   .substring(0, classNameLength);
                String srcGroupDisplay = getSrcGrpDisp(srcGroup);
                
                ignoreClsNameChanges = true;
                tfClassToTest.setText(className + srcGroupDisplay);
                ignoreClsNameChanges = false;
                
                classNameLength = className.length();
                classNameChanged();
                srcGroupNameDisplayed = true;
                setNavigationFilterEnabled(true);
            } finally {
                ignoreClsNameChanges = false;
                programmaticChange = false;
            }
        }
        
        updateInteractionRestrictionsState();

            /*
         * There is no need to check and set validity.
         * The user should be offered to choose a source root only when
         * the entered class name is valid and the class exists
         * in at least two source roots.
         */
        
        /* update target folder: */
        if (allTestSourceGroups.length > 1) {
            boolean locationChanged = updateLocationComboBox();
            if (locationChanged) {
                updateTargetFolderData();
            }
        }
        
        /* update name of the file to be created: */
        updateCreatedFileName();
        
        /* set 'srcFile': */
        srcFile = (srcGroup != null)
                  ? srcGroup.getRootFolder().getFileObject(srcRelFileNameSys)
                  : null;
        
        assert (srcGroup == null) || (srcFile != null);
    }
    
    /**
     */
    private static String getSrcGrpDisp(SourceGroup srcGroup) {
        if (srcGroup == null) {
            return "";                                                  //NOI18N
        } else {
            String srcGroupName = srcGroup.getDisplayName();
            return new StringBuffer(srcGroupName.length() + 3)
                   .append(' ')
                   .append('(').append(srcGroupName).append(')')
                   .toString();
        }
    }
    
    /**
     */
    private void setNavigationFilterEnabled(boolean enabled) {
        if (enabled == navigationFilterEnabled) {
            if (enabled) {
                clsNameNavigationFilter.ensureCursorInRange();
            }
            return;
        }
        
        if (enabled) {
            if (clsNameNavigationFilter == null) {
                clsNameNavigationFilter = new ClsNameNavigationFilter();
            }
            tfClassToTest.setNavigationFilter(clsNameNavigationFilter);
            clsNameNavigationFilter.ensureCursorInRange();
        } else {
            tfClassToTest.setNavigationFilter(null);
        }
        this.navigationFilterEnabled = enabled;
    }
    
    /**
     * <!-- PENDING -->
     */
    private void updateInteractionRestrictionsState() {
        setInteractionRestrictionsSuspended(
                !classNameValid || !classExists || (srcGroup != null));
    }
    
    /**
     */
    private void updateTargetFolderData() {
        Object item = cboxLocation.getSelectedItem();
        if (item != null) {
            SourceGroup targetSourceGroup = (SourceGroup)
                                            ((NamedObject) item).object;
            testRootFolder = targetSourceGroup.getRootFolder();
            testsRootDirName = FileUtil.getFileDisplayName(testRootFolder);
        } else {
            testRootFolder = null;
            testsRootDirName = "";                                      //NOI18N
        }
    }
    
    /**
     * Called whenever selection in the Location combo-box is changed.
     */
    private void locationChanged() {
        updateTargetFolderData();
        updateCreatedFileName();
    }
    
    /**
     */
    private void classNameChanged() {
        if (ignoreClsNameChanges) {
            return;
        }
        
        String className;
        if (!programmaticChange) {
            className = tfClassToTest.getText().trim();
            classNameLength = className.length();
        } else {
            className = tfClassToTest.getText().substring(0, classNameLength);
        }
        
        String testClassName;
        if (className.length() != 0) {
            srcRelFileNameSys = className.replace('.', '/')
                                + ".java";                              //NOI18N
            testClassName = className + testClassNameSuffix;
            testRelFileName = testClassName.replace('.', File.separatorChar)
                              + ".java";                                //NOI18N
        } else {
            srcRelFileNameSys = "";                                     //NOI18N
            testClassName = "";                                         //NOI18N
            testRelFileName = "";                                       //NOI18N
        }
        tfTestClass.setText(testClassName);
        
        if (!programmaticChange) {
            updateCreatedFileName();
            if (checkClassNameValidity()) {
                checkSelectedClassExists();
            }
            setErrorMsg(errMsg);
            setValidity();

            /*
             * The user modified the class name.
             * It may be ambiguous - it may match classes in multiple SourceGroups.
             */
            if (multipleSourceRoots) {
                setSelectedSrcGroup(null, false);
            }
        }
        
        if (multipleSourceRoots) {
            updateInteractionRestrictionsState();
        }
    }
    
    /**
     * Identifies all <code>SourceGroup</code>s containing file having the
     * name entered by the user.
     * This method assumes that at least one such <code>SourceGroup</code>
     * has already been found and its index stored in field
     * {@link #sourceGroupParentIndex}.
     *
     * @return  array of matching <code>SourceGroup</code>s
     *          (always contains at least one element)
     */
    private SourceGroup[] findParentGroupCandidates() {
        assert sourceGroupParentIndex >= 0;
        
        List<SourceGroup> cands = null;
        final int count = testableSourceGroups.length;
        for (int i = sourceGroupParentIndex + 1; i < count; i++) {
            final FileObject groupRoot = testableSourceGroupsRoots[i];
            FileObject srcFile = groupRoot.getFileObject(srcRelFileNameSys);
            if (srcFile != null && testableSourceGroups[i].contains(srcFile)) {
                if (cands == null) {
                    cands = new ArrayList<SourceGroup>(testableSourceGroups.length - i + 1);
                    cands.add(testableSourceGroups[sourceGroupParentIndex]);
                }
                cands.add(testableSourceGroups[i]);
            }
        }
        return cands == null
              ? new SourceGroup[] {testableSourceGroups[sourceGroupParentIndex]}
              : cands.toArray(new SourceGroup[0]);
    }
    
    /**
     */
    private void updateCreatedFileName() {
        tfCreatedFile.setText(testsRootDirName + File.separatorChar + testRelFileName);
    }
    
    /**
     * Checks validity of the entered class name, updates messages
     * on the message stack and updates the <code>classNameValid</code> field.
     *
     * @see  #msgStack
     * @see  #setValidity()
     */
    private boolean checkClassNameValidity() {
        String className = tfClassToTest.getText().trim();
        if (srcGroupNameDisplayed) {
            className = className.substring(0, classNameLength);
        }
        
        if (className.length() == 0) {
            errMsg = null;
            classNameValid = false;
        } else if (JUnitUtils.isValidClassName(className)) {
            errMsg = null;
            classNameValid = true;
        } else {
            if (msgClassNameInvalid == null) {
                msgClassNameInvalid = NbBundle.getMessage(
                        GuiUtils.class,
                        "MSG_InvalidClassName");                        //NOI18N
            }
            errMsg = msgClassNameInvalid;
            classNameValid = false;
        }
        
        return classNameValid;
    }
    
    /**
     * Checks whether a class having the entered name exists, updates messages
     * on the message stack and updates the <code>classExists</code> field.
     *
     * @see  #setValidity()
     */
    private boolean checkSelectedClassExists() {
        sourceGroupParentIndex = -1;
        
        final int count = testableSourceGroups.length;
        for (int i = 0; i < count; i++) {
            final FileObject groupRoot = testableSourceGroupsRoots[i];
            FileObject srcFile = groupRoot.getFileObject(srcRelFileNameSys);
            if (srcFile != null && testableSourceGroups[i].contains(srcFile)) {
                this.srcFile = srcFile;
                sourceGroupParentIndex = i;
                break;
            }
        }
        
        classExists = (sourceGroupParentIndex != -1);
        
        if (classExists) {
            errMsg = null;
        } else {
            if (msgClassToTestDoesNotExist == null) {
                msgClassToTestDoesNotExist = NbBundle.getMessage(
                        SimpleTestStepLocation.class,
                        "MSG_ClassToTestDoesNotExist");                 //NOI18N
            }
            errMsg = msgClassToTestDoesNotExist;
        }
        
        return classExists;
    }
    
    /**
     * Updates the <code>isValid</code> field and notifies all registered
     * <code>ChangeListener</code>s if validity has changed.
     */
    private void setValidity() {
        boolean wasValid = isValid;
        
        isValid = classNameValid && classExists;
        
        if (isValid != wasValid) {
            fireChange();
            
            updateInteractionRestrictionsState();
            
            /*
             * This must be called after fireChange() because fireChange()
             * sets state (enabled/disabled) of the default button.
             */
            if (isValid
                    && interactionRestrictionsEnabled
                    && !interactionRestrictionsActive) {
                tryActivateInteractionRestrictions();
            }
        }
    }
    
    /**
     * Displays the given message in the wizard's message area.
     *
     * @param  message  message to be displayed, or <code>null</code>
     *                  if the message area should be cleared
     */
    private void setErrorMsg(String message) {
        if (wizard != null) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
        }
    }
    
    /**
     * Displays a class chooser dialog and lets the user to select a class.
     * If the user confirms their choice, full name of the selected class
     * is put into the <em>Class To Test</em> text field.
     */
    private void chooseClass() {
        try {
            final Node[] sourceGroupNodes
                    = new Node[testableSourceGroups.length];
            for (int i = 0; i < sourceGroupNodes.length; i++) {
                /*
                 * Note:
                 * Precise structure of this view is *not* specified by the API.
                 */
                Node srcGroupNode
                       = PackageView.createPackageView(testableSourceGroups[i]);
                sourceGroupNodes[i]
                       = new FilterNode(srcGroupNode,
                                        new JavaChildren(srcGroupNode));
            }
            
            Node rootNode;
            if (sourceGroupNodes.length == 1) {
                rootNode = new FilterNode(
                        sourceGroupNodes[0],
                        new JavaChildren(sourceGroupNodes[0]));
            } else {
                Children children = new Children.Array();
                children.add(sourceGroupNodes);
                
                AbstractNode node = new AbstractNode(children);
                node.setName("Project Source Roots");                   //NOI18N
                node.setDisplayName(
                        NbBundle.getMessage(getClass(), "LBL_Sources"));//NOI18N
                //PENDING - set a better icon for the root node
                rootNode = node;
            }
            
            NodeAcceptor acceptor = new NodeAcceptor() {
                public boolean acceptNodes(Node[] nodes) {
                    Node.Cookie cookie;
                    return nodes.length == 1
                           && (cookie = nodes[0].getCookie(DataObject.class))
                              != null
                           && ((DataObject) cookie).getPrimaryFile().isFolder()
                              == false;
                }
            };
            
            Node selectedNode = NodeOperation.getDefault().select(
                    NbBundle.getMessage(SimpleTestStepLocation.class,
                                        "LBL_WinTitle_SelectClass"),    //NOI18N
                    NbBundle.getMessage(SimpleTestStepLocation.class,
                                        "LBL_SelectClassToTest"),       //NOI18N
                    rootNode,
                    acceptor)[0];
            
            SourceGroup selectedSourceGroup;
            if (sourceGroupNodes.length == 1) {
                selectedSourceGroup = testableSourceGroups[0];
            } else {
                Node previous = null;
                Node current = selectedNode.getParentNode();
                Node parent;
                while ((parent = current.getParentNode()) != null) {
                    previous = current;
                    current = parent;
                }
                /*
                 * 'current' now contains the root node of displayed node
                 * hierarchy. 'current' contains a parent node of the source
                 * root and 'previous' contains the parent source root of
                 * the selected class.
                 */
                selectedSourceGroup = null;
                Node selectedSrcGroupNode = previous;
                for (int i = 0; i < sourceGroupNodes.length; i++) {
                    if (sourceGroupNodes[i] == selectedSrcGroupNode) {
                        selectedSourceGroup = testableSourceGroups[i];
                        sourceGroupParentIndex = i;
                        break;
                    }
                }
                assert selectedSourceGroup != null;
                assert sourceGroupParentIndex >= 0;
            }
            srcGroup = selectedSourceGroup;
            
            FileObject selectedFileObj
                    = selectedNode.getCookie(DataObject.class).getPrimaryFile();
            
            /* display selected class name: */
            try {
                programmaticChange = true;
                
                String className = getClassName(selectedFileObj);
                classNameLength = className.length();
                if (!multipleSourceRoots) {
                    /*
                     * Caution! Calling setText("className") triggers two
                     * text change events - once when the original text is
                     * cleared and the second time when the new text is set.
                     * Method classNameChanged() must only be called when the
                     * text change is complete (see issue #91794) so we set
                     * the 'ignoreClsNameChanges' flag for the time the text
                     * is being changed and then call the classNameChanged()
                     * explicitely.
                     */
                    ignoreClsNameChanges = true;
                    tfClassToTest.setText(className);
                    ignoreClsNameChanges = false;
                    classNameChanged();
                } else {
                    String srcGroupDisplay = getSrcGrpDisp(selectedSourceGroup);

                    ignoreClsNameChanges = true;
                    tfClassToTest.setText(className + srcGroupDisplay);
                    ignoreClsNameChanges = false;
                    
                    classNameLength = className.length();
                    classNameChanged();
                    srcGroupNameDisplayed = true;
                    setNavigationFilterEnabled(true);
                }
                /*
                 * Change of text of the Class to Test text-field triggers
                 * update of variable 'testRelFileName'.
                 */
            } finally {
                ignoreClsNameChanges = false;
                programmaticChange = false;
            }
            
            /* set class name validity: */
            classNameValid = true;
            classExists = true;
            setErrorMsg(null);
            setValidity();
            updateInteractionRestrictionsState();
            
            /* update target folder: */
            if (multipleSourceRoots && (allTestSourceGroups.length > 1)) {
                boolean locationChanged = updateLocationComboBox();
                if (locationChanged) {
                    updateTargetFolderData();       //sets also 'testRootFolder'
                }
            }
            
            /* update name of the file to be created: */
            updateCreatedFileName();
            
            /* set 'srcFile': */
            srcFile = selectedFileObj;
            
        } catch (UserCancelException ex) {
            // if the user cancels the choice, do nothing
        }
    }
    
    private static String getClassName(FileObject fileObj) {
        //PENDING: is it ensured that the classpath is non-null?
        return ClassPath.getClassPath(fileObj, ClassPath.SOURCE)
               .getResourceName(fileObj, '.', false);
    }
    
    public Component getComponent() {
        return visualComp;
    }
    
    public boolean isValid() {
        return isValid;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.junit.wizards.SimpleTest");//NOI18N
    }
    
    public void readSettings(WizardDescriptor settings) {
        wizard = settings;
        
        chkPublic.setSelected(
               Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_PUBLIC)));
        chkProtected.setSelected(
               Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_PROTECTED)));
        chkPackagePrivate.setSelected(
               Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_PACKAGE)));
        chkSetUp.setSelected(
               Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_SETUP)));
        chkTearDown.setSelected(
               Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_TEARDOWN)));
        chkBeforeClass.setSelected(
                Boolean.TRUE.equals(settings.getProperty(GuiUtils.CHK_BEFORE_CLASS)));
        chkAfterClass.setSelected(
                Boolean.TRUE.equals(settings.getProperty(GuiUtils.CHK_AFTER_CLASS)));
        chkMethodBodies.setSelected(
           Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_METHOD_BODIES)));
        chkJavadoc.setSelected(
               Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_JAVADOC)));
        chkHints.setSelected(
               Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_HINTS)));
    }
    
    public void storeSettings(WizardDescriptor settings) {
        wizard = settings;
        
        wizard.putProperty(SimpleTestCaseWizard.PROP_CLASS_TO_TEST,
                           srcFile);
        wizard.putProperty(SimpleTestCaseWizard.PROP_TEST_ROOT_FOLDER,
                           testRootFolder);
        wizard.putProperty(GuiUtils.CHK_PUBLIC,
                           Boolean.valueOf(chkPublic.isSelected()));
        wizard.putProperty(GuiUtils.CHK_PROTECTED,
                           Boolean.valueOf(chkProtected.isSelected()));
        wizard.putProperty(GuiUtils.CHK_PACKAGE,
                           Boolean.valueOf(chkPackagePrivate.isSelected()));
        wizard.putProperty(GuiUtils.CHK_SETUP,
                           Boolean.valueOf(chkSetUp.isSelected()));
        wizard.putProperty(GuiUtils.CHK_TEARDOWN,
                           Boolean.valueOf(chkTearDown.isSelected()));
        settings.putProperty(GuiUtils.CHK_BEFORE_CLASS,
                           Boolean.valueOf(chkBeforeClass.isSelected()));
        settings.putProperty(GuiUtils.CHK_AFTER_CLASS,
                           Boolean.valueOf(chkAfterClass.isSelected()));
        wizard.putProperty(GuiUtils.CHK_METHOD_BODIES,
                           Boolean.valueOf(chkMethodBodies.isSelected()));
        wizard.putProperty(GuiUtils.CHK_JAVADOC,
                           Boolean.valueOf(chkJavadoc.isSelected()));
        wizard.putProperty(GuiUtils.CHK_HINTS,
                           Boolean.valueOf(chkHints.isSelected()));
    }
    
    public void addChangeListener(ChangeListener l) {
        if (changeListeners == null) {
            changeListeners = new ArrayList<ChangeListener>(4);
        }
        changeListeners.add(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        if (changeListeners != null) {
            if (changeListeners.remove(l) && changeListeners.isEmpty()) {
                changeListeners = null;
            }
        }
    }
    
    private void fireChange() {
        if (changeListeners != null) {
            ChangeEvent e = new ChangeEvent(this);
            for (ChangeListener l : changeListeners) {
                l.stateChanged(e);
            }
        }
    }
    
    /**
     */
    void setUp(final JUnitUtils utils) {
        final Project project = utils.getProject();
        
        if (project == this.project) {
            return;
        }
        
        this.project = project;
        this.sourcesToTestsMap = utils.getSourcesToTestsMap(true);
        
        int sourceGroupsCnt = sourcesToTestsMap.size();
        Set<Map.Entry<SourceGroup,Object[]>> mapEntries = sourcesToTestsMap.entrySet();
        List<SourceGroup> testGroups = new ArrayList<SourceGroup>(sourceGroupsCnt + 4);
        
        testableSourceGroups = new SourceGroup[sourceGroupsCnt];
        testableSourceGroupsRoots = new FileObject[sourceGroupsCnt];
        multipleSourceRoots = (sourceGroupsCnt > 1);
        
        Iterator<Map.Entry<SourceGroup,Object[]>> iterator = mapEntries.iterator();
        for (int i = 0; i < sourceGroupsCnt; i++) {
            Map.Entry<SourceGroup,Object[]> entry = iterator.next();
            SourceGroup srcGroup = entry.getKey();
            
            testableSourceGroups[i] = srcGroup;
            testableSourceGroupsRoots[i] = srcGroup.getRootFolder();
            
            Object[] testGroupsSubset = entry.getValue();
            for (int j = 0; j < testGroupsSubset.length; j++) {
                SourceGroup testGroup = (SourceGroup) testGroupsSubset[j];
                if (!testGroups.contains(testGroup)) {
                    testGroups.add(testGroup);
                }
            }
        }
        allTestSourceGroups = testGroups.toArray(new SourceGroup[0]);
        
        tfProjectName.setText(
                ProjectUtils.getInformation(project).getDisplayName());
        try {
            programmaticChange = true;
            
            ignoreClsNameChanges = true;
            tfClassToTest.setText("");                                  //NOI18N
            ignoreClsNameChanges = false;
            
            classNameLength = 0;
            classNameChanged();
            srcGroupNameDisplayed = false;
            setNavigationFilterEnabled(false);
        } finally {
            ignoreClsNameChanges = false;
            programmaticChange = false;
        }
        if (checkClassNameValidity()) {
            checkSelectedClassExists();
        } else {
            classExists = false;
        }
        setErrorMsg(errMsg);
        setValidity();
        
        //PENDING - if possible, we should pre-set the test source group
        //          corresponding to the currently selected node
        updateLocationComboBox();
        updateTargetFolderData();           //sets also 'testRootFolder'
        updateCreatedFileName();
        
        srcFile = null;
        
        if (!multipleSourceRoots) {
            setInteractionRestrictionsEnabled(false);
        } else {
            AbstractDocument doc = (AbstractDocument)
                                   tfClassToTest.getDocument();
            if (clsNameDocumentFilter == null) {
                clsNameDocumentFilter = new ClsNameDocumentFilter();
            }
            if (doc.getDocumentFilter() != clsNameDocumentFilter) {
                doc.setDocumentFilter(clsNameDocumentFilter);
            }
            setInteractionRestrictionsEnabled(true);
        }
    }

    void selectLocation(FileObject locationFO) {
	Object[] srcRootsToOffer;
        if ((allTestSourceGroups.length == 1) || (srcGroup == null)) {
            srcRootsToOffer = allTestSourceGroups;
        } else {
            srcRootsToOffer = sourcesToTestsMap.get(srcGroup);
        }
	NamedObject namedObject = new NamedObject(srcRootsToOffer[0], "");
	for (int i = 0; i < srcRootsToOffer.length; i++) {
	    Object srcRootToOffer = srcRootsToOffer[i];
	    if(((SourceGroup)srcRootToOffer).getRootFolder().equals(locationFO)) {
		namedObject = new NamedObject(srcRootsToOffer[i], ((SourceGroup)srcRootToOffer).getDisplayName());
		break;
	    }
	}
        cboxLocation.setSelectedItem(namedObject);
        updateTargetFolderData();
        updateCreatedFileName();
    }
    
    /**
     */
    void cleanUp() {
        setInteractionRestrictionsEnabled(false);
    }
    
    /**
     * <!-- PENDING -->
     */
    private void setInteractionRestrictionsEnabled(boolean enabled) {
        if (enabled == interactionRestrictionsEnabled) {
            return;
        }
        
        class DisplayabilityListener implements HierarchyListener {
            public void hierarchyChanged(HierarchyEvent e) {
                long flags = e.getChangeFlags();
                if ((flags & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) {
                    if (visualComp.isDisplayable()) {
                        if (interactionRestrictionsEnabled) {
                            setInteractionRestrictionsActive(true);
                        }
                    } else {
                        setInteractionRestrictionsActive(false);
                    }
                }
            }
        }
        
        if (enabled) {
            this.interactionRestrictionsEnabled = true;
            
            assert displayabilityListener == null;
            displayabilityListener = new DisplayabilityListener();
            visualComp.addHierarchyListener(displayabilityListener);

            if (visualComp.isDisplayable()) {
                setInteractionRestrictionsActive(true);
            }
        } else {
            this.interactionRestrictionsEnabled = false;
            
            setInteractionRestrictionsActive(false);
            
            visualComp.removeHierarchyListener(displayabilityListener);
            displayabilityListener = null;
        }
    }
    
    /**
     * Activates or deactivates the focus detection mechanism.
     * <!-- PENDING -->
     */
    private void setInteractionRestrictionsActive(boolean active) {
        if (active == this.interactionRestrictionsActive) {
            return;
        }
        
        if (active) {
            tryActivateInteractionRestrictions();
        } else {
            deactivateInteractionRestrictions();
        }
    }
    
    /**
     */
    private void tryActivateInteractionRestrictions() {
        assert interactionRestrictionsActive == false;
        assert interactionRestrictionsEnabled;

        if (rootPane == null) {
            rootPane = SwingUtilities.getRootPane(visualComp);
        }
        
        if (rootPane != null) {
            defaultButton = rootPane.getDefaultButton();
            if (defaultButton != null) {
                activateInteractionRestrictions();
            }
        }
    }
    
    /**
     */
    private void activateInteractionRestrictions() {
        assert interactionRestrictionsActive == false;
        assert (rootPane != null) && (defaultButton != null);
        
        if ((mouseBlocked == null) || (mnemonicBlocked == null)) {
            findComponentsToBlock();
            assert (mouseBlocked != null) && (mnemonicBlocked != null);
        }
        blockDefaultRootPaneAction();
        blockMnemonics();
        setMouseClicksBlockingActive(!interactionRestrictionsSuspended);
        
        interactionRestrictionsActive = true;
    }
    
    /**
     */
    private void deactivateInteractionRestrictions() {
        assert interactionRestrictionsActive == true;
        assert (defaultButton != null) && (rootPane != null);
        
        setMouseClicksBlockingActive(false);
        unblockMnemonics();
        unblockDefaultRootPaneAction();
        
        defaultButton = null;
        rootPane = null;

        interactionRestrictionsActive = false;
        interactionRestrictionsSuspended = false;
    }
    
    /**
     */
    private void setInteractionRestrictionsSuspended(boolean suspended) {
        if (suspended != this.interactionRestrictionsSuspended) {
            setMouseClicksBlockingActive(interactionRestrictionsActive
                                         && !suspended);
            this.interactionRestrictionsSuspended = suspended;
        }
    }
    
    /**
     */
    private void setMouseClicksBlockingActive(boolean blockingActive) {
        if (blockingActive != this.mouseClicksBlocked) {
            if (blockingActive) {
                blockMouseClicks();
            } else {
                unblockMouseClicks();
            }
            this.mouseClicksBlocked = blockingActive;
        }
    }
    
    /**
     * Searches the visual component and collects components
     * on which mouse events or activation by mnemonics needs to be check
     * and evaluated.
     *
     * @see  #mouseBlocked
     * @see  #mnemonicBlocked
     */
    private void findComponentsToBlock() {
        assert rootPane != null;
        
        final Collection<Component> mouseBlocked
                = new ArrayList<Component>(20);
        final Collection<JComponent> mnemBlocked
                = new ArrayList<JComponent>(20);

        final List<Component> stack = new ArrayList<Component>(16);
        stack.add(rootPane.getContentPane());
        int lastIndex = 0;
        
        while (lastIndex != -1) {
            
            Component c = stack.remove(lastIndex--);
            
            if (!c.isVisible()) {
                continue;
            }
            
            if (c instanceof JLabel) {
                JLabel lbl = (JLabel) c;
                Component labelFor = lbl.getLabelFor();
                if ((labelFor != null) && (labelFor != tfClassToTest)
                        && (lbl.getDisplayedMnemonic() != 0)) {
                    mnemBlocked.add(lbl);
                }
            } else if (c instanceof AbstractButton) {
                if (c != btnBrowse) {
                    AbstractButton btn = (AbstractButton) c;
                    mouseBlocked.add(btn);
                    if (btn.getMnemonic() != 0) {
                        mnemBlocked.add(btn);
                    }
                }
            } else if (!(c instanceof Container)) {
                if (c.isFocusable() && (c != tfClassToTest)) {
                    mouseBlocked.add(c);
                }
            } else {
                Component[] content = ((Container) c).getComponents();
                switch (content.length) {
                    case 0:
                        break;
                    case 1:
                        stack.add(content[0]);
                        lastIndex++;
                        break;
                    default:
                        stack.addAll(Arrays.asList(content));
                        lastIndex += content.length;
                        break;
                }
            }
        }
        //mouseBlocked.add(defaultButton);
        //mnemBlocked.add(defaultButton);
        
        this.mouseBlocked = new Component[mouseBlocked.size()];
        if (mouseBlocked.size() != 0) {
            mouseBlocked.toArray(this.mouseBlocked);
        }
        this.mnemonicBlocked = new JComponent[mnemBlocked.size()];
        if (mnemBlocked.size() != 0) {
            mnemBlocked.toArray(this.mnemonicBlocked);
        }
    }
    
    /**
     */
    private void blockDefaultRootPaneAction() {
        assert (rootPane != null) && (defaultButton != null)
               && (rootPane.getDefaultButton() == defaultButton);
        
        final String actionKey1 = "press";                              //NOI18N
        final String actionKey2 = "pressed";                            //NOI18N
        String actionKey;
        
        ActionMap actionMap = rootPane.getActionMap();
        
        Action originalAction = actionMap.get(actionKey = actionKey1);
        if (originalAction == null) {
            originalAction = actionMap.get(actionKey = actionKey2);
        }
        assert originalAction != null;
        
        if (originalAction == null) {
            return;
        }
        
        actionMap.put(actionKey, new SelectSrcGrpAction(rootPane,
                                                        originalAction));
        rootPaneDefaultActionKey = actionKey;
        rootPaneDefaultAction = originalAction;
    }
    
    /**
     */
    private void unblockDefaultRootPaneAction() {
        assert rootPane != null;
        
        if (rootPaneDefaultAction == null) {
            
            /* blockDefaultRootPaneAction() did not pass */
            return;
        }
        
        rootPane.getActionMap().put(rootPaneDefaultActionKey,
                                    rootPaneDefaultAction);
        
        rootPaneDefaultActionKey = null;
        rootPaneDefaultAction = null;
    }
    
    /**
     * Modifies behaviour of the default button.
     */
    private void blockMnemonics() {
        assert rootPane != null;
        
        if (actionMappingInfo == null) {
            findActionMappings();
        }
        
        assert actionMappingInfo != null;
        assert actionMappingInfo.length == mnemonicBlocked.length;
        
        final JComponent[] comps = mnemonicBlocked;
        for (int i = 0; i < comps.length; i++) {
            ActionMappingInfo mappingInfo = actionMappingInfo[i];
            if (mappingInfo != null) {
                comps[i].getActionMap().put(
                        mappingInfo.actionKey,
                        new SelectSrcGrpAction(comps[i],
                                               mappingInfo.originalAction));
            } else if (comps[i] instanceof JLabel) {
                ActionMap map = new JLabelActionMap(comps[i]);
                map.setParent(comps[i].getActionMap());
                comps[i].setActionMap(map);
                continue;
            }
        }
    }
    
    /**
     */
    private void unblockMnemonics() {
        assert rootPane != null;
        
        if (actionMappingInfo == null) {
            
            /* blockMnemonics() did not pass */
            return;
        }
        
        assert actionMappingInfo.length == mnemonicBlocked.length;
        
        final JComponent[] comps = mnemonicBlocked;
        for (int i = 0; i < comps.length; i++) {
            ActionMappingInfo mappingInfo = actionMappingInfo[i];
            if (mappingInfo != null) {
                comps[i].getActionMap().put(
                        mappingInfo.actionKey,
                        mappingInfo.inProximateActionMap
                                ? mappingInfo.originalAction
                                : (Action) null);
            } else if (comps[i] instanceof JLabel) {
                comps[i].setActionMap(comps[i].getActionMap().getParent());
            }
        }
    }
    
    /**
     */
    private void findActionMappings() {
        assert mnemonicBlocked != null;
        
        final String actionKey1 = "pressed";                            //NOI18N
        final String actionKey2 = "press";                              //NOI18N
        
        actionMappingInfo = new ActionMappingInfo[mnemonicBlocked.length];
        
        final JComponent[] comps = mnemonicBlocked;
        for (int i = 0; i < comps.length; i++) {
            JComponent c = comps[i];
            
            ActionMap actionMap = comps[i].getActionMap();
            
            String primaryKey = actionKey1;
            String secondaryKey = actionKey2;
            
            if (c instanceof JLabel) {
                actionMappingInfo[i] = null;
                continue;
            }
            
            String actionKey;
            Action originalAction = actionMap.get(actionKey = primaryKey);
            if (originalAction == null) {
                originalAction = actionMap.get(actionKey = secondaryKey);
            }
            if (originalAction == null) {
                ErrorManager.getDefault()
                .log(ErrorManager.EXCEPTION,
                     "JUnitWizard - Test for Existing Class: "          //NOI18N
                     + "press action not found for a "                  //NOI18N
                     + c.getClass().getName() + " component");          //NOI18N
                actionMappingInfo[i] = null;
                continue;
            }
            
            ActionMappingInfo mappingInfo = new ActionMappingInfo();
            mappingInfo.actionKey = actionKey;
            mappingInfo.originalAction = originalAction;
            /*mappingInfo.inProximateActionMap = false;*/     //it's the default
            
            /* find whether the mapping is defined in the proximate ActionMap */
            final String keyToFind = actionKey;
            final Object[] keys = actionMap.keys();
            if (keys != null) {
                for (int j = 0; j < keys.length; j++) {
                    if (keyToFind.equals(keys[j])) {
                        mappingInfo.inProximateActionMap = true;
                        break;
                    }
                }
            }

            actionMappingInfo[i] = mappingInfo;
        }
    }
    
    /**
     * Contains information about <code>ActionMap</code> mapping
     * of a UI component.
     * There is one instance of this class per each JComponent
     * in the {@link #mnemonicBlocked} array.
     */
    private static class ActionMappingInfo {
        /** action key for action which activates the component */
        String actionKey;
        /** original action mapped to the actionKey */
        Action originalAction;
        /**
         * true if the mapping was defined in the component's
         * proximate ActionMap; false otherwise
         */
        boolean inProximateActionMap;
    }
    
    /**
     * <!-- PENDING -->
     */
    final class JLabelActionMap extends ActionMap {
        
        private final Component component;
        
        JLabelActionMap(Component comp) {
            super();
            this.component = comp;
        }
        
        @Override
        public Action get(Object key) {
            if (key.equals("press")) {                                  //NOI18N
                Action defaultAction = super.get(key);
                return (defaultAction != null)
                       ? new SelectSrcGrpAction(component, defaultAction)
                       : null;
            } else {
                return super.get(key);
            }
        }
        
    }
    
    /**
     * Sets up a glass pane - one part of the focus change detection mechanism.
     */
    private void blockMouseClicks() {
        assert rootPane != null;

        final Component glassPane = rootPane.getGlassPane();
        
        if (glassPaneListener == null) {
            glassPaneListener = new GlassPaneListener();
        }
        glassPane.addMouseListener(glassPaneListener);
        glassPane.addMouseMotionListener(glassPaneListener);
        glassPane.setVisible(true);
    }
    
    /**
     * Cleans up a glass pane - one part of the focus change detection
     * mechanism.
     */
    private void unblockMouseClicks() {
        assert rootPane != null;
        
        if (glassPaneListener == null) {
            return;
        }
        
        final Component glassPane = rootPane.getGlassPane();
        
        glassPane.setVisible(false);
        glassPane.removeMouseMotionListener(glassPaneListener);
        glassPane.removeMouseListener(glassPaneListener);
    }

    /**
     *
     */
    final class GlassPaneListener implements MouseInputListener {
        final Component glassPane = rootPane.getGlassPane();
        final Component layeredPane = rootPane.getLayeredPane();
        final Container contentPane = rootPane.getContentPane();
        
        public void mouseMoved(MouseEvent e) {
            redispatchEvent(e);
        }
        public void mouseDragged(MouseEvent e) {
            redispatchEvent(e);
        }
        public void mouseClicked(MouseEvent e) {
            redispatchEvent(e);
        }
        public void mouseEntered(MouseEvent e) {
            redispatchEvent(e);
        }
        public void mouseExited(MouseEvent e) {
            redispatchEvent(e);
        }
        public void mousePressed(MouseEvent e) {
            evaluateEvent(e);
        }
        public void mouseReleased(MouseEvent e) {
            redispatchEvent(e);
        }
        private void evaluateEvent(MouseEvent e) {
            assert multipleSourceRoots;
            
            Component component = getDeepestComponent(e);
            if (component == null) {
                return;
            }

            boolean isBlocked = false;
            if (SwingUtilities.isLeftMouseButton(e)) {
                final Component[] blocked = mouseBlocked;
                for (int i = 0; i < blocked.length; i++) {
                    if (component == blocked[i]) {
                        isBlocked = true;
                        break;
                    }
                }
            }
            
            boolean askUserToChoose;
            SourceGroup[] candidates = null;
            if (!isBlocked || interactionRestrictionsSuspended) {
                askUserToChoose = false;
            } else if (component == defaultButton) {
                candidates = findParentGroupCandidates();
                askUserToChoose = (candidates.length > 1);
            } else if (!SwingUtilities.isDescendingFrom(component,
                                                        visualComp)) {
                askUserToChoose = false;
            } else {
                candidates = findParentGroupCandidates();
                askUserToChoose = (candidates.length > 1);
            }
            
            assert (askUserToChoose == false) || (candidates.length > 1);
            
            if (askUserToChoose) {
                SourceGroup srcGroup = chooseSrcGroup(candidates);
                if (srcGroup != null) {
                    setSelectedSrcGroup(srcGroup);
                    focusGainAllowedFor = component;
                    component.requestFocus();
                }
            } else {
                if (candidates != null) {
                    assert candidates.length == 1;
                    
                    setSelectedSrcGroup(candidates[0]);
                }
                focusGainAllowedFor = component;
                try {
                    redispatchEvent(e, component);
                } finally {
                    clearFocusGainAllowedVar();
                }
            }
        }
        private void redispatchEvent(MouseEvent e) {
            Component deepestComp = getDeepestComponent(e);
            if (deepestComp != null) {
                redispatchEvent(e, deepestComp);
            }
        }
        private void redispatchEvent(MouseEvent e, Component component) {
            Point componentPoint
                    = SwingUtilities.convertPoint(glassPane,
                                                  e.getPoint(),
                                                  component);
            component.dispatchEvent(
                    new MouseEvent(component,
                                   e.getID(),
                                   e.getWhen(),
                                   e.getModifiers(),
                                   componentPoint.x,
                                   componentPoint.y,
                                   e.getClickCount(),
                                   e.isPopupTrigger()));
        }
        private Component getDeepestComponent(MouseEvent e) {
            Point contentPanePoint
                    = SwingUtilities.convertPoint(glassPane,
                                                  e.getPoint(),
                                                  contentPane);
            return SwingUtilities.getDeepestComponentAt(
                            contentPane,
                            contentPanePoint.x,
                            contentPanePoint.y);
        }
    }
    
    /**
     * Action that is activated by a mnemonic keystroke.
     */
    private class SelectSrcGrpAction extends AbstractAction {
        private final Component component;
        private final Action delegate;
        public SelectSrcGrpAction(Component comp, Action delegate) {
            this.component = comp;
            this.delegate = delegate;
        }
        public void actionPerformed(ActionEvent e) {
            assert multipleSourceRoots;
            
            boolean askUserToChoose;
            SourceGroup[] candidates = null;
            if (interactionRestrictionsSuspended) {
                askUserToChoose = false;
            } else if ((component == defaultButton)
                       || (component == rootPane)) {
                candidates = findParentGroupCandidates();
                askUserToChoose = (candidates.length > 1);
            } else if (!SwingUtilities.isDescendingFrom(component,
                                                        visualComp)) {
                askUserToChoose = false;
            } else {
                candidates = findParentGroupCandidates();
                askUserToChoose = (candidates.length > 1);
            }
            
            assert (askUserToChoose == false) || (candidates.length > 1);
            
            if (askUserToChoose) {
                SourceGroup srcGroup = chooseSrcGroup(candidates);
                if (srcGroup != null) {
                    setSelectedSrcGroup(srcGroup);
                    if (component == rootPane) {
                        defaultButton.requestFocus();
                    } else {
                        component.requestFocus();
                    }
                }
            } else {
                if (candidates != null) {
                    assert candidates.length == 1;
                    
                    setSelectedSrcGroup(candidates[0]);
                }
                redispatchEvent(e);
            }
        }
        private void redispatchEvent(ActionEvent e) {
            focusGainAllowedFor = component;
            try {
                delegate.actionPerformed(e);
            } finally {
                clearFocusGainAllowedVar();
            }
        }
        @Override
        public boolean isEnabled() {
            return delegate.isEnabled();
        }
    }
    
    /**
     * <!-- PENDING -->
     */
    private class ClsNameDocumentFilter extends DocumentFilter {
        public ClsNameDocumentFilter () {}
        
        @Override
        public void replace(DocumentFilter.FilterBypass bypass,
                            int offset,
                            int length,
                            String text,
                            AttributeSet attrs) throws BadLocationException {
            if (!programmaticChange && srcGroupNameDisplayed) {
                removeSrcGroupName(bypass);
            }
            super.replace(bypass, offset, length, text, attrs);
        }
        @Override
        public void insertString(
                            DocumentFilter.FilterBypass bypass,
                            int offset,
                            String string,
                            AttributeSet attr) throws BadLocationException {
            if (!programmaticChange && srcGroupNameDisplayed) {
                removeSrcGroupName(bypass);
            }
            super.insertString(bypass, offset, string, attr);
        }
        @Override
        public void remove(DocumentFilter.FilterBypass bypass,
                           int offset,
                           int length) throws BadLocationException {
            if (!programmaticChange && srcGroupNameDisplayed) {
                removeSrcGroupName(bypass);
            }
            super.remove(bypass, offset, length);
        }
        private void removeSrcGroupName(DocumentFilter.FilterBypass bypass)
                                                throws BadLocationException {
            bypass.remove(classNameLength,
                          tfClassToTest.getText().length() - classNameLength);
            srcGroupNameDisplayed = false;
            setNavigationFilterEnabled(false);
        }
    }
    
    /**
     * <!-- PENDING -->
     */
    private class ClsNameNavigationFilter extends NavigationFilter {
        public ClsNameNavigationFilter () {}
        
        @Override
        public void setDot(NavigationFilter.FilterBypass bypass,
                           int dot,
                           Position.Bias bias) {
            if (dot > classNameLength) {
                bypass.setDot(classNameLength, bias);
            } else {
                super.setDot(bypass, dot, bias);
            }
        }
        @Override
        public void moveDot(NavigationFilter.FilterBypass bypass,
                           int dot,
                           Position.Bias bias) {
            if (dot > classNameLength) {
                bypass.moveDot(classNameLength, bias);
            } else {
                super.moveDot(bypass, dot, bias);
            }
        }
        public void ensureCursorInRange() {
            if (srcGroupNameDisplayed) {
                if (tfClassToTest.getCaretPosition() > classNameLength) {
                    tfClassToTest.setCaretPosition(classNameLength);
                }
            }
        }
    }
    
    /**
     * Sends a request to clear the {@link #focusGainAllowedFor} variable
     * to the end of the event queue.
     */
    private void clearFocusGainAllowedVar() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                focusGainAllowedFor = null;
            }
        });
    }
    
    /**
     */
    private static boolean checkObjChanged(Object oldObj, Object newObj) {
        return ((oldObj != null) || (newObj != null))
               && ((oldObj == null) || !oldObj.equals(newObj));
    }
    
    /* *
     * /
    private static void printCallstack(String header) {
        
        //PENDING - this method is not needed in the final version
        
        if (header != null) {
            System.out.println(header);
        }

        StackTraceElement[] frames = new Exception().getStackTrace();
        int count = Math.min(5, frames.length);
        for (int i = 1; i < frames.length; i++) {
            String methodName = frames[i].getMethodName();
            if (!methodName.startsWith("access$")) {
                System.out.println("   " + methodName + "(...)");
                if (--count == 0) {
                    break;
                }
            }
        }
        System.out.println();
    }
     */
    
}
