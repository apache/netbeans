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
package org.netbeans.spi.java.queries;

import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.queries.AccessibilityQuery;
import org.openide.filesystems.FileObject;

/**
 * Permits providers to mark certain Java packages as being inaccessible to
 * outside code despite possibly containing public classes.
 * <p>
 * A default implementation is registered by the
 * <code>org.netbeans.modules.java.project</code> module which looks up the
 * project corresponding to the file (if any) and checks whether that
 * project has an implementation of this interface in its lookup. If so, it
 * delegates to that implementation. Therefore it is not generally necessary
 * for a project type provider to register its own global implementation of
 * this query, if it depends on the Java Project module and uses this style.
 * </p>
 * @see org.netbeans.api.java.queries.AccessibilityQuery
 * @see <a href="@org-netbeans-modules-projectapi@/org/netbeans/api/project/FileOwnerQuery.html">FileOwnerQuery</a>
 * @see <a href="@org-netbeans-modules-projectapi@/org/netbeans/api/project/Project.html#getLookup()">Project#getLookup</a>
 * @author Tomas Zezula
 * @since 1.64
 */
public interface AccessibilityQueryImplementation2 {

    /**
     * Checks whether a given Java package (folder of source files)
     * is intended to be publicly accessed by code residing in other
     * compilation units.
     * @param pkg a Java source package
     * @return the {@link Result} object encapsulating the accessibility of the Java package.
     */
    @CheckForNull
    public Result isPubliclyAccessible(@NonNull FileObject pkg);

    /**
     * Result of finding accessibility of a Java package, encapsulating the answer as well as the
     * ability to listen to it.
     */
    interface Result {
        /**
         * Returns the accessibility.
         * @return the {@link AccessibilityQuery.Accessibility}
         */
        @NonNull
        AccessibilityQuery.Accessibility getAccessibility();
        /**
         * Add a listener to changes of source level.
         * @param listener a listener to add
         */
        void addChangeListener(@NonNull ChangeListener listener);

        /**
         * Remove a listener to changes of source level.
         * @param listener a listener to add
         */
        void removeChangeListener(@NonNull ChangeListener listener);
    }
}
