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

package org.netbeans.modules.projectapi.nb;

// XXX COPIED from org.openide.util w/ changes:
//     weak -> soft
//     timeout
//     removed map key functionality

import java.lang.ref.WeakReference;
import org.openide.util.BaseUtilities;
import org.openide.util.RequestProcessor;

/**
 * A weak reference which is held strongly for a while after last access.
 * Lifecycle:
 * <ol>
 * <li>Created. Referent held strongly. A task is scheduled into the request
 *     processor for some time in the future (currently 15 seconds).</li>
 * <li>Expired. After the timeout, the reference switches to a normal weak
 *     reference.</li>
 * <li>Touched. If the value is accessed before it is garbage collected,
 *     whether the reference is expired or not, the reference is "touched".
 *     This means that the referent is again held strongly and the timeout
 *     is started from scratch.</li>
 * <li>Dead. If after expiry there is no access before the next full GC cycle,
 *     the GC algorithm may reclaim the reference. In this case the reference
 *     of course dies.</li>
 * </ol>
 * @author Jesse Glick
 */
public final class TimedWeakReference<T> extends WeakReference<T> implements Runnable {
    
    public static int TIMEOUT = 15000;
    
    private static final RequestProcessor RP = new RequestProcessor("TimedWeakReference"); // NOI18N
    
    private RequestProcessor.Task task;
    
    private T o;
    
    /** Time when the object was last time touched */
    private long touched;
    
    /**
     * Create a weak reference with timeout.
     * @param o the referent
     */
    public TimedWeakReference(T o) {
        super(o, BaseUtilities.activeReferenceQueue());
        this.o = o;
        task = RP.create(this);
        task.schedule(TIMEOUT);
    }
    
    public synchronized void run() {
        if (o != null) {
            //System.err.println("Expire " + k);
            // how long we've really been idle
            long unused  = System.currentTimeMillis() - touched;
            if (unused > TIMEOUT / 2) {
                o = null;
                touched = 0;
            } else {
                task.schedule(TIMEOUT - (int) unused);
            }
        }
    }
    
    public synchronized T get() {
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
