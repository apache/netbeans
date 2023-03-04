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

import org.netbeans.modules.visual.graph.layout.TreeGraphLayout;

/**
 * @author David Kaspar
 * @since 2.4
 */
public class GraphLayoutSupport {

    /**
     * Sets a root node to a tree graph layout.
     * @param graph the tree graph layout
     * @param rootNode the root node
     * @since 2.4
     */
    public static <N,E> void setTreeGraphLayoutRootNode (GraphLayout<N,E> graph, N rootNode) {
        if (graph instanceof TreeGraphLayout)
            ((TreeGraphLayout<N,E>) graph).setRootNode (rootNode);
    }

    /**
     * Sets properties to a tree graph layout.
     * @param graph the tree graph layout
     * @param originX the x-axis origin
     * @param originY the y-axis origin
     * @param verticalGap the vertical gap between cells
     * @param horizontalGap the horizontal gap between cells
     * @param vertical if true, then layout organizes the graph vertically; if false, then horizontally
     * @since 2.7
     */
    public static <N,E> void setTreeGraphLayoutProperties (GraphLayout<N,E> graph, int originX, int originY, int verticalGap, int horizontalGap, boolean vertical) {
        if (graph instanceof TreeGraphLayout)
            ((TreeGraphLayout<N,E>) graph).setProperties (originX, originY, verticalGap, horizontalGap, vertical);
    }

   /**
     * Sets properties to a tree graph layout.
     * @param graph the tree graph layout
     * @param originX the x-axis origin
     * @param originY the y-axis origin
     * @param verticalGap the vertical gap between cells
     * @param horizontalGap the horizontal gap between cells
     * @param vertical if true, then layout organizes the graph vertically; if false, then horizontally
     * @param minimizeGap if true, then minimize the gap between cells; if false do the normal tree layout.
     * @since 2.25
     */
    public static <N, E> void setTreeGraphLayoutProperties(GraphLayout<N, E> graph, int originX, int originY, int verticalGap, int horizontalGap, boolean vertical, boolean minimizeGap) {
        if (graph instanceof TreeGraphLayout)
            setTreeGraphLayoutProperties(graph, originX, originY, verticalGap, horizontalGap, vertical, minimizeGap, TreeGraphLayoutAlignment.TOP);
    }

   /**
     * Sets properties to a tree graph layout.
     * @param graph the tree graph layout
     * @param originX the x-axis origin
     * @param originY the y-axis origin
     * @param verticalGap the vertical gap between cells
     * @param horizontalGap the horizontal gap between cells
     * @param vertical if true, then layout organizes the graph vertically; if false, then horizontally
     * @param minimizeGap if true, then minimize the gap between cells; if false do the normal tree layout.
     * @param alignment alignment the alignment of the nodes in their level. Choose wheter {@code TreeGraphLayout.Alignment.TOP}, {@code TreeGraphLayout.Alignment.CENTER} or {@code TreeGraphLayout.Alignment.BOTTOM}
     * @since 2.25
     */
    public static <N, E> void setTreeGraphLayoutProperties(GraphLayout<N, E> graph, int originX, int originY, int verticalGap, int horizontalGap, boolean vertical, boolean minimizeGap, TreeGraphLayoutAlignment alignment) {
        if (graph instanceof TreeGraphLayout)
            ((TreeGraphLayout<N, E>) graph).setProperties(originX, originY, verticalGap, horizontalGap, vertical,minimizeGap, alignment);
    }

}
