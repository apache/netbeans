/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.browser.spi;

import java.awt.Image;
import org.netbeans.modules.web.browser.api.BrowserFamilyId;

/**
 *
 */
public interface EnhancedBrowserFactory {

    /**
     * Type of browser.
     */
    BrowserFamilyId getBrowserFamilyId();

    /**
     * Image icon representing this browser.
     * @param small return 16x16 icon if true; otherwise 24x24 size expected
     * @return can return null (in which case a fallback icon will be used as
     * implemented in WebBrowser itself)
     */
    Image getIconImage(boolean small);

    /**
     * Display name of browser.
     * 
     * @return can return null (in which case display name of the node which
     * represents registration of this browser in the SystemFileSystem will be
     * used instead)
     */
    String getDisplayName();

    /**
     * An ID which uniquely identifies this browser and can be persisted for
     * future references to this browser.
     * @return can be null (in which case browser instance is expected
     * to be registered as file in default filesystem and its Lookup.Item.getId
     * is used instead.)
     */
    String getId();

    /**
     * Is this factory producing browsers with tight NetBeans integration (Connector).
     * @return
     */
    boolean hasNetBeansIntegration();

    /**
     * Can this factory produce a browser for current OS? This is an API alternative
     * to using "hidden" attribute in registrations via module layer.
     */
    boolean canCreateHtmlBrowserImpl();
}
