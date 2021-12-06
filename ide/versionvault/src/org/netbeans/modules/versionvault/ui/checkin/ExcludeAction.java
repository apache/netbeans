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

package org.netbeans.modules.versionvault.ui.checkin;

import java.util.Set;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versionvault.ClearcaseModuleConfig;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.versionvault.Clearcase;
import org.netbeans.modules.versionvault.FileInformation;
import org.netbeans.modules.versionvault.FileStatusCache;
import org.netbeans.modules.versionvault.util.ClearcaseUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.NbBundle;

/**
 * @author Maros Sandor
 */
public class ExcludeAction extends AbstractAction {
    
    private VCSContext context;    
    
    public ExcludeAction(VCSContext context) {        
        this.context = context;
        putValue(Action.NAME, getNameFromContext(context));        
    }

    @Override
    public boolean isEnabled() {        
        FileStatusCache cache = Clearcase.getInstance().getFileStatusCache();
        Set<File> roots = context.getRootFiles();        
        if (roots.size() == 0) return false;
        boolean include = false;
        boolean exclude = false;
        for (File root : roots) {            
            FileInformation info = cache.getCachedInfo(root);            
            if(info != null && ((info.getStatus() & CheckinAction.ALLOW_CHECKIN) == 0)) {
                return false;
            }
            if(ClearcaseModuleConfig.isExcludedFromCommit(root.getAbsolutePath())) {
                if(include) {
                    return false;
                }              
                exclude = true;
            } else {
                if(exclude) {
                    return false;
                }         
                include = true;
            }
        }
        return true;
    }
    
    public void actionPerformed(ActionEvent e) {
        Utils.logVCSActionEvent("CC");
        Set<File> roots = context.getRootFiles();
        if(roots.size() == 0 ) {
            return;
        }
        try {
            List<String> exclusions = new ArrayList<String>();
            File file = null;            
            for (File root : roots) {
                if(file == null) {
                    file = root;
                }
                exclusions.add(root.getAbsolutePath());
            }                                
            boolean excluded = ClearcaseModuleConfig.isExcludedFromCommit(file.getAbsolutePath());
            if(excluded) {
                ClearcaseModuleConfig.removeExclusionPaths(exclusions);   
            } else {
                ClearcaseModuleConfig.addExclusionPaths(exclusions);                   
            }         
        } finally {
            ClearcaseUtils.afterCommandRefresh(roots.toArray(new File[roots.size()]), false);            
        }
    }    
    
    private static String getNameFromContext(VCSContext context) {
        Set<File> roots = context.getRootFiles();
        if(roots.size() == 0 ) {
            return NbBundle.getMessage(ExcludeAction.class, "Action_Exclude_from_Checkin_Name"); //NOI18N
        }

        String name = null;
        boolean included = false;
        boolean excluded = false;
        for (File root : roots) {            
            if(ClearcaseModuleConfig.isExcludedFromCommit(root.getAbsolutePath())) {
                if(included) {
                    break;
                }              
                excluded = true;
            } else {
                if(excluded) {
                    break;
                }         
                included = true;
            }            
        }    
        if(excluded) {
            name = NbBundle.getMessage(ExcludeAction.class, "Action_Include_to_Checkin_Name"); //NOI18N
        } else {
            name = NbBundle.getMessage(ExcludeAction.class, "Action_Exclude_from_Checkin_Name"); //NOI18N
        }
        return name;            
    }    
}
