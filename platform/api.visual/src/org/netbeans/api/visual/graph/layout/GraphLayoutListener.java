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
package org.netbeans.api.visual.graph.layout;

import java.awt.*;

/**
 * @author David Kaspar
 */
public interface GraphLayoutListener<N,E> {

    /**
     * Called when a graph layout is started.
     * @param graph the universal graph
     */
    void graphLayoutStarted (UniversalGraph<N, E> graph);

    /**
     * Called when a graph layout is finished.
     * @param graph the universal graph
     */
    void graphLayoutFinished (UniversalGraph<N, E> graph);

    /**
     * Called when a graph layout resolves a new location for a node.
     * @param graph the universal graph
     * @param node the node with changed preferred location
     * @param previousPreferredLocation the previous preferred location
     * @param newPreferredLocation the new preferred location
     */
    void nodeLocationChanged (UniversalGraph<N,E> graph, N node, Point previousPreferredLocation, Point newPreferredLocation);

}
