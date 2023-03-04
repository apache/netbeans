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

package org.netbeans.modules.debugger.jpda.js.breakpoints.io;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.javascript2.debug.EditorLineHandler;
import org.netbeans.modules.javascript2.debug.EditorLineHandlerFactory;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.netbeans.modules.javascript2.debug.breakpoints.io.BreakpointsFromGroup;
import org.netbeans.modules.javascript2.debug.breakpoints.io.BreakpointsFromGroup.TestGroupProperties;
import org.netbeans.modules.javascript2.debug.sources.SourceFilesCache;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Martin
 */
@DebuggerServiceRegistration(types={Properties.Reader.class})
public class JSBreakpointReader implements Properties.Reader {
    
    private static final Logger LOG = Logger.getLogger(JSBreakpointReader.class.getName());

    private static final String BREAKPOINTS_TO_ENABLE = "breakpointsToEnable";
    private static final String BREAKPOINTS_TO_DISABLE = "breakpointsToDisable";
    private static final String BP_CUSTOM_GROUP = "CustomGroup";
    private static final String BP_FILE_GROUP = "FileGroup";
    private static final String BP_PROJECT_GROUP = "ProjectGroup";
    private static final String BP_TYPE_GROUP = "TypeGroup";
    
    private static final String OLD_JS_LINE_BP = "org.netbeans.modules.debugger.jpda.js.breakpoints.JSLineBreakpoint";
    
    @Override
    public String[] getSupportedClassNames() {
        return new String[] {
            OLD_JS_LINE_BP, 
        };
    }

    @Override
    public Object read(String className, Properties properties) {
        JSLineBreakpoint b = null;
        if (className.equals(OLD_JS_LINE_BP)) {
            String urlStr = properties.getString (JSLineBreakpoint.PROP_URL, null);
            int lineNumber = properties.getInt (JSLineBreakpoint.PROP_LINE_NUMBER, 1);
            try {
                URL url = new URL(urlStr);
                FileObject fo = URLMapper.findFileObject(url);
                if (fo == null) {
                    if (SourceFilesCache.URL_PROTOCOL.equals(url.getProtocol())) {
                        EditorLineHandler lineHandler = EditorLineHandlerFactory.getHandler(url, lineNumber);
                        return new JSLineBreakpoint(lineHandler);
                    } else {
                        return null;
                    }
                } else {
                    EditorLineHandler lineHandler = EditorLineHandlerFactory.getHandler(fo, lineNumber);
                    if (lineHandler == null) {
                        return null;
                    }
                    b = new JSLineBreakpoint(lineHandler);
                }
            } catch (MalformedURLException ex) {
                LOG.log(Level.CONFIG, "urlStr = "+urlStr, ex);
                return null;
            }
            
        }
        if (b == null) {
            throw new IllegalStateException("Unknown breakpoint type: \""+className+"\"");
        }
        b.setCondition(properties.getString(JSLineBreakpoint.PROP_CONDITION, null));
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
            b.setBreakpointsToEnable(getBreakpointsFromGroup(properties, BREAKPOINTS_TO_ENABLE));
            b.setBreakpointsToDisable(getBreakpointsFromGroup(properties, BREAKPOINTS_TO_DISABLE));
        }
        return b;
    }

    @Override
    public void write(Object object, Properties properties) {
        // org.netbeans.modules.javascript2.debug.breakpoints.io.JSBreakpointReader is used instead.
    }
    
    private static Set<Breakpoint> getBreakpointsFromGroup(Properties properties, String base) {
        String bpGroup = properties.getString(base + BP_CUSTOM_GROUP, null);
        if (bpGroup != null) {
            return new BreakpointsFromGroup(bpGroup);
        }
        bpGroup = properties.getString(base + BP_FILE_GROUP, null);
        if (bpGroup != null) {
            try {
                URL url = new URL(bpGroup);
                FileObject fo = URLMapper.findFileObject(url);
                if (fo != null) {
                    return new BreakpointsFromGroup(new TestGroupProperties(fo));
                }
            } catch (MalformedURLException ex) {
            }
        }
        bpGroup = properties.getString(base + BP_PROJECT_GROUP, null);
        if (bpGroup != null) {
            try {
                URL url = new URL(bpGroup);
                FileObject fo = URLMapper.findFileObject(url);
                if (fo != null) {
                    Project project = ProjectManager.getDefault().findProject(fo);
                    if (project != null) {
                        return new BreakpointsFromGroup(new TestGroupProperties(project));
                    }
                }
            } catch (IOException | IllegalArgumentException ex) {
            }
        }
        bpGroup = properties.getString(base + BP_TYPE_GROUP, null);
        if (bpGroup != null) {
            return new BreakpointsFromGroup(new TestGroupProperties(bpGroup));
        }
        return Collections.emptySet();
    }
    
}
