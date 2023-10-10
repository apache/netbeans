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
package org.netbeans.modules.refactoring.java;

import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.swing.Icon;
import javax.swing.text.Position.Bias;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.refactoring.java.plugins.JavaWhereUsedQueryPlugin;
import org.netbeans.modules.refactoring.java.spi.JavaWhereUsedFilters;
import org.netbeans.modules.refactoring.java.spi.JavaWhereUsedFilters.ReadWrite;
import org.netbeans.modules.refactoring.java.ui.UIUtilities;
import org.netbeans.modules.refactoring.java.ui.WhereUsedPanel;
import org.netbeans.modules.refactoring.java.ui.tree.ElementGripFactory;
import org.netbeans.modules.refactoring.spi.FiltersManager;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import static org.netbeans.modules.refactoring.java.Bundle.*;

@NbBundle.Messages({"WARN_ElementNotFound=The destination was not found."})
public class WhereUsedElement extends SimpleRefactoringElementImplementation implements FiltersManager.Filterable {
    private final PositionBounds bounds;
    private final String htmlText;
    private final String elementText;
    private final FileObject parentFile;
    private final JavaWhereUsedFilters.ReadWrite access;
    private final boolean inComment;
    private final boolean inImport;
    private final boolean inPlatform;
    private final boolean inDependency;
    private final boolean inTestclass;

    public WhereUsedElement(PositionBounds bounds, String htmlText, String elementText, FileObject parentFile, TreePath tp, CompilationInfo info, ReadWrite access, boolean inTestclass, boolean inPlatform, boolean inDependency, boolean inComment, boolean inImport) {
        this.bounds = bounds;
        this.htmlText = htmlText;
        this.elementText = elementText;
        this.parentFile = parentFile;
        if (tp != null) {
            ElementGripFactory.getDefault().put(parentFile, tp, info);
        }
        ElementGripFactory.getDefault().put(parentFile, inTestclass);
        this.access = access;
        this.inTestclass = inTestclass;
        this.inPlatform = inPlatform;
        this.inDependency = inDependency;
        this.inComment = inComment;
        this.inImport = inImport;
    }

    @Override
    public String getDisplayText() {
        return htmlText;
    }

    @Override
    public Lookup getLookup() {
        Object composite = null;
        if(bounds != null) {
            composite = ElementGripFactory.getDefault().get(parentFile, bounds.getBegin().getOffset());
        }
        if (composite==null) {
            composite = parentFile;
        }
        Icon icon = null;
        if(access != null) {
            switch(access) {
                case WRITE:
                    icon = ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/java/resources/found_item_write.png", false);
                    break;
                case READ_WRITE:
                    icon = ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/java/resources/found_item_readwrite.png", false);
                    break;
                default:
                case READ:
                    icon = ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/java/resources/found_item_read.png", false);
                    break;
            }
        } else if(inComment) {
            icon = ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/java/resources/found_item_comment.png", false);
        } else if(inImport) {
            icon = ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/java/resources/found_item_import.png", false);
        }
        return icon != null ? Lookups.fixed(composite, icon) : Lookups.singleton(composite);
    }

    @Override
    public PositionBounds getPosition() {
        return bounds;
    }

    @Override
    public String getText() {
        return elementText;
    }

    @Override
    public void performChange() {
    }

    @Override
    public void openInEditor() {
        if(parentFile == null || !parentFile.isValid()) {
             StatusDisplayer.getDefault().setStatusText(WARN_ElementNotFound());
        } else {
            super.openInEditor();
        }
    }

    @Override
    public FileObject getParentFile() {
        return parentFile;
    }

    public JavaWhereUsedFilters.ReadWrite getAccess() {
        return access;
    }
    
    public static WhereUsedElement create(CompilationInfo compiler, TreePath tree, boolean inTest) {
        return create(compiler, tree, inTest, false, false);
    }
    
    public static WhereUsedElement create(CompilationInfo compiler, TreePath tree, boolean inTest, boolean inPlatform, boolean inDependency) {
        return create(compiler, tree, null, inTest, inPlatform, inDependency, new AtomicBoolean());
    }
    
    public static WhereUsedElement create(CompilationInfo compiler, TreePath tree, boolean inTest, boolean inPlatform, boolean inDependency, AtomicBoolean inImport) {
        return create(compiler, tree, null, inTest, inPlatform, inDependency, inImport);
    }
    
    public static WhereUsedElement create(CompilationInfo compiler, TreePath tree, JavaWhereUsedFilters.ReadWrite access, boolean inTest, boolean inPlatform, boolean inDependency, AtomicBoolean inImport) {
        CompilationUnitTree unit = tree.getCompilationUnit();
        CharSequence content = compiler.getSnapshot().getText();
        SourcePositions sp = compiler.getTrees().getSourcePositions();
        Tree t= tree.getLeaf();
        int start;
        int end;
        boolean anonClassNameBug128074 = false;
        TreeUtilities treeUtils = compiler.getTreeUtilities();

        if (t.getKind() == Tree.Kind.IDENTIFIER
                && "super".contentEquals(((IdentifierTree) t).getName()) // NOI18N
                && treeUtils.isSynthetic(tree)) {
            // in case of synthetic constructor call find real constructor or class declaration
            tree = getEnclosingTree(tree);
            if (treeUtils.isSynthetic(tree)) {
                tree = getEnclosingTree(tree.getParentPath());
            }
            t = tree.getLeaf();
        }
        
        boolean elementInImport = false;
        if(t.getKind() == Tree.Kind.IDENTIFIER || t.getKind() == Tree.Kind.MEMBER_SELECT) {
            TreePath enclosingTree = getEnclosingImportTree(tree);
            if(enclosingTree != null) {
                elementInImport = true;
                inImport.set(true);
            }
        }

        if (TreeUtilities.CLASS_TREE_KINDS.contains(t.getKind())) {
            int[] pos = treeUtils.findNameSpan((ClassTree)t);
            if (pos == null) {
                Tree tr = tree.getParentPath().getLeaf();
                if (tr instanceof NewClassTree) {
                    NewClassTree newClass = (NewClassTree) tr;
                    start = (int) sp.getStartPosition(unit, newClass.getIdentifier());
                    end = (int) sp.getEndPosition(unit, newClass.getIdentifier());
                } else {
                    //#121084 hotfix
                    //happens for anonymous innerclasses
                    anonClassNameBug128074 = true;
                    start = end = (int) sp.getStartPosition(unit, t);
                }
                // #213723 hotfix, happens for enum values
                if(start < 0) {
                    TreePath parentPath = tree.getParentPath();
                    if(parentPath != null && (parentPath = parentPath.getParentPath()) != null
                            && parentPath.getLeaf().getKind() == Tree.Kind.VARIABLE) {
                        VariableTree enum_var = (VariableTree) parentPath.getLeaf();
                        pos = treeUtils.findNameSpan(enum_var);
                        if (pos == null) {
                            //#121084 hotfix
                            start = end = (int) sp.getStartPosition(unit, enum_var);
                        } else {
                            start = pos[0];
                            end = pos[1];
                        }
                    }
                }
            } else {
                start = pos[0];
                end = pos[1];
            }
        } else if (t.getKind() == Tree.Kind.METHOD) {
            int[] pos = treeUtils.findNameSpan((MethodTree)t);
            if (pos == null) {
                //#121084 hotfix
                start = end = (int) sp.getStartPosition(unit, t);
            } else {
                start = pos[0];
                end = pos[1];
            }
        } else if (t.getKind() == Tree.Kind.NEW_CLASS) {
            Tree ident = ((NewClassTree)t).getIdentifier();
            if (ident.getKind() == Tree.Kind.PARAMETERIZED_TYPE) {
                ident = ((ParameterizedTypeTree)ident).getType();
            }
            if (ident.getKind()== Tree.Kind.MEMBER_SELECT) {
                int[] pos = treeUtils.findNameSpan((MemberSelectTree) ident);
                if (pos == null) {
                    //#121084 hotfix
                    start = end = (int) sp.getStartPosition(unit, ident);
                } else {
                    start = pos[0];
                    end = pos[1];
                }
            } else {
                TreePath varTreePath = tree.getParentPath();
                Tree varTree = varTreePath.getLeaf();
                Trees trees = compiler.getTrees();
                Element element = trees.getElement(varTreePath);
                if (element != null && varTree.getKind() == Tree.Kind.VARIABLE && element.getKind() == ElementKind.ENUM_CONSTANT) {
                    int[] pos = treeUtils.findNameSpan((VariableTree)varTree);
                    if (pos == null) {
                        //#121084 hotfix
                        start = end = (int) sp.getStartPosition(unit, varTree);
                    } else {
                        start = pos[0];
                        end = pos[1];
                    }
                } else {
                    start = (int) sp.getStartPosition(unit, ident);
                    end = (int) sp.getEndPosition(unit, ident);
                }
            }
        } else if (t.getKind() == Tree.Kind.MEMBER_SELECT) {
            int[] pos = treeUtils.findNameSpan((MemberSelectTree) t);
            if (pos == null) {
                //#121084 hotfix
                start = end = (int) sp.getStartPosition(unit, t);
            } else {
                start = pos[0];
                end = pos[1];
            }
        } else {
            start = (int) sp.getStartPosition(unit, t);
            end = (int) sp.getEndPosition(unit, t);
            if (end == -1) {
                if (!compiler.getTreeUtilities().isSynthetic(tree)) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new RuntimeException("Cannot get end position for " + t.getClass().getName() + " " + t + " file:" + compiler.getFileObject().getPath())); // NOI18N
                }
                end = start;
            }
        }

        assert start>0:"Cannot find start position in file " + unit.getSourceFile().getName() + "\n tree=" + tree.toString();
        assert end>0:"Cannot find end position in file " + unit.getSourceFile().getName() + "\n tree=" + tree.toString();
        LineMap lm = tree.getCompilationUnit().getLineMap();
        long line = lm.getLineNumber(start);
        long endLine = lm.getLineNumber(end);
        long sta = lm.getStartPosition(line);
        int eof = content.length();
        long lastLine = lm.getLineNumber(eof);
        long en = lastLine > endLine ? lm.getStartPosition(endLine + 1) - 1 : eof;
        StringBuilder sb = new StringBuilder();
        sb.append(UIUtilities.getHtml(trimStart(content.subSequence((int) sta, start).toString())));
        sb.append("<b>"); //NOI18N
        sb.append(content.subSequence(start, end));
        sb.append("</b>");//NOI18N
        sb.append(UIUtilities.getHtml(trimEnd(content.subSequence(end, (int) en).toString())));
        
        DataObject dob = null;
        try {
            dob = DataObject.find(compiler.getFileObject());
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        CloneableEditorSupport ces = JavaWhereUsedQueryPlugin.findCloneableEditorSupport(dob);
        PositionRef ref1 = ces.createPositionRef(start, Bias.Forward);
        PositionRef ref2 = ces.createPositionRef(end, Bias.Forward);
        PositionBounds bounds = new PositionBounds(ref1, ref2);
        TreePath tr = getEnclosingTree(tree);
        return new WhereUsedElement(
                bounds,
                start==end && anonClassNameBug128074 ? NbBundle.getMessage(WhereUsedPanel.class, "LBL_AnonymousClass"):sb.toString().trim(),
                start==end && anonClassNameBug128074 ? NbBundle.getMessage(WhereUsedPanel.class, "LBL_AnonymousClass"):content.subSequence((int)sta, (int)en).toString().trim(),
                compiler.getFileObject(),
                tr,
                compiler, access, inTest, inPlatform,inDependency, false, elementInImport);
    }
    
    private static String trimStart(String s) {
        for (int x = 0; x < s.length(); x++) {
            if (!Character.isWhitespace(s.charAt(x))) {
                return s.substring(x);
            }
        }
        return "";
    }
    
    private static String trimEnd(String s) {
        for (int x = s.length()-1; x >=0; x--) {
            if (!Character.isWhitespace(s.charAt(x))) {
                return s.substring(0, x + 1);
            }
        }
        return "";
    }
    
    public static WhereUsedElement create(int start, int end, CompilationInfo compiler, boolean inTest, boolean inPlatform, boolean inDependency) {
        CharSequence content = compiler.getSnapshot().getText();
        LineMap lm = compiler.getCompilationUnit().getLineMap();
        long line = lm.getLineNumber(start);
        long endLine = lm.getLineNumber(end);
        long sta = lm.getStartPosition(line);
        int eof = content.length();
        long lastLine = lm.getLineNumber(eof);
        long en = lastLine > endLine ? lm.getStartPosition(endLine + 1) - 1 : eof;
        StringBuilder sb = new StringBuilder();
        sb.append(UIUtilities.getHtml(trimStart(content.subSequence((int) sta, start).toString())));
        sb.append("<b>"); //NOI18N
        sb.append(content.subSequence(start, end));
        sb.append("</b>");//NOI18N
        sb.append(UIUtilities.getHtml(trimEnd(content.subSequence(end, (int) en).toString())));
        
        DataObject dob = null;
        try {
            dob = DataObject.find(compiler.getFileObject());
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        CloneableEditorSupport ces = JavaWhereUsedQueryPlugin.findCloneableEditorSupport(dob);
        PositionRef ref1 = ces.createPositionRef(start, Bias.Forward);
        PositionRef ref2 = ces.createPositionRef(end, Bias.Forward);
        PositionBounds bounds = new PositionBounds(ref1, ref2);
        return new WhereUsedElement(bounds, sb.toString().trim(),
                content.subSequence((int)sta, (int)en).toString(),
                compiler.getFileObject(), null, compiler, null, inTest, inPlatform, inDependency, true, false);
    }
    
    private static TreePath getEnclosingImportTree(TreePath tp) {
        while(tp != null) {
            Tree tree = tp.getLeaf();
            if (tree.getKind() == Tree.Kind.IMPORT) {
                return tp;
            }
            tp = tp.getParentPath();
        }
        return null;
    }
    
    private static TreePath getEnclosingTree(TreePath tp) {
        while(tp != null) {
            Tree tree = tp.getLeaf();
            if (TreeUtilities.CLASS_TREE_KINDS.contains(tree.getKind()) || tree.getKind() == Tree.Kind.METHOD || tree.getKind() == Tree.Kind.IMPORT || tree.getKind() == Tree.Kind.VARIABLE) {
                return tp;
            } 
            tp = tp.getParentPath();
        }
        return null;
    }

    @Override
    public boolean filter(FiltersManager manager) {
        boolean show = true;

        if(JavaWhereUsedQueryPlugin.DEPENDENCIES) {
            if (inPlatform) {
                show = show && manager.isSelected(JavaWhereUsedFilters.PLATFORM.getKey());
            }

            if (inDependency) {
                show = show && manager.isSelected(JavaWhereUsedFilters.DEPENDENCY.getKey());
            }
        }

        if (inTestclass) {
            show = show && manager.isSelected(JavaWhereUsedFilters.TESTFILE.getKey());
        }

        show = show && manager.isSelected(JavaWhereUsedFilters.SOURCEFILE.getKey());

        if (access != null) {
            show = show && manager.isSelected(access.getKey());
        }
        
        if (inComment) {
            show = show && manager.isSelected(JavaWhereUsedFilters.COMMENT.getKey());
        }
        
        if (inImport) {
            show = show && manager.isSelected(JavaWhereUsedFilters.IMPORT.getKey());
        }
        return show;
    }
}
