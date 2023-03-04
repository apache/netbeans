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

package org.openide.explorer.propertysheet;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyEditorManager;
import java.lang.reflect.Method;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.Node;

/** An extension to the basic test with static methods for pixel checking,
 * thread-safe key and button pushing and assorted stuff like that.  Thread
 * safety is accomplished by use of InvokeAndWait for tests run off the EQ,
 * and by draining the event queue of pending events in our own event loop for
 * tests running on the EQ, so all events happen.
 * <p>
 * In particular, this class contains
 * pre-checks for focus behavior and graphics environment, so that if the
 * focus or graphics behavior of the client system is outside the parameters
 * in which some tests are reliable, they will not be run.
 *
 * @author  Tim Boudreau  */
public class ExtTestCase extends NbTestCase {
    /** Dialog used for initial focus test */
    private static JDialog jd;
    /** If focus dependent tests can be run or not */
    private static Boolean focusTestsSafe = null;
    /** Frame used for initial focus test */
    protected static JFrame jf = null;
    /** Length of the thread sleep when sleep() is called */
    private static int SLEEP_LENGTH = 100;
    /** JTextField for typing tests */
    private static JTextField jtf = null;
    
    static {
        //Register the basic core property editors
        String[] syspesp = PropertyEditorManager.getEditorSearchPath();
        String[] nbpesp = new String[] {
            "org.netbeans.beaninfo.editors", // NOI18N
            "org.openide.explorer.propertysheet.editors", // NOI18N
        };
        String[] allpesp = new String[syspesp.length + nbpesp.length];
        System.arraycopy(nbpesp, 0, allpesp, 0, nbpesp.length);
        System.arraycopy(syspesp, 0, allpesp, nbpesp.length, syspesp.length);
        PropertyEditorManager.setEditorSearchPath(allpesp);
    }
    
    /** Creates a new instance of Exttest */
    public ExtTestCase(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                System.err.println("canSafelyRunFocusTests: " + canSafelyRunFocusTests());
            }
        });
    }
    
    protected static final void installCorePropertyEditors() {
        String[] syspesp = PropertyEditorManager.getEditorSearchPath();
        String[] nbpesp = new String[] {
            "org.netbeans.beaninfo.editors", // NOI18N
            "org.openide.explorer.propertysheet.editors", // NOI18N
        };
        String[] allpesp = new String[syspesp.length + nbpesp.length];
        System.arraycopy(nbpesp, 0, allpesp, 0, nbpesp.length);
        System.arraycopy(syspesp, 0, allpesp, nbpesp.length, syspesp.length);
        PropertyEditorManager.setEditorSearchPath(allpesp);
    }
    
    public void testNothing() {
        //do nothing - method just here to keep JUnit happy
    }
    
    /** Determine if the focus behavior in this environment is what is required
     * for dialog tests to pass.  A number of property sheet tests rely on
     * standard focus behaviors, such as newly shown dialogs receiving focus on
     * their default component, and hiding a dialog restoring focus to their
     * parent frame.  This method tests those behaviors, so that if the current
     * machine has aberrant behavior (some Linux window managers, notably
     * Sawfish, do), these tests can be skipped and no false fails will show up
     * when the tests are run.  */
    public static boolean canSafelyRunFocusTests() {
        if (focusTestsSafe != null) {
            return focusTestsSafe.booleanValue();
        }
        try {
            jf = new JFrame();
            JButton jb = new JButton("Show the dialog");
            jb.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    try {
                        showDialog(jf);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            jf.getContentPane().setLayout(new BorderLayout());
            jf.getContentPane().add(jb, BorderLayout.CENTER);
            jf.setBounds(20,20, 100,100);
            new WaitWindow(jf);
            
            boolean frameGotFocus = checkFocusedContainer(jf);
            
            if (!frameGotFocus) {
                System.err.println("Newly shown JFrame did not get focus.  Cannot reliably run focus-behavior-dependent tests on this machine");
                focusTestsSafe = Boolean.FALSE;
                return false;
            }
            
            click(jb);
            
            boolean frameLostFocus = checkNotFocusedContainer(jf);
            if (!frameLostFocus) {
                System.err.println("Newly shown child dialog of a frame did not remove focus from the frame. Cannot reliably run focus-behavior-dependent tests on this machine");
                focusTestsSafe = Boolean.FALSE;
                if (jd != null) {
                    jd.hide();
                    jd.dispose();
                }
                return false;
            }
            
            boolean dlgGotFocus = checkFocusedContainer(jd);
            if (!dlgGotFocus) {
                System.err.println("Newly shown child dialog of a frame did not receive focus when it was shown. Cannot reliably run focus-behavior-dependent tests on this machine");
                focusTestsSafe = Boolean.FALSE;
                return false;
            }
            
            boolean typingWorks =  tryDispatchingKeystrokes(jtf);
            if (!typingWorks) {
                System.err.println("Typing into dialog did not produce expected result");
                focusTestsSafe = Boolean.FALSE;
                return false;
            }
            
            jd.hide();
            //            WaitFocus wf = new WaitFocus(jf);
            
            sleep();
            
            boolean dlgReturnedFocus = checkFocusedContainer(jf);
            if (!dlgReturnedFocus) {
                System.err.println("Hiding child dialog did not return focus to parent. Cannot reliably run focus-behavior-dependent tests on this machine.");
                focusTestsSafe = Boolean.FALSE;
                return false;
            }
            
            focusTestsSafe = Boolean.TRUE;
            System.err.println("Focus dependent tests can be safely run and will be included.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            focusTestsSafe = Boolean.FALSE;
            return false;
        } finally {
            if (jf != null) {
                jf.hide();
                jf.dispose();
            }
            if (jd != null) {
                jd.hide();
                jd.dispose();
            }
            jf = null;
            jd = null;
            jtf = null;
        }
    }
    
    private static boolean debug = false;
    /** See if we're in debug mode */
    protected static boolean isDebug() {
        return debug;
    }
    
    protected static void setCurrentNode(final Node node, final PropertySheet ps) throws Exception {
        Runnable run = new Runnable() {
            public void run() {
                ps.setCurrentNode(node);
            }
        };
        invokeNow(run);
        ensurePainted(ps);
        sleep();
    }
    
    protected static void ensurePainted(final JComponent ps) throws Exception {
        //issues 39205 & 39206 - ensure the property sheet really repaints
        //before we get the value, or the value in the editor will not
        //have changed
        if (SwingUtilities.isEventDispatchThread()) {
            Graphics g = ps.getGraphics();
            ps.paintImmediately(0,0,ps.getWidth(), ps.getHeight());
        } else {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    Graphics g = ps.getGraphics();
                    ps.paintImmediately(0,0,ps.getWidth(), ps.getHeight());
                }
            });
        }
    }
    
    /** Switch on debug mode - this will introduce delays so the tests slow
     * down enough to watch what's happening on screen */
    protected static void setDebug(boolean val) {
        debug = val;
    }
    
    private static boolean tryDispatchingKeystrokes(final JTextField j) throws Exception {
        requestFocus(j);
        j.selectAll();
        sleep();
        
        pressKey(j, KeyEvent.VK_SHIFT);
        
        typeString("HELLO", j);
        
        releaseKey(j, KeyEvent.VK_SHIFT);
        
        System.err.println("Text area text is now " + j.getText());
        
        return "HELLO".equals(j.getText());
    }
    
    protected static Component focusComp() throws Exception {
        sleep();
        return KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
    }
    
    /** Determine if a container or its child has focus.  If it is a window,
     * it must be the focused window */
    protected static boolean checkFocusedContainer(Container c) throws Exception {
        synchronized (c.getTreeLock()) {
            c.getTreeLock().notifyAll();
        }
        if (SwingUtilities.isEventDispatchThread()) {
            //If the hide action is pending, allow it to happen before
            //checking if something happened
            drainEventQueue();
        } else {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    try {
                        drainEventQueue();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        Toolkit.getDefaultToolkit().sync();
        
        //Here we're waiting for the native window system to do *its* focus
        //transferring.  So do some extra waiting - otherwise sometimes focus
        //hasn't arrived at the frame and the events handled by AWT
        sleep();
        sleep();
        sleep();
        sleep();
        sleep();
        
        Window w = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
        Component currowner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        Component permowner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
        
        if (w == null) {
            return false;
        }
        
        if (w.isAncestorOf(currowner)) {
            //believe it or not this happens
            if (c instanceof Window && w != c) {
                System.err.println("Focused window is " + w);
                return false;
            }
        }
        if (c.isAncestorOf(currowner)) {
            return true;
        }
        if (c.isAncestorOf(permowner)) {
            return true;
        }
        return false;
    }
    
    /** Determine that a container or its child does not have focus.  If it is a window,
     * it must not the focused window */
    protected static boolean checkNotFocusedContainer(Container c) throws Exception {
        return !checkFocusedContainer(c);
    }
    
    private static void showDialog(JFrame parent) throws Exception {
        jd = new JDialog(parent);
        jtf = new JTextField("What a day I'm having!");
        jd.getContentPane().setLayout(new BorderLayout());
        jd.getContentPane().add(jtf, BorderLayout.CENTER);
        jd.setBounds(400,400,100, 50);
        new WaitWindow(jd);
        sleep();
    }
    
    /** Perform a mouse press, release and click events on the target component */
    protected static void click(final Component comp) throws Exception {
        maybeInvokeLater(new Clicker(comp, MouseEvent.MOUSE_PRESSED));
        sleep();
        maybeInvokeLater(new Clicker(comp, MouseEvent.MOUSE_RELEASED));
        sleep();
        maybeInvokeLater(new Clicker(comp, MouseEvent.MOUSE_CLICKED));
        sleep();
    }
    
    /** Perform a mouse press, release and click events on the target component
     * or its child at the coordinates passed.  */
    protected static void click(final Component comp, int x, int y) throws Exception {
        maybeInvokeLater(new Clicker(comp, x, y, MouseEvent.MOUSE_PRESSED));
        sleep();
        maybeInvokeLater(new Clicker(comp, x, y, MouseEvent.MOUSE_RELEASED));
        sleep();
        maybeInvokeLater(new Clicker(comp, x, y, MouseEvent.MOUSE_CLICKED));
        sleep();
    }
    
    /** Perform a mouse press on the target component */
    protected static void press(final Component comp)  throws Exception {
        maybeInvokeLater(new Clicker(comp, MouseEvent.MOUSE_PRESSED));
        sleep();
    }
    
    /** Perform a mouse press on the target component or its child at the
     * passed coordinates */
    protected static void press(final Component comp, int x, int y)  throws Exception {
        maybeInvokeLater(new Clicker(comp, x, y, MouseEvent.MOUSE_PRESSED));
        sleep();
    }
    
    /** Perform a mouse release on the target component */
    protected static void release(final Component comp)  throws Exception {
        maybeInvokeLater(new Clicker(comp, MouseEvent.MOUSE_PRESSED));
        sleep();
    }
    
    /** Perform a mouse release on the target component or its child at the
     * passed coordinates */
    protected static void release(final Component comp, int x, int y)  throws Exception {
        maybeInvokeLater(new Clicker(comp, x, y, MouseEvent.MOUSE_PRESSED));
        sleep();
    }
    
    /** Thread-safe focus requesting */
    protected static void requestFocus(final Component comp) throws Exception {
        maybeInvokeLater(new FocusRequester(comp));
        sleep();
    }
    
    /** Send a mouse pressed to a particular cell of a JTable */
    protected static void pressCell(JTable tbl, int x, int y) throws Exception {
        Rectangle r = tbl.getCellRect(x, y, false);
        System.err.println("Pressing table at " + (r.x+5) + "," + r.y+5);
        press(tbl, r.x+5, r.y+5);
    }
    
    private static class FocusRequester implements Runnable {
        private Component comp;
        public FocusRequester(Component comp) {
            this.comp = comp;
        }
        public void run() {
            comp.requestFocus();
        }
    }
    
    private static interface EventGenerator {
        public AWTEvent getEvent();
    }
    
    /** Class which dispatches a mouse event of the passed type to the
     * target component or its child at the passed coordinates */
    private static class Clicker implements Runnable, EventGenerator {
        int x = -1;
        int y = -1;
        private Component target = null;
        int type;
        public Clicker(Component target, int type) {
            this.target = target;
            this.type = type;
        }
        
        public Clicker(Component target, int x, int y, int type) {
            this.target = target;
            this.x = x;
            this.y = y;
            this.type = type;
        }
        
        public AWTEvent getEvent() {
            Point toClick;
            if (x == -1 && y == -1) {
                toClick = new Point(5,5);
            } else {
                toClick = new Point(x,y);
            }
            Component realtarget = target.getComponentAt(toClick);
            
            MouseEvent result = new MouseEvent(realtarget, type,
                    System.currentTimeMillis(), MouseEvent.BUTTON1_MASK, toClick.x,
                    toClick.y, 2, false);
            
            return result;
        }
        
        public void run() {
            try {
                dispatchEvent(null, getEvent());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    /** Invokes a runnable on the event queue if the current thread is not the
     * event queue, or synchronously if it is.  If it is invoked synchronously,
     * the event queue will be drained before this method returns, so any events
     * generated by the runnable have been processed.  If a runtime exception
     * is thrown while the passed-in runnable is running, it will be rethrown
     * by this method, in the calling thread.  */
    protected static void maybeInvokeLater(Runnable run) throws Exception {
        WrapperRunnable wrap = new WrapperRunnable(run);
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeAndWait(wrap);
        } else {
            if (run instanceof EventGenerator) {
                AWTEvent evt = ((EventGenerator)run).getEvent();
                ((Component) evt.getSource()).dispatchEvent(evt);
            } else {
                wrap.run();
            }
        }
        wrap.throwAnyExceptions();
        sleep();
    }
    
    protected static void invokeNow(Runnable run) throws Exception {
        WrapperRunnable wrap = new WrapperRunnable(run);
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeAndWait(wrap);
        } else {
            if (run instanceof EventGenerator) {
                AWTEvent evt = ((EventGenerator)run).getEvent();
                ((Component) evt.getSource()).dispatchEvent(evt);
            } else {
                wrap.run();
            }
        }
        wrap.throwAnyExceptions();
        sleep();
    }
    
    /** Runnable which wraps another runnable and holds any exception thrown while
     * running it, for rethrowing later.  */
    public static class WrapperRunnable implements Runnable {
        protected Exception exception = null;
        private Runnable run;
        public WrapperRunnable(Runnable run) {
            this.run = run;
        }
        
        protected WrapperRunnable() {
            this.run = null;
        }
        
        public void throwAnyExceptions() throws Exception {
            if (exception != null) {
                throw exception;
            }
        }
        
        public void run() {
            if (run == null) {
                //Should never happen
                return;
            }
            try {
                run.run();
                if (run instanceof WrapperRunnable) {
                    ((WrapperRunnable)run).throwAnyExceptions();
                }
            } catch (Exception e) {
                exception = e;
            }
        }
    }
    
    protected static void typeString(String s, Component comp) throws Exception {
        char[] c = s.toCharArray();
        for (int i=0; i < c.length; i++) {
            typeKey(comp, c[i]);
        }
    }
    
    /** Fake the keystroke ctrl+key to the component, sending pressed,
     * released and typed events */
    protected static void ctrlTypeKey(Component target, int key) throws Exception {
        typeKey(target, key, KeyEvent.CTRL_MASK);
    }
    
    /** Fake the keystroke ctrl+shift+key to the component, sending pressed,
     * released and typed events */
    protected static void ctrlShiftTypeKey(Component target, int key) throws Exception {
        typeKey(target, key, KeyEvent.SHIFT_MASK | KeyEvent.CTRL_MASK);
    }
    
    /** Fake the keystroke shift+key to the component, sending pressed,
     * released and typed events */
    protected static void shiftTypeKey(Component target, int key) throws Exception {
        typeKey(target, key, KeyEvent.SHIFT_MASK);
    }
    
    /** Fake a keystroke to the component, sending pressed,
     * released and typed events */
    protected static void typeKey(Component target, int key) throws Exception {
        pressKey(target, key);
        typedKey(target, key);
        releaseKey(target, key);
    }
    
    /** Fake a keystroke to the component, sending pressed,
     * released and typed events with the specified modifier mask
     * (ctrl, shift, alt, meta) */
    protected static void typeKey(Component target, int key, int mask) throws Exception {
        pressKey(target, key, mask);
        typedKey(target, key, mask);
        releaseKey(target, key, mask);
    }
    
    /** Fake a key-pressed event to the component */
    protected static void pressKey(Component target, int key) throws Exception {
        maybeInvokeLater(new Typist(target, key, KeyEvent.KEY_PRESSED, 0));
        sleep();
    }
    
    /** Fake a key-pressed event to the component with the specified modifier mask */
    protected static void pressKey(Component target, int key, int mask) throws Exception {
        maybeInvokeLater(new Typist(target, key, KeyEvent.KEY_PRESSED, mask));
        sleep();
    }
    
    /** Fake a key-released event to the component*/
    protected static void releaseKey(Component target, int key) throws Exception {
        maybeInvokeLater(new Typist(target, key, KeyEvent.KEY_RELEASED, 0));
        sleep();
    }
    
    /** Fake a key-released event to the component with the specified modifier mask */
    protected static void releaseKey(Component target, int key, int mask) throws Exception {
        maybeInvokeLater(new Typist(target, key, KeyEvent.KEY_RELEASED, mask));
        sleep();
    }
    
    /** Fake a key-released event to the component */
    protected static void typedKey(Component target, int key) throws Exception {
        maybeInvokeLater(new Typist(target, key,
                KeyEvent.KEY_TYPED, 0));
        sleep();
    }
    
    /** Fake a key-typed event to the component with the specified modifier mask */
    protected static void typedKey(Component target, int key, int mask) throws Exception {
        maybeInvokeLater(new Typist(target, key,
                KeyEvent.KEY_TYPED, mask));
        sleep();
    }
    
    public static boolean waitForAnythingToGetFocus() {
        Component foc = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        int ct=0;
        while (foc == null) {
            try {
                Thread.currentThread().sleep(100);
            } catch (Exception e ){}
            foc = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            ct++;
            if (ct > 200) {
                break;
            }
        }
        return foc != null;
    }
    
    public static boolean waitForDialog() {
        Container c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        int ct = 0;
        while (!(c instanceof Dialog)) {
            try {
                Thread.currentThread().sleep(50);
            } catch (Exception e) {}
            c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
            ct++;
            if (ct > 100) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean waitForFrame() {
        Container c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        int ct = 0;
        while (!(c instanceof JFrame)) {
            try {
                Thread.currentThread().sleep(50);
            } catch (Exception e) {}
            c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
            ct++;
            if (ct > 100) {
                return false;
            }
        }
        return true;
    }
    
    public static Component waitForComponentOrChildToGetFocus(Container c) {
        Component foc = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        int ct=0;
        while (foc == null || (foc != c && !c.isAncestorOf(foc))) {
            try {
                Thread.currentThread().sleep(100);
            } catch (Exception e ) {}
            foc = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            ct++;
            if (ct > 200) {
                break;
            }
        }
        return foc;
    }
    
    /** Runnable which fakes a keystroke to a component */
    private static class Typist implements Runnable, EventGenerator {
        private Component target;
        private int key;
        private int type;
        private int mask;
        public Typist(Component target, int key, int type, int mask) {
            this.target = target;
            this.key = key;
            this.type = type;
            this.mask = mask;
        }
        
        public AWTEvent getEvent() {
            return createKeyEvent(target, key, type, mask);
        }
        
        public void run() {
            target.dispatchEvent(getEvent());
        }
    }
    
    /** Construct a key event for the target component of the passed type, with
     * the passed key mask */
    private static KeyEvent createKeyEvent(Component target, int key, int type, int mask) {
        KeyEvent result;
        if (type != KeyEvent.KEY_TYPED) {
            result = new KeyEvent(target, type, System.currentTimeMillis(), mask,
                    key, (char) key, KeyEvent.KEY_LOCATION_STANDARD);
        } else {
            result = new KeyEvent(target, type, System.currentTimeMillis(), mask,
                    KeyEvent.VK_UNDEFINED, (char) key);
        }
        return result;
    }
    
    public static class WaitFocus implements FocusListener {
        
        public WaitFocus(Component c) throws Exception {
            c.addFocusListener(this);
            requestFocus(c);
            if (!SwingUtilities.isEventDispatchThread()) {
                synchronized (this) {
                    try {
                        wait(5000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
        boolean gained = false;
        public void focusGained(FocusEvent e) {
            gained = true;
            synchronized(this) {
                notifyAll();
            }
        }
        
        public void focusLost(FocusEvent e) {
        }
        
    }
    
    /** Class which shows a window and does not return until that window is
     * definitely on-screen and ready to receive events */
    static class WaitWindow extends WindowAdapter {
        boolean shown=false;
        public WaitWindow(JFrame f) throws Exception {
            f.addWindowListener(this);
            f.show();
            f.toFront();
            f.requestFocus();
            
            if (f.isShowing()) {
                return;
            }
            Runnable run = new Runnable() {
                public void run() {
                    if (!shown) {
                        synchronized(WaitWindow.this) {
                            try {
                                //System.err.println("Waiting for window");
                                wait(5000);
                            } catch (Exception e) {}
                        }
                    }
                }
            };
            maybeInvokeLater(run);
            int ct = 0;
            while (!f.isShowing()) {
                ct++;
                try {
                    Thread.currentThread().sleep(400);
                } catch (Exception e) {
                    
                }
                if (ct > 100) {
                    break;
                }
            }
            ct=0;
            Container c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
            while (c != f) {
                try {
                    Thread.currentThread().sleep(400);
                } catch (Exception e) {
                    
                }
                c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
                ct++;
                if (ct > 100) {
                    break;
                }
            }
        }
        
        public WaitWindow(JDialog f) throws Exception {
            f.addWindowListener(this);
            f.show();
            f.toFront();
            f.requestFocus();
            Runnable run = new Runnable() {
                public void run() {
                    if (!shown) {
                        synchronized(this) {
                            try {
                                if (!SwingUtilities.isEventDispatchThread()) {
                                    wait(5000);
                                }
                            } catch (Exception e) {}
                        }
                    }
                }
            };
            maybeInvokeLater(run);
            if (!shown) {
                synchronized(this) {
                    try {
                        //System.err.println("Waiting for window");
                        wait(6000);
                    } catch (Exception e) {}
                }
            }
            int ct = 0;
            while (!f.isShowing()) {
                ct++;
                try {
                    Thread.currentThread().sleep(400);
                } catch (Exception e) {
                    
                }
                if (ct > 100) {
                    break;
                }
            }
            ct=0;
            Container c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
            while (c != f) {
                try {
                    Thread.currentThread().sleep(400);
                } catch (Exception e) {
                    
                }
                c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
                ct++;
                if (ct > 100) {
                    break;
                }
            }
        }
        
        @Override
        public void windowOpened(WindowEvent e) {
            shown = true;
            synchronized(this) {
                notifyAll();
                if (e.getSource() instanceof JFrame) {
                    ((JFrame) e.getSource()).removeWindowListener(this);
                } else {
                    ((JDialog) e.getSource()).removeWindowListener(this);
                }
            }
        }
    }
    
    /** Drains the event queue, either forcibly if called on the AWT thread,
     * or by posting a series of runnables via invokeAndWait if called from
     * some other thread. */
    protected static void sleep() throws Exception {
        if (isDebug()) {
            try {
                Thread.currentThread().sleep(400);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        if (SwingUtilities.isEventDispatchThread()) {
            try {
                drainEventQueue();
                if (!isDebug()) {
                    Thread.currentThread().sleep(SLEEP_LENGTH);
                }
            } catch (InterruptedException ie) {
                //go away
            }
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        System.currentTimeMillis();
                    }
                });
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        System.currentTimeMillis();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private static void popEventQueue() throws Exception {
        try {
            Method m = EventQueue.class.getDeclaredMethod("pop", null);
            m.setAccessible(true);
            m.invoke(Toolkit.getDefaultToolkit().getSystemEventQueue(), null);
        } catch (Exception e) {
            
        }
    }
    
    private static void dispatchEvent(EventQueue queue, AWTEvent evt) throws Exception {
        if (queue == null) {
            queue = Toolkit.getDefaultToolkit().getSystemEventQueue();
        }
        Method m = EventQueue.class.getDeclaredMethod("dispatchEvent", new Class[] {AWTEvent.class});
        m.setAccessible(true);
        if (evt.getSource() instanceof JTextField) {
            foo = System.currentTimeMillis();
        }
        m.invoke(queue, new Object[] {evt});
    }
    static long foo = 0;
    
    /** Drain the event queue.  ONLY CALL THIS METHOD FROM THE EVENT DISPATCH
     * THREAD OR IT WILL DO BAD THINGS! */
    private static void drainEventQueue() throws Exception {
        AWTEvent evt = EventQueue.getCurrentEvent();
        //Dispatch any events that the code that just ran may have generated,
        //so dialogs appear, things get focus and paint, stuff like that
        while (Toolkit.getDefaultToolkit().getSystemEventQueue().peekEvent() != null) {
            //do fetch this every time, its value can change
            EventQueue queue = Toolkit.getDefaultToolkit().getSystemEventQueue();
            evt = queue.getNextEvent();
            dispatchEvent(queue, evt);
        }
    }
    
}
