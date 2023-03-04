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

package org.openide.awt;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 * Tests for the Actions class.
 * @author David Strupl
 */
public class ActionsTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(ActionsTest.class);
    }

    static {
        MockServices.setServices(TestConnector.class);
        assertFalse("Initialized Actions class outside of AWT thread", EventQueue.isDispatchThread());
        Actions.cutAmpersand("None");
    }
    
    // colors of the testing images in this order:
    // (test recognizes the icon by the white/black colors in specified positions :-)))
    //  FIRST EIGHT (original)         SECOND EIGHT (selected)
    // 0 testIcon.gif                 8 testIcon_selected.gif
    // 1 testIcon_rollover.gif        9 testIcon_rolloverSelected.gif
    // 2 testIcon_pressed.gif        10      --not-used--
    // 3 testIcon_disabled.gif       11 testIcon_disabledSelected.gif
    // 4 testIcon24.gif              12 testIcon24_selected.gif
    // 5 testIcon24_rollover.gif     13 testIcon24_rolloverSelected.gif
    // 6 testIcon24_pressed.gif      14      --not-used--
    // 7 testIcon24_disabled.gif     15 testIcon24_disabledSelected.gif
    private static int[][] RESULT_COLORS_00 = {
        {255, 255, 255},
        {0, 0, 0},
        {255, 255, 255},
        {0, 0, 0},
        {255, 255, 255},
        {0, 0, 0},
        {255, 255, 255},
        {0, 0, 0},
        {255, 255, 255},
        {0, 0, 0},
        {255, 255, 255},
        {0, 0, 0},
        {255, 255, 255},
        {0, 0, 0},
        {255, 255, 255},
        {0, 0, 0},
    };
    private static int[][] RESULT_COLORS_01 = {
        {255, 255, 255},
        {255, 255, 255},
        {0, 0, 0},
        {0, 0, 0},
        {255, 255, 255},
        {255, 255, 255},
        {0, 0, 0},
        {0, 0, 0},
        {255, 255, 255},
        {255, 255, 255},
        {0, 0, 0},
        {0, 0, 0},
        {255, 255, 255},
        {255, 255, 255},
        {0, 0, 0},
        {0, 0, 0},
    };
    private static int[][] RESULT_COLORS_11 = {
        {255, 255, 255},
        {255, 255, 255},
        {255, 255, 255},
        {255, 255, 255},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {255, 255, 255},
        {255, 255, 255},
        {255, 255, 255},
        {255, 255, 255},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
    };
    private static int[][] RESULT_COLORS_10 = {
        {255, 255, 255},
        {255, 255, 255},
        {255, 255, 255},
        {255, 255, 255},
        {255, 255, 255},
        {255, 255, 255},
        {255, 255, 255},
        {255, 255, 255},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
    };
    
    
    public ActionsTest(String name) {
        super(name);
    }
    
    /**
     * Test whether pressed, rollover and disabled icons
     * work for javax.swing.Action.
     */
    public void testIconsAction() throws Exception {
        JButton jb = new JButton();
        Actions.connect(jb, new TestAction());

        Icon icon = jb.getIcon();
        assertNotNull(icon);
        checkIfLoadedCorrectIcon(icon, jb, 0, "Enabled icon");

        Icon rolloverIcon = jb.getRolloverIcon();
        assertNotNull(rolloverIcon);
        checkIfLoadedCorrectIcon(rolloverIcon, jb, 1, "Rollover icon");

        Icon pressedIcon = jb.getPressedIcon();
        assertNotNull(pressedIcon);
        checkIfLoadedCorrectIcon(pressedIcon, jb, 2, "Pressed icon");

        Icon disabledIcon = jb.getDisabledIcon();
        assertNotNull(disabledIcon);
        checkIfLoadedCorrectIcon(disabledIcon, jb, 3, "Disabled icon");
        
        Icon selectedIcon = jb.getSelectedIcon();
        assertNotNull(selectedIcon);
        checkIfLoadedCorrectIcon(selectedIcon, jb, 8, "Selected icon");
        
        Icon rolloverSelectedIcon = jb.getRolloverSelectedIcon();
        assertNotNull(rolloverSelectedIcon);
        checkIfLoadedCorrectIcon(rolloverSelectedIcon, jb, 9, "RolloverSelected icon");

        // no pressedSelected
        
        Icon disabledSelectedIcon = jb.getDisabledSelectedIcon();
        assertNotNull(disabledSelectedIcon);
        checkIfLoadedCorrectIcon(disabledSelectedIcon, jb, 11, "DisabledSelected icon");
    }
    
    /**
     * Test whether pressed, rollover and disabled icons
     * work for SystemAction.
     */
    public void testIconsSystemAction() throws Exception {
        Action saInstance = SystemAction.get(TestSystemAction.class);
        
        JButton jb = new JButton();
        Actions.connect(jb, saInstance);
        
        Icon icon = jb.getIcon();
        assertNotNull(icon);
        checkIfLoadedCorrectIcon(icon, jb, 0, "Enabled icon");
        
        Icon rolloverIcon = jb.getRolloverIcon();
        assertNotNull(rolloverIcon);
        checkIfLoadedCorrectIcon(rolloverIcon, jb, 1, "Rollover icon");
        
        Icon pressedIcon = jb.getPressedIcon();
        assertNotNull(pressedIcon);
        checkIfLoadedCorrectIcon(pressedIcon, jb, 2, "Pressed icon");
        
        Icon disabledIcon = jb.getDisabledIcon();
        assertNotNull(disabledIcon);
        checkIfLoadedCorrectIcon(disabledIcon, jb, 3, "Disabled icon");
    }
    
    /**
     * Test whether pressed, rollover and disabled 24x24 icons
     * work for javax.swing.Action.
     */
    public void testIconsAction24() throws Exception {
        JButton jb = new JButton();
        jb.putClientProperty("PreferredIconSize",new Integer(24));
        Actions.connect(jb, new TestAction());
        
        Icon icon = jb.getIcon();
        assertNotNull(icon);
        checkIfLoadedCorrectIcon(icon, jb, 4, "Enabled icon24");
        
        Icon rolloverIcon = jb.getRolloverIcon();
        assertNotNull(rolloverIcon);
        checkIfLoadedCorrectIcon(rolloverIcon, jb, 5, "Rollover icon24");
        
        Icon pressedIcon = jb.getPressedIcon();
        assertNotNull(pressedIcon);
        checkIfLoadedCorrectIcon(pressedIcon, jb, 6, "Pressed icon24");
        
        Icon disabledIcon = jb.getDisabledIcon();
        assertNotNull(disabledIcon);
        checkIfLoadedCorrectIcon(disabledIcon, jb, 7, "Disabled icon24");

        Icon selectedIcon = jb.getSelectedIcon();
        assertNotNull(selectedIcon);
        checkIfLoadedCorrectIcon(selectedIcon, jb, 12, "Selected icon24");

        Icon rolloverSelectedIcon = jb.getRolloverSelectedIcon();
        assertNotNull(rolloverSelectedIcon);
        checkIfLoadedCorrectIcon(rolloverSelectedIcon, jb, 13, "RolloverSelected icon24");

        // no pressedSelected

        Icon disabledSelectedIcon = jb.getDisabledSelectedIcon();
        assertNotNull(disabledSelectedIcon);
        checkIfLoadedCorrectIcon(disabledSelectedIcon, jb, 15, "DisabledSelected icon24");
    }

    /**
     * Tests that "unknownIcon" is used if no iconBase
     */
    public void testToggleButtonUnknownIcon() throws Exception {
        AbstractButton b = new AlwaysEnabledAction.DefaultIconToggleButton();
        Action action = new TestAction();
        action.putValue("iconBase", null);
        Actions.connect(b, action);
        Icon icon = b.getIcon();
        assertNotNull("null ToggleButton icon", icon);
        Icon expectedIcon = ImageUtilities.loadImageIcon("org/openide/awt/resources/unknown.gif", false); //NOI18N
        assertEquals("unkownIcon not used", expectedIcon, icon);
    }
    
    /**
     * #47527
     * Tests if "noIconInMenu" really will NOT push the icon from the action
     * to the menu item.
     */
    public void testNoIconInMenu() throws Exception {
        JMenuItem item = new JMenuItem();
        item.setIcon(null);
        Actions.connect(item, new TestNoMenuIconAction(), false);
        assertNull(item.getIcon());
    }
    
    /**
     * Test whether pressed, rollover and disabled 24x24 icons
     * work for SystemAction.
     */
    public void testIconsSystemAction24() throws Exception {
        Action saInstance = SystemAction.get(TestSystemAction.class);
        
        JButton jb = new JButton();
        jb.putClientProperty("PreferredIconSize",new Integer(24));
        Actions.connect(jb, saInstance);
        
        Icon icon = jb.getIcon();
        assertNotNull(icon);
        checkIfLoadedCorrectIcon(icon, jb, 4, "Enabled icon");
        
        Icon rolloverIcon = jb.getRolloverIcon();
        assertNotNull(rolloverIcon);
        checkIfLoadedCorrectIcon(rolloverIcon, jb, 5, "Rollover icon");
        
        Icon pressedIcon = jb.getPressedIcon();
        assertNotNull(pressedIcon);
        checkIfLoadedCorrectIcon(pressedIcon, jb, 6, "Pressed icon");
        
        Icon disabledIcon = jb.getDisabledIcon();
        assertNotNull(disabledIcon);
        checkIfLoadedCorrectIcon(disabledIcon, jb, 7, "Disabled icon");
    }
    
    /**
     * Tests if changes in accelerator key or name of the action does not change the tooltip
     * of the button if the action has a custom tooltip. See first part of #57974.
     */
    public void testTooltipsArePersistent() throws Exception {
        Action action = new ActionsTest.TestActionWithTooltip();
        JButton button = new JButton();
        
        Actions.connect(button, action);
        
        JFrame f = new JFrame();
        
        f.getContentPane().add(button);
        f.setVisible(true);
        
        assertTrue(button.getToolTipText().equals(TestActionWithTooltip.TOOLTIP));
        
        action.putValue(Action.NAME, "new-name");
        
        assertTrue(button.getToolTipText().equals(TestActionWithTooltip.TOOLTIP));
        
        action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('a'));
        
        assertTrue(button.getToolTipText().indexOf(TestActionWithTooltip.TOOLTIP) != (-1));
        
        f.setVisible(false);
    }
    
    /**
     * Tests if the tooltip is made out of the NAME if there is not tooltip set for an action.
     * See also #57974.
     */
    public void testTooltipsIsBuiltFromNameIfNoTooltip() throws Exception {
        Action action = new ActionsTest.TestAction();
        JButton button = new JButton();
        
        Actions.connect(button, action);
        
        JFrame f = new JFrame();
        
        f.getContentPane().add(button);
        f.setVisible(true);
        
        assertTrue(button.getToolTipText().equals("test"));
        
        action.putValue(Action.NAME, "new-name");
        
        assertTrue(button.getToolTipText().equals("new-name"));
        
        action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('a'));
        
        assertTrue(button.getToolTipText().indexOf("new-name") != (-1));
        
        f.setVisible(false);
    }
    
    /**
     * Tests if the accelerator key is shown in the button's tooltip for actions with
     * custom tooltips.
     */
    public void testTooltipsContainAccelerator() throws Exception {
        Action action = new ActionsTest.TestActionWithTooltip();
        JButton button = new JButton();
        
        Actions.connect(button, action);
        
        JFrame f = new JFrame();
        
        f.getContentPane().add(button);
        f.setVisible(true);
        
        assertTrue(button.getToolTipText().equals(TestActionWithTooltip.TOOLTIP));
        
        action.putValue(Action.NAME, "new-name");
        
        assertTrue(button.getToolTipText().equals(TestActionWithTooltip.TOOLTIP));
        
        action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK));

        assertTrue(button.getToolTipText().contains("Ctrl+C"));
        
        action.putValue(Action.SHORT_DESCRIPTION, null);
        
        assertTrue(button.getToolTipText().contains("Ctrl+C"));
        
        f.setVisible(false);
    }

    /**
     * Tests whether the ButtonActionConnector is being called. The testing
     * implementation is set to "active" only for this test - so the other
     * tests should retain the behaviour like running without the
     * ButtonActionConnector.
     */
    public void testButtonActionConnector() throws Exception {
        TestConnector tc = Lookup.getDefault().lookup(TestConnector.class);
        tc.setActive(true);
        Action action = new ActionsTest.TestAction();
        JButton button = new JButton();
        Actions.connect(button, action);
        assertEquals(1, tc.getConnectCalled());
        JMenuItem jmi = new JMenuItem();
        Actions.connect(jmi, action, false);
        assertEquals(3, tc.getConnectCalled());
        tc.setActive(false);
    }
    
    public void testPopupTextIsTaken() throws Exception {
        Action action = new ActionsTest.TestAction();
        JMenuItem item = new JMenuItem();
        JMenu jmenu = new JMenu();
        jmenu.addNotify();
        assertTrue("Peer created", jmenu.isDisplayable());
        jmenu.getPopupMenu().addNotify();
        assertTrue("Peer for popup", jmenu.getPopupMenu().isDisplayable());

        action.putValue("popupText", "&Ahoj");
        action.putValue("menuText", "&Ble");
        action.putValue(action.NAME, "&Mle");
        
        Actions.connect(item, action, true);
        
        assertEquals(Utilities.isMac() ? 0 : 'A', item.getMnemonic());
        assertEquals("Ahoj", item.getText());
    }

    public void testMenuTextIsTaken() throws Exception {
        Action action = new ActionsTest.TestAction();
        JMenuItem item = new JMenuItem();
        JMenu jmenu = new JMenu();
        jmenu.addNotify();
        assertTrue("Peer created", jmenu.isDisplayable());
        jmenu.getPopupMenu().addNotify();
        assertTrue("Peer for popup", jmenu.getPopupMenu().isDisplayable());

        //action.putValue("popupText", "&Ahoj");
        action.putValue("menuText", "&Ble");
        action.putValue(action.NAME, "&Mle");
        
        Actions.connect(item, action, false);
        
        assertEquals(Utilities.isMac() ? 0 : 'B', item.getMnemonic());
        assertEquals("Ble", item.getText());
    }
    
    public void testActionNameIsTaken() throws Exception {
        Action action = new ActionsTest.TestAction();
        JMenuItem item = new JMenuItem();
        JMenu jmenu = new JMenu();
        jmenu.addNotify();
        assertTrue("Peer created", jmenu.isDisplayable());
        jmenu.getPopupMenu().addNotify();
        assertTrue("Peer for popup", jmenu.getPopupMenu().isDisplayable());

        //action.putValue("popupText", "&Ahoj");
        //action.putValue("menuText", "&Ble");
        action.putValue(action.NAME, "&Mle");
        
        Actions.connect(item, action, false);
        
        assertEquals(Utilities.isMac() ? 0 : 'M', item.getMnemonic());
        assertEquals("Mle", item.getText());
    }

    public void testPopupMnemonics() throws Exception { // #83952
        Action a = new ActionsTest.TestAction();
        a.putValue(Action.NAME, "&Ahoj");
        JMenuItem i = new JMenuItem();
        Actions.connect(i, a, false);
        assertEquals("use defined mnemonic when a menu item", Utilities.isMac() ? 0 : 'A', i.getMnemonic());
        assertEquals("Ahoj", i.getText());
        a.putValue("menuText", "&Boj");
        i = new JMenuItem();
        Actions.connect(i, a, false);
        assertEquals("use menuText when appropriate", Utilities.isMac() ? 0 : 'B', i.getMnemonic());
        assertEquals("Boj", i.getText());
        i = new JMenuItem();
        Actions.connect(i, a, true);
        assertEquals("popups normally do not use mnemonics", 0, i.getMnemonic());
        assertEquals("Boj", i.getText());
        a.putValue("popupText", "&Ciao");
        i = new JMenuItem();
        Actions.connect(i, a, true);
        assertEquals("but this can be overridden using popupText", Utilities.isMac() ? 0 : 'C', i.getMnemonic());
        assertEquals("Ciao", i.getText());
    }

    public void testForID() throws Exception {
        // most positive usages already covered by ActionProcessorTest, just check IAEs
        assertNull(Actions.forID("Sub/Category", "whatever"));
        assertNull(Actions.forID("Stuff", "this.is.an.ID"));
        try {
            Actions.forID("Actions/Stuff", "ok");
            fail();
        } catch (IllegalArgumentException x) {/* OK */}
        try {
            Actions.forID("Stuff", "not-an-id");
            fail();
        } catch (IllegalArgumentException x) {/* OK */}
    }
    
    public void testCheckPrioritiesOfIcons() {
        AbstractAction aa = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        Icon icon = ImageUtilities.loadImageIcon("org/openide/awt/TestIcon_big.png", true);
        aa.putValue(Action.SMALL_ICON, icon);
        aa.putValue("iconBase", "org/openide/awt/data/testIcon.gif");
        
        JButton b = new JButton();
        Actions.connect(b, aa);
        
        JMenuItem m = new JMenuItem();
        Actions.connect(m, aa, false);
        
        
        assertSame("Using the same icon (small" + icon, b.getIcon(), m.getIcon());
    }

    public void testCheckPrioritiesOfIconsWithStringSmallIcon() {
        AbstractAction aa = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        Object icon = "org/openide/awt/TestIcon_big.png";
        aa.putValue(Action.SMALL_ICON, icon);
        aa.putValue("iconBase", "org/openide/awt/data/testIcon.gif");
        
        JButton b = new JButton();
        Actions.connect(b, aa);
        
        JMenuItem m = new JMenuItem();
        Actions.connect(m, aa, false);
        
        
        assertSame("Using the same icon (small" + icon, b.getIcon(), m.getIcon());
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    private void checkIfLoadedCorrectIcon(Icon icon, Component c, int rowToCheck, String nameOfIcon) {
        checkIfIconOk(icon, c, 0, 0, RESULT_COLORS_00[rowToCheck], nameOfIcon);
        checkIfIconOk(icon, c, 0, 1, RESULT_COLORS_01[rowToCheck], nameOfIcon);
        checkIfIconOk(icon, c, 1, 1, RESULT_COLORS_11[rowToCheck], nameOfIcon);
        checkIfIconOk(icon, c, 1, 0, RESULT_COLORS_10[rowToCheck], nameOfIcon);
    }
    
    /**
     * Checks colors on coordinates X,Y of the icon and compares them
     * to expectedResult.
     */
    private void checkIfIconOk(Icon icon, Component c, int pixelX, int pixelY, int[] expectedResult, String nameOfIcon) {
        BufferedImage bufImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        icon.paintIcon(c, bufImg.getGraphics(), 0, 0);
        int[] res = bufImg.getData().getPixel(pixelX, pixelY, (int[])null);
        log("Icon height is " + icon.getIconHeight());
        log("Icon width is " + icon.getIconWidth());
        for (int i = 0; i < res.length; i++) {
            // Huh, Ugly hack. the sparc returns a fuzzy values +/- 1 unit e.g. 254 for Black instead of 255 as other OSs do
            // this hack doesn't broken the functionality which should testing
            assertTrue(nameOfIcon + ": Color of the ["+pixelX+","+pixelY+"] pixel is " + res[i] + ", expected was " + expectedResult[i], Math.abs(res[i] - expectedResult[i]) < 10);
        }
    }
    
    private static final class TestSystemAction extends SystemAction {
        
        public void actionPerformed(ActionEvent e) {
        }
        
        public HelpCtx getHelpCtx() {
            return null;
        }
        
        public String getName() {
            return "TestSystemAction";
        }
        
        protected String iconResource() {
            return "org/openide/awt/data/testIcon.gif";
        }
        
    }
    
    private static final class TestAction extends AbstractAction {
        
        public TestAction() {
            putValue("iconBase", "org/openide/awt/data/testIcon.gif");
            putValue(NAME, "test");
        }
        
        public void actionPerformed(ActionEvent e) {
        }
        
    }
    
    private static final class TestNoMenuIconAction extends AbstractAction {
        
        public TestNoMenuIconAction() {
            putValue("iconBase", "org/openide/awt/data/testIcon.gif");
            putValue("noIconInMenu", Boolean.TRUE);
        }
        
        public void actionPerformed(ActionEvent e) {
        }
        
    }
    
    private static final class TestActionWithTooltip extends AbstractAction {
        
        private static String TOOLTIP = "tooltip";
        
        public TestActionWithTooltip() {
            putValue(NAME, "name");
            putValue(SHORT_DESCRIPTION, TOOLTIP);
        }
        
        public void actionPerformed(ActionEvent e) {
        }
        
    }
    
    public static final class TestConnector implements Actions.ButtonActionConnector {
        
        private int called = 0;
        private boolean active = false;
        
        public TestConnector() {
            assertFalse("Don't initialize while calling connect on AWT dispatch thread", EventQueue.isDispatchThread());
        }
        
        public boolean connect(AbstractButton button, Action action) {
            if (!active) {
                return false;
            }
            called +=1;
            return true;
        }

        public boolean connect(JMenuItem item, Action action, boolean popup) {
            if (!active) {
                return false;
            }
            called += 2;
            return true;
        }
        
        public int getConnectCalled() {
            return called;
        }
        public void setActive(boolean a) {
            called = 0;
            active = a;
        }
    }
    
}
