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
package org.netbeans.modules.masterfs.filebasedfs.naming;

import java.io.File;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

final class NameRef extends WeakReference<FileNaming> {
    /** either reference to NameRef or to Integer as an index to names array */
    private Object next;
    static final ReferenceQueue<FileNaming> QUEUE = new ReferenceQueue<FileNaming>();

    public NameRef(FileNaming referent) {
        super(referent, QUEUE);
    }

    public Integer getIndex() {
        assert Thread.holdsLock(NamingFactory.class);
        NameRef nr = this;
        while (nr != null) {
            if (nr.next instanceof Integer) {
                return (Integer) nr.next;
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
        List<NameRef> all = new ArrayList<NameRef>();
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
