/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.spiimpl.refactoring;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

@ActionID(id = "org.netbeans.modules.java.hints.jackpot.impl.refactoring.ApplyPatternAction", category = "Refactoring")
@ActionRegistration(iconInMenu = true, displayName = "#CTL_ApplyPatternAction")
@ActionReferences({
    @ActionReference(path = "Menu/Refactoring", position = 1850),
    @ActionReference(path = "Projects/org-netbeans-modules-java-j2seproject/Actions", position = 2350),
    @ActionReference(path = "Projects/org-netbeans-modules-ant-freeform/Actions", position = 1650),
    @ActionReference(path = "Projects/org-netbeans-modules-j2ee-clientproject/Actions", position = 2350),
    @ActionReference(path = "Projects/org-netbeans-modules-j2ee-ejbjarproject/Actions", position = 2350),
    @ActionReference(path = "Projects/org-netbeans-modules-web-project/Actions", position = 2350),
    @ActionReference(path = "Projects/org-netbeans-modules-maven/Actions", position = 2850),
    @ActionReference(path = "Projects/org-netbeans-modules-apisupport-project/Actions", position = 3050),
    @ActionReference(path = "Projects/org-netbeans-modules-apisupport-project-suite/Actions", position = 2450)
})
@Messages("CTL_ApplyPatternAction=Inspect and &Transform...")
public final class ApplyPatternAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        Node[] n = TopComponent.getRegistry().getActivatedNodes();
        final Lookup context = n.length > 0 ? n[0].getLookup():Lookup.EMPTY;
        Utilities.invokeAfterScanFinished(new Runnable() {
            @Override
            public void run() {
                InspectAndRefactorUI.openRefactoringUI(context);
            }
        }, Bundle.CTL_ApplyPatternAction());
    }
}
