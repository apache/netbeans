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

package org.netbeans.spi.queries;

import java.io.File;

/**
 * Determine whether files should be shared (for example in a VCS) or are intended
 * to be unshared.
 * <div class="nonnormative">
 * <p>
 * Could be implemented e.g. by project types which know that certain files or folders in
 * a project (e.g. <code>src/</code>) are intended for VCS sharing while others
 * (e.g. <code>build/</code>) are not.
 * </p>
 * <p>
 * Note that the Project API module registers a default implementation of this query
 * which delegates to the project which owns the queried file, if there is one.
 * This is more efficient than searching instances in global lookup, so use that
 * facility wherever possible.
 * </p>
 * </div>
 * <p>
 * Threading note: implementors should avoid acquiring locks that might be held
 * by other threads. Generally treat this interface similarly to SPIs in
 * {@link org.openide.filesystems} with respect to threading semantics.
 * </p>
 * @see org.netbeans.api.queries.SharabilityQuery
 * @see <a href="@org-netbeans-modules-project-ant@/org/netbeans/spi/project/support/ant/AntProjectHelper.html#createSharabilityQuery(java.lang.String[],%20java.lang.String[])"><code>AntProjectHelper.createSharabilityQuery(...)</code></a>
 * @since 1.27
 * @author Jesse Glick
 * @deprecated Use {@link org.netbeans.spi.queries.SharabilityQueryImplementation2} instead.
 * 
 */
@Deprecated public interface SharabilityQueryImplementation {
    
    /**
     * Check whether a file or directory should be shared.
     * If it is, it ought to be committed to a VCS if the user is using one.
     * If it is not, it is either a disposable build product, or a per-user
     * private file which is important but should not be shared.
     * @param file a {@link org.openide.filesystems.FileUtil#normalizeFile normalized} file to check for sharability (may or may not yet exist)
     * @return one of {@link org.netbeans.api.queries.SharabilityQuery}'s constants
     */
    int getSharability(File file);
    
}
