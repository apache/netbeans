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
