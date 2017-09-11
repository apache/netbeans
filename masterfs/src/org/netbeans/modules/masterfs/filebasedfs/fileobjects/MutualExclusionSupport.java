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

package org.netbeans.modules.masterfs.filebasedfs.fileobjects;


import org.openide.util.WeakSet;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.masterfs.filebasedfs.utils.FSException;

public final class MutualExclusionSupport<K> {

    private static final int TRIES = Integer.getInteger(               //#229903
            "org.netbeans.modules.masterfs.mutualexclusion.tries", 10); //NOI18N

    private final Map<K,Set<Closeable>> exclusive = Collections.synchronizedMap(new WeakHashMap<K,Set<Closeable>>());
    private final Map<K,Set<Closeable>> shared = Collections.synchronizedMap(new WeakHashMap<K,Set<Closeable>>());

    public MutualExclusionSupport() {
    }

    public synchronized Closeable addResource(final K key, final boolean isShared) throws IOException {
        boolean isInUse = true;        
        final Map<K,Set<Closeable>> unexpected = (isShared) ? exclusive : shared;
        final Map<K,Set<Closeable>> expected = (isShared) ? shared : exclusive;

        final Set<Closeable> unexpectedCounter = unexpected.get(key);
        Set<Closeable> expectedCounter = expected.get(key);

        for (int i = 0; i < TRIES && isInUse; i++) {
            isInUse = unexpectedCounter != null && unexpectedCounter.size() > 0;

            if (!isInUse) {            
                if (expectedCounter == null) {
                    expectedCounter = new WeakSet<Closeable>();
                    expected.put(key, expectedCounter);
                }
                isInUse = !isShared && expectedCounter.size() > 0;            
            }
            
            if (isInUse) {
                try {
                    wait(200);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        if (isInUse) {
            try {
                FSException.io(isShared ? "EXC_CannotGetSharedAccess" : "EXC_CannotGetExclusiveAccess", key.toString()); // NOI18N
            } catch (IOException x) {
                assert addStack(x, unexpectedCounter, expectedCounter);
                throw x;
            }
        }


        final Closeable retVal = new Closeable(key, isShared);
        expectedCounter.add(retVal);
        return retVal;
    }
    private boolean addStack(IOException x, Set<Closeable> unexpectedCounter, Set<Closeable> expectedCounter) {
        try {
            addStack(x, unexpectedCounter);
            addStack(x, expectedCounter);
        } catch (IllegalArgumentException e) { // #233546
            Logger log = Logger.getLogger(MutualExclusionSupport.class.getName());
            String unexpectedStr = unexpectedCounter == null
                    ? "null" //NOI18N
                    : Arrays.toString(unexpectedCounter.toArray());
            String expectedStr = expectedCounter == null
                    ? "null" //NOI18N
                    : Arrays.toString(expectedCounter.toArray());
            log.log(Level.WARNING, "Cannot add stack to exception: " //NOI18N
                    + "unexpectedCounter: {0}, expectedCounter: {1}", //NOI18N
                    new Object[]{unexpectedStr, expectedStr});
            log.log(Level.INFO, null, e);
            log.log(Level.INFO, "Exception x", x); //NOI18N
        }
        return true;
    }
    private void addStack(IOException x, Set<Closeable> cs) {
        if (cs != null) {
            for (Closeable c : cs) {
                Throwable stack = c.stack;
                if (stack != null) {
                    Throwable t = x;
                    while (t.getCause() != null) {
                        t = t.getCause();
                    }
                    t.initCause(stack);
                }
            }
        }
    }

    private synchronized void removeResource(final K key, final Closeable value, final boolean isShared) {
        final Map<K,Set<Closeable>> expected = isShared ? shared : exclusive;

        final Set<Closeable> expectedCounter = expected.get(key);
        if (expectedCounter != null) {
            expectedCounter.remove(value);
        }
    }

    synchronized boolean isBeingWritten(K file) {
        final Set<Closeable> counter = exclusive.get(file);
        return counter != null && !counter.isEmpty();
    }


    public final class Closeable {
        private final boolean isShared;
        private final Reference<K> keyRef;
        private boolean isClosed = false;
        Throwable stack;

        private Closeable(final K key, final boolean isShared) {
            this.isShared = isShared;
            this.keyRef = new WeakReference<K>(key);
            assert populateStack();
        }

        private boolean populateStack() {
            stack = new Throwable("opened stream here");
            return true;
        }

        public void close() {
            if (!isClosed()) {
                isClosed = true;
                final K key = keyRef.get();
                if (key != null) {
                    removeResource(key, this, isShared);
                }
            }
        }

        public boolean isClosed() {
            return isClosed;
        }
    }
}
