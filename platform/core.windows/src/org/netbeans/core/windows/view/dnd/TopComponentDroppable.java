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


package org.netbeans.core.windows.view.dnd;


import org.netbeans.core.windows.view.ViewElement;
import org.openide.windows.TopComponent;

import java.awt.*;


/**
 * Interface which allows container to provide support for dynamic
 * drop target indication, thus handling possible
 * drop operations for all its sub components and actually
 * provides the drop operation to the container.
 *
 * @author  Peter Zavadsky
 *
 * @see DropTargetGlassPane
 */
public interface TopComponentDroppable {
    /** Gets <code>Shape</code> object needed to used as indicator
     * of possible drop operation.
     * @param location within the container's glass pane coordinates */
    public Shape getIndicationForLocation(Point location);

    /** Gets constraint to be used for specified location
     * of possible drop operation.
     * @param location within the container's glass pane coordinates 
     * @return can return <code>null</code> if default constraints should
     *          should be used */
    public Object getConstraintForLocation(Point location);

    /** Gets actual drop component, i.e. the one which absobs the possible dropped
     * top component. Used to detect its bounds, for drop indication. */
    public Component getDropComponent();
    
    /** Gets view element into which to perform the drop operation. */
    public ViewElement getDropViewElement();
    
    // XXX
    /** Checks whether the specified given TopComponent or Mode can be dropped. */
    public boolean canDrop(TopComponentDraggable transfer, Point location);
    
    // XXX
    /** Checks whether this droppable supports kind of winsys transfer.
     * Either <code>Constants.MODE_KIND_EDITOR</code> or <code>Constants.MODE_KIND_VIEW or both. */
    public boolean supportsKind(TopComponentDraggable transfer);
    
    /**
     * @return Mode kind of the originating mode when just a single TopComponent
     * is being dragged or the kind of the mode that is being dragged.
     */
    public int getKind();
}
