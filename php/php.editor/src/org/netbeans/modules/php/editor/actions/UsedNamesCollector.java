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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.api.AliasedName;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.UseScope;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeNode;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.UseStatement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class UsedNamesCollector {
    private final PHPParseResult parserResult;
    private final int caretPosition;
    private Map<String, List<UsedNamespaceName>> possibleNames;
    private static final List<String> SPECIAL_NAMES = new LinkedList<>();

    static {
        SPECIAL_NAMES.add("parent"); //NOI18N
        SPECIAL_NAMES.add("self"); //NOI18N
        SPECIAL_NAMES.add("static"); //NOI18N
    }

    public UsedNamesCollector(final PHPParseResult parserResult, final int caretPosition) {
        this.parserResult = parserResult;
        this.caretPosition = caretPosition;
    }

    public Map<String, List<UsedNamespaceName>> collectNames() {
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(parserResult.getModel().getFileScope(), caretPosition);
        assert namespaceScope != null;
        OffsetRange offsetRange = namespaceScope.getBlockRange();
        NamespaceNameVisitor namespaceNameVisitor = new NamespaceNameVisitor(offsetRange, namespaceScope);
        parserResult.getProgram().accept(namespaceNameVisitor);
        possibleNames = namespaceNameVisitor.getExistingNames();
        return filterNamesWithoutUses(namespaceNameVisitor.getScopeMap());
    }

    private Map<String, List<UsedNamespaceName>> filterNamesWithoutUses(final Map<String, NamespaceScope> scopeMap) {
        final Map<String, List<UsedNamespaceName>> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<UsedNamespaceName>> entry : possibleNames.entrySet()) {
            if (!existsUseForTypeName(scopeMap.get(entry.getKey()).getAllDeclaredSingleUses(), QualifiedName.create(entry.getKey()))) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    private boolean existsUseForTypeName(final Collection<? extends UseScope> declaredUses, final QualifiedName typeName) {
        boolean result = false;
        String firstSegmentName = typeName.getSegments().getFirst();
        for (UseScope useElement : declaredUses) {
            AliasedName aliasName = useElement.getAliasedName();
            if (aliasName != null) {
                if (firstSegmentName.equals(aliasName.getAliasName())) {
                    result = true;
                    break;
                }
            } else {
                // GH-5330
                // do not check the end string of the declared name
                // check whether segment is the same name
                // e.g. OtherSameNamePart and SameNamePart are end with "SameNamePart"
                QualifiedName declaredName = QualifiedName.create(useElement.getName());
                if (declaredName.getSegments().getLast().equals(firstSegmentName)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    private static class NamespaceNameVisitor extends DefaultVisitor {
        private final OffsetRange offsetRange;
        private final Map<String, NamespaceScope> scopeMap = new HashMap<>();
        private final Map<String, List<UsedNamespaceName>> existingNames = new LinkedHashMap<>();
        private final NamespaceScope namespaceScope;
        private NamespaceScope currentScope;

        public NamespaceNameVisitor(OffsetRange offsetRange, NamespaceScope namespaceScope) {
            this.offsetRange = offsetRange;
            this.namespaceScope = namespaceScope;
        }

        @Override
        public void scan(ASTNode node) {
            if (isNodeForScan(node)) {
                super.scan(node);
            }
        }

        private boolean isNodeForScan(final ASTNode node) {
            return node != null && isInNamespace(node) && !(node instanceof UseStatement);
        }

        private boolean isInNamespace(ASTNode node) {
            return offsetRange.containsInclusive(node.getStartOffset()) || offsetRange.containsInclusive(node.getEndOffset());
        }

        @Override
        public void visit(Program node) {
            // just for safety to reset initial scope on a next run
            currentScope = namespaceScope;
            scan(node.getStatements());
            scan(node.getComments());
        }

        @Override
        public void visit(NamespaceDeclaration node) {
            if (namespaceScope.isDefaultNamespace()) {
                currentScope = ModelUtils.getNamespaceScope(namespaceScope.getFileScope(), node.getBody().getStartOffset());
            }
            scan(node.getBody());
        }

        @Override
        public void visit(NamespaceName node) {
            UsedNamespaceName usedName = new UsedNamespaceName(node, currentScope);
            if (isValidTypeName(usedName.getName())) {
                processUsedName(usedName);
            }
        }

        @Override
        public void visit(PHPDocTypeNode node) {
            UsedNamespaceName usedName = new UsedNamespaceName(node, currentScope);
            if (isValidTypeName(usedName.getName()) && isValidAliasTypeName(usedName.getName())) {
                processUsedName(usedName);
            }
        }

        private boolean isValidTypeName(final String typeName) {
            return !SPECIAL_NAMES.contains(typeName)
                    && !Type.isPrimitive(typeName)
                    && !typeName.contains("<") // NOI18N e.g. array<int, ClassName>
                    && !typeName.contains("{"); // NOI18N e.g. array{'foo': int, "bar": string}
        }

        private boolean isValidAliasTypeName(final String typeName) {
            return !SPECIAL_NAMES.contains(typeName) && !Type.isPrimitiveAlias(typeName);
        }

        private void processUsedName(final UsedNamespaceName usedName) {
            List<UsedNamespaceName> usedNames = existingNames.get(usedName.getName());
            if (usedNames == null) {
                usedNames = new LinkedList<>();
                existingNames.put(usedName.getName(), usedNames);
                scopeMap.put(usedName.getName(), usedName.getInScope());
            }
            usedNames.add(usedName);
        }

        public Map<String, List<UsedNamespaceName>> getExistingNames() {
            return Collections.unmodifiableMap(existingNames);
        }

        public Map<String, NamespaceScope> getScopeMap() {
            return Collections.unmodifiableMap(scopeMap);
        }
    }

    private static class NamespaceDeclarationVisitor extends DefaultVisitor {

        private final List<NamespaceDeclaration> globalNamespaceDeclarations = new ArrayList<>();

        public List<NamespaceDeclaration> getGlobalNamespaceDeclarations() {
            return Collections.unmodifiableList(globalNamespaceDeclarations);
        }

        @Override
        public void visit(NamespaceDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (node.isBracketed() && node.getName() == null) {
                globalNamespaceDeclarations.add(node);
            }
            super.visit(node);
        }

    }
}
