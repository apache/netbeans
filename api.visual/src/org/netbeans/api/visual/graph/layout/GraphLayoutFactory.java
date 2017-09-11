/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        return new TreeGraphLayout<N, E>(originX, originY, verticalGap, horizontalGap, vertical, minimizeGap, alignment);
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
        return new OrthogonalLayout(graphScene, animate);
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
        return new HierarchicalLayout(graphScene, animate);
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
        return new HierarchicalLayout(graphScene, animate, inverted);
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
        return new HierarchicalLayout(graphScene, animate, inverted, xOffset, layerOffset);
    }
    

}
