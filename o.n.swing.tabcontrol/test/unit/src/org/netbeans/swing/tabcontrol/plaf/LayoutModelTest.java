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

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import junit.framework.TestCase;
import org.netbeans.swing.tabcontrol.DefaultTabDataModel;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDataModel;

/** Tests for all of the functionality of TabLayoutModel instances
 *
 * @author  Tim Boudreau
 */
public class LayoutModelTest extends TestCase {
    DefaultTabDataModel mdl=null;
    DefaultTabSelectionModel sel = null;
    TestLayoutModel lay = null;

    public LayoutModelTest(String testName) {
        super(testName);
    }
    
    public void setUp() {
        prepareModel();
    }
    
    Icon ic = new Icon () {
        public int getIconWidth() {
            return 16;
        }
        public int getIconHeight() {
            return 16;
        }
        public void paintIcon (Component c, Graphics g, int x, int y) {
            //do nothing
        }
    };
    
    Icon sameSizeIcon = new Icon () {
        public int getIconWidth() {
            return 16;
        }
        public int getIconHeight() {
            return 16;
        }
        public void paintIcon (Component c, Graphics g, int x, int y) {
            //do nothing
        }
    };
    
    Icon biggerIcon = new Icon () {
        public int getIconWidth() {
            return 22;
        }
        public int getIconHeight() {
            return 22;
        }
        public void paintIcon (Component c, Graphics g, int x, int y) {
            //do nothing
        }
    };    
    
    /** Weird, but this class was adapted from a standalone test written
     * long ago and rescued from cvs history.  It didn't use JUnit, and 
     * the assertTrue argument order was reversed.  So in the interest of 
     * laziness... */
    private void assertPravda (boolean val, String msg) {
        assertTrue (msg, val);
    }
    
    int padX;
    int padY;
    private void prepareModel() {
        TabData[] td = new TabData[25];
        int ct = 0;
        for (char c='a'; c < 'z'; c++) {
            char[] ch = new char[ct+1];
            Arrays.fill (ch, c);
            String name = new String (ch);
            Component comp = new JLabel(name);
            comp.setName (name);
            td[ct] = new TabData (comp, ic, name, "tip:"+name);
            ct++;
        }
        padX = 2;
        padY = 2;
        mdl = new DefaultTabDataModel (td);
        JLabel jl = new JLabel();
        jl.setBorder (BorderFactory.createEmptyBorder());
        lay = new TestLayoutModel (mdl, jl);
        lay.setPadding (new Dimension(padX, padY));
    }
    
    /*
    public void run() {
        testSizes();
        testRemoval();
        System.err.println("All tests passed for layout model");
    }
     */
    
    public void testSizes() {
        System.err.println("testSizes");
        int pos=0;
        for (int i=0; i < mdl.size(); i++) {
            int expectedSize = ic.getIconWidth() + i + padX + 1;
            assertPravda (lay.getW(i) == expectedSize, "Width of " + (i+1) + " - "
            + mdl.getTab(i).getText() + " should be " + expectedSize + " but is " 
            + lay.getW(i));
            assertPravda (pos == lay.getX(i), "X at " +  i + " should be " + pos + " but is " + lay.getX(i));
            pos += lay.getW(i);
        }
    }
    
    public void testRemoval() {
        System.err.println("testRemoval");
        mdl.removeTab (0);
        int expectedSize = ic.getIconWidth() + 2 + padX;
        assertPravda (lay.getW(0) == expectedSize, "Removed item at 0, new 0 item not correct size");
    }
    

    /** A default model subclass that uses character count for width for testing   */
    class TestLayoutModel extends BaseTabLayoutModel {
        public TestLayoutModel(TabDataModel model, JComponent target) {
            super (model, new JLabel()); 
        }        
        
        protected int textWidth (int index) {
            return model.getTab (index).getText().length();
        }
        
        protected int textHeight (int index) {
            return 16;
        }
    }
    
}
