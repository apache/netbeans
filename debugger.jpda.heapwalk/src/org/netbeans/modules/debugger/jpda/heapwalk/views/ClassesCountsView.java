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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.debugger.jpda.heapwalk.views;

import org.netbeans.lib.profiler.heap.Heap;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.lang.ref.WeakReference;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.jpda.JPDADebugger;

import org.netbeans.modules.profiler.heapwalk.ClassesController;
import org.netbeans.modules.profiler.heapwalk.ClassesListController;
import org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker;

import org.netbeans.modules.debugger.jpda.heapwalk.HeapImpl;


import org.openide.ErrorManager;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.windows.TopComponent;

/**
 * The Instances View that refers to Profiler Heap Walking UI
 * 
 * @author Martin Entlicher
 */
public class ClassesCountsView extends TopComponent implements org.openide.util.HelpCtx.Provider {
    
    private transient EngineListener listener;
    private transient JPanel content;
    private transient HeapFragmentWalker hfw;
    private transient ClassesListController clc;
    private transient RequestProcessor defaultRP = new RequestProcessor(ClassesCountsView.class.getName());

    /**
     * Creates a new instance of ClassesCountsView
     */
    public ClassesCountsView () {
        setIcon (ImageUtilities.loadImage ("org/netbeans/modules/debugger/resources/classesView/Classes.png")); // NOI18N
        setLayout (new BorderLayout ());
        // Remember the location of the component when closed.
        putClientProperty("KeepNonPersistentTCInModelWhenClosed", Boolean.TRUE); // NOI18N
    }
    
    @Override
    protected String preferredID() {
        //return this.getClass().getName();
        // Return the ID of the old classes view:
        return "org.netbeans.modules.debugger.jpda.ui.views.ClassesView"; // NOI18N
    }
    
    
    private void setUp() {
        if (listener == null) {
            listener = new EngineListener();
            listener.start();
        }
        if (content == null) {
            listener.getRefreshContentTask().schedule(10);
            //setContent();
        }
    }
    
    private synchronized void tearDown() {
        if (content != null) {
            final JPanel tempContent = content;
            content = null;
            hfw = null;
            clc = null;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    remove(tempContent);
                }
            });
        }
    }
    
    private void setContent() {
        assert javax.swing.SwingUtilities.isEventDispatchThread();
        JPDADebugger debugger = null;
        DebuggerEngine engine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (engine != null) {
            debugger = engine.lookupFirst(null, JPDADebugger.class);
        }
        if (content != null) {
            remove(content);
            content = null;
        }
        if (debugger != null && debugger.canGetInstanceInfo()) {
            final JPDADebugger fDebugger = debugger;
            RequestProcessor rp = engine.lookupFirst(null, RequestProcessor.class);
            if (rp == null) {
                rp = defaultRP;
            }
            rp.post(new Runnable() {
                @Override
                public void run() {
                    Heap heap = new HeapImpl(fDebugger);
                    final HeapFragmentWalker hfw = new DebuggerHeapFragmentWalker(heap);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            ClassesController cc;
                            synchronized (ClassesCountsView.this) {
                                ClassesCountsView.this.hfw = hfw;
                                cc = hfw.getClassesController();
                                if (content != null) {
                                    remove(content);
                                }
                                content = cc.getPanel();
                                clc = cc.getClassesListController();
                                java.awt.Component header = cc.getClassesListController().getPanel().getComponent(0);
                                header.setVisible(false);
                                cc.getClassesListController().setColumnVisibility(3, false);
                                add(content, BorderLayout.CENTER);
                            }
                            repaint();
                            revalidate();
                        }
                    });
                }
            });
        }
    }
    
    private void refreshContent() {
        if (clc != null) {
            clc.updateData();
        } else {
            setContent();
        }
    }
    
    HeapFragmentWalker getCurrentFragmentWalker() {
        return hfw;
    }
    
    @Override
    protected void componentShowing () {
        super.componentShowing ();
            setUp();
    }
    
    @Override
    protected void componentHidden () {
        super.componentHidden ();
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
            tearDown();
            if (listener != null) {
                listener.stop();
                listener = null;
            }
    }
    
    // <RAVE>
    // Implement getHelpCtx() with the correct helpID
    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
            return new org.openide.util.HelpCtx("NetbeansDebuggerInstancesNode"); // NOI18N
    }
    // </RAVE>
    
    @Override
    public int getPersistenceType () {
        return PERSISTENCE_ALWAYS;
    }
        
    @Override
    public boolean requestFocusInWindow () {
        super.requestFocusInWindow ();
        if (content == null) {
            return false;
        }
        return content.requestFocusInWindow ();
    }
    
    @Override
    public String getName () {
        //return "Class Counts";
        return NbBundle.getMessage (ClassesCountsView.class, "CTL_Classes_view");
    }
    
    @Override
    public String getToolTipText () {
        //return "Class Counts";
        return NbBundle.getMessage (ClassesCountsView.class, "CTL_Classes_tooltip");// NOI18N
    }
    
    private final class EngineListener extends DebuggerManagerAdapter {
        
        private WeakReference<JPDADebugger> lastDebugger = new WeakReference<JPDADebugger>(null);
        private Task refreshTask;
        
        void start() {
            DebuggerManager.getDebuggerManager ().addDebuggerListener (
                DebuggerManager.PROP_CURRENT_ENGINE,
                this
            );
            DebuggerEngine engine = DebuggerManager.getDebuggerManager ().getCurrentEngine();
            attachToStateChange(engine);
        }
        
        void stop() {
            DebuggerManager.getDebuggerManager ().removeDebuggerListener (
                DebuggerManager.PROP_CURRENT_ENGINE,
                this
            );
            detachFromStateChange();
        }
        
        private synchronized void attachToStateChange(DebuggerEngine engine) {
            detachFromStateChange();
            if (engine == null) {
                return ;
            }
            JPDADebugger debugger = engine.lookupFirst(null, JPDADebugger.class);
            if (debugger == null) {
                return ;
            }
            debugger.addPropertyChangeListener(JPDADebugger.PROP_STATE, this);
            lastDebugger = new WeakReference<JPDADebugger>(debugger);
        }
        
        private synchronized void detachFromStateChange() {
            JPDADebugger debugger = lastDebugger.get();
            if (debugger != null) {
                debugger.removePropertyChangeListener(JPDADebugger.PROP_STATE, this);
            }
            if (refreshTask != null) {
                refreshTask.cancel();
                refreshTask = null;
            }
            tearDown();
        }
        
        @Override
        public void propertyChange (PropertyChangeEvent e) {
            if (e.getSource() instanceof JPDADebugger) {
                int state = ((JPDADebugger) e.getSource()).getState();
                if (state == JPDADebugger.STATE_DISCONNECTED) {
                    detachFromStateChange();
                } else if (state != JPDADebugger.STATE_STARTING) {
                    getRefreshContentTask().schedule(10);
                }
                return ;
            }
            DebuggerEngine engine = (DebuggerEngine) e.getNewValue();
            attachToStateChange(engine);
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setContent();
                    getRefreshContentTask().schedule(10);
                }
            });
        }
        
        private synchronized Task getRefreshContentTask() {
            if (refreshTask == null) {
                refreshTask = defaultRP.create(new Runnable() {
                    @Override
                    public void run() {
                        try {
                        HeapFragmentWalker fragmentWalker = ClassesCountsView.this.hfw;
                        if (fragmentWalker != null) {
                            Heap heap = fragmentWalker.getHeapFragment();
                            ((HeapImpl) heap).computeClasses();
                        }

                        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                            @Override
                            public void run() {
                                refreshContent();
                            }
                        });
                        } catch (InterruptedException iex) {
                            return ;
                        } catch (java.lang.reflect.InvocationTargetException itex) {
                            ErrorManager.getDefault().notify(itex);
                        }
                        JPDADebugger debugger;
                        synchronized (EngineListener.this) {
                            debugger = lastDebugger.get();
                        }
                        if (debugger != null) {
                            if (debugger.getState() == JPDADebugger.STATE_RUNNING) {
                                synchronized (ClassesCountsView.this) {
                                    if (refreshTask != null) {
                                        refreshTask.schedule(2000);
                                    }
                                }
                            }
                        }
                    }
                });
            }
            return refreshTask;
        }
    
    }
}
