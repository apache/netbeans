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

package org.netbeans.spi.queries;

import java.io.File;

/**
 * Determine whether files should be shared (for example in a VCS) or are intended
 * to be unshared.
 * <div class="nonnormative">
 * <p>
 * Could be implemented e.g. by project types which know that certain files or folders in
 * a project (e.g. <samp>src/</samp>) are intended for VCS sharing while others
 * (e.g. <samp>build/</samp>) are not.
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
