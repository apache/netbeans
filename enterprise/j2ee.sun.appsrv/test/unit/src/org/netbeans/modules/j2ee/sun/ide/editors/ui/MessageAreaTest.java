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

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author vkraemer
 */
public class MessageAreaTest extends TestCase {

    public void testCoverage() {
        MessageArea ma = new MessageArea();
        ma = new MessageArea("foolish text");
        ma = new MessageArea("");
        // not null safe
        //ma = new MessageArea(null);
        ma.addNotify();
        //ma.setWidth(100);
        List l = new ArrayList();
        l.add("a");
        l.add("b");
        ma.setBulletItems("");
        // compile error
        // ma.setBulletItems(null);
        ma.setBulletItems("item1 item2 item3");
        ma.setBulletItems(new String[] { "item1", "item2", "item3" });
        ma.setBulletItems(new String[] { null });
        ma.setBulletItems(l);
        l.add(null);
        ma.setBulletItems(l);
        ma.setEndText("end text");
        ma.setEndText("");
        // not safe
        //ma.setEndText(null);
        ma.appendBulletItem("item4 item5 item6");
        ma.appendBulletItems(new String[] { "item7", "item8", "item9" });
        ma.appendBulletItems(new String[] { null });
        ma.appendBulletItems(l);
        ma.appendText("a b c");
        ma.setForeground(Color.CYAN);
        ma.setFont(Font.decode("ARIAL Bold 18"));
        ma.setText("a b c");
        ma.setWidth(100);
        ma.addNotify();
     }
    
    public MessageAreaTest(String testName) {
        super(testName);
    }
    
    /**
     * Test of addNotify method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MessageArea.
     *
    public void testAddNotify() {
        System.out.println("testAddNotify");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of setWidth method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MessageArea.
     *
    public void testSetWidth() {
        System.out.println("testSetWidth");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of setFont method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MessageArea.
     *
    public void testSetFont() {
        System.out.println("testSetFont");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of setForeground method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MessageArea.
     *
    public void testSetForeground() {
        System.out.println("testSetForeground");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of setText method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MessageArea.
     *
    public void testSetText() {
        System.out.println("testSetText");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of setEndText method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MessageArea.
     *
    public void testSetEndText() {
        System.out.println("testSetEndText");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of appendText method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MessageArea.
     *
    public void testAppendText() {
        System.out.println("testAppendText");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of setBulletItems method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MessageArea.
     *
    public void testSetBulletItems() {
        System.out.println("testSetBulletItems");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of appendBulletItem method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MessageArea.
     *
    public void testAppendBulletItem() {
        System.out.println("testAppendBulletItem");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of appendBulletItems method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MessageArea.
     *
    public void testAppendBulletItems() {
        System.out.println("testAppendBulletItems");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    */
    
}
