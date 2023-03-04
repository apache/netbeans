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


package org.netbeans.core.windows;

import org.netbeans.core.windows.view.View;

/**
 * Class which describes one type of change (in model) which is sent
 * <code>ViewRequestor</code> from <code>Central</code>.
 *
 * @author  Peter Zavadsky
 */
final class ViewRequest {

    /** To distinguish between individual mode or top components. */
    public final Object source;

    public final int type;

    public final Object oldValue;

    public final Object newValue;


    /** Creates a new instance of ChangeInfo */
    public ViewRequest(Object source, int type, Object oldValue, Object newValue) {
        this.source   = source;
        this.type     = type;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
    
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append ("ViewRequest@");
        result.append (System.identityHashCode(this));
        result.append (" [TYPE=");
        String tp;
        switch (type) {
            case View.CHANGE_ACTIVE_MODE_CHANGED :
                tp = "CHANGE_ACTIVE_MODE_CHANGED"; //NOI18N
                break;
            case View.CHANGE_EDITOR_AREA_BOUNDS_CHANGED :
                tp = "CHANGE_EDITOR_AREA_BOUNDS_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_EDITOR_AREA_CONSTRAINTS_CHANGED :
                tp = "CHANGE_EDITOR_AREA_CONSTRAINTS_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_EDITOR_AREA_STATE_CHANGED :
                tp = "CHANGE_EDITOR_AREA_STATE_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_EDITOR_AREA_FRAME_STATE_CHANGED :
                tp = "CHANGE_EDITOR_AREA_FRAME_STATE_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_MAIN_WINDOW_BOUNDS_JOINED_CHANGED :
                tp = "CHANGE_MAIN_WINDOW_BOUNDS_JOINED_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_MAIN_WINDOW_BOUNDS_SEPARATED_CHANGED :
                tp = "CHANGE_MAIN_WINDOW_BOUNDS_SEPARATED_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_MAIN_WINDOW_FRAME_STATE_JOINED_CHANGED :
                tp = "CHANGE_MAIN_WINDOW_FRAME_STATE_JOINED_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_MAIN_WINDOW_FRAME_STATE_SEPARATED_CHANGED :
                tp = "CHANGE_MAIN_WINDOW_FRAME_STATE_SEPARATED_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_MAXIMIZED_MODE_CHANGED :
                tp = "CHANGE_MAXIMIZED_MODE_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_MODE_SELECTED_TOPCOMPONENT_CHANGED :
                tp = "CHANGE_MODE_SELECTED_TOPCOMPONENT_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_MODE_BOUNDS_CHANGED :
                tp = "CHANGE_MODE_BOUNDS_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_MODE_CONSTRAINTS_CHANGED :
                tp = "CHANGE_MODE_CONSTRAINTS_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_MODE_FRAME_STATE_CHANGED :
                tp = "CHANGE_MODE_FRAME_STATE_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_TOOLBAR_CONFIGURATION_CHANGED :
                tp = "CHANGE_TOOLBAR_CONFIGURATION_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_TOPCOMPONENT_ICON_CHANGED :
                tp = "CHANGE_TOPCOMPONENT_ICON_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_TOPCOMPONENT_DISPLAY_NAME_CHANGED :
                tp = "CHANGE_TOPCOMPONENT_DISPLAY_NAME_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_TOPCOMPONENT_DISPLAY_NAME_ANNOTATION_CHANGED :
                tp = "CHANGE_TOPCOMPONENT_DISPLAY_NAME_ANNOTATION_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_TOPCOMPONENT_TOOLTIP_CHANGED :
                tp = "CHANGE_TOPCOMPONENT_TOOLTIP_CHANGED"; //NOI18N
                break;
            case  View.CHANGE_TOPCOMPONENT_ACTIVATED :
                tp = "CHANGE_TOPCOMPONENT_ACTIVATED"; //NOI18N
                break;
            case  View.CHANGE_DND_PERFORMED :
                tp = "CHANGE_DND_PERFORMED"; //NOI18N
                break;
            case  View.CHANGE_UI_UPDATE :
                tp = "CHANGE_UI_UPDATE"; //NOI18N
                break;
            case  View.TOPCOMPONENT_REQUEST_ATTENTION :
                tp = "TOPCOMPONENT_REQUEST_ATTENTION"; //NOI18N
                break;
            case  View.TOPCOMPONENT_CANCEL_REQUEST_ATTENTION :
                tp = "TOPCOMPONENT_CANCEL_REQUEST_ATTENTION"; //NOI18N
                break;
            case  View.TOPCOMPONENT_ATTENTION_HIGHLIGHT_ON :
                tp = "TOPCOMPONENT_ATTENTION_HIGHLIGHT_ON"; //NOI18N
                break;
            case  View.TOPCOMPONENT_ATTENTION_HIGHLIGHT_OFF :
                tp = "TOPCOMPONENT_ATTENTION_HIGHLIGHT_OFF"; //NOI18N
                break;
            default :
                tp = "UNKNOWN";
                break;
        }
        result.append (tp).append ("]  [oldValue:").append(oldValue)
                .append("] [newValue:").append(newValue)
                .append("] [source:").append(source).append(']');
        return result.toString();
    }
        
}

