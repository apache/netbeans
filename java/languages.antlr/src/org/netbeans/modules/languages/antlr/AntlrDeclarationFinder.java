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

import java.util.HashSet;
import java.util.Set;
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
import org.netbeans.modules.languages.antlr.AntlrParserResult.Reference;
import org.netbeans.modules.languages.antlr.v4.Antlr4ParserResult;
import org.openide.filesystems.FileObject;

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
        FileObject fo = info.getSnapshot().getSource().getFileObject();
        Set<FileObject> scannedFiles = new HashSet<>();

        DeclarationLocation ret = getDeclarationLocation(fo, ref, DeclarationLocation.NONE, scannedFiles);
        if (ret == DeclarationLocation.NONE) {
            FileObject rfo = fo.getParent().getFileObject(ref, "g4");
            if (rfo != null) {
                ret = new DeclarationFinder.DeclarationLocation(rfo, 0);
            }
        }
        return ret;
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

    public static DeclarationLocation getDeclarationLocation(FileObject fo, String name, DeclarationLocation existingDL, Set<FileObject> scannedFiles) {
        DeclarationLocation resultDL = existingDL;

        if(scannedFiles.contains(fo)) {
            return resultDL;
        }
        scannedFiles.add(fo);

        AntlrParserResult<?> result = AntlrParser.getParserResult(fo);

        Reference ref = result.references.get(name);

        if(ref != null && ref.defOffset != OffsetRange.NONE) {
            AntlrStructureItem asi = new AntlrStructureItem.RuleStructureItem(name, false, fo, ref.defOffset.getStart(), ref.defOffset.getEnd());
            DeclarationLocation dln = new DeclarationFinder.DeclarationLocation(fo, ref.defOffset.getStart(), asi);
            if (resultDL == DeclarationLocation.NONE) {
                resultDL = dln;
            }
            // If multiple declaration locations are possible (antlr4
            // allows redefinition), the original location must be part
            // of the alternative locations.
            resultDL.addAlternative(new AlternativeLocationImpl(dln));
        }

        if(result instanceof Antlr4ParserResult) {
            for(String s: ((Antlr4ParserResult) result).getImports()) {
                FileObject importedFo = fo.getParent().getFileObject(s, "g4");
                if(importedFo != null) {
                    resultDL = getDeclarationLocation(importedFo, name, resultDL, scannedFiles);
                }
            }
        }

        return resultDL;
    }

    private static class AlternativeLocationImpl implements AlternativeLocation {

        private final DeclarationLocation location;

        public AlternativeLocationImpl(DeclarationLocation location) {
            this.location = location;
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
            return 0;
        }

    }
}
