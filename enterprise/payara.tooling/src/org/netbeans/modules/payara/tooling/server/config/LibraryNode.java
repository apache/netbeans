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
package org.netbeans.modules.payara.tooling.server.config;

/**
 * Internal library node element.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class LibraryNode {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Library ID. */
    final String libraryID;

    /** Class path file set. */
    final FileSet classpath;

    /** Java doc file set. */
    final FileSet javadocs;

    /** Java sources file set. */
    final FileSet sources;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of internal library node element.
     * <p/>
     * @param libraryID Library ID.
     * @param classpath Class path file set
     * @param javadocs  Java doc file set.
     * @param sources   Java sources file set.
     */
    public LibraryNode(final String libraryID, final FileSet classpath,
            final FileSet javadocs, final FileSet sources) {
        this.libraryID = libraryID;
        this.classpath = classpath;
        this.javadocs = javadocs;
        this.sources = sources;
    }
    
}
