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

package org.netbeans.spi.navigator;

import org.openide.awt.UndoRedo;

/** Description of navigation view with undo/redo support on top of basic
 * NavigatorPanel features.
 * 
 * Clients will implement this interface when they need undo and redo support
 * enabled for their Navigator view/panel.
 *
 * Implementors of this interface will be plugged into Navigator UI.
 * @see NavigatorPanel.Registration
 * 
 * @since 1.5
 *
 * @author Dafe Simonek
 */
public interface NavigatorPanelWithUndo extends NavigatorPanel {

    /** Returns instance of UndoRedo which will be propagated into 
     * Navigator TopComponent's getUndoRedo() when this panel is active.
     * 
     * It allows clients to enable undo/redo management and undo/redo actions for
     * this panel in Navigator.
     * 
     * @return Instance of UndoRedo.
     */
    public UndoRedo getUndoRedo ();
    
}
