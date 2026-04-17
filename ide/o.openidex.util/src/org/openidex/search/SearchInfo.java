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

package org.openidex.search;

import java.util.Iterator;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 * Defines which <code>DataObject</code>s should be searched.
 * Iterator returned by this interface's method enumerates
 * <code>DataObject</code>s that should be searched.
 * <p>
 * <code>SearchInfo</code> objects are used by module User Utilities
 * &ndash; in actions <em>Find</em> (since User Utilities 1.16)
 * and <em>Find in Projects</em> (since User Utilities 1.23).
 * Action <em>Find</em> obtains <code>SearchInfo</code> from
 * <a href="@org-openide-nodes@/org/openide/nodes/Node.html#getLookup()"><code>Lookup</code> of nodes</a>
 * the action was invoked on. Action <em>Find in Projects</em> obtains
 * <code>SearchInfo</code> from
 * <a href="@org-netbeans-modules-projectapi@/org/netbeans/api/project/Project.html#getLookup()"><code>Lookup</code>
 * of the projects</a>.
 * </p>
 * <p>
 *  <b>Recommendation</b>: Use {@link Files} instead of this interface.
 *  See {@link #objectsToSearch()} and {@link Files#filesToSearch()}.
 * </p>
 *
 * @see  SearchInfoFactory
 * @see  <a href="@org-openide-loaders@/org/openide/loaders/DataObject.html"><code>DataObject</code></a>
 * @see  <a href="@org-openide-nodes@/org/openide/nodes/Node.html#getLookup()"><code>Node.getLookup()</code></a>
 * @see  <a href="@org-netbeans-modules-projectapi@/org/netbeans/api/project/Project.html#getLookup()"><code>Project.getLookup()</code></a>
 * @since  org.openidex.util/3 3.2
 * @author  Marian Petras
 */
public interface SearchInfo {

    /**
     * Determines whether the object which provided this <code>SearchInfo</code>
     * can be searched.
     * This method determines whether the <em>Find</em> action should be enabled
     * for the object or not.
     * <p>
     * This method must be very quick as it may be called frequently and its
     * speed may influence responsiveness of the whole application. If the exact
     * algorithm for determination of the result value should be slow, it is
     * better to return <code>true</code> than make the method slow.
     *
     * @return  <code>false</code> if the object is known that it cannot be
     *          searched; <code>true</code> otherwise
     * @since  org.openidex.util/3 3.3
     */
    public boolean canSearch();

    /**
     * Specifies which <code>DataObject</code>s should be searched.
     * The returned <code>Iterator</code> needn't implement method
     * {@link java.util.Iterator#remove remove()} (i.e. it may throw
     * <code>UnsupportedOperationException</code> instead of actual
     * implementation).
     *
     * <p>
     *  <b>Recommendation:</b> Use interface {@link Files} and its method
     *  {@link Files#filesToSearch}. It avoids unnecessary creation of
     *  {@link org.openide.loaders.DataObject}s, so searching works faster
     *  and consumes less memory.
     * </p>
     *
     * @return  iterator which iterates over <code>DataObject</code>s
     *          to be searched
     */
    public Iterator<DataObject> objectsToSearch();

    /**
     * Additionally defines which <code>FileObject</code>s should be searched.
     *
     * @since org.openidex.util/3 3.20
     * @author kaktus
     */
    public interface Files extends SearchInfo{

    /**
     * Specifies which <code>FileObject</code>s should be searched.
     * The returned <code>Iterator</code> needn't implement method
     * {@link java.util.Iterator#remove remove()} (i.e. it may throw
     * <code>UnsupportedOperationException</code> instead of actual
     * implementation).
     *
     * <div class="nonnormative"><p>
     *  If you implement this method, you usually do not need to implement
     *  {@link #objectsToSearch()}, i.e. {@link #objectsToSearch()} can throw
     *  {@link UnsupportedOperationException}.
     * </p></div>
     *
     * @return  iterator which iterates over <code>FileObject</code>s
     *          to be searched
     */
    public Iterator<FileObject> filesToSearch();
    }
}
