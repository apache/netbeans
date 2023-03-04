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

import java.awt.EventQueue;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import spin.Spin;
import spin.Starter;
import threaddemo.locking.Locks;
import threaddemo.locking.Worker;

/**
 * An asynchronous Phadhail impl using Spin.
 * Spin does not appear to handle nested beans so we do that part manually.
 * We keep a dedicated single thread for running stuff in
 * (Spin's default behavior is to spawn a thread for every method call).
 * @author Jesse Glick
 */
final class SpunPhadhail extends Spin {
    
    private static final Starter starter = new Starter() {
        public void start(Runnable r) {
            Worker.start(r);
        }
    };
    
    private static final Map<Phadhail, Reference<Phadhail>> instances = new WeakHashMap<Phadhail,Reference<Phadhail>>();
    
    /** factory */
    public static Phadhail forPhadhail(Phadhail _ph) {
        assert EventQueue.isDispatchThread();
        Reference<Phadhail> r = instances.get(_ph);
        Phadhail ph = (r != null) ? r.get() : null;
        if (ph == null) {
            Spin spin = new SpunPhadhail(_ph);
            ph = BufferedPhadhail.forPhadhail((Phadhail)spin.getProxy());
            instances.put(_ph, new WeakReference<Phadhail>(ph));
        }
        return ph;
    }
    
    private final Phadhail ph;
    
    private SpunPhadhail(Phadhail ph) {
        super(ph, Spin.SPIN_OFF, starter);
        this.ph = ph;
    }
    
    /** overridden to recursively wrap phadhails */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String mname = method.getName();
        if (mname.equals("toString")) {
            return "SpunPhadhail<" + ph + ">";
        } else if (mname.equals("lock")) {
            return Locks.event();
        } else if (mname.equals("equals")) {
            return args[0] == ph ? Boolean.TRUE : Boolean.FALSE;
        } else if (mname.equals("hashCode")) {
            return new Integer(ph.hashCode());
        } else if (mname.endsWith("PhadhailListener")) {
            // Can do this synch - it's thread-safe and fast.
            assert args != null;
            assert args.length == 1;
            // Need to wrap this too!
            Spin spin = new SpunPhadhailListener((PhadhailListener)args[0], (Phadhail)proxy);
            PhadhailListener l = (PhadhailListener)spin.getProxy();
            if (mname.equals("addPhadhailListener")) {
                ph.addPhadhailListener(l);
            } else {
                assert mname.equals("removePhadhailListener") : mname;
                ph.removePhadhailListener(l);
            }
            return null;
        } else {
            assert EventQueue.isDispatchThread() : mname;
            Object result = super.invoke(proxy, method, args);
            if (result instanceof Phadhail) {
                return forPhadhail((Phadhail)result);
            } else if (result instanceof List) {
                // I.e. from getChildren(). Need to wrap result phadhails.
                @SuppressWarnings("unchecked")
                List<Phadhail> l = (List<Phadhail>) result;
                return new SpunChildrenList(l);
            } else {
                // Just pass on the call.
                return result;
            }
        }
    }
    
    private static final class SpunChildrenList extends AbstractList<Phadhail> {
        private final List<Phadhail> orig;
        private final Phadhail[] kids;
        public SpunChildrenList(List<Phadhail> orig) {
            this.orig = orig;
            kids = new Phadhail[orig.size()];
        }
        public Phadhail get(int i) {
            assert EventQueue.isDispatchThread();
            if (kids[i] == null) {
                kids[i] = forPhadhail(orig.get(i));
            }
            return kids[i];
        }
        public int size() {
            assert EventQueue.isDispatchThread();
            return kids.length;
        }
    }
    
    private static final class SpunPhadhailListener extends Spin {
        
        private final PhadhailListener l;
        private final Phadhail ph;
        
        public SpunPhadhailListener(PhadhailListener l, Phadhail ph) {
            super(l, Spin.SPIN_OVER, starter);
            this.l = l;
            this.ph = ph;
        }
        
        /** overridden to translate PhadhailEvent's */
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String mname = method.getName();
            if (mname.equals("toString")) {
                return "SpunPhadhailListener<" + l + ">";
            } else if (mname.equals("equals")) {
                return args[0] == l ? Boolean.TRUE : Boolean.FALSE;
            } else if (mname.equals("hashCode")) {
                return new Integer(l.hashCode());
            } else {
                assert mname.endsWith("Changed"): mname;
                assert EventQueue.isDispatchThread() : mname;
                assert args != null;
                assert args.length == 1;
                Object arg2;
                // Need to translate the original Phadhail event source to the proxy.
                if (mname.equals("childrenChanged")) {
                    arg2 = PhadhailEvent.create(ph);
                } else {
                    assert mname.equals("nameChanged");
                    PhadhailNameEvent orig = (PhadhailNameEvent)args[0];
                    arg2 = PhadhailNameEvent.create(ph, orig.getOldName(), orig.getNewName());
                }
                return super.invoke(proxy, method, new Object[] {arg2});
            }
        }
    
    }
    
}
