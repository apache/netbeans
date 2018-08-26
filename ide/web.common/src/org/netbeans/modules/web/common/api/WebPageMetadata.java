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

package org.netbeans.modules.web.common.api;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.netbeans.modules.web.common.spi.WebPageMetadataProvider;
import org.openide.util.Lookup;

/**
 *
 * @author marekfukala
 */
public final class WebPageMetadata {

    public static final String MIMETYPE = "mimeType"; //NOI18N

    /**
     * Returns metadata map merged from all WebPageMetadataProvider-s
     *
     * @param lookup lookup context objects here
     * @return may return null if there is not a single provider passing metadata for the page
     */
    public static WebPageMetadata getMetadata(Lookup lookup) {
        Map<String, Object> mergedMap = null;
        Collection<? extends WebPageMetadataProvider> providers = Lookup.getDefault().lookupAll(WebPageMetadataProvider.class);
        for(WebPageMetadataProvider provider : providers) {
            Map<String, ? extends Object> metamap = provider.getMetadataMap(lookup);
            if(metamap != null) {
                if(mergedMap == null) {
                    mergedMap = new TreeMap<String, Object>();
                }
                mergedMap.putAll(metamap);
            }
        }
        
        return mergedMap != null ? new WebPageMetadata(mergedMap) : null;
    }

    private Map<String, ? extends Object> metamap;

    public WebPageMetadata(Map<String, ? extends Object> metamap) {
        this.metamap = metamap;
    }


    public Collection<String> keys() {
        return metamap.keySet();
    }

    public Object value(String key) {
        return metamap.get(key);
    }

}
