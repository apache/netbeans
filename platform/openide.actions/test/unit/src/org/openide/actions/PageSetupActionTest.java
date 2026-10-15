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
package org.openide.actions; 
 
import java.awt.AWTException; 
import java.awt.Robot; 
import java.awt.event.KeyEvent; 
import junit.framework.Test; 
import junit.framework.TestSuite; 
import org.netbeans.junit.NbTestCase; 
import org.openide.util.HelpCtx; 
 
/** 
 * A JUnit test for PageSetupAction. 
 * 
 * @author Manfred Riem 
 * @version $Revision$ 
 */ 
public class PageSetupActionTest extends NbTestCase { 
    /** 
     * Stores our single instance. 
     */ 
    private static PageSetupAction instance = new PageSetupAction(); 
     
    /** 
     * Constructor. 
     * 
     * @param testName the name of the test. 
     */ 
    public PageSetupActionTest(String testName) { 
        super(testName); 
    } 
 
    /** 
     * Setup before testing. 
     */ 
    protected void setUp() throws Exception { 
    } 
 
    /** 
     * Cleanup after testing. 
     */ 
    protected void tearDown() throws Exception { 
    } 
 
    /** 
     * Suite method. 
     */ 
    public static Test suite() { 
        TestSuite suite = new TestSuite(PageSetupActionTest.class); 
         
        return suite; 
    } 
 
    /** 
     * Test performAction method. 
     */ 
    public void testPerformAction() { 
        Thread thread = new Thread(){ 
            public void run() { 
                instance.performAction(); 
            } 
        }; 
         
        thread.start(); 
 
        try { 
            Thread.sleep(1000); 
        } 
        catch(InterruptedException ie) { 
        } 
         
        try { 
            Robot robot = new Robot(); 
            robot.keyPress(KeyEvent.VK_ESCAPE); 
        } 
        catch(AWTException ae) { 
            ae.printStackTrace(); 
        } 
 
        try { 
            Thread.sleep(1000); 
        } 
        catch(InterruptedException ie) { 
        } 
    } 
 
    /** 
     * Test asynchronous method. 
     */ 
    public void testAsynchronous() { 
        boolean expected = false; 
        boolean result   = instance.asynchronous(); 
         
        assertEquals(expected, result); 
    } 
 
    /** 
     * Test getName method. 
     */ 
    public void testGetName() { 
        String expected = "Pa&ge Setup..."; 
        String result   = instance.getName(); 
         
        assertEquals(expected, result); 
    } 
 
    /** 
     * Test getHelpCtx method. 
     */ 
    public void testGetHelpCtx() { 
        HelpCtx expected = new HelpCtx(PageSetupAction.class);
        HelpCtx result   = instance.getHelpCtx(); 
         
        assertEquals(expected, result); 
    } 
} 
