/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.editor.codegen;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;

import org.netbeans.api.editor.EditorActionNames;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.editor.overridden.PopupUtil;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Remove the enclosing parts of a nested statement.
 *
 * @author Dusan Balek
 */
@EditorActionRegistration(name = EditorActionNames.removeSurroundingCode,
                          mimeType = JavaKit.JAVA_MIME_TYPE,
                          menuPath = "Source",
                          menuPosition = 2280,
                          menuText = "#" + EditorActionNames.removeSurroundingCode + "_menu_text")
public class RemoveSurroundingCodeAction extends BaseAction {

    @Override
    public void actionPerformed(final ActionEvent evt, final JTextComponent component) {
        if (component == null || !component.isEditable() || !component.isEnabled()) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        final BaseDocument doc = (BaseDocument) component.getDocument();
        final JavaSource js = JavaSource.forDocument(doc);
        if (js != null) {
            final AtomicBoolean cancel = new AtomicBoolean();
            ProgressUtils.runOffEventDispatchThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        js.runUserActionTask(new Task<CompilationController>() {
                            @Override
                            public void run(final CompilationController controller) throws Exception {
                                try {
                                    if (cancel.get()) {
                                        return;
                                    }
                                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);

                                    if (cancel.get()) {
                                        return;
                                    }
                                    final TreeUtilities tu = controller.getTreeUtilities();
                                    final List<CodeDeleter> codeDeleters = new ArrayList<>();
                                    final int caretOffset = component.getCaretPosition();
                                    final TokenSequence<JavaTokenId> ts = controller.getTokenHierarchy().tokenSequence(JavaTokenId.language());
                                    ts.move(caretOffset);
                                    if (ts.moveNext()) {
                                        if (ts.token().id() == JavaTokenId.BLOCK_COMMENT || ts.token().id() == JavaTokenId.LINE_COMMENT) {
                                            codeDeleters.add(new CommentDeleter(component, ts));
                                        }
                                    }
                                    TreePath tp = tu.pathFor(caretOffset);
                                    while (tp != null) {
                                        final Tree leaf = tp.getLeaf();
                                        switch (leaf.getKind()) {
                                            case IF:
                                                if (insideElse(controller, (IfTree) leaf, component.getCaretPosition())) {
                                                    codeDeleters.add(new TreeDeleter(controller, component, tp, false));
                                                }
                                            case FOR_LOOP:
                                            case ENHANCED_FOR_LOOP:
                                            case WHILE_LOOP:
                                            case DO_WHILE_LOOP:
                                            case SYNCHRONIZED:
                                            case TRY:
                                                codeDeleters.add(new TreeDeleter(controller, component, tp));
                                                 break;
                                            case BLOCK:
                                                if (tp.getParentPath().getLeaf().getKind() == Tree.Kind.BLOCK) {
                                                    codeDeleters.add(new TreeDeleter(controller, component, tp));
                                                }
                                                break;
                                            case PARENTHESIZED:
                                                if (tp.getParentPath().getLeaf().getKind() != Tree.Kind.IF && !Utilities.containErrors(tp.getParentPath().getLeaf())) {
                                                    codeDeleters.add(new TreeDeleter(controller, component, tp));
                                                }
                                                break;
                                        }
                                        tp = tp.getParentPath();
                                    }

                                    if (codeDeleters.size() > 0) {
                                        SwingUtilities.invokeLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                int altHeight = -1;
                                                Point where = null;
                                                try {
                                                    Rectangle carretRectangle = component.modelToView(caretOffset);
                                                    altHeight = carretRectangle.height;
                                                    where = new Point(carretRectangle.x, carretRectangle.y + carretRectangle.height);
                                                    SwingUtilities.convertPointToScreen(where, component);
                                                } catch (BadLocationException ble) {
                                                }
                                                if (where == null) {
                                                    where = new Point(-1, -1);
                                                }
                                                PopupUtil.showPopup(new RemoveSurroundingCodePanel(component, codeDeleters), null, where.x, where.y, true, altHeight);
                                            }
                                        });
                                    } else {
                                        component.getToolkit().beep();
                                    }
                                } catch (IOException ioe) {
                                    component.getToolkit().beep();
                                }
                            }
                        }, true);
                    } catch (IOException ioe) {
                        component.getToolkit().beep();
                    }
                }
            }, getShortDescription(), cancel, false);
        }
    }

    private String getShortDescription() {
        String name = (String) getValue(Action.NAME);
        if (name != null) {
            try {
                return NbBundle.getMessage(RemoveSurroundingCodeAction.class, name);
            } catch (MissingResourceException mre) {
            }
        }
        return name;
    }

    private boolean insideElse(CompilationController controller, IfTree ifTree, int caretPosition) {
        if (ifTree.getElseStatement() == null) {
            return false;
        }
        SourcePositions sp = controller.getTrees().getSourcePositions();
        int end = (int) sp.getEndPosition(controller.getCompilationUnit(), ifTree.getThenStatement());
        return end > 0 && caretPosition > end;
    }

    private static final AttributeSet DELETE_HIGHLIGHT = AttributesUtilities.createImmutable(StyleConstants.Background, new Color(245, 245, 245), StyleConstants.Foreground, new Color(180, 180, 180));
    private static final AttributeSet REMAIN_HIGHLIGHT = AttributesUtilities.createImmutable(StyleConstants.Background, new Color(210, 240, 210));

    private static class TreeDeleter implements CodeDeleter {

        private JTextComponent component;
        private TreePathHandle tpHandle;
        private boolean unwrap;
        private OffsetsBag bag;

        private TreeDeleter(CompilationInfo cInfo, JTextComponent component, TreePath path) throws BadLocationException {
            this(cInfo, component, path, true);
        }

        private TreeDeleter(CompilationInfo cInfo, JTextComponent component, TreePath path, boolean unwrap) throws BadLocationException {
            this.component = component;
            this.tpHandle = TreePathHandle.create(path, cInfo);
            this.unwrap = unwrap;
            this.bag = createOffsetsBag(component, cInfo.getTreeUtilities(), cInfo.getTrees().getSourcePositions(), path);
        }

        @Override
        public String getDisplayName() {
            switch (tpHandle.getKind()) {
                case IF:
                    return unwrap ? "if (...) ..." : "else ..."; //NOI18N
                case FOR_LOOP:
                case ENHANCED_FOR_LOOP:
                    return "for (...) ..."; //NOI18N
                case WHILE_LOOP:
                    return "while (...) ..."; //NOI18N
                case DO_WHILE_LOOP:
                    return "do ... while(...)"; //NOI18N
                case SYNCHRONIZED:
                    return "synchronized (...) ..."; //NOI18N
                case TRY:
                    return "try ..."; //NOI18N
                case BLOCK:
                    return "{...}"; //NOI18N
                case PARENTHESIZED:
                    return "(...)"; //NOI18N
            }
            throw new IllegalStateException("Unsupported kind: " + tpHandle.getKind()); //NOI18N
        }

        @Override
        public void invoke() {
            JavaSource js = JavaSource.forDocument(component.getDocument());
            if (js != null) {
                try {
                    ModificationResult mr = js.runModificationTask(new Task<WorkingCopy>() {
                        @Override
                        public void run(WorkingCopy copy) throws IOException {
                            copy.toPhase(JavaSource.Phase.PARSED);
                            TreePath tp = tpHandle.resolve(copy);
                            if (tp != null) {
                                TreeMaker tm = copy.getTreeMaker();
                                TreeUtilities tu = copy.getTreeUtilities();
                                Tree tree = tp.getLeaf();
                                Tree parent = tp.getParentPath().getLeaf();
                                ArrayList<StatementTree> stats = new ArrayList<>();
                                List<Comment> trailingComments = null;
                                switch (tree.getKind()) {
                                    case IF:
                                        IfTree it = (IfTree) tree;
                                        if (unwrap) {
                                            addStat(it.getThenStatement(), stats);
                                        } else {
                                            addStat(tm.If(it.getCondition(), it.getThenStatement(), null), stats);
                                        }
                                        addStat(it.getElseStatement(), stats);
                                        trailingComments = getTrailingComments(tu, it.getElseStatement() != null ? it.getElseStatement() : it.getThenStatement());
                                        break;
                                    case FOR_LOOP:
                                        ForLoopTree flt = (ForLoopTree) tree;
                                        stats.addAll(flt.getInitializer());
                                        addStat(flt.getStatement(), stats);
                                        trailingComments = getTrailingComments(tu, flt.getStatement());
                                        break;
                                    case ENHANCED_FOR_LOOP:
                                        EnhancedForLoopTree eflt = (EnhancedForLoopTree) tree;
                                        VariableTree var = eflt.getVariable();
                                        stats.add(tm.Variable(var.getModifiers(), var.getName(), var.getType(), tm.Literal(null)));
                                        addStat(eflt.getStatement(), stats);
                                        trailingComments = getTrailingComments(tu, eflt.getStatement());
                                       break;
                                    case WHILE_LOOP:
                                        WhileLoopTree wlt = (WhileLoopTree) tree;
                                        addStat(wlt.getStatement(), stats);
                                        trailingComments = getTrailingComments(tu, wlt.getStatement());
                                        break;
                                    case DO_WHILE_LOOP:
                                        DoWhileLoopTree dwlt = (DoWhileLoopTree) tree;
                                        addStat(dwlt.getStatement(), stats);
                                        break;
                                    case SYNCHRONIZED:
                                        SynchronizedTree st = (SynchronizedTree) tree;
                                        addStat(st.getBlock(), stats);
                                        trailingComments = getTrailingComments(tu, st.getBlock());
                                        break;
                                    case TRY:
                                        TryTree tt = (TryTree) tree;
                                        for (Tree t : tt.getResources()) {
                                            addStat((StatementTree)t, stats);
                                        }
                                        addStat(tt.getBlock(), stats);
                                        addStat(tt.getFinallyBlock(), stats);
                                        trailingComments = getTrailingComments(tu, tt.getFinallyBlock() != null
                                                ? tt.getFinallyBlock() : tt.getCatches().isEmpty() ? tt.getBlock() : tt.getCatches().get(tt.getCatches().size() - 1));
                                        break;
                                    case BLOCK:
                                        BlockTree bt = (BlockTree) tree;
                                        addStat(bt, stats);
                                        break;
                                    case PARENTHESIZED:
                                        ParenthesizedTree pt = (ParenthesizedTree) tree;
                                        copy.rewrite(tree, pt.getExpression());
                                        return;
                                }
                                if (!stats.isEmpty()) {
                                    for (Comment comment : tu.getComments(tree, true)) {
                                        tm.addComment(stats.get(0), comment, true);
                                    }
                                    if (trailingComments == null) {
                                        trailingComments = tu.getComments(tree, false);
                                    }
                                    for (Comment comment : trailingComments) {
                                        tm.addComment(stats.get(stats.size() - 1), comment, false);
                                    }
                                }
                                if (parent.getKind() == Tree.Kind.BLOCK) {
                                    BlockTree block = (BlockTree) parent;
                                    int idx = -1;
                                    List<? extends StatementTree> blockStats = block.getStatements();
                                    for (int i = 0; i < blockStats.size(); i++) {
                                        if (tree == blockStats.get(i)) {
                                            idx = i;
                                            break;
                                        }
                                    }
                                    if (idx >= 0) {
                                        block = tm.removeBlockStatement(block, idx);
                                        for (int i = stats.size() - 1; i >= 0; i--) {
                                            block = tm.insertBlockStatement(block, idx, stats.get(i));
                                        }
                                    }
                                    copy.rewrite(parent, block);
                                } else {
                                    Tree newTree = stats.size() > 1 ? tm.Block(stats, false) : stats.size() == 1 ? stats.get(0) : null;
                                    copy.rewrite(tree, newTree);
                                }
                            }
                        }
                    });
                    GeneratorUtils.guardedCommit(component, mr);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        @Override
        public OffsetsBag getHighlight() {
            return bag;
        }
        
        private List<Comment> getTrailingComments(TreeUtilities tu, Tree tree) {
            return tree.getKind() == Tree.Kind.BLOCK ? tu.getComments(tree, false) : null;
        }

        private OffsetsBag createOffsetsBag(JTextComponent component, TreeUtilities tu, SourcePositions sp, TreePath path) throws BadLocationException {
            Document doc = component.getDocument();
            OffsetsBag offsetsBag = new OffsetsBag(doc, true);
            int start = (int) sp.getStartPosition(path.getCompilationUnit(), path.getLeaf());
            if (start >= 0) {
                List<int[]> positions = new ArrayList<>();
                Tree tree = path.getLeaf();
                switch (tree.getKind()) {
                    case IF:
                        IfTree it = (IfTree) tree;
                        if (unwrap) {
                            positions.add(getBounds(tu, sp, path.getCompilationUnit(), it.getThenStatement()));
                        } else {
                            start = (int) sp.getEndPosition(path.getCompilationUnit(), it.getThenStatement());
                            int end = (int) sp.getStartPosition(path.getCompilationUnit(), it.getElseStatement());
                            int off = doc.getText(start, end - start).indexOf("else"); //NOI18N
                            if (off > 0) {
                                start += off;
                            }
                        }
                        positions.add(getBounds(tu, sp, path.getCompilationUnit(), it.getElseStatement()));
                        break;
                    case FOR_LOOP:
                        ForLoopTree flt = (ForLoopTree) tree;
                        List<? extends StatementTree> inits = flt.getInitializer();
                        if (inits != null && !inits.isEmpty()) {
                            int[] bounds = {-1, -1};
                            bounds[0] = getStart(tu, sp, path.getCompilationUnit(), inits.get(0));
                            bounds[1] = getEnd(tu, sp, path.getCompilationUnit(), inits.get(inits.size() - 1));
                            positions.add(bounds);
                        }
                        positions.add(getBounds(tu, sp, path.getCompilationUnit(), flt.getStatement()));
                        break;
                    case ENHANCED_FOR_LOOP:
                        EnhancedForLoopTree eflt = (EnhancedForLoopTree) tree;
                        positions.add(getBounds(tu, sp, path.getCompilationUnit(), eflt.getVariable()));
                        positions.add(getBounds(tu, sp, path.getCompilationUnit(), eflt.getStatement()));
                        break;
                    case WHILE_LOOP:
                        WhileLoopTree wlt = (WhileLoopTree) tree;
                        positions.add(getBounds(tu, sp, path.getCompilationUnit(), wlt.getStatement()));
                        break;
                    case DO_WHILE_LOOP:
                        DoWhileLoopTree dwlt = (DoWhileLoopTree) tree;
                        positions.add(getBounds(tu, sp, path.getCompilationUnit(), dwlt.getStatement()));
                        break;
                    case SYNCHRONIZED:
                        SynchronizedTree st = (SynchronizedTree) tree;
                        positions.add(getBounds(tu, sp, path.getCompilationUnit(), st.getBlock()));
                        break;
                    case TRY:
                        TryTree tt = (TryTree) tree;
                        for (Tree t : tt.getResources()) {
                            positions.add(getBounds(tu, sp, path.getCompilationUnit(), ((StatementTree)t)));
                        }
                        positions.add(getBounds(tu, sp, path.getCompilationUnit(), tt.getBlock()));
                        positions.add(getBounds(tu, sp, path.getCompilationUnit(), tt.getFinallyBlock()));
                        break;
                    case BLOCK:
                        BlockTree bt = (BlockTree) tree;
                        positions.add(getBounds(tu, sp, path.getCompilationUnit(), bt));
                        break;
                    case PARENTHESIZED:
                        ParenthesizedTree pt = (ParenthesizedTree) tree;
                        positions.add(getBounds(tu, sp, path.getCompilationUnit(), pt.getExpression()));
                        break;
                }
                for (int[] bounds : positions) {
                    if (bounds[0] >= 0 && bounds[1] > bounds[0]) {
                        offsetsBag.addHighlight(start, bounds[0], DELETE_HIGHLIGHT);
                        offsetsBag.addHighlight(bounds[0], bounds[1], REMAIN_HIGHLIGHT);
                        start = bounds[1];
                    }
                }
                int end = (int) sp.getEndPosition(path.getCompilationUnit(), path.getLeaf());
                if (end > start) {
                    offsetsBag.addHighlight(start, end, DELETE_HIGHLIGHT);
                }
            }
            return offsetsBag;
        }

        private void addStat(StatementTree stat, List<StatementTree> to) {
            if (stat != null) {
                if (stat.getKind() == Tree.Kind.BLOCK) {
                    to.addAll(((BlockTree) stat).getStatements());
                } else {
                    to.add(stat);
                }
            }
        }

        private int[] getBounds(TreeUtilities tu, SourcePositions sp, CompilationUnitTree cut, Tree tree) {
            int[] bounds = {-1, -1};
            if (tree != null) {
                if (tree.getKind() == Tree.Kind.BLOCK) {
                    List<? extends StatementTree> stats = ((BlockTree) tree).getStatements();
                    if (stats != null && !stats.isEmpty()) {
                        bounds[0] = getStart(tu, sp, cut, stats.get(0));
                        bounds[1] = getEnd(tu, sp, cut, stats.get(stats.size() - 1));
                    }
                } else {
                    bounds[0] = getStart(tu, sp, cut, tree);
                    bounds[1] = getEnd(tu, sp, cut, tree);
                }
            }
            return bounds;
        }
        
        private int getStart(TreeUtilities tu, SourcePositions sp, CompilationUnitTree cut, Tree tree) {
            List<Comment> comments = tu.getComments(tree, true);
            return comments.isEmpty() ? (int) sp.getStartPosition(cut, tree) : comments.get(0).pos();
        }

        private int getEnd(TreeUtilities tu, SourcePositions sp, CompilationUnitTree cut, Tree tree) {
            List<Comment> comments = tu.getComments(tree, false);
            return comments.isEmpty() ? (int) sp.getEndPosition(cut, tree) : comments.get(comments.size() - 1).endPos();
        }
    }
    
    private static class CommentDeleter implements CodeDeleter {

        private final boolean lineComment;
        private final JTextComponent component;
        private final int offset;
        private final int length;
        private final OffsetsBag bag;

        public CommentDeleter(JTextComponent component, TokenSequence<JavaTokenId> ts) {
            this.lineComment = ts.token().id() == JavaTokenId.LINE_COMMENT;
            this.component = component;
            this.offset = ts.offset();
            this.length = ts.token().length();
            this.bag = new OffsetsBag(component.getDocument(), true);
            if (lineComment) {
                bag.addHighlight(offset, offset + 2, DELETE_HIGHLIGHT);
                bag.addHighlight(offset + 2, offset + length, REMAIN_HIGHLIGHT);
            } else {
                bag.addHighlight(offset, offset + 2, DELETE_HIGHLIGHT);
                bag.addHighlight(offset + 2, offset + length - 2, REMAIN_HIGHLIGHT);
                bag.addHighlight(offset + length -2, offset + length, DELETE_HIGHLIGHT);
            }
        }

        @Override
        public String getDisplayName() {
            return lineComment ? "// ..." : "/* ... */"; //NOI18N
        }

        @Override
        public void invoke() {
            Document doc = component.getDocument();
            TokenSequence<JavaTokenId> ts = TokenHierarchy.get(doc).tokenSequence(JavaTokenId.language());
            ts.move(component.getCaretPosition());
            if (ts.moveNext()) {
                if ((ts.token().id() == JavaTokenId.BLOCK_COMMENT || ts.token().id() == JavaTokenId.LINE_COMMENT)
                        && ts.offset() == offset && ts.token().length() == length) {
                    try {
                        if (!lineComment) {
                            doc.remove(offset + length - 2, 2);
                        }
                        doc.remove(offset, 2);
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }

        @Override
        public OffsetsBag getHighlight() {
            return bag;
        }        
    }    
}
