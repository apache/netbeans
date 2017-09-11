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

import java.util.HashSet;
import java.util.Set;
import junit.framework.TestCase;

/**
 *
 * @author tim
 */
public class TabStateTest extends TestCase {

    public TabStateTest(String testName) {
        super(testName);
    }

    private TS ts = null;
    public void setUp() {
        ts = new TS();
    }

/*
        public static final int TabState.REPAINT_ON_MOUSE_ENTER_TAB = 1;
        public static final int REPAINT_ALL_ON_MOUSE_ENTER_TABS_AREA = 3;
        public static final int TabState.REPAINT_ON_MOUSE_ENTER_CLOSE_BUTTON = 4;
        public static final int TabState.REPAINT_ON_MOUSE_PRESSED = 8;
        public static final int REPAINT_SELECTION_ON_ACTIVATION_CHANGE = 16;
        public static final int REPAINT_ALL_TABS_ON_ACTIVATION_CHANGE = 32; //includes selection
        public static final int TabState.REPAINT_ON_SELECTION_CHANGE = 64;
        public static final int REPAINT_ALL_TABS_ON_SELECTION_CHANGE = 192;
        public static final int TabState.REPAINT_ON_CLOSE_BUTTON_PRESSED = 256;
 */    
    
    /**
     * Test of setPressed method, of class org.netbeans.swing.tabcontrol.plaf.TabState.
     */
    public void testSetPressed() {
        System.out.println("testSetPressed");
        
        policy = 0;
        ts.setPressed (5);
        ts.assertChange("Initial set of selected index should trigger a call to change");
        ts.assertNoTabsRepainted("No tabs should have been repainted with a 0 policy, after pressing a tab");
        
        policy = TabState.REPAINT_ON_MOUSE_PRESSED;
        ts.setPressed (6);
//        ts.assertTabRepainted("With policy TabState.REPAINT_ON_MOUSE_PRESSED, pressing tab 6 should generate a repaint of only tab 6", 6);
        ts.assertChange ("Changing index from 5 to 6 should cause a call to change");
        
        policy = TabState.REPAINT_ON_MOUSE_PRESSED | TabState.REPAINT_ON_SELECTION_CHANGE | TabState.REPAINT_ON_CLOSE_BUTTON_PRESSED;
    }
    
    /**
     * Test of setContainsMouse method, of class org.netbeans.swing.tabcontrol.plaf.TabState.
     */
    public void testSetContainsMouse() {
        System.out.println("testSetContainsMouse");
        
    }
    
    /**
     * Test of setCloseButtonContainsMouse method, of class org.netbeans.swing.tabcontrol.plaf.TabState.
     */
    public void testSetCloseButtonContainsMouse() {
        System.out.println("testSetCloseButtonContainsMouse");
        
    }
    
    /**
     * Test of setMousePressedInCloseButton method, of class org.netbeans.swing.tabcontrol.plaf.TabState.
     */
    public void testSetMousePressedInCloseButton() {
        System.out.println("testSetMousePressedInCloseButton");
        
    }
    
    /**
     * Test of setSelected method, of class org.netbeans.swing.tabcontrol.plaf.TabState.
     */
    public void testSetSelected() {
        System.out.println("testSetSelected");
        
    }
    
    /**
     * Test of setMouseInTabsArea method, of class org.netbeans.swing.tabcontrol.plaf.TabState.
     */
    public void testSetMouseInTabsArea() {
        System.out.println("testSetMouseInTabsArea");
        
    }
    
    /**
     * Test of setActive method, of class org.netbeans.swing.tabcontrol.plaf.TabState.
     */
    public void testSetActive() {
        System.out.println("testSetActive");
        
    }
    
    /**
     * Test of clearTransientStates method, of class org.netbeans.swing.tabcontrol.plaf.TabState.
     */
    public void testClearTransientStates() {
        System.out.println("testClearTransientStates");
        
    }
    
    
    private class TS extends TabState {
        private int possibleChangeLastTab = Integer.MIN_VALUE;
        private int possibleChangeCurrTab = Integer.MIN_VALUE;
        private int possibleChangeType = Integer.MIN_VALUE;
        private Boolean possibleChangePrevValue = null;
        private Boolean possibleChangeCurrValue = null;
        private int possibleChangeBoolType = Integer.MIN_VALUE;

        
        private void clear() {
            possibleChangeLastTab = Integer.MIN_VALUE;
            possibleChangeCurrTab = Integer.MIN_VALUE;
            possibleChangeType = Integer.MIN_VALUE;
            possibleChangePrevValue = null;
            possibleChangeCurrValue = null;
            possibleChangeBoolType = Integer.MIN_VALUE;
        }
        
        public void assertPossibleChange (String msg) {
            int _possibleChangeLastTab = possibleChangeLastTab;
            int _possibleChangeCurrTab = possibleChangeCurrTab; 
            int _possibleChangeType = possibleChangeType;
            Boolean _possibleChangePrevValue = possibleChangePrevValue;
            Boolean _possibleChangeCurrValue = possibleChangeCurrValue;
            int _possibleChangeBoolType = possibleChangeBoolType;
            clear();
            
            if (_possibleChangeLastTab == Integer.MIN_VALUE && _possibleChangePrevValue == null) {
                fail ("no event occured - " + msg);
            }
        }
        
        public void assertPossibleChange (String msg, int lastTab, int currTab, int type) {
            int _possibleChangeLastTab = possibleChangeLastTab;
            int _possibleChangeCurrTab = possibleChangeCurrTab; 
            int _possibleChangeType = possibleChangeType;
            Boolean _possibleChangePrevValue = possibleChangePrevValue;
            Boolean _possibleChangeCurrValue = possibleChangeCurrValue;
            int _possibleChangeBoolType = possibleChangeBoolType;
            clear();
            
            if (_possibleChangeLastTab == Integer.MIN_VALUE || _possibleChangeLastTab == Integer.MIN_VALUE) {
                fail ("no event occurred - " + msg);
            }
            
            if (_possibleChangeLastTab != lastTab || _possibleChangeCurrTab != currTab || _possibleChangeType != type) {
                fail ("wrong event occured - " + msg + " lastTab " + _possibleChangeLastTab + " currTab " + _possibleChangeCurrTab + " type " + _possibleChangeType);
            }
        }
        

        public void assertPossibleChange (String msg, boolean prevVal, boolean currVal, int type) {
            int _possibleChangeLastTab = possibleChangeLastTab;
            int _possibleChangeCurrTab = possibleChangeCurrTab; 
            int _possibleChangeType = possibleChangeType;
            Boolean _possibleChangePrevValue = possibleChangePrevValue;
            Boolean _possibleChangeCurrValue = possibleChangeCurrValue;
            int _possibleChangeBoolType = possibleChangeBoolType;
            clear();
            if (_possibleChangePrevValue == null || _possibleChangeCurrValue == null) {
                fail ("no event occured - " + msg);
            }
            
            if (_possibleChangePrevValue.booleanValue() != prevVal || _possibleChangeCurrValue.booleanValue() != currVal || _possibleChangeBoolType != type) {
                fail ("wrong event occured - " + msg + " prevVal: " + _possibleChangePrevValue + " currVal " + _possibleChangeCurrValue + " type " + _possibleChangeBoolType);
            }
        }        
        
        protected void possibleChange(int lastTab, int currTab, int type) {
            possibleChangeLastTab = lastTab;
            possibleChangeCurrTab = currTab;
            possibleChangeType = type;
            super.possibleChange (lastTab, currTab, type);
        }
        
        protected void possibleChange(boolean prevVal, boolean currVal, int type) {
            possibleChangePrevValue = prevVal ? Boolean.TRUE : Boolean.FALSE;
            possibleChangeCurrValue = prevVal ? Boolean.TRUE : Boolean.FALSE;
            possibleChangeBoolType = type;
            super.possibleChange (prevVal, currVal, type);
        }
        
        private int repaintTabInt = Integer.MAX_VALUE;
        private Set repaintedTabs = null;
        
        public void assertNoTabsRepainted(String msg) {
            if (repaintedTabs != null) {
                fail ("Tabs were repainted: " + repaintedTabs);
            }
            assertAllTabsNotRepainted(msg);
        }
        
        private void assertTabRepainted (String msg, int tab) {
            if (repaintedTabs == null) {
                fail ("No tabs repainted - " + msg);
            }
            Set set = new HashSet (repaintedTabs);
            repaintedTabs = null;
            assertTrue ("Number of tabs repainted should be 1 but is " + set.size() + " - contents: " + set, set.size() == 1);
            Integer in = (Integer) set.iterator().next();
            assertTrue ("Wrong tab repainted - should be " + tab + " but is " + in + " - " + msg, in.intValue() == tab);
        }
        
        private void assertTabsRepainted (String msg, int[] tabs) {
            if (repaintedTabs == null) {
                fail ("No tabs repainted - " + msg );
            }
            Set set = new HashSet (repaintedTabs);
            repaintedTabs = null;
            for (int i=0; i < tabs.length; i++) {
                if (!set.contains(new Integer(tabs[i]))) {
                    fail (msg + " Tab " + tabs[i] + " was not repainted - repainted tabs were " + set);
                }
            }
        }
        
        protected void repaintTab(int tab) {
            if (repaintedTabs == null) {
                repaintedTabs = new HashSet();
            }
            repaintedTabs.add (new Integer(tab));
        }
        
        public void assertAllTabsRepainted(String msg) {
            Boolean b = allTabsRepainted;
            allTabsRepainted = null;
            assertTrue ("repaintAllTabs not called - " + msg, b.booleanValue());
        }
        
        public void assertAllTabsNotRepainted (String msg) {
            assertTrue ("All tabs repainted - " + msg, allTabsRepainted == null);
        }
        
        private Boolean allTabsRepainted = null;
        protected void repaintAllTabs() {
            allTabsRepainted = Boolean.TRUE;
        }
        
        private int changeLastTab = Integer.MIN_VALUE;
        private int changeCurrTab = Integer.MIN_VALUE;
        private int changeType = Integer.MIN_VALUE;
        private int typeOfChange = Integer.MIN_VALUE;
        
        public void assertChange (String msg) {
            if (changeLastTab == Integer.MIN_VALUE || changeCurrTab == Integer.MIN_VALUE || changeType == Integer.MIN_VALUE || typeOfChange == Integer.MIN_VALUE) {
                fail ("Change not called - " + msg);
            }
        }
        
        public void assertNoChange (String msg) {
            if (changeLastTab == Integer.MIN_VALUE || changeCurrTab == Integer.MIN_VALUE || changeType == Integer.MIN_VALUE || typeOfChange == Integer.MIN_VALUE) {
                
            } else {
                fail (msg);
            }
        }
        
        public void assertChange (String msg, int lastTab, int currTab) {
            assertChange (msg);
            if (lastTab != changeLastTab || currTab != changeCurrTab) {
                fail (msg + " - Wrong arguments for call to change: " + "lastTab " + changeLastTab + " currTab " + changeCurrTab);
            }
            changeLastTab = Integer.MIN_VALUE;
            changeCurrTab = Integer.MIN_VALUE;
            typeOfChange = Integer.MIN_VALUE;
            changeType = Integer.MIN_VALUE;
        }
        
        public void assertChange (String msg, int lastTab, int currTab, int type, int cType) {
            int t = changeType;
            int tc = typeOfChange;
            assertChange (msg, lastTab, currTab);
            if (type != t || tc != cType) {
                fail (msg + " - Wrong arguments for call to change: " + stateToString (t) + " - " + changeToString(cType));
            }
        }
        
        
        protected void change(int lastTab, int currTab, int type, int changeType) {
            changeLastTab = lastTab;
            changeCurrTab = currTab;
            typeOfChange = type;
            this.changeType = changeType;
            super.change (lastTab, currTab, type, changeType);
        }

        public int getRepaintPolicy(int tab) {
            return policy;
        }
    }
    
    private int policy = 0;
}
