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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.web.jsf.navigation;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.openide.util.Exceptions;

/**
 *
 * @author joelle
 */
public class FacesModelUtility {

    private static final Logger LOGGER = Logger.getLogger(FacesModelUtility.class.getName());

    private FacesModelUtility() {
    }

    public static String getToViewIdFiltered(NavigationCase navCase) {
        final String viewId = navCase.getToViewId();
        return getViewIdFiltiered( viewId );
    }

    public static String getFromViewIdFiltered( NavigationRule navRule ){
        final String viewId = navRule.getFromViewId();
        return getViewIdFiltiered( viewId );
    }

    public static String getViewIdFiltiered( String paramViewId ){
        String viewId = paramViewId;
        if( viewId != null && viewId.length() > 0 && viewId.charAt(0) == '/') {
            viewId = viewId.replaceFirst("/", "");
        }
        return viewId;
    }

    public static void setToViewId(NavigationCase navCase, String filteredName ){
        if( filteredName != null && filteredName.length() > 0 ) {
            final String unfilteredName = "/".concat(filteredName);
            navCase.setToViewId(unfilteredName);
        } else {
            navCase.setToViewId(filteredName);
        }
        
    }

    public static void setFromViewId( NavigationRule navRule, String filteredName ){

        if( filteredName != null && filteredName.length() > 0 ) {
            final String unfilteredName = "/".concat(filteredName);
            navRule.setFromViewId(unfilteredName);
        } else {
            navRule.setFromViewId(filteredName);
        }
    }

    /**
     * Renames a page in the faces configuration file.
     * @param oldDisplayName
     * @param newDisplayName
     */
    public static void renamePageInModel(JSFConfigModel configModel, String oldDisplayName, String newDisplayName ) {
        if (oldDisplayName.equals(newDisplayName)){
            return;
        }
        configModel.startTransaction();
        final FacesConfig facesConfig = configModel.getRootComponent();
        final List<NavigationRule> navRules = facesConfig.getNavigationRules();
        for( NavigationRule navRule : navRules ){
            final String fromViewId = getFromViewIdFiltered(navRule);
            if ( fromViewId != null && fromViewId.equals(oldDisplayName) ){
                //                navRule.setFromViewId(newDisplayName);
                setFromViewId(navRule, newDisplayName);
            }
            final List<NavigationCase> navCases = navRule.getNavigationCases();
            for( NavigationCase navCase : navCases ) {
                //                String toViewId = navCase.getToViewId();
                final String toViewId = getToViewIdFiltered(navCase);
                if ( toViewId != null && toViewId.equals(oldDisplayName) ) {
                    //                    navCase.setToViewId(newDisplayName);
                    setToViewId(navCase, newDisplayName);
                }
            }
        }

        try {
            configModel.endTransaction();
            configModel.sync();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalStateException ise) {
            Exceptions.printStackTrace(ise);
        }
    }
    
}
