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
