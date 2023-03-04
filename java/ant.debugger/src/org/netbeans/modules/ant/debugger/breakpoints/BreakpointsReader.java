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

package org.netbeans.modules.ant.debugger.breakpoints;

import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.Properties;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;


/**
 *
 * @author Jan Jancura
 */
public class BreakpointsReader implements Properties.Reader {
    
    
    @Override
    public String [] getSupportedClassNames () {
        return new String[] {
            AntBreakpoint.class.getName (), 
        };
    }
    
    @Override
    public Object read (String typeID, Properties properties) {
        if (!(typeID.equals (AntBreakpoint.class.getName ())))
            return null;
        
        Line line = getLine (
            properties.getString ("url", null),
            properties.getInt ("lineNumber", 1));
        if (line == null) return null;
        AntBreakpoint b = new AntBreakpoint (line);
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
        if (properties.getBoolean (Breakpoint.PROP_ENABLED, true))
            b.enable ();
        else
            b.disable ();
        return b;
    }
    
    @Override
    public void write (Object object, Properties properties) {
        AntBreakpoint b = (AntBreakpoint) object;
        FileObject fo = b.getLine().getLookup().
            lookup (FileObject.class);
            properties.setString("url", fo.toURL().toString());
            properties.setInt (
                "lineNumber", 
                b.getLine ().getLineNumber ()
            );
            properties.setString (
                Breakpoint.PROP_GROUP_NAME, 
                b.getGroupName ()
            );
            properties.setBoolean (Breakpoint.PROP_ENABLED, b.isEnabled ());
            properties.setInt(Breakpoint.PROP_HIT_COUNT_FILTER, b.getHitCountFilter());
            Breakpoint.HIT_COUNT_FILTERING_STYLE style = b.getHitCountFilteringStyle();
            properties.setInt(Breakpoint.PROP_HIT_COUNT_FILTER+"_style", style != null ? style.ordinal() : 0); // NOI18N
    }
    

    private Line getLine (String url, int lineNumber) {
        FileObject file;
        try {
            file = URLMapper.findFileObject (new URL (url));
        } catch (MalformedURLException e) {
            return null;
        }
        if (file == null) return null;
        DataObject dataObject;
        try {
            dataObject = DataObject.find (file);
        } catch (DataObjectNotFoundException ex) {
            return null;
        }
        if (dataObject == null) return null;
        LineCookie lineCookie = dataObject.getLookup().lookup(LineCookie.class);
        if (lineCookie == null) return null;
        Line.Set ls = lineCookie.getLineSet ();
        if (ls == null) return null;
        try {
            return ls.getCurrent (lineNumber);
        } catch (IndexOutOfBoundsException e) {
        } catch (IllegalArgumentException e) {
        }
        return null;
    }
}
