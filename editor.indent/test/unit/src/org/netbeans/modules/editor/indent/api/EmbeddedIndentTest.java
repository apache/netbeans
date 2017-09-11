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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
