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


package org.netbeans.core.windows.view;


import org.netbeans.core.windows.WindowSystemSnapshot;
import java.awt.*;
import org.netbeans.core.windows.view.dnd.TopComponentDraggable;
import org.openide.windows.TopComponent;


/**
 * Represents view part of window system, define types of changes which are relevant
 * for GUI, and method {@link #changeGUI} which implemenation does all the
 * view's task.
 *
 * @author  Peter Zavadsky
 */
public interface View {

    // Global (the highest) level changes.
    public int CHANGE_VISIBILITY_CHANGED                        = 0;
    public int CHANGE_MAIN_WINDOW_BOUNDS_JOINED_CHANGED         = 1;
    public int CHANGE_MAIN_WINDOW_BOUNDS_SEPARATED_CHANGED      = 2;
    public int CHANGE_MAIN_WINDOW_FRAME_STATE_JOINED_CHANGED    = 3;
    public int CHANGE_MAIN_WINDOW_FRAME_STATE_SEPARATED_CHANGED = 4;
    public int CHANGE_EDITOR_AREA_STATE_CHANGED                 = 5;
    public int CHANGE_EDITOR_AREA_FRAME_STATE_CHANGED           = 6;
    public int CHANGE_EDITOR_AREA_BOUNDS_CHANGED                = 7;
    public int CHANGE_EDITOR_AREA_CONSTRAINTS_CHANGED           = 8;
    public int CHANGE_ACTIVE_MODE_CHANGED                       = 9;
    public int CHANGE_TOOLBAR_CONFIGURATION_CHANGED             = 10;
    public int CHANGE_MAXIMIZED_MODE_CHANGED                    = 11;
    public int CHANGE_MODE_ADDED                                = 12;
    public int CHANGE_MODE_REMOVED                              = 13;
    public int CHANGE_MODE_CONSTRAINTS_CHANGED                  = 14;

    
    // Mode level changes
    public int CHANGE_MODE_BOUNDS_CHANGED                = 20;
    public int CHANGE_MODE_FRAME_STATE_CHANGED           = 21;
    public int CHANGE_MODE_SELECTED_TOPCOMPONENT_CHANGED = 22;
    public int CHANGE_MODE_TOPCOMPONENT_ADDED            = 23;
    public int CHANGE_MODE_TOPCOMPONENT_REMOVED          = 24;
    
    // TopComponent level changes
    public int CHANGE_TOPCOMPONENT_DISPLAY_NAME_CHANGED            = 31;
    public int CHANGE_TOPCOMPONENT_DISPLAY_NAME_ANNOTATION_CHANGED = 32;
    public int CHANGE_TOPCOMPONENT_TOOLTIP_CHANGED                 = 33;
    public int CHANGE_TOPCOMPONENT_ICON_CHANGED                    = 34;

    // Compound changes
    public int CHANGE_TOPCOMPONENT_ATTACHED            = 41;
    public int CHANGE_TOPCOMPONENT_ARRAY_ADDED         = 42;
    public int CHANGE_TOPCOMPONENT_ARRAY_REMOVED       = 43;
    public int CHANGE_TOPCOMPONENT_ACTIVATED           = 44;
    public int CHANGE_MODE_CLOSED                      = 45;
    public int CHANGE_DND_PERFORMED                    = 46;
    public int CHANGE_TOPCOMPONENT_AUTO_HIDE_ENABLED   = 47;
    public int CHANGE_TOPCOMPONENT_AUTO_HIDE_DISABLED  = 48;
    
    // Others
    public int CHANGE_UI_UPDATE    = 61;
    
    public int TOPCOMPONENT_REQUEST_ATTENTION = 63;
    public int TOPCOMPONENT_CANCEL_REQUEST_ATTENTION = 64;
    /**
     * @since 2.54
     */
    public int TOPCOMPONENT_ATTENTION_HIGHLIGHT_ON = 65;
    /**
     * @since 2.54
     */
    public int TOPCOMPONENT_ATTENTION_HIGHLIGHT_OFF = 66;
    public int CHANGE_MAXIMIZE_TOPCOMPONENT_SLIDE_IN = 67;

    //toggle TopComponent busy
    public int TOPCOMPONENT_SHOW_BUSY = 70;
    public int TOPCOMPONENT_HIDE_BUSY = 71;
    
    /** Provides GUI changes to manifest model changes to user. */
    public void changeGUI(ViewEvent[] viewEvents, WindowSystemSnapshot snapshot);
    
    // XXX
    public boolean isDragInProgress();
    // XXX
    public Frame getMainWindow();
    
    public Component getEditorAreaComponent();
    
    public String guessSlideSide(TopComponent tc);

    public void userStartedKeyboardDragAndDrop( TopComponentDraggable draggable );
}

