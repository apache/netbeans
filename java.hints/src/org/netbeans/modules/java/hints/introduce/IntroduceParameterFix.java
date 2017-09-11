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
package org.netbeans.modules.java.hints.introduce;

import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.modules.refactoring.java.api.ui.JavaRefactoringActionsFactory;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jan Becicka
 */
public final class IntroduceParameterFix implements Fix {
    private final TreePathHandle tph;

    public IntroduceParameterFix(TreePathHandle tph) {
        this.tph = tph;
    }

    @Override
    public String getText() {
        return NbBundle.getMessage(IntroduceParameterFix.class, "FIX_IntroduceParameter"); // NOI18N
    }

    @Override
    public ChangeInfo implement() throws Exception {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Node[] activatedNodes = TopComponent.getRegistry().getActivatedNodes();
                if (activatedNodes.length > 0) {
                    EditorCookie ec = activatedNodes[0].getLookup().lookup(EditorCookie.class);
                    Action a = JavaRefactoringActionsFactory.introduceParameterAction().createContextAwareInstance(Lookups.fixed(activatedNodes[0],ec));
                    a.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
                }
                
            }
        });
        return null;
    }

    @Override
    public String toString() {
        return "[Introduce Parameter Fix]";
    }

}