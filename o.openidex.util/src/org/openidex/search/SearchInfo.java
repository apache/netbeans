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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2007 Sun
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
