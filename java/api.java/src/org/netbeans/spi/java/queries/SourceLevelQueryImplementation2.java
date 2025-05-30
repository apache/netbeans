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
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.openide.filesystems.FileObject;

/**
 *
 * Permits providers to return specification source level of Java source file.
 * <p>
 * A default implementation is registered by the
 * <code>org.netbeans.modules.java.project</code> module which looks up the
 * project corresponding to the file (if any) and checks whether that
 * project has an implementation of this interface in its lookup. If so, it
 * delegates to that implementation. Therefore it is not generally necessary
 * for a project type provider to register its own global implementation of
 * this query, if it depends on the Java Project module and uses this style.
 * </p>
 * @see org.netbeans.api.java.queries.SourceLevelQuery
 * @see <a href="@org-netbeans-modules-projectapi@/org/netbeans/api/project/FileOwnerQuery.html">FileOwnerQuery</a>
 * @see <a href="@org-netbeans-modules-projectapi@/org/netbeans/api/project/Project.html#getLookup()">Project#getLookup</a>
 * @see org.netbeans.api.java.classpath.ClassPath#BOOT
 * @author Tomas Zezula
 * @since 1.30
 */
public interface SourceLevelQueryImplementation2 {

    /**
     * Returns source level of the given Java file. For acceptable return values
     * see the documentation of <code>-source</code> command line switch of
     * <code>javac</code> compiler .
     * @param javaFile Java source file in question
     * @return source level of the Java file encapsulated as {@link Result}, or
     *    null if the file is not handled by this provider.
     */
    Result getSourceLevel(FileObject javaFile);

    /**
     * Result of finding source level, encapsulating the answer as well as the
     * ability to listen to it.
     * @since 1.30
     */
    interface Result {

        /**
         * Get the source level.
         * @return a source level of the Java file, e.g. "1.3", "1.4", "1.5"
         * or null if the source level is unknown. It is allowed to return source level synonyms
         *    e.g. "5" for "1.5". These synonyms are always normalized by
         * {@link SourceLevelQuery#getSourceLevel}.
         */
        @CheckForNull String getSourceLevel();

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

    /**
     * Result of finding a source level and profile as well as the ability
     * to listen on changes.
     * @since 1.47
     */
    interface Result2 extends Result {

        /**
         * Returns the required profile.
         * @return the required profile. If the profile is either unknown
         * or unsupported by actual source level it returns the {@link SourceLevelQuery.Profile#DEFAULT}.
         * <div class="nonnormative">
         * The JDK 8 provides three limited profiles (compact1, compact2, compact3) in addition
         * to the full JDK. Each profile specifies a specific set of Java API packages and
         * contains all of the APIs of the smaller profile, @see http://openjdk.java.net/jeps/161
         * </div>
         */
        @NonNull
        SourceLevelQuery.Profile getProfile();
    }
}
