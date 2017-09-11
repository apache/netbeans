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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class EmptyCancelForCancellableTask extends AbstractHint {

    public EmptyCancelForCancellableTask() {
        super(false, true, HintSeverity.WARNING);
    }

    public String getDescription() {
        return NbBundle.getMessage(EmptyCancelForCancellableTask.class, "DSC_EmptyCancel");
    }

    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.METHOD);
    }

    private static Set<String> typesToCheck = new HashSet<String>(
            Arrays.asList(
                "org.netbeans.api.java.source.CancellableTask<org.netbeans.api.java.source.CompilationInfo>", //NOI18N
                "org.netbeans.modules.java.hints.spi.Rule" //NOI18N
            )
    );
    
    public List<ErrorDescription> run(CompilationInfo compilationInfo, TreePath treePath) {
        Element e = compilationInfo.getTrees().getElement(treePath);
        
        if (   e == null
            || e.getKind() != ElementKind.METHOD
            || !"cancel".equals(e.getSimpleName().toString()) //NOI18N
            || e.getModifiers().contains(Modifier.ABSTRACT)) {
            return null;
        }
        
        Element clazz = e.getEnclosingElement();
        
        if (!clazz.getKind().isClass()) {
            return null;
        }
        
        boolean found = false;
        
        OUT: for (String toCheck : typesToCheck) {
            TypeElement clazzTE = (TypeElement) clazz;
            TypeMirror  clazzTM = clazzTE.asType();
            TypeMirror  typeToCheck = compilationInfo.getTreeUtilities().parseType(toCheck, clazzTE);
            
            if (typeToCheck.getKind() != TypeKind.DECLARED)
                continue;
            
            TypeElement typeToCheckTE = (TypeElement) ((DeclaredType) typeToCheck).asElement();
            
            if (   compilationInfo.getTypes().isSubtype(clazzTM, typeToCheck)
                && !clazzTM.equals(typeToCheck)) {
                for (ExecutableElement ee : ElementFilter.methodsIn(typeToCheckTE.getEnclosedElements())) {
                    if (compilationInfo.getElements().overrides((ExecutableElement) e, ee, clazzTE)) {
                        found = true;
                        break OUT;
                    }
                }
            }
        }
        
        if (!found) {
            return null;
        }
        
        MethodTree mt = (MethodTree) treePath.getLeaf();
        
        if (mt.getBody() == null || !mt.getBody().getStatements().isEmpty()) {
            return null;
        }
        
        int[] span = compilationInfo.getTreeUtilities().findNameSpan((MethodTree) treePath.getLeaf());

        if (span != null) {
            String message = NbBundle.getMessage(EmptyCancelForCancellableTask.class, "MSG_EmptyCancel");
            ErrorDescription ed = ErrorDescriptionFactory.createErrorDescription(getSeverity().toEditorSeverity(), message, compilationInfo.getFileObject(), span[0], span[1]);

            return Collections.singletonList(ed);
        }
        
        return null;
    }

    public String getId() {
        return EmptyCancelForCancellableTask.class.getName();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(EmptyCancelForCancellableTask.class, "LBL_EmptyCancel");
    }

    public void cancel() {
    }

}
