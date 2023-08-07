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
import java.awt.Window;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Mutex;
import org.openide.windows.WindowManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import org.netbeans.core.windows.view.ui.DefaultSeparateContainer;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
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
                Window w = preferredParent;
                if (w != null) {
                    // Verify the preferred parent
                    Component p = Utilities.findDialogParent(w);
                    if (p != w) {
                        w = null;
                    }
                }
                if (w == null) {
                    w = findDialogParent();
                    if (!(w instanceof NbPresenter) || !w.isVisible()) {
                        // undocked window is not instanceof NbPresenter although it's NetBeans's native window
                        // all docked windows implements ModeUIBase interface
                        if (! (w instanceof DefaultSeparateContainer.ModeUIBase)) {
                            Container cont = SwingUtilities.getAncestorOfClass(Window.class, w);
                            if (cont instanceof DefaultSeparateContainer.ModeUIBase) {
                                w = (Window) cont;
                            } else {
                                // don't set non-ide window as parent
                                w = WindowManager.getDefault ().getMainWindow ();
                            }
                        }
                    }
                }
                NbDialog dlg;
                if (w instanceof Frame) {
                    dlg = new NbDialog(d, (Frame) w);
                } else if (w instanceof Dialog) {
                    dlg = new NbDialog(d, (Dialog) w);
                } else {
                    dlg = new NbDialog(d, WindowManager.getDefault().getMainWindow());
                }
                customizeDlg(dlg);
                dlg.requestFocusInWindow ();
                return dlg;
            }
        });
    }
    
    private Window findDialogParent() {
        Component parentComponent = Utilities.findDialogParent(null);
        Window parent = findDialogParent(parentComponent);
        if (parent == null || parent == JOptionPane.getRootFrame()
                || parent instanceof NbPresenter && ((NbPresenter) parent).isLeaf()) {
            return WindowManager.getDefault().getMainWindow();
        }
        return parent;
    }

    private Window findDialogParent(Component component) {
        if (component == null) {
            return null;
        }
        if (component instanceof Frame || component instanceof Dialog) {
            return (Window) component;
        }
        return findDialogParent(component.getParent());
    }

    /** Notifies user by a dialog.
     * @param descriptor description that contains needed informations
     * @return the option that has been choosen in the notification.
     */
    public Object notify (NotifyDescriptor descriptor) {
        return notify(descriptor, new AWTQuery (descriptor));
    }


    class AWTQuery implements Runnable {
        public final NotifyDescriptor descriptor;
        
        public Object result;
        public boolean running;
        public volatile boolean noParent;
        public volatile boolean cancelled;
        
        // @GuardedBy(this)
        NbPresenter presenter;

        public AWTQuery(NotifyDescriptor descriptor) {
            this.descriptor = descriptor;
        }

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
        
        public boolean cancel(boolean cancelIfRunning) {
            NbPresenter p;
            synchronized (this) {
                if (cancelled) {
                    return false;
                }
                if (running && !cancelIfRunning) {
                    return false;
                }
                this.cancelled = true;
                p = this.presenter;
                if (p == null) {
                    return true;
                }
            }
            // attempt to cancel the dialog:
            if (SwingUtilities.isEventDispatchThread()) {
                p.setVisible(false);
            } else {
                SwingUtilities.invokeLater(() -> p.setVisible(false));
            }
            return true;
        }

        @SuppressWarnings("deprecation")
        public void showDialog () {
            if (cancelled) {
                descriptor.setValue(NotifyDescriptor.CLOSED_OPTION);
                return;
            }
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

            NbPresenter presenter;
            Window parent = noParent ? null : findDialogParent();

            if (descriptor instanceof DialogDescriptor) {
                if (parent instanceof Dialog) {
                    presenter = new NbDialog((DialogDescriptor) descriptor, (Dialog) parent);
                } else if (parent instanceof Frame) {
                    presenter = new NbDialog((DialogDescriptor) descriptor, (Frame) parent);
                } else {
                    presenter = new NbDialog((DialogDescriptor) descriptor, (Frame) null);
                }
            } else {
                if (parent instanceof Dialog) {
                    presenter = new NbPresenter(descriptor, (Dialog) parent, true);
                } else if (parent instanceof Frame) {
                    presenter = new NbPresenter(descriptor, (Frame) parent, true);
                } else {
                    presenter = new NbPresenter(descriptor, (Frame) null, true);
                }
            }
            
            synchronized (this) {
                this.presenter = presenter;
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
    /** Notifies user by a dialog.
     * @param descriptor description that contains needed informations
     * @param noParent don't set any window as parent of dialog, if flag is true
     * @return the option that has been choosen in the notification.
     */
    private Object notify (final NotifyDescriptor descriptor, AWTQuery query) {
        if (GraphicsEnvironment.isHeadless()) {
            return NotifyDescriptor.CLOSED_OPTION;
        }
        
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
        notifyLater(new AWTQuery(descriptor));
    }
    
    private void notifyLater(final AWTQuery q) {
        Runnable r = () -> DialogDisplayerImpl.this.notify(q.descriptor, q);
        
        List<Runnable> local = run;
        if (local != null) {
            q.noParent = true;
            local.add(r);
        } else {
            Mutex.EVENT.postReadRequest(r);
        }
    }
    
    private static void customizeDlg(NbPresenter presenter) {
        for (PresenterDecorator p : Lookup.getDefault().lookupAll(PresenterDecorator.class)) {
            p.customizePresenter(presenter);
        }
    }

    @Override
    public <T extends NotifyDescriptor> CompletableFuture<T> notifyFuture(T descriptor) {
        class AWTQuery2 extends AWTQuery {
            volatile CompletableFuture res;

            public AWTQuery2(NotifyDescriptor descriptor) {
                super(descriptor);
            }

            @Override
            public void showDialog() {
                try {
                    super.showDialog();
                    Object r = descriptor.getValue();
                    if (cancelled || r == NotifyDescriptor.CLOSED_OPTION || r == NotifyDescriptor.CANCEL_OPTION) {
                        res.completeExceptionally(new CancellationException());
                    } else {
                        res.complete(descriptor);
                    }
                } catch (ThreadDeath td) {
                    throw td;
                } catch (Throwable t) {
                    res.completeExceptionally(t);
                }
            }
        }
        
        final AWTQuery2 q = new AWTQuery2(descriptor);
        
        class CF extends CompletableFuture<T> {
            {
                q.res = this;
            }
            
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                if (isDone()) {
                    return false;
                }
                q.cancel(mayInterruptIfRunning);
                return super.cancel(mayInterruptIfRunning); 
            }
        }
        
        CompletableFuture<T> cf =  new CF();
        q.res = cf;
        notifyLater(q);
        return cf;
    }
}
