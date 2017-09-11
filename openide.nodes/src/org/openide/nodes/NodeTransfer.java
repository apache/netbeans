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

package org.openide.nodes;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.MultiTransferObject;
import org.openide.util.datatransfer.PasteType;

/** Class that contains specific datatransfer flavors and methods to work with
* nodes. There are flavors to allow a node
* to be copied or cut, and to decide its paste types.
* <p>This is a dummy utility class--no instances are possible.
*
* @author Jaroslav Tulach
*/
public abstract class NodeTransfer extends Object {
    /** Constants for drag-n-drop operations.
    * Are exactly the same as constants
    * in {@link DnDConstants}.
    */
    public static final int DND_NONE = DnDConstants.ACTION_NONE;
    public static final int DND_COPY = DnDConstants.ACTION_COPY;
    public static final int DND_MOVE = DnDConstants.ACTION_MOVE;
    public static final int DND_COPY_OR_MOVE = DnDConstants.ACTION_COPY | DnDConstants.ACTION_MOVE;
    public static final int DND_LINK = DnDConstants.ACTION_LINK;
    public static final int DND_REFERENCE = DnDConstants.ACTION_LINK;

    /** Constant indicating copying to the clipboard.
    * Equal to {@link #DND_COPY}, because
    * copy to clipboard and d'n'd copy should be the same.
    */
    public static final int CLIPBOARD_COPY = DND_COPY;

    /** Constant indicating cutting to the clipboard.
    */
    public static final int CLIPBOARD_CUT = 0x04;

    /** Generic mask for copying nodes (do not destroy the original).
    * Equal to {@link #CLIPBOARD_COPY} or {@link #DND_COPY}.
    */
    public static final int COPY = CLIPBOARD_COPY | DND_COPY;

    /** Generic mask for moving nodes (destroy the original).
    * Equal to {@link #CLIPBOARD_CUT} or {@link #DND_MOVE}.
    */
    public static final int MOVE = CLIPBOARD_CUT | DND_MOVE;

    /** Flavor for representation class {@link NodeTransfer.Paste}.
    * Provides methods for obtaining a set of {@link PasteType}s when
    * the target node is known.
    */
    private static final DataFlavor nodePasteFlavor;
    static {
        try {
            nodePasteFlavor = new DataFlavor(
                    "application/x-java-openide-nodepaste;class=org.openide.nodes.Node", // NOI18N
                    Node.getString("LBL_nodePasteFlavor"),
                    Node.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    private static final String dndMimeType = "application/x-java-openide-nodednd;class=org.openide.nodes.Node;mask="; //NOI18N

    private NodeTransfer() {
    }

    /** Creates data flavor for given mask of dnd actions.
    * @param actions any mask of dnd constants DND_* and CLIPBOARD_*
    */
    private static DataFlavor createDndFlavor(int actions) {
        Exception ex;
        try {
            return new DataFlavor(dndMimeType + actions,
                    null, Node.class.getClassLoader());
        } catch (IllegalArgumentException iae) {
            ex = iae;
        } catch (ClassNotFoundException cnfE) {
            ex = cnfE;
        }
        throw new IllegalStateException("Cannot createDndFlavor(" + actions + ")", ex); // NOI18N
    }

    /** Creates transferable that represents a node operation, such as cut-to-clipboard.
    * The transferable will be recognizable by {@link #node}, {@link #nodes}, and {@link #cookie}.
    *
    * @param n the node to create a transferable for
    * @param actions the action performed on the node
    * @return the transferable
    */
    public static ExTransferable.Single transferable(final Node n, int actions) {
        return new ExTransferable.Single(createDndFlavor(actions)) {
                public Object getData() {
                    return n;
                }
            };
    }

    /** Obtain a node from a transferable.
    * Probes the transferable in case it includes a flavor corresponding
    * to a node operation (which you must specify a mask for).
    *
    * @param t transferable
    * @param action one of the <code>DND_*</code> or <code>CLIPBOARD_*</code> constants
    * @return the node or <code>null</code>
    */
    public static Node node(Transferable t, int action) {
        DataFlavor[] flavors = t.getTransferDataFlavors();

        if (flavors == null) {
            return null;
        }

        int len = flavors.length;

        String subtype = "x-java-openide-nodednd"; // NOI18N
        String primary = "application"; // NOI18N
        String mask = "mask"; // NOI18N

        for (int i = 0; i < len; i++) {
            DataFlavor df = flavors[i];

            if (df.getSubType().equals(subtype) && df.getPrimaryType().equals(primary)) {
                try {
                    int m = Integer.valueOf(df.getParameter(mask)).intValue();

                    if ((m & action) != 0) {
                        // found the node
                        return (Node) t.getTransferData(df);
                    }
                } catch (NumberFormatException nfe) {
                    maybeReportException(nfe);
                } catch (ClassCastException cce) {
                    maybeReportException(cce);
                } catch (IOException ioe) {
                    maybeReportException(ioe);
                } catch (UnsupportedFlavorException ufe) {
                    maybeReportException(ufe);
                }
            }
        }

        return null;
    }

    /** Obtain a list of nodes from a transferable.
    * If there is only a single node in the transferable, this will just return a singleton
    * array like {@link #node}.
    * If there is a {@link ExTransferable#multiFlavor multiple transfer} (of at least one element),
    * each element of which
    * contains a node, then an array of these will be returned.
    * If neither of these things is true, <code>null</code> will be returned.
    * <p>This is a convenience method intended for those who wish to specially support pastes
    * of multiple nodes at once. (By default, an explorer will
    * fall back to presenting each component of a multiple-item transferable separately when checking for paste
    * types on a target node, so if you have only one paste type and it makes no difference whether all of the nodes
    * are pasted together or separately, you can just use {@link #node}.)
    * <p>If you wish to test for cookies, you should do so manually
    * according to your specific logic.
    * @param t the transferable to probe
    * @param action a DnD or clipboard constant
    * @return a non-empty array of nodes, or <code>null</code>
    */
    public static Node[] nodes(Transferable t, int action) {
        try {
            if (t.isDataFlavorSupported(ExTransferable.multiFlavor)) {
                MultiTransferObject mto = (MultiTransferObject) t.getTransferData(ExTransferable.multiFlavor);
                int count = mto.getCount();
                Node[] ns = new Node[count];
                boolean ok = true;

                for (int i = 0; i < count; i++) {
                    Node n = node(mto.getTransferableAt(i), action);

                    if (n == null) {
                        ok = false;

                        break;
                    } else {
                        ns[i] = n;
                    }
                }

                if (ok && (count > 0)) {
                    return ns;
                }
            } else {
                Node n = node(t, action);

                if (n != null) {
                    return new Node[] { n };
                }
            }
        } catch (ClassCastException cce) {
            maybeReportException(cce);
        } catch (IOException ioe) {
            maybeReportException(ioe);
        } catch (UnsupportedFlavorException ufe) {
            maybeReportException(ufe);
        }

        return null;
    }

    /** Obtain a cookie instance from the copied node in a transferable.
    * <P>
    * First of all it checks whether the given transferable contains
    * a node and then asks for the cookie.
    * <p>If you wish to specially support multiple-node transfers, please use {@link #nodes}
    * and manually check for the desired combination of cookies.
    *
    * @param t transferable to check in
    * @param cookie cookie representation class to look for
    * @param action the action which was used to store the node
    *
    * @return cookie or <code>null</code> if it does not exist
    */
    public static <T extends Node.Cookie> T cookie(Transferable t, int action, Class<T> cookie) {
        Node n = node(t, action);

        return (n == null) ? null : n.getCookie(cookie);
    }

    /** Creates transfer object that is used to carry an intelligent
    * paste source through transferable or clipboard.
    * {@link #findPaste} can retrieve it.
    * @param paste the intelligent source of paste types
    * @return the transferable
    */
    public static ExTransferable.Single createPaste(final Paste paste) {
        return new ExTransferable.Single(nodePasteFlavor) {
                public Object getData() {
                    return paste;
                }
            };
    }

    /** Find an intelligent source of paste types in a transferable.
    * Note that {@link AbstractNode#createPasteTypes} looks for this
    * by default, so cut/copied nodes may specify how they may be pasted
    * to some external node target.
    * @param t the transferable to test
    * @return the intelligent source or <code>null</code> if none is in the transferable
    */
    public static Paste findPaste(Transferable t) {
        try {
            if (t.isDataFlavorSupported(nodePasteFlavor)) {
                return (Paste) t.getTransferData(nodePasteFlavor);
            }
        } catch (ClassCastException cce) {
            maybeReportException(cce);
        } catch (IOException ioe) {
            maybeReportException(ioe);
        } catch (UnsupportedFlavorException ufe) {
            maybeReportException(ufe);
        }

        return null;
    }

    /** Print a stack trace if debugging is on.
    * Used for exceptions that could occur when probing transferables,
    * which should not interrupt the probing with an error, but
    * indicate a bug elsewhere and should be reported somehow.
    * @param e the exception
    */
    private static void maybeReportException(Exception e) {
        Logger.getLogger(NodeTransfer.class.getName()).log(Level.WARNING, "Node transfer error: {0}", e.getMessage());
        Logger.getLogger(NodeTransfer.class.getName()).log(Level.INFO, null, e);
    }

    /** An intelligent source of paste types (ways how to paste)
    * for a target node.
    * <P>
    * Each node should check for this
    * type in a paste operation to allow anyone to insert something
    * into it.
    * <P>
    * Sample example of implementation of {@link Node#getPasteTypes}:
    * <p><code><PRE>
    *   public PasteType[] getPasteTypes (Transferable t) {
    *     NodeTransfer.Paste p = (NodeTransfer.Paste)t.getTransferData (
    *       NodeTransfer.nodePasteFlavor
    *     );
    *     return p.types (this);
    *   }
    * </PRE></code>
    */
    public interface Paste {
        /** Method that checks the type of target node and can
        * decide which paste types it supports.
        *
        * @param target the target node
        * @return array of paste types that are valid for such a target node
        */
        public PasteType[] types(Node target);
    }
}
