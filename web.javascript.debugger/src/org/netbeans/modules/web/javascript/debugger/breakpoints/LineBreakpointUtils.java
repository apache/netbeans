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

package org.netbeans.modules.web.javascript.debugger.breakpoints;

import java.net.URL;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.netbeans.modules.web.common.api.RemoteFileCache;
import org.netbeans.modules.web.common.api.ServerURLMapping;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Entlicher, Antoine Vandecreme
 */
class LineBreakpointUtils {
    
    /**
     * Difference from getURLString method is that project's local file URL 
     * (eg file://myproject/src/foo.html) is not converted into project's
     * deployment URL (ie http://localhost/smth/foo.html). When persisting 
     * breakpoints they should always be persisted in form of project's local
     * file URL.
     */
    static String getURLStringToPersist(JSLineBreakpoint lb) {
        return getURLStringImpl(lb, null, null, false);
    }
    
    /**
     * See also getURLStringToPersist().
     */
    static String getURLString(JSLineBreakpoint lb, Project p, URL urlConnectionBeingDebugged) {
        return getURLStringImpl(lb, p, urlConnectionBeingDebugged, true);
    }
    
    static String getURLString(FileObject fo, Project p, URL urlConnectionBeingDebugged) {
        return getURLStringImpl(fo, p, urlConnectionBeingDebugged, true);
    }
    
    private static String getURLStringImpl(JSLineBreakpoint lb, Project p, URL urlConnectionBeingDebugged, boolean applyInternalServerMapping) {
        FileObject fo = lb.getFileObject();
        return getURLStringImpl(fo, p, urlConnectionBeingDebugged, applyInternalServerMapping);
    }
    
    private static String getURLStringImpl(FileObject fo, Project p, URL urlConnectionBeingDebugged, boolean applyInternalServerMapping) {
        String url;
        URL remoteURL = RemoteFileCache.isRemoteFile(fo);
        if (remoteURL == null) {
            // should "file://foo.bar" be translated into "http://localhost/smth/foo.bar"?
            if (applyInternalServerMapping && p != null) {
                assert urlConnectionBeingDebugged != null;
                URL internalServerURL = ServerURLMapping.toServer(p, ServerURLMapping.CONTEXT_PROJECT_SOURCES, fo);
                boolean useTestingContext = false;
                if (internalServerURL == null) {
                    useTestingContext = true;
                } else {
                    if (!internalServerURL.getHost().equals(urlConnectionBeingDebugged.getHost()) ||
                            internalServerURL.getPort() != urlConnectionBeingDebugged.getPort()) {
                        // if FileObject was resolved to a server which is different from current
                        // debugging connection then try to resolve the FileObject 
                        // in ServerURLMapping.CONTEXT_PROJECT_TESTS context
                        useTestingContext = true;
                    }
                }
                if (useTestingContext && p != null) {
                    URL internalServerURL2 = ServerURLMapping.toServer(p, ServerURLMapping.CONTEXT_PROJECT_TESTS, fo);
                    if (internalServerURL2 != null && 
                            (internalServerURL2.getHost().equals(urlConnectionBeingDebugged.getHost()) ||
                            internalServerURL2.getPort() == urlConnectionBeingDebugged.getPort())) {
                        // use it:
                        internalServerURL = internalServerURL2;
                    }
                }
                if (internalServerURL != null) {
                    return internalServerURL.toExternalForm();
                }
            }
            url = fo.toURL().toExternalForm();
        } else {
            url = remoteURL.toExternalForm();
        }
        return url;
    }

    
}
