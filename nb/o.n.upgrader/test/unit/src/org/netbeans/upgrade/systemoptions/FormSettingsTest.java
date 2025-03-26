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

package org.netbeans.upgrade.systemoptions;

/**
 * @author Radek Matous
 */
public class FormSettingsTest extends BasicTestForImport {
    public FormSettingsTest(String testName) {
        super(testName, "formsettings.settings");
    }
    @Override
    public void testPreferencesNodePath() throws Exception {
        assertPreferencesNodePath("/org/netbeans/modules/form");
    }
    @Override
    public void testPropertyNames() throws Exception {
        assertPropertyNames(new String[] {
            "applyGridToPosition",
                    "applyGridToSize",
                    "connectionBorderColor",
                    "displayWritableOnly",
                    "dragBorderColor",
                    "editorSearchPath",
                    "eventVariableName",
                    "foldGeneratedCode",
                    "formDesignerBackgroundColor",
                    "formDesignerBorderColor",
                    "generateMnemonicsCode",
                    "gridX",
                    "gridY",
                    "guidingLineColor",
                    "layoutCodeTarget",
                    "listenerGenerationStyle",
                    "registeredEditors",
                    "selectionBorderColor",
                    "selectionBorderSize",
                    "toolBarPalette",
                    "useIndentEngine",
                    "variablesLocal",
                    "variablesModifier"
        });
    }
    
    public void testApplyGridToSize() throws Exception { assertProperty("applyGridToSize","true"); }
    public void testDisplayWritableOnly() throws Exception { assertProperty("displayWritableOnly","true"); }
    public void testEventVariableName() throws Exception { assertProperty("eventVariableName","evt"); }
    public void testFoldGeneratedCode() throws Exception { assertProperty("foldGeneratedCode","true"); }
    public void testGenerateMnemonicsCode() throws Exception { assertProperty("generateMnemonicsCode","false"); }
    public void testGridX() throws Exception { assertProperty("gridX","10"); }
    public void testGridY() throws Exception { assertProperty("gridY","10"); }
    public void testListenerGenerationStyle() throws Exception { assertProperty("listenerGenerationStyle","0"); }
    public void testlayoutCodeTarget() throws Exception { assertProperty("layoutCodeTarget","0"); }    
    public void testSelectionBorderSize() throws Exception { assertProperty("selectionBorderSize","1"); }
    public void testToolBarPalette() throws Exception { assertProperty("toolBarPalette","true"); }
    public void testUseIndentEngine() throws Exception { assertProperty("useIndentEngine","false"); }
    public void testVariablesLocal() throws Exception { assertProperty("variablesLocal","true"); }
    public void testVariablesModifie() throws Exception { assertProperty("variablesModifier","0"); }
    public void testApplyGridToPosition() throws Exception { assertProperty("applyGridToPosition","true"); }
    
    public void testEditorSearchPath() throws Exception { assertProperty("editorSearchPath","org.netbeans.modules.form.editors2"); }    
    public void testRegisteredEditors() throws Exception { assertProperty("registeredEditors","aaaaaaa | bbbbbbbbb"); }    

    public void testConnectionBorderColor() throws Exception { assertProperty("connectionBorderColor","-16776961"); }
    public void testDragBorderColor() throws Exception { assertProperty("dragBorderColor","-8355712"); }
    public void testFormDesignerBackgroundColor() throws Exception { assertProperty("formDesignerBackgroundColor","-1"); }
    public void testFormDesignerBorderColor() throws Exception { assertProperty("formDesignerBorderColor","-2039553"); }
    public void testGuidingLineColor() throws Exception { assertProperty("guidingLineColor","-7361596"); }
    public void testSelectionBorderColor() throws Exception { assertProperty("selectionBorderColor","-23552"); }    
}
