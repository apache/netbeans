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

package threaddemo.apps.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.WeakListeners;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import threaddemo.data.DomProvider;
import threaddemo.data.PhadhailLookups;
import threaddemo.locking.RWLock;
import threaddemo.model.Phadhail;
import threaddemo.model.PhadhailEvent;
import threaddemo.model.PhadhailListener;
import threaddemo.model.PhadhailNameEvent;

// XXX make an IndexImpl be GCable and not hold onto Phadhail's

/**
 * Actual implementation of the index.
 * @author Jesse Glick
 */
final class IndexImpl implements Index, Runnable, PhadhailListener, ChangeListener {
    
    private static final Logger logger = Logger.getLogger(IndexImpl.class.getName());
    
    private final Phadhail root;
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private boolean running = false;
    private final LinkedList<Phadhail> toProcess = new LinkedList<Phadhail>();
    private final Map<Phadhail, Map<String, Integer>> processed = new /*Weak*/HashMap<Phadhail,Map<String,Integer>>();
    private final Map<DomProvider, Phadhail> domProviders2Phadhails = new WeakHashMap<DomProvider,Phadhail>();
    private final Map<Phadhail, Phadhail> phadhails2Parents = new WeakHashMap<Phadhail,Phadhail>();
    
    public IndexImpl(Phadhail root) {
        this.root = root;
    }
    
    public RWLock getLock() {
        return root.lock();
    }
    
    public Map<String,Integer> getData() {
        assert getLock().canRead();
        Map<String,Integer> data = processed.get(root);
        if (data != null) {
            return Collections.unmodifiableMap(data);
        } else {
            return Collections.emptyMap();
        }
    }
    
    public Phadhail getRoot() {
        return root;
    }
    
    public void addChangeListener(final ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    private void fireChange() {
        List<ChangeListener> ls;
        synchronized (listeners) {
            if (listeners.isEmpty()) {
                return;
            }
            ls = new ArrayList<ChangeListener>(listeners);
        }
        ChangeEvent ev = new ChangeEvent(this);
        for (ChangeListener l : ls) {
            l.stateChanged(ev);
        }
    }
    
    public void start() {
        synchronized (toProcess) {
            if (!running) {
                toProcess.add(root);
                Thread t = new Thread(this, "IndexImpl parsing: " + root);
                t.setDaemon(true);
                t.start();
                running = true;
            }
        }
    }
    
    public void cancel() {
        synchronized (toProcess) {
            running = false;
        }
    }
    
    public void run() {
        while (true) {
            final Phadhail next;
            synchronized (toProcess) {
                if (!running) {
                    break;
                }
                while (toProcess.isEmpty()) {
                    try {
                        toProcess.wait();
                    } catch (InterruptedException e) {
                        assert false : e;
                    }
                }
                next = toProcess.removeFirst();
            }
            process(next);
        }
    }
    
    private void process(final Phadhail ph) {
        getLock().read(new Runnable() {
            public void run() {
                if (processed.containsKey(ph)) {
                    // Already computed, do nothing.
                    return;
                }
                if (ph.hasChildren()) {
                    processChildren(ph);
                } else {
                    // Data, maybe.
                    final Map<String,Integer> computed = compute(ph);
                    getLock().writeLater(new Runnable() {
                        public void run() {
                            processed.put(ph, computed);
                            if (!computed.isEmpty()) {
                                bubble(ph);
                            }
                        }
                    });
                }
            }
        });
    }
    
    private void processChildren(Phadhail ph) {
        synchronized (toProcess) {
            for (Phadhail kid : ph.getChildren()) {
                phadhails2Parents.put(kid, ph);
                if (!toProcess.contains(kid)) {
                    toProcess.add(kid);
                }
            }
            toProcess.notify();
        }
        // XXX use WeakListener instead? ... not if Index is long-lived though
        ph.removePhadhailListener(this);
        ph.addPhadhailListener(this);
    }
    
    private int count;
    private Map<String,Integer> compute(Phadhail ph) {
        assert getLock().canRead();
        assert !ph.hasChildren();
        logger.log(Level.FINER, "Computing index for {0} [#{1}]", new Object[] {ph, ++count});
        // XXX technically should listen to lookup changes...
        DomProvider p = (DomProvider) PhadhailLookups.getLookup(ph).lookup(DomProvider.class);
        if (p == null) {
            logger.finer("no DomProvider here");
            return Collections.emptyMap();
        }
        domProviders2Phadhails.put(p, ph);
        Document d;
        try {
            d = p.getDocument();
        } catch (IOException e) {
            logger.log(Level.FINE, "Parsing failed for {0}: {1}", new Object[] {ph.getName(), e.getMessage()});
            return Collections.emptyMap();
        }
        // Wait till after p.getDocument(), since that will fire stateChanged
        // the first time it is called (not ready -> ready)
        p.addChangeListener(WeakListeners.change(this, p));
        Map<String,Integer> m = new HashMap<String,Integer>();
        NodeList l = d.getElementsByTagName("*");
        for (int i = 0; i < l.getLength(); i++) {
            String name = ((Element)l.item(i)).getTagName();
            Integer old = m.get(name);
            m.put(name, old != null ? old + 1 : 1);
        }
        logger.log(Level.FINER, "Parse succeeded for {0}", ph);
        logger.log(Level.FINEST, "Parse results: {0}", m);
        return m;
    }
    
    private void bubble(Phadhail ph) {
        assert getLock().canWrite();
        logger.log(Level.FINER, "bubble: {0} data size: {1}", new Object[] {ph, processed.size()});
        logger.log(Level.FINEST, "bubble: {0} data: {1}", new Object[] {ph, processed});
        if (ph == root) {
            getLock().read(new Runnable() {
                public void run() {
                    fireChange();
                }
            });
        } else {
            Phadhail parent = (Phadhail)phadhails2Parents.get(ph);
            assert parent != null : ph;
            assert parent.hasChildren();
            Map<String,Integer> recalc = new HashMap<String,Integer>();
            for (Phadhail kid : parent.getChildren()) {
                Map<String,Integer> subdata = processed.get(kid);
                if (subdata == null) {
                    // OK, kid is simply not yet calculated, will bubble changes later.
                    continue;
                }
                for (Map.Entry<String, Integer> e : subdata.entrySet()) {
                    String name = e.getKey();
                    int x1 = e.getValue();
                    if (recalc.containsKey(name)) {
                        recalc.put(name, x1 + recalc.get(name));
                    } else {
                        recalc.put(name, x1);
                    }
                }
            }
            processed.put(parent, recalc);
            bubble(parent);
        }
    }
    
    private void invalidate(final Phadhail ph) {
        getLock().writeLater(new Runnable() {
            public void run() {
                processed.remove(ph);
                synchronized (toProcess) {
                    if (!toProcess.contains(ph)) {
                        toProcess.add(ph);
                        toProcess.notify();
                    }
                }
            }
        });
    }
    
    public void childrenChanged(PhadhailEvent ev) {
        Phadhail ph = ev.getPhadhail();
        logger.log(Level.FINER, "childrenChanged: {0}", ph);
        invalidate(ph);
    }
    
    public void nameChanged(PhadhailNameEvent ev) {
        // ignore
    }
    
    public void stateChanged(ChangeEvent e) {
        DomProvider p = (DomProvider)e.getSource();
        Phadhail ph = domProviders2Phadhails.get(p);
        assert ph != null;
        logger.log(Level.FINER, "stateChanged: {0}", ph);
        invalidate(ph);
    }
    
}
