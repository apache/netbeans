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

import gui.propertyeditors.utilities.CoreSupport;

import org.netbeans.jellytools.properties.editors.ProcessDescriptorCustomEditorOperator;

import org.netbeans.junit.NbTestSuite;

/**
 * Tests of NbProcessDescriptor Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_NbProcessDescriptor extends PropertyEditorsTest {

    public String propertyName_L;
    public String propertyValue_L;
    public String propertyValueExpectation_L;

    public boolean waitDialog = false;

    private String DELIM = ";";
    
    private static String FS_Data_path;
    
    /** Creates a new instance of PropertyType_NbProcessDescriptor */
    public PropertyType_NbProcessDescriptor(String testName) {
        super(testName);
    }
    
    
    public void setUp(){
        propertyName_L = "NbProcessDescriptor";
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        
        //TODO write new way for promoD
        //String path = CoreSupport.getSystemPath("gui/data", CoreSupport.beanName, "java");
        String path = ""; 
        
        FS_Data_path = path.substring(0,path.lastIndexOf(System.getProperty("file.separator")));
        
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_NbProcessDescriptor("testByInPlace"));
        suite.addTest(new PropertyType_NbProcessDescriptor("verifyCustomizer"));
        suite.addTest(new PropertyType_NbProcessDescriptor("testCustomizerCancel"));
        suite.addTest(new PropertyType_NbProcessDescriptor("testCustomizerOk"));
        return suite;
    }
    
    
    public void testCustomizerOk() {
        propertyValue_L = FS_Data_path + DELIM + "okArgument";
        propertyValueExpectation_L =  FS_Data_path + " okArgument";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerCancel(){
        propertyValue_L = "cancel" + DELIM + "cancel_argument";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByCustomizerCancel(propertyName_L, false);
    }
    
    public void testByInPlace(){
        propertyValue_L = "process argument";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByInPlace(propertyName_L, propertyValue_L, true);
    }
    
    public void verifyCustomizer() {
        verifyCustomizer(propertyName_L);
    }
    
    public void setCustomizerValue() {
        ProcessDescriptorCustomEditorOperator customizer = new ProcessDescriptorCustomEditorOperator(propertyCustomizer);
        
        int index = propertyValue_L.indexOf(DELIM);
        String process = propertyValue_L.substring(0,index);
        String argument = propertyValue_L.substring(index + DELIM.length());
        
        customizer.setProcess(process);
        customizer.setArguments(argument);
        
    }
    
    public void verifyPropertyValue(boolean expectation) {
        verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, waitDialog);
    }
    
    public void verifyCustomizerLayout() {
        ProcessDescriptorCustomEditorOperator customizer = new ProcessDescriptorCustomEditorOperator(propertyCustomizer);
        customizer.verify();
        customizer.btOK();
        customizer.btCancel();
    }    
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_NbProcessDescriptor.class));
        junit.textui.TestRunner.run(suite());
    }
    
}
