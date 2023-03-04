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

package org.netbeans.modules.debugger.jpda.truffle.breakpoints.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.Properties;
import org.netbeans.modules.debugger.jpda.truffle.breakpoints.TruffleLineBreakpoint;
import org.netbeans.modules.debugger.jpda.truffle.source.Source;
import org.netbeans.modules.javascript2.debug.EditorLineHandler;
import org.netbeans.modules.javascript2.debug.EditorLineHandlerFactory;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;

/**
 * Breakpoints storage.
 */
@DebuggerServiceRegistration(types={Properties.Reader.class})
public class TruffleBreakpointReader implements Properties.Reader {

    private static final Logger LOG = Logger.getLogger(TruffleBreakpointReader.class.getName());
    
    @Override
    public String[] getSupportedClassNames() {
        return new String[] {
            TruffleLineBreakpoint.class.getName (), 
        };
    }

    @Override
    public Object read(String className, Properties properties) {
        TruffleLineBreakpoint b = null;
        if (className.equals(TruffleLineBreakpoint.class.getName())) {
            String urlStr = properties.getString (TruffleLineBreakpoint.PROP_URL, null);
            int lineNumber = properties.getInt (TruffleLineBreakpoint.PROP_LINE_NUMBER, 1);
            try {
                URL url = new URL(urlStr);
                FileObject fo = URLMapper.findFileObject(url);
                if (fo == null) {
                    if (isTransientURL(url)) {
                        EditorLineHandler line = EditorLineHandlerFactory.getHandler(url, lineNumber);
                        b = new TruffleLineBreakpoint(line);
                    } else {
                        return null;
                    }
                } else {
                    if (Lookup.getDefault().lookup(EditorLineHandlerFactory.class) != null) {
                        EditorLineHandler line = EditorLineHandlerFactory.getHandler(fo, lineNumber);
                        if (line != null) {
                            b = new TruffleLineBreakpoint(line);
                        } else {
                            return null;
                        }
                    } else {
                        b = new TruffleLineBreakpoint(url, lineNumber);
                    }
                }
            } catch (MalformedURLException ex) {
                LOG.log(Level.CONFIG, "urlStr = "+urlStr, ex);
                return null;
            }
            
        }
        if (b == null) {
            throw new IllegalStateException("Unknown breakpoint type: \""+className+"\"");
        }
        b.setCondition(properties.getString(TruffleLineBreakpoint.PROP_CONDITION, null));
        /*b.setPrintText (
            properties.getString (JSBreakpoint.PROP_PRINT_TEXT, "")
        );*/
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
        if (b.canHaveDependentBreakpoints()) {
            // TODO
        }
        return b;
    }
    
    private boolean isTransientURL(URL url) {
        return Source.URL_PROTOCOL.equals(url.getProtocol());
    }

    @Override
    public void write(Object object, Properties properties) {
        TruffleLineBreakpoint b = (TruffleLineBreakpoint) object;
        properties.setString (
            Breakpoint.PROP_GROUP_NAME, 
            b.getGroupName ()
        );
        properties.setBoolean (Breakpoint.PROP_ENABLED, b.isEnabled ());
        properties.setInt(Breakpoint.PROP_HIT_COUNT_FILTER, b.getHitCountFilter());
        Breakpoint.HIT_COUNT_FILTERING_STYLE style = b.getHitCountFilteringStyle();
        properties.setInt(Breakpoint.PROP_HIT_COUNT_FILTER+"_style", style != null ? style.ordinal() : 0); // NOI18N
        if (b.canHaveDependentBreakpoints()) {
            // TODO
        }
        
        properties.setString(TruffleLineBreakpoint.PROP_CONDITION, b.getCondition());
        URL url = b.getURL();
        int line = b.getLineNumber();
        properties.setString(TruffleLineBreakpoint.PROP_URL, url.toExternalForm());
        properties.setInt(TruffleLineBreakpoint.PROP_LINE_NUMBER, line);
    }
    
}
