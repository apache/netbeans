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
package org.netbeans.modules.web.browser.spi;

import java.net.URL;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;

/**
 * An SPI which allows browser to indicate that given server URL needs to be
 * converted into a browser specific URL before opening. For example Chrome browser
 * in Android device needs to use IP address instead of localhost address; this
 * method allows the browser to return mapping which expresses for example that
 * any server URL starting with text "http://localhost:1234/" needs to be
 * converted into browser URL starting with "http://192.168.0.1/".
 *
 * Project's ServerURLMappingImplementation implementations should check whether
 * project's currently selected browser provides this feature and perform URL
 * translation. That way any client of ServerURLMapping gets the target URL.
 */
public interface BrowserURLMapperImplementation {

    /**
     * Return mapping description if given URL representing given project file
     * from given project should be translated.
     * @return can return null if no mapping is suitable for given params
     */
    @CheckForNull BrowserURLMapper toBrowser(Project p, FileObject projectFile, URL serverURL);

    /**
     * Description of mapping from server URL to browser URL.
     */
    public static final class BrowserURLMapper {
        private String serverURLRoot;
        private String browserURLRoot;

        public BrowserURLMapper(String serverURLRoot, String browserURLRoot) {
            this.serverURLRoot = serverURLRoot;
            this.browserURLRoot = browserURLRoot;
        }

        public String getServerURLRoot() {
            return serverURLRoot;
        }
        
        public void setServerURLRoot(String s) {
            serverURLRoot = s;
        }

        public String getBrowserURLRoot() {
            return browserURLRoot;
        }
        
        public void setBrowserURLRoot(String url) {
            browserURLRoot = url;
        }

    }

}
