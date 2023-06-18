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
package org.netbeans.modules.web.jsf.editor.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.web.jsfapi.api.JsfVersion;
import org.netbeans.modules.web.jsfapi.api.Library;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ImportData {

    public volatile boolean shouldShowNamespacesPanel;
    public volatile JsfVersion jsfVersion;

    private final List<DataItem> dataItems = new ArrayList<>();
    private final List<DataItem> dataItemsToReplace = new ArrayList<>();
    private final List<Attribute> dataToRemove = new ArrayList<>();


    public void add(DataItem item) {
        dataItems.add(item);
    }

    public void addToReplace(DataItem item) {
        dataItemsToReplace.add(item);
    }

    public void addToRemove(Attribute item) {
        dataToRemove.add(item);
    }

    public List<DataItem> getItems() {
        return new ArrayList<>(dataItems);
    }

    public List<DataItem> getItemsToReplace() {
        return new ArrayList<>(dataItemsToReplace);
    }

    public List<Attribute> getItemsToRemove() {
        return new ArrayList<>(dataToRemove);
    }

    public List<VariantItem> getDefaultVariants() {
        List<VariantItem> result = new ArrayList<>();
        for (DataItem dataItem : dataItems) {
            result.add(dataItem.getDefaultVariant());
        }
        return result;
    }

    public static class DataItem {

        private final String prefix;
        private final List<VariantItem> variants;
        private final VariantItem defaultVariant;

        public DataItem(String prefix, List<VariantItem> variants, VariantItem defaultVariant) {
            this.prefix = prefix;
            this.variants = variants;
            this.defaultVariant = defaultVariant;
        }

        public String getTypeName() {
            return prefix;
        }

        public List<VariantItem> getVariants() {
            return new ArrayList<>(variants);
        }

        public VariantItem getDefaultVariant() {
            return defaultVariant;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 97 * hash + Objects.hashCode(this.prefix);
            hash = 97 * hash + Objects.hashCode(this.variants);
            hash = 97 * hash + Objects.hashCode(this.defaultVariant);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DataItem other = (DataItem) obj;
            if (!Objects.equals(this.prefix, other.prefix)) {
                return false;
            }
            if (!Objects.equals(this.variants, other.variants)) {
                return false;
            }
            if (!Objects.equals(this.defaultVariant, other.defaultVariant)) {
                return false;
            }
            return true;
        }
    }

    public static class VariantItem {

        private final String prefix;
        private final String namespace;
        private final Library library;

        public VariantItem(String prefix, String namespace, Library library) {
            this.prefix = prefix;
            this.namespace = namespace;
            this.library = library;
        }

        public String getPrefix() {
            return prefix;
        }

        public String getNamespace() {
            return namespace;
        }

        public Library getLibrary() {
            return library;
        }

        @Override
        public String toString() {
            return getNamespace();
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 67 * hash + Objects.hashCode(this.prefix);
            hash = 67 * hash + Objects.hashCode(this.namespace);
            hash = 67 * hash + Objects.hashCode(this.library);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final VariantItem other = (VariantItem) obj;
            if (!Objects.equals(this.prefix, other.prefix)) {
                return false;
            }
            if (!Objects.equals(this.namespace, other.namespace)) {
                return false;
            }
            if (!Objects.equals(this.library, other.library)) {
                return false;
            }
            return true;
        }

    }
}
