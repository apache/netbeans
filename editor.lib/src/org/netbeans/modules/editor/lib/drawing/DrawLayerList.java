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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.editor.lib.drawing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;
import org.netbeans.editor.EditorDebug;

/** Draw layer list stores multiple draw-layers sorted
* according to their visibility which is the integer giving the z-order
* in which the layers are sorted. It also provides an iterator
* to go through the draw layer members.
*
* @author Miloslav Metelka
* @version 1.00
*/


public final class DrawLayerList {

    private static final Logger LOG = Logger.getLogger(DrawLayerList.class.getName());

    private static final DrawLayer[] EMPTY = new DrawLayer[0];

    private DrawLayer[] layers = EMPTY;

    private final ArrayList visibilityList = new ArrayList();

    public DrawLayerList() {
        // no-op
    }

    /** Add the new layer to the list depending on visibility.
    * @param layer layer to add to the layer list
    * @return true when new layer was added false otherwise. The layer
    *   is not added if there is already a layer with the same name.
    *   There can be a layer with the same visibility like the layer
    *   being added.
    */
    synchronized boolean add(DrawLayer layer, int visibility) {
        if (indexOf(layer.getName()) >= 0) { // already layer with that name
            return false;
        }

        int indAdd = layers.length;
        for (int i = 0; i < layers.length; i++) {
            if (((Integer)visibilityList.get(i)).intValue() > visibility) {
                indAdd = i;
                break;
            }
        }

        ArrayList l = new ArrayList(Arrays.asList(layers));
        l.add(indAdd, layer);
        layers = new DrawLayer[layers.length + 1];
        l.toArray(layers);

        visibilityList.add(indAdd, new Integer(visibility));

        return true;
    }

    synchronized void add(DrawLayerList l) {
        DrawLayer[] lta = l.layers;
        for (int i = 0; i < lta.length; i++) {
            add(lta[i], ((Integer)l.visibilityList.get(i)).intValue());
        }
    }

    /** Remove layer specified by layerName from layer list.
    * @param layer layer to remove from the layer list
    */
    synchronized DrawLayer remove(String layerName) {
        int ind = indexOf(layerName);
        DrawLayer removed = null;

        if (ind >= 0) {
            removed = layers[ind];
            ArrayList l = new ArrayList(Arrays.asList(layers));
            l.remove(ind);
            layers = new DrawLayer[layers.length - 1];
            l.toArray(layers);

            visibilityList.remove(ind);
        }

        return removed;
    }

    synchronized void remove(DrawLayerList l) {
        DrawLayer[] lta = l.layers;
        for (int i = 0; i < lta.length; i++) {
            remove(lta[i].getName());
        }
    }

    synchronized DrawLayer findLayer(String layerName) {
        int ind = indexOf(layerName);
        return (ind >= 0) ? layers[ind] : null;
    }

    /** Get the snapshot of the current layers. This is useful
    * for drawing process that would otherwise have to hold
    * a lock on editorUI so that no layer would be added or removed
    * during the drawing.
    */
    synchronized DrawLayer[] currentLayers() {
        return (DrawLayer[])layers.clone();
    }

    private int indexOf(String layerName) {
        for (int i = 0; i < layers.length; i++) {
            if (layerName.equals(layers[i].getName())) {
                return i;
            }
        }
        return -1;
    }

    public @Override String toString() {
        switch (layers.length) {
        case 0:
            return "No layers"; // NOI18N
        case 1:
            return "Standalone " + layers[0]; // NOI18N
        default:
            return "Layers:\n" + EditorDebug.debugArray(layers); // NOI18N
        }
    }

}
