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
package org.netbeans.modules.versionvault.ui.update;

import org.netbeans.modules.versionvault.client.OutputWindowNotificationListener;
import org.netbeans.modules.versionvault.client.UpdateCommand;
import org.netbeans.modules.versionvault.client.AfterCommandRefreshListener;
import org.netbeans.modules.versionvault.FileInformation;
import org.netbeans.modules.versionvault.FileStatusCache;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versionvault.Clearcase;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileFilter;
import java.util.*;

import org.netbeans.modules.versionvault.util.ClearcaseUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.NbBundle;

/**
 * Updates selected files/folders in the snapshot view.
 * 
 * @author Maros Sandor
 */
public class UpdateAction extends AbstractAction {
    
    private final VCSContext context;

    public UpdateAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }
    
    @Override
    public boolean isEnabled() {
        Set<File> roots = context.getRootFiles();
        if (roots.size() == 0) return false;
        if (!ClearcaseUtils.containsSnapshot(context)) return false;
        FileStatusCache cache = Clearcase.getInstance().getFileStatusCache();
        for (File file : roots) {
            FileInformation info = cache.getCachedInfo(file);
            if(info != null && 
               (info.getStatus() & FileInformation.STATUS_VERSIONED) == 0 ){
                return false;
            }            
        }
        return true;
    }    
    
    public void actionPerformed(ActionEvent e) {
        Utils.logVCSActionEvent("CC");
        Set<File> files = context.computeFiles(updateFileFilter);        

        // the whole tree for every root has to be refeshed as
        // the update might have changed the files structure
        List<File> filesToRefresh = new ArrayList<File>();
        for (File file : files) {
            filesToRefresh.addAll(ClearcaseUtils.getFilesTree(file));            
        }
        
        UpdateCommand cmd = 
                new UpdateCommand(
                    files.toArray(new File[files.size()]), 
                    UpdateCommand.HijackedAction.DoNotTouch,
                    new AfterCommandRefreshListener(filesToRefresh.toArray(new File[filesToRefresh.size()])), 
                    new OutputWindowNotificationListener());
        Clearcase.getInstance().getClient().post(NbBundle.getMessage(UpdateAction.class, "Progress_Updating"),cmd); //NOI18N
    }

    public static void update(VCSContext context) {
        new UpdateAction("", context).actionPerformed(null); //NOI18N
    }
    
    private static final FileFilter updateFileFilter = new FileFilter() {
        public boolean accept(File pathname) {
            return true;
        }
    };
}
