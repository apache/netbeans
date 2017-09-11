/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
