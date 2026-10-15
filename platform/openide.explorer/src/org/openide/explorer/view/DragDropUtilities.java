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

package org.openide.explorer.view;

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeNode;
import org.netbeans.modules.openide.explorer.ExternalDragAndDrop;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;
import org.openide.awt.Mnemonics;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.UserCancelException;
import org.openide.util.datatransfer.ExClipboard;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.ExTransferable.Multi;
import org.openide.util.datatransfer.MultiTransferObject;
import org.openide.util.datatransfer.PasteType;

/** Class that provides methods for common tasks needed during
* drag and drop when working with explorer views.
*
* @author Dafe Simonek
*/
final class DragDropUtilities extends Object {
    static final boolean dragAndDropEnabled = !GraphicsEnvironment.isHeadless() &&
            Boolean.parseBoolean(System.getProperty("netbeans.dnd.enabled", "true")); // NOI18N
    static final int NODE_UP = -1;
    static final int NODE_CENTRAL = 0;
    static final int NODE_DOWN = 1;
    static Runnable postDropRun = null;

    // helper constants
    static final int NoDrag = 0;
    static final int NoDrop = 1;

    /** No need to instantiate this class */
    private DragDropUtilities() {
    }

    /** Utility method.
    * @return true if given node supports given action,
    * false otherwise.
    */
    static boolean checkNodeForAction(Node node, int dragAction) {
        if (
            node.canCut() &&
                ((dragAction == DnDConstants.ACTION_MOVE) || (dragAction == DnDConstants.ACTION_COPY_OR_MOVE))
        ) {
            return true;
        }

        if (
            node.canCopy() &&
                ((dragAction == DnDConstants.ACTION_COPY) || (dragAction == DnDConstants.ACTION_COPY_OR_MOVE) ||
                (dragAction == DnDConstants.ACTION_LINK) || (dragAction == DnDConstants.ACTION_REFERENCE))
        ) {
            return true;
        }

        // hmmm, conditions not satisfied..
        return false;
    }

    /** Gets right transferable of given nodes (according to given
    * drag action) and also converts the transferable.<br>
    * Can be called only with correct action constant.
    * @return The transferable.
    */
    static Transferable getNodeTransferable(Node[] nodes, int dragAction)
    throws IOException {
        Transferable[] tArray = new Transferable[nodes.length];

        for (int i = 0; i < nodes.length; i++) {
            if ((dragAction & DnDConstants.ACTION_MOVE) != 0) {
                tArray[i] = nodes[i].clipboardCut();
            } else {
                tArray[i] = nodes[i].drag ();
            }
        }
        Transferable result;
        if (tArray.length == 1) {
            // only one node, so return regular single transferable
            result = tArray[0];
        } else {
            // enclose the transferables into multi transferable
            result = ExternalDragAndDrop.maybeAddExternalFileDnd( new Multi(tArray) );
        }

        Clipboard c = getClipboard();
        if (c instanceof ExClipboard) {
            return ((ExClipboard) c).convert(result);
        } else {
            return result;
        }
    }

    /** Returns transferable of given node
    * @return The transferable.
    */
    static Transferable getNodeTransferable(Node node, int dragAction)
    throws IOException {
        return getNodeTransferable(new Node[] { node }, dragAction);
    }

    /** Sets a runnable it will be executed after drop action is performed.
     * @param run a runnable for execution */
    static void setPostDropRun(Runnable run) {
        postDropRun = run;
    }

    /* Invokes the stored runnable if it is there and than set to null.
     */
    private static void invokePostDropRun() {
        if (postDropRun != null) {
            SwingUtilities.invokeLater(postDropRun);
            postDropRun = null;
        }
    }

    /**
     * Performs the drop. Performs paste on given paste type.
     * (part of bugfix #37279, performPaste returns array of new nodes in target folder)
     * @param type paste type
     * @param targetFolder target folder for given paste type, can be null
     * @return array of new added nodes in target folder
     */
    static Node[] performPaste(PasteType type, Node targetFolder) {
        //System.out.println("performing drop...."+type); // NOI18N
        try {
            if (targetFolder == null) {
                // call paste action
                type.paste();

                return new Node[] {  };
            }

            Node[] preNodes = targetFolder.getChildren().getNodes(true);

            // call paste action
            type.paste();

            Node[] postNodes = targetFolder.getChildren().getNodes(true);

            // calculate new nodes
            List<Node> pre = Arrays.asList(preNodes);
            List<Node> post = Arrays.asList(postNodes);
            Iterator<Node> it = post.iterator();
            List<Node> diff = new ArrayList<Node>();

            while (it.hasNext()) {
                Node n = it.next();

                if (!pre.contains(n)) {
                    diff.add(n);
                }
            }

            return diff.toArray(new Node[0]);

            /*Clipboard clipboard = T opManager.getDefault().getClipboard();
            if (trans != null) {
              ClipboardOwner owner = trans instanceof ClipboardOwner ?
                (ClipboardOwner)trans
              :
                new StringSelection ("");
              clipboard.setContents(trans, owner);
            }*/
        } catch (UserCancelException exc) {
            // ignore - user just pressed cancel in some dialog....
            return new Node[] {  };
        } catch (IOException e) {
            Exceptions.printStackTrace(e);

            return new Node[] {  };
        }
    }

    /** Returns array of paste types for given transferable.
    * If given transferable contains multiple transferables,
    * multi paste type which encloses paste types of all contained
    * transferables is returned.
    * Returns empty array if given node did not accepted the transferable
    * (or some sub-transferables in multi transferable)
    *
    * @param node given node to ask fro paste types
    * @param trans transferable to discover
    */
    static PasteType[] getPasteTypes(Node node, Transferable trans) {
        if (!trans.isDataFlavorSupported(ExTransferable.multiFlavor)) {
            // only single, so return paste types
            PasteType[] pt = null;

            try {
                pt = node.getPasteTypes(trans);
            } catch (NullPointerException npe) {
                Exceptions.printStackTrace(npe);

                // there are not paste types
            }

            return pt;
        } else {
            // multi transferable, we must do extra work
            try {
                MultiTransferObject obj = (MultiTransferObject) trans.getTransferData(ExTransferable.multiFlavor);
                int count = obj.getCount();
                Transferable[] t = new Transferable[count];
                PasteType[] p = new PasteType[count];
                PasteType[] curTypes = null;

                // extract default paste types of transferables
                for (int i = 0; i < count; i++) {
                    t[i] = obj.getTransferableAt(i);
                    curTypes = node.getPasteTypes(t[i]);

                    // return if not accepted
                    if (curTypes.length == 0) {
                        return curTypes;
                    }

                    p[i] = curTypes[0];
                }

                // return new multi paste type
                return new PasteType[] { new MultiPasteType(t, p) };
            } catch (UnsupportedFlavorException e) {
                // ignore and return empty array
            } catch (IOException e) {
                // ignore and return empty array
            }
        }

        return new PasteType[0];
    }

    /** Returns drop type for given transferable and drop action
    * If given transferable contains multiple transferables,
    * multi paste type which encloses drop types of all contained
    * transferables is returned.
    * Returns null if given node did not accepted the transferable
    * (or some sub-transferables in multi transferable)
    *
    * @param node given node to ask fro paste types
    * @param trans transferable to discover
    * @param action drop action
    * @param dropIndex where the object is dropped
    */
    static PasteType getDropType(Node node, Transferable trans, int action, int dropIndex) {
        PasteType paste = null;
        
        try {
            paste = node.getDropType(trans, action, dropIndex);
            if (paste!=null)
                return paste;
        } catch (NullPointerException npe) {
            Exceptions.printStackTrace(npe);
        }
            
        if (trans.isDataFlavorSupported(ExTransferable.multiFlavor)) {
            // multi transferable, we must do extra work
            try {
                MultiTransferObject obj = (MultiTransferObject) trans.getTransferData(ExTransferable.multiFlavor);
                int count = obj.getCount();
                Transferable[] t = new Transferable[count];
                PasteType[] p = new PasteType[count];
                PasteType pt = null;

                // extract default drop type of transferables
                for (int i = 0; i < count; i++) {
                    t[i] = obj.getTransferableAt(i);
                    pt = node.getDropType(t[i], action, dropIndex);

                    // return null if not accepted
                    if (pt == null) {
                        return pt;
                    }

                    p[i] = pt;
                }

                // return new multi drop type
                return new MultiPasteType(t, p);
            } catch (UnsupportedFlavorException e) {
                // ignore and return null
            } catch (IOException e) {
                // ignore and return null
            }
        }

        return null;
    }

    /** Notifies user that the drop was not succesfull. */
    static void dropNotSuccesfull() {
        DialogDisplayer.getDefault().notify(
            new Message(
                NbBundle.getMessage(TreeViewDropSupport.class, "MSG_NoPasteTypes"),
                NotifyDescriptor.WARNING_MESSAGE
            )
        );
    }

    /** If our clipboard is not found return the default system clipboard. */
    private static Clipboard getClipboard() {
        Clipboard c = Lookup.getDefault().lookup(Clipboard.class);

        if (c == null) {
            c = Toolkit.getDefaultToolkit().getSystemClipboard();
        }

        return c;
    }

    /** Utility method created by Enno Sandner. Is it needed?
     * I don't know (dstrupl).
     */
    static Node secureFindNode(Object o) {
        assert o instanceof TreeNode : "Object " + o + " is instanceof TreeNode";
        try {
            return Visualizer.findNode(o);
        } catch (ClassCastException e) {
            e.printStackTrace();

            return null;
        }
    }

    /**
     * Creates and populates popup as a result of
     * dropping an item.
     * @author Enno Sandner
     */
    static JPopupMenu createDropFinishPopup(final TreeSet pasteTypes) {
        JPopupMenu menu = new JPopupMenu();

        //System.arraycopy(pasteTypes, 0, pasteTypes_, 0, pasteTypes.length);
        final JMenuItem[] items_ = new JMenuItem[pasteTypes.size()];

        ActionListener aListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JMenuItem source = (JMenuItem) e.getSource();

                    final Iterator it = pasteTypes.iterator();

                    for (int i = 0; it.hasNext(); i++) {
                        PasteType action = (PasteType) it.next();

                        if (items_[i].equals(source)) {
                            DragDropUtilities.performPaste(action, null);
                            invokePostDropRun();

                            break;
                        }
                    }
                }
            };

        Iterator it = pasteTypes.iterator();

        for (int i = 0; it.hasNext(); i++) {
            items_[i] = new JMenuItem();
            Mnemonics.setLocalizedText(items_[i], ((PasteType) it.next()).getName());
            items_[i].addActionListener(aListener);
            menu.add(items_[i]);
        }

        menu.addSeparator();

        JMenuItem abortItem = new JMenuItem(NbBundle.getMessage(DragDropUtilities.class, "MSG_ABORT"));
        menu.add(abortItem);

        return menu;
    }

    /** Paste type used when in clipbopard is MultiTransferable */
    static final class MultiPasteType extends PasteType {
        // Attributes

        /** Array of transferables */
        Transferable[] t;

        /** Array of paste types */
        PasteType[] p;

        // Operations

        /** Constructs new MultiPasteType for the given
        * transferables and paste types.*/
        MultiPasteType(Transferable[] t, PasteType[] p) {
            this.t = t;
            this.p = p;
        }

        /** Performs the paste action.
        * @return Transferable which should be inserted into the
        *   clipboard after paste action. It can be null, which means
        *   that clipboard content should be cleared.
        */
        public Transferable paste() throws IOException {
            int size = p.length;
            Transferable[] arr = new Transferable[size];

            // perform paste for all source transferables
            for (int i = 0; i < size; i++) {
                //System.out.println("Pasting #" + i); // NOI18N
                arr[i] = p[i].paste();
            }

            return new Multi(arr);
        }
    }
     // end of MultiPasteType
}
