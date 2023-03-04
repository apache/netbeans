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
package org.netbeans.modules.java.testrunner.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.gsf.testrunner.plugin.CommonSettingsProvider;
import org.netbeans.modules.gsf.testrunner.plugin.GuiUtilsProvider;
import org.netbeans.modules.gsf.testrunner.ui.spi.TestCreatorConfiguration;
import org.netbeans.modules.java.testrunner.GuiUtils;
import org.netbeans.modules.java.testrunner.JavaUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Pair;

/**
 *
 * @author Theofanis Oikonomou
 */
public class JavaTestCreatorConfiguration extends TestCreatorConfiguration {
    
    private JCheckBox chkAbstractImpl;
    private JCheckBox chkComments;
    private JCheckBox chkContent;
    private JCheckBox chkExceptions;
    private JCheckBox chkGenerateSuites;
    private JCheckBox chkJavaDoc;
    private JCheckBox chkPackage;
    private JCheckBox chkPackagePrivateClasses;
    private JCheckBox chkProtected;
    private JCheckBox chkPublic;
    private JCheckBox chkSetUp;
    private JCheckBox chkTearDown;
    private JCheckBox chkBeforeClass;
    private JCheckBox chkAfterClass;
    /** resource bundle used during initialization of this panel */
    private ResourceBundle bundle;
    private final FileObject[] activatedFileObjects;

    JavaTestCreatorConfiguration(FileObject[] activatedFileObjects) {
        assert activatedFileObjects != null;
        this.activatedFileObjects = activatedFileObjects;
    }

    @Override
    public boolean canHandleProject(@NonNull String framework) {
        return framework.startsWith("JUnit") || GuiUtils.TESTNG_TEST_FRAMEWORK.equals(framework);
    }

    @Override
    public JTextComponent getMessagePanel(Context context) {
        Color color = UIManager.getColor("nb.errorForeground");         //NOI18N
        if (color == null) {
            color = new Color(89, 79, 191);   //RGB suggested by Bruce in #28466
        }
        return GuiUtils.createMultilineLabel("", color);
    }

    @Override
    public Component getConfigurationPanel(TestCreatorConfiguration.Context context) {
        initBundle();
        JPanel jpCodeGen;
        try {
            /* create the components: */
            String[] chkBoxIDs = new String[14];
            JCheckBox[] chkBoxes;
            Collection<? extends GuiUtilsProvider> providers = Lookup.getDefault().lookupAll(GuiUtilsProvider.class);
            if (context.isMultipleClasses()) {
                for (GuiUtilsProvider provider : providers) {
                    chkBoxIDs = new String[]{
                        provider.getCheckboxText("CHK_PUBLIC"),
                        provider.getCheckboxText("CHK_PROTECTED"),
                        provider.getCheckboxText("CHK_PACKAGE"),
                        provider.getCheckboxText("CHK_PACKAGE_PRIVATE_CLASSES"),
                        provider.getCheckboxText("CHK_ABSTRACT_CLASSES"),
                        provider.getCheckboxText("CHK_EXCEPTION_CLASSES"),
                        provider.getCheckboxText("CHK_SUITES"),
                        provider.getCheckboxText("CHK_SETUP"),
                        provider.getCheckboxText("CHK_TEARDOWN"),
                        provider.getCheckboxText("CHK_BEFORE_CLASS"),
                        provider.getCheckboxText("CHK_AFTER_CLASS"),
                        provider.getCheckboxText("CHK_METHOD_BODIES"),
                        provider.getCheckboxText("CHK_JAVADOC"),
                        provider.getCheckboxText("CHK_HINTS")
                    };
                    break;
                }
            } else {
                for (GuiUtilsProvider provider : providers) {
                    chkBoxIDs = new String[]{
                        provider.getCheckboxText("CHK_PUBLIC"),
                        provider.getCheckboxText("CHK_PROTECTED"),
                        provider.getCheckboxText("CHK_PACKAGE"),
                        null, // CHK_PACKAGE_PRIVATE_CLASSES,
                        null, // CHK_ABSTRACT_CLASSES,
                        null, // CHK_EXCEPTION_CLASSES,
                        null, // CHK_SUITES,
                        provider.getCheckboxText("CHK_SETUP"),
                        provider.getCheckboxText("CHK_TEARDOWN"),
                        provider.getCheckboxText("CHK_BEFORE_CLASS"),
                        provider.getCheckboxText("CHK_AFTER_CLASS"),
                        provider.getCheckboxText("CHK_METHOD_BODIES"),
                        provider.getCheckboxText("CHK_JAVADOC"),
                        provider.getCheckboxText("CHK_HINTS")
                    };
                    break;
                }
            }
            chkBoxes = new JCheckBox[chkBoxIDs.length];
            for (GuiUtilsProvider provider : providers) {
                chkBoxes = provider.createCheckBoxes(chkBoxIDs);
                break;
            }
            int i = 0;
            chkPublic = chkBoxes[i++];
            chkProtected = chkBoxes[i++];
            chkPackage = chkBoxes[i++];
            chkPackagePrivateClasses = chkBoxes[i++];       //may be null
            chkAbstractImpl = chkBoxes[i++];            //may be null
            chkExceptions = chkBoxes[i++];            //may be null
            chkGenerateSuites = chkBoxes[i++];            //may be null
            chkSetUp = chkBoxes[i++];
            chkTearDown = chkBoxes[i++];
            chkBeforeClass = chkBoxes[i++];
            chkAfterClass = chkBoxes[i++];
            chkContent = chkBoxes[i++];
            chkJavaDoc = chkBoxes[i++];
            chkComments = chkBoxes[i++];

            /* create groups of checkboxes: */
            JComponent methodAccessLevels = null;
            for (GuiUtilsProvider provider : providers) {
                methodAccessLevels = provider.createChkBoxGroup(
                        bundle.getString("CommonTestsCfgOfCreate.groupAccessLevels"), //NOI18N
                        new JCheckBox[]{chkPublic, chkProtected, chkPackage});
                break;
            }
            JComponent classTypes = null;
            JComponent optionalClasses = null;
            if (context.isMultipleClasses()) {
                for (GuiUtilsProvider provider : providers) {
                    classTypes = provider.createChkBoxGroup(
                            bundle.getString("CommonTestsCfgOfCreate.groupClassTypes"), //NOI18N
                            new JCheckBox[]{chkPackagePrivateClasses,
                                chkAbstractImpl, chkExceptions});
                    optionalClasses = provider.createChkBoxGroup(
                            bundle.getString("CommonTestsCfgOfCreate.groupOptClasses"), //NOI18N
                            new JCheckBox[]{chkGenerateSuites});
                    break;
                }
            }
            JComponent optionalCode = null;
            JComponent optionalComments = null;
            for (GuiUtilsProvider provider : providers) {
                optionalCode = provider.createChkBoxGroup(
                        bundle.getString("CommonTestsCfgOfCreate.groupOptCode"), //NOI18N
                        new JCheckBox[]{chkSetUp, chkTearDown, chkBeforeClass, chkAfterClass, chkContent});
                optionalComments = provider.createChkBoxGroup(
                        bundle.getString("CommonTestsCfgOfCreate.groupOptComments"), //NOI18N
                        new JCheckBox[]{chkJavaDoc, chkComments});
                break;
            }

            /* create the left column of options: */
            Box leftColumn = Box.createVerticalBox();
            leftColumn.add(methodAccessLevels);
            if (context.isMultipleClasses()) {
                leftColumn.add(Box.createVerticalStrut(11));
                leftColumn.add(classTypes);
            } else {
                /*
                 * This strut ensures that width of the left column is not limited.
                 * If it was limited, the rigth column would not move when the
                 * dialog is horizontally resized.
                 */
                leftColumn.add(Box.createVerticalStrut(0));
            }
            leftColumn.add(Box.createVerticalGlue());

            /* create the right column of options: */
            Box rightColumn = Box.createVerticalBox();
            if (context.isMultipleClasses()) {
                rightColumn.add(optionalClasses);
                rightColumn.add(Box.createVerticalStrut(11));
            }
            rightColumn.add(optionalCode);
            rightColumn.add(Box.createVerticalStrut(11));
            rightColumn.add(optionalComments);
            rightColumn.add(Box.createVerticalGlue());

            jpCodeGen = new JPanel();
            jpCodeGen.setLayout(new BoxLayout(jpCodeGen, BoxLayout.X_AXIS));
            jpCodeGen.add(leftColumn);
            jpCodeGen.add(Box.createHorizontalStrut(24));
            jpCodeGen.add(rightColumn);

            /* decorate the panel: */
            addTitledBorder(jpCodeGen,
                    new Insets(12, 12, 11, 12),
                    bundle.getString("CommonTestsCfgOfCreate.jpCodeGen.title"));//NOI18N

            /* tune the layout: */
            methodAccessLevels.setAlignmentX(0.0f);
            if (context.isMultipleClasses()) {
                classTypes.setAlignmentX(0.0f);
                optionalClasses.setAlignmentX(0.0f);
            }
            optionalCode.setAlignmentX(0.0f);
            optionalComments.setAlignmentX(0.0f);
            initializeCheckBoxStates(context.isMultipleClasses());
            
            setupUserInteraction(context.getCallback());
        } finally {
            unlinkBundle();
        }

        return jpCodeGen;
    }
    
    private void setupUserInteraction(Callback callback) {
        final ItemListener listener = new CheckBoxListener(callback);

        chkPublic.addItemListener(listener);
        chkProtected.addItemListener(listener);
        chkPackage.addItemListener(listener);
    }

    /**
     * Listener object that listens on state changes of some check-boxes.
     */
    private final class CheckBoxListener implements ItemListener {
        private final Callback callback;
        public CheckBoxListener (Callback callback) {
            this.callback = callback;
        }
        
        @Override
        public void itemStateChanged(ItemEvent e) {
            final Object source = e.getSource();
            
            assert source == chkPublic
                   || source == chkProtected
                   || source == chkPackage;
            callback.checkAcceptability();
        }
        
    }

    @Override
    public void persistConfigurationPanel(Context context) {
        rememberCheckBoxStates(context.isMultipleClasses());
    }
    
    /**
     * Loads a resource bundle so that it can be used during intialization
     * of this panel.
     *
     * @see  #unlinkBundle
     */
    private void initBundle() {
        Collection<? extends GuiUtilsProvider> providers = Lookup.getDefault().lookupAll(GuiUtilsProvider.class);
        for (GuiUtilsProvider provider : providers) {
            bundle = provider.getBundle();
            break;
        }
    }
    
    /**
     * Nulls the resource bundle so that it is not held in memory when it is
     * not used.
     *
     * @see  #initBundle
     */
    private void unlinkBundle() {
        bundle = null;
    }
    
    /**
     * Adds a border and a title around a given component.
     * If the component already has some border, it is overridden (not kept).
     *
     * @param  component  component the border and title should be added to
     * @param  insets  insets between the component and the titled border
     * @param  title  text of the title
     */
    private static void addTitledBorder(JComponent component,
                                        Insets insets,
                                        String title) {
        Border insideBorder = BorderFactory.createEmptyBorder(
                insets.top, insets.left, insets.bottom, insets.right);
        Border outsideBorder = new TitledBorder(
                BorderFactory.createEtchedBorder(), title);
        component.setBorder(new CompoundBorder(outsideBorder, insideBorder));
    }

    /**
     * Reads JUnit settings and initializes checkboxes accordingly.
     *
     * @see  #persistConfigurationPanel
     */
    private void initializeCheckBoxStates(boolean multipleClasses) {
        boolean chkPublicB = true;
        boolean chkProtectedB = true;
        boolean chkPackageB = true;
        boolean chkCommentsB = true;
        boolean chkContentB = true;
        boolean chkJavaDocB = true;
        boolean chkGenerateSuitesB = true;
        boolean chkPackagePrivateClassesB = true;
        boolean chkAbstractImplB = true;
        boolean chkExceptionsB = true;
        boolean chkSetUpB = true;
        boolean chkTearDownB = true;
        boolean chkBeforeClassB = true;
        boolean chkAfterClassB = true;
        Collection<? extends CommonSettingsProvider> providers = Lookup.getDefault().lookupAll(CommonSettingsProvider.class);
        for (CommonSettingsProvider provider : providers) {
            chkPublicB = provider.isMembersPublic();
            chkProtectedB = provider.isMembersProtected();
            chkPackageB = provider.isMembersPackage();
            chkCommentsB = provider.isBodyComments();
            chkContentB = provider.isBodyContent();
            chkJavaDocB = provider.isJavaDoc();
            chkGenerateSuitesB = provider.isGenerateSuiteClasses();
            chkPackagePrivateClassesB = provider.isIncludePackagePrivateClasses();
            chkAbstractImplB = provider.isGenerateAbstractImpl();
            chkExceptionsB = provider.isGenerateExceptionClasses();
            chkSetUpB = provider.isGenerateSetUp();
            chkTearDownB = provider.isGenerateTearDown();
            chkBeforeClassB = provider.isGenerateClassSetUp();
            chkAfterClassB = provider.isGenerateClassTearDown();
            break;
        }
        
        chkPublic.setSelected(chkPublicB);
        chkProtected.setSelected(chkProtectedB);
        chkPackage.setSelected(chkPackageB);
        chkComments.setSelected(chkCommentsB);
        chkContent.setSelected(chkContentB);
        chkJavaDoc.setSelected(chkJavaDocB);
        if (multipleClasses) {
            chkGenerateSuites.setSelected(chkGenerateSuitesB);
            chkPackagePrivateClasses.setSelected(chkPackagePrivateClassesB);
            chkAbstractImpl.setSelected(chkAbstractImplB);
            chkExceptions.setSelected(chkExceptionsB);
        }
        chkSetUp.setSelected(chkSetUpB);
        chkTearDown.setSelected(chkTearDownB);
        chkBeforeClass.setSelected(chkBeforeClassB);
        chkAfterClass.setSelected(chkAfterClassB);
    }
    
    /**
     * Stores settings given by checkbox states to JUnit settings.
     *
     * @see  #initializeCheckBoxStatesf
     */
    private void rememberCheckBoxStates(boolean multipleClasses) {
        Collection<? extends CommonSettingsProvider> providers = Lookup.getDefault().lookupAll(CommonSettingsProvider.class);
        for (CommonSettingsProvider provider : providers) {
            provider.setMembersPublic(chkPublic.isSelected());
            provider.setMembersProtected(chkProtected.isSelected());
            provider.setMembersPackage(chkPackage.isSelected());
            provider.setBodyComments(chkComments.isSelected());
            provider.setBodyContent(chkContent.isSelected());
            provider.setJavaDoc(chkJavaDoc.isSelected());
            if (multipleClasses) {
                provider.setGenerateSuiteClasses(chkGenerateSuites.isSelected());
                provider.setIncludePackagePrivateClasses(
                        chkPackagePrivateClasses.isSelected());
                provider.setGenerateAbstractImpl(chkAbstractImpl.isSelected());
                provider.setGenerateExceptionClasses(chkExceptions.isSelected());
            }
            provider.setGenerateSetUp(chkSetUp.isSelected());
            provider.setGenerateTearDown(chkTearDown.isSelected());
            provider.setGenerateClassSetUp(chkBeforeClass.isSelected());
            provider.setGenerateClassTearDown(chkAfterClass.isSelected());
            break;
        }
    }

    @Override
    public Pair<String, String> getSourceAndTestClassNames(FileObject fileObj, boolean isTestNG, boolean isSelenium) {
        String[] sourceAndTestClassNames = JavaUtils.getSourceAndTestClassNames(fileObj, isTestNG, isSelenium);
        return Pair.of(sourceAndTestClassNames[0], sourceAndTestClassNames[1]);
    }

    @Override
    public Object[] getTestSourceRoots(Collection<SourceGroup> createdSourceRoots, FileObject fo) {
        return JavaUtils.getTestSourceRoots(createdSourceRoots, fo);
    }
    
}
