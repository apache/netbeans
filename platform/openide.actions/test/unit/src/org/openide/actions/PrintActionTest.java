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
package org.openide.actions; 
 
import java.awt.Component; 
import java.awt.Image; 
import java.awt.datatransfer.Transferable; 
import java.io.IOException; 
import junit.framework.Test; 
import junit.framework.TestSuite; 
import org.netbeans.junit.NbTestCase; 
import org.openide.cookies.PrintCookie; 
import org.openide.nodes.Children; 
import org.openide.nodes.Node; 
import org.openide.util.HelpCtx; 
import org.openide.util.actions.CookieAction; 
import org.openide.util.datatransfer.NewType; 
import org.openide.util.datatransfer.PasteType; 
 
/** 
 * A JUnit test for PrintAction. 
 * 
 * @author Manfred Riem 
 * @version $Revision$ 
 */ 
public class PrintActionTest extends NbTestCase { 
    /** 
     * Stores the instance. 
     */ 
    private static PrintAction instance = new PrintAction(); 
     
    /** 
     * Constructor. 
     * 
     * @param testName the name of the test. 
     */ 
    public PrintActionTest(String testName) { 
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
     * Test cookieClasses method. 
     */ 
    public void testCookieClasses() { 
        Class[] result = instance.cookieClasses(); 
         
        assertNotNull(result); 
        assertEquals(PrintCookie.class, result[0]); 
    } 
     
    /** 
     * Test performAction method. 
     */ 
    public void testPerformAction() { 
        Node[] nodes = new Node[1]; 
          
        nodes[0] = new PrintActionTestNode(); 
        instance.performAction(nodes); 
    } 
     
    /** 
     * Test asynchronous method. 
     */ 
    public void testAsynchronous() { 
        boolean expected = true; 
        boolean result   = instance.asynchronous(); 
         
        assertEquals(expected, result); 
    } 
     
    /** 
     * Test mode method. 
     */ 
    public void testMode() { 
        int expected = CookieAction.MODE_EXACTLY_ONE; 
        int result   = instance.mode(); 
         
        assertEquals(expected, result); 
    } 
     
    /** 
     * Test getName method. 
     */ 
    public void testGetName() { 
        String expected = "&Print..."; 
        String result   = instance.getName(); 
         
        assertEquals(expected, result); 
    } 
     
    /** 
     * Test getHelpCtx method. 
     */ 
    public void testGetHelpCtx() { 
        HelpCtx expected = new HelpCtx(PrintAction.class); 
        HelpCtx result   = instance.getHelpCtx(); 
         
        assertEquals(expected, result); 
    } 
     
    /** 
     * Test iconResource method. 
     */ 
    public void testIconResource() { 
        String expected = "org/openide/resources/actions/print.png"; 
        String result   = instance.iconResource(); 
         
        assertEquals(expected, result); 
    } 
     
    /** 
     * Inner class needed for testing. 
     */ 
    public class PrintActionTestNode extends Node implements PrintCookie { 
        public PrintActionTestNode() { 
            super(Children.LEAF, null); 
        } 
         
        public Node.Cookie getCookie(Class clazz) { 
            if (clazz == PrintCookie.class) { 
                return this; 
            } 
            return null; 
        } 
         
        public Node cloneNode() { 
            return null; 
        } 
 
        public Image getIcon(int type) { 
            return null; 
        } 
 
        public Image getOpenedIcon(int type) { 
            return null; 
        } 
 
        public HelpCtx getHelpCtx() { 
            return null; 
        } 
 
        public boolean canRename() { 
            return false; 
        } 
 
        public boolean canDestroy() { 
            return false; 
        } 
 
        public Node.PropertySet[] getPropertySets() { 
            return null; 
        } 
 
        public Transferable clipboardCopy() throws IOException { 
            return null; 
        } 
 
        public Transferable clipboardCut() throws IOException { 
            return null; 
        } 
 
        public Transferable drag() throws IOException { 
            return null; 
        } 
 
        public boolean canCopy() { 
            return false; 
        } 
 
        public boolean canCut() { 
            return false; 
        } 
 
        public PasteType[] getPasteTypes(Transferable t) { 
            return null; 
        } 
 
        public PasteType getDropType(Transferable t, int action, int index) { 
            return null; 
        } 
 
        public NewType[] getNewTypes() { 
            return null; 
        } 
 
        public boolean hasCustomizer() { 
            return false; 
        } 
 
        public Component getCustomizer() { 
            return null; 
        } 
 
        public Node.Handle getHandle() { 
            return null; 
        } 
 
        public void print() { 
        } 
    } 
} 
