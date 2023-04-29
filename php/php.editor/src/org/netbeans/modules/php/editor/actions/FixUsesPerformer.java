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
import java.util.Collections;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.document.LineDocumentUtils;
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
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.UseScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.UnusedUsesCollector;
import org.netbeans.modules.php.editor.parser.UnusedUsesCollector.UnusedOffsetRanges;
import org.netbeans.modules.php.editor.parser.astnodes.DeclareStatement;
import org.netbeans.modules.php.editor.parser.astnodes.GroupUseStatementPart;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.UseStatement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

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
    private final PHPParseResult parserResult;
    private final ImportData importData;
    private final List<ItemVariant> selections;
    private final boolean removeUnusedUses;
    private final boolean putInPSR12Order;
    private final Options options;
    private boolean isEdited = false;
    private EditList editList;
    private BaseDocument baseDocument;
    private NamespaceScope namespaceScope;

    public FixUsesPerformer(
            final PHPParseResult parserResult,
            final ImportData importData,
            final List<ItemVariant> selections,
            final boolean removeUnusedUses,
            final boolean putInPSR12Order,
            final Options options) {
        this.parserResult = parserResult;
        this.importData = importData;
        this.selections = selections;
        this.removeUnusedUses = removeUnusedUses;
        this.putInPSR12Order = putInPSR12Order;
        this.options = options;
    }

    private void perform(boolean append) {
        final Document document = parserResult.getSnapshot().getSource().getDocument(false);
        if (document instanceof BaseDocument) {
            baseDocument = (BaseDocument) document;
            editList = new EditList(baseDocument);
            namespaceScope = ModelUtils.getNamespaceScope(parserResult.getModel().getFileScope(), importData.caretPosition);
            assert namespaceScope != null;
            processSelections(append);
            editList.apply();
        }
    }

    public void perform() {
        perform(false);
    }

    public void performAppend() {
        assert this.selections.size() == 1 : "Expected size == 1 but got: " + selections.size();
        perform(true);
    }

    @NbBundle.Messages("FixUsesPerformer.noChanges=Fix imports: No Changes")
    private void processSelections(boolean append) {
        final List<ImportData.DataItem> dataItems = resolveDuplicateSelections();
        TreeMap<Integer, List<UsePart>> usePartsMap = new TreeMap<>();
        assert selections.size() <= dataItems.size()
                : "The selections size must not be larger than the dataItems size. selections size: " + selections.size() + " > dataItems size: " + dataItems.size(); // NOI18N

        CheckVisitor visitor = new CheckVisitor();
        Program program = parserResult.getProgram();
        if (program != null) {
            program.accept(visitor);
        }

        UsesInsertStringHelper usesInsertStringHelper = new UsesInsertStringHelper(editList, visitor.getUsedRanges(), visitor.hasMultipleUses(), append, 0);

        for (int i = 0; i < selections.size(); i++) {
            ItemVariant itemVariant = selections.get(i);
            // we shouldn't use itemVariant if there is no any real dataItem related to it
            if (itemVariant.canBeUsed() && !dataItems.get(i).getUsedNamespaceNames().isEmpty()) {
                NamespaceScope currentScope = dataItems.get(i).getUsedNamespaceNames().get(0).getInScope();
                int mapKey = currentScope.getBlockRange().getStart();
                if (usePartsMap.get(mapKey) == null) {
                    usePartsMap.put(mapKey, processScopeDeclaredUses(currentScope, new ArrayList<>(), usesInsertStringHelper));
                }

                SanitizedUse sanitizedUse = new SanitizedUse(
                        new UsePart(modifyUseName(itemVariant.getName()), UsePart.Type.create(itemVariant.getType()), 0, itemVariant.isFromAliasedElement()),
                        usePartsMap.get(mapKey),
                        createAliasStrategy(i, usePartsMap.get(mapKey), selections));
                if (sanitizedUse.shouldBeUsed()) {
                    usePartsMap.get(mapKey).add(sanitizedUse.getSanitizedUsePart());
                }
                for (UsedNamespaceName usedNamespaceName : dataItems.get(i).getUsedNamespaceNames()) {
                    editList.replace(usedNamespaceName.getOffset(), usedNamespaceName.getReplaceLength(), sanitizedUse.getReplaceName(usedNamespaceName), false, 0);
                }
            }
        }
        replaceUnimportedItems();

        int lastDeclareIndex = 0;
        List<NamespaceScope> declaredNamespaces;
        if (namespaceScope.isDefaultNamespace()) {
            declaredNamespaces = new ArrayList(namespaceScope.getFileScope().getDeclaredNamespaces());
            declaredNamespaces.sort((left, right) -> left.getBlockRange().getStart() - right.getBlockRange().getStart());
        } else {
            declaredNamespaces = new ArrayList();
            declaredNamespaces.add(namespaceScope);
        }
        for (NamespaceScope currentScope : declaredNamespaces) {
            int mapKey = currentScope.getBlockRange().getStart();
            if (usePartsMap.get(mapKey) == null) {
                usePartsMap.put(mapKey, processScopeDeclaredUses(currentScope, new ArrayList<>(), usesInsertStringHelper));
            }

            int startOffset = getNamespaceScopeOffset(currentScope);
            int endOffset = currentScope.isDefaultNamespace() && visitor.getGlobalNamespaceEndOffset() > 0
                    ? visitor.getGlobalNamespaceEndOffset()
                    : currentScope.getBlockRange().getEnd();

            // because only declare(strict_types=1) should go first, but declare(ticks=1) could be everywhere
            // we have to restrict declare start offset to prevent wrong USE placement after declare in cases such
            // function { declare(ticks=1); } or class A { puclic function fn () { declare(ticks=1); } }
            // in the feature it would be better to have DeclareStatmentScope for that
            int maxDeclareOffset = currentScope.getElements().isEmpty() ? endOffset : currentScope.getElements().get(0).getOffset();
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
            for (int j = lastDeclareIndex; j < visitor.getDeclareStatements().size(); j++) {
                if (maxDeclareOffset < visitor.getDeclareStatements().get(j).getStartOffset()) {
                    break;
                }
                startOffset = Math.max(startOffset, visitor.getDeclareStatements().get(j).getEndOffset());
                lastDeclareIndex = j;
            }
            usesInsertStringHelper.namespaceChange(startOffset);
            insertString(usesInsertStringHelper, usePartsMap.get(mapKey));
        }

        // it could be safely deleted after because editList offsets have no overlaps with old ones
        for (Map.Entry<Integer, Integer> entry : usesInsertStringHelper.getUsesToRemove().entrySet()) {
            int useStartOffset;
            if (usesInsertStringHelper.needToSaveHeadingWhitespace(entry.getKey())) {
                useStartOffset = entry.getKey();
            } else {
                useStartOffset = getOffsetWithoutLeadingWhitespaces(entry.getKey());
                if (usesInsertStringHelper.needToReplaceFollowingWhitespace(useStartOffset)) {
                    usesInsertStringHelper.skipFollowingWhitespaceReplace(useStartOffset);
                }
            }
            isEdited = true;
            editList.replace(useStartOffset, entry.getValue() - useStartOffset, EMPTY_STRING, false, 0);
        }
        // we have to change following whitespaces to one new line if there was appended new use
        // and wasn't removed old, which was first before
        for (Integer entry : usesInsertStringHelper.getFollowingWhitespaceReplaceOffsets()) {
            int useEndOffset = getOffsetWithoutFollowingWhitespaces(entry);
            if (useEndOffset != entry) {
                isEdited = true;
                editList.replace(entry, useEndOffset - entry, NEW_LINE, false, 0);
            }
        }

        if (!isEdited) {
            StatusDisplayer.getDefault().setStatusText(Bundle.FixUsesPerformer_noChanges());
        }
    }

    private List<UsePart> processScopeDeclaredUses(NamespaceScope namespaceScope, List<UsePart> useParts, UsesInsertStringHelper usesInsertStringHelper) {
        for (GroupUseScope groupUseElement : namespaceScope.getDeclaredGroupUses()) {
            for (UseScope useElement : groupUseElement.getUseScopes()) {
                if (!processUseElement(useElement, useParts)) {
                    usesInsertStringHelper.addToRemoveMap(useElement.getOffset());
                }
            }
        }
        for (UseScope useElement : namespaceScope.getDeclaredSingleUses()) {
            assert !useElement.isPartOfGroupUse() : useElement;
            if (!processUseElement(useElement, useParts)) {
                usesInsertStringHelper.addToRemoveMap(useElement.getOffset());
            }
        }
        return useParts;
    }

    private int getNamespaceScopeOffset(NamespaceScope scope) {
        int useScopeStart = scope.getBlockRange().getStart();
        if (useScopeStart == 0) {
            // this is check for case when we don't have namespace declaration in file
            // such <?php but tag could be not on the first line, also we put statements
            // after phpdoc blocks due to PSR/PER
            baseDocument.readLock();
            try {
                TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPositionedSequence(baseDocument, scope.getBlockRange().getEnd());
                if (ts != null) {
                    LexUtilities.findPreviousToken(ts, Arrays.asList(PHPTokenId.PHP_OPENTAG, PHPTokenId.PHPDOC_COMMENT_END));
                    if (ts.token().id().equals(PHPTokenId.PHP_OPENTAG) || ts.token().id().equals(PHPTokenId.PHPDOC_COMMENT_END)) {
                        useScopeStart = ts.offset() + ts.token().length();
                    }
                }
            } finally {
                baseDocument.readUnlock();
            }
        } else {
            //because when semicolon in the end of a namespace, the block starts
            //after semicolon, but for brace it starts before curly open
            //we can't just add +1 because of such case <?php namespace NS;?>
            baseDocument.readLock();
            try {
                if (LexUtilities.getTokenChar(baseDocument, useScopeStart) == CURLY_OPEN) {
                    useScopeStart++;
                }
            } finally {
                baseDocument.readUnlock();
            }
        }
        return useScopeStart;
    }

    private boolean shouldAppendLineAfter(int lineOffset) {
        try {
            return LineDocumentUtils.getNextNonWhitespace(baseDocument, lineOffset, LineDocumentUtils.getLineEnd(baseDocument, lineOffset)) != -1;
        }  catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    private void replaceUnimportedItems() {
        for (ImportData.DataItem dataItem : importData.getItemsToReplace()) {
            ItemVariant defaultVariant = dataItem.getDefaultVariant();
            for (UsedNamespaceName usedNamespaceName : dataItem.getUsedNamespaceNames()) {
                isEdited = true;
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
                        && dataItems.get(j).getUsedNamespaceNames().get(0).getInScope() == dataItems.get(i).getUsedNamespaceNames().get(0).getInScope()
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

    private boolean processUseElement(final UseScope useElement, final List<UsePart> useParts) {
        if (isUsed(useElement) || !removeUnusedUses) {
            AliasedName aliasedName = useElement.getAliasedName();
            if (aliasedName != null) {
                useParts.add(new UsePart(
                        modifyUseName(aliasedName.getRealName().toString()) + AS_CONCAT + aliasedName.getAliasName(),
                        UsePart.Type.create(useElement.getType()),
                        useElement.getOffset()));
            } else {
                useParts.add(new UsePart(
                        modifyUseName(useElement.getName()),
                        UsePart.Type.create(useElement.getType()),
                        useElement.getOffset()));
            }
            return true;
        }
        return false;
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

    private void insertString(final UsesInsertStringHelper usesInsertStringHelper, final List<UsePart> useParts) {
        if (useParts.isEmpty()) {
            return;
        }

        if (putInPSR12Order) {
            Collections.sort(useParts, (u1, u2) -> {
                int result = 0;
                if (UsePart.Type.TYPE.equals(u1.getType()) && UsePart.Type.TYPE.equals(u2.getType())) {
                    result = 0;
                } else if (UsePart.Type.TYPE.equals(u1.getType()) && UsePart.Type.CONST.equals(u2.getType())) {
                    result = -1;
                } else if (UsePart.Type.TYPE.equals(u1.getType()) && UsePart.Type.FUNCTION.equals(u2.getType())) {
                    result = -1;
                } else if (UsePart.Type.CONST.equals(u1.getType()) && UsePart.Type.TYPE.equals(u2.getType())) {
                    result = 1;
                } else if (UsePart.Type.CONST.equals(u1.getType()) && UsePart.Type.CONST.equals(u2.getType())) {
                    result = 0;
                } else if (UsePart.Type.CONST.equals(u1.getType()) && UsePart.Type.FUNCTION.equals(u2.getType())) {
                    result = 1;
                } else if (UsePart.Type.FUNCTION.equals(u1.getType()) && UsePart.Type.TYPE.equals(u2.getType())) {
                    result = 1;
                } else if (UsePart.Type.FUNCTION.equals(u1.getType()) && UsePart.Type.CONST.equals(u2.getType())) {
                    result = -1;
                } else if (UsePart.Type.FUNCTION.equals(u1.getType()) && UsePart.Type.FUNCTION.equals(u2.getType())) {
                    result = 0;
                }
                return result == 0 ? u1.getTextPart().compareToIgnoreCase(u2.getTextPart()) : result;
            });
        } else {
            Collections.sort(useParts);
        }

        String indentString = null;
        if (options.preferGroupUses()
                || options.preferMultipleUseStatementsCombined()) {
            CodeStyle codeStyle = CodeStyle.get(baseDocument);
            indentString = IndentUtils.createIndentString(codeStyle.getIndentSize(), codeStyle.expandTabToSpaces(), codeStyle.getTabSize());
        }

        usesInsertStringHelper.setWrapInNewLinesOnAppend(useParts.size() == 1);
        if (options.preferGroupUses() && usesInsertStringHelper.canGroupUses()
                && options.getPhpVersion().compareTo(PhpVersion.PHP_70) >= 0) {
            createStringForGroupUse(usesInsertStringHelper, useParts, indentString);
        } else if (options.preferMultipleUseStatementsCombined()) {
            createStringForMultipleUse(usesInsertStringHelper, useParts, indentString);
        } else {
            createStringForCommonUse(usesInsertStringHelper, useParts);
        }

        if (!usesInsertStringHelper.isAppendUses()) {
            try {
                String replaceString = baseDocument.getText(usesInsertStringHelper.getInitialStartOffset(), usesInsertStringHelper.getStartOffset() - usesInsertStringHelper.getInitialStartOffset());
                if (replaceString.equals(usesInsertStringHelper.getResultString())) {
                    usesInsertStringHelper.resetResultString();
                    return;
                }
            } catch (BadLocationException ex) {
                LOGGER.log(Level.WARNING, "Incorrect offset for uses replacement: {0}", ex.offsetRequested()); // NOI18N
            }
            this.isEdited = true;
            usesInsertStringHelper.confirmResultString();

            if (shouldAppendLineAfter(usesInsertStringHelper.getStartOffset())) {
                editList.replace(usesInsertStringHelper.getStartOffset(), 0, NEW_LINE, false, 0);
            }
        }
    }

    private void createStringForGroupUse(final UsesInsertStringHelper usesInsertStringHelper, List<UsePart> useParts, String indentString) {
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
                    assert false : "Unknown type: " + usePart.getType();
            }
        }
        // types
        boolean isFirstNewType = typeUseParts.size() == 1 && typeUseParts.get(0).getOffset() == 0;
        usesInsertStringHelper.setWrapInNewLinesOnAppend(useParts.size() > 1 && isFirstNewType && putInPSR12Order);
        createStringForGroupUse(usesInsertStringHelper, indentString, USE_PREFIX, typeUseParts);
        if (putInPSR12Order) {
            if (!functionUseParts.isEmpty() && usesInsertStringHelper.getResultString().length() > 0) {
                usesInsertStringHelper.appendToResultString(NEW_LINE);
            }
            // functions
            boolean isFirstNewFunction = functionUseParts.size() == 1 && functionUseParts.get(0).getOffset() == 0;
            usesInsertStringHelper.setWrapInNewLinesOnAppend(isFirstNewFunction);
            createStringForGroupUse(usesInsertStringHelper, indentString, USE_FUNCTION_PREFIX, functionUseParts);

            if (!constUseParts.isEmpty() && usesInsertStringHelper.getResultString().length() > 0) {
                usesInsertStringHelper.appendToResultString(NEW_LINE);
            }
            // constants
            boolean isFirstNewConst = constUseParts.size() == 1 && constUseParts.get(0).getOffset() == 0;
            usesInsertStringHelper.setWrapInNewLinesOnAppend(isFirstNewConst);
            createStringForGroupUse(usesInsertStringHelper, indentString, USE_CONST_PREFIX, constUseParts);
        } else {
            // constants
            createStringForGroupUse(usesInsertStringHelper, indentString, USE_CONST_PREFIX, constUseParts);
            // functions
            createStringForGroupUse(usesInsertStringHelper, indentString, USE_FUNCTION_PREFIX, functionUseParts);
        }
    }

    private List<String> usePartsToNamespaces(List<UsePart> useParts) {
        return useParts.stream()
                .map(part -> part.getTextPart())
                .collect(Collectors.toList());
    }

    private void createStringForGroupUse(final UsesInsertStringHelper usesInsertStringHelper, String indentString, String usePrefix, List<UsePart> useParts) {
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
                    processGroupedUseParts(usesInsertStringHelper, indentString, usePrefix, lastGroupUsePrefix, groupedUseParts);
                }
                lastGroupUsePrefix = groupUsePrefix;
                processNonGroupedUseParts(usesInsertStringHelper, indentString, nonGroupedUseParts);
                groupedUseParts.add(usePart);
            } else {
                processGroupedUseParts(usesInsertStringHelper, indentString, usePrefix, lastGroupUsePrefix, groupedUseParts);
                nonGroupedUseParts.add(usePart);
            }
        }
        processNonGroupedUseParts(usesInsertStringHelper, indentString, nonGroupedUseParts);
        processGroupedUseParts(usesInsertStringHelper, indentString, usePrefix, lastGroupUsePrefix, groupedUseParts);
    }

    private void processNonGroupedUseParts(final UsesInsertStringHelper usesInsertStringHelper, String indentString, List<UsePart> nonGroupedUseParts) {
        if (nonGroupedUseParts.isEmpty()) {
            return;
        }
        // it is hard to handle multiple use and group at once on appendUses
        // because there is a probability of recursive changes, so we just fallback
        // to common use insertion in such case to avoid complexity of code
        if (options.preferMultipleUseStatementsCombined() && !usesInsertStringHelper.isAppendUses()) {
            createStringForMultipleUse(usesInsertStringHelper, nonGroupedUseParts, indentString);
        } else {
            createStringForCommonUse(usesInsertStringHelper, nonGroupedUseParts);
        }
        nonGroupedUseParts.clear();
    }

    private void processGroupedUseParts(final UsesInsertStringHelper usesInsertStringHelper, String indentString, String usePrefix, String groupUsePrefix, List<UsePart> groupedUseParts) {
        if (groupedUseParts.isEmpty()) {
            return;
        }
        StringBuilder insertString = new StringBuilder();
        assert groupUsePrefix != null : groupedUseParts;
        String properGroupUsePrefix = modifyUseName(groupUsePrefix);
        insertString.append(usePrefix).append(properGroupUsePrefix).append(CURLY_OPEN).append(NEW_LINE);
        boolean first = true;
        int prefixLength = properGroupUsePrefix.length();
        boolean hasChanges = false;
        int firstElementOffset = 0;
        for (UsePart groupUsePart : groupedUseParts) {
            usesInsertStringHelper.addToPossibleRemoveMap(groupUsePart.getOffset());
            usesInsertStringHelper.moveEndOffset(groupUsePart.getOffset());
            if (groupUsePart.getOffset() == 0) {
                hasChanges = true;
            } else {
                firstElementOffset = (groupUsePart.getOffset() == 0 || (firstElementOffset != 0 && firstElementOffset < groupUsePart.getOffset()))
                    ? firstElementOffset : groupUsePart.getOffset();
            }
            if (first) {
                first = false;
            } else {
                insertString.append(COMMA).append(NEW_LINE);
            }
            insertString.append(indentString).append(groupUsePart.getTextPart().substring(prefixLength));
        }
        insertString.append(NEW_LINE).append(CURLY_CLOSE).append(SEMICOLON);
        usesInsertStringHelper.applyDirectChangeAtOrAppend(firstElementOffset, hasChanges, insertString.toString());
        groupedUseParts.clear();
    }

    private void createStringForMultipleUse(final UsesInsertStringHelper usesInsertStringHelper, List<UsePart> useParts, String indentString) {
        if (useParts.isEmpty()) {
            return;
        }
        StringBuilder insertString = new StringBuilder();
        UsePart.Type lastUsePartType = null;
        boolean hasChanges = false;
        int firstElementOffset = 0;
        for (Iterator<UsePart> it = useParts.iterator(); it.hasNext(); ) {
            UsePart usePart = it.next();
            usesInsertStringHelper.addToPossibleRemoveMap(usePart.getOffset());
            usesInsertStringHelper.moveEndOffset(usePart.getOffset());
            if (usePart.getOffset() == 0) {
                hasChanges = true;
            } else {
                firstElementOffset = (usePart.getOffset() == 0 || (firstElementOffset != 0 && firstElementOffset < usePart.getOffset()))
                    ? firstElementOffset : usePart.getOffset();
            }
            if (lastUsePartType != null) {
                if (lastUsePartType == usePart.getType()) {
                    insertString.append(COMMA).append(NEW_LINE).append(indentString);
                } else {
                    insertString.append(SEMICOLON);
                    if (!usesInsertStringHelper.applyDirectChangeAtOrAppend(firstElementOffset, hasChanges, insertString.toString())) {
                        firstElementOffset = 0;
                    }
                    hasChanges = false;
                    insertString.setLength(0);
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
        insertString.append(SEMICOLON);
        usesInsertStringHelper.applyDirectChangeAtOrAppend(firstElementOffset, hasChanges, insertString.toString());
    }

    private void createStringForCommonUse(final UsesInsertStringHelper usesInsertStringHelper, List<UsePart> useParts) {
        StringBuilder insertString = new StringBuilder();
        UsePart.Type lastUseType = null;
        boolean hasChanges = false;
        int typeElementsCount = 0;
        int firstElementOffset = 0;
        for (UsePart usePart : useParts) {
            if (putInPSR12Order && lastUseType != null && lastUseType != usePart.getType()) {
                if (usesInsertStringHelper.isAppendUses()) {
                    if (hasChanges) {
                        usesInsertStringHelper.setWrapInNewLinesOnAppend(typeElementsCount == 1);
                        usesInsertStringHelper.confirmResultStringAt(firstElementOffset);
                    } else {
                        usesInsertStringHelper.resetResultString();
                        usesInsertStringHelper.moveStartOffsetToEnd();
                        firstElementOffset = 0;
                    }
                    hasChanges = false;
                } else {
                    usesInsertStringHelper.appendToResultString(NEW_LINE);
                }
                typeElementsCount = 0;
            }
            // here we check changes in whole type block for PSR12Order
            hasChanges = hasChanges || usePart.getOffset() == 0;
            firstElementOffset = (usePart.getOffset() == 0 || (firstElementOffset != 0 && firstElementOffset < usePart.getOffset()))
                    ? firstElementOffset : usePart.getOffset();
            lastUseType = usePart.getType();
            typeElementsCount++;

            insertString.append(usePart.getUsePrefix()).append(usePart.getTextPart()).append(SEMICOLON);
            if (usesInsertStringHelper.isAppendUses() && !putInPSR12Order) {
                if (usesInsertStringHelper.moveEndOffset(usePart.getOffset())) {
                    usesInsertStringHelper.addToPossibleRemoveMap(usePart.getOffset());
                    usesInsertStringHelper.moveStartOffsetToEnd();
                }
                // here we check inline change only
                hasChanges = usePart.getOffset() == 0;
                if (hasChanges) {
                    usesInsertStringHelper.applyDirectChange(insertString.toString());
                } else {
                    usesInsertStringHelper.resetPossibleRemoves();
                }
            } else {
                usesInsertStringHelper.addToPossibleRemoveMap(usePart.getOffset());
                usesInsertStringHelper.moveEndOffset(usePart.getOffset());
                usesInsertStringHelper.appendToResultString(insertString.toString());
            }
            insertString.setLength(0);
        }
        if (putInPSR12Order && usesInsertStringHelper.isAppendUses()) {
            if (hasChanges) {
                usesInsertStringHelper.setWrapInNewLinesOnAppend(typeElementsCount == 1);
                usesInsertStringHelper.confirmResultStringAt(firstElementOffset);
            } else {
                usesInsertStringHelper.resetResultString();
                usesInsertStringHelper.moveStartOffsetToEnd();
            }
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

    private int getOffsetWithoutFollowingWhitespaces(final int endOffset) {
        int result = endOffset;
        baseDocument.readLock();
        try {
            TokenSequence<PHPTokenId> ts = LexUtilities.getPHPTokenSequence(baseDocument, endOffset);
            if (ts != null) {
                ts.move(endOffset);
                while (ts.moveNext() && ts.token().id().equals(PHPTokenId.WHITESPACE)) {
                    result = ts.offset() + ts.token().length();
                }
            }
        } finally {
            baseDocument.readUnlock();
        }
        return result;
    }

    //~ inner classes
    private static class UsesInsertStringHelper {
        private final EditList editList;
        private final Map<Integer, OffsetRange> usedRanges;
        private final boolean appendUses;
        private final boolean hasMultipleUses;
        private final StringBuilder resultString = new StringBuilder();
        private final Set<Integer> followingWhitespaceReplaceOffset = new HashSet<>();
        private final Set<Integer> savedHeadingWhitespaceOffset = new HashSet<>();
        private final Map<Integer, Integer> usesToRemove = new HashMap<>();
        private final List<Integer> usesToRemoveOffsetTmp = new ArrayList<>();
        private int startOffset;
        private int initialStartOffset;
        private int endOffset;
        private boolean wrapInNewLines = false;

        UsesInsertStringHelper(EditList editList, Map<Integer, OffsetRange> usedRanges, boolean hasMultipleUses, boolean appendUses, int startOffset) {
            this.editList = editList;
            this.usedRanges = usedRanges;
            this.startOffset = startOffset;
            this.initialStartOffset = startOffset;
            this.hasMultipleUses = hasMultipleUses;
            this.appendUses = appendUses;
        }

        public OffsetRange getUsedRangeOffset(int offset) {
            return usedRanges.get(offset);
        }

        public void addToPossibleRemoveMap(int offset) {
            if (usedRanges.get(offset) != null) {
                usesToRemoveOffsetTmp.add(offset);
            }
        }

        public Map<Integer, Integer> getUsesToRemove() {
            return Collections.unmodifiableMap(usesToRemove);
        }

        public int getStartOffset() {
            return startOffset;
        }

        public int getInitialStartOffset() {
            return initialStartOffset;
        }

        public void namespaceChange(int startOffset) {
            this.startOffset = startOffset;
            this.initialStartOffset = startOffset;
            this.endOffset = startOffset;
        }

        public Set<Integer> getFollowingWhitespaceReplaceOffsets() {
            return Collections.unmodifiableSet(followingWhitespaceReplaceOffset);
        }

        public boolean needToSaveHeadingWhitespace(int offset) {
            return savedHeadingWhitespaceOffset.contains(offset);
        }

        public boolean needToReplaceFollowingWhitespace(int offset) {
            return followingWhitespaceReplaceOffset.contains(offset);
        }

        public void skipFollowingWhitespaceReplace(int offset) {
            followingWhitespaceReplaceOffset.remove((Integer) offset);
        }

        public boolean moveEndOffset(int removedUseOffset) {
            OffsetRange removed = this.getUsedRangeOffset(removedUseOffset);
            if (removed != null) {
                this.endOffset = removed.getEnd() > endOffset ? removed.getEnd() : endOffset;
                return true;
            }
            return false;
        }

        public void moveStartOffsetToEnd() {
            this.startOffset = this.endOffset;
        }

        public boolean applyDirectChangeAtOrAppend(int firstElementOffset, boolean hasChanges, String insertString) {
            if (isAppendUses()) {
                if (hasChanges) {
                    applyDirectChangeAt(firstElementOffset, insertString);
                } else {
                    resetPossibleRemoves();
                    moveStartOffsetToEnd();
                    return false;
                }
            } else {
                appendToResultString(insertString);
            }
            return true;
        }

        public void applyDirectChangeAt(int offset, String insertString) {
            if (offset == 0 || this.getUsedRangeOffset(offset) == null) {
                applyDirectChange(insertString);
                return;
            }
            int currentStart = this.startOffset;
            this.startOffset = this.getUsedRangeOffset(offset).getEnd();
            savedHeadingWhitespaceOffset.add(this.getUsedRangeOffset(offset).getStart());
            // remove new line because of insert on existing place
            applyDirectChange(insertString.substring(1));
            this.startOffset = currentStart;
        }

        public void applyDirectChange(String insertString) {
            for (Integer offset : usesToRemoveOffsetTmp) {
                addToRemoveMap(offset);
            }

            if (shouldAppendLineBefore()) {
                editList.replace(getStartOffset(), 0, NEW_LINE, false, 0);
                followingWhitespaceReplaceOffset.add(getStartOffset());
                editList.replace(getStartOffset(), 0, insertString, false, 0);
            } else {
                editList.replace(getStartOffset(), 0, insertString, false, 0);
            }

            if (wrapInNewLines) {
                editList.replace(getStartOffset(), 0, NEW_LINE, false, 0);
                wrapInNewLines = false;
            }
        }

        public void setWrapInNewLinesOnAppend(boolean value) {
            wrapInNewLines = value;
        }

        public void resetPossibleRemoves() {
            usesToRemoveOffsetTmp.clear();
        }

        public void confirmResultStringAt(int offset) {
            if (offset == 0 || this.getUsedRangeOffset(offset) == null) {
                confirmResultString();
                return;
            }
            int currentStart = this.startOffset;
            this.startOffset = this.getUsedRangeOffset(offset).getEnd();
            savedHeadingWhitespaceOffset.add(this.getUsedRangeOffset(offset).getStart());
            // remove new line because of insert on existing place
            this.resultString.deleteCharAt(0);
            confirmResultString();
            this.startOffset = currentStart;
        }

        public void confirmResultString() {
            if (resultString.length() > 0) {
                for (Integer offset : usesToRemoveOffsetTmp) {
                    addToRemoveMap(offset);
                }
                if (appendUses && shouldAppendLineBefore()) {
                    editList.replace(getStartOffset(), 0, NEW_LINE, false, 0);
                    followingWhitespaceReplaceOffset.add(getStartOffset());
                }
                editList.replace(getStartOffset(), 0, resultString.toString(), false, 0);
                if (appendUses && wrapInNewLines) {
                    editList.replace(getStartOffset(), 0, NEW_LINE, false, 0);
                    wrapInNewLines = false;
                }
                resultString.setLength(0);
            }
        }

        public void resetResultString() {
            resetPossibleRemoves();
            resultString.setLength(0);
        }

        public boolean isAppendUses() {
            return appendUses;
        }

        public boolean canGroupUses() {
            return !appendUses || (appendUses && !hasMultipleUses);
        }

        public void addToRemoveMap(int offset) {
            if (usedRanges.get(offset) == null) {
                return;
            }
            usesToRemove.put(usedRanges.get(offset).getStart(), usedRanges.get(offset).getEnd());
        }

        public String getResultString() {
            return resultString.toString();
        }

        public void appendToResultString(String insertString) {
            if (resultString.length() == 0 && !appendUses) {
                resultString.append(NEW_LINE);
            }
            resultString.append(insertString);
        }

        private boolean shouldAppendLineBefore() {
            return (startOffset == initialStartOffset && !followingWhitespaceReplaceOffset.contains(initialStartOffset))
                    || (wrapInNewLines && appendUses);
        }
    }

    private static class CheckVisitor extends DefaultVisitor {

        private List<DeclareStatement> declareStatements = new ArrayList<>();
        private List<NamespaceDeclaration> globalNamespaceDeclarations = new ArrayList<>();
        private final Map<Integer, OffsetRange> usedRanges = new HashMap<>();
        private int globalNamespaceEndOffset = 0;
        private boolean hasMultipleUses = false;

        public List<DeclareStatement> getDeclareStatements() {
            return Collections.unmodifiableList(declareStatements);
        }

        public List<NamespaceDeclaration> getGlobalNamespaceDeclarations() {
            return Collections.unmodifiableList(globalNamespaceDeclarations);
        }

        public Map<Integer, OffsetRange> getUsedRanges() {
            return Collections.unmodifiableMap(usedRanges);
        }

        public int getGlobalNamespaceEndOffset() {
            return globalNamespaceEndOffset;
        }

        public boolean hasMultipleUses() {
            return hasMultipleUses;
        }

        @Override
        public void visit(UseStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (node.getParts().size() > 1) {
                hasMultipleUses = true;
            }
            if (node.getParts().get(0) instanceof GroupUseStatementPart) {
                GroupUseStatementPart part = (GroupUseStatementPart) node.getParts().get(0);
                if (part.getItems().size() > 0) {
                    usedRanges.put(part.getItems().get(0).getStartOffset(), new OffsetRange(node.getStartOffset(), node.getEndOffset()));
                }
            } else {
                usedRanges.put(node.getParts().get(0).getStartOffset(), new OffsetRange(node.getStartOffset(), node.getEndOffset()));
            }
            super.visit(node);
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
            if (globalNamespaceEndOffset == 0 || globalNamespaceEndOffset > declaration.getBody().getEndOffset()) {
                globalNamespaceEndOffset = declaration.getBody().getStartOffset();
            }
            if (declaration.isBracketed() && declaration.getName() == null) {
                globalNamespaceDeclarations.add(declaration);
            }
            super.visit(declaration);
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
            return new UsePart(hasAlias() ? usePartToSanitization.getTextPart() + AS_CONCAT + alias : usePartToSanitization.getTextPart(), usePartToSanitization.getType(), usePartToSanitization.getOffset(), usePartToSanitization.isFromAliasedElement());
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
        }

        private final String textPart;
        private final Type type;
        private final boolean isFromAliasedElement;
        private final int offset;

        private UsePart(String textPart, Type type, int offset, boolean isFromAliasedElement) {
            this.textPart = textPart;
            this.type = type;
            this.offset = offset;
            this.isFromAliasedElement = isFromAliasedElement;
        }

        private UsePart(String textPart, Type type, int offset) {
            this(textPart, type, offset, false);
        }

        public String getTextPart() {
            return textPart;
        }

        public Type getType() {
            return type;
        }

        public int getOffset() {
            return offset;
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
