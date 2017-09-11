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
package org.openide.actions; 
 
import java.awt.Component; 
import java.awt.Image; 
import java.awt.datatransfer.Transferable; 
import java.io.IOException; 
import junit.framework.Test; 
import junit.framework.TestSuite; 
import org.netbeans.junit.NbTestCase; 
import org.openide.cookies.OpenCookie; 
import org.openide.nodes.Children; 
import org.openide.nodes.Node; 
import org.openide.util.HelpCtx; 
import org.openide.util.actions.CookieAction; 
import org.openide.util.datatransfer.NewType; 
import org.openide.util.datatransfer.PasteType; 
 
/** 
 * A JUnit test for OpenAction. 
 * 
 * @author Manfred Riem 
 * @version $Revision$ 
 */ 
public class OpenActionTest extends NbTestCase { 
    /** 
     * Stores our single instance. 
     */ 
    private static OpenAction instance = new OpenAction(); 
     
    /** 
     * Constructor. 
     * 
     * @param testName the name of the test. 
     */ 
    public OpenActionTest(String testName) { 
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
        TestSuite suite = new TestSuite(OpenActionTest.class); 
         
        return suite; 
    } 
 
    /** 
     * Test cookieClasses method. 
     */ 
    public void testCookieClasses() { 
        Class[] result = instance.cookieClasses(); 
         
        assertNotNull(result); 
        assertEquals(OpenCookie.class, result[0]); 
    } 
 
    /** 
     * Test surviveFocusChange method. 
     */ 
    public void testSurviveFocusChange() { 
        boolean expected = false; 
        boolean result   = instance.surviveFocusChange(); 
         
        assertEquals(expected, result); 
    } 
 
    /** 
     * Test mode method. 
     */ 
    public void testMode() { 
        int expected = CookieAction.MODE_ANY; 
        int result   = instance.mode(); 
         
        assertEquals(expected, result); 
    } 
 
    /** 
     * Test getName method. 
     */ 
    public void testGetName() { 
        String expected = "Open"; 
        String result   = instance.getName(); 
         
        assertEquals(expected, result); 
    } 
 
    /** 
     * Test getHelpCtx method. 
     */ 
    public void testGetHelpCtx() { 
        HelpCtx expected = new HelpCtx(OpenAction.class);; 
        HelpCtx result   = instance.getHelpCtx(); 
         
        assertEquals(expected, result); 
    } 
 
    /** 
     * Test performAction method. 
     */ 
    public void testPerformAction() { 
        Node[] nodes = new Node[1]; 
         
        nodes[0] = new OpenActionTestNode(); 
         
        instance.performAction(nodes); 
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
     * Inner class for testing purposes. 
     */ 
    public class OpenActionTestNode extends Node implements OpenCookie { 
        public OpenActionTestNode() { 
            super(Children.LEAF, null); 
        } 
         
        public Node.Cookie getCookie(Class clazz) { 
            if (clazz == OpenCookie.class) { 
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
 
        public void open() { 
        } 
    } 
} 
