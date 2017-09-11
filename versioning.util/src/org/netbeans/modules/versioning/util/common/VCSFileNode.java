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

package org.netbeans.modules.versioning.util.common;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import org.openide.util.NbBundle;

/**
 * Represents a versioned file.
 *
 * @author Tomas Stupka
 */
public abstract class VCSFileNode<I extends VCSFileInformation> {

    private final File file;
    private final File root;
    private String shortPath;
    private VCSCommitOptions commitOption;

    public VCSFileNode(File root, File file) {
        assert file != null && root != null;
        this.file = file;
        this.root = root;        
    }

    public abstract VCSCommitOptions getDefaultCommitOption (boolean withExclusions);
    public abstract I getInformation();
    
    public String getStatusText () {
        return getInformation().getStatusText();
    }
    
    public VCSCommitOptions getCommitOptions() {
        if(commitOption == null) {
            commitOption = getDefaultCommitOption(true);
        }
        return commitOption;
    }
    
    void setCommitOptions(VCSCommitOptions option) {
        commitOption = option;
    }
        
    public String getName() {
        return file.getName();
    }

    public File getFile() {
        return file;
    }

    public File getRoot () {
        return root;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof VCSFileNode && file.equals(((VCSFileNode) o).file);
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }

    public FileObject getFileObject() {
        return FileUtil.toFileObject(file);
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
    
    public String getRelativePath() {        
        if(shortPath == null) {
            String path = file.getAbsolutePath();
            String rootPath = root.getAbsolutePath();
            if (path.startsWith(rootPath)) {
                if (path.length() == rootPath.length()) {
                    shortPath = "."; //NOI18N
                } else {
                    shortPath = path.substring(rootPath.length() + 1);
                }
            } else {
                shortPath = NbBundle.getMessage(VCSFileNode.class, "LBL_Location_NotInRepository"); //NOI18N
            }
        }
        return shortPath;
    }    
}
