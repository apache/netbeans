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

package org.netbeans.modules.groovy.support.wizard.impl;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.api.templates.TemplateRegistrations;
import org.netbeans.modules.groovy.support.wizard.AbstractGroovyWizard;
import org.netbeans.modules.groovy.support.wizard.JUnit;
import org.netbeans.modules.gsf.testrunner.api.SelfResizingPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * General wizard iterator implementation for Groovy JUnit Test.
 *
 * This class has been created because we need to hook to some of the WizardIterator
 * methods (e.g. we have to create test source root if it doesn't exist when creating
 * new Groovy JUnit test, we have to add JUnit and Groovy library dependencies etc.).
 *
 * The implementation depends on the actual project type, because the actions are
 * different for Ant and Maven projects (e.g. in Maven we need to add dependency
 * to pom.xml file, but in Ant we just add library to the classpath, etc.).
 *
 * @see ProjectTypeStrategy
 * @see AntProjectTypeStrategy
 * @see MavenProjectTypeStrategy
 *
 * @author Martin Janicek
 */
@TemplateRegistrations(value = {
    @TemplateRegistration(
        folder = "Groovy",
        position = 121,
        content = "/org/netbeans/modules/groovy/support/resources/GroovyJUnitTest.groovy",
        scriptEngine = "freemarker",
        displayName = "Groovy JUnit Test",
        iconBase = "org/netbeans/modules/groovy/support/resources/GroovyFile16x16.png",
        description = "/org/netbeans/modules/groovy/support/resources/GroovyJUnitTest.html",
        category = {
            "groovy",
            "java-main-class"
        }
    ),

    @TemplateRegistration(
        folder = "Groovy",
        position = 131,
        content = "/org/netbeans/modules/groovy/support/resources/GroovyJUnit3Test.groovy",
        scriptEngine = "freemarker",
        displayName = "Groovy JUnit 3.x Test",
        iconBase = "org/netbeans/modules/groovy/support/resources/GroovyFile16x16.png",
        description = "/org/netbeans/modules/groovy/support/resources/GroovyJUnitTest.html",
        category = "invisible"
    ),

    @TemplateRegistration(
        folder = "Groovy",
        position = 141,
        content = "/org/netbeans/modules/groovy/support/resources/GroovyJUnit4Test.groovy",
        scriptEngine = "freemarker",
        displayName = "Groovy JUnit 4.x Test",
        iconBase = "org/netbeans/modules/groovy/support/resources/GroovyFile16x16.png",
        description = "/org/netbeans/modules/groovy/support/resources/GroovyJUnitTest.html",
        category = "invisible"
    )
})
public final class GroovyJUnitTestWizard extends AbstractGroovyWizard {

    private static final ResourceBundle BUNDLE = NbBundle.getBundle(GroovyJUnitTestWizard.class);


    private GroovyJUnitTestWizard() {
    }

    @Override
    protected List<SourceGroup> getSourceGroups() {
        List<SourceGroup> sourceGroups = retrieveGroups();

        if (!strategy.existsGroovyTestFolder(sourceGroups)) {
            strategy.createGroovyTestFolder();

            // Retrieve the source groups again, but now with a newly created /test/groovy folder
            sourceGroups = retrieveGroups();
        }

        final List<SourceGroup> testSourceGroups = strategy.getOnlyTestSourceGroups(sourceGroups);
        if (!testSourceGroups.isEmpty()) {
            return testSourceGroups;
        } else {
            return sourceGroups;
        }
    }

    @Override
    protected FileObject findCorrectTemplate() {
        JUnit currentJUnit = strategy.findJUnitVersion();
        if (currentJUnit == JUnit.NOT_DECLARED) {
            JUnit jUnitToUse = askUserWhichJUnitToUse();
            if (jUnitToUse == null) {
                return super.findCorrectTemplate();
            }

            strategy.setjUnitVersion(jUnitToUse);
            strategy.addJUnitLibrary(jUnitToUse);
        } else {
            strategy.setjUnitVersion(currentJUnit);
        }

        return strategy.findTemplate(wiz);
    }

    private JUnit askUserWhichJUnitToUse() {
        JRadioButton radioButtonForJUnit3 = new JRadioButton();
        JRadioButton radioButtonForJUnit4 = new JRadioButton();

        Mnemonics.setLocalizedText(radioButtonForJUnit3, BUNDLE.getString("LBL_JUnit3_generator"));                       //NOI18N
        Mnemonics.setLocalizedText(radioButtonForJUnit4, BUNDLE.getString("LBL_JUnit4_generator"));                       //NOI18N

        radioButtonForJUnit3.getAccessibleContext().setAccessibleDescription(BUNDLE.getString("AD_JUnit3_generator"));    //NOI18N
        radioButtonForJUnit4.getAccessibleContext().setAccessibleDescription(BUNDLE.getString("AD_JUnit4_generator"));    //NOI18N

        ButtonGroup group = new ButtonGroup();
        group.add(radioButtonForJUnit3);
        group.add(radioButtonForJUnit4);
        radioButtonForJUnit4.setSelected(true);

        JComponent msg = createMultilineLabel(BUNDLE.getString("MSG_select_junit_version")); //NOI18N
        JPanel choicePanel = new JPanel(new GridLayout(0, 1, 0, 3));
        choicePanel.add(radioButtonForJUnit3);
        choicePanel.add(radioButtonForJUnit4);

        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.add(msg, BorderLayout.NORTH);
        panel.add(choicePanel, BorderLayout.CENTER);

        JButton button = new JButton();
        Mnemonics.setLocalizedText(button, BUNDLE.getString("LBL_Select"));     //NOI18N
        button.getAccessibleContext().setAccessibleDescription("AD_Select");    //NOI18N
        button.getAccessibleContext().setAccessibleName("AN_Select");           //NOI18N

        Object answer = DialogDisplayer.getDefault().notify(
                new DialogDescriptor(
                        wrapDialogContent(panel),
                        BUNDLE.getString("LBL_title_select_generator"),         //NOI18N
                        true,
                        new Object[] {button, NotifyDescriptor.CANCEL_OPTION},
                        button,
                        DialogDescriptor.DEFAULT_ALIGN,
                        null,
                        (ActionListener) null));

        if (answer == button) {
            if (radioButtonForJUnit3.isSelected()) {
                return JUnit.JUNIT3;
            } else {
                return JUnit.JUNIT4;
            }
        } else {
            return null;
        }
    }

    private JTextComponent createMultilineLabel(String text) {
        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEnabled(false);
        textArea.setOpaque(false);
        textArea.setColumns(25);
        textArea.setDisabledTextColor(new JLabel().getForeground());

        return textArea;
    }

    private static JComponent wrapDialogContent(JComponent comp) {
        JComponent result;

        result = new SelfResizingPanel();
        result.setLayout(new GridLayout());
        result.add(comp);
        result.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        result.getAccessibleContext().setAccessibleDescription(BUNDLE.getString("AD_title_select_generator")); //NOI18N
        return result;
    }
}
