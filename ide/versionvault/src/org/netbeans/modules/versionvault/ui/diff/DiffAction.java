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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

/*
 * Copyright 2021 HCL America, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.netbeans.modules.versionvault.ui.diff;

import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versionvault.util.ClearcaseUtils;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Opens the Diff view panel.
 * 
 * @author Maros Sandor
 */
public class DiffAction extends AbstractAction {
    
    private final VCSContext    context;

    public DiffAction(String name, VCSContext context) {
        super(name);
        this.context = context;
        setEnabled(context.getFiles().size() > 0);
    }
    
    @Override
    public boolean isEnabled() {
        return ClearcaseUtils.containsVersionedFiles(context);
    }

    public void actionPerformed(ActionEvent ev) {
        Utils.logVCSActionEvent("CC");
        diff(context, Setup.DIFFTYPE_LOCAL, Utils.getContextDisplayName(context));        
    }

    public static void diff(VCSContext ctx, int type, String contextName) {
        MultiDiffPanel panel = new MultiDiffPanel(ctx, type, contextName); // spawns bacground DiffPrepareTask
        DiffTopComponent tc = new DiffTopComponent(panel);
        tc.setName(NbBundle.getMessage(DiffAction.class, "CTL_DiffPanel_Title", contextName)); // NOI18N
        tc.open();
        tc.requestActive();        
    }

    public static void diff(File file, String rev1, String rev2) {
        MultiDiffPanel panel = new MultiDiffPanel(file, rev1, rev2); // spawns bacground DiffPrepareTask
        DiffTopComponent tc = new DiffTopComponent(panel);
        tc.setName(NbBundle.getMessage(DiffAction.class, "CTL_DiffPanel_Title", file.getName())); // NOI18N
        tc.open();
        tc.requestActive();
    }
}
