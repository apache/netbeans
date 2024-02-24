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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.actions.FixUsesAction.Options;
import org.netbeans.modules.php.editor.actions.ImportData.ItemVariant;
import org.netbeans.modules.php.editor.api.AliasedName;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.indent.CodeStyle;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.GroupUseScope;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.UseScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.UnusedUsesCollector;
import org.netbeans.modules.php.editor.parser.UnusedUsesCollector.UnusedOffsetRanges;
import org.netbeans.modules.php.editor.parser.astnodes.DeclareStatement;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.UseStatement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class FixUsesPerformer {

    private static final Logger LOGGER = Logger.getLogger(FixUsesPerformer.class.getName());
    private static final String NEW_LINE = "\n"; //NOI18N
    private static final char SEMICOLON = ';'; //NOI18N
    private static final char SPACE = ' '; //NOI18N
    private static final String USE_KEYWORD = "use"; //NOI18N
    private static final String USE_PREFIX = NEW_LINE + USE_KEYWORD + SPACE; //NOI18N
    private static final String USE_CONST_PREFIX = NEW_LINE + USE_KEYWORD + SPACE + "const" + SPACE; //NOI18N
    private static final String USE_FUNCTION_PREFIX = NEW_LINE + USE_KEYWORD + SPACE + "function" + SPACE; //NOI18N
    private static final String AS_KEYWORD = "as"; //NOI18N
    private static final String AS_CONCAT = SPACE + AS_KEYWORD + SPACE;
    private static final String EMPTY_STRING = ""; //NOI18N
    private static final char COMMA = ','; //NOI18N
    private static final char CURLY_OPEN = '{'; //NOI18N
    private static final char CURLY_CLOSE = '}'; //NOI18N
    private static final Map<UsePart.Type, Integer> PSR12_TYPE_PRIORITIES = new HashMap<>();
    private static final Map<UsePart.Type, Integer> DEFAULT_TYPE_PRIORITIES = new HashMap<>();
    private final PHPParseResult parserResult;
    private final ImportData importData;
    private final List<ItemVariant> selections;
    private final boolean removeUnusedUses;
    private final Options options;
    private EditList editList;
    private BaseDocument baseDocument;

    static {
        DEFAULT_TYPE_PRIORITIES.put(UsePart.Type.TYPE, 0);
        DEFAULT_TYPE_PRIORITIES.put(UsePart.Type.CONST, 1);
        DEFAULT_TYPE_PRIORITIES.put(UsePart.Type.FUNCTION, 2);
        PSR12_TYPE_PRIORITIES.put(UsePart.Type.TYPE, 0);
        PSR12_TYPE_PRIORITIES.put(UsePart.Type.FUNCTION, 1);
        PSR12_TYPE_PRIORITIES.put(UsePart.Type.CONST, 2);
    }

    public FixUsesPerformer(
            final PHPParseResult parserResult,
            final ImportData importData,
            final List<ItemVariant> selections,
            final boolean removeUnusedUses,
            final Options options) {
        this.parserResult = parserResult;
        this.importData = importData;
        this.selections = selections;
        this.removeUnusedUses = removeUnusedUses;
        this.options = options;
    }

    public void perform() {
        final Document document = parserResult.getSnapshot().getSource().getDocument(false);
        if (document instanceof BaseDocument) {
            baseDocument = (BaseDocument) document;
            editList = new EditList(baseDocument);
            processSelections();
            editList.apply();
        }
    }

    @NbBundle.Messages("FixUsesPerformer.noChanges=Fix imports: No Changes")
    private void processSelections() {
        final List<ImportData.DataItem> dataItems = resolveDuplicateSelections();
        assert selections.size() <= dataItems.size()
                : "The selections size must not be larger than the dataItems size. selections size: " + selections.size() + " > dataItems size: " + dataItems.size(); // NOI18N
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(parserResult, importData.caretPosition);
        assert namespaceScope != null;
        CheckVisitor checkVisitor = new CheckVisitor();
        Program program = parserResult.getProgram();
        if (program != null) {
            program.accept(checkVisitor);
        }
        int startOffset = getOffset(namespaceScope, importData.caretPosition, checkVisitor);
        List<UsePart> useParts = new ArrayList<>();
        Collection<? extends GroupUseScope> declaredGroupUses = namespaceScope.getDeclaredGroupUses();
        for (GroupUseScope groupUseElement : declaredGroupUses) {
            for (UseScope useElement : groupUseElement.getUseScopes()) {
                processUseElement(useElement, useParts);
            }
        }
        Collection<? extends UseScope> declaredUses = namespaceScope.getDeclaredSingleUses();
        for (UseScope useElement : declaredUses) {
            assert !useElement.isPartOfGroupUse() : useElement;
            processUseElement(useElement, useParts);
        }
        for (int i = 0; i < selections.size(); i++) {
            ItemVariant itemVariant = selections.get(i);
            if (itemVariant.canBeUsed()) {
                SanitizedUse sanitizedUse = new SanitizedUse(
                        new UsePart(modifyUseName(itemVariant.getName()), UsePart.Type.create(itemVariant.getType()), itemVariant.isFromAliasedElement()),
                        useParts,
                        createAliasStrategy(i, useParts, selections));
                if (sanitizedUse.shouldBeUsed()) {
                    useParts.add(sanitizedUse.getSanitizedUsePart());
                }
                for (UsedNamespaceName usedNamespaceName : dataItems.get(i).getUsedNamespaceNames()) {
                    editList.replace(usedNamespaceName.getOffset(), usedNamespaceName.getReplaceLength(), sanitizedUse.getReplaceName(usedNamespaceName), false, 0);
                }
            }
        }
        replaceUnimportedItems();
        Map<UsePart.Type, Integer> typePriorities = getTypeOrderPriorities(useParts, checkVisitor.getExistingTypeOrderPriorities());
        String insertString = createInsertString(useParts, typePriorities);
        insertUses(startOffset, insertString, checkVisitor);
    }

    private void insertUses(int startOffset, String insertString, CheckVisitor visitor) {
        List<OffsetRange> usedRanges = visitor.getUsedRanges();
        String existingUses = getExistingUses(usedRanges);
        // avoid being recognized as a modified file
        if (insertString.isEmpty()
                || existingUses.equals(insertString.trim())) {
            StatusDisplayer.getDefault().setStatusText(Bundle.FixUsesPerformer_noChanges());
        } else {
            processExistingUses(usedRanges);
            editList.replace(startOffset, 0, insertString, false, 0);
        }
    }

    private void replaceUnimportedItems() {
        for (ImportData.DataItem dataItem : importData.getItemsToReplace()) {
            ItemVariant defaultVariant = dataItem.getDefaultVariant();
            for (UsedNamespaceName usedNamespaceName : dataItem.getUsedNamespaceNames()) {
                editList.replace(usedNamespaceName.getOffset(), usedNamespaceName.getReplaceLength(), defaultVariant.getName(), false, 0);
            }
        }
    }

    private List<ImportData.DataItem> resolveDuplicateSelections() {
        final List<ImportData.DataItem> dataItems = new ArrayList<>(importData.getItems());
        List<ItemVariant> selectionsCopy = new ArrayList<>(selections);
        List<Integer> itemIndexesToRemove = new ArrayList<>();
        for (int i = 0; i < selections.size(); i++) {
            List<UsedNamespaceName> usedNamespaceNames = new ArrayList<>();
            ItemVariant baseVariant = selections.get(i);
            if (!baseVariant.canBeUsed()) {
                continue;
            }
            for (int j = i + 1; j < selectionsCopy.size(); j++) {
                ItemVariant testedVariant = selectionsCopy.get(j);
                if (baseVariant.equals(testedVariant)
                        && itemIndexesToRemove.add(j)) {
                    ImportData.DataItem duplicateItem = dataItems.get(j);
                    usedNamespaceNames.addAll(duplicateItem.getUsedNamespaceNames());
                }
            }
            if (!usedNamespaceNames.isEmpty()) {
                dataItems.get(i).addUsedNamespaceNames(usedNamespaceNames);
            }
        }
        Collections.sort(itemIndexesToRemove);
        Collections.reverse(itemIndexesToRemove);
        for (int itemIndexToRemove : itemIndexesToRemove) {
            // we need to remove selection as well as its dataitem (they are paired by their indexes)
            selections.remove(itemIndexToRemove);
            dataItems.remove(itemIndexToRemove);
        }
        return dataItems;
    }

    private AliasStrategy createAliasStrategy(final int selectionIndex, final List<UsePart> existingUseParts, final List<ItemVariant> selections) {
        AliasStrategy createAliasStrategy;
        if (options.aliasesCapitalsOfNamespaces()) {
            createAliasStrategy = new CapitalsStrategy(selectionIndex, existingUseParts, selections);
        } else {
            createAliasStrategy = new UnqualifiedNameStrategy(selectionIndex, existingUseParts, selections);
        }
        return createAliasStrategy;
    }

    private void processUseElement(final UseScope useElement, final List<UsePart> useParts) {
        if (isUsed(useElement) || !removeUnusedUses) {
            AliasedName aliasedName = useElement.getAliasedName();
            if (aliasedName != null) {
                useParts.add(new UsePart(
                        modifyUseName(aliasedName.getRealName().toString()) + AS_CONCAT + aliasedName.getAliasName(),
                        UsePart.Type.create(useElement.getType())));
            } else {
                useParts.add(new UsePart(
                        modifyUseName(useElement.getName()),
                        UsePart.Type.create(useElement.getType())));
            }
        }
    }

    private String modifyUseName(final String useName) {
        String result = useName;
        if (options.startUseWithNamespaceSeparator()) {
            result = result.startsWith(ImportDataCreator.NS_SEPARATOR) ? result : ImportDataCreator.NS_SEPARATOR + result;
        } else {
            result = result.startsWith(ImportDataCreator.NS_SEPARATOR) ? result.substring(ImportDataCreator.NS_SEPARATOR.length()) : result;
        }
        return result;
    }

    private boolean isUsed(final UseScope useElement) {
        boolean result = true;
        for (UnusedOffsetRanges unusedRange : new UnusedUsesCollector(parserResult).collect()) {
            if (unusedRange.getRangeToVisualise().containsInclusive(useElement.getOffset())) {
                result = false;
                break;
            }
        }
        return result;
    }

    private String createInsertString(final List<UsePart> useParts, final Map<UsePart.Type, Integer> typeOrderPriorities) {
        StringBuilder insertString = new StringBuilder();
        sort(useParts, typeOrderPriorities);
        if (!useParts.isEmpty()) {
            insertString.append(NEW_LINE);
        }
        String indentString = null;
        if (options.preferGroupUses()
                || options.preferMultipleUseStatementsCombined()) {
            CodeStyle codeStyle = CodeStyle.get(baseDocument);
            indentString = IndentUtils.createIndentString(codeStyle.getIndentSize(), codeStyle.expandTabToSpaces(), codeStyle.getTabSize());
        }

        if (options.preferGroupUses()
                && options.getPhpVersion().compareTo(PhpVersion.PHP_70) >= 0) {
            insertString.append(createStringForGroupUse(useParts, indentString, typeOrderPriorities));
        } else if (options.preferMultipleUseStatementsCombined()) {
            insertString.append(createStringForMultipleUse(useParts, indentString));
        } else {
            insertString.append(createStringForCommonUse(useParts));
        }
        return insertString.toString();
    }

    private Map<UsePart.Type, Integer> getTypeOrderPriorities(List<UsePart> useParts, Map<UsePart.Type, Integer> existingTypeOrderPriorities) {
        boolean useExistingOrder = true;
        for (UsePart usePart : useParts) {
            if (existingTypeOrderPriorities.get(usePart.getType()) == null) {
                useExistingOrder = false;
                break;
            }
        }
        if (options.keepExistingUseTypeOrder() && useExistingOrder) {
            return existingTypeOrderPriorities;
        } else if (options.putInPSR12Order()) {
            return Collections.unmodifiableMap(PSR12_TYPE_PRIORITIES);
        }
        return Collections.unmodifiableMap(DEFAULT_TYPE_PRIORITIES);
    }

    private void sort(List<UsePart> useParts, final Map<UsePart.Type, Integer> typePriorities) {
        useParts.sort((u1, u2) -> {
            int result = 0;
            Integer p1 = typePriorities.get(u1.getType());
            Integer p2 = typePriorities.get(u2.getType());
            if (p1 != null && p2 != null) {
                result = Integer.compare(p1, p2);
            }
            return result == 0 ? u1.getTextPart().compareToIgnoreCase(u2.getTextPart()) : result;
        });
    }

    private String createStringForGroupUse(List<UsePart> useParts, String indentString, Map<UsePart.Type, Integer> typePriorities) {
        List<UsePart> typeUseParts = new ArrayList<>(useParts.size());
        List<UsePart> constUseParts = new ArrayList<>(useParts.size());
        List<UsePart> functionUseParts = new ArrayList<>(useParts.size());
        for (UsePart usePart : useParts) {
            switch (usePart.getType()) {
                case TYPE:
                    typeUseParts.add(usePart);
                    break;
                case CONST:
                    constUseParts.add(usePart);
                    break;
                case FUNCTION:
                    functionUseParts.add(usePart);
                    break;
                default:
                    assert false : "Unknown type: " + usePart.getType(); // NOI18N
            }
        }
        StringBuilder insertString = new StringBuilder();
        final Map<String, List<UsePart>> groupUseParts = new HashMap<>();
        groupUseParts.put(USE_PREFIX, typeUseParts);
        groupUseParts.put(USE_CONST_PREFIX, constUseParts);
        groupUseParts.put(USE_FUNCTION_PREFIX, functionUseParts);
        createStringForGroupUse(insertString, indentString, groupUseParts, typePriorities);
        return insertString.toString();
    }

    private List<String> usePartsToNamespaces(List<UsePart> useParts) {
        return useParts.stream()
                .map(part -> part.getTextPart())
                .collect(Collectors.toList());
    }

    private void createStringForGroupUse(StringBuilder insertString, String indentString, Map<String, List<UsePart>> useParts, Map<UsePart.Type, Integer> typePriorities) {
        createStringForGroupUse(getOrderedUseTypePrefixes(typePriorities), useParts, insertString, indentString);
    }

    private List<String> getOrderedUseTypePrefixes(Map<UsePart.Type, Integer> typePriorities) {
        String[] priorities = new String[typePriorities.size()];
        for (Map.Entry<UsePart.Type, Integer> entry : typePriorities.entrySet()) {
            Integer position = entry.getValue();
            priorities[position] =  getUseTypePrefix(entry.getKey());
        }
        return Arrays.asList(priorities);
    }

    private String getUseTypePrefix(UsePart.Type type) {
        switch (type) {
            case TYPE:
                return USE_PREFIX;
            case FUNCTION:
                return USE_FUNCTION_PREFIX;
            case CONST:
                return USE_CONST_PREFIX;
            default:
                assert false : "Unkown type: " + type; // NOI18N
                return USE_PREFIX;
        }
    }

    private void createStringForGroupUse(List<String> orderedUseTypePrefixes, Map<String, List<UsePart>> useParts, StringBuilder insertString, String indentString) {
        for (String useType : orderedUseTypePrefixes) {
            createStringForGroupUse(Pair.of(useType, useParts), insertString, indentString);
        }
    }

    private void createStringForGroupUse(Pair<String, Map<String, List<UsePart>>> useParts, StringBuilder insertString, String indentString) {
        String useType = useParts.first();
        if (!useParts.second().get(useType).isEmpty()) {
            appendNewLineBetweenUseTypes(insertString);
        }
        createStringForGroupUse(insertString, indentString, useType, useParts.second().get(useType));
    }

    private void appendNewLineBetweenUseTypes(StringBuilder insertString) {
        for (int i = 0; i < options.getBlankLinesBetweenUseTypes(); i++) {
            appendNewLine(insertString);
        }
    }

    private void appendNewLine(StringBuilder insertString) {
        if (insertString.length() > 0) {
            insertString.append(NEW_LINE);
        }
    }

    private void createStringForGroupUse(StringBuilder insertString, String indentString, String usePrefix, List<UsePart> useParts) {
        List<UsePart> groupedUseParts = new ArrayList<>(useParts.size());
        List<UsePart> nonGroupedUseParts = new ArrayList<>(useParts.size());
        List<String> prefixes = CodeUtils.getCommonNamespacePrefixes(usePartsToNamespaces(useParts));
        String lastGroupUsePrefix = null;
        for (UsePart usePart : useParts) {
            String fqNamespace = CodeUtils.fullyQualifyNamespace(usePart.getTextPart());
            String groupUsePrefix = null;
            for (String prefix : prefixes) {
                if (fqNamespace.startsWith(prefix)) {
                    groupUsePrefix = prefix;
                    break;
                }
            }
            if (groupUsePrefix != null) {
                if (lastGroupUsePrefix != null
                        && !lastGroupUsePrefix.equals(groupUsePrefix)) {
                    processGroupedUseParts(insertString, indentString, usePrefix, lastGroupUsePrefix, groupedUseParts);
                }
                lastGroupUsePrefix = groupUsePrefix;
                processNonGroupedUseParts(insertString, indentString, nonGroupedUseParts);
                groupedUseParts.add(usePart);
            } else {
                processGroupedUseParts(insertString, indentString, usePrefix, lastGroupUsePrefix, groupedUseParts);
                nonGroupedUseParts.add(usePart);
            }
        }
        processNonGroupedUseParts(insertString, indentString, nonGroupedUseParts);
        processGroupedUseParts(insertString, indentString, usePrefix, lastGroupUsePrefix, groupedUseParts);
    }

    private void processNonGroupedUseParts(StringBuilder insertString, String indentString, List<UsePart> nonGroupedUseParts) {
        if (nonGroupedUseParts.isEmpty()) {
            return;
        }
        if (options.preferMultipleUseStatementsCombined()) {
            insertString.append(createStringForMultipleUse(nonGroupedUseParts, indentString));
        } else {
            insertString.append(createStringForCommonUse(nonGroupedUseParts));
        }
        nonGroupedUseParts.clear();
    }

    private void processGroupedUseParts(StringBuilder insertString, String indentString, String usePrefix, String groupUsePrefix, List<UsePart> groupedUseParts) {
        if (groupedUseParts.isEmpty()) {
            return;
        }
        assert groupUsePrefix != null : groupedUseParts;
        String properGroupUsePrefix = modifyUseName(groupUsePrefix);
        insertString.append(usePrefix).append(properGroupUsePrefix).append(CURLY_OPEN).append(NEW_LINE);
        boolean first = true;
        int prefixLength = properGroupUsePrefix.length();
        for (UsePart groupUsePart : groupedUseParts) {
            if (first) {
                first = false;
            } else {
                insertString.append(COMMA).append(NEW_LINE);
            }
            insertString.append(indentString).append(groupUsePart.getTextPart().substring(prefixLength));
        }
        insertString.append(NEW_LINE).append(CURLY_CLOSE).append(SEMICOLON);
        groupedUseParts.clear();
    }

    private String createStringForMultipleUse(List<UsePart> useParts, String indentString) {
        StringBuilder insertString = new StringBuilder();
        UsePart.Type lastUsePartType = null;
        for (Iterator<UsePart> it = useParts.iterator(); it.hasNext(); ) {
            UsePart usePart = it.next();
            if (lastUsePartType != null) {
                if (lastUsePartType == usePart.getType()) {
                    insertString.append(COMMA).append(NEW_LINE).append(indentString);
                } else {
                    insertString.append(SEMICOLON);
                    appendNewLineBetweenUseTypes(insertString);
                }
            }
            if (lastUsePartType != usePart.getType()) {
                lastUsePartType = usePart.getType();
                switch (usePart.getType()) {
                    case TYPE:
                        insertString.append(USE_PREFIX);
                        break;
                    case CONST:
                        insertString.append(USE_CONST_PREFIX);
                        break;
                    case FUNCTION:
                        insertString.append(USE_FUNCTION_PREFIX);
                        break;
                    default:
                        insertString.append(USE_PREFIX);
                }
            }
            insertString.append(usePart.getTextPart());
        }
        if (!useParts.isEmpty()) {
            insertString.append(SEMICOLON);
        }
        return insertString.toString();
    }

    private String createStringForCommonUse(List<UsePart> useParts) {
        StringBuilder result = new StringBuilder();
        UsePart.Type lastUseType = null;
        for (UsePart usePart : useParts) {
            if (lastUseType != null && lastUseType != usePart.getType()) {
                appendNewLineBetweenUseTypes(result);
            }
            result.append(usePart.getUsePrefix()).append(usePart.getTextPart()).append(SEMICOLON);
            lastUseType = usePart.getType();
        }
        return result.toString();
    }

    private String getExistingUses(List<OffsetRange> usedRanges) {
        String existingUses = EMPTY_STRING;
        if (!usedRanges.isEmpty()) {
            int start = usedRanges.get(0).getStart();
            int end = usedRanges.get(usedRanges.size() - 1).getEnd();
            try {
                existingUses = baseDocument.getText(start, end - start);
            } catch (BadLocationException ex) {
                LOGGER.log(Level.WARNING, "Invalid offset: {0}", ex.offsetRequested()); // NOI18N
            }
        }
        return existingUses;
    }

    private void processExistingUses(List<OffsetRange> usedRanges) {
        for (OffsetRange offsetRange : usedRanges) {
            int startOffset = getOffsetWithoutLeadingWhitespaces(offsetRange.getStart());
            editList.replace(startOffset, offsetRange.getEnd() - startOffset, EMPTY_STRING, false, 0);
        }
    }

    private int getOffsetWithoutLeadingWhitespaces(final int startOffset) {
        int result = startOffset;
        baseDocument.readLock();
        try {
            TokenSequence<PHPTokenId> ts = LexUtilities.getPHPTokenSequence(baseDocument, startOffset);
            if (ts != null) {
                ts.move(startOffset);
                while (ts.movePrevious() && ts.token().id().equals(PHPTokenId.WHITESPACE)) {
                    result = ts.offset();
                }
            }
        } finally {
            baseDocument.readUnlock();
        }
        return result;
    }

    private int getOffset(NamespaceScope namespaceScope, int caretPosition, CheckVisitor checkVisitor) {
        try {
            ModelElement lastSingleUse = getLastUse(namespaceScope, false);
            ModelElement lastGroupUse = getLastUse(namespaceScope, true);
            if (lastSingleUse != null
                    && lastGroupUse != null) {
                if (lastSingleUse.getOffset() > lastGroupUse.getOffset()) {
                    return LineDocumentUtils.getLineEnd(baseDocument, lastSingleUse.getOffset());
                }
                // XXX is this correct?
                return LineDocumentUtils.getLineEnd(baseDocument, lastGroupUse.getNameRange().getEnd());
            }
            if (lastSingleUse != null) {
                return LineDocumentUtils.getLineEnd(baseDocument, lastSingleUse.getOffset());
            }
            if (lastGroupUse != null) {
                // XXX is this correct?
                return LineDocumentUtils.getLineEnd(baseDocument, lastGroupUse.getNameRange().getEnd());
            }
            // NETBEANS-4978 check whether declare statemens exist
            // e.g. in the following case, insert the code(use statements) after the declare statement
            // declare (strict_types=1);
            // class TestClass
            // {
            //     public function __construct()
            //     {
            //         $test = new Foo();
            //     }
            // }
            int offset = LineDocumentUtils.getLineEnd(baseDocument, namespaceScope.getOffset());
            if (namespaceScope.isDefaultNamespace()) {
                // GH-5578: e.g. namespaceScope offset is 0 when phptag is in HTML
                // <html>
                //      <?php
                //      new InHtml();
                //      ?>
                offset = Integer.max(offset, getFirstPhpTagPosition(parserResult, namespaceScope));
            }
            if (namespaceScope.isDefaultNamespace()) {
                List<NamespaceDeclaration> globalNamespaceDeclarations = checkVisitor.getGlobalNamespaceDeclarations();
                if (!globalNamespaceDeclarations.isEmpty()) {
                    offset = globalNamespaceDeclarations.get(0).getBody().getStartOffset() + 1; // +1: {
                }
                for (NamespaceDeclaration globalNamespace : globalNamespaceDeclarations) {
                    if (globalNamespace.getStartOffset() <= caretPosition
                            && caretPosition <= globalNamespace.getEndOffset()) {
                        offset = globalNamespace.getBody().getStartOffset() + 1; // +1: {
                        break;
                    }
                }
            }
            offset = processDeclareStatementsOffset(namespaceScope, checkVisitor, offset);
            return offset;
        } catch (BadLocationException ex) {
            LOGGER.log(Level.WARNING, "Invalid offset: {0}", ex.offsetRequested()); // NOI18N
        }
        return 0;
    }

    private static int processDeclareStatementsOffset(NamespaceScope namespaceScope, CheckVisitor checkVisitor, int offset) {
        int result = offset;
        // e.g. declare statements may be other than behind the namespace name
        // namespace NS;
        // function foo() {
        //     declare(ticks=1) {}
        // }
        int maxDeclareOffset = getMaxDeclareOffset(namespaceScope, checkVisitor);
        for (DeclareStatement declareStatement : checkVisitor.getDeclareStatements()) {
            if (maxDeclareOffset < declareStatement.getStartOffset()) {
                break;
            }
            result = Math.max(result, declareStatement.getEndOffset());
        }
        return result;
    }

    private static int getMaxDeclareOffset(NamespaceScope namespaceScope, CheckVisitor checkVisitor) {
        int maxDeclareOffset = namespaceScope.getBlockRange().getEnd();
        if (!namespaceScope.getElements().isEmpty()) {
            for (ModelElement element : namespaceScope.getElements()) {
                if (isInDeclare(element.getOffset(), checkVisitor.getDeclareStatements())) {
                    maxDeclareOffset = getDeclareEndPosition(element.getOffset(), checkVisitor.getDeclareStatements());
                    continue;
                }
                maxDeclareOffset = element.getOffset();
                break;
            }
        }
        return maxDeclareOffset;
    }

    private static boolean isInDeclare(int offset, List<DeclareStatement> declareStatements) {
        // e.g.
        // declare(ticks=1) {
        //     $test = 1; // is this element in declare?
        // }
        for (DeclareStatement declareStatement : declareStatements) {
            if (isInDeclare(offset, declareStatement)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isInDeclare(int offset, DeclareStatement declareStatement) {
        return declareStatement.getStartOffset() < offset && offset < declareStatement.getEndOffset();
    }

    private static int getDeclareEndPosition(int offset, List<DeclareStatement> declareStatements) {
        for (DeclareStatement declareStatement : declareStatements) {
            if (isInDeclare(offset, declareStatement)) {
                return declareStatement.getEndOffset();
            }
        }
        return -1;
    }

    @CheckForNull
    private static ModelElement getLastUse(NamespaceScope namespaceScope, boolean group) {
        ModelElement offsetElement = null;
        Collection<? extends Scope> declaredUses = group ? namespaceScope.getDeclaredGroupUses() : namespaceScope.getDeclaredSingleUses();
        for (Scope useElement : declaredUses) {
            if (offsetElement == null
                    || offsetElement.getOffset() < useElement.getOffset()) {
                offsetElement = useElement;
            }
        }
        return offsetElement;
    }

    private static int getFirstPhpTagPosition(PHPParseResult parserResult, NamespaceScope namespaceScope) {
        final int startOffset = namespaceScope.getOffset();
        int result = -1;
        if (namespaceScope.isDefaultNamespace()) {
            TokenHierarchy<?> tokenHierarchy = parserResult.getSnapshot().getTokenHierarchy();
            TokenSequence<PHPTokenId> ts = LexUtilities.getPHPTokenSequence(tokenHierarchy, startOffset);
            if (ts != null) {
                ts.move(startOffset);
                while (ts.moveNext()) {
                    if (ts.token().id() == PHPTokenId.PHP_OPENTAG) {
                        result = ts.offset() + ts.token().length();
                        break;
                    }
                }
            }
        }
        return result;
    }

    //~ inner classes
    private static class CheckVisitor extends DefaultVisitor {

        private final List<DeclareStatement> declareStatements = new ArrayList<>();
        private final List<NamespaceDeclaration> globalNamespaceDeclarations = new ArrayList<>();
        private final List<OffsetRange> usedRanges = new LinkedList<>();
        private final Map<UsePart.Type, Integer> existingTypeOrderPriorities = new HashMap<>();

        public List<DeclareStatement> getDeclareStatements() {
            return Collections.unmodifiableList(declareStatements);
        }

        public List<NamespaceDeclaration> getGlobalNamespaceDeclarations() {
            return Collections.unmodifiableList(globalNamespaceDeclarations);
        }

        public List<OffsetRange> getUsedRanges() {
            return Collections.unmodifiableList(usedRanges);
        }

        public Map<UsePart.Type, Integer> getExistingTypeOrderPriorities() {
            return Collections.unmodifiableMap(existingTypeOrderPriorities);
        }

        @Override
        public void visit(DeclareStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            declareStatements.add(node);
            super.visit(node);
        }

        @Override
        public void visit(NamespaceDeclaration declaration) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (declaration.isBracketed() && declaration.getName() == null) {
                globalNamespaceDeclarations.add(declaration);
            }
            super.visit(declaration);
        }

        @Override
        public void visit(UseStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            usedRanges.add(new OffsetRange(node.getStartOffset(), node.getEndOffset()));
            UsePart.Type usePartType = UsePart.Type.create(node.getType());
            Integer priority = existingTypeOrderPriorities.get(usePartType);
            if (priority == null) {
                existingTypeOrderPriorities.put(usePartType, existingTypeOrderPriorities.size());
            }
            super.visit(node);
        }
    }

    private interface AliasStrategy {
        String createAlias(final QualifiedName qualifiedName);
    }

    private abstract static class AliasStrategyImpl implements AliasStrategy {

        private final int selectionIndex;
        private final List<UsePart> existingUseParts;
        private final List<ItemVariant> selections;

        public AliasStrategyImpl(final int selectionIndex, final List<UsePart> existingUseParts, final List<ItemVariant> selections) {
            this.selectionIndex = selectionIndex;
            this.existingUseParts = existingUseParts;
            this.selections = selections;
        }

        @Override
        public String createAlias(final QualifiedName qualifiedName) {
            String result = "";
            final String possibleAliasedName = getPossibleAliasName(qualifiedName);
            String newAliasedName = possibleAliasedName;
            int i = 1;
            while (existSelectionWith(newAliasedName, selectionIndex) || existUseWith(newAliasedName)) {
                i++;
                newAliasedName = possibleAliasedName + i;
                result = newAliasedName;
            }
            return result.isEmpty() && mustHaveAlias(qualifiedName) ? possibleAliasedName : result;
        }

        private boolean mustHaveAlias(final QualifiedName qualifiedName) {
            final String unqualifiedName = qualifiedName.getName();
            return existSelectionWith(unqualifiedName, selectionIndex) || existUseWith(unqualifiedName);
        }

        private boolean existSelectionWith(final String name, final int selectionIndex) {
            for (int i = selectionIndex + 1; i < selections.size(); i++) {
                if (endsWithName(selections.get(i).getName(), name)) {
                    return true;
                }
            }
            return false;
        }

        private boolean existUseWith(final String name) {
            for (UsePart existingUsePart : existingUseParts) {
                if (endsWithName(existingUsePart.getTextPart(), name) || existingUsePart.getTextPart().endsWith(SPACE + name)) {
                    return true;
                }
            }
            return false;
        }

        private boolean endsWithName(final String usePart, final String name) {
            return usePart.endsWith(ImportDataCreator.NS_SEPARATOR + name);
        }

        protected abstract String getPossibleAliasName(final QualifiedName qualifiedName);

    }

    private static class CapitalsStrategy extends AliasStrategyImpl {

        public CapitalsStrategy(int selectionIndex, List<UsePart> existingUseParts, List<ItemVariant> selections) {
            super(selectionIndex, existingUseParts, selections);
        }

        @Override
        protected String getPossibleAliasName(final QualifiedName qualifiedName) {
            final StringBuilder sb = new StringBuilder();
            for (String segment : qualifiedName.getSegments()) {
                sb.append(Character.toUpperCase(segment.charAt(0)));
            }
            return sb.toString();
        }

    }

    private static class UnqualifiedNameStrategy extends AliasStrategyImpl {

        public UnqualifiedNameStrategy(int selectionIndex, List<UsePart> existingUseParts, List<ItemVariant> selections) {
            super(selectionIndex, existingUseParts, selections);
        }

        @Override
        protected String getPossibleAliasName(QualifiedName qualifiedName) {
            return qualifiedName.getName();
        }

    }

    private static class SanitizedUse {

        private final UsePart usePartToSanitization;
        private String alias;
        private final boolean shouldBeUsed;

        public SanitizedUse(final UsePart usePartToSanitization, final List<UsePart> existingUseParts, final AliasStrategy createAliasStrategy) {
            this.usePartToSanitization = usePartToSanitization;
            QualifiedName qualifiedName = QualifiedName.create(usePartToSanitization.getTextPart());
            if (!existingUseParts.contains(usePartToSanitization) && !usePartToSanitization.isFromAliasedElement()) {
                alias = createAliasStrategy.createAlias(qualifiedName);
                shouldBeUsed = true;
            } else {
                shouldBeUsed = false;
            }
        }

        public UsePart getSanitizedUsePart() {
            return new UsePart(hasAlias() ? usePartToSanitization.getTextPart() + AS_CONCAT + alias : usePartToSanitization.getTextPart(), usePartToSanitization.getType(), usePartToSanitization.isFromAliasedElement());
        }

        private boolean hasAlias() {
            return alias != null && !alias.isEmpty();
        }

        public String getReplaceName(final UsedNamespaceName usedNamespaceName) {
            String result;
            if (hasAlias()) {
                result = alias;
            } else {
                if (usePartToSanitization.isFromAliasedElement()) {
                    result = usePartToSanitization.getTextPart();
                } else {
                    result = usedNamespaceName.getReplaceName();
                }
            }
            return result;
        }

        public boolean shouldBeUsed() {
            return shouldBeUsed;
        }
    }

    private static class UsePart implements Comparable<UsePart> {

        enum Type {
            TYPE {
                @Override
                String getUsePrefix() {
                    return USE_PREFIX;
                }
            },
            CONST {
                @Override
                String getUsePrefix() {
                    return USE_CONST_PREFIX;
                }
            },
            FUNCTION {
                @Override
                String getUsePrefix() {
                    return USE_FUNCTION_PREFIX;
                }
            };

            abstract String getUsePrefix();

            static Type create(ItemVariant.Type type) {
                Type result;
                switch (type) {
                    case CLASS: // no break
                    case INTERFACE: // no break
                    case TRAIT: // no break
                    case ENUM:
                        result = TYPE;
                        break;
                    case CONST:
                        result = CONST;
                        break;
                    case FUNCTION:
                        result = FUNCTION;
                        break;
                    default:
                        result = TYPE;
                }
                return result;
            }

            static Type create(UseScope.Type type) {
                Type result;
                switch (type) {
                    case TYPE:
                        result = TYPE;
                        break;
                    case CONST:
                        result = CONST;
                        break;
                    case FUNCTION:
                        result = FUNCTION;
                        break;
                    default:
                        result = TYPE;
                }
                return result;
            }

            static Type create(UseStatement.Type type) {
                Type usePartType = TYPE;
                switch (type) {
                    case TYPE:
                        usePartType = TYPE;
                        break;
                    case FUNCTION:
                        usePartType = FUNCTION;
                        break;
                    case CONST:
                        usePartType = CONST;
                        break;
                    default:
                        assert false : "Unknown Type: " + type; // NOI18N
                        break;
                }
                return usePartType;
            }
        }

        private final String textPart;
        private final Type type;
        private final boolean isFromAliasedElement;

        private UsePart(String textPart, Type type, boolean isFromAliasedElement) {
            this.textPart = textPart;
            this.type = type;
            this.isFromAliasedElement = isFromAliasedElement;
        }

        private UsePart(String textPart, Type type) {
            this(textPart, type, false);
        }

        public String getTextPart() {
            return textPart;
        }

        public Type getType() {
            return type;
        }

        public String getUsePrefix() {
            return type.getUsePrefix();
        }

        public boolean isFromAliasedElement() {
            return isFromAliasedElement;
        }

        @Override
        public int compareTo(UsePart other) {
            int result = 0;
            if (UsePart.Type.TYPE.equals(getType()) && UsePart.Type.TYPE.equals(other.getType())) {
                result = 0;
            } else if (UsePart.Type.TYPE.equals(getType()) && UsePart.Type.CONST.equals(other.getType())) {
                result = -1;
            } else if (UsePart.Type.TYPE.equals(getType()) && UsePart.Type.FUNCTION.equals(other.getType())) {
                result = -1;
            } else if (UsePart.Type.CONST.equals(getType()) && UsePart.Type.TYPE.equals(other.getType())) {
                result = 1;
            } else if (UsePart.Type.CONST.equals(getType()) && UsePart.Type.CONST.equals(other.getType())) {
                result = 0;
            } else if (UsePart.Type.CONST.equals(getType()) && UsePart.Type.FUNCTION.equals(other.getType())) {
                result = -1;
            } else if (UsePart.Type.FUNCTION.equals(getType()) && UsePart.Type.TYPE.equals(other.getType())) {
                result = 1;
            } else if (UsePart.Type.FUNCTION.equals(getType()) && UsePart.Type.CONST.equals(other.getType())) {
                result = 1;
            } else if (UsePart.Type.FUNCTION.equals(getType()) && UsePart.Type.FUNCTION.equals(other.getType())) {
                result = 0;
            }
            return result == 0 ? getTextPart().compareToIgnoreCase(other.getTextPart()) : result;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 71 * hash + Objects.hashCode(this.textPart);
            hash = 71 * hash + Objects.hashCode(this.type);
            hash = 71 * hash + (this.isFromAliasedElement ? 1 : 0);
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
            final UsePart other = (UsePart) obj;
            if (!Objects.equals(this.textPart, other.textPart)) {
                return false;
            }
            if (this.type != other.type) {
                return false;
            }
            return this.isFromAliasedElement == other.isFromAliasedElement;
        }

        @Override
        public String toString() {
            return "UsePart{" + type + " " + textPart + '}'; // NOI18N
        }

    }

}
