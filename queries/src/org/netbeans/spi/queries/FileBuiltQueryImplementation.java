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

import org.netbeans.api.queries.FileBuiltQuery;
import org.openide.filesystems.FileObject;

/**
 * Test whether a file can be considered to be built (up to date).
 * Register to default lookup.
 * <p class="nonnormative">
 * Rather than registering a global instance, if your implementation
 * is applicable only to project-owned files, you should add it to
 * <a href="@org-netbeans-modules-projectapi@/org/netbeans/api/project/Project.html#getLookup"><code>Project.getLookup()</code></a>
 * and depend on
 * the <code>org.netbeans.modules.projectapi</code> module.
 * </p>
 * @see FileBuiltQuery
 * @see <a href="@org-netbeans-modules-project-ant@/org/netbeans/spi/project/support/ant/AntProjectHelper.html#createGlobFileBuiltQuery(java.lang.String[],%20java.lang.String[])"><code>AntProjectHelper.createGlobFileBuiltQuery(...)</code></a>
 * @author Jesse Glick
 */
public interface FileBuiltQueryImplementation {
    
    /**
     * Check whether a (source) file has been <em>somehow</em> built
     * or processed.
     * This would typically mean that at least its syntax has been
     * validated by a build system, some conventional output file exists
     * and is at least as new as the source file, etc.
     * For example, for a <samp>Foo.java</samp> source file, this could
     * check whether <samp>Foo.class</samp> exists (in the appropriate
     * build directory) with at least as new a timestamp.
     * @param file a source file which can be built to a direct product
     * @return a status object that can be queries and listened to,
     *         or null for no answer
     */
    FileBuiltQuery.Status getStatus(FileObject file);
    
}
