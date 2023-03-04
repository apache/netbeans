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

package org.netbeans.modules.web.common.api;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.web.common.spi.WebPageMetadataProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author marekfukala
 */
public final class WebPageMetadata {

    /**
     * MIME type of the webpage content.
     */
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
    
    /**
     * 
     * Returns an artificial mime type so the user can enable/disable the error checks
     * for particular content. For example the {@code text/facelets+xhtml} mime type is returned for
    .* xhtml pages with facelets content. This allows to normally verify the plain xhtml file
     * even if their mime type is {@code text/html} sure the correct solution would be to let the 
     * mime resolver to create different mime type, but since the resolution can be pretty complex it 
     * is not done this way.
     * <p>
     * (description copied from org.netbeans.modules.html.editor.api.Utils)
     * <p>
     * If the passed {@code result} does not contain relevant information, the underlying file or source's
     * MIME type is returned.
     * 
     * @param parsed parser result instance
     * @param useSnapshot if true, will default to source or file MIME type.
     * @return supplemental MIME type.
     * @since 1.109
     */
    public static String getContentMimeType(Parser.Result parsed, boolean useSnapshot) {
        FileObject fo;
        WebPageMetadata wpmeta = getMetadata(parsed);
        if (wpmeta != null) {
            //get an artificial mimetype for the web page, this doesn't have to be equal
            //to the fileObjects mimetype.
            String mimeType = (String) wpmeta.value(WebPageMetadata.MIMETYPE);
            if (mimeType != null) {
                return mimeType;
            }
        }
        if (useSnapshot) {
            fo = parsed.getSnapshot().getSource().getFileObject();
            return fo != null ?
                    fo.getMIMEType():
                   parsed.getSnapshot().getMimeType();
        } else {
            return null;
        }
    }

    static WebPageMetadata getMetadata(Parser.Result parsed) {
        InstanceContent ic = new InstanceContent();
        ic.add(parsed);
        Lookup lkp = new AbstractLookup(ic);
        if (parsed instanceof Lookup.Provider) {
            lkp = new ProxyLookup(lkp, ((Lookup.Provider)parsed).getLookup());
        }
         return getMetadata(lkp);
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
