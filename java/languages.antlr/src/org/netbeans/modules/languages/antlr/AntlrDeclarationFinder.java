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
package org.netbeans.modules.languages.antlr;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport.Query;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport.Query.Factory;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

import static org.netbeans.modules.languages.antlr.AntlrIndexer.FIELD_DECLARATION;
import static org.netbeans.modules.languages.antlr.AntlrIndexer.transitiveImports;

/**
 *
 * @author lkishalmi
 */
public class AntlrDeclarationFinder implements DeclarationFinder {

    @Override
    public DeclarationLocation findDeclaration(ParserResult info, int caretOffset) {
        TokenSequence<?> ts = info.getSnapshot().getTokenHierarchy().tokenSequence();
        ts.move(caretOffset);
        ts.movePrevious();
        ts.moveNext();
        Token<?> token = ts.token();
        String ref = String.valueOf(token.text());
        return getDeclarationLocation(info.getSnapshot().getSource().getFileObject(), ref);
    }

    @Override
    public OffsetRange getReferenceSpan(Document document, int caretOffset) {
        AbstractDocument doc = (AbstractDocument) document;
        doc.readLock();
        try {
            TokenHierarchy<Document> th = TokenHierarchy.get(doc);
            TokenSequence<?> ts = th.tokenSequence();
            ts.move(caretOffset);
            ts.movePrevious();
            ts.moveNext();
            Token<?> token = ts.token();
            if ((token.id() == AntlrTokenId.RULE) || (token.id() == AntlrTokenId.TOKEN)) {
                int start = ts.offset();
                ts.moveNext();
                int end = ts.offset();
                return new OffsetRange(start, end);
            } else {
                return OffsetRange.NONE;
            }
        } finally {
            doc.readUnlock();
        }
    }

    public static DeclarationLocation getDeclarationLocation(FileObject sourceFile, String name) {
        try {
            QuerySupport qs = AntlrIndexer.getQuerySupport(sourceFile);
            Factory qf = qs.getQueryFactory();
            String targetDefinition = name + AntlrIndexer.SEPARATOR;
            List<FileObject> candidates = transitiveImports(qs, sourceFile);
            Query query = qf.and(
                    // Only consider the file itself or imported files
                    qf.or(
                           candidates.stream()
                                    .map(fo -> qs.getQueryFactory().file(fo))
                                    .collect(Collectors.toList())
                                    .toArray(new Query[0])
                    ),
                    qf.field(FIELD_DECLARATION, targetDefinition, QuerySupport.Kind.PREFIX)
            );

            DeclarationFinder.DeclarationLocation dl = null;
            for(IndexResult ir: query.execute(FIELD_DECLARATION)) {
                for (String value : ir.getValues(FIELD_DECLARATION)) {
                    if (!value.startsWith(targetDefinition)) {
                        continue;
                    }
                    String[] values = value.split("\\\\");
                    int start = Integer.parseInt(values[1]);
                    int end = Integer.parseInt(values[2]);
                    AntlrStructureItem asi = new AntlrStructureItem.RuleStructureItem(name, ir.getFile(), start, end);
                    DeclarationLocation dln = new DeclarationFinder.DeclarationLocation(ir.getFile(), start, asi);
                    if (dl == null) {
                        dl = dln;
                    }
                    // If multiple declaration locations are possible (antlr4
                    // allows redefinition), the original location must be part
                    // of the alternative locations.
                    //
                    // The sortIdx describes the "depth" of the inheritence tree
                    // until the location is found. Nearer imports are preferred
                    dl.addAlternative(new AlternativeLocationImpl(dln, candidates.indexOf(ir.getFile())));
                }
            }
            if(dl == null) {
                dl = DeclarationFinder.DeclarationLocation.NONE;
            }
            return dl;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return DeclarationFinder.DeclarationLocation.NONE;
        }
    }

    private static class AlternativeLocationImpl implements AlternativeLocation {

        private final DeclarationLocation location;
        private final int sortIdx;

        public AlternativeLocationImpl(DeclarationLocation location, int sortIdx) {
            this.location = location;
            this.sortIdx = sortIdx;
        }

        @Override
        public ElementHandle getElement() {
            return getLocation().getElement();
        }

        @Override
        public String getDisplayHtml(HtmlFormatter formatter) {
            return getLocation().toString();
        }

        @Override
        public DeclarationFinder.DeclarationLocation getLocation() {
            return location;
        }

        @Override
        public int compareTo(DeclarationFinder.AlternativeLocation o) {
            if(o instanceof AlternativeLocationImpl) {
                return sortIdx - ((AlternativeLocationImpl) o).sortIdx;
            } else {
                return 0;
            }
        }

    }
}
