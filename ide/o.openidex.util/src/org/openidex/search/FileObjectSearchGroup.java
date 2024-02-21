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
        Node[] nodes = normalizeNodes(searchRoots.toArray(new Node[0]));

        List<FileObject> children = new ArrayList<FileObject>(nodes.length);

        for (Node node : nodes) {
            DataFolder dataFolder = node.getCookie(DataFolder.class);
            if (dataFolder != null) {
                children.add(dataFolder.getPrimaryFile());
            }
        }

        return children.toArray(new FileObject[0]);
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

        return ret.toArray(new Node[0]);
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
