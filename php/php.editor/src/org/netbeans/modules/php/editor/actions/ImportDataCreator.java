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
package org.netbeans.modules.php.editor.actions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.actions.FixUsesAction.Options;
import org.netbeans.modules.php.editor.actions.ImportData.DataItem;
import org.netbeans.modules.php.editor.actions.ImportData.ItemVariant;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.AliasedElement;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.ConstantElement;
import org.netbeans.modules.php.editor.api.elements.EnumElement;
import org.netbeans.modules.php.editor.api.elements.FullyQualifiedElement;
import org.netbeans.modules.php.editor.api.elements.FunctionElement;
import org.netbeans.modules.php.editor.api.elements.InterfaceElement;
import org.netbeans.modules.php.editor.api.elements.TraitElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
@NbBundle.Messages("DoNotUseType=Don't import.")
public class ImportDataCreator {
    public static final String NS_SEPARATOR = "\\"; //NOI18N
    private final Map<String, List<UsedNamespaceName>> usedNames;
    private final Index phpIndex;
    private final QualifiedName currentNamespace;
    private boolean shouldShowUsesPanel = false;
    private final Options options;
    private final List<PossibleItem> possibleItems = new ArrayList<>();

    private static Collection<FullyQualifiedElement> sortFQElements(final Collection<FullyQualifiedElement> filteredFQElements) {
        final List<FullyQualifiedElement> sortedFQElements = new ArrayList<>(filteredFQElements);
        sortedFQElements.sort(new FQElementsComparator());
        return sortedFQElements;
    }

    public ImportDataCreator(final Map<String, List<UsedNamespaceName>> usedNames, final Index phpIndex, final QualifiedName currentNamespace, final Options options) {
        this.usedNames = usedNames;
        this.phpIndex = phpIndex;
        this.currentNamespace = currentNamespace;
        this.options = options;
    }

    public ImportData create() {
        for (String fqElementName : new TreeSet<>(usedNames.keySet())) {
            processFQElementName(fqElementName);
        }
        ImportData data = new ImportData();
        for (PossibleItem possibleItem : possibleItems) {
            possibleItem.insertData(data);
        }
        data.shouldShowUsesPanel = shouldShowUsesPanel;
        return data;
    }

    private void processFQElementName(final String fqElementName) {
        // GH-6039: avoid getting all types
        if (!StringUtils.hasText(fqElementName)) {
            return;
        }
        // GH-6075
        String fqeName = CodeUtils.removeNullableTypePrefix(fqElementName);
        Collection<FullyQualifiedElement> possibleFQElements = fetchPossibleFQElements(fqeName);
        Collection<FullyQualifiedElement> filteredPlatformConstsAndFunctions = filterPlatformConstsAndFunctions(possibleFQElements);
        Collection<FullyQualifiedElement> filteredDuplicates = filterDuplicates(filteredPlatformConstsAndFunctions);
        Collection<FullyQualifiedElement> filteredExactUnqualifiedNames = filterExactUnqualifiedName(filteredDuplicates, fqeName);
        if (filteredExactUnqualifiedNames.isEmpty()) {
            if (options.getPhpVersion().compareTo(PhpVersion.PHP_56) >= 0) {
                possibleItems.add(new EmptyItem(fqeName));
            } else {
                if (!isConstOrFunction(fqeName)) {
                    possibleItems.add(new EmptyItem(fqeName));
                }
            }
        } else {
            Collection<FullyQualifiedElement> filteredFQElements = filterFQElementsFromCurrentNamespace(filteredExactUnqualifiedNames);
            if (filteredFQElements.isEmpty()) {
                possibleItems.add(new ReplaceItem(fqeName, filteredExactUnqualifiedNames));
            } else {
                possibleItems.add(new ValidItem(
                        fqeName,
                        filteredFQElements,
                        filteredFQElements.size() != filteredExactUnqualifiedNames.size()));
            }
        }
    }

    private boolean isConstOrFunction(String typeName) {
        boolean result = false;
        Collection<FunctionElement> possibleFunctions = phpIndex.getFunctions(NameKind.prefix(typeName));
        if (possibleFunctions != null && !possibleFunctions.isEmpty()) {
            result = true;
        } else {
            Collection<ConstantElement> possibleConstants = phpIndex.getConstants(NameKind.prefix(typeName));
            if (possibleConstants != null && !possibleConstants.isEmpty()) {
                result = true;
            }
        }
        return result;
    }

    private Collection<FullyQualifiedElement> fetchPossibleFQElements(final String typeName) {
        Collection<FullyQualifiedElement> possibleTypes = new HashSet<>();
        NameKind nameKind = NameKind.prefix(typeName);
        possibleTypes.addAll(phpIndex.getClasses(nameKind));
        possibleTypes.addAll(phpIndex.getInterfaces(nameKind));
        if (options.getPhpVersion().compareTo(PhpVersion.PHP_54) >= 0) {
            possibleTypes.addAll(phpIndex.getTraits(nameKind));
        }
        if (options.getPhpVersion().compareTo(PhpVersion.PHP_56) >= 0) {
            possibleTypes.addAll(phpIndex.getFunctions(nameKind));
            possibleTypes.addAll(phpIndex.getConstants(nameKind));
        }
        if (options.getPhpVersion().compareTo(PhpVersion.PHP_81) >= 0) {
            possibleTypes.addAll(phpIndex.getEnums(nameKind));
        }
        return possibleTypes;
    }

    private Collection<FullyQualifiedElement> filterDuplicates(final Collection<FullyQualifiedElement> possibleFQElements) {
        Collection<FullyQualifiedElement> result = new HashSet<>();
        Collection<String> filteredFQElements = new HashSet<>();
        for (FullyQualifiedElement fqElement : possibleFQElements) {
            String typeElementName = fqElement.toString();
            if (!filteredFQElements.contains(typeElementName)) {
                filteredFQElements.add(typeElementName);
                result.add(fqElement);
            }
        }
        return result;
    }

    private Collection<FullyQualifiedElement> filterPlatformConstsAndFunctions(final Collection<FullyQualifiedElement> possibleFQElements) {
        Collection<FullyQualifiedElement> result = new HashSet<>();
        for (FullyQualifiedElement fqElement : possibleFQElements) {
            if (fqElement instanceof ClassElement
                    || fqElement instanceof InterfaceElement
                    || fqElement instanceof TraitElement
                    || fqElement instanceof EnumElement) {
                result.add(fqElement);
            } else if (!fqElement.isPlatform()) {
                result.add(fqElement);
            }
        }
        return result;
    }

    private Collection<FullyQualifiedElement> filterExactUnqualifiedName(final Collection<FullyQualifiedElement> possibleFQElements, final String typeName) {
        Collection<FullyQualifiedElement> result = new HashSet<>();
        for (FullyQualifiedElement fqElement : possibleFQElements) {
            if (fqElement.getFullyQualifiedName().toString().endsWith(typeName)) {
                result.add(fqElement);
            }
        }
        return result;
    }

    private Collection<FullyQualifiedElement> filterFQElementsFromCurrentNamespace(final Collection<FullyQualifiedElement> possibleFQElements) {
        Collection<FullyQualifiedElement> result = new HashSet<>();
        for (FullyQualifiedElement fqElement : possibleFQElements) {
            if (!fqElement.getNamespaceName().equals(currentNamespace)) {
                result.add(fqElement);
            }
        }
        return result;
    }

    private boolean hasDefaultNamespaceName(final Collection<FullyQualifiedElement> possibleFQElements) {
        boolean result = false;
        for (FullyQualifiedElement fqElement : possibleFQElements) {
            if (fqElement.getNamespaceName().isDefaultNamespace()) {
                result = true;
                break;
            }
        }
        return result;
    }

    private interface PossibleItem {

        void insertData(ImportData data);

    }

    private static class EmptyItem implements PossibleItem {
        private final String typeName;

        public EmptyItem(String typeName) {
            this.typeName = typeName;
        }

        @Override
        @NbBundle.Messages("CanNotBeResolved=<html><font color='#FF0000'>&lt;cannot be resolved&gt;")
        public void insertData(ImportData data) {
            ItemVariant itemVariant = new ItemVariant(Bundle.CanNotBeResolved(), ItemVariant.UsagePolicy.CAN_NOT_BE_USED, ItemVariant.Type.ERROR, false);
            data.add(new DataItem(typeName, Arrays.asList(new ItemVariant[] {itemVariant}), itemVariant));
        }

    }

    private final class ReplaceItem implements PossibleItem {
        private final String fqName;
        private final Collection<FullyQualifiedElement> filteredExactUnqualifiedNames;

        public ReplaceItem(String fqName, Collection<FullyQualifiedElement> filteredExactUnqualifiedNames) {
            this.fqName = fqName;
            this.filteredExactUnqualifiedNames = filteredExactUnqualifiedNames;
        }

        @Override
        public void insertData(ImportData data) {
            FullyQualifiedElement fqElement = findBestElement();
            assert fqElement != null;
            String itemVariantReplaceName = options.preferFullyQualifiedNames()
                    ? fqElement.getFullyQualifiedName().toString()
                    : fqElement.getName();
            ItemVariant replaceItemVariant = new ItemVariant(itemVariantReplaceName, ItemVariant.UsagePolicy.CAN_BE_USED);
            assert usedNames.get(fqName) != null : "fqName: " + fqName; // NOI18N
            data.addJustToReplace(new DataItem(fqName, Collections.singletonList(replaceItemVariant), replaceItemVariant, usedNames.get(fqName)));
        }

        private FullyQualifiedElement findBestElement() {
            String fullName = '\\' + fqName; // NOI18N
            for (FullyQualifiedElement element : filteredExactUnqualifiedNames) {
                if (fullName.equals(element.getFullyQualifiedName().toString())) {
                    return element;
                }
            }
            return ModelUtils.getFirst(filteredExactUnqualifiedNames);
        }
    }

    private final class ValidItem implements PossibleItem {
        private final Collection<FullyQualifiedElement> filteredFQElements;
        private final String typeName;
        private final boolean existsFQElementFromCurrentNamespace;

        private ValidItem(String typeName, Collection<FullyQualifiedElement> filteredFQElements, boolean existsFQELEMENTFromCurrentNamespace) {
            this.typeName = typeName;
            this.filteredFQElements = filteredFQElements;
            this.existsFQElementFromCurrentNamespace = existsFQELEMENTFromCurrentNamespace;
        }

        @Override
        public void insertData(ImportData data) {
            Collection<FullyQualifiedElement> sortedFQElements = sortFQElements(filteredFQElements);
            List<ItemVariant> variants = new ArrayList<>();
            ItemVariant defaultValue = null;
            boolean isFirst = true;
            for (FullyQualifiedElement fqElement : sortedFQElements) {
                String variantName = fqElement.getFullyQualifiedName().toString();
                boolean isFromAliasedElement = false;
                if (fqElement instanceof AliasedElement) {
                    AliasedElement aliasedElement = (AliasedElement) fqElement;
                    variantName = aliasedElement.getAliasedName().getAliasName();
                    isFromAliasedElement = true;
                }
                ItemVariant itemVariant = new ItemVariant(
                        variantName,
                        ItemVariant.UsagePolicy.CAN_BE_USED,
                        fqElement.getPhpElementKind(),
                        isFromAliasedElement);
                variants.add(itemVariant);
                if (isFirst) {
                    defaultValue = itemVariant;
                    isFirst = false;
                }
                shouldShowUsesPanel = true;
            }
            ItemVariant dontUseItemVariant = new ItemVariant(Bundle.DoNotUseType(), ItemVariant.UsagePolicy.CAN_NOT_BE_USED);
            variants.add(dontUseItemVariant);
            QualifiedName qualifiedTypeName = QualifiedName.create(typeName);
            if (qualifiedTypeName.getKind().isFullyQualified()) {
                if (options.preferFullyQualifiedNames()) {
                    defaultValue = dontUseItemVariant;
                }
            } else {
                if ((currentNamespace.isDefaultNamespace() && hasDefaultNamespaceName(sortedFQElements))
                        || existsFQElementFromCurrentNamespace) {
                    defaultValue = dontUseItemVariant;
                }
            }
            Collections.sort(variants);
            assert usedNames.get(typeName) != null : "typeName: " + typeName; // NOI18N
            data.add(new DataItem(typeName, variants, defaultValue, usedNames.get(typeName)));
        }

    }

    private static class FQElementsComparator implements Comparator<FullyQualifiedElement>, Serializable {

        @Override
        public int compare(FullyQualifiedElement o1, FullyQualifiedElement o2) {
            return o1.getFullyQualifiedName().toString().compareToIgnoreCase(o2.getFullyQualifiedName().toString()) * -1;
        }

    }

}
