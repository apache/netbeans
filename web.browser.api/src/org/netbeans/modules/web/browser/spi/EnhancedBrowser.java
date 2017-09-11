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
