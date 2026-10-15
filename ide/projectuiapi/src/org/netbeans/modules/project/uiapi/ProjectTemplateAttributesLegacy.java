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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 * Provides attributes that can be used inside scripting templates. It delegates
 * attributes query to providers registered in project lookups.
 *
 * @author Jan Pokorsky
 */
@org.openide.util.lookup.ServiceProvider(service=CreateFromTemplateAttributesProvider.class)
public final class ProjectTemplateAttributesLegacy implements CreateFromTemplateAttributesProvider {

    @Override
    public Map<String, ?> attributesFor(DataObject template, DataFolder target, String name) {
        FileObject targetF = target.getPrimaryFile();
        Project prj = FileOwnerQuery.getOwner(targetF);
        Map<String, Object> all = new HashMap<>();
        if (prj != null) {
            // call old providers
            Collection<? extends CreateFromTemplateAttributesProvider> oldProvs = prj.getLookup().lookupAll(CreateFromTemplateAttributesProvider.class);
            if (!oldProvs.isEmpty()) {
                for (CreateFromTemplateAttributesProvider attrs : oldProvs) {
                    Map<String, ? extends Object> m = attrs.attributesFor(template, target, name);
                    if (m != null) {
                        all.putAll(m);
                    }
                }
            }
        }
        all.put(ProjectTemplateAttributesLegacy.class.getName(), Boolean.TRUE);
        return checkProjectAttrs(all, targetF);
    }
    
    static Map<String, ? extends Object> checkProjectAttrs(Map<String, Object> m, FileObject parent) {
        return ProjectTemplateAttributesProvider.checkProjectAttrs(m, m, parent);
    }
    
}
