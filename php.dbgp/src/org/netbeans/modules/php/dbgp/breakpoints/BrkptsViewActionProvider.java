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
package org.netbeans.modules.php.dbgp.breakpoints;

import javax.swing.Action;

import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.text.Line;
import org.openide.util.NbBundle;

/**
 * @author ads
 *
 */
public class BrkptsViewActionProvider implements NodeActionsProviderFilter {
    private static final String GO_TO_SOURCE_LABEL = "CTL_Breakpoint_GoToSource_Label"; // NOI18N

    @Override
    public Action[] getActions(NodeActionsProvider original, Object node) throws UnknownTypeException {
        Action[] actions = original.getActions(node);
        if (node instanceof LineBreakpoint) {
            Action[] newActions = new Action[actions.length + 2];
            newActions[0] = GO_TO_SOURCE_ACTION;
            newActions[1] = null;
            System.arraycopy(actions, 0, newActions, 2, actions.length);
            actions = newActions;
        }
        return actions;
    }

    @Override
    public void performDefaultAction(NodeActionsProvider original, Object node) throws UnknownTypeException {
        if (node instanceof LineBreakpoint) {
            goToSource((LineBreakpoint) node);
        } else {
            original.performDefaultAction(node);
        }
    }

    private static void goToSource(LineBreakpoint breakpoint) {
        Line line = breakpoint.getLine();
        if (line != null) {
            line.show(Line.ShowOpenType.REUSE, Line.ShowVisibilityType.FOCUS);
        }
    }
    private static final Action GO_TO_SOURCE_ACTION = Models.createAction(
            NbBundle.getMessage(BrkptsViewActionProvider.class, GO_TO_SOURCE_LABEL),
            new GoToSourcePerformer(),
            Models.MULTISELECTION_TYPE_EXACTLY_ONE);

    private static class GoToSourcePerformer implements Models.ActionPerformer {

        @Override
        public boolean isEnabled(Object arg) {
            return true;
        }

        @Override
        public void perform(Object[] nodes) {
            goToSource((LineBreakpoint) nodes[0]);
        }

    }

}
