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

package org.netbeans.modules.editor.lib;

import java.io.PrintStream;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.undo.UndoManager;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.random.RandomTestContainer;
import org.netbeans.lib.editor.util.random.DocumentTesting;
import org.netbeans.lib.editor.util.random.RandomTestContainer.Context;
import org.netbeans.lib.editor.util.random.RandomText;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.lib2.document.ModRootElement;

public class TrailingWhitespaceRemoveTest extends NbTestCase {

    public TrailingWhitespaceRemoveTest(String testName) {
        super(testName);
    }

    @Override
    public PrintStream getLog() {
        return System.err;
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    public void testTrailingWhitespaceRemove_modifiedLines() throws Exception {
        checkTrailingWhitespaceRemove("modified-lines", "abc a\nd\n ef\n\n123 \n567 \nghi\n");
    }

    public void testTrailingWhitespaceRemove_always() throws Exception {
        checkTrailingWhitespaceRemove("always", "abc a\nd\n ef\n\n123\n567\nghi\n");
    }

    public void testTrailingWhitespaceRemove_never() throws Exception {
        checkTrailingWhitespaceRemove("never", "abc a \nd \n ef\n\n123 \n567 \nghi\n ");
    }

    private void checkTrailingWhitespaceRemove(String policy, String result) throws Exception {
        MockServices.setServices(MockMimeLookup.class);
        MockMimeLookup.setInstances(MimePath.parse(""),
                new TrailingWhitespaceRemove.FactoryImpl()
        );
        
        RandomTestContainer container = DocumentTesting.initContainer(null);
        container.setName(this.getName());
//        container.putProperty(RandomTestContainer.LOG_OP, Boolean.TRUE);
//        container.putProperty(DocumentTesting.LOG_DOC, Boolean.TRUE);
        container.addOp(new Op());
        container.addCheck(new Check());

        // Init trailing spaces removal
//        Document doc = container.getInstance(Document.class);
        BaseDocument doc = new BaseDocument(BaseKit.class, false);
        UndoManager undoManager = new UndoManager();
        doc.addUndoableEditListener(undoManager);
        doc.putProperty(UndoManager.class, undoManager);

        container.putProperty(Document.class, doc); // Replace original doc

        RandomText randomText = RandomText.join(
                RandomText.lowerCaseAZ(1),
                RandomText.spaceTabNewline(1),
                RandomText.phrase(" \n ", 1)
        );
        container.putProperty(RandomText.class, randomText);
        
        // Do a fixed scenario
        DocumentTesting.insert(container.context(), 0, "abc\ndef\n\n123 \n567 \nghi");
        ModRootElement modRootElement = ModRootElement.get(doc);
        modRootElement.resetMods(null);

        DocumentTesting.insert(container.context(), 3, " a ");
        //  000000000011111111111222222222
        //  012345678901234567890123456789
        // "abc a ndefnn123 n567 nghi"
        DocumentTesting.insert(container.context(), 8, " \n ");
        //  000000000011111111111222222222
        //  012345678901234567890123456789
        // "abc a nd n efnn123 n567 nghi"
        DocumentTesting.insert(container.context(), 28, "\n ");
        //  000000000011111111111222222222
        //  012345678901234567890123456789
        // "abc a nd n efnn123 n567 nghin "
        removeTrailingWhitespace(container.context(), policy);
        // Should be
        //  000000000011111111111222222222
        //  012345678901234567890123456789
        // "abc andn efnn123 n567 nghin"
        assertEquals(result, doc.getText(0, doc.getLength()));
        

        RandomTestContainer.Round round = container.addRound();
        round.setOpCount(1000);
        round.setRatio(DocumentTesting.INSERT_CHAR, 6);
        round.setRatio(DocumentTesting.INSERT_TEXT, 3);
        round.setRatio(DocumentTesting.INSERT_PHRASE, 1);
        round.setRatio(DocumentTesting.REMOVE_CHAR, 3);
        round.setRatio(DocumentTesting.REMOVE_TEXT, 1);
        round.setRatio(DocumentTesting.UNDO, 1);
        round.setRatio(DocumentTesting.REDO, 1);
        round.setRatio(Op.NAME, 0.5d);
        container.run(1213202006348L);
//        container.run(0L); // Random operation

    }

    public static void checkNoTrailingWhitespace(Document doc) {
        Element lineRoot = DocumentUtilities.getParagraphRootElement(doc);
        CharSequence docText = DocumentUtilities.getText(doc);
        for (int i = 0; i < lineRoot.getElementCount(); i++) {
            Element lineElem = lineRoot.getElement(i);
            int lineLastOffset = lineElem.getEndOffset() - 2;
            if (lineLastOffset >= lineElem.getStartOffset()) { // At least one non newline char
                switch (docText.charAt(lineLastOffset)) {
                    case ' ':
                    case '\t':
                        throw new IllegalStateException("Trailing whitespace exists at lineIndex=" + i + // NOI18N
                                ", lineStartOffset=" + lineElem.getStartOffset() + // NOI18N
                                ", lineEndOffset=" + lineElem.getEndOffset() + // NOI18N
                                '\n' + dumpLines(null, doc));
                }
            }
        }
    }

    public static StringBuilder dumpLines(StringBuilder sb, Document doc) {
        if (sb == null)
            sb = new StringBuilder(doc.getLength() + 200);
        Element lineRoot = DocumentUtilities.getParagraphRootElement(doc);
        CharSequence docText = DocumentUtilities.getText(doc);
        int lineCount = lineRoot.getElementCount();
        sb.append(lineCount).append(" document lines:\n");
        for (int i = 0; i < lineCount; i++) {
            Element lineElem = lineRoot.getElement(i);
            int startOffset = lineElem.getStartOffset();
            int endOffset = lineElem.getEndOffset();
            ArrayUtilities.appendBracketedIndex(sb, i, 2);
            sb.append('<').append(startOffset).append(',').append(endOffset).append("> \"");
            while (startOffset < endOffset) {
                CharSequenceUtilities.debugChar(sb, docText.charAt(startOffset++));
            }
            sb.append("\"\n");
        }
        return sb;
    }
    
    public static void removeTrailingWhitespace(Context context, String policy) {
        Document doc = context.getInstance(Document.class);
        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        prefs.put(SimpleValueNames.ON_SAVE_REMOVE_TRAILING_WHITESPACE, policy);
        Runnable beforeSaveRunnable = (Runnable) doc.getProperty("beforeSaveRunnable");
        assertNotNull(beforeSaveRunnable);
        beforeSaveRunnable.run();
    }

    private static void removeTrailingWhitespace(Document doc) throws Exception {
        Element lineRoot = DocumentUtilities.getParagraphRootElement(doc);
        CharSequence docText = DocumentUtilities.getText(doc);
        int lineCount = lineRoot.getElementCount();
        for (int i = 0; i < lineCount; i++) {
            Element lineElem = lineRoot.getElement(i);
            int lineStartOffset = lineElem.getStartOffset();
            int lineLastOffset = lineElem.getEndOffset() - 1;
            int offset;
            for (offset = lineLastOffset - 1; offset >= lineStartOffset; offset--) {
                char c = docText.charAt(offset);
                // Currently only remove ' ' and '\t' - may be revised
                if (c != ' ' && c != '\t') {
                    break;
                }
            }
            // Increase offset (either below lineStartOffset or on non-white char)
            offset++;
            if (offset < lineLastOffset) {
                doc.remove(offset, lineLastOffset - offset);
            }
        }
    }

    private static final class Op extends RandomTestContainer.Op {

        static final String NAME = "trailing-whitespace-remove";

        public Op() {
            super(NAME);
        }

        @Override
        protected void run(Context context) throws Exception {
            Document doc = context.getInstance(Document.class);
            logOp(context, doc, "Before");
            Runnable beforeSaveRunnable = (Runnable) doc.getProperty("beforeSaveRunnable");
            assertNotNull(beforeSaveRunnable);
            beforeSaveRunnable.run();
            ModRootElement modRootElement = ModRootElement.get(doc); // Should be non-null for BaseDocument
            modRootElement.resetMods(null);

            logOp(context, doc, "After");
            // Remove all chars from document because the following case is currently unhandled:
            // 1. There is a line " a\n"
            // 2. Remove 'a'
            // 3. Line is " \n" but trailing WS will not be removed.
//            doc.remove(0, doc.getLength());
            //checkNoTrailingWhitespace(doc);
            
        }

        private void logOp(Context context, Document doc, String msg) throws Exception {
            if (Boolean.TRUE.equals(context.round().getPropertyOrNull(RandomTestContainer.LOG_OP))) {
                StringBuilder sb = new StringBuilder(doc.getLength() + 50);
                dumpLines(sb, doc);
                sb.append("\n");
                context.container().logger().info(sb.toString());
            }

        }

    }

    private static final class Check extends RandomTestContainer.Check {
        
        private final boolean LOG_STATUS = false;

        @Override
        protected void check(Context context) throws Exception {
            Document doc = context.getInstance(Document.class);
            TrailingWhitespaceRemove tws = (TrailingWhitespaceRemove) doc.getProperty(TrailingWhitespaceRemove.class);
            if (LOG_STATUS) {
                StringBuilder sb = new StringBuilder(50);
                sb.append(tws).append("\n");
                dumpLines(sb, doc).append('\n');
                context.container().logger().info(sb.toString());
            }
            ModRootElement modRootElement = ModRootElement.get(doc); // Should be non-null for BaseDocument
            modRootElement.checkConsistency();
        }

    }

}
