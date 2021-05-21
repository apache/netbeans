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

/**
 * Interface for various implementations that read data from domain config (domain.xml).
 *
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public interface XMLReader {

    /**
     * Every implementation needs to provide path objects.
     * Path represents the xpath on which the reader wants to be notified.
     *
     * @return paths that the reader listens to
     */
    public List<TreeParser.Path> getPathsToListen();

}
