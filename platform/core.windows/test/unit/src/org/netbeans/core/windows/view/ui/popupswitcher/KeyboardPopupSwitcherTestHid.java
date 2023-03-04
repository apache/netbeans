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

package org.netbeans.core.windows.view.ui.popupswitcher;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.UIManager;
import junit.framework.TestCase;


/**
 * Convenient IDE tester. Tests DocumentSwitcherTable. Just run and play with
 * Ctrl+Tab and Ctrl+Shift+Tab keys.
 *
 * @author mkrauskopf
 */
public class KeyboardPopupSwitcherTestHid extends TestCase
        implements KeyEventDispatcher {
    
    private JFrame frame;
    private Item[] items1 = new Item[100];
    private Item[] items2 = new Item[100];
    
    public KeyboardPopupSwitcherTestHid(String testName) {
        super(testName);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            System.err.println("Cannot set L&F: " + ex);
        }
    }
    
    protected void setUp() {
        KeyboardFocusManager keyboardFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        keyboardFocusManager.addKeyEventDispatcher(this);
        frame = createFrame();
        frame.setVisible(true);
        
        items1[0] = new DummyItem("Something.txt", new DummyIcon(Color.BLUE));
        items1[1] = new DummyItem("Sometime.txt", new DummyIcon());
        items1[2] = new DummyItem("Somewhere.txt", new DummyIcon(Color.YELLOW));
        items1[3] = new DummyItem("Something.txt", new DummyIcon(Color.BLUE));
        items1[4] = new DummyItem("Very Very Very Long" +
                " name with a lot of words in its name bla bla bla bla bla bla" +
                " which sould be shortened and should ends with three dots [...]." +
                " Hmmmmm", new DummyIcon());
        items1[5] = new DummyItem("Somewhere.txt", new DummyIcon(Color.YELLOW));
        for( int i=6; i<70; i++ )
            items1[i] = new DummyItem("s1.txt", new DummyIcon());
        items1[70] = new DummyItem("null icon", null);
        for( int i=71; i<90; i++ )
            items1[i] = new DummyItem("s1.txt", new DummyIcon());
        items1[90] = new DummyItem(null, new DummyIcon(Color.BLACK));
        for( int i=91; i<100; i++ )
            items1[i] = new DummyItem("s1.txt", new DummyIcon(Color.GREEN));
        
        items2[0] = new DummyItem("Something.txt", new DummyIcon(Color.BLUE));
        items2[1] = new DummyItem("Sometime.txt", new DummyIcon());
        items2[2] = new DummyItem("Somewhere.txt", new DummyIcon(Color.YELLOW));
        items2[3] = new DummyItem("Something.txt", new DummyIcon(Color.BLUE));
        items2[4] = new DummyItem("Very Very Very Long" +
                " name with a lot of words in its name bla bla bla bla bla bla" +
                " which sould be shortened and should ends with three dots [...]." +
                " Hmmmmm", new DummyIcon());
        items2[5] = new DummyItem("Somewhere.txt", new DummyIcon(Color.YELLOW));
        for( int i=6; i<70; i++ )
            items2[i] = new DummyItem("s1.txt", new DummyIcon());
        items2[70] = new DummyItem("null icon", null);
        for( int i=71; i<90; i++ )
            items2[i] = new DummyItem("s1.txt", new DummyIcon());
        items2[90] = new DummyItem(null, new DummyIcon(Color.BLACK));
        for( int i=91; i<100; i++ )
            items2[i] = new DummyItem("s1.txt", new DummyIcon(Color.GREEN));

        // wait until a developer close the frame
        sleepForever();
        keyboardFocusManager.removeKeyEventDispatcher(this);
    }
    
    public void testFake() {
        // needed to "run" this class
    }
    
    private JFrame createFrame() {
        JFrame frame = new JFrame(getClass().getName());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(600, 400));
        frame.setLocationRelativeTo(null);
        return frame;
    }
    
    @Override
    public boolean dispatchKeyEvent(java.awt.event.KeyEvent e) {
        boolean isCtrl = e.getModifiers() == InputEvent.CTRL_MASK;
        boolean isCtrlShift = e.getModifiers() == (InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK);
        boolean doPopup = (e.getKeyCode() == KeyEvent.VK_TAB) &&
                (isCtrl || isCtrlShift);
        if (doPopup && !KeyboardPopupSwitcher.isShown()) {
            // create popup with our SwitcherTable
            KeyboardPopupSwitcher.showPopup(new Model( items1, items2, true ), KeyEvent.VK_CONTROL, e.getKeyCode(), (e.getModifiers() & InputEvent.SHIFT_MASK)==0);
            return true;
        }
        if( KeyboardPopupSwitcher.isShown() ) {
            KeyboardPopupSwitcher.doProcessShortcut( e );
        }
        
        return false;
    }

    private static class DummyItem extends Item {

        private final Item[] subItems;
        private final Item active;

        public DummyItem( String displayName, Icon icon ) {
            super( displayName, displayName, icon, false );
            subItems = new Item[10];
            for( int i=0; i<subItems.length-1; i++ ) {
                subItems[i] = new DummySubItem( "Sub-tab " + i, icon, false );
            }
            subItems[subItems.length-1] = new DummySubItem( "Sub-tab with name so long it should be truncated.", icon, false );
            active = new DummySubItem( "Sub-tab", icon, true );
        }

        @Override
        public void activate() {
            System.err.println( "Item activated: " + getDisplayName() );
        }

        @Override
        public boolean hasSubItems() {
            return true;
        }

        @Override
        public Item[] getActivatableSubItems() {
            return subItems;
        }

        @Override
        public Item getActiveSubItem() {
            return active;
        }

        @Override
        public boolean isTopItem() {
            return true;
        }

        @Override
        public boolean isParentOf( Item subItem ) {
            if( null != active && subItem == active )
                return true;
            for( Item item : subItems ) {
                if( subItem == item )
                    return true;
            }
            return false;
        }
    }

    private static class DummySubItem extends Item {

        public DummySubItem( String displayName, Icon icon, boolean active ) {
            super( displayName, displayName, icon, active );
        }

        @Override
        public void activate() {
            System.err.println( "Sub-item activated: " + getDisplayName() );
        }

        @Override
        public boolean hasSubItems() {
            return false;
        }

        @Override
        public Item[] getActivatableSubItems() {
            return null;
        }

        @Override
        public Item getActiveSubItem() {
            return null;
        }

        @Override
        public boolean isTopItem() {
            return false;
        }

        @Override
        public boolean isParentOf( Item subItem ) {
            return false;
        }
    }
    
    /**
     * Dummy icon meant for testing prupose.
     */
    private static class DummyIcon implements Icon {
        Color color;
        private DummyIcon(Color color) {
            this.color = color;
        }
        private DummyIcon() {
            this(Color.RED);
        }
        @Override
        public int getIconWidth() {
            return 16;
        }
        @Override
        public int getIconHeight() {
            return 16;
        }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            int left = ((JComponent) c).getInsets().left;
            int top = ((JComponent) c).getInsets().top;
            g.setColor(color);
            g.fillRect(left + 2, top + 2, 12, 12);
            g.setColor(Color.BLACK);
            g.fillRect(left + 4, top + 4, 8, 8);
        }
    }
    
    private void sleep() {
        sleep(500);
    }
    
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private void sleepForever() {
        boolean dumb = true;
        while(dumb) {
            sleep();
        }
    }
}
