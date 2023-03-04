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
        final List<Entry> entries = entrySupport().getEntries();
        // use entries count rather than node count, as some keys may result in no
        // nodes, but they're still add()ed.
        
        // TODO: the refresh is not completely correct: if the ChildFactory inserts a (really!) new
        // key into the list of existing ones, the count-based detection causes setKeys()
        // to be callled and the node(s) for the yet-not-reported keys will be formally deleted
        // and later re-created. But if content is only added or unchanged, the refresh won't
        // cause nodes to be deleted + recreated.
        // Implementation detail: there's one extra fixed Entry (Children.AE) 
        // that represents this dynamic node array. 
        final int minimalCount = Math.max(entries.size() - 1, 0);
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
