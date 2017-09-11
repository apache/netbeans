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

import org.openide.awt.HtmlRenderer;
import org.openide.awt.ListPane;
import org.openide.nodes.Node;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/** Default renderer for nodes. Can paint either Nodes directly or
 * can be used to paint objects produced by NodeTreeModel, etc.
 *
 * @see org.openide.nodes.Node
 *
 * @author Jaroslav Tulach, Tim Boudreau
 */
public class NodeRenderer extends Object implements TreeCellRenderer, ListCellRenderer {
    private static NodeRenderer instance = null;

    // ********************
    // Support for dragging
    // ********************

    /** Value of the cell with 'drag under' visual feedback */
    private static VisualizerNode draggedOver;

    /** Flag indicating if to use big icons. */
    private boolean bigIcons = false;
    /** Flag indicating whether to show icons. */
    private boolean showIcons = true;
    private int labelGap;
    private HtmlRenderer.Renderer renderer = HtmlRenderer.createRenderer();

    /** Creates default renderer. */
    public NodeRenderer() {
    }

    /** Creates renderer.
     * @param bigIcons use big icons if possible
     * @deprecated bigIcons was only used by IconView, and not used by that
     * anymore.  NodeRenderer will automatically detect if the view it's
     * rendering for is an instance of IconView.
     */
    public @Deprecated NodeRenderer(boolean bigIcons) {
        this.bigIcons = bigIcons;
    }

    /** Get the singleton instance used by all explorer views.
     *
     * @deprecated This method no longer returns a shared instance, as it
     *  caused problems with one view setting properties (such as enabled
     *  state) on the renderer and the renderer then being used in its altered
     *  state by a different view.   Views should create their own instance of
     *  NodeRenderer instead.
     */
    public static @Deprecated NodeRenderer sharedInstance() {
        if (instance == null) {
            instance = new NodeRenderer();
        }

        IllegalStateException ise = new IllegalStateException(
                "NodeRenderer." + "sharedInstance() is deprecated.  Create an instance of NodeRenderer" + "instead"
            );
        Logger.getLogger(NodeRenderer.class.getName()).log(Level.WARNING, null, ise);

        return instance;
    }
    
    public final void setShowIcons(boolean showIcons) {
        this.showIcons = showIcons;
        if (!showIcons) {
            labelGap = new JLabel().getIconTextGap();
        }
    }
    
    public final boolean isShowIcons() {
        return showIcons;
    }

    /** Finds the component that is capable of drawing the cell in a tree.
     * @param value value can be either <code>Node</code>
     * or a <code>VisualizerNode</code>.
     * @return component to draw the value
     */
    @Override
    public Component getTreeCellRendererComponent(
        JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus
    ) {
        assertEDTAccess();
        VisualizerNode vis = findVisualizerNode(value);

        if (vis == draggedOver) {
            sel = true;
        }

        String text = vis.getHtmlDisplayName();
        boolean isHtml = text != null;

        if (!isHtml) {
            text = vis.getDisplayName();
        }

        //Get our result value - really it is ren, but this call causes
        //it to configure itself with the passed values
        Component result = renderer.getTreeCellRendererComponent(tree, text, sel, expanded, leaf, row, hasFocus);

        result.setEnabled(tree.isEnabled());
        renderer.setHtml(isHtml);

        //Do our additional configuration - set up the icon and possibly
        //do some hacks to make it look focused for TreeTableView
        configureFrom(renderer, tree, expanded, sel, vis);

        return result;
    }

    /** This is the only method defined by <code>ListCellRenderer</code>.  We just
     * reconfigure the <code>Jlabel</code> each time we're called.
     */
    @Override
    public Component getListCellRendererComponent(
        JList list, Object value, int index, boolean sel, boolean cellHasFocus
    ) {
        assertEDTAccess();
        VisualizerNode vis = findVisualizerNode(value);

        if (vis == draggedOver) {
            sel = true;
        }

        String text = vis.getHtmlDisplayName();
        if (list.getModel() instanceof NodeListModel) {
            int depth = NodeListModel.findVisualizerDepth(list.getModel(), vis);
            if (depth == -1) {
                text = NbBundle.getMessage(NodeRenderer.class, "LBL_UP");
            }
        }
        boolean isHtml = text != null;
        if (!isHtml) {
            text = vis.getDisplayName();
        }

        //Get our result value - really it is ren, but this call causes
        //it to configure itself with the passed values
        Component result = renderer.getListCellRendererComponent(
                list, text, index, sel, cellHasFocus || (value == draggedOver)
            );
        renderer.setHtml(isHtml);
        result.setEnabled(list.isEnabled());

        //Do our additional configuration - set up the icon and possibly
        //do some hacks to make it look focused for TreeTableView
        int iconWidth = configureFrom(renderer, list, false, sel, vis);

        boolean bi = this.bigIcons || list instanceof ListPane;

        if (bi) {
            renderer.setCentered(true);
        } else {
            //Indent elements in a ListView/ChoiceView relative to their position
            //in the node tree.  Only does anything if you've subclassed and
            //overridden createModel().  Does anybody do that?
            if (list.getModel() instanceof NodeListModel && (((NodeListModel) list.getModel()).getDepth() > 1)) {
                int indent = iconWidth * NodeListModel.findVisualizerDepth(list.getModel(), vis);

                renderer.setIndent(indent);
            }
        }

        return result;
    }

    /** Utility method which performs configuration which is common to all of the renderer
     * implementations - sets the icon and focus properties on the renderer
     * from the VisualizerNode.
     *
     */
    private int configureFrom(
        HtmlRenderer.Renderer ren, Container target, boolean useOpenedIcon, boolean sel, VisualizerNode vis
    ) {
        if (!isShowIcons()) {
            ren.setIcon(null);
            ren.setIndent(labelGap);
            return 24;
        }
        
        Icon icon = vis.getIcon(useOpenedIcon, bigIcons);

        if (icon.getIconWidth() > 0) {
            //Max annotated icon width is 24, so to have all the text and all
            //the icons come out aligned, set the icon text gap to the difference
            //plus a two pixel margin
            ren.setIconTextGap(24 - icon.getIconWidth());
        } else {
            //If the icon width is 0, fill the space and add in
            //the extra two pixels so the node names are aligned (btw, this
            //does seem to waste a frightful amount of horizontal space in
            //a tree that can use all it can get)
            ren.setIndent(26);
        }

        try {
            ren.setIcon(icon);
        } catch (NullPointerException ex) {
            Exceptions.attachMessage(ex, "icon: " + icon); // NOI18N
            Exceptions.attachMessage(ex, "vis: " + vis); // NOI18N
            Exceptions.attachMessage(ex, "ren: " + ren); // NOI18N
            throw ex;
        }

        if (target instanceof TreeTable.TreeTableCellRenderer) {
            TreeTable.TreeTableCellRenderer ttRen = (TreeTable.TreeTableCellRenderer) target;
            TreeTable tt = ttRen.getTreeTable();
            ren.setParentFocused(ttRen.treeTableHasFocus() || tt.isEditing());
        }

        return (icon.getIconWidth() == 0) ? 24 : icon.getIconWidth();
    }

    /** Utility method to find a visualizer node for the object passed to
     * any of the cell renderer methods as the value */
    private static VisualizerNode findVisualizerNode(Object value) {
        if (value instanceof Node) {
            return VisualizerNode.getVisualizer(null, (Node)value);
        } else if (value instanceof VisualizerNode) {
            return (VisualizerNode)value;
        } else if (value == null || " ".equals(value) || "".equals(value)) {
            return VisualizerNode.EMPTY;
        } else {
            throw new ClassCastException("Unexpected value: " + value);
        }
    }

    /** DnD operation enters. Update look and feel to the 'drag under' state.
     * @param dragged the value of cell which should have 'drag under' visual feedback
         */
    static void dragEnter(Object dragged) {
        draggedOver = (VisualizerNode) dragged;
    }

    /** DnD operation exits. Revert to the normal look and feel. */
    static void dragExit() {
        draggedOver = null;
    }

    private void assertEDTAccess () {
        boolean check = false;
        assert check = true;
        if (check && !EventQueue.isDispatchThread() && System.getProperty("nbjunit.workdir") == null) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            boolean whitespaced = false;
            for (int i = 0; i < stackTrace.length; ++i) {
                StackTraceElement elem = stackTrace[i];
                if ("org.openide.explorer.view.TreeView".equals(elem.getClassName()) && "<init>".equals(elem.getMethodName())) {
                    whitespaced = true;
                    break;
                }
            }
            assert whitespaced || EventQueue.isDispatchThread() : "Should be called in EDT only!";
        }
    }
}
