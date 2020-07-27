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
 *
 * @author: Arthur Sadykov
 */
public class NewJavaFileWizardTest extends JellyTestCase {

    private static final String SAMPLE_PROJECT_NAME = "SampleProject";
    private static final String JAVA_CATEGORY = "Java";
    private static final String JAVA_CLASS_FILE_TYPE = "Java Class";
    private static final String SERIALIZABLE_FQN = "java.io.Serializable";
    private static final String OBSERVER_FQN = "java.util.Observer";
    private static final String INTERFACES_LABEL_TEXT = "Interfaces:";
    private static final String SUPERCLASS_LABEL_TEXT = "Superclass:";
    private static final String SAMPLE_PACKAGE_NAME = "sample";
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
    private static final String BASE_CLASS_FQN = "sample.BaseClass";
    private static final String IMPORT_BASE_CLASS = "import sample.BaseClass;";
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
        openDataProjects(SAMPLE_PROJECT_NAME);
        ProjectsTabOperator.invoke();
        wizardOperator = NewFileWizardOperator.invoke();
        wizardOperator.selectProject(SAMPLE_PROJECT_NAME);
        wizardOperator.selectCategory(JAVA_CATEGORY);
        wizardOperator.selectFileType(JAVA_CLASS_FILE_TYPE);
        wizardOperator.next();
        JLabelOperator classNameLabelOperator = new JLabelOperator(wizardOperator, CLASS_NAME_LABEL_TEXT);
        JTextFieldOperator classNameTextFieldOperator =
                new JTextFieldOperator((JTextField) classNameLabelOperator.getLabelFor());
        classNameTextFieldOperator.setText(MAIN_CLASS_NAME);
        JLabelOperator packageLabelOperator = new JLabelOperator(wizardOperator, PACKAGE_LABEL_TEXT);
        JComboBoxOperator packageComboBoxOperator =
                new JComboBoxOperator((JComboBox) packageLabelOperator.getLabelFor());
        packageComboBoxOperator.typeText(SAMPLE_PACKAGE_NAME);
        JLabelOperator superclassLabelOperator = new JLabelOperator(wizardOperator, SUPERCLASS_LABEL_TEXT);
        superclassTextFieldOperator = new JTextFieldOperator((JTextField) superclassLabelOperator.getLabelFor());
        JLabelOperator interfacesLabelOperator =
                new JLabelOperator(wizardOperator, INTERFACES_LABEL_TEXT);
        interfacesTextAreaOperator =
                new JTextAreaOperator((JTextArea) interfacesLabelOperator.getLabelFor());
    }

    public void testShouldAddImportsExtendsAndImplementsClauses() {
        superclassTextFieldOperator.setText(STACK_FQN);
        interfacesTextAreaOperator.append(SERIALIZABLE_FQN);
        interfacesTextAreaOperator.append("\n");
        interfacesTextAreaOperator.append(OBSERVER_FQN);
        wizardOperator.finish();
        EditorOperator editorOperator = new EditorOperator(MAIN_CLASS_NAME);
        String text = editorOperator.getText();
        assertTrue("Generated class should contain import for java.util.Stack", text.contains(IMPORT_STACK));
        assertTrue("Generated class should contain import for java.io.Serializable", text.contains(IMPORT_SERIALIZABLE));
        assertTrue("Generated class should contain import for java.util.Observer", text.contains(IMPORT_OBSERVER));
        String expectedClassSignature = "public class Main extends Stack implements Serializable, Observer";
        assertTrue("Generated class should have expected signature", text.contains(expectedClassSignature));
    }

    public void testShouldNotAddImportsForClassesFromJavaLangPackage() {
        superclassTextFieldOperator.setText(THREAD_FQN);
        interfacesTextAreaOperator.setText(CLONEABLE_FQN);
        wizardOperator.finish();
        EditorOperator editorOperator = new EditorOperator(MAIN_CLASS_NAME);
        String text = editorOperator.getText();
        assertTrue("Generated class should not contain import for java.lang.Thread", !text.contains(IMPORT_THREAD));
        assertTrue("Generated class should not contain import for java.lang.Cloneable", !text.contains(IMPORT_CLONEABLE));
        String expectedClassSignature = "public class Main extends Thread implements Cloneable";
        assertTrue("Generated class should have expected signature", text.contains(expectedClassSignature));
    }
    
    public void testShouldNotAddImportsForClassesFromSamePackage() {
        superclassTextFieldOperator.setText(BASE_CLASS_FQN);
        wizardOperator.finish();
        EditorOperator editorOperator = new EditorOperator(MAIN_CLASS_NAME);
        String text = editorOperator.getText();
        assertTrue("Generated class should not contain import for sample.BaseClass", !text.contains(IMPORT_BASE_CLASS));
        String expectedClassSignature = "public class Main extends Base";
        assertTrue("Generated class should have expected signature", text.contains(expectedClassSignature));
    }

    @Override
    protected void tearDown() throws Exception {
        closeOpenedProjects();
        String separator = File.separator;
        String testFileName = getDataDir().getAbsolutePath() + separator + SAMPLE_PROJECT_NAME + separator + "src"
                + separator + SAMPLE_PACKAGE_NAME + separator + MAIN_CLASS_NAME + ".java";
        File testFile = new File(testFileName);
        testFile.delete();
    }
}
