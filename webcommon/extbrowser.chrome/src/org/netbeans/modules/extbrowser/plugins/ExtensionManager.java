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

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.extbrowser.plugins.ExtensionManagerAccessor.BrowserExtensionManager;
import org.netbeans.modules.extbrowser.plugins.chrome.ChromeManagerAccessor;
import org.netbeans.modules.extbrowser.plugins.chrome.ChromiumManagerAccessor;
import org.netbeans.modules.web.browser.api.BrowserFamilyId;



/**
 * @author ads
 *
 */
public final class ExtensionManager {

    public static enum ExtensitionStatus {
        INSTALLED,
        MISSING,
        NEEDS_UPGRADE,
        DISABLED,
        UNKNOWN
    }
    
    private ExtensionManager(){
    }
    
    public static ExtensitionStatus isInstalled( BrowserFamilyId id ){
        if ( id == null ){
            // TODO : show browser chooser
        }
        else {
            ExtensionManagerAccessor accessor = ACCESSORS.get(id);
            if ( accessor == null ){
                return ExtensitionStatus.UNKNOWN;
            }
            BrowserExtensionManager manager = accessor.getManager();
            return manager.isInstalled();
        }
        return ExtensitionStatus.UNKNOWN;
    }
    
    /**
     * @return true if extension is available
     */
    public static boolean installExtension(  BrowserFamilyId id , 
            ExtensionManager.ExtensitionStatus currentStatus){
        if ( id == null ){
            // TODO : show browser chooser
        }
        else {
            ExtensionManagerAccessor accessor = ACCESSORS.get(id);
            if ( accessor == null ){
                return false ;
            }
            BrowserExtensionManager manager = accessor.getManager();
            return manager.install( currentStatus );
        }
        return false;
    }
    
    private static Map<BrowserFamilyId, ExtensionManagerAccessor> ACCESSORS = 
        new HashMap<BrowserFamilyId, ExtensionManagerAccessor>();
    
    static {
        ACCESSORS.put( BrowserFamilyId.CHROME , new ChromeManagerAccessor());
        ACCESSORS.put( BrowserFamilyId.CHROMIUM , new ChromiumManagerAccessor());
    }
}
