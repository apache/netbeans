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

package org.netbeans.core.windows.services;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jaroslav Tulach, Jiri Rechtacek
 */
public class DialogDisplayerImplTest extends NbTestCase {
    private DialogDisplayer dd;
    private final Object RESULT = "DialogDisplayerImplTestResult";
    private JOptionPane pane;
    private JButton closeOwner;
    private DialogDescriptor childDD;
    private JButton openChild;
    private JButton closeChild;
    private Component child;
    @SuppressWarnings("NonConstantLogger")
    private Logger LOG;
    
    public DialogDisplayerImplTest (String testName) {
        super (testName);
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }
    
    @Override
    protected void setUp() throws Exception {
        LOG = Logger.getLogger("test." + getName());
        dd = new DialogDisplayerImpl (RESULT);
        closeOwner = new JButton ("Close this dialog");
        childDD = new DialogDescriptor ("Child", "Child", false, null);
        openChild = new JButton ("Open child");
        closeChild = new JButton ("Close child");
        pane = new JOptionPane ("", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[] {openChild, closeChild});
    }

    @Override
    protected void tearDown() throws Exception {
        while (true) {
            Window w = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
            if (w == null) {
                break;
            }
            w.setVisible(false);
            w.dispose();
        }
        super.tearDown();
    }

    @Override
    protected int timeOut() {
        return 30000;
    }

    @Override
    protected boolean runInEQ () {
        return false;
    }
    
    public void testUnitTestByDefaultReturnsRESULT () throws Exception {
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation ("AnyQuestion?");
        Object r = dd.notify (nd);
        assertEquals (RESULT, r);
    }

    public void testWorksFromAWTImmediatelly () throws Exception {
        class FromAWT implements Runnable {
            @Override
            public void run () {
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation ("HowAreYou?");
                Object r  = dd.notify (nd);
                assertEquals ("Returns ok", RESULT, r);
            }
        }
        
        SwingUtilities.invokeAndWait (new FromAWT ());
    }
    
    public void testDeadlock41544IfItIsNotPossibleToAccessAWTReturnAfterTimeout () throws Exception {
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation ("HowAreYou?");
        
        class BlockAWT implements Runnable {
            volatile int state;
            @Override
            public synchronized void run () {
                setState(1);
                try {
                    notify ();
                    long t = System.currentTimeMillis ();
                    wait (40000);
                    if (System.currentTimeMillis () - t > 37000) {
                        // this is wrong
                        setState(3);
                        // wait for the dialog to finish
                        notify ();
                        return ;
                    }
                } catch (Exception ex) {
                }
                setState(2);
                notify ();
            }

            /**
             * @return the state
             */
            int getState() {
                return state;
            }

            /**
             * @param state the state to set
             */
            void setState(int state) {
                LOG.info("Changing state to " + state);
                this.state = state;
            }
        }
        
        BlockAWT b = new BlockAWT ();
        synchronized (b) {
            SwingUtilities.invokeLater (b);
            b.wait ();
            assertEquals ("In state one", 1, b.getState());
        }
        LOG.info("Before notify " + b.getState());
        Object res = dd.notify (nd);
        LOG.info("After notify " + b.getState());
        
        if (b.getState() == 3) {
            fail ("This means that the AWT blocked timeouted - e.g. no time out implemented in the dd.notify at all");
        }

        assertEquals ("Returns as closed, if cannot access AWT", NotifyDescriptor.CLOSED_OPTION, res);

        synchronized (b) {
            b.notify ();
            LOG.info("Before wait " + b.getState());
            b.wait ();
            assertEquals ("Exited correctly", 2, b.getState());
        }
    }
    
    @RandomlyFails
    public void testLeafDialog () throws Exception {
        boolean leaf = true;
        DialogDescriptor ownerDD = new DialogDescriptor (pane, "Owner", true, new Object[] {closeOwner}, null, 0, null, null, leaf);
        final Dialog owner = DialogDisplayer.getDefault ().createDialog (ownerDD);
        
        // make leaf visible
        postInAwtAndWaitOutsideAwt (new Runnable () {
            @Override
            public void run () {
                owner.setVisible (true);
            }
        });
        assertShowing("Owner should be visible", true, owner);
        
        child = DialogDisplayer.getDefault ().createDialog (childDD);

        // make the child visible
        postInAwtAndWaitOutsideAwt (new Runnable () {
            @Override
            public void run () {
                child.setVisible (true);
            }
        });
        assertShowing("Child will be visible", true, child);
        
        Window w = SwingUtilities.windowForComponent(child);
        assertFalse ("No dialog is owned by leaf dialog.", owner.equals (w.getOwner ()));
        assertEquals ("The leaf dialog has no child.", 0, owner.getOwnedWindows ().length);
        
        assertTrue ("Leaf is visible", owner.isVisible ());
        assertTrue ("Child is visible", child.isVisible ());
        
        // close the leaf window
        postInAwtAndWaitOutsideAwt (new Runnable () {
            @Override
            public void run () {
                owner.setVisible (false);
            }
        });
        assertShowing("Disappear", false, owner);
        
        assertFalse ("Leaf is dead", owner.isVisible ());
        assertTrue ("Child is visible still", child.isVisible ());
        
        // close the child dialog
        postInAwtAndWaitOutsideAwt (new Runnable () {
            @Override
            public void run () {
                child.setVisible (false);
            }
        });        
        assertShowing("Child is invisible", false, child);
        
        assertFalse ("Child is dead too", child.isVisible ());
    }
    
    @RandomlyFails
    public void testLeafNotify() throws Exception {
        boolean leaf = true;
        DialogDescriptor ownerDD = new DialogDescriptor (pane, "Owner", true, new Object[] {closeOwner}, null, 0, null, null, leaf);
        final Dialog owner = DialogDisplayer.getDefault ().createDialog (ownerDD);
        
        // make leaf visible
        postInAwtAndWaitOutsideAwt (new Runnable () {
            @Override
            public void run () {
                owner.setVisible (true);
            }
        });
        assertShowing("Owner should be visible", true, owner);
        
        child = new JButton();
        final NotifyDescriptor nd = new NotifyDescriptor.Message(child);

        // make the child visible
        RequestProcessor.getDefault().post(new Runnable () {
            @Override
            public void run () {
                DialogDisplayer.getDefault().notify(nd);
            }
        });
        assertShowing("Child will be visible", true, child);
        
        Window w = SwingUtilities.windowForComponent(child);
        assertFalse ("No dialog is owned by leaf dialog.", owner.equals (w.getOwner ()));
        assertEquals ("The leaf dialog has no child.", 0, owner.getOwnedWindows ().length);
        
        assertTrue ("Leaf is visible", owner.isVisible ());
        assertTrue ("Child is visible", child.isVisible ());
        
        // close the leaf window
        postInAwtAndWaitOutsideAwt (new Runnable () {
            @Override
            public void run () {
                owner.setVisible (false);
            }
        });
        assertShowing("Disappear", false, owner);
        
        assertFalse ("Leaf is dead", owner.isVisible ());
        assertTrue ("Child is visible still", child.isVisible ());

        w.setVisible(false);
        assertShowing("Child is invisible", false, child);
        
        assertFalse ("Child is dead too", child.isShowing());
    }
    
    @RandomlyFails
    public void testNonLeafDialog () throws Exception {
        boolean leaf = false;
        DialogDescriptor ownerDD = new DialogDescriptor (pane, "Owner", true, new Object[] {closeOwner}, null, 0, null, null, leaf);
        final Dialog owner = DialogDisplayer.getDefault ().createDialog (ownerDD);
        
        // make leaf visible
        postInAwtAndWaitOutsideAwt (new Runnable () {
            @Override
            public void run () {
                owner.setVisible (true);
            }
        });
        assertShowing("Owner is visible", true, owner);
        
        child = DialogDisplayer.getDefault ().createDialog (childDD);

        // make the child visible
        postInAwtAndWaitOutsideAwt (new Runnable () {
            @Override
            public void run () {
                child.setVisible (true);
            }
        });
        assertShowing("child is visible too", true, child);
        
        Window w = (Window)child;
        assertTrue ("The child is owned by leaf dialog.", owner.equals (w.getOwner ()));
        assertEquals ("The leaf dialog has one child.", 1, owner.getOwnedWindows ().length);
        
        assertTrue ("Leaf is visible", owner.isVisible ());
        assertTrue ("Child is visible", child.isVisible ());
        
        // close the leaf window
        postInAwtAndWaitOutsideAwt (new Runnable () {
            @Override
            public void run () {
                owner.setVisible (false);
            }
        });
        assertShowing("Onwer is invisible", false, owner);
        
        assertFalse ("Leaf is dead", owner.isVisible ());
        assertFalse ("Child is dead too", child.isVisible ());
    }
    
    public void testParent() {
        DialogDescriptor dd = new DialogDescriptor (pane, "Owner");
        Dialog dlg = DialogDisplayer.getDefault ().createDialog (dd, null);
        assertEquals(WindowManager.getDefault().getMainWindow(), dlg.getOwner());
        Frame frame = new Frame();
        dlg = DialogDisplayer.getDefault ().createDialog (dd ,frame);
        assertEquals(frame, dlg.getOwner());
    }
    
    @RandomlyFails
    public void testNestedDialogParent() throws Exception {
        Frame f = null;
        Dialog owner = new Dialog(f, true);
        postInAwtAndWaitOutsideAwt(() -> owner.setVisible(true));
        assertShowing("Owner is invisible", true, owner);

        child = new JButton();
        final NotifyDescriptor nd = new NotifyDescriptor.Message(child);
        postInAwtAndWaitOutsideAwt(() -> DialogDisplayer.getDefault().notify(nd));

        assertShowing("Child is invisible", true, child);
        Window w = SwingUtilities.windowForComponent(child);
        assertSame("Window parent is not owner", owner, w.getParent());
    }

    static void postInAwtAndWaitOutsideAwt (final Runnable run) throws Exception {
        // pendig to better implementation
        SwingUtilities.invokeLater (run);
//        Thread.sleep (10);
        while (EventQueue.getCurrentEvent () != null) {
//            Thread.sleep (10);
        }
    }
    
    private void waitAWT() throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() { @Override public void run() { } });
    }

    private void assertShowing(String msg, boolean showing, Component c) throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            if (c.isShowing() == showing) {
                break;
            }
            LOG.log(Level.INFO, "Another round ({0}): {1}", new Object[]{i, c});
            Thread.sleep(100);
        }
        assertEquals(msg, showing, c.isShowing());
    }
    
    /**
     * Checks that the CompletableFuture completes and the descriptor has the selected
     * option.
     */
    public void testNotifyCompletion1() throws Exception {
        SwingUtilities.invokeAndWait(() -> DialogDisplayerImpl.runDelayed());
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation ("AnyQuestion?");
        Object r = dd.notifyFuture(nd).thenApply((d) -> d.getValue()).get();
        assertEquals (RESULT, r);
    }

    /**
     * Checks that selecting CLOSED option will complete the Future exceptionally.
     */
    public void testNotifyCompletionUserClosed() throws Exception {
        SwingUtilities.invokeAndWait(() -> DialogDisplayerImpl.runDelayed());
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation ("AnyQuestion?");
        DialogDisplayerImpl dd2 = new DialogDisplayerImpl (NotifyDescriptor.CLOSED_OPTION);
        CompletableFuture<Object> cf =  dd2.notifyFuture(nd).thenApply((d) -> d.getValue());
        try {
            cf.get();
            fail("Should fail with CancellationException");
        } catch (ExecutionException ex) {
            assertTrue(ex.getCause() instanceof CancellationException);
        }
    }
    
    /**
     * Checks that selecting CANCEL option will complete the Future exceptionally.
     */
    public void testNotifyCompletionUserCancelled() throws Exception {
        SwingUtilities.invokeAndWait(() -> DialogDisplayerImpl.runDelayed());
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation ("AnyQuestion?");
        DialogDisplayerImpl dd2 = new DialogDisplayerImpl (NotifyDescriptor.CLOSED_OPTION);
        CompletableFuture<Object> cf =  dd2.notifyFuture(nd).thenApply((d) -> d.getValue());
        try {
            cf.get();
            fail("Should fail with CancellationException");
        } catch (ExecutionException ex) {
            assertTrue(ex.getCause() instanceof CancellationException);
        }
    }
    
    class P extends JPanel {
        CountDownLatch displayed = new CountDownLatch(1);

        @Override
        public void addNotify() {
            super.addNotify(); 
            SwingUtilities.invokeLater(() -> displayed.countDown());
        }
    }
    
    /**
     * Checks that cancel to the Future returned from the dialog will close the dialog
     * @throws Exception 
     */
    public void testUserCancelClosesDialog() throws Exception {
        P panel = new P();
        SwingUtilities.invokeAndWait(() -> DialogDisplayerImpl.runDelayed());
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(panel);
        
        CompletableFuture<NotifyDescriptor> cf = DialogDisplayer.getDefault().notifyFuture(nd);
        Throwable[] ex = new Throwable[1];
        
        CompletableFuture<NotifyDescriptor> cf2 = cf.
            exceptionally((t) -> {
                ex[0] = t;
                return null; 
            }).
            thenApply((x) -> x);
        
        // wait for the dialog to be displayed
        assertTrue(panel.displayed.await(10, TimeUnit.SECONDS));
        
        cf.cancel(true);
        
        NotifyDescriptor d = cf2.get(10, TimeUnit.SECONDS);
        
        assertNull(d);
        assertTrue(ex[0] instanceof CancellationException);
    }
    
    public void testExampleHandlingWithCancel() throws Exception {
        SwingUtilities.invokeAndWait(() -> DialogDisplayerImpl.runDelayed());
        
        class UserData {
            String answer1;
            String answer2;
        }
        
        NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine("Question", "Title");
        CompletableFuture<NotifyDescriptor.InputLine> cf = DialogDisplayer.getDefault().notifyFuture(nd);
        
        // chain additional processing on normal completion, and substitute some default on error
        cf.thenApply(d -> {
            UserData userData = new UserData();
            // this will not execute, if user cancels "Question" dialog. Neither will execute subsequent steps.
            userData.answer1 = d.getInputText();
            // do something with user input and display another question
            NotifyDescriptor.InputLine nd2 = new NotifyDescriptor.InputLine("Question2", "Title");
        
            return DialogDisplayer.getDefault().notifyFuture(nd).
                thenApply(x -> {
                // pass userData to the next step
                userData.answer2 = x.getInputText();
                return userData;
            });
        }).thenApply((data) -> {
            // do some finalization steps. This code will not execute if Question or Question2 is cancelled.
            return data;
        }).exceptionally(ex -> {
            if (!(ex instanceof CancellationException)) {
                // do error handling
            }
            return null;
        });
    }

}
