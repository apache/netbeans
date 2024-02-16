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
package org.netbeans.modules.java.source.pretty;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.LambdaExpressionTree.BodyKind;
import com.sun.source.tree.MemberReferenceTree.ReferenceMode;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.PatternTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import static com.sun.source.tree.Tree.*;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;

import com.sun.source.doctree.AttributeTree;
import com.sun.source.doctree.AttributeTree.ValueKind;
import com.sun.source.doctree.AuthorTree;
import com.sun.source.doctree.CommentTree;
import com.sun.source.doctree.DeprecatedTree;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocRootTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.DocTreeVisitor;
import com.sun.source.doctree.EndElementTree;
import com.sun.source.doctree.EntityTree;
import com.sun.source.doctree.ErroneousTree;
import com.sun.source.doctree.HiddenTree;
import com.sun.source.doctree.IdentifierTree;
import com.sun.source.doctree.IndexTree;
import com.sun.source.doctree.InheritDocTree;
import com.sun.source.doctree.LinkTree;
import com.sun.source.doctree.LiteralTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ProvidesTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.doctree.ReturnTree;
import com.sun.source.doctree.SeeTree;
import com.sun.source.doctree.SerialDataTree;
import com.sun.source.doctree.SerialFieldTree;
import com.sun.source.doctree.SerialTree;
import com.sun.source.doctree.SinceTree;
import com.sun.source.doctree.StartElementTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.doctree.ThrowsTree;
import com.sun.source.doctree.UnknownBlockTagTree;
import com.sun.source.doctree.UnknownInlineTagTree;
import com.sun.source.doctree.UsesTree;
import com.sun.source.doctree.ValueTree;
import com.sun.source.doctree.VersionTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.SwitchExpressionTree;

import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.*;
import static com.sun.tools.javac.code.Flags.*;
import com.sun.tools.javac.comp.Operators;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.DCTree;
import com.sun.tools.javac.tree.DCTree.DCReference;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Convert;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CodeStyle.*;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.Comment.Style;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.source.builder.CommentHandlerService;
import org.netbeans.modules.java.source.query.CommentHandler;
import org.netbeans.modules.java.source.query.CommentSet;
import org.netbeans.modules.java.source.builder.CommentSetImpl;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.save.CasualDiff;
import org.netbeans.modules.java.source.save.CasualDiff.StringTemplateFragmentTree;
import org.netbeans.modules.java.source.save.DiffContext;
import org.netbeans.modules.java.source.save.PositionEstimator;
import org.netbeans.modules.java.source.save.Reformatter;
import org.netbeans.modules.java.source.transform.FieldGroupTree;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.util.Exceptions;

/** Prints out a tree as an indented Java source program.
 */
public final class VeryPretty extends JCTree.Visitor implements DocTreeVisitor<Void, Void>, TrimBufferObserver {

    private static final char[] hex = "0123456789ABCDEF".toCharArray();
    private static final String REPLACEMENT = "%[a-z]*%";
    private static final String ERROR = "<error>"; //NOI18N

    private final CodeStyle cs;
    public  final CharBuffer out;

    private final Names names;
    private final CommentHandler commentHandler;
    private final Operators operators;
    private final WidthEstimator widthEstimator;
    private final DanglingElseChecker danglingElseChecker;

    /**
     * Suppresses printing of variable type. Used when printing parameters for IMPLICIT-param lambdas
     */
    public boolean suppressVariableType;
    public JCClassDecl enclClass; // the enclosing class.
    private int indentSize;
    private int prec; // visitor argument: the current precedence level.
    private boolean printingMethodParams;
    private DiffContext diffContext;
    private CommentHandlerService comments;

    private int fromOffset = -1;
    private int toOffset = -1;
    private boolean insideAnnotation = false;

    private final Map<Tree, ?> tree2Tag;
    private final Map<Tree, DocCommentTree> tree2Doc;
    private final Map<Object, int[]> tag2Span;
    private final String origText;
    private int initialOffset = 0;

    public VeryPretty(DiffContext diffContext) {
        this(diffContext, diffContext.style, null, null, null, null);
    }

    public VeryPretty(DiffContext diffContext, CodeStyle cs) {
        this(diffContext, cs, null, null, null, null);
    }

    public VeryPretty(DiffContext diffContext, CodeStyle cs, Map<Tree, ?> tree2Tag, Map<Tree, DocCommentTree> tree2Doc, Map<?, int[]> tag2Span, String origText) {
        this(diffContext.context, cs, tree2Tag, tree2Doc, tag2Span, origText);
        this.diffContext = diffContext;
    }

    public VeryPretty(DiffContext diffContext, CodeStyle cs, Map<Tree, ?> tree2Tag, Map<Tree, DocCommentTree> tree2Doc, Map<?, int[]> tag2Span, String origText, int initialOffset) {
        this(diffContext, cs, tree2Tag, tree2Doc, tag2Span, origText);
        this.initialOffset = initialOffset; //provide intial offset of this priter
    }

    private VeryPretty(Context context, CodeStyle cs, Map<Tree, ?> tree2Tag, Map<Tree, DocCommentTree> tree2Doc, Map<?, int[]> tag2Span, String origText) {
	names = Names.instance(context);
	enclClass = null;
        commentHandler = CommentHandlerService.instance(context);
        operators = Operators.instance(context);
	widthEstimator = new WidthEstimator(context);
        danglingElseChecker = new DanglingElseChecker();
        prec = TreeInfo.notExpression;
        this.cs = cs;
        out = new CharBuffer(cs.getRightMargin(), cs.getTabSize(), cs.expandTabToSpaces());
        out.addTrimObserver(this);
        this.indentSize = cs.getIndentSize();
        this.tree2Tag = tree2Tag;
        this.tree2Doc = tree2Doc == null ? Collections.<Tree, DocCommentTree>emptyMap(): tree2Doc;
        this.tag2Span = (Map<Object, int[]>) tag2Span;//XXX
        this.origText = origText;
        this.comments = CommentHandlerService.instance(context);
    }

    public void setInitialOffset(int offset) {
        initialOffset = offset < 0 ? 0 : offset;
    }

    public int getInitialOffset() {
        return initialOffset;
    }

    @Override
    public String toString() {
	return out.toString();
    }

    public void toLeftMargin() {
	out.toLeftMargin();
    }

    public void reset(int margin, int col) {
	out.setLength(0);
	out.leftMargin = margin;
        out.col = col;
    }

    public int getIndent() {
        return out.leftMargin;
    }
    
    public void setIndent(int indent) {
        out.leftMargin = indent;
    }

    /** Increase left margin by indentation width.
     */
    public int indent() {
	int old = out.leftMargin;
	out.leftMargin = old + indentSize;
	return old;
    }

    public void undent(int old) {
	out.leftMargin = old;
    }

    public void newline() {
	out.nlTerm();
    }

    private void newLineNoTrim() {
        out.nlTermNoTrim();
    }

    public void blankline() {
        out.blanklines(1);
    }

    public int setPrec(int prec) {
        int old = this.prec;
        this.prec = prec;
        return old;
    }

    public final void print(String s) {
	if (s == null)
	    return;
        out.append(s);
    }           

    public final void print(Name n) {
        if (n == null)
            return;
	out.append(n.toString());
    }
    
    private void print(javax.lang.model.element.Name n) {
        if (n == null)
            return;
        print(n.toString());
    }

    public void print(JCTree t) {
        if (t == null) return;
        blankLines(t, true);
        toLeftMargin();
        doAccept(t, true);
        blankLines(t, false);
    }
    
    public void print(DCTree t) {
        print(t, false);
    }
    
    public void print(DCTree t, boolean noMarginAfter) {
        if (t == null) return;
        blankLines(t, true, false);
        toLeftMargin();
        doAccept(t);
        blankLines(t, false, noMarginAfter);
    }
    
    private Map<JCTree, Integer> overrideStartPositions;

    private int getOldPos(JCTree oldT) {
        if (overrideStartPositions != null) {
            Integer i = overrideStartPositions.get(oldT);
            if (i != null) {
                return i;
            }
        }
        return TreeInfo.getStartPos(oldT);
    }
    public int endPos(JCTree t) {
        return TreeInfo.getEndPos(t, diffContext.origUnit.endPositions);
    }
    
    private java.util.List<? extends StatementTree> getStatements(Tree tree) {
        switch (tree.getKind()) {
            case BLOCK: return ((BlockTree) tree).getStatements();
            case CASE: return ((CaseTree) tree).getStatements();
            default: return null;
        }
    }
    
    private java.util.List<JCVariableDecl> printOriginalPartOfFieldGroup(FieldGroupTree fgt) {
        java.util.List<JCVariableDecl> variables = fgt.getVariables();
        TreePath tp = TreePath.getPath(diffContext.origUnit, variables.get(0));
        TreePath parent = tp != null ? tp.getParentPath() : null;
            
        if (parent == null) return variables;
            
        java.util.List<? extends StatementTree> statements = getStatements(parent.getLeaf());
        
        if (statements == null) return variables;
        JCVariableDecl firstDecl = fgt.getVariables().get(0);
        int startIndex = statements.indexOf(firstDecl);
            
        if (startIndex < 0) return variables; //XXX: should not happen
        
        int origCount = 0;
        int s = statements.size();
        for (JCTree t : variables) {
            if (startIndex >= s || statements.get(startIndex++) != t) break;
            origCount++;
        }
        
        if (origCount < 2) return variables;
        
        int firstPos = getOldPos(firstDecl);
        int groupStart = startIndex;
        // find the start of the field group among the original statement. The fieldgroup is detected using
        // positions from the TreeInfo; all fieldgroup members have the same starting position.
        while (groupStart > 0) {
            Tree t = statements.get(groupStart - 1);
            if (!(t instanceof JCVariableDecl)) {
                break;
            }
            if (getOldPos((JCVariableDecl)t) != firstPos) {
                break;
            }
            groupStart--;
        }
        int firstIdentStart = ((JCTree)statements.get(groupStart)).pos;
        if (groupStart < startIndex) {
            // must print the declaration part up to the 1st variable name (VarDecl.pos from JCTree), doPrintOriginalTrees
            // would include the whole region including the members present in the original FieldGroup
            copyToIndented(firstPos, firstIdentStart);

            Map<JCTree, Integer> m = new IdentityHashMap<>(origCount);
            for (int i = 0; i < origCount; i++) {
                m.put(variables.get(i), variables.get(i).pos);
            }
            overrideStartPositions = m;
        }
        doPrintOriginalTree(variables.subList(0, origCount), true);
        overrideStartPositions = null;
        return variables.subList(origCount, variables.size());
    }
    
    public Set<Tree> oldTrees = Collections.emptySet();
    public SortedSet<int[]> reindentRegions = new TreeSet<>(new Comparator<int[]>() {
        @Override public int compare(int[] o1, int[] o2) {
            return o1[0] - o2[0];
        }
    });
    private boolean commentsEnabled;
    private final Set<Tree> trailingCommentsHandled = Collections.newSetFromMap(new IdentityHashMap<Tree, Boolean>());
    private final Set<Tree> innerCommentsHandled = Collections.newSetFromMap(new IdentityHashMap<Tree, Boolean>());
    private void doAccept(JCTree t, boolean printComments/*XXX: should ideally always print comments?*/) {
        if (!handlePossibleOldTrees(Collections.singletonList(t), printComments)) {
            if (printComments) printPrecedingComments(t, true);
            
            int start = out.length();

            if (t instanceof FieldGroupTree) {
                //XXX: should be able to use handlePossibleOldTrees over the individual variables:
                FieldGroupTree fgt = (FieldGroupTree) t;
                if (fgt.isEnum()) {
                    printEnumConstants(List.from(fgt.getVariables().toArray(new JCTree[0])), !fgt.isEnum() || fgt.moreElementsFollowEnum(), printComments);
                } else {
                    java.util.List<JCVariableDecl> remainder = printOriginalPartOfFieldGroup(fgt);
                    
                    //XXX: this will unroll the field group (see FieldGroupTest.testMove187766)
                    //XXX: copied from visitClassDef
                    boolean firstMember = remainder.size() == fgt.getVariables().size();
                    for (JCVariableDecl var : remainder) {
                        oldTrees.remove(var);
                        assert !isEnumerator(var);
                        assert !isSynthetic(var);
                        printStat(var, true, firstMember, true, true, printComments);
                        firstMember = false;
                    }
                }
            } else {
                boolean saveComments = this.commentsEnabled;
                this.commentsEnabled = printComments;
                t.accept(this);
                this.commentsEnabled = saveComments;
            }

            int end = out.length();

            Object tag = tree2Tag != null ? tree2Tag.get(t) : null;

            if (tag != null) {
                tag2Span.put(tag, new int[]{start + initialOffset, end + initialOffset});
            }
        
            if (printComments) {
                printInnerCommentsAsTrailing(t, true);
                printTrailingComments(t, true);
            }
        }
    }
    
    private void doAccept(DCTree t) {
//        int start = toString().length();

//        if (!handlePossibleOldTrees(Collections.singletonList(t), false)) {
            t.accept(this, null);
//        }

//        int end = toString().length();

//        System.err.println("t: " + t);
//        System.err.println("thr=" + System.identityHashCode(t));
//        Object tag = tree2Tag != null ? tree2Tag.get(t) : null;
//
//        if (tag != null) {
//            tag2Span.put(tag, new int[]{start + initialOffset, end + initialOffset});
//        }
    }

    public boolean handlePossibleOldTrees(java.util.List<? extends JCTree> toPrint, boolean includeComments) {
        for (JCTree t : toPrint) {
            if (!oldTrees.contains(t)) return false;
            if (t.getKind() == Kind.ARRAY_TYPE) {
                return false;//XXX #197584: C-like array are cannot be copied as old trees.
            }
            CommentSet cs = commentHandler.getComments(t);
            if (cs.hasChanges()) return false;
        }
        
        if (toPrint.size() > 1) {
            //verify that all the toPrint trees belong to the same parent, and appear
            //in the same uninterrupted order under that parent:
            TreePath tp = TreePath.getPath(diffContext.mainUnit, toPrint.get(0));
            TreePath parent = tp.getParentPath();
            
            if (parent == null) return false; //XXX: should not happen, right?
            
            java.util.List<? extends StatementTree> statements = getStatements(parent.getLeaf());
            
            if (statements == null) return false;
            
            int startIndex = statements.indexOf(toPrint.get(0));
            
            if (startIndex < 0) return false; //XXX: should not happen
            
            for (JCTree t : toPrint) {
                if (statements.get(startIndex++) != t) return false;
            }
        }
        
        doPrintOriginalTree(toPrint, includeComments);
        
        return true;
    }
    
    private void doPrintOriginalTree(java.util.List<? extends JCTree> toPrint, final boolean includeComments) {
        if (out.isWhitespaceLine()) toLeftMargin();
        
        JCTree firstTree = toPrint.get(0);
        JCTree lastTree = toPrint.get(toPrint.size() - 1);

        CommentSet old = commentHandler.getComments(firstTree);
        final int realStart;

        //XXX hack:
        if (includeComments) {
            realStart = Math.min(getOldPos(firstTree), CasualDiff.commentStart(diffContext, old, CommentSet.RelativePosition.PRECEDING, getOldPos(firstTree)));
        } else {
            realStart = getOldPos(firstTree);
        }
        
        final int newStart = out.length() + initialOffset;

        final int[] realEnd = {endPos(lastTree)};
        new ErrorAwareTreeScanner<Void, Void>() {
            @Override
            public Void scan(Tree node, Void p) {
                if (node != null) {
                    CommentSetImpl old = comments.getComments(node);
                    if (includeComments) {
                        realEnd[0] = Math.max(realEnd[0], Math.max(CasualDiff.commentEnd(old, CommentSet.RelativePosition.INLINE), CasualDiff.commentEnd(old, CommentSet.RelativePosition.TRAILING)));
                        trailingCommentsHandled.add(node);
                    }
                    
                    Object tag = tree2Tag != null ? tree2Tag.get(node) : null;

                    if (tag != null) {
                        int s = getOldPos((JCTree) node);
                        int e = endPos((JCTree) node);
                        tag2Span.put(tag, new int[]{s - realStart + newStart, e - realStart + newStart});
                    }
                    
                }
                return super.scan(node, p);
            }
        }.scan(lastTree, null);

        //XXX: handle comments!
        copyToIndented(realStart, realEnd[0]);
    }

    private static final Logger LOG = Logger.getLogger(CasualDiff.class.getName());
    private void copyToIndented(int from, int to) {
        if (from == to) {
            return;
        } else if (from > to || from < 0 || to < 0) {
            // #104107 - log the source when this problem occurs.
            LOG.log(Level.INFO, "-----\n" + origText + "-----\n");
            LOG.log(Level.INFO, "Illegal values: from = " + from + "; to = " + to + "." +
                "Please, attach your messages.log to new issue!");
            if (to >= 0)
                eatChars(from-to);
            return;
        } else if (to > origText.length()) {
            // #99333, #97801: Debug message for the issues.
            LOG.severe("-----\n" + origText + "-----\n");
            throw new IllegalArgumentException("Copying to " + to + " is greater then its size (" + origText.length() + ").");
        }

        String text = origText.substring(from, to);
        
        int newLine = text.indexOf("\n") + 1;
        boolean wasWhitespaceLine = out.isWhitespaceLine();
        
        if (newLine == 0 && !wasWhitespaceLine) {
            print(text);
        } else {
            int start = out.length();
            print(text);
            int end = start + text.length();

            reindentRegions.add(new int[] {initialOffset + start + (wasWhitespaceLine ? 0 : newLine), initialOffset + end});
        }
    }

    /**
     * Adjusts {@link #reindentRegions} if the char buffer conntents is
     * trimmed.
     * 
     * @param limit 
     */
    @Override
    public void trimmed(int limit) {
        SortedSet<int[]> s = reindentRegions;
        while (!s.isEmpty()) {
            int[] reg = s.last();
            if (reg[1] <= initialOffset + limit) {
                break;
            }
            if (reg[0] >= initialOffset + limit) {
                // the region should be removed
                s.remove(reg);
            } else {
                reg[1] = initialOffset + limit;
                break;
            }
        }
    }
    

    /** Print a package declaration.
     */
    public void printPackage(JCExpression pid) {
        if (pid != null) {
            blankLines(cs.getBlankLinesBeforePackage());
            print("package ");
            printExpr(pid);
            print(';');
            blankLines(cs.getBlankLinesAfterPackage());
        }
    }

    public static final String ANNOTATIONS = "%annotations%"; //NOI18N
    public static final String NAME = "%name%"; //NOI18N
    public static final String TYPE = "%type%"; //NOI18N
    public static final String THROWS = "%throws%"; //NOI18N
    public static final String IMPLEMENTS = "%implements%"; //NOI18N
    public static final String EXTENDS = "%extends%"; //NOI18N
    public static final String TYPEPARAMETERS = "%typeparameters%"; //NOI18N
    public static final String FLAGS = "%flags%"; //NOI18N
    public static final String PARAMETERS = "%parameters%"; //NOI18N

    public String getMethodHeader(MethodTree t, String s) {
        JCMethodDecl tree = (JCMethodDecl) t;
        printAnnotations(tree.mods.annotations);
        s = replace(s, ANNOTATIONS);
        printFlags(tree.mods.flags);
        s = replace(s, FLAGS);
        if (tree.name == names.init) {
            print(enclClass.name);
            s = replace(s, NAME);
        } else {
            if (tree.typarams != null) {
                printTypeParameters(tree.typarams);
                needSpace();
                s = replace(s, TYPEPARAMETERS);
            }
            print(tree.restype);
            s = replace(s, TYPE);
            out.clear();
            print(tree.name);
            s = replace(s, NAME);
        }
        print('(');
        wrapTrees(tree.params, WrapStyle.WRAP_NEVER, out.col);
        print(')');
        s = replace(s, PARAMETERS);
        if (tree.thrown.nonEmpty()) {
            print(" throws ");
            wrapTrees(tree.thrown, WrapStyle.WRAP_NEVER, out.col);
            s = replace(s, THROWS);
        }
        return s.replaceAll(REPLACEMENT,"");
    }

    public String getClassHeader(ClassTree t, String s) {
        JCClassDecl tree = (JCClassDecl) t;
        printAnnotations(tree.mods.annotations);
        s = replace(s, ANNOTATIONS);
        long flags = tree.mods.flags;
        if ((flags & ENUM) != 0)
            printFlags(flags & ~(INTERFACE | FINAL));
        else
            printFlags(flags & ~(INTERFACE | ABSTRACT));
        s = replace(s, FLAGS);
        if ((flags & INTERFACE) != 0) {
            print("interface ");
            print(tree.name);
            s = replace(s, NAME);
            printTypeParameters(tree.typarams);
            s = replace(s, TYPEPARAMETERS);
            if (tree.implementing.nonEmpty()) {
                print(" extends ");
                wrapTrees(tree.implementing, WrapStyle.WRAP_NEVER, out.col);
                s = replace(s, EXTENDS);
            }
        } else {
            if ((flags & ENUM) != 0)
                print("enum ");
            else {
                if ((flags & ABSTRACT) != 0)
                    print("abstract ");
                print("class ");
            }
            print(tree.name);
            s = replace(s, NAME);
            printTypeParameters(tree.typarams);
            s = replace(s, TYPEPARAMETERS);
            if (tree.extending != null) {
                print(" extends ");
                print(tree.extending);
                s = replace(s, EXTENDS);
            }
            if (tree.implementing.nonEmpty()) {
                print(" implements ");
                wrapTrees(tree.implementing, WrapStyle.WRAP_NEVER, out.col);
                s = replace(s, IMPLEMENTS);
            }
        }
        return s.replaceAll(REPLACEMENT,"");
    }

    public String getVariableHeader(VariableTree t, String s) {
        JCVariableDecl tree = (JCVariableDecl) t;
        printAnnotations(tree.mods.annotations);
        s = replace(s, ANNOTATIONS);
	printFlags(tree.mods.flags);
        s = replace(s, FLAGS);
	print(tree.vartype);
        s = replace(s, TYPE);
	needSpace();
	print(tree.name);
        s = replace(s, NAME);
        return s.replaceAll(REPLACEMENT,"");
    }

    /**************************************************************************
     * Visitor methods
     *************************************************************************/

    @Override
    public void visitTopLevel(JCCompilationUnit tree) {
        List<JCTree> l = tree.defs;
        if (l.head.hasTag(Tag.PACKAGEDEF)) {
            print(l.head);
            l = l.tail;
        }
        ArrayList<JCImport> imports = new ArrayList<JCImport>();
        while (l.nonEmpty() && l.head.getTag() == JCTree.Tag.IMPORT){
            imports.add((JCImport) l.head);
            l = l.tail;
        }
        printImportsBlock(imports, !l.isEmpty());
	while (l.nonEmpty()) {
            printStat(l.head, true, false, false, true, false);
            l = l.tail;
	}
    }

    @Override
    public void visitModuleDef(JCModuleDecl tree) {
	toLeftMargin();
        printAnnotations(tree.mods.annotations);
        if (tree.getModuleType() == ModuleTree.ModuleKind.OPEN) {
            print("open ");
        }
        print("module ");
        print(fullName(tree.qualId));
	int old = cs.indentTopLevelClassMembers() ? indent() : out.leftMargin;
	int bcol = old;
        switch(cs.getModuleDeclBracePlacement()) {
        case NEW_LINE:
            newline();
            toColExactly(old);
            break;
        case NEW_LINE_HALF_INDENTED:
            newline();
	    bcol += (indentSize >> 1);
            toColExactly(bcol);
            break;
        case NEW_LINE_INDENTED:
            newline();
	    bcol = out.leftMargin;
            toColExactly(bcol);
            break;
        }
        if (cs.spaceBeforeModuleDeclLeftBrace())
            needSpace();
	print('{');
        printInnerCommentsAsTrailing(tree, true);
	if (!tree.directives.isEmpty()) {
	    blankLines(cs.getBlankLinesAfterModuleHeader());
            boolean firstDirective = true;
            for (JCTree t : tree.directives) {
                printStat(t, true, firstDirective, true, true, false);
                firstDirective = false;
            }
	    blankLines(cs.getBlankLinesBeforeModuleClosingBrace());
        } else {
            printEmptyBlockComments(tree, false);
        }
        toColExactly(bcol);
	undent(old);
	print('}');
    }

    @Override
    public void visitExports(JCExports tree) {
        print("exports ");
        print(fullName(tree.qualid));
        if (tree.moduleNames.nonEmpty()) {
            wrap("to ", cs.wrapExportsToKeyword());
            wrapTrees(tree.moduleNames, cs.wrapExportsToList(), cs.alignMultilineExports()
                    ? out.col : out.leftMargin + cs.getContinuationIndentSize());
        }
        print(';');
    }

    @Override
    public void visitOpens(JCOpens tree) {
        print("opens ");
        print(fullName(tree.qualid));
        if (tree.moduleNames.nonEmpty()) {
            wrap("to ", cs.wrapOpensToKeyword());
            wrapTrees(tree.moduleNames, cs.wrapOpensToList(), cs.alignMultilineOpens()
                    ? out.col : out.leftMargin + cs.getContinuationIndentSize());
        }
        print(';');
    }

    @Override
    public void visitRequires(JCRequires tree) {
        print("requires ");
        if (tree.isStaticPhase)
            print("static ");
        if (tree.isTransitive)
            print("transitive ");
        print(fullName(tree.moduleName));
        print(';');
    }

    @Override
    public void visitProvides(JCProvides tree) {
        print("provides ");
        print(fullName(tree.serviceName));
        if (tree.implNames.nonEmpty()) {
            wrap("with ", cs.wrapProvidesWithKeyword());
            wrapTrees(tree.implNames, cs.wrapProvidesWithList(), cs.alignMultilineProvides()
                    ? out.col : out.leftMargin + cs.getContinuationIndentSize());
        }
        print(';');
    }

    @Override
    public void visitUses(JCUses tree) {
        print("uses ");
        print(fullName(tree.qualid));
        print(';');
    }

    @Override
    public void visitPackageDef(JCPackageDecl tree) {
        if (tree != null) {
            printAnnotations(tree.getAnnotations());
            printPackage(tree.pid);
        }
    }
    
    @Override
    public void visitImport(JCImport tree) {
        print("import ");
        if (tree.staticImport)
            print("static ");
        print(fullName(tree.qualid));
        print(';');
    }

    @Override
    public void visitClassDef(JCClassDecl tree) {
        JCClassDecl enclClassPrev = enclClass;
	enclClass = tree;
	toLeftMargin();
        printAnnotations(tree.mods.annotations);
	long flags = tree.mods.flags;
	if ((flags & ENUM) != 0)
	    printFlags(flags & ~(INTERFACE | FINAL));
	else
	    printFlags(flags & ~(INTERFACE | ABSTRACT));
	if ((flags & INTERFACE) != 0 || (flags & ANNOTATION) != 0) {
            if ((flags & ANNOTATION) != 0) print('@');
	    print("interface ");
	    print(tree.name);
	    printTypeParameters(tree.typarams);
	    if (tree.implementing.nonEmpty()) {
                wrap("extends ", cs.wrapExtendsImplementsKeyword());
		wrapTrees(tree.implementing, cs.wrapExtendsImplementsList(), cs.alignMultilineImplements()
                        ? out.col : out.leftMargin + cs.getContinuationIndentSize());
	    }
	} else {
	    if ((flags & ENUM) != 0)
		print("enum ");
	    else {
		if ((flags & ABSTRACT) != 0)
		    print("abstract ");
		print("class ");
	    }
	    print(tree.name);
	    printTypeParameters(tree.typarams);
	    if (tree.extending != null) {
                wrap("extends ", cs.wrapExtendsImplementsKeyword());
		print(tree.extending);
	    }
	    if (tree.implementing.nonEmpty()) {
                wrap("implements ", cs.wrapExtendsImplementsKeyword());
		wrapTrees(tree.implementing, cs.wrapExtendsImplementsList(), cs.alignMultilineImplements()
                        ? out.col : out.leftMargin + cs.getContinuationIndentSize());
	    }
	}
	int old = cs.indentTopLevelClassMembers() ? indent() : out.leftMargin;
	int bcol = old;
        switch(cs.getClassDeclBracePlacement()) {
        case NEW_LINE:
            newline();
            toColExactly(old);
            break;
        case NEW_LINE_HALF_INDENTED:
            newline();
	    bcol += (indentSize >> 1);
            toColExactly(bcol);
            break;
        case NEW_LINE_INDENTED:
            newline();
	    bcol = out.leftMargin;
            toColExactly(bcol);
            break;
        }
        if (cs.spaceBeforeClassDeclLeftBrace())
            needSpace();
	print('{');
        printInnerCommentsAsTrailing(tree, true);
        java.util.List<JCTree> members = CasualDiff.filterHidden(diffContext, tree.defs);
	if (!members.isEmpty()) {
	    blankLines(enclClass.name.isEmpty() ? cs.getBlankLinesAfterAnonymousClassHeader() : (flags & ENUM) != 0 ? cs.getBlankLinesAfterEnumHeader() : cs.getBlankLinesAfterClassHeader());
            boolean firstMember = true;
            for (JCTree t : members) {
                printStat(t, true, firstMember, true, true, false);
                firstMember = false;
            }
	    blankLines(enclClass.name.isEmpty() ? cs.getBlankLinesBeforeAnonymousClassClosingBrace() : (flags & ENUM) != 0 ? cs.getBlankLinesBeforeEnumClosingBrace() : cs.getBlankLinesBeforeClassClosingBrace());
        } else {
            printEmptyBlockComments(tree, false);
        }
        toColExactly(bcol);
	undent(old);
	print('}');
	enclClass = enclClassPrev;
    }

    private void printEnumConstants(java.util.List<? extends JCTree> defs, boolean forceSemicolon, boolean printComments) {
        boolean first = true;
        boolean hasNonEnumerator = false;
        for (JCTree c : defs) {
            if (isEnumerator(c)) {
                boolean col = false;
                if (first) {
                    col = true;
                    first = false;
                } else {
                    print(cs.spaceBeforeComma() ? " ," : ",");
                    switch(cs.wrapEnumConstants()) {
                    case WRAP_IF_LONG:
                        int rm = cs.getRightMargin();
                        if (widthEstimator.estimateWidth(c, rm - out.col) + out.col + 1 <= rm) {
                            if (cs.spaceAfterComma())
                                print(' ');
                            break;
                        }
                    case WRAP_ALWAYS:
                        newline();
                        col = true;
                        break;
                    case WRAP_NEVER:
                        if (cs.spaceAfterComma())
                            print(' ');
                        break;
                    }
                }
                printStat(c, true, false, col, false, printComments);
            } else if (!isSynthetic(c)) {
                hasNonEnumerator = true;
            }
        }
        if (hasNonEnumerator || forceSemicolon) {
            print(";");
            newline();
        }
    }

    @Override
    public void visitMethodDef(JCMethodDecl tree) {
	if ((tree.mods.flags & Flags.SYNTHETIC)==0 &&
		tree.name != names.init ||
		enclClass != null) {
	    JCClassDecl enclClassPrev = enclClass;
	    enclClass = null;
            printAnnotations(tree.mods.annotations);
            printFlags(tree.mods.flags);
            if (tree.typarams != null) {
                printTypeParameters(tree.typarams);
                needSpace();
            }
            if (tree.name == names.init || tree.name.contentEquals(enclClassPrev.name)) {
                print(enclClassPrev.name);
            } else {
                print(tree.restype);
                needSpace();
                print(tree.name);
            }
            print(cs.spaceBeforeMethodDeclParen() ? " (" : "(");
            if (cs.spaceWithinMethodDeclParens() && tree.params.nonEmpty())
                print(' ');
            boolean oldPrintingMethodParams = printingMethodParams;
            printingMethodParams = true;
            wrapTrees(tree.params, cs.wrapMethodParams(), cs.alignMultilineMethodParams()
                    ? out.col : out.leftMargin + cs.getContinuationIndentSize(),
                      true);
            printingMethodParams = oldPrintingMethodParams;
            if (cs.spaceWithinMethodDeclParens() && tree.params.nonEmpty())
                needSpace();
            print(')');
            if (tree.thrown.nonEmpty()) {
                wrap("throws ", cs.wrapThrowsKeyword());
                wrapTrees(tree.thrown, cs.wrapThrowsList(), cs.alignMultilineThrows()
                        ? out.col : out.leftMargin + cs.getContinuationIndentSize(),
                          true);
            }
            if (tree.body != null) {
                printBlock(tree.body, tree.body.stats, cs.getMethodDeclBracePlacement(), cs.spaceBeforeMethodDeclLeftBrace(), true);
            } else {
                if (tree.defaultValue != null) {
                    print(" default ");
                    printExpr(tree.defaultValue);
                }
                print(';');
            }
            enclClass = enclClassPrev;
	}
    }

    @Override
    public void visitVarDef(JCVariableDecl tree) {
        boolean notEnumConst = (tree.mods.flags & Flags.ENUM) == 0;
        printAnnotations(tree.mods.annotations);
        if (notEnumConst) {
            printFlags(tree.mods.flags);
            if (!suppressVariableType) {
                if ((tree.mods.flags & VARARGS) != 0) {
                    // Variable arity method. Expecting  ArrayType, print ... instead of [].
                    if (Kind.ARRAY_TYPE == tree.vartype.getKind()) {
                        printExpr(((JCArrayTypeTree) tree.vartype).elemtype);
                    } else {
                        printExpr(tree.vartype);
                    }
                    print("...");
                } else {
                    print(tree.vartype);
                }
            }
        }
        if (tree.vartype != null && !suppressVariableType) //should also check the flags?
            needSpace();
        if (!ERROR.contentEquals(tree.name))
            print(tree.name);
        if (tree.init != null) {
            if (notEnumConst) {
                printVarInit(tree);
            } else {
                JCNewClass newClsTree = (JCNewClass) tree.init;
                if (newClsTree.args.nonEmpty()) {
                    print(cs.spaceBeforeMethodCallParen() ? " (" : "(");
                    if (cs.spaceWithinMethodCallParens())
                        print(' ');
                    wrapTrees(newClsTree.args,
                            cs.wrapMethodCallArgs(),
                            cs.alignMultilineCallArgs() ? out.col : out.leftMargin + cs.getContinuationIndentSize()
                    );
                    print(cs.spaceWithinMethodCallParens() ? " )" : ")");
                }
                if (newClsTree.def != null) {
                    JCClassDecl enclClassPrev = enclClass;
                    enclClass = newClsTree.def;
                    printBlock(null, newClsTree.def.defs, cs.getOtherBracePlacement(), cs.spaceBeforeClassDeclLeftBrace(), true);
                    enclClass = enclClassPrev;
                }
            }
        }
        if ((prec == TreeInfo.notExpression) && notEnumConst) {
            print(';');
        }
    }

    public void printVarInit(final JCVariableDecl tree) {
        int col = out.col;
        if (!ERROR.contentEquals(tree.name))
            col -= tree.name.length();
        wrapAssignOpTree("=", col, new Runnable() {
            @Override public void run() {
                printNoParenExpr(tree.init);
            }
        });
    }

    @Override
    public void visitSkip(JCSkip tree) {
	print(';');
    }

    @Override
    public void visitBlock(JCBlock tree) {
	printFlags(tree.flags, false);
	printBlock(tree, tree.stats, cs.getOtherBracePlacement(), (tree.flags & Flags.STATIC) != 0 ? cs.spaceBeforeStaticInitLeftBrace() : false, false);
    }

    @Override
    public void visitDoLoop(JCDoWhileLoop tree) {
	print("do");
        if (cs.spaceBeforeDoLeftBrace())
            print(' ');
	printIndentedStat(tree.body, cs.redundantDoWhileBraces(), cs.spaceBeforeDoLeftBrace(), cs.wrapDoWhileStatement());
        boolean prevblock = tree.body.getKind() == Tree.Kind.BLOCK || cs.redundantDoWhileBraces() == BracesGenerationStyle.GENERATE;
        if (cs.placeWhileOnNewLine() || !prevblock) {
            newline();
            toLeftMargin();
        } else if (cs.spaceBeforeWhile()) {
	    needSpace();
        }
	print("while");
        print(cs.spaceBeforeWhileParen() ? " (" : "(");
        if (cs.spaceWithinWhileParens())
            print(' ');
	printNoParenExpr(tree.cond);
	print(cs.spaceWithinWhileParens()? " );" : ");");
    }

    @Override
    public void visitWhileLoop(JCWhileLoop tree) {
	print("while");
        print(cs.spaceBeforeWhileParen() ? " (" : "(");
        if (cs.spaceWithinWhileParens())
            print(' ');
	printNoParenExpr(tree.cond);
	print(cs.spaceWithinWhileParens() ? " )" : ")");
	printIndentedStat(tree.body, cs.redundantWhileBraces(), cs.spaceBeforeWhileLeftBrace(), cs.wrapWhileStatement());
    }

    @Override
    public void visitForLoop(JCForLoop tree) {
	print("for");
        print(cs.spaceBeforeForParen() ? " (" : "(");
        if (cs.spaceWithinForParens())
            print(' ');
        int col = out.col;
	if (tree.init.nonEmpty()) {
	    if (tree.init.head.getTag() == JCTree.Tag.VARDEF) {
		printNoParenExpr(tree.init.head);
		for (List<? extends JCTree> l = tree.init.tail; l.nonEmpty(); l = l.tail) {
		    JCVariableDecl vdef = (JCVariableDecl) l.head;
		    print(", " + vdef.name + " = ");
		    printNoParenExpr(vdef.init);
		}
	    } else {
		printExprs(tree.init);
	    }
	}
        String sep = cs.spaceBeforeSemi() ? " ;" : ";";
	print(sep);
        if (tree.cond != null) {
            switch(cs.wrapFor()) {
            case WRAP_IF_LONG:
                int rm = cs.getRightMargin();
                if (widthEstimator.estimateWidth(tree.cond, rm - out.col) + out.col + 1 <= rm) {
                    if (cs.spaceAfterSemi())
                        print(' ');
                    break;
                }
            case WRAP_ALWAYS:
                newline();
                toColExactly(cs.alignMultilineFor() ? col : out.leftMargin + cs.getContinuationIndentSize());
                break;
            case WRAP_NEVER:
                if (cs.spaceAfterSemi())
                    print(' ');
                break;
            }
	    printNoParenExpr(tree.cond);
        }
	print(sep);
        if (tree.step.nonEmpty()) {
            switch(cs.wrapFor()) {
            case WRAP_IF_LONG:
                int rm = cs.getRightMargin();
                if (widthEstimator.estimateWidth(tree.step, rm - out.col) + out.col + 1 <= rm) {
                    if (cs.spaceAfterSemi())
                        print(' ');
                    break;
                }
            case WRAP_ALWAYS:
                newline();
                toColExactly(cs.alignMultilineFor() ? col : out.leftMargin + cs.getContinuationIndentSize());
                break;
            case WRAP_NEVER:
                if (cs.spaceAfterSemi())
                    print(' ');
                break;
            }
            printExprs(tree.step);
        }
	print(cs.spaceWithinForParens() ? " )" : ")");
	printIndentedStat(tree.body, cs.redundantForBraces(), cs.spaceBeforeForLeftBrace(), cs.wrapForStatement());
    }

    @Override
    public void visitLabelled(JCLabeledStatement tree) {
        toColExactly(cs.absoluteLabelIndent() ? 0 : out.leftMargin);
	print(tree.label);
	print(':');
        int old = out.leftMargin;
        out.leftMargin += cs.getLabelIndent();
        toColExactly(out.leftMargin);
	printStat(tree.body);
        undent(old);
    }

    @Override
    public void visitLambda(JCLambda tree) {
        boolean useParens = cs.parensAroundSingularLambdaParam() ||
                            tree.params.size() != 1 ||
                            tree.paramKind != JCLambda.ParameterKind.IMPLICIT;
        if (useParens) {
            print(cs.spaceWithinLambdaParens() && tree.params.nonEmpty() ? "( " : "(");
        }
        boolean oldPrintingMethodParams = printingMethodParams;
        printingMethodParams = true;
        suppressVariableType = tree.paramKind == JCLambda.ParameterKind.IMPLICIT;
        wrapTrees(tree.params, cs.wrapLambdaParams(), cs.alignMultilineLambdaParams()
                ? out.col : out.leftMargin + cs.getContinuationIndentSize(),
                  true);
        suppressVariableType = false;
        printingMethodParams = oldPrintingMethodParams;
        if (useParens) {
            if (cs.spaceWithinLambdaParens() && tree.params.nonEmpty())
                needSpace();
            print(')');
        }
        print(cs.spaceAroundLambdaArrow() ? " ->" : "->");
        if (tree.getBodyKind() == BodyKind.STATEMENT) {
            printBlock(tree.body, cs.getOtherBracePlacement(), cs.spaceAroundLambdaArrow());
        } else {
            int rm = cs.getRightMargin();
            switch(cs.wrapBinaryOps()) {
            case WRAP_IF_LONG:
                if (widthEstimator.estimateWidth(tree.body, rm - out.col) + out.col <= cs.getRightMargin()) {
                    if(cs.spaceAroundLambdaArrow())
                        print(' ');
                    break;
                }
            case WRAP_ALWAYS:
                newline();
                toColExactly(out.leftMargin + cs.getContinuationIndentSize());
                break;
            case WRAP_NEVER:
                if(cs.spaceAroundLambdaArrow())
                    print(' ');
                break;
            }
            printExpr(tree.body, TreeInfo.notExpression);
        }
    }

    @Override
    public void visitSwitch(JCSwitch tree) {
	print("switch");
        print(cs.spaceBeforeSwitchParen() ? " (" : "(");
        if (cs.spaceWithinSwitchParens())
            print(' ');
	printNoParenExpr(tree.selector);
        print(cs.spaceWithinSwitchParens() ? " )" : ")");
        int bcol = out.leftMargin;
        switch(cs.getOtherBracePlacement()) {
        case NEW_LINE:
            newline();
            toColExactly(bcol);
            break;
        case NEW_LINE_HALF_INDENTED:
            newline();
	    bcol += (indentSize >> 1);
            toColExactly(bcol);
            break;
        case NEW_LINE_INDENTED:
            newline();
	    bcol += indentSize;
            toColExactly(bcol);
            break;
        }
        if (cs.spaceBeforeSwitchLeftBrace())
            needSpace();
	print('{');
        if (tree.cases.nonEmpty()) {
            newline();
            printStats(tree.cases);
            toColExactly(bcol);
        }
	print('}');
    }

    @Override
    public void visitSwitchExpression(JCSwitchExpression tree) {
        print("switch");
        print(cs.spaceBeforeSwitchParen() ? " (" : "(");
        if (cs.spaceWithinSwitchParens()) {
            print(' ');
        }
        printNoParenExpr(tree.selector);
        print(cs.spaceWithinSwitchParens() ? " )" : ")");
        int bcol = out.leftMargin;
        switch (cs.getOtherBracePlacement()) {
            case NEW_LINE:
                newline();
                toColExactly(bcol);
                break;
            case NEW_LINE_HALF_INDENTED:
                newline();
                bcol += (indentSize >> 1);
                toColExactly(bcol);
                break;
            case NEW_LINE_INDENTED:
                newline();
                bcol += indentSize;
                toColExactly(bcol);
                break;
        }
        if (cs.spaceBeforeSwitchLeftBrace()) {
            needSpace();
        }
        print('{');
        if (!tree.getCases().isEmpty()) {
            newline();
            ListBuffer<JCTree.JCCase> newTcases = new ListBuffer<JCTree.JCCase>();
            for (CaseTree t : tree.getCases()) {
                newTcases.append((JCTree.JCCase) t);
            }
            printStats(newTcases.toList());
            toColExactly(bcol);
        }
        print('}');
    }

    @Override
    public void visitCase(JCCase tree) {
        int old = cs.indentCasesFromSwitch() ? indent() : out.leftMargin;
        toLeftMargin();
        java.util.List<JCCaseLabel> labels = tree.labels;
        if (labels.size() == 1 && labels.get(0).hasTag(Tag.DEFAULTCASELABEL)) {
            print("default");
        } else {
            print("case ");
            String sep = "";
            for (JCTree lab : labels) {
                print(sep);
                printNoParenExpr(lab);
                sep = ", "; //TODO: space or not should be a configuration setting
            }
            if (tree.getGuard() != null) {
                needSpace();
                print("when ");
                print(tree.getGuard());
            }
        }
        Object caseKind = tree.getCaseKind();
        if (caseKind == null || !String.valueOf(caseKind).equals("RULE")) {
            print(':');
            newline();
            indent();
            printStats(tree.stats);
            undent(old);
        } else {
            print(" -> "); //TODO: configure spaces!
            printStat(tree.stats.head);
            undent(old);
        }
    }

    @Override
    public void visitSynchronized(JCSynchronized tree) {
	print("synchronized");
        print(cs.spaceBeforeSynchronizedParen() ? " (" : "(");
        if (cs.spaceWithinSynchronizedParens())
            print(' ');
	printNoParenExpr(tree.lock);
	print(cs.spaceWithinSynchronizedParens() ? " )" : ")");
	printBlock(tree.body, cs.getOtherBracePlacement(), cs.spaceBeforeSynchronizedLeftBrace());
    }

    @Override
    public void visitTry(JCTry tree) {
	print("try");
        if (!tree.getResources().isEmpty()) {
            print(" ("); //XXX: space should be according to the code style!
            for (Iterator<? extends JCTree> it = tree.getResources().iterator(); it.hasNext();) {
                JCTree r = it.next();
                //XXX: disabling copying of original text, as the ending ';' needs to be removed in some cases.
                oldTrees.remove(r);
                printPrecedingComments(r, false);
                printExpr(r, 0);
                printTrailingComments(r, false);
                if (it.hasNext()) print(";");
            }
            print(") "); //XXX: space should be according to the code style!
        }
	printBlock(tree.body, cs.getOtherBracePlacement(), cs.spaceBeforeTryLeftBrace());
	for (List < JCCatch > l = tree.catchers; l.nonEmpty(); l = l.tail)
	    printStat(l.head);
	if (tree.finalizer != null) {
	    printFinallyBlock(tree.finalizer);
	}
    }

    @Override
    public void visitCatch(JCCatch tree) {
        if (cs.placeCatchOnNewLine()) {
            newline();
            toLeftMargin();
        } else if (cs.spaceBeforeCatch()) {
            needSpace();
        }
	print("catch");
        print(cs.spaceBeforeCatchParen() ? " (" : "(");
        if (cs.spaceWithinCatchParens())
            print(' ');
	printNoParenExpr(tree.param);
	print(cs.spaceWithinCatchParens() ? " )" : ")");
	printBlock(tree.body, cs.getOtherBracePlacement(), cs.spaceBeforeCatchLeftBrace());
    }

    @Override
    public void visitConditional(JCConditional tree) {
        printExpr(tree.cond, TreeInfo.condPrec - 1);
        switch(cs.wrapTernaryOps()) {
        case WRAP_IF_LONG:
            int rm = cs.getRightMargin();
            if (widthEstimator.estimateWidth(tree.truepart, rm - out.col) + out.col + 1 <= rm) {
                if (cs.spaceAroundTernaryOps())
                    print(' ');
                break;
            }
        case WRAP_ALWAYS:
            newline();
            toColExactly(out.leftMargin + cs.getContinuationIndentSize());
            break;
        case WRAP_NEVER:
            if (cs.spaceAroundTernaryOps())
                print(' ');
            break;
        }
        print(cs.spaceAroundTernaryOps() ? "? " : "?");
        printExpr(tree.truepart, TreeInfo.condPrec);
        switch(cs.wrapTernaryOps()) {
        case WRAP_IF_LONG:
            int rm = cs.getRightMargin();
            if (widthEstimator.estimateWidth(tree.falsepart, rm - out.col) + out.col + 1 <= rm) {
                if (cs.spaceAroundTernaryOps())
                    print(' ');
                break;
            }
        case WRAP_ALWAYS:
            newline();
            toColExactly(out.leftMargin + cs.getContinuationIndentSize());
            break;
        case WRAP_NEVER:
            if (cs.spaceAroundTernaryOps())
                print(' ');
            break;
        }
        print(cs.spaceAroundTernaryOps() ? ": " : ":");
        printExpr(tree.falsepart, TreeInfo.condPrec);
    }

    @Override
    public void visitIf(JCIf tree) {
	print("if");
        print(cs.spaceBeforeIfParen() ? " (" : "(");
        if (cs.spaceWithinIfParens())
            print(' ');
	printNoParenExpr(tree.cond);
	print(cs.spaceWithinIfParens() ? " )" : ")");
        boolean prevblock = tree.thenpart.getKind() == Tree.Kind.BLOCK && cs.redundantIfBraces() != BracesGenerationStyle.ELIMINATE || cs.redundantIfBraces() == BracesGenerationStyle.GENERATE;
	if (tree.elsepart != null && danglingElseChecker.hasDanglingElse(tree.thenpart)) {
	    printBlock(tree.thenpart, cs.getOtherBracePlacement(), cs.spaceBeforeIfLeftBrace());
	    prevblock = true;
	} else
	    printIndentedStat(tree.thenpart, cs.redundantIfBraces(), cs.spaceBeforeIfLeftBrace(), cs.wrapIfStatement());
	if (tree.elsepart != null) {
        printElse(tree, prevblock);
	}
    }

    public void printElse(JCIf tree, boolean prevblock) {
	    if (cs.placeElseOnNewLine() || !prevblock) {
                newline();
                toLeftMargin();
            } else if (cs.spaceBeforeElse()) {
		needSpace();
            }
	    print("else");
	    if (tree.elsepart.getKind() == Tree.Kind.IF && cs.specialElseIf()) {
		needSpace();
		printStat(tree.elsepart);
	    } else
		printIndentedStat(tree.elsepart, cs.redundantIfBraces(), cs.spaceBeforeElseLeftBrace(), cs.wrapIfStatement());
    }

    @Override
    public void visitExec(JCExpressionStatement tree) {
	printNoParenExpr(tree.expr);
	if (prec == TreeInfo.notExpression)
	    print(';');
    }

    @Override
    public void visitBreak(JCBreak tree) {
        print("break");
        if (tree.getLabel() != null) {
            needSpace();
            print(tree.getLabel());
        }
        print(';');
    }

    public void visitYield(JCYield tree) {
        print("yield");
        ExpressionTree expr = tree.getValue();
        if (expr != null) {
            needSpace();
            print((JCTree) expr);
        }
        print(';');
    }

    @Override
    public void visitContinue(JCContinue tree) {
	print("continue");
	if (tree.label != null) {
	    needSpace();
	    print(tree.label);
	}
	print(';');
    }

    @Override
    public void visitReturn(JCReturn tree) {
	print("return");
	if (tree.expr != null) {
	    needSpace();
	    printNoParenExpr(tree.expr);
	}
	print(';');
    }

    @Override
    public void visitThrow(JCThrow tree) {
	print("throw ");
	printNoParenExpr(tree.expr);
	print(';');
    }

    @Override
    public void visitAssert(JCAssert tree) {
	print("assert ");
	printExpr(tree.cond);
	if (tree.detail != null) {
            print(cs.spaceBeforeColon() ? " :" : ":");
            switch(cs.wrapAssert()) {
            case WRAP_IF_LONG:
                int rm = cs.getRightMargin();
                if (widthEstimator.estimateWidth(tree.detail, rm - out.col) + out.col + 1 <= rm) {
                    if (cs.spaceAfterColon())
                        print(' ');
                    break;
                }
            case WRAP_ALWAYS:
                newline();
                toColExactly(out.leftMargin + cs.getContinuationIndentSize());
                break;
            case WRAP_NEVER:
                if (cs.spaceAfterColon())
                    print(' ');
                break;
            }
	    printExpr(tree.detail);
	}
	print(';');
    }

    @Override
    public void visitApply(JCMethodInvocation tree) {
        int prevPrec = this.prec;
        this.prec = TreeInfo.postfixPrec;
        printMethodSelect(tree);
        this.prec = prevPrec;
	print(cs.spaceBeforeMethodCallParen() ? " (" : "(");
        if (cs.spaceWithinMethodCallParens() && tree.args.nonEmpty())
            print(' ');
	wrapTrees(tree.args, cs.wrapMethodCallArgs(), cs.alignMultilineCallArgs()
                ? out.col : out.leftMargin + cs.getContinuationIndentSize());
	print(cs.spaceWithinMethodCallParens() && tree.args.nonEmpty() ? " )" : ")");
    }

    public void printMethodSelect(JCMethodInvocation tree) {
        if (tree.meth.getTag() == JCTree.Tag.SELECT) {
            JCFieldAccess left = (JCFieldAccess)tree.meth;
            printExpr(left.selected);
            boolean wrapAfterDot = cs.wrapAfterDotInChainedMethodCalls();
            if (wrapAfterDot)
                print('.');
            if (left.selected.getTag() == JCTree.Tag.APPLY) {
                switch(cs.wrapChainedMethodCalls()) {
                case WRAP_IF_LONG:
                    int rm = cs.getRightMargin();
                    int estWidth = left.name.length();
                    if (tree.typeargs.nonEmpty())
                        estWidth += widthEstimator.estimateWidth(tree.typeargs, rm - out.col - estWidth) + 2;
                    estWidth += widthEstimator.estimateWidth(tree.args, rm - out.col - estWidth) + 2;
                    if (estWidth + out.col <= rm)
                        break;
                case WRAP_ALWAYS:
                    newline();
                    toColExactly(out.leftMargin + cs.getContinuationIndentSize());
                    break;
                }
            }
            if (!wrapAfterDot)
                print('.');
            if (tree.typeargs.nonEmpty())
                printTypeArguments(tree.typeargs);
            print(left.name);
        } else {
            if (tree.typeargs.nonEmpty())
                printTypeArguments(tree.typeargs);
            printExpr(tree.meth);
        }
    }
    @Override
    public void visitNewClass(JCNewClass tree) {
	if (tree.encl != null) {
	    printExpr(tree.encl);
	    print('.');
	}
	print("new ");
        if (tree.typeargs.nonEmpty()) {
            print("<");
            printExprs(tree.typeargs);
            print(">");
        }
	if (tree.encl == null)
	    print(tree.clazz);
	else if (tree.clazz.type != null)
	    print(tree.clazz.type.tsym.name);
	else
	    print(tree.clazz);
	print(cs.spaceBeforeMethodCallParen() ? " (" : "(");
        if (cs.spaceWithinMethodCallParens()  && tree.args.nonEmpty())
            print(' ');
	wrapTrees(tree.args, cs.wrapMethodCallArgs(), cs.alignMultilineCallArgs()
                ? out.col : out.leftMargin + cs.getContinuationIndentSize());
	print(cs.spaceWithinMethodCallParens() && tree.args.nonEmpty() ? " )" : ")");
	if (tree.def != null) {
            printNewClassBody(tree);
	}
    }
    
    public void printNewClassBody(JCNewClass tree) {
        JCClassDecl enclClassPrev = enclClass;
        enclClass = tree.def;
        printBlock(null, tree.def.defs, cs.getOtherBracePlacement(), cs.spaceBeforeClassDeclLeftBrace(), true, true);
        enclClass = enclClassPrev;
    }

    @Override
    public void visitNewArray(JCNewArray tree) {
	if (tree.elemtype != null) {
	    print("new ");
	    int n = tree.elems != null ? 1 : 0;
	    JCTree elemtype = tree.elemtype;
	    while (elemtype.getTag() == JCTree.Tag.TYPEARRAY) {
		n++;
		elemtype = ((JCArrayTypeTree) elemtype).elemtype;
	    }
	    printExpr(elemtype);
	    for (List<? extends JCTree> l = tree.dims; l.nonEmpty(); l = l.tail) {
		print(cs.spaceWithinArrayInitBrackets() ? "[ " : "[");
		printNoParenExpr(l.head);
		print(cs.spaceWithinArrayInitBrackets() ? " ]" : "]");
	    }
	    while(--n >= 0)
                print(cs.spaceWithinArrayInitBrackets() ? "[ ]" : "[]");
	}
	if (tree.elems != null) {
            if (cs.spaceBeforeArrayInitLeftBrace())
                needSpace();
	    print('{');
            if (cs.spaceWithinBraces())
                print(' ');
	    wrapTrees(tree.elems, cs.wrapArrayInit(), cs.alignMultilineArrayInit()
                    ? out.col : out.leftMargin + cs.getContinuationIndentSize());
	    print(cs.spaceWithinBraces() ? " }" : "}");
	}
    }

    @Override
    public void visitParens(JCParens tree) {
	print('(');
        if (cs.spaceWithinParens())
            print(' ');
	printExpr(tree.expr);
	print(cs.spaceWithinParens() ? " )" : ")");
    }

    @Override
    public void visitAssign(final JCAssign tree) {
        int col = out.col;
	printExpr(tree.lhs, TreeInfo.assignPrec + 1);
        wrapAssignOpTree("=", col, new Runnable() {
            @Override public void run() {
                printExpr(tree.rhs, TreeInfo.assignPrec);
            }
        });
    }

    @Override
    public void visitAssignop(JCAssignOp tree) {
        int col = out.col;
	printExpr(tree.lhs, TreeInfo.assignopPrec + 1);
	if (cs.spaceAroundAssignOps())
            print(' ');
	print(operators.operatorName(tree.getTag().noAssignOp()));
        print('=');
	int rm = cs.getRightMargin();
        switch(cs.wrapAssignOps()) {
        case WRAP_IF_LONG:
            if (widthEstimator.estimateWidth(tree.rhs, rm - out.col) + out.col <= cs.getRightMargin()) {
                if(cs.spaceAroundAssignOps())
                    print(' ');
                break;
            }
        case WRAP_ALWAYS:
            newline();
            toColExactly(cs.alignMultilineAssignment() ? col : out.leftMargin + cs.getContinuationIndentSize());
            break;
        case WRAP_NEVER:
            if(cs.spaceAroundAssignOps())
                print(' ');
            break;
        }
	printExpr(tree.rhs, TreeInfo.assignopPrec);
    }

    @Override
    public void visitUnary(JCUnary tree) {
	int ownprec = TreeInfo.opPrec(tree.getTag());
	Name opname;
        switch (tree.getTag()) {
            case POS: opname = names.fromString("+"); break;
            case NEG: opname = names.fromString("-"); break;
            default: opname = operators.operatorName(tree.getTag()); break;
        }
	if (tree.getTag().ordinal() <= JCTree.Tag.PREDEC.ordinal()) { //XXX: comparing ordinals!
            if (cs.spaceAroundUnaryOps()) {
                needSpace();
                print(opname);
                print(' ');
            } else {
                print(opname);
                if (   (tree.getTag() == JCTree.Tag.POS && (tree.arg.getTag() == JCTree.Tag.POS || tree.arg.getTag() == JCTree.Tag.PREINC))
                    || (tree.getTag() == JCTree.Tag.NEG && (tree.arg.getTag() == JCTree.Tag.NEG || tree.arg.getTag() == JCTree.Tag.PREDEC))) {
                    print(' ');
                }
            }
	    printExpr(tree.arg, ownprec);
	} else {
	    printExpr(tree.arg, ownprec);
            if (cs.spaceAroundUnaryOps()) {
                print(' ');
                print(opname);
                print(' ');
            } else {
                print(opname);
            }
	}
    }

    @Override
    public void visitBinary(JCBinary tree) {
	int ownprec = TreeInfo.opPrec(tree.getTag());
	Name opname = operators.operatorName(tree.getTag());
        int col = out.col;
	printExpr(tree.lhs, ownprec);
	if(cs.spaceAroundBinaryOps())
            print(' ');
	print(opname);
        boolean needsSpace =    cs.spaceAroundBinaryOps()
                             || (tree.getTag() == JCTree.Tag.PLUS  && (tree.rhs.getTag() == JCTree.Tag.POS || tree.rhs.getTag() == JCTree.Tag.PREINC))
                             || (tree.getTag() == JCTree.Tag.MINUS && (tree.rhs.getTag() == JCTree.Tag.NEG || tree.rhs.getTag() == JCTree.Tag.PREDEC));
	int rm = cs.getRightMargin();
        switch(cs.wrapBinaryOps()) {
        case WRAP_IF_LONG:
            if (widthEstimator.estimateWidth(tree.rhs, rm - out.col) + out.col <= cs.getRightMargin()) {
                if(needsSpace)
                    print(' ');
                break;
            }
        case WRAP_ALWAYS:
            newline();
            toColExactly(cs.alignMultilineBinaryOp() ? col : out.leftMargin + cs.getContinuationIndentSize());
            break;
        case WRAP_NEVER:
            if(needsSpace)
                print(' ');
            break;
        }
	printExpr(tree.rhs, ownprec + 1);
    }

    @Override
    public void visitTypeCast(JCTypeCast tree) {
	print(cs.spaceWithinTypeCastParens() ? "( " : "(");
	print(tree.clazz);
	print(cs.spaceWithinTypeCastParens() ? " )" : ")");
        if (cs.spaceAfterTypeCast())
            needSpace();
        if (diffContext.origUnit != null && TreePath.getPath(diffContext.origUnit, tree.expr) != null) {
            int a = TreeInfo.getStartPos(tree.expr);
            int b = TreeInfo.getEndPos(tree.expr, diffContext.origUnit.endPositions);
            print(diffContext.origText.substring(a, b));
            return;
        }
	printExpr(tree.expr, TreeInfo.prefixPrec);
    }

    @Override
    public void visitTypeUnion(JCTypeUnion that) {
        boolean sep = cs.spaceAroundBinaryOps();
        wrapTrees(that.getTypeAlternatives(), 
                cs.wrapDisjunctiveCatchTypes(), 
                cs.alignMultilineDisjunctiveCatchTypes() ? out.col : out.leftMargin + cs.getContinuationIndentSize(),
                false, sep, sep, "|"); // NOI18N
    }

    @Override
    public void visitTypeIntersection(JCTypeIntersection tree) {
        printExprs(tree.bounds, " & ");
    }

    @Override
    public void visitTypeTest(JCInstanceOf tree) {
	printExpr(tree.expr, TreeInfo.ordPrec);
	print(" instanceof ");
	print(CasualDiff.getPattern(tree));
    }

    @Override
    public void visitIndexed(JCArrayAccess tree) {
	printExpr(tree.indexed, TreeInfo.postfixPrec);
	print('[');
	printExpr(tree.index);
	print(']');
    }

    @Override
    public void visitSelect(JCFieldAccess tree) {
        printExpr(tree.selected, TreeInfo.postfixPrec);
        print('.');
        print(tree.name);
    }

    @Override
    public void visitIdent(JCIdent tree) {
        print(tree.name);
    }

    @Override
    public void visitLiteral(JCLiteral tree) {
        long start, end;
        if (   diffContext != null
            && diffContext.origUnit != null
            && (start = diffContext.trees.getSourcePositions().getStartPosition(diffContext.origUnit, tree)) >= 0 //#137564
            && (end = diffContext.getEndPosition(diffContext.origUnit, tree)) >= 0
            && origText != null) {
            print(origText.substring((int) start, (int) end));
            return ;
        }
        if (   diffContext != null
            && diffContext.mainUnit != null
            && (start = diffContext.trees.getSourcePositions().getStartPosition(diffContext.mainUnit, tree)) >= 0 //#137564
            && (end = diffContext.getEndPosition(diffContext.mainUnit, tree)) >= 0
            && diffContext.mainCode != null) {
            print(diffContext.mainCode.substring((int) start, (int) end));
            return ;
        }
	switch (tree.typetag) {
	  case INT:
	    print(tree.value.toString());
	    break;
	  case LONG:
	    print(tree.value.toString() + "L");
	    break;
	  case FLOAT:
	    print(tree.value.toString() + "F");
	    break;
	  case DOUBLE:
	    print(tree.value.toString());
	    break;
	  case CHAR:
	    print("\'" +
		  quote(
		  String.valueOf((char) ((Number) tree.value).intValue()), '"') +
		  "\'");
	    break;
	   case CLASS:
             if (tree.value instanceof String) {
                 String leading;
                 String trailing;
                 if (tree instanceof StringTemplateFragmentTree) {
                     StringTemplateFragmentTree stf = (StringTemplateFragmentTree) tree;
                     switch (stf.fragmentKind) {
                         case START: leading = "\""; trailing = "\\{"; break;
                         case MIDDLE: leading = "}"; trailing = "\\{"; break;
                         case END: leading = "}"; trailing = "\""; break;
                         default: throw new IllegalStateException(stf.fragmentKind.name());
                     }
                 } else {
                     leading = trailing = "\"";
                 }
                 print(leading);
                 print(quote((String) tree.value, '\''));
                 print(trailing);
             } else if (tree.value instanceof String[]) {
                 int indent = out.col;
                 print("\"\"\"");
                 newline();
                 String[] lines = (String[]) tree.value;
                 for (int i = 0; i < lines.length; i++) {
                     out.toCol(indent);
                     String line = lines[i];
                     for (int c = 0; c < line.length(); c++) {
                         if (line.startsWith("\"\"\"", c)) {
                             print('\\');
                             print('"');
                         } else if (line.charAt(c) != '\'' && line.charAt(c) != '"') {
                             print(Convert.quote(line.charAt(c)));
                         } else {
                             print(line.charAt(c));
                         }
                     }
                     if (i + 1 < lines.length) {
                         newLineNoTrim();
                     }
                 }
                 print("\"\"\"");
             } else {
                 throw new IllegalStateException("Incorrect literal value.");
             }
	    break;
          case BOOLEAN:
            print(tree.getValue().toString());
            break;
          case BOT:
            print("null");
            break;
	  default:
	    print(tree.value.toString());
	}
    }

    private static String quote(String val, char keep) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < val.length(); i++) {
            char c = val.charAt(i);
            if (c != keep) {
                sb.append(Convert.quote(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    
    private static final String[] typeTagNames = new String[TypeTag.values().length];
    
    static {
        for (TypeTag tt : TypeTag.values()) {
            typeTagNames[tt.ordinal()] = tt.name().toLowerCase(Locale.ENGLISH);
        }
    }
    
    /**
     * Workaround for defect #239258. Converts typetag names into lowercase using ENGLISH locale.
     */
    static String typeTagName(TypeTag tt) {
        return typeTagNames[tt.ordinal()];
    }

    @Override
    public void visitTypeIdent(JCPrimitiveTypeTree tree) {
	print(typeTagName(tree.typetag));
    }

    @Override
    public void visitTypeArray(JCArrayTypeTree tree) {
	printExpr(tree.elemtype);
	print("[]");
    }

    @Override
    public void visitTypeApply(JCTypeApply tree) {
	printExpr(tree.clazz);
	print('<');
	printExprs(tree.arguments);
	print('>');
    }

    @Override
    public void visitAnnotatedType(JCAnnotatedType tree) {
	printExprs(tree.annotations);
        print(' ');
	printExpr(tree.underlyingType);
    }
    
    @Override
    public void visitTypeParameter(JCTypeParameter tree) {
	print(tree.name);
	if (tree.bounds.nonEmpty()) {
	    print(" extends ");
	    printExprs(tree.bounds, " & ");
	}
    }

    @Override
    public void visitWildcard(JCWildcard tree) {
	print(String.valueOf(tree.kind));
	if (tree.kind.kind != BoundKind.UNBOUND)
	    printExpr(tree.inner);
    }

    @Override
    public void visitModifiers(JCModifiers tree) {
	printAnnotations(tree.annotations);
	printFlags(tree.flags);
    }

    @Override
    public void visitAnnotation(JCAnnotation tree) {
        boolean oldInsideAnnotation = insideAnnotation;
        insideAnnotation = true;
        if (!printAnnotationsFormatted(List.of(tree))) {
            print("@");
            printExpr(tree.annotationType);
            if (tree.args.nonEmpty()) {
                print(cs.spaceBeforeAnnotationParen() ? " (" : "(");
                if (cs.spaceWithinAnnotationParens())
                    print(' ');
                printExprs(tree.args);
                print(cs.spaceWithinAnnotationParens() ? " )" : ")");
            }
        }
        insideAnnotation = oldInsideAnnotation;
    }

    @Override
    public void visitForeachLoop(JCEnhancedForLoop tree) {
	print("for");
        print(cs.spaceBeforeForParen() ? " (" : "(");
        if (cs.spaceWithinForParens())
            print(' ');
        printExpr(tree.getVariable());
        String sep = cs.spaceBeforeColon() ? " :" : ":";
        print(cs.spaceAfterColon() ? sep + " " : sep);
        printExpr(tree.getExpression());
        print(cs.spaceWithinForParens() ? " )" : ")");
	printIndentedStat(tree.getStatement(), cs.redundantForBraces(), cs.spaceBeforeForLeftBrace(), cs.wrapForStatement());
    }

    @Override
    public void visitReference(JCMemberReference tree) {
        printExpr(tree.expr);
        print(cs.spaceAroundMethodReferenceDoubleColon() ? " :: " : "::");
        if (tree.typeargs != null && !tree.typeargs.isEmpty()) {
            print("<");
            printExprs(tree.typeargs);
            print(">");
        }
        if (tree.getMode() == ReferenceMode.INVOKE) print(tree.name);
        else print("new");
    }

    @Override
    public void visitBindingPattern(JCBindingPattern tree) {
        print((JCTree) tree.var.vartype);
        print(' ');
        print((Name) tree.var.name);
    }

    @Override
    public void visitDefaultCaseLabel(JCDefaultCaseLabel tree) {
        print("default");
    }

    @Override
    public void visitConstantCaseLabel(JCConstantCaseLabel tree) {
        printExpr(tree.expr);
    }

    @Override
    public void visitStringTemplate(JCStringTemplate tree) {
        printExpr(tree.processor, TreeInfo.postfixPrec);
        print('.');

        Iterator<? extends String> fragmentIt = tree.fragments.iterator();
        Iterator<? extends JCExpression> expressionIt = tree.expressions.iterator();
        boolean start = true;

        while (expressionIt.hasNext()) {
            if (start) {
                print("\"");
            } else {
                print("}");
            }
            print(quote(fragmentIt.next(), '\''));
            print("\\{");
            print(expressionIt.next());
            start = false;
        }
        print("}");
        print(quote(fragmentIt.next(), '\''));
        print("\"");
    }

    @Override
    public void visitLetExpr(LetExpr tree) {
	print("(let " + tree.defs + " in " + tree.expr + ")");
    }

    @Override
    public void visitErroneous(JCErroneous tree) {
	print("(ERROR)");
    }

    @Override
    public void visitTree(JCTree tree) {
        print("(UNKNOWN: " + tree + ")");
        newline();
    }

    @Override
    public void visitPatternCaseLabel(JCPatternCaseLabel tree) {
        print(tree.pat);
    }

    @Override
    public void visitRecordPattern(JCRecordPattern tree) {
        print(tree.deconstructor);
        print("(");
        Iterator<JCPattern> it = tree.nested.iterator();
        while (it.hasNext()) {
            JCPattern pattern = it.next();
            doAccept(pattern, true);
            if (it.hasNext()) {
                print(", ");
            }
        }
        print(")");
    }

    /**************************************************************************
     * Private implementation
     *************************************************************************/

    private void print(char c) {
	out.append(c);
    }

    private void needSpace() {
	out.needSpace();
    }

    private void blankLines(int n) {
        out.blanklines(n);
    }

    private void blankLines(JCTree tree, boolean before) {
        if (tree == null) {
            return;
        }
        int n = 0;
        // NOTE - out.blankLines() may truncate a previous line, iff it contains trailing whitespace.
        switch (tree.getKind()) {
            case ANNOTATION_TYPE:
            case CLASS:
            case ENUM:
            case INTERFACE:
                n = before ? cs.getBlankLinesBeforeClass() : cs.getBlankLinesAfterClass();
        	if (((JCClassDecl) tree).defs.nonEmpty() && !before) {
                    n = 0;
                } else {
                    out.blanklines(n);
                    toLeftMargin();
                }
                return;
            case METHOD: // do not handle for sythetic things
        	if ((((JCMethodDecl) tree).mods.flags & Flags.SYNTHETIC) == 0 &&
                    ((JCMethodDecl) tree).name != names.init ||
                    enclClass != null)
                {
                    n = before
                            ? isFirst(tree, enclClass.defs)
                            ? enclClass.name == names.empty
                            ? cs.getBlankLinesAfterAnonymousClassHeader()
                            : cs.getBlankLinesAfterClassHeader()
                            : cs.getBlankLinesBeforeMethods()
                            : isLast(tree, enclClass.defs)
                            ? enclClass.name == names.empty
                            ? cs.getBlankLinesBeforeAnonymousClassClosingBrace()
                            : cs.getBlankLinesBeforeClassClosingBrace()
                            : cs.getBlankLinesAfterMethods();
                    out.blanklines(n);
        	    toLeftMargin();
                }
                return;
            case VARIABLE: // just for the fields
                if (enclClass != null && enclClass.name != names.empty && (((JCVariableDecl) tree).mods.flags & ENUM) == 0) {
                    n = before
                            ? isFirst(tree, enclClass.defs)
                            ? enclClass.name == names.empty
                            ? cs.getBlankLinesAfterAnonymousClassHeader()
                            : cs.getBlankLinesAfterClassHeader()
                            : cs.getBlankLinesBeforeFields()
                            : isLast(tree, enclClass.defs)
                            ? enclClass.name == names.empty
                            ? cs.getBlankLinesBeforeAnonymousClassClosingBrace()
                            : cs.getBlankLinesBeforeClassClosingBrace()
                            : cs.getBlankLinesAfterFields();
                    out.blanklines(n);
                    if (before) toLeftMargin();
                }
                return;
        }
    }
    
    private boolean isFirst(JCTree tree, List<? extends JCTree> list) {
        for (JCTree t : list) {
            if (!isSynthetic(t)) {
                return t == tree;
            }
        }
        return false;
    }
    
    private boolean isLast(JCTree tree, List<? extends JCTree> list) {
        boolean b = false;
        for (JCTree t : list) {
            if (!isSynthetic(t)) {
                b = t == tree;
            }
        }
        return b;
    }
    
    /**
     * The following tags are block-tags
     * <ul>
     * <li>@author (classes and interfaces only, required)</li>
     * <li>@version (classes and interfaces only, required. See footnote 1)</li>
     * <li>@param (methods and constructors only)</li>
     * <li>@return (methods only)</li>
     * <li>@exception (</li>
     * <li>@throws is a synonym added in Javadoc 1.2)</li>
     * <li>@see</li>
     * <li>@since</li>
     * <li>@serial (or @serialField or @serialData)</li>
     * <li>@deprecated (see How and When To Deprecate APIs)</li>
     * </ul>
     */
    private void blankLines(DCTree tree, boolean before, boolean suppressMarginAfter) {
        if (tree == null) {
            return;
        }
        switch (tree.getKind()) {
            case AUTHOR:
            case DEPRECATED:
            case EXCEPTION:
            case PARAM:
            case RETURN:
            case SEE:
            case SERIAL:
            case SERIAL_DATA:
            case SERIAL_FIELD:
            case SINCE:
            case THROWS:
            case UNKNOWN_BLOCK_TAG:
            case VERSION:
                if(before) {
                    newline();
                    toLeftMargin();
                    print(" * ");
                }
                break;
            case DOC_COMMENT:
                if(before) {
                    blankline();
                } else {
                    newline();
                }
                if (!suppressMarginAfter) {
                    toLeftMargin();
                }
                break;
            default:
                break;
        }
    }

    private void toColExactly(int n) {
	if (n < out.col) newline();
	out.toCol(n);
    }

    protected void printTagName(DocTree node) {
        out.append("@");
        out.append(node.getKind().tagName);
    }

    @Override
    public Void visitAttribute(AttributeTree node, Void p) {
        print(node.getName());
        String quote;
        switch (node.getValueKind()) {
            case EMPTY:
                return null;
            case UNQUOTED:
                quote = "";
                break;
            case SINGLE:
                quote = "'";
                break;
            case DOUBLE:
                quote = "\"";
                break;
            default:
                throw new AssertionError();
        }
        print("=");
        print(quote);
        for (DocTree docTree : node.getValue()) {
            doAccept((DCTree)docTree);
        }
        print(quote);
        return null;
    }

    @Override
    public Void visitAuthor(AuthorTree node, Void p) {
        printTagName(node);
        print(" ");
        for (DocTree docTree : node.getName()) {
            doAccept((DCTree)docTree);
        }
        return null;
    }

    @Override
    public Void visitComment(CommentTree node, Void p) {
        print(node.getBody());
        return null;
    }

    @Override
    public Void visitDeprecated(DeprecatedTree node, Void p) {
        printTagName(node);
        if (!node.getBody().isEmpty()) {
            needSpace();
            for (DocTree docTree : node.getBody()) {
                doAccept((DCTree)docTree);
            }
        }
        return null;
    }

    @Override
    public Void visitDocComment(DocCommentTree node, Void p) {
        print("/**");
        newline();
        toLeftMargin();
        print(" * ");
        for (DocTree docTree : node.getFirstSentence()) {
            doAccept((DCTree)docTree);
        }
        for (DocTree docTree : node.getBody()) {
            doAccept((DCTree)docTree);
        }
        for (DocTree docTree : node.getBlockTags()) {
            newline();
            toLeftMargin();
            print(" * ");
            doAccept((DCTree)docTree);
        }
        newline();
        toLeftMargin();
        print(" */");
        return null;
    }

    @Override
    public Void visitDocRoot(DocRootTree node, Void p) {
        print("{");
        printTagName(node);
        print("}");
        return null;
    }

    @Override
    public Void visitStartElement(StartElementTree node, Void p) {
        print("<");
        print(node.getName());
        java.util.List<? extends DocTree> attrs = node.getAttributes();
        if (!attrs.isEmpty()) {
            print(" ");
            for (DocTree docTree : attrs) {
                doAccept((DCTree)docTree);
            }
            DocTree last = attrs.get(attrs.size() - 1);
            if (node.isSelfClosing() && last instanceof AttributeTree
                    && ((AttributeTree) last).getValueKind() == ValueKind.UNQUOTED)
                print(" ");
        }
        if (node.isSelfClosing())
            print("/");
        print(">");
        return null;
    }

    @Override
    public Void visitEndElement(EndElementTree node, Void p) {
        print("</");
        print(node.getName());
        print(">");
        return null;
    }

    @Override
    public Void visitEntity(EntityTree node, Void p) {
        print("&");
        print(node.getName());
        print(";");
        return null;
    }

    @Override
    public Void visitErroneous(ErroneousTree node, Void p) {
        print(node.getBody());
        return null;
    }

    @Override
    public Void visitHidden(HiddenTree node, Void p) {
        printTagName(node);
        if (!node.getBody().isEmpty()) {
            print(" ");
            for (DocTree docTree : node.getBody()) {
                doAccept((DCTree)docTree);
            }
        }
        return null;
    }

    @Override
    public Void visitIdentifier(IdentifierTree node, Void p) {
        print(node.getName());
        return null;
    }

    @Override
    public Void visitIndex(IndexTree node, Void p) {
        print("{");
        printTagName(node);
        print(" ");
        doAccept((DCTree)node.getSearchTerm());
        if (!node.getDescription().isEmpty()) {
            print(" ");
            for (DocTree docTree : node.getDescription()) {
                doAccept((DCTree)docTree);
            }
        }
        print("}");
        return null;
    }

    @Override
    public Void visitInheritDoc(InheritDocTree node, Void p) {
        print("{");
        printTagName(node);
        print("}");
        return null;
    }

    @Override
    public Void visitLink(LinkTree node, Void p) {
        print("{");
        printTagName(node);
        print(" ");
        doAccept((DCTree)node.getReference());
        if (!node.getLabel().isEmpty()) {
            print(" ");
            for (DocTree docTree : node.getLabel()) {
                doAccept((DCTree)docTree);
            }
        }
        print("}");
        return null;
    }

    @Override
    public Void visitLiteral(LiteralTree node, Void p) {
        print("{");
        printTagName(node);
        print(" ");
        doAccept((DCTree)node.getBody());
        print("}");
        return null;
    }

    @Override
    public Void visitParam(ParamTree node, Void p) {
        printTagName(node);
        needSpace();
        if(node.isTypeParameter()) {
           print('<');
        }
        doAccept((DCTree)node.getName());
        if(node.isTypeParameter()) {
           print('>');
        }
        if(!node.getDescription().isEmpty()) {
            needSpace();
        }
        for (DocTree docTree : node.getDescription()) {
            doAccept((DCTree)docTree);
        }
        return null;
    }

    @Override
    public Void visitProvides(ProvidesTree node, Void p) {
        printTagName(node);
        needSpace();
        doAccept((DCTree)node.getServiceType());
        if (!node.getDescription().isEmpty()) {
            needSpace();
            for (DocTree docTree : node.getDescription()) {
                doAccept((DCTree)docTree);
            }
        }
        return null;
    }

    @Override
    public Void visitReference(ReferenceTree node, Void p) {
        //TODO: should use formatting settings:
        DCReference refNode = (DCReference) node;
        if (refNode.qualifierExpression != null) {
            print(refNode.qualifierExpression);
        }
        if (refNode.memberName != null) {
            print("#");
            print(refNode.memberName);
        }
        if (refNode.paramTypes != null) {
            print("(");
            boolean first = true;
            for (Tree param : refNode.paramTypes) {
                if (!first) print(", ");
                print(param.toString());
                first = false;
            }
            print(")");
        }
        return null;
    }

    @Override
    public Void visitReturn(ReturnTree node, Void p) {
        printTagName(node);
        print(" ");
        for (DocTree docTree : node.getDescription()) {
            doAccept((DCTree)docTree);
        }
        return null;
    }

    @Override
    public Void visitSee(SeeTree node, Void p) {
        printTagName(node);
        boolean first = true;
        boolean needSep = true;
        for (DocTree t: node.getReference()) {
            if (needSep) print(" ");
            needSep = (first && (t instanceof ReferenceTree));
            first = false;
            print((DCTree)t);
        }
        return null;
    }

    @Override
    public Void visitSerial(SerialTree node, Void p) {
        printTagName(node);
        if (!node.getDescription().isEmpty()) {
            print(" ");
            for (DocTree docTree : node.getDescription()) {
                doAccept((DCTree)docTree);
            }
        }
        return null;
    }

    @Override
    public Void visitSerialData(SerialDataTree node, Void p) {
        printTagName(node);
        if (!node.getDescription().isEmpty()) {
            print(" ");
            for (DocTree docTree : node.getDescription()) {
                doAccept((DCTree)docTree);
            }
        }
        return null;
    }

    @Override
    public Void visitSerialField(SerialFieldTree node, Void p) {
        printTagName(node);
        print(" ");
        print((DCTree)node.getName());
        print(" ");
        print((DCTree)node.getType());
        if (!node.getDescription().isEmpty()) {
            print(" ");
            for (DocTree docTree : node.getDescription()) {
                doAccept((DCTree)docTree);
            }
        }
        return null;
    }

    @Override
    public Void visitSince(SinceTree node, Void p) {
        printTagName(node);
        print(" ");
        for (DocTree docTree : node.getBody()) {
            doAccept((DCTree)docTree);
        }
        return null;
    }

    @Override
    public Void visitText(TextTree node, Void p) {
        print(node.getBody());
        return null;
    }

    @Override
    public Void visitThrows(ThrowsTree node, Void p) {
        printTagName(node);
        needSpace();
        doAccept((DCTree)node.getExceptionName());
        if(!node.getDescription().isEmpty()) {
            needSpace();
            for (DocTree docTree : node.getDescription()) {
                doAccept((DCTree)docTree);
            }
        }
        return null;
    }

    @Override
    public Void visitUnknownBlockTag(UnknownBlockTagTree node, Void p) {
        print("@");
        print(node.getTagName());
        print(" ");
        for (DocTree docTree : node.getContent()) {
            doAccept((DCTree)docTree);
        }
        return null;
    }

    @Override
    public Void visitUnknownInlineTag(UnknownInlineTagTree node, Void p) {
        print("{");
        print("@");
        print(node.getTagName());
        print(" ");
        for (DocTree docTree : node.getContent()) {
            doAccept((DCTree)docTree);
        }
        print("}");
        return null;
    }

    @Override
    public Void visitUses(UsesTree node, Void p) {
        printTagName(node);
        needSpace();
        doAccept((DCTree)node.getServiceType());
        if (!node.getDescription().isEmpty()) {
            needSpace();
            for (DocTree docTree : node.getDescription()) {
                doAccept((DCTree)docTree);
            }
        }
        return null;
    }

    @Override
    public Void visitValue(ValueTree node, Void p) {
        print("{");
        printTagName(node);
        if (node.getReference() != null) {
            print(" ");
            print((DCTree)node.getReference());
        }
        print("}");
        return null;
    }

    @Override
    public Void visitVersion(VersionTree node, Void p) {
        printTagName(node);
        print(" ");
        for (DocTree docTree : node.getBody()) {
            doAccept((DCTree)docTree);
        }
        return null;
    }

    @Override
    public Void visitOther(DocTree node, Void p) {
        print("(UNKNOWN: " + node + ")");
        newline();
        return null;
    }

    private final class Linearize extends ErrorAwareTreeScanner<Boolean, java.util.List<Tree>> {
        @Override
        public Boolean scan(Tree node, java.util.List<Tree> p) {
            p.add(node);
            super.scan(node, p);
            return tree2Tag.containsKey(node);
        }
        @Override
        public Boolean reduce(Boolean r1, Boolean r2) {
            return r1 == Boolean.TRUE || r2 == Boolean.TRUE;
        }
    }

    private final class CopyTags extends ErrorAwareTreeScanner<Void, java.util.List<Tree>> {
        private final CompilationUnitTree fake;
        private final SourcePositions sp;
        public CopyTags(CompilationUnitTree fake, SourcePositions sp) {
            this.fake = fake;
            this.sp = sp;
        }
        @Override
        public Void scan(Tree node, java.util.List<Tree> p) {
            if (p.isEmpty()) {
                return null;//the original tree(s) had less elements than the current trees???
            }
            Object tag = tree2Tag.get(p.remove(0));
            if (tag != null) {
                tag2Span.put(tag, new int[] {out.length() + initialOffset + (int) sp.getStartPosition(fake, node), out.length() + initialOffset + (int) sp.getEndPosition(fake, node)});
            }
            return super.scan(node, p);
        }
    }

    private void adjustSpans(Iterable<? extends Tree> original, String code) {
        if (tree2Tag == null) {
            return; //nothing to  copy
        }
        
        java.util.List<Tree> linearized = new LinkedList<Tree>();
        if (!new Linearize().scan(original, linearized) != Boolean.TRUE) {
            return; //nothing to  copy
        }
        
            ClassPath empty = ClassPathSupport.createClassPath(new URL[0]);
            ClasspathInfo cpInfo = ClasspathInfo.create(JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries(), empty, empty);
            JavacTaskImpl javacTask = JavacParser.createJavacTask(cpInfo, null, null, null, null, null, null, null, Arrays.asList(FileObjects.memoryFileObject("", "Scratch.java", code)));
            com.sun.tools.javac.util.Context ctx = javacTask.getContext();
            JavaCompiler.instance(ctx).genEndPos = true;
            CompilationUnitTree tree = javacTask.parse().iterator().next(); //NOI18N
            SourcePositions sp = JavacTrees.instance(ctx).getSourcePositions();
            ClassTree clazz = (ClassTree) tree.getTypeDecls().get(0);

            new CopyTags(tree, sp).scan(clazz.getModifiers().getAnnotations(), linearized);
    }

    private boolean reallyPrintAnnotations;

    private static String whitespace(int num) {
        StringBuilder res = new StringBuilder(num);

        while (num-- > 0) {
            res.append(' ');
        }

        return res.toString();
    }

    private boolean printAnnotationsFormatted(List<JCAnnotation> annotations) {
        if (reallyPrintAnnotations) return false;
        
        VeryPretty del = new VeryPretty(diffContext, cs, new HashMap<Tree, Object>(), tree2Doc, new HashMap<Object, int[]>(), origText, 0);
        del.reallyPrintAnnotations = true;
        del.printingMethodParams = printingMethodParams;

        del.printAnnotations(annotations);

        String str = del.out.toString();
        int col = printingMethodParams ? out.leftMargin + cs.getContinuationIndentSize() : out.col;
        
        str = Reformatter.reformat(str + " class A{}", cs, cs.getRightMargin() - col);

        str = str.trim().replace("\n", "\n" + whitespace(col));

        try {
            adjustSpans(annotations, str);
        } catch (Exception e) {
            return false;
        }
        str = str.substring(0, str.lastIndexOf("class")).trim();

        print(str);

        return true;
    }
    
    private void printAnnotations(List<JCAnnotation> annotations) {
        if (annotations.isEmpty()) return ;

        if (!printingMethodParams && printAnnotationsFormatted(annotations)) {
            toColExactly(out.leftMargin);
            return ;
        }
        
        while (annotations.nonEmpty()) {
	    printNoParenExpr(annotations.head);
            if (annotations.tail != null && annotations.tail.nonEmpty()) {
                if (printingMethodParams) {
                    print(' ');
                } else {
                    switch(cs.wrapAnnotations()) {
                    case WRAP_IF_LONG:
                        int rm = cs.getRightMargin();
                        if (widthEstimator.estimateWidth(annotations.tail.head, rm - out.col) + out.col + 1 <= rm) {
                            print(' ');
                            break;
                        }
                    case WRAP_ALWAYS:
                        newline();
                        toColExactly(out.leftMargin);
                        break;
                    case WRAP_NEVER:
                        print(' ');
                        break;
                    }
                }
            } else {
                if (!printingMethodParams)
                    toColExactly(out.leftMargin);
            }
            annotations = annotations.tail;
        }
        if (printingMethodParams) {
            out.needSpace();
        }
    }

    public void printFlags(long flags) {
        printFlags(flags, true);
    }

    public void printFinallyBlock(JCBlock finalizer) {
        if (cs.placeFinallyOnNewLine()) {
            newline();
            toLeftMargin();
        } else if (cs.spaceBeforeFinally()) {
            needSpace();
        }
        print("finally");
        printBlock(finalizer, cs.getOtherBracePlacement(), cs.spaceBeforeFinallyLeftBrace());
    }

    public void printFlags(long flags, boolean addSpace) {
	print(flagNames(flags & ~INTERFACE & ~ANNOTATION & ~ENUM));
        if ((flags & StandardFlags) != 0) {
            if (cs.placeNewLineAfterModifiers())
                toColExactly(out.leftMargin);
            else if (addSpace)
	        needSpace();
        }
    }
    
    private static final String[] flagLowerCaseNames = new String[Flag.values().length];
    
    static {
        for (Flag flag : Flag.values()) {
            flagLowerCaseNames[flag.ordinal()] = flag.name().toLowerCase(Locale.ENGLISH);
        }
    }
    
    /**
     * Workaround for defect #239258. Prints flag names converted to lowercase in ENGLISH locale to 
     * avoid weird Turkish I > i-without-dot-above conversion.
     * 
     * @param flags flags
     * @return flag names, space-separated.
     */
    public static String flagNames(long flags) {
        flags = flags & Flags.ExtendedStandardFlags;
        StringBuilder buf = new StringBuilder();
        String sep = ""; // NOI18N
        for (Flag flag : Flags.asFlagSet(flags)) {
            buf.append(sep);
            String fname = flagLowerCaseNames[flag.ordinal()];
            buf.append(fname);
            sep = " "; // NOI18N
        }
        return buf.toString().trim();
    }

    public void printBlock(JCTree oldT, JCTree newT, Kind parentKind) {
        switch (parentKind) {
            case ENHANCED_FOR_LOOP:
            case FOR_LOOP:
                printIndentedStat(newT, cs.redundantForBraces(), cs.spaceBeforeForLeftBrace(), cs.wrapForStatement());
                break;
            case WHILE_LOOP:
                printIndentedStat(newT, cs.redundantWhileBraces(), cs.spaceBeforeWhileLeftBrace(), cs.wrapWhileStatement());
                break;
            case IF:
                printIndentedStat(newT, cs.redundantIfBraces(), cs.spaceBeforeIfLeftBrace(), cs.wrapIfStatement());
                break;
            case DO_WHILE_LOOP:
                printIndentedStat(newT, cs.redundantDoWhileBraces(), cs.spaceBeforeDoLeftBrace(), cs.wrapDoWhileStatement());
                if (cs.placeWhileOnNewLine()) {
                    newline();
                    toLeftMargin();
                } else if (cs.spaceBeforeWhile()) {
                    needSpace();
                }
        }
    }

    public void printImportsBlock(java.util.List<? extends JCTree> imports, boolean maybeAppendNewLine) {
        boolean hasImports = !imports.isEmpty();
        CodeStyle.ImportGroups importGroups = null;
        if (hasImports) {
            blankLines(Math.max(cs.getBlankLinesBeforeImports(), diffContext.origUnit.getPackageName() != null ? cs.getBlankLinesAfterPackage() : 0));
            if (cs.separateImportGroups())
                importGroups = cs.getImportGroups();
        }
        int lastGroup = -1;
        for (JCTree importStat : imports) {
            if (importGroups != null) {
                Name name = fullName(((JCImport)importStat).qualid);
                int group = name != null ? importGroups.getGroupId(name.toString(), ((JCImport)importStat).staticImport) : -1;
                if (lastGroup >= 0 && lastGroup != group)
                    blankline();
                lastGroup = group;
            }
            printStat(importStat);
            newline();
        }
        if (hasImports && maybeAppendNewLine) {
            blankLines(cs.getBlankLinesAfterImports());
        }
    }

    public void eatChars(int count) {
        out.eatAwayChars(count);
    }

    private void printExpr(JCTree tree) {
	printExpr(tree, TreeInfo.noPrec);
    }

    private void printNoParenExpr(JCTree tree) {
	while (tree instanceof JCParens)
	    tree = ((JCParens) tree).expr;
	printExpr(tree, TreeInfo.noPrec);
    }

    private void printExpr(JCTree tree, int prec) {
	if (tree == null) {
	    print("/*missing*/");
	} else {
	    int prevPrec = this.prec;
	    this.prec = prec;
            doAccept(tree, commentsEnabled);
	    this.prec = prevPrec;
	}
    }

    private <T extends JCTree >void printExprs(List < T > trees) {
        String sep = cs.spaceBeforeComma() ? " ," : ",";
	printExprs(trees, cs.spaceAfterComma() ? sep + " " : sep);
    }

    private <T extends JCTree >void printExprs(List < T > trees, String sep) {
	if (trees.nonEmpty()) {
	    printNoParenExpr(trees.head);
	    for (List < T > l = trees.tail; l.nonEmpty(); l = l.tail) {
		print(sep);
		printNoParenExpr(l.head);
	    }
	}
    }

    private void printStat(JCTree tree) {
        printStat(tree, false, false);
    }
    
    private void printStat(JCTree tree, boolean member, boolean first) {
        printStat(tree, member, first, false, false, false);
    }

    /**
     * Prints blank lines before, positions to the exact column (optional), prints tree and
     * blank lines after. And optional additional newline.
     * 
     * @param tree
     * @param member
     * @param first
     * @param col
     * @param nl 
     */
    private void printStat(JCTree tree, boolean member, boolean first, boolean col, boolean nl, boolean printComments) {
	if(tree==null) {
            if (col) {
                toColExactly(out.leftMargin);
            }
            print(';');
            if (nl) {
                newline();
            }
        }
	else {
            if (!first)
                blankLines(tree, true);
            if (col) {
                toColExactly(out.leftMargin);
            }
            // because of comment duplication 
	    if(printComments) printPrecedingComments(tree, !member);
            printInnerCommentsAsTrailing(tree, !member);
            printExpr(tree, TreeInfo.notExpression);
	    int tag = tree.getTag().ordinal();//XXX: comparing ordinals!!!
	    if(JCTree.Tag.APPLY.ordinal()<=tag && tag<=JCTree.Tag.MOD_ASG.ordinal()) print(';');
            
            printTrailingComments(tree, !member);
            blankLines(tree, false);
            if (nl) {
                newline();
            }
	}
    }

    private void printIndentedStat(JCTree tree, BracesGenerationStyle redundantBraces, boolean spaceBeforeLeftBrace, WrapStyle wrapStat) {
        if (fromOffset >= 0 && toOffset >= 0 && (TreeInfo.getStartPos(tree) < fromOffset || TreeInfo.getEndPos(tree, diffContext.origUnit.endPositions) > toOffset))
            redundantBraces = BracesGenerationStyle.LEAVE_ALONE;
	switch(redundantBraces) {
        case GENERATE:
            printBlock(tree, cs.getOtherBracePlacement(), spaceBeforeLeftBrace);
            return;
        case ELIMINATE:
	    while(tree instanceof JCBlock) {
		List<JCStatement> t = ((JCBlock) tree).stats;
		if(t.isEmpty() || t.tail.nonEmpty()) break;
		if (t.head instanceof JCVariableDecl)
		    // bogus code has a variable declaration -- leave alone.
		    break;
		printPrecedingComments(tree, true);
		tree = t.head;
	    }
        case LEAVE_ALONE:
            if (tree instanceof JCBlock) {
                printBlock(tree, cs.getOtherBracePlacement(), spaceBeforeLeftBrace);
                return;
            }
            final int old = indent();
            final JCTree toPrint = tree;
            wrapTree(wrapStat, spaceBeforeLeftBrace, out.leftMargin, new Runnable() {
                @Override public void run() {
                    printStat(toPrint);
                    undent(old);
                }
            });
	}
    }

    private void printStats(List < ? extends JCTree > trees) {
        printStats(trees, false);
    }

    private void printStats(List < ? extends JCTree > trees, boolean members) {
        java.util.List<JCTree> filtered = CasualDiff.filterHidden(diffContext, trees);

        if (!filtered.isEmpty() && handlePossibleOldTrees(filtered, true)) return;

        boolean first = true;
	for (JCTree t : filtered) {
	     printStat(t, members, first, true, false, false);
            first = false;
	}
    }

    private void printBlock(JCTree t, BracePlacement bracePlacement, boolean spaceBeforeLeftBrace) {
        JCTree block;
	List<? extends JCTree> stats;
	if (t instanceof JCBlock) {
            block = t;
	    stats = ((JCBlock) t).stats;
        } else {
            block = null;
	    stats = List.of(t);
        }
	printBlock(block, stats, bracePlacement, spaceBeforeLeftBrace, true);
    }

    private void printBlock(JCTree tree, List<? extends JCTree> stats, BracePlacement bracePlacement, boolean spaceBeforeLeftBrace, boolean printComments) {
        printBlock(tree, stats, bracePlacement, spaceBeforeLeftBrace, false, printComments);
    }

    public int conditionStartHack = (-1);
    
    private void printBlock(JCTree tree, List<? extends JCTree> stats, BracePlacement bracePlacement, boolean spaceBeforeLeftBrace, boolean members, boolean printComments) {
        if (printComments) printPrecedingComments(tree, true);
	int old = indent();
	int bcol = old;
        switch(bracePlacement) {
        case NEW_LINE:
            newline();
            toColExactly(old);
            break;
        case NEW_LINE_HALF_INDENTED:
            newline();
	    bcol += (indentSize >> 1);
            toColExactly(bcol);
            break;
        case NEW_LINE_INDENTED:
            newline();
	    bcol = out.leftMargin;
            toColExactly(bcol);
            break;
        }
        String trailing = null;
        if (conditionStartHack != (-1)) {
            TokenSequence<JavaTokenId> ts = TokenHierarchy.create(toString().substring(conditionStartHack), JavaTokenId.language()).tokenSequence(JavaTokenId.language());
            boolean found;
            ts.moveEnd();
            while ((found = ts.movePrevious()) && PositionEstimator.nonRelevant.contains(ts.token().id()))
                ;
            if (found) {
                String content = toString();
                trailing = content.substring(conditionStartHack + ts.offset() + ts.token().text().length());
                out.used -= trailing.length();
                out.col -= trailing.length();
            }
        }
        if (spaceBeforeLeftBrace)
            needSpace();
	print('{');
        if (trailing != null) print(trailing);
        boolean emptyBlock = true;
        for (List<? extends JCTree> l = stats; l.nonEmpty(); l = l.tail) {
            if (!isSynthetic(l.head)) {
                emptyBlock = false;
                break;
            }
        }
	if (emptyBlock) {
            printEmptyBlockComments(tree, members);
        } else {
            if (innerCommentsHandled.add(tree)) {
                java.util.List<Comment> comments = commentHandler.getComments(tree).getComments(CommentSet.RelativePosition.INNER);
                for (Comment c : comments) {
                    printComment(c, false, members);
                }
            }
            if (members)
                blankLines(cs.getBlankLinesAfterAnonymousClassHeader());
            else
                newline();
	    printStats(stats, members);
        }
        toColExactly(bcol);
	undent(old);
	print('}');
        if (printComments) printTrailingComments(tree, true);
    }

    private void printTypeParameters(List < JCTypeParameter > trees) {
	if (trees.nonEmpty()) {
	    print('<');
	    printExprs(trees);
	    print('>');
	}
    }

    private void printTypeArguments(List<? extends JCExpression> typeargs) {
        if (typeargs.nonEmpty()) {
            print('<');
            printExprs(typeargs);
            print('>');
        }
    }

    private Set<Tree> precedingCommentsHandled = new HashSet<Tree>();

    private void printPrecedingComments(JCTree tree, boolean printWhitespace) {
        if (!precedingCommentsHandled.add(tree)) {
            return;
        }
        CommentSet commentSet = commentHandler.getComments(tree);
        java.util.List<Comment> pc = commentSet.getComments(CommentSet.RelativePosition.PRECEDING);
        DocCommentTree doc = tree2Doc.get(tree);
        if (!pc.isEmpty()) {
            Comment javadoc = null;
            for (Comment comment : pc) {
                if(comment.style() == Style.JAVADOC) {
                    javadoc = comment;
                }
            }
            for (Comment c : pc) {
                if(doc != null && c == javadoc) {
                    print((DCTree)doc);
                    doc = null;
                } else {
                    printComment(c, true, printWhitespace);
                }
            }
        }
        if(doc!=null) {
            print((DCTree)doc);
        }
    }

    private void printInnerCommentsAsTrailing(JCTree tree, boolean printWhitespace) {
        if (innerCommentsHandled.contains(tree)) return ;
        CommentSet commentSet = commentHandler.getComments(tree);
        java.util.List<Comment> cl = commentSet.getComments(CommentSet.RelativePosition.INNER);
        innerCommentsHandled.add(tree);
        for (Comment comment : cl) {
            printComment(comment, true, printWhitespace);
        }
    }

    private void printTrailingComments(JCTree tree, boolean printWhitespace) {
        if (trailingCommentsHandled.contains(tree)) return ;
        CommentSet commentSet = commentHandler.getComments(tree);
        java.util.List<Comment> cl = commentSet.getComments(CommentSet.RelativePosition.INLINE);
        for (Comment comment : cl) {
            trailingCommentsHandled.add(tree);
            printComment(comment, true, printWhitespace);
        }
        java.util.List<Comment> tc = commentSet.getComments(CommentSet.RelativePosition.TRAILING);
        if (!tc.isEmpty()) {
            trailingCommentsHandled.add(tree);
            for (Comment c : tc)
                printComment(c, false, printWhitespace);
        }
    }

    private void printEmptyBlockComments(JCTree tree, boolean printWhitespace) {
//        LinkedList<Comment> comments = new LinkedList<Comment>();
//        if (cInfo != null) {
//            int pos = TreeInfo.getEndPos(tree, diffContext.origUnit.endPositions) - 1;
//            if (pos >= 0) {
//                TokenSequence<JavaTokenId> tokens = cInfo.getTokenHierarchy().tokenSequence(JavaTokenId.language());
//                tokens.move(pos);
//                moveToSrcRelevant(tokens, Direction.BACKWARD);
//                int indent = NOPOS;
//                while (tokens.moveNext() && nonRelevant.contains(tokens.token().id())) {
//                    if (tokens.index() > lastReadCommentIdx) {
//                        switch (tokens.token().id()) {
//                            case LINE_COMMENT:
//                                comments.add(Comment.create(Style.LINE, tokens.offset(), tokens.offset() + tokens.token().length(), indent, tokens.token().toString()));
//                                indent = 0;
//                                break;
//                            case BLOCK_COMMENT:
//                                comments.add(Comment.create(Style.BLOCK, tokens.offset(), tokens.offset() + tokens.token().length(), indent, tokens.token().toString()));
//                                indent = NOPOS;
//                                break;
//                            case JAVADOC_COMMENT:
//                                comments.add(Comment.create(Style.JAVADOC, tokens.offset(), tokens.offset() + tokens.token().length(), indent, tokens.token().toString()));
//                                indent = NOPOS;
//                                break;
//                            case WHITESPACE:
//                                String tokenText = tokens.token().toString();
//                                comments.add(Comment.create(Style.WHITESPACE, NOPOS, NOPOS, NOPOS, tokenText));
//                                int newLinePos = tokenText.lastIndexOf('\n');
//                                if (newLinePos < 0) {
//                                    if (indent >= 0)
//                                        indent += tokenText.length();
//                                } else {
//                                    indent = tokenText.length() - newLinePos - 1;
//                                }
//                                break;
//                        }
//                        lastReadCommentIdx = tokens.index();
//                    }
//                }
//            }
//        }
        if (!innerCommentsHandled.add(tree)) {
            return;
        }
        java.util.List<Comment> comments = commentHandler.getComments(tree).getComments(CommentSet.RelativePosition.INNER);
        for (Comment c : comments)
            printComment(c, false, printWhitespace);
    }

    public void printComment(Comment comment, boolean preceding, boolean printWhitespace) {
        printComment(comment, preceding, printWhitespace, false);
    }

    public void printComment(Comment comment, boolean preceding, boolean printWhitespace, boolean preventClosingWhitespace) {
        boolean onlyWhitespaces = out.isWhitespaceLine();
        if (Comment.Style.WHITESPACE == comment.style()) {
            if (false && printWhitespace) {
                char[] data = comment.getText().toCharArray();
                int n = -1;
                for (int i = 0; i < data.length; i++) {
                    if (data[i] == '\n') {
                        n++;
                    }
                }
                if (n > 0) {
                    if (out.lastBlankLines > 0 && out.lastBlankLines < n)
                        n = out.lastBlankLines;
                    blankLines(n);
                    toLeftMargin();
                }
            }
            return;
        }
        String body = comment.getText();
        boolean rawBody = body.length() == 0 || body.charAt(0) != '/';
        LinkedList<CommentLine> lines = new LinkedList<CommentLine>();
        int stpos = -1;
        int limit = body.length();
        for (int i = 0; i < limit; i++) {
            char c = body.charAt(i);
            if (c == '\n') {
                lines.add(new CommentLine(stpos, stpos < 0 ? 0 : i - stpos, body));
                stpos = -1;
            } else if (c > ' ' && stpos < 0) {
                stpos = i;
            }
        }
        if (stpos >= 0 && stpos < limit)
            lines.add(new CommentLine(stpos, limit - stpos, body));
        if (comment.pos() > 0 && comment.endPos() < diffContext.origText.length() && diffContext.origText.substring(comment.pos() - 1, comment.endPos()).contentEquals("\n" + comment.getText())) {
            if (out.lastBlankLines == 0 && !preceding)
                newline();
            out.toLineStart();
        } else  if (comment.indent() == 0) {
            if (!preceding && out.lastBlankLines == 0 && comment.style() != Style.LINE)
                newline();
            out.toLineStart();
        } else if (comment.indent() > 0 && !preceding) {
            if (out.lastBlankLines == 0 && comment.style() != Style.LINE)
                newline();
            toLeftMargin();
        } else if (comment.indent() < 0 && !preceding) {
            if (out.lastBlankLines == 0)
                newline();
            toLeftMargin();
        } else {
            needSpace();
        }
        if (rawBody) {
            switch (comment.style()) {
                case LINE:
                    print("// ");
                    break;
                case BLOCK:
                    print("/* ");
                    break;
                case JAVADOC:
                    if (!onlyWhitespaces)
                        newline();
                    toLeftMargin();
                    print("/**");
                    newline();
                    toLeftMargin();
                    print(" * ");
            }
        }
        if (!lines.isEmpty())
            lines.removeFirst().print(out.col);
        while (!lines.isEmpty()) {
            newline();
            toLeftMargin();
            CommentLine line = lines.removeFirst();
            if (rawBody)
                print(" * ");
            else if (line.body.charAt(line.startPos) == '*')
                print(' ');
            line.print(out.col);
        }
        if (rawBody) {
            switch (comment.style()) {
                case BLOCK:
                    print(" */");
                    break;
                case JAVADOC:
                    newline();
                    toLeftMargin();
                    print(" */");
                    newline();
                    toLeftMargin();
                    break;
            }
        }
        if ((onlyWhitespaces && !preventClosingWhitespace) || comment.style() == Style.LINE) {
            newline();
            if (!preventClosingWhitespace) {
                toLeftMargin();
            }
        } else {
            if (!preventClosingWhitespace) {
                needSpace();
            }
        }
    }

    private void wrap(String s, WrapStyle wrapStyle) {
        switch(wrapStyle) {
        case WRAP_IF_LONG:
            if (s.length() + out.col <= cs.getRightMargin()) {
                print(' ');
                break;
            }
        case WRAP_ALWAYS:
            newline();
            toColExactly(out.leftMargin + cs.getContinuationIndentSize());
            break;
        case WRAP_NEVER:
            print(' ');
            break;
        }
        print(s);
    }

    private <T extends JCTree> void wrapTrees(List<T> trees, WrapStyle wrapStyle, int wrapIndent) {
        wrapTrees(trees, wrapStyle, wrapIndent, false); //TODO: false for "compatibility", with the previous release, but maybe should be true for everyone?
    }
    
    private <T extends JCTree> void wrapTrees(List<T> trees, WrapStyle wrapStyle, int wrapIndent, boolean wrapFirst) {
        wrapTrees(trees, wrapStyle, wrapIndent, wrapFirst, cs.spaceBeforeComma(), cs.spaceAfterComma(), ","); // NOI18N
    }

    private <T extends JCTree> void wrapTrees(List<T> trees, WrapStyle wrapStyle, int wrapIndent, boolean wrapFirst,
            boolean spaceBeforeSeparator, boolean spaceAfterSeparator, String separator) {
        
        boolean first = true;
        for (List < T > l = trees; l.nonEmpty(); l = l.tail) {
            if (!first) {
                if (spaceBeforeSeparator) {
                    print(' '); // NOI18N
                }
                print(separator);
            }
            
            if (!first || wrapFirst) {
                switch(first && wrapStyle != WrapStyle.WRAP_NEVER ? WrapStyle.WRAP_IF_LONG : wrapStyle) {
                case WRAP_IF_LONG:
                    int rm = cs.getRightMargin();
                    boolean space = spaceAfterSeparator && !first;
                    if (widthEstimator.estimateWidth(l.head, rm - out.col) + out.col + (space ? 1 : 0) <= rm) {
                        if (space)
                            print(' ');
                        break;
                    }
                case WRAP_ALWAYS:
                    newline();
                    toColExactly(wrapIndent);
                    break;
                case WRAP_NEVER:
                    if (spaceAfterSeparator && !first)
                        print(' ');
                    break;
                }
            }
            printNoParenExpr(l.head);
            first = false;
        }
    }
    
    private void wrapAssignOpTree(final String operator, int col, final Runnable print) {
        final boolean spaceAroundAssignOps = cs.spaceAroundAssignOps();
        if (cs.wrapAfterAssignOps()) {
            if (spaceAroundAssignOps)
                print(' ');
            print(operator);
        }
        wrapTree(cs.wrapAssignOps(), spaceAroundAssignOps, cs.alignMultilineAssignment() ? col : out.leftMargin + cs.getContinuationIndentSize(), new Runnable() {
            @Override public void run() {
                if (!cs.wrapAfterAssignOps()) {
                    print(operator);
                    if (spaceAroundAssignOps)
                        print(' ');
                }
                print.run();
            }
        });
    }
    
    private void wrapTree(WrapStyle wrapStyle, boolean needsSpaceBefore, int colAfterWrap, Runnable print) {
        switch(wrapStyle) {
        case WRAP_NEVER:
            if (needsSpaceBefore)
                needSpace();
            print.run();
            return;
        case WRAP_IF_LONG:
            int oldhm = out.harden();
            int oldc = out.col;
            int oldu = out.used;
            int oldm = out.leftMargin;
            int oldPrec = prec;
            try {
                if (needsSpaceBefore)
                    needSpace();
                print.run();
                out.restore(oldhm);
                return;
            } catch(Throwable t) {
                out.restore(oldhm);
                out.col = oldc;
                out.used = oldu;
                out.leftMargin = oldm;
                prec = oldPrec;
            }
        case WRAP_ALWAYS:
            if (out.col > 0)
                newline();
            toColExactly(colAfterWrap);
            print.run();
        }
    }

    public Name fullName(JCTree tree) {
	switch (tree.getTag()) {
	case IDENT:
	    return ((JCIdent) tree).name;
	case SELECT:
            JCFieldAccess sel = (JCFieldAccess)tree;
	    Name sname = fullName(sel.selected);
	    return sname != null && !sname.isEmpty() ? sname.append('.', sel.name) : sel.name;
	default:
	    return null;
	}
    }

    // consider usage of TreeUtilities.isSynthethic() - currently tree utilities
    // is not available in printing class and method is insufficient for our
    // needs.
    private boolean isSynthetic(JCTree tree) {
        if (tree.getKind() == Kind.METHOD) {
            //filter synthetic constructors
            return (((JCMethodDecl)tree).mods.flags & Flags.GENERATEDCONSTR) != 0L;
        }
        //filter synthetic superconstructor calls
        if (tree.getKind() == Kind.EXPRESSION_STATEMENT && diffContext.origUnit != null) {
            JCExpressionStatement est = (JCExpressionStatement) tree;
            if (est.expr.getKind() == Kind.METHOD_INVOCATION) {
                JCMethodInvocation mit = (JCMethodInvocation) est.getExpression();
                if (mit.meth.getKind() == Kind.IDENTIFIER) {
                    JCIdent it = (JCIdent) mit.getMethodSelect();
                    return it.name == names._super && diffContext.syntheticTrees.contains(tree);
                }
            }
        }
	return false;
    }

    /** Is the given tree an enumerator definition? */
    private static boolean isEnumerator(JCTree tree) {
        return tree.getTag() == JCTree.Tag.VARDEF && (((JCVariableDecl) tree).mods.flags & ENUM) != 0;
    }

    private String replace(String a,String b) {
        a = a.replace(b, out.toString());
        out.clear();
        return a;
    }

    private class CommentLine {
	private int startPos;
	private int length;
        private String body;
	CommentLine(int sp, int l, String b) {
	    if((length = l)==0) {
		startPos = 0;
	    } else {
		startPos = sp;
            }
            body = b;
	}
	public void print(int col) {
	    if(length>0) {
		int limit = startPos+length;
		for(int i = startPos; i<limit; i++)
		    out.append(body.charAt(i));
	    }
	}
    }
}
