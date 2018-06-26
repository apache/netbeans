/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.php.dbgp.breakpoints;

import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.api.debugger.Properties;
import org.netbeans.modules.php.dbgp.breakpoints.FunctionBreakpoint.Type;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;

/**
 *
 * @author ads
 */
public class BreakpointsReader implements Properties.Reader {
    private static final String LINE_NUMBER = "lineNumber"; // NOI18N
    private static final String URL = "url"; // NOI18N
    private static final String ENABED = "enabled"; // NOI18N
    private static final String FUNC_NAME = "functionName"; // NOI18N
    private static final String TYPE = "type"; // NOI18N
    private static final String GROUP_NAME = "groupName"; // NOI18N

    @Override
    public String[] getSupportedClassNames() {
        return new String[]{
            LineBreakpoint.class.getName(),
            FunctionBreakpoint.class.getName()
        };
    }

    @Override
    public Object read(String typeID, Properties properties) {
        if (typeID.equals(LineBreakpoint.class.getName())) {
            Line line = getLine(properties.getString(URL, null), properties.getInt(LINE_NUMBER, 1));
            if (line == null) {
                return null;
            }
            LineBreakpoint breakpoint = new LineBreakpoint(line);
            if (!properties.getBoolean(ENABED, true)) {
                breakpoint.disable();
            }
            breakpoint.setGroupName(properties.getString(GROUP_NAME, ""));
            return breakpoint;
        } else if (typeID.equals(FunctionBreakpoint.class.getName())) {
            String func = properties.getString(FUNC_NAME, null);
            Type type = Type.forString(properties.getString(TYPE, null));
            if (func == null || type == null) {
                return null;
            }
            FunctionBreakpoint breakpoint = new FunctionBreakpoint(type, func);
            if (!properties.getBoolean(ENABED, true)) {
                breakpoint.disable();
            }
            breakpoint.setGroupName(properties.getString(GROUP_NAME, ""));
            return breakpoint;
        } else {
            return null;
        }
    }

    @Override
    public void write(Object object, Properties properties) {
        if (object instanceof LineBreakpoint) {
            LineBreakpoint breakpoint = (LineBreakpoint) object;
            FileObject fileObject = breakpoint.getLine().getLookup().lookup(FileObject.class);
            properties.setString(URL, fileObject.toURL().toString());
            properties.setInt(LINE_NUMBER, breakpoint.getLine().getLineNumber());
            properties.setBoolean(ENABED, breakpoint.isEnabled());
            properties.setString(GROUP_NAME, breakpoint.getGroupName());
        } else if (object instanceof FunctionBreakpoint) {
            FunctionBreakpoint breakpoint = (FunctionBreakpoint) object;
            String func = breakpoint.getFunction();
            properties.setString(FUNC_NAME, func);
            properties.setString(TYPE, breakpoint.getType().toString());
            properties.setBoolean(ENABED, breakpoint.isEnabled());
            properties.setString(GROUP_NAME, breakpoint.getGroupName());
        }
    }

    private Line getLine(String url, int lineNumber) {
        FileObject file = getFileObject(url);
        if (file == null) {
            return null;
        }
        DataObject dataObject;
        try {
            dataObject = DataObject.find(file);
        } catch (DataObjectNotFoundException ex) {
            return null;
        }
        if (dataObject == null) {
            return null;
        }
        LineCookie lineCookie = dataObject.getLookup().lookup(LineCookie.class);
        if (lineCookie == null) {
            return null;
        }
        Line.Set ls = lineCookie.getLineSet();
        if (ls == null) {
            return null;
        }
        try {
            return ls.getCurrent(lineNumber);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private FileObject getFileObject(String url) {
        FileObject file;
        try {
            file = URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException e) {
            return null;
        }
        return file;
    }

}
