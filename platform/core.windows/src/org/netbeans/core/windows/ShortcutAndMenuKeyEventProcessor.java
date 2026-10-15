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

package org.netbeans.core.windows;

import java.awt.AWTEvent;
import java.awt.AWTKeyStroke;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.KeyEventDispatcher;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import org.netbeans.core.NbKeymap;
import org.netbeans.core.NbLifecycleManager;
import org.netbeans.core.windows.view.ui.popupswitcher.KeyboardPopupSwitcher;
import org.openide.actions.ActionManager;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 * this class registers itself to the KeyboardFocusManager as a key event
 * post-processor as well as a key event dispatcher.  It invokes the action
 * bound to the key stroke, or routes unconsumed key events to the menu bar.
 * If a menu is already shown, all key events are routed to the main menu bar.
 * 
 * @author Tran Duc Trung
 */
final class ShortcutAndMenuKeyEventProcessor implements KeyEventDispatcher, KeyEventPostProcessor, AWTEventListener {
    
    private static ShortcutAndMenuKeyEventProcessor defaultInstance;
    
    private static boolean installed = false;

    /* holds original set of focus forward traversal keys */
    private static Set<AWTKeyStroke> defaultForward;
    /* holds original set of focus backward traversal keys */
    private static Set<AWTKeyStroke> defaultBackward;
    
    private static final Logger log = Logger.getLogger("org.netbeans.core.windows.ShortcutAndMenuKeyEventProcessor"); // NOI18N
    
    private  ShortcutAndMenuKeyEventProcessor() {
    }
    

    private static synchronized ShortcutAndMenuKeyEventProcessor getDefault() {
        if(defaultInstance == null) {
            defaultInstance = new ShortcutAndMenuKeyEventProcessor();
        }
        
        return defaultInstance;
    }
    
    
    public static synchronized void install() {
        if(installed) {
            return;
        }
        
        ShortcutAndMenuKeyEventProcessor instance = getDefault();
        
        KeyboardFocusManager keyboardFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        keyboardFocusManager.addKeyEventDispatcher(instance);
        keyboardFocusManager.addKeyEventPostProcessor(instance);
        // #63252: Disable focus traversal functionality of Ctrl+Tab and Ctrl+Shift+Tab,
        // to allow our own document switching (RecentViewListAction)
        defaultForward = keyboardFocusManager.getDefaultFocusTraversalKeys(
                            KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        defaultBackward = keyboardFocusManager.getDefaultFocusTraversalKeys(
                            KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
        keyboardFocusManager.setDefaultFocusTraversalKeys(
            KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
            Collections.singleton(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, 0))
        );                
        keyboardFocusManager.setDefaultFocusTraversalKeys(
            KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
            Collections.singleton(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK))
        );
        Toolkit.getDefaultToolkit().addAWTEventListener(instance, AWTEvent.MOUSE_EVENT_MASK);
    }
    
    public static synchronized void uninstall() {
        if(!installed) {
            return;
        }
        
        ShortcutAndMenuKeyEventProcessor instance = getDefault();
        
        KeyboardFocusManager keyboardFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        keyboardFocusManager.removeKeyEventDispatcher(instance);
        keyboardFocusManager.removeKeyEventPostProcessor(instance);
        // reset default focus traversal keys
        keyboardFocusManager.setDefaultFocusTraversalKeys(
                KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, defaultForward
        );                
        keyboardFocusManager.setDefaultFocusTraversalKeys(
                KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, defaultBackward
        );                
        defaultBackward = null;
        defaultForward = null;
        Toolkit.getDefaultToolkit().removeAWTEventListener(instance);
    }

    private boolean wasPopupDisplayed;
    private int lastModifiers;
    private char lastKeyChar;
    private boolean lastSampled = false;
    private boolean skipNextTyped = false;

    @Override
    public void eventDispatched(AWTEvent event) {
        if (event instanceof MouseEvent) {
            processMouseEvent((MouseEvent) event);
        }
    }
    
    public boolean postProcessKeyEvent(KeyEvent ev) {
        if (ev.isConsumed())
            return false;

        if (processShortcut(ev))
            return true;

        Window w = SwingUtilities.windowForComponent(ev.getComponent());        
        if (w instanceof Dialog && !WindowManagerImpl.isSeparateWindow(w))
            return false;
        
        JFrame mw = (JFrame)WindowManagerImpl.getInstance().getMainWindow();
        if (w == mw) {
            return false;
        }

        JMenuBar mb = mw.getJMenuBar();
        if (mb == null)
            return false;
        boolean pressed = (ev.getID() == KeyEvent.KEY_PRESSED);        
        boolean res = invokeProcessKeyBindingsForAllComponents(ev, mw, pressed);
        
        if (res)
            ev.consume();
        return res;
    }

    public boolean dispatchKeyEvent(KeyEvent ev) {
        log.fine("dispatchKeyEvent ev: " + ev.paramString()
        + " source:" + ev.getSource().getClass().getName());
        // in some ctx, may need event filtering
        if (NbKeymap.getContext().length != 0) {
            // Ignore anything but KeyPressed inside ctx, #67187
            if (ev.getID() != KeyEvent.KEY_PRESSED) {
                ev.consume();
                return true;
            }
            
            skipNextTyped = true;

            Component comp = ev.getComponent();
            if (!(comp instanceof JComponent) ||
                ((JComponent)comp).getClientProperty("context-api-aware") == null) {
                    // not context api aware, don't pass subsequent events
                processShortcut(ev);
                // ignore processShortcut result, consume everything while in ctx
                return true; 
            }
        }
 
        if (ev.getID() == KeyEvent.KEY_PRESSED
            && ev.getModifiers() == (InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK)
            && (ev.getKeyCode() == KeyEvent.VK_PAUSE
                || ev.getKeyCode() == KeyEvent.VK_CANCEL)
            ) {
            Object source = ev.getSource();
            if (source instanceof Component) {
                Component focused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                System.err.println("*** ShortcutAndMenuKeyEventProcessor: current focus owner = " + focused); // NOI18N
            }
            ev.consume();
            return true;
        }
        
	
	// multi-shortcut in middle
        if (ev.getID() == KeyEvent.KEY_TYPED && skipNextTyped) {
            ev.consume();
            skipNextTyped = false;
            return true;
        }

        if (ev.getID() == KeyEvent.KEY_RELEASED) {
            skipNextTyped = false;
        }
        
        if (ev.getID() == KeyEvent.KEY_PRESSED) {
            // decompose to primitive fields to avoid memory profiler confusion (keyEvent keeps source reference)
            lastKeyChar = ev.getKeyChar();
            lastModifiers = ev.getModifiers();
            lastSampled = true;
        }
        
        MenuElement[] arr = MenuSelectionManager.defaultManager().getSelectedPath();
        if (arr == null || arr.length == 0) {
            wasPopupDisplayed = false;

            // Only here for fix #41477:
            // To be able to catch and dispatch Ctrl+TAB and Ctrl+Shift+Tab
            // in our own way, it's needed to do as soon as here, because
            // otherwise Swing will use these keys as focus traversals, which 
            // means that TopComponent which contains focusCycleRoot inside itself
            // will grab these shortcuts, which is not desirable 
            return KeyboardPopupSwitcher.processShortcut(ev);
        }

        if (!wasPopupDisplayed
            && lastSampled == true
            && ev.getID() == KeyEvent.KEY_TYPED
            && lastModifiers == InputEvent.ALT_MASK
            && ev.getModifiers() == InputEvent.ALT_MASK
            && lastKeyChar == ev.getKeyChar()
            ) {
            wasPopupDisplayed = true;
            ev.consume();
            return true;
        }

        wasPopupDisplayed = true;
        
        MenuSelectionManager.defaultManager().processKeyEvent(ev);
        
        // commented out as #130919 fix - I don't know why this was here, but
        // it did prevent keyboard functioning in menus in dialogs
        /*if (!ev.isConsumed() && arr != null && arr.length > 0 && arr[0] instanceof JMenuBar) {
            ev.setSource(WindowManagerImpl.getInstance().getMainWindow());
        }*/
        
        return ev.isConsumed();
    }

    private boolean processShortcut(KeyEvent ev) {
        //ignore shortcut keys when the IDE is shutting down
        if (NbLifecycleManager.isExiting()) {
            ev.consume();
            return true;
        }
        
        KeyStroke ks = KeyStroke.getKeyStrokeForEvent(ev);
        Window w = SwingUtilities.windowForComponent(ev.getComponent());

        // don't process shortcuts if this is a help frame
        if ((w instanceof JFrame) && ((JFrame)w).getRootPane().getClientProperty("netbeans.helpframe") != null) // NOI18N
            return true;
        
        // don't let action keystrokes to propagate from both
        // modal and nonmodal dialogs, but propagate from separate floating windows,
        // even if they are backed by JDialog
        if ((w instanceof Dialog) &&
            !WindowManagerImpl.isSeparateWindow(w) &&
            !isTransmodalAction(ks)) {
            return false;
        }
        
        // Provide a reasonably useful action event that identifies what was focused
        // when the key was pressed, as well as what keystroke ran the action.
        ActionEvent aev = new ActionEvent(
            ev.getSource(), ActionEvent.ACTION_PERFORMED, Utilities.keyToString(ks));
            
        Keymap root = Lookup.getDefault().lookup(Keymap.class);
        Action a = root.getAction (ks);
        if (a != null && a.isEnabled()) {
            ActionManager am = Lookup.getDefault().lookup(ActionManager.class);
            am.invokeAction(a, aev);
            ev.consume();
            return true;
        }
        return false;
    }

    private void processMouseEvent(MouseEvent mev) {
        if (mev.getID() != MouseEvent.MOUSE_PRESSED
                || mev.getButton() <= MouseEvent.BUTTON3
                || mev.isPopupTrigger()
                || mev.isConsumed()) {
            return;
        }
        int button = mev.getButton();
        if (Utilities.getOperatingSystem() == Utilities.OS_LINUX) {
            // the JDK drops buttons for vertical scroll
            // drop buttons for horizontal scroll here.
            button -= 2;
            if (button <= 3) {
                return;
            }
        }
        //ignore when the IDE is shutting down
        if (NbLifecycleManager.isExiting()) {
            return;
        }
        int keycode = Utilities.mouseButtonKeyCode(button);
        if (keycode == KeyEvent.VK_UNDEFINED) {
            return;
        }
        int modifiers = 0;
        if (mev.isControlDown()) {
            modifiers |= InputEvent.CTRL_DOWN_MASK;
        }
        if (mev.isAltDown()) {
            modifiers |= InputEvent.ALT_DOWN_MASK;
        }
        if (mev.isShiftDown()) {
            modifiers |= InputEvent.SHIFT_DOWN_MASK;
        }
        if (mev.isMetaDown()) {
            modifiers |= InputEvent.META_DOWN_MASK;
        }

        KeyStroke ks = KeyStroke.getKeyStroke(keycode, modifiers);
        Window w = SwingUtilities.windowForComponent(mev.getComponent());

        // don't process shortcuts if this is a help frame
        if ((w instanceof JFrame) && ((JFrame) w).getRootPane().getClientProperty("netbeans.helpframe") != null) { // NOI18N
            return;
        }

        // don't let action keystrokes to propagate from both
        // modal and nonmodal dialogs, but propagate from separate floating windows,
        // even if they are backed by JDialog
        if ((w instanceof Dialog)
                && !WindowManagerImpl.isSeparateWindow(w)
                && !isTransmodalAction(ks)) {
            return;
        }

        // Provide a reasonably useful action event that identifies what was focused
        // when the key was pressed, as well as what keystroke ran the action.
        ActionEvent aev = new ActionEvent(
                mev.getSource(), ActionEvent.ACTION_PERFORMED, Utilities.keyToString(ks));

        Action action = null;
        Component focused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focused instanceof JTextComponent) {
            action = ((JTextComponent) focused).getKeymap().getAction(ks);
        }
        if (action == null) {
            Keymap km = Lookup.getDefault().lookup(Keymap.class);
            action = (km != null) ? km.getAction(ks) : null;
        }

        if (action != null && action.isEnabled()) {
            action.actionPerformed(aev);
            mev.consume();
        }
    }

    private static boolean invokeProcessKeyBindingsForAllComponents(
        KeyEvent e, Container container, boolean pressed)
    {
        try {
            Method m = JComponent.class.getDeclaredMethod(
                "processKeyBindingsForAllComponents", // NOI18N
                new Class[] { KeyEvent.class, Container.class, Boolean.TYPE });
            if (m == null)
                return false;

            m.setAccessible(true);
            Boolean b = (Boolean) m.invoke(null, new Object[] { e, container, pressed ? Boolean.TRUE : Boolean.FALSE });
            return b.booleanValue();
        } catch (Exception ex) {
            //ex.printStackTrace();
        }
        
        return false;
    }

    /**
     * Checks to see if a given keystroke is bound to an action which should
     * function on all focused components.  This includes the Main Window,
     * dialogs, popup menus, etc.  Otherwise only the Main Window and
     * TopComponents will receive the keystroke.  By default, off, unless the
     * action has a property named <code>OpenIDE-Transmodal-Action</code> which
     * is set to {@link Boolean#TRUE}.
     * @param key the keystroke to check
     * @return <code>true</code> if transmodal; <code>false</code> if a normal
     * action, or the key is not bound to anything in the global keymap
     */
    private static boolean isTransmodalAction (KeyStroke key) {
        Keymap root = Lookup.getDefault().lookup(Keymap.class);
        Action a = root.getAction (key);
        if (a == null) return false;
        Object val = a.getValue ("OpenIDE-Transmodal-Action"); // NOI18N
        return val != null && val.equals (Boolean.TRUE);
    }

}
