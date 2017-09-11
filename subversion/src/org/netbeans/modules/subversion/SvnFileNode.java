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

package org.netbeans.modules.subversion;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 * Represents real or virtual (non-local) file.
 *
 * @author Maros Sandor
 */
public class SvnFileNode {

    /**
     * Careful, may not be normalized
     * @return 
     */
    private final File file;
    private final File normalizedFile;
    private FileObject fileObject;
    private String relativePath;
    private String copy;
    private boolean copyScanned;
    private Boolean fileFlag;
    private String mimeType;

    public SvnFileNode(File file) {
        this.file = file;
        File norm = FileUtil.normalizeFile(file);
        if (Utilities.isMac() || Utilities.isUnix()) {
            FileInformation fi = Subversion.getInstance().getStatusCache().getStatus(file);
            FileInformation fiNorm = Subversion.getInstance().getStatusCache().getStatus(norm);
            if (fi.getStatus() != fiNorm.getStatus()) {
                norm = null;
            }
        }
        normalizedFile = norm;
    }

    public String getName() {
        return file.getName();
    }

    public FileInformation getInformation() {
        return Subversion.getInstance().getStatusCache().getStatus(file); 
    }

    /**
     * Careful, returned file may not be normalized
     * @return 
     */
    public File getFile() {
        return file;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof SvnFileNode && file.equals(((SvnFileNode) o).file);
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }

    public FileObject getFileObject() {
        if (fileObject == null && normalizedFile != null) {
            fileObject = FileUtil.toFileObject(normalizedFile);
        }
        return fileObject;
    }

    public Object[] getLookupObjects() {
        List<Object> list = new ArrayList<Object>(2);
        list.add(file);
        FileObject fo = getFileObject();
        if (fo != null) {
            list.add(fo);
        }
        return list.toArray(new Object[list.size()]);
    }

    /**
     * Returns relativePath of this node's file or the full resourceUrl if explicitly selected in Options.
     * @return relative path of this node's file.
     */
    public String getLocation() {
        if (relativePath == null) {
            try {
                assert !java.awt.EventQueue.isDispatchThread();
                relativePath = SvnModuleConfig.getDefault().isRepositoryPathPrefixed()
                        ? SvnUtils.decodeToString(SvnUtils.getRepositoryUrl(getFile())) : SvnUtils.getRelativePath(getFile());
            } catch (SVNClientException ex) {
                SvnClientExceptionHandler.notifyException(ex, false, false);
            }
            if (relativePath == null) {
                relativePath = NbBundle.getMessage(SvnFileNode.class, "SvnFileNode.relativePath.unknown"); //NOI18N
            }
        }
        return relativePath;
    }

    public String getCopy () {
        if (!copyScanned) {
            assert !java.awt.EventQueue.isDispatchThread();
            copy = SvnUtils.getCopy(getFile());
            copyScanned = true;
        }
        return copy;
    }

    public boolean isFile () {
        if (fileFlag == null) {
            fileFlag = file.isFile();
        }
        return fileFlag;
    }

    public String getMimeType () {
        if (isFile() && mimeType == null) {
            mimeType = SvnUtils.getMimeType(normalizedFile == null ? FileUtil.normalizeFile(file) : normalizedFile);
        }
        return mimeType;
    }

    public void initializeProperties() {
        getLocation();
        getCopy();
        isFile();
        getMimeType();
        getInformation().getEntry(file); // CommitTableModel.getValueAt may trigger status
    }
}
