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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.autoupdate.updateprovider;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
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
}
