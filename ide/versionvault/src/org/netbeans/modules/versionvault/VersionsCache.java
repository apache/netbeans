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
package org.netbeans.modules.versionvault;

import org.netbeans.modules.versionvault.client.NotificationListener;
import org.netbeans.modules.versionvault.client.GetCommand;
import org.netbeans.modules.versionvault.*;
import org.netbeans.modules.versionvault.client.status.FileEntry;
import org.netbeans.modules.versionvault.util.ClearcaseUtils;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileUtil;

import java.io.File;
import java.io.IOException;

/**
 * Now the 'cache' does not cache revisions of files, it fetches them everytime they are needed.
 * 
 * @author Maros Sandor
 */
public class VersionsCache implements NotificationListener {
    
    private static final VersionsCache instance = new VersionsCache();

    public static final String REVISION_BASE = "BASE"; //NOI18N
    
    public static final String REVISION_CURRENT = "LOCAL"; // NOI18N
    
    public static final String REVISION_HEAD    = "HEAD"; // NOI18N
    
    private VersionsCache() {
    }
    
    public static VersionsCache getInstance() {
        return instance;
    }

    public File getFileRevision(File workingCopy, String revision) throws IOException {
        return getRemoteFile(workingCopy, revision, false);
    }
    
    public File getRemoteFile(File workingCopy, String revision, boolean beQuiet) throws IOException {
        if (REVISION_CURRENT.equals(revision)) {
            return workingCopy.exists() ? workingCopy : null;
        } else if (REVISION_BASE.equals(revision)) {
            FileInformation info = Clearcase.getInstance().getFileStatusCache().getInfo(workingCopy);
            if (info == null) return null;
            FileEntry fileEntry = info.getFileEntry(Clearcase.getInstance().getClient(), workingCopy);
            if (fileEntry == null) return null;
            revision = fileEntry.getVersionSelector();
            if (revision == null) return null;
        }
        String revisionSpec = ClearcaseUtils.getExtendedName(workingCopy, revision);  
                
        File tempFile = File.createTempFile("nbclearcase-", "get"); //NOI18N
        tempFile.delete();        
        
        GetCommand cmd = new GetCommand(tempFile, revisionSpec, this);        
        Clearcase.getInstance().getClient().post(NbBundle.getMessage(VersionsCache.class, "Progress_Getting_Clearcased_File"), cmd).waitFinished(); //NOI18N
        tempFile.deleteOnExit();
        if (!cmd.hasFailed() && tempFile.isFile()) return FileUtil.normalizeFile(tempFile);
        return null;
    }

    public void commandStarted() {
    }

    public void outputText(String line) {
    }

    public void errorText(String line) {
    }

    public void commandFinished() {
    }
}
