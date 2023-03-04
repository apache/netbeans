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

package org.netbeans.modules.java.source.save;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.templates.CreateDescriptor;
import org.netbeans.api.templates.CreateFromTemplateAttributes;
import org.netbeans.api.templates.FileBuilder;
import org.netbeans.modules.java.source.FileObjectFromTemplateCreator;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
@ServiceProvider(service=CreateFromTemplateAttributes.class)
public class OverlayTemplateAttributesProvider implements CreateFromTemplateAttributes {

    @Override
    public Map<String, ?> attributesFor(CreateDescriptor desc) {
        FileObject origFile = (FileObject) desc.getTarget().getAttribute(FileObjectFromTemplateCreator.ATTR_ORIG_FILE);
        if (origFile == null) return null;

        Map<String, Object> all = new HashMap<>();

        FileBuilder bld = new FileBuilder(desc.getTemplate(), origFile);
        CreateDescriptor childDesc = bld.withParameters(desc.getParameters()).createDescriptor(false);
        for (CreateFromTemplateAttributes provider : Lookup.getDefault().lookupAll(CreateFromTemplateAttributes.class)) {
            Map<String, ? extends Object> map = provider.attributesFor(childDesc);
            if (map != null) {
                for (Map.Entry<String, ? extends Object> e : map.entrySet()) {
                    all.put(e.getKey(), e.getValue());
                }
            }
        }

        return all;
    }

}
