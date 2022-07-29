/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import java.awt.datatransfer.Transferable;

/**
 * This interface controls an accept (drag &amp; drop) action.
 *
 * @author David Kaspar
 */
public interface AcceptProvider {

    /**
     * Checks whether a transferable can be dropped on a widget at a specific point.
     * @param widget the widget could be dropped
     * @param point the drop location in local coordination system of the widget
     * @param transferable the transferable
     * @return the state
     */
    ConnectorState isAcceptable (Widget widget, Point point, Transferable transferable);

    /**
     * Handles the drop of a transferable.
     * @param widget the widget where the transferable is dropped
     * @param point the drop location in local coordination system of the widget
     * @param transferable the transferable
     */
    void accept (Widget widget, Point point, Transferable transferable);

}
