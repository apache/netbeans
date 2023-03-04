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
