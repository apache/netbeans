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

package org.netbeans.modules.web.clientproject.spi.platform;

import org.netbeans.modules.web.browser.api.WebBrowser;

/**
 * Provider of ClientProjectEnhancedBrowserImplementation to be registered in
 * project type lookup via
 * @ProjectServiceProvider(projectType = "org-netbeans-modules-web-clientproject", ...).
 * Provider decides based on browser family type whether they handle browser or not.
 */
public interface ClientProjectEnhancedBrowserProvider {
    
    ClientProjectEnhancedBrowserImplementation getEnhancedBrowser(WebBrowser webBrowser);
    
}
