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

package org.netbeans.modules.autoupdate.updateprovider;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.netbeans.modules.autoupdate.services.Utilities;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;


/** XXX <code>UpdateProvider</code> providers items for Autoupdate infrastructure. The items
 * are available on e.g. Update Center. Items can represents NetBeans Module,
 * its Localization, Feature as group of NetBeans Modules or special
 * components which needs own native installer to make them accessible in NetBeans product.
 * The infrastructure finds out <code>UpdateProvider</code> in <code>Lookup.defaultProvider()</code>,
 * the provider can be registring declaratively in XML layer.
 * Note: the former Autoupdate module allows declaration of former <code>AutoupdateType</code> on XML
 * layer, these declaration are read as new one UpdateProvider by reason of backward compatability.
 *
 * @author Jiri Rechtacek
 */
public abstract class InstalledUpdateProvider {
    protected InstalledUpdateProvider() {
    }
    
    /** Method for subclasses to provide their own list of installed modules.
     */
    protected abstract Map<String, ModuleInfo> getModuleInfos (boolean force);
    

    /** Handy method for obtaining list of installed modules.
     */
    public static Map<String, ModuleInfo> getInstalledModules () {
        return defaultProvider ().getModuleInfos (false);
    }
    
    private static InstalledUpdateProvider defaultProvider () {
        InstalledUpdateProvider iup = Lookup.getDefault().lookup(InstalledUpdateProvider.class);
        assert iup != null;
        return iup;
    }

    /** The {@link UpdateProvider} that represents the installed modules
     * as returned by {@link #getModuleInfos(boolean)}.
     */
    public static UpdateProvider getDefault() {
        return UP.DEFAULT;
    }
    
    private static final class UP implements UpdateProvider {
        static final UP DEFAULT = new UP();
        
        private UP() {
        }
        
        @Override
        public String getName () {
            return "installed-module-provider";
        }

        @Override
        public String getDisplayName () {
            return getName ();
        }

        @Override
        public String getDescription () {
            return null;
        }

        @Override
        public CATEGORY getCategory() {
            return CATEGORY.COMMUNITY;
        }


        @Override
        public Map<String, UpdateItem> getUpdateItems () throws IOException {
            Map<String, UpdateItem> res = new HashMap<String, UpdateItem> ();
            for (ModuleInfo info : defaultProvider().getModuleInfos (true).values ()) {
                Date time = null; // XXX: it's too expensive, should be extracted lazy - Utilities.readInstallTimeFromUpdateTracking (info);
                String installTime = null;
                if (time != null) {
                    installTime = Utilities.formatDate(time);
                }
                UpdateItemImpl impl = new InstalledModuleItem (
                        info.getCodeNameBase (),
                        info.getSpecificationVersion () == null ? null : info.getSpecificationVersion ().toString (),
                        info,
                        null, // XXX author
                        null, // installed cluster
                        installTime

                        );

                UpdateItem updateItem = Utilities.createUpdateItem (impl);
                res.put (info.getCodeName () + '_' + info.getSpecificationVersion (), updateItem);
            }
            return res;
        }

        @Override
        public boolean refresh (boolean force) throws IOException {
            defaultProvider().getModuleInfos(false);
            return true;
        }
    }
}
