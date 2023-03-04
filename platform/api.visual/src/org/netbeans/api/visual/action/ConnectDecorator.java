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

import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;

/**
 * This interface decorates a connect action.
 *
 * @author David Kaspar
 */
public interface ConnectDecorator {

    /**
     * Creates a connection widget that is temporarily used for visualization of a connection while user is dragging (creating) it.
     * @param scene the scene where the connection widget will be used
     * @return the connection widget
     */
    ConnectionWidget createConnectionWidget (Scene scene);

    /**
     * Creates a source anchor for a specified source widget. The anchor will be used at the temporary connection widget created by the createConnectionWidget method.
     * @param sourceWidget the source widget
     * @return the source anchor
     */
    Anchor createSourceAnchor (Widget sourceWidget);

    /**
     * Creates a target anchor for a specified target widget. The anchor will be used at the temporary connection widget created by the createConnectionWidget method.
     * @param targetWidget the source widget
     * @return the target anchor
     */
    Anchor createTargetAnchor (Widget targetWidget);

    /**
     * Creates a floating anchor which will be used when the connection target is not attached to any widget. The anchor will be used at the temporary connection widget created by the createConnectionWidget method.
     * @param location the scene location of the mouse cursor
     * @return the floating anchor; usually FixedAnchor
     */
    Anchor createFloatAnchor (Point location);

}
