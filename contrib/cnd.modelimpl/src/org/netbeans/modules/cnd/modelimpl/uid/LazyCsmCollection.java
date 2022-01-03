/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.modelimpl.uid;

import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.modelimpl.impl.services.UIDFilter;

/**
 * The lazy implementation of the collection 
 * backed by the collection of UIDs.
 * 
 * It uses two template parameters: Tuid and Tfact.
 * The idea behind that is that often we use 
 * interface type (e.g. CsmFile) UID, 
 * while implementation knows that all the instances are 
 * of implementation type (e.g. FileImpl).
 * So Tuid is an interface (CsmFile) type, 
 * while Tfact is implementation (FileImpl) type.
 *  
 */
public class LazyCsmCollection<Tuid, Tfact extends Tuid> implements Collection<Tfact> {

    private final Collection<CsmUID<Tuid>> uids;
    private final boolean allowNullsAndSkip;

    public LazyCsmCollection(Collection<CsmUID<Tuid>> uids, boolean allowNullsAndSkip) {
        this.uids = uids;
        this.allowNullsAndSkip = allowNullsAndSkip;
    }

    private Tfact convertToObject(CsmUID<? extends Tfact> uid) {
        return (Tfact) UIDCsmConverter.UIDtoCsmObject(uid);
    }

    @Override
    public int size() {
        return uids.size();
    }

    @Override
    public boolean isEmpty() {
        return uids.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        Iterator<Tfact> it = iterator();
        while (it.hasNext()) {
            Tfact object = it.next();
            if (o == object ||
                    o != null && o.equals(object)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<Tfact> iterator() {
        return allowNullsAndSkip ? new MySafeIterator() : new MyIterator();
    }

    public Iterator<Tfact> iterator(CsmFilter filter) {
        return new MySafeIterator(filter);
    }

    @Override
    public Object[] toArray() {
        Object[] result = new Object[size()];
        Iterator<Tfact> e = iterator();
        int i = 0;
        for (; e.hasNext(); i++) {
            result[i] = e.next();
        }
        if (i < size()) {
            Object[] a = new Object[i];
            System.arraycopy(result, 0, a, 0, i);
            result = a;
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        int size = size();
        if (a.length < size) {
            a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        }

        Iterator<T> it = (Iterator<T>) iterator();
        Object[] result = a;
        int i = 0;
        for (; it.hasNext(); i++) {
            result[i] = it.next();
        }
        if (i < size()) {
            a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), i);
            System.arraycopy(result, 0, a, 0, i);
        }
        return a;
    }

    @Override
    public boolean add(Tfact o) {
        return uids.add(UIDs.<Tuid>get(o));
    }

    @Override
    public boolean remove(Object o) {
        return uids.remove(UIDs.get(o));
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        Iterator<?> e = c.iterator();
        while (e.hasNext()) {
            if (!contains(e.next())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends Tfact> c) {
        boolean modified = false;
        if (c instanceof LazyCsmCollection<?, ?>) {
            // input collection c is Tfact based
            // Tfact is extension of Tuid => col.uids provides array of needed type
            @SuppressWarnings("unchecked") // checked
            final LazyCsmCollection<Tuid, ? extends Tfact> col = (LazyCsmCollection<Tuid, ? extends Tfact>) c;
            return uids.addAll(col.uids);
        } else {
            Iterator<? extends Tfact> it = c.iterator();
            while (it.hasNext()) {
                if (add(it.next())) {
                    modified = true;
                }
            }
        }
        return modified;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        Iterator<?> it = iterator();
        while (it.hasNext()) {
            if (c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean modified = false;
        Iterator<Tfact> it = iterator();
        while (it.hasNext()) {
            if (!c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public void clear() {
        uids.clear();
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("["); // NOI18N

        Iterator<Tfact> it = iterator();
        boolean hasNext = it.hasNext();
        while (hasNext) {
            Tfact o = it.next();
            buf.append(o == this ? "(this Collection)" : String.valueOf(o));  // NOI18N
            hasNext = it.hasNext();
            if (hasNext) {
                buf.append(", "); // NOI18N
            }
        }

        buf.append("]"); // NOI18N
        return buf.toString();
    }

    private class MyIterator implements Iterator<Tfact> {

        private final Iterator<CsmUID<Tuid>> it;

        private MyIterator() {
            it = uids.iterator();
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public Tfact next() {
            // we know that Tfact is the real type so cast is okay
            @SuppressWarnings("unchecked") // checked
            CsmUID<Tfact> uid = (CsmUID<Tfact>) it.next();
            Tfact decl = convertToObject(uid);
            assert decl != null : "no object for UID " + uid;
            return decl;
        }

        @Override
        public void remove() {
            it.remove();
        }
    }

    private class MySafeIterator implements Iterator<Tfact> {

        private final Iterator<CsmUID<Tuid>> it;
        private Tfact next;
        private final CsmFilter filter;

        private MySafeIterator() {
            this(null);
        }

        private MySafeIterator(CsmFilter filter) {
            this.filter = filter;
            it = uids.iterator();
            next = getNextNonNull();
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        private Tfact getNextNonNull() {
            Tfact out = null;
            while (out == null && it.hasNext()) {
                // we know that Tfact is the real type so cast is okay
                @SuppressWarnings("unchecked") // checked
                CsmUID<Tfact> uid = (CsmUID<Tfact>) it.next();
                if (uid == null ||
                        (filter != null && !((UIDFilter) filter).accept(uid))) {
                    continue;
                }
                out = convertToObject(uid);
            }
            return out;
        }

        @Override
        public Tfact next() {
            Tfact decl = next;
            next = getNextNonNull();
            return decl;
        }

        @Override
        public void remove() {
            it.remove();
        }
    }
}
