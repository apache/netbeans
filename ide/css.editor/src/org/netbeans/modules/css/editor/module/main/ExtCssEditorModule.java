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
package org.netbeans.modules.css.editor.module.main;

import java.util.Collection;
import java.util.Map;
import org.netbeans.modules.css.editor.module.spi.CssEditorModule;
import org.netbeans.modules.css.editor.module.spi.Utilities;
import org.netbeans.modules.css.lib.api.CssModule;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
public abstract class ExtCssEditorModule extends CssEditorModule {
    
    private Map<String,PropertyDefinition> propertyDescriptors;
     
    protected abstract String getPropertyDefinitionsResourcePath();
    
    protected abstract CssModule getCssModule();
    
    private synchronized Map<String, PropertyDefinition> getProperties() {
        if (propertyDescriptors == null) {
            propertyDescriptors = Utilities.parsePropertyDefinitionFile(getPropertyDefinitionsResourcePath(), getCssModule());
        }
        return propertyDescriptors;
    }

    @Override
    public Collection<String> getPropertyNames(FileObject file) {
        return getProperties().keySet();
    }

    @Override
    public PropertyDefinition getPropertyDefinition(String propertyName) {
        //Note: the context param is ignored by the "standard" css editor modules.
        PropertyDefinition pd = getProperties().get(propertyName);
        if (pd != null || propertyName == null) {
            return pd;
        } else {
            return getProperties().get(propertyName.toLowerCase());
        }
    }
}
