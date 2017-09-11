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
package org.netbeans.modules.whitelist;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.whitelist.WhiteListQuery;
import org.netbeans.spi.whitelist.WhiteListQueryImplementation;
import org.netbeans.spi.whitelist.WhiteListQueryImplementation.WhiteListImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 *
 */
public class WhiteListQueryImplementationMerged implements WhiteListQueryImplementation {
    
    private Lookup lkp;
    private final Map<FileObject,Reference<WhiteListImplementation>> canonicalCache;

    public WhiteListQueryImplementationMerged(Lookup lkp) {
        this.lkp = lkp;
        this.canonicalCache = new WeakHashMap<FileObject, Reference<WhiteListImplementation>>();
    }

    @Override
    public synchronized WhiteListImplementation getWhiteList(final FileObject file) {
        final Reference<WhiteListImplementation> ref = canonicalCache.get(file);
        WhiteListImplementation wl = ref == null ? null : ref.get();
        if (wl != null) {
            return wl;
        }
        final Lookup.Result<WhiteListQueryImplementation> lr = lkp.lookupResult(WhiteListQueryImplementation.class);
        boolean empty = true;
        for (WhiteListQueryImplementation impl : lr.allInstances()) {
            WhiteListImplementation i = impl.getWhiteList(file);
            if (i != null) {
                empty = false;
                break;
            }
        }
        if (empty) {
            return null;
        }
        wl = new WhiteListImplementationMerged(lr,file);
        canonicalCache.put(file,new WeakReference<WhiteListImplementation>(wl));
        return wl;
    }

    private static class WhiteListImplementationMerged implements WhiteListImplementation, ChangeListener, LookupListener {

        private final Lookup.Result<WhiteListQueryImplementation> lr;
        private final FileObject file;
        private final ChangeSupport changeSupport;
        //@GuardedBy("this")
        private Map<WhiteListImplementation,ChangeListener> cache;

        @SuppressWarnings("LeakingThisInConstructor")
        public WhiteListImplementationMerged(
            @NonNull final Lookup.Result<WhiteListQueryImplementation> lr,
            @NonNull final FileObject file) {
            this.lr = lr;
            this.file = file;
            this.changeSupport = new ChangeSupport(this);
            this.lr.addLookupListener(WeakListeners.create(LookupListener.class, this, this.lr));
        }

        @Override
        @NonNull
        public WhiteListQuery.Result check(
                @NonNull final ElementHandle<?> element,
                @NonNull final WhiteListQuery.Operation operation) {
            List<WhiteListQuery.RuleDescription> rules = new ArrayList<WhiteListQuery.RuleDescription>();
            for (WhiteListImplementation impl : getWhiteLists()) {
                WhiteListQuery.Result r = impl.check(element, operation);
                if (r != null && !r.isAllowed()) {
                    rules.addAll(r.getViolatedRules());
                }
            }
            if (rules.isEmpty()) {
                return new WhiteListQuery.Result();
            } else {
                return new WhiteListQuery.Result(rules);
            }
        }

        @Override
        public void addChangeListener(@NonNull final ChangeListener listener) {
            this.changeSupport.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(@NonNull final ChangeListener listener) {
            this.changeSupport.removeChangeListener(listener);
        }

        @Override
        public void stateChanged(final ChangeEvent event) {
            this.changeSupport.fireChange();
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            synchronized(this) {
                cache = null;
            }
            this.changeSupport.fireChange();
        }

        private Iterable<WhiteListImplementation> getWhiteLists() {
            synchronized (this) {
                if (cache != null) {
                    return cache.keySet();
                }
            }
            final Map<WhiteListImplementation,ChangeListener> map = new IdentityHashMap<WhiteListImplementation,ChangeListener>();
            for (WhiteListQueryImplementation wlq : lr.allInstances()) {
                final WhiteListImplementation wl = wlq.getWhiteList(file);
                if (wl != null) {
                    final ChangeListener cl = WeakListeners.change(this, wl);
                    wl.addChangeListener(cl);
                    map.put(wl, cl);
                }
            }
            synchronized (this) {
                if (cache == null) {
                    cache = map;
                }
                return cache.keySet();
            }
        }

    }
}
