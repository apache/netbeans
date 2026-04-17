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
package org.netbeans.modules.masterfs.filebasedfs.naming;

import java.io.File;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

final class NameRef extends WeakReference<FileNaming> {
    /** either reference to NameRef or to Integer as an index to names array */
    private Object next;
    static final ReferenceQueue<FileNaming> QUEUE = new ReferenceQueue<>();

    public NameRef(FileNaming referent) {
        super(referent, QUEUE);
    }

    public int getIndex() {
        assert Thread.holdsLock(NamingFactory.class);
        NameRef nr = this;
        while (nr != null) {
            if (nr.next instanceof Integer integer) {
                return integer;
            }
            nr = nr.next();
        }
        return -1;
    }

    public NameRef next() {
        if (next instanceof Integer) {
            return null;
        }
        return (NameRef) next;
    }

    public File getFile() {
        FileNaming r = get();
        return r == null ? null : r.getFile();
    }

    public NameRef remove(NameRef what) {
        assert Thread.holdsLock(NamingFactory.class);
        if (what == this) {
            return next();
        }
        NameRef me = this;
        while (me.next != what) {
            if (me.next instanceof Integer) {
                return this;
            }
            me = (NameRef) me.next;
        }
        me.next = me.next().next;
        return this;
    }

    final void setNext(NameRef nr) {
        assert Thread.holdsLock(NamingFactory.class);
        assert next == null : "There is next " + next;
        this.next = nr;
    }

    final void setIndex(int index) {
        assert Thread.holdsLock(NamingFactory.class);
        assert next == null : "There is next " + next;
        next = index;
    }

    final void skip(NameRef ref) {
        assert Thread.holdsLock(NamingFactory.class);
        assert next == ref;
        assert ref.get() == null;
        next = ref.next;
    }

    final Iterable<NameRef> disconnectAll() {
        assert Thread.holdsLock(NamingFactory.class);
        List<NameRef> all = new ArrayList<>();
        NameRef nr = this;
        while (nr != null) {
            NameRef nn = nr.next();
            nr.next = null;
            if (nr.get() != null) {
                all.add(nr);
            }
            nr = nn;
        }
        return all;
    }
}
