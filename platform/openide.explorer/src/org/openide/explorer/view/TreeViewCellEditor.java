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

import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.util.EventObject;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;
import org.openide.util.Exceptions;


/** In-place editor in the tree view component.
*
* @author Petr Hamernik
*/
class TreeViewCellEditor extends DefaultTreeCellEditor implements CellEditorListener, FocusListener,
    MouseMotionListener, MouseListener {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -2171725285964032312L;

    // Attributes

    /** Indicates whether is drag and drop currently active or not */
    boolean dndActive = false;

    /** True, if the editation was cancelled by the user.
    */
    private boolean cancelled = false;

    /** Stopped is true, if the editation is over (editingStopped is called for the
        first time). The two variables have virtually the same function, but are kept
        separate for code clarity.
    */
    private boolean stopped = false;

    private boolean wasFocusOwner = true;

    /** Construct a cell editor.
    * @param tree the tree
    */
    public TreeViewCellEditor(JTree tree) {
        //Use a dummy DefaultTreeCellEditor - we'll set up the correct
        //icon when we fetch the editor component (see EOF).  Not sure
        //it's wildly vaulable to subclass DefaultTreeCellEditor here - 
        //we override most everything
        super(tree, new DefaultTreeCellRenderer());

        // deal with selection if already exists
        if (tree.getSelectionCount() == 1) {
            lastPath = tree.getSelectionPath();
        }

        addCellEditorListener(this);
    }

    /** Implements <code>CellEditorListener</code> interface method. */
    public void editingStopped(ChangeEvent e) {
        //CellEditor sometimes(probably after stopCellEditing() call) gains one focus but loses two
        if (stopped) {
            return;
        }

        stopped = true;

        TreePath lastP = lastPath;

        if (lastP != null) {
            Node n = Visualizer.findNode(lastP.getLastPathComponent());

            if ((n != null) && n.canRename()) {
                String newStr = (String) getCellEditorValue();
                ViewUtil.nodeRename(n, newStr);
            }
        }
    }

    /** Implements <code>CellEditorListener</code> interface method. */
    public void editingCanceled(ChangeEvent e) {
        cancelled = true;
    }

    /** Overrides superclass method. If the source is a <code>JTextField</code>,
     * i.e. cell editor, it cancels editing, otherwise it calls superclass method. */
    @Override
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() instanceof JTextField) {
            cancelled = true;
            cancelCellEditing();
        } else {
            super.actionPerformed(evt);
        }
    }

    /** Implements <code>FocusListener</code> interface method. */
    public void focusLost(FocusEvent evt) {
        if (evt.getSource() == tree) {
            // stop timer if tree lost focus (#153009)
            abortTimer();
            return;
        }

        if (stopped || cancelled) {
            return;
        }

        if (!stopCellEditing()) {
            cancelCellEditing();
        }
    }

    /** Dummy implementation of <code>FocusListener</code> interface method. */
    public void focusGained(FocusEvent evt) {
    }

    /**
     * This is invoked if a TreeCellEditor is not supplied in the constructor.
     * It returns a TextField editor.
     */
    @Override
    protected TreeCellEditor createTreeCellEditor() {
        JTextField tf = new JTextField() {
                @Override
                public void addNotify() {
                    stopped = cancelled = false;
                    super.addNotify();
                    requestFocus();
                }
            };
        //#137454 - text not visible in in-place editor
        String laf = UIManager.getLookAndFeel().getID();
        if( "GTK".equals( laf ) ) { //NOI18N
            tf.setBorder( BorderFactory.createEmptyBorder() );
        } else if( "Nimbus".equals( laf ) ) { //NOI18N
            tf.setBorder( BorderFactory.createLineBorder( new JTree().getBackground() ) );
        }
        tf.registerKeyboardAction( //TODO update to use inputMap/actionMap
            this, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true), JComponent.WHEN_FOCUSED);

        tf.addFocusListener(this);

        Ed ed = new Ed(tf);
        ed.setClickCountToStart(1);
        ed.getComponent().getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(TreeViewCellEditor.class, "ACSD_TreeViewCellEditor")
        ); // NOI18N
        ed.getComponent().getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(TreeViewCellEditor.class, "ACSN_TreeViewCellEditor")
        ); // NOI18N

        return ed;
    }

    /*
    * If the realEditor returns true to this message, prepareForEditing
    * is messaged and true is returned.
    */
    @Override
    public boolean isCellEditable(EventObject event) {
        if (event instanceof MouseEvent) {
            if (!SwingUtilities.isLeftMouseButton((MouseEvent) event) || ((MouseEvent) event).isPopupTrigger()) {
                abortTimer();
                return false;
            }
            if (!wasFocusOwner) {
                wasFocusOwner = true;
                return false;
            }
        }

        if (lastPath != null) {
            Node n = Visualizer.findNode(lastPath.getLastPathComponent());

            if ((n == null) || !n.canRename()) {
                return false;
            }
        } else {
            // Disallow rename when multiple nodes are selected
            return false;
        }

        // disallow editing if we are in DnD operation
        if (dndActive) {
            return false;
        }

        return super.isCellEditable(event);
    }

    @Override
    protected void determineOffset(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row) {
        if (renderer != null) {
            renderer.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, true);
            editingIcon = renderer.getIcon();

            if (editingIcon != null) {
                offset = renderer.getIconTextGap() + editingIcon.getIconWidth();
            } else {
                offset = 0;
            }
        } else {
            editingIcon = null;
            offset = 0;
        }
    }

    /*** Sets the state od drag and drop operation.
    * It's here only because of JTree's bug which allows to
    * start the editing even if DnD operation occurs
    * (bug # )
    */
    void setDnDActive(boolean dndActive) {
        if (!dndActive) {
            tree.removeMouseMotionListener(this);
        }

        this.dndActive = dndActive;
    }

    @Override
    protected void setTree(JTree newTree) {
        if ((newTree != tree) && (timer != null) && timer.isRunning()) {
            tree.removeMouseMotionListener(this);
        }

        if (newTree != tree) {
            if (tree != null) {
                tree.removeMouseListener(this);
                tree.removeFocusListener(this);
            }
            if (newTree != null) {
                newTree.addMouseListener(this);
                newTree.addFocusListener(this);
            }
        }

        super.setTree(newTree);
    }

    // bugfix #33765, cancel timer if the mouse leaves a selection rectangle
    public void mouseDragged(MouseEvent e) {
        Point p = e.getPoint();
        boolean b = checkContinueTimer(p);

        if (!b) {
            abortTimer();
        }
    }

    public void mouseMoved(MouseEvent e) {
        Point p = e.getPoint();
        boolean b = checkContinueTimer(p);

        if (!b) {
            abortTimer();
        }
    }

    void abortTimer() {
        if ((timer != null) && timer.isRunning()) {
            timer.stop();
            tree.removeMouseMotionListener(this);
        }
    }

    @Override
    protected void startEditingTimer() {
        tree.addMouseMotionListener(this);
        super.startEditingTimer();
    }

    @Override
    protected void prepareForEditing() {
        abortTimer();
        tree.removeMouseMotionListener(this);

        super.prepareForEditing();
    }

    private boolean checkContinueTimer(Point p) {
        Rectangle r = tree.getPathBounds(tree.getSelectionPath());

        if (r == null) {
            return false;
        }

        return (r.contains(p));
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        wasFocusOwner = tree.isFocusOwner();
    }

    public void mouseReleased(MouseEvent e) {
    }

    /** Redefined default cell editor to convert nodes to name */
    class Ed extends DefaultCellEditor {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -6373058702842751408L;

        public Ed(JTextField tf) {
            super(tf);
        }

        /** Main method of the editor.
        * @return component of editor
        */
        @Override
        public Component getTreeCellEditorComponent(
            JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row
        ) {
            Node ren = Visualizer.findNode(value);

            if ((ren != null) && (ren.canRename())) {
                delegate.setValue(ren.getName());
            } else {
                delegate.setValue(""); // NOI18N
            }

            editingIcon = ((VisualizerNode) value).getIcon(expanded, false);

            ((JTextField) editorComponent).selectAll();

            return editorComponent;
        }
    }
}
