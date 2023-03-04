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

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.css.editor.module.spi.CssEditorModule;
import org.netbeans.modules.css.lib.api.CssColor;
import org.netbeans.modules.css.lib.api.CssModule;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * The colors module functionality is partially implemented in the DefaultCssModule
 * from historical reasons. Newly added features are implemented here.
 *
 * @author mfukala@netbeans.org
 */
@ServiceProvider(service = CssEditorModule.class)
public class ColorsModule extends ExtCssEditorModule implements CssModule {

    private static final String PROPERTY_DEFINITIONS_PATH = "org/netbeans/modules/css/editor/module/main/properties/colors"; //NOI18N    
    
    private static final String COLOR_LIST_PROPERTY_NAME = "@colors-list"; //NOI18N
    private final PropertyDefinition colorsListPropertyDescriptor = new PropertyDefinition(
            COLOR_LIST_PROPERTY_NAME,
            generateColorsList(), this);

    private String generateColorsList() {
        StringBuilder sb = new StringBuilder();
        CssColor[] vals = CssColor.values();
        for (int i = 0; i < vals.length; i++) {
            sb.append(' ');
            sb.append(vals[i]);
            if (i < vals.length - 1) {
                sb.append(" |"); //NOI18N
            }
        }
        return sb.toString();
    }

    @Override
    public String getName() {
        return "color"; //NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(this.getClass(), Constants.CSS_MODULE_DISPLAYNAME_BUNDLE_KEY_PREFIX + getName());
    }

    @Override
    public String getSpecificationURL() {
        return "http://www.w3.org/TR/css3-color"; //NOI18N
    }

    @Override
    protected String getPropertyDefinitionsResourcePath() {
        return PROPERTY_DEFINITIONS_PATH;
    }

    @Override
    protected CssModule getCssModule() {
        return this;
    }

    @Override
    public Collection<String> getPropertyNames(FileObject file) {
        Collection<String> names = new ArrayList<>(super.getPropertyNames(file));
        names.add(COLOR_LIST_PROPERTY_NAME);
        return names;
    }

    @Override
    public PropertyDefinition getPropertyDefinition(String propertyName) {
        if(COLOR_LIST_PROPERTY_NAME.equals(propertyName)) {
            return colorsListPropertyDescriptor;
        } else {
            return super.getPropertyDefinition(propertyName);
        }
    }
    
    
}
