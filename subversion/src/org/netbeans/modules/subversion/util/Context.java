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

package org.netbeans.modules.subversion.util;

import org.netbeans.modules.versioning.spi.VersioningSupport;

import java.io.File;
import java.io.Serializable;
import java.util.*;
import org.netbeans.modules.versioning.util.Utils;

/**
 * Encapsulates context of an action. There are two ways in which context may be defined:
 * - list of files (f/b.txt, f/c.txt, f/e.txt)
 * - list of roots (top folders) plus list of exclusions (f),(a.txt, d.txt)
 *
 * @author Maros Sandor
 */
public class Context implements Serializable {

    public static final Context Empty = new Context( getEmptyList(), getEmptyList(), getEmptyList() );

    private static final long serialVersionUID = 1L;
    
    private final List<File> filteredFiles;
    private final List<File> rootFiles;
    private final List<File> exclusions;

    public Context(List<File> filteredFiles, List<File> rootFiles, List<File> exclusions) {
        this.filteredFiles = filteredFiles;
        this.rootFiles = rootFiles;
        this.exclusions = exclusions;
        while (normalize());
    }

    public Context(File file) {
        this(new File [] { file });
    }

    public Context(File [] files) {
        List<File> list = new ArrayList<File>(files.length);
        list.addAll(Arrays.asList(files));
        removeDuplicates(list);
        this.filteredFiles = list;
        this.rootFiles = list;
        this.exclusions = Collections.emptyList();
    }

    private boolean normalize() {
        for (Iterator<File> i = rootFiles.iterator(); i.hasNext();) {
            File root = i.next();
            for (Iterator<File> j = exclusions.iterator(); j.hasNext();) {
                File exclusion = j.next();
                if (Utils.isAncestorOrEqual(exclusion, root)) {
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

    private void removeDuplicates(List<File> files) {
        List<File> newFiles = new ArrayList<File>();
        outter: for (Iterator<File> i = files.iterator(); i.hasNext();) {
            File file = i.next();
            for (Iterator<File> j = newFiles.iterator(); j.hasNext();) {
                File includedFile = j.next();
                if (Utils.isAncestorOrEqual(includedFile, file) && (file.isFile() || !VersioningSupport.isFlat(includedFile))) continue outter;
                if (Utils.isAncestorOrEqual(file, includedFile) && (includedFile.isFile() || !VersioningSupport.isFlat(file))) {
                    j.remove();
                }
            }
            newFiles.add(file);
        }
        files.clear();
        files.addAll(newFiles);
    }
    
    private void exclusionRemoved(File exclusion, File root) {
        File [] exclusionChildren = exclusion.listFiles();
        if (exclusionChildren == null) return;
        for (int i = 0; i < exclusionChildren.length; i++) {
            File child = exclusionChildren[i];
            if (!Utils.isAncestorOrEqual(root, child)) {
                exclusions.add(child);
            }
        }
    }

    public List<File> getRoots() {
        return rootFiles;
    }

    public List<File> getExclusions() {
        return exclusions;
    }

    /**
     * Gets exact set of files to operate on, it is effectively defined as (rootFiles - exclusions). This set
     * is NOT suitable for Update command because Update should operate on all rootFiles and just exclude some subfolders.
     * Otherwise update misses new files and folders directly in rootFiles folders. 
     *  
     * @return files to operate on
     */ 
    public File [] getFiles() {
        return filteredFiles.toArray(new File[filteredFiles.size()]);
    }

    public File[] getRootFiles() {
        return rootFiles.toArray(new File[rootFiles.size()]);
    }
    
    public boolean contains(File file) {
        outter : for (Iterator i = rootFiles.iterator(); i.hasNext();) {
            File root = (File) i.next();
            if (SvnUtils.isParentOrEqual(root, file)) {
                for (Iterator j = exclusions.iterator(); j.hasNext();) {
                    File excluded = (File) j.next();
                    if (Utils.isAncestorOrEqual(excluded, file)) {
                        continue outter;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public static final List<File> getEmptyList() {
        return Collections.emptyList();
    }
}
