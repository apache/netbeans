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

package threaddemo.data;

import java.util.Map;
import java.util.WeakHashMap;
import org.openide.cookies.SaveCookie;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import threaddemo.locking.RWLock;
import threaddemo.model.Phadhail;

// XXX this is inefficient - e.g. LookNode.getIcon will force the PhadhailLookup
// to be created! PhadhailLook should just ask for any special lookup items, e.g.
// the SaveCookie, otherwise return a simple list with the editor support. There
// should be a way to listen to any phadhails with one listener.

/**
 * Serves "cookies" for phadhails.
 * @author Jesse Glick
 */
public class PhadhailLookups {
    
    /** no instances */
    private PhadhailLookups() {}
    
    private static final Map<Phadhail,PhadhailLookup> lookups = new WeakHashMap<Phadhail,PhadhailLookup>();
    
    // XXX rather than being synch, should be readAccess, and modified/saved should be writeAccess
    public static synchronized Lookup getLookup(Phadhail ph) {
        PhadhailLookup l = lookups.get(ph);
        if (l == null) {
            l = new PhadhailLookup(ph);
            lookups.put(ph, l);
        }
        return l;
    }
    
    // Access from PhadhailEditorSupport
    static void modified(Phadhail ph, SaveCookie s) {
        ((PhadhailLookup)getLookup(ph)).modified(s);
    }
    
    static void saved(Phadhail ph, SaveCookie s) {
        ((PhadhailLookup)getLookup(ph)).saved(s);
    }

    // XXX #32203 would be really helpful here!
    private static final class PhadhailLookup extends AbstractLookup implements InstanceContent.Convertor<Object,Object> {
        
        private static final Object KEY_EDITOR = "editor";
        private static final Object KEY_DOM_PROVIDER = "domProvider";
        
        private final Phadhail ph;
        // XXX Have to keep the InstanceContent separately; it is a field in AbstractLookup
        // but we cannot access it!
        private final InstanceContent c;
        private PhadhailEditorSupport ed = null;
        
        public PhadhailLookup(Phadhail ph) {
            this(ph, new InstanceContent());
        }
        
        private PhadhailLookup(Phadhail ph, InstanceContent c) {
            super(c);
            this.ph = ph;
            this.c = c;
        }
        
        protected void initialize() {
            if (!ph.hasChildren()) {
                c.add(KEY_EDITOR, this);
                if (ph.getName().endsWith(".xml")) {
                    c.add(KEY_DOM_PROVIDER, this);
                }
            }
            super.initialize();
        }
        
        public void modified(SaveCookie s) {
            c.add(s);
        }
        
        public void saved(SaveCookie s) {
            c.remove(s);
        }
        
        private PhadhailEditorSupport getEd() {
            if (ed == null) {
                ed = new PhadhailEditorSupport(ph);
            }
            return ed;
        }
        
        public Object convert(Object obj) {
            if (obj == KEY_EDITOR) {
                return getEd();
            } else {
                assert obj == KEY_DOM_PROVIDER;
                RWLock m = ph.lock(); // XXX may need a different lock...
                return new DomSupport(ph, getEd(), m);
            }
        }
        
        public Class<?> type(Object obj) {
            if (obj == KEY_EDITOR) {
                return PhadhailEditorSupport.class; // a bunch of interfaces
            } else {
                assert obj == KEY_DOM_PROVIDER;
                return DomProvider.class;
            }
        }
        
        public String displayName(Object obj) {
            throw new UnsupportedOperationException();
        }
        
        public String id(Object obj) {
            return "PhadhailLookup[" + ph + "," + obj + "]";
        }
        
    }
    
}
