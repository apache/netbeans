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
package org.netbeans.modules.web.bootsfaces;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.web.jsfapi.api.Attribute;
import org.netbeans.modules.web.jsfapi.api.Library;
import org.netbeans.modules.web.jsfapi.api.Tag;
import org.netbeans.modules.web.jsfapi.api.TagFeature;
import org.netbeans.modules.web.jsfapi.spi.TagFeatureProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 * {@link TagFeatureProvider} for BootsFaces.
 * 
 * @author toster
 */
@ServiceProvider(service = TagFeatureProvider.class)
public class BootsFacesTagFeatureProvider implements TagFeatureProvider {

    private static final String BOOTSFACES_UI_NAMESPACE = "http://bootsfaces.net/ui"; //NOI18N

    private static final String VALUE = "value"; //NOI18N
    private static final String VAR = "var"; //NOI18N
        
    @Override
    public <T extends TagFeature> Collection<T> getFeatures(final Tag tag, Library library, Class<T> clazz) {
        if (clazz.equals(TagFeature.IterableTagPattern.class)) {
            final BootsFacesTagFeatureProvider.IterableTag iterableTag = resolveIterableTag(library, tag);
            if (iterableTag != null) {
                return Collections.singleton(clazz.cast(new TagFeature.IterableTagPattern() {
                    @Override
                    public Attribute getVariable() {
                        return tag.getAttribute(iterableTag.getVariableAtribute());
                    }

                    @Override
                    public Attribute getItems() {
                        return tag.getAttribute(iterableTag.getItemsAtribute());
                    }
                }));
            }

        }
        return Collections.emptyList();
    }

    private BootsFacesTagFeatureProvider.IterableTag resolveIterableTag(Library library, Tag tag) {
        for (BootsFacesTagFeatureProvider.IterableTag iterableTag : BootsFacesTagFeatureProvider.IterableTag.values()) {
            if (library.getNamespace() != null
                    && iterableTag.getNamespace() != null
                    && library.getNamespace().equalsIgnoreCase(iterableTag.getNamespace())) {
                return iterableTag;
            }
        }
        return null;
    }

    private enum IterableTag {

        DATA_TABLE(BOOTSFACES_UI_NAMESPACE, "dataTable", VALUE, VAR); //NOI18N

        private final String namespace;
        private final String name;
        private final String itemsAtribute;
        private final String variableAtribute;

        private IterableTag(String namespace, String name, String itemsAtribute, String variableAtribute) {
            this.namespace = namespace;
            this.name = name;
            this.itemsAtribute = itemsAtribute;
            this.variableAtribute = variableAtribute;
        }

        public String getNamespace() {
            return namespace;
        }

        public String getName() {
            return name;
        }

        public String getItemsAtribute() {
            return itemsAtribute;
        }

        public String getVariableAtribute() {
            return variableAtribute;
        }
    }
}
