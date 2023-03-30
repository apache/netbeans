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
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.Objects;
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

    public void perform() {
        final Document document = parserResult.getSnapshot().getSource().getDocument(false);
        if (document instanceof BaseDocument) {
            baseDocument = (BaseDocument) document;
            editList = new EditList(baseDocument);
            namespaceScope = ModelUtils.getNamespaceScope(parserResult.getModel().getFileScope(), importData.caretPosition);
            assert namespaceScope != null;
            processSelections();
            editList.apply();
        }
    }

    @NbBundle.Messages("FixUsesPerformer.noChanges=Fix imports: No Changes")
    private void processSelections() {
        final List<ImportData.DataItem> dataItems = resolveDuplicateSelections();
        TreeMap<Integer, List<UsePart>> usePartsMap = new TreeMap<>();
        assert selections.size() <= dataItems.size()
                : "The selections size must not be larger than the dataItems size. selections size: " + selections.size() + " > dataItems size: " + dataItems.size(); // NOI18N
        for (int i = 0; i < selections.size(); i++) {
            ItemVariant itemVariant = selections.get(i);
            // we shouldn't use itemVariant if there is no any real dataItem related to it
            if (itemVariant.canBeUsed() && !dataItems.get(i).getUsedNamespaceNames().isEmpty()) {
                NamespaceScope currentScope = dataItems.get(i).getUsedNamespaceNames().get(0).getInScope();
                int mapKey = currentScope.getBlockRange().getStart();
                if (usePartsMap.get(mapKey) == null) {
                    usePartsMap.put(mapKey, processScopeDeclaredUses(currentScope, new ArrayList<>()));
                }

                SanitizedUse sanitizedUse = new SanitizedUse(
                        new UsePart(modifyUseName(itemVariant.getName()), UsePart.Type.create(itemVariant.getType()), itemVariant.isFromAliasedElement()),
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

        CheckVisitor visitor = new CheckVisitor();
        Program program = parserResult.getProgram();
        if (program != null) {
            program.accept(visitor);
        }

        boolean emptyString = true;
        int lastUsedRangeIndex = 0;
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
                usePartsMap.put(mapKey, processScopeDeclaredUses(currentScope, new ArrayList<>()));
            }

            int startOffset = getNamespaceScopeOffset(currentScope);
            int endOffset = currentScope.isDefaultNamespace() && visitor.getGlobalNamespaceEndOffset() > 0
                    ? visitor.getGlobalNamespaceEndOffset()
                    : currentScope.getBlockRange().getEnd();

            int compareStringOffsetStart = 0;
            List <OffsetRange> replace = new ArrayList();
            for (int i = lastUsedRangeIndex; i < visitor.getUsedRanges().size(); i++) {
                OffsetRange offsetRange = visitor.getUsedRanges().get(i);
                if (endOffset < offsetRange.getStart()) {
                    break;
                }
                lastUsedRangeIndex = i;
                if (startOffset > offsetRange.getStart()) {
                    continue;
                }

                compareStringOffsetStart = compareStringOffsetStart > 0 ? compareStringOffsetStart : offsetRange.getStart();
                int useStartOffset = getOffsetWithoutLeadingWhitespaces(offsetRange.getStart());
                replace.add(new OffsetRange(useStartOffset, offsetRange.getEnd()));
                startOffset = offsetRange.getEnd();
            }

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

            String insertString = createInsertString(usePartsMap.get(mapKey));
            // avoid being recognized as a modified file
            if (insertString.isEmpty()) {
                // remove unused namespaces if exists
                for (OffsetRange offsetRange : replace) {
                    editList.replace(offsetRange.getStart(), offsetRange.getLength(), EMPTY_STRING, false, 0);
                    emptyString = false;
                }
                continue;
            }

            try {
                // get -2/+2 for new lines before string
                String replaceString = compareStringOffsetStart > 1 ? baseDocument.getText(compareStringOffsetStart - 2, startOffset - compareStringOffsetStart + 2) : "";
                if (replaceString.equals(insertString)) {
                    continue;
                }

                for (OffsetRange offsetRange : replace) {
                    editList.replace(offsetRange.getStart(), offsetRange.getLength(), EMPTY_STRING, false, 0);
                }
                editList.replace(startOffset, 0, insertString, false, 0);
                if (shouldAppendLineAfter(startOffset)) {
                    editList.replace(startOffset, 0, NEW_LINE, false, 0);
                }
                emptyString = false;
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        if (emptyString) {
            StatusDisplayer.getDefault().setStatusText(Bundle.FixUsesPerformer_noChanges());
        }
    }

    private List<UsePart> processScopeDeclaredUses(NamespaceScope namespaceScope, List<UsePart> useParts) {
        for (GroupUseScope groupUseElement : namespaceScope.getDeclaredGroupUses()) {
            for (UseScope useElement : groupUseElement.getUseScopes()) {
                processUseElement(useElement, useParts);
            }
        }
        for (UseScope useElement : namespaceScope.getDeclaredSingleUses()) {
            assert !useElement.isPartOfGroupUse() : useElement;
            processUseElement(useElement, useParts);
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
                        useScopeStart = ts.offset() + ts.token().length() + 1;
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

    private String createInsertString(final List<UsePart> useParts) {
        StringBuilder insertString = new StringBuilder();
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
            insertString.append(createStringForGroupUse(useParts, indentString));
        } else if (options.preferMultipleUseStatementsCombined()) {
            insertString.append(createStringForMultipleUse(useParts, indentString));
        } else {
            insertString.append(createStringForCommonUse(useParts));
        }
        return insertString.toString();
    }

    private String createStringForGroupUse(List<UsePart> useParts, String indentString) {
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
        StringBuilder insertString = new StringBuilder();
        // types
        createStringForGroupUse(insertString, indentString, USE_PREFIX, typeUseParts);
        if (putInPSR12Order) {
            if (!functionUseParts.isEmpty()) {
                appendNewLine(insertString);
            }
            // functions
            createStringForGroupUse(insertString, indentString, USE_FUNCTION_PREFIX, functionUseParts);

            if (!constUseParts.isEmpty()) {
                appendNewLine(insertString);
            }
            // constants
            createStringForGroupUse(insertString, indentString, USE_CONST_PREFIX, constUseParts);
        } else {
            // constants
            createStringForGroupUse(insertString, indentString, USE_CONST_PREFIX, constUseParts);
            // functions
            createStringForGroupUse(insertString, indentString, USE_FUNCTION_PREFIX, functionUseParts);
        }
        return insertString.toString();
    }

    private void appendNewLine(StringBuilder insertString) {
        if (insertString.length() > 0) {
            insertString.append(NEW_LINE);
        }
    }

    private List<String> usePartsToNamespaces(List<UsePart> useParts) {
        return useParts.stream()
                .map(part -> part.getTextPart())
                .collect(Collectors.toList());
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
            if (putInPSR12Order && lastUseType != null && lastUseType != usePart.getType()) {
                appendNewLine(result);
            }
            result.append(usePart.getUsePrefix()).append(usePart.getTextPart()).append(SEMICOLON);
            lastUseType = usePart.getType();
        }
        return result.toString();
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

    //~ inner classes
    private static class CheckVisitor extends DefaultVisitor {

        private List<DeclareStatement> declareStatements = new ArrayList<>();
        private List<NamespaceDeclaration> globalNamespaceDeclarations = new ArrayList<>();
        private final List<OffsetRange> usedRanges = new LinkedList<>();
        private int globalNamespaceEndOffset = 0;

        public List<DeclareStatement> getDeclareStatements() {
            return Collections.unmodifiableList(declareStatements);
        }

        public List<NamespaceDeclaration> getGlobalNamespaceDeclarations() {
            return Collections.unmodifiableList(globalNamespaceDeclarations);
        }

        public List<OffsetRange> getUsedRanges() {
            return Collections.unmodifiableList(usedRanges);
        }

        public int getGlobalNamespaceEndOffset() {
            return globalNamespaceEndOffset;
        }

        @Override
        public void visit(UseStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            usedRanges.add(new OffsetRange(node.getStartOffset(), node.getEndOffset()));
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
