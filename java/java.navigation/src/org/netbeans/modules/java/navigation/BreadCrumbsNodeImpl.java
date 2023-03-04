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
package org.netbeans.modules.java.navigation;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExportsTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.OpensTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ProvidesTree;
import com.sun.source.tree.RequiresTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.UsesTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import com.sun.source.util.Trees;
import java.awt.Image;
import java.io.CharConversionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.swing.Icon;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.breadcrumbs.spi.BreadcrumbsController;
import org.netbeans.modules.editor.breadcrumbs.spi.BreadcrumbsElement;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbCollections;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;

/**
 *
 * @author Dusan Balek
 */
public class BreadCrumbsNodeImpl implements BreadcrumbsElement {

    private static final String COLOR = "#707070";
    private final Lookup lookup;
    private final BreadCrumbsNodeImpl parent;
    private final TreePathHandle tph;
    private final Callable<? extends Image> iconProvider;
    private final String htmlDisplayName;
//    private OpenAction openAction;

    public BreadCrumbsNodeImpl(final BreadCrumbsNodeImpl parent, final TreePathHandle tph,
        final Image icon, final String htmlDisplayName, final FileObject fileObject, final int[] pos) {
        this(
            parent,
            tph,
            new Callable<Image> () {
                @Override
                @CheckForNull
                public Image call() throws Exception {
                    return icon;
                }
            },
            htmlDisplayName,
            fileObject,
            pos);
    }

    private BreadCrumbsNodeImpl(
        @NullAllowed final BreadCrumbsNodeImpl parent,
        @NonNull final TreePathHandle tph,
        @NonNull final Callable<? extends Image> iconProvider,
        @NullAllowed final String htmlDisplayName,
        @NonNull final FileObject fileObject,
        @NonNull final int[] pos) {
        this.lookup = Lookups.fixed(tph, pos, new OpenableImpl(fileObject, pos[0]));
        this.parent = parent;
        this.tph = tph;
        this.iconProvider = iconProvider;
        this.htmlDisplayName = htmlDisplayName;
    }

    @Override
    public String getHtmlDisplayName() {
        return htmlDisplayName;
    }

    @Override
    public Image getIcon(int type) {
        try {
            return iconProvider.call();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    private static final String CONSTRUCTOR_NAME = "<init>";
    private static final String ERR_NAME = "<error>";
    
    public static BreadCrumbsNodeImpl createBreadcrumbs(BreadCrumbsNodeImpl parent, final CompilationInfo info, TreePath path, boolean elseSection) {
        final Trees trees = info.getTrees();
        final SourcePositions sp = trees.getSourcePositions();
        int[] pos = new int[] {(int) sp.getStartPosition(path.getCompilationUnit(), path.getLeaf()), (int) sp.getEndPosition(path.getCompilationUnit(), path.getLeaf())};
            final Tree leaf = path.getLeaf();
            switch (leaf.getKind()) {
                case COMPILATION_UNIT:
                    TreePathHandle tph = TreePathHandle.create(path, info);
                    return new BreadCrumbsNodeImpl(parent, tph, (Image) null, FileUtil.getFileDisplayName(info.getFileObject()), info.getFileObject(), pos);
                case MODULE:
                    tph = TreePathHandle.create(path, info);
                    return new BreadCrumbsNodeImpl(parent, tph, iconProviderFor(info, path), ((ModuleTree)leaf).getName().toString(), info.getFileObject(), pos);
                case REQUIRES:
                    tph = TreePathHandle.create(path, info);
                    StringBuilder sb = new StringBuilder("requires "); //NOI18N
                    sb.append("<font color=").append(COLOR).append(">"); // NOI18N
                    sb.append(escape(((RequiresTree) leaf).getModuleName().toString()));
                    sb.append("</font>"); //NOI18N
                    return new BreadCrumbsNodeImpl(parent, tph, iconProviderFor(info, path), sb.toString(), info.getFileObject(), pos);
                case EXPORTS:
                    tph = TreePathHandle.create(path, info);
                    sb = new StringBuilder("exports "); //NOI18N
                    sb.append("<font color=").append(COLOR).append(">"); // NOI18N
                    sb.append(escape(((ExportsTree) leaf).getPackageName().toString()));
                    sb.append("</font>"); //NOI18N
                    return new BreadCrumbsNodeImpl(parent, tph, iconProviderFor(info, path), sb.toString(), info.getFileObject(), pos);
                case OPENS:
                    tph = TreePathHandle.create(path, info);
                    sb = new StringBuilder("opens "); //NOI18N
                    sb.append("<font color=").append(COLOR).append(">"); // NOI18N
                    sb.append(escape(((OpensTree) leaf).getPackageName().toString()));
                    sb.append("</font>"); //NOI18N
                    return new BreadCrumbsNodeImpl(parent, tph, iconProviderFor(info, path), sb.toString(), info.getFileObject(), pos);
                case PROVIDES:
                    tph = TreePathHandle.create(path, info);
                    sb = new StringBuilder("provides "); //NOI18N
                    sb.append("<font color=").append(COLOR).append(">"); // NOI18N
                    sb.append(simpleName(((ProvidesTree) leaf).getServiceName()));
                    sb.append("</font>"); //NOI18N
                    return new BreadCrumbsNodeImpl(parent, tph, iconProviderFor(info, path), sb.toString(), info.getFileObject(), pos);
                case USES:
                    tph = TreePathHandle.create(path, info);
                    sb = new StringBuilder("uses "); //NOI18N
                    sb.append("<font color=").append(COLOR).append(">"); // NOI18N
                    sb.append(simpleName(((UsesTree) leaf).getServiceName()));
                    sb.append("</font>"); //NOI18N
                    return new BreadCrumbsNodeImpl(parent, tph, iconProviderFor(info, path), sb.toString(), info.getFileObject(), pos);
                case CLASS:
                case INTERFACE:
                case ENUM:
                case ANNOTATION_TYPE:
                    tph = TreePathHandle.create(path, info);
                    return new BreadCrumbsNodeImpl(parent, tph, iconProviderFor(info, path), className(path), info.getFileObject(), pos);
                case METHOD:
                    tph = TreePathHandle.create(path, info);
                    MethodTree mt = (MethodTree) leaf;
                    CharSequence name;
                    if (mt.getName().contentEquals(CONSTRUCTOR_NAME)) {
                        name = ((ClassTree) path.getParentPath().getLeaf()).getSimpleName();
                    } else {
                        name = mt.getName();
                    }
                    return new BreadCrumbsNodeImpl(parent, tph, iconProviderFor(info, path), name.toString(), info.getFileObject(), pos);
                case VARIABLE:
                    tph = TreePathHandle.create(path, info);
                    return new BreadCrumbsNodeImpl(parent, tph, iconProviderFor(info, path), ((VariableTree) leaf).getName().toString(), info.getFileObject(), pos);
                case CASE:
                    tph = TreePathHandle.create(path, info);
                    ExpressionTree expr = ((CaseTree) leaf).getExpression();
                    sb = new StringBuilder(expr == null ? "default:" : "case "); //NOI18N
                    if (expr != null) {
                        sb.append("<font color=").append(COLOR).append(">"); // NOI18N
                        sb.append(escape(((CaseTree) leaf).getExpression().toString()));
                        sb.append(":"); //NOI18N
                        sb.append("</font>"); //NOI18N
                    }
                    return new BreadCrumbsNodeImpl(parent, tph, DEFAULT_ICON, sb.toString(), info.getFileObject(), pos);
                case CATCH:
                    tph = TreePathHandle.create(path, info);
                    sb = new StringBuilder("catch "); //NOI18N
                    sb.append("<font color=").append(COLOR).append(">"); // NOI18N
                    sb.append(escape(((CatchTree) leaf).getParameter().toString()));
                    sb.append("</font>"); //NOI18N
                    return new BreadCrumbsNodeImpl(parent, tph, DEFAULT_ICON, sb.toString(), info.getFileObject(), pos);
                case DO_WHILE_LOOP:
                    tph = TreePathHandle.create(path, info);
                    sb = new StringBuilder("do ... while "); //NOI18N
                    sb.append("<font color=").append(COLOR).append(">"); // NOI18N
                    sb.append(escape(((DoWhileLoopTree) leaf).getCondition().toString()));
                    sb.append("</font>"); //NOI18N
                    return new BreadCrumbsNodeImpl(parent, tph, DEFAULT_ICON, sb.toString(), info.getFileObject(), pos);
                case ENHANCED_FOR_LOOP:
                    tph = TreePathHandle.create(path, info);
                    sb = new StringBuilder("for "); //NOI18N
                    sb.append("<font color=").append(COLOR).append(">"); // NOI18N
                    sb.append("("); //NOI18N
                    sb.append(escape(((EnhancedForLoopTree) leaf).getVariable().toString()));
                    sb.append(" : "); //NOI18N
                    sb.append(escape(((EnhancedForLoopTree) leaf).getExpression().toString()));
                    sb.append(")"); //NOI18N
                    sb.append("</font>"); //NOI18N
                    return new BreadCrumbsNodeImpl(parent, tph, DEFAULT_ICON, sb.toString(), info.getFileObject(), pos);
                case FOR_LOOP:
                    tph = TreePathHandle.create(path, info);
                    sb = new StringBuilder("for "); //NOI18N
                    sb.append("<font color=").append(COLOR).append(">"); // NOI18N
                    sb.append("("); //NOI18N
                    List<? extends StatementTree> initializer = ((ForLoopTree) leaf).getInitializer();
                    if (!initializer.isEmpty() && initializer.get(0).getKind() == Kind.VARIABLE) {
                        sb.append(escape(initializer.get(0).toString()));
                        for (VariableTree currentVar : NbCollections.checkedListByCopy(initializer.subList(1, initializer.size()), VariableTree.class, true)) {
                            sb.append(", ");
                            sb.append(escape(currentVar.getName().toString()));
                            if (currentVar.getInitializer() != null) {
                                sb.append(" = ");
                                sb.append(escape(currentVar.getInitializer().toString()));
                            }
                        }
                    } else {
                        boolean first = true;
                        for (StatementTree init : initializer) {
                            if (!first) sb.append(", ");
                            if (init.getKind() == Kind.EXPRESSION_STATEMENT) {
                                sb.append(((ExpressionStatementTree) init).getExpression().toString());
                            } else {
                                sb.append(init.toString());
                            }
                            first = false;
                        }
                    }
                    sb.append("; "); //NOI18N
                    if (((ForLoopTree) leaf).getCondition() != null) {
                        sb.append(escape(((ForLoopTree) leaf).getCondition().toString()));
                    }
                    sb.append("; "); //NOI18N
                    boolean first = true;
                    for (ExpressionStatementTree update : ((ForLoopTree) leaf).getUpdate()) {
                        if (!first) sb.append(", ");
                        sb.append(update.getExpression().toString());
                        first = false;
                    }
                    sb.append(")"); //NOI18N
                    sb.append("</font>"); //NOI18N
                    return new BreadCrumbsNodeImpl(parent, tph, DEFAULT_ICON, sb.toString(), info.getFileObject(), pos);
                case IF:
                    tph = TreePathHandle.create(path, info);
                    sb = new StringBuilder(""); //NOI18N
                    Tree last = leaf;
                    while (path != null && path.getLeaf().getKind() == Kind.IF) {
                        StringBuilder temp = new StringBuilder(""); //NOI18N
                        temp.append("if "); //NOI18N
                        temp.append("<font color=").append(COLOR).append(">"); // NOI18N
                        temp.append(escape(((IfTree) path.getLeaf()).getCondition().toString()));
                        temp.append("</font>"); //NOI18N
                        
                        if (((IfTree) path.getLeaf()).getElseStatement() == last || (path.getLeaf() == leaf && elseSection)) {
                            temp.append(" else");
                        }
                        temp.append(" ");
                        sb.insert(0, temp.toString());
                        last = path.getLeaf();
                        path = path.getParentPath();
                    }
                    sb.delete(sb.length() - 1, sb.length());
                    IfTree it = (IfTree) leaf;
                    int elseStart = pos[1] + 1;
                    if (it.getElseStatement() != null) {
                        TokenSequence<JavaTokenId> ts = info.getTokenHierarchy().tokenSequence(JavaTokenId.language());
                        ts.move(elseStart = (int) sp.getStartPosition(path.getCompilationUnit(), it.getElseStatement()));
                        boolean success;
                        while ((success = ts.movePrevious()) && ts.token().id() != JavaTokenId.ELSE)
                            ;
                        elseStart = success ? Math.min(ts.offset(), elseStart) : elseStart;
                    }
                    if (elseSection) {
                        int endPos = (int) sp.getEndPosition(path.getCompilationUnit(), it.getElseStatement());
                        if (it.getElseStatement().getKind() == Kind.IF) {
                            endPos = (int) sp.getStartPosition(path.getCompilationUnit(), it.getElseStatement()) - 1;
                        }
                        pos = new int[] {elseStart, endPos};
                    } else {
                        pos[1] = elseStart - 1;
                    }
                    return new BreadCrumbsNodeImpl(parent, tph, DEFAULT_ICON, sb.toString(), info.getFileObject(), pos);
                case SWITCH:
                    tph = TreePathHandle.create(path, info);
                    sb = new StringBuilder("switch "); //NOI18N
                    sb.append("<font color=").append(COLOR).append(">"); // NOI18N
                    sb.append(escape(((SwitchTree) leaf).getExpression().toString()));
                    sb.append("</font>"); //NOI18N
                    return new BreadCrumbsNodeImpl(parent, tph, DEFAULT_ICON, sb.toString(), info.getFileObject(), pos);
                case SYNCHRONIZED:
                    tph = TreePathHandle.create(path, info);
                    sb = new StringBuilder("synchronized "); //NOI18N
                    sb.append("<font color=").append(COLOR).append(">"); // NOI18N
                    sb.append(escape(((SynchronizedTree) leaf).getExpression().toString()));
                    sb.append("</font>"); //NOI18N
                    return new BreadCrumbsNodeImpl(parent, tph, DEFAULT_ICON, sb.toString(), info.getFileObject(), pos);
                case TRY:
                    tph = TreePathHandle.create(path, info);
                    sb = new StringBuilder("try"); //NOI18N
                    return new BreadCrumbsNodeImpl(parent, tph, DEFAULT_ICON, sb.toString(), info.getFileObject(), pos);
                case WHILE_LOOP:
                    tph = TreePathHandle.create(path, info);
                    sb = new StringBuilder("while "); //NOI18N
                    sb.append("<font color=").append(COLOR).append(">"); // NOI18N
                    sb.append(escape(((WhileLoopTree) leaf).getCondition().toString()));
                    sb.append("</font>"); //NOI18N
                    return new BreadCrumbsNodeImpl(parent, tph, DEFAULT_ICON, sb.toString(), info.getFileObject(), pos);
                case BLOCK:
                    tph = TreePathHandle.create(path, info);
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(path.getParentPath().getLeaf().getKind())) {
                        sb = new StringBuilder(((BlockTree)leaf).isStatic() ? "&lt;static init&gt;" : "&lt;init&gt;"); //NOI18N
                        return new BreadCrumbsNodeImpl(parent, tph, DEFAULT_ICON, sb.toString(), info.getFileObject(), pos);
                    } else if (path.getParentPath().getLeaf().getKind() == Kind.TRY && ((TryTree) path.getParentPath().getLeaf()).getFinallyBlock() == leaf) {
                        sb = new StringBuilder("finally"); //NOI18N
                        return new BreadCrumbsNodeImpl(parent, tph, DEFAULT_ICON, sb.toString(), info.getFileObject(), pos);
                    }
                    break;
            }

            return null;
    }

    private static final Pattern UNICODE_SEQUENCE = Pattern.compile("\\\\u([0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z][0-9a-zA-Z])");
    static String escape(String s) {
        if (s != null) {
            //unescape unicode sequences first (would be better if Pretty would not print them, but that might be more difficult):
            Matcher matcher = UNICODE_SEQUENCE.matcher(s);
            
            if (matcher.find()) {
                StringBuilder result = new StringBuilder();
                int lastReplaceEnd = 0;
                do {
                    result.append(s.substring(lastReplaceEnd, matcher.start()));
                    int ch = Integer.parseInt(matcher.group(1), 16);
                    result.append((char) ch);
                    lastReplaceEnd = matcher.end();
                } while (matcher.find());
                result.append(s.substring(lastReplaceEnd));
                s = result.toString();
            }
            try {
                return XMLUtil.toAttributeValue(s);
            } catch (CharConversionException ex) {
            }
        }
        return null;
    }
    
    private static final Image DEFAULT_ICON = BreadcrumbsController.NO_ICON;
    
    private static Callable<? extends Image> iconProviderFor(CompilationInfo info, TreePath path) {
        Element el = info.getTrees().getElement(path);
        final ElementKind kind = el == null ? null : el.getKind();
        final Set<Modifier> modifiers = el == null ? null : el.getModifiers();
        final ModuleElement.DirectiveKind directiveKind = directiveKind(path.getLeaf().getKind());
        return new Callable<Image>() {
            @Override
            public Image call() throws Exception {
                Icon icon = null;
                if (kind == null) {
                    if (directiveKind == null)
                        return DEFAULT_ICON;
                    icon = ElementIcons.getModuleDirectiveIcon(directiveKind);
                } else {
                    assert modifiers != null;
                    icon = ElementIcons.getElementIcon(kind, modifiers);
                }
                if (icon == null) return DEFAULT_ICON;
                return ImageUtilities.icon2Image(icon);
            }
        };
    }
    
    private static ModuleElement.DirectiveKind directiveKind(Kind treeKind) {
        switch (treeKind) {
            case EXPORTS:
                return ModuleElement.DirectiveKind.EXPORTS;
            case PROVIDES:
                return ModuleElement.DirectiveKind.PROVIDES;
            case REQUIRES:
                return ModuleElement.DirectiveKind.REQUIRES;
            case USES:
                return ModuleElement.DirectiveKind.USES;
        }
        return null;
    }
    
    private static String className(TreePath path) {
        ClassTree ct = (ClassTree) path.getLeaf();
        
        if (path.getParentPath().getLeaf().getKind() == Kind.NEW_CLASS) {
            NewClassTree nct = (NewClassTree) path.getParentPath().getLeaf();
            
            if (nct.getClassBody() == ct) {
                return simpleName(nct.getIdentifier());
            }
        } else if (path.getParentPath().getLeaf() == path.getCompilationUnit()) {
            ExpressionTree pkg = path.getCompilationUnit().getPackageName();
            String pkgName = pkg != null ? pkg.toString() : null;
            if (pkgName != null && !pkgName.contentEquals(ERR_NAME)) {
                return pkgName + '.' + ct.getSimpleName().toString();
            }
        }
        
        return ct.getSimpleName().toString();
    }
    
    private static String simpleName(Tree t) {
        switch (t.getKind()) {
            case PARAMETERIZED_TYPE:
                return simpleName(((ParameterizedTypeTree) t).getType());
            case IDENTIFIER:
                return ((IdentifierTree) t).getName().toString();
            case MEMBER_SELECT:
                return ((MemberSelectTree) t).getIdentifier().toString();
            default:
                return "";//XXX
        }
    }
    
    private final AtomicReference<List<BreadcrumbsElement>> children = new AtomicReference<>();
    
    public List<BreadcrumbsElement> getChildren() {
        List<BreadcrumbsElement> cached = children.get();
        
        if (cached != null) return cached;
        
        final List<BreadcrumbsElement> result = new ArrayList<>();
        try {
            final FileObject file = tph.getFileObject();
            if (file == null) {
                return result;
            }
            JavaSource js = JavaSource.forFileObject(file);

            if (js == null) return result;

            js.runUserActionTask(new Task<CompilationController>() {
                @Override public void run(final CompilationController cc) throws Exception {
                    cc.toPhase(Phase.RESOLVED); //XXX: resolved?

                    final TreePath tp = tph.resolve(cc);

                    if (tp == null) {
                         //XXX: log
                        return;
                    }

                    tp.getLeaf().accept(new ErrorAwareTreeScanner<Void, TreePath>() {
                        @Override public Void scan(Tree node, TreePath p) {
                            if (node == null) return null;
                            if (node.getKind() == Kind.IF) {
                                IfTree it = (IfTree) node;
                                BreadCrumbsNodeImpl n = createBreadcrumbs(BreadCrumbsNodeImpl.this, cc, new TreePath(p, node), false);
                                assert n != null;
                                result.add(n);
                                if (it.getElseStatement() != null) {
                                    n = createBreadcrumbs(BreadCrumbsNodeImpl.this, cc, new TreePath(p, node), true);
                                    assert n != null;
                                    result.add(n);

                                    if (it.getElseStatement().getKind() == Kind.IF) {
                                        scan((IfTree) it.getElseStatement(), new TreePath(p, node));
                                    }
                                }
                                return null;
                            }
                            p = new TreePath(p, node);
                            if (cc.getTreeUtilities().isSynthetic(p)) return null;
                            BreadCrumbsNodeImpl n = createBreadcrumbs(BreadCrumbsNodeImpl.this, cc, p, false);
                            if (n != null) {
                                result.add(n);
                            } else {
                                return super.scan(node, p);
                            }
                            return null;
                        }
                        @Override public Void visitMethod(MethodTree node, TreePath p) {
                            return scan(node.getBody(), p);
                        }
                    }, tp);
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return children.compareAndSet(null, result) ? result : children.get();
    }

    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public BreadcrumbsElement getParent() {
        return parent;
    }
    
    private static final class OpenableImpl implements Openable, OpenCookie {

        private final FileObject file;
        private final int pos;

        public OpenableImpl(FileObject file, int pos) {
            this.file = file;
            this.pos = pos;
        }
        
        @Override
        public void open() {
            UiUtils.open(file, pos);
        }
        
    }
}
