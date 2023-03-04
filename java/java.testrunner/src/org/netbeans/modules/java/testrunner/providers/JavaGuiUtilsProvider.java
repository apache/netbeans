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
package org.netbeans.modules.java.testrunner.providers;

import java.awt.Color;
import java.util.HashMap;
import java.util.ResourceBundle;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.gsf.testrunner.plugin.GuiUtilsProvider;
import org.netbeans.modules.java.testrunner.GuiUtils;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Theofanis Oikonomou
 */
@ServiceProvider(service=GuiUtilsProvider.class, position=10)
public class JavaGuiUtilsProvider extends GuiUtilsProvider {
    
    private HashMap<String, String> checkboxes = new HashMap<String, String>();
    {
        checkboxes.put("CHK_PUBLIC", GuiUtils.CHK_PUBLIC);
        checkboxes.put("CHK_PROTECTED", GuiUtils.CHK_PROTECTED);
        checkboxes.put("CHK_PACKAGE", GuiUtils.CHK_PACKAGE);
        checkboxes.put("CHK_PACKAGE_PRIVATE_CLASSES", GuiUtils.CHK_PACKAGE_PRIVATE_CLASSES);
        checkboxes.put("CHK_ABSTRACT_CLASSES", GuiUtils.CHK_ABSTRACT_CLASSES);
        checkboxes.put("CHK_EXCEPTION_CLASSES", GuiUtils.CHK_EXCEPTION_CLASSES);
        checkboxes.put("CHK_SUITES", GuiUtils.CHK_SUITES);
        checkboxes.put("CHK_SETUP", GuiUtils.CHK_SETUP);
        checkboxes.put("CHK_TEARDOWN", GuiUtils.CHK_TEARDOWN);
        checkboxes.put("CHK_BEFORE_CLASS", GuiUtils.CHK_BEFORE_CLASS);
        checkboxes.put("CHK_AFTER_CLASS", GuiUtils.CHK_AFTER_CLASS);
        checkboxes.put("CHK_METHOD_BODIES", GuiUtils.CHK_METHOD_BODIES);
        checkboxes.put("CHK_JAVADOC", GuiUtils.CHK_JAVADOC);
        checkboxes.put("CHK_HINTS", GuiUtils.CHK_HINTS);
        checkboxes.put("CHK_INTEGRATION_TESTS", GuiUtils.CHK_INTEGRATION_TESTS);
    }

    @Override
    public String getMessageFor(String key) {
        return NbBundle.getMessage(GuiUtils.class, key);
    }

    @Override
    public ResourceBundle getBundle() {
        return NbBundle.getBundle(GuiUtils.class);
    }

    @Override
    public JTextComponent createMultilineLabel(String text, Color color) {
        return GuiUtils.createMultilineLabel(text, color);
    }

    @Override
    public String getCheckboxText(String key) {        
        return checkboxes.get(key);
    }

    @Override
    public JCheckBox[] createCheckBoxes(String[] ids) {
        return GuiUtils.createCheckBoxes(ids);
    }

    @Override
    public JComponent createChkBoxGroup(String title, JCheckBox[] elements) {
        return GuiUtils.createChkBoxGroup(title, elements);
    }

    @Override
    public String getTestngFramework() {
        return GuiUtils.TESTNG_TEST_FRAMEWORK;
    }

    @Override
    public String getJunitFramework() {
        return GuiUtils.JUNIT_TEST_FRAMEWORK;
    }
    
}
