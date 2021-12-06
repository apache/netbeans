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
package org.netbeans.modules.versionvault.ui.label;

import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.VersioningOutputManager;
import org.netbeans.modules.versioning.util.Utils;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.*;

import org.netbeans.modules.versionvault.*;
import org.netbeans.modules.versionvault.client.AfterCommandRefreshListener;
import org.netbeans.modules.versionvault.client.ExecutionUnit;
import org.netbeans.modules.versionvault.client.LabelCommand;
import org.netbeans.modules.versionvault.client.OutputWindowNotificationListener;
import org.openide.util.NbBundle;

/**
 * Label action.
 * 
 * @author Tomas Stupka
 */
public class LabelAction extends AbstractAction {
    
    private final VCSContext context;
    protected final VersioningOutputManager voutput;
    
    static int ALLOW_LABEL = FileInformation.STATUS_VERSIONED;
    static String RECENT_LABEL_MESSAGES = "label.messages";
    
    private File[] files;    
    
    public LabelAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
        voutput = VersioningOutputManager.getInstance();
    }
    
    @Override
    public boolean isEnabled() {
        FileStatusCache cache = Clearcase.getInstance().getFileStatusCache();
        Set<File> roots = context.getRootFiles();        
        for (File root : roots) {
            FileInformation info = cache.getCachedInfo(root);            
            if(info != null && ((info.getStatus() & ALLOW_LABEL) != 0)) {
                return true;
            }
        }
        return false;
    }
    
    public void actionPerformed(ActionEvent ev) {
        Utils.logVCSActionEvent("CC");
        Set<File> roots = context.getRootFiles();
        
        Label label = new Label(Utils.getContextDisplayName(context), roots.toArray(new File[roots.size()]));
        if(!label.show()) {
            return; 
        }
        
        files = roots.toArray(new File[roots.size()]);
        LabelCommand cmd = 
                new LabelCommand(
                    files, 
                    label.getLabel(), 
                    label.getComment(), 
                    label.getReplace(), 
                    label.getRecurse(), 
                    label.getFollow(), 
                    label.getVersion(), 
                    new OutputWindowNotificationListener(), 
                    new AfterCommandRefreshListener(files));
        Clearcase.getInstance().getClient().post(NbBundle.getMessage(LabelAction.class, "Progress_Setting_Label"),cmd); //NOI18N
    }

}
