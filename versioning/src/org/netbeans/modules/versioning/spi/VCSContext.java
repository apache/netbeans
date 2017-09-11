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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.versioning.spi;

import org.openide.nodes.Node;
import org.openide.util.Lookup;

import java.io.File;
import java.io.FileFilter;
import java.util.*;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 * This encapsulates a context, typically set of selected files or nodes. Context is passed to VCSAnnotators when
 * asked for actions available on a given context or to annotate a name (label) representing a context.
 * 
 * @author Maros Sandor
 */
public final class VCSContext {

    private org.netbeans.modules.versioning.core.spi.VCSContext delegate;
    
    /**
     * VCSContext that contains no files.
     */
    public static final VCSContext EMPTY = new VCSContext(org.netbeans.modules.versioning.core.spi.VCSContext.EMPTY);
    
    private Set<File> unfilteredRootFiles;
    private Set<File> exclusions;
    private Set<File> rootFiles;
    private Set<File> computedFilesCached;
    private FileFilter fileFilterCached;
    
    static {
        org.netbeans.modules.versioning.Accessor.IMPL = new AccessorImpl();
    }
        
    VCSContext(org.netbeans.modules.versioning.core.spi.VCSContext delegate) {
        assert accept(delegate);
        this.delegate = delegate;
    }
    
    /**
     * Initializes the context from array of nodes (typically currently activated nodes).
     * Nodes are converted to Files based on their nature. 
     * For example Project Nodes are queried for their SourceRoots and those roots become root files of this context and
     * exclusions list is constructed using sourceRoot.contains() queries.
     * 
     * Nodes' lookups are examined in the following way (the first applied rule wins):
     * - if there's a File, the File is added to set of root files
     * - if there's a Project, project's source roots of type Sources.TYPE_GENERIC are added to set of root files and
     *   all direct children that do not belong to the project (sg.contains() == false) are added to set of exclusions
     * - if there's a FileObject, it is added to set of root files
     * - if there's a DataObject, all dao.files() are added to set of root files 
     * 
     * @param nodes array of Nodes
     * @return VCSContext containing nodes and corresponding files they represent
     */
    public synchronized static VCSContext forNodes(Node[] nodes) {
        final org.netbeans.modules.versioning.core.spi.VCSContext delegate = org.netbeans.modules.versioning.core.spi.VCSContext.forNodes(nodes);
        return accept(delegate) ? new VCSContext(delegate) : EMPTY;
    }
        
    /**
     * Returns the smallest possible set of all files that lie under Root files and are NOT 
     * under some Excluded file. 
     * Technically, for every file in the returned set all of the following is true:
     * 
     * - the file itself or at least one of its ancestors is a root file/folder
     * - neither the file itself nor any of its ancestors is an exluded file/folder
     * - the file passed through the supplied FileFilter
     *  
     * @param filter custom file filter
     * @return filtered set of files that must pass through the filter
     */
    public synchronized Set<File> computeFiles(FileFilter filter) {
        if (computedFilesCached == null || filter != fileFilterCached) {
            Set<VCSFileProxy> files = delegate.computeFiles(new ProxyFileFilter(filter));
            computedFilesCached = toFileSet(files);
            fileFilterCached = filter;
        }
        return computedFilesCached;
    }
    
    /**
     * Retrieves elements that make up this VCS context. The returned lookup may be empty
     * or may contain any number of the following elements:
     * - instances of Node that were originally used to construct this context object
     *
     * @return Lookup lookup of this VCSContext
     */ 
    public Lookup getElements() {
        return delegate.getElements();
    }

    /**
     * Retrieves set of files/folders that represent this context.
     * This set contains all files the user selected, unfiltered.
     * For example, if the user selects two elements: folder /var and file /var/Foo.java then getFiles() 
     * returns both of them and getRootFiles returns only the folder /var. 
     * This method is suitable for versioning systems that DO manage folders, such as Clearcase. 
     * 
     * @return Set<File> set of Files this context represents
     * @see #getRootFiles() 
     * @since 1.6
     */ 
    public Set<File> getFiles() {
        if (unfilteredRootFiles == null) {
            unfilteredRootFiles = toFileSet(delegate.getFiles());
        }
        return unfilteredRootFiles;
    }

    /**
     * Retrieves set of root files/folders that represent this context.
     * This set only contains context roots, not files/folders that are contained within these roots.
     * For example, if the user selects two elements: folder /var and file /var/Foo.java then getFiles() 
     * returns both of them and getRootFiles returns only the folder /var. 
     * This method is suitable for versioning systems that do not manage folders, such as CVS. 
     * 
     * @return Set<File> set of Files this context represents
     * @see #getFiles() 
     */ 
    public Set<File> getRootFiles() {
        if (rootFiles == null) {
            rootFiles = toFileSet(delegate.getRootFiles());
        }
        return rootFiles;
    }

    /**
     * Retrieves set of files/folders that are excluded from this context. Exclusions are files or folders that
     * are descendants of a root folder and should NOT be a part of a versioning operation. For example, an CVS/Update command
     * run on a project that contains a subproject should not touch any files in the subproject. Therefore the VCSContext for
     * the action would contain one root file (the project's root) and one exclusion (subproject root).
     * 
     * @return Set<File> set of files and folders that are not part of (are excluded from) this context. 
     * All their descendands are excluded too.
     */ 
    public Set<File> getExclusions() {
        if(exclusions == null) {
            exclusions = toFileSet(delegate.getExclusions());
        }
        return exclusions;
    }

    /**
     * Determines whether the supplied File is contained in this context. In other words, the file must be either a root file/folder
     * or be a descendant of a root folder and also must NOT be an excluded file/folder or be a descendant of an excluded folder. 
     * 
     * @param file a File to test
     * @return true if this context contains the supplied file, false otherwise 
     */ 
    public boolean contains(File file) {
        return delegate.contains(VCSFileProxy.createFileProxy(file));
    }
        
    private static Set<File> toFileSet(Set<VCSFileProxy> files) {
        Set<File> s = new HashSet<File>(files.size());
        for (VCSFileProxy fileProxy : files) {
            s.add(fileProxy.toFile());
        }
        return s;
    }    

    private class ProxyFileFilter implements org.netbeans.modules.versioning.core.spi.VCSContext.FileFilter {
        private final FileFilter filter;

        public ProxyFileFilter(FileFilter filter) {
            this.filter = filter;
        }
        
        @Override
        public boolean accept(VCSFileProxy file) {
            return filter.accept(file.toFile());
        }
    }
    
    private static boolean accept(org.netbeans.modules.versioning.core.spi.VCSContext delegate) {
        Set<VCSFileProxy> roots = delegate.getRootFiles();
        for (VCSFileProxy root : roots) {
            if(root.toFile() ==  null) {
                return false;
            }
        }
        return true;
    }    
}
