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
