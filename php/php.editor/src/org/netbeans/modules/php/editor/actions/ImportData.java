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
package org.netbeans.modules.php.editor.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.swing.Icon;
import org.netbeans.modules.php.editor.api.PhpElementKind;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class ImportData {
    public volatile boolean shouldShowUsesPanel;
    public volatile int caretPosition;
    private final List<DataItem> dataItems = new ArrayList<>();
    private final List<DataItem> dataItemsToReplace = new ArrayList<>();

    public void add(DataItem item) {
        dataItems.add(item);
    }

    public void addJustToReplace(DataItem item) {
        dataItemsToReplace.add(item);
    }

    public List<DataItem> getItems() {
        return new ArrayList<>(dataItems);
    }

    public List<DataItem> getItemsToReplace() {
        return new ArrayList<>(dataItemsToReplace);
    }

    public List<ItemVariant> getDefaultVariants() {
        List<ItemVariant> result = new ArrayList<>();
        for (DataItem dataItem : dataItems) {
            result.add(dataItem.getDefaultVariant());
        }
        return result;
    }

    public static class DataItem {
        private final String typeName;
        private final List<ItemVariant> variants;
        private final ItemVariant defaultVariant;
        private final List<UsedNamespaceName> usedNamespaceNames;

        public DataItem(String typeName, List<ItemVariant> variants, ItemVariant defaultVariant) {
            this(typeName, variants, defaultVariant, Collections.EMPTY_LIST);
        }

        public DataItem(String typeName, List<ItemVariant> variants, ItemVariant defaultVariant, List<UsedNamespaceName> usedNamespaceNames) {
            this.typeName = typeName;
            this.variants = variants;
            this.defaultVariant = defaultVariant;
            this.usedNamespaceNames = usedNamespaceNames;
        }

        public String getTypeName() {
            return typeName;
        }

        public List<ItemVariant> getVariants() {
            return new ArrayList<>(variants);
        }

        public Icon[] getVariantIcons() {
            Icon[] variantIcons = new Icon[variants.size()];
            for (int i = 0; i < variants.size(); i++) {
                ItemVariant itemVariant = variants.get(i);
                variantIcons[i] = itemVariant.getIcon();
            }
            return variantIcons;
        }

        public ItemVariant getDefaultVariant() {
            return defaultVariant;
        }

        public List<UsedNamespaceName> getUsedNamespaceNames() {
            return new ArrayList<>(usedNamespaceNames);
        }

        public void addUsedNamespaceNames(List<UsedNamespaceName> usedNsNames) {
            usedNamespaceNames.addAll(usedNsNames);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + (this.typeName != null ? this.typeName.hashCode() : 0);
            hash = 37 * hash + (this.variants != null ? this.variants.hashCode() : 0);
            hash = 37 * hash + (this.defaultVariant != null ? this.defaultVariant.hashCode() : 0);
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
            if ((this.typeName == null) ? (other.typeName != null) : !this.typeName.equals(other.typeName)) {
                return false;
            }
            if (this.variants != other.variants && (this.variants == null || !this.variants.equals(other.variants))) {
                return false;
            }
            return !((this.defaultVariant == null) ? (other.defaultVariant != null) : !this.defaultVariant.equals(other.defaultVariant));
        }

    }

    public static class ItemVariant implements Comparable<ItemVariant> {

        public static enum UsagePolicy {
            CAN_BE_USED() {

                @Override
                boolean canBeUsed() {
                    return true;
                }

            },

            CAN_NOT_BE_USED() {

                @Override
                boolean canBeUsed() {
                    return false;
                }

            };

            abstract boolean canBeUsed();
        }

        public enum Type {
            CONST {

                @Override
                public Icon getIcon() {
                    return IconsUtils.getElementIcon(PhpElementKind.CONSTANT);
                }

            },
            FUNCTION {

                @Override
                public Icon getIcon() {
                    return IconsUtils.getElementIcon(PhpElementKind.FUNCTION);
                }

            },
            INTERFACE {

                @Override
                public Icon getIcon() {
                    return IconsUtils.getElementIcon(PhpElementKind.IFACE);
                }

            },
            CLASS {

                @Override
                public Icon getIcon() {
                    return IconsUtils.getElementIcon(PhpElementKind.CLASS);
                }

            },
            TRAIT {

                @Override
                public Icon getIcon() {
                    return IconsUtils.getElementIcon(PhpElementKind.TRAIT);
                }

            },
            ENUM {

                @Override
                public Icon getIcon() {
                    return IconsUtils.getElementIcon(PhpElementKind.ENUM);
                }

            },
            ERROR {

                @Override
                public Icon getIcon() {
                    return IconsUtils.getErrorGlyphIcon();
                }

            },
            NONE {

                @Override
                public Icon getIcon() {
                    return null;
                }

            };

            public abstract Icon getIcon();

            public static Type create(PhpElementKind phpElementKind) {
                Type result;
                switch (phpElementKind) {
                    case CONSTANT:
                        result = CONST;
                        break;
                    case FUNCTION:
                        result = FUNCTION;
                        break;
                    case IFACE:
                        result = INTERFACE;
                        break;
                    case CLASS:
                        result = CLASS;
                        break;
                    case TRAIT:
                        result = TRAIT;
                        break;
                    case ENUM:
                        result = ENUM;
                        break;
                    default:
                        result = ERROR;
                }
                return result;
            }
        }

        private final String name;
        private final UsagePolicy usagePolicy;
        private final Type type;
        private final boolean isFromAliasedElement;

        public ItemVariant(String name, UsagePolicy usagePolicy) {
            this(name, usagePolicy, Type.NONE, false);
        }

        public ItemVariant(String name, UsagePolicy usagePolicy, Type type, boolean isFromAliasedElement) {
            assert name != null;
            this.name = name;
            this.usagePolicy = usagePolicy;
            this.type = type;
            this.isFromAliasedElement = isFromAliasedElement;
        }

        public ItemVariant(String name, UsagePolicy usagePolicy, PhpElementKind phpElementKind, boolean isFromAliasedElement) {
            this(name, usagePolicy, Type.create(phpElementKind), isFromAliasedElement);
        }

        public String getName() {
            return name;
        }

        public Type getType() {
            return type;
        }

        public Icon getIcon() {
            return type.getIcon();
        }

        public boolean canBeUsed() {
            return usagePolicy.canBeUsed();
        }

        public boolean isFromAliasedElement() {
            return isFromAliasedElement;
        }

        @Override
        public int compareTo(ItemVariant other) {
            return getName().compareToIgnoreCase(other.getName());
        }

        @Override
        public String toString() {
            return getName();
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + Objects.hashCode(this.name);
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
            final ItemVariant other = (ItemVariant) obj;
            return Objects.equals(this.name, other.name);
        }

    }
}
