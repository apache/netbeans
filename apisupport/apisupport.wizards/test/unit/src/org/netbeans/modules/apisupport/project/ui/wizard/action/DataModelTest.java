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

package org.netbeans.modules.apisupport.project.ui.wizard.action;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.apisupport.project.ui.wizard.action.DataModel.ActionReferenceModel;

/**
 * Tests {@link DataModel}.
 *
 * @author Martin Krauskopf
 */
public class DataModelTest extends NbTestCase {
    
    public DataModelTest(String name) {
        super(name);
    }
    
    public void testActionReferenceCreate() throws Exception {
        ActionReferenceModel res = DataModel.createActionReference("mypath/sub", 30, 130, 100, "myname");
        assertEquals("mypath/sub", res.path());
        assertEquals(100, res.position());
        assertEquals("myname", res.name());
        assertEquals("before", 30, res.separatorBefore());
        assertEquals("after", 130, res.separatorAfter());
    }

    /* XXX rewrite to use mock data
    public void testDataModelGenerationForAlwaysEnabledActions() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        FileSystem fs = LayerUtils.getEffectiveSystemFilesystem(project);
        FileObject root = fs.getRoot();
        FileUtil.createData(root, "Menu/Help/Tutorials/quick-start.url").setAttribute("position", 100);
        FileUtil.createData(root, "Menu/Help/Tutorials/prj-import-guide.url").setAttribute("position", 200);
        FileUtil.createData(root, "Toolbars/Edit/org-openide-actions-FindAction.instance").setAttribute("position", 6000);
        
        WizardDescriptor wd = new WizardDescriptor() {};
        wd.putProperty(ProjectChooserFactory.WIZARD_KEY_PROJECT, project);
        DataModel data = new DataModel(wd);
        
        // first panel data (Action Type)
        data.setAlwaysEnabled(true);
        
        // second panel data (GUI Registration)
        data.setCategory("Actions/Tools");
        // global menu item
        data.setGlobalMenuItemEnabled(true);
        data.setGMIParentMenu("Menu/Help/Tutorials");
        data.setGMIPosition(new Position("quick-start.url", "prj-import-guide.url"));
        data.setGMISeparatorBefore(true);
        data.setGMISeparatorAfter(true);
        // global toolbar button
        data.setToolbarEnabled(true);
        data.setToolbar("Toolbars/Edit");
        data.setToolbarPosition(new Position("org-openide-actions-FindAction.instance", null));
        // global keyboard shortcut
        data.setKeyboardShortcutEnabled(true);
        data.setKeyStroke("DA-B");
        
        data.setFileTypeContextEnabled(true);
        data.setFTContextType("Loaders/text/xml/Actions/");
        
        // third panel data (Name, Icon, and Location)
        data.setClassName("BeepAction");
        data.setDisplayName("Beep");
        data.setPackageName("org.example.module1");
        
        CreatedModifiedFiles cmf = data.getCreatedModifiedFiles();
        assertEquals(
            Arrays.asList("src/org/example/module1/BeepAction.java"),
            Arrays.asList(cmf.getCreatedPaths())
        );
        assertEquals(
            Arrays.asList(new String[] {"nbproject/project.xml" }),
            Arrays.asList(cmf.getModifiedPaths())
        );
        
        cmf.run();
        
        FileObject ba = project.getSourceDirectory().getFileObject("org/example/module1/BeepAction.java");
        assertNotNull("BeepAction was generated", ba);
        String text = ba.asText();

        if (text.toLowerCase().contains("freemarker")) {
            fail("There shall be no errors in the generated BeepAction.java:\n" + text);
        }
        if (text.toLowerCase().contains("333")) {
            fail("Postion 333x signals wrongly defined position:\n" + text);
        }
        if (text.toLowerCase().contains("-1")) {
            fail("Postion -1 should not be printed at all:\n" + text);
        }
        if (!text.toLowerCase().contains("position=150")) {
            fail("Postion position=150 is what is in middle of 100 and :\n" + text);
        }
        if (!text.toLowerCase().contains("position=6100") || !text.contains("Toolbars/Edit")) {
            fail("Toolbar is generated:\n" + text);
        }
        if (!text.contains("Loaders/text/xml/Actions")) {
            fail("Context action is generated:\n" + text);
        }
        if (!text.contains("separatorBefore=125")) {
            fail("separatorBefore shall be there:\n" + text);
        }
        if (!text.contains("separatorAfter=175")) {
            fail("separatorAfter shall be there:\n" + text);
        }
    }
    */

//    XXX: failing test, fix or delete
//    public void testDataModelGenarationForConditionallyEnabledActions() throws Exception {
//        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
//        WizardDescriptor wd = new WizardDescriptor() {};
//        wd.putProperty(ProjectChooserFactory.WIZARD_KEY_PROJECT, project);
//        DataModel data = new DataModel(wd);
//
//        // first panel data (Action Type)
//        data.setAlwaysEnabled(false);
//        data.setCookieClasses(new String[] {DataModel.PREDEFINED_COOKIE_CLASSES[1], DataModel.PREDEFINED_COOKIE_CLASSES[2]});
//        data.setMultiSelection(false);
//
//        // second panel data (GUI Registration)
//        data.setCategory("Actions/Tools");
//        // global menu item
//        data.setGlobalMenuItemEnabled(true);
//        data.setGMIParentMenu("Menu/Help/Tutorials");
//        data.setGMIPosition(new Position("quick-start.url", "prj-import-guide.url"));
//        data.setGMISeparatorBefore(true);
//        data.setGMISeparatorAfter(true);
//        // global toolbar button
//        data.setToolbarEnabled(true);
//        data.setToolbar("Toolbars/Edit");
//        data.setToolbarPosition(new Position("org-openide-actions-FindAction.instance", null));
//        // file type context menu item
//        data.setFileTypeContextEnabled(true);
//        data.setFTContextType("Loaders/text/x-java/Actions");
//        data.setFTContextPosition(new Position(null, "OpenAction.instance"));
//        data.setFTContextSeparatorBefore(false);
//        data.setFTContextSeparatorAfter(true);
//        // editor context menu item
//        data.setEditorContextEnabled(true);
//        data.setEdContextType("Editors/text/x-java/Popup");
//        data.setEdContextPosition(new Position(null, "generate-goto-popup"));
//        data.setEdContextSeparatorBefore(false);
//        data.setEdContextSeparatorAfter(true);
//
//        // third panel data (Name, Icon, and Location)
//        data.setClassName("BeepAction");
//        data.setDisplayName("Beep");
//        data.setPackageName("org.example.module1");
//
//        CreatedModifiedFiles cmf = data.getCreatedModifiedFiles();
//        assertEquals("new files",
//                Arrays.asList(new String[] {"src/org/example/module1/BeepAction.java", "src/org/example/module1/Bundle.properties"}),
//                Arrays.asList(cmf.getCreatedPaths()));
//        assertEquals("modified files",
//                Arrays.asList(new String[] {"nbproject/project.xml", "src/org/example/module1/resources/layer.xml"}),
//                Arrays.asList(cmf.getModifiedPaths()));
//
//        cmf.run();
//
//        String[] supposedContent = new String[] {
//            "<filesystem>",
//                    "<folder name=\"Actions\">",
//                    "<folder name=\"Tools\">",
//                    "<file name=\"org-example-module1-BeepAction.instance\"/>",
//                    "</folder>",
//                    "</folder>",
//                    "<folder name=\"Editors\">",
//                    "<folder name=\"text\">",
//                    "<folder name=\"x-java\">",
//                    "<folder name=\"Popup\">",
//                    "<attr name=\"org-example-module1-BeepAction.shadow/generate-goto-popup\" boolvalue=\"true\"/>",
//                    "<attr name=\"org-example-module1-BeepAction.shadow/org-example-module1-separatorAfter.instance\" boolvalue=\"true\"/>",
//                    "<attr name=\"org-example-module1-separatorAfter.instance/generate-goto-popup\" boolvalue=\"true\"/>",
//                    "<file name=\"org-example-module1-BeepAction.shadow\">",
//                    "<attr name=\"originalFile\" stringvalue=\"Actions/Tools/org-example-module1-BeepAction.instance\"/>",
//                    "</file>",
//                    "<file name=\"org-example-module1-separatorAfter.instance\">",
//                    "<attr name=\"instanceClass\" stringvalue=\"javax.swing.JSeparator\"/>",
//                    "</file>",
//                    "</folder>",
//                    "</folder>",
//                    "</folder>",
//                    "</folder>",
//                    "<folder name=\"Loaders\">",
//                    "<folder name=\"text\">",
//                    "<folder name=\"x-java\">",
//                    "<folder name=\"Actions\">",
//                    "<attr name=\"org-example-module1-BeepAction.shadow/OpenAction.instance\" boolvalue=\"true\"/>",
//                    "<attr name=\"org-example-module1-BeepAction.shadow/org-example-module1-separatorAfter.instance\" boolvalue=\"true\"/>",
//                    "<attr name=\"org-example-module1-separatorAfter.instance/OpenAction.instance\" boolvalue=\"true\"/>",
//                    "<file name=\"org-example-module1-BeepAction.shadow\">",
//                    "<attr name=\"originalFile\" stringvalue=\"Actions/Tools/org-example-module1-BeepAction.instance\"/>",
//                    "</file>",
//                    "<file name=\"org-example-module1-separatorAfter.instance\">",
//                    "<attr name=\"instanceClass\" stringvalue=\"javax.swing.JSeparator\"/>",
//                    "</file>",
//                    "</folder>",
//                    "</folder>",
//                    "</folder>",
//                    "</folder>",
//                    "<folder name=\"Menu\">",
//                    "<folder name=\"Help\">",
//                    "<folder name=\"Tutorials\">",
//                    "<attr name=\"org-example-module1-BeepAction.shadow/org-example-module1-separatorAfter.instance\" boolvalue=\"true\"/>",
//                    "<attr name=\"org-example-module1-BeepAction.shadow/prj-import-guide.url\" boolvalue=\"true\"/>",
//                    "<attr name=\"org-example-module1-separatorAfter.instance/prj-import-guide.url\" boolvalue=\"true\"/>",
//                    "<attr name=\"org-example-module1-separatorBefore.instance/org-example-module1-BeepAction.shadow\" boolvalue=\"true\"/>",
//                    "<attr name=\"quick-start.url/org-example-module1-BeepAction.shadow\" boolvalue=\"true\"/>",
//                    "<attr name=\"quick-start.url/org-example-module1-separatorBefore.instance\" boolvalue=\"true\"/>",
//                    "<file name=\"org-example-module1-BeepAction.shadow\">",
//                    "<attr name=\"originalFile\" stringvalue=\"Actions/Tools/org-example-module1-BeepAction.instance\"/>",
//                    "</file>",
//                    "<file name=\"org-example-module1-separatorAfter.instance\">",
//                    "<attr name=\"instanceClass\" stringvalue=\"javax.swing.JSeparator\"/>",
//                    "</file>",
//                    "<file name=\"org-example-module1-separatorBefore.instance\">",
//                    "<attr name=\"instanceClass\" stringvalue=\"javax.swing.JSeparator\"/>",
//                    "</file>",
//                    "</folder>",
//                    "</folder>",
//                    "</folder>",
//                    "<folder name=\"Toolbars\">",
//                    "<folder name=\"Edit\">",
//                    "<attr name=\"org-openide-actions-FindAction.instance/org-example-module1-BeepAction.shadow\" boolvalue=\"true\"/>",
//                    "<file name=\"org-example-module1-BeepAction.shadow\">",
//                    "<attr name=\"originalFile\" stringvalue=\"Actions/Tools/org-example-module1-BeepAction.instance\"/>",
//                    "</file>",
//                    "</folder>",
//                    "</folder>",
//                    "</filesystem>"
//        };
//
//        CreatedModifiedFilesTest.assertLayerContent(supposedContent,
//                new File(getWorkDir(), "module1/src/org/example/module1/resources/layer.xml"));
//    }
    
}

