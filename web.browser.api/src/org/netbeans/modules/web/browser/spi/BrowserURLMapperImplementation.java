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
