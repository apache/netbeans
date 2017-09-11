/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
