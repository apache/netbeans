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
package org.netbeans.modules.visual.graph.layout.orthogonalsupport;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.Face.Dart;

/**
 *
 * @author ptliu
 */
public class EmbeddedPlanarGraph {

    private MGraph originalGraph;
    private ArrayList<Face> faces;

    /**
     * 
     * @param graph
     * @return
     */
    public static EmbeddedPlanarGraph createGraph(MGraph graph) {
        return new EmbeddedPlanarGraph(graph);
    }

    /**
     * 
     * @param graph
     */
    private EmbeddedPlanarGraph(MGraph graph) {
        this.originalGraph = graph;
        faces = new ArrayList<Face>();
    }

    /**
     * 
     * @return
     */
    public MGraph getOriginalGraph() {
        return originalGraph;
    }

    /**
     * 
     * @return
     */
    public ArrayList<Face> getFaces() {
        return faces;
    }

    /**
     * 
     * @param newFace
     */
    public void addFace(Face newFace) {
        if (!faces.contains(newFace)) {
            faces.add(newFace);
        }
    }

    /**
     * 
     * @param newFaces
     */
    public void addFaces(Collection<Face> newFaces) {
        faces.addAll(newFaces);
    }

    /**
     * 
     * @param faceToRemove
     */
    public void removeFace(Face faceToRemove) {
        faces.remove(faceToRemove);
    }

    /**
     * 
     * @param facesToRemove
     */
    public void removeFaces(Collection<Face> facesToRemove) {
        faces.removeAll(facesToRemove);
    }

    /**
     * 
     * @param face
     * @param dart
     * @return
     */
    public Face getOppositeFace(Face face, Dart dart) {
        for (Face f : faces) {
            if (f != face) {
                if (f.containsEdge(dart.getEdge())) {
                    return f;
                }
            }
        }

        return null;
    }

    /**
     * 
     * @return
     */
    public Face getOuterFace() {
        for (Face face : faces) {
            if (face.isOuterFace()) {
                return face;
            }
        }

        return null;
    }
}

