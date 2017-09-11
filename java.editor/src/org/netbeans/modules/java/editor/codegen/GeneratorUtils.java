/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.java.editor.codegen;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.editor.GuardedException;
import org.netbeans.editor.Utilities;
import org.openide.DialogDescriptor;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda, Dusan Balek
 */
public class GeneratorUtils {
    
    private static final ErrorManager ERR = ErrorManager.getDefault().getInstance(GeneratorUtils.class.getName());
    public static final int GETTERS_ONLY = 1;
    public static final int SETTERS_ONLY = 2;

    private GeneratorUtils() {
    }
    
    public static void generateAllAbstractMethodImplementations(WorkingCopy wc, TreePath path) {
        assert TreeUtilities.CLASS_TREE_KINDS.contains(path.getLeaf().getKind());
        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
        if (te != null) {
            ClassTree clazz = (ClassTree)path.getLeaf();
            GeneratorUtilities gu = GeneratorUtilities.get(wc);
            ElementUtilities elemUtils = wc.getElementUtilities();
            clazz = gu.insertClassMembers(clazz, gu.createAbstractMethodImplementations(te, elemUtils.findUnimplementedMethods(te)));
            wc.rewrite(path.getLeaf(), clazz);
        }
    }
    
    public static void generateAbstractMethodImplementations(WorkingCopy wc, TreePath path, List<? extends ExecutableElement> elements, int offset) {
        assert TreeUtilities.CLASS_TREE_KINDS.contains(path.getLeaf().getKind());
        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
        if (te != null) {
            ClassTree clazz = (ClassTree)path.getLeaf();
            wc.rewrite(clazz, insertClassMembers(wc, clazz, GeneratorUtilities.get(wc).createAbstractMethodImplementations(te, elements), offset));
        }
    }

    public static void generateAbstractMethodImplementation(WorkingCopy wc, TreePath path, ExecutableElement element, int offset) {
        assert TreeUtilities.CLASS_TREE_KINDS.contains(path.getLeaf().getKind());
        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
        if (te != null) {
            ClassTree clazz = (ClassTree)path.getLeaf();
            wc.rewrite(clazz, insertClassMember(wc, clazz, GeneratorUtilities.get(wc).createAbstractMethodImplementation(te, element), offset));
        }
    }
    
    public static void generateMethodOverrides(WorkingCopy wc, TreePath path, List<? extends ExecutableElement> elements, int offset) {
        assert TreeUtilities.CLASS_TREE_KINDS.contains(path.getLeaf().getKind());
        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
        if (te != null) {
            ClassTree clazz = (ClassTree)path.getLeaf();
            wc.rewrite(clazz, insertClassMembers(wc, clazz, GeneratorUtilities.get(wc).createOverridingMethods(te, elements), offset));
        }
    }
    
    public static void generateMethodOverride(WorkingCopy wc, TreePath path, ExecutableElement element, int offset) {
        assert TreeUtilities.CLASS_TREE_KINDS.contains(path.getLeaf().getKind());
        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
        if (te != null) {
            ClassTree clazz = (ClassTree)path.getLeaf();
            wc.rewrite(clazz, insertClassMember(wc, clazz, GeneratorUtilities.get(wc).createOverridingMethod(te, element), offset));
        }
    }

    public static void generateConstructor(WorkingCopy wc, TreePath path, Iterable<? extends VariableElement> initFields, ExecutableElement inheritedConstructor, int offset) {
        ClassTree clazz = (ClassTree)path.getLeaf();
        TypeElement te = (TypeElement) wc.getTrees().getElement(path);
        Tree c2 = wc.resolveRewriteTarget(clazz);
        // hack in case the class was already rewritten, i.e. by create subclass generator.
        if (c2 instanceof ClassTree && clazz != c2) {
            clazz = (ClassTree)c2;
        }
        wc.rewrite(clazz, insertClassMembers(wc, clazz, Collections.singletonList(GeneratorUtilities.get(wc).createConstructor(te, initFields, inheritedConstructor)), offset));
    }
    
    public static void generateConstructors(WorkingCopy wc, TreePath path, Iterable<? extends VariableElement> initFields, List<? extends ExecutableElement> inheritedConstructors, int offset) {
        ClassTree clazz = (ClassTree)path.getLeaf();
        TypeElement te = (TypeElement) wc.getTrees().getElement(path);
        GeneratorUtilities gu = GeneratorUtilities.get(wc);
        List<Tree> members = new ArrayList<>();
        for (ExecutableElement inheritedConstructor : inheritedConstructors) {
            members.add(gu.createConstructor(te, initFields, inheritedConstructor));
        }
        wc.rewrite(clazz, insertClassMembers(wc, clazz, members, offset));
    }
    
    public static void generateGettersAndSetters(WorkingCopy wc, TreePath path, Iterable<? extends VariableElement> fields, int type, int offset) {
        assert TreeUtilities.CLASS_TREE_KINDS.contains(path.getLeaf().getKind());
        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
        if (te != null) {
            GeneratorUtilities gu = GeneratorUtilities.get(wc);
            ClassTree clazz = (ClassTree)path.getLeaf();
            List<Tree> members = new ArrayList<>();
            for(VariableElement element : fields) {
                if (type != SETTERS_ONLY) {
                    members.add(gu.createGetter(te, element));
                }
                if (type != GETTERS_ONLY) {
                    members.add(gu.createSetter(te, element));
                }
            }
            wc.rewrite(clazz, insertClassMembers(wc, clazz, members, offset));
        }
    }

    public static ClassTree insertClassMembers(WorkingCopy wc, ClassTree clazz, List<? extends Tree> members, int offset) throws IllegalStateException {
        if (members.isEmpty()) {
            return clazz;
        }
        for (Tree member : members) {
            Tree dup = checkDuplicates(wc, clazz, member);
            if (dup != null) {
                throw new DuplicateMemberException((int) wc.getTrees().getSourcePositions().getStartPosition(wc.getCompilationUnit(), dup));
            }
        }
        return GeneratorUtilities.get(wc).insertClassMembers(clazz, members, offset);
    }
    
    public static ClassTree insertClassMember(WorkingCopy wc, ClassTree clazz, Tree member, int offset) throws IllegalStateException {
        return insertClassMembers(wc, clazz, Collections.singletonList(member), offset);
    }
    
    /**
     * @param info tested file's info
     * @return true if SourceVersion of source represented by provided info supports Override
     */
    public static boolean supportsOverride(@NonNull CompilationInfo info) {
        return SourceVersion.RELEASE_5.compareTo(info.getSourceVersion()) <= 0
               && info.getElements().getTypeElement("java.lang.Override") != null;
    }

    private static List<TypeElement> getAllClasses(TypeElement of) {
        List<TypeElement> result = new ArrayList<>();
        TypeMirror sup = of.getSuperclass();
        TypeElement te = sup.getKind() == TypeKind.DECLARED ? (TypeElement) ((DeclaredType)sup).asElement() : null;
        
        result.add(of);
        
        if (te != null) {
            result.addAll(getAllClasses(te));
        } else {
            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ERR.log(ErrorManager.INFORMATIONAL, "te=null, t=" + of);
            }
        }
        
        return result;
    }
    
    static DialogDescriptor createDialogDescriptor( JComponent content, String label ) {
        final JButton[] buttons = new JButton[2];
        buttons[0] = new JButton(NbBundle.getMessage(GeneratorUtils.class, "LBL_generate_button") );
	buttons[0].getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GeneratorUtils.class, "A11Y_Generate"));
        buttons[1] = new JButton(NbBundle.getMessage(GeneratorUtils.class, "LBL_cancel_button") );
        final DialogDescriptor dd = new DialogDescriptor(content, label, true, buttons, buttons[0], DialogDescriptor.DEFAULT_ALIGN, null, null);
        dd.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (DialogDescriptor.PROP_VALID.equals(evt.getPropertyName())) {
                    buttons[0].setEnabled(dd.isValid());
                }
            }
        });
        return dd;
    }
    
    public static void guardedCommit(JTextComponent component, ModificationResult mr) throws IOException {
        try {
            mr.commit();
        } catch (IOException e) {
            if (e.getCause() instanceof GuardedException) {
                String message = NbBundle.getMessage(GeneratorUtils.class, "ERR_CannotApplyGuarded");

                Utilities.setStatusBoldText(component, message);
                Logger.getLogger(GeneratorUtils.class.getName()).log(Level.FINE, null, e);
            }
        }
    }
    
    private static Tree checkDuplicates(WorkingCopy wc, ClassTree clazz, Tree member) {
        List<? extends VariableTree> memberParams = null;
        TreePath tp = null;
        outer: for (Tree tree : clazz.getMembers()) {
            if (tp == null) {
                tp = new TreePath(wc.getCompilationUnit());
            }
            if (tree.getKind() == member.getKind() && !wc.getTreeUtilities().isSynthetic(new TreePath(tp, tree))) {
                switch (member.getKind()) {
                    case CLASS:
                        if (((ClassTree)member).getSimpleName().contentEquals(((ClassTree)tree).getSimpleName())) {
                            return tree;
                        }
                        break;
                    case VARIABLE:
                        if (((VariableTree)member).getName().contentEquals(((VariableTree)tree).getName())) {
                            return tree;
                        }
                        break;
                    case METHOD:
                        if (((MethodTree)member).getName().contentEquals(((MethodTree)tree).getName())) {
                            if (memberParams == null) {
                                memberParams = ((MethodTree)member).getParameters();
                            }
                            List<? extends VariableTree> treeParams = ((MethodTree)tree).getParameters();
                            if (memberParams.size() == treeParams.size()) {
                                Iterator<? extends VariableTree> memberIt = memberParams.iterator();
                                Iterator<? extends VariableTree> treeIt = treeParams.iterator();
                                while (memberIt.hasNext() && treeIt.hasNext()) {
                                    TypeMirror mTM = wc.getTrees().getTypeMirror(new TreePath(tp, memberIt.next().getType()));
                                    TypeMirror tTM = wc.getTrees().getTypeMirror(new TreePath(tp, treeIt.next().getType()));
                                    if (!wc.getTypes().isSameType(mTM, tTM)) {
                                        continue outer;
                                    }
                                }
                                return tree;
                            }
                        }
                        break;
                }
            }
        }
        return null;
    }
    
    public static class DuplicateMemberException extends IllegalStateException {
        private int pos;

        public DuplicateMemberException(int pos) {
            super("Class member already exists");
            this.pos = pos;
        }

        public int getPos() {
            return pos;
        }
    }
}
