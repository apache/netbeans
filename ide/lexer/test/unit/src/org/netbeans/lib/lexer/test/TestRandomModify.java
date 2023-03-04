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

package org.netbeans.lib.lexer.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.util.CharSequenceUtilities;


/**
 * Test lexer implementation for correctness by doing random
 * inserts/removals of random characters.
 *
 * @author mmetelka
 */
public class TestRandomModify {
    
    private boolean debugOperation;

    private boolean debugHierarchy;

    private boolean debugDocumentText;
    
    private boolean skipLexerConsistencyCheck;
    
    private Random random;
    
    private Document doc;
    
    private int opId;
    
    private int startDebugOpCount;
    
    private int maxDocLength;
    
    private List<SnapshotDescription> snapshots = new ArrayList<SnapshotDescription>();

    public TestRandomModify() {
        this(0);
    }
    
    public TestRandomModify(long seed) {
        this.doc = new javax.swing.text.PlainDocument();

        this.random = new Random();
        if (seed == 0) { // Use currentTimeMillis() (btw nanoTime() in 1.5 instead)
            seed = System.currentTimeMillis();
        }
        System.err.println("TestRandomModify with SEED=" + seed + "L");
        random.setSeed(seed);
    }

    private boolean isOpDebug() {
        return opId >= startDebugOpCount;
    }

    public boolean isDebugOperation() {
        return debugOperation && isOpDebug();
    }
    
    /**
     * Set whether info about each operation being performed in token hierarchy
     * should be dumped to system err after each operation.
     */
    public void setDebugOperation(boolean debugOperation) {
        this.debugOperation = debugOperation;
    }

    public boolean isDebugHierarchy() {
        return debugHierarchy && isOpDebug();
    }
    
    /**
     * Set whether complete text of the modified document
     * should be dumped to system err after each operation.
     */
    public void setDebugHierarchy(boolean debugHierarchy) {
        this.debugHierarchy = debugHierarchy;
    }

    public boolean isDebugDocumentText() {
        return debugDocumentText && isOpDebug();
    }
    
    /**
     * Set whether contents of the token hierarchy being tested
     * should be dumped to system err after each operation.
     */
    public void setDebugDocumentText(boolean debugDocumentText) {
        this.debugDocumentText = debugDocumentText;
    }
    
    public boolean isSkipLexerConsistencyCheck() {
        return skipLexerConsistencyCheck;
    }
    
    public void setSkipLexerConsistencyCheck(boolean skipLexerConsistencyCheck) {
        this.skipLexerConsistencyCheck = skipLexerConsistencyCheck;
    }

    public void test(RandomModifyDescriptor[] randomModifyDescriptors) throws Exception {
        for (int i = 0; i < randomModifyDescriptors.length; i++) {
            RandomModifyDescriptor descriptor = randomModifyDescriptors[i];
            int debugOpFragment = Math.max(descriptor.opCount() / 5, 100);
            int nextDebugOp = debugOpFragment - 1;
            for (int op = 0; op < descriptor.opCount(); op++) {
                opId++;
                double r = random().nextDouble() * descriptor.ratioSum();
                boolean success = false;
                try {
                    action(r, descriptor);
                    success = true;
                } finally {
                    if (!success) {
                        System.err.println("ACTION FAILED opId=" + opId);
                    }
                }
                if (op == nextDebugOp) {
                    nextDebugOp = Math.min(nextDebugOp + debugOpFragment, descriptor.opCount() - 1);
                    System.err.println(String.valueOf(op+1) + " of " + descriptor.opCount() + " operations finished.");
                }
            }
        }
        
        System.err.println("Maximum document length: " + maxDocLength());
    }
    
    protected double action(double r, RandomModifyDescriptor descriptor) throws Exception {
        if ((r -= descriptor.insertCharRatio()) < 0) {
            if (descriptor.randomTextProvider().randomCharAvailable()) {
                char ch = descriptor.randomTextProvider().randomChar(random());
                insertText(String.valueOf(ch));
            } else { // random char not available
                insertText(""); // possibly debug the operation
            }

        } else if ((r -= descriptor.insertTextRatio()) < 0) {
            String text = descriptor.randomTextProvider().randomText(random(),
                    descriptor.insertTextMaxLength());
            insertText(text);

        } else if ((r -= descriptor.insertFixedTextRatio()) < 0) {
            String fixedText = descriptor.randomTextProvider().randomFixedText(random());
            insertText(fixedText);

        } else if ((r -= descriptor.removeCharRatio()) < 0) {
            removeText(1);

        } else if ((r -= descriptor.removeTextRatio()) < 0) {
            int length = random().nextInt(descriptor.removeTextMaxLength());
            removeText(length);

//        } else if ((r -= descriptor.createSnapshotRatio()) < 0) {
//            createSnapshot();
//            
//        } else if ((r -= descriptor.destroySnapshotRatio()) < 0) {
//            destroySnapshot();
        }
        return r;
    }
    
    public void insertText(int offset, String text) throws Exception {
        if (text.length() > 0) {
            if (isDebugOperation()) {
                int beforeTextStartOffset = Math.max(offset - 5, 0);
                String beforeText = document().getText(beforeTextStartOffset, offset - beforeTextStartOffset);
                int afterTextEndOffset = Math.min(offset + 5, document().getLength());
                String afterText = doc.getText(offset, afterTextEndOffset - offset);
                System.err.println(opIdString() + " INSERT(" + offset +
                        ", " + text.length() +"): \"" +
                        CharSequenceUtilities.debugText(text) +"\" text-around: \"" +
                        CharSequenceUtilities.debugText(beforeText) + '|' + 
                        CharSequenceUtilities.debugText(afterText) + "\""
                );
                if (isDebugDocumentText()) {
                    StringBuilder sb = new StringBuilder();
                    String beforeOffsetText = CharSequenceUtilities.debugText(doc.getText(0, offset));
                    for (int i = 0; i < beforeOffsetText.length(); i++) {
                        sb.append('-');
                    }
                    sb.append("\\ \"");
                    CharSequenceUtilities.debugText(sb, text);
                    sb.append("\"\n\"");
                    sb.append(beforeOffsetText).append(CharSequenceUtilities.debugText(
                            doc.getText(offset, doc.getLength() - offset))).append('"');
                    System.err.println(sb.toString());
                }
            }
            document().insertString(offset, text, null);
            insertTextNotify(offset, text);
            maxDocLength = Math.max(document().getLength(), maxDocLength);
            checkConsistency();

        } else {
            if (isDebugOperation()) {
                System.err.println(opIdString() + " INSERT cannot be done (text=\"\")");
            }
        }
    }
    
    public void insertText(String text) throws Exception {
        int offset = random().nextInt(document().getLength() + 1);
        insertText(offset, text);
    }
    
    protected void insertTextNotify(int offset, String text) throws Exception {
    }

    public void removeText(int offset, int length) throws Exception {
        if (length > 0) {
            if (isDebugOperation()) {
                System.err.println(opIdString() + " REMOVE(" + offset
                        + ", " + length + "): \""
                        + CharSequenceUtilities.debugText(document().getText(offset, length))
                        + "\""
                );
            }
            if (isDebugDocumentText()) {
                StringBuilder sb = new StringBuilder();
                String beforeOffsetText = CharSequenceUtilities.debugText(doc.getText(0, offset));
                for (int i = 0; i <= beforeOffsetText.length(); i++) {
                    sb.append('-');
                }
                for (int i = 0; i < length; i++) {
                    sb.append('x');
                }
                sb.append("\n\"");
                sb.append(beforeOffsetText).append(CharSequenceUtilities.debugText(
                        doc.getText(offset, doc.getLength() - offset))).append('"');
                System.err.println(sb.toString());
            }
            document().remove(offset, length);
            removeTextNotify(offset, length);
            checkConsistency();

        } else { // No operation
            if (isDebugOperation()) {
                System.err.println(opIdString() + " REMOVE cannot be done (length=0)");
            }
        }
    }
    
    public void removeText(int length) throws Exception {
        length = Math.min(document().getLength(), length);
        int offset = random().nextInt(document().getLength() - length + 1);
        removeText(offset, length);
    }
    
    protected void removeTextNotify(int offset, int length) throws Exception {
    }
    
//    public void createSnapshot() throws Exception {
//        junit.framework.TestCase.fail();
//        TokenHierarchy hi = TokenHierarchy.get(doc);
//        TokenHierarchy snapshot = hi.createSnapshot();
//        Language<?> language = (Language<?>)
//                doc.getProperty(Language.class);
//        TokenHierarchy batchMirror = TokenHierarchy.create(doc.getText(0, doc.getLength()), language);
//        snapshots.add(new SnapshotDescription(snapshot, batchMirror));
//        if (isDebugOperation()) {
//            System.err.println(opIdString() + " CREATED SNAPSHOT. "
//                    + snapshots.size() + " snapshots.");
//        }
//        checkConsistency();
//    }
//    
//    public void destroySnapshot() throws Exception {
//        junit.framework.TestCase.fail();
//        if (snapshots.size() > 0) {
//            int index = random().nextInt(snapshots.size());
//            snapshots.remove(index);
//            if (isDebugOperation()) {
//                System.err.println(opIdString() + " DESTROYED SNAPSHOT. "
//                        + snapshots.size() + " snapshots.");
//            }
//            checkConsistency();
//
//        } else { // no snapshots
//            if (isDebugOperation()) {
//                System.err.println(opIdString() + " DESTROY SNAPSHOT cannot be done - no snapshots.");
//            }
//        }
//    }

    public final int opId() {
        return opId;
    }
    
    public final String opIdString() {
        String s = String.valueOf(opId());
        while (s.length() < 3) {
            s = " " + s;
        }
        return "OPER[" + s + "]";
    }

    public int startDebugOpCount() {
        return startDebugOpCount;
    }
        
    public void setStartDebugOpCount(int startDebugOpCount) {
        this.startDebugOpCount = startDebugOpCount;
    }
        
    public final Random random() {
        return random;
    }
    
    public final Document document() {
        return doc;
    }
    
    public void clearDocument() throws Exception {
        doc.remove(0, doc.getLength());
        // Verify that there are no tokens
        LexerTestUtilities.incCheck(doc, true);
    }
    
    public final Language<?> language() {
        return (Language<?>)doc.getProperty(Language.class);
    }
    
    public final void setLanguage(Language<?> language) {
        doc.putProperty(Language.class, language);
    }
    
    public final int maxDocLength() {
        return maxDocLength;
    }

    protected void checkConsistency() throws Exception {
        if (!isSkipLexerConsistencyCheck()) {
            doc.render(new Runnable() {
                @Override
                public void run() {
                    LexerTestUtilities.incCheck(doc, true);

                    // Possibly debug the hierarchy - do it after incCheck() so that hierarchy is fully inited
                    if (isDebugHierarchy()) {
                        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
                        if (hi != null) {
                            System.err.println("DEBUG hierarchy:\n" + hi + "\n");
                        }
                    }

                    for (int i = 0; i < snapshots.size(); i++) {
                        SnapshotDescription sd = snapshots.get(i);
                        TokenHierarchy<?> bm = sd.batchMirror();
                        TokenHierarchy<?> s = sd.snapshot();
                        if (isDebugOperation()) {
                            System.err.println("Comparing snapshot " + i + " of " + snapshots.size());
                        }
                        // Check snapshot without comparing lookaheads and states
                        LexerTestUtilities.assertTokenSequencesEqual(null, bm.tokenSequence(), bm,
                                s.tokenSequence(), s, false, false);
                    }
                }
            });
        }
    }
    
    private static final class SnapshotDescription {
        
        private final TokenHierarchy<?> snapshot;
        
        private final TokenHierarchy<?> batchMirror;
        
        public SnapshotDescription(TokenHierarchy snapshot, TokenHierarchy batchMirror) {
            this.snapshot = snapshot;
            this.batchMirror = batchMirror;
        }
        
        public TokenHierarchy snapshot() {
            return snapshot;
        }
        
        public TokenHierarchy batchMirror() {
            return batchMirror;
        }

    }

}
