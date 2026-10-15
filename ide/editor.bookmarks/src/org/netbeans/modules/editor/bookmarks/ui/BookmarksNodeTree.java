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
package org.netbeans.modules.editor.bookmarks.ui;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.bookmarks.BookmarkInfo;
import org.netbeans.modules.editor.bookmarks.BookmarkManager;
import org.netbeans.modules.editor.bookmarks.BookmarkManagerEvent;
import org.netbeans.modules.editor.bookmarks.BookmarkUtils;
import org.netbeans.modules.editor.bookmarks.ProjectBookmarks;
import org.netbeans.modules.editor.bookmarks.FileBookmarks;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

/**
 * Tree of nodes used for tree view and other purposes.
 *
 * @author Miloslav Metelka
 */
public final class BookmarksNodeTree {

    // -J-Dorg.netbeans.modules.editor.bookmarks.ui.BookmarksNodeTree.level=FINE
    private static final Logger LOG = Logger.getLogger(BookmarksNodeTree.class.getName());

    private BookmarksRootNode rootNode;
    
    BookmarksNodeTree() {
        rootNode = new BookmarksRootNode();
    }
    
    Node rootNode() {
        return rootNode;
    }
    
    public void updateNodeTree() {
        BookmarkManager lockedBookmarkManager = BookmarkManager.getLocked();
        try {
            rootNode.children().updateKeys(lockedBookmarkManager.getSortedActiveProjectBookmarks());
            List<Node> projectNodes = rootNode.getChildren().snapshot();
            for (Node pNode : projectNodes) {
                ProjectBookmarksChildren pChildren =
                        (ProjectBookmarksChildren) pNode.getChildren();
                if (true) { // Possibly store changed PBs URIs and rebuild only changed PBs
                    pChildren.updateKeys(lockedBookmarkManager.getSortedFileBookmarks(pChildren.projectBookmarks));
                    for (Node fNode : pChildren.snapshot()) {
                        FileBookmarksChildren fChildren = (FileBookmarksChildren) fNode.getChildren();
                        fChildren.updateKeys(lockedBookmarkManager.getSortedBookmarks(fChildren.fileBookmarks));
                    }
                }
            }
        } finally {
            lockedBookmarkManager.unlock();
        }
    }
    
    public void updateBookmarkNodes(BookmarkManagerEvent evt) {
        List<BookmarkNode> bookmarkNodes = bookmarkNodes(false);
        for (BookmarkNode bookmarkNode : bookmarkNodes) {
            if (evt.getChange(bookmarkNode.getBookmarkInfo()) != null) {
                bookmarkNode.notifyBookmarkChanged();
            }
        }
    }
    
    public Map<BookmarkInfo,BookmarkNode> createBookmark2NodeMap() {
        List<BookmarkNode> bNodes = bookmarkNodes(true);
        Map<BookmarkInfo,BookmarkNode> bookmark2NodeMap =
                new HashMap<BookmarkInfo, BookmarkNode>(bNodes.size() << 1, 0.5f);
        for (BookmarkNode bNode : bNodes) {
            bookmark2NodeMap.put(bNode.getBookmarkInfo(), bNode);
        }
        return bookmark2NodeMap;
    }
    
    public List<BookmarkNode> bookmarkNodes(boolean addBookmarksWindowNode) {
        List<BookmarkNode> bookmarkNodes = new ArrayList<BookmarkNode>();
        if (addBookmarksWindowNode) {
            bookmarkNodes.add(new BookmarkNode(BookmarkInfo.BOOKMARKS_WINDOW));
        }
        collectBookmarkNodes(bookmarkNodes, rootNode);
        return bookmarkNodes;
    }
    
    public Node findFirstBookmarkNode(ProjectBookmarks projectBookmarks, FileObject fo) {
        if (rootNode != null) {
            List<Node> projectNodes = rootNode.getChildren().snapshot();
            for (Node pNode : projectNodes) {
                ProjectBookmarksChildren pChildren =
                        (ProjectBookmarksChildren) pNode.getChildren();
                if (pChildren.projectBookmarks == projectBookmarks) {
                    for (Node fNode : pChildren.snapshot()) {
                        FileBookmarksChildren fChildren = (FileBookmarksChildren) fNode.getChildren();
                        if (fChildren.fileBookmarks.getFileObject() == fo) {
                            List<Node> bNodes = fChildren.snapshot();
                            if (!bNodes.isEmpty()) {
                                return bNodes.get(0);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private void collectBookmarkNodes(List<BookmarkNode> bookmarkNodes, Node n) {
        if (n instanceof BookmarkNode) {
            bookmarkNodes.add((BookmarkNode)n);
        } else {
            for (Node cn : n.getChildren().getNodes(true)) {
                collectBookmarkNodes(bookmarkNodes, cn);
            }
        }
    }

    static final class BookmarksRootNode extends AbstractNode {

        public BookmarksRootNode() {
            super(new BookmarksRootNodeChildren());
        }
        
        BookmarksRootNodeChildren children() {
            return (BookmarksRootNodeChildren) getChildren();
        }
        
    }
    
    static final class BookmarksRootNodeChildren extends Children.Keys<ProjectBookmarks> {
        
        BookmarksRootNodeChildren() {
        }
        
        @Override
        protected void addNotify() {
            super.addNotify();
            BookmarkManager lockedBookmarkManager = BookmarkManager.getLocked();
            try {
                updateKeys(lockedBookmarkManager.getSortedActiveProjectBookmarks());
            } finally {
                lockedBookmarkManager.unlock();
            }
        }

        void updateKeys(List<ProjectBookmarks> sortedProjectBookmarks) {
            updateKeys(sortedProjectBookmarks.toArray(new ProjectBookmarks[0]));
        }

        void updateKeys(ProjectBookmarks[] sortedProjectBookmarks) {
            setKeys(sortedProjectBookmarks);
        }
        
        @Override
        protected Node[] createNodes(ProjectBookmarks projectBookmarks) {
            URI prjURI = projectBookmarks.getProjectURI();
            Project prj = BookmarkUtils.findProject(prjURI);
            LogicalViewProvider lvp = (prj != null) ? prj.getLookup().lookup(LogicalViewProvider.class) : null;
            Node prjNode = (lvp != null) ? lvp.createLogicalView() : null;
            if (prjNode == null) {
                prjNode = new AbstractNode(Children.LEAF);
                prjNode.setDisplayName(projectBookmarks.getProjectDisplayName());
            }
            Node retNode = new FilterNode(prjNode, new ProjectBookmarksChildren(projectBookmarks)) {
                @Override
                public boolean canCopy() {
                    return false;
                }

                @Override
                public boolean canCut() {
                    return false;
                }

                @Override
                public boolean canDestroy() {
                    return false;
                }

                @Override
                public boolean canRename() {
                    return false;
                }
            };
            return new Node[] { retNode };
        }

    }
    
    static final class ProjectBookmarksChildren extends Children.Keys<FileBookmarks> {
        
        final ProjectBookmarks projectBookmarks;
        
        ProjectBookmarksChildren(ProjectBookmarks projectBookmarks) {
            this.projectBookmarks = projectBookmarks;
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            BookmarkManager lockedBookmarkManager = BookmarkManager.getLocked();
            try {
                updateKeys(lockedBookmarkManager.getSortedFileBookmarks(projectBookmarks));
            } finally {
                lockedBookmarkManager.unlock();
            }
        }

        void updateKeys(FileBookmarks[] sortedFileBookmarks) {
            setKeys(sortedFileBookmarks);
        }
        
        @Override
        protected Node[] createNodes(FileBookmarks fileBookmarks) {
            Node foNode;
            FileObject fo = fileBookmarks.getFileObject();
            if (fo == null) {
                return null; // No node for this key
            }
            try {
                DataObject dob = DataObject.find(fo);
                foNode = dob.getNodeDelegate().cloneNode();
            } catch (DataObjectNotFoundException ex) {
                foNode = new AbstractNode(Children.LEAF);
                foNode.setDisplayName(fo.getNameExt());
            }
            return new Node[]{ new FilterNode(foNode, new FileBookmarksChildren(fileBookmarks)){
                @Override
                public boolean canCopy() {
                    return false;
                }

                @Override
                public boolean canCut() {
                    return false;
                }

                @Override
                public boolean canDestroy() {
                    return false;
                }

                @Override
                public boolean canRename() {
                    return false;
                }
            }};
        }

    }
    
    static final class FileBookmarksChildren extends Children.Keys<BookmarkInfo> {
        
        final FileBookmarks fileBookmarks;
        
        FileBookmarksChildren(FileBookmarks fileBookmarks) {
            this.fileBookmarks = fileBookmarks;
        }

        public FileBookmarks getFileBookmarks() {
            return fileBookmarks;
        }
        
        @Override
        protected void addNotify() {
            super.addNotify();
            BookmarkManager lockedBookmarkManager = BookmarkManager.getLocked();
            try {
                updateKeys(lockedBookmarkManager.getSortedBookmarks(fileBookmarks));
            } finally {
                lockedBookmarkManager.unlock();
            }
        }

        void updateKeys(BookmarkInfo[] bookmarks) {
            setKeys(bookmarks);
        }
        
        @Override
        protected Node[] createNodes(BookmarkInfo bookmark) {
            return new Node[] { new BookmarkNode(bookmark) };
        }
        
    }

}
