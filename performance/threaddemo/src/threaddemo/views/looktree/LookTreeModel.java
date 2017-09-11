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

package threaddemo.views.looktree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.spi.looks.LookSelector;

/**
 * Tree model displaying a tree of represented objects using looks.
 * @author Jesse Glick
 */
final class LookTreeModel implements TreeModel {

    private static final Logger logger = Logger.getLogger(LookTreeModel.class.getName());
    
    private final Object rootObject;
    private final LookSelector sel;
    private LookTreeNode root;
    private final List<TreeModelListener> listeners;
    
    public LookTreeModel(Object root, LookSelector sel) {
        listeners = new ArrayList<TreeModelListener>();
        this.rootObject = root;
        this.sel = sel;
    }
    
    public void addNotify() {
        root = LookTreeNode.createRoot(rootObject, sel, this);
        fireChildrenChange(root);
    }
    
    public void removeNotify() {
        root.forgetEverything();
        root = null;
    }
    
    public Object getRoot() {
        return root;
    }
    
    public Object getChild(Object parent, int index) {
        LookTreeNode n = (LookTreeNode)parent;
        return n.getChild(index);
    }
    
    public int getChildCount(Object parent) {
        LookTreeNode n = (LookTreeNode)parent;
        //logger.log(Level.FINER, "childCount of {0} is {1}", new Object[] {parent, n.getChildren().size()});
        return n.getChildCount();
    }
    
    public int getIndexOfChild(Object parent, Object child) {
        LookTreeNode n = (LookTreeNode)parent;
        return n.getIndexOfChild((LookTreeNode)child);
    }
    
    public boolean isLeaf(Object node) {
        LookTreeNode n = (LookTreeNode)node;
        return n.isLeaf();
    }
    
    public void addTreeModelListener(TreeModelListener l) {
        listeners.add(l);
    }
    
    public void removeTreeModelListener(TreeModelListener l) {
        listeners.remove(l);
    }
    
    @SuppressWarnings("unchecked")
    public void valueForPathChanged(TreePath path, Object newValue) {
        LookTreeNode n = (LookTreeNode)path.getLastPathComponent();
        try {
            n.getLook().rename( n.getData(), (String)newValue, n.getLookup() );
            // XXX cell renderer does not adjust size to match new value...
        } catch (IOException e) {
            // More or less normal.
            logger.info(e.toString());
        }
    }
    
    void fireDisplayChange(LookTreeNode source) {
        if (listeners.isEmpty()) {
            return;
        }
        LookTreeNode parent = source.getParent();
        TreePath path = findPath(parent != null ? parent : source);
        int[] childIndices = parent != null ? new int[] {getIndexOfChild(parent, source)} : null;
        Object[] children = parent != null ? new Object[] {source} : null;
        TreeModelEvent ev = new TreeModelEvent(this, path, childIndices, children);
        for (TreeModelListener l : listeners) {
            l.treeNodesChanged(ev);
        }
    }
    
    void fireChildrenChange(LookTreeNode source) {
        logger.log(Level.FINER, "fireChildrenChange: {0}", source);
        if (listeners.isEmpty()) {
            return;
        }
        // XXX this is crude, could try to actually compute added/removed children...
        TreePath path = (source == root) ? null : findPath(source.getParent());
        TreeModelEvent ev = new TreeModelEvent(this, path, null, null);
        for (TreeModelListener l : listeners) {
            logger.log(Level.FINER, "firing: {0} to {1}", new Object[] {ev, l});
            l.treeStructureChanged(ev);
        }
    }
    
    private TreePath findPath(LookTreeNode node) {
        /*
        ArrayList l = new ArrayList(20);
        for (LookTreeNode n = node; n != null; n = n.getParent()) {
            l.add(n);
        }
        Collections.reverse(l);
        return new TreePath(l.toArray());
         */
        return new LookTreePath(node);
    }
    
}
