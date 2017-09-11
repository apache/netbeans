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
package org.openide.actions;

import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.*;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/** Move an item down in a list.
* This action is final only for performance reasons.
* @see Index
*
* @author   Ian Formanek, Dafe Simonek
*/
public final class MoveDownAction extends NodeAction {
    /** the key to listener to reorder of selected nodes */
    private static final String PROP_ORDER_LISTENER = "sellistener"; // NOI18N

    /** Holds index cookie on which we are listening */
    private Reference<Index> curIndexCookie;

    /* Initilizes the set of properties.
    */
    protected void initialize() {
        super.initialize();

        // initializes the listener
        OrderingListener sl = new OrderingListener();
        putProperty(PROP_ORDER_LISTENER, sl);
    }

    private Index getCurIndexCookie() {
        return curIndexCookie == null ? null : curIndexCookie.get();
    }

    protected void performAction(Node[] activatedNodes) {
        // we need to check activatedNodes, because there's no
        // guarantee that they not changed between enable() and
        // performAction calls
        Index cookie = getIndexCookie(activatedNodes);

        if (cookie == null) {
            return;
        }

        int nodeIndex = cookie.indexOf(activatedNodes[0]);

        if ((nodeIndex >= 0) && (nodeIndex < (cookie.getNodesCount() - 1))) {
            cookie.moveDown(nodeIndex);
        }
    }

    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(Node[] activatedNodes) {
        // remove old listener, if any
        Index idx = getCurIndexCookie();

        if (idx != null) {
            idx.removeChangeListener((ChangeListener) getProperty(PROP_ORDER_LISTENER));
            idx = null;
        }

        Index cookie = getIndexCookie(activatedNodes);

        if (cookie == null) {
            return false;
        }

        int nodeIndex = cookie.indexOf(activatedNodes[0]);

        // now start listening to reordering changes
        cookie.addChangeListener((OrderingListener) getProperty(PROP_ORDER_LISTENER));
        curIndexCookie = new WeakReference<Index>(cookie);

        return (nodeIndex >= 0) && (nodeIndex < (cookie.getNodesCount() - 1));
    }

    public String getName() {
        return NbBundle.getMessage(MoveDownAction.class, "MoveDown");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(MoveDownAction.class);
    }

    /** Helper method. Returns index cookie or null, if some
    * conditions aren't satisfied */
    private Index getIndexCookie(Node[] activatedNodes) {
        if ((activatedNodes == null) || (activatedNodes.length != 1)) {
            return null;
        }

        Node parent = activatedNodes[0].getParentNode();

        if (parent == null) {
            return null;
        }

        return (Index) parent.getCookie(Index.class);
    }

    /** Listens to the ordering changes and enables/disables the
    * action if appropriate */
    private final class OrderingListener implements ChangeListener {
        OrderingListener() {
        }

        public void stateChanged(ChangeEvent e) {
            Node[] activatedNodes = getActivatedNodes();
            Index cookie = getIndexCookie(activatedNodes);

            if (cookie == null) {
                setEnabled(false);
            } else {
                int nodeIndex = cookie.indexOf(activatedNodes[0]);
                setEnabled((nodeIndex >= 0) && (nodeIndex < (cookie.getNodesCount() - 1)));
            }
        }
    }
}
