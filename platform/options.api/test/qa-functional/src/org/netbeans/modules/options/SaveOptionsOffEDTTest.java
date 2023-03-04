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
package org.netbeans.modules.options;

import java.util.Arrays;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author theofanis
 */
public class SaveOptionsOffEDTTest extends JellyTestCase {

    private OptionsOperator optionsOperator;
    private JTabbedPaneOperator jTabbedPaneOperator;
    
    /**
     * Constructor required by JUnit
     */
    public SaveOptionsOffEDTTest(String testName) {
        super(testName);
    }

    @Override
    protected int timeOut() {
        return 700000;
    }

    /**
     * Creates suite from particular test cases.
     */
    public static Test suite() {
        return NbModuleSuite.createConfiguration(SaveOptionsOffEDTTest.class).
                addTest("testSaveOptionsOffEDT").
                addTest("testCorrectTabSelected").clusters(".*").enableModules(".*").gui(true).suite();
    }
    
    public void testSaveOptionsOffEDT() {
        for(String category: Arrays.asList("General", "FontsAndColors", "Editor", "Keymaps", "Java", "PHP", "C/C++", "Miscellaneous")) {
            selectTabAndSave(category);
        }
    }
    
    public void testCorrectTabSelected() {
        for(String category: Arrays.asList("General", "FontsAndColors", "Editor", "Keymaps", "Java", "PHP", "C/C++", "Miscellaneous")) {
            selectTabAndClose(category);
        }
    }
    
    private void selectTabAndClose(String category) {
        open(category, true);
        if (category.equals("General") || category.equals("Keymaps")) {
            optionsOperator.btOK().doClick();
        } else {
            jTabbedPaneOperator = new JTabbedPaneOperator(optionsOperator);
            for (int i = 0; i < jTabbedPaneOperator.getTabCount(); i++) {
                if (i > 0) {
                    open(category, false);
                    jTabbedPaneOperator = new JTabbedPaneOperator(optionsOperator);
                    assertEquals("Wrong selected tab index", i - 1, jTabbedPaneOperator.getSelectedIndex());
                }
                jTabbedPaneOperator.selectPage(i);
                new EventTool().waitNoEvent(500);
                if(i % 2 == 0) {
                    optionsOperator.btOK().doClick();
                } else {
                    optionsOperator.btCancel().doClick();
                }
            }
        }
    }
    
    private void selectTabAndSave(String category) {
        open(category, true);
        if (category.equals("General") || category.equals("Keymaps")) {
            optionsOperator.btOK().doClick();
        } else {
            jTabbedPaneOperator = new JTabbedPaneOperator(optionsOperator);
            for (int i = 0; i < jTabbedPaneOperator.getTabCount(); i++) {
                if (i > 0) {
                    open(category, false);
                    jTabbedPaneOperator = new JTabbedPaneOperator(optionsOperator);
                }
                jTabbedPaneOperator.selectPage(i);
                new EventTool().waitNoEvent(500);
                optionsOperator.btOK().doClick();
            }
        }
    }
    
    private void open(String category, boolean selectCategory) {
        optionsOperator = OptionsOperator.invoke();
        optionsOperator.setLocation(100, 100);
        new EventTool().waitNoEvent(500);
        
        if (selectCategory) {
            if (category.equals("General")) {
                optionsOperator.selectGeneral();
            } else if (category.equals("Editor")) {
                optionsOperator.selectEditor();
            } else if (category.equals("FontsAndColors")) {
                optionsOperator.selectFontAndColors();
            } else if (category.equals("Keymaps")) {
                optionsOperator.selectKeymap();
            } else if (category.equals("Java")) {
                optionsOperator.selectJava();
            } else if (category.equals("Miscellaneous")) {
                optionsOperator.selectMiscellaneous();
            } else if (category.equals("PHP") || category.equals("C/C++")) {
                optionsOperator.selectCategory(category);
            }
            new EventTool().waitNoEvent(500);
        }
    }
}
