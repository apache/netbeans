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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
