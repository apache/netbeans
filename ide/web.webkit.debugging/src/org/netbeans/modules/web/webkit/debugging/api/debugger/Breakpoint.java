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
package org.netbeans.modules.web.webkit.debugging.api.debugger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;

/**
 * Wrapper for Debugger.setBreakpointByUrl return value.
 */
public class Breakpoint extends AbstractObject {
    
    public static final String PROP_LOCATION = "location";
    
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private JSONObject location;

    Breakpoint(JSONObject object, WebKitDebugging webkit) {
        super(object, webkit);
    }
    
    public String getBreakpointID() {
        return (String)getObject().get("breakpointId");
    }

    /**
     * Get the breakpoint's location
     * @return The location or <code>null</code> when not defined.
     */
    public JSONObject getBreakpointLocation() {
        synchronized (this) {
            if (location == null) {
                JSONArray locations = (JSONArray)getObject().get("locations");
                if (!locations.isEmpty()) {
                    location = (JSONObject) locations.get(0);
                }
            }
            return location;
        }
    }
    
    /**
     * Get the breakpoint's line number
     * @return The line number, or -1 when not defined.
     */
    public long getLineNumber() {
        JSONObject location = getBreakpointLocation();
        if (location != null) {
            return (Long) location.get("lineNumber");
        } else {
            return -1l;
        }
    }
    
    /**
     * Get the breakpoint's column number
     * @return The column number, or <code>0</code> when not defined.
     */
    public long getColumnNumber() {
        JSONObject location = getBreakpointLocation();
        Long col = null;
        if (location != null) {
            col = (Long) location.get("columnNumber");
        }
        if (col == null) {
            return 0l;
        } else {
            return col;
        }
    }

    void notifyResolved(JSONObject location) {
        JSONObject oldLocation;
        synchronized (this) {
            oldLocation = this.location;
            this.location = location;
        }
        pcs.firePropertyChange(PROP_LOCATION, oldLocation, location);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(pcl);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener(pcl);
    }
    
}
