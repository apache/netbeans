/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.fold;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.spi.editor.fold.FoldTypeProvider;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 * MIMEtype specific registry, which collects values for a single enum type (enumType). 
 * It looks up
 * {@link ExtEnum.MimeEnumProvider} in the {@link MimeLookup}. It supports efficient
 * valueOf lookup and changes to the set of the values.
 * <p/>
 * Clients <b>must not cache</b> the set of values and treat it as
 * an universe. As modules are enabled / disabled,
 * the set of available values may change and further queries to {@link #valueSet}
 * may return different results.
 * 
 * <p/>
 * Note: this class was trimmed down from generic enum registry. When other extensible
 * enums are introduced, fold registry will just wrap the real enum. Some constructions may
 * be unnecessary general ;)
 *
 * @author sdedic
 */
public final class FoldRegistry  {
    private static final Logger LOG = Logger.getLogger(FoldRegistry.class.getName());

    /**
     * The enum type being registered
     */
    private Class  enumType;

    /**
     * For a MimePath, holds set of applicable enums and value-to-enum map
     */
    private final Map<MimePath, R>  enums = new HashMap<MimePath, R>();
    
    private FoldRegistry(Class enumType) {
        this.enumType = enumType;
    }
    
    private static volatile Reference<FoldRegistry>     INSTANCE = new WeakReference(null);
    
    public static FoldRegistry get() {
        FoldRegistry fr = INSTANCE.get();
        if (fr == null) {
            synchronized (FoldRegistry.class) {
                fr = INSTANCE.get();
                if (fr == null) {
                    fr = new FoldRegistry(FoldType.class);
                    INSTANCE = new SoftReference(fr);
                }
            }
        }
        return fr;
    }
    
    public Collection<FoldType> values(MimePath mime) {
        return get(mime).enums;
    }
    
    public FoldType valueOf(MimePath mime, String val) {
        return get(mime).valueOf(val);
    }
    
    public FoldType.Domain getDomain(MimePath mime) {
        return get(mime);
    }
    
    private R get(MimePath mime) {
        synchronized (enums) {
            R r = enums.get(mime);
            if (r != null) {
                return r;
            }
        }
        
        return refreshMime(mime, null);
    }
    
    /**
     * Representation of the domain for a specific MIMEtype. It listens on a Lookup.Result
     * and refreshes the contents iff lookup changes - that is if a module is (un)loaded,
     * or the value provider set changes.
     * <p/>
     * Updates to enums must occur under synchro
     * 
     */
    private static class R implements LookupListener, FoldType.Domain {
        final FoldRegistry      dom;
        final MimePath          mime;
        /**
         * Listens on mime lookup 
         */
        final Lookup.Result     result;
        /**
         * Listens on parent's mime lookup, possibly null
         */
        final Lookup.Result     result2;
        Collection<ChangeListener> listeners;
        
        /**
         * Set of all enum values
         */
        // @GuardedBy(this)
        private Set<FoldType>         enums;
        
        /**
         * Allows to faster map an enum to a value. Produced lazily by map(), cleared by reset().
         */
        volatile Map<String, FoldType>      valueMap;
        
        public R(FoldRegistry dom, MimePath mime, Result result, Result result2) {
            this.dom = dom;
            this.mime = mime;
            this.result = result;
            this.result2 = result2;
            result.addLookupListener(WeakListeners.create(LookupListener.class, 
                    this, result));
            if (result2 != null) {
                result2.addLookupListener(WeakListeners.create(LookupListener.class, 
                        this, result2));
            }
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            dom.refreshMime(mime, this);
        }

        @Override
        public Collection<FoldType> values() {
            return Collections.unmodifiableCollection(map().values());
        }
        
        private Map<String, FoldType> map() {
            Map<String, FoldType> m;
            
            m = valueMap;
            if (m == null) {
                synchronized (this) {
                    if (valueMap != null) {
                        return valueMap;
                    }
                    Set<FoldType> vals = enums;
                    m = new LinkedHashMap<String, FoldType>();
                    for (FoldType e : vals) {
                        FoldType old = m.put(e.code(), e);
                        if (old != null) {
                            throw new IllegalArgumentException("Two fold types share the same code: " + old + " and " + e);
                        }
                    }
                    this.valueMap = m;
                }
            }
            return m;
        }
        
        @Override
        public FoldType  valueOf(String val) {
            return map().get(val);
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            synchronized (this) {
                if (listeners == null) {
                    listeners = new ArrayList<ChangeListener>();
                }
                listeners.add(l);
            }
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            synchronized (this) {
                if (listeners != null) {
                    listeners.remove(l);
                }
            }
        }
        
        void reset(Set<FoldType> allValues) {
            ChangeListener[] ll;
            synchronized (this) {
                enums = allValues;
                valueMap = null;
                if (listeners == null) {
                    return;
                }
                ll = listeners.toArray(new ChangeListener[listeners.size()]);
            }
            ChangeEvent e = new ChangeEvent(this);
            for (int i = 0; i < ll.length; i++) {
                ll[i].stateChanged(e);
            }
        }
    }
    
    
    private R refreshMime(MimePath mime, R holder) {
        Lookup.Result<FoldTypeProvider> r;
        Lookup.Result<FoldTypeProvider> pr = null;

        if (holder == null) {
            r = MimeLookup.getLookup(mime).lookup(
                    new Lookup.Template(
                        FoldTypeProvider.class
                    )
            );
            // get the inherited MimePaths:
            String parentMime = mime.getInheritedType();
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("Get providers for " + mime + ", parent " + parentMime);
            }
            if (parentMime != null) {
                pr = MimeLookup.getLookup(parentMime).lookup(
                        new Lookup.Template(
                            FoldTypeProvider.class
                        )
                );
            }
        } else {
            r = holder.result;
            pr = holder.result2;
        }
        
        
        Collection<? extends FoldTypeProvider> providers = r.allInstances();
        Collection<? extends FoldTypeProvider> parentProvs = pr == null ? 
                Collections.<FoldTypeProvider>emptySet() : 
                new ArrayList<>(pr.allInstances());
        
        for (Iterator<? extends FoldTypeProvider> it = parentProvs.iterator(); it.hasNext(); ) {
            FoldTypeProvider p = it.next();
            if (p.inheritable()) {
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.FINER, "Inheritable: " + p);
                }
                // leave only providers, which are !inheritable, so they'll be ignored.
                it.remove();
            }
        }
        
        Set<FoldType> allValues = new LinkedHashSet<FoldType>();
        
        for (FoldTypeProvider p : providers) {
            if (parentProvs.contains(p)) {
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.FINER, "Removing not inheritable: " + p);
                }
                // provider registered for parent, but inheritance is disallowed.
                continue;
            }
            
            Collection<FoldType> vals = p.getValues(enumType);
            if (vals != null) {
                // check that none enum overrides another one
                allValues.addAll(vals);
            }
        }
        
        boolean register = holder == null;
        if (holder == null) {
            holder = new R(this, mime, r, pr);
        }
        // sync
        holder.reset(allValues);

        if (register) {
            synchronized(enums) {
                R oldHolder = enums.put(mime, holder);
                if (oldHolder != null) {
                    // unlikely, but can happen.
                    enums.put(mime, oldHolder);
                    holder = oldHolder;
                }
            }
        }
        return holder;
    }
}
