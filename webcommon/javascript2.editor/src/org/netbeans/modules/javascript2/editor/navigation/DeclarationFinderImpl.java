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
package org.netbeans.modules.javascript2.editor.navigation;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.editor.EditorExtender;
import org.netbeans.modules.javascript2.editor.FileUtils;
import org.netbeans.modules.javascript2.editor.Utils;
import org.netbeans.modules.javascript2.model.api.IndexedElement;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId;
import org.netbeans.modules.javascript2.model.api.JsElement;
import org.netbeans.modules.javascript2.model.api.JsFunction;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.api.Model;
import org.netbeans.modules.javascript2.model.api.Occurrence;
import org.netbeans.modules.javascript2.types.api.Type;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.netbeans.modules.javascript2.model.api.Index;
import org.netbeans.modules.javascript2.model.api.ModelUtils;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Pisl
 */
public class DeclarationFinderImpl implements DeclarationFinder {

    private final Language<JsTokenId> language;

    public DeclarationFinderImpl(Language<JsTokenId> language) {
        this.language = language;
    }

    @Override
    public DeclarationLocation findDeclaration(ParserResult info, int caretOffset) {
        if (!(info instanceof JsParserResult)) {
            return DeclarationLocation.NONE;
        }
        JsParserResult jsResult = (JsParserResult)info;
        Snapshot snapshot = jsResult.getSnapshot();
        TokenSequence ts = LexUtilities.getTokenSequence(snapshot, caretOffset, language);
        int offset = info.getSnapshot().getEmbeddedOffset(caretOffset);

        ts.move(offset);
        if (ts.moveNext()) {
            if (ts.token().id() == JsTokenId.STRING) {
                //are we in the import expression to navigate the imported file?
                String path = ts.token().text().toString();
                Token<? extends JsTokenId> token = LexUtilities.findPreviousToken(ts, Utils.LOOK_FOR_IMPORT_EXPORT_TOKENS);
                if (token.id() == JsTokenId.KEYWORD_IMPORT || token.id() == JsTokenId.KEYWORD_EXPORT) {
                    FileObject currentFO = snapshot.getSource().getFileObject();
                    FileObject destinationFO = FileUtils.findFileObject(currentFO, path, false);
                    if (destinationFO != null) {
                        return new DeclarationLocation(destinationFO, 0);
                    } else {
                        return DeclarationLocation.NONE;
                    }
                }
            }
        }
        Model model = Model.getModel(jsResult, false);
        model.resolve();

        OccurrencesSupport os = new OccurrencesSupport(model);
        Occurrence occurrence = os.getOccurrence(offset);
        if (occurrence != null) {
            JsObject object = occurrence.getDeclarations().iterator().next();
            JsObject parent = object.getParent();
            Collection<? extends TypeUsage> assignments = (parent == null) ? null : parent.getAssignmentForOffset(offset);
            if (assignments != null && assignments.isEmpty()) {
                assignments = parent.getAssignments();
            }
            Index jsIndex = Index.get(snapshot.getSource().getFileObject());
            List<IndexResult> indexResults = new ArrayList<>();
            if (assignments == null || assignments.isEmpty()) {
                FileObject fo = object.getFileObject();
                if (object.isDeclared()) {

                    if (fo != null) {
                        if (fo.equals(snapshot.getSource().getFileObject()) && object.getDeclarationName() != null) {
                            int docOffset = LexUtilities.getLexerOffset(jsResult, getDeclarationOffset(object));
                            if (docOffset > -1) {
                                if (ts != null) {
                                    ts.move(offset);
                                    if (ts.moveNext()) {
                                        int docTsOffset = LexUtilities.getLexerOffset(jsResult, ts.offset());
                                        if (!(docTsOffset <= docOffset && docOffset <= (docTsOffset + ts.token().length()))) {
                                            // return the declaration only if it's not the same identifier
                                            return new DeclarationLocation(fo, docOffset, object);
                                        }
                                    }
                                }
                            }
                        } else {
                            // TODO we need to solve to translating model offsets to the doc offset for other files?
                            return new DeclarationLocation(fo, getDeclarationOffset(object), object);
                        }

                    }
                } else {
                    Collection<? extends IndexResult> items = Index.get(fo).findByFqn(
                            object.getFullyQualifiedName(), Index.TERMS_BASIC_INFO);
                    indexResults.addAll(items);
                    DeclarationLocation location = processIndexResult(indexResults);
                    if (location != null) {
                        return location;
                    }
                    // get FQN of the object that we need resolved
                    String fqn = object.getFullyQualifiedName();
                    // we need all parts of FQN
                    String[] fqnParts = fqn.split("\\.");
                    // parent should contain the top object
                    parent = object;
                    while (parent != null  && !fqnParts[0].equals(parent.getFullyQualifiedName())) {
                        parent  = parent.getParent();
                    }
                    if (parent == null) {
                        return DeclarationLocation.NONE;
                    }
                    int partIndex = 1;
                    // find the last object that is defined in the file / model. The rest will be done through the index.
                    while (partIndex < fqnParts.length) {
                        JsObject property = parent.getProperty(fqnParts[partIndex]);
                        if (property != null && property.isDeclared()) {
                            partIndex++;
                            parent = property;
                        } else {
                            break;
                        }
                    }
                    // build the FQN of the last defined property in the file / model
                    String lastDefinedFQN = parent.getFullyQualifiedName();
                    List<IndexResult> rItems = new ArrayList<>();
                    if (partIndex < fqnParts.length) {
                        // find the next property from FQN in the index for the defined property in the file / model
                        rItems.addAll(findPropertyOfType(jsIndex, lastDefinedFQN.toString(), fqnParts[partIndex]));
                        parent = findPropertyOrParameterInModel(parent, fqnParts[partIndex]);
                        partIndex++;
                        for (int i = partIndex; (!rItems.isEmpty()) && i < fqnParts.length; i++ ) {
                            List<IndexResult> copy = new ArrayList<>(rItems);
                            rItems.clear();
                            // and for the found property find next property from the FQN
                            for (IndexResult indexResult : copy) {
                                rItems.addAll(findPropertyOfType(jsIndex, IndexedElement.getFQN(indexResult), fqnParts[i]));
                            }
                            if (rItems.isEmpty() && parent != null) {
                                // require js places parameter assignment only in the model. The assignments are not available in the indes
                                Collection<? extends TypeUsage> assigns = parent.getAssignments();
                                if (!assigns.isEmpty()) {
                                    for (Type type : assigns) {
                                        String afqn = ModelUtils.getFQNFromType(type);
                                        rItems.addAll(findPropertyOfType(jsIndex, afqn, fqnParts[i]));
                                    }
                                }
                            }
                            if (parent != null) {
                                parent = findPropertyOrParameterInModel(parent, fqnParts[i]);
                            }
                        }
                    }
                    location = processIndexResult(rItems);
                    if (location != null) {
                        return location;
                    }
                }
            } else {
                FileObject fo = object.getFileObject();
                if (object.isDeclared()) {
                    if (fo != null) {
                        if (fo.equals(snapshot.getSource().getFileObject())) {
                            int docOffset = LexUtilities.getLexerOffset(jsResult, getDeclarationOffset(object));
                            if (docOffset > -1) {
                                return new DeclarationLocation(fo, docOffset, object);
                            }
                        } else {
                            // TODO we need to solve to translating model offsets to the doc offset for other files?
                            return new DeclarationLocation(fo, getDeclarationOffset(object), object);
                        }

                    }
                }
                if (ts != null) {
                    ts.move(offset);
                    if (ts.moveNext() && (ts.token().id() == JsTokenId.IDENTIFIER || ts.token().id() == JsTokenId.PRIVATE_IDENTIFIER)) {
                        String propertyName = ts.token().text().toString();
                        for (Type type : assignments) {
                            String fqn = ModelUtils.getFQNFromType(type);
                            Collection<? extends IndexResult> items = findPropertyOfType(jsIndex, fqn, propertyName);
                            if (items.isEmpty()) {
                                Collection<? extends IndexResult> tmpItems = jsIndex.findByFqn(fqn, Index.TERMS_BASIC_INFO);
                                for (IndexResult indexResult : tmpItems) {
                                    Collection<TypeUsage> tmpAssignments = IndexedElement.getAssignments(indexResult);
                                    for (Type tmpType : tmpAssignments) {
                                        items = findPropertyOfType(jsIndex, ModelUtils.getFQNFromType(tmpType), propertyName);
                                        indexResults.addAll(items);
                                    }
                                }
                            } else {
                                indexResults.addAll(items);
                            }
                        }
                        DeclarationLocation location = processIndexResult(indexResults);
                        if (location != null) {
                            return location;
                        }
                    }
                }
            }
        }
        for (DeclarationFinder finder : EditorExtender.getDefault().getDeclarationFinders()) {
            DeclarationLocation loc = finder.findDeclaration(info, caretOffset);
            if (loc != null && loc != DeclarationLocation.NONE) {
                return loc;
            }
        }
        // try to find the symbol in index and offer the declarations.
        if (occurrence != null) {
            JsObject object = occurrence.getDeclarations().iterator().next();
            FileObject fo = object.getFileObject();
            if (fo != null && object.getName() != null) {
                Collection<? extends IndexResult> items = Index.get(fo).query(Index.FIELD_BASE_NAME, object.getName(), QuerySupport.Kind.EXACT, Index.TERMS_BASIC_INFO);
                List<IndexResult> indexResults = new ArrayList<>();
                for (IndexResult item : items) {
                    IndexedElement element = IndexedElement.create(item);
                    if (!element.getModifiers().contains(Modifier.PRIVATE) && element.getJSKind() != JsElement.Kind.PARAMETER) {
                        indexResults.add(item);
                    }
                }
                DeclarationLocation location = processIndexResult(indexResults);
                if (location != null) {
                    return location;
                }
            }
        }

        return DeclarationLocation.NONE;
    }

    private int getDeclarationOffset(JsObject object) {
        return object.getDeclarationName() != null
                ? object.getDeclarationName().getOffsetRange().getStart()
                : object.getOffset();
    }

    private JsObject findPropertyOrParameterInModel(JsObject parent, String name) {
        JsObject object = parent.getProperty(name);
        if (object == null && parent instanceof JsFunction) {
            object = ((JsFunction)parent).getParameter(name);
        }
        return object;
    }

    private Collection<? extends IndexResult> findPropertyOfType(Index jsIndex, String fqn, String propertyName) {
        return findPropertyOfType(jsIndex, fqn, propertyName, 0);
    }

    private Collection<? extends IndexResult> findPropertyOfType(Index jsIndex, String fqn, String propertyName, int count) {
        List<IndexResult> items = new ArrayList<>();
        if (count > 5) {
            return items;
        }
        items.addAll(jsIndex.findByFqn(
                fqn + "." + propertyName, Index.TERMS_BASIC_INFO)); // NOI18N
        if (items.isEmpty()) {
            items.addAll(jsIndex.findByFqn(fqn + ".prototype." + propertyName, Index.TERMS_BASIC_INFO)); // NOI18N
        }
        if (items.isEmpty()) {
            Collection<? extends IndexResult> findByFqn = jsIndex.findByFqn(fqn, Index.TERMS_BASIC_INFO);
            for (IndexResult indexResult : findByFqn) {
            Collection<TypeUsage> assignments = IndexedElement.getAssignments(indexResult);
                for (Type tmpType : assignments) {
                    items.addAll(findPropertyOfType(jsIndex, ModelUtils.getFQNFromType(tmpType), propertyName, count++));
                }
            }
        }
        return items;
    }

    private DeclarationLocation processIndexResult(List<IndexResult> indexResults) {
        if (!indexResults.isEmpty()) {
            IndexResult iResult = indexResults.get(0);
            if (iResult.getFile() == null) {
                return null;
            }
            String value = iResult.getValue(Index.FIELD_OFFSET);
            int offset = Integer.parseInt(value);
            HashSet<String> alreadyThere = new HashSet<>();
            DeclarationLocation location = new DeclarationLocation(iResult.getFile(), offset, IndexedElement.create(iResult));
            alreadyThere.add(iResult.getFile().getPath() + offset);
            if (indexResults.size() > 1) {
                for (int i = 0; i < indexResults.size(); i++) {
                    iResult = indexResults.get(i);
                    if (iResult != null && iResult.getFile() != null && !alreadyThere.contains(iResult.getFile().getPath() + offset)) {
                        location.addAlternative(new AlternativeLocationImpl(iResult));
                        alreadyThere.add(iResult.getFile().getPath() + offset);
                    }
                }
            }
            return location;
        }
        return null;
    }

    @Override
    public OffsetRange getReferenceSpan(final Document doc, final int caretOffset) {
        if (doc == null) {
            return OffsetRange.NONE;
        }
        final OffsetRange[] value = new OffsetRange[1];

        doc.render(() -> {
            TokenSequence<? extends JsTokenId> ts = LexUtilities.getTokenSequence(doc, caretOffset, language);
            if (ts != null) {
                ts.move(caretOffset);
                if (ts.moveNext() && (ts.token().id() == JsTokenId.IDENTIFIER || ts.token().id() == JsTokenId.PRIVATE_IDENTIFIER)) {
                    value[0] = new OffsetRange(ts.offset(), ts.offset() + ts.token().length());
                } else if (ts.token() != null && ts.token().id() == JsTokenId.DOC_COMMENT) {
                    TokenSequence<? extends JsDocumentationTokenId> tsDoc = LexerUtils.getTokenSequence(doc, caretOffset, JsDocumentationTokenId.language(), true);
                    if (tsDoc != null) {
                        if (tsDoc.token() != null && tsDoc.token().id() == JsDocumentationTokenId.OTHER) {
                            if (tsDoc.moveNext() && tsDoc.token().id() == JsDocumentationTokenId.BRACKET_RIGHT_CURLY
                                    && tsDoc.movePrevious() && tsDoc.movePrevious() && tsDoc.token().id() == JsDocumentationTokenId.BRACKET_LEFT_CURLY) {
                                tsDoc.moveNext();
                                value[0] = new OffsetRange(tsDoc.offset(), tsDoc.offset() + tsDoc.token().length());
                            }
                        }
                    }
                } else if (ts.token() != null && ts.token().id() == JsTokenId.STRING) {
                    // we need to check, where we are in the import expression (ES6)
                    int start = ts.offset();
                    int end = ts.offset() + ts.token().length();
                    Token<? extends JsTokenId> token = LexUtilities.findPreviousToken(ts, Utils.LOOK_FOR_IMPORT_EXPORT_TOKENS);
                    if (token.id() == JsTokenId.KEYWORD_IMPORT || token.id() == JsTokenId.KEYWORD_EXPORT) {
                        value[0] = new OffsetRange (start, end);
                    }
                }
            }
        });
        if (value[0] != null) {
            return value[0];
        }

        OffsetRange result;
        for (DeclarationFinder finder : EditorExtender.getDefault().getDeclarationFinders()) {
            result = finder.getReferenceSpan(doc, caretOffset);
            if (result != null && result != OffsetRange.NONE) {
                return result;
            }
        }
        return OffsetRange.NONE;
    }

    // Note: this class has a natural ordering that is inconsistent with equals.
    // We have to implement AlternativeLocation
    @org.netbeans.api.annotations.common.SuppressWarnings("EQ_COMPARETO_USE_OBJECT_EQUALS")
    public static class AlternativeLocationImpl implements AlternativeLocation {

        private final IndexResult iResult;
        private final int offset;
        private final DeclarationLocation location;
        private final IndexedElement element;
        private String stringLocation;

        public AlternativeLocationImpl(IndexResult iResult) {
            this.iResult = iResult;
            String value = iResult.getValue(Index.FIELD_OFFSET);
            this.offset = Integer.parseInt(value);
            this.location = new DeclarationLocation(iResult.getFile(), offset);
            this.element = IndexedElement.create(iResult);
        }

        @Override
        public ElementHandle getElement() {
            return element;
        }

        private String getStringLocation() {
            if(stringLocation != null) {
                return stringLocation;
            }
            int lineNumber = 0;
            int count = 0;
            List<String> asLines;
            try {
                asLines = element.getFileObject().asLines();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                asLines = null;
            }
            if (asLines != null) {
                for (String line : asLines) {
                    lineNumber++;
                    count += line.length();
                    if (count >= offset) {
                        break;
                    }
                }
            }
            String result = iResult.getRelativePath();
            if (lineNumber > 0) {
                result = result + " : " + lineNumber; //NOI18N
            }
            stringLocation = result;
            return result;
        }

        @Override
        public String getDisplayHtml(HtmlFormatter formatter) {
            formatter.appendText(getStringLocation());
            return formatter.getText();
        }

        @Override
        public DeclarationLocation getLocation() {
            return location;
        }

        @Override
        public int compareTo(AlternativeLocation o) {
            AlternativeLocationImpl ali = (AlternativeLocationImpl)o;
            String relPath1 = iResult.getRelativePath();
            String relPath2 = ali.iResult.getRelativePath();
            int comparison = relPath1.compareTo(relPath2);
            if(comparison != 0) {
                return comparison;
            }
            return offset - ali.offset;
        }

    }

}
