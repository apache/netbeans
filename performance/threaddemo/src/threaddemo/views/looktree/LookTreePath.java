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

import javax.swing.tree.TreePath;

/**
 * Path from root to a lower "bottom" node in the look tree.
 * @author Jesse Glick
 */
public final class LookTreePath extends TreePath {

    private final LookTreeNode bottom;

    public LookTreePath(LookTreeNode node) {
        bottom = node;
    }

    public Object[] getPath() {
        int c = getPathCount();
        Object[] path = new Object[c];
        LookTreeNode n = bottom;
        for (int i = c - 1; i >= 0; i--) {
            path[i] = n;
            n = n.getParent();
        }
        return path;
    }
    
    public int getPathCount() {
        LookTreeNode n = bottom;
        int i = 0;
        while (n != null) {
            i++;
            n = n.getParent();
        }
        return i;
    }
    
    public Object getPathComponent(int x) {
        int c = getPathCount();
        LookTreeNode n = bottom;
        for (int i = 0; i < c - x - 1; i++) {
            n = n.getParent();
        }
        return n;
    }
    
    public TreePath pathByAddingChild(Object child) {
        return new LookTreePath((LookTreeNode)child);
    }
    
    public boolean equals(Object o) {
        return (o instanceof LookTreePath) &&
            ((LookTreePath)o).bottom == bottom;
    }
    
    public int hashCode() {
        return bottom.hashCode();
    }
    
    public Object getLastPathComponent() {
        return bottom;
    }
    
    public TreePath getParentPath() {
        LookTreeNode p = bottom.getParent();
        if (p != null) {
            return new LookTreePath(p);
        } else {
            return null;
        }
    }
    
}

