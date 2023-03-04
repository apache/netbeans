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

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JLabel;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.StringComparator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author theofanis
 */
public class SearchInOptionsTest extends JellyTestCase {

    private StringComparator stringComparator;
    private OptionsOperator optionsOperator;
    private JTextFieldOperator jTextFieldOperator;
    private JTabbedPaneOperator jTabbedPaneOperator;

    /**
     * Constructor required by JUnit
     */
    public SearchInOptionsTest(String testName) {
        super(testName);
    }

    /**
     * Creates suite from particular test cases.
     */
    public static Test suite() {
        return NbModuleSuite.createConfiguration(SearchInOptionsTest.class).addTest(
                "testSearchInOptionsWindow").clusters(".*").enableModules(".*").gui(true).suite();
    }

    public void testSearchInOptionsWindow() {
        OptionsOperator.invoke();
        new EventTool().waitNoEvent(1000);
        log("Option dialog was opened");

        optionsOperator = new OptionsOperator();
        new EventTool().waitNoEvent(1000);

        jTextFieldOperator = new JTextFieldOperator(optionsOperator);
        stringComparator = Operator.getDefaultStringComparator();

        String[] tabIndexes = {"General"};
        String[] selectedCategories = {"Editor"};
        ArrayList<String> enabledCategories = new ArrayList<String>();
        enabledCategories.add("Editor");
        searchFor("general editor", tabIndexes, selectedCategories, enabledCategories);

        tabIndexes[0] = "Macros";
        searchFor("macros", tabIndexes, selectedCategories, enabledCategories);

        tabIndexes[0] = "Code Completion";
        searchFor("completion", tabIndexes, selectedCategories, enabledCategories);

        tabIndexes[0] = "Code Templates";
        searchFor("templates", tabIndexes, selectedCategories, enabledCategories);

        tabIndexes[0] = "Spellchecker";
        searchFor("dictionary", tabIndexes, selectedCategories, enabledCategories);

        tabIndexes[0] = "On Save";
        searchFor("save", tabIndexes, selectedCategories, enabledCategories);

        enabledCategories.clear();
        enabledCategories.add("General");
        tabIndexes[0] = null;
        selectedCategories[0] = "General";
        searchFor("proxy", tabIndexes, selectedCategories, enabledCategories);

        enabledCategories.clear();
        enabledCategories.add("Fonts & Colors");
        tabIndexes[0] = "Syntax";
        selectedCategories[0] = "FontsAndColors";
        searchFor("syntax", tabIndexes, selectedCategories, enabledCategories);

        tabIndexes[0] = "Highlighting";
        searchFor("highlighting", tabIndexes, selectedCategories, enabledCategories);

        tabIndexes[0] = "Versioning";
        searchFor("versioning", tabIndexes, selectedCategories, enabledCategories);

        enabledCategories.clear();
        enabledCategories.add("Keymap");
        tabIndexes[0] = null;
        selectedCategories[0] = "Keymaps";
        searchFor("keymap", tabIndexes, selectedCategories, enabledCategories);

        enabledCategories.clear();
        enabledCategories.add("Java");
        tabIndexes[0] = "Maven";
        selectedCategories[0] = "Java";
        searchFor("maven", tabIndexes, selectedCategories, enabledCategories);

        tabIndexes[0] = "Ant";
        selectedCategories[0] = "Java";
        searchFor("ant", tabIndexes, selectedCategories, enabledCategories);

        tabIndexes[0] = "JavaFX";
        selectedCategories[0] = "Java";
        searchFor("fx", tabIndexes, selectedCategories, enabledCategories);

        enabledCategories.clear();
        enabledCategories.add("Miscellaneous");
        tabIndexes[0] = "Groovy";
        selectedCategories[0] = "Miscellaneous";
        searchFor("groovy", tabIndexes, selectedCategories, enabledCategories);

        tabIndexes[0] = "SVG";
        selectedCategories[0] = "Miscellaneous";
        searchFor("svg", tabIndexes, selectedCategories, enabledCategories);

        tabIndexes[0] = "Terminal";
        selectedCategories[0] = "Miscellaneous";
        searchFor("terminal", tabIndexes, selectedCategories, enabledCategories);

        enabledCategories.add("Fonts & Colors");
        tabIndexes = new String[2];
        selectedCategories = new String[2];
        tabIndexes[0] = "Diff";
        tabIndexes[1] = "Diff";
        selectedCategories[0] = "FontsAndColors";
        selectedCategories[1] = "Miscellaneous";
        searchFor("diff", tabIndexes, selectedCategories, enabledCategories);

        enabledCategories.clear();
        enabledCategories.add("PHP");
        tabIndexes = new String[1];
        selectedCategories = new String[1];
        tabIndexes[0] = "ApiGen";
        selectedCategories[0] = "PHP";
        searchFor("apigen", tabIndexes, selectedCategories, enabledCategories);

        tabIndexes[0] = "Symfony2";
        searchFor("symfony2", tabIndexes, selectedCategories, enabledCategories);

        tabIndexes[0] = "Smarty";
        searchFor("smarty", tabIndexes, selectedCategories, enabledCategories);

        enabledCategories.clear();
        enabledCategories.add("C/C++");
        tabIndexes[0] = "Build Tools";
        selectedCategories[0] = "C/C++";
        searchFor("build tools", tabIndexes, selectedCategories, enabledCategories);

        tabIndexes[0] = "Code Assistance";
        searchFor("code assistance", tabIndexes, selectedCategories, enabledCategories);

        enabledCategories.clear();
        enabledCategories.addAll(Arrays.asList("General", "Editor", "Fonts & Colors", "Keymap", "Java", "PHP", "C/C++", "Miscellaneous"));
        searchFor("", tabIndexes, selectedCategories, enabledCategories);
    }

    private void searchFor(String searchTxt, String[] selectedTabTitles, String[] selectedCategories, ArrayList<String> enabledCategories) {
        jTextFieldOperator.setText(searchTxt);
        new EventTool().waitNoEvent(500);
        jTextFieldOperator.pushKey(KeyEvent.VK_ENTER);
        new EventTool().waitNoEvent(1000);
        for (int i = 0; i < selectedCategories.length; i++) {
            String selectedCategory = selectedCategories[i];
            if (selectedCategory.equals("General")) {
                optionsOperator.selectGeneral();
            } else if (selectedCategory.equals("Editor")) {
                optionsOperator.selectEditor();
            } else if (selectedCategory.equals("FontsAndColors")) {
                optionsOperator.selectFontAndColors();
            } else if (selectedCategory.equals("Keymaps")) {
                optionsOperator.selectKeymap();
            } else if (selectedCategory.equals("Java")) {
                optionsOperator.selectJava();
            } else if (selectedCategory.equals("Miscellaneous")) {
                optionsOperator.selectMiscellaneous();
            } else if (selectedCategory.equals("PHP") || selectedCategory.equals("C/C++")) {
                optionsOperator.selectCategory(selectedCategory);
            }
            new EventTool().waitNoEvent(1000);
            String selectedTabTitle = selectedTabTitles[i];
            if (selectedTabTitle != null) {
                jTabbedPaneOperator = new JTabbedPaneOperator(optionsOperator);
                assertEquals(selectedTabTitle, jTabbedPaneOperator.getTitleAt(jTabbedPaneOperator.getSelectedIndex()));
            }
        }
        for (String category : enabledCategories) {
            assertTrue(getJLabelOperator(category).isEnabled());
        }
    }

    private JLabelOperator getJLabelOperator(final String category) {
        return new JLabelOperator(optionsOperator, new ComponentChooser() {
            @Override
            public boolean checkComponent(Component comp) {
                if (comp.getClass().getName().equals("org.netbeans.modules.options.OptionsPanel$CategoryButton") ||// NOI18N
                        comp.getClass().getName().equals("org.netbeans.modules.options.OptionsPanel$NimbusCategoryButton")) { // NOI18N
                    if (((JLabel) comp).getText() != null) {
                        return stringComparator.equals(((JLabel) comp).getText(), category);
                    }
                }
                return false;
            }

            @Override
            public String getDescription() {
                return "OptionsPanel$CategoryButton with text " + category; // NOI18N
            }
        });
    }
}
