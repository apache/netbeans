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
