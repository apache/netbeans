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

import java.util.StringTokenizer;

import org.netbeans.jellytools.properties.editors.RectangleCustomEditorOperator;

import org.netbeans.jemmy.JemmyException;

import org.netbeans.junit.NbTestSuite;

/**
 * Tests of Rectangle Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_Rectangle extends PropertyEditorsTest {

    public String propertyName_L;
    public String propertyValue_L;
    public String propertyValueExpectation_L;

    public boolean waitDialog = false;

    /** Creates a new instance of PropertyType_Rectangle */
    public PropertyType_Rectangle(String testName) {
        super(testName);
    }
    
    
    public void setUp(){
        propertyName_L = "Rectangle";
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_Rectangle("verifyCustomizer"));
        suite.addTest(new PropertyType_Rectangle("testCustomizerCancel"));
        suite.addTest(new PropertyType_Rectangle("testCustomizerOk"));
        suite.addTest(new PropertyType_Rectangle("testByInPlace"));
        suite.addTest(new PropertyType_Rectangle("testByInPlaceOneValue"));
        suite.addTest(new PropertyType_Rectangle("testByInPlaceInvalid"));
        suite.addTest(new PropertyType_Rectangle("testCustomizerInvalid"));
        return suite;
    }
    
    public void testCustomizerOk() {
        propertyValue_L = "10, 20, 30, 40";
        propertyValueExpectation_L = "["+propertyValue_L+"]";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerCancel(){
        propertyValue_L = "100, 100, 200, 200";
        propertyValueExpectation_L = "["+propertyValue_L+"]";
        waitDialog = false;
        setByCustomizerCancel(propertyName_L, false);
    }
    
    public void testCustomizerInvalid(){
        propertyValue_L = "xx, 20, 30, 50";
        propertyValueExpectation_L = "["+propertyValue_L+"]";
        waitDialog = true;
        setByCustomizerOk(propertyName_L, false);
    }
    
    public void testByInPlace(){
        propertyValue_L = "30, 40, 50, 60";
        propertyValueExpectation_L = "["+propertyValue_L+"]";
        waitDialog = false;
        setByInPlace(propertyName_L, propertyValue_L, true);
    }
    
    public void testByInPlaceOneValue(){
        propertyValue_L = "70";
        propertyValueExpectation_L = "["+propertyValue_L+", "+propertyValue_L+", "+propertyValue_L+", "+propertyValue_L+"]";
        waitDialog = false;
        setByInPlace(propertyName_L, propertyValue_L, true);
    }
    
    public void testByInPlaceInvalid(){
        propertyValue_L = "xx";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = true;
        setByInPlace(propertyName_L, propertyValue_L, false);
    }
    
    public void verifyCustomizer() {
        verifyCustomizer(propertyName_L);
    }
    
    public void setCustomizerValue() {
        RectangleCustomEditorOperator customizer = new RectangleCustomEditorOperator(propertyCustomizer);
        StringTokenizer st = new StringTokenizer(propertyValue_L, ", ");
        int x = st.countTokens();
        
        if(x>4)
            throw new JemmyException("ERROR: InsetsCustomizer.setValue(\""+propertyValue_L+"\") - {number values="+x+"}.");
        
        customizer.setRectangleValue(st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken());
    }
    
    public void verifyPropertyValue(boolean expectation) {
        verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, waitDialog);
    }
    
    public void verifyCustomizerLayout() {
        RectangleCustomEditorOperator customizer = new RectangleCustomEditorOperator(propertyCustomizer);
        customizer.verify();
        customizer.btOK();
        customizer.btCancel();
    }    
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_Rectangle.class));
        junit.textui.TestRunner.run(suite());
    }
    
}
