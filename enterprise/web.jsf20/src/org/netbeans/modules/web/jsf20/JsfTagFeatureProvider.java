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
package org.netbeans.modules.web.jsf20;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.web.jsfapi.api.Attribute;
import org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo;
import org.netbeans.modules.web.jsfapi.api.Library;
import org.netbeans.modules.web.jsfapi.api.Tag;
import org.netbeans.modules.web.jsfapi.api.TagFeature;
import org.netbeans.modules.web.jsfapi.spi.TagFeatureProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 * {@link TagFeatureProvider} for JSF/Jakarta Faces.
 *
 * @author petrpodzimek
 */
@ServiceProvider(service = TagFeatureProvider.class)
public class JsfTagFeatureProvider implements TagFeatureProvider {

    private static final String VAR = "var";        //NOI18N
    private static final String VALUE = "value";    //NOI18N

    @Override
    public <T extends TagFeature> Collection<T> getFeatures(final Tag tag, Library library, Class<T> clazz) {
        if (clazz.equals(TagFeature.IterableTagPattern.class)) {
            final IterableTag iterableTag = resolveIterableTag(library, tag);
            if (iterableTag != null) {
                return Collections.singleton(clazz.cast(new TagFeature.IterableTagPattern() {
                    @Override
                    public Attribute getVariable() {
                        return tag.getAttribute(iterableTag.getVariableAttribute());
                    }

                    @Override
                    public Attribute getItems() {
                        return tag.getAttribute(iterableTag.getItemsAttribute());
                    }
                }));
            }

        }
        return Collections.emptyList();
    }

    private IterableTag resolveIterableTag(Library library, Tag tag) {
        String libraryNamespace = library.getNamespace();
        if (libraryNamespace == null) {
            return null;
        }
        for (IterableTag iterableTag : IterableTag.values()) {
            if (iterableTag.getName().equals(tag.getName())
                    && libraryNamespace.equals(iterableTag.getLibraryInfo().getNamespace())) {
                return iterableTag;
            }
        }
        return null;
    }

    private enum IterableTag {

        FOR_EACH(DefaultLibraryInfo.JSTL_CORE, "forEach", "items", VAR),         //NOI18N
        SELECT_ITEMS(DefaultLibraryInfo.JSF_CORE, "selectItems", VALUE, VAR),    //NOI18N
        SELECT_ITEMS_GROUPS(DefaultLibraryInfo.JSF_CORE, "selectItemGroups", VALUE, VAR), //NOI18N
        DATA_TABLE(DefaultLibraryInfo.HTML, "dataTable", VALUE, VAR),            //NOI18N
        REPEAT(DefaultLibraryInfo.FACELETS, "repeat", VALUE, VAR);               //NOI18N

        private final DefaultLibraryInfo libraryInfo;
        private final String name;
        private final String itemsAttribute;
        private final String variableAttribute;

        private IterableTag(DefaultLibraryInfo libraryInfo, String name, String itemsAttribute, String variableAttribute) {
            this.libraryInfo = libraryInfo;
            this.name = name;
            this.itemsAttribute = itemsAttribute;
            this.variableAttribute = variableAttribute;
        }

        public DefaultLibraryInfo getLibraryInfo() {
            return libraryInfo;
        }

        public String getName() {
            return name;
        }

        public String getItemsAttribute() {
            return itemsAttribute;
        }

        public String getVariableAttribute() {
            return variableAttribute;
        }
    }
}
