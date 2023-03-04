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
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;

/**
 * This interface decorates a reconnect action.
 *
 * @author David Kaspar
 */
public interface ReconnectDecorator {

    /**
     * Creates an anchor for a specified replacement widget (of a connection source or target which is going to be reconnected).
     * @param replacementWidget the replacement widget
     * @return the anchor
     */
    Anchor createReplacementWidgetAnchor (Widget replacementWidget);

    /**
     * Creates a floating anchor for a specified location when there is no replacement a widget
     * @param location the scene location
     * @return the floating anchor; usually FixedAnchor
     */
    Anchor createFloatAnchor (Point location);

}
