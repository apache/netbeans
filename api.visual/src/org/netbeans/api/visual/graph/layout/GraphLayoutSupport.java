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
