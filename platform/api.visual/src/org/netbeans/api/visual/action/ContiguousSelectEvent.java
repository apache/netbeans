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
package org.netbeans.api.visual.action;

import org.netbeans.api.visual.widget.Widget;

import java.awt.*;

/**
 * Represents an event for ContiguousSelectEvent passed to ContiguousSelectProvider. Contains information about selection-type, previously and currently choosen objects spots.
 *
 * @author David Kaspar
 * @since 2.17
 */
public final class ContiguousSelectEvent {

    private final Widget previouslyChoosenWidget;
    private final Point previouslyChoosenLocalLocation;

    private final Widget choosenWidget;
    private final Point choosenLocalLocation;

    private final SelectionType selectionType;

    private ContiguousSelectEvent (Widget previouslyChoosenWidget, Point previouslyChoosenLocalLocation, Widget choosenWidget, Point choosenLocalLocation, SelectionType selectionType) {
        this.previouslyChoosenWidget = previouslyChoosenWidget;
        this.previouslyChoosenLocalLocation = previouslyChoosenLocalLocation;
        this.choosenWidget = choosenWidget;
        this.choosenLocalLocation = choosenLocalLocation;
        this.selectionType = selectionType;
    }

    /**
     * Returns a previously choosen widget.
     * @return the previously choosen widget
     */
    public Widget getPreviouslyChoosenWidget () {
        return previouslyChoosenWidget;
    }

    /**
     * Returns a local location of a previously choosen widget.
     * @return the local location of the previously choosen widget
     */
    public Point getPreviouslyChoosenLocalLocation () {
        return previouslyChoosenLocalLocation != null ? new Point (previouslyChoosenLocalLocation) : null;
    }

    /**
     * Returns a choosen widget.
     * @return the choosen widget
     */
    public Widget getChoosenWidget () {
        return choosenWidget;
    }

    /**
     * Returns a local location of a choosen widget.
     * @return the local location of the choosen widget
     */
    public Point getChoosenLocalLocation () {
        return choosenLocalLocation != null ?  new Point (choosenLocalLocation) : null;
    }

    /**
     * Represents a selection type.
     * @return the selection type
     */
    public SelectionType getSelectionType () {
        return selectionType;
    }

    /**
     * Creates an event. Meant to be used by the library only.
     * @param previousWidget the previously choosen widget
     * @param previousLocalLocation the local location of the previously choosen widget
     * @param choosenWidget the choosen widget
     * @param choosenLocalLocation the local location of the currently choosen widget
     * @param selectionType the selection type invoked by an user
     * @return the contiguous select event
     */
    public static ContiguousSelectEvent create (Widget previousWidget, Point previousLocalLocation, Widget choosenWidget, Point choosenLocalLocation, SelectionType selectionType) {
        assert selectionType != null;
        return new ContiguousSelectEvent (previousWidget, previousLocalLocation, choosenWidget, choosenLocalLocation, selectionType);
    }

    /**
     * Defines a type of a selection.
     */
    public enum SelectionType {

        /**
         * Represents a normal selection that replace the previous selection.
         * Usually invokes without any key-modifier.
         */
        REPLACE_NON_CONTIGUOUS,

        /**
         * Represents a normal selection that replace the previous selection.
         * All objects that are between previously and current choosen spots defines the current selection.
         * Usually invokes with Shift key-modifier.
         */
        REPLACE_CONTIGUOUS,

        /**
         * Represents an additive selection where the new selection should be added to the current selection.
         * Usually invokes with Ctrl key-modifier.
         */
        ADDITIVE_NON_CONTIGUOUS,

        /**
         * Represents an additive selection where the new selection should be added to the current selection.
         * All objects that are between previously and current choosen spots defines the current selection.
         * Usually invokes with Ctrl and Shift key-modifiers.
         */
        ADDITIVE_CONTIGUOUS,

    }

}
