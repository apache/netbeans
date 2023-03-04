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

package org.openide.windows;

import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;

/**
 * When an implementation of this class is available in the global Lookup and
 * an object is being dragged over some parts of the main window of the IDE then
 * the window system may call methods of this class to decide whether it can
 * accept or reject the drag operation. And when the object is actually dropped
 * into the IDE then this class will be asked to handle the drop.
 *
 * @since 6.7
 *
 * @author S. Aubrecht
 */
public abstract class ExternalDropHandler {

    /**
     * @return True if the dragged object can be dropped into the IDE, false
     * if the DataFlavor(s) are not supported.
     */
    public abstract boolean canDrop( DropTargetDragEvent e );

    /**
     * This method is called when the dragged object is already dropped to decide
     * whether the drop can be accepted.
     *
     * @return True if the dropped object is supported (i.e. handleDrop method
     * can process the object), false otherwise.
     */
    public abstract boolean canDrop( DropTargetDropEvent e );

    /**
     * When an object is dropped into the IDE this method must process it (e.g.
     * open the dropped file in a new editor tab).
     *
     * @return True if the dropped object was processed successfully, false otherwise.
     */
    public abstract boolean handleDrop( DropTargetDropEvent e );
}
