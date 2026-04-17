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
package org.netbeans.modules.web.primefaces;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.netbeans.modules.web.jsfapi.api.Attribute;
import org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo;
import org.netbeans.modules.web.jsfapi.api.Library;
import org.netbeans.modules.web.jsfapi.api.LibraryInfo;
import org.netbeans.modules.web.jsfapi.api.Tag;
import org.netbeans.modules.web.jsfapi.api.TagFeature;
import org.netbeans.modules.web.jsfapi.spi.TagFeatureProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 * {@link TagFeatureProvider} for PrimeFaces.
 * 
 * @author petrpodzimek
 */
@ServiceProvider(service = TagFeatureProvider.class)
public class PrimeFacesTagFeatureProvider implements TagFeatureProvider {

    private static final String VALUE = "value";
    private static final String VAR = "var";

    private static final Map<LibraryInfo, Map<String, IterableTag>> TAGS_MAP;

    static {
        TAGS_MAP = Stream.of(IterableTag.values())
                .collect(Collectors.groupingBy(IterableTag::getLibraryInfo,
                        Collectors.toMap(IterableTag::getName, Function.identity())));
    }

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

    private PrimeFacesTagFeatureProvider.IterableTag resolveIterableTag(Library library, Tag tag) {
        String libraryNamespace = library.getNamespace();
        if (libraryNamespace == null) {
            return null;
        }
        LibraryInfo libraryInfo = DefaultLibraryInfo.forNamespace(libraryNamespace);
        if (libraryInfo == null) {
            return null;
        }
        return Optional.ofNullable(TAGS_MAP.get(libraryInfo)).map(subMap -> subMap.get(tag.getName())).orElse(null);
    }

    private enum IterableTag {

        ACCORDION_PANEL(DefaultLibraryInfo.PRIMEFACES, "accordionPanel", VALUE, VAR),
        CAROUSEL(DefaultLibraryInfo.PRIMEFACES, "carousel", VALUE, VAR),
        CHRONOLINE(DefaultLibraryInfo.PRIMEFACES, "chronoline", VALUE, VAR),
        COLUMNS(DefaultLibraryInfo.PRIMEFACES, "columns", VALUE, VAR),
        DATA_GRID(DefaultLibraryInfo.PRIMEFACES, "dataGrid", VALUE, VAR),
        DATA_LIST(DefaultLibraryInfo.PRIMEFACES, "dataList", VALUE, VAR),
        DATA_SCROLLER(DefaultLibraryInfo.PRIMEFACES, "dataScroller", VALUE, VAR),
        DATA_TABLE(DefaultLibraryInfo.PRIMEFACES, "dataTable", VALUE, VAR),
        DATA_VIEW(DefaultLibraryInfo.PRIMEFACES, "dataView", VALUE, VAR),
        FEED_READER(DefaultLibraryInfo.PRIMEFACES, "feedReader", VALUE, VAR),
        GALLERIA(DefaultLibraryInfo.PRIMEFACES, "galleria", VALUE, VAR),
        ORDER_LIST(DefaultLibraryInfo.PRIMEFACES, "orderList", VALUE, VAR),
        PICK_LIST(DefaultLibraryInfo.PRIMEFACES, "pickList", VALUE, VAR),
        RING(DefaultLibraryInfo.PRIMEFACES, "ring", VALUE, VAR),
        SHEET(DefaultLibraryInfo.PRIMEFACES, "sheet", VALUE, VAR),
        SUB_TABLE(DefaultLibraryInfo.PRIMEFACES, "subTable", VALUE, VAR),
        TAB_VIEW(DefaultLibraryInfo.PRIMEFACES, "tabView", VALUE, VAR),
        TREE(DefaultLibraryInfo.PRIMEFACES, "tree", VALUE, VAR),
        TREE_TABLE(DefaultLibraryInfo.PRIMEFACES, "treeTable", VALUE, VAR),
        // these come from primefaces extensions
        PE_FLUID_GRID(DefaultLibraryInfo.PRIMEFACES_EXTENSIONS, "fluidGrid", VALUE, VAR),
        PE_SHEET(DefaultLibraryInfo.PRIMEFACES_EXTENSIONS, "sheet", VALUE, VAR);

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
