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
package org.openide.util;

import java.lang.ref.SoftReference;

import java.util.Map;


/**
 * A soft reference which is held strongly for a while after last access.
 * Lifecycle:
 * <ol>
 * <li>Created. Referent held strongly. A task is scheduled into the request
 *     processor for some time in the future (currently 30 seconds).</li>
 * <li>Expired. After the timeout, the reference switches to a normal soft
 *     reference.</li>
 * <li>Touched. If the value is accessed before it is garbage collected,
 *     whether the reference is expired or not, the reference is "touched".
 *     This means that the referent is again held strongly and the timeout
 *     is started from scratch.</li>
 * <li>Dead. If after expiry there is no access before the next full GC cycle,
 *     the GC algorithm may reclaim the reference. In this case the reference
 *     of course dies. As a bonus, it will try to remove itself as the value
 *     from a map of your choice, to make it convenient to use these references
 *     as values in a caching map without leaking memory for the key.</li>
 * </ol>
 * @author Jesse Glick
 */
final class TimedSoftReference<T> extends SoftReference<T> implements Runnable {
    private static final int TIMEOUT = 30000;
    private static final RequestProcessor RP = new RequestProcessor("TimedSoftReference"); // NOI18N
    private RequestProcessor.Task task;
    private T o;
    private final Map m;
    private final Object k;

    /** Time when the object was last time touched */
    private long touched;

    /**
     * Create a soft reference with timeout.
     * The supplied map serves double duty as a synchronization lock
     * for the reference's state changes.
     * @param o the referent
     * @param m a map in which this reference may serve as a value
     * @param k the key whose value in <code>m</code> may be this reference
     */
    public TimedSoftReference(T o, Map m, Object k) {
        super(o, BaseUtilities.activeReferenceQueue());
        this.o = o;
        this.m = m;
        this.k = k;
        task = RP.create(this);
        task.schedule(TIMEOUT);
    }

    public void run() {
        synchronized (m) {
            if (o != null) {
                //System.err.println("Expire " + k);
                // how long we've really been idle
                long unused = System.currentTimeMillis() - touched;

                if (unused > (TIMEOUT / 2)) {
                    o = null;
                    touched = 0;
                } else {
                    task.schedule(TIMEOUT - (int) unused);
                }
            } else {
                // clean up map ref, we are dead
                //System.err.println("Die " + k);
                m.remove(k);
            }
        }
    }

    public T get() {
        synchronized (m) {
            if (o == null) {
                o = super.get();
            }

            if (o != null) {
                // touch me
                //System.err.println("Touch " + k);
                if (touched == 0) {
                    task.schedule(TIMEOUT);
                }

                touched = System.currentTimeMillis();

                return o;
            } else {
                return null;
            }
        }
    }
}
