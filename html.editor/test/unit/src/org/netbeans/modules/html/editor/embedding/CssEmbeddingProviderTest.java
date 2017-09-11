/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.embedding;

import org.netbeans.modules.html.editor.embedding.CssEmbeddingProvider;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.html.editor.test.TestBase;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Marek Fukala
 */
public class CssEmbeddingProviderTest extends TestBase {

    public CssEmbeddingProviderTest(String name) {
        super(name);
    }

    public void testBasicFile() throws Exception {
        checkVirtualSource("testfiles/embedding/test1.html");
    }

    public void testIssue174241() throws Exception {
        checkVirtualSource("testfiles/embedding/testIssue174241.html");
        checkVirtualSource("testfiles/embedding/testIssue174241_1.html");
    }
    
    public void testIssue215267() throws Exception {
        checkVirtualSource("testfiles/embedding/issue215267.html");
    }
    
    public void testIssue215267_css_embedding_at_offset() throws Exception {
        FileObject fo = getTestFile("testfiles/embedding/issue215267.html");
        assertNotNull(fo);

        Source source = Source.create(fo);
        assertNotNull(source);
        
        final int caret = 147;
        ParserManager.parse(Collections.singleton(source), new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                Result parserResult = resultIterator.getParserResult(caret);
                assert parserResult instanceof CssParserResult;
            }
        });
        
    }

    private void checkVirtualSource(String file) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);

        Source source = Source.create(fo);
        assertNotNull(source);

        Snapshot snapshot = source.createSnapshot();
        assertNotNull(snapshot);

        CssEmbeddingProvider provider = new CssEmbeddingProvider();

        List<Embedding> embeddings = provider.getEmbeddings(snapshot);
        assertNotNull(embeddings);
        assertEquals(1, embeddings.size());

        Embedding embedding = embeddings.get(0);
        Snapshot virtualSource = embedding.getSnapshot();
        String virtualSourceText = virtualSource.getText().toString();

        assertDescriptionMatches(file, virtualSourceText, false, ".virtual");
    }
}
