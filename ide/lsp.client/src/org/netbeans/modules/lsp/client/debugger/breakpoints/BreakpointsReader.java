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

package org.netbeans.modules.lsp.client.debugger.breakpoints;

import java.net.MalformedURLException;
import java.net.URL;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.Properties;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

@DebuggerServiceRegistration(types={Properties.Reader.class})
public final class BreakpointsReader implements Properties.Reader {

    @Override
    public String [] getSupportedClassNames () {
        return new String[] {
            DAPLineBreakpoint.class.getName (),
        };
    }

    @Override
    public Object read (String typeID, Properties properties) {
        if (!(typeID.equals (DAPLineBreakpoint.class.getName ())))
            return null;

        DAPLineBreakpoint b;
        int lineNumber = properties.getInt("lineNumber", 0) + 1;
        String url = properties.getString ("url", null);
        FileObject fo;
        try {
            fo = URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException ex) {
            fo = null;
        }
        if (fo == null) {
            // The user file is gone
            return null;
        }
        b = DAPLineBreakpoint.create(fo, lineNumber);
        b.setGroupName(
            properties.getString (Breakpoint.PROP_GROUP_NAME, "")
        );
        int hitCountFilter = properties.getInt(Breakpoint.PROP_HIT_COUNT_FILTER, 0);
        Breakpoint.HIT_COUNT_FILTERING_STYLE hitCountFilteringStyle;
        if (hitCountFilter > 0) {
            hitCountFilteringStyle = Breakpoint.HIT_COUNT_FILTERING_STYLE.values()
                    [properties.getInt(Breakpoint.PROP_HIT_COUNT_FILTER+"_style", 0)]; // NOI18N
        } else {
            hitCountFilteringStyle = null;
        }
        b.setHitCountFilter(hitCountFilter, hitCountFilteringStyle);
        String condition = properties.getString(DAPLineBreakpoint.PROP_CONDITION, null);
        if (condition != null && !condition.isEmpty()) {
            b.setCondition(condition);
        }
        if (properties.getBoolean (Breakpoint.PROP_ENABLED, true))
            b.enable ();
        else
            b.disable ();
        return b;
    }

    @Override
    public void write (Object object, Properties properties) {
        DAPLineBreakpoint b = (DAPLineBreakpoint) object;
        FileObject fo = b.getFileObject();
        properties.setString("url", fo.toURL().toString());
        properties.setInt (
            "lineNumber",
            b.getLineNumber() - 1
        );
        properties.setString (
            Breakpoint.PROP_GROUP_NAME,
            b.getGroupName ()
        );
        properties.setBoolean (Breakpoint.PROP_ENABLED, b.isEnabled ());
        properties.setInt(Breakpoint.PROP_HIT_COUNT_FILTER, b.getHitCountFilter());
        Breakpoint.HIT_COUNT_FILTERING_STYLE style = b.getHitCountFilteringStyle();
        properties.setInt(Breakpoint.PROP_HIT_COUNT_FILTER+"_style", style != null ? style.ordinal() : 0); // NOI18N
        String condition = b.getCondition();
        if (condition == null) {
            condition = "";
        }
        properties.setString(DAPLineBreakpoint.PROP_CONDITION, condition);
    }
}
