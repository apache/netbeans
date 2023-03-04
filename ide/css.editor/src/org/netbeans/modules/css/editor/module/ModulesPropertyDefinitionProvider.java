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
package org.netbeans.modules.css.editor.module;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.css.editor.module.spi.CssEditorModule;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinitionProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author marekfukala
 */
@ServiceProvider(service=PropertyDefinitionProvider.class)
public class ModulesPropertyDefinitionProvider implements PropertyDefinitionProvider {

    @Override
    public Collection<String> getPropertyNames(FileObject context) {
        Collection<String> all = new ArrayList<>();
        for (CssEditorModule module : CssModuleSupport.getModules()) {
            all.addAll(module.getPropertyNames(context));
        }
        return all;
    }

    @Override
    public PropertyDefinition getPropertyDefinition(String propertyName) {
        for (CssEditorModule module : CssModuleSupport.getModules()) {
            PropertyDefinition def = module.getPropertyDefinition(propertyName);
            if(def != null) {
                return def;
            }
        }
        return null;
    }
    
}
