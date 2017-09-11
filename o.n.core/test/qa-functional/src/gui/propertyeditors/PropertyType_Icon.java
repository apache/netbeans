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

import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;

import org.netbeans.jellytools.properties.editors.IconCustomEditorOperator;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;

import org.netbeans.junit.NbTestSuite;



/**
 * Tests of Icon Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_Icon extends PropertyEditorsTest {
    
    public String propertyName_L;
    public String propertyValue_L;
    public String propertyValueExpectation_L;
    
    private final String FILE = "File: ";
    private final String CLASSPATH = "Classpath: ";
    private final String URL = "URL: ";
    private final String NO_PICTURE = "No Picture";
    
    public boolean waitDialog = false;
    
    /** Creates a new instance of PropertyType_Icon */
    public PropertyType_Icon(String testName) {
        super(testName);
    }
    
    
    public void setUp(){
        propertyName_L = "Icon";
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_Icon("verifyCustomizer"));
        suite.addTest(new PropertyType_Icon("testCustomizerCancel"));
        suite.addTest(new PropertyType_Icon("testCustomizerOkURL"));
        suite.addTest(new PropertyType_Icon("testCustomizerOkFile"));
        suite.addTest(new PropertyType_Icon("testCustomizerOkClasspath"));
        suite.addTest(new PropertyType_Icon("testCustomizerOkNoPicture"));
        suite.addTest(new PropertyType_Icon("testByInPlace"));
        suite.addTest(new PropertyType_Icon("testByInPlaceInvalid"));
        //        suite.addTest(new PropertyType_Icon("testCustomizerInvalid"));
        return suite;
    }
    
    
    public void testCustomizerOkURL() {
        propertyValue_L = URL + "http://www.netbeans.org/1.gif";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerOkFile() {
        propertyValue_L = FILE + "/home/mm119185/samplxxxxeDir.gif";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerOkClasspath() {
        propertyValue_L = CLASSPATH + "/gui/propertyeditors/data/ColorPreview.gif";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerOkNoPicture() {
        propertyValue_L = NO_PICTURE;
        propertyValueExpectation_L = "null";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerCancel(){
        propertyValue_L = URL + "http://www.netbeans.org/2.gif";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByCustomizerCancel(propertyName_L, false);
    }
    
/*    public void testCustomizerInvalid(){
        propertyValue_L = "xx";
        propertyValueExpectation_L = "File: "+propertyValue_L;
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
 */
    public void testByInPlace(){
        propertyValue_L = CLASSPATH +"/trash/PropertyEditorsTest.java";;
        propertyValueExpectation_L = "Invalid value " + propertyValue_L;
        waitDialog = false;
        setByInPlace(propertyName_L, propertyValue_L, true);
    }
    
    public void testByInPlaceInvalid(){
        propertyValue_L = "xx";
        propertyValueExpectation_L = "File: " +propertyValue_L;
        waitDialog = false;
        setByInPlace(propertyName_L, propertyValue_L, true);
    }
    
    public void verifyCustomizer() {
        verifyCustomizer(propertyName_L);
    }
    
    public void setCustomizerValue() {
        IconCustomEditorOperator customizer = new IconCustomEditorOperator(propertyCustomizer);
        
        String type;
        String path;
        int delim_index = propertyValue_L.indexOf(": ");
        
        if(delim_index>0){
            type = propertyValue_L.substring(0,delim_index+2);
            path = propertyValue_L.substring(delim_index+1).trim();
            
            if(type.equalsIgnoreCase(NO_PICTURE)){
                // TODO - update
                //customizer.noPicture();
            }else if(type.equalsIgnoreCase(URL)){
                // TODO - update
                //customizer.uRL();
                customizer.setName(path);
            }else if(type.equalsIgnoreCase(FILE)){
                // TODO - update
                //customizer.file();
                customizer.setName(path);
            }else if(type.equalsIgnoreCase(CLASSPATH)){
                // TODO - update
                //customizer.classpath();
                //customizer.setName(path); - hack because setName doesn't push Enter on the end of action
                //customizer.txtName().enterText(path);
                new EventTool().waitNoEvent(6000);
            }else {
                throw new JemmyException("ERROR: value is (\""+propertyValue_L+"\") - wrong format or unknown source type!!! type=["+type+"]/path=["+path+"]");
            }
            
        }else{
            // TODO - update
            //customizer.noPicture();
        }
        
    }
    
    public void verifyPropertyValue(boolean expectation) {
        verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, waitDialog);
    }
    
    
    public String getValue(String property) {
        String returnValue;
        PropertySheetOperator propertiesTab = new PropertySheetOperator(propertiesWindow);
        
        returnValue = new Property(propertiesTab, property).getValue();
        err.println("X GET VALUE = [" + returnValue + "].");
        
        // hack for icon poperty, this action expects, that right value is displayed (with label "Invalid value") as Accessible Name
        returnValue = new Property(propertiesTab, property).getValue();
        returnValue = returnValue.substring(returnValue.indexOf(property)+property.length()+2);
        err.println("X GET VALUE ACCESSIBLE NAME = [" + returnValue + "].");
        
        return returnValue;
    }
    
    public void verifyCustomizerLayout() {
        IconCustomEditorOperator customizer = new IconCustomEditorOperator(propertyCustomizer);
        // TODO - update
        //customizer.verify();
        customizer.btOK();
        customizer.btCancel();
    }    
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_Icon.class));
        junit.textui.TestRunner.run(suite());
    }
    
}
