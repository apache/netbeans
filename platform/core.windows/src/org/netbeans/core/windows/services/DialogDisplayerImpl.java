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
import java.awt.Container;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Mutex;
import org.openide.windows.WindowManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import org.netbeans.core.windows.view.ui.DefaultSeparateContainer;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of <code>org.openide.DialogDisplayer</code>.
 *
 * @author  Jesse Glick
 */
@ServiceProvider(service=DialogDisplayer.class)
public class DialogDisplayerImpl extends DialogDisplayer {
    /** delayed runnables */
    private static List<Runnable> run = Collections.synchronizedList(new ArrayList<Runnable>());
    
    /** non-null if we are running in unit test and should no show any dialogs */
    private Object testResult;
    
    /** Creates a new instance of DialogDisplayerImpl */
    public DialogDisplayerImpl() {
        this (null);
    }
    
    DialogDisplayerImpl (Object testResult) {
        this.testResult = testResult;
    }
    
    /* Runs list of tasks gathered from notifyLater calls */
    public static void runDelayed() {
        NbPresenter.LOG.fine("runDelayed");
        List<Runnable> local = run;
        run = null;
        if (local == null) {
            NbPresenter.LOG.fine("runDelayed, nothing");
            return;
        }
        
        assert EventQueue.isDispatchThread();
        for (Runnable r : local) {
            NbPresenter.LOG.fine("runDelayed, run = " + r);
            r.run();
        }
        NbPresenter.LOG.fine("runDelayed, done");
    }
    
    public Dialog createDialog (final DialogDescriptor d) {
        return createDialog(d, null);
    }

    /** Creates new dialog. */
    public Dialog createDialog (final DialogDescriptor d, final Frame preferredParent) {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
        return Mutex.EVENT.readAccess (new Mutex.Action<Dialog> () {
            public Dialog run () {
                // if a modal dialog active use it as parent
                // otherwise use the main window
                if (NbPresenter.currentModalDialog != null) {
                    NbDialog dlg;
                    if (NbPresenter.currentModalDialog.isLeaf ()) {
                        dlg = new NbDialog(d, WindowManager.getDefault ().getMainWindow ());
                    } else {
                        dlg = new NbDialog(d, NbPresenter.currentModalDialog);
                    }
                    customizeDlg(dlg);
                    return dlg;
                }
                else {
                    Window w = preferredParent;
                    if( null == w ) {
                        w = KeyboardFocusManager.getCurrentKeyboardFocusManager ().getActiveWindow ();
                        if (!(w instanceof NbPresenter) || !w.isVisible()) {
                            // undocked window is not instanceof NbPresenter although it's NetBeans's native window
                            // all docked windows implements ModeUIBase interface
                            if (! (w instanceof DefaultSeparateContainer.ModeUIBase)) {
                                Container cont = SwingUtilities.getAncestorOfClass(Window.class, w);
                                if (cont != null && (cont instanceof DefaultSeparateContainer.ModeUIBase)) {
                                    w = (Window) cont;
                                } else {
                                    // don't set non-ide window as parent
                                    w = WindowManager.getDefault ().getMainWindow ();
                                }
                            }
                        } else if (w instanceof NbPresenter && ((NbPresenter) w).isLeaf ()) {
                            w = WindowManager.getDefault ().getMainWindow ();
                        }
                    }
                    NbDialog dlg;
                    if (w instanceof Dialog) {
                        dlg = new NbDialog(d, (Dialog) w);
                    } else {
                        Frame f = w instanceof Frame ? (Frame) w : WindowManager.getDefault ().getMainWindow ();
                        dlg = new NbDialog(d, f);
                    }
                    customizeDlg(dlg);
                    dlg.requestFocusInWindow ();
                    return dlg;
                }
            }
        });
    }
    
    /** Notifies user by a dialog.
     * @param descriptor description that contains needed informations
     * @return the option that has been choosen in the notification.
     */
    public Object notify (NotifyDescriptor descriptor) {
        return notify(descriptor, false);
    }

    /** Notifies user by a dialog.
     * @param descriptor description that contains needed informations
     * @param noParent don't set any window as parent of dialog, if flag is true
     * @return the option that has been choosen in the notification.
     */
    private Object notify (final NotifyDescriptor descriptor, final boolean noParent) {
        if (GraphicsEnvironment.isHeadless()) {
            return NotifyDescriptor.CLOSED_OPTION;
        }
        class AWTQuery implements Runnable {
            public Object result;
            public boolean running;
        
            public void run () {
                synchronized (this) {
                    notify ();   
                    running = true;
                }
                
                showDialog ();

                synchronized (this) {
                    this.result = descriptor.getValue();
                    notifyAll ();
                }
            }
            
            @SuppressWarnings("deprecation")
            public void showDialog () {
                if (testResult != null) {
                    // running in Unit test
                    descriptor.setValue (testResult);
                    return;
                }
                
                Component focusOwner = null;
                Component comp = org.openide.windows.TopComponent.getRegistry ().getActivated ();
                Component win = comp;
                while ((win != null) && (!(win instanceof Window))) win = win.getParent ();
                if (win != null) focusOwner = ((Window)win).getFocusOwner ();

                // if a modal dialog is active use it as parent
                // otherwise use the main window

                NbPresenter presenter = null;
                if (descriptor instanceof DialogDescriptor) {
                    if (NbPresenter.currentModalDialog != null) {
                        if (NbPresenter.currentModalDialog.isLeaf ()) {
                            presenter = new NbDialog((DialogDescriptor) descriptor, WindowManager.getDefault ().getMainWindow ());
                        } else {
                            presenter = new NbDialog((DialogDescriptor) descriptor, NbPresenter.currentModalDialog);
                        }
                    } else {
                        Window w = KeyboardFocusManager.getCurrentKeyboardFocusManager ().getActiveWindow ();
                        if (w instanceof NbPresenter && ((NbPresenter) w).isLeaf ()) {
                            w = WindowManager.getDefault ().getMainWindow ();
                        }
                        Frame f = w instanceof Frame ? (Frame) w : WindowManager.getDefault().getMainWindow();
                        if (noParent) {
                            f = null;
                        }
                        presenter = new NbDialog((DialogDescriptor) descriptor, f);
                    }
                } else {
                    if (NbPresenter.currentModalDialog != null) {
                        if (NbPresenter.currentModalDialog.isLeaf()) {
                            presenter = new NbPresenter(descriptor, WindowManager.getDefault().getMainWindow(), true);
                        } else {
                            presenter = new NbPresenter(descriptor, NbPresenter.currentModalDialog, true);
                        }
                    } else {
                        Frame f = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow() 
                            instanceof Frame ? 
                            (Frame) KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow() 
                            : WindowManager.getDefault().getMainWindow();
                        
                        if (noParent) {
                            f = null;
                        }
                        presenter = new NbPresenter(descriptor, f, true);
                    }
                }
                
                //#47150 - horrible hack for vcs module
                if ("true".equals(System.getProperty("javahelp.ignore.modality"))) { //NOI18N
                    presenter.getRootPane().putClientProperty ("javahelp.ignore.modality", "true"); //NOI18N
                    System.setProperty("javahelp.ignore.modality", "false"); //NOI18N
                }
                
                customizeDlg(presenter);

                //Bugfix #8551
                presenter.getRootPane().requestDefaultFocus();
                presenter.setVisible(true);

                // dialog is gone, restore the focus

                if (focusOwner != null) {
                    win.requestFocusInWindow();
                    comp.requestFocusInWindow();
                    if( !(focusOwner instanceof JRootPane ) ) //#85068
                        focusOwner.requestFocusInWindow();
                }
            }
        }
        
        AWTQuery query = new AWTQuery ();
        
        if (javax.swing.SwingUtilities.isEventDispatchThread ()) {
            query.showDialog ();
            return descriptor.getValue ();
        }
        
        synchronized (query) {
            javax.swing.SwingUtilities.invokeLater (query);
            try {
                query.wait (10000);
            } catch (InterruptedException ex) {
                // ok, should not happen and does not matter
            }
            
            if (query.running) {
                while (query.result == null) {
                    try {
                        query.wait (3000);
                    } catch (InterruptedException ex) {
                        // one more round
                    }
                }
                return query.result;
            } else {
                return NotifyDescriptor.CLOSED_OPTION;
            }
        }
    }

    /* Schedules notification for specific later time if called before
     * <code>runDelayed</code>, otherwise works as superclass method.
     */  
    @Override
    public void notifyLater(final NotifyDescriptor descriptor) {
        class R implements Runnable {
            public boolean noParent;
            
            public void run() {
                DialogDisplayerImpl.this.notify(descriptor, noParent);
            }
        }
        R r = new R();
        
        List<Runnable> local = run;
        if (local != null) {
            r.noParent = true;
            local.add(r);
        } else {
            EventQueue.invokeLater(r);
        }
    }
    private static void customizeDlg(NbPresenter presenter) {
        for (PresenterDecorator p : Lookup.getDefault().lookupAll(PresenterDecorator.class)) {
            p.customizePresenter(presenter);
        }
    }
}
