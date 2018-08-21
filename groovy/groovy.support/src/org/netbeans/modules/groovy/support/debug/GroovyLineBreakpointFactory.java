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

package org.netbeans.modules.groovy.support.debug;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.java.classpath.ClassPath;
import static org.netbeans.modules.groovy.support.debug.Bundle.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle.Messages;

/**
 * Factory for creating new Groovy line breakpoints.
 * 
 * @author Martin Janicek
 */
public class GroovyLineBreakpointFactory {

    private static final Logger LOGGER = Logger.getLogger(GroovyLineBreakpointFactory.class.getName());
    
    
    private GroovyLineBreakpointFactory() {
    }

    /**
     * Creates a new breakpoint for the given parameters.
     *
     * @param url a url
     * @param lineNumber a line number
     * @return a new breakpoint for given parameters
     */
    @Messages("CTL_Default_Print_Text=Breakpoint reached at line {lineNumber} in {groovyName} by thread {threadName}.")
    public static LineBreakpoint create(String url, int lineNumber) {
        String pt = CTL_Default_Print_Text();
        String printText = pt.replace("{groovyName}", getGroovyName(url));

        LineBreakpoint groovyBreakpoint = LineBreakpoint.create(url, lineNumber);
        groovyBreakpoint.setStratum("Groovy"); // NOI18N
        groovyBreakpoint.setSourceName(getGroovyName(url));
        groovyBreakpoint.setSourcePath(getGroovyPath(url));
        groovyBreakpoint.setPreferredClassName(getClassFilter(url));
        groovyBreakpoint.setPrintText(printText);
        groovyBreakpoint.setHidden(false);

        return groovyBreakpoint;
    }

    private static FileObject getFileObjectFromUrl(String url) {

        FileObject fo = null;

        try {
            fo = URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException e) {
            //noop
        }
        return fo;
    }

    private static String getClassFilter(String url) {
        String relativePath = getGroovyPath(url);
        if (relativePath == null) {
            return "";
        }

        if (relativePath.endsWith(".groovy")) { // NOI18N
            relativePath = relativePath.substring(0, relativePath.length() - 7);
        }
        return relativePath.replace('/', '.') + "*";
    }

    private static String getGroovyName(String url) {
        FileObject fo = getFileObjectFromUrl(url);
        if (fo != null) {
            return fo.getNameExt();
        }
        return (url == null) ? null : url.toString();
    }

    private static String getGroovyPath(String url) {
        FileObject fo = getFileObjectFromUrl(url);
        String relativePath = url;

        if (fo != null) {
            ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
            if (cp == null) {
                LOGGER.log(Level.FINE, "No classpath for {0}", url);
                return null;
            }
            FileObject root = cp.findOwnerRoot(fo);
            if (root == null) {
                return null;
            }
            relativePath = FileUtil.getRelativePath(root, fo);
        }

        return relativePath;
    }
}
