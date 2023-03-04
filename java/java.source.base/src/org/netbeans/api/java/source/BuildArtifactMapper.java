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

package org.netbeans.api.java.source;

import java.io.File;
import java.net.URL;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.modules.java.source.usages.BuildArtifactMapperImpl;

/**
 * @since 0.37
 * 
 * @author Jan Lahoda
 */
public class BuildArtifactMapper {

    /**
     * Add an {@link ArtifactsUpdated} listener. The method {@link ArtifactsUpdated#artifactsUpdated(java.lang.Iterable)}
     * will be called each time the files inside the output folder are updated.
     * The output folder computed for the source root using the {@link BinaryForSourceQuery}.
     * The files in the output folder are updated only if file <code>.netbeans_automatic_build</code>
     * exists inside the output folder.
     * 
     * @param sourceRoot the listener will be assigned to this source root
     * @param listener listener to add
     * @since 0.37
     */
    public static void addArtifactsUpdatedListener(@NonNull URL sourceRoot, @NonNull ArtifactsUpdated listener) {
        BuildArtifactMapperImpl.addArtifactsUpdatedListener(sourceRoot, listener);
    }
    
    /**
     * Remove an {@link ArtifactsUpdated} listener.
     *
     * @param sourceRoot the listener will be assigned to this source root
     * @param listener listener to add
     * @since 0.37
     */
    public static void removeArtifactsUpdatedListener(@NonNull URL sourceRoot, @NonNull ArtifactsUpdated listener) {
        BuildArtifactMapperImpl.removeArtifactsUpdatedListener(sourceRoot, listener);
    }

    /**
     * Notify that the files in the output directory has been updated.
     * @since 0.37
     */
    public static interface ArtifactsUpdated {
        /**
         * Notify that the files in the output directory has been updated.
         *
         * @param artifacts the updated files
         * @since 0.37
         */
        public void artifactsUpdated(@NonNull Iterable<File> artifacts);
    }

    /**
     * Checks if compile on save is supported in the current configuration.
     *
     * @return true iff compile on save is supported in this run.
     * @since 2.32
     */
    public static boolean isCompileOnSaveSupported() {
        return BuildArtifactMapperImpl.isCompileOnSaveSupported();
    }
}
