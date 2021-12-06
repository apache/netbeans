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

import org.openide.util.NbBundle;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versionvault.util.ClearcaseUtils;
import org.netbeans.modules.versionvault.VersionsCache;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.diff.Difference;

import java.io.*;
import java.util.*;

/**
 * Stream source for diffing CC managed files.
 *
 * @author Maros Sandor
 */
public class DiffStreamSource extends StreamSource {

    private final File      baseFile;
    private final String    revision;
    private final String    title;
    private String          mimeType;

    /**
     * Null is a valid value if base file does not exist in this revision. 
     */ 
    private File            remoteFile;

    /**
     * Creates a new StreamSource implementation for Diff engine.
     * 
     * @param baseFile
     * @param revision file revision, may be null if the revision does not exist (ie for new files)
     * @param title title to use in diff panel
     */ 
    public DiffStreamSource(File baseFile, String revision, String title) {
        this.baseFile = baseFile;
        this.revision = revision;
        this.title = title;
    }

    public String getName() {
        if (baseFile != null) {
            return baseFile.getName();
        } else {
            return NbBundle.getMessage(DiffStreamSource.class, "LBL_Diff_Anonymous"); // NOI18N
        }
    }

    public String getTitle() {
        return title;
    }

    public synchronized String getMIMEType() {
        try {
            init();
        } catch (IOException e) {
            return null; // XXX use error manager HACK null  potentionally kills DiffViewImpl, NPE while constructing EditorKit
        }
        return mimeType;
    }

    public synchronized Reader createReader() throws IOException {
        init();
        if (revision == null || remoteFile == null) return null;
        if (!mimeType.startsWith("text/")) {
            return null;
        } else {
            return Utils.createReader(remoteFile);
        }
    }

    public Writer createWriter(Difference[] conflicts) throws IOException {
        throw new IOException("Operation not supported"); // NOI18N
    }

    @Override
    public boolean isEditable() {
        return VersionsCache.REVISION_CURRENT.equals(revision) && isPrimary();
    }

    private boolean isPrimary() {
        FileObject fo = FileUtil.toFileObject(baseFile);
        if (fo != null) {
            try {
                DataObject dao = DataObject.find(fo);
                return fo.equals(dao.getPrimaryFile());
            } catch (DataObjectNotFoundException e) {
                // no dataobject, never mind
            }
        }
        return true;
    }

    @Override
    public synchronized Lookup getLookup() {
        try {
            init();
        } catch (IOException e) {
            return Lookups.fixed();
        }
        if (remoteFile == null || !isPrimary()) return Lookups.fixed();
        FileObject remoteFo = FileUtil.toFileObject(remoteFile);
        if (remoteFo == null) return Lookups.fixed();

        return Lookups.fixed(remoteFo);
    }
    
    /**
     * Loads data over network.
     */
    synchronized void init() throws IOException {
        if (remoteFile != null || revision == null) return;
        if (baseFile.isDirectory()) {
            mimeType = "content/unknown"; // NOI18N
            return;
        }
        mimeType = ClearcaseUtils.getMimeType(baseFile);
        try {
            if (isEditable()) {
                // we cannot move editable documents because that would break Document sharing
                remoteFile = VersionsCache.getInstance().getFileRevision(baseFile, revision);
            } else {
                File tempFolder = Utils.getTempFolder();
                // To correctly get content of the base file, we need to checkout all files that belong to the same
                // DataObject. One example is Form files: data loader removes //GEN:BEGIN comments from the java file but ONLY
                // if it also finds associate .form file in the same directory
                Set<File> allFiles = Utils.getAllDataObjectFiles(baseFile);
                for (File file : allFiles) {
                    boolean isBase = file.equals(baseFile);
                    try {
                        File rf = VersionsCache.getInstance().getFileRevision(file, revision);
                        if(rf == null) {
                            remoteFile = null;
                            return;
                        }
                        File newRemoteFile = new File(tempFolder, file.getName());
                        newRemoteFile.deleteOnExit();
                        Utils.copyStreamsCloseAll(new FileOutputStream(newRemoteFile), new FileInputStream(rf));
                        if (isBase) {
                            remoteFile = newRemoteFile;
                            Utils.associateEncoding(file, newRemoteFile);                            
                        }
                    } catch (Exception e) {
                        if (isBase) throw e;
                        // we cannot check out peer file so the dataobject will not be constructed properly
                    }
                }
            }
            if (!baseFile.exists() && remoteFile != null && remoteFile.exists()) {
                mimeType = ClearcaseUtils.getMimeType(remoteFile);
            }
        } catch (Exception e) {
            // TODO detect interrupted IO (exception subclass), i.e. user cancel
            IOException failure = new IOException("Can not load remote file for " + baseFile); // NOI18N
            failure.initCause(e);
            throw failure;
        }
    }
}
