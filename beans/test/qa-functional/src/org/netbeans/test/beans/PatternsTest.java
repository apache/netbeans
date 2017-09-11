/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.test.beans;

import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.beans.operators.Navigator;

/**
 *
 * @author ssazonov
 */
public class PatternsTest extends BeansTestCase {

    private EditorOperator editor;
    private Navigator navigator;

    public PatternsTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp(); 
        editor = openEditor("beans", "Beans");
        navigator = new Navigator();
        navigator.setScopeToBeanPatterns();

    }

    @Override
    protected void tearDown() throws Exception {
        if (editor != null) {
            editor.closeDiscard();
        }
        super.tearDown(); 
    }

    public void testEmpty() {
        String expected = "root\n__Beans\n";
        navigator.waitForString(expected);

    }

    public void testGetter() {
        putAndType(4, 1, "public int getX(){return 1;}");
        String expected = "root\n__Beans\n____(r/-) Property Pattern : x\n";
        navigator.waitForString(expected);
    }
    
    public void testSetter() {
        putAndType(4, 1, "public void setX(int a){}");
        String expected = "root\n__Beans\n____(-/w) Property Pattern : x\n";
        navigator.waitForString(expected);        
    }
    
    public void testGetterSetter() {
        putAndType(4, 1, "public int getX(){return 1;} public void setX(int x){}");
        String expected = "root\n__Beans\n____(r/w) Property Pattern : x\n";
        navigator.waitForString(expected);        
        
    }
    
    public void testInvalidPattern() {
        putAndType(4, 1, "int getX(){return 1;} public void sEtX(){}");
        String expected = "root\n__Beans\n";
        navigator.waitForString(expected);        
    }
    
    public void testIndexedSetter() {
        putAndType(4, 1, "public void setX(int i,String a) {}");
        String expected = "root\n__Beans\n____(-/w) Indexed Property Pattern : x\n";
        navigator.waitForString(expected);                        
    }
    
    public void testIndexedGetter() {
        putAndType(4, 1, "public String getX(int i) {return \"\";}");
        String expected = "root\n__Beans\n____(r/-) Indexed Property Pattern : x\n";
        navigator.waitForString(expected);                        
        
    }
    
    public void testIndexedGetterSetter() {
        putAndType(4, 1, "public void setX(int i,String a) {} public String getX(int i) {return \"\";}");
        String expected = "root\n__Beans\n____(r/w) Indexed Property Pattern : x\n";
        navigator.waitForString(expected);                        
    }
    
    public void testInvalidIndexedPattern() {
        putAndType(4, 1, "public void sEtX(int i,String a) {} String getX(int i) {return \"\";}");
        String expected = "root\n__Beans\n";
        navigator.waitForString(expected);                        
    }
    
    public void testChangeSupport() {
        putAndType(5, 1, "public void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {}");
        putAndType(4, 1, "public void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {}");
        String expected = "root\n__Beans\n____Multicast Event Source Pattern : propertyChangeListener\n";
        navigator.waitForString(expected);                        
    }
    
    public void testUnicastChangeSupport() {
       putAndType(5, 1, "public void addPropertyChangeListener(java.beans.PropertyChangeListener listener) throws java.util.TooManyListenersException {}");
        putAndType(4, 1, "public void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {}");
        String expected = "root\n__Beans\n____Unicast Event Source Pattern : propertyChangeListener\n";
        navigator.waitForString(expected);                                        
    }
    
    public void testBooleanGetter() {
        putAndType(4, 1, "public boolean isA() { return true;}");
        String expected = "root\n__Beans\n____(r/-) Property Pattern : a\n";
        navigator.waitForString(expected);                                
    }
    
    public void testInnerClasse() {
        putAndType(4, 1, "class Inner { public boolean isA() { return true;}}");
        String expected = "root\n__Beans\n____Inner\n______(r/-) Property Pattern : a\n";
        navigator.waitForString(expected);                        
    }

    

    private void putAndType(int row, int col, String text) {
        editor.setCaretPosition(row, col);
        JEditorPaneOperator jepo = editor.txtEditorPane();
        jepo.typeText(text);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(PatternsTest.class)
                .enableModules(".*")
                .clusters(".*"));
    }
}
