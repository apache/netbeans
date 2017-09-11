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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package gui.propertyeditors;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.properties.editors.StringArrayCustomEditorOperator;

import org.netbeans.jemmy.operators.JTextFieldOperator;

import org.netbeans.junit.NbTestSuite;

/**
 * Tests of Identifier Array Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_IdentifierArray extends PropertyEditorsTest {

    public String propertyName_L;
    public String propertyValue_L;
    public String propertyValueExpectation_L;

    public boolean waitDialog = false;
    
    private final String ADD = "Add:";
    private final String REMOVE = "Remove:";
    private final String EDIT = "Edit:";
    private final String UP = "Up:";
    private final String DOWN = "Down:";
    
    private final String EE = "; ";
    
    /** Creates a new instance of PropertyType_IdentifierArray */
    public PropertyType_IdentifierArray(String testName) {
        super(testName);
    }
    
    
    public void setUp(){
        propertyName_L = "Identifier []";
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_IdentifierArray("testByInPlace"));
        suite.addTest(new PropertyType_IdentifierArray("verifyCustomizer"));
        suite.addTest(new PropertyType_IdentifierArray("testCustomizerCancel"));
        suite.addTest(new PropertyType_IdentifierArray("testCustomizerAdd"));
        suite.addTest(new PropertyType_IdentifierArray("testCustomizerRemove"));
        suite.addTest(new PropertyType_IdentifierArray("testCustomizerEdit"));
        suite.addTest(new PropertyType_IdentifierArray("testCustomizerUp"));
        suite.addTest(new PropertyType_IdentifierArray("testCustomizerDown"));
        return suite;
    }
    
    
    public void testCustomizerAdd() {
        propertyValue_L = ADD + "add";
        propertyValueExpectation_L = "remove, down, up, edit, add";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerRemove() {
        propertyValue_L = REMOVE + "remove";
        propertyValueExpectation_L = "down, up, edit, add";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerEdit() {
        propertyValue_L = EDIT + "edit" + EE + "newEdit";
        propertyValueExpectation_L = "down, up, newEdit, add";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }

    public void testCustomizerUp() {
        propertyValue_L = UP + "up";
        propertyValueExpectation_L = "up, down, newEdit, add";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerDown() {
        propertyValue_L = DOWN + "down";
        propertyValueExpectation_L = "up, newEdit, down, add";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerCancel(){
        propertyValue_L = ADD + "cancel";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByCustomizerCancel(propertyName_L, false);
    }
    
    public void testByInPlace(){
        propertyValue_L = "remove, down, up, edit";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByInPlace(propertyName_L, propertyValue_L, true);
    }
    
    public void verifyCustomizer() {
        verifyCustomizer(propertyName_L);
    }
    
    public void setCustomizerValue() {
        StringArrayCustomEditorOperator customizer = new StringArrayCustomEditorOperator(propertyCustomizer);
        
        if(propertyValue_L.startsWith(ADD)){
            customizer.btAdd().pushNoBlock();
            NbDialogOperator dialog = new NbDialogOperator("Enter");
            new JTextFieldOperator(dialog).setText(getItem(propertyValue_L,ADD));
            dialog.ok();
        }
        
        if(propertyValue_L.startsWith(REMOVE)){
            customizer.remove(getItem(propertyValue_L,REMOVE));
        }
        
        if(propertyValue_L.startsWith(EDIT)){
            customizer.lstItemList().selectItem(getItem(propertyValue_L,EDIT));
            customizer.btEdit().pushNoBlock();
            NbDialogOperator dialog = new NbDialogOperator("Enter");
            new JTextFieldOperator(dialog).setText(getItem(propertyValue_L,EE));
            dialog.ok();
        }
        
        if(propertyValue_L.startsWith(UP)){
            customizer.up(getItem(propertyValue_L,UP));
        }
        
        if(propertyValue_L.startsWith(DOWN)){
            customizer.down(getItem(propertyValue_L,DOWN));
        }
        
    }
    
    public void verifyPropertyValue(boolean expectation) {
        verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, waitDialog);
    }
    
    
    private String getItem(String str, String delim) {
        int first = str.indexOf(delim);
        int end = str.indexOf(EE);

        if(end > 0 && !delim.equals(EE)){
            return str.substring(delim.length(), end);
        } else {
            return str.substring(first + delim.length());
        }
    }
    
    public void verifyCustomizerLayout() {
        StringArrayCustomEditorOperator customizer = new StringArrayCustomEditorOperator(propertyCustomizer);
        customizer.btAdd();
        customizer.btRemove();
        customizer.btEdit();
        customizer.btUp();
        customizer.btDown();
        customizer.lstItemList();
        customizer.btOK();
        customizer.btCancel();
    }    
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_IdentifierArray.class));
        junit.textui.TestRunner.run(suite());
    }
    
}
