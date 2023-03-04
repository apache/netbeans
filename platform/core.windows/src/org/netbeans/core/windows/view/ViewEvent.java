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


/**
 * Class which describes request sent to <code>View</code> from <code>Central</code>
 * to process GUI update accordingly to it.
 *
 * @author  Peter Zavadsky
 */
public class ViewEvent {

    /** To distinguish between individual mode or top components. */
    private final Object source;

    private final int type;

    private final Object oldValue;

    private final Object newValue;


    /** Creates a new instance of ChangeInfo */
    public ViewEvent(Object source, int type, Object oldValue, Object newValue) {
        this.source   = source;
        this.type     = type;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public Object getSource() {
        return source;
    }
    
    public int getType() {
        return type;
    }
    
    public Object getOldValue() {
        return oldValue;
    }
    
    public Object getNewValue() {
        return newValue;
    }
    
    /**
     * overriden for debugging reasons..
     */
    public String toString() {
        StringBuffer buf = new StringBuffer(100);
        buf.append("ViewEvent:"); //NOI18N
        String typeStr = "Unknown"; //NOI18N
        switch (type) {
            case View.CHANGE_ACTIVE_MODE_CHANGED : typeStr = "CHANGE_ACTIVE_MODE_CHANGED"; break; //NOI18N
            case View.CHANGE_DND_PERFORMED : typeStr = "CHANGE_DND_PERFORMED"; break; //NOI18N
            case View.CHANGE_EDITOR_AREA_BOUNDS_CHANGED : typeStr = "CHANGE_EDITOR_AREA_BOUNDS_CHANGED"; break; //NOI18N
            case View.CHANGE_EDITOR_AREA_CONSTRAINTS_CHANGED : typeStr = ".CHANGE_EDITOR_AREA_CONSTRAINTS_CHANGED"; break; //NOI18N
            case View.CHANGE_EDITOR_AREA_FRAME_STATE_CHANGED : typeStr = "CHANGE_EDITOR_AREA_FRAME_STATE_CHANGED"; break; //NOI18N
            case View.CHANGE_EDITOR_AREA_STATE_CHANGED : typeStr = "CHANGE_EDITOR_AREA_STATE_CHANGED"; break; //NOI18N
            case View.CHANGE_MAIN_WINDOW_BOUNDS_JOINED_CHANGED : typeStr = "CHANGE_MAIN_WINDOW_BOUNDS_JOINED_CHANGED"; break; //NOI18N
            case View.CHANGE_MAIN_WINDOW_BOUNDS_SEPARATED_CHANGED : typeStr = "CHANGE_MAIN_WINDOW_BOUNDS_SEPARATED_CHANGED"; break; //NOI18N
            case View.CHANGE_MAIN_WINDOW_FRAME_STATE_JOINED_CHANGED : typeStr = "CHANGE_MAIN_WINDOW_FRAME_STATE_JOINED_CHANGED"; break; //NOI18N
            case View.CHANGE_MAIN_WINDOW_FRAME_STATE_SEPARATED_CHANGED : typeStr = "CHANGE_MAIN_WINDOW_FRAME_STATE_SEPARATED_CHANGED"; break; //NOI18N
            case View.CHANGE_MAXIMIZED_MODE_CHANGED : typeStr = "CHANGE_MAXIMIZED_MODE_CHANGED"; break; //NOI18N
            case View.CHANGE_MODE_ADDED : typeStr = "CHANGE_MODE_ADDED"; break; //NOI18N
            case View.CHANGE_MODE_CLOSED : typeStr = "CHANGE_MODE_CLOSED"; break; //NOI18N
            case View.CHANGE_MODE_BOUNDS_CHANGED : typeStr = "CHANGE_MODE_BOUNDS_CHANGED"; break; //NOI18N
            case View.CHANGE_MODE_CONSTRAINTS_CHANGED : typeStr = "CHANGE_MODE_CONSTRAINTS_CHANGED"; break; //NOI18N
            case View.CHANGE_MODE_FRAME_STATE_CHANGED : typeStr = "CHANGE_MODE_FRAME_STATE_CHANGED"; break; //NOI18N
            case View.CHANGE_MODE_REMOVED : typeStr = "CHANGE_MODE_REMOVED"; break; //NOI18N
            case View.CHANGE_MODE_SELECTED_TOPCOMPONENT_CHANGED : typeStr = "CHANGE_MODE_SELECTED_TOPCOMPONENT_CHANGED"; break; //NOI18N
            case View.CHANGE_MODE_TOPCOMPONENT_ADDED : typeStr = "CHANGE_MODE_TOPCOMPONENT_ADDED"; break; //NOI18N
            case View.CHANGE_MODE_TOPCOMPONENT_REMOVED : typeStr = "CHANGE_MODE_TOPCOMPONENT_REMOVED"; break; //NOI18N
            case View.CHANGE_TOOLBAR_CONFIGURATION_CHANGED : typeStr = "CHANGE_TOOLBAR_CONFIGURATION_CHANGED"; break; //NOI18N
            case View.CHANGE_TOPCOMPONENT_ACTIVATED : typeStr = "CHANGE_TOPCOMPONENT_ACTIVATED"; break; //NOI18N
            case View.CHANGE_TOPCOMPONENT_ARRAY_ADDED : typeStr = "CHANGE_TOPCOMPONENT_ARRAY_ADDED"; break; //NOI18N
            case View.CHANGE_TOPCOMPONENT_ARRAY_REMOVED : typeStr = "CHANGE_TOPCOMPONENT_ARRAY_REMOVED"; break; //NOI18N
            case View.CHANGE_TOPCOMPONENT_ATTACHED : typeStr = "CHANGE_TOPCOMPONENT_ATTACHED"; break; //NOI18N
            case View.CHANGE_TOPCOMPONENT_DISPLAY_NAME_ANNOTATION_CHANGED : typeStr = "CHANGE_TOPCOMPONENT_DISPLAY_NAME_ANNOTATION_CHANGED"; break; //NOI18N
            case View.CHANGE_TOPCOMPONENT_DISPLAY_NAME_CHANGED : typeStr = "CHANGE_TOPCOMPONENT_DISPLAY_NAME_CHANGED"; break; //NOI18N
            case View.CHANGE_TOPCOMPONENT_ICON_CHANGED : typeStr = "CHANGE_TOPCOMPONENT_ICON_CHANGED"; break; //NOI18N
            case View.CHANGE_TOPCOMPONENT_TOOLTIP_CHANGED : typeStr = "CHANGE_TOPCOMPONENT_TOOLTIP_CHANGED"; break; //NOI18N
            case View.CHANGE_UI_UPDATE : typeStr = "CHANGE_UI_UPDATE"; break; //NOI18N
            case View.CHANGE_VISIBILITY_CHANGED : typeStr = "CHANGE_VISIBILITY_CHANGED"; break; //NOI18N
            case View.TOPCOMPONENT_REQUEST_ATTENTION : typeStr = "TOPCOMPONENT_REQUEST_ATTENTION"; break; //NOI18N
            case View.TOPCOMPONENT_CANCEL_REQUEST_ATTENTION : typeStr = "TOPCOMPONENT_CANCEL_REQUEST_ATTENTION"; break; //NOI18N
            case View.TOPCOMPONENT_ATTENTION_HIGHLIGHT_OFF : typeStr = "TOPCOMPONENT_ATTENTION_HIGHLIGHT_OFF"; break; //NOI18N
            case View.TOPCOMPONENT_ATTENTION_HIGHLIGHT_ON : typeStr = "TOPCOMPONENT_ATTENTION_HIGHLIGHT_ON"; break; //NOI18N
            case View.TOPCOMPONENT_SHOW_BUSY : typeStr = "TOPCOMPONENT_SHOW_BUSY"; break; //NOI18N
            case View.TOPCOMPONENT_HIDE_BUSY : typeStr = "TOPCOMPONENT_HIDE_BUSY"; break; //NOI18N
        }
        buf.append(typeStr);
        buf.append("\nnewValue="); //NOI18N
        buf.append(newValue);
        buf.append("\noldValue="); //NOI18N
        buf.append(oldValue);
        return buf.toString();
    }
    
}

