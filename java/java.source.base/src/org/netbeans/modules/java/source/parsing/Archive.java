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

package org.netbeans.modules.java.source.parsing;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Set;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
/** Methods for accessing an archive. Archive represents zip file or
 * folder.
 *
 * @author Petr Hrebejk
 */
public interface Archive {
    // New implementation Archive Interface ------------------------------------


    /** Gets all files in given folder
     *  @param folderName name of folder to list, path elements separated by / char
     *  @param entry owning ClassPath.Entry to check the excludes or null if everything should be included
     *  @param kinds to list, may be null => all types
     *  @param filter to filter the file content
     *  @param recursive if true content of subfolders is included
     *  @return the listed files
     */
    public Iterable<JavaFileObject> getFiles( String folderName, ClassPath.Entry entry, Set<JavaFileObject.Kind> kinds, JavaFileFilterImplementation filter, boolean recursive) throws IOException;

    /*
     * Returns a new {@link JavaFileObject} for given path.
     * May throw an UnsupportedOperationException if the operation is not supported (eg. zip archive).
     * @param relativePath path from the root, separated by '/' character (resource name)
     * @return the {@link JavaFileObject}
     */
    public JavaFileObject create (final String relativeName, JavaFileFilterImplementation filter) throws UnsupportedOperationException;

    /**
     * Cleans cached data
     */
    public void clear ();

    /**
     * Returns a {@link JavaFileObject} for given name or null
     * @param name of resource
     * @return a file
     */
    public JavaFileObject getFile(final @NonNull String name) throws IOException;

    /**
     * Returns a {@link URI} for a directory of the given name, or null if it does
     * not exist.
     *
     * @param dirName the name of the directory
     * @return a URI if the given directory is in this archive, {@code null} otherwise.
     */
    public URI getDirectory(final @NonNull String dirName) throws IOException;

    /**
     * Checks if the {@link Archive} is represents a multi release archive.
     * @return true if the {@link Archive} is supports multiple releases.
     */
    public boolean isMultiRelease();

    public static Archive EMPTY = new Archive() {
        @Override
        public Iterable<JavaFileObject> getFiles(String folderName, ClassPath.Entry entry, Set<JavaFileObject.Kind> kinds, JavaFileFilterImplementation filter, boolean recursive) throws IOException {
            return Collections.emptyList();
        }

        @Override
        public JavaFileObject create(String relativeName, JavaFileFilterImplementation filter) throws UnsupportedOperationException {
            return null;
        }

        @Override
        public void clear() {
        }

        @Override
        public JavaFileObject getFile(String name) throws IOException {
            return null;
        }

        @Override
        public URI getDirectory(String name) throws IOException {
            return null;
        }

        @Override
        public boolean isMultiRelease() {
            return false;
        }
    };
}
