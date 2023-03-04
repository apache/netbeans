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

package org.netbeans.api.queries;

import javax.swing.event.ChangeListener;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

// XXX is the Status interface scalable enough for efficient use even from e.g. a Look?
// May need to be revisited, perhaps with an optional more efficient implementation...

/**
 * Test whether a file can be considered to be built (up to date).
 * @see FileBuiltQueryImplementation
 * @author Jesse Glick
 */
public final class FileBuiltQuery {
    
    private static final Lookup.Result<FileBuiltQueryImplementation> implementations =
        Lookup.getDefault().lookupResult(FileBuiltQueryImplementation.class);
    
    private FileBuiltQuery() {}
    
    /**
     * Check whether a (source) file has been <em>somehow</em> built
     * or processed.
     * <div class="nonnormative">
     * <p>
     * This would typically mean that at least its syntax has been
     * validated by a build system, some conventional output file exists
     * and is at least as new as the source file, etc.
     * For example, for a <samp>Foo.java</samp> source file, this could
     * check whether <samp>Foo.class</samp> exists (in the appropriate
     * build directory) with at least as new a timestamp.
     * </p>
     * <p>
     * <strong>Implementation note:</strong> the current implementation of this
     * method does not react to changes in lookup results for
     * {@link FileBuiltQueryImplementation}. For example, if there is initially
     * no provider for a given file, the return value may be null, and a client
     * will not be see the change if a provider is later installed dynamically.
     * Similarly, removal of a provider will not automatically invalidate an
     * existing {@link Status} object; and a change in the provider responsible
     * for a given file will not produce updates in an existing {@link Status}.
     * A future implementation may however be enhanced to return proxy statuses
     * which react to changes in the provider responsible for the file and always
     * delegate to the current provider, if there is one.
     * </p>
     * </div>
     * @param file a source file which can be built to a direct product
     * @return a status object that can be listened to, or null for no answer
     */
    public static Status getStatus(FileObject file) {
        if (!file.isValid()) {
            // Probably a race condition of some kind, abort gracefully.
            return null;
        }
        for (FileBuiltQueryImplementation fbqi : implementations.allInstances()) {
            Status s = fbqi.getStatus(file);
            if (s != null) {
                return s;
            }
        }
        return null;
    }
    
    /**
     * Result of getting built status for a file.
     * Besides encoding the actual result, it permits listening to changes.
     * @see #getStatus
     */
    public interface Status {
        
        /**
         * Check whether the file is currently built.
         * @return true if it is up-to-date, false if it may still need to be built
         */
        boolean isBuilt();
        
        /**
         * Add a listener to changes.
         * @param l a listener to add
         */
        void addChangeListener(ChangeListener l);
        
        /**
         * Stop listening to changes.
         * @param l a listener to remove
         */
        void removeChangeListener(ChangeListener l);
        
    }
    
}
