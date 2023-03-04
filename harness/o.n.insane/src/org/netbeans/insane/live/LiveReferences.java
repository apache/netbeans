/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
