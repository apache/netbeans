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

package org.netbeans.modules.project.uiapi;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.api.templates.CreateDescriptor;
import org.netbeans.api.templates.CreateFromTemplateAttributes;
import org.netbeans.api.templates.FileBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbCollections;

/**
 * Provides attributes that can be used inside scripting templates. It delegates
 * attributes query to providers registered in project lookups.
 *
 * @author Jan Pokorsky
 */
@org.openide.util.lookup.ServiceProvider(service=CreateFromTemplateAttributes.class)
public final class ProjectTemplateAttributesProvider implements CreateFromTemplateAttributes {
    
    private static final String ATTR_PROJECT = "project"; // NOI18N
    private static final String ATTR_LICENSE = "license"; // NOI18N
    private static final String ATTR_LICENSE_PATH = "licensePath"; // NOI18N
    private static final String ATTR_ENCODING = "encoding"; // NOI18N

    @Override
    public Map<String, ?> attributesFor(CreateDescriptor desc) {
        FileObject templateF = desc.getTemplate();
        FileObject targetF = desc.getTarget();
        String name = desc.getProposedName();
        Project prj = FileOwnerQuery.getOwner(targetF);
        Map<String, Object> all = new HashMap<>();
        boolean needFill = true;
        if (prj != null) {
            // call old providers
            needFill = desc.getValue(ProjectTemplateAttributesLegacy.class.getName()) == null;
            if (needFill) {
                Collection<? extends CreateFromTemplateAttributesProvider> oldProvs = prj.getLookup().lookupAll(CreateFromTemplateAttributesProvider.class);
                if (!oldProvs.isEmpty()) {
                    try {
                        DataObject t = DataObject.find(targetF);
                        if (t instanceof DataFolder) {
                            DataFolder target = (DataFolder)t;
                            DataObject template = DataObject.find(templateF);
                            for (CreateFromTemplateAttributesProvider attrs : oldProvs) {
                                Map<String, ? extends Object> m = attrs.attributesFor(template, target, name);
                                if (m != null) {
                                    all.putAll(m);
                                }
                            }
                        }
                    } catch (IOException ex) {
                        // an unexpected error
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            // call new providers last, so they can override anything old providers could screw up.
            // new providers should get all attributes collected from previous (new-style) CFTAs incl. attributes provided
            // by [deprecated] CFTAPs accumulated above.
            FileBuilder bld = FileBuilder.fromDescriptor(desc);
            // temporary:
            for (CreateFromTemplateAttributes attrs : prj.getLookup().lookupAll(CreateFromTemplateAttributes.class)) {
                CreateDescriptor childDesc = bld.withParameters(all).createDescriptor(false);
                Map<String, ? extends Object> m = attrs.attributesFor(childDesc);
                if (m != null) {
                    if (m.containsKey(ATTR_PROJECT)) {
                        needFill = true;
                    }
                    all.putAll(m);
                }
            }
        }
        if (needFill) {
            Map<String, Object> check = new HashMap<String, Object>(desc.getParameters());
            check.putAll(all);
            return checkProjectAttrs(check, all, targetF);
        } else {
            // ProjectTemplateAttributesLegacy already run, so project properties are filled in.
            return all;
        }
    }
    
    static Map<String, ? extends Object> checkProjectAttrs(Map<String, Object>  check, Map<String, Object> m, FileObject parent) {
        Object prjAttrObj = check != null ? check.get(ATTR_PROJECT) : null;
        if (prjAttrObj instanceof Map) {
            Map<String, Object> prjAttrs = NbCollections.checkedMapByFilter((Map) prjAttrObj, String.class, Object.class, false);
            Map<String, Object> newPrjAttrs = new HashMap<String, Object>(prjAttrs);
            m.put(ATTR_PROJECT, newPrjAttrs);
            ensureProjectAttrs(newPrjAttrs, parent);
            return m;
        }
        if (prjAttrObj != null) {
            // What can we do?
            return m;
        }
        Map<String, Object> projectMap = new HashMap<String, Object>();
        ensureProjectAttrs(projectMap, parent);
        if (m != null) {
            m.put(ATTR_PROJECT, projectMap); // NOI18N
            return m;
        }
        return Collections.singletonMap(ATTR_PROJECT, projectMap);
    }
    
    private static void ensureProjectAttrs(Map<String, Object> map, FileObject parent) {
        if (map.get(ATTR_LICENSE) == null) {
            map.put(ATTR_LICENSE, "default"); // NOI18N
        }
        if (map.get(ATTR_LICENSE_PATH) == null) {
            map.put(ATTR_LICENSE_PATH, "Templates/Licenses/license-" + map.get(ATTR_LICENSE).toString() + ".txt"); // NOI18N
        }
        String url = map.get(ATTR_LICENSE_PATH).toString();
        if (FileUtil.getConfigFile(url) == null) { //now we have filesystem based template for sure, convert to file:///path to have freemarker process it
            try {
                URI uri = URI.create(url);
                //freemarker.cache.TemplateCache.normalizeName appears to 
                // check for :// to skip processing the path
                map.put(ATTR_LICENSE_PATH, new URI("file", "", uri.getPath(), null).toString());
            } catch (Exception malformedURLException) {
            }
        } else {
            // now we have to assume we are dealing with the teplate from system filesystem.
            // in order to get through the freemarker, the path needs to "absolute" in freemarker terms - http://freemarker.sourceforge.net/docs/ref_directive_include.html
            // relative would mean relative to the template and we cannot be sure what the path from template to license template is..
            // it used to be, ../Licenses/ or ../../Licenses but can be anything similar, just based on where the template resides.
            map.put(ATTR_LICENSE_PATH, "/" + url);
            //appears to cover both the new and old default value of the include path
        }  
        if (map.get(ATTR_ENCODING) == null) {
            Charset charset = FileEncodingQuery.getEncoding(parent);
            String encoding = charset != null ? charset.name() : "UTF-8"; // NOI18N
            map.put(ATTR_ENCODING, encoding);
        }
    }
}
