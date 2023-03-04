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
package org.netbeans.api.search.provider;

import java.net.URI;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.spi.search.SearchFilterDefinition;
import org.openide.filesystems.FileObject;

/**
 * Listener for various events during searching.
 *
 * @author jhavlin
 */
public abstract class SearchListener {

    /** Constructor for subclasses. */
    protected SearchListener() {}

    /**
     * Called when a file is skipped - filtered out by a filter.
     *
     * @param fileObject the skipped file object.
     * @param filter filter that filtered out the file (can be null).
     * @param message message describing reasons for skipping (can be null).
     */
    public void fileSkipped(@NonNull FileObject fileObject,
            @NullAllowed SearchFilterDefinition filter,
            @NullAllowed String message) {
    }

    /**
     * Called when a file is skipped - filtered out by a filter.
     *
     * @param uri the skipped URI.
     * @param filter filter that filtered out the file (can be null).
     * @param message message describing reasons for skipping (can be null).
     *
     * @since org.netbeans.api.search/1.4
     */
    public void fileSkipped(@NonNull URI uri,
            @NullAllowed SearchFilterDefinition filter,
            @NullAllowed String message) {
    }

    /**
     * Called when a directory was visited.
     *
     * @param path Path of the visited directory.
     */
    public void directoryEntered(@NonNull String path) {
    }

    /**
     * Called when matching in file content is to start.
     *
     * @param fileName Name of file.
     */
    public void fileContentMatchingStarted(@NonNull String fileName) {
    }

    /**
     * Called when matching in file progresses.
     *
     * If matching in a file reaches some interresting point (e.g. next matched
     * line), the matching algorithm can call this method. The implementators
     * will probably update progress bar or do something similar.
     *
     * @param fileName Name of file whose content is being read.
     * @param fileOffset Offset in file that has been processed.
     */
    public void fileContentMatchingProgress(@NonNull String fileName,
            long fileOffset) {
    }

    /**
     * Called when an exception occurs during file content checking.
     *
     * @param fileName File that caused the error.
     * @param throwable Error description.
     */
    public void fileContentMatchingError(@NonNull String fileName,
            @NonNull Throwable throwable) {
    }

    /**
     * Called when a general error occurs.
     *
     * @param t Error description.
     */
    public void generalError(@NonNull Throwable t) {
    }
}
