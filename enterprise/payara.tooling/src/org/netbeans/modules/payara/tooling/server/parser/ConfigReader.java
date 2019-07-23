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
package org.netbeans.modules.payara.tooling.server.parser;

import java.util.List;
import java.util.Map;

/**
 * Common library configuration XML elements reader.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public abstract class ConfigReader implements XMLReader {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** <code>fileset</code> XML element reader. */
    final FilesetReader filesetReader = new FilesetReader();

    /** <code>file</code> XML element reader. */
    final PathReader pathReader
            = new PathReader("/server/library/classpath");

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get file sets retrieved from XML elements.
     * <p/>
     * @return File sets retrieved from XML elements.
     */
    Map<String, List<String>> getFilesets() {
        return filesetReader.getFilesets();
    }
    
    /**
     * Get paths retrieved from XML elements.
     * <p/>
     * @return Paths sets retrieved from XML elements.
     */
    List<String> getPaths() {
        return pathReader.getPaths();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Reset this XML element reader.
     */
    void reset() {
        filesetReader.reset();
        pathReader.reset();
    }
}
