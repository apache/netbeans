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
package org.openide.util.lookup;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

final class MetaInfCache {
    private int knownInstancesCount;
    private final List<Reference<Object>> knownInstances;

    public MetaInfCache(int size) {
        knownInstances = new ArrayList<Reference<Object>>();
        for (int i = 0; i < size; i++) {
            knownInstances.add(null);
        }
    }

    public synchronized Object findInstance(Class<?> c) {
        int size = knownInstances.size();
        int index = hashForClass(c, size);
        for (int i = 0; i < size; i++) {
            Reference<Object> ref = knownInstances.get(index);
            if (ref == null) {
                break;
            }
            Object obj = ref.get();
            if (obj != null) {
                if (c == obj.getClass()) {
                    return obj;
                }
            }
            if (++index == size) {
                index = 0;
            }
        }
        return null;
    }

    public synchronized void storeInstance(Object o) {
        hashPut(o);
        int size = knownInstances.size();
        if (knownInstancesCount > size * 2 / 3) {
            MetaInfServicesLookup.LOGGER.log(Level.CONFIG, "Cache of size {0} is 2/3 full. Rehashing.", size);
            MetaInfCache newCache = new MetaInfCache(size * 2);
            for (Reference<Object> r : knownInstances) {
                if (r == null) {
                    continue;
                }
                Object instance = r.get();
                if (instance == null) {
                    continue;
                }
                newCache.storeInstance(instance);
            }

            this.knownInstances.clear();
            this.knownInstances.addAll(newCache.knownInstances);
            this.knownInstancesCount = newCache.knownInstancesCount;
        }
    }

    private void hashPut(Object o) {
        assert Thread.holdsLock(this);
        Class<?> c = o.getClass();
        int size = knownInstances.size();
        int index = hashForClass(c, size);
        for (int i = 0; i < size; i++) {
            Reference<Object> ref = knownInstances.get(index);
            Object obj = ref == null ? null : ref.get();
            if (obj == null) {
                knownInstances.set(index, new WeakReference<Object>(o));
                knownInstancesCount++;
                break;
            }
            if (++index == size) {
                index = 0;
            }
        }
    }
    
    private static int hashForClass(Class<?> c, int size) {
        return Math.abs(c.hashCode() % size);
    }
}
