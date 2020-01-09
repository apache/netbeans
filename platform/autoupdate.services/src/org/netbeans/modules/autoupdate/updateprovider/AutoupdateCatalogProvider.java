/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.netbeans.modules.autoupdate.services.Trampoline;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.util.Parameters;

/**
 *
 * @author Jiri Rechtacek
 */
public class AutoupdateCatalogProvider implements UpdateProvider {
    private URL updateCenter;
    private final String codeName;
    private String displayName;
    private AutoupdateCatalogCache cache = AutoupdateCatalogCache.getDefault ();
    private static final Logger LOG = Logger.getLogger ("org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalog");
    private String description;
    private boolean descriptionInitialized;
    private ProviderCategory category;
    private String contentDescription;
    private boolean contentDescriptionInitialized;
    private boolean trusted;

    public AutoupdateCatalogProvider (String name, String displayName, URL updateCenter) {
        this(name, displayName, updateCenter, ProviderCategory.forValue(CATEGORY.COMMUNITY));
    }
    
    /**
     * Creates a new instance of AutoupdateCatalog
     */
    public AutoupdateCatalogProvider (String name, String displayName, URL updateCenter, ProviderCategory category) {
        Parameters.notNull("name", name);
        this.codeName = name;
        this.displayName = displayName;
        this.updateCenter = updateCenter;
        this.category = category;
    }
    public AutoupdateCatalogProvider (String name, String displayName, URL updateCenter, CATEGORY category) {
        this(name, displayName, updateCenter, ProviderCategory.forValue(category));
    }
    
    @Override
    public String getName () {
        return codeName;
    }
    
    @Override
    public String getDisplayName () {
        return displayName == null ? codeName : displayName;
    }
    
    @Override
    public String getDescription () {
        if (description == null && !descriptionInitialized) {
            try {
               getUpdateItems();
            } catch (IOException e) {
            }            
        }
        return description;
    }
    
    public void setNotification (String notification) {
        this.description = notification;
        this.descriptionInitialized = true;
    }

    public String getContentDescription () {
        if (contentDescription == null && !contentDescriptionInitialized) {
            try {
               getUpdateItems();
            } catch (IOException e) {
            }            
        }
        return contentDescription;
    }
    
    public void setContentDescription (String description) {
        this.contentDescription = description;
        this.contentDescriptionInitialized = true;
    }

    @Override
    public Map<String, UpdateItem> getUpdateItems () throws IOException {
            URL toParse = cache.getCatalogURL(codeName);
            if (toParse == null) {
                LOG.log (Level.FINE, "No content in cache for {0} provider. Returns EMPTY_MAP", codeName);
                return Collections.emptyMap ();
            }

            Map <String, UpdateItem> map;
            synchronized(cache.getLock(toParse)) {
                map = AutoupdateCatalogParser.getUpdateItems (toParse, this);
            }
            for(UpdateItem ui: map.values()) {
                UpdateItemImpl impl = Trampoline.SPI.impl(ui);
            }
            descriptionInitialized = true;
            return map;        
    }
    
    @Override
    public boolean refresh (boolean force) throws IOException {
        boolean res;
        LOG.log (Level.FINER, "Try write(force? {0}) to cache Update Provider {1} from {2}", new Object[]{force, codeName, getUpdateCenterURL ()});
        if (force) {
            res = cache.writeCatalogToCache (codeName, getUpdateCenterURL ()) != null;
            description = null;
            descriptionInitialized = false;            
        } else {
            res = true;
        }
        return res;
    }
    
    public URL getUpdateCenterURL () {
        assert updateCenter != null : "XMLCatalogUpdatesProvider " + codeName + " must have a URL to Update Center";
        return updateCenter;
    }
    
    public void setUpdateCenterURL (URL newUpdateCenter) {
        assert newUpdateCenter != null;
        updateCenter = newUpdateCenter;
    }
    
    @Override
    public String toString () {
        return displayName + "[" + codeName + "] to " + updateCenter;
    }

    @Override
    public CATEGORY getCategory() {
        return category.toEnum();
    }    
    
    public ProviderCategory getProviderCategory() {
        return category;
    }

    public boolean isTrusted() {
        return trusted;
    }

    public void setTrusted(boolean trusted) {
        this.trusted = trusted;
    }
}
