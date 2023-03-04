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

import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.openide.util.Lookup;

/**
 * to be registered as service and provide panels for the ArtifactViewer
 * @author mkleint
 */
public interface ArtifactViewerPanelProvider {


    /***
     *
     * @param content lookup that will contain Artifact from start and
     * eventually at some point in time later also DependencyNode and MavenProject instances..
     * @return
     */
    MultiViewDescription createPanel(Lookup content);
}
