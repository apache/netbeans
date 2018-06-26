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

package org.netbeans.modules.j2ee.sun.ide.editors.ui;

import java.awt.Font;
import junit.framework.TestCase;

/**
 *
 * @author vkraemer
 */
public class MultiLineFieldTest extends TestCase {

    public void testCoverage() {
        MultiLineField mlf = new MultiLineField();
        mlf = new MultiLineField("a\nb");
        mlf.useMonospacedFont(true);
        mlf.setFont(new Font("Serif",Font.BOLD,32));
        mlf.addNotify();
        mlf.setText("c d");
        mlf.setText(new String[] { "abc","123" });
        mlf.setError(true);
        mlf.setBounds(0,0, 300, 100);
        mlf.getAccessibleContext();
        mlf.getMinimumSize();
        mlf.getPreferredSize();
        mlf.getText();
    
    }
    
    public MultiLineFieldTest(String testName) {
        super(testName);
    }
    
    /**
     * Test of setInsets method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MultiLineField.
     *
    public void testSetInsets() {
        System.out.println("testSetInsets");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of useMonospacedFont method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MultiLineField.
     *
    public void testUseMonospacedFont() {
        System.out.println("testUseMonospacedFont");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of addNotify method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MultiLineField.
     *
    public void testAddNotify() {
        System.out.println("testAddNotify");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of getPreferredSize method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MultiLineField.
     *
    public void testGetPreferredSize() {
        System.out.println("testGetPreferredSize");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of getMinimumSize method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MultiLineField.
     *
    public void testGetMinimumSize() {
        System.out.println("testGetMinimumSize");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of setSize method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MultiLineField.
     *
    public void testSetSize() {
        System.out.println("testSetSize");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of setBounds method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MultiLineField.
     *
    public void testSetBounds() {
        System.out.println("testSetBounds");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of paint method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MultiLineField.
     *
    public void testPaint() {
        System.out.println("testPaint");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of setText method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MultiLineField.
     *
    public void testSetText() {
        System.out.println("testSetText");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of getText method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MultiLineField.
     *
    public void testGetText() {
        System.out.println("testGetText");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of setError method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MultiLineField.
     *
    public void testSetError() {
        System.out.println("testSetError");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of getAccessibleContext method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MultiLineField.
     *
    public void testGetAccessibleContext() {
        System.out.println("testGetAccessibleContext");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    // TODO add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    */
    
}
