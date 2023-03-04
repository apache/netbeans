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
 * This interface controls a select action.
 *
 * @author David Kaspar
 */
public interface SelectProvider {

    /**
     * Called to check whether aiming is allowed
     * @param widget the aimed widget
     * @param localLocation the local location of a mouse cursor while aiming is invoked by an user
     * @param invertSelection if true, then the invert selection is invoked by an user.
     * @return true, if aiming is allowed and widget is set to aimed state while mouse button is pressed;
     *         false, if aiming is disallowed and widget is not set to aimed state at any time.
     */
    boolean isAimingAllowed (Widget widget, Point localLocation, boolean invertSelection);

    /**
     * Called to check whether the selection is allowed.
     * @param widget the selected widget
     * @param localLocation the local location of a mouse cursor while selection is invoked by an user
     * @param invertSelection if true, then the invert selection is invoked by an user.
     * @return true, if selection is allowed; false, if selection is disallowed
     */
    boolean isSelectionAllowed (Widget widget, Point localLocation, boolean invertSelection);

    /**
     * Called to perform the selection.
     * @param widget the selected widget
     * @param localLocation the local location of a mouse cursor while selection is invoked by an user
     * @param invertSelection if true, then the invert selection is invoked by an user.
     */
    void select (Widget widget, Point localLocation, boolean invertSelection);

}
