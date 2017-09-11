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
package org.openide.explorer.view;

import org.openide.nodes.Node;

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;



/**
 * Manager for explorer DnD.
 *
 *
 * @author  Jiri Rechtacek
 *
 * @see TreeViewDragSupport
 * @see TreeViewDropSupport
 */
final class ExplorerDnDManager {
    /** Singleton instance of explorer dnd manager. */
    private static ExplorerDnDManager defaultDnDManager;
    private Node[] draggedNodes;
    private Transferable draggedTransForCut;
    private Transferable draggedTransForCopy;
    private boolean isDnDActive = false;
    private int nodeAllowed = 0;
    private boolean maybeExternalDnD = false;

    /** Creates a new instance of <code>WindowsDnDManager</code>. */
    private ExplorerDnDManager() {
    }

    /** Gets the singleton instance of this window dnd manager. */
    static synchronized ExplorerDnDManager getDefault() {
        if (defaultDnDManager == null) {
            defaultDnDManager = new ExplorerDnDManager();
        }

        return defaultDnDManager;
    }

    void setDraggedNodes(Node[] n) {
        draggedNodes = n;
    }

    Node[] getDraggedNodes() {
        return draggedNodes;
    }

    void setDraggedTransferable(Transferable trans, boolean isCut) {
        if (isCut) {
            draggedTransForCut = trans;
        } else {
            draggedTransForCopy = trans;
        }
    }

    Transferable getDraggedTransferable(boolean isCut) {
        if (isCut) {
            return draggedTransForCut;
        }

        // only for copy
        return draggedTransForCopy;
    }

    void setNodeAllowedActions(int actions) {
        nodeAllowed = actions;
    }

    final int getNodeAllowedActions() {
        if( !isDnDActive && maybeExternalDnD )
            return DnDConstants.ACTION_COPY_OR_MOVE;
        
        return nodeAllowed;
    }

    void setDnDActive(boolean state) {
        isDnDActive = state;
    }

    boolean isDnDActive() {
        return isDnDActive || maybeExternalDnD;
    }

    int getAdjustedDropAction(int action, int allowed) {
        int possibleAction = action;

        if ((possibleAction & allowed) == 0) {
            possibleAction = allowed;
        }

        if ((possibleAction & getNodeAllowedActions()) == 0) {
            possibleAction = getNodeAllowedActions();
        }

        return possibleAction;
    }

    void setMaybeExternalDragAndDrop( boolean maybeExternalDnD ) {
        this.maybeExternalDnD = maybeExternalDnD;
    }
}
