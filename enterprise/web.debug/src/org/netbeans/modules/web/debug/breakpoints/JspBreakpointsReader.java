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

package org.netbeans.modules.web.debug.breakpoints;

import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.*;

/**
 *
 * @author Martin Grebac
 */
public class JspBreakpointsReader implements Properties.Reader {

    public String [] getSupportedClassNames () {
        return new String[] { JspLineBreakpoint.class.getName () };
    }

    public Object read (String typeID, Properties properties) {

        JspLineBreakpoint b = null;
        if (typeID.equals (JspLineBreakpoint.class.getName ())) {
            String url = properties.getString(JspLineBreakpoint.PROP_URL, null);
            // #110349 - ignore loading of breakpoints which do not have URL
            if (url == null || url.trim().length() == 0) {
                return null;
            }
            b = JspLineBreakpoint.create (
                url,
                properties.getInt(JspLineBreakpoint.PROP_LINE_NUMBER, 1)
            );
            b.setCondition(properties.getString (JspLineBreakpoint.PROP_CONDITION, ""));
            b.setPrintText(properties.getString (JspLineBreakpoint.PROP_PRINT_TEXT, ""));
            b.setGroupName(properties.getString (Breakpoint.PROP_GROUP_NAME, ""));
            b.setSuspend(properties.getInt (JspLineBreakpoint.PROP_SUSPEND, JspLineBreakpoint.SUSPEND_ALL));
            if (properties.getBoolean (JspLineBreakpoint.PROP_ENABLED, true)) {
                b.enable ();
            } else {
                b.disable ();
            }
        }

        return b;
    }
    
    public void write (Object object, Properties properties) {

        if (object instanceof JspLineBreakpoint) {
            JspLineBreakpoint b = (JspLineBreakpoint) object;
            properties.setString (JspLineBreakpoint.PROP_PRINT_TEXT, b.getPrintText ());
            properties.setString (JspLineBreakpoint.PROP_GROUP_NAME, b.getGroupName ());
            properties.setInt (JspLineBreakpoint.PROP_SUSPEND, b.getSuspend ());
            properties.setBoolean (JspLineBreakpoint.PROP_ENABLED, b.isEnabled ());        
            properties.setString (JspLineBreakpoint.PROP_URL, b.getURL ());
            properties.setInt (JspLineBreakpoint.PROP_LINE_NUMBER, b.getLineNumber ());
            properties.setString (JspLineBreakpoint.PROP_CONDITION, b.getCondition ());
        }
        return;
    }
}
