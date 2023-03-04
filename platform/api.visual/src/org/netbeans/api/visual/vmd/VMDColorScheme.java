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
package org.netbeans.api.visual.vmd;

import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;

/**
 * This class specifies look and feel of vmd widgets. There are predefined schemes in VMDFactory class.
 *
 * @author David Kaspar
 * @since 2.5
 */
public abstract class VMDColorScheme {

    /**
     * Creates a new vmd color scheme.
     * @since 2.5
     */
    protected VMDColorScheme () {
    }

    /**
     * Called to install UI to a node widget.
     * @param widget the node widget
     * @since 2.5
     */
    public abstract void installUI (VMDNodeWidget widget);

    /**
     * Called to update UI of a node widget. Called from VMDNodeWidget.notifyStateChanged method.
     * @param widget the node widget
     * @param previousState the previous state
     * @param state the new state
     * @since 2.5
     */
    public abstract void updateUI (VMDNodeWidget widget, ObjectState previousState, ObjectState state);

    /**
     * Returns whether the node minimize button is on the right side of the node header.
     * @param widget the node widget
     * @return true, if the button is on the right side; false, if the button is on the left side
     * @since 2.5
     */
    public abstract boolean isNodeMinimizeButtonOnRight (VMDNodeWidget widget);

    /**
     * Returns an minimize-widget image for a specific node widget.
     * @param widget the node widget
     * @return the minimize-widget image
     * @since 2.5
     */
    public abstract Image getMinimizeWidgetImage (VMDNodeWidget widget);

    /**
     * Called to create a pin-category widget.
     * @param widget the node widget
     * @param categoryDisplayName the category display name
     * @return the pin-category widget
     * @since 2.5
     */
    public abstract Widget createPinCategoryWidget (VMDNodeWidget widget, String categoryDisplayName);

    /**
     * Called to install UI to a connection widget.
     * @param widget the connection widget
     * @since 2.5
     */
    public abstract void installUI (VMDConnectionWidget widget);

    /**
     * Called to update UI of a connection widget. Called from VMDConnectionWidget.notifyStateChanged method.
     * @param widget the connection widget
     * @param previousState the previous state
     * @param state the new state
     * @since 2.5
     */
    public abstract void updateUI (VMDConnectionWidget widget, ObjectState previousState, ObjectState state);

    /**
     * Called to install UI to a pin widget.
     * @param widget the pin widget
     * @since 2.5
     */
    public abstract void installUI (VMDPinWidget widget);

    /**
     * Called to update UI of a pin widget. Called from VMDPinWidget.notifyStateChanged method.
     * @param widget the pin widget
     * @param previousState the previous state
     * @param state the new state
     * @since 2.5
     */
    public abstract void updateUI (VMDPinWidget widget, ObjectState previousState, ObjectState state);

    /**
     * Returns a gap size of a node-anchor from a node-widget.
     * @param anchor the node anchor
     * @return the gap size
     * @since 2.5
     */
    public abstract int getNodeAnchorGap (VMDNodeAnchor anchor);

}
