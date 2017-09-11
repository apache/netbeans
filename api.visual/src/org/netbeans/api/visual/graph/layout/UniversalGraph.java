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

import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.graph.GraphPinScene;

import java.util.Collection;
import java.util.ArrayList;

/**
 * This interface represents an universal graph.
 * It unifies a view on a GraphScene and GraphPinScene as a nodes-edges only graph.
 * This is commonly used by graph-oriented layout algorithms.
 *
 * @author David Kaspar
 */
public abstract class UniversalGraph<N,E> {

    /**
     * Returns a related scene as a ObjectScene.
     * @return the related scene which is represented by the universal graph.
     */
    public abstract ObjectScene getScene ();

    /**
     * Returns a collection of nodes in the graph.
     * @return the collection of nodes
     */
    public abstract Collection<N> getNodes ();

    /**
     * Returns a collection of edges in the graph.
     * @return the collection of edges
     */
    public abstract Collection<E> getEdges ();

    /**
     * Returns edges that are attached to a specified node.
     * @param node the node
     * @param allowsOutputEdges if true then it finds all edges that has the node as their source
     * @param allowsInputEdges if true then it finds all edges that has the node as their target
     * @return the collection of all found edges
     */
    public abstract Collection<E> findNodeEdges (N node, boolean allowsOutputEdges, boolean allowsInputEdges);

    /**
     * Returns an edge source.
     * @param edge the edge
     * @return the edge source
     */
    public abstract N getEdgeSource (E edge);

    /**
     * Returns an edge target.
     * @param edge the edge
     * @return the edge target
     */
    public abstract N getEdgeTarget (E edge);

    
    static <N,E> UniversalGraph<N, E> createUniversalGraph (final GraphScene<N, E> scene) {
        return new UniversalGraph<N, E>() {

            public ObjectScene getScene () {
                return scene;
            }

            public Collection<N> getNodes () {
                return scene.getNodes ();
            }

            public Collection<E> getEdges () {
                return scene.getEdges ();
            }

            public Collection<E> findNodeEdges (N node, boolean allowsOutputEdges, boolean allowsInputEdges) {
                return scene.findNodeEdges (node, allowsOutputEdges, allowsInputEdges);
            }

            public N getEdgeSource (E edge) {
                return scene.getEdgeSource (edge);
            }

            public N getEdgeTarget (E edge) {
                return scene.getEdgeTarget (edge);
            }

        };
    }

    static <N,E,P> UniversalGraph<N, E> createUniversalGraph (final GraphPinScene<N, E, P> scene) {
        return new UniversalGraph<N, E>() {

            public ObjectScene getScene () {
                return scene;
            }

            public Collection<N> getNodes () {
                return scene.getNodes ();
            }

            public Collection<E> getEdges () {
                return scene.getEdges ();
            }

            public Collection<E> findNodeEdges (N node, boolean allowsOutputEdges, boolean allowsInputEdges) {
                ArrayList<E> list = new ArrayList<E> ();
                for (P pin : scene.getNodePins (node))
                    list.addAll (scene.findPinEdges (pin, allowsOutputEdges, allowsInputEdges));
                return list;
            }

            public N getEdgeSource (E edge) {
                return scene.getPinNode (scene.getEdgeSource (edge));
            }

            public N getEdgeTarget (E edge) {
                return scene.getPinNode (scene.getEdgeTarget (edge));
            }

        };
    }

}

