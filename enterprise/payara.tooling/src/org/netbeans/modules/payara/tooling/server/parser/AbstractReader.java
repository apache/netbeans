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

/**
 * Abstract XML element reader.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public abstract class AbstractReader extends TreeParser.NodeListener {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Tree parser element path. */
    final String path;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of Java EE platform check configuration
     * XML element reader.
     * <p/>
     * @param pathPrefix Tree parser path prefix to be prepended before
     *                   current XML element.
     * @param node       XML element name.
     */
    AbstractReader(final String pathPrefix, final String node) {
        StringBuilder sb = new StringBuilder(
                (pathPrefix != null ? pathPrefix.length() : 0)
                + TreeParser.PATH_SEPARATOR.length() + node.length());
        if (pathPrefix != null)
            sb.append(pathPrefix);
        sb.append(TreeParser.PATH_SEPARATOR);
        sb.append(node);
        path = sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get current element tree parser path.
     * <p/>
     * @return Current element tree parser path.
     */
    String getPath() {
        return path;
    }

}
