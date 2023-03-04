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

import org.netbeans.modules.web.browser.api.WebBrowserFeatures;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;


/**
 * SPI describing additional browser behaviours.
 */
public interface EnhancedBrowser {
    
    void initialize(WebBrowserFeatures browserFeatures);
    
    /**
     * Close communication chanel (if there is any) between IDE and browser.
     * Closing browser window is an optional feature and it may or may not be
     * supported by current browser.
     * @param closeTab should the browser window be closed as well?
     */
    void close(boolean closeTab);

    /**
     * Is this browser capable of reloading rendered page? For example embedded
     * Webkit browser or Chrome with NB plugin is.
     */
    boolean canReloadPage();

    /**
     * A way to associate a project context with a URL which is going to be 
     * opened in the browser. This method should be called before HtmlBrowser.Impl.setURL
     * 
     * @param projectContext a lookup containing Project instance; could be empty lookup if
     * non-project URL is being opened in the browser
     */
    void setProjectContext(Lookup projectContext);

    /**
     * Check whether change in given file should be ignored or not. For example
     * 'Chrome with NetBeans Connector' browser handles CSS file changes
     * directly via CSS editing support and therefore generic 'Refresh On Save' mechanism
     * should ignore all CSS changed. However if WebKit debugging protocol was
     * aborted then CSS file changes needs to be handled by generic 'Refresh On Save'
     * mechanism. This SPI allows browser to indicate whether file change should
     * be ignored or not by generic reload mechanism.
     * @param fo file to test
     * @return true if file change should be ignored and no attempt should be made
     *      to reload it (or any file which depends on it) in browser
     */
    boolean ignoreChange(FileObject fo);

}
