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
package org.netbeans.modules.templates;

import org.openide.loaders.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.templates.CreateDescriptor;
import org.openide.filesystems.FileObject;
import org.netbeans.api.templates.CreateFromTemplateAttributes;
import org.netbeans.modules.openide.loaders.DataObjectAccessor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Bridges loader-based handler registration to the fileobject-based one. Provides
 * compatibility with NB &lt;= 8.0.1. New clients are encouraged to use the new
 * {@link CreateFromTemplateAttributes} interface directly.
 * 
 * @author sdedic
 */
@ServiceProvider(service = CreateFromTemplateAttributes.class, position = Integer.MIN_VALUE + 1)
public class AttributeHandlerBridge implements CreateFromTemplateAttributes {
    private Lookup.Result<CreateFromTemplateAttributesProvider> providers;

    public AttributeHandlerBridge() {
        providers = Lookup.getDefault().lookupResult(CreateFromTemplateAttributesProvider.class);
    }
    
    @Override
    public Map<String, ?> attributesFor(CreateDescriptor desc) {
        FileObject template = desc.getTemplate();
        FileObject target = desc.getTarget();
        Collection<? extends CreateFromTemplateAttributesProvider> c = providers.allInstances();
        if (c.isEmpty()) {
            return Collections.emptyMap();
        }
        DataObject d;
        DataFolder fld;
        
        try {
            d = DataObject.find(template);
            fld = DataFolder.findFolder(target);
        } catch (DataObjectNotFoundException ex) {
            // ???
            Exceptions.printStackTrace(ex);
            return Collections.emptyMap();
        }
        HashMap<String,Object> all = new HashMap<String,Object>();
        for (CreateFromTemplateAttributesProvider p : c) {
            // must use getName, since some features may rely on that null propagates to the Provider 
            // if the initiator does not specify a name.
            Map<String, ? extends Object> map = p.attributesFor(d, fld, DataObjectAccessor.DEFAULT.getOrigName());
            if (map != null) {
                for (Map.Entry<String,? extends Object> e : map.entrySet()) {
                    all.put(e.getKey(), e.getValue());
                }
            }
        }
        
        return all;
    }
}
