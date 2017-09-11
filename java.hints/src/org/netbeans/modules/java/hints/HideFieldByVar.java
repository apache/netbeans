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

import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav tulach
 */
public class HideFieldByVar extends HideField {
    public HideFieldByVar() {
        super("LocalVariableHidesMemberVariable");
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(DoubleCheck.class, "MSG_HiddenFieldByVar"); // NOI18N
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(DoubleCheck.class, "HINT_HiddenFieldByVar"); // NOI18N
    }

    @Override
    protected List<Fix> computeFixes(CompilationInfo compilationInfo, TreePath treePath, int[] bounds) {
        if (treePath.getLeaf().getKind() != Kind.VARIABLE) {
            return null;
        }
        VariableTree vt = (VariableTree)treePath.getLeaf();
        Element el = compilationInfo.getTrees().getElement(treePath);
        if (el == null) {
            return null;
        }
        if (el.getKind() == ElementKind.FIELD) {
            return null;
        }
        boolean isStatic = false;
        while (el != null && !(el instanceof TypeElement)) {
            isStatic = el.getModifiers().contains(Modifier.STATIC);
            el = el.getEnclosingElement();
        }
        if (el == null) {
            return null;
        }
        if (treePath.getParentPath().getLeaf().getKind() == Kind.METHOD) {
            // skip method values
            return null;
        }

        Element hidden = null;
        for (Element e : getAllMembers(compilationInfo, (TypeElement)el)) {
            if (stop) {
                return null;
            }
            
            if (e.getKind() != ElementKind.FIELD) {
                continue;
            }
            if (isStatic && !e.getModifiers().contains(Modifier.STATIC)) {
                continue;
            }
            if (e.getSimpleName() == vt.getName()) {
                hidden = e;
                break;
            }
        }
        if (hidden == null) {
            return null;
        }

        int[] span = compilationInfo.getTreeUtilities().findNameSpan(vt);
        
        if (span == null) {
            return null;
        }
        List<Fix> fixes = Collections.<Fix>singletonList(new FixImpl(
            (span[1] + span[0]) / 2,
            compilationInfo.getFileObject(),
            true
        ));
        bounds[0] = span[0];
        bounds[1] = span[1];
        return fixes;
    }
    
}
