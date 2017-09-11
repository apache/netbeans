/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
            updateKeys(sortedProjectBookmarks.toArray(new ProjectBookmarks[sortedProjectBookmarks.size()]));
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
