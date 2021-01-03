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

package org.netbeans.modules.python.source.queries;

import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;

/**
 *
 * Permits providers to return specification source level of Java source file.
 * <p>
 * A default implementation is registered by the
 * <code>org.netbeans.modules.python.project</code> module which looks up the
 * project corresponding to the file (if any) and checks whether that
 * project has an implementation of this interface in its lookup. If so, it
 * delegates to that implementation. Therefore it is not generally necessary
 * for a project type provider to register its own global implementation of
 * this query, if it depends on the Python Project module and uses this style.
 * </p>
 * @see org.netbeans.modules.python.source.queries.SourceLevelQuery
 */
public interface SourceLevelQueryImplementation {

    /**
     * Returns source level of the given Python file.
     * @param pythonFile Python source file in question
     * @return source level of the Python file encapsulated as {@link Result}, or
     *    null if the file is not handled by this provider.
     */
    Result getSourceLevel(FileObject pythonFile);

    /**
     * Result of finding source level, encapsulating the answer as well as the
     * ability to listen to it.
     */
    interface Result {

        /**
         * Get the source level.
         * @return a source level of the Python file, e.g. "2.6", "3.0", "3.1"
         * or null if the source level is unknown. It is allowed to return source level synonyms
         *    e.g. "3" for "Python 3". These synonyms are always normalized by
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
}
