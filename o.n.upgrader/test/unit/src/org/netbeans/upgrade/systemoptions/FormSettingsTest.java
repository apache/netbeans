/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997/2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.upgrade.systemoptions;

/**
 * @author Radek Matous
 */
public class FormSettingsTest extends BasicTestForImport {
    public FormSettingsTest(String testName) {
        super(testName, "formsettings.settings");
    }
    public void testPreferencesNodePath() throws Exception {
        assertPreferencesNodePath("/org/netbeans/modules/form");
    }
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
