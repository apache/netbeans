/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.project.ui.problems;

import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.ProjectProblems;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Zezula
 */
@ActionID(id = "org.netbeans.modules.project.ui.problems.BrokenProjectActionFactory", category = "Project")
@ActionRegistration(displayName = "#LBL_Fix_Broken_Links_Action", lazy=false)
public class BrokenProjectActionFactory extends AbstractAction implements ContextAwareAction {

    public BrokenProjectActionFactory() {
        putValue(Action.NAME, NbBundle.getMessage(BrokenProjectActionFactory.class, "LBL_Fix_Broken_Links_Action"));
        setEnabled(false);
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        throw new IllegalStateException();
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        final Collection<? extends Project> p = actionContext.lookupAll(Project.class);
        if (p.size() != 1) {
            return this;
        }
        return new BrokenProjectAction(p.iterator().next());
    }


    /** This action is created only when project has broken references.
     * Once these are resolved the action is disabled.
     */
    private static class BrokenProjectAction extends AbstractAction {

        private final Project prj;

        BrokenProjectAction(@NonNull final Project prj) {
            this.prj = prj;        
            putValue(Action.NAME, NbBundle.getMessage(BrokenProjectActionFactory.class, "LBL_Fix_Broken_Links_Action"));
            setEnabled(ProjectProblems.isBroken(prj));
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
//                helper.requestUpdate();
            ProjectProblems.showCustomizer(prj);
        }

    }

}