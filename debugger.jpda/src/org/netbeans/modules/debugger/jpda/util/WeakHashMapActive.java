/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.openide.util.BaseUtilities;

/**
 * A weak hash map, that automatically release entries as soon as they are freed by GC.
 * 
 * @author Martin Entlicher
 */
// TODO: Make it a public API. There's another copy of this class in debugger.jpda.projects module.
public final class WeakHashMapActive<K,V> extends AbstractMap<K,V> {
    
    private final ReferenceQueue<Object> queue;
    private final Map<KeyReference<K>, V> map;
    
    public WeakHashMapActive() {
        super();
        map = new HashMap<>();
        queue = BaseUtilities.activeReferenceQueue();
    }
    
    @Override
    public V put(K key, V value) {
        KeyReference<K> rk = new KeyReference<>(key, queue);
        synchronized (map) {
            return map.put(rk, value);
        }
    }

    @Override
    public V get(Object key) {
        KeyReference<Object> rk = new KeyReference<>(key, null);
        synchronized (map) {
            return map.get(rk);
        }
    }

    @Override
    public V remove(Object key) {
        KeyReference<Object> rk = new KeyReference<>(key, null);
        synchronized (map) {
            return map.remove(rk);
        }
    }
    
    @Override
    public void clear() {
        synchronized (map) {
            map.clear();
        }
    }

    @Override
    public int size() {
        synchronized (map) {
            return map.size();
        }
    }
    
    @Override
    public Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException("Not supported.");
    }
    
    private class KeyReference<K> extends WeakReference<K> implements Runnable {
        
        private final int hash;
        
        KeyReference(K r, ReferenceQueue<? super K> queue) {
            super(r, queue);
            hash = r.hashCode();
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof KeyReference)) {
                return false;
            }
            KeyReference kr = (KeyReference) obj;
            K k1 = get();
            Object k2 = kr.get();
            if (k1 == null && k2 == null) {
                return hash == kr.hash;
            }
            return (k1 == k2 || (k1 != null && k1.equals(k2)));
        }

        @Override
        public void run() {
            // Collected
            synchronized (map) {
                map.remove(this);
            }
        }
        
    }
    
}
