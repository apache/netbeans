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
package org.netbeans.modules.java.hints.introduce;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.swing.JButton;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.StopProcessing;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.ParseException;
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
        IntroduceConstantFix fix = new IntroduceConstantFix(h, info.getSnapshot().getSource(), varName, numDuplicates, offset, TreePathHandle.create(constantTarget, info));
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

    public IntroduceConstantFix(TreePathHandle handle, Source source, String guessedName, int numDuplicates, int offset, TreePathHandle target) {
        super(handle, source, guessedName, numDuplicates, null, true, true, offset, true, target);
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

    @Override
    public ModificationResult getModificationResult() throws ParseException {
        return ModificationResult.runModificationTask(Collections.singleton(source), new Worker(guessedName, permitDuplicates, true, EnumSet.of(Modifier.PRIVATE), IntroduceFieldPanel.INIT_FIELD, null, false));
    }
}
