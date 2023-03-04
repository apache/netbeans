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
package org.netbeans.modules.extbrowser.plugins;

import java.util.List;
import org.netbeans.modules.web.browser.api.BrowserFamilyId;


/**
 * @author ads
 *
 */
public interface ExtensionManagerAccessor {

    BrowserExtensionManager getManager();
    
    public abstract static class AbstractBrowserExtensionManager 
        implements BrowserExtensionManager 
    {
        protected static final String PLUGIN_MODULE_NAME = 
            "org.netbeans.modules.extbrowser.chrome";             // NOI18N
        
        protected abstract String getCurrentPluginVersion();
        
        protected boolean isUpdateRequired(String extVersion) {
            String currentVersion = getCurrentPluginVersion();
            if (extVersion == null) {
                return true;
            }
            else if (currentVersion == null) {
                return false;
            }

            List<Integer> extList = Utils.getVersionParts(extVersion);
            List<Integer> minList = Utils.getVersionParts(currentVersion);

            for (int i = 0; i < Math.max(extList.size(), minList.size()); i++) {
                int extValue = i >= extList.size() ? 0 : extList.get(i);
                int minValue = i >= minList.size() ? 0 : minList.get(i);

                if (extValue < minValue) {
                    return true;
                } else if (extValue > minValue) {
                    return false;
                }
            }

            return false;
        }
    }
    
    static interface BrowserExtensionManager {
        
        ExtensionManager.ExtensitionStatus isInstalled();
        
        boolean install( ExtensionManager.ExtensitionStatus currentStatus);

        BrowserFamilyId getBrowserFamilyId();
    }
    
}
