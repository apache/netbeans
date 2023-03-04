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
package org.netbeans.modules.web.debug;

import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.web.debug.util.Utils;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin
 */
@SourcePathProvider.Registration(path = "netbeans-JPDASession")
public class FirstSourceURLProvider extends SourcePathProvider {
    
    private static final String[] NO_SOURCE_ROOTS = new String[]{};
    
    private final JPDADebugger debugger;
    private final ThreadLocal<Boolean> evaluating = new ThreadLocal<Boolean>();
    private final Map<JPDAClassType, String> urlsByClassType = new HashMap<JPDAClassType, String>();
    
    public FirstSourceURLProvider(ContextProvider context) {
        debugger = context.lookupFirst(null, JPDADebugger.class);
    }

    @Override
    public String getURL(String relativePath, boolean global) {
        if (!Utils.hasContext(relativePath)) {
            return null;
        }
        Boolean isEval = evaluating.get();
        if (Boolean.TRUE.equals(isEval)) {
            return null;
        }
        JPDAClassType currentType = getCurrentType();
        if (currentType == null) {
            return null;
        }
        synchronized (urlsByClassType) {
            if (urlsByClassType.containsKey(currentType)) {
                String url = urlsByClassType.get(currentType);
                return url;
            }
        }
        String url = null;
        try {
            evaluating.set(Boolean.TRUE);
            Variable contextPathVar = debugger.evaluate("request.getContextPath()");
            String contextPath = contextPathVar.getValue();
            if (contextPath.startsWith("\"") && contextPath.endsWith("\"")) {
                contextPath = contextPath.substring(1, contextPath.length() - 1);
            }
            FileObject docBase = Utils.getDocumentBaseForContextPath(contextPath);
            if (docBase != null) {
                FileObject fo = docBase.getFileObject(relativePath);
                if (fo != null) {
                    url = fo.toURL().toExternalForm();
                }
            }
        } catch (InvalidExpressionException ex) {
        } finally {
            evaluating.remove();
        }
        synchronized (urlsByClassType) {
            urlsByClassType.put(currentType, url);
        }
        return url;
    }
    
    private JPDAClassType getCurrentType() {
        CallStackFrame currentCallStackFrame = debugger.getCurrentCallStackFrame();
        if (currentCallStackFrame == null) {
            return null;
        }
        //currentCallStackFrame.getClassType() - TODO: Add to API
        try {
            Method getClassTypeMethod = currentCallStackFrame.getClass().getMethod("getClassType");
            return (JPDAClassType) getClassTypeMethod.invoke(currentCallStackFrame);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    @Override
    public String getRelativePath(String url, char directorySeparator, boolean includeExtension) {
        return null;
    }

    @Override
    public String[] getSourceRoots() {
        return NO_SOURCE_ROOTS;
    }

    @Override
    public void setSourceRoots(String[] sourceRoots) {
    }

    @Override
    public String[] getOriginalSourceRoots() {
        return NO_SOURCE_ROOTS;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
    }
    
}
