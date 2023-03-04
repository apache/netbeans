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
package org.netbeans.modules.web.jsf.icefaces;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.web.jsfapi.api.Attribute;
import org.netbeans.modules.web.jsfapi.api.Library;
import org.netbeans.modules.web.jsfapi.api.Tag;
import org.netbeans.modules.web.jsfapi.api.TagFeature;
import org.netbeans.modules.web.jsfapi.spi.TagFeatureProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 * {@link TagFeatureProvider} for IceFaces.
 *
 * @author petrpodzimek
 */
@ServiceProvider(service = TagFeatureProvider.class)
public class IceFacesTagFeatureProvider implements TagFeatureProvider {

    private static final String ICESOFT_COMPONENT_NAMESPACE = "http://www.icesoft.com/icefaces/component";
    private static final String ICEFACES_COMPONENT_NAMESPACE = "http://www.icefaces.org/icefaces/components";
    
    private static final String VALUE = "value";
    private static final String VAR = "var";

    @Override
    public <T extends TagFeature> Collection<T> getFeatures(final Tag tag, Library library, Class<T> clazz) {
        if (clazz.equals(TagFeature.IterableTagPattern.class)) {
            final IceFacesTagFeatureProvider.IterableTag iterableTag = resolveIterableTag(library, tag);
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

    private IceFacesTagFeatureProvider.IterableTag resolveIterableTag(Library library, Tag tag) {
        for (IceFacesTagFeatureProvider.IterableTag iterableTag : IceFacesTagFeatureProvider.IterableTag.values()) {
            if (library.getNamespace() != null
                    && iterableTag.getNamespace() != null
                    && library.getNamespace().equalsIgnoreCase(iterableTag.getNamespace())) {
                return iterableTag;
            }
        }
        return null;
    }

    private enum IterableTag {

        ICESOFT_COLUMNS(ICESOFT_COMPONENT_NAMESPACE, "columns", VALUE, VAR),
        ICESOFT_DATA_TABLE(ICESOFT_COMPONENT_NAMESPACE, "dataTable", VALUE, VAR),
        ICESOFT_TREE(ICESOFT_COMPONENT_NAMESPACE, "tree", VALUE, VAR),
        ICESOFT_REPEAT(ICESOFT_COMPONENT_NAMESPACE, "repeat", VALUE, VAR),
        ICEFACES_DATA_TABLE(ICEFACES_COMPONENT_NAMESPACE, "dataTable", VALUE, VAR);
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
