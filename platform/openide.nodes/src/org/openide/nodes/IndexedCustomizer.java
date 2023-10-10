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

package org.openide.nodes;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Customizer;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.ExTransferable;

/** A dialog for reordering nodes. This dialog can reorder
* nodes for all implementors of the {@link Index} cookie.
* The dialog can invoke reorder actions on a given <code>Index</code>
* implementation immediatelly, or these actions can be accumulated
* and invoked at once, when the dialog is closed.
*
* <p>This class is final only for performance reasons.
*
* @author   Jan Jancura, Ian Formanek, Dafe Simonek
* @deprecated Better to use {@link Index.Support#showIndexedCustomizer} which behaves better
*             with the window system.
*/
@Deprecated
public final class IndexedCustomizer extends JDialog implements Customizer {
    // initializations ................................................................................
    static final long serialVersionUID = -8731362267771694641L;

    // variables .....................................................................................

    /** The actual JList control */
    private JList control;

    /** Buttons */
    private JButton buttonUp;

    /** Buttons */
    private JButton buttonDown;

    /** Buttons */
    private JButton buttonClose;

    /** index to sort */
    private Index index;
    private Node[] nodes;

    /** Whether or not change the order immediatelly */
    private boolean immediateReorder = true;

    /** Permutation array, which stores moves in case when
    * immediateReorder property is false */
    private int[] permutation;

    /** Listener to the changes in the nodes */
    private ChangeListener nodeChangesL;

    /** Construct a new customizer. */
    public IndexedCustomizer() {
        this(null, true);
    }

    /** Construct a dummy customizer.
     * Might not actually be used as a JDialog, however its GUI
     * layout and logic will be used.
     * Cf. #9323.
     * @param p a container on which to draw the GUI
     * @param closeButton if true, add a Close button and other dialog logic, else no
     */
    IndexedCustomizer(Container p, boolean closeButton) {
        super(TMUtil.mainWindow(), true);

        GridBagConstraints constraints;

        if (closeButton) {
            setDefaultCloseOperation(javax.swing.JDialog.DISPOSE_ON_CLOSE);

            // attach cancel also to Escape key
            getRootPane().registerKeyboardAction(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        setVisible(false);
                        dispose();
                    }
                }, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0, true),
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW
            );

            setTitle(Node.getString("LAB_order"));
        }
         // closeButton

        if (p == null) {
            p = getContentPane();
        }

        p.setLayout(new GridBagLayout());

        JLabel l = new JLabel(Node.getString("LAB_listOrder"));
        l.setDisplayedMnemonic(Node.getString("LAB_listOrder_Mnemonic").charAt(0));
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(12, 12, 2, 12);
        p.add(l, constraints);

        control = new AutoscrollJList();
        l.setLabelFor(control);
        control.addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if (control.isSelectionEmpty()) {
                        buttonUp.setEnabled(false);
                        buttonDown.setEnabled(false);
                    } else {
                        int i = control.getSelectedIndex();

                        if (i > 0) { //PENDING - jeste testovat, jestli jsou OrderedCookie.Child
                            buttonUp.setEnabled(true);
                        } else {
                            buttonUp.setEnabled(false);
                        }

                        if (i < (nodes.length - 1)) {
                            buttonDown.setEnabled(true);
                        } else {
                            buttonDown.setEnabled(false);
                        }
                    }
                }
            }
        );
        control.setCellRenderer(new IndexedListCellRenderer());
        control.setVisibleRowCount(15);
        control.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        // list has to be scrolling
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.insets = new Insets(0, 12, 11, 11);
        p.add(new JScrollPane(control), constraints);

        JPanel bb = new JPanel();

        if (closeButton) {
            buttonClose = new JButton(Node.getString("Button_close"));
            buttonClose.setMnemonic(Node.getString("Button_close_Mnemonic").charAt(0));
        }

        buttonUp = new JButton(Node.getString("Button_up"));
        buttonUp.setMnemonic(Node.getString("Button_up_Mnemonic").charAt(0));
        buttonDown = new JButton(Node.getString("Button_down"));
        buttonDown.setMnemonic(Node.getString("Button_down_Mnemonic").charAt(0));

        bb.setLayout(new GridBagLayout());

        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 5, 11);
        bb.add(buttonUp, constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weighty = 1.0;
        constraints.insets = new Insets(0, 0, 0, 11);
        bb.add(buttonDown, constraints);

        if (closeButton) {
            constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = 2;
            constraints.anchor = GridBagConstraints.SOUTH;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.insets = new Insets(0, 0, 11, 11);
            bb.add(buttonClose, constraints);
        }

        buttonUp.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int i = control.getSelectedIndex();
                    moveUp(i);
                    updateList();
                    control.setSelectedIndex(i - 1);
                    control.ensureIndexIsVisible(i - 1);
                    control.repaint();
                }
            }
        );

        buttonDown.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int i = control.getSelectedIndex();
                    moveDown(i);
                    updateList();
                    control.setSelectedIndex(i + 1);
                    control.ensureIndexIsVisible(i + 1);
                    control.repaint();
                }
            }
        );

        if (closeButton) {
            buttonClose.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        doClose();
                        dispose();
                    }
                }
            );
        }

        buttonUp.setEnabled(false);
        buttonDown.setEnabled(false);
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.VERTICAL;
        p.add(bb, constraints);

        // disable drag support, as DnD crashes all environment
        // under current implementation of VMs on unixes, ans causes
        // some deadlocks on windows...
        //dragSupport = new IndexedDragSource(control);
        //dropSupport = new IndexedDropTarget(this, dragSupport);
        if (closeButton) {
            pack();
            setBounds(org.openide.util.Utilities.findCenterBounds(getSize()));

            buttonClose.requestFocus(); // to get shortcuts to work

            buttonClose.getAccessibleContext().setAccessibleDescription(Node.getString("ACSD_Button_close"));
        }

        buttonUp.getAccessibleContext().setAccessibleDescription(Node.getString("ACSD_Button_up"));
        buttonDown.getAccessibleContext().setAccessibleDescription(Node.getString("ACSD_Button_down"));
        control.getAccessibleContext().setAccessibleDescription(Node.getString("ACSD_ListOrder"));
        p.getAccessibleContext().setAccessibleDescription(Node.getString("ACSD_IndexedCustomizer"));
        getAccessibleContext().setAccessibleDescription(Node.getString("ACSD_IndexedCustomizer"));
    }

    /** Simulate behavior of the Close button, minus actual dialog disposal.
     * Might do the same as setImmediateReorder(true), but I am not sure.
     */
    void doClose() {
        if ((!immediateReorder) && (index != null) && (permutation != null)) {
            int[] realPerm = new int[permutation.length];

            for (int i = 0; i < realPerm.length; i++) {
                realPerm[permutation[i]] = i;

                //System.out.println (i + "-->" + permutation[i]); // NOI18N
            }

            index.reorder(realPerm);
        }
    }

    // other methods ................................................................................

    /** Called when an explored context changes and the list needs to be
    * recreated.
    */
    private void updateList() {
        if (index == null) {
            return;
        }

        Node[] localNodes = index.getNodes();

        //System.out.println ("Nodes taken, size: " + localNodes.length); // NOI18N
        // obtain nodes with help from permutation array, if
        // conditions met
        if (!immediateReorder) {
            getPermutation();

            int origLength = permutation.length;
            int newLength = localNodes.length;

            if (origLength < newLength) {
                // some nodes added, we must synchronize the permutation
                nodes = new Node[newLength];

                int[] newPerm = new int[newLength];
                System.arraycopy(newPerm, 0, permutation, 0, origLength);

                for (int i = 0; i < newLength; i++) {
                    if (i < origLength) {
                        nodes[i] = localNodes[permutation[i]];
                    } else {
                        // added nodes....
                        nodes[i] = localNodes[i];
                        newPerm[i] = i;
                    }
                }

                permutation = newPerm;
            } else if (origLength > newLength) {
                // some nodes removed, we must re-initialize the permutation
                nodes = new Node[newLength];
                permutation = new int[newLength];

                for (int i = 0; i < newLength; i++) {
                    nodes[i] = localNodes[i];
                    permutation[i] = i;
                }
            } else {
                // node count is the same, only permute the nodes
                nodes = new Node[newLength];

                for (int i = 0; i < newLength; i++)
                    nodes[i] = localNodes[permutation[i]];
            }
        } else {
            nodes = localNodes.clone();
        }

        control.setListData(nodes);

        if ((nodes.length > 0) && (control.getSelectedIndex() == -1)) {
            control.setSelectedIndex(0);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(300, super.getPreferredSize().height);
    }

    /** Will reorders be reflected immediately?
    * @return <code>true</code> if so
    */
    public boolean isImmediateReorder() {
        return immediateReorder;
    }

    /** Set whether reorders will take effect immediately.
    * @param immediateReorder <code>true</code> if so
    */
    public void setImmediateReorder(boolean immediateReorder) {
        if (this.immediateReorder == immediateReorder) {
            return;
        }

        this.immediateReorder = immediateReorder;

        if (immediateReorder) {
            if (permutation != null) {
                index.reorder(permutation);
                permutation = null;
                updateList();
            }
        }
    }

    // implementation of Customizer ............................................................

    /** Set the nodes to reorder.
    * @param bean must implement {@link Index}
    * @throws IllegalArgumentException if not
    */
    public void setObject(Object bean) {
        if (bean instanceof Index) {
            index = (Index) bean;

            // add weak listener to the Index
            nodeChangesL = new ChangeListener() {
                        public void stateChanged(ChangeEvent ev) {
                            SwingUtilities.invokeLater(
                                new Runnable() {
                                    public void run() {
                                        updateList();
                                    }
                                }
                            );
                        }
                    };
            updateList();
            control.invalidate();
            validate();
            index.addChangeListener(org.openide.util.WeakListeners.change(nodeChangesL, index));
        } else {
            throw new IllegalArgumentException();
        }
    }

    // I don't change any property...
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }

    /** Moves up. Performs differently according to
    * immediateReorder property value.
    */
    private void moveUp(final int position) {
        if (index == null) {
            return;
        }

        if (immediateReorder) {
            index.moveUp(position);
        } else {
            getPermutation();

            int temp = permutation[position];
            permutation[position] = permutation[position - 1];
            permutation[position - 1] = temp;
        }
    }

    /** Moves down. Performs differently according to
    * immediateReorder property value.
    */
    private void moveDown(final int position) {
        if (index == null) {
            return;
        }

        if (immediateReorder) {
            index.moveDown(position);
        } else {
            getPermutation();

            int temp = permutation[position];
            permutation[position] = permutation[position + 1];
            permutation[position + 1] = temp;
        }
    }

    /** Safe getter for permutation.
    * Initializes permutation to identical permutation if it is null.<br>
    * index variable must not be null when this method called */
    private int[] getPermutation() {
        if (permutation == null) {
            if (nodes == null) {
                nodes = index.getNodes().clone();
            }

            permutation = new int[nodes.length];

            for (int i = 0; i < nodes.length; permutation[i] = i++)
                ;
        }

        return permutation;
    }

    /** Permute the list as given permutation dictates.
    * Sets selection to the given selected index.
    * Called from dropSupport as result of succesfull DnD operation.
    */
    void performReorder(int[] perm, int selected) {
        if (immediateReorder) {
            index.reorder(perm);
        } else {
            // merge current and reversed given permutation
            // (reverse given permutation first)
            int[] reversed = new int[perm.length];

            for (int i = 0; i < reversed.length; i++)
                reversed[perm[i]] = i;

            int[] orig = getPermutation();
            permutation = new int[orig.length];

            for (int i = 0; i < orig.length; i++) {
                permutation[i] = orig[reversed[i]];

                //        System.out.println(permutation[i] + " ----> " + i); // NOI18N
            }
        }

        updateList();
        control.setSelectedIndex(selected);
        control.repaint();
    }

    /** Implementation of drag functionality in
    * reorder dialog. */
    private static final class IndexedDragSource implements DragGestureListener, DragSourceListener {
        /** Asociated JList component where the drag will
        * take place */
        JList comp;

        /** User gesture that initiated the drag */
        DragGestureEvent dge;

        /** Out data flavor used to transfer the index */
        DataFlavor myFlavor;

        /** Creates drag source with asociated list where drag
        * will take place.
        * Also creates the default gesture and asociates this with
        * given component */
        IndexedDragSource(JList comp) {
            this.comp = comp;

            // initialize gesture
            DragSource ds = DragSource.getDefaultDragSource();
            ds.createDefaultDragGestureRecognizer(comp, DnDConstants.ACTION_MOVE, this);
        }

        /** Initiating the drag */
        public void dragGestureRecognized(DragGestureEvent dge) {
            // check allowed actions
            if ((dge.getDragAction() & DnDConstants.ACTION_MOVE) == 0) {
                return;
            }

            // prepare transferable and start the drag
            int index = comp.locationToIndex(dge.getDragOrigin());

            // no index, then no dragging...
            if (index < 0) {
                return;
            }

            //      System.out.println("Starting drag..."); // NOI18N
            // create our flavor for transferring the index
            myFlavor = new DataFlavor(
                    String.class, NbBundle.getBundle(IndexedCustomizer.class).getString("IndexedFlavor")
                );

            try {
                dge.startDrag(DragSource.DefaultMoveDrop, new IndexTransferable(myFlavor, index), this);

                // remember the gesture
                this.dge = dge;
            } catch (InvalidDnDOperationException exc) {
                Logger.getLogger(IndexedCustomizer.class.getName()).log(Level.WARNING, null, exc);

                // PENDING notify user - cannot start the drag
            }
        }

        public void dragEnter(DragSourceDragEvent dsde) {
        }

        public void dragOver(DragSourceDragEvent dsde) {
        }

        public void dropActionChanged(DragSourceDragEvent dsde) {
        }

        public void dragExit(DragSourceEvent dse) {
        }

        public void dragDropEnd(DragSourceDropEvent dsde) {
        }

        /** Utility accessor */
        DragGestureEvent getDragGestureEvent() {
            return dge;
        }
    }
     // end of IndexedDragSource

    /** Implementation of drop functionality in
    * reorder dialog. */
    private static final class IndexedDropTarget implements DropTargetListener {
        /** Asociated JList component for dropping */
        JList comp;

        /** Cell renderer which renders the list items */
        IndexedListCellRenderer cellRenderer;

        /** Indexed dialog */
        IndexedCustomizer dialog;

        /** Drag source support instance */
        IndexedDragSource ids;

        /** last index dragged over */
        int lastIndex = -1;

        /** Creates the instance, makes given component active for
        * drop operation. */
        IndexedDropTarget(IndexedCustomizer dialog, IndexedDragSource ids) {
            this.dialog = dialog;
            this.comp = dialog.control;
            this.cellRenderer = (IndexedListCellRenderer) this.comp.getCellRenderer();
            this.ids = ids;
            new DropTarget(comp, DnDConstants.ACTION_MOVE, this, true);
        }

        /** User is starting to drag over us */
        public void dragEnter(DropTargetDragEvent dtde) {
            if (!checkConditions(dtde)) {
                dtde.rejectDrag();
            } else {
                lastIndex = comp.locationToIndex(dtde.getLocation());
                cellRenderer.draggingEnter(lastIndex, ids.getDragGestureEvent().getDragOrigin(), dtde.getLocation());
                comp.repaint(comp.getCellBounds(lastIndex, lastIndex));
            }
        }

        /** User drag over us */
        public void dragOver(DropTargetDragEvent dtde) {
            if (!checkConditions(dtde)) {
                dtde.rejectDrag();

                if (lastIndex >= 0) {
                    cellRenderer.draggingExit();
                    comp.repaint(comp.getCellBounds(lastIndex, lastIndex));
                    lastIndex = -1;
                }
            } else {
                dtde.acceptDrag(DnDConstants.ACTION_MOVE);

                int index = comp.locationToIndex(dtde.getLocation());

                if (lastIndex == index) {
                    cellRenderer.draggingOver(index, ids.getDragGestureEvent().getDragOrigin(), dtde.getLocation());
                } else {
                    if (lastIndex < 0) {
                        lastIndex = index;
                    }

                    cellRenderer.draggingExit();
                    cellRenderer.draggingEnter(index, ids.getDragGestureEvent().getDragOrigin(), dtde.getLocation());
                    comp.repaint(comp.getCellBounds(lastIndex, index));
                    lastIndex = index;
                }
            }
        }

        public void dropActionChanged(DropTargetDragEvent dtde) {
        }

        /** User exits the dragging */
        public void dragExit(DropTargetEvent dte) {
            if (lastIndex >= 0) {
                cellRenderer.draggingExit();
                comp.repaint(comp.getCellBounds(lastIndex, lastIndex));
            }
        }

        /** Takes given index transferable and reorders
        * the items as appropriate (and if possible) */
        public void drop(DropTargetDropEvent dtde) {
            // reject all but local moves
            if ((DnDConstants.ACTION_MOVE != dtde.getDropAction()) || !dtde.isLocalTransfer()) {
                dtde.rejectDrop();
            }

            int target = comp.locationToIndex(dtde.getLocation());

            if (target < 0) {
                dtde.rejectDrop();

                return;
            }

            Transferable t = dtde.getTransferable();

            //      System.out.println("Dropping..."); // NOI18N
            dtde.acceptDrop(DnDConstants.ACTION_MOVE);

            try {
                int source = Integer.parseInt((String) t.getTransferData(ids.myFlavor));

                if (source != target) {
                    performReorder(source, target);
                    dtde.dropComplete(true);
                } else {
                    dtde.dropComplete(false);
                }
            } catch (IOException exc) {
                dtde.dropComplete(false);
            } catch (UnsupportedFlavorException exc) {
                dtde.dropComplete(false);
            } catch (NumberFormatException exc) {
                dtde.dropComplete(false);
            }
        }

        /** Actually performs the reordering which results from
        * succesfull drag-drop operation.
        * @param source
        */
        void performReorder(int source, int target) {
            int[] myPerm = new int[comp.getModel().getSize()];

            // positions will change only between source and target
            // indexes, the rest remains the same
            for (int i = 0; i < Math.min(source, target); i++)
                myPerm[i] = i;

            for (int i = Math.max(source, target) + 1; i < myPerm.length; i++)
                myPerm[i] = i;

            // reorder the rest
            myPerm[source] = target;

            if (source > target) {
                // dragging was up the list
                for (int i = target; i < source; i++)
                    myPerm[i] = i + 1;
            } else {
                // dragging was down the list
                for (int i = source + 1; i < (target + 1); i++)
                    myPerm[i] = i - 1;
            }

            // and finally perform the reordering
            dialog.performReorder(myPerm, target);
        }

        /** @return True if conditions to continue with DnD
        * operation were satisfied */
        boolean checkConditions(DropTargetDragEvent dtde) {
            int index = comp.locationToIndex(dtde.getLocation());

            return (DnDConstants.ACTION_MOVE == dtde.getDropAction()) && (index >= 0);
        }
    }
     // end of IndexedDropTarget

    /** This class takes responsibility of presenting the
    * asociated index as transferable object.
    */
    private static final class IndexTransferable extends ExTransferable.Single {
        /** Index to transfer */
        int index;

        /** Creates transferable of given index */
        IndexTransferable(DataFlavor flavor, int index) {
            super(flavor);
            this.index = index;
        }

        /* Returns string representation of index */
        protected Object getData() throws IOException, UnsupportedFlavorException {
            return String.valueOf(index);
        }
    }
     // end of IndexTransferable

    /** Implements drag and drop visual feedback
    * support for node list cell rendeder.
    */
    private static final class IndexedListCellRenderer implements javax.swing.ListCellRenderer {
        static final long serialVersionUID = -5526451942677242944L;
        protected static Border hasFocusBorder;

        static {
            hasFocusBorder = new LineBorder(UIManager.getColor("List.focusCellHighlight")); // NOI18N
        }

        /** delegate to use for rendering. Usually NodeRenderer, but if run
         * without explorer, then it uses DefaultListCellRenderer
         */
        private javax.swing.ListCellRenderer delegate = TMUtil.findListCellRenderer();

        /** Index of currently drag under cell in parent list */
        int dragIndex;

        /** Creates new renderer */
        IndexedListCellRenderer() {
            dragIndex = -1;
        }

        /** DnD operation enters, update visual
        * presentation to the drag under state */
        public void draggingEnter(int index, Point startingLoc, Point currentLoc) {
            //      System.out.println("Entering index: " + index); // NOI18N
            this.dragIndex = index;
        }

        /** DnD operation dragging over. */
        public void draggingOver(int index, Point startingLoc, Point currentLoc) {
        }

        /** DnD operation exits, reset visual state
        * back to the normal */
        public void draggingExit() {
            dragIndex = -1;
        }

        public Component getListCellRendererComponent(
            JList list, Object value, int index, boolean isSelected, boolean cellHasFocus
        ) {
            JComponent result = (JComponent) delegate.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus
                );

            if (index == dragIndex) {
                //        System.out.println("Drawing...."); // NOI18N
                result.setBorder(hasFocusBorder);
            }

            return result;
        }
    }
     // end of IndexedListCellRenderer

    /** Implements autoscrolling support for JList.
    * However, JList must be contained in some JViewport.
    */
    private static class AutoscrollJList extends JList implements Autoscroll {
        static final long serialVersionUID = 5495776972406885734L;

        /** Autoscroll insets */
        Insets scrollInsets;

        /** Insets for the autoscroll method to decide
        * whether really perform or not */
        Insets realInsets;

        /** Viewport we are in */
        JViewport viewport;

        AutoscrollJList() {
        }

        /** notify the Component to autoscroll */
        public void autoscroll(Point cursorLoc) {
            JViewport viewport = getViewport();
            Point viewPos = viewport.getViewPosition();
            int viewHeight = viewport.getExtentSize().height;

            if ((cursorLoc.y - viewPos.y) <= realInsets.top) {
                // scroll up
                viewport.setViewPosition(new Point(viewPos.x, Math.max(viewPos.y - realInsets.top, 0)));
            } else if (((viewPos.y + viewHeight) - cursorLoc.y) <= realInsets.bottom) {
                // scroll down
                viewport.setViewPosition(
                    new Point(viewPos.x, Math.min(viewPos.y + realInsets.bottom, this.getHeight() - viewHeight))
                );
            }
        }

        /** @return the Insets describing the autoscrolling
        * region or border relative to the geometry of the
        * implementing Component.
        */
        public Insets getAutoscrollInsets() {
            if (scrollInsets == null) {
                int height = this.getHeight();
                scrollInsets = new Insets(height, 0, height, 0);

                // compute also autoscroll insets for viewport
                //Rectangle rect = getViewport().getViewRect();
                realInsets = new Insets(15, 0, 15, 0);
            }

            return scrollInsets;
        }

        /** Asociates given viewport with this list.
        * (Viewport is usually parent containing this component) */
        JViewport getViewport() {
            if (viewport == null) {
                Component comp = this;

                while (!(comp instanceof JViewport) && (comp != null)) {
                    comp = comp.getParent();
                }

                viewport = (JViewport) comp;
            }

            return viewport;
        }
    }
     // end of AutoscrollJViewport
}
