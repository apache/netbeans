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

        visibilityList.add(indAdd, Integer.valueOf(visibility));

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
