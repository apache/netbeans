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
package org.netbeans.test.permanentUI;

import java.awt.Component;
import java.awt.Container;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import junit.framework.Test;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.junit.Manager;
import org.netbeans.test.permanentUI.utils.NbMenuItem;
import org.netbeans.test.permanentUI.utils.ProjectContext;
import org.netbeans.test.permanentUI.utils.Utilities;

/**
 *
 * @author Lukas Hasik, Marian.Mirilovic@oracle.com
 */
public class OptionsTest extends PermUITestCase {

    public OptionsTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return OptionsTest.emptyConfiguration().
                addTest(OptionsTest.class, "testOptionsCategories").
                clusters(".*").enableModules(".*").
                suite();
    }

    @Override
    public void initialize() throws IOException {
        // do nothing
    }
    
    @Override
    public ProjectContext getContext() {
        return ProjectContext.NONE;    
    }

    public void testOptionsCategories() {
        OptionsOperator oo = OptionsOperator.invoke();//open options

        Component optionsPanel = oo.findSubComponent(getCompChooser("org.netbeans.modules.options.OptionsPanel"));
        //we need container to be able to traverse inside   
        Container optionsContainer = ContainerOperator.findContainerUnder(optionsPanel);
        ArrayList<Component> optionsCategories = new ArrayList<Component>();
        optionsCategories.addAll(Utilities.findComponentsInContainer(
                optionsContainer,
                getCompChooser("org.netbeans.modules.options.OptionsPanel$CategoryButton"),
                true));
        optionsCategories.addAll(Utilities.findComponentsInContainer(
                optionsContainer,
                getCompChooser("org.netbeans.modules.options.OptionsPanel$NimbusCategoryButton"),
                true));

        NbMenuItem ideOptions = new NbMenuItem(getName());//let store it in NbMenuItem TODO: refactor to make it simplier
        ArrayList<NbMenuItem> categories = new ArrayList<NbMenuItem>();
        NbMenuItem miscCategory = null;//remember the miscellanous because you will add the subcategories to it
        for (Component component : optionsCategories) {
            NbMenuItem optionsCategory = new NbMenuItem(((JLabel) component).getText());
            categories.add(optionsCategory);
            if (optionsCategory.getName().equals("Miscellaneous")) {//NOI18N
                miscCategory = optionsCategory;
            }
        }
        ideOptions.setSubmenu(categories);

        oo.selectMiscellaneous();//switch to Miscelenous

        JTabbedPane miscellaneousPanel = (JTabbedPane) oo.findSubComponent(getCompChooser("javax.swing.JTabbedPane"));

        ArrayList<NbMenuItem> miscCategories = new ArrayList<NbMenuItem>();
        for (int i = 0; i < miscellaneousPanel.getTabCount(); i++) {
            NbMenuItem miscCategoryItem = new NbMenuItem();
            miscCategoryItem.setName(miscellaneousPanel.getTitleAt(i));//
            miscCategories.add(miscCategoryItem);
        }
        miscCategory.setSubmenu(miscCategories);

        //load categories order from golden file
        LogFiles logFiles = new LogFiles();
        PrintStream ideFileStream = null;
        PrintStream goldenFileStream = null;

        try {
            ideFileStream = logFiles.getIdeFileStream();
            goldenFileStream = logFiles.getGoldenFileStream();

            //read the golden file
            NbMenuItem goldenOptions = Utilities.parseSubTreeByLines(getGoldenFile("options", "options-categories").getAbsolutePath());
            goldenOptions.setName(getName());

            //make a diff
            Utilities.printMenuStructure(ideFileStream, ideOptions, "  ", 100);
            Utilities.printMenuStructure(goldenFileStream, goldenOptions, "  ", 100);

            Manager.getSystemDiff().diff(logFiles.pathToIdeLogFile, logFiles.pathToGoldenLogFile, logFiles.pathToDiffLogFile);
            String message = Utilities.readFileToString(logFiles.pathToDiffLogFile);
            assertFile(message, logFiles.pathToGoldenLogFile, logFiles.pathToIdeLogFile, logFiles.pathToDiffLogFile);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        } finally {
            ideFileStream.close();
            goldenFileStream.close();
        }
    }

}
