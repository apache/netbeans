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
package org.netbeans.modules.java.lsp.server.debugging.breakpoints;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;

/**
 * Creates Groovy breakpoints.
 */
public class GroovyBreakpointFactory {

    private GroovyBreakpointFactory() {}

    public static LineBreakpoint create(String url, int lineNumber) {
        LineBreakpoint groovyBreakpoint = LineBreakpoint.create(url, lineNumber);

        groovyBreakpoint.setStratum("Groovy"); // NOI18N
        FileObject fo = findFileObjectFromUrl(url);
        String sourceName = setGroovySourceName(groovyBreakpoint, fo);
        String sourcePath = setGroovySourcePath(groovyBreakpoint, fo);
        setImplClassFilter(groovyBreakpoint, sourcePath);
        setPrintText(groovyBreakpoint, sourceName);

        return groovyBreakpoint;
    }

    private static FileObject findFileObjectFromUrl(String url) {
        FileObject fo;
        try {
            fo = URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException e) {
            fo = null;
        }
        return fo;
    }

    private static void setImplClassFilter(LineBreakpoint b, String relativePath) {
        if (relativePath != null) {
            int dot = relativePath.lastIndexOf('.');
            if (dot > 0) {
                relativePath = relativePath.substring(0, dot);
                String pattern = relativePath.replace('/', '.') + "*";
                b.setPreferredClassName(pattern);
            }
        }
    }

    private static String setGroovySourceName(LineBreakpoint b, FileObject fo) {
        if (fo != null) {
            String name = fo.getNameExt();
            b.setSourceName(name);
            return name;
        }
        return null;
    }
    private static String setGroovySourcePath(LineBreakpoint b, FileObject fo) {
        if (fo != null) {
            ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
            if (cp != null) {
                FileObject root = cp.findOwnerRoot(fo);
                if (root != null) {
                    String relativePath = FileUtil.getRelativePath(root, fo);
                    b.setSourcePath(relativePath);
                    return relativePath;
                }
            }
        }
        return null;
    }

    @NbBundle.Messages("CTL_Default_Print_Text=Breakpoint hit at line {lineNumber} in {groovyName} by thread {threadName}.")
    private static void setPrintText(LineBreakpoint b, String sourceName) {
        String printText;
        if (sourceName != null) {
            printText = Bundle.CTL_Default_Print_Text().replace("{groovyName}", sourceName); // NOI18N
        } else {
            printText = Bundle.CTL_Default_Print_Text().replace("{groovyName}", "?");        // NOI18N
        }
        b.setPrintText(printText);
    }
}
