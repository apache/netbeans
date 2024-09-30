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

package org.netbeans.api.java.source;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import com.sun.tools.javac.tree.JCTree;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.source.builder.CommentHandlerService;
import org.netbeans.modules.java.source.builder.CommentSetImpl;
import org.netbeans.modules.java.source.query.CommentSet;

import static org.netbeans.modules.java.source.save.PositionEstimator.NOPOS;

/**
 * Attaches comments to trees.
 *
 * @author Pavel Flaska, Rastislav Komara, Jan Lahoda
 */
class AssignComments extends ErrorAwareTreeScanner<Void, Void> {
    
    private static final EnumSet<Tree.Kind> JAVADOC_KINDS = EnumSet.of(
            Tree.Kind.CLASS,
            Tree.Kind.INTERFACE,
            Tree.Kind.ENUM,

            Tree.Kind.METHOD,
            Tree.Kind.VARIABLE
    );
    
    private final CompilationInfo info;
    private final CompilationUnitTree unit;
    private final Tree commentMapTarget;
    private final TokenSequence<JavaTokenId> seq;
    private final CommentHandlerService commentService;
    private final SourcePositions positions;
    private int tokenIndexAlreadyAdded = -1;
    private boolean mapComments;
    private Tree parent = null;

    public AssignComments(final CompilationInfo info,
            final Tree commentMapTarget,
            final TokenSequence<JavaTokenId> seq,
            final CompilationUnitTree cut) {
        this(info, commentMapTarget, seq, cut, info.getTrees().getSourcePositions());
    }

    public AssignComments(final CompilationInfo info,
            final Tree commentMapTarget,
            final TokenSequence<JavaTokenId> seq,
            final SourcePositions positions) {
        this(info, commentMapTarget, seq, info.getCompilationUnit(), positions);
    }

    private AssignComments(final CompilationInfo info,
            final Tree commentMapTarget,
            final TokenSequence<JavaTokenId> seq,
            final CompilationUnitTree cut,
            final SourcePositions positions) {
        this.info = info;
        this.unit = cut;
        this.seq = seq;
        this.commentMapTarget = commentMapTarget;
        this.commentService = CommentHandlerService.instance(info.impl.getJavacTask().getContext());
        this.positions = positions;
    }

    /**
     * If != -1, provides an upper limit for preceding comments. Used in infix and postfix trees,
     * where the child should be assigned comments first; the parent might grab comments only from the
     * start of the leftmost child.
     */
    private int limit = -1;
    
    /**
     * Infix and Postfix children are traversed recursively; in order to avoid depth^2 traversal, remember
     * each found limit position
     */
    private final Map<Tree, Integer>  limitCache = new HashMap<>();

    private int setupLimit(Tree t) {
        int pos = ((JCTree)t).pos;
        Tree child;
        switch (t.getKind()) {
            // assignment
            case ASSIGNMENT: 
                child = ((AssignmentTree)t).getVariable();
                break;
            
            // compound assignment
            case AND_ASSIGNMENT:
            case DIVIDE_ASSIGNMENT:
            case LEFT_SHIFT_ASSIGNMENT:
            case MINUS_ASSIGNMENT:
            case MULTIPLY_ASSIGNMENT:
            case OR_ASSIGNMENT:
            case PLUS_ASSIGNMENT:
            case REMAINDER_ASSIGNMENT:
            case RIGHT_SHIFT_ASSIGNMENT:
            case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
                child = ((CompoundAssignmentTree)t).getVariable();
                break;
                
            // binary ops
            case AND:
            case CONDITIONAL_AND: case CONDITIONAL_OR:
            case DIVIDE:
            case EQUAL_TO: case GREATER_THAN: case GREATER_THAN_EQUAL:
            case LEFT_SHIFT: case LESS_THAN: case LESS_THAN_EQUAL:
            case MINUS: case MULTIPLY:
            case NOT_EQUAL_TO: case OR:
            case PLUS: case REMAINDER: case RIGHT_SHIFT:
            case UNSIGNED_RIGHT_SHIFT:
                child = ((BinaryTree)t).getLeftOperand();
                break;
                
            case INSTANCE_OF:
                child = ((InstanceOfTree)t).getExpression();
                break;
            
            // unary - postfix
            
            case POSTFIX_DECREMENT: case POSTFIX_INCREMENT:
                child = ((UnaryTree)t).getExpression();
                break;
                
            // ternary
            case CONDITIONAL_EXPRESSION:
                child = ((ConditionalExpressionTree)t).getCondition();
                break;
                
            case MEMBER_SELECT:
                child = ((MemberSelectTree)t).getExpression();
                break;
                
            case VARIABLE: {
                VariableTree varT = (VariableTree)t;
                if (varT.getModifiers() != null) {
                    child = varT.getModifiers();
                } else {
                    child = null;
                }
                break;
            }
            case METHOD: {
                MethodTree mT = (MethodTree)t;
                if (mT.getModifiers() != null) {
                    child = mT.getModifiers();
                } else {
                    child = null;
                }
                break;
            }
            case CLASS: {
                ClassTree cT = (ClassTree)t;
                if (cT.getModifiers() != null) {
                    child = cT.getModifiers();
                } else {
                    child = null;
                }
                break;
            }
                
            case MODIFIERS: {
                ModifiersTree modT = (ModifiersTree)t;
                List<? extends AnnotationTree> annTs = modT.getAnnotations();
                if (annTs == null || annTs.isEmpty()) {
                    return pos;
                }
                int modPos = ((JCTree)annTs.get(0)).pos;
                if (modPos < pos) {
                    child = annTs.get(0);
                } else {
                    child = null;
                }
                break;
            }
                
            default:
                return pos;
        }
        if (child != null) {
            Integer x = limitCache.get(t);
            if (x != null) {
                return x;
            }
            int l = setupLimit(child);
            limitCache.put(t, l);
            return l;
        } else {
            return pos;
        }
    }
    
    @Override
    public Void scan(Tree tree, Void p) {
        if (tree == null) {
            return null;
        } else {
            //XXX:
            boolean oldMapComments = mapComments;
            try {
                limit = -1;
                mapComments |= tree == commentMapTarget;
                if ((commentMapTarget != null) && info.getTreeUtilities().isSynthetic(new TreePath(new TreePath(unit), tree)))
                    return null;
                limit = setupLimit(tree);
                if (commentMapTarget != null) {
                    mapComments2(tree, true, false);
                }
                Tree oldParent = parent;
                try {
                    parent = tree;
                    super.scan(tree, p);
                } finally {
                    parent = oldParent;
                }
                if (commentMapTarget != null) {
                    mapComments2(tree, false, tree.getKind() != Tree.Kind.BLOCK || parent == null || parent.getKind() != Tree.Kind.METHOD);
                    if (mapComments) {
                        ((CommentSetImpl) createCommentSet(tree)).commentsMapped();
                    }
                }
                return null;
            } finally {
                mapComments = oldMapComments;
            }
        }
    }
        
    private void mapComments2(Tree tree, boolean preceding, boolean trailing) {
        if (((JCTree) tree).pos <= 0) {
            return;
        }
        collect(tree, preceding, trailing);
    }
    
    /*
        Implementation of new gathering algorithm based on comment weighting by natural (my) aligning of comments to statements.
     */
    
    private static Logger log = Logger.getLogger(AssignComments.class.getName());
    
    private void collect(Tree tree, boolean preceding, boolean trailing) {
        if (isEvil(tree)) {
            return;
        }
        if (preceding) {
            int pos = findInterestingStart((JCTree) tree);
            if (pos >= 0) {
                seq.move(pos);
                lookForPreceedings(seq, tree);
            } else {
                pos = ((JCTree)tree).pos;
                seq.move(pos);
                seq.moveNext();
            }
            if (tree instanceof BlockTree) {
                BlockTree blockTree = (BlockTree) tree;
                if (blockTree.getStatements().isEmpty()) {
                    lookWithinEmptyBlock(seq, blockTree);
                }
            } else if (tree instanceof ClassTree) {
                ClassTree clazz = (ClassTree)tree;
                int cnt = clazz.getMembers().size();
                if (cnt == 1) {
                    Tree mt = clazz.getMembers().get(0);
                    if (mt.getKind() == Tree.Kind.METHOD && info.getTreeUtilities().isSynthetic(new TreePath(new TreePath(unit), mt))) {
                        cnt--;
                    }
                }
                if (cnt == 0) {
                    // look within empty class/interface decls, too
                    int cPos = ((JCTree)tree).pos;
                    seq.move(cPos);
                    if (seq.moveNext()) {
                        lookWithinEmptyBlock(seq, clazz);
                    }
                }
            }
        } else {
            lookForInline(seq, tree);
            if (trailing) {
                lookForTrailing(seq, tree);
            }
        }

        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "T: " + tree + "\nC: " + commentService.getComments(tree));
        }
    }

    private void lookForInline(TokenSequence<JavaTokenId> seq, Tree tree) {
        seq.move((int) positions.getEndPosition(unit, tree));
        CommentsCollection result = new CommentsCollection();
        while (seq.moveNext()) {
            if (seq.token().id() == JavaTokenId.WHITESPACE) {
                if (numberOfNL(seq.token()) > 0) {
                    break;
                }
            } else if (isComment(seq.token().id())) {
                if (alreadySeenJavadoc(seq.token(), seq)) {
                    continue;
                }
                if (seq.index() > tokenIndexAlreadyAdded)
                    result.add(seq.token());
                tokenIndexAlreadyAdded = seq.index();
                if (seq.token().id() == JavaTokenId.LINE_COMMENT) {
                    break;
                }
            } else {
                break;
            }
        }
        attachComments(tree, result, CommentSet.RelativePosition.INLINE);
    }

    private void attachComments(Tree tree, CommentsCollection result, CommentSet.RelativePosition position) {
        if (!mapComments) {
            return;
        }
        if (result.isEmpty()) {
            return;
        }
        CommentSetImpl cs = commentService.getComments(tree);
        for (Token<JavaTokenId> token : result) {
            attachComment(position, cs, token);
        }
    }

    private boolean isEvil(Tree tree) {
        Tree.Kind kind = tree.getKind();
        switch (kind) {
            case COMPILATION_UNIT:
                CompilationUnitTree cut = (CompilationUnitTree) tree;
                return cut.getPackageName() == null;
//            case MODIFIERS:
            case PRIMITIVE_TYPE:
                return true;
            default: return false;
        }
    }
    
    private boolean parentEatsTralingComment(TokenSequence<JavaTokenId> seq, Tree t) {
        if (parent == null || lastWhiteNewline == -1) {
            return false;
        }
        int commentIndent = seq.offset() - lastWhiteNewline;
        Tree.Kind k = parent.getKind();
        boolean ok =  k == Tree.Kind.WHILE_LOOP || k == Tree.Kind.DO_WHILE_LOOP || k == Tree.Kind.IF;
        if (!ok) {
            return false;
        }
        // check indentation:
        int treeIndent = countIndent(seq, t);
        return (treeIndent < commentIndent);
    }
    
    private int countIndent(TokenSequence<JavaTokenId> seq, Tree tree) {
        int st = (int)positions.getStartPosition(unit, tree);
        int save = seq.offset();
        int nl = -1;
        seq.move(st);
        while (seq.movePrevious()) {
            Token<JavaTokenId> tukac = seq.token();
            if (tukac.id() != JavaTokenId.WHITESPACE) {
                if (tukac.id() == JavaTokenId.LINE_COMMENT) {
                    nl = seq.offset() + tukac.length();
                }
                break;
            }
            nl = tukac.text().toString().lastIndexOf('\n');
            if (nl != -1) {
                break;
            }
        }
        seq.move(save);
        if (!seq.moveNext()) {
            seq.movePrevious();
        }
        return nl == -1 ? -1 : st - nl;
    }
    
    private boolean alreadySeenJavadoc(Token<JavaTokenId> token, TokenSequence<JavaTokenId> seq) {
        return (token.id() == JavaTokenId.JAVADOC_COMMENT || token.id() == JavaTokenId.JAVADOC_COMMENT_LINE_RUN) &&
               (mixedJDocTokenIndexes.contains(seq.index()));
    }
    
    private void lookForTrailing(TokenSequence<JavaTokenId> seq, Tree tree) {
        //TODO: [RKo] This does not work correctly... need improvemetns.
        seq.move((int) positions.getEndPosition(unit, tree));
        List<TrailingCommentsDataHolder> comments = new LinkedList<TrailingCommentsDataHolder>();
        int maxLines = 0;
        int newlines = 0;
        int lastIndex = -1;
        while (seq.moveNext()) {
            if (lastIndex == (-1)) lastIndex = seq.index();
            Token<JavaTokenId> t = seq.token();
            if (t.id() == JavaTokenId.WHITESPACE) {
                int nls = numberOfNL(t, seq.offset());
                newlines += nls;
                // do not map trailing comments for statements enclosed in do/while/if
            } else if (isComment(t.id())) {
                // TODO: trailing javadocs should be ignored
                if (alreadySeenJavadoc(t, seq)) {
                    continue;
                }
                if (newlines > 0 && parentEatsTralingComment(seq, tree)) {
                    return;
                }
                if (seq.index() > tokenIndexAlreadyAdded) 
                    comments.add(new TrailingCommentsDataHolder(newlines, t, lastIndex));
                maxLines = Math.max(maxLines, newlines);
                if (t.id() == JavaTokenId.LINE_COMMENT) {
                    newlines = 1;
                    lastWhiteNewline = seq.offset() + t.length();
                } else {
                    newlines = 0;
                    lastWhiteNewline = -1;
                }
                lastIndex = -1;
            } else {
                if (t.id() == JavaTokenId.RBRACE || t.id() == JavaTokenId.ELSE) maxLines = Integer.MAX_VALUE;
                break;
            }

        }

        int index = seq.index() - 1;

        maxLines = Math.max(maxLines, newlines);

        for (TrailingCommentsDataHolder h : comments) {
            boolean assign = false;
            
            if (h.newlines < maxLines) {
                // if the comment is a javadoc one and there IS a valid declaration at seq.index(), the javadoc should
                // not be eaten although separated by many lines:
                assign = true;
                
                if (index >= 0 && maxLines < Integer.MAX_VALUE && seq.token().length() > 0 && (h.comment.id() == JavaTokenId.JAVADOC_COMMENT || h.comment.id() == JavaTokenId.JAVADOC_COMMENT_LINE_RUN)) {
                    TreePath tp = info.getTreeUtilities().pathFor(seq.offset() + 1);
                    // traverse up to last parent that claims the position
                    while (tp.getParentPath() != null && 
                           positions.getStartPosition(info.getCompilationUnit(), tp.getParentPath().getLeaf()) == seq.offset()) {
                        tp = tp.getParentPath();
                    }
                    if (tp != null && JAVADOC_KINDS.contains(tp.getLeaf().getKind())) {
                        assign = false;
                    }
                }
            }
            if (assign) {
                attachComments(Collections.singleton(h.comment), tree, CommentSet.RelativePosition.TRAILING);
            } else {
                index = h.index - 1;
                seq.moveIndex(h.index);
                break;
            }
        }
        
        tokenIndexAlreadyAdded = index;
    }

    private static final class TrailingCommentsDataHolder {
        private final int newlines;
        private final Token<JavaTokenId> comment;
        private final int index;
        public TrailingCommentsDataHolder(int newlines, Token<JavaTokenId> comment, int index) {
            this.newlines = newlines;
            this.comment = comment;
            this.index = index;
        }
    }

    private void lookWithinEmptyBlock(TokenSequence<JavaTokenId> seq, Tree tree) {
        // moving into opening brace.
        if (moveTo(seq, JavaTokenId.LBRACE, true)) {
            int idx = -1;
            if (seq.moveNext()) {
                JavaTokenId id = seq.token().id();
                idx = seq.index();
                if (id == JavaTokenId.WHITESPACE || isComment(id)) {
                    CommentsCollection cc = getCommentsCollection(seq, Integer.MAX_VALUE);
                    attachComments(tree, cc, CommentSet.RelativePosition.INNER);
                }
            }
            if (tokenIndexAlreadyAdded < idx) {
                tokenIndexAlreadyAdded = idx;
            }
        } else {
            int end = (int) positions.getEndPosition(unit, tree);
            seq.move(end); seq.moveNext();
        }
    }

    /**
     * Moves <code>seq</code> to first occurence of specified <code>toToken</code> if specified direction.
     * @param seq sequence of tokens
     * @param toToken token to stop on.
     * @param forward move forward if true, backward otherwise
     * @return true if token has been reached.
     */
    private boolean moveTo(TokenSequence<JavaTokenId> seq, JavaTokenId toToken, boolean forward) {
        if (seq.token() == null) return false;//seq.move(<end-of-stream>) might have been called - see test224577.
        do {
            if (toToken == seq.token().id()) {
                return true;
            } 
        } while (forward ? seq.moveNext() : seq.movePrevious());
        return false;
    }
    
    /**
     * Indexes for Javadoc tokens to skip. Sometimes javadocs can be mixed in between preceding 
     * tree constituents (e.g. between modifiers of a method). In that case, they will be 
     * assigned to the method before scanning continues to the modifier tree; tokenIndexAlreadyAdded
     * will not record these commenst scanned "in advance". This Set is a filter so they will
     * not be added again to nested constituents of the tree.
     */
    private Set<Integer>    mixedJDocTokenIndexes = new HashSet<>();
    
    private void lookForPreceedings(TokenSequence<JavaTokenId> seq, Tree tree) {
        int reset = ((JCTree) tree).pos;
        int plainCommentLimit = reset;
        if (limit >= 0) {
            // infix and postfix trees must not eat comments up to the symbol, otherwise
            // comments which belong to nested subtrees of operands could be eaten and incorrectly assigned.
            // Javadoc comments will be pushed upwards by individual nodes
            plainCommentLimit = Math.min(reset, limit);
        }
        CommentsCollection cc = null;
        while (seq.moveNext() && seq.offset() < plainCommentLimit) {
            JavaTokenId id = seq.token().id();
            if (isComment(id)) {
                if (cc == null) {
                    cc = getCommentsCollection(seq, Integer.MAX_VALUE);
                } else {
                    cc.merge(getCommentsCollection(seq, Integer.MAX_VALUE));
                }
            }
        }
        tokenIndexAlreadyAdded = seq.index();
        if (plainCommentLimit < reset && JAVADOC_KINDS.contains(tree.getKind())) {
            // look specifically for Javadocs

            // NOTE: local variables and method parameters will be affected by this, too - 
            // potential javadoc among their modifiers will be assigned to the variable,
            // rather than to the nearest modifier.
            int start = reset;
            int end = 0;
            CommentsCollection result = new CommentsCollection();
            while (seq.moveNext() && seq.offset() < reset) {
                JavaTokenId id = seq.token().id();
                if (id == JavaTokenId.JAVADOC_COMMENT || id == JavaTokenId.JAVADOC_COMMENT_LINE_RUN) {
                    mixedJDocTokenIndexes.add(seq.index());
                    start = Math.min(seq.offset(), start);
                    end = Math.max(seq.offset() + seq.token().length(), end);
                    result.add(seq.token());
                }
            }
            if (!result.isEmpty()) {
                if (cc == null) {
                    cc = result;
                } else {
                    cc.merge(result);
                }
            }
        }
        attachComments(cc, tree, CommentSet.RelativePosition.PRECEDING);
        seq.move(reset);
        seq.moveNext();
    }

    /**
     * Looking for position where to start looking up for preceeding commnets.
     * @param tree tree to examine.
     * @return position where to start 
     */
    private int findInterestingStart(JCTree tree) {
        int pos = (int) positions.getStartPosition(unit, tree);
        if (pos <= 0) return 0;
        seq.move(pos);
        boolean previousSucceeded;
        boolean ranOnce = false;
        while (previousSucceeded = seq.movePrevious() && tokenIndexAlreadyAdded < seq.index()) {
            ranOnce = true;
            switch (seq.token().id()) {
                case WHITESPACE:
                case LINE_COMMENT:
                case JAVADOC_COMMENT:
                case JAVADOC_COMMENT_LINE_RUN:
                case BLOCK_COMMENT:
                    continue;
                case LBRACE:
                    /*
                        we are reaching parent tree element. This tree has no siblings or is first child. We have no 
                        interest in number of NL before this kind of comments. This comments are always considered 
                        as preceeding to tree.
                    */
                    return seq.offset() + seq.token().length();
                default:
                    return seq.offset() + seq.token().length();
            }
        }
        if (!previousSucceeded && !ranOnce) return -1;
        return seq.offset() + (tokenIndexAlreadyAdded >= seq.index() ? seq.token().length() : 0);
    }

    private void attachComments(Iterable<? extends Token<JavaTokenId>> foundComments, Tree tree, CommentSet.RelativePosition positioning) {
        if (foundComments == null || !foundComments.iterator().hasNext() || !mapComments) return;
        CommentSetImpl set = (CommentSetImpl) createCommentSet(tree);
        if (set.areCommentsMapped()) return ;
        for (Token<JavaTokenId> comment : foundComments) {
            attachComment(positioning, set, comment);
        }
    }
    
    private void attachComment(CommentSet.RelativePosition positioning, CommentSet set, Token<JavaTokenId> comment) {
        Comment c = Comment.create(getStyle(comment.id()), comment.offset(null),
                getEndPos(comment), NOPOS, getText(comment));
        set.addComment(positioning, c);        
    }

    private String getText(Token<JavaTokenId> comment) {
        return String.valueOf(comment.text());
    }

    private int getEndPos(Token<JavaTokenId> comment) {
        return comment.offset(null) + comment.length();
    }

    private Comment.Style getStyle(JavaTokenId id) {
        switch (id) {
            case JAVADOC_COMMENT, JAVADOC_COMMENT_LINE_RUN:
                return Comment.Style.JAVADOC;
            case LINE_COMMENT:
                return Comment.Style.LINE;
            case BLOCK_COMMENT:
                return Comment.Style.BLOCK;
            default:
                return Comment.Style.WHITESPACE;
        }
    }
    
    private int lastWhiteNewline;

    private int numberOfNL(Token<JavaTokenId> t) {
        return numberOfNL(t, -1);
    }
    
    private int numberOfNL(Token<JavaTokenId> t, int offset) {
        int count = 0;
        CharSequence charSequence = t.text();
        for (int i = 0; i < charSequence.length(); i++) {
            char a = charSequence.charAt(i);
            if ('\n' == a) {
                lastWhiteNewline = offset + i;
                count++;
            }
        }
        if (offset == -1) {
            lastWhiteNewline = -1;
        }
        return count;
    }

    /**
     * Note - Because of {@link Comment.Style#WHITESPACE}, whitespaces are also
     * recorded
     */
    private CommentsCollection getCommentsCollection(TokenSequence<JavaTokenId> ts, int maxTension) {
        CommentsCollection result = new CommentsCollection();
        Token<JavaTokenId> t = ts.token();
        result.add(t);
        boolean isLC = t.id() == JavaTokenId.LINE_COMMENT;
        int lastCommentIndex = ts.index();
        int start = ts.offset();
        int end = ts.offset() + ts.token().length();
        while (ts.moveNext()) {
            if (ts.index() < tokenIndexAlreadyAdded) continue;
            t = ts.token();
            if (isComment(t.id())) {
                if ((t.id() == JavaTokenId.JAVADOC_COMMENT || t.id() == JavaTokenId.JAVADOC_COMMENT_LINE_RUN) &&
                    mixedJDocTokenIndexes.contains(ts.index())) {
                    // skip javadocs already added
                    continue;
                }
                result.add(t);
                start = Math.min(ts.offset(), start);
                end = Math.max(ts.offset() + t.length(), end);
                isLC = t.id() == JavaTokenId.LINE_COMMENT;
                lastCommentIndex = ts.index();                
            } else if (t.id() == JavaTokenId.WHITESPACE) {
                if ((numberOfNL(t) + (isLC ? 1 : 0)) > maxTension) {
                    break;
                }
            } else {
                break;                
            }
        }
        ts.moveIndex(lastCommentIndex);
        ts.moveNext();
        tokenIndexAlreadyAdded = ts.index();
        result.setBounds(new int[]{start, end});
//        System.out.println("tokenIndexAlreadyAdded = " + tokenIndexAlreadyAdded);
        return result;
    }

    private CommentSet createCommentSet(Tree lastTree) {
        return commentService.getComments(lastTree);
    }

    private boolean isComment(JavaTokenId tid) {
        switch (tid) {
            case LINE_COMMENT:
            case BLOCK_COMMENT:
            case JAVADOC_COMMENT:
            case JAVADOC_COMMENT_LINE_RUN:
                return true;
            default:
                return false;
        }
    }

    private static class CommentsCollection implements Iterable<Token<JavaTokenId>> {
        private final int[] bounds = {NOPOS, NOPOS};
        private final List<Token<JavaTokenId>> comments = new LinkedList<Token<JavaTokenId>>();

        void add(Token<JavaTokenId> comment) {
            comments.add(comment);
        }

        boolean isEmpty() {
            return comments.isEmpty();
        }

        public Iterator<Token<JavaTokenId>> iterator() {
            return comments.iterator();
        }

        void setBounds(int[] bounds) {
            this.bounds[0] = bounds[0];
            this.bounds[1] = bounds[1];
        }

        public int[] getBounds() {
            return bounds.clone();
        }

        public void merge(CommentsCollection cc) {
            comments.addAll(cc.comments);
            this.bounds[0] = Math.min(this.bounds[0], cc.bounds[0]);
            this.bounds[1] = Math.max(this.bounds[1], cc.bounds[1]);
        }
    }
}
