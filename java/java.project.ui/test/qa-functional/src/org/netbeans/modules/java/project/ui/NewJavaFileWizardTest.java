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
package org.netbeans.modules.java.project.ui;

import java.io.File;
import java.io.IOException;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 * @author: Arthur Sadykov
 */
public class NewJavaFileWizardTest extends JellyTestCase {

    private static final String TEST_PROJECT_NAME = "TestProject";
    private static final String JAVA_CATEGORY = "Java";
    private static final String JAVA_CLASS_FILE_TYPE = "Java Class";
    private static final String JAVA_INTERFACE_FILE_TYPE = "Java Interface";
    private static final String JAVA_ENUM_FILE_TYPE = "Java Enum";
    private static final String JAVA_ANNOTATION_TYPE_FILE_TYPE = "Java Annotation Type";
    private static final String JAVA_EXCEPTION_FILE_TYPE = "Java Exception";
    private static final String JAVA_MAIN_CLASS_FILE_TYPE = "Java Main Class";
    private static final String JAVA_SINGLETON_CLASS_FILE_TYPE = "Java Singleton Class";
    private static final String EMPTY_JAVA_FILE_TYPE = "Empty Java File";
    private static final String JAVA_RECORD_FILE_TYPE = "Java Record";
    private static final String SERIALIZABLE_FQN = "java.io.Serializable";
    private static final String OBSERVER_FQN = "java.util.Observer";
    private static final String INTERFACES_LABEL_TEXT = "Interfaces:";
    private static final String SUPERCLASS_LABEL_TEXT = "Superclass:";
    private static final String SUPERINTERFACE_LABEL_TEXT = "Superinterface:";
    private static final String TEST_PACKAGE_NAME = "test";
    private static final String MAIN_CLASS_NAME = "Main";
    private static final String PACKAGE_LABEL_TEXT = "Package:";
    private static final String CLASS_NAME_LABEL_TEXT = "Class Name:";
    private static final String STACK_FQN = "java.util.Stack";
    private static final String THREAD_FQN = "java.lang.Thread";
    private static final String CLONEABLE_FQN = "java.lang.Cloneable";
    private static final String IMPORT_STACK = "import java.util.Stack;";
    private static final String IMPORT_SERIALIZABLE = "import java.io.Serializable;";
    private static final String IMPORT_OBSERVER = "import java.util.Observer;";
    private static final String IMPORT_THREAD = "import java.lang.Thread;";
    private static final String IMPORT_CLONEABLE = "import java.lang.Cloneable;";
    private static final String TEST_CLASS_FQN = "test.Test";
    private static final String IMPORT_TEST_CLASS = "import test.Test;";
    private static final String NEW_LINE = "\n";
    private JTextFieldOperator superclassTextFieldOperator;
    private JTextAreaOperator interfacesTextAreaOperator;
    private NewFileWizardOperator wizardOperator;

    public NewJavaFileWizardTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return createModuleTest(NewJavaFileWizardTest.class);
    }

    @Override
    public void setUp() throws IOException {
        clearWorkDir();
        openDataProjects(TEST_PROJECT_NAME);
        ProjectsTabOperator.invoke();
        wizardOperator = NewFileWizardOperator.invoke();
        wizardOperator.selectProject(TEST_PROJECT_NAME);
        wizardOperator.selectCategory(JAVA_CATEGORY);
    }

    public void testNewJavaClassWizardShouldContainExtensionAndImplementationBoxes() {
        wizardOperator.selectFileType(JAVA_CLASS_FILE_TYPE);
        wizardOperator.next();
        assertExtensionBoxIsPresent();
        assertImplementationBoxIsPresent();
        wizardOperator.cancel();
    }

    public void testNewJavaInterfaceWizardShouldContainOnlyExtensionBoxForInterface() {
        wizardOperator.selectFileType(JAVA_INTERFACE_FILE_TYPE);
        wizardOperator.next();
        assertExtensionBoxForInterfaceIsPresent();
        assertImplementationBoxIsNotPresent();
        wizardOperator.cancel();
    }

    public void testNewJavaEnumWizardShouldContainOnlyImplementationBox() {
        wizardOperator.selectFileType(JAVA_ENUM_FILE_TYPE);
        wizardOperator.next();
        assertExtensionBoxIsNotPresent();
        assertImplementationBoxIsPresent();
        wizardOperator.cancel();
    }

    public void testNewJavaAnnotationTypeWizardShouldContainNeitherExtensionNorImplementationBox() {
        wizardOperator.selectFileType(JAVA_ANNOTATION_TYPE_FILE_TYPE);
        wizardOperator.next();
        assertExtensionBoxIsNotPresent();
        assertImplementationBoxIsNotPresent();
        wizardOperator.cancel();
    }

    public void testNewJavaExceptionWizardShouldContainExtensionAndImplementationBoxes() {
        wizardOperator.selectFileType(JAVA_EXCEPTION_FILE_TYPE);
        wizardOperator.next();
        assertExtensionBoxIsPresent();
        assertImplementationBoxIsPresent();
        wizardOperator.cancel();
    }

    public void testNewJavaMainClassWizardShouldContainExtensionAndImplementationBoxes() {
        wizardOperator.selectFileType(JAVA_MAIN_CLASS_FILE_TYPE);
        wizardOperator.next();
        assertExtensionBoxIsPresent();
        assertImplementationBoxIsPresent();
        wizardOperator.cancel();
    }

    public void testNewJavaSingletonClassWizardShouldContainExtensionAndImplementationBoxes() {
        wizardOperator.selectFileType(JAVA_SINGLETON_CLASS_FILE_TYPE);
        wizardOperator.next();
        assertExtensionBoxIsPresent();
        assertImplementationBoxIsPresent();
        wizardOperator.cancel();
    }

    public void testNewEmptyJavaFileWizardShouldContainNeitherExtensionNorImplementationBox() {
        wizardOperator.selectFileType(EMPTY_JAVA_FILE_TYPE);
        wizardOperator.next();
        assertExtensionBoxIsNotPresent();
        assertImplementationBoxIsNotPresent();
        wizardOperator.cancel();
    }

    public void testNewJavaRecordWizardShouldContainOnlyImplementationBox() {
        wizardOperator.selectFileType(JAVA_RECORD_FILE_TYPE);
        wizardOperator.next();
        assertExtensionBoxIsNotPresent();
        assertImplementationBoxIsPresent();
        wizardOperator.cancel();
    }

    public void testShouldAddRequiredImports() {
        instantiateOperatorsForNewJavaClassWizard();
        superclassTextFieldOperator.setText(STACK_FQN);
        interfacesTextAreaOperator.append(SERIALIZABLE_FQN);
        interfacesTextAreaOperator.append(NEW_LINE);
        interfacesTextAreaOperator.append(OBSERVER_FQN);
        wizardOperator.finish();
        EditorOperator editorOperator = new EditorOperator(MAIN_CLASS_NAME);
        String text = editorOperator.getText();
        assertGeneratedClassContainsImport(IMPORT_STACK, text);
        assertGeneratedClassContainsImport(IMPORT_SERIALIZABLE, text);
        assertGeneratedClassContainsImport(IMPORT_OBSERVER, text);
    }

    public void testShouldAddRequiredExtendsAndImplementsClauses() {
        instantiateOperatorsForNewJavaClassWizard();
        superclassTextFieldOperator.setText(STACK_FQN);
        interfacesTextAreaOperator.append(SERIALIZABLE_FQN);
        interfacesTextAreaOperator.append(NEW_LINE);
        interfacesTextAreaOperator.append(OBSERVER_FQN);
        wizardOperator.finish();
        EditorOperator editorOperator = new EditorOperator(MAIN_CLASS_NAME);
        String text = editorOperator.getText();
        assertGeneratedClassContainsImport(IMPORT_STACK, text);
        assertGeneratedClassContainsImport(IMPORT_SERIALIZABLE, text);
        assertGeneratedClassContainsImport(IMPORT_OBSERVER, text);
        assertGeneratedClassHasHeader("public class Main extends Stack implements Serializable, Observer", text);
    }

    public void testShouldNotAddImportsForClassesFromJavaLangPackage() {
        instantiateOperatorsForNewJavaClassWizard();
        superclassTextFieldOperator.setText(THREAD_FQN);
        interfacesTextAreaOperator.setText(CLONEABLE_FQN);
        wizardOperator.finish();
        EditorOperator editorOperator = new EditorOperator(MAIN_CLASS_NAME);
        String text = editorOperator.getText();
        assertGeneratedClassNotContainsImport(IMPORT_THREAD, text);
        assertGeneratedClassNotContainsImport(IMPORT_CLONEABLE, text);
        assertGeneratedClassHasHeader("public class Main extends Thread implements Cloneable", text);
    }

    public void testShouldNotAddImportsForClassesFromSamePackage() {
        instantiateOperatorsForNewJavaClassWizard();
        superclassTextFieldOperator.setText(TEST_CLASS_FQN);
        wizardOperator.finish();
        EditorOperator editorOperator = new EditorOperator(MAIN_CLASS_NAME);
        String text = editorOperator.getText();
        assertGeneratedClassNotContainsImport(IMPORT_TEST_CLASS, text);
        assertGeneratedClassHasHeader("public class Main extends Test", text);
    }

    private void instantiateOperatorsForNewJavaClassWizard() {
        wizardOperator.selectFileType(JAVA_CLASS_FILE_TYPE);
        wizardOperator.next();
        JLabelOperator classNameLabelOperator = new JLabelOperator(wizardOperator, CLASS_NAME_LABEL_TEXT);
        JTextFieldOperator classNameTextFieldOperator =
                new JTextFieldOperator((JTextField) classNameLabelOperator.getLabelFor());
        classNameTextFieldOperator.setText(MAIN_CLASS_NAME);
        JLabelOperator packageLabelOperator = new JLabelOperator(wizardOperator, PACKAGE_LABEL_TEXT);
        JComboBoxOperator packageComboBoxOperator =
                new JComboBoxOperator((JComboBox) packageLabelOperator.getLabelFor());
        packageComboBoxOperator.typeText(TEST_PACKAGE_NAME);
        JLabelOperator superclassLabelOperator = new JLabelOperator(wizardOperator, SUPERCLASS_LABEL_TEXT);
        superclassTextFieldOperator = new JTextFieldOperator((JTextField) superclassLabelOperator.getLabelFor());
        JLabelOperator interfacesLabelOperator = new JLabelOperator(wizardOperator, INTERFACES_LABEL_TEXT);
        interfacesTextAreaOperator = new JTextAreaOperator((JTextArea) interfacesLabelOperator.getLabelFor());
    }

    private void assertExtensionBoxIsPresent() {
        JLabel superclassLabel =
                JLabelOperator.findJLabel(wizardOperator.getContentPane(), SUPERCLASS_LABEL_TEXT, true, true);
        assertNotNull("The wizard should contain the 'Superclass' label", superclassLabel);
    }

    private void assertExtensionBoxForInterfaceIsPresent() {
        JLabel superclassLabel =
                JLabelOperator.findJLabel(wizardOperator.getContentPane(), SUPERINTERFACE_LABEL_TEXT, true, true);
        assertNotNull("The wizard should contain the 'Superinterface' label", superclassLabel);
    }

    private void assertExtensionBoxIsNotPresent() {
        JLabel superclassLabel =
                JLabelOperator.findJLabel(wizardOperator.getContentPane(), SUPERCLASS_LABEL_TEXT, true, true);
        assertNull("The wizard should not contain the 'Superclass' label", superclassLabel);
    }

    private void assertImplementationBoxIsPresent() {
        JLabel interfacesLabel =
                JLabelOperator.findJLabel(wizardOperator.getContentPane(), INTERFACES_LABEL_TEXT, true, true);
        assertNotNull("The wizard should contain the 'Interfaces' label", interfacesLabel);
    }

    private void assertImplementationBoxIsNotPresent() {
        JLabel interfacesLabel =
                JLabelOperator.findJLabel(wizardOperator.getContentPane(), INTERFACES_LABEL_TEXT, true, true);
        assertNull("The wizard should not contain the 'Interfaces' label", interfacesLabel);
    }

    private void assertGeneratedClassContainsImport(String expected, String actual) {
        assertTrue("Generated class should contain import for " + expected.substring(7), actual.contains(expected));
    }

    private void assertGeneratedClassNotContainsImport(String expected, String actual) {
        assertFalse("Generated class should not contain import for " + expected.substring(7), actual.contains(expected));
    }

    private void assertGeneratedClassHasHeader(String expected, String actual) {
        assertTrue("Generated class should have expected header", actual.contains(expected));
    }

    @Override
    protected void tearDown() throws Exception {
        closeOpenedProjects();
        String separator = File.separator;
        String testFileName = getDataDir().getAbsolutePath() + separator + TEST_PROJECT_NAME + separator + "src"
                + separator + TEST_PACKAGE_NAME + separator + MAIN_CLASS_NAME + ".java";
        File testFile = new File(testFileName);
        testFile.delete();
    }
}
