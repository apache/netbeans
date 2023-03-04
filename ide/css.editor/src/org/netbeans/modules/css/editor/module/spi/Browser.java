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
package org.netbeans.modules.css.editor.module.spi;

import java.net.URL;
import org.netbeans.modules.css.lib.api.properties.PropertyCategory;

/**
 * Represents a browser 
 * 
 * @author mfukala@netbeans.org
 */
public abstract class Browser {

    /**
     * Returns a {@link PropertyCategory} of the vendor specific properties.
     * @return 
     */
    public abstract PropertyCategory getPropertyCategory();
        
    /**
     * 
     * @return name of the browsers vendor (Mozilla, Microsoft, ...)
     */
    public abstract String getVendor();

    /**
     * 
     * @return name of the browser (Firefox, Chrome, ...)
     */
    public abstract String getName();
    
    /**
     * 
     * @return brief browser description. 
     */
    public abstract String getDescription();

    /**
     * 
     * @return name of the rendering engive along with its version (gecko 5.0,...)
     */
    public abstract String getRenderingEngineId();
    
    /**
     * 
     * @return a vendor specific property prefix, (moz, o, ...)
     */
    public abstract String getVendorSpecificPropertyId();
    
    /**
     * 
     * @return the vendor specific property prefix with the added dashes as
     * it appears in the css notation: -moz-, -o-, -webkit-, ...
     */
    public final String getVendorSpecificPropertyPrefix() {
        return new StringBuilder().append('-').append(getVendorSpecificPropertyId()).append('-').toString();
    }
    
    /**
     * Return a small icon (16-20px) representing the browser.
     * The icon is used to represent the browser in some user UIs.
     * 
     */
    public abstract URL getActiveIcon();
    
    /**
     * Return a small icon (16-20px) representing the browser in an inactive
     * state. The icon should be in gray color preferably.
     * 
     * The icon is used to represent the browser in some user UIs.
     * 
     */
    public abstract URL getInactiveIcon();

}
