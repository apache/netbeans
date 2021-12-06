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
package org.netbeans.modules.versionvault.ui;

import java.util.Set;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versionvault.ClearcaseModuleConfig;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import org.netbeans.modules.versionvault.Clearcase;
import org.netbeans.modules.versionvault.FileInformation;
import org.netbeans.modules.versionvault.FileStatusCache;
import org.netbeans.modules.versionvault.util.ClearcaseUtils;
import org.netbeans.modules.versionvault.util.ProgressSupport;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.NbBundle;

/**
 * @author Maros Sandor
 */
public class IgnoreAction extends AbstractAction {
    
    private VCSContext context;    
    
    public IgnoreAction(VCSContext context) {
        super(getNameFromContext(context));
        this.context = context;
    }

    @Override
    public boolean isEnabled() {        
        return getStatus(context) != -1;
    }
    
    public void actionPerformed(ActionEvent e) {
        Utils.logVCSActionEvent("CC");
        final Set<File> roots = context.getRootFiles();
        if(roots.size() == 0 ) {
            return;
        }
        int status = getStatus(context);
        String progressDisplayName;
        if(status == FileInformation.STATUS_NOTVERSIONED_IGNORED) {
            progressDisplayName = NbBundle.getMessage(IgnoreAction.class, "IgnoreAction.progress.unignoring");
        } else {
            progressDisplayName = NbBundle.getMessage(IgnoreAction.class, "IgnoreAction.progress.ignoring");
        }
        try {
            ProgressSupport ps = new ProgressSupport(Clearcase.getInstance().getRequestProcessor(), progressDisplayName) {
                @Override
                protected void perform() {
                    for (File file : roots) {
                        if (ClearcaseModuleConfig.isIgnored(file)) {
                            ClearcaseModuleConfig.setUnignored(file);
                        } else {
                            ClearcaseModuleConfig.setIgnored(file);
                        }
                    }
                }
            };
            ps.start();
        } finally {            
            ClearcaseUtils.afterCommandRefresh(roots.toArray(new File[roots.size()]), true);            
        }        
    }

    private static String getNameFromContext(VCSContext context) {
        int status = getStatus(context);
        if(status == FileInformation.STATUS_NOTVERSIONED_IGNORED) {
            return NbBundle.getMessage(IgnoreAction.class, "IgnoreAction_Name_Unignore"); //NOI18N
        } else {
            return NbBundle.getMessage(IgnoreAction.class, "IgnoreAction_Name_Ignore"); //NOI18N
        }        
    }
    
    private static int getStatus(VCSContext context) {
        FileStatusCache cache = Clearcase.getInstance().getFileStatusCache();        
        File[] files = context.getRootFiles().toArray(new File[context.getRootFiles().size()]);        
        int status = -1;
        for (int i = 0 ; i < files.length; i++) {
            File file = files[i];            
            if(i == 0) {
                status = cache.getInfo(file).getStatus();                
                if( ( status & 
                      (FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | 
                       FileInformation.STATUS_NOTVERSIONED_IGNORED) ) == 0 ) {
                    return -1;
                }
            } else {
                if( cache.getInfo(file).getStatus() != status ) {
                    return -1;
                }            
            }                
        }        
        return status;
    }
    
}
