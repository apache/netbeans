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
package org.netbeans.spi.queries;

import java.net.URI;

/**
 * A query which should typically be provided by a VCS to give information
 * about whether some files can be considered part of one logical directory tree.
 * <p>
 * This should be treated as a heuristic, useful when deciding whether to use
 * absolute or relative links between path locations.
 * </p>
 * <p>
 * The file names might refer to nonexistent files. A provider may or may not
 * be able to say anything useful about them in this case.
 * </p>
 * <p>
 * File names passed to this query will already have been normalized according to
 * the semantics of {@link org.openide.filesystems.FileUtil#normalizeFile}.
 * </p>
 * <p>
 * Threading note: implementors should avoid acquiring locks that might be held
 * by other threads. Generally treat this interface similarly to SPIs in
 * {@link org.openide.filesystems} with respect to threading semantics.
 * </p>
 * @see org.netbeans.api.queries.CollocationQuery
 * @since 1.27
 * @author Jesse Glick
 * @author Alexander Simon
 */
public interface CollocationQueryImplementation2 {
    
    /**
     * Check whether two files are logically part of one directory tree.
     * For example, if both files are stored in CVS, with the same server
     * (<code>CVSROOT</code>) they might be considered collocated.
     * If they are to be collocated their absolute paths must share a
     * prefix directory, i.e. they must be located in the same filesystem root.
     * If nothing is known about them, return false.
     * @param file1 one file
     * @param file2 another file
     * @return true if they are probably part of one logical tree
     */
    boolean areCollocated(URI file1, URI file2);
    
    /**
     * Find a root of a logical tree containing this file, if any.
     * The path of the root (if there is one) must be a prefix of the path of the file.
     * @param file a file on disk (must be an absolute URI)
     * @return an ancestor directory which is the root of a logical tree,
     *         if any (else null) (must be an absolute URI)
     */
    URI findRoot(URI file);
    
}

