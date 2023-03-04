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
