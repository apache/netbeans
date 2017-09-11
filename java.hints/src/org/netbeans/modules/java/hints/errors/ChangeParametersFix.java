/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.Tree;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring.ParameterInfo;
import org.netbeans.modules.refactoring.java.api.ui.JavaRefactoringActionsFactory;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import static org.netbeans.modules.java.hints.errors.Bundle.*;

/**
 *
 * @author Ralph Ruijs
 */
@NbBundle.Messages({"LBL_FIX_ChangeMethodParameters=Change Method Signature from {0} to {1}",
                    "LBL_FIX_ChangeConstructorParameters=Change constructor from {0} to {1}"})
public final class ChangeParametersFix implements Fix {
    private final boolean doFullRefactoring;
    private final TreePathHandle tph;
    private final String declaration;
    private final String newDeclaration;
    private final ChangeParametersRefactoring.ParameterInfo[] newParameterInfo;
    private final Set<Modifier> modifiers;
    private final boolean isConstr;

    public ChangeParametersFix(boolean doFullRefactoring, TreePathHandle tph, Set<Modifier> modifiers, String declaration, String newDeclaration, ParameterInfo[] newParameterInfo, boolean isConstr) {
        this.doFullRefactoring = doFullRefactoring;
        this.tph = tph;
	this.modifiers = modifiers;
        this.declaration = declaration;
        this.newDeclaration = newDeclaration;
        this.newParameterInfo = newParameterInfo;
        this.isConstr = isConstr;
    }

    @Override
    public String getText() {
        return isConstr? LBL_FIX_ChangeConstructorParameters(declaration, newDeclaration) : LBL_FIX_ChangeMethodParameters(declaration, newDeclaration); // NOI18N
    }

    @Override
    public ChangeInfo implement() throws Exception {
        if(doFullRefactoring) {
            doFullChangeMethodParameters(tph, newParameterInfo);
            return null;
        }
        ChangeParametersRefactoring refactoring = new ChangeParametersRefactoring(tph);
        refactoring.setParameterInfo(new ChangeParametersRefactoring.ParameterInfo[]{});
        refactoring.setModifiers(modifiers);
        refactoring.setParameterInfo(newParameterInfo);
        RefactoringSession session = RefactoringSession.create("ChangeMethodParameterHint"); // NOI18N
        Problem problem = null;
        problem = refactoring.preCheck();
        if (problem != null) {
            doFullChangeMethodParameters(tph, newParameterInfo);
            return null;
        }
        problem = refactoring.prepare(session);
        if (problem != null) {
            doFullChangeMethodParameters(tph, newParameterInfo);
            return null;
        }
        problem = session.doRefactoring(false);
        if (problem != null) {
            doFullChangeMethodParameters(tph, newParameterInfo);
            return null;
        }
        return null;
    }
    
    private static void doFullChangeMethodParameters(TreePathHandle tph, ParameterInfo[] newParameterInfo) {
        InstanceContent ic = new InstanceContent();
        ic.add(tph);
        ic.add(new AbstractNode(Children.LEAF, Lookups.singleton(tph)));
        for (ParameterInfo parameterInfo : newParameterInfo) {
            ic.add(parameterInfo);
        }
        Lookup actionContext = new AbstractLookup(ic);
        final Action a = JavaRefactoringActionsFactory.changeParametersAction().createContextAwareInstance(actionContext);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                a.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
            }
        });
    }
}
