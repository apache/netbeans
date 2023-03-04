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

package org.netbeans.modules.web.jsf.editor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.common.api.WebPageMetadata;
import org.netbeans.modules.web.common.spi.WebPageMetadataProvider;
import org.netbeans.modules.web.jsfapi.api.JsfSupport;
import org.netbeans.modules.web.jsfapi.api.JsfUtils;
import org.netbeans.modules.web.jsfapi.spi.JsfSupportProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author marekfukala
 */
@ServiceProvider(service=WebPageMetadataProvider.class)
public class JsfPageMetadataProvider implements WebPageMetadataProvider {

    static final String JSF_LIBRARIES_KEY = "jsfLibraries"; //NOI18N
    
    @Override
    public Map<String, ? extends Object> getMetadataMap(Lookup lookup) {
        SyntaxAnalyzerResult result = lookup.lookup(SyntaxAnalyzerResult.class);
        if(result == null) {
            return null;
        }
        JsfSupport jsfs = null;
        FileObject file = result.getSource().getSourceFileObject();
        if(file != null) {
            if(org.netbeans.modules.web.jsf.editor.JsfUtils.XHTML_MIMETYPE.equals(file.getMIMEType())) {
                //do not bother with .html and other files
                jsfs = JsfSupportProvider.get(file);
            }
        } else {
            //may be fileless source, try snapshot
            Snapshot snapshot = result.getSource().getSnapshot();
            if(snapshot != null) {
                jsfs = JsfSupportProvider.get(snapshot.getSource());
            }
        }

        if(jsfs == null) {
            return null;
        }

        Collection<String> namespaces = result.getAllDeclaredNamespaces().keySet();
        Collection<String> jsfNamespaces = new HashSet<>();
        for(String ns : namespaces) {
            if(jsfs.getLibrary(ns) != null) {
                jsfNamespaces.add(ns);
            }
        }

        if(jsfNamespaces.isEmpty()) {
            return null; //no jsf content
        }

        Map<String, Object> resultMap = new TreeMap<>();
        resultMap.put(JSF_LIBRARIES_KEY, jsfNamespaces);
        resultMap.put(WebPageMetadata.MIMETYPE, JsfUtils.JSF_XHTML_FILE_MIMETYPE);
        
        return resultMap;
    }

}
