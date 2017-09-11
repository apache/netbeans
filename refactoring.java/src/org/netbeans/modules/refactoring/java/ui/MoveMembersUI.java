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
package org.netbeans.modules.refactoring.java.ui;

import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.api.JavaMoveMembersProperties;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Ralph Ruijs
 */
@NbBundle.Messages("LBL_MoveMembers=Move Members")
public class MoveMembersUI implements RefactoringUI {
    private MoveMembersPanel panel;
    private final TreePathHandle[] selectedElements;
    private transient MoveRefactoring refactoring;
    private InstanceContent ic;

    public MoveMembersUI(TreePathHandle... selectedElement) {
        this.selectedElements = selectedElement;
        this.ic = new InstanceContent();
        this.refactoring = new MoveRefactoring(new AbstractLookup(ic));
        refactoring.getContext().add(new JavaMoveMembersProperties(selectedElement));
    }

    @Override
    public String getName() {
        return getString ("LBL_MoveMembers");
    }

    @Override
    public String getDescription() {
        return getName();
    }

    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new MoveMembersPanel(selectedElements, parent);
        }
        return panel;
    }

    private Problem setParameters(boolean checkOnly) {
        List<? extends TreePathHandle> handles = panel.getHandles();
        ic.set(handles, null);
        TreePathHandle target = panel.getTarget();
        refactoring.setTarget(target == null? Lookup.EMPTY:Lookups.fixed(target));
        JavaMoveMembersProperties properties = refactoring.getContext().lookup(JavaMoveMembersProperties.class);
        if(properties == null) {
            refactoring.getContext().add(properties = new JavaMoveMembersProperties(selectedElements));
        }
        properties.setVisibility(panel.getVisibility());
        properties.setDelegate(panel.getDelegate());
        properties.setUpdateJavaDoc(panel.getUpdateJavaDoc());
        properties.setAddDeprecated(panel.getDeprecated());
        return checkOnly? refactoring.fastCheckParameters() : refactoring.checkParameters();
    }

    @Override
    public Problem checkParameters() {
        return setParameters(true);
    }

    @Override
    public Problem setParameters() {
        return setParameters(false);
    }

    @Override
    public boolean hasParameters() {
        return true;
    }

    @Override
    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.refactoring.java.ui.MoveMembersUI"); // NOI18N
    }

    private String getString(String key) {
        return NbBundle.getMessage(MoveMembersUI.class, key);
    }
}
