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

package org.netbeans.lib.editor.util.random;

import java.lang.reflect.InvocationTargetException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.random.RandomTestContainer.Context;

public class DocumentTesting {

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

    /** Maximum number of chars inserted by INSERT_TEXT op. */
    public static final String INSERT_TEXT_MAX_LENGTH = "doc-insert-text-max-length";

    /** Maximum number of chars removed by REMOVE_TEXT op. */
    public static final String REMOVE_TEXT_MAX_LENGTH = "doc-remove-text-max-length";

    /** Maximum number of undo/redo (4 by default) to be performed at once. */
    public static final String UNDO_REDO_COUNT = "doc-undo-redo-count";

    /** Ratio (0.3 by default) with which there will be inverse operation
     * to just performed undo/redo.
     */
    public static final String UNDO_REDO_INVERSE_RATIO = "doc-undo-redo-inverse-ratio";
    
    /**
     * Whether invoke operations in same thread (necessary e.g. for runAtomic()) or in AWT true/false.
     */
    private static final String SAME_THREAD_INVOKE = "doc-same-thread-invoke"; // NOI18N

    /** java.lang.Boolean whether whole document text should be logged. */
    public static final String LOG_DOC = "doc-log-doc";

    public static RandomTestContainer initContainer(RandomTestContainer container) {
        if (container == null)
            container = new RandomTestContainer();
        Document doc = getDocument(container);
        if (doc == null) {
            doc = new TestDocument();
            container.putProperty(Document.class, doc);
        }

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
        container.addOp(new UndoRedo(UNDO));
        container.addOp(new UndoRedo(REDO));
        return container;
    }

    public static void initUndoManager(RandomTestContainer container) {
        Document doc = DocumentTesting.getDocument(container);
        UndoManager undoManager = (UndoManager) doc.getProperty(UndoManager.class);
        if (undoManager == null) {
            undoManager = new UndoManager();
            doc.addUndoableEditListener(undoManager);
            doc.putProperty(UndoManager.class, undoManager);
        }
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

    public static boolean isLogDoc(PropertyProvider provider) {
        return Boolean.TRUE.equals(provider.getPropertyOrNull(LOG_DOC));
    }

    public static void setLogDoc(PropertyProvider provider, boolean logDoc) {
        provider.putProperty(LOG_DOC, logDoc);
    }

    public static void insert(Context context, final int offset, final String text) throws Exception {
        final Document doc = getDocument(context);
        // Possibly do logging
        if (context.isLogOp()) {
            int beforeTextStartOffset = Math.max(offset - 5, 0);
            String beforeText = doc.getText(beforeTextStartOffset, offset - beforeTextStartOffset);
            int afterTextEndOffset = Math.min(offset + 5, doc.getLength());
            String afterText = doc.getText(offset, afterTextEndOffset - offset);
            StringBuilder sb = context.logOpBuilder();
            sb.append(" INSERT(").append(offset);
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
            StringBuilder sb = new StringBuilder(doc.getLength() + offset + 100);
            String beforeOffsetText = CharSequenceUtilities.debugText(doc.getText(0, offset));
            for (int i = 0; i < beforeOffsetText.length(); i++) {
                sb.append('-');
            }
            sb.append("\\ \"");
            CharSequenceUtilities.debugText(sb, text);
            sb.append("\"\n\"");
            sb.append(beforeOffsetText);
            CharSequenceUtilities.debugText(sb,
                    doc.getText(offset, doc.getLength() - offset));
            sb.append("\"\n");
            context.logOp(sb);
        }

        invoke(context, new Runnable() {
            @Override
            public void run() {
                try {
                    doc.insertString(offset, text, null);
                } catch (BadLocationException e) {
                    throw new IllegalStateException(e);
                }
            }
        });
    }

    public static void remove(Context context, final int offset, final int length) throws Exception {
        final Document doc = getDocument(context);
        // Possibly do logging
        if (context.isLogOp()) {
            StringBuilder sb = context.logOpBuilder();
            sb.append(" REMOVE(").append(offset).append(", ").append(length).append("): \"");
            CharSequenceUtilities.debugText(sb, doc.getText(offset, length));
            sb.append("\"\n");
            context.logOp(sb);
        }
        if (isLogDoc(context)) {
            StringBuilder sb = new StringBuilder(doc.getLength() + offset + 100);
            String beforeOffsetText = CharSequenceUtilities.debugText(doc.getText(0, offset));
            for (int i = 0; i <= beforeOffsetText.length(); i++) {
                sb.append('-');
            }
            for (int i = 0; i < length; i++) {
                sb.append('x');
            }
            sb.append("\n\"");
            sb.append(beforeOffsetText);
            CharSequenceUtilities.debugText(sb,
                    doc.getText(offset, doc.getLength() - offset));
            sb.append("\"\n");
            context.logOp(sb);
        }

        invoke(context, new Runnable() {
            @Override
            public void run() {
                try {
                    doc.remove(offset, length);
                } catch (BadLocationException e) {
                    throw new IllegalStateException(e);
                }
            }
        });
    }

    public static void undo(Context context, final int count) throws Exception {
        final Document doc = getDocument(context);
        final UndoManager undoManager = (UndoManager) doc.getProperty(UndoManager.class);
        logUndoRedoOp(context, "UNDO", count);
        invoke(context, new Runnable() {
            @Override
            public void run() {
                try {
                    int cnt = count;
                    while (undoManager.canUndo() && --cnt >= 0) {
                        undoManager.undo();
                    }
                } catch (CannotUndoException e) {
                    throw new IllegalStateException(e);
                }
            }
        });
        logPostUndoRedoOp(context, count);
    }

    public static void redo(Context context, final int count) throws Exception {
        final Document doc = getDocument(context);
        final UndoManager undoManager = (UndoManager) doc.getProperty(UndoManager.class);
        logUndoRedoOp(context, "REDO", count);
        invoke(context, new Runnable() {
            @Override
            public void run() {
                try {
                    int cnt = count;
                    while (undoManager.canRedo() && --cnt >= 0) {
                        undoManager.redo();
                    }
                } catch (CannotRedoException e) {
                    throw new IllegalStateException(e);
                }
            }
        });
        logPostUndoRedoOp(context, count);
    }
    
    public static void setSameThreadInvoke(Context context, boolean sameThreadInvoke) {
        context.putProperty(SAME_THREAD_INVOKE, sameThreadInvoke);
        if (sameThreadInvoke) { // Disable logging of non-EDT usage
            Logger.getLogger("org.netbeans.editor.BaseDocument.EDT").setLevel(Level.OFF);
            Logger.getLogger("org.netbeans.modules.editor.lib2.view.DocumentView.EDT").setLevel(Level.OFF);
        }
    }
    
    /**
     * Invoke the runnable in a right thread depending on a context property
     * @param r
     */
    private static void invoke(Context context, Runnable r) 
            throws InterruptedException, InvocationTargetException
    {
        boolean sameThreadInvoke = Boolean.TRUE.equals(context.getPropertyOrNull(SAME_THREAD_INVOKE));
        if (sameThreadInvoke) {
            r.run();
        } else {
            SwingUtilities.invokeAndWait(r);
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

    static final class InsertOp extends RandomTestContainer.Op {

        public InsertOp(String name) {
            super(name);
        }

        @Override
        protected void run(Context context) throws Exception {
            Document doc = getDocument(context);
            Random random = context.container().random();
            int offset = random.nextInt(doc.getLength() + 1);
            RandomText randomText = context.getInstance(RandomText.class);
            String text;
            if (INSERT_CHAR == name()) { // Just use ==
                text = randomText.randomText(random, 1);
            } else if (INSERT_TEXT == name()) { // Just use ==
                Integer maxLength = (Integer) context.getPropertyOrNull(INSERT_TEXT_MAX_LENGTH);
                if (maxLength == null)
                    maxLength = Integer.valueOf(10);
                int textLength = random.nextInt(maxLength) + 1;
                text = randomText.randomText(random, textLength);
            } else if (INSERT_PHRASE == name()) { // Just use ==
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
                Document doc = getDocument(context);
                int docLen = doc.getLength();
                if (docLen == 0)
                    return; // Nothing to possibly remove
                Random random = context.container().random();
                int length;
                if (REMOVE_CHAR == name()) { // Just use ==
                    length = 1;
                } else if (REMOVE_TEXT == name()) { // Just use ==
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

    static final class UndoRedo extends RandomTestContainer.Op {

        UndoRedo(String name) {
            super(name);
        }

        @Override
        protected void run(Context context) throws Exception {
            Integer maxCount = context.getProperty(UNDO_REDO_COUNT, Integer.valueOf(4));
            Random random = context.container().random();
            int count = random.nextInt(maxCount + 1);
            int inverseCount = (count > 0) ? random.nextInt(count + 1) : 0;
            if (UNDO == name()) { // Just use ==
                undo(context, count);
                redo(context, inverseCount);
            } else if (REDO == name()) { // Just use ==
                redo(context, count);
                undo(context, inverseCount);
            } else {
                throw new IllegalStateException("Unexpected op name=" + name());
            }
        }

    }

}
