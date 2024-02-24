/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
        return list.toArray(new Object[0]);
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
