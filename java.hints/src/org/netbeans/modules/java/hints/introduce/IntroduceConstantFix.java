/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.introduce;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.swing.JButton;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.StopProcessing;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 * Refactored from IntroduceFix originally by lahvac
 *
 * @author sdedic
 */
public class IntroduceConstantFix extends IntroduceFieldFix {

    static TreePath findAcceptableConstantTarget(CompilationInfo info, TreePath from) {
        boolean compileTimeConstant = info.getTreeUtilities().isCompileTimeConstantExpression(from);
        while (from != null) {
            if (TreeUtilities.CLASS_TREE_KINDS.contains(from.getLeaf().getKind())) {
                if (from.getParentPath().getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
                    return from;
                }
                if (compileTimeConstant || ((ClassTree) from.getLeaf()).getModifiers().getFlags().contains(Modifier.STATIC)) {
                    return from;
                }
            }
            from = from.getParentPath();
        }
        return null;
    }

    @Override
    public String getText() {
        return NbBundle.getMessage(IntroduceConstantFix.class, "FIX_IntroduceConstant");
    }
    
    /**
     * Creates an 'introduce constant' fix.
     *
     * Note: the fix will not reference CompilationInfo and will remember only handles to TreePaths.
     *
     * @param resolved the path for expression or variable declaration to convert
     * @param info compilation context
     * @param value the actual expression or a variable initializer.
     * @param guessedName proposed name
     * @param numDuplicates number of other duplicates
     * @param offset offset for the hint
     * @param variableRewrite if variable name should be changed (?)
     * @param cancel cancel flag
     * @return
     */
    static IntroduceFieldFix createConstant(TreePath resolved, CompilationInfo info, TreePath value, String guessedName, int numDuplicates, int offset, boolean variableRewrite, AtomicBoolean cancel) {
        CodeStyle cs = CodeStyle.getDefault(info.getFileObject());
        boolean isConstant = checkConstantExpression(info, value);
        TreePath constantTarget = isConstant ? findAcceptableConstantTarget(info, resolved) : null;
        if (!isConstant || constantTarget == null || cancel.get()) {
            return null;
        }
        TreePathHandle h = TreePathHandle.create(resolved, info);
        String varName;
        if (variableRewrite) {
            varName = guessedName;
        } else {
            String proposed = Utilities.toConstantName(guessedName);
            varName = Utilities.makeNameUnique(info, info.getTrees().getScope(constantTarget), proposed, cs.getStaticFieldNamePrefix(), cs.getStaticFieldNameSuffix());
        }
        ClassTree clazz = (ClassTree)constantTarget.getLeaf();
        Element el = info.getTrees().getElement(constantTarget);
        if (el == null || !(el.getKind().isClass() || el.getKind().isInterface())) {
            return null;
        }
        IntroduceConstantFix fix = new IntroduceConstantFix(h, info.getJavaSource(), varName, numDuplicates, offset, TreePathHandle.create(constantTarget, info));
        fix.setTargetIsInterface(clazz.getKind() == Tree.Kind.INTERFACE);
        return fix;
    }

    static boolean checkConstantExpression(final CompilationInfo info, TreePath path) {
        InstanceRefFinder finder = new InstanceRefFinder(info, path) {
            @Override
            public Object visitIdentifier(IdentifierTree node, Object p) {
                Element el = info.getTrees().getElement(getCurrentPath());
                if (el == null || el.asType() == null || el.asType().getKind() == TypeKind.ERROR) {
                    return null;
                }
                if (el.getKind() == ElementKind.LOCAL_VARIABLE || el.getKind() == ElementKind.PARAMETER) {
                    throw new StopProcessing();
                } else if (el.getKind() == ElementKind.FIELD) {
                    if (!el.getModifiers().contains(Modifier.FINAL)) {
                        throw new StopProcessing();
                    }
                }
                return super.visitIdentifier(node, p);
            }
        };
        try {
            finder.process();
            return  !(finder.containsInstanceReferences() || finder.containsLocalReferences() || finder.containsReferencesToSuper());
        } catch (StopProcessing e) {
            return false;
        }
    }

    public IntroduceConstantFix(TreePathHandle handle, JavaSource js, String guessedName, int numDuplicates, int offset, TreePathHandle target) {
        super(handle, js, guessedName, numDuplicates, null, true, true, offset, true, target);
    }

    @Override
    public String toString() {
        return "[IntroduceFix:" + guessedName + ":" + duplicatesCount + ":" + IntroduceKind.CREATE_CONSTANT + "]"; // NOI18N
    }

    @Override
    protected String getCaption() {
        return NbBundle.getMessage(IntroduceHint.class, "CAP_IntroduceConstant");
    }

    @Override
    protected IntroduceFieldPanel createPanel(JButton btnOk) {
        return new IntroduceFieldPanel(
                guessedName, null,
                duplicatesCount, 
                true,
                handle.getKind() == Tree.Kind.VARIABLE, 
                IntroduceFieldPanel.CONSTANT, "introduceField", btnOk);
    }

    @Override
    protected TreePath findTargetClass(WorkingCopy copy, TreePath resolved) {
        return findAcceptableConstantTarget(copy, resolved);
    }
    
    
    
}
