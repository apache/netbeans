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

package org.netbeans.installer.utils.helper;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.netbeans.installer.utils.StringUtils;

/**
 *
 * @author Kirill Sorokin
 */
public class Feature {
    private String id;
    
    private long offset;
    
    private ExtendedUri iconUri;
    
    private Map<Locale, String> displayNames;
    private Map<Locale, String> descriptions;
    
    public Feature(
            final String id,
            final long offset,
            final ExtendedUri iconUri,
            final Map<Locale, String> displayNames,
            final Map<Locale, String> descriptions) {
        this.id = id;
        
        this.offset = offset;
        
        this.iconUri = iconUri;
        
        this.displayNames = new HashMap<Locale, String>();
        this.displayNames.putAll(displayNames);
        
        this.descriptions = new HashMap<Locale, String>();
        this.descriptions.putAll(descriptions);
    }
    
    public String getId() {
        return id;
    }
    
    public long getOffset() {
        return offset;
    }
    
    public ExtendedUri getIconUri() {
        return iconUri;
    }
    
    public String getDisplayName() {
        return getDisplayName(Locale.getDefault());
    }
    
    public String getDisplayName(final Locale locale) {
        return StringUtils.getLocalizedString(displayNames,locale);
    }
    
    public Map<Locale, String> getDisplayNames() {
        return displayNames;
    }
    
    public String getDescription() {
        return getDescription(Locale.getDefault());
    }
    
    public String getDescription(final Locale locale) {
        return StringUtils.getLocalizedString(descriptions,locale);
    }
    
    public Map<Locale, String> getDescriptions() {
        return descriptions;
    }
    
    public boolean equals(final Feature feature) {
        return feature != null ? id.equals(feature.getId()) : false;
    }
}
