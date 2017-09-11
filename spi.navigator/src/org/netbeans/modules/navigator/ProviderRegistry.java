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

package org.netbeans.modules.navigator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Storage/lookup of NavigatorPanel providers. Providers are mapped to
 * mime types they support. 
 *
 * @author Dafe Simonek
 */
class ProviderRegistry {
    
    /** folder in layer file system where navigator panels are searched for */
    static final String PANELS_FOLDER = "Navigator/Panels/"; //NOI18N
    /** template for finding all NavigatorPanel instances in lookup */
    private static final Lookup.Template<NavigatorPanel> NAV_PANEL_TEMPLATE = 
            new Lookup.Template<NavigatorPanel>(NavigatorPanel.class);
    
    /** singleton instance */
    private static ProviderRegistry instance;
    
    /** Mapping between mime types and provider instances. Note that 
     * Collections.EMPTY_LIST serves as special value telling us that
     * we already searched for providers for specific content type and found
     * no providers. This ensures no useless repetitive searches. 
     */
    private Map<String, Collection<? extends NavigatorPanel>> contentTypes2Providers;


    /** Singleton, no external instantiation */
    private ProviderRegistry () {
    }

    /********* public area *********/
    
    public static ProviderRegistry getInstance () {
        if (instance == null ) {
            instance = new ProviderRegistry();
        }
        return instance;
    }
    
    /** Finds appropriate providers for given data content type
     * (similar to mime type)
     * and returns list of provider classes.
     *
     * @return Collection of providers, which implements NavigatorPanel interface.
     * Never return null, only empty List if no provider exists for given content type.
     */
    public Collection<? extends NavigatorPanel> getProviders (String contentType) {
        if (contentTypes2Providers == null) {
            contentTypes2Providers = new HashMap<String, Collection<? extends NavigatorPanel>>(15);
        }
        Collection<? extends NavigatorPanel> result = contentTypes2Providers.get(contentType);
        if (result == null) {
            // load and instantiate provider classes
            result = loadProviders(contentType);
            contentTypes2Providers.put(contentType, result);
        }
            
        return result;
    }
    
    /******* private stuff ***********/

    
    /** Returns collection of NavigatorPanels or empty collection if no provider
     * exist for given content type
     */
    private Collection<? extends NavigatorPanel> loadProviders (String contentType) {
        String path = PANELS_FOLDER + contentType;

        Lookup.Result<NavigatorPanel> result = Lookups.forPath(path).lookup(NAV_PANEL_TEMPLATE);

        return result.allInstances();
    }

}
