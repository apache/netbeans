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

package org.netbeans.modules.j2ee.sun.ide.editors.ui;

import javax.swing.JLabel;
import junit.framework.TestCase;
import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePairsPropertyEditor;
import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;

/**
 *
 * @author vkraemer
 */
public class DDTablePanelTest extends TestCase {

    public void testCoverage() {
        NameValuePair vals[] = new NameValuePair[1];
        NameValuePair nvp;
        nvp = vals[0] = new NameValuePair();
        vals[0].setParamName("foo");
        vals[0].setParamValue("bar");
        nvp.setParamDescription("this is my description");
        nvp.getParamDescription();
        nvp.getParamName();
        nvp.getParamValue();
        NameValuePairsPropertyEditor nvppe = 
            new NameValuePairsPropertyEditor(vals);
        nvppe.getAsText();
        nvppe.getValue();
        nvppe.isPaintable();
        nvppe.setAsText("abc 123");
        nvppe.setValue(vals);
        nvppe.supportsCustomEditor();
        DDTablePanel panel = (DDTablePanel) nvppe.getCustomEditor();
        panel.setVerticalScrollBarValue(59);
        panel.setSelectedRow(0);
//        panel.setSelectedRow(1);
//        panel.setSelectedRow(-1);
        panel.linkLabel(new JLabel("test label"));
        panel.getSelectedRow();
        panel.getPropertyValue();
        panel.getHeaderColor();
        panel.setSelectedRow(0);
        //panel.editSelectedRow();
    }
    
    public DDTablePanelTest(String testName) {
        super(testName);
    }
    
    /**
     * Test of addListSelectionListener method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.DDTablePanel.
     *
    public void testAddListSelectionListener() {
        System.out.println("testAddListSelectionListener");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of addVerticalScrollBarAdjustmentListener method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.DDTablePanel.
     *
    public void testAddVerticalScrollBarAdjustmentListener() {
        System.out.println("testAddVerticalScrollBarAdjustmentListener");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of setVerticalScrollBarValue method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.DDTablePanel.
     *
    public void testSetVerticalScrollBarValue() {
        System.out.println("testSetVerticalScrollBarValue");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of linkLabel method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.DDTablePanel.
     *
    public void testLinkLabel() {
        System.out.println("testLinkLabel");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of getHeaderColor method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.DDTablePanel.
     *
    public void testGetHeaderColor() {
        System.out.println("testGetHeaderColor");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of getSelectedRow method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.DDTablePanel.
     *
    public void testGetSelectedRow() {
        System.out.println("testGetSelectedRow");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of setSelectedRow method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.DDTablePanel.
     *
    public void testSetSelectedRow() {
        System.out.println("testSetSelectedRow");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of setCellEditor method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.DDTablePanel.
     *
    public void testSetCellEditor() {
        System.out.println("testSetCellEditor");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of editSelectedRow method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.DDTablePanel.
     *
    public void testEditSelectedRow() {
        System.out.println("testEditSelectedRow");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of getPropertyValue method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.DDTablePanel.
     *
    public void testGetPropertyValue() {
        System.out.println("testGetPropertyValue");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    // TODO add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    */
    
}
