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

import java.util.logging.Logger;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.*;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import java.util.Arrays;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/** Move an item up in a list.
*
* @see Index
* @author Ian Formanek, Jan Jancura, Dafe Simonek
*/
public final class MoveUpAction extends NodeAction {
    /** the key to listener to reorder of selected nodes */
    private static final String PROP_ORDER_LISTENER = "sellistener"; // NOI18N
    private static Logger err = Logger.getLogger("org.openide.actions.MoveUpAction"); // NOI18N

    /** Holds index cookie on which we are listening */
    private Reference curIndexCookie;

    /* Initilizes the set of properties.
    */
    protected void initialize() {
        err.fine("initialize");

        super.initialize();

        // initializes the listener
        OrderingListener sl = new OrderingListener();
        putProperty(PROP_ORDER_LISTENER, sl);
    }

    /** Getter for curIndexCookie */
    private Index getCurIndexCookie() {
        return ((curIndexCookie == null) ? null : (Index) curIndexCookie.get());
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

        if (nodeIndex > 0) {
            cookie.moveUp(nodeIndex);
        }
    }

    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(Node[] activatedNodes) {
        err.fine(
            "enable; activatedNodes=" + ((activatedNodes == null) ? null : Arrays.asList(activatedNodes))
        );

        // remove old listener, if any
        Index idx = getCurIndexCookie();

        if (idx != null) {
            idx.removeChangeListener((ChangeListener) getProperty(PROP_ORDER_LISTENER));
        }

        Index cookie = getIndexCookie(activatedNodes);

        if (err != null) {
            err.fine("enable; cookie=" + cookie);
        }

        if (cookie == null) {
            return false;
        }

        // now start listening to reordering changes
        cookie.addChangeListener((OrderingListener) getProperty(PROP_ORDER_LISTENER));
        curIndexCookie = new WeakReference<Index>(cookie);

        int index = cookie.indexOf(activatedNodes[0]);

        if (err != null) {
            err.fine("enable; index=" + index);

            if (index == -1) {
                Node parent = activatedNodes[0].getParentNode();
                err.fine(
                    "enable; parent=" + parent + "; parent.children=" + Arrays.asList(parent.getChildren().getNodes())
                );
            }
        }

        return index > 0;
    }

    public String getName() {
        return NbBundle.getMessage(MoveUpAction.class, "MoveUp");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(MoveUpAction.class);
    }

    /** Helper method. Returns index cookie or null, if some
    * conditions weren't satisfied */
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

            err.fine(
                "stateChanged; activatedNodes=" + ((activatedNodes == null) ? null : Arrays.asList(activatedNodes))
            );

            Index cookie = getIndexCookie(activatedNodes);

            if (err != null) {
                err.fine("stateChanged; cookie=" + cookie);
            }

            if (cookie == null) {
                setEnabled(false);
            } else {
                int index = cookie.indexOf(activatedNodes[0]);

                if (err != null) {
                    err.fine("stateChanged; index=" + index);
                }

                setEnabled(index > 0);
            }
        }
    }
}
