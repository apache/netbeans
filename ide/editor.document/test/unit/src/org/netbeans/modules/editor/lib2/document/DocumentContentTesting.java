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

package org.netbeans.modules.editor.lib2.document;

import java.util.Random;
import javax.swing.JEditorPane;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.random.PropertyProvider;
import org.netbeans.lib.editor.util.random.RandomTestContainer;
import org.netbeans.lib.editor.util.random.RandomTestContainer.Context;
import org.netbeans.lib.editor.util.random.RandomText;

public class DocumentContentTesting {

    /** Name of op that inserts a random single char into document. */
    public static final String INSERT_CHAR = "doc-insert-char";

    /**
     * Name of op that inserts multiple random chars at once into document.
     * INSERT_TEXT_MAX_LENGTH gives how much chars at once at maximum.
     */
    public static final String INSERT_TEXT = "doc-insert-text";

    /** Name of op that inserts a random phrase into document. */
    public static final String INSERT_PHRASE = "doc-insert-phrase";

    /** Name of op that removes a single char from document. */
    public static final String REMOVE_CHAR = "doc-remove-char";

    /**
     * Name of op that removes multiple chars at once from document.
     * REMOVE_TEXT_MAX_LENGTH gives how much chars at once at maximum.
     */
    public static final String REMOVE_TEXT = "doc-remove-text";

    /** Undo by doc.getProperty(UndoManager.class). */
    public static final String UNDO = "doc-undo";

    /** Redo by doc.getProperty(UndoManager.class). */
    public static final String REDO = "doc-redo";
    
    /** Create position. */
    public static final String CREATE_POSITION = "doc-create-position";
    
    /** Create position. */
    public static final String CREATE_BACKWARD_BIAS_POSITION = "doc-create-backward-bias-position";
    
    /** Release existing position. */
    public static final String RELEASE_POSITION = "doc-release-position";
    

    /** Maximum number of chars inserted by INSERT_TEXT op. */
    public static final String INSERT_TEXT_MAX_LENGTH = "doc-insert-text-max-length";

    /** Maximum number of chars removed by REMOVE_TEXT op. */
    public static final String REMOVE_TEXT_MAX_LENGTH = "doc-remove-text-max-length";

    /** Maximum number of undo/redo (4 by default) to be performed at once. */
    public static final String MAX_UNDO_REDO_COUNT = "doc-undo-redo-count";

    /** Maximum number of create/releasePosition (4 by default) to be performed at once. */
    public static final String MAX_CREATE_RELEASE_POSITION_COUNT = "doc-create-release-position-count";

    /** Ratio (0.3 by default) with which there will be inverse operation
     * to just performed undo/redo.
     */
    public static final String UNDO_REDO_INVERSE_RATIO = "doc-undo-redo-inverse-ratio";
    
    /** java.lang.Boolean whether whole document text should be logged. */
    public static final String LOG_DOC = "doc-log-doc";

    public static RandomTestContainer createContainer() {
        RandomTestContainer container = new RandomTestContainer();
        container.addCheck(new DocumentContentCheck());
        Document doc = getDocument(container);
        Document expectedDoc = getExpectedDocument(container);
        if (expectedDoc == null) {
            expectedDoc = new ExpectedDocument();
            container.putProperty(ExpectedDocument.class, expectedDoc);
        }
        if (doc == null) {
            doc = new TestEditorDocument();
            container.putProperty(Document.class, doc);
        }
        ((ExpectedDocument)expectedDoc).setTestDocument((TestEditorDocument)doc);
        UndoManager undoManager = getUndoManager(container);
        if (undoManager == null) {
            undoManager = new UndoMgr(container);
            container.putProperty(UndoManager.class, undoManager);
            doc.putProperty(UndoManager.class, undoManager);
            // Add them here to be sure they are added just once (not correct - should analyze existing listeners)
            doc.addUndoableEditListener(undoManager);
            expectedDoc.addUndoableEditListener(undoManager);
        }
        container.putProperty(UNDO_REDO_INVERSE_RATIO, 0.3f);

        // Init a default random text if not yet inited (can be reinited later)
        if (container.getInstanceOrNull(RandomText.class) == null) {
            RandomText randomText = RandomText.join(
                    RandomText.lowerCaseAZ(3),
                    RandomText.spaceTabNewline(1),
                    RandomText.phrase("   ", 1),
                    RandomText.phrase("\n\n", 1),
                    RandomText.phrase("\t\tabcdef\t", 1));
            container.putProperty(RandomText.class, randomText);
        }

        container.addOp(new InsertOp(INSERT_CHAR));
        container.addOp(new InsertOp(INSERT_TEXT));
        container.addOp(new InsertOp(INSERT_PHRASE));
        container.addOp(new RemoveOp(REMOVE_CHAR));
        container.addOp(new RemoveOp(REMOVE_TEXT));
        container.addOp(new UndoRedoOp(UNDO));
        container.addOp(new UndoRedoOp(REDO));
        container.addOp(new PositionOp(CREATE_POSITION));
        container.addOp(new PositionOp(CREATE_BACKWARD_BIAS_POSITION));
        container.addOp(new PositionOp(RELEASE_POSITION));
        return container;
    }

    /**
     * Get document from test container by consulting either an editor pane's document
     * or a document property.
     *
     * @param provider non-null property provider
     * @return document instance or null.
     */
    public static Document getDocument(PropertyProvider provider) {
        JEditorPane pane = provider.getInstanceOrNull(JEditorPane.class);
        if (pane != null) {
            return pane.getDocument();
        } else {
            return provider.getInstanceOrNull(Document.class);
        }
    }

    public static Document getValidDocument(PropertyProvider provider) {
        Document doc = getDocument(provider);
        if (doc == null) {
            throw new IllegalStateException("Null Document for property provider " + provider); // NOI18N
        }
        return doc;
    }
    
    public static Document getExpectedDocument(PropertyProvider provider) {
        return provider.getInstanceOrNull(ExpectedDocument.class);
    }

    public static Document getValidExpectedDocument(PropertyProvider provider) {
        Document doc = getExpectedDocument(provider);
        if (doc == null) {
            throw new IllegalStateException("Null ExpectedDocument for property provider " + provider); // NOI18N
        }
        return doc;
    }

    public static UndoManager getUndoManager(PropertyProvider provider) {
        return provider.getInstanceOrNull(UndoManager.class);
    }

    public static UndoManager getValidUndoManager(PropertyProvider provider) {
        UndoManager undoManager = getUndoManager(provider);
        if (undoManager == null) {
            throw new IllegalStateException("Null UndoManager for property provider " + provider); // NOI18N
        }
        return undoManager;
    }

    public static boolean isLogDoc(PropertyProvider provider) {
        return Boolean.TRUE.equals(provider.getPropertyOrNull(LOG_DOC));
    }

    public static void setLogDoc(PropertyProvider provider, boolean logDoc) {
        provider.putProperty(LOG_DOC, logDoc);
    }

    public static void insert(Context context, final int offset, final String text) throws Exception {
        final Document doc = getDocument(context);
        final Document expectedDoc = getExpectedDocument(context);
        // Possibly do logging
        if (context.isLogOp()) {
            int beforeTextStartOffset = Math.max(offset - 5, 0);
            String beforeText = expectedDoc.getText(beforeTextStartOffset, offset - beforeTextStartOffset);
            int afterTextEndOffset = Math.min(offset + 5, expectedDoc.getLength());
            String afterText = expectedDoc.getText(offset, afterTextEndOffset - offset);
            StringBuilder sb = context.logOpBuilder();
            sb.append("INSERT(").append(offset);
            sb.append(", ").append(text.length()).append("): \"");
            CharSequenceUtilities.debugText(sb, text);
            sb.append("\" text-around: \"");
            CharSequenceUtilities.debugText(sb, beforeText);
            sb.append('|');
            CharSequenceUtilities.debugText(sb, afterText);
            sb.append("\"\n");
            context.logOp(sb);
        }
        if (isLogDoc(context)) {
            StringBuilder sb = context.logOpBuilder();
            sb.append("  before-INSERT-docText: \"");
            CharSequenceUtilities.debugText(sb, expectedDoc.getText(0, expectedDoc.getLength()));
            sb.append("\"\n");
            context.logOp(sb);
        }

        expectedDoc.insertString(offset, text, null);
        doc.insertString(offset, text, null);
    }

    public static void remove(Context context, final int offset, final int length) throws Exception {
        final Document expectedDoc = getExpectedDocument(context);
        final Document doc = getDocument(context);
        // Possibly do logging
        if (context.isLogOp()) {
            StringBuilder sb = context.logOpBuilder();
            sb.append("REMOVE(").append(offset).append(", ").append(length).append(")").append('\n');
            context.logOp(sb);
        }
        if (isLogDoc(context)) {
            StringBuilder sb = context.logOpBuilder();
            sb.append("  before-REMOVE-docText: \"");
            CharSequenceUtilities.debugText(sb, expectedDoc.getText(0, expectedDoc.getLength()));
            sb.append("\"\n");
            context.logOp(sb);
        }

        expectedDoc.remove(offset, length);
        doc.remove(offset, length);
    }

    public static void undo(Context context, final int count) throws Exception {
        final UndoManager undoManager = getValidUndoManager(context);
        logUndoRedoOp(context, "UNDO", count);
        int cnt = count;
        while (undoManager.canUndo() && --cnt >= 0) {
            undoManager.undo();
            checkContent(context);
        }
        logPostUndoRedoOp(context, cnt);
    }

    public static void redo(Context context, final int count) throws Exception {
        final UndoManager undoManager = getValidUndoManager(context);
        logUndoRedoOp(context, "REDO", count);
        int cnt = count;
        while (undoManager.canRedo() && --cnt >= 0) {
            undoManager.redo();
            checkContent(context);
        }
        logPostUndoRedoOp(context, cnt);
    }
    
    public static Position createPosition(Context context, int offset) throws Exception {
        return createPosition(context, offset, false);
    }

    public static Position createPosition(Context context, int offset, boolean backwardBias) throws Exception {
        final Document expectedDoc = getExpectedDocument(context);
        final Document doc = getDocument(context);
        if (context.isLogOp()) {
            StringBuilder sb = context.logOpBuilder();
            sb.append(backwardBias ? "CREATE_BACKWARD_BIAS_POSITION(" : "CREATE_POSITION(");
            sb.append(offset).append("): \n");
            context.logOp(sb);
        }
        Position testPos = (expectedDoc instanceof ExpectedDocument)
                ? ((ExpectedDocument)expectedDoc).createSyncedTestPosition(offset, backwardBias)
                : (backwardBias
                        ? ((TestEditorDocument)doc).createBackwardBiasPosition(offset)
                        :doc.createPosition(offset));
        // Since EditorDocumentContent implements position sharing the test should obey
        // and if shared position is returned it should only check that the existing position has the right offset.
        return testPos;
    }

    public static void releasePosition(Context context, Position pos) throws Exception {
        final Document expectedDoc = getExpectedDocument(context);
        if (context.isLogOp()) {
            StringBuilder sb = context.logOpBuilder();
            sb.append("RELEASE_POSITION(").append(pos).append(")");
            sb.append("\n");
            context.logOp(sb);
        }
        if (expectedDoc instanceof ExpectedDocument) {
            ((ExpectedDocument)expectedDoc).releaseSyncedTestPosition(pos);
        }
    }
    
    private static void logUndoRedoOp(Context context, String opType, int count) {
        if (context.isLogOp()) {
            StringBuilder sb = context.logOpBuilder();
            sb.append(opType);
            sb.append(' ').append(count).append(" times");
            sb.append('\n');
            context.logOp(sb);
        }
    }

    private static void logPostUndoRedoOp(Context context, int remainingCount) {
        if (remainingCount > 0 && context.isLogOp()) {
            StringBuilder sb = new StringBuilder(100);
            sb.append(remainingCount).append(" unfinished");
            sb.append('\n');
            context.logOp(sb);
        }
    }

    static void checkContent(final Context context) throws Exception {
        final Document doc = getDocument(context);
        final Document expectedDoc = getExpectedDocument(context);
        doc.render(new Runnable() {
            @Override
            public void run() {
                expectedDoc.render(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            checkContentImpl(context);
                        } catch (Exception ex) {
                            throw new IllegalStateException(ex);
                        }
                    }
                });
            }
        });
    }

    static void checkContentImpl(Context context) throws Exception {
        final Document doc = getDocument(context);
        final Document expectedDoc = getExpectedDocument(context);
        int docLen = doc.getLength();
        assert (docLen == expectedDoc.getLength()) :
                "docLen=" + doc.getLength() + " != expectedDocLen=" + expectedDoc.getLength();
        String expectedDocText = expectedDoc.getText(0, docLen + 1);
        String docText = doc.getText(0, docLen + 1);
        if (!expectedDocText.equals(docText)) {
            assert false : "Document texts differ";
        }
        if (expectedDoc instanceof ExpectedDocument) {
            ((ExpectedDocument) expectedDoc).checkConsistency();
        }
        if (doc instanceof TestEditorDocument) {
            TestEditorDocument testDoc = (TestEditorDocument) doc;
            testDoc.getDocumentContent().checkConsistency();
        }
        
        Element eLineRoot = expectedDoc.getDefaultRootElement();
        Element lineRoot = doc.getDefaultRootElement();
        int eLineCount = eLineRoot.getElementCount();
        int lineCount = lineRoot.getElementCount();
        NbTestCase.assertEquals(eLineCount, lineCount);
        for (int i = 0; i < lineCount; i++) {
            Element eLine = eLineRoot.getElement(i);
            Element line = lineRoot.getElement(i);
            int eStartOffset = eLine.getStartOffset();
            int startOffset = line.getStartOffset();
            int eEndOffset = eLine.getEndOffset();
            int endOffset = line.getEndOffset();
            NbTestCase.assertEquals(eStartOffset, startOffset);
            NbTestCase.assertEquals(eEndOffset, endOffset);
        }
    }

    static final class InsertOp extends RandomTestContainer.Op {

        public InsertOp(String name) {
            super(name);
        }

        @Override
        protected void run(Context context) throws Exception {
            Document expectedDoc = getExpectedDocument(context);
            Random random = context.container().random();
            int offset = random.nextInt(expectedDoc.getLength() + 1);
            RandomText randomText = context.getInstance(RandomText.class);
            String text;
            if (INSERT_CHAR.equals(name())) {
                text = randomText.randomText(random, 1);
            } else if (INSERT_TEXT.equals(name())) {
                Integer maxLength = (Integer) context.getPropertyOrNull(INSERT_TEXT_MAX_LENGTH);
                if (maxLength == null)
                    maxLength = Integer.valueOf(10);
                int textLength = random.nextInt(maxLength) + 1;
                text = randomText.randomText(random, textLength);
            } else if (INSERT_PHRASE.equals(name())) {
                text = randomText.randomPhrase(random);
            } else {
                throw new IllegalStateException("Unexpected op name=" + name());
            }
            insert(context, offset, text);
        }

    }

    static final class RemoveOp extends RandomTestContainer.Op {

        public RemoveOp(String name) {
            super(name);
        }

        @Override
        protected void run(Context context) throws Exception {
            Document expectedDoc = getExpectedDocument(context);
            int docLen = expectedDoc.getLength();
            if (docLen == 0)
                return; // Nothing to possibly remove
            Random random = context.container().random();
            int length;
            if (REMOVE_CHAR.equals(name())) {
                length = 1;
            } else if (REMOVE_TEXT.equals(name())) {
                Integer maxLength = (Integer) context.getPropertyOrNull(REMOVE_TEXT_MAX_LENGTH);
                if (maxLength == null)
                    maxLength = Integer.valueOf(10);
                if (maxLength > docLen)
                    maxLength = Integer.valueOf(docLen);
                length = random.nextInt(maxLength) + 1;
            } else {
                throw new IllegalStateException("Unexpected op name=" + name());
            }
            int offset = random.nextInt(docLen - length + 1);
            remove(context, offset, length);
        }

    }

    static final class UndoRedoOp extends RandomTestContainer.Op {

        UndoRedoOp(String name) {
            super(name);
        }

        @Override
        protected void run(Context context) throws Exception {
            Integer maxCount = context.getProperty(MAX_UNDO_REDO_COUNT, Integer.valueOf(4));
            Float inverseRatio = context.getProperty(MAX_UNDO_REDO_COUNT, 0.3f);
            Random random = context.container().random();
            int count = random.nextInt(maxCount + 1);
            float performInverse = random.nextFloat();
            int inverseCount = (count > 0 && performInverse >= inverseRatio) ? random.nextInt(count + 1) : 0;
            if (UNDO.equals(name())) {
                undo(context, count);
                redo(context, inverseCount);
            } else if (REDO.equals(name())) {
                redo(context, count);
                undo(context, inverseCount);
            } else {
                throw new IllegalStateException("Unexpected op name=" + name());
            }
        }

    }

    static final class PositionOp extends RandomTestContainer.Op {

        PositionOp(String name) {
            super(name);
        }

        @Override
        protected void run(Context context) throws Exception {
            Document expectedDoc = getExpectedDocument(context);
            Random random = context.container().random();
            Integer maxCount = context.getProperty(MAX_CREATE_RELEASE_POSITION_COUNT, Integer.valueOf(4));
            int count = random.nextInt(maxCount + 1);
            while (--count >= 0) {
                int offset = random.nextInt(expectedDoc.getLength() + 2);
                if (CREATE_POSITION.equals(name())) {
                    createPosition(context, offset, false);
                } else if (CREATE_BACKWARD_BIAS_POSITION.equals(name())) {
                    createPosition(context, offset, true);
                } else if (RELEASE_POSITION.equals(name())) {
                    if (expectedDoc instanceof ExpectedDocument) {
                        ExpectedDocument expDoc = (ExpectedDocument) expectedDoc;
                        int syncPositionCount = expDoc.syncPositionCount();
                        if (syncPositionCount > 0) {
                            int index = random.nextInt(syncPositionCount);
                            if (context.isLogOp()) {
                                StringBuilder sb = context.logOpBuilder();
                                sb.append("RELEASE_POSITION[").append(index).append("])");
                                sb.append("\n");
                                context.logOp(sb);
                            }
                            expDoc.releaseSyncedPosition(index);
                        }
                    }
                } else {
                    throw new IllegalStateException("Unexpected op name=" + name());
                }
            }
        }

    }

    static final class UndoMgr extends UndoManager {
        
        private RandomTestContainer container;
        
        private UndoEditPair undoEditPair;

        public UndoMgr(RandomTestContainer container) {
            this.container = container;
        }

        @Override
        public void undoableEditHappened(UndoableEditEvent e) {
            if (e.getSource() == getValidExpectedDocument(container)) {
                assert undoEditPair == null;
                undoEditPair = new UndoEditPair();
                undoEditPair.expectedUndoEdit = e.getEdit();
            } else if (e.getSource() == getValidDocument(container)) {
                assert (undoEditPair != null);
                undoEditPair.testUndoEdit = e.getEdit();
                addEdit(undoEditPair);
                undoEditPair = null;
            } else {
                throw new IllegalStateException("Unexpected source of undo event: " + e);
            }
        }

    }

    static final class UndoEditPair extends AbstractUndoableEdit {
        
        UndoableEdit expectedUndoEdit;
        
        UndoableEdit testUndoEdit;

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            expectedUndoEdit.undo();
            testUndoEdit.undo();
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            expectedUndoEdit.redo();
            testUndoEdit.redo();
        }

    }

    static final class DocumentContentCheck extends RandomTestContainer.Check {

        @Override
        protected void check(Context context) throws Exception {
            checkContent(context);
        }

    }

}
