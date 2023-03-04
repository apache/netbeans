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

package threaddemo.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import threaddemo.locking.RWLock;

/**
 * Wraps a plain Phadhail and buffers its list of children.
 * @author Jesse Glick
 */
final class BufferedPhadhail implements Phadhail, PhadhailListener {

    private static final Map<Phadhail, Reference<BufferedPhadhail>> instances = new WeakHashMap<Phadhail,Reference<BufferedPhadhail>>();
    
    public static Phadhail forPhadhail(Phadhail ph) {
        if (ph.hasChildren() && !(ph instanceof BufferedPhadhail)) {
            Reference<BufferedPhadhail> r = instances.get(ph);
            BufferedPhadhail bph = (r != null) ? r.get() : null;
            if (bph == null) {
                bph = new BufferedPhadhail(ph);
                instances.put(ph, new WeakReference<BufferedPhadhail>(bph));
            }
            return bph;
        } else {
            return ph;
        }
    }
    
    private final Phadhail ph;
    private Reference<List<Phadhail>> kids;
    private List<PhadhailListener> listeners = null;
    
    private BufferedPhadhail(Phadhail ph) {
        this.ph = ph;
    }
    
    public List<Phadhail> getChildren() {
        List<Phadhail> phs = null;
        if (kids != null) {
            phs = kids.get();
        }
        if (phs == null) {
            // Need to (re)calculate the children.
            phs = new BufferedChildrenList(ph.getChildren());
            kids = new WeakReference<List<Phadhail>>(phs);
        }
        return phs;
    }
    
    private static final class BufferedChildrenList extends AbstractList<Phadhail> {
        private final List<Phadhail> orig;
        private final Phadhail[] kids;
        public BufferedChildrenList(List<Phadhail> orig) {
            this.orig = orig;
            kids = new Phadhail[orig.size()];
        }
        public Phadhail get(int i) {
            if (kids[i] == null) {
                kids[i] = forPhadhail(orig.get(i));
            }
             return kids[i];
        }
        public int size() {
            return kids.length;
        }
    }
    
    public String getName() {
        return ph.getName();
    }
    
    public String getPath() {
        return ph.getPath();
    }
    
    public boolean hasChildren() {
        return ph.hasChildren();
    }
    
    public void addPhadhailListener(PhadhailListener l) {
        if (listeners == null) {
            ph.addPhadhailListener(this);
            listeners = new ArrayList<PhadhailListener>();
        }
        listeners.add(l);
    }
    
    public void removePhadhailListener(PhadhailListener l) {
        if (listeners != null) {
            listeners.remove(l);
            if (listeners.isEmpty()) {
                listeners = null;
                ph.removePhadhailListener(this);
            }
        }
    }
    
    public void rename(String nue) throws IOException {
        ph.rename(nue);
    }
    
    public Phadhail createContainerPhadhail(String name) throws IOException {
        return ph.createContainerPhadhail(name);
    }
    
    public Phadhail createLeafPhadhail(String name) throws IOException {
        return ph.createLeafPhadhail(name);
    }
    
    public void delete() throws IOException {
        ph.delete();
    }
    
    public InputStream getInputStream() throws IOException {
        return ph.getInputStream();
    }
    
    public OutputStream getOutputStream() throws IOException {
        return ph.getOutputStream();
    }
    
    public void childrenChanged(PhadhailEvent ev) {
        // clear cache
        kids = null;
        if (listeners != null) {
            PhadhailEvent ev2 = PhadhailEvent.create(this);
            for (PhadhailListener l : listeners) {
                l.childrenChanged(ev2);
            }
        }
    }
    
    public void nameChanged(PhadhailNameEvent ev) {
        if (listeners != null) {
            PhadhailNameEvent ev2 = PhadhailNameEvent.create(this, ev.getOldName(), ev.getNewName());
            for (PhadhailListener l : listeners) {
                l.nameChanged(ev2);
            }
        }
    }
    
    public String toString() {
        return "BufferedPhadhail<" + ph + ">@" + Integer.toHexString(System.identityHashCode(this));
    }
    
    public RWLock lock() {
        return ph.lock();
    }
    
}
