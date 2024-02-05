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
package org.netbeans.modules.php.editor.codegen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.AliasedName;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.indent.CodeStyle;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.GroupUseScope;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.UseScope;
import static org.netbeans.modules.php.editor.model.UseScope.Type.CONST;
import static org.netbeans.modules.php.editor.model.UseScope.Type.FUNCTION;
import static org.netbeans.modules.php.editor.model.UseScope.Type.TYPE;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.DeclareStatement;
import org.netbeans.modules.php.editor.parser.astnodes.EmptyStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Statement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;

public final class AutoImport {

    private static final Logger LOGGER = Logger.getLogger(AutoImport.class.getName());
    public static final String PARAM_NAME = "php-auto-import"; // NOI18N
    public static final String PARAM_KEY_FQ_NAME = "fqName"; // NOI18N
    public static final String PARAM_KEY_ALIAS_NAME = "aliasName"; // NOI18N
    public static final String PARAM_KEY_USE_TYPE = "useType"; // NOI18N
    public static final String USE_TYPE = "type"; // NOI18N
    public static final String USE_FUNCTION = "function"; // NOI18N
    public static final String USE_CONST = "const"; // NOI18N

    private final PHPParseResult parserResult;

    public static boolean sameUseNameExists(String name, String fqName, UseScope.Type useScopeType, NamespaceScope namespaceScope) {
        Collection<? extends GroupUseScope> declaredGroupUses = namespaceScope.getDeclaredGroupUses();
        Collection<? extends UseScope> declaredSingleUses = namespaceScope.getDeclaredSingleUses();
        for (GroupUseScope declaredGroupUse : declaredGroupUses) {
            List<UseScope> useScopes = declaredGroupUse.getUseScopes();
            if (hasSameNameInSingleUses(name, fqName, useScopeType, useScopes)) {
                return true;
            }
        }
        return hasSameNameInSingleUses(name, fqName, useScopeType, declaredSingleUses);
    }

    private static boolean hasSameNameInSingleUses(String name, String fqName, UseScope.Type useScopeType, Collection<? extends UseScope> declaredSingleUses) {
        for (UseScope declaredSingleUse : declaredSingleUses) {
            UseScope.Type type = declaredSingleUse.getType();
            if (type != useScopeType
                    || fqName.equals(declaredSingleUse.getName())) {
                continue;
            }
            AliasedName aliasedName = declaredSingleUse.getAliasedName();
            String elementName;
            if (aliasedName != null) {
                elementName = aliasedName.getAliasName();
            } else {
                QualifiedName qualifiedName = QualifiedName.create(declaredSingleUse.getName());
                elementName = qualifiedName.getName();
            }
            if (name.equals(elementName)) {
                return true;
            }
        }
        return false;
    }

    public static UseScope.Type getUseScopeType(String useType) {
        UseScope.Type useScopeType = UseScope.Type.TYPE;
        switch (useType) {
            case AutoImport.USE_TYPE:
                useScopeType = UseScope.Type.TYPE;
                break;
            case AutoImport.USE_FUNCTION:
                useScopeType = UseScope.Type.FUNCTION;
                break;
            case AutoImport.USE_CONST:
                useScopeType = UseScope.Type.CONST;
                break;
            default:
                assert false : "Unknown type: " + useType; // NOI18N
                break;
        }
        return useScopeType;
    }

    public static AutoImport get(PHPParseResult parserResult) {
        return new AutoImport(parserResult);
    }

    private AutoImport(PHPParseResult parserResult) {
        this.parserResult = parserResult;
    }

    public void insert(Hints hints, int caretPosition) {
        insert(hints.getFqName(), hints.getAliasName(), hints.getUseScopeType(), caretPosition);
    }

    void insert(String fqInsertName, UseScope.Type type, int caretPosition) {
        insert(fqInsertName, CodeUtils.EMPTY_STRING, type, caretPosition);
    }

    void insert(String fqInsertName, String aliasName, UseScope.Type type, int caretPosition) {
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(parserResult, caretPosition);
        if (!canInsert(fqInsertName, namespaceScope)) {
            return;
        }
        assert namespaceScope != null;
        Document document = parserResult.getSnapshot().getSource().getDocument(false);
        if (document == null) {
            document = parserResult.getSnapshot().getSource().getDocument(true);
            LOGGER.log(Level.INFO, "document was opened forcibly"); // NOI18N
        }
        BaseDocument baseDocument = (document instanceof BaseDocument) ? (BaseDocument) document : null;
        if (baseDocument == null) {
            return;
        }
        String aliasedName = fqInsertName;
        if (!aliasName.isEmpty()) {
            aliasedName = aliasedName + " as " + aliasName; // NOI18N
        }
        AutoImportResolver resolver = AutoImportResolver.create(aliasedName, type, parserResult, namespaceScope, baseDocument);
        resolver.resolve();
        insertUseStatement(resolver, baseDocument);
    }

    private boolean canInsert(String fqInsertName, NamespaceScope namespaceScope) {
        boolean result = true;
        if (!fqInsertName.contains(CodeUtils.NS_SEPARATOR) && StringUtils.isEmpty(namespaceScope.getName())) {
            result = false;
        } else {
            String name = namespaceScope.getName();
            if (name.equals(QualifiedName.create(fqInsertName).getNamespaceName())) {
                result = false;
            }
        }
        return result;
    }

    private void insertUseStatement(AutoImportResolver resolver, BaseDocument baseDocument) {
        if (!resolver.canImport()) {
            return;
        }
        int insertOffset = resolver.getInsertOffset();
        String insertString = resolver.getInsertString();
        EditList editList = new EditList(baseDocument);
        editList.replace(insertOffset, 0, insertString, true, 0);
        editList.apply();
    }

    private static final class AutoImportResolver {

        private static final Logger LOGGER = Logger.getLogger(AutoImportResolver.class.getName());

        private final String insertName;
        private final UseScope.Type type;
        private final PHPParseResult parserResult;
        private final NamespaceScope namespaceScope;
        private final BaseDocument baseDocument;
        private final TokenSequence<PHPTokenId> tokenSequence;
        private final List<GroupUseScope> declaredGroupUses;
        private final List<UseScope> declaredSingleUses;
        private final List<UseScope> typeUseScopes = new ArrayList<>();
        private final List<UseScope> constUseScopes = new ArrayList<>();
        private final List<UseScope> functionUseScopes = new ArrayList<>();
        private final Map<String, UseScope> typeNames = new LinkedHashMap<>();
        private final Map<String, UseScope> constNames = new LinkedHashMap<>();
        private final Map<String, UseScope> functionNames = new LinkedHashMap<>();
        private int insertOffset = -1;
        private String insertString = CodeUtils.EMPTY_STRING;
        private int indexOfInsertName = -1;
        private boolean canImport = true;
        private boolean addNewLineBeforeUse = false;

        public static AutoImportResolver create(String insertName, UseScope.Type type, PHPParseResult parserResult, NamespaceScope namespaceScope, BaseDocument baseDocument) {
            Collection<? extends GroupUseScope> declaredGroupUses = namespaceScope.getDeclaredGroupUses();
            Collection<? extends UseScope> declaredSingleUses = namespaceScope.getDeclaredSingleUses();
            AutoImportResolver autoImportResolver = new AutoImportResolver(insertName, type, parserResult, namespaceScope, declaredGroupUses, declaredSingleUses, baseDocument);
            autoImportResolver.init();
            return autoImportResolver;
        }

        private AutoImportResolver(String insertName, UseScope.Type type, PHPParseResult parserResult, NamespaceScope namespaceScope, Collection<? extends GroupUseScope> declaredGroupUses, Collection<? extends UseScope> declaredSingleUses, BaseDocument baseDocument) {
            this.insertName = insertName;
            this.insertString = insertName;
            this.type = type;
            this.parserResult = parserResult;
            this.namespaceScope = namespaceScope;
            this.declaredGroupUses = new ArrayList<>(declaredGroupUses);
            this.declaredSingleUses = new ArrayList<>(declaredSingleUses);
            this.baseDocument = baseDocument;
            this.tokenSequence = parserResult.getSnapshot().getTokenHierarchy().tokenSequence(PHPTokenId.language());
        }

        public int getInsertOffset() {
            return insertOffset;
        }

        public String getInsertString() {
            return insertString;
        }

        public boolean canImport() {
            return canImport && insertOffset != -1;
        }

        public void resolve() {
            // get use scopes of curent use type
            Map<String, UseScope> useScopeNames = getNamedUseScopes(type);
            if (useScopeNames.keySet().contains(insertName)) {
                canImport = false;
                return;
            }
            List<String> names = getUseScopeNames(useScopeNames);
            indexOfInsertName = findInsertNameIndex(names);
            if (indexOfInsertName > 0) {
                // check whether we can insert it top of group use
                String name = names.get(indexOfInsertName);
                if (canInsertIntoNextGroupUse(name, useScopeNames)) {
                    processInsertingBeforeNextUse(names, useScopeNames, indexOfInsertName);
                } else {
                    processInsertingAfterPreviousUse(names, useScopeNames, indexOfInsertName);
                }
            } else if (indexOfInsertName == 0) {
                processInsertingBeforeNextUse(names, useScopeNames, 0);
            } else {
                processInserting();
            }
        }

        private boolean canInsertIntoNextGroupUse(String name, Map<String, UseScope> useScopeNames) {
            // check whether we can insert it top of group use
            UseScope useScope = useScopeNames.get(name);
            if (useScope != null && useScope.isPartOfGroupUse()) {
                String groupUseBaseName = getGroupUseBaseName(useScope);
                if (insertName.startsWith(groupUseBaseName)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isPartOfMultipleUse(UseScope useScope) {
            tokenSequence.move(useScope.getOffset());
            if (tokenSequence.moveNext()) {
                Token<? extends PHPTokenId> useToken = LexUtilities.findPreviousToken(tokenSequence, Arrays.asList(PHPTokenId.PHP_USE));
                assert useToken != null : "Use statement should start with \"use\", but not found it"; // NOI18N
                while (tokenSequence.moveNext()) {
                    if (tokenSequence.token().id() == PHPTokenId.PHP_SEMICOLON) {
                        break;
                    }
                    if (tokenSequence.token().id() == PHPTokenId.PHP_TOKEN
                            && TokenUtilities.equals(tokenSequence.token().text(), ",")) { // NOI18N
                        return true;
                    }
                }
            }
            return false;
        }

        private int getEndOfUseStetement(UseScope useScope) {
            tokenSequence.move(useScope.getOffset());
            while (tokenSequence.moveNext()) {
                if (tokenSequence.token().id() == PHPTokenId.PHP_SEMICOLON) {
                    return tokenSequence.offset() + tokenSequence.token().length();
                }
            }
            return getLineEnd(useScope.getOffset());
        }

        private void processInsertingAfterPreviousUse(List<String> names, Map<String, UseScope> useScopeNames, int indexOfInsertName) {
            String previousName = names.get(indexOfInsertName - 1);
            UseScope previousUseScope = useScopeNames.get(previousName);
            if (previousUseScope.isPartOfGroupUse()) {
                String baseName = getGroupUseBaseName(previousUseScope);
                if (insertName.startsWith(baseName)) {
                    insertOffset = previousUseScope.getNameRange().getEnd();
                    insertString = ", " + insertName.substring(baseName.length() + CodeUtils.NS_SEPARATOR.length());
                } else {
                    // use Vendor\Package\AAA\{A1, A2, A3};
                    // insertName: Vendor\Package\BBB\B1
                    GroupUseScope previousGroupUseScope = findGroupUseScope(previousUseScope);
                    if (previousGroupUseScope != null) {
                        insertOffset = getLineEnd(previousGroupUseScope.getNameRange().getEnd());
                        insertString = CodeUtils.NEW_LINE + createSingleUseStatement();
                    }
                }
            } else if (isPartOfMultipleUse(previousUseScope)) {
                // add single use statement
                insertOffset = getEndOfUseStetement(previousUseScope);
                insertString = CodeUtils.NEW_LINE + createSingleUseStatement();
            } else {
                // add single use statment
                insertOffset = getLineEnd(previousUseScope.getNameRange().getEnd());
                insertString = createSingleUseStatement();
            }
        }

        private void processInsertingBeforeNextUse(List<String> names, Map<String, UseScope> useScopeNames, int indexOfInsertName) {
            if (!names.isEmpty()) {
                String nextName = names.get(indexOfInsertName);
                UseScope nextUseScope = useScopeNames.get(nextName);
                if (nextUseScope.isPartOfGroupUse()) {
                    String groupUseBaseName = getGroupUseBaseName(nextUseScope);
                    if (insertName.startsWith(groupUseBaseName)) {
                        insertOffset = nextUseScope.getNameRange().getStart();
                        insertString = insertName.substring(groupUseBaseName.length() + CodeUtils.NS_SEPARATOR.length()) + ", "; // NOI18N
                    } else {
                        // use Vendor\Package\AAA\{A1, A2, A3};
                        // insertName: Vendor\Package\AA1\B1
                        GroupUseScope nextGroupUseScope = findGroupUseScope(nextUseScope);
                        if (nextGroupUseScope != null) {
                            insertOffset = getLineStart(nextGroupUseScope.getNameRange().getStart());
                            insertString = createSingleUseStatement();
                        }
                    }
                } else {
                    insertOffset = getLineStart(nextUseScope.getOffset());
                    insertString = createSingleUseStatement();
                }
            }
        }

        private void processInserting() {
            List<UseScope> typeScopes = getUseScopes(UseScope.Type.TYPE);
            List<UseScope> functionScopes = getUseScopes(UseScope.Type.FUNCTION);
            List<UseScope> constScopes = getUseScopes(UseScope.Type.CONST);
            if (typeScopes.isEmpty() && functionScopes.isEmpty() && constScopes.isEmpty()) {
                processInsertingFirstUse();
            } else {
                processInsertingFirstUseKind(typeScopes, functionScopes, constScopes);
            }
        }

        private void processInsertingFirstUse() {
            int offset = namespaceScope.getBlockRange().getStart();
            String word = getWord(offset);
            if (word != null && word.equals("{")) { // NOI18N
                offset++;
            }
            List<? extends ModelElement> elements = namespaceScope.getElements();
            if (!elements.isEmpty()) {
                offset = getLineStart(elements.get(0).getOffset());
                // find attribute
                int attributeStart = findAttributeStart(offset);
                while (attributeStart != -1) {
                    // e.g. #[A1]#[A2] class C {}
                    offset = attributeStart;
                    attributeStart = findAttributeStart(attributeStart);
                }
                int phpDocStart = findPhpDocStart(offset);
                if (phpDocStart != -1) {
                    offset = phpDocStart;
                }
                int start = findInsertStart(offset);
                if (start != -1) {
                    offset = start;
                }
                offset = getLineStart(offset);
            } else {
                // find declare statement
                DeclareStatement lastDeclareStatement = findLastDeclareStatement();
                if (lastDeclareStatement != null) {
                    Statement body = lastDeclareStatement.getBody();
                    if (!(body instanceof EmptyStatement)) {
                        // e.g.
                        // declare(ticks=1) {
                        // }
                        addNewLineBeforeUse = true;
                    }
                    offset = lastDeclareStatement.getEndOffset();
                }
            }
            insertOffset = offset;
            insertString = createSingleUseStatement();
        }

        private int findAttributeStart(int offset) {
            int result = -1;
            tokenSequence.move(offset);
            if (tokenSequence.movePrevious()) {
                List<PHPTokenId> ignores = Arrays.asList(
                        PHPTokenId.PHP_LINE_COMMENT,
                        PHPTokenId.PHPDOC_COMMENT,
                        PHPTokenId.PHPDOC_COMMENT_START,
                        PHPTokenId.PHPDOC_COMMENT_END,
                        PHPTokenId.PHP_COMMENT,
                        PHPTokenId.PHP_COMMENT_START,
                        PHPTokenId.PHP_COMMENT_END,
                        PHPTokenId.WHITESPACE
                );
                Token<? extends PHPTokenId> findPrevious = LexUtilities.findPrevious(tokenSequence, ignores);
                if (findPrevious != null
                        && TokenUtilities.textEquals(findPrevious.text(), "]")) { // NOI18N
                    Token<? extends PHPTokenId> attributeToken = LexUtilities.findPreviousToken(tokenSequence, Arrays.asList(PHPTokenId.PHP_ATTRIBUTE));
                    if (attributeToken != null) {
                        return tokenSequence.offset();
                    }
                }
            }
            return result;
        }

        private int findPhpDocStart(int offset) {
            int result = -1;
            tokenSequence.move(offset);
            if (tokenSequence.movePrevious()) {
                List<PHPTokenId> ignores = Arrays.asList(
                        PHPTokenId.WHITESPACE
                );
                Token<? extends PHPTokenId> findPrevious = LexUtilities.findPrevious(tokenSequence, ignores);
                if (findPrevious != null
                        && findPrevious.id() == PHPTokenId.PHPDOC_COMMENT_END) {
                    Token<? extends PHPTokenId> phpDocStart = LexUtilities.findPreviousToken(tokenSequence, Arrays.asList(PHPTokenId.PHPDOC_COMMENT_START));
                    if (phpDocStart != null) {
                        return tokenSequence.offset();
                    }
                }
            }
            return result;
        }

        private int findInsertStart(int offset) {
            int result = -1;
            tokenSequence.move(offset);
            while (tokenSequence.movePrevious()) {
                PHPTokenId id = tokenSequence.token().id();
                if (id == PHPTokenId.WHITESPACE) {
                    if (hasBlankLine(tokenSequence.token())) {
                        result = tokenSequence.offset() + tokenSequence.token().length();
                        break;
                    }
                }
                if (id != PHPTokenId.WHITESPACE
                        && id != PHPTokenId.PHPDOC_COMMENT_START
                        && id != PHPTokenId.PHPDOC_COMMENT
                        && id != PHPTokenId.PHPDOC_COMMENT_END
                        && id != PHPTokenId.PHP_COMMENT_START
                        && id != PHPTokenId.PHP_COMMENT
                        && id != PHPTokenId.PHP_COMMENT_END
                        && id != PHPTokenId.PHP_LINE_COMMENT
                        ) {
                    break;
                }
                if (id == PHPTokenId.PHPDOC_COMMENT_START
                        || id == PHPTokenId.PHP_COMMENT_START
                        || id == PHPTokenId.PHP_LINE_COMMENT) {
                    result = tokenSequence.offset();
                }
            }
            return result;
        }

        @CheckForNull
        private DeclareStatement findLastDeclareStatement() {
            Program program = parserResult.getProgram();
            if (program != null) {
                CheckVisitor checkVisitor = new CheckVisitor();
                program.accept(checkVisitor);
                List<DeclareStatement> declareStatements = checkVisitor.getDeclareStatements();
                if (!declareStatements.isEmpty()) {
                    return declareStatements.get(declareStatements.size() - 1);
                }
            }
            return null;
        }

        private void processInsertingFirstUseKind(List<UseScope> typeScopes, List<UseScope> functionScopes, List<UseScope> constScopes) {
            List<UseScope> allUseScopes = getAllUseScopes();
            CodeStyle codeStyle = CodeStyle.get(baseDocument);
            boolean isPSR12 = codeStyle.putInPSR12Order();
            switch (type) {
                case TYPE:
                    // add to top
                    setInsertOffsetBeforeTop(allUseScopes);
                    break;
                case CONST:
                    // const scopes is empty
                    if (isPSR12) {
                        setInsertOffsetAfterBottom(allUseScopes);
                    } else {
                        if (!typeScopes.isEmpty()) {
                            setInsertOffsetAfterBottom(typeScopes);
                        } else {
                            setInsertOffsetBeforeTop(functionScopes);
                        }
                    }
                    break;
                case FUNCTION:
                    // function scopes is empty
                    if (isPSR12) {
                        if (!typeScopes.isEmpty()) {
                            setInsertOffsetAfterBottom(typeScopes);
                        } else {
                            setInsertOffsetBeforeTop(constScopes);
                        }
                    } else {
                        setInsertOffsetAfterBottom(allUseScopes);
                    }
                    break;
                default:
                    assert false : "Unknown type: " + type; // NOI18N
                    break;
            }
            insertString = createSingleUseStatement();
        }

        private void setInsertOffsetBeforeTop(List<UseScope> useScopes) {
            UseScope topScope = useScopes.get(0);
            setInsertOffsetBefore(topScope);
        }

        private void setInsertOffsetBefore(UseScope topUseScope) {
            if (topUseScope.isPartOfGroupUse()) {
                GroupUseScope groupUseScope = findGroupUseScope(topUseScope);
                if (groupUseScope != null) {
                    insertOffset = getLineStart(groupUseScope.getNameRange().getStart());
                }
            } else {
                insertOffset = getLineStart(topUseScope.getNameRange().getStart());
            }
        }

        private void setInsertOffsetAfterBottom(List<UseScope> useScopes) {
            UseScope bottomScope = useScopes.get(useScopes.size() - 1);
            setInsertOffsetAfter(bottomScope);
        }

        private void setInsertOffsetAfter(UseScope bottomUseScope) {
            if (bottomUseScope.isPartOfGroupUse()) {
                GroupUseScope groupUseScope = findGroupUseScope(bottomUseScope);
                if (groupUseScope != null) {
                    insertOffset = getLineEnd(groupUseScope.getNameRange().getEnd());
                    addNewLineBeforeUse = true;
                }
            } else if (isPartOfMultipleUse(bottomUseScope)) {
                insertOffset = getEndOfUseStetement(bottomUseScope);
                addNewLineBeforeUse = true;
            } else {
                insertOffset = getLineEnd(bottomUseScope.getNameRange().getEnd());
            }
        }

        private List<String> getUseScopeNames(Map<String, UseScope> useScopeNames) {
            List<String> names = new ArrayList<>(useScopeNames.keySet());
            if (!names.isEmpty()) {
                names.add(insertName); // sentinel
            }
            return names;
        }

        private int findInsertNameIndex(List<String> names) {
            int insertNameIndex = -1;
            for (String name : names) {
                int currentIndex = names.indexOf(name);
                if (insertName.compareToIgnoreCase(name) <= 0) {
                    insertNameIndex = currentIndex;
                    break;
                }
                if (name.compareToIgnoreCase(names.get(currentIndex + 1)) > 0) {
                    insertNameIndex = currentIndex + 1;
                    break;
                }
            }
            if (insertNameIndex == -1 && !names.isEmpty()) {
                insertNameIndex = 0;
            }
            return insertNameIndex;
        }

        @CheckForNull
        private String getWord(int offset) {
            try {
                return LineDocumentUtils.getWord(baseDocument, offset);
            } catch (BadLocationException ex) {
                LOGGER.log(Level.WARNING, "Invalid offset: {0}", ex.offsetRequested()); // NOI18N
            }
            return null;
        }

        private int getLineStart(int offset) {
            return LineDocumentUtils.getLineStart(baseDocument, offset);
        }

        private int getLineEnd(int offset) {
            try {
                return LineDocumentUtils.getLineEnd(baseDocument, offset);
            } catch (BadLocationException ex) {
                LOGGER.log(Level.WARNING, "Invalid offset: {0}", ex.offsetRequested()); // NOI18N
            }
            return offset;
        }

        private boolean hasOtherTypeUseAbove() {
            if (insertOffset != -1) {
                List<UseScope> useScopes = getUseScopesExceptFor(type);
                sortByOffset(useScopes);
                if (!useScopes.isEmpty()) {
                    for (UseScope useScope : useScopes) {
                        if (useScope.getOffset() < insertOffset) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        private boolean hasBlankLinesBeforeInsertOffset() {
            if (insertOffset != -1) {
                tokenSequence.move(insertOffset);
                if (tokenSequence.moveNext()) {
                    Token<PHPTokenId> token = tokenSequence.token();
                    if (token.id() == PHPTokenId.WHITESPACE) {
                        return hasBlankLine(token);
                    }
                    if (token.id() != PHPTokenId.PHP_USE) {
                        return false;
                    }
                }
                if (tokenSequence.movePrevious()) {
                    Token<PHPTokenId> token = tokenSequence.token();
                    if (token.id() == PHPTokenId.WHITESPACE) {
                        return hasBlankLine(token);
                    }
                }
            }
            return false;
        }

        private boolean hasBlankLine(Token<PHPTokenId> token) {
            return token.text().chars().filter(c -> c == '\n').count() >= 2;
        }

        private String getBlankLinesBeteenUseTypes() {
            StringBuilder sb = new StringBuilder();
            CodeStyle codeStyle = CodeStyle.get(baseDocument);
            if (codeStyle.getBlankLinesBetweenUseTypes() > 0 && hasOtherTypeUseAbove()) {
                for (int i = 0; i < codeStyle.getBlankLinesBetweenUseTypes(); i++) {
                    sb.append(CodeUtils.NEW_LINE);
                }
            }
            return sb.toString();
        }

        private String createSingleUseStatement() {
            StringBuilder extraSpaces = new StringBuilder();
            if (addToTopOfUseType() && !hasBlankLinesBeforeInsertOffset()) {
                // e.g.
                // use Vendor\Package\Type;
                // use function Vendor\Package\func00; // add here
                // use function Vendor\Package\func01;
                extraSpaces.append(getBlankLinesBeteenUseTypes());
            }
            if (extraSpaces.length() == 0 && addNewLineBeforeUse) {
                extraSpaces.append(CodeUtils.NEW_LINE);
            }
            OffsetRange blockRange = namespaceScope.getBlockRange();
            String blockStartWord = null;
            if (blockRange != null) {
                blockStartWord = getWord(blockRange.getStart());
            }
            if (blockStartWord != null && blockStartWord.startsWith("{")) { // NOI18N
                // e.g. namespace Foo\Bar {}
                CodeStyle codeStyle = CodeStyle.get(baseDocument);
                extraSpaces.append(IndentUtils.createIndentString(codeStyle.getIndentSize(), codeStyle.expandTabToSpaces(), codeStyle.getTabSize()));
            }
            return createSingleUseStatement(extraSpaces.toString());
        }

        private String createSingleUseStatement(String extraSpaces) {
            StringBuilder sb = new StringBuilder();
            if (insertOffset == 0) {
                sb.append("<?php").append(CodeUtils.NEW_LINE); // NOI18N
            }
            sb.append(extraSpaces);
            sb.append("use "); // NOI18N
            switch (type) {
                case TYPE:
                    // noop
                    break;
                case CONST:
                    sb.append("const "); // NOI18N
                    break;
                case FUNCTION:
                    sb.append("function "); // NOI18N
                    break;
                default:
                    assert false : "Unknown Type: " + type; // NOI18N
                    // noop
                    break;
            }
            sb.append(insertName).append(";"); // NOI18N
            if (insertOffset == 0) {
                sb.append(CodeUtils.NEW_LINE).append("?>"); // NOI18N
            }
            sb.append(CodeUtils.NEW_LINE);
            return sb.toString();
        }

        private boolean addToTopOfUseType() {
            return indexOfInsertName == 0;
        }

        private String getGroupUseBaseName(UseScope useScope) {
            String baseNamespaceName = QualifiedName.create(useScope.getName()).getNamespaceName();
            if (useScope.isPartOfGroupUse()) {
                GroupUseScope groupUseScope = findGroupUseScope(useScope);
                if (groupUseScope != null) {
                    boolean find = false;
                    while (!find && !StringUtils.isEmpty(baseNamespaceName)) {
                        find = true;
                        for (UseScope useScope1 : groupUseScope.getUseScopes()) {
                            if (!useScope1.getName().startsWith(baseNamespaceName)) {
                                find = false;
                                break;
                            }
                        }
                        if (find) {
                            break;
                        }
                        baseNamespaceName = QualifiedName.create(baseNamespaceName).getNamespaceName();
                    }
                }
            }
            return baseNamespaceName;
        }

        @CheckForNull
        private GroupUseScope findGroupUseScope(UseScope useScope) {
            for (GroupUseScope declaredGroupUse : declaredGroupUses) {
                for (UseScope declaredUseScope : declaredGroupUse.getUseScopes()) {
                    if (useScope == declaredUseScope) {
                        return declaredGroupUse;
                    }
                }
            }
            return null;
        }

        private List<UseScope> getAllUseScopes() {
            List<UseScope> allUseScopes = new ArrayList<>();
            allUseScopes.addAll(getUseScopes(UseScope.Type.TYPE));
            allUseScopes.addAll(getUseScopes(UseScope.Type.FUNCTION));
            allUseScopes.addAll(getUseScopes(UseScope.Type.CONST));
            sortByOffset(allUseScopes);
            return allUseScopes;
        }

        private List<UseScope> getUseScopes(UseScope.Type type) {
            switch (type) {
                case TYPE:
                    return Collections.unmodifiableList(typeUseScopes);
                case FUNCTION:
                    return Collections.unmodifiableList(functionUseScopes);
                case CONST:
                    return Collections.unmodifiableList(constUseScopes);
                default:
                    assert false : "Unknown type: " + type; // NOI18N
                    return Collections.emptyList();
            }
        }

        private List<UseScope> getUseScopesExceptFor(UseScope.Type type) {
            List<UseScope> useScopes = new ArrayList<>();
            switch (type) {
                case TYPE:
                    useScopes.addAll(getUseScopes(UseScope.Type.FUNCTION));
                    useScopes.addAll(getUseScopes(UseScope.Type.CONST));
                    break;
                case FUNCTION:
                    useScopes.addAll(getUseScopes(UseScope.Type.TYPE));
                    useScopes.addAll(getUseScopes(UseScope.Type.CONST));
                    break;
                case CONST:
                    useScopes.addAll(getUseScopes(UseScope.Type.TYPE));
                    useScopes.addAll(getUseScopes(UseScope.Type.FUNCTION));
                    break;
                default:
                    assert false : "Unknown type: " + type; // NOI18N
                    return Collections.emptyList();
            }
            return useScopes;
        }

        private Map<String, UseScope> getNamedUseScopes(UseScope.Type type) {
            Map<String, UseScope> names = new LinkedHashMap<>();
            switch (type) {
                case TYPE:
                    names.putAll(typeNames);
                    break;
                case CONST:
                    names.putAll(constNames);
                    break;
                case FUNCTION:
                    names.putAll(functionNames);
                    break;
                default:
                    assert false : "Unknown type: " + type; // NOI18N
                    break;
            }
            return names;
        }

        private void init() {
            processGroupUses();
            processSingleUses();
            sortEachUseKindScope();
            for (UseScope useScope : typeUseScopes) {
                addUseScope(typeNames, useScope);
            }
            for (UseScope useScope : constUseScopes) {
                addUseScope(constNames, useScope);
            }
            for (UseScope useScope : functionUseScopes) {
                addUseScope(functionNames, useScope);
            }
        }

        private void addUseScope(Map<String, UseScope> useScopeNames, UseScope useScope) {
            String name = useScope.getName();
            AliasedName aliasedName = useScope.getAliasedName();
            if (aliasedName != null) {
                name += " as " + aliasedName.getAliasName(); // NOI18N
            }
            useScopeNames.put(name, useScope);
        }

        private void processGroupUses() {
            for (GroupUseScope declaredGroupUse : declaredGroupUses) {
                switch (declaredGroupUse.getType()) {
                    case TYPE:
                        typeUseScopes.addAll(declaredGroupUse.getUseScopes());
                        break;
                    case CONST:
                        constUseScopes.addAll(declaredGroupUse.getUseScopes());
                        break;
                    case FUNCTION:
                        functionUseScopes.addAll(declaredGroupUse.getUseScopes());
                        break;
                    default:
                        assert false : "Unhandled Type: " + declaredGroupUse.getType(); // NOI18N
                        break;
                }
            }
        }

        private void processSingleUses() {
            for (UseScope declaredSingleUse : declaredSingleUses) {
                switch (declaredSingleUse.getType()) {
                    case TYPE:
                        typeUseScopes.add(declaredSingleUse);
                        break;
                    case CONST:
                        constUseScopes.add(declaredSingleUse);
                        break;
                    case FUNCTION:
                        functionUseScopes.add(declaredSingleUse);
                        break;
                    default:
                        assert false : "Unhandled Type: " + declaredSingleUse.getType(); // NOI18N
                        break;
                }
            }
        }

        private void sortEachUseKindScope() {
            sortByOffset(typeUseScopes);
            sortByOffset(constUseScopes);
            sortByOffset(functionUseScopes);
        }

        private void sortByOffset(List<UseScope> useScopes) {
            useScopes.sort((use1, use2) -> Integer.compare(use1.getOffset(), use2.getOffset()));
        }
    }

    private static class CheckVisitor extends DefaultVisitor {

        private final List<DeclareStatement> declareStatements = new ArrayList<>();

        @Override
        public void visit(DeclareStatement node) {
            declareStatements.add(node);
            super.visit(node);
        }

        public List<DeclareStatement> getDeclareStatements() {
            return Collections.unmodifiableList(declareStatements);
        }
    }

    public static final class Hints {

        private final String fqName;
        private final String useType;
        @NullAllowed
        private final String aliasName;

        public Hints(String fqName, String useType, @NullAllowed String aliasName) {
            this.fqName = fqName;
            assert !useType.isEmpty();
            this.useType = useType;
            this.aliasName = aliasName;
        }

        public Hints(String fqName, String useType) {
            this(fqName, useType, null);
        }

        public String getFqName() {
            return fqName;
        }

        public String getUseType() {
            return useType;
        }

        public UseScope.Type getUseScopeType() {
            return AutoImport.getUseScopeType(useType);
        }

        @CheckForNull
        public String getAliasName() {
            return aliasName;
        }
    }
}
