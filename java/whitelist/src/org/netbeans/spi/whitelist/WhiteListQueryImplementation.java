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
package org.netbeans.spi.whitelist;

import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.whitelist.WhiteListQuery;
import org.openide.filesystems.FileObject;

/**
 * Permits projects to provide wite list used to emit errors for usages of non allowed
 * types or methods. The project types supporting a runtime with class usage
 * restriction implement this interface to provide a white list of allowed types (methods).
 * @author Tomas Zezula
 * @author David Konecny
 */
public interface WhiteListQueryImplementation {

    /**
     * Returns a white list for given file.
     * @param file to return white list for.
     * @return the {@link WhiteListImplementation} for given file or null if no white list
     * is associated with given file.
     */
    @CheckForNull
    WhiteListImplementation getWhiteList(
          @NonNull FileObject file);

    /**
     * All white lists which should be displayed in UI for user should implement
     * this interface. And on the other hand non-visible implementations like
     * for example merging WhiteListQueryImplementation should not implement it.
     */
    public interface UserSelectable extends WhiteListQueryImplementation {
        String getDisplayName();
        String getId();
    }

    /**
     * The white list implementation used to emit errors for usages of non allowed
     * types or methods.
     */
    public interface WhiteListImplementation {

        /**
         * Checks if given method (type) can be invoked (accessed).
         * @param element to check
         * @param operation the operation which should be tested
         * @return a {@link Result} holding the details.
         */
        @NonNull
        WhiteListQuery.Result check(@NonNull ElementHandle<?> element, @NonNull WhiteListQuery.Operation operation);

        /**
         * Adds {@link ChangeListener} to white list.
         * @param listener to be added
         */
        void addChangeListener(@NonNull final ChangeListener listener);

        /**
         * Removes {@link ChangeListener} from white list.
         * @param listener to be removed
         */
        void removeChangeListener(@NonNull final ChangeListener listener);
    }

}
