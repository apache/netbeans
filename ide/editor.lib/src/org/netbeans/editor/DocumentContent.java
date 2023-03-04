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

package org.netbeans.editor;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.text.Segment;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.AbstractDocument;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.UndoableEdit;
import org.netbeans.lib.editor.util.AbstractCharSequence;
import org.netbeans.modules.editor.lib.impl.BasePosition;
import org.netbeans.modules.editor.lib.impl.MarkVector;
import org.netbeans.modules.editor.lib.impl.MultiMark;

/**
 * Content of the document.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

final class DocumentContent implements AbstractDocument.Content, CharSeq, GapStart {
    
    private static final char[] EMPTY_CHAR_ARRAY = new char[0];

    /**
     * Invalid undoable edit being used to mark that the line undo was already
     * processed. It must never be undone/redone as it's used in a flyweight
     * way but the undomanager's operation changes states of undoable edits
     * being undone/redone.
     */
    private static final UndoableEdit INVALID_EDIT = new AbstractUndoableEdit();
    
    private static final boolean debugUndo
            = Boolean.getBoolean("netbeans.debug.editor.document.undo");
        
    /** Vector holding the marks for the document */
    private final MarkVector markVector;
    
    /** Array with gap holding the text of the document */
    private char[] charArray;

    /** Start index of the gap */
    private int gapStart;

    /** Length of the gap */
    private int gapLength;
    
    private boolean conservativeReallocation;
    
    DocumentContent() {
        charArray = EMPTY_CHAR_ARRAY;
        markVector = new MarkVector();
        
        // Insert implied '\n'
        insertText(0, "\n"); // NOI18N
    }
    
    public final int getGapStart() { // to implement GapStart
        return gapStart;
    }

    public UndoableEdit insertString(int offset, String text)
    throws BadLocationException {

        checkBounds(offset, 0, length() - 1);
        return new Edit(offset, text);
    }
    
    public UndoableEdit remove(int offset, int length)
    throws BadLocationException {

        checkBounds(offset, length, length() - 1);
        return new Edit(offset, length);
    }

    public Position createPosition(int offset) throws BadLocationException {
        checkOffset(offset);
        BasePosition pos = new BasePosition();
//        registerStack(pos);
        markVector.insert(markVector.createMark(pos, offset));
        return pos;
    }

    public Position createBiasPosition(int offset, Position.Bias bias)
    throws BadLocationException {
        checkOffset(offset);
        BasePosition pos = new BasePosition();
//        registerStack(pos);
        markVector.insert(markVector.createBiasMark(pos, offset, bias));
        return pos;
    }

    MultiMark createBiasMark(int offset, Position.Bias bias) throws BadLocationException {
        checkOffset(offset);
        return markVector.insert(markVector.createBiasMark(offset, bias));
    }

    MultiMark createMark(int offset) throws BadLocationException {
        checkOffset(offset);
        return markVector.insert(markVector.createMark(offset));
    }

//    private Map<List<StackTraceElement>, int[]> stack2Count;
//    private int dumpIndex;
//    private void registerStack(BasePosition pos) {
//        if (stack2Count == null) {
//            stack2Count = new HashMap<List<StackTraceElement>, int[]>();
//        }
//        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
//        List<StackTraceElement> stack = Arrays.asList(stackTrace).subList(3, Math.min(stackTrace.length, 13));
//        int[] count = stack2Count.get(stack);
//        if (count == null) {
//            // count[0] is total count allocated
//            // count[1] is last dumpIndex when active marks with that stack were computed
//            // count[2] total count of active marks for dumpIndex so far
//            // if count[1] != dumpIndex the count[2] is reset to zero
//            count = new int[3];
//            stack2Count.put(stack, count);
//        }
//        count[0]++;
//        pos.allocStack = stack;
//
//        if (count[0] % 300 == 0) {
//            System.gc();
//            // Dump the map
//            dumpIndex++;
//            // Count all active marks
//            synchronized (markVector) {
//                int markCount = markVector.getMarkCount();
//                for (int i = 0; i < markCount; i++) {
//                    MultiMark mark = markVector.getMark(i);
//                    if (mark != null) {
//                        BasePosition pos2 = mark.get();
//                        if (pos2 != null) {
//                            int[] count2 = stack2Count.get(pos2.allocStack);
//                            if (count2[1] != dumpIndex) {
//                                count2[1] = dumpIndex;
//                                count2[2] = 0; // Reset counting for different dumpIndex
//                            }
//                            count2[2]++;
//                        }
//                    }
//                }
//            }
//            Set<Map.Entry<List<StackTraceElement>,int[]>> sortedStacks
//                    = new TreeSet<Map.Entry<List<StackTraceElement>,int[]>>(
//                        new Comparator<Map.Entry<List<StackTraceElement>,int[]>>() {
//                            @Override
//                            public int compare(Entry<List<StackTraceElement>, int[]> e1, Entry<List<StackTraceElement>, int[]> e2) {
//                                // Sort from highest current number of marks
//                                return e2.getValue()[2] - e1.getValue()[2];
//                            }
//                        }
//            );
//            sortedStacks.addAll(stack2Count.entrySet());
//
//            StringBuilder sb = new StringBuilder(1000);
//            int i = 0;
//            for (Map.Entry<List<StackTraceElement>,int[]> entry : sortedStacks) {
//                int[] entryCount = entry.getValue();
//                if (entryCount[1] != dumpIndex) {
//                    entryCount[1] = dumpIndex;
//                    entryCount[2] = 0;
//                }
//                sb.append("STACK for " + entryCount[2] + " positions (" + entryCount[0] + " totally allocated):\n");
//                List<StackTraceElement> elems = entry.getKey();
//                for (StackTraceElement e : elems) {
//                    sb.append("    ").append(e).append('\n');
//                }
//                if (++i > 8) { // Only output the most used stacktraces
//                    break;
//                }
//            }
//            sb.append(toString()).append(", stacks: ").append(stack2Count.size());
//            System.err.println(sb);
//        }
//    }

    public int length() {
        return charArray.length - gapLength;
    }
    
    public void getChars(int offset, int length, Segment chars)
    throws BadLocationException {

        checkBounds(offset, length, length());
        
        if ((offset + length) <= gapStart) { // completely below gap
            chars.array = charArray;
            chars.offset = offset;
            
        } else if (offset >= gapStart) { // completely above gap
            chars.array = charArray;
            chars.offset = offset + gapLength;
            
        } else { // spans the gap, must copy
            chars.array = copySpanChars(offset, length);
            chars.offset = 0;
        }
        
        chars.count = length;
    }

    public String getString(int offset, int length)
    throws BadLocationException {

        checkBounds(offset, length, length());
        return getText(offset, length);
    }
    
    String getText(int offset, int length) {
        if (offset < 0 || length < 0) {
            throw new IllegalStateException("offset=" + offset + ", length=" + length); // NOI18N
        }

        String ret;
        if ((offset + length) <= gapStart) { // completely below gap
            ret = new String(charArray, offset, length);
            
        } else if (offset >= gapStart) { // completely above gap
            ret = new String(charArray, offset + gapLength, length);
            
        } else { // spans the gap, must copy
            ret = new String(copySpanChars(offset, length));
        }
        
        return ret;
    }

    public char charAt(int index) {
        return charArray[getRawIndex(index)];
    }
    
    public CharSequence createCharSequenceView() {
        return new CharSequenceImpl();
    }
    
    void compact() {
        if (gapLength > 0) {
            int newLength = charArray.length - gapLength;
            char[] newCharArray = new char[newLength];
            int gapEnd = gapStart + gapLength;
            System.arraycopy(charArray, 0, newCharArray, 0, gapStart);
            System.arraycopy(charArray, gapEnd, newCharArray, gapStart, 
                charArray.length - gapEnd);
            charArray = newCharArray;
            gapStart = charArray.length;
            gapLength = 0;
        }
        
        markVector.compact();
    }
    
    boolean isConservativeReallocation() {
        return conservativeReallocation;
    }
    
    void setConservativeReallocation(boolean conservativeReallocation) {
        this.conservativeReallocation = conservativeReallocation;
    }
    
    private int getRawIndex(int index) {
        return (index < gapStart) ? index : (index + gapLength);
    }
    
    private void moveGap(int index) {
        if (index <= gapStart) { // move gap down
            int moveSize = gapStart - index;
            System.arraycopy(charArray, index, charArray,
                gapStart + gapLength - moveSize, moveSize);
            gapStart = index;

        } else { // above gap
            int gapEnd = gapStart + gapLength;
            int moveSize = index - gapStart;
            System.arraycopy(charArray, gapEnd, charArray, gapStart, moveSize);
            gapStart += moveSize;
        }
    }
    
    private void enlargeGap(int extraLength) {
        int newLength; // means expansion length first
        if (conservativeReallocation)
            newLength = Math.min(4096, charArray.length / 10);
        else
            newLength = charArray.length;
        newLength = Math.max(10, charArray.length + newLength + extraLength);
        int gapEnd = gapStart + gapLength;
        int afterGapLength = (charArray.length - gapEnd);
        int newGapEnd = newLength - afterGapLength;
        char[] newCharArray = new char[newLength];
        System.arraycopy(charArray, 0, newCharArray, 0, gapStart);
        System.arraycopy(charArray, gapEnd, newCharArray, newGapEnd, afterGapLength);
        charArray = newCharArray;
        gapLength = newGapEnd - gapStart;
    }

    private char[] copyChars(int offset, int length) {
        char[] ret;
        if ((offset + length) <= gapStart) { // completely below gap
            ret = new char[length];
            System.arraycopy(charArray, offset, ret, 0, length);
            
        } else if (offset >= gapStart) { // completely above gap
            ret = new char[length];
            System.arraycopy(charArray, offset + gapLength, ret, 0, length);
            
        } else { // spans the gap, must copy
            ret = copySpanChars(offset, length);
        }
        
        return ret;
    }

    private char[] copySpanChars(int offset, int length) {
        char[] ret = new char[length];
        int belowGap = gapStart - offset;
        System.arraycopy(charArray, offset, ret, 0, belowGap);
        System.arraycopy(charArray, gapStart + gapLength,
            ret, belowGap, length - belowGap);
        return ret;
    }

    void insertText(int offset, String text) {
        ///*DEBUG*/System.err.println("DocumentContent.insertText(" + offset + ", \"" + text + "\")");
        int textLength = text.length();
        int extraLength = textLength - gapLength;
        if (extraLength > 0) {
            enlargeGap(extraLength);
        }
        if (offset != gapStart) {
            moveGap(offset);
        }
        text.getChars(0, textLength, charArray, gapStart);
        gapStart += textLength;
        gapLength -= textLength;
    }
    
    void removeText(int offset, int length) {
        ///*DEBUG*/System.err.println("DocumentContent.removeText(" + offset + ", " + length + ")");
        if (offset >= gapStart) { // completely over gap
            if (offset > gapStart) {
                moveGap(offset);
            }

        } else { // completely below gap or spans the gap
            int endOffset = offset + length;
            if (endOffset <= gapStart) {
                if (endOffset < gapStart) {
                    moveGap(endOffset);
                }
                gapStart -= length;
                
            } else { // spans gap
                gapStart = offset;
            }
        }

        gapLength += length;
    }
    
    private void checkOffset(int offset) throws BadLocationException {
        if (offset > length()) { // can be doc.getLength() + 1 i.e. getEndPosition()
            throw new BadLocationException("Invalid offset=" + offset // I18N // NOI18N
                + ", docLength=" + (length() - 1), offset); // I18N // NOI18N
        }
    }

    private void checkBounds(int offset, int length, int limitOffset)
    throws BadLocationException {

	if (offset < 0) {
	    throw new BadLocationException("Invalid offset=" + offset, offset); // NOI18N
	}
        if (length < 0) {
            throw new BadLocationException("Invalid length" + length, length); // NOI18N
        }
	if (offset + length > limitOffset) {
	    throw new BadLocationException(
                "docLength=" + (length() - 1) // NOI18N
                + ":  Invalid offset" // NOI18N
                + ((length != 0) ? "+length" : "") // NOI18N
                + "=" + (offset + length), // NOI18N
                (offset + length)
            );
	}
    }

    @Override
    public String toString() {
        return "charArray.length=" + charArray.length + ", " + markVector.toString();
    }
    
    private final class CharSequenceImpl extends AbstractCharSequence.StringLike {

        public char charAt(int index) {
            return DocumentContent.this.charAt(index);
        }

        public int length() {
            // this is slightly different from AbstractDocument.getText(), which does not include ending '\n'
            // see #159502; in general various highliging code needs accessing the artifical
            // '\n' at the end of a document, because it is the only way how to define
            // line highlights (ie. highligh that expands beyond EOL) for the last line in the document.
            return DocumentContent.this.length();
        }

        @Override
        public String toString() {
            return DocumentContent.this.getText(0, length());
        }

    } // End of CharSequenceImpl class

    class Edit extends AbstractUndoableEdit {
        
        /** Constructor used for insert.
         * @param offset offset of insert.
         * @param text inserted text.
         */
        Edit(int offset, String text) {
            this.offset = offset;
            this.length = text.length();
            this.text = text;

            undoOrRedo(length, false); // pretend redo
            
        }
        
        /** Constructor used for remove.
         * @param offset offset of remove.
         * @param length length of the removed text.
         */
        Edit(int offset, int length) {
            this.offset = offset;
            this.length = -length;
            
            // Added to make sure the text is not inited later at unappropriate time
            this.text = getText(offset, length);

            undoOrRedo(-length, false); // pretend redo
            
        }
        
        private int offset;
        
        private int length;
        
        private String text;
        
        private MarkVector.Undo markVectorUndo;
        
        public @Override void undo() throws CannotUndoException {
            super.undo();

            if (debugUndo) {
                /*DEBUG*/System.err.println("UNDO-" + dump()); // NOI18N
            }
            undoOrRedo(-length, true);
        }
        
        public @Override void redo() throws CannotRedoException {
            super.redo();

            if (debugUndo) {
                /*DEBUG*/System.err.println("REDO-" + dump()); // NOI18N
            }
            undoOrRedo(length, false);
        }
        
        private String dump() {
            return ((length >= 0) ? "INSERT" : "REMOVE") // NOI18N
                + ":offset=" + offset + ", length=" + length // NOI18N
                + ", text='" + text + '\''; // NOI18N
        }
        
        private void undoOrRedo(int len, boolean undo) {
            // Fix text content
            if (len < 0) { // do remove
                removeText(offset, -len);
            } else { // do insert
                insertText(offset, text);
            }
            
            // Update marks
            markVectorUndo = markVector.update(offset, len, markVectorUndo);
        }

        /**
         * @return text of the modification.
         */
        final String getUndoRedoText() {
            return text;
        }
        
    }
}
