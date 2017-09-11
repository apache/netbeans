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
package org.openide.nodes;

import java.awt.EventQueue;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.openide.util.RequestProcessor;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Children object which creates its keys on a background thread.  To use,
 * implement {@link ChildFactory} and pass that to the constructor.
 *
 * @author Tim Boudreau
 * @param T the type of key object used to create the child nodes
 */
final class AsynchChildren <T> extends Children.Keys <Object> implements 
                                                          ChildFactory.Observer, 
                                                          Runnable {
    private final ChildFactory<T> factory;
    private final RequestProcessor.Task task;
    private static final RequestProcessor PROC = new RequestProcessor("Asynch " //NOI18N
             + "children creator ", 4, true); //NOI18N
    private static final Logger logger = Logger.getLogger(AsynchChildren.class.getName());
    /**
     * Create a new AsyncChildren instance with the passed provider object
     * which will manufacture key objects and nodes.
     * @param factory An object which can provide a list of keys and make
     *        Nodes for them
     */ 
    AsynchChildren(ChildFactory<T> factory) {
        this.factory = factory;
        task = PROC.create(this, true);
    }
    
    volatile boolean initialized = false;
    protected @Override void addNotify() {
        logger.log (Level.FINER, "addNotify on {0}", new Object[] { this });
        if ((!initialized && task.isFinished()) || cancelled) {
            cancelled = false;
            Node n = factory.getWaitNode();
            if (n != null) {
                setKeys (new Object[] { n });
            }
            task.schedule(0);
        }
    }
    
    protected @Override void removeNotify() {
        logger.log (Level.FINER, "removeNotify on {0}", new Object[] { this });
        try {
            cancelled = true;
            task.cancel();
            initialized = false;
            setKeys (Collections.<Object>emptyList());
        } finally {
            synchronized (notifyLock) { //#170794 ensure setting of flag and call to add/removeNotify() are atomic
                if (notified) {
                    factory.removeNotify();
                }
            }
        }
    }
    
    /**
     * Notify this AsynchChildren that it should reconstruct its children,
     * calling <code>provider.asynchCreateKeys()</code> and setting the
     * keys to that.  Call this method if the list of child objects is known
     * or likely to have changed.
     * @param immediate If true, the keys are updated synchronously from the
     *  calling thread.  Set this to true only if you know that updating
     *  the keys will <i>not</i> be an expensive or time-consuming operation.
     */ 
    public void refresh(boolean immediate) {
        immediate &= !EventQueue.isDispatchThread();
        logger.log (Level.FINE, "Refresh on {0} immediate {1}", new Object[]  //NOI18N
            { this, immediate });
        if (logger.isLoggable(Level.FINEST)) {
            logger.log (Level.FINEST, "Refresh: ", new Exception()); //NOI18N
        }
        if (immediate) {
            boolean done;
            List <T> keys = new LinkedList <T> ();
            do {
                done = factory.createKeys(keys);
            } while (!done);
            setKeys (keys);
        } else {
            task.schedule (0);
        }
    }

    @Override
    public Node[] getNodes(boolean optimalResult) {
        Node[] result = super.getNodes();
        if (optimalResult) {
            // The getNodes() call above called addNotify() and started the task
            // for the first time if needed.
            task.waitFinished();
            result = super.getNodes();
        }
        return result;
    }

    @Override
    public Node findChild(String name) {
        Node[] result = getNodes(true);
        return super.findChild(name);
    }

    @SuppressWarnings("unchecked") // Union2<T,Node> undesirable since refresh could not use raw keys list
    protected Node[] createNodes(Object key) {
        if (ChildFactory.isWaitNode(key)) {
            return new Node[] { (Node) key };
        } else {
            return factory.createNodesForKey ((T) key);
        }
    }

    @Override
    protected void destroyNodes(Node[] arr) {
        super.destroyNodes(arr);
        factory.destroyNodes(arr);
    }

    volatile boolean cancelled = false;
    volatile boolean notified;
    private final Object notifyLock = new Object();
    private static final class Stop extends RuntimeException {}
    public void run() {
        boolean fail = cancelled || Thread.interrupted();
        logger.log (Level.FINE, "Running background children creation on " + //NOI18N
                "{0} fail = {1}", new Object[] { this, fail }); //NOI18N
        if (fail) {
            setKeys (Collections.<T>emptyList());
            return;
        }
        final int minimalCount = getNodesCount();
        List <T> keys = new LinkedList <T> () {
            @Override public boolean add(T e) {
                if (cancelled || Thread.interrupted()) {
                    throw new Stop();
                }
                super.add(e);
                LinkedList<Object> newKeys = new LinkedList<Object>(this);
                Node n = factory.getWaitNode();
                if (n != null) {
                    newKeys.add(n);
                }
                newKeys.removeAll(Collections.singleton(null)); // #206958
                if (newKeys.size() > minimalCount) {
                    setKeys(newKeys);
                }
                return true;
            }
            // #206556 Y02 - could override other mutator methods if ever needed
        };
        boolean done;
        do {
            synchronized (notifyLock) {
                if (!notified) {
                    notified = true;
                    factory.addNotify();
                }
            }
            if (cancelled || Thread.interrupted()) {
                setKeys (Collections.<T>emptyList());
                return;
            }
            try {
                done = factory.createKeys(keys);
            } catch (Stop stop) {
                done = true;
            }
            if (cancelled || Thread.interrupted()) {
                setKeys (Collections.<T>emptyList());
                return;
            }
            LinkedList<Object> newKeys = new LinkedList<Object>(keys);
            if (!done) {
                Node n = factory.getWaitNode();
                if (n != null) {
                    newKeys.add(n);
                }
            }
            setKeys (newKeys);
        } while (!done && !Thread.interrupted() && !cancelled);
        initialized = done;
    }

    @Override
    public String toString() {
        return super.toString() + "[" + factory + "]";
    }
}
