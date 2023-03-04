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

package org.netbeans.modules.form;

import java.awt.dnd.DropTargetDragEvent;

import org.netbeans.modules.form.palette.PaletteItem;

/**
 * Interface allowing drag and drop of nodes to form module.
 *
 * @author Jan Stola, Tomas Pavek
 */
public interface NewComponentDrop {

    /**
     * Describes the primary component that should be added.
     *
     * @param dtde corresponding drop target drag event.
     * @return palette item that describes the component that should be added.
     */
    PaletteItem getPaletteItem(DropTargetDragEvent dtde);

    /**
     * Callback method that notifies about the added component. You should
     * set properties of the added component or add other beans to the model
     * in this method.
     *
     * @param componentId ID of the newly added component.
     * @param droppedOverId ID of a component the new component has been dropped over;
     * used only if the dropped component is non-visual, it is <code>null</code> otherwise.
     */
    void componentAdded(String componentId, String droppedOverId);

}
