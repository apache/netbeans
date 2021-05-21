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

import java.util.List;
import java.util.Map;

/**
 * Library content set for library content for Payara features configuration.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class FileSet {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Paths retrieved from XML elements. */
    private final List<String> paths;

    /** Links retrieved from XML elements. */
    private final List<String> links;

    /** File sets retrieved from XML elements. */
    private final Map<String, List<String>> filesets;

    /** Links retrieved from XML elements. */
    private final List<String> lookups;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of Library content for Payara libraries
     * configuration.
     * <p/>
     * @param paths    Paths retrieved from XML elements.
     * @param links    Links retrieved from XML elements.
     * @param filesets File sets retrieved from XML elements.
     * @param lookups  Lookups retrieved from XML elements.
     */
    public FileSet(final List<String> paths, final List<String> links,
            final Map<String, List<String>> filesets,
            final List<String> lookups) {
        this.paths = paths;
        this.links = links;
        this.filesets = filesets;
        this.lookups = lookups;
    }

    /**
     * Creates an instance of Library content for Payara libraries
     * configuration.
     * <p/>
     * Content of links and lookups is set to <code>null</code>.
     * <p/>
     * @param paths    Paths retrieved from XML elements.
     * @param filesets File sets retrieved from XML elements.
     */
    public FileSet(final List<String> paths,
            final Map<String, List<String>> filesets) {
        this(paths, null, filesets, null);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get paths retrieved from XML elements.
     * <p/>
     * @return Paths sets retrieved from XML elements.
     */
    public List<String> getPaths() {
        return paths;
    }

    /**
     * Get links retrieved from XML elements.
     * <p/>
     * @return Links sets retrieved from XML elements.
     */
    public List<String> getLinks() {
        return links;
    }

    /**
     * Get file sets retrieved from XML elements.
     * <p/>
     * @return File sets retrieved from XML elements.
     */
    public Map<String, List<String>> getFilesets() {
        return filesets;
    }
    
    /**
     * Get lookups retrieved from XML elements.
     * <p/>
     * @return Links sets retrieved from XML elements.
     */
    public List<String> getLookups() {
        return lookups;
    }

}
