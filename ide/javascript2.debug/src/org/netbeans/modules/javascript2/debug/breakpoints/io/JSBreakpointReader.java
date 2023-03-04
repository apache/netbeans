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

package org.netbeans.modules.javascript2.debug.breakpoints.io;

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
import org.netbeans.modules.javascript2.debug.breakpoints.JSBreakpointsInfoManager;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.netbeans.modules.javascript2.debug.breakpoints.io.BreakpointsFromGroup.TestGroupProperties;
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
    
    @Override
    public String[] getSupportedClassNames() {
        return new String[] {
            JSLineBreakpoint.class.getName (), 
        };
    }

    @Override
    public Object read(String className, Properties properties) {
        JSLineBreakpoint b = null;
        if (className.equals(JSLineBreakpoint.class.getName())) {
            String urlStr = properties.getString (JSLineBreakpoint.PROP_URL, null);
            int lineNumber = properties.getInt (JSLineBreakpoint.PROP_LINE_NUMBER, 1);
            try {
                URL url = new URL(urlStr);
                FileObject fo = URLMapper.findFileObject(url);
                if (fo == null) {
                    if (JSBreakpointsInfoManager.getDefault().isTransientURL(url)) {
                        EditorLineHandler line = EditorLineHandlerFactory.getHandler(url, lineNumber);
                        b = new JSLineBreakpoint(line);
                    } else {
                        return null;
                    }
                } else {
                    EditorLineHandler line = EditorLineHandlerFactory.getHandler(fo, lineNumber);
                    if (line != null) {
                        b = new JSLineBreakpoint(line);
                    } else {
                        return null;
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
        JSLineBreakpoint b = (JSLineBreakpoint) object;
        /*properties.setString (
            JSBreakpoint.PROP_PRINT_TEXT, 
            b.getPrintText ()
        );*/
        properties.setString (
            Breakpoint.PROP_GROUP_NAME, 
            b.getGroupName ()
        );
        properties.setBoolean (Breakpoint.PROP_ENABLED, b.isEnabled ());
        properties.setInt(Breakpoint.PROP_HIT_COUNT_FILTER, b.getHitCountFilter());
        Breakpoint.HIT_COUNT_FILTERING_STYLE style = b.getHitCountFilteringStyle();
        properties.setInt(Breakpoint.PROP_HIT_COUNT_FILTER+"_style", style != null ? style.ordinal() : 0); // NOI18N
        if (b.canHaveDependentBreakpoints()) {
            Set<Breakpoint> breakpointsToEnable = b.getBreakpointsToEnable();
            setBreakpointsFromGroup(properties, BREAKPOINTS_TO_ENABLE, breakpointsToEnable);
            Set<Breakpoint> breakpointsToDisable = b.getBreakpointsToDisable();
            setBreakpointsFromGroup(properties, BREAKPOINTS_TO_DISABLE, breakpointsToDisable);
        }
        
        properties.setString(JSLineBreakpoint.PROP_CONDITION, b.getCondition());
        if (b instanceof JSLineBreakpoint) {
            JSLineBreakpoint lb = (JSLineBreakpoint) b;
            URL url = lb.getURL();
            int line = lb.getLineNumber();
            properties.setString(JSLineBreakpoint.PROP_URL, url.toExternalForm());
            properties.setInt(JSLineBreakpoint.PROP_LINE_NUMBER, line);
        }
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
    
    private static void setBreakpointsFromGroup(Properties properties, String base, Set<Breakpoint> breakpointsFromGroup) {
        String customGroup = null;
        String fileURL = null;
        String projectURL = null;
        String type = null;
        if (breakpointsFromGroup instanceof BreakpointsFromGroup) {
            BreakpointsFromGroup bfg = (BreakpointsFromGroup) breakpointsFromGroup;
            customGroup = bfg.getGroupName();
            TestGroupProperties tgp = bfg.getTestGroupProperties();
            if (tgp != null) {
                FileObject fo = tgp.getFileObject();
                if (fo != null) {
                    URL url = fo.toURL();
                    fileURL = url.toExternalForm();
                }
                Project project = tgp.getProject();
                if (project != null) {
                    fo = project.getProjectDirectory();
                    URL url = fo.toURL();
                    projectURL = url.toExternalForm();
                }
                type = tgp.getType();
            }
        }
        properties.setString(base + BP_CUSTOM_GROUP, customGroup);
        properties.setString(base + BP_FILE_GROUP, fileURL);
        properties.setString(base + BP_PROJECT_GROUP, projectURL);
        properties.setString(base + BP_TYPE_GROUP, type);
    }
    
}
