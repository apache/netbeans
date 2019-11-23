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

import java.util.LinkedList;
import java.util.List;

/**
 * <code>sources</code> library configuration XML element reader.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class ConfigReaderSources extends ConfigReader {

    ////////////////////////////////////////////////////////////////////////////
    // XML reader methods                                                     //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Provide paths to listen on.
     * <p/>
     * Sets readers for internal <code>javadocs</code> elements.
     * <p/>
     * @return Paths that the reader listens to.
     */
    @Override
    public List<TreeParser.Path> getPathsToListen() {
        LinkedList<TreeParser.Path> paths = new LinkedList<>();
        paths.add(new TreeParser.Path("/server/library/sources/file", pathReader));
        paths.add(new TreeParser.Path("/server/library/sources/fileset", filesetReader));
        return paths;
    }
    
}
