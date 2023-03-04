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
package org.netbeans.modules.cordova.platforms;

import org.netbeans.modules.cordova.platforms.spi.MobileDebugTransport;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.browser.spi.BrowserURLMapperImplementation;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * This class replaces CordovaMappingImpl and also MobileDebugTransport.translate.
 */
public class BrowserURLMapperImpl implements BrowserURLMapperImplementation {

    @Override
    public @CheckForNull BrowserURLMapperImplementation.BrowserURLMapper toBrowser(Project p, FileObject projectFile, URL serverURL) {
        try {
            URI uri = serverURL.toURI();
            if (uri.getAuthority() != null && uri.getAuthority().contains("localhost")) { // NOI18N
                String baseUrl = uri.getScheme() + "://" + uri.getAuthority();
                return new BrowserURLMapperImplementation.BrowserURLMapper(baseUrl,
                        baseUrl.replace("localhost", WebUtils.getLocalhostInetAddress().getHostAddress())); // NOI18N
            }
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
}
