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
