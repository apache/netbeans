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
package org.netbeans.modules.php.editor.csl;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.DeclarationFinder.AlternativeLocation;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.FullyQualifiedElement;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.Occurence;
import org.netbeans.modules.php.editor.model.OccurencesSupport;
import org.netbeans.modules.php.editor.model.VariableScope;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo.Kind;
import org.netbeans.modules.php.editor.model.nodes.MagicMethodDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.PhpDocTypeTagInfo;
import org.netbeans.modules.php.editor.parser.PHPDocCommentParser;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocMethodTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeTag;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Radek Matous
 */
public class DeclarationFinderImpl implements DeclarationFinder {

    private static final RequestProcessor RP = new RequestProcessor(DeclarationFinderImpl.class);
    private static final Logger LOGGER = Logger.getLogger(DeclarationFinderImpl.class.getName());
    private static final int RESOLVING_TIMEOUT = 300;

    @Override
    public DeclarationLocation findDeclaration(ParserResult info, int caretOffset) {
        return findDeclarationImpl(info, caretOffset);
    }

    @Override
    public OffsetRange getReferenceSpan(final Document doc, final int caretOffset) {
        OffsetRange offsetRange = OffsetRange.NONE;
        Future<ReferenceSpanCrate> crateFuture = RP.submit(new ReferenceSpanCrateFetcher(Source.create(doc)));
        try {
            ReferenceSpanCrate crate = crateFuture.get(RESOLVING_TIMEOUT, TimeUnit.MILLISECONDS);
            if (crate != null) {
                Model model = crate.getModel();
                TokenHierarchy<?> tokenHierarchy = crate.getTokenHierarchy();
                if (model != null && tokenHierarchy != null) {
                    TokenSequence<PHPTokenId> ts = LexUtilities.getPHPTokenSequence(tokenHierarchy, caretOffset);
                    offsetRange = getReferenceSpan(ts, caretOffset, model);
                }
            }
        } catch (InterruptedException ex) {
            LOGGER.log(Level.FINE, "Resolving of reference span offset range has been interrupted.");
        } catch (ExecutionException ex) {
            LOGGER.log(Level.SEVERE, "Exception has been thrown during resolving of reference span offset range.", ex);
        } catch (TimeoutException ex) {
            LOGGER.log(Level.FINE, "Timeout for resolving reference span offset range has been exceed: {0}", RESOLVING_TIMEOUT);
        }
        return offsetRange;
    }

    public static OffsetRange getReferenceSpan(TokenSequence<PHPTokenId> ts, final int caretOffset, final Model model) {
        Parameters.notNull("ts", model); //NOI18N
        Parameters.notNull("model", model); //NOI18N
        return new ReferenceSpanFinder(model).getReferenceSpan(ts, caretOffset);
    }

    public static DeclarationLocation findDeclarationImpl(ParserResult info, int caretOffset) {
        if (!(info instanceof PHPParseResult)) {
            return DeclarationLocation.NONE;
        }
        PHPParseResult result = (PHPParseResult) info;
        final Model model = result.getModel(Model.Type.COMMON);
        OccurencesSupport occurencesSupport = model.getOccurencesSupport(caretOffset);
        Occurence underCaret = occurencesSupport.getOccurence();
        return findDeclarationImpl(underCaret, info);
    }

    private static DeclarationLocation findDeclarationImpl(Occurence underCaret, ParserResult info) {
        DeclarationLocation location = DeclarationLocation.NONE;
        if (underCaret != null) {
            Collection<? extends PhpElement> gotoDeclarations = underCaret.gotoDeclarations();
            if (gotoDeclarations == null || gotoDeclarations.isEmpty()) {
                return DeclarationLocation.NONE;
            }
            PhpElement declaration = gotoDeclarations.iterator().next();
            FileObject declarationFo = declaration.getFileObject();
            if (declarationFo == null) {
                return DeclarationLocation.NONE;
            }
            location = new DeclarationLocation(declarationFo, declaration.getOffset(), declaration);
            Collection<? extends PhpElement> alternativeDeclarations = gotoDeclarations;
            if (alternativeDeclarations.size() > 1) {
                final FileObject currentFile = info.getSnapshot().getSource().getFileObject();
                int numberOfCurrentDeclaration = 0;
                DeclarationLocation alternatives = DeclarationLocation.NONE;
                for (PhpElement elem : alternativeDeclarations) {
                    FileObject elemFo = elem.getFileObject();
                    if (elemFo == null) {
                        continue;
                    }
                    DeclarationLocation declLocation = new DeclarationLocation(elemFo, elem.getOffset(), elem);
                    if (currentFile == elemFo) {
                        location = declLocation;
                        numberOfCurrentDeclaration++;
                    }
                    AlternativeLocation al = new AlternativeLocationImpl(elem, declLocation);
                    if (alternatives == DeclarationLocation.NONE) {
                        alternatives = al.getLocation();
                    }
                    alternatives.addAlternative(al);
                }
                return (numberOfCurrentDeclaration == 1
                        && !EnumSet.<Occurence.Accuracy>of(Occurence.Accuracy.MORE_TYPES, Occurence.Accuracy.MORE).contains(underCaret.degreeOfAccuracy()))
                        ? location
                        : alternatives;
            }
        }
        return location;
    }

    private static class ReferenceSpanCrate {
        private Model model;
        private TokenHierarchy<?> tokenHierarchy;

        /**
         *
         * @return model, or null.
         */
        @CheckForNull
        public Model getModel() {
            return model;
        }

        /**
         *
         * @return token hierarchy, or null.
         */
        @CheckForNull
        public TokenHierarchy<?> getTokenHierarchy() {
            return tokenHierarchy;
        }

        public void setModel(Model model) {
            this.model = model;
        }

        public void setTokenHierarchy(TokenHierarchy<?> tokenHierarchy) {
            this.tokenHierarchy = tokenHierarchy;
        }

    }

    private static class ReferenceSpanCrateFetcher implements Callable<ReferenceSpanCrate> {
        private final Source source;

        public ReferenceSpanCrateFetcher(final Source source) {
            this.source = source;
        }

        @Override
        public ReferenceSpanCrate call() throws Exception {
            final ReferenceSpanCrate crate = new ReferenceSpanCrate();
            ParserManager.parse(Collections.singletonList(source), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    Result parserResult = resultIterator.getParserResult();
                    if (parserResult instanceof PHPParseResult) {
                        PHPParseResult phpParserResult = (PHPParseResult) parserResult;
                        crate.setModel(phpParserResult.getModel(Model.Type.COMMON));
                        crate.setTokenHierarchy(resultIterator.getSnapshot().getTokenHierarchy());
                    }
                }
            });
            return crate;
        }

    }

    private static class ReferenceSpanFinder {

        private static final int RECURSION_LIMIT = 100;
        // e.g.  @var VarType $variable
        private static final Pattern INLINE_PHP_VAR_COMMENT_PATTERN = Pattern.compile("^[ \t]*@var[ \t]+.+[ \t]+\\$.+$"); // NOI18N
        private static final Logger LOGGER = Logger.getLogger(DeclarationFinderImpl.class.getName());

        private int recursionCounter = 0;
        private final Model model;

        public ReferenceSpanFinder(final Model model) {
            this.model = model;
        }

        public OffsetRange getReferenceSpan(TokenSequence<PHPTokenId> ts, final int caretOffset) {
            if (ts == null) {
                return OffsetRange.NONE;
            }
            ts.move(caretOffset);
            int startTSOffset = 0;
            if (ts.moveNext()) {
                startTSOffset = ts.offset();
                Token<PHPTokenId> token = ts.token();
                PHPTokenId id = token.id();
                if (id.equals(PHPTokenId.PHP_STRING) || id.equals(PHPTokenId.PHP_VARIABLE)) {
                    return new OffsetRange(ts.offset(), ts.offset() + token.length());
                }
                if (id.equals(PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING)) {
                    OffsetRange retval = new OffsetRange(ts.offset(), ts.offset() + token.length());
                    int maxForgingTokens = 4; // REQUIRE_INCLUDE_TOKEN [WS] [OPENING_BRACE] [WS] = include_once ( 'file.php');
                    for (int i = 0; i < maxForgingTokens && ts.movePrevious(); i++) {
                        token = ts.token();
                        id = token.id();
                        if (id.equals(PHPTokenId.PHP_INCLUDE)
                                || id.equals(PHPTokenId.PHP_INCLUDE_ONCE)
                                || id.equals(PHPTokenId.PHP_REQUIRE)
                                || id.equals(PHPTokenId.PHP_REQUIRE_ONCE)) {
                            return retval;
                        }
                        if (id.equals(PHPTokenId.PHP_STRING) && token.text().toString().equalsIgnoreCase("define")) { //NOI18N
                            return retval;
                        }
                    }
                } else if (id.equals(PHPTokenId.PHPDOC_COMMENT)) {
                    String tokenText = token.text().toString();
                    if (INLINE_PHP_VAR_COMMENT_PATTERN.matcher(tokenText).matches()) {
                        OffsetRange offsetRange = getVarCommentOffsetRange(ts, tokenText, caretOffset);
                        if (offsetRange != null) {
                            return offsetRange;
                        }
                    }
                    PHPDocCommentParser docParser = new PHPDocCommentParser();
                    PHPDocBlock docBlock = docParser.parse(ts.offset() - 3, ts.offset() + token.length(), token.text().toString());
                    ASTNode[] hierarchy = Utils.getNodeHierarchyAtOffset(docBlock, caretOffset);
                    PhpDocTypeTagInfo node = null;
                    if (hierarchy != null && hierarchy.length > 0) {
                        if (hierarchy[0] instanceof PHPDocTypeTag) {
                            PHPDocTypeTag typeTag = (PHPDocTypeTag) hierarchy[0];
                            // parameter does not start with ws, so check not only "<" but also "=="
                            // e.g. @method C testMechod(C $class)
                            if (typeTag.getStartOffset() <= caretOffset && caretOffset <= typeTag.getEndOffset()) {
                                VariableScope scope = model.getVariableScope(caretOffset);
                                List<? extends PhpDocTypeTagInfo> tagInfos = PhpDocTypeTagInfo.create(typeTag, Kind.CLASS, scope);
                                for (PhpDocTypeTagInfo typeTagInfo : tagInfos) {
                                    if (typeTagInfo.getKind().equals(Kind.CLASS)
                                            && typeTagInfo.getRange().containsInclusive(caretOffset)) {
                                        node = typeTagInfo;
                                        break;
                                    }
                                    if (typeTagInfo.getKind().equals(Kind.USE_ALIAS) && typeTagInfo.getRange().containsInclusive(caretOffset)) {
                                        node = typeTagInfo;
                                        break;
                                    }
                                }
                                if (node == null || !node.getRange().containsInclusive(caretOffset)) {
                                    tagInfos = PhpDocTypeTagInfo.create(typeTag, Kind.VARIABLE, scope);
                                    for (PhpDocTypeTagInfo typeTagInfo : tagInfos) {
                                        if (typeTagInfo.getKind().equals(Kind.VARIABLE)) {
                                            node = typeTagInfo;
                                            break;
                                        }
                                    }
                                }
                                if (node != null) {
                                    return node.getRange().containsInclusive(caretOffset) ? node.getRange() : OffsetRange.NONE;
                                }
                                if (typeTag instanceof PHPDocMethodTag) {
                                    OffsetRange magicMethodRange = getMagicMethodRange((PHPDocMethodTag) typeTag, caretOffset);
                                    if (magicMethodRange != OffsetRange.NONE) {
                                        return magicMethodRange;
                                    }
                                }
                            }
                        } else {
                            List<PHPDocTag> tags = docBlock.getTags();
                            for (PHPDocTag phpDocTag : tags) {
                                if (phpDocTag instanceof PHPDocMethodTag) {
                                    OffsetRange magicMethodRange = getMagicMethodRange((PHPDocMethodTag) phpDocTag, caretOffset);
                                    if (magicMethodRange != OffsetRange.NONE) {
                                        return magicMethodRange;
                                    }
                                }
                            }

                        }
                    }
                } else if (id.equals(PHPTokenId.PHP_COMMENT) && token.text() != null) {
                    OffsetRange offsetRange = getVarCommentOffsetRange(ts, token.text().toString(), caretOffset);
                    if (offsetRange != null) {
                        return offsetRange;
                    }
                }
            }
            if (caretOffset == startTSOffset) {
                if (recursionCounter < RECURSION_LIMIT) {
                    recursionCounter++;
                    // if there is not a refence, and the curet is just beetween two tokens,
                    // try the previous token. See issue #199329
                    return getReferenceSpan(ts, caretOffset - 1);
                } else {
                    logRecursion(ts);
                }
            }
            return OffsetRange.NONE;
        }

        private OffsetRange getMagicMethodRange(PHPDocMethodTag methodTag, final int caretOffset) {
            OffsetRange offsetRange = OffsetRange.NONE;
            MagicMethodDeclarationInfo methodInfo = MagicMethodDeclarationInfo.create(methodTag);
            if (methodInfo != null) {
                if (methodInfo.getRange().containsInclusive(caretOffset)) {
                    offsetRange = methodInfo.getRange();
                } else if (methodInfo.getTypeRange().containsInclusive(caretOffset)) {
                    offsetRange = methodInfo.getTypeRange();
                }
            }
            return offsetRange;
        }

        @CheckForNull
        private OffsetRange getVarCommentOffsetRange(TokenSequence<PHPTokenId> ts, String text, int caretOffset) {
            if (text.contains(CodeUtils.VAR_TAG)) {
                String[] segments = CodeUtils.WHITE_SPACES_PATTERN.split(text.trim());
                for (int i = 0; i < segments.length; i++) {
                    String seg = segments[i];
                    // e.g. /** @var Type1|Type2 $varName */ or /* @var $varName Type1|Type2 */
                    if (seg.equals(CodeUtils.VAR_TAG) && segments.length > i + 2) { // 2: type and variable name
                        for (int j = 1; j <= 2; j++) {
                            seg = segments[i + j];
                            if (seg != null && seg.trim().length() > 0) {
                                int indexOfType = text.indexOf(seg);
                                assert indexOfType != -1;
                                indexOfType += ts.offset();
                                for (OffsetRange range : getTypeRanges(seg, indexOfType)) {
                                    if (range.containsInclusive(caretOffset)) {
                                        return range;
                                    }
                                }
                            }
                        }
                        return OffsetRange.NONE;
                    }
                }
            }
            return null;
        }

        private Set<OffsetRange> getTypeRanges(String types, int startOffset) {
            String[] splitTypes = Type.splitTypes(types);
            Set<OffsetRange> ranges = new HashSet<>((int) (splitTypes.length * 1.5));
            // to avoid adding the same type range as the first one,
            // keep the end index of the last type
            // e.g. (Foo&Bar)|(Foo&Baz)
            int fromEndIndexOfLastType = 0;
            for (String splitType : splitTypes) {
                String type = splitType;
                int indexOfType = types.indexOf(type, fromEndIndexOfLastType);
                assert indexOfType != -1;
                if (CodeUtils.isNullableType(type)) {
                    indexOfType++;
                    type = type.substring(1);
                }
                int rangeStart = startOffset + indexOfType;
                int rangeEnd = rangeStart + type.length();
                ranges.add(new OffsetRange(rangeStart, rangeEnd));
                fromEndIndexOfLastType = indexOfType + type.length();
            }
            return ranges;
        }

        private void logRecursion(TokenSequence<PHPTokenId> ts) {
            CharSequence tokenText = null;
            if (ts != null) {
                Token<PHPTokenId> token = ts.token();
                if (token != null) {
                    tokenText = token.text();
                } else {
                    tokenText = "Possibly between tokens"; //NOI18N
                }
            }
            LOGGER.log(Level.WARNING, "Stack overflow detection - limit: {0}, token: {1}", new Object[]{RECURSION_LIMIT, tokenText});
        }

    }

    public static class AlternativeLocationImpl implements AlternativeLocation {

        private PhpElement modelElement;
        private DeclarationLocation declaration;

        public AlternativeLocationImpl(PhpElement modelElement, DeclarationLocation declaration) {
            this.modelElement = modelElement;
            this.declaration = declaration;
        }

        @Override
        public ElementHandle getElement() {
            return modelElement;
        }

        @Override
        public String getDisplayHtml(HtmlFormatter formatter) {
            formatter.reset();
            ElementKind ek = modelElement.getKind();

            formatter.name(ek, true);
            if ((modelElement instanceof FullyQualifiedElement) && !((FullyQualifiedElement) modelElement).getNamespaceName().isDefaultNamespace()) {
                QualifiedName namespaceName = ((FullyQualifiedElement) modelElement).getNamespaceName();
                formatter.appendText(namespaceName.append(modelElement.getName()).toString());
            } else {
                formatter.appendText(modelElement.getName());
            }
            formatter.name(ek, false);

            if (declaration.getFileObject() != null) {
                formatter.appendText(" in ");
                formatter.appendText(FileUtil.getFileDisplayName(declaration.getFileObject()));
            }

            return formatter.getText();
        }

        @Override
        public DeclarationLocation getLocation() {
            return declaration;
        }

        @Override
        public int compareTo(AlternativeLocation o) {
            AlternativeLocationImpl i = (AlternativeLocationImpl) o;
            return this.modelElement.getName().compareTo(i.modelElement.getName());
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 89 * hash + (this.modelElement != null ? this.modelElement.hashCode() : 0);
            hash = 89 * hash + (this.declaration != null ? this.declaration.hashCode() : 0);
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
            final AlternativeLocationImpl other = (AlternativeLocationImpl) obj;
            if (this.modelElement != other.modelElement && (this.modelElement == null || !this.modelElement.equals(other.modelElement))) {
                return false;
            }
            if (this.declaration != other.declaration && (this.declaration == null || !this.declaration.equals(other.declaration))) {
                return false;
            }
            return true;
        }

    }
}
