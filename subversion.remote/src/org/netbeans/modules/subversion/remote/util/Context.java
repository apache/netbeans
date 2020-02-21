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
package org.netbeans.modules.subversion.remote.util;

import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.openide.filesystems.FileSystem;

/**
 * Encapsulates context of an action. There are two ways in which context may be defined:
 * - list of files (f/b.txt, f/c.txt, f/e.txt)
 * - list of roots (top folders) plus list of exclusions (f),(a.txt, d.txt)
 *
 * 
 */
public class Context {

    public static final Context Empty = new Context( getEmptyList(), getEmptyList(), getEmptyList() );

    private static final long serialVersionUID = 1L;
    
    private final List<VCSFileProxy> filteredFiles;
    private final List<VCSFileProxy> rootFiles;
    private final List<VCSFileProxy> exclusions;

    public Context(List<VCSFileProxy> filteredFiles, List<VCSFileProxy> rootFiles, List<VCSFileProxy> exclusions) {
        this.filteredFiles = filteredFiles;
        this.rootFiles = rootFiles;
        this.exclusions = exclusions;
        while (normalize());
    }

    public Context(VCSFileProxy file) {
        this(new VCSFileProxy [] { file });
    }

    public Context(VCSFileProxy [] files) {
        List<VCSFileProxy> list = new ArrayList<>(files.length);
        list.addAll(Arrays.asList(files));
        removeDuplicates(list);
        this.filteredFiles = list;
        this.rootFiles = list;
        this.exclusions = Collections.emptyList();
    }

    public FileSystem getFileSystem() {
        for (VCSFileProxy root : rootFiles) {
            return VCSFileProxySupport.getFileSystem(root);
        }
        return null;
    }

    public VCSFileProxy getTopFolder() {
        for (VCSFileProxy root : rootFiles) {
            return Subversion.getInstance().getTopmostManagedAncestor(root);
        }
        return null;
    }
    
    private boolean normalize() {
        for (Iterator<VCSFileProxy> i = rootFiles.iterator(); i.hasNext();) {
            VCSFileProxy root = i.next();
            for (Iterator<VCSFileProxy> j = exclusions.iterator(); j.hasNext();) {
                VCSFileProxy exclusion = j.next();
                if (VCSFileProxySupport.isAncestorOrEqual(exclusion, root)) {
                    j.remove();
                    exclusionRemoved(exclusion, root);
                    return true;
                }
            }
        }
        removeDuplicates(rootFiles);
        removeDuplicates(exclusions);
        return false;
    }

    private void removeDuplicates(List<VCSFileProxy> files) {
        List<VCSFileProxy> newFiles = new ArrayList<>();
        outter: for (Iterator<VCSFileProxy> i = files.iterator(); i.hasNext();) {
            VCSFileProxy file = i.next();
            for (Iterator<VCSFileProxy> j = newFiles.iterator(); j.hasNext();) {
                VCSFileProxy includedFile = j.next();
                if (VCSFileProxySupport.isAncestorOrEqual(includedFile, file) && (file.isFile() || !VersioningSupport.isFlat(includedFile))) {
                    continue outter;
                }
                if (VCSFileProxySupport.isAncestorOrEqual(file, includedFile) && (includedFile.isFile() || !VersioningSupport.isFlat(file))) {
                    j.remove();
                }
            }
            newFiles.add(file);
        }
        files.clear();
        files.addAll(newFiles);
    }
    
    private void exclusionRemoved(VCSFileProxy exclusion, VCSFileProxy root) {
        VCSFileProxy [] exclusionChildren = exclusion.listFiles();
        if (exclusionChildren == null) {
            return;
        }
        for (int i = 0; i < exclusionChildren.length; i++) {
            VCSFileProxy child = exclusionChildren[i];
            if (!VCSFileProxySupport.isAncestorOrEqual(root, child)) {
                exclusions.add(child);
            }
        }
    }

    public List<VCSFileProxy> getRoots() {
        return rootFiles;
    }

    public List<VCSFileProxy> getExclusions() {
        return exclusions;
    }

    /**
     * Gets exact set of files to operate on, it is effectively defined as (rootFiles - exclusions). This set
     * is NOT suitable for Update command because Update should operate on all rootFiles and just exclude some subfolders.
     * Otherwise update misses new files and folders directly in rootFiles folders. 
     *  
     * @return files to operate on
     */ 
    public VCSFileProxy [] getFiles() {
        return filteredFiles.toArray(new VCSFileProxy[filteredFiles.size()]);
    }

    public VCSFileProxy[] getRootFiles() {
        return rootFiles.toArray(new VCSFileProxy[rootFiles.size()]);
    }
    
    public boolean contains(VCSFileProxy file) {
        outter : for (Iterator<VCSFileProxy> i = rootFiles.iterator(); i.hasNext();) {
            VCSFileProxy root = i.next();
            if (SvnUtils.isParentOrEqual(root, file)) {
                for (Iterator<VCSFileProxy> j = exclusions.iterator(); j.hasNext();) {
                    VCSFileProxy excluded = j.next();
                    if (VCSFileProxySupport.isAncestorOrEqual(excluded, file)) {
                        continue outter;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public static final List<VCSFileProxy> getEmptyList() {
        return Collections.emptyList();
    }
    
    public void writeObject(ObjectOutput out) throws IOException {
        out.writeInt(rootFiles.size());
        for(VCSFileProxy root : rootFiles) {
            URI uri = VCSFileProxySupport.toURI(root);
            out.writeObject(uri);
        }
        out.writeInt(filteredFiles.size());
        for(VCSFileProxy root : filteredFiles) {
            URI uri = VCSFileProxySupport.toURI(root);
            out.writeObject(uri);
        }
        out.writeInt(exclusions.size());
        for(VCSFileProxy root : exclusions) {
            URI uri = VCSFileProxySupport.toURI(root);
            out.writeObject(uri);
        }
    }

    public Context(ObjectInput in) throws IOException, ClassNotFoundException {
        int size = in.readInt();
        rootFiles = new ArrayList<>(size);
        for(int i = 0; i < size; i++) {
            URI uri = (URI)in.readObject();
            VCSFileProxy root = VCSFileProxySupport.fromURI(uri);
            if (root != null) {
                rootFiles.add(root);
            }
        }
        size = in.readInt();
        filteredFiles = new ArrayList<>(size);
        for(int i = 0; i < size; i++) {
            URI uri = (URI)in.readObject();
            VCSFileProxy root = VCSFileProxySupport.fromURI(uri);
            if (root != null) {
                filteredFiles.add(root);
            }
        }
        size = in.readInt();
        exclusions = new ArrayList<>(size);
        for(int i = 0; i < size; i++) {
            URI uri = (URI)in.readObject();
            VCSFileProxy root = VCSFileProxySupport.fromURI(uri);
            if (root != null) {
                exclusions.add(root);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        for(VCSFileProxy root : rootFiles) {
            if (buf.length()>0) {
                buf.append('\n');
            }
            buf.append(root.getPath());
        }
        return buf.toString();
    }
}
