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

package org.netbeans.core.windows.view.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.plaf.UIResource;
import org.netbeans.core.windows.actions.MaximizeWindowAction;
import org.openide.awt.CloseButtonFactory;
import org.openide.awt.TabbedPaneFactory;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * Copy of original CloseButtonTabbedPane from the NetBeans 3.4 winsys.  Old code never dies.
 * (moved from openide.awt module)
 *
 * @author Tran Duc Trung
 * @author S. Aubrecht
 * @since 2.52
 *
 */
final class CloseButtonTabbedPane extends JTabbedPane implements PropertyChangeListener {
    
    private Action scrollLeftAction;
    private Action scrollRightAction;
    
    private static final boolean IS_AQUA_LAF = "Aqua".equals( UIManager.getLookAndFeel().getID() ); //NOI18N

    CloseButtonTabbedPane() {
            // close tab via middle button
            addMouseListener(new MouseAdapter() {
                // Tab index at the time of the previous two mouse presses.
                private int lastTwoIdx[] = new int[] {-1, -1};
                // Tab index of an ongoing middle mouse button press.
                private int ongoingMiddleIdx = -1;

                @Override
                public void mousePressed(MouseEvent e) {
                    int idx =
                        getUI().tabForCoordinate(CloseButtonTabbedPane.this, e.getX(), e.getY());
                    lastTwoIdx = new int[] { idx, lastTwoIdx[0] };
                    if (SwingUtilities.isMiddleMouseButton(e)) {
                        ongoingMiddleIdx = idx;
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (SwingUtilities.isMiddleMouseButton(e)) {
                        int idx = getUI().tabForCoordinate(CloseButtonTabbedPane.this, e.getX(), e.getY());
                        if (idx >= 0) {
                            Component comp = getComponentAt(idx);
                            if (idx == ongoingMiddleIdx && comp != null && !hideCloseButton(comp)) {
                                fireCloseRequest(comp);
                            }
                        }
                        ongoingMiddleIdx = -1;
                    }
                }

            @Override
            public void mouseClicked( MouseEvent e ) {
                if( e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton( e ) ) {
                    /* Fix for bug #268681. Avoid maximizing if the user is simply clicking the
                    "left" or "right" tab-switching buttons quickly. By the time the double click
                    event is delivered, the initial press or click of the left or right buttons may
                    already have changed which tab button is currently under the mouse cursor, so
                    instead of trying to detect whether the button pressed is a left/right button,
                    perform the maximization action only if the same tab button was showing under
                    the mouse cursor for both of the button presses involved in the double click. */
                    if (!(lastTwoIdx[0] >= 0 && lastTwoIdx[0] == lastTwoIdx[1]))
                      return;
                    //toggle maximize
                    TopComponent tc = ( TopComponent ) SwingUtilities.getAncestorOfClass( TopComponent.class, CloseButtonTabbedPane.this );
                    if( null != tc ) {
                        MaximizeWindowAction mwa = new MaximizeWindowAction(tc);
                        if( mwa.isEnabled() )
                            mwa.actionPerformed(null);
                    }
                }
            }

            });
            
            //mouse wheel scrolling
            addMouseWheelListener(new MouseWheelListener() {
                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    if( e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL ) {
                        if( e.getWheelRotation() < 0 ) {
                            scrollTabsLeft();
                        } else {
                            scrollTabsRight();
                        }
                    }
                }
            });
        //Bugfix #28263: Disable focus.
        setFocusable(false);
        setFocusCycleRoot(true);
        setFocusTraversalPolicy(new CBTPPolicy());
        setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    private Component sel() {
        Component c = getSelectedComponent();
        return c == null ? this : c;
    }

    private class CBTPPolicy extends FocusTraversalPolicy {
        @Override
        public Component getComponentAfter(Container aContainer, Component aComponent) {
            return sel();
        }

        @Override
        public Component getComponentBefore(Container aContainer, Component aComponent) {
            return sel();
        }

        @Override
        public Component getFirstComponent(Container aContainer) {
            return sel();
        }

        @Override
        public Component getLastComponent(Container aContainer) {
            return sel();
        }

        @Override
        public Component getDefaultComponent(Container aContainer) {
            return sel();
        }
    }

    private int pressedCloseButtonIndex = -1;
    private int mouseOverCloseButtonIndex = -1;

    @Override
    public void insertTab(String title, Icon icon, Component component, String tip, int index) {
        super.insertTab(title, icon, component, tip, index);
            component.addPropertyChangeListener(TabbedPaneFactory.NO_CLOSE_BUTTON, this);
            if (!hideCloseButton(component)) {
                setTabComponentAt(index, new ButtonTab());
            }
        if (title != null) {
            setTitleAt(index, title);
        }
        validate();
    }

    @Override
    public void updateUI() {
        super.updateUI();
        //#179323 - disable ctrl+page up/down actions if there's just one tab,
        //parent container, e.g. TopComponent tabs, may want to handle these keys itself
        ActionMap am = getActionMap();
        Action a = am.get("navigatePageUp");
        if( null != a && !(a instanceof MyNavigateAction) ) {
            am.put("navigatePageUp", new MyNavigateAction(a));
        }
        a = am.get("navigatePageDown");
        if( null != a && !(a instanceof MyNavigateAction) ) {
            am.put("navigatePageDown", new MyNavigateAction(a));
        }
        scrollRightAction = am.get("scrollTabsForwardAction"); //NOI18N
        scrollLeftAction = am.get("scrollTabsBackwardAction"); //NOI18N
    }
    
    private void scrollTabsLeft() {
        if( IS_AQUA_LAF ) {
            int selIndex = getSelectedIndex();
            if( selIndex > 0 ) {
                setSelectedIndex(selIndex-1);
            }
        } else if( null != scrollLeftAction && scrollLeftAction.isEnabled() ) {
            scrollLeftAction.actionPerformed(new ActionEvent(this, 0, "")); //NOI18N
        }
    }
    
    private void scrollTabsRight() {
        if( IS_AQUA_LAF ) {
            int selIndex = getSelectedIndex();
            if( selIndex < getTabCount()-1 ) {
                setSelectedIndex(selIndex+1);
            }
        } else if( null != scrollRightAction && scrollRightAction.isEnabled() ) {
            try {
                scrollRightAction.actionPerformed(new ActionEvent(this, 0, "")); //NOI18N
            } catch( ArrayIndexOutOfBoundsException aioobE ) {
                //#248255
                //We're using private implementation detail of BasicTabbedPaneUI here and
                //the default implementation allows scrolling through buttons only.
                //The logic to enable/disable right scrolling is divided into two parts -
                //one part disabled the button, the other part disables the action.
                //When scrolling through the action directly the part that disables the button
                //never gets called so we're getting an exception like this.
                invalidate();
                Logger.getAnonymousLogger().log(Level.INFO, null, aioobE);
            }
        }
    }
    
    @Override
    public void removeTabAt(int index) {
        Component c = getComponentAt(index);
        c.removePropertyChangeListener(TabbedPaneFactory.NO_CLOSE_BUTTON, this);
        super.removeTabAt(index);
    }

    private static final boolean HTML_TABS_BROKEN = htmlTabsBroken();
    private static boolean htmlTabsBroken() {
        String version = System.getProperty("java.version");
        for (int i = 14; i < 18; i++) {
            if (version.startsWith("1.6.0_" + i)) {
                return true;
            }
        }
        if( version.startsWith("1.6.0") && IS_AQUA_LAF )
            return true;
        return false;
    }
    private final Pattern removeHtmlTags = HTML_TABS_BROKEN ? Pattern.compile("\\<.*?\\>") : null;

    @Override
    public void setTitleAt(int idx, String title) {
        if (title == null) {
            super.setTitleAt(idx, null);
            return;
        }
        // workaround for JDK bug (http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6670274)
        // NB issue #113388
        if (removeHtmlTags != null && title.startsWith("<html>")) {
            title = removeHtmlTags.matcher(title).replaceAll("");
            title = title.replace("&nbsp;", "");
        }

        super.setTitleAt(idx, title);
    }

    private Component findTabAt(int index) {
        int componentIndex = -1;
        for( Component c : getComponents() ) {
            if( c instanceof UIResource )
                continue;
            if( ++componentIndex == index )
                return c;
        }
        return null;
    }

    private boolean hideCloseButton(Component c) {
        if (c instanceof JComponent) {
            Object prop = ((JComponent) c).getClientProperty(TabbedPaneFactory.NO_CLOSE_BUTTON);
            if (prop instanceof Boolean && (Boolean) prop) {
                return true;
            }
        }
        return false;
    }

    private Rectangle getCloseButtonBoundsAt(int i) {
        Component c = findTabAt(i);
        //if NO_CLOSE_BUTTON -> return null
        if (hideCloseButton(c)) {
            return null;
        }
        Rectangle b = getBoundsAt(i);
        if (b == null)
            return null;
        else {
            b = new Rectangle(b);
            fixGetBoundsAt(b);

            Dimension tabsz = getSize();
            if (b.x + b.width >= tabsz.width
                || b.y + b.height >= tabsz.height)
                return null;
             // bugfix #110654
             if (b.width == 0 || b.height == 0) {
                 return null;
             }
            if( (isWindowsVistaLaF() || isWindowsXPLaF() || isWindowsLaF()) && i == getSelectedIndex() ) {
                b.x -= 3;
                b.y -= 2;
            } else if( isWindowsXPLaF() || isWindowsLaF() || IS_AQUA_LAF ) {
                b.x -= 2;
            } else if( isGTKLaF() && i == getSelectedIndex() ) {
                b.x -= 1;
                b.y -= 2;
            }
            if( i == getTabCount()-1 ) {
                if( isMetalLaF() )
                    b.x--;
                else if( IS_AQUA_LAF ) 
                    b.x -= 3;
            }
            return new Rectangle(b.x + b.width - 13,
                                 b.y + b.height / 2 - 5,
                                 12,
                                 12);
        }
    }

    private static boolean isWindows10() {
        String osName = System.getProperty ("os.name");
        return osName.indexOf("Windows 10") >= 0
            || (osName.equals( "Windows NT (unknown)" ) && "10.0".equals( System.getProperty("os.version") ));
    }

    private static boolean isWindows11() {
        String osName = System.getProperty ("os.name");
        return osName.indexOf("Windows 11") >= 0;
    }

    private boolean isWindowsVistaLaF() {
        String osName = System.getProperty ("os.name");
        return osName.indexOf("Vista") >= 0 
            || (osName.equals( "Windows NT (unknown)" ) && "6.0".equals( System.getProperty("os.version") ));
    }
    
    private boolean isWindowsXPLaF() {
        Boolean isXP = (Boolean)Toolkit.getDefaultToolkit().
                        getDesktopProperty("win.xpstyle.themeActive"); //NOI18N
        return isWindowsLaF() && (isXP == null ? false : isXP.booleanValue());
    }
    
    private boolean isWindowsLaF () {
        String lfID = UIManager.getLookAndFeel().getID();
        return lfID.endsWith("Windows"); //NOI18N
    }
    
    private boolean isMetalLaF () {
        String lfID = UIManager.getLookAndFeel().getID();
        return "Metal".equals( lfID ); //NOI18N
    }

    private boolean isGTKLaF () {
        return "GTK".equals( UIManager.getLookAndFeel().getID() ); //NOI18N
    }
    
    private void setPressedCloseButtonIndex(int index) {
        if (pressedCloseButtonIndex == index)
            return;

        if (pressedCloseButtonIndex >= 0
        && pressedCloseButtonIndex < getTabCount()) {
            Rectangle r = getCloseButtonBoundsAt(pressedCloseButtonIndex);
            if (r != null) {
                repaint(r.x, r.y, r.width + 2, r.height + 2);
            }

            JComponent c = _getJComponentAt(pressedCloseButtonIndex);
            if( c != null )
                setToolTipTextAt(pressedCloseButtonIndex, c.getToolTipText());
        }

        pressedCloseButtonIndex = index;

        if (pressedCloseButtonIndex >= 0
        && pressedCloseButtonIndex < getTabCount()) {
            Rectangle r = getCloseButtonBoundsAt(pressedCloseButtonIndex);
            if (r != null) {
                repaint(r.x, r.y, r.width + 2, r.height + 2);
            }
            setMouseOverCloseButtonIndex(-1);
            setToolTipTextAt(pressedCloseButtonIndex, null);
        }
    }

    private void setMouseOverCloseButtonIndex(int index) {
        if (mouseOverCloseButtonIndex == index)
            return;

        if (mouseOverCloseButtonIndex >= 0
        && mouseOverCloseButtonIndex < getTabCount()) {
            Rectangle r = getCloseButtonBoundsAt(mouseOverCloseButtonIndex);
            if (r != null) {
                repaint(r.x, r.y, r.width + 2, r.height + 2);
            }
            JComponent c = _getJComponentAt(mouseOverCloseButtonIndex);
            if( c != null )
                setToolTipTextAt(mouseOverCloseButtonIndex, c.getToolTipText());
        }

        mouseOverCloseButtonIndex = index;

        if (mouseOverCloseButtonIndex >= 0
        && mouseOverCloseButtonIndex < getTabCount()) {
            Rectangle r = getCloseButtonBoundsAt(mouseOverCloseButtonIndex);
            if (r != null) {
                repaint(r.x, r.y, r.width + 2, r.height + 2);
            }
            setPressedCloseButtonIndex(-1);
            setToolTipTextAt(mouseOverCloseButtonIndex, null);
        }
    }

    private JComponent _getJComponentAt( int tabIndex ) {
        Component c = getComponentAt( tabIndex );
        return c instanceof JComponent ? (JComponent)c : null;
    }
    
    private void fireCloseRequest(Component c) {
        firePropertyChange(TabbedPaneFactory.PROP_CLOSE, null, c);
        if (getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT) {
            int idx = getSelectedIndex();
            if (idx > 0) {
                setSelectedIndex(0);
                setSelectedIndex(idx);
            }
        }
    }

    static void fixGetBoundsAt(Rectangle b) {
        if (b.y < 0)
            b.y = -b.y;
        if (b.x < 0)
            b.x = -b.x;
    }

    static int findTabForCoordinate(JTabbedPane tab, int x, int y) {
        for (int i = 0; i < tab.getTabCount(); i++) {
            Rectangle b = tab.getBoundsAt(i);
            if (b != null) {
                b = new Rectangle(b);
                fixGetBoundsAt(b);

                if (b.contains(x, y)) {
                    return i;
                }
            }
        }
        return -1;
    }
    

    @Override
    protected void processMouseEvent (MouseEvent me) {
        try {
            super.processMouseEvent (me);
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            //Bug in BasicTabbedPaneUI$Handler:  The focusIndex field is not
            //updated when tabs are removed programmatically, so it will try to
            //repaint a tab that's not there
            Exceptions.attachLocalizedMessage(aioobe,
                                              "Suppressed AIOOBE bug in BasicTabbedPaneUI"); //NOI18N
            Logger.getAnonymousLogger().log(Level.WARNING, null, aioobe);
        }
    }

    @Override
    protected void fireStateChanged() {
        try {
            super.fireStateChanged();
        } catch( ArrayIndexOutOfBoundsException e ) {
            if( Utilities.isMac() ) {
                //#126651 - JTabbedPane is buggy on Mac OS
            } else {
                throw e;
            }
        }
    }

    @Override
    public Color getBackgroundAt(int index) {
        if( isWindowsLaF() && !isWindowsXPLaF() ) {
            // In Windows L&F selected and unselected tab may have same color
            // which make hard to distinguish which tab is selected (especially
            // in SCROLL_TAB_LAYOUT). In such case manage tab colors manually.
            Color selected = UIManager.getColor("controlHighlight");
            Color unselected = UIManager.getColor("control");
            if (selected.equals(unselected)) {
                //make unselected tabs darker
                unselected = new Color(Math.max(selected.getRed() - 12, 0),
                        Math.max(selected.getGreen() - 12, 0),
                        Math.max(selected.getBlue() - 12, 0));
            }
            return index == getSelectedIndex() ? selected : unselected;
        }
        return super.getBackgroundAt(index);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof Component) {
            assert evt.getPropertyName().equals(TabbedPaneFactory.NO_CLOSE_BUTTON);
            Component c = (Component) evt.getSource();
            int idx = indexOfComponent(c);
            boolean noCloseButton = (Boolean) evt.getNewValue();
            setTabComponentAt(idx, noCloseButton ? null : new ButtonTab());
        }
    }
    
    /**
     * Custom tab component for JTabbedPane
     */
    class ButtonTab extends JPanel {
        JLabel label;

        public ButtonTab() {
            super(new FlowLayout(FlowLayout.LEFT, 0, 0));
            setOpaque(false);
            label = new JLabel("") {
                
                private String lastText = null;
                
                @Override
                public String getText() {
                    String currentText = "";
                    int i = indexOfTabComponent(ButtonTab.this);
                    if (i >= 0)
                        currentText = getTitleAt(i);
                    
                    if (null != lastText && lastText.equals(currentText))
                        return lastText;
                    
                    lastText = currentText;
                    if (!super.getText().equals(currentText)) {
                        setText(currentText);
                        }
                    return currentText;
                }
                
                @Override
                public void setText(String text) {
                    super.setText(text);
                    if (isWindowsLaF() && (isWindows10() || isWindows11())) {
                        int r = text.endsWith(" ") || text.endsWith("&nbsp;</html>") ? 0 : 3; // NOI18N
                        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, r));
                    }
                }

                @Override
                public Icon getIcon() {
                    int i = indexOfTabComponent(ButtonTab.this);
                    Icon icon = i >= 0 ? getIconAt(i) : null;
                    if (super.getIcon() != icon) {
                        setIcon(icon);
                    }
                    return icon;
                }
            };
            add(label);
            JButton tabCloseButton = CloseButtonFactory.createCloseButton();
            if (IS_AQUA_LAF) {
              // NETBEANS-172: Improve positioning of label and close button within the tab button.
              setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
              tabCloseButton.setBorder(BorderFactory.createEmptyBorder(2, 2, 0, 0));
            }
            tabCloseButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    int i = indexOfTabComponent(ButtonTab.this);
                    if (i != -1) {
                        fireCloseRequest(CloseButtonTabbedPane.this.getComponentAt(i));
                    }
                }
            });
            add(tabCloseButton);
        }
    }

    private class MyNavigateAction extends AbstractAction {

        private final Action orig;

        public MyNavigateAction( Action orig ) {
            this.orig = orig;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            orig.actionPerformed(e);
        }

        @Override
        public boolean isEnabled() {
            return getTabCount() > 1;
        }
    }
}
