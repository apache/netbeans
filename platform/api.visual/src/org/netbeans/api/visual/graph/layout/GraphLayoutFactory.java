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

import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.modules.visual.graph.layout.HierarchicalLayout;
import org.netbeans.modules.visual.graph.layout.OrthogonalLayout;
import org.netbeans.modules.visual.graph.layout.TreeGraphLayout;

/**
 * The factory class of all built-in GraphLayout based implementations.
 * 
 * @author David Kaspar
 * @since 2.4
 */
public class GraphLayoutFactory {

    /**
     * Creates a tree graph layout.
     * Use GraphLayoutSupport.setTreeGraphLayoutRootNode method to set the root node of the graph.
     * If not set/found, then layout is not executed.
     * Note: Use GraphLayoutSupport.setTreeGraphLayoutProperties method to set the parameters of the layout later.
     * @param originX the x-axis origin
     * @param originY the y-axis origin
     * @param verticalGap the vertical gap between cells
     * @param horizontalGap the horizontal gap between cells
     * @param vertical if true, then layout organizes the graph vertically; if false, then horizontally
     * @return the tree graph layout
     * @since 2.4
     */
    public static <N, E> GraphLayout<N, E> createTreeGraphLayout(int originX, int originY, int verticalGap, int horizontalGap, boolean vertical) {
        return new TreeGraphLayout<N, E>(originX, originY, verticalGap, horizontalGap, vertical);
    }

    /**
     * Creates a tree graph layout.
     * Use GraphLayoutSupport.setTreeGraphLayoutRootNode method to set the root node of the graph.
     * If not set/found, then layout is not executed.
     * Note: Use GraphLayoutSupport.setTreeGraphLayoutProperties method to set the parameters of the layout later.
     * @param originX the x-axis origin
     * @param originY the y-axis origin
     * @param verticalGap the vertical gap between cells
     * @param horizontalGap the horizontal gap between cells
     * @param vertical if true, then layout organizes the graph vertically; if false, then horizontally
     * @param minimizeGap if true, then minimize the gap between cells; if false do the normal tree layout
     * @return the tree graph layout
     * @since 2.25
     */
    public static <N, E> GraphLayout<N, E> createTreeGraphLayout(int originX, int originY, int verticalGap, int horizontalGap, boolean vertical, boolean minimizeGap) {
        return createTreeGraphLayout(originX, originY, verticalGap, horizontalGap, vertical, minimizeGap, TreeGraphLayoutAlignment.TOP);
    }

    /**
     * Creates a tree graph layout.
     * Use GraphLayoutSupport.setTreeGraphLayoutRootNode method to set the root node of the graph.
     * If not set/found, then layout is not executed.
     * Note: Use GraphLayoutSupport.setTreeGraphLayoutProperties method to set the parameters of the layout later.
     * @param originX the x-axis origin
     * @param originY the y-axis origin
     * @param verticalGap the vertical gap between cells
     * @param horizontalGap the horizontal gap between cells
     * @param vertical if true, then layout organizes the graph vertically; if false, then horizontally
     * @param minimizeGap if true, then minimize the gap between cells; if false do the normal tree layout
     * @param alignment the alignment of the nodes in their level. Choose wheter {@code TreeGraphLayout.Alignment.TOP}, {@code TreeGraphLayout.Alignment.CENTER} or {@code TreeGraphLayout.Alignment.BOTTOM}
     * @return the tree graph layout
     * @since 2.25
     */
    public static <N, E> GraphLayout<N, E> createTreeGraphLayout(int originX, int originY, int verticalGap, int horizontalGap, boolean vertical, boolean minimizeGap, TreeGraphLayoutAlignment alignment) {
        return new TreeGraphLayout<>(originX, originY, verticalGap, horizontalGap, vertical, minimizeGap, alignment);
    }

    /**
     * 
     * @param <N> the node class for the nodes in the graph.
     * @param <E> the edge class for the edges in the graph.
     * @param graphScene the GraphScene on which the layout is to be invoked.
     * @param animate if true, the layout will animate the nodes into their new
     * positions.
     * @return a GraphLayout to be invoked from the calling class.
     */
    public static <N, E> GraphLayout<N, E> createOrthogonalGraphLayout(GraphScene<N, E> graphScene, boolean animate) {
        return new OrthogonalLayout<>(graphScene, animate);
    }

    /**
     * 
     * @param <N> the node class for the nodes in the graph.
     * @param <E> the edge class for the edges in the graph.
     * @param graphScene the GraphScene on which the layout is to be invoked.
     * @param animate if true, the layout will animate the nodes into their new
     * positions.
     * @return a GraphLayout to be invoked from the calling class.
     */
    public static <N, E> GraphLayout<N, E> createHierarchicalGraphLayout(GraphScene<N, E> graphScene, boolean animate) {
        return new HierarchicalLayout<>(graphScene, animate);
    }

    /**
     * 
     * @param <N> the node class for the nodes in the graph.
     * @param <E> the edge class for the edges in the graph.
     * @param graphScene the GraphScene on which the layout is to be invoked.
     * @param animate if true, the layout will animate the nodes into their new
     * positions.
     * @param inverted if true, the target nodes of an edge will be poisitioned
     * in a layer higher than its source node.
     * @return a GraphLayout to be invoked from the calling class.
     */
    public static <N, E> GraphLayout<N, E> createHierarchicalGraphLayout(GraphScene<N, E> graphScene, boolean animate, boolean inverted) {
        return new HierarchicalLayout<>(graphScene, animate, inverted);
    }
    
    /**
     * 
     * @param <N> the node class for the nodes in the graph.
     * @param <E> the edge class for the edges in the graph.
     * @param graphScene the GraphScene on which the layout is to be invoked.
     * @param animate if true, the layout will animate the nodes into their new
     * positions.
     * @param inverted if true, the target nodes of an edge will be poisitioned
     * in a layer higher than its source node.
     * @param xOffset the horizontal distance or gutter between the nodes.
     * @param layerOffset the vertical distance between the layers of nodes.
     * @return a GraphLayout to be invoked from the calling class.
     */
    public static <N, E> GraphLayout<N, E> createHierarchicalGraphLayout(GraphScene<N, E> graphScene, boolean animate, boolean inverted,
            int xOffset, int layerOffset) {
        return new HierarchicalLayout<>(graphScene, animate, inverted, xOffset, layerOffset);
    }
    

}
