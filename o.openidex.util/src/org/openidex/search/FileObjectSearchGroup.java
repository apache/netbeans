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

package org.openidex.search;

import java.util.ArrayList;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Search group which perform search on file objects. It is a
 * convenience and the default implementation of <code>SearchGroup</code>
 * abstract class.
 *
 * @author  Peter Zavadsky
 * @author  Marian Petras
 * @see org.openidex.search.SearchGroup
 */
public class FileObjectSearchGroup extends SearchGroup {

    /**
     * {@inheritDoc} If the specified search type does not support searching
     * in <code>FileObject</code>s, the group is left unmodified, too.
     *
     * @see  SearchType#getSearchTypeClasses()
     */
    @Override
    protected void add(SearchType searchType) {
        boolean ok = false;
        for (Class clazz : searchType.getSearchTypeClasses()) {
            if (clazz == FileObject.class) {
                ok = true;
                break;
            }
        }
        if (ok) {
            super.add(searchType);
        }
    }

    /**
     * Actuall search implementation. Fires PROP_FOUND notifications.
     * Implements superclass abstract method. */
    public void doSearch() {
        FileObject[] rootFolders = getFileFolders();
        
        if (rootFolders == null) {
            return;
        }
        for (FileObject rootFolder : rootFolders) {
            if (!scanFolder(rootFolder)) {
                return;
            }
        }
    }
    
    /** Gets data folder roots on which to search. */
    private FileObject[] getFileFolders() {
        Node[] nodes = normalizeNodes(searchRoots.toArray(new Node[searchRoots.size()]));

        List<FileObject> children = new ArrayList<FileObject>(nodes.length);

        for (Node node : nodes) {
            DataFolder dataFolder = node.getCookie(DataFolder.class);
            if (dataFolder != null) {
                children.add(dataFolder.getPrimaryFile());
            }
        }

        return children.toArray(new FileObject[children.size()]);
    }
    
    /** Scans data folder recursivelly. 
     * @return <code>true</code> if scanned entire folder successfully
     * or <code>false</code> if scanning was stopped. */
    private boolean scanFolder(FileObject folder) {
        for (FileObject child : folder.getChildren()) {
            // Test if the search was stopped.
            if (stopped) {
                stopped = true;
                return false;
            }
            
            if (child.isFolder()) {
                if (!scanFolder(child)) {
                    return false;
                }
            } else {
                processSearchObject(child);
            }
        }

        return true;
    }


    /** Gets node for found object. Implements superclass method.
     * @return node delegate for found data object or <code>null</code>
     * if the object is not of <code>DataObjectType</code> */
    public Node getNodeForFoundObject(final Object object) {
        if (!(object instanceof FileObject)) {
            return null;
        }
        try {
            return DataObject.find((FileObject) object).getNodeDelegate();
        } catch (DataObjectNotFoundException dnfe) {
            return new AbstractNode(Children.LEAF) {
                @Override
                public String getName() {
                    return ((FileObject) object).getName();
                }
            };
        }
    }
      
    

    /** Removes kids from node array. Helper method. */
    private static Node[] normalizeNodes(Node[] nodes) {

        List<Node> ret = new ArrayList<Node>();

        for (Node node : nodes) {
            if (!hasParent(node, nodes)) {
                ret.add(node);
            }
        }

        return ret.toArray(new Node[ret.size()]);
    }

    /** Tests if the node has parent. Helper method. */
    private static boolean hasParent(Node node, Node[] nodes) {
        for (Node parent = node.getParentNode(); parent != null; parent = parent.getParentNode()) {
            for (Node n : nodes) {
                if (n.equals(parent)) {
                    return true;
                }
            }
        }
        return false;
    }
    
}
