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

package org.netbeans.modules.maven.indexer.spi.ui;

import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.openide.util.lookup.ServiceProvider;

/**
 * Displays artifacts under the Maven Repositories node.
 * @since 2.6
 * @see ServiceProvider
 */
public interface ArtifactNodeSelector {

    /**
     * Tries to select the node corresponding to a given artifact, if it can be located.
     * @param artifact to display
     */
    void select(NBVersionInfo artifact);

}
