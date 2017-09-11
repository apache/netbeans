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

package org.netbeans.modules.debugger.jpda.ui.models;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;

import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.WeakListeners;


/**
 * This tree model provides an array of CallStackFrame objects.
 *
 * @author Jan Jancura, Martin Entlicher
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/CallStackView", types={TreeModel.class})
public class CallStackTreeModel implements TreeModel {

    private static boolean verbose = 
        (System.getProperty ("netbeans.debugger.viewrefresh") != null) &&
        (System.getProperty ("netbeans.debugger.viewrefresh").indexOf ('c') >= 0);
    
    private JPDADebuggerImpl            debugger;
    private Collection<ModelListener>   listeners = new HashSet<ModelListener>();
    private Listener                    listener;
    
   
    public CallStackTreeModel (ContextProvider lookupProvider) {
        debugger = (JPDADebuggerImpl) lookupProvider.
            lookupFirst (null, JPDADebugger.class);
    }
    
    /** 
     *
     * @return threads contained in this group of threads
     */
    public Object[] getChildren (Object parent, int from, int to) 
    throws UnknownTypeException {
        if ( parent.equals (ROOT) ||
             (parent instanceof JPDAThread) 
        ) {
            // 1) get Thread
            JPDAThread thread;
            if (parent.equals (ROOT)) {
                thread = debugger.getCurrentThread ();
            } else {
                thread = (JPDAThread) parent;
            }
            if (thread == null) {
                return new String[] {"No current thread"}; // TODO make localizable!!!
            }
            
            // 2) get StackFrames for this Thread
            try {
                CallStackFrame[] sfs = thread.getCallStack(from, to);
                return sfs;
            } catch (AbsentInformationException aiex) {
                if (aiex.getCause() instanceof IncompatibleThreadStateException) {
                    return new String[] {"Thread is running"}; // TODO make localizable!!!
                } else {
                    return new String[] {"No call stack information available."}; // TODO make localizable!!!
                }
            }
        } else
        throw new UnknownTypeException (parent);
    }
    
    /**
     * Returns number of children for given node.
     * 
     * @param   node the parent node
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     *
     * @return  true if node is leaf
     */
    public int getChildrenCount (Object parent) throws UnknownTypeException {
        if ( parent.equals (ROOT) ||
             (parent instanceof JPDAThread) 
        ) {
            // Performance, see issue #59058.
            return Integer.MAX_VALUE;
            /*
            // 1) get Thread
            JPDAThread thread;
            if (parent.equals (ROOT)) {
                thread = debugger.getCurrentThread ();
            } else {
                thread = (JPDAThread) parent;
            }
            if (thread == null) {
                return 1; //new String [] {"No current thread"};
            }
            
            return thread.getStackDepth();
             */
        } else
        throw new UnknownTypeException (parent);
    }
    
    /** 
     *
     * @return threads contained in this group of threads
     */
    public Object getRoot () {
        return ROOT;
    }
    
    public boolean isLeaf (Object node) throws UnknownTypeException {
        if (node.equals (ROOT)) return false;
        if (node instanceof CallStackFrame) return true;
        throw new UnknownTypeException (node);
    }

    /** 
     *
     * @param l the listener to add
     */
    public void addModelListener (ModelListener l) {
        synchronized (listeners) {
            listeners.add (l);
            if (listener == null) {
                listener = new Listener (this, debugger);
            }
        }
    }

    /** 
     *
     * @param l the listener to remove
     */
    public void removeModelListener (ModelListener l) {
        synchronized (listeners) {
            listeners.remove (l);
            if (listeners.size () == 0) {
                listener.destroy ();
                listener = null;
            }
        }
    }
    
    public void fireTreeChanged () {
        ModelListener[] ls;
        synchronized (listeners) {
            ls = listeners.toArray(new ModelListener[0]);
        }
        ModelEvent ev = new ModelEvent.TreeChanged(this);
        for (int i = 0; i < ls.length; i++) {
            ls[i].modelChanged (ev);
        }
    }
    
    
    /**
     * Listens on JPDADebugger on PROP_STATE
     */
    private static class Listener implements PropertyChangeListener {
        
        private JPDADebugger debugger;
        private WeakReference<CallStackTreeModel> model;
        
        public Listener (
            CallStackTreeModel tm,
            JPDADebugger debugger
        ) {
            this.debugger = debugger;
            model = new WeakReference<CallStackTreeModel>(tm);
            debugger.addPropertyChangeListener (this);
            JPDAThreadImpl lastCurrentThread = (JPDAThreadImpl) debugger.getCurrentThread();
            if (lastCurrentThread != null) {
                lastCurrentThread.addPropertyChangeListener(
                        WeakListeners.propertyChange(this, lastCurrentThread));
            }
        }
        
        private CallStackTreeModel getModel () {
            CallStackTreeModel tm = model.get ();
            if (tm == null) {
                destroy ();
            }
            return tm;
        }
        
        void destroy () {
            debugger.removePropertyChangeListener (this);
            if (task != null) {
                // cancel old task
                task.cancel ();
                if (verbose)
                    System.out.println("CSTM cancel old task " + task);
                task = null;
            }
        }
        
        // currently waiting / running refresh task
        // there is at most one
        private Task task;
        
        // check also whether the current thread was resumed/suspended
        // the call stack needs to be refreshed after invokeMethod() which resumes the thread
        public synchronized void propertyChange (PropertyChangeEvent e) {
            boolean refresh = false;
            String propertyName = e.getPropertyName();
            if (propertyName == debugger.PROP_CURRENT_THREAD) {
                JPDAThreadImpl lastCurrentThread = (JPDAThreadImpl) debugger.getCurrentThread();
                if (lastCurrentThread != null) {
                    lastCurrentThread.addPropertyChangeListener(
                            WeakListeners.propertyChange(this, lastCurrentThread));
                    refresh = true;
                }
            }
            if (propertyName == JPDAThread.PROP_SUSPENDED
                    && Boolean.TRUE.equals(e.getNewValue())) {
                if (e.getSource() == debugger.getCurrentThread()) {
                    refresh = true;
                }
            }
            if ((propertyName == debugger.PROP_STATE)
                 && (debugger.getState() == debugger.STATE_STOPPED)
            ) {
                refresh = true;
            }
            if (refresh) {
                synchronized (this) {
                    if (task == null) {
                        task = ((JPDADebuggerImpl) debugger).getRequestProcessor().create(new Refresher());
                    }
                    task.schedule(200);
                }
            }
        }
        
        private class Refresher extends Object implements Runnable {
            public void run() {
                if (debugger.getState () == debugger.STATE_STOPPED) {
                    CallStackTreeModel tm = getModel ();
                    if (tm != null) {
                        tm.fireTreeChanged();
                    }
                }
            }
        }
    }
}

