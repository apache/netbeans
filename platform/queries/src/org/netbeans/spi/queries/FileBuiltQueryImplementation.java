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

import org.netbeans.api.queries.FileBuiltQuery;
import org.openide.filesystems.FileObject;

/**
 * Test whether a file can be considered to be built (up to date).
 * Register to default lookup.
 * <p class="nonnormative">
 * Rather than registering a global instance, if your implementation
 * is applicable only to project-owned files, you should add it to
 * <a href="@org-netbeans-modules-projectapi@/org/netbeans/api/project/Project.html#getLookup--"><code>Project.getLookup()</code></a>
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
     * For example, for a <code>Foo.java</code> source file, this could
     * check whether <code>Foo.class</code> exists (in the appropriate
     * build directory) with at least as new a timestamp.
     * @param file a source file which can be built to a direct product
     * @return a status object that can be queries and listened to,
     *         or null for no answer
     */
    FileBuiltQuery.Status getStatus(FileObject file);
    
}
