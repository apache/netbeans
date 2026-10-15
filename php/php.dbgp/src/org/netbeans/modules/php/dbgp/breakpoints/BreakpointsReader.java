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
package org.netbeans.modules.php.dbgp.breakpoints;

import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.api.debugger.Properties;
import org.netbeans.modules.php.dbgp.breakpoints.FunctionBreakpoint.Type;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.text.Line;

/**
 *
 * @author ads
 */
public class BreakpointsReader implements Properties.Reader {

    private static final String ENABLED = "enabled"; // NOI18N
    private static final String FUNC_NAME = "functionName"; // NOI18N
    private static final String EXCEPTION_NAME = "exceptionName"; // NOI18N
    private static final String TYPE = "type"; // NOI18N
    private static final String GROUP_NAME = "groupName"; // NOI18N
    private static final String[] SUPPORTED_CLASS_NAMES =  new String[] {
            LineBreakpoint.class.getName(),
            FunctionBreakpoint.class.getName(),
            ExceptionBreakpoint.class.getName()
        };

    @Override
    public String[] getSupportedClassNames() {
        return SUPPORTED_CLASS_NAMES;
    }

    @Override
    public Object read(String typeID, Properties properties) {
        if (typeID.equals(LineBreakpoint.class.getName())) {
            Line line = getLine(properties.getString(LineBreakpoint.PROP_URL, null), properties.getInt(LineBreakpoint.PROP_LINE_NUMBER, 1));
            if (line == null) {
                return null;
            }
            LineBreakpoint breakpoint = new LineBreakpoint(line);
            if (!properties.getBoolean(ENABLED, true)) {
                breakpoint.disable();
            }
            breakpoint.setGroupName(properties.getString(GROUP_NAME, ""));
            breakpoint.setCondition(properties.getString(LineBreakpoint.PROP_CONDITION, null));
            return breakpoint;
        } else if (typeID.equals(FunctionBreakpoint.class.getName())) {
            String func = properties.getString(FUNC_NAME, null);
            Type type = Type.forString(properties.getString(TYPE, null));
            if (func == null || type == null) {
                return null;
            }
            FunctionBreakpoint breakpoint = new FunctionBreakpoint(type, func);
            if (!properties.getBoolean(ENABLED, true)) {
                breakpoint.disable();
            }
            breakpoint.setGroupName(properties.getString(GROUP_NAME, ""));
            return breakpoint;
        } else if (typeID.equals(ExceptionBreakpoint.class.getName())) {
            String exception = properties.getString(EXCEPTION_NAME, null);
            ExceptionBreakpoint breakpoint = new ExceptionBreakpoint(exception);
            if (!properties.getBoolean(ENABLED, true)) {
                breakpoint.disable();
            }
            breakpoint.setGroupName(properties.getString(GROUP_NAME, "")); // NOI18N
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
            properties.setString(LineBreakpoint.PROP_URL, fileObject.toURL().toString());
            properties.setInt(LineBreakpoint.PROP_LINE_NUMBER, breakpoint.getLine().getLineNumber());
            properties.setBoolean(ENABLED, breakpoint.isEnabled());
            properties.setString(GROUP_NAME, breakpoint.getGroupName());
            properties.setString(LineBreakpoint.PROP_CONDITION, breakpoint.getCondition());
        } else if (object instanceof FunctionBreakpoint) {
            FunctionBreakpoint breakpoint = (FunctionBreakpoint) object;
            String func = breakpoint.getFunction();
            properties.setString(FUNC_NAME, func);
            properties.setString(TYPE, breakpoint.getType().toString());
            properties.setBoolean(ENABLED, breakpoint.isEnabled());
            properties.setString(GROUP_NAME, breakpoint.getGroupName());
        } else if (object instanceof ExceptionBreakpoint) {
            ExceptionBreakpoint breakpoint = (ExceptionBreakpoint) object;
            String exception = breakpoint.getException();
            properties.setString(EXCEPTION_NAME, exception);
            properties.setBoolean(ENABLED, breakpoint.isEnabled());
            properties.setString(GROUP_NAME, breakpoint.getGroupName());
        }
    }

    private Line getLine(String url, int lineNumber) {
        FileObject file = getFileObject(url);
        if (file == null) {
            return null;
        }
        return Utils.getLine(file, lineNumber);
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
