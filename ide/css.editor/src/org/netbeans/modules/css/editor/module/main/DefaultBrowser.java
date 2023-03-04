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
package org.netbeans.modules.css.editor.module.main;

import java.net.URL;
import org.netbeans.modules.css.editor.module.spi.Browser;
import org.netbeans.modules.css.lib.api.properties.PropertyCategory;

/**
 *
 * @author mfukala@netbeans.org
 */
public class DefaultBrowser extends Browser {

    private static final String DEFAULT_ICONS_LOCATION = "/org/netbeans/modules/css/resources/icons/"; //NOI18N
    
    private String iconBase;
    private String name, vendor, vendorSpecificPropertyId, renderingEngineId;
    private URL active, inactive;
    private PropertyCategory propertyCategory;

    public DefaultBrowser(String name, String vendor, String renderingEngineId, String vendorSpecificPropertyPrefix, String iconBase, PropertyCategory propertyCategory) {
        this.name = name;
        this.vendor = vendor;
        this.renderingEngineId = renderingEngineId;
        this.vendorSpecificPropertyId = vendorSpecificPropertyPrefix;
        this.iconBase = iconBase;
        this.propertyCategory = propertyCategory;
    }

    @Override
    public PropertyCategory getPropertyCategory() {
        return propertyCategory;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVendor() {
        return vendor;
    }

    @Override
    public synchronized URL getActiveIcon() {
        if(active == null) {
            active = DefaultBrowser.class.getResource(
                DEFAULT_ICONS_LOCATION + iconBase + ".png"); //NOI18N
        }
        return active;
    }

    @Override
    public synchronized URL getInactiveIcon() {
        if(inactive == null) {
            inactive = DefaultBrowser.class.getResource(
                DEFAULT_ICONS_LOCATION + iconBase + "-disabled.png"); //NOI18N
        }
        return inactive;
    }

    @Override
    public String getDescription() {
        return new StringBuilder().append(getVendor()).append(' ').append(getName()).toString();
    }

    @Override
    public String getVendorSpecificPropertyId() {
        return vendorSpecificPropertyId;
    }

    @Override
    public String getRenderingEngineId() {
        return renderingEngineId;
    }
}
