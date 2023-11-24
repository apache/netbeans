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
package org.netbeans.modules.java.source.save;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeInfo;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.modules.java.source.transform.FieldGroupTree;
import static org.netbeans.api.java.lexer.JavaTokenId.*;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.source.save.CasualDiff.LineInsertionType;
import org.openide.util.Pair;

/**
 * Estimates the position for given element or element set. Offsets are
 * available from SourcePositions, but these are not precise enough for
 * generator. -- Generator has to handle comments and empty lines and
 * spaces too.
 *
 * @author Pavel Flaska
 */
public abstract class PositionEstimator {
    
    /**
     * For JCTree instances that have no associated source position.
     */
    public static final int NOPOS = -2;
    
    final List<? extends Tree> oldL;
    final List<? extends Tree> newL;
    final DiffContext diffContext;
    boolean initialized;
    final TokenSequence<JavaTokenId> seq;
    GuardedSectionManager guards;

    PositionEstimator(final List<? extends Tree> oldL, final List<? extends Tree> newL, final DiffContext diffContext) {
        this.oldL = oldL;
        this.newL = newL;
        this.diffContext = diffContext;
        this.seq = diffContext != null ? diffContext.tokenSequence : null;
        if (diffContext.doc instanceof StyledDocument) {
            this.guards = GuardedSectionManager.getInstance((StyledDocument) diffContext.doc);
        } else {
            this.guards = null;
        }
        initialized = false;
    }
        
    int[][] matrix;
    
    /**
     * Initialize data for provided lists.
     */
    protected abstract void initialize();

    /**
     * Computes the offset position when inserting to {@code index}.
     *
     * @param   index  represents order in list
     * @return  offset where to insert
     */
    public abstract int getInsertPos(int index);
    
    /**
     * Computes the start and end positions for element at {@code index}.
     * 
     * @param   index
     * @return  two integers containing start and end position
     * @throws  IndexOutOfBoundsException {@inheritDoc}
     */
    public abstract int[] getPositions(int index);

    /**
     * In case old list does not contain any element, try to estimate the
     * position where to start. User has to provide empty buffers to allow
     * to put some formatting stuff to head and tail of section.
     *
     * @param   startPos
     * @param   aHead     buffer where head formatting stuff will be added
     * @param   aTail     buffer where tail formatting stuff will be added
     * @return  position where to start
     */
    abstract int prepare(final int startPos, StringBuilder aHead, StringBuilder aTail);

    /**
     * Returns of whole section. Used, when all item in the list are removed,
     * e.g. when all imports are removed.
     * 
     * @param  replacement can contain the text which will replace the whole
     *         section
     * 
     * @return start offset and end offset of the section
     */
    public abstract int[] sectionRemovalBounds(StringBuilder replacement);
            
    /**
     * Return line insertion type for given estimator.
     */
    public LineInsertionType lineInsertType() {
        return LineInsertionType.NONE;
    }
    
    public abstract String head();
    public abstract String sep();
    public abstract String getIndentString();
    public String append(int index) {return "";}
    
    // remove the method after all calls will be refactored!
    public int[][] getMatrix() { 
        if (!initialized) initialize();
        return matrix; 
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // implementors
    static class ImplementsEstimator extends BaseEstimator {
        ImplementsEstimator(List<? extends Tree> oldL, 
                            List<? extends Tree> newL,
                            DiffContext diffContext)
        {
            super(IMPLEMENTS, oldL, newL, diffContext);
        }
    }
    
    static class ExtendsEstimator extends BaseEstimator {
        ExtendsEstimator(List<? extends Tree> oldL, 
                         List<? extends Tree> newL,
                         DiffContext diffContext)
        {
            super(EXTENDS, oldL, newL, diffContext);
        }
    }
    
    static class ThrowsEstimator extends BaseEstimator {
        ThrowsEstimator(List<? extends ExpressionTree> oldL, 
                        List<? extends ExpressionTree> newL,
                        DiffContext diffContext)
        {
            super(THROWS, oldL, newL, diffContext);
        }
    }
    
    static class CasePatternEstimator extends BaseEstimator {
        CasePatternEstimator(List<? extends Tree> oldL, 
                             List<? extends Tree> newL,
                             DiffContext diffContext)
        {
            super(CASE, oldL, newL, diffContext);
        }

        @Override
        public String head() {
            return precToken.fixedText() + " ";
        }
        
    }
    
    static class StringTemaplateEstimator extends BaseEstimator {
        StringTemaplateEstimator(List<? extends Tree> oldL, 
                             List<? extends Tree> newL,
                             DiffContext diffContext)
        {
            super(DOT, oldL, newL, diffContext);
        }

        @Override
        public String head() {
            return precToken.fixedText();
        }

        @Override
        public int getInsertPos(int index) {
            if (index == oldL.size()) {
                return diffContext.getEndPosition(diffContext.origUnit, (JCTree) oldL.get(index - 1));
            }
            return (int) diffContext.trees.getSourcePositions().getStartPosition(diffContext.origUnit, oldL.get(index));
        }

        @Override
        public int[] getPositions(int index) {
            int start = (int) diffContext.trees.getSourcePositions().getStartPosition(diffContext.origUnit, oldL.get(index));
            int end = diffContext.getEndPosition(diffContext.origUnit, (JCTree) oldL.get(index));

            return new int[] {start, end};
        }

    }

    static class ExportsOpensToEstimator extends BaseEstimator {
        
        ExportsOpensToEstimator(List<? extends ExpressionTree> oldL,
                           List<? extends ExpressionTree> newL,
                           DiffContext diffContext)
        {
            super(TO, oldL, newL, diffContext);
        }        
    }

    static class ProvidesWithEstimator extends BaseEstimator {
        
        ProvidesWithEstimator(List<? extends ExpressionTree> oldL,
                           List<? extends ExpressionTree> newL,
                           DiffContext diffContext)
        {
            super(WITH, oldL, newL, diffContext);
        }        
    }

    /**
     * Provides positions for imports section. Computes positions for exisiting
     * imports and suggest insert position for newly added/inserted import.
     */
    static class ImportsEstimator extends PositionEstimator {
        
        public ImportsEstimator(final List<? extends ImportTree> oldL, 
                                final List<? extends ImportTree> newL, 
                                final DiffContext diffContext)
        {
            super(oldL, newL, diffContext);
        }

        List<int[]> data;
        
        @Override()
        public void initialize() {
            int size = oldL.size();
            data = new ArrayList<int[]>(size);
            SourcePositions positions = diffContext.trees.getSourcePositions();
            CompilationUnitTree compilationUnit = diffContext.origUnit;
            
            for (Tree item : oldL) {
                int treeStart = (int) positions.getStartPosition(compilationUnit, item);
                int treeEnd = (int) positions.getEndPosition(compilationUnit, item);
                
                seq.move(treeStart);
                seq.moveNext();
                int wideStart = goAfterLastNewLine(seq);
                seq.move(treeStart);
                seq.moveNext();
                if (null != moveToSrcRelevant(seq, Direction.BACKWARD)) {
                    seq.moveNext();
                }
                int previousEnd = seq.offset();
                Token<JavaTokenId> token;
                while (nonRelevant.contains((token = seq.token()).id())) {
                    int localResult = -1;
                    switch (token.id()) {
                        case WHITESPACE:
                            int indexOf = token.text().toString().indexOf('\n');
                            if (indexOf > 0) {
                                localResult = seq.offset() + indexOf + 1;
                            }
                            break;
                        case LINE_COMMENT:
                            previousEnd = seq.offset() + token.text().length();
                            break;
                    }
                    if (localResult > 0) {
                        previousEnd = localResult;
                        break;
                    }
                    if (!seq.moveNext()) break;
                }
                seq.move(treeEnd);
                int wideEnd = treeEnd;
                while (seq.moveNext() && nonRelevant.contains((token = seq.token()).id())) {
                    if (JavaTokenId.WHITESPACE == token.id()) {
                        int indexOf = token.text().toString().indexOf('\n');
                        if (indexOf > -1) {
                            wideEnd = seq.offset() + indexOf + 1;
                            break;
                        } else {
                            wideEnd = seq.offset();
                        }
                    } else if (JavaTokenId.LINE_COMMENT == token.id()) {
                        wideEnd = seq.offset() + token.text().length();
                        break;
                    } else if (JavaTokenId.JAVADOC_COMMENT == token.id()) {
                        break;
                    }
                }
                if (wideEnd < treeEnd) wideEnd = treeEnd;
                data.add(new int[] { wideStart, wideEnd, previousEnd });
            }
            initialized = true;
        }

        @Override()
        public int getInsertPos(int index) {
            if (!initialized) initialize();
            if (data.isEmpty()) {
                return -1;
            } else {
                return index == data.size() ? data.get(index-1)[2] : data.get(index)[0];
            }
        }

        // when first element is inserted, analyse the spacing and
        // do decision about adding new lines.
        @Override()
        public int prepare(final int startPos, StringBuilder aHead, StringBuilder aTail) {
            if (!initialized) initialize();
            CompilationUnitTree cut = diffContext.origUnit;
            int resultPos = 0;
            if (cut.getTypeDecls().isEmpty()) {
                return diffContext.origText.length();
            } else {
                int tdpos = 0;
                SourcePositions positions = diffContext.trees.getSourcePositions();
                int typeDeclStart;
                do {
                    Tree t = cut.getTypeDecls().get(tdpos);
                    typeDeclStart = (int) positions.getStartPosition(cut, t);
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(t.getKind())) {
                        break;
                    }
                    tdpos++;
                    // as if no decls are available
                    typeDeclStart = diffContext.origText.length();
                } while (tdpos < cut.getTypeDecls().size());
                seq.move(typeDeclStart);
                if (null != moveToSrcRelevant(seq, Direction.BACKWARD)) {
                    resultPos = seq.offset() + seq.token().length();
                } else {
                    return 0;
                }
            }
            int counter = 0;
            while (seq.moveNext() && nonRelevant.contains(seq.token().id()) && counter < 3) {
                if (JavaTokenId.WHITESPACE == seq.token().id()) {
                    String white = seq.token().text().toString();
                    int index = 0, pos = 0;
                    while ((pos = white.indexOf('\n', pos)) > -1) {
                        ++counter;
                        ++pos;
                        if (counter < 3) {
                            index = pos;
                        }
                    }
                    resultPos += index;
                } else if (JavaTokenId.LINE_COMMENT == seq.token().id()) {
                    ++counter;
                    resultPos += seq.token().text().toString().length();
                } else if (JavaTokenId.BLOCK_COMMENT == seq.token().id() ||
                           JavaTokenId.JAVADOC_COMMENT == seq.token().id()) {
                    // do not continue when javadoc comment was found!
                    break;
                }
            }
            return resultPos;
        }
        
        @Override()
        public int[] getPositions(int index) {
            if (!initialized) initialize();
            return data.get(index).clone();
        }
        
        @Override
        public LineInsertionType lineInsertType() {
            return LineInsertionType.AFTER;
        }
        
        @Override()
        public String head() {
            throw new UnsupportedOperationException("Not applicable for imports!");
        }

        @Override()
        public String sep() { 
            throw new UnsupportedOperationException("Not applicable for imports!");
        }

        @Override()
        public String getIndentString() {
            throw new UnsupportedOperationException("Not applicable for imports!");
        }

        @Override
        public String toString() {
            String result = "";
            for (int i = 0; i < data.size(); i++) {
                int[] pos = data.get(i);
                String s = diffContext.origText.substring(pos[0], pos[1]);
                result += "\"" + s + "\"\n";
            }
            return result;
        }
    
        /**
         * Used when all elements from the list was removed.
         */
        public int[] sectionRemovalBounds(StringBuilder replacement) {
            // this part should be generalized
            assert !oldL.isEmpty() && newL.isEmpty(); // check the call correctness
            SourcePositions positions = diffContext.trees.getSourcePositions();
            CompilationUnitTree compilationUnit = diffContext.origUnit;
            int sectionStart = (int) positions.getStartPosition(compilationUnit, oldL.get(0));
            int sectionEnd = (int) positions.getEndPosition(compilationUnit, oldL.get(oldL.size()-1));
            // end of generalization part
            
            seq.move(sectionStart);
            seq.moveNext();
            Token<JavaTokenId> token;
            while (seq.movePrevious() && nonRelevant.contains((token = seq.token()).id())) {
                if (JavaTokenId.LINE_COMMENT == token.id()) {
                    seq.moveNext();
                    sectionStart = seq.offset();
                    break;
                } else if (JavaTokenId.BLOCK_COMMENT == token.id() || JavaTokenId.JAVADOC_COMMENT == token.id()) {
                    break;
                } else if (JavaTokenId.WHITESPACE == token.id()) {
                    int indexOf = token.text().toString().indexOf('\n');
                    if (indexOf > -1) {
                        sectionStart = seq.offset() + indexOf + 1;
                    } else {
                        sectionStart = seq.offset();
                    }
            }
            }
            seq.move(sectionEnd);
            seq.movePrevious();
            while (seq.moveNext() && nonRelevant.contains((token = seq.token()).id())) {
                if (JavaTokenId.LINE_COMMENT == token.id()) {
                    sectionEnd = seq.offset();
                    if (seq.moveNext()) {
                        sectionEnd = seq.offset();
                    }
                    break;
                } else if (JavaTokenId.BLOCK_COMMENT == token.id() || JavaTokenId.JAVADOC_COMMENT == token.id()) {
                    break;
                } else if (JavaTokenId.WHITESPACE == token.id()) {
                    int indexOf = token.text().toString().indexOf('\n');
                    if (indexOf > -1) {
                        sectionEnd = seq.offset() + indexOf + 1;
                    } else {
                        sectionEnd = seq.offset() + token.text().length();
                    }
                }
            }
            return new int[] { sectionStart, sectionEnd };
        }
    }
    
    static class CasesEstimator extends PositionEstimator {
        
        private List<int[]> data;
        
        public CasesEstimator(final List<? extends Tree> oldL, 
                                final List<? extends Tree> newL, 
                                final DiffContext diffContext)
        {
            super(oldL, newL, diffContext);
        }
        
        @Override()
        public void initialize() {
            int size = oldL.size();
            data = new ArrayList<int[]>(size);
            SourcePositions positions = diffContext.trees.getSourcePositions();
            CompilationUnitTree compilationUnit = diffContext.origUnit;
            
            for (Tree item : oldL) {
                int treeStart = (int) positions.getStartPosition(compilationUnit, item);
                int treeEnd = (int) positions.getEndPosition(compilationUnit, item);

                seq.move(treeStart);
                seq.moveNext();
                if (null != moveToSrcRelevant(seq, Direction.BACKWARD)) {
                    seq.moveNext();
                }
                int previousEnd = seq.offset();
                Token<JavaTokenId> token;
                while (nonRelevant.contains((token = seq.token()).id())) {
                    int localResult = -1;
                    switch (token.id()) {
                        case WHITESPACE:
                            int indexOf = token.text().toString().indexOf('\n');
                            if (indexOf > -1) {
                                localResult = seq.offset() + indexOf + 1;
                            } 
                            break;
                        case LINE_COMMENT:
                            previousEnd = seq.offset() + token.text().length();
                            break;
                    }
                    if (localResult >= 0) {
                        previousEnd = localResult;
                        break;
                    }
                    if (!seq.moveNext()) break;
                }
                seq.move(treeEnd);
                int wideEnd = treeEnd;
                while (seq.moveNext() && nonRelevant.contains((token = seq.token()).id())) {
                    if (JavaTokenId.WHITESPACE == token.id()) {
                        int indexOf = token.text().toString().indexOf('\n');
                        if (indexOf > -1) {
                            wideEnd = seq.offset() + indexOf + 1;
                        } else {
                            wideEnd = seq.offset();
                        }
                    } else if (JavaTokenId.LINE_COMMENT == token.id()) {
                        wideEnd = seq.offset() + token.text().length();
                        break;
                    } else if (JavaTokenId.JAVADOC_COMMENT == token.id()) {
                        break;
                    }
                    if (wideEnd > treeEnd)
                        break;
                }
                if (wideEnd < treeEnd) wideEnd = treeEnd;
                data.add(new int[] { previousEnd, wideEnd, previousEnd });
            }
            initialized = true;
        }
        
        @Override()
        public int getInsertPos(int index) {
            if (!initialized) initialize();
            if (data.isEmpty()) {
                return -1;
            } else {
                return index == data.size() ? data.get(index-1)[2] : data.get(index)[0];
            }
        }

        /**
         * Used when all elements from the list was removed.
         */
        public int[] sectionRemovalBounds(StringBuilder replacement) {
            if (!initialized) initialize();
            // this part should be generalized
            assert !oldL.isEmpty() && newL.isEmpty(); // check the call correctness
            SourcePositions positions = diffContext.trees.getSourcePositions();
            CompilationUnitTree compilationUnit = diffContext.origUnit;
            int sectionStart = (int) positions.getStartPosition(compilationUnit, oldL.get(0));
            int sectionEnd = (int) positions.getEndPosition(compilationUnit, oldL.get(oldL.size()-1));
            // end of generalization part
            
            seq.move(sectionStart);
            seq.moveNext();
            Token<JavaTokenId> token;
            while (seq.movePrevious() && nonRelevant.contains((token = seq.token()).id())) {
                if (JavaTokenId.LINE_COMMENT == token.id()) {
                    seq.moveNext();
                    sectionStart = seq.offset();
                    break;
                } else if (JavaTokenId.BLOCK_COMMENT == token.id() || JavaTokenId.JAVADOC_COMMENT == token.id()) {
                    break;
                } else if (JavaTokenId.WHITESPACE == token.id()) {
                    int indexOf = token.text().toString().indexOf('\n');
                    if (indexOf > -1) {
                        sectionStart = seq.offset() + indexOf + 1;
                    } else {
                        sectionStart = seq.offset();
                    }
                }
            }
            seq.move(sectionEnd);
            seq.movePrevious();
            while (seq.moveNext() && nonRelevant.contains((token = seq.token()).id())) {
                if (JavaTokenId.LINE_COMMENT == token.id()) {
                    sectionEnd = seq.offset();
                    if (seq.moveNext()) {
                        sectionEnd = seq.offset();
                    }
                    break;
                } else if (JavaTokenId.BLOCK_COMMENT == token.id() || JavaTokenId.JAVADOC_COMMENT == token.id()) {
                    break;
                } else if (JavaTokenId.WHITESPACE == token.id()) {
                    int indexOf = token.text().toString().lastIndexOf('\n');
                    if (indexOf > -1) {
                        sectionEnd = seq.offset() + indexOf + 1;
                    } else {
                        sectionEnd += seq.offset() + token.text().length();
                    }
                }
            }
            return new int[] { sectionStart, sectionEnd };
        }
        
        public String head() { return ""; }

        public String sep() { return ""; }

        public String getIndentString() { return ""; }
        
        @Override()
        public int[] getPositions(int index) {
            if (!initialized) initialize();
            return data.get(index).clone();
        }
        
        public int prepare(int startPos, StringBuilder aHead,
                           StringBuilder aTail) {
            seq.move(startPos);
            seq.moveNext();
            moveToSrcRelevant(seq, Direction.BACKWARD);
            while (seq.moveNext() && nonRelevant.contains(seq.token().id())) {
                if (JavaTokenId.WHITESPACE == seq.token().id()) {
                    int newlineInToken = seq.token().text().toString().indexOf('\n');
                    if (newlineInToken > -1) {
                        return seq.offset() + newlineInToken + 1;
                    }
                } else if (JavaTokenId.LINE_COMMENT == seq.token().id()) {
                    return seq.offset() + seq.token().text().length();
                }
            }
            return startPos;
        }
        
        @Override
        public String toString() {
            if (!initialized) initialize();
            String result = "";
            for (int i = 0; i < data.size(); i++) {
                int[] pos = data.get(i);
                String s = diffContext.origText.substring(pos[0], pos[1]);
                result += "[" + s + "]";
            }
            return result;
        }

        @Override
        public LineInsertionType lineInsertType() {
            return LineInsertionType.AFTER;
        }

    }
    
    /**
     * Provides position estimator for features in type declaration.
     */
    static class AnnotationsEstimator extends PositionEstimator {
        
        public AnnotationsEstimator(List<? extends Tree> oldL, 
                                 List<? extends Tree> newL,
                                 DiffContext diffContext)
        {
            super(oldL, newL, diffContext);
        }

        public void initialize() {
            int size = oldL.size();
            matrix = new int[size+1][5];
            matrix[size] = new int[] { -1, -1, -1, -1, -1 };
            SourcePositions positions = diffContext.trees.getSourcePositions();
            CompilationUnitTree compilationUnit = diffContext.origUnit;
            int i = 0;
            
            for (Tree item : oldL) {
                int treeStart = (int) positions.getStartPosition(compilationUnit, item);
                int treeEnd = (int) positions.getEndPosition(compilationUnit, item);
                // stupid hack, we have to remove syntetic constructors --
                // should be filtered before and shouldn't be part of this
                // collection (oldL)
                if (treeEnd < 0) continue;
                
                seq.move(treeStart);
                int startIndex = seq.index();
                // go back to opening/closing curly, semicolon or other
                // token java-compiler important token.
                moveToSrcRelevant(seq, Direction.BACKWARD);
                seq.moveNext();
                int veryBeg = seq.index();
                seq.move(treeEnd);
                matrix[i++] = new int[] { veryBeg, veryBeg, veryBeg, startIndex, seq.index() };
                if (i == size) {
                    seq.move(treeEnd);
                    matrix[i][2] = seq.index();
                }
            }
            initialized = true;
        }
        
        @Override()
        public int getInsertPos(int index) {
            if (!initialized) initialize();
            int tokenIndex = matrix[index][2];
            // cannot do any decision about the position - probably first
            // element is inserted, no information is available. Call has
            // to decide.
            if (tokenIndex == -1) return -1;
            seq.moveIndex(tokenIndex);
            seq.moveNext();
            int off = goAfterLastNewLine(seq);
            return off;
        }
        
        public String head() { return ""; }

        public String sep() { return ""; }

        public String getIndentString() { return ""; }
        
        public int[] getPositions(int index) {
            if (!initialized) initialize();
            int begin = getInsertPos(index);
            if (matrix[index][4] != -1) {
                seq.moveIndex(matrix[index][4]);
                seq.moveNext();
            }
            int end = goAfterFirstNewLine(seq);
            return new int [] { begin, end };
        }
        
        @Override
        public LineInsertionType lineInsertType() {
            return LineInsertionType.AFTER;
        }
        
        public int prepare(int startPos, StringBuilder aHead,
                           StringBuilder aTail) {
            return startPos;
        }
        
        public int[] sectionRemovalBounds(StringBuilder replacement) {
            if (!initialized) initialize();
            // this part should be generalized
            assert !oldL.isEmpty() && newL.isEmpty(); // check the call correctness
            SourcePositions positions = diffContext.trees.getSourcePositions();
            CompilationUnitTree compilationUnit = diffContext.origUnit;
            int sectionStart = (int) positions.getStartPosition(compilationUnit, oldL.get(0));
            int sectionEnd = (int) positions.getEndPosition(compilationUnit, oldL.get(oldL.size()-1));
            // end of generalization part
            
            seq.move(sectionStart);
            seq.moveNext();
            Token<JavaTokenId> token;
            int fullLineSectionStart = -1;
            while (seq.movePrevious() && nonRelevant.contains((token = seq.token()).id())) {
                if (JavaTokenId.LINE_COMMENT == token.id()) {
                    seq.moveNext();
                    sectionStart = seq.offset();
                    break;
                } else if (JavaTokenId.BLOCK_COMMENT == token.id() || JavaTokenId.JAVADOC_COMMENT == token.id()) {
                    break;
                } else if (JavaTokenId.WHITESPACE == token.id()) {
                    //#196053: not removing leading whitespaces, see ClassMemberTest.test196053b
                    // but save the location at the line start for the case that the annotation section occupies a full line.
                    int indexOf = token.text().toString().lastIndexOf('\n');
                    if (indexOf > -1) {
                        fullLineSectionStart = seq.offset() + indexOf + 1;
                    } else {
                        fullLineSectionStart = seq.offset();
                    }
                }
            }
            seq.move(sectionEnd);
            seq.movePrevious();
            while (seq.moveNext() && nonRelevant.contains((token = seq.token()).id())) {
                if (JavaTokenId.LINE_COMMENT == token.id()) {
                    sectionEnd = seq.offset();
                    if (seq.moveNext()) {
                        sectionEnd = seq.offset();
                    }
                    break;
                } else if (JavaTokenId.BLOCK_COMMENT == token.id() || JavaTokenId.JAVADOC_COMMENT == token.id()) {
                    break;
                } else if (JavaTokenId.WHITESPACE == token.id()) {
                    int indexOf = token.text().toString().lastIndexOf('\n');
                    if (indexOf > -1) {
                        sectionEnd = seq.offset() + indexOf + 1;
                        if (fullLineSectionStart > -1) {
                            sectionStart = fullLineSectionStart;
                        }
                    } else {
                        sectionEnd = seq.offset() + token.text().length();
                    }
                }
            }
            return new int[] { sectionStart, sectionEnd };
        }

    }
    
    private abstract static class BaseEstimator extends PositionEstimator {
        
        JavaTokenId precToken;
        private ArrayList<String> separatorList;

        private BaseEstimator(JavaTokenId precToken,
                List<? extends Tree> oldL,
                List<? extends Tree> newL,
                DiffContext diffContext)
        {
            super(oldL, newL, diffContext);
            this.precToken = precToken;
        }
        
        public String head() { return " " + precToken.fixedText() + " "; }
        public String sep()  { return ", "; }
        
        @SuppressWarnings("empty-statement")
        public void initialize() {
            separatorList = new ArrayList<String>(oldL.size());
            boolean first = true;
            int size = oldL.size();
            matrix = new int[size+1][5];
            matrix[size] = new int[] { -1, -1, -1, -1, -1 };
            int i = 0;
            SourcePositions positions = diffContext.trees.getSourcePositions();
            CompilationUnitTree compilationUnit = diffContext.origUnit;
            for (Tree item : oldL) {
                String separatedText = "";
                int treeStart = (int) positions.getStartPosition(compilationUnit, item);
                int treeEnd = (int) positions.getEndPosition(compilationUnit, item);
                seq.move(treeStart);
                int startIndex = seq.index();
                int beforer = -1;
                if (first) {
                    // go back to throws keywrd.
                    while (seq.movePrevious() && seq.token().id() != precToken) ;
                    int throwsIndex = seq.index();
                    beforer = throwsIndex+1;
                    // go back to closing )
                    moveToSrcRelevant(seq, Direction.BACKWARD);
                    seq.moveNext();
                    int beg = seq.index();
                    seq.move(treeEnd);
                    matrix[i++] = new int[] { beg, throwsIndex, beforer, startIndex, seq.index() };
                    first = false;
                } else {
                    int afterPrevious = matrix[i-1][4];
                    // move to comma
                    while (seq.movePrevious() && (seq.token().id() != COMMA))
                        if (seq.token().id() == WHITESPACE)
                            separatedText = seq.token().text() + separatedText;
                        else if (seq.token().id() == LINE_COMMENT)
                            separatedText = '\n' + separatedText;
                    separatorList.add(separatedText);
                    int separator = seq.index();
                    int afterSeparator = separator + 1; // bug
                    if (afterPrevious == separator) {
                        afterPrevious = -1;
                    }
                    seq.move(treeEnd);
                    matrix[i++] = new int[] { afterPrevious, separator, afterSeparator, startIndex, seq.index() };
                }
                if (i == size) {
                    // go forward to { or ;
                    moveToSrcRelevant(seq, Direction.FORWARD);
                    matrix[i][2] = seq.index();
                }
                seq.move(treeEnd);
            }
            initialized = true;
        }
        
        public String getIndentString() {
            if (!initialized) initialize();
            Map<String, Integer> map = new HashMap<String, Integer>();
            for (String item : separatorList) {
                String s = item;
                if (s.lastIndexOf("\n") > -1) {
                    s = s.substring(item.lastIndexOf("\n"));
                }
                Integer count = map.get(s);
                if (count != null) {
                    map.put(s, count++);
                } else {
                    map.put(s, 1);
                }
            }
            int max = -1;
            String s = null;
            for (String item : map.keySet()) {
                if (map.get(item) > max) {
                    s = item;
                    max = map.get(item);
                }
            }
            return s;
        }
        
        public int getInsertPos(int index) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public int[] getPositions(int index) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    
        public int prepare(int startPos, StringBuilder aHead,
                           StringBuilder aTail) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public int[] sectionRemovalBounds(StringBuilder replacement) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Provides position estimator for features in type declaration.
     */
    static class MembersEstimator extends PositionEstimator {
        
        private List<int[]> data;
        private List<String> append;
        private int minimalLeftPosition;
        private final boolean skipTrailingSemicolons;
        private int sectionEnd;
        private int sectionStart;
        
        public MembersEstimator(final List<? extends Tree> oldL, 
                                final List<? extends Tree> newL, 
                                final DiffContext diffContext,
                                boolean skipTrailingSemicolons)
        {
            super(oldL, newL, diffContext);
            this.minimalLeftPosition = (-1);
            this.skipTrailingSemicolons = skipTrailingSemicolons;
        }
        
        public MembersEstimator(final List<? extends Tree> oldL, 
                                final List<? extends Tree> newL, 
                                final int minimalLeftPosition,
                                final DiffContext diffContext,
                                boolean skipTrailingSemicolons)
        {
            super(oldL, newL, diffContext);
            this.minimalLeftPosition = minimalLeftPosition;
            this.skipTrailingSemicolons = skipTrailingSemicolons;
        }
        
        private void findNextBoundary(Tree item, int start, int end) {
            int off = Math.max(start, end -1);
            this.sectionEnd = diffContext.blockSequences.findSectionEnd(off);
            this.sectionStart = diffContext.blockSequences.findSectionStart(start);
        }
        
        private JavaTokenId moveToSrcRelevantBounded(TokenSequence seq, Direction dir) {
            int bound = dir == Direction.BACKWARD ? sectionStart : sectionEnd;
            return moveToDifferentThan(seq, dir, nonRelevant, bound);
        }
        
        @Override()
        public void initialize() {
            this.sectionEnd = diffContext.textLength;
            int size = oldL.size();
            data = new ArrayList<int[]>(size);
            append = new ArrayList<String>(size);
            SourcePositions positions = diffContext.trees.getSourcePositions();
            CompilationUnitTree compilationUnit = diffContext.origUnit;
            boolean first = true;
            for (Tree item : oldL) {
                int treeStart = (int) positions.getStartPosition(compilationUnit, item);
                int treeEnd = (int) positions.getEndPosition(compilationUnit, item);
                
                findNextBoundary(item, treeStart, treeEnd);
                
                // teribolak
                if (item instanceof FieldGroupTree) { //
                    FieldGroupTree fgt = ((FieldGroupTree) item);
                    List<JCVariableDecl> vars = fgt.getVariables();
                    treeEnd = (int) positions.getEndPosition(compilationUnit, vars.get(vars.size()-1));
                } else {
                    seq.move(treeEnd);
                    if (seq.movePrevious() && seq.offset() >= sectionStart && nonRelevant.contains(seq.token().id())) {
                        moveToSrcRelevantBounded(seq, Direction.BACKWARD);
                        seq.moveNext();
                        treeEnd = Math.max(seq.offset(), treeStart);
                    }
                }

                String itemAppend = "";
                int    appendInsertPos = -1;

                if (isEnum(item)) {
                    seq.move(treeEnd);
                    moveToSrcRelevantBounded(seq, Direction.FORWARD);
                    if (JavaTokenId.COMMA == seq.token().id()) {
                        treeEnd = seq.offset() + seq.token().length();
                        moveToSrcRelevantBounded(seq, Direction.FORWARD);
                    }
                    if (JavaTokenId.SEMICOLON == seq.token().id()) {
                        seq.moveNext();
                    } else {
                        itemAppend = ";";
                        appendInsertPos = treeEnd;
                    }
                    treeEnd = seq.offset();
                }

                seq.move(treeStart);
                seq.moveNext();
                if (null != moveToSrcRelevantBounded(seq, Direction.BACKWARD)) {
                    seq.moveNext();
                }
                int previousEnd = seq.offset();
                Token<JavaTokenId> token;
                // preceding whitespace is joined with the statement as follows:
                // 1/ if the preceding whitespace contains a newline, all whitespace following the newline
                // is assigned to the statement.
                // 2/ on the 1st line in the file all preceding whitespace is assigned
                // 3/ if consecutive commands are separated by more than 1 WS char, all except the last one is
                // assigned to the preceding command; the single whitespace is joined with the next command.
                // 4/ all immediately following whitespace up to the newline are assigned to the command
                // (3) is important if commands are torn apart; each of them will be separated by WS
                int wsOnlyStart = -1;
                // localResult will receive start of non-whitespace part of the statment (either statement, or comment)
                int localResult = -1;
                int insertPos = -1;
                boolean nlBefore = item.getKind() != Tree.Kind.EMPTY_STATEMENT;
                int wsStart = -1;
                int wsEnd = -1;
                while (nonRelevant.contains((token = seq.token()).id())) {
                    switch (token.id()) {
                        case WHITESPACE:
                            int indexOf = token.text().toString().lastIndexOf('\n');
                            if (indexOf > -1) {
                                // indexOf cannot be -1; insertPos will point after the 1st newline in preceding
                                // whitespace, while "element begin" will point after the last line to preserve
                                // whitespaces when deleting the element.
                                insertPos = seq.offset() + (token.text().toString().indexOf('\n')) + 1;
                                localResult = seq.offset() + indexOf + 1;
                                nlBefore = true;
                            } else if (first || previousEnd == 0) {
                                wsOnlyStart = previousEnd;
                            } else if (token.length() > 1) {
                                wsOnlyStart = seq.offset() + token.length() - 1;
                            }
                            break;
                        case LINE_COMMENT:
                        case BLOCK_COMMENT:
                        case JAVADOC_COMMENT:
                            if (wsStart == -1) {
                                wsStart = localResult;
                            }
                            localResult = seq.offset();
                            break;
                    }
                    if (localResult > 0) {
                        previousEnd = localResult;
                        break;
                    }
                    if (!seq.moveNext() || seq.offset() >= sectionEnd) break;
                }
                if (localResult == -1) {
                    // fallback in case the statement is preceded just by whitespace. Its position will extend,
                    // and localResult will point at the statement text
                    if (wsOnlyStart >= 0) {
                        previousEnd = wsOnlyStart;
                    }
                    localResult = seq.offset();
                }
                if (wsStart == -1) {
                    wsStart = localResult;
                }
                first = false;
                if (minimalLeftPosition != (-1) && minimalLeftPosition > previousEnd) {
                    previousEnd = minimalLeftPosition;
                    insertPos = localResult = minimalLeftPosition;
                }
                if (insertPos == -1) {
                    insertPos = localResult;
                }
                seq.move(treeEnd);
                int wideEnd = treeEnd;

                LinkedList<Pair<Integer, Integer>> commentEndPos = new LinkedList<Pair<Integer, Integer>>();
                int maxLines = 0;
                int newlines = 0;
                boolean cont = true;
                // tokens after block boundary must be scanned, bcs right brace may be found, which will join all comments
                // to the preceding statement. Otherwise, comments could be left dangling and end position could point in between
                // an element and following guarded block end.
                while (cont && seq.moveNext()) {
                    Token<JavaTokenId> t = seq.token();
                    switch(t.id()) {
                        case WHITESPACE:
                            // ignore wsp after block boundary
                            if (seq.offset() >= sectionEnd) {
                                break;
                            }
                            if (newlines == 0) {
                                int indexOf = t.text().toString().indexOf('\n');
                                if (indexOf > -1) {
                                    if (commentEndPos.isEmpty()) {
                                        wideEnd = seq.offset() + indexOf + (nlBefore ? 1 : 0);
                                        wsEnd = wideEnd;
                                    } else {
                                        commentEndPos.add(Pair.of(commentEndPos.getLast().first(), seq.offset() + indexOf + 1));
                                    }
                                } else if (t.length() > 1) {
                                    wideEnd = seq.offset() + t.length() - 1;
                                } else {
                                    // whitespace does not extend to the end of line; join the trailing whitespace with
                                    // the statement.
                                    wideEnd = seq.offset() + t.length();
                                }
                            }
                            newlines += numberOfNL(t);
                            break;
                        case LINE_COMMENT:
                        case BLOCK_COMMENT:
                            // ignore wsp after block boundary
                            if (seq.offset() >= sectionEnd) {
                                break;
                            }
                            if (wsEnd == -1) {
                                wsEnd = wideEnd;
                            }
                            if (seq.offset() > minimalLeftPosition)
                                commentEndPos.add(Pair.of(newlines, seq.offset() + t.text().length()));
                            maxLines = Math.max(maxLines, newlines);
                            if (t.id() == JavaTokenId.LINE_COMMENT) {
                                newlines = 1;
                            } else {
                                newlines = 0;
                            }
                            break;
                        case SEMICOLON:
                            if (skipTrailingSemicolons) {
                                wideEnd = seq.offset() + t.text().length();
                            } else {
                                cont = false;
                            }
                            break;
                        case RBRACE:
                            // end of block, assign all remaining comments to the preceding element.
                            maxLines = Integer.MAX_VALUE;
                        case JAVADOC_COMMENT:
                        default:
                            cont = false;
                            break;
                    }
                }
                maxLines = Math.max(maxLines, newlines);
                for (Pair<Integer, Integer> comment : commentEndPos) {
                    if (comment.first() < maxLines || comment.first() == 0) {
                        wideEnd = comment.second();
                    } else {
                        break;
                    }
                }
                if (wideEnd < treeEnd) wideEnd = treeEnd;
                if (wsEnd == -1) {
                    wsEnd = treeEnd;
                }
                if (minimalLeftPosition < wideEnd) minimalLeftPosition = wideEnd;
                data.add(new int[] { previousEnd, wideEnd, previousEnd, appendInsertPos, insertPos, wsStart, wsEnd });
                append.add(itemAppend);
            }
            initialized = true;
        }

        private boolean isEnum(Tree tree) {
            if (tree instanceof FieldGroupTree) return ((FieldGroupTree) tree).isEnum();
            if (tree instanceof VariableTree) return (((JCVariableDecl) tree).getModifiers().flags & Flags.ENUM) != 0;
            return false;
        }
        
        private int numberOfNL(Token<JavaTokenId> t) {
            int count = 0;
            CharSequence charSequence = t.text();
            for (int i = 0; i < charSequence.length(); i++) {
                char a = charSequence.charAt(i);
                if ('\n' == a) {
                    count++;
                }
            }
            return count;
        }

        @Override()
        public int getInsertPos(int index) {
            if (!initialized) initialize();
            if (data.isEmpty()) {
                return -1;
            } else {
                int pos = (index == data.size() ? data.get(index-1)[1] : data.get(index)[4]);
                return moveBelowGuarded(pos);
            }
        }

        /**
         * Used when all elements from the list was removed.
         */
        public int[] sectionRemovalBounds(StringBuilder replacement) {
            if (!initialized) initialize();
            // this part should be generalized
            assert !oldL.isEmpty() && newL.isEmpty(); // check the call correctness
            SourcePositions positions = diffContext.trees.getSourcePositions();
            CompilationUnitTree compilationUnit = diffContext.origUnit;
            int sectionStart = (int) positions.getStartPosition(compilationUnit, oldL.get(0));
            int sectionEnd = (int) positions.getEndPosition(compilationUnit, oldL.get(oldL.size()-1));
            // end of generalization part
            
            seq.move(sectionStart);
            seq.moveNext();
            Token<JavaTokenId> token;
            while (seq.movePrevious() && nonRelevant.contains((token = seq.token()).id())) {
                if (JavaTokenId.LINE_COMMENT == token.id()) {
//                    seq.moveNext();
//                    sectionStart = seq.offset();
//                    break;
                    continue;
                } else if (JavaTokenId.BLOCK_COMMENT == token.id() || JavaTokenId.JAVADOC_COMMENT == token.id()) {
                    continue;
                } else if (JavaTokenId.WHITESPACE == token.id()) {
                    int indexOf = token.text().toString().indexOf('\n');
                    if (indexOf > -1) {
                        sectionStart = seq.offset() + indexOf + 1;
                    } else {
                        sectionStart = seq.offset();
                    }
                }
            }
            seq.move(sectionEnd);
            seq.movePrevious();
            
            // PENDING: comment conditional removal (if mapped) should be replicated into other Estimators.
            boolean moreWhitespaces = false;
            int lastNewline = -1;
            while (seq.moveNext() && nonRelevant.contains((token = seq.token()).id())) {
                if (JavaTokenId.LINE_COMMENT == token.id()) {
                    sectionEnd = seq.offset();
                    boolean mapped = diffContext.usedComments.get(sectionEnd) != null;
                    // only remove 1st line of line comment if there's a whitespace between the removed
                    // content and the comment
                    if (!mapped) {
                        if (!moreWhitespaces && seq.moveNext()) {
                            sectionEnd = seq.offset();
                        }
                        break;
                    } else {
                        sectionEnd = seq.offset() + seq.token().length();
                        lastNewline = sectionEnd;
                    }
                } else if (JavaTokenId.BLOCK_COMMENT == token.id() || JavaTokenId.JAVADOC_COMMENT == token.id()) {
                    boolean mapped = diffContext.usedComments.get(sectionEnd) != null;
                    if (!mapped) {
                        break;
                    }
                    // comments from removed statements are colleted elsewhere
                    lastNewline = -1;
                    continue;
                } else if (JavaTokenId.WHITESPACE == token.id()) {
                    int indexOf = token.text().toString().lastIndexOf('\n');
                    int after = seq.offset() + token.text().length();
                    if (indexOf > -1) {
                        sectionEnd = seq.offset() + indexOf + 1;
                        moreWhitespaces |= token.text().toString().indexOf('\n') != indexOf;
                        lastNewline = sectionEnd;
                    } else if (lastNewline == -1) {
                        sectionEnd = after;
                    }
                }
            }
            return new int[] { sectionStart, sectionEnd };
        }
        
        public String head() { return ""; }

        public String sep() { return ""; }

        public String getIndentString() { return ""; }

        @Override
        public String append(int index) {
            return append.get(index);
        }
        
        @Override()
        public int[] getPositions(int index) {
            if (!initialized) initialize();
            return data.get(index).clone();
        }
        
        @Override
        public LineInsertionType lineInsertType() {
            return LineInsertionType.AFTER;
        }
        
        public int prepare(int startPos, StringBuilder aHead,
                           StringBuilder aTail) {
            seq.move(startPos);
            seq.moveNext();
            moveToSrcRelevant(seq, Direction.BACKWARD);
            while (seq.moveNext() && nonRelevant.contains(seq.token().id())) {
                if (JavaTokenId.WHITESPACE == seq.token().id()) {
                    int newlineInToken = seq.token().text().toString().indexOf('\n');
                    if (newlineInToken > -1) {
                        return seq.offset() + newlineInToken + 1;
                    }
                } else if (JavaTokenId.LINE_COMMENT == seq.token().id()) {
                    return seq.offset() + seq.token().text().length();
                }
            }
            return startPos;
        }
        
        @Override
        public String toString() {
            // state should be saved to avoid hard-to-spot defects when debugging initialize()
            boolean inited = initialized;
            int spos = seq.offset();
            if (!inited) initialize();
            String result = "";
            for (int i = 0; i < data.size(); i++) {
                int[] pos = data.get(i);
                String s = diffContext.origText.substring(pos[0], pos[1]);
                result += "[" + s + "]";
            }
            this.seq.move(spos);
            this.seq.moveNext();
            this.initialized = inited;
            return result;
        }

    }
        
    /**
     * Provides position estimator for features in type declaration.
     */
    static class CatchesEstimator extends PositionEstimator {
        
        private final boolean hasFinally;
        private List<int[]> data;
        
        public CatchesEstimator(final List<? extends Tree> oldL, 
                                final List<? extends Tree> newL, 
                                final boolean hasFinally,
                                final DiffContext diffContext)
        {
            super(oldL, newL, diffContext);
            this.hasFinally = hasFinally;
        }
        
        @Override()
        public void initialize() {
            int size = oldL.size();
            data = new ArrayList<int[]>(size);
            SourcePositions positions = diffContext.trees.getSourcePositions();
            CompilationUnitTree compilationUnit = diffContext.origUnit;
            
            for (Tree item : oldL) {
                int treeStart = (int) positions.getStartPosition(compilationUnit, item);
                int treeEnd = (int) positions.getEndPosition(compilationUnit, item);

                seq.move(treeStart);
                seq.moveNext();
                if (null != moveToSrcRelevant(seq, Direction.BACKWARD)) {
                    seq.moveNext();
                }
                int previousEnd = seq.offset();
                Token<JavaTokenId> token;
                while (nonRelevant.contains((token = seq.token()).id())) {
                    int localResult = -1;
                    switch (token.id()) {
                        case WHITESPACE:
                            int indexOf = token.text().toString().indexOf('\n');
                            if (indexOf > -1) {
                                localResult = seq.offset() + indexOf;
                            }
                            break;
                        case LINE_COMMENT:
                            previousEnd = seq.offset() + token.text().length();
                            break;
                    }
                    if (localResult > 0) {
                        previousEnd = localResult;
                        break;
                    }
                    if (!seq.moveNext()) break;
                }
                data.add(new int[] { previousEnd, treeEnd, previousEnd });
            }
            initialized = true;
        }
        
        @Override()
        public int getInsertPos(int index) {
            if (!initialized) initialize();
            if (data.isEmpty()) {
                return -1;
            } else {
                return index == data.size() ? data.get(index-1)[2] : data.get(index)[0];
            }
        }

        /**
         * Used when all elements from the list was removed.
         */
        public int[] sectionRemovalBounds(StringBuilder replacement) {
            if (!initialized) initialize();
            // this part should be generalized
            assert !oldL.isEmpty() && newL.isEmpty(); // check the call correctness
            SourcePositions positions = diffContext.trees.getSourcePositions();
            CompilationUnitTree compilationUnit = diffContext.origUnit;
            int sectionStart = (int) positions.getStartPosition(compilationUnit, oldL.get(0));
            int sectionEnd = (int) positions.getEndPosition(compilationUnit, oldL.get(oldL.size()-1));
            // end of generalization part
            
            seq.move(sectionStart);
            seq.moveNext();
            Token<JavaTokenId> token;
            while (seq.movePrevious() && nonRelevant.contains((token = seq.token()).id())) {
                if (JavaTokenId.LINE_COMMENT == token.id()) {
                    seq.moveNext();
                    sectionStart = seq.offset();
                    break;
                } else if (JavaTokenId.BLOCK_COMMENT == token.id() || JavaTokenId.JAVADOC_COMMENT == token.id()) {
                    break;
                } else if (JavaTokenId.WHITESPACE == token.id()) {
                    int indexOf = token.text().toString().indexOf('\n');
                    if (indexOf > -1) {
                        sectionStart = seq.offset() + indexOf + 1;
                    } else {
                        sectionStart = seq.offset();
                    }
                }
            }
            seq.move(sectionEnd);
            int wideEnd = sectionEnd;
            token = null;
            seq.movePrevious();
            while (seq.moveNext() && nonRelevant.contains((token = seq.token()).id())) {
                if (JavaTokenId.LINE_COMMENT == token.id()) {
                    wideEnd = seq.offset();
                    if (seq.moveNext()) {
                        wideEnd = seq.offset();
                    }
                    break;
                } else if (JavaTokenId.BLOCK_COMMENT == token.id() || JavaTokenId.JAVADOC_COMMENT == token.id()) {
                    break;
                } else if (JavaTokenId.WHITESPACE == token.id()) {
                    int indexOf = token.text().toString().lastIndexOf('\n');
                    if (indexOf > -1) {
                        wideEnd = seq.offset() + indexOf + 1;
                    } else {
                        wideEnd = seq.offset() + token.text().length();
                    }
                }
            }
            return new int[] { sectionStart, token != null && token.id() != JavaTokenId.FINALLY ? wideEnd : sectionEnd};
        }
        
        public String head() { return ""; }

        public String sep() { return ""; }

        public String getIndentString() { return ""; }
        
        @Override()
        public int[] getPositions(int index) {
            if (!initialized) initialize();
            return data.get(index).clone();
        }
        
        public int prepare(int startPos, StringBuilder aHead,
                           StringBuilder aTail) {
            if (!hasFinally) return startPos;
            seq.move(startPos);
            seq.moveNext();
            moveToSrcRelevant(seq, Direction.BACKWARD);
            while (seq.moveNext() && nonRelevant.contains(seq.token().id())) {
                if (JavaTokenId.WHITESPACE == seq.token().id()) {
                    int newlineInToken = seq.token().text().toString().indexOf('\n');
                    if (newlineInToken > -1) {
                        return seq.offset() + newlineInToken + 1;
                    }
                } else if (JavaTokenId.LINE_COMMENT == seq.token().id()) {
                    return seq.offset() + seq.token().text().length();
                }
            }
            return startPos;
        }
        
        @Override
        public String toString() {
            if (!initialized) initialize();
            String result = "";
            for (int i = 0; i < data.size(); i++) {
                int[] pos = data.get(i);
                String s = diffContext.origText.substring(pos[0], pos[1]);
                result += "[" + s + "]";
            }
            return result;
        }

    }

    /**
     * Provides position estimator for top-level classes
     */
    static class TopLevelEstimator extends PositionEstimator {
        
        private List<int[]> data;
        
        public TopLevelEstimator(final List<? extends Tree> oldL, 
                                 final List<? extends Tree> newL, 
                                 final DiffContext diffContext)
        {
            super(oldL, newL, diffContext);
        }
        
        @Override()
        public void initialize() {
            int size = oldL.size();
            data = new ArrayList<int[]>(size);
            SourcePositions positions = diffContext.trees.getSourcePositions();
            CompilationUnitTree compilationUnit = diffContext.origUnit;
            
            for (Tree item : oldL) {
                int treeStart = (int) positions.getStartPosition(compilationUnit, item);
                int treeEnd = (int) positions.getEndPosition(compilationUnit, item);

                if (treeEnd == (-1) && item.getKind() == Kind.CLASS) {
                    //unnamed class, use last member, or start pos:
                    ClassTree clazz = (ClassTree) item;
                    Tree lastMember = clazz.getMembers().get(clazz.getMembers().size() - 1);
                    treeEnd = (int) positions.getEndPosition(compilationUnit, lastMember);
                    if (treeEnd == (-1)) {
                        //TODO: test
                        treeEnd = treeStart;
                    }
                }

                seq.move(treeStart);
                seq.moveNext();
                if (null != moveToSrcRelevant(seq, Direction.BACKWARD)) {
                    seq.moveNext();
                }
                int previousEnd = seq.offset();
                Token<JavaTokenId> token;
                while (nonRelevant.contains((token = seq.token()).id())) {
                    int localResult = -1;
                    switch (token.id()) {
                        case WHITESPACE:
                            int indexOf = token.text().toString().indexOf('\n');
                            if (indexOf > -1) {
                                localResult = seq.offset() + indexOf + 1;
                            }
                            break;
                        case LINE_COMMENT:
                            previousEnd = seq.offset() + token.text().length();
                            break;
                    case JAVADOC_COMMENT:
                            previousEnd = seq.offset();
                            break;
                    }
                    if (localResult > 0) {
                        previousEnd = localResult;
                        break;
                    }
                    if (!seq.moveNext()) break;
                }
                int wideStart = previousEnd;
                seq.move(treeStart);
                seq.moveNext();
                seq.movePrevious();
                while (nonRelevant.contains((token = seq.token()).id())) {
                    int localResult = -1;
                    switch (token.id()) {
                        case WHITESPACE:
                            int indexOf = token.text().toString().lastIndexOf('\n');
                            if (indexOf > -1) {
                                localResult = seq.offset() + indexOf + 1;
                            }
                            break;
                        case LINE_COMMENT:
                            localResult = seq.offset() + token.text().length();
                            break;
                        case JAVADOC_COMMENT:
                        case BLOCK_COMMENT:
                            wideStart = seq.offset();
                            break;
                    }
                    if (wideStart > previousEnd) {
                        break;
                    }
                    if (localResult > 0) {
                        wideStart = localResult;
                    }
                    if (!seq.movePrevious()) break;
                }
                
                seq.move(treeEnd);
                int wideEnd = treeEnd;
                while (seq.moveNext() && nonRelevant.contains((token = seq.token()).id())) {
                    if (JavaTokenId.WHITESPACE == token.id()) {
                        int indexOf = token.text().toString().indexOf('\n');
                        if (indexOf > -1) {
                            wideEnd = seq.offset() + indexOf + 1;
                        } else {
                            wideEnd = seq.offset();
                        }
                    } else if (JavaTokenId.LINE_COMMENT == token.id()) {
                        wideEnd = seq.offset() + token.text().length();
                        break;
                    } else if (JavaTokenId.JAVADOC_COMMENT == token.id()) {
                        break;
                    }
                    if (wideEnd > treeEnd)
                        break;
                }
                if (wideEnd < treeEnd) wideEnd = treeEnd;
                data.add(new int[] { wideStart, wideEnd, previousEnd });
            }
            initialized = true;
        }
        
        @Override()
        public int getInsertPos(int index) {
            if (!initialized) initialize();
            if (data.isEmpty()) {
                return -1;
            } else {
                return index == data.size() ? data.get(index-1)[2] : data.get(index)[0];
            }
        }

        /**
         * Used when all elements from the list was removed.
         */
        public int[] sectionRemovalBounds(StringBuilder replacement) {
            if (!initialized) initialize();
            // this part should be generalized
            assert !oldL.isEmpty() && newL.isEmpty(); // check the call correctness
            SourcePositions positions = diffContext.trees.getSourcePositions();
            CompilationUnitTree compilationUnit = diffContext.origUnit;
            int sectionStart = (int) positions.getStartPosition(compilationUnit, oldL.get(0));
            int sectionEnd = (int) positions.getEndPosition(compilationUnit, oldL.get(oldL.size()-1));
            // end of generalization part
            
            seq.move(sectionStart);
            seq.moveNext();
            Token<JavaTokenId> token;
            while (seq.movePrevious() && nonRelevant.contains((token = seq.token()).id())) {
                if (JavaTokenId.LINE_COMMENT == token.id()) {
                    seq.moveNext();
                    sectionStart = seq.offset();
                    break;
                } else if (JavaTokenId.BLOCK_COMMENT == token.id() || JavaTokenId.JAVADOC_COMMENT == token.id()) {
                    break;
                } else if (JavaTokenId.WHITESPACE == token.id()) {
                    int indexOf = token.text().toString().indexOf('\n');
                    if (indexOf > -1) {
                        sectionStart = seq.offset() + indexOf + 1;
                    } else {
                        sectionStart = seq.offset();
                    }
                }
            }
            seq.move(sectionEnd);
            seq.movePrevious();
            while (seq.moveNext() && nonRelevant.contains((token = seq.token()).id())) {
                if (JavaTokenId.LINE_COMMENT == token.id()) {
                    sectionEnd = seq.offset();
                    if (seq.moveNext()) {
                        sectionEnd = seq.offset();
                    }
                    break;
                } else if (JavaTokenId.BLOCK_COMMENT == token.id() || JavaTokenId.JAVADOC_COMMENT == token.id()) {
                    break;
                } else if (JavaTokenId.WHITESPACE == token.id()) {
                    int indexOf = token.text().toString().lastIndexOf('\n');
                    if (indexOf > -1) {
                        sectionEnd = seq.offset() + indexOf + 1;
                    } else {
                        sectionEnd += seq.offset() + token.text().length();
                    }
                }
            }
            return new int[] { sectionStart, sectionEnd };
        }
        
        public String head() { return ""; }

        public String sep() { return ""; }

        public String getIndentString() { return ""; }
        
        @Override()
        public int[] getPositions(int index) {
            if (!initialized) initialize();
            return data.get(index).clone();
        }
        
        @Override
        public LineInsertionType lineInsertType() {
            return LineInsertionType.AFTER;
        }

        @Override()
        public int prepare(int startPos, StringBuilder aHead,
                           StringBuilder aTail) {
            seq.moveEnd();
            if (seq.movePrevious()) {
                if (JavaTokenId.WHITESPACE == seq.token().id()) {
                    int firstNewLineIndex = -1;
                    String tokenText = seq.token().text().toString();
                    if ((firstNewLineIndex = tokenText.indexOf('\n')) > -1) {
                        if (tokenText.lastIndexOf('\n') == firstNewLineIndex) {
                            aHead.append('\n');
                        }
                    } else {
                        aHead.append("\n\n");
                    }
                } else if (JavaTokenId.LINE_COMMENT != seq.token().id()) {
                    aHead.append("\n\n");
                }
                return seq.offset() + seq.token().text().length();
            }
            return startPos;
        }
        
        @Override()
        public String toString() {
            if (!initialized) initialize();
            String result = "";
            for (int i = 0; i < data.size(); i++) {
                int[] pos = data.get(i);
                String s = diffContext.origText.substring(pos[0], pos[1]);
                result += "[" + s + "]";
            }
            return result;
        }

    }
    ////////////////////////////////////////////////////////////////////////////
    // Utility methods
    @SuppressWarnings("empty-statement")
    int moveBelowGuarded(int pos) {
        if (diffContext != null) {
            return diffContext.blockSequences.findNextWritablePos(pos);
        }
        return pos;
    }

    /**
     * Moves in specified direction to java source relevant token.
     *
     * In other words, it moves until the token is something important
     * for javac compiler. (every token except WHITESPACE, BLOCK_COMMENT,
     * LINE_COMMENT and JAVADOC_COMMENT)
     *
     * @param  seq  token sequence which is used for move.
     * @param  dir  direction - either forward or backward.
     * @return      relevant token identifier.
     *
     */
    public static JavaTokenId moveToSrcRelevant(TokenSequence<JavaTokenId> seq,
                                                 Direction dir)
    {
        return moveToDifferentThan(seq, dir, nonRelevant);
    }
    
    /**
     * 
     * @param seq the token sequence
     * @param dir direction
     * @return 
     */
    public static int offsetToSrcWiteOnLine(TokenSequence<JavaTokenId> seq,
                                                 Direction dir)
    {
        boolean notBound = false;
        seq.moveNext();
        int savePos = seq.offset();
        switch (dir) {
            case BACKWARD:
                while ((notBound = seq.movePrevious())) {
                    JavaTokenId tid = seq.token().id();
                    if (tid == WHITESPACE) {
                        int nl = seq.token().text().toString().indexOf('\n');
                        if (nl > -1) {
                            // return the position after newline:
                            return seq.offset() + nl + 1;
                        }
                    } else if (!nonRelevant.contains(tid)) {
                        break;
                    } else {
                        savePos = seq.offset();
                    }
                }
                break;
            case FORWARD:
                while ((notBound = seq.moveNext())) {
                    JavaTokenId tid = seq.token().id();
                    if (tid == WHITESPACE) {
                        int nl = seq.token().text().toString().indexOf('\n');
                        if (nl > -1) {
                            // return the position after newline:
                            return seq.offset() + nl;
                        }
                    } else if (!nonRelevant.contains(tid)) {
                        break;
                    } else {
                        savePos = seq.offset() + seq.token().length();
                    }
                }
                break;
        }
        if (!notBound) {
            return -1;
        }
        return savePos;
    }

    @SuppressWarnings("empty-statement")
    public static JavaTokenId moveToDifferentThan(
        TokenSequence<JavaTokenId> seq,
        Direction dir,
        EnumSet<JavaTokenId> set) {
        return moveToDifferentThan(seq, dir, set, -1);
    }
    
    public static JavaTokenId moveToDifferentThan(
        TokenSequence<JavaTokenId> seq,
        Direction dir,
        EnumSet<JavaTokenId> set, int boundary) 
    {
        boolean notBound = false;
        switch (dir) {
            case BACKWARD:
                while ((notBound = seq.movePrevious()) /* && (boundary == -1 || seq.offset() >= boundary) */ && set.contains(seq.token().id())) {
                    if (boundary != -1 && seq.offset() < boundary) {
                        notBound = false;
                        break;
                    }
                }
                break;
            case FORWARD:
                while ((notBound = seq.moveNext()) && /* (boundary == -1 || seq.offset() < boundary) && */ set.contains(seq.token().id())) {
                    if (boundary != -1 && seq.offset() >= boundary) {
                        notBound = false;
                        break;
                    }
                }
                break;
        }
        return notBound ? seq.token().id() : null;
    }
    
    private static int goAfterFirstNewLine(final TokenSequence<JavaTokenId> seq) {
        // ensure that we are not after the last token, if so,
        // go to last
        if (seq.token() == null) 
            seq.movePrevious();
        
        int base = seq.offset();
        seq.movePrevious();
        while (seq.moveNext() && nonRelevant.contains(seq.token().id())) {
            switch (seq.token().id()) {
                case LINE_COMMENT:
                    seq.moveNext();
                    return seq.offset();
                case WHITESPACE:
                    char[] c = seq.token().text().toString().toCharArray();
                    int index = 0;
                    while (index < c.length) {
                        if (c[index++] == '\n') {
//                            while (index < c.length)
//                                if (c[index] != ' ' && c[index] != '\t')
//                                    break;
//                                else
//                                    ++index;
                            return base + index;
                        }
                    }
            }
        }
        return base;
    }
    
    @SuppressWarnings("empty-statement")
    private static int goAfterLastNewLine(final TokenSequence<JavaTokenId> seq) {
        int base = seq.offset();
        seq.movePrevious();
        while (seq.moveNext() && nonRelevant.contains(seq.token().id())) ;
        
        while (seq.movePrevious() && nonRelevant.contains(seq.token().id())) {
            switch (seq.token().id()) {
                case LINE_COMMENT:
                    seq.moveNext();
                    return seq.offset();
                case WHITESPACE:
                    char[] c = seq.token().text().toString().toCharArray();
                    for (int i = c.length; i > 0; ) {
                        if (c[--i] == '\n') {
                            return seq.offset() + i + 1;
                        }
                    }
            }
        }
        if ((seq.index() == 0 || seq.moveNext()) && nonRelevant.contains(seq.token().id())) {
            return seq.offset();
        }
        return base;
    }
    
    public static final boolean isSeparator(JavaTokenId id) {
        return "separator".equals(id.primaryCategory()); //NOI18N
    }
    
    /**
     * Represents non-relevant tokens in java source. (Tokens which are not
     * important for javac, i.e. line and block comments, empty lines and
     * whitespaces.)
     */
    public static final EnumSet<JavaTokenId> nonRelevant = EnumSet.<JavaTokenId>of(
            LINE_COMMENT, 
            BLOCK_COMMENT,
            JAVADOC_COMMENT,
            WHITESPACE
    );

    /**
     * Represents the direction to move. Either forward or backward.
     */
    public enum Direction {
        FORWARD, BACKWARD;
    }
    
    static int moveFwdToToken(TokenSequence<JavaTokenId> tokenSequence, 
            final int pos,
            JavaTokenId id)
    {
        tokenSequence.move(pos);
        tokenSequence.moveNext(); // Assumes the pos is located within input bounds
        while (!id.equals(tokenSequence.token().id())) {
            if (!tokenSequence.moveNext())
                return -1;
        }
        return tokenSequence.offset();
    }
    
    static JavaTokenId moveFwdToOneOfTokens(TokenSequence<JavaTokenId> tokenSequence, 
            final int pos,
            EnumSet<JavaTokenId> ids)
    {
        tokenSequence.move(pos);
        tokenSequence.moveNext(); // Assumes the pos is located within input bounds
        while (!ids.contains(tokenSequence.token().id())) {
            if (!tokenSequence.moveNext())
                return null;
        }
        return tokenSequence.token().id();
    }

    static int moveBackToToken(TokenSequence<JavaTokenId> tokenSequence, 
            final int pos,
            JavaTokenId id)
    {
        tokenSequence.move(pos);
        tokenSequence.moveNext(); // Assumes the pos is located within input bounds
        while (!id.equals(tokenSequence.token().id())) {
            if (!tokenSequence.movePrevious())
                return -1;
        }
        return tokenSequence.offset();
    }
    
}
