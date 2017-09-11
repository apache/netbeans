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

package org.netbeans.insane.live;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.swing.BoundedRangeModel;

import org.netbeans.insane.impl.LiveEngine;
import org.netbeans.insane.scanner.Filter;

/**
 * A live references engine entry point.
 * Provides a means for tracing root-paths of given heap objects.
 *
 * @author nenik
 */
public final class LiveReferences {
    
    /**
     * Traces the heap from known roots until all of the objects in 
     * <code>objs</code> are found or all of the reachable heap is covered.
     * This call is highly time consuming and can block all of the application,
     * so it is mostly useful for debugging and runtime field analysis.
     *
     * @param objs a Collection of objects to trace
     * @return a map with one entry for each found object that maps from
     * the object to a {@link Path} instance.
     */
    public static Map<Object,Path> fromRoots(Collection<Object> objs) {
        return new LiveEngine().trace(objs, null);
    }

    /**
     * Traces the heap from known roots until all of the objects in 
     * <code>objs</code> are found or all of the reachable heap is covered.
     * This call is highly time consuming and can block all of the application,
     * so it is mostly useful for debugging and runtime field analysis.
     * This variant allows approximate tracking of the scan progress,
     * but for real visual feedback, paintImmediatelly might be necessary.
     *
     * @param objs a Collection of objects to trace
     * @param rootsHint a set of Object that should be considered roots. Can be null.
     * @param progress a model of a ProgressBar to be notified during the scan. Can be null.
     * @return a map with one entry for each found object that maps from
     * the object to a {@link Path} instance.
     *
     */
    public static Map<Object,Path> fromRoots(Collection<Object> objs, Set<Object> rootsHint, BoundedRangeModel progress) {
        return new LiveEngine(progress).trace(objs, rootsHint);
    }

    /**
     * Traces the heap from known roots until all of the objects in 
     * <code>objs</code> are found or all of the reachable heap is covered.
     * This call is highly time consuming and can block all of the application,
     * so it is mostly useful for debugging and runtime field analysis.
     * This variant allows approximate tracking of the scan progress,
     * but for real visual feedback, paintImmediatelly might be necessary.
     *
     * @param objs a Collection of objects to trace
     * @param rootsHint a set of Object that should be considered roots. Can be null.
     * @param progress a model of a ProgressBar to be notified during the scan. Can be null.
     * @param f the {@link Filter} to apply on heap's live objects.
     * @return a map with one entry for each found object that maps from
     * the object to a {@link Path} instance.
     *
     */
    public static Map<Object,Path> fromRoots(Collection<Object> objs, Set<Object> rootsHint, BoundedRangeModel progress, Filter f) {
        return new LiveEngine(progress, f).trace(objs, rootsHint);
    }
    
    /** No instances */
    private LiveReferences() {}
}
