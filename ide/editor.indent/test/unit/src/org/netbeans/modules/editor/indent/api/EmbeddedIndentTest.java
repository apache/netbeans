/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.editor.indent.api;

import java.util.prefs.BackingStoreException;
import java.util.List;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.Preferences;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.lang.TestLineTokenId;
import org.netbeans.lib.lexer.lang.TestPlainTokenId;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.ReformatTask;

/**
 *
 * @author Miloslav Metelka
 */
public class EmbeddedIndentTest extends NbTestCase {
    
    private LineReformatTask.Factory lineReformatTaskFactory;
    
    private PlainReformatTask.Factory plainReformatTaskFactory;
    
    public EmbeddedIndentTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(MockMimeLookup.class);

        Preferences prefs = new AbstractPreferences(null, "") {
            protected @Override void putSpi(String key, String value) {
            }
            protected @Override String getSpi(String key) {
                return null;
            }
            protected @Override void removeSpi(String key) {
            }
            protected @Override void removeNodeSpi() throws BackingStoreException {
            }
            protected @Override String[] keysSpi() throws BackingStoreException {
                return new String[0];
            }
            protected @Override String[] childrenNamesSpi() throws BackingStoreException {
                return new String[0];
            }
            protected @Override AbstractPreferences childSpi(String name) {
                return null;
            }
            protected @Override void syncSpi() throws BackingStoreException {
            }
            protected @Override void flushSpi() throws BackingStoreException {
            }
        };
        
        // Text will be lexed as TestLineTokenId.LINE
        TestLanguageProvider.register(TestLineTokenId.language());
        lineReformatTaskFactory = new LineReformatTask.TestFactory();
        MockMimeLookup.setInstances(MimePath.parse(TestLineTokenId.MIME_TYPE), lineReformatTaskFactory, prefs);
        
        // Each TestLineTokenId.LINE will be branched into TestPlainTokenId.WORD and WHITESPACE
        TestLanguageProvider.registerEmbedding(TestLineTokenId.language().mimeType(), TestLineTokenId.LINE,
            TestPlainTokenId.language(), 0, 0, false);
        plainReformatTaskFactory = new PlainReformatTask.TestFactory();
        MockMimeLookup.setInstances(
                MimePath.parse(LanguagePath.get(TestLineTokenId.language()).embedded(TestPlainTokenId.language()).mimePath()), 
                plainReformatTaskFactory,
                prefs
        );
    }

    protected @Override void tearDown() throws Exception {
        MockMimeLookup.setLookup(MimePath.parse(TestLineTokenId.MIME_TYPE));
        MockMimeLookup.setLookup(
                MimePath.parse(LanguagePath.get(TestLineTokenId.language()).embedded(TestPlainTokenId.language()).mimePath())
        );
        super.tearDown();
    }
    
    public void testFindIndentTaskFactory() throws BadLocationException {
        Document doc = new PlainDocument();
        doc.insertString(0, "first line\nsecond-line", null);
        doc.putProperty("mimeType", TestLineTokenId.MIME_TYPE);
        
        TokenHierarchy hi = TokenHierarchy.get(doc);
        assertNotNull(hi);
        TokenSequence<?> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        TokenSequence<?> ets = ts.embedded();
        assertNotNull(ets);
        // There should be two language paths - root one and one with plain embedding
        assertEquals(2, hi.languagePaths().size());
        
        Reformat reformat = Reformat.get(doc);
        reformat.lock();
        try {
            //doc.atomicLock();
            try {
                reformat.reformat(0, doc.getLength());
            } finally {
                //doc.atomicUnlock();
            }
        } finally {
            reformat.unlock();
        }
        
        String text = doc.getText(0, doc.getLength());
        // lineReformatTaskFactory should be called first and its mimetype at the begining of document
        // and plainReformatTaskFactory should be called second and also add its mimetype at the begining of document
        assertEquals(TestPlainTokenId.MIME_TYPE + "/" + TestLineTokenId.MIME_TYPE + "/first line\nsecond-line", text);
    }

    private static final class LineReformatTask implements ReformatTask {
        
        static Position reformatPos;
        
        private Context context;
        
        LineReformatTask(Context context) {
            this.context = context;
        }

        public void reformat() throws BadLocationException {
            assertEquals(context.startOffset(), 0);
            int lineStartOffset = context.lineStartOffset(context.startOffset());
            assertEquals(lineStartOffset, context.startOffset());
            context.modifyIndent(context.startOffset(), 2);
            assertEquals(context.lineIndent(lineStartOffset), 2);
            context.modifyIndent(context.startOffset(), 0);
            assertEquals(context.lineIndent(lineStartOffset), 0);
            List<Context.Region> regions = context.indentRegions();
            Context.Region region = regions.get(0);
            assertEquals(region.getStartOffset(), 0);
            assertEquals(region.getEndOffset(), context.document().getLength());
            context.document().insertString(context.startOffset(), TestLineTokenId.MIME_TYPE + "/", null);
            reformatPos = context.document().createPosition(context.startOffset());
        }

        public ExtraLock reformatLock() {
            return null;
        }
        
        static final class TestFactory implements ReformatTask.Factory {
            
            public ReformatTask createTask(Context context) {
                return new LineReformatTask(context);
            }
            
        }

    }
    
    private static final class PlainReformatTask implements ReformatTask {
        
        static Position reformatPos;

        private Context context;
        
        
        PlainReformatTask(Context context) {
            this.context = context;
        }

        public void reformat() throws BadLocationException {
            context.document().insertString(context.startOffset(), TestPlainTokenId.MIME_TYPE + "/", null);
            reformatPos = context.document().createPosition(context.startOffset());
        }
        
        public ExtraLock reformatLock() {
            return null;
        }
        
        static final class TestFactory implements ReformatTask.Factory {
            
            public ReformatTask createTask(Context context) {
                return new PlainReformatTask(context);
            }
            
        }

    }
    
}
