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
package org.netbeans.lib.html.lexer;

import java.util.Collection;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.html.lexer.HtmlLexerPlugin;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author marekfukala
 */
public class HtmlPlugins {
    
    private static HtmlPlugins DEFAULT;

    public static synchronized HtmlPlugins getDefault() {
        if(DEFAULT == null) {
            DEFAULT = new HtmlPlugins();
        }
        return DEFAULT;
    }
    
    private Lookup.Result<HtmlLexerPlugin> lookupResult;
    private Collection<? extends HtmlLexerPlugin> plugins;
    private String[][] data;
    
    private HtmlPlugins() {
        Lookup lookup = MimeLookup.getLookup("text/html");
        lookupResult = lookup.lookupResult(HtmlLexerPlugin.class);
        lookupResult.addLookupListener(new LookupListener() {

            @Override
            public void resultChanged(LookupEvent ev) {
                refresh();
            }
        });
        
        refresh();
    }
    
    private void refresh() {
        Collection<? extends HtmlLexerPlugin> allInstances = lookupResult.allInstances();
        plugins = allInstances;
        data = new String[3][allInstances.size()];
        int idx = 0;
        for(HtmlLexerPlugin fact : allInstances) {
            data[0][idx] = fact.getOpenDelimiter();
            data[1][idx] = fact.getCloseDelimiter();
            data[2][idx] = fact.getContentMimeType();
            idx++;
        }
    }
    
    public String[] getOpenDelimiters() {
        return data[0];
    }
    
    public String[] getCloseDelimiters() {
        return data[1];
    }
    
    public String[] getMimeTypes() {
        return data[2];
    }
    
    public String createAttributeEmbedding(String elementName, String attributeName) {
        for (HtmlLexerPlugin plugin : plugins) {
            String embeddingMimeType = plugin.createAttributeEmbedding(elementName, attributeName);
            if(embeddingMimeType != null) {
                return embeddingMimeType;
            }
        }
        return null;
    }
}
