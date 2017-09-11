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
package org.openide.explorer.propertysheet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.beans.FeatureDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.nodes.Node;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.NodeAdapter;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;



/**
 * Implements a property sheet for a set of nodes. Can be used as a 
 * standalone component (e.g. without a connection to {@link org.openide.explorer.ExplorerManager}).
 * For example to display properties of a JavaBean one could use:
 * <pre>
 * Object bean = ...;
 * JPanel container = ...;
 * PropertySheet ps = new PropertySheet();
 * ps.setNodes(new Node[] { new {@link org.openide.nodes.BeanNode}(bean) });
 * container.add(ps);
 * </pre>
 *
 * <strong>Note that this class should be final, but for backward compatibility,
 * cannot be.  Subclassing this class is strongly discouraged</strong>
 *
 * @author   Tim Boudreau, Jan Jancura, Jaroslav Tulach
 */
public class PropertySheet extends JPanel {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -7698351033045864945L;

    // public constants ........................................................

    /** Deprecated - no code outside the property sheet should be interested
     *  in how items are sorted.
     *@deprecated Relic of the original property sheet implementation, will never be fired. */
    public @Deprecated static final String PROPERTY_SORTING_MODE = "sortingMode"; // NOI18N

    /** Property giving current value color.
     *@deprecated Relic of the original property sheet implementation, will never be fired. */
    public @Deprecated static final String PROPERTY_VALUE_COLOR = "valueColor"; // NOI18N

    /** Property giving current disabled property color.
     *@deprecated Relic of the original property sheet implementation, , will never be fired. */
    public @Deprecated static final String PROPERTY_DISABLED_PROPERTY_COLOR = "disabledPropertyColor"; // NOI18N

    /** Property with the current page index.
     *@deprecated Relic of the original property sheet implementation, , will never be fired.*/
    public @Deprecated static final String PROPERTY_CURRENT_PAGE = "currentPage"; // NOI18N

    /** Property for plastic mode.
     *@deprecated Relic of the original property sheet implementation, , will never be fired.   */
    public @Deprecated static final String PROPERTY_PLASTIC = "plastic"; // NOI18N

    /** Property for the painting style.
     *@deprecated Relic of the original property sheet implementation, will never be fired. */
    public @Deprecated static final String PROPERTY_PROPERTY_PAINTING_STYLE = "propertyPaintingStyle"; // NOI18N

    /** Property for whether only writable properties should be displayed.
     *@deprecated Relic of the original property sheet implementation, will never be fired.*/
    public @Deprecated static final String PROPERTY_DISPLAY_WRITABLE_ONLY = "displayWritableOnly"; // NOI18N

    /** Constant for showing properties as a string always.
     *@deprecated Relic of the original property sheet implementation, useless.  */
    public @Deprecated static final int ALWAYS_AS_STRING = 1;

    /** Constant for preferably showing properties as string.
     *@deprecated Relic of the original property sheet implementation, does useless.    */
    public @Deprecated static final int STRING_PREFERRED = 2;

    /** Constant for preferably painting property values.
     *@deprecated Relic of the original property sheet implementation, does useless.   */
    public @Deprecated static final int PAINTING_PREFERRED = 3;

    /** Constant for unsorted sorting mode. */
    public static final int UNSORTED = 0;

    /** Constant for by-name sorting mode. */
    public static final int SORTED_BY_NAMES = 1;

    /** Constant for by-type sorting mode.
     * @deprecated Not supported since NetBeans 3.6
     **/
    public @Deprecated static final int SORTED_BY_TYPES = 2;

    /** Icon for the toolbar.
     * @deprecated Presumably noone uses this variable. If you want to customize
     *  the property sheet look you can change the image files directly (or use your
     *  own).
     */
    static @Deprecated protected Icon iNoSort;

    /** Icon for the toolbar.
     * @deprecated Presumably noone uses this variable. If you want to customize
     *  the property sheet look you can change the image files directly (or use your
     *  own).
     */
    static @Deprecated protected Icon iAlphaSort;

    /** Icon for the toolbar.
     * @deprecated Presumably noone uses this variable. If you want to customize
     *  the property sheet look you can change the image files directly (or use your
     *  own).
     */
    static @Deprecated protected Icon iTypeSort;

    /** Icon for the toolbar.
     * @deprecated Presumably noone uses this variable. If you want to customize
     *  the property sheet look you can change the image files directly (or use your
     *  own).
     */
    static @Deprecated protected Icon iDisplayWritableOnly;

    /** Icon for the toolbar.
     * @deprecated Presumably noone uses this variable. If you want to customize
     *  the property sheet look you can change the image files directly (or use your
     *  own).
     */
    static @Deprecated protected Icon iCustomize;

    /** Action command/input map key for popup menu invocation action */
    private static final String ACTION_INVOKE_POPUP = "invokePopup"; //NOI18N

    /** Action command/input map key for help invocation action */
    private static final String ACTION_INVOKE_HELP = "invokeHelp"; //NOI18N

    /** Init delay for second change of the selected nodes. */
    private static final int INIT_DELAY = 70;

    /** Maximum delay for repeated change of the selected nodes. */
    private static final int MAX_DELAY = 150;

    /**Debugging option to suppress all use of tabs */
    private static final boolean neverTabs = Boolean.getBoolean("netbeans.ps.nevertabs"); //NOI18N
    static final boolean forceTabs = Boolean.getBoolean("nb.ps.forcetabs");

    /** property sheet processor */
    private static final RequestProcessor RP = new RequestProcessor("Property Sheet"); // NOI18N

    /** Holds the sort mode for the property sheet */
    private int sortingMode = UNSORTED;

    /**Tracks whether the description area should be shown */
    private boolean showDesc;

    /** Temporary storage for the last selected node in the case the property
     * sheet was removed temporarily from a container (winsys DnD) */
    private Reference<Node> storedNode;

    //Package private for unit tests
    SheetTable table = new SheetTable();
    PSheet psheet = new PSheet();
    HelpAction helpAction = new HelpAction();

    // delayed setting nodes (partly impl issue 27781)
    //package private for unit testing
    transient Node[] helperNodes;
    private transient RequestProcessor.Task scheduleTask;
    private transient RequestProcessor.Task initTask;
    SheetPCListener pclistener = new SheetPCListener();

    /** Create a new property sheet */
    public PropertySheet() {
        init();
        initActions();
    }

    /** Install actions the property sheet will need */
    private void initActions() {
        Action invokePopupAction = new MutableAction(MutableAction.INVOKE_POPUP, this);

        table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_MASK), ACTION_INVOKE_POPUP);
        table.getActionMap().put(ACTION_INVOKE_POPUP, invokePopupAction);

        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_MASK), ACTION_INVOKE_POPUP
        );
        getActionMap().put(ACTION_INVOKE_POPUP, invokePopupAction);

        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), ACTION_INVOKE_HELP
        );
        getActionMap().put(ACTION_INVOKE_HELP, helpAction);
    }

    @Override
    public void addNotify() {
        super.addNotify();

        Node oldSelection = null;

        if (storedNode != null) {
            oldSelection = storedNode.get();
        }

        if (oldSelection != null) {
            setCurrentNode(oldSelection);
        }
    }

    @Override
    public void updateUI() {
        UIManager.get("nb.propertysheet"); //Causes default colors for the property sheet to be bootstrapped into
                                           //UIDefaults - see core/swing/plaf

        super.updateUI();
    }

    @Override
    public void removeNotify() {
        Node lastSel = null;

        if (pclistener != null) {
            //Save the last selection - if we're being transiently removed,
            //i.e. because of drag and drop, we'll want to reset it on the
            //next addNotify if it hasn't disappeared
            lastSel = pclistener.detach();
        }

        doSetNodes(null);

        if (lastSel != null) {
            //Save the selected node in case we're re-added to a container
            storedNode = new WeakReference<Node>(lastSel);
        }

        super.removeNotify();
        table.getReusablePropertyEnv().setBeans(null);
        table.getReusablePropertyEnv().setNode(null);
        table.getReusablePropertyModel().setProperty(null);
    }

    /**
     * @return true if the quick search feature is currently allowed, false otherwise.
     * To be allowed, quick search must not have been disabled either globally or on
     * this instance
     * @since 6.37
     */
    public boolean isQuickSearchAllowed() {
        return table.isQuickSearchAllowed();
    }

    /**
     * Allows one to set whether or not the quick search feature should be
     * enabled on this instance.
     *
     * @param isQuickSearchAllowed true if the quick search feature should be
     * allowed on this instance, false otherwise.
     * @since 6.37
     */
    public void setQuickSearchAllowed(boolean isQuickSearchAllowed) {
        table.setQuickSearchAllowed(isQuickSearchAllowed);
    }

    /** Prepare the initial state of the property sheet */
    private void init() {
        Font f = UIManager.getFont("controlFont"); //NOI18N

        if (f == null) {
            //Aqua
            f = UIManager.getFont("Tree.font"); //NOI18N
        }

        if (f != null) {
            table.setFont(f);
        }

        showDesc = PropUtils.shouldShowDescription();
        setLayout(new BorderLayout());
        psheet.setBackground(table.getBackground());
        setBackground(table.getBackground());
        psheet.setMarginColor(PropUtils.getSetRendererColor());

        psheet.add(table);
        add(psheet, BorderLayout.CENTER);

        table.setBorder(BorderFactory.createEmptyBorder());

        setDescriptionVisible(showDesc);
        setMinimumSize(new Dimension(100, 50));
        psheet.setEmptyString(NbBundle.getMessage(PropertySheet.class, "CTL_NoProperties")); //NOI18N

        TabSelectionListener listener = new TabSelectionListener();

        psheet.addSelectionChangeListener(listener);

        table.addChangeListener(listener);

        try {
            setSortingMode(PropUtils.getSavedSortOrder());
        } catch (PropertyVetoException e) {
            //Should never happen unless someone manually modifies
            //backing storage
            Exceptions.printStackTrace(e);
        }
    }

    
    private boolean popupEnabled = true;
    /**
     * Set whether or not the popup menu should be available on
     * right-click.
     * @param val If true, right-clicking the property sheet will show a popup
     *  offering sorting options, show/hide description area, etc.
     * @since 6.9
     */ 
    public final void setPopupEnabled(boolean val) {
        this.popupEnabled = val;
    }
    
    /**
     * Set the visibility of the description area.
     * 
     * @param val Whether or not it should be visible
     * @since 6.9
     */ 
    public final void setDescriptionAreaVisible (boolean val) {
        if (isDescriptionVisible() != val) {
            int state = psheet.getState();

            if (!val) {
                int newState = ((state & PSheet.STATE_HAS_TABS) != 0) ? PSheet.STATE_HAS_TABS : 0;

                psheet.setState(newState);
            } else {
                int newState = ((state & PSheet.STATE_HAS_TABS) != 0)
                    ? (PSheet.STATE_HAS_TABS | PSheet.STATE_HAS_DESCRIPTION) : PSheet.STATE_HAS_DESCRIPTION;

                psheet.setState(newState);
            }
        }
    }
 
    /** Enable/disable display of the description area */
    void setDescriptionVisible(boolean val) {
        setDescriptionAreaVisible (val);
        PropUtils.saveShowDescription(val);
    }

    boolean isDescriptionVisible() {
        return (psheet.getState() & PSheet.STATE_HAS_DESCRIPTION) != 0;
    }

    /** Overridden to route focus requests to the table */
    @Override
    public void requestFocus() {
        if (table.getParent() != null) {
            table.requestFocus();
        } else {
            super.requestFocus();
        }
    }

    /** Overridden to route focus requests to the table */
    @Override
    public boolean requestFocusInWindow() {
        if (table.getParent() != null) {
            return table.requestFocusInWindow();
        } else {
            return super.requestFocusInWindow();
        }
    }

    /**
     * Set the nodes explored by this property sheet.
     *
     * @param nodes nodes to be explored
     */
    private void doSetNodes(Node[] nodes) {
        if ((nodes == null) || (nodes.length == 0)) {
            table.getPropertySetModel().setPropertySets(null);
            table.getReusablePropertyEnv().clear();
            psheet.setTabbedContainerItems(new Object[0], new String[0]);
            return;
        }

        final Node n = (nodes.length == 1) ? nodes[0] : new ProxyNode(nodes);
        setCurrentNode(n);
    }

    /**Set the nodes explored by this property sheet.
     * @param nodes nodes to be explored or null to clear the sheet
     */
    public synchronized void setNodes(Node[] nodes) {
        final boolean loggable = PropUtils.isLoggable(PropertySheet.class);

        if (loggable) {
            PropUtils.log(PropertySheet.class, "SetNodes " + (null == nodes ? "<null>" : Arrays.asList(nodes)));
        }

        //Performance - check equality and avoid some extra repaints - repainting
        //the property sheet can be expensive
        if ((nodes != null) && (nodes.length > 0) && (pclistener != null)) {
            if ((nodes.length == 1) && (nodes[0] == pclistener.getNode())) {
                if (loggable) {
                    PropUtils.log(PropertySheet.class, "  Same node selected as before; no redisplay needed");
                }

                return;
            } else if (pclistener.getNode() instanceof ProxyNode) {
                if (loggable) {
                    PropUtils.log(PropertySheet.class, "  Selected node is a proxy node - comparing contents.");
                }

                Node[] currNodes = ((ProxyNode) pclistener.getNode()).getOriginalNodes();

                if (Arrays.asList(nodes).equals(Arrays.asList(currNodes))) {
                    if (loggable) {
                        PropUtils.log(
                            PropertySheet.class,
                            "  Proxy node represents the same " + "nodes already showing.  Showing: " +
                            Arrays.asList(currNodes) + " requested " + Arrays.asList(nodes)
                        );

                        HashSet<Node> currs = new HashSet<Node>(Arrays.asList(currNodes));
                        HashSet<Node> reqs = new HashSet<Node>(Arrays.asList(nodes));

                        if (currs.size() != currNodes.length) {
                            PropUtils.log(
                                PropertySheet.class,
                                " A hashSet of the current nodes does NOT have the same number " +
                                " of elements as the array of current nodes!  Check " +
                                "your hashCode()/equals() contract.  One or more nodes in " +
                                "the array are claiming to be the same node."
                            );
                        }

                        if (reqs.size() != nodes.length) {
                            PropUtils.log(
                                PropertySheet.class,
                                " A hashSet of the requested selected nodes does NOT have the same number " +
                                " of elements as the array of current nodes!  Check your hashCode()/equals() contract" +
                                " One or more nodes in the array are claiming to be the same node."
                            );
                        }
                    }

                    return;
                }
            }
        } else if ((nodes == null) || (nodes.length == 0)) {
            if (pclistener != null) {
                pclistener.detach();
            }

            // try to cancel previous pending node setting which is now
            // obsoleted by following clear
            RequestProcessor.Task curTask = getScheduleTask();
            if (!curTask.equals(initTask)) {
                curTask.cancel();
            }
            
            if (EventQueue.isDispatchThread()) {
                if (loggable) {
                    PropUtils.log(PropertySheet.class, "  Nodes cleared on event queue.  Emptying model.");
                }

                table.getPropertySetModel().setPropertySets(null);
                table.getReusablePropertyEnv().clear();
                helperNodes = null;
                psheet.setTabbedContainerItems(new Object[0], new String[0]);
            } else {
                EventQueue.invokeLater(
                    new Runnable() {
                        public void run() {
                            if (loggable) {
                                PropUtils.log(
                                    PropertySheet.class,
                                    "  Nodes " + "cleared off event queue.  Empty model later on EQ."
                                );
                            }

                            table.getPropertySetModel().setPropertySets(null);
                            table.getReusablePropertyEnv().clear();
                            helperNodes = null;
                            psheet.setTabbedContainerItems(new Object[0], new String[0]);
                        }
                    }
                );
            }

            return;
        }

        RequestProcessor.Task task = getScheduleTask();
        helperNodes = nodes;

        //Clear any saved node if setNodes is called while we're offscreen
        storedNode = null;

        if (task.equals(initTask)) {
            //if task is only init task then set nodes immediatelly
            scheduleTask.schedule(0);
            task.schedule(INIT_DELAY);
        } else {
            // in a task run then increase delay and reschedule task
            int delay = task.getDelay() * 2;

            if (delay > MAX_DELAY) {
                delay = MAX_DELAY;
            }

            if (delay < INIT_DELAY) {
                delay = INIT_DELAY;
            }

            if (loggable) {
                PropUtils.log(PropertySheet.class, " Scheduling delayed update of selected nodes.");
            }

            task.schedule(delay);
        }
    }

    private synchronized RequestProcessor.Task getScheduleTask() {
        if (scheduleTask == null) {
            scheduleTask = RP.post(new Runnable() {
                @Override
                public void run() {
                    Node[] tmp = helperNodes;
                    if (tmp != null) {
                        for (Node n : tmp) {
                            // pre-initialized outside of AWT thread
                            n.getPropertySets();
                        }
                    }
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            final boolean loggable = PropUtils.isLoggable(PropertySheet.class);
                            Node[] nodesToSet = helperNodes;
                            if (loggable) {
                                PropUtils.log(
                                    PropertySheet.class,
                                    "Delayed " + "updater setting nodes to " + //NOI18N
                                        (null == nodesToSet ? "null" : Arrays.asList(nodesToSet)) //NOI18N
                                );
                            }

                            doSetNodes(nodesToSet);
                        }
                    });
                }
            });
            initTask = RP.post(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        // if none task runs then return initTask to wait for next changes
        if (initTask.isFinished() && scheduleTask.isFinished()) {
            return initTask;
        }

        // if some task runs then return schedule task which will set nodes
        return scheduleTask;
    }

    // end of delayed    

    /** This has to be called from the AWT thread. */
    void setCurrentNode(Node node) {
        Node old = pclistener.getNode();

        if (old != node) {
            psheet.storeScrollAndTabInfo();
        }

        final boolean loggable = PropUtils.isLoggable(PropertySheet.class);

        if (loggable) {
            PropUtils.log(PropertySheet.class, "SetCurrentNode:" + node);
        }

        //        table.setNode (node);
        PropertySetModel psm = table.getPropertySetModel();
        Node.PropertySet[] ps = node.getPropertySets();

        //bloc below copied from original impl - is this common/needed?
        if (ps == null) {
            // illegal node behavior => log warning about it
            Logger.getAnonymousLogger().warning("Node " + node + ": getPropertySets() returns null!"); // NOI18N
            ps = new Node.PropertySet[] {  };

            //Prepare the reusable model/env's node
        }

        table.getReusablePropertyEnv().setNode(node);

        assert noNullPropertyLists(ps) : "Node " + node + " returns null from getProperties() for one or " +
        "more of its property sets"; //NOI18N

        if (table.isEditing()) {
            table.removeEditor();
        }

        boolean usingTabs = needTabs(node);

        if (usingTabs) {
            psheet.setState(psheet.getState() | PSheet.STATE_HAS_TABS);

            TabInfo info = getTabItems(node);

            psheet.setTabbedContainerItems(info.sets, info.titles);
            psheet.manager().setCurrentNodeName(node.getName());
            if( info.sets.length > 0 )
                psm.setPropertySets(info.getSets(0));
        } else {
            psm.setPropertySets(ps);
            psheet.setState(
                ((psheet.getState() & PSheet.STATE_HAS_DESCRIPTION) != 0) ? PSheet.STATE_HAS_DESCRIPTION : 0
            );
            psheet.setTabbedContainerItems(new Object[0], new String[0]);
        }

        psheet.adjustForName(node.getName());

        table.setBeanName(node.getDisplayName());

        String description = (String) node.getValue("nodeDescription"); //NOI18N

        psheet.setDescription(node.getDisplayName(), (description == null) ? node.getShortDescription() : description);

        pclistener.attach(node);

        if (isDescriptionVisible()) {
            helpAction.checkContext();
        }
    }

    private boolean noNullPropertyLists(PropertySet[] ps) {
        boolean result = true;

        for (int i = 0; i < ps.length; i++) {
            result &= (ps[i] != null && ps[i].getProperties() != null);

            if (!result) {
                break;
            }
        }

        return result;
    }

    /**Deprecated, does nothing.
     * @param style Irrelevant
     * @deprecated Relic of the original property sheet implementation.  Does nothing.*/
    public @Deprecated void setPropertyPaintingStyle(int style) {
    }

    /**Deprecated, returns no meaningful value.
     * @return the mode
     * @see #setPropertyPaintingStyle
     * @deprecated Relic of the original property sheet implementation.  Does nothing. */
    public @Deprecated int getPropertyPaintingStyle() {
        return 0;
    }

    /**
     * Set the sorting mode.
     * @param sortingMode one of {@link #UNSORTED} or {@link #SORTED_BY_NAMES}. {@link #SORTED_BY_TYPES} is
     *        no longer supported.
     * @throws PropertyVetoException if a value other than one of the defined sorting modes is set
     */
    public void setSortingMode(int sortingMode) throws PropertyVetoException {
        try {
            table.getPropertySetModel().setComparator(PropUtils.getComparator(sortingMode));
            this.sortingMode = sortingMode;
            psheet.setMarginPainted(!PropUtils.neverMargin && (getSortingMode() == UNSORTED));
            PropUtils.putSortOrder(sortingMode);
        } catch (IllegalArgumentException iae) {
            throw new PropertyVetoException(
                NbBundle.getMessage(PropertySheet.class, "EXC_Unknown_sorting_mode"),
                new PropertyChangeEvent(this, PROPERTY_SORTING_MODE, new Integer(0), new Integer(sortingMode))
            ); //NOI18N
        }
    }

    /**Get the sorting mode.
     * @return the mode
     * @see #setSortingMode   */
    public int getSortingMode() {
        return sortingMode;
    }

    /** Deprecated.  Does nothing.
     * @param index index of the page to select
     * @deprecated Relic of the original property sheet implementation.  Does nothing.
     */
    public @Deprecated void setCurrentPage(int index) {
    }

    /**
     * Deprecated.  Does nothing.
     * @deprecated Relic of the original property sheet implementation.  Does nothing.
     * @param str name of the tab to select
     * @return always returns false
     */
    public @Deprecated boolean setCurrentPage(String str) {
        return false;
    }

    /**Deprecated.  Does nothing.
     * @return index of currently selected page
     * @deprecated Relic of the original property sheet implementation.  Does nothing. */
    public @Deprecated int getCurrentPage() {
        //        return pages.getSelectedIndex ();
        return 0;
    }

    /**Deprecated.  Does nothing.
     * @param plastic true if so
     * @deprecated Relic of the original property sheet implementation.  Display of properties
     * is handled by the look and feel.
     */
    public @Deprecated void setPlastic(boolean plastic) {
    }

    /**Test whether buttons in sheet are plastic.
     * @return <code>true</code> if so
     * @deprecated Relic of the original property sheet implementation.  Does nothing.*/
    public @Deprecated boolean getPlastic() {
        return false;
    }

    /**Deprecated.  Does nothing.
     * @param color the new color
     * @deprecated Relic of the original property sheet implementation.  Display of properties
     * is handled by the look and feel.  */
    public @Deprecated void setValueColor(Color color) {
    }

    /**Deprecated.  Does nothing.
     * @deprecated Relic of the original property sheet implementation.  Display of properties
     * is handled by the look and feel.
     * @return the color */
    public @Deprecated Color getValueColor() {
        return Color.BLACK;
    }

    /**Deprecated.  Does nothing.
     * @deprecated Relic of the original property sheet implementation.  Does nothing.
     * @param color the new color  */
    public @Deprecated void setDisabledPropertyColor(Color color) {
    }

    /**Deprecated.  Does not return a meaningful value.
     * @deprecated Relic of the original property sheet implementation.  Display of properties
     * is handled by the look and feel.
     * @return the color */
    public @Deprecated Color getDisabledPropertyColor() {
        return Color.GRAY;
    }

    /**Deprecated.  Does nothing.
     * @param b <code>true</code> if this is desired
     * @deprecated Relic of the original property sheet implementation.  Does nothing.*/
    public @Deprecated void setDisplayWritableOnly(boolean b) {
    }

    /**Deprecated.  Does not return a meaningful value.
     * @deprecated Relic of the original property sheet implementation.  Does nothing.
     * @return <code>true</code> if so */
    public @Deprecated boolean getDisplayWritableOnly() {
        return false;
    }

    final void showPopup(Point p) {
        if( !popupEnabled )
            return;

        JPopupMenu popup = createPopupMenu();

        if( null == popup ) {
            JMenuItem helpItem = new JMenuItem();
            JRadioButtonMenuItem sortNamesItem = new JRadioButtonMenuItem();
            JRadioButtonMenuItem unsortedItem = new JRadioButtonMenuItem();
            JCheckBoxMenuItem descriptionItem = new JCheckBoxMenuItem();
            JMenuItem defaultValueItem = new JMenuItem();
            popup = new JPopupMenu();

            unsortedItem.setSelected(getSortingMode() == UNSORTED);
            sortNamesItem.setSelected(getSortingMode() == SORTED_BY_NAMES);
            helpAction.checkContext();
            helpItem.setAction(helpAction);
            sortNamesItem.setAction(new MutableAction(MutableAction.SORT_NAMES, this));
            unsortedItem.setAction(new MutableAction(MutableAction.UNSORT, this));
            descriptionItem.setAction(new MutableAction(MutableAction.SHOW_DESCRIPTION, this));
            descriptionItem.setSelected(isDescriptionVisible());
            defaultValueItem.setAction(new MutableAction(MutableAction.RESTORE_DEFAULT, this));

            FeatureDescriptor fd = table.getSelection();
            defaultValueItem.setEnabled(PropUtils.shallBeRDVEnabled(fd));

            popup.add(unsortedItem);
            popup.add(sortNamesItem);
            popup.add(new JSeparator());
            popup.add(descriptionItem);
            popup.add(new JSeparator());
            popup.add(defaultValueItem);
            popup.add(new JSeparator());
            popup.add(helpItem);
        }
        popup.show(psheet, p.x, p.y);
    }

    /**
     * Subclasses may override this method to create a custom popup menu that will
     * show on right-click in the property sheet.
     * @return Custom popup menu or null to use the default popup menu provided
     * by this class.
     * @since 6.47
     */
    protected JPopupMenu createPopupMenu() {
        return null;
    }

    /**
     * Check if the PropertySet the given property belongs to is expanded or not.
     * @param fd Property or PropertySet to check.
     * @return True if the PropertySet the given property belongs is expanded.
     * @since 6.47
     */
    protected final boolean isExpanded( FeatureDescriptor fd ) {
        return table.getPropertySetModel().isExpanded( fd );
    }

    /**
     * Expand or collapse the PropertySet the given property belongs to.
     * @param fd 
     * @since 6.47
     */
    protected final void toggleExpanded( FeatureDescriptor fd ) {
        int index = table.getPropertySetModel().indexOf( fd );
        if( index >= 0 ) {
            table.getPropertySetModel().toggleExpanded( index );
        }
    }

    /**
     * Retrieve currently selected property or PropertySet.
     * @return Selected property or PropertySet or null if there is no selection.
     * @since 6.47
     */
    protected final FeatureDescriptor getSelection() {
        return table.getSelection();
    }

    /**
     * Select the given property.
     * @param fd Property to select
     * @param startEditing True to start editing of that property.
     */
    private void select( FeatureDescriptor fd, boolean startEditing ) {
        table.select( fd, startEditing );
    }

    Node[] getCurrentNodes() {
        Node n = pclistener.getNode();

        if (n != null) {
            if (n instanceof ProxyNode) {
                return ((ProxyNode) n).getOriginalNodes();
            } else {
                return new Node[] { n };
            }
        }

        return new Node[0];
    }

    private static final boolean needTabs(Node n) {
        boolean needTabs = true;

        if (forceTabs) {
            return true;
        }

        if (n instanceof ProxyNode) {
            Node[] nodes = ((ProxyNode) n).getOriginalNodes();

            for (int i = 0; i < nodes.length; i++) {
                assert nodes[i] != n : "Proxy node recursively references itself"; //NOI18N
                needTabs &= needTabs(nodes[i]);

                if (!needTabs) {
                    break;
                }
            }
        } else {
            PropertySet[] ps = n.getPropertySets();
            needTabs = forceTabs ? (ps.length > 1) : (neverTabs ? false : false);

            //neverTabs is a debugging option to force tab use one tab per property set
            if (!neverTabs) {
                for (int i = 0; (i < ps.length) && !needTabs; i++) {
                    if (ps[i] == null) {
                        throw new NullPointerException("Node " + n + " contains null in its getPropertySets() array"); // NOI18N
                    }
                    needTabs |= (ps[i].getValue("tabName") != null); //NOI18N
                }
            }
        }

        return needTabs;
    }

    private static final TabInfo getTabItems(Node n) {
        Map<String, List<PropertySet>> titlesToContents = new HashMap<String, List<PropertySet>>();
        ArrayList<String> order = new ArrayList<String>();

        PropertySet[] sets = n.getPropertySets();

        for (int i = 0; i < sets.length; i++) {
            String currTab = (String) sets[i].getValue("tabName"); //NOI18N

            if (currTab == null) {
                currTab = PropUtils.basicPropsTabName();
            }

            List<PropertySet> l = titlesToContents.get(currTab);

            if (l == null) {
                l = new ArrayList<PropertySet>();
                l.add(sets[i]);
                titlesToContents.put(currTab, l);
            } else {
                l.add(sets[i]);
            }

            if (!order.contains(currTab)) {
                order.add(currTab);
            }
        }

        String[] titles = new String[order.size()];
        Object[] setSets = new Object[order.size()];
        int count = 0;

        for (Iterator<String> i = order.iterator(); i.hasNext();) {
            titles[count] = i.next();

            List<PropertySet> currSets = titlesToContents.get(titles[count]);
            setSets[count] = new PropertySet[currSets.size()];
            setSets[count] = currSets.toArray((PropertySet[]) setSets[count]);
            count++;
        }

        return new TabInfo(titles, setSets);
    }

    @Override
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        super.firePropertyChange(propertyName, oldValue, newValue);
        // on macos we get this hint about focus lost..
        if ("MACOSX".equals(propertyName)) {
            this.table.focusLostCancel();
        }
    }

    private class TabSelectionListener implements ChangeListener, FocusListener {
        public void stateChanged(ChangeEvent e) {
            helpAction.checkContext();

            if (e.getSource() instanceof SheetTable) {
                SheetTable tbl = (SheetTable) e.getSource();
                FeatureDescriptor fd = tbl.getSelection();
                Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();

                if ((focusOwner != tbl) && !tbl.isKnownComponent(focusOwner) && !isAncestorOf(focusOwner)) {
                    fd = null;
                }

                if (fd != null) {
                    String ttl = fd.getDisplayName();
                    String desc = fd.getShortDescription();
                    psheet.setDescription(ttl, desc);
                } else {
                    Node n = pclistener.getNode();

                    if (n != null) {
                        String ttl = n.getDisplayName();
                        String desc = (String) n.getValue("nodeDescription"); //NOI18N

                        if (desc == null) {
                            desc = n.getShortDescription();
                        }

                        psheet.setDescription(ttl, desc);
                    } else {
                        psheet.setDescription(null, null);
                    }
                }
            } else {
                if (!psheet.isAdjusting()) {
                    psheet.storeScrollAndTabInfo();
                }

                PropertySet[] sets = (PropertySet[]) psheet.getTabbedContainerSelection();

                if (sets != null) {
                    table.getPropertySetModel().setPropertySets(sets);

                    if ((sets.length > 0) && !psheet.isAdjusting()) {
                        String tab = (String) sets[0].getValue("tabName"); //NOI18N
                        tab = (tab == null) ? PropUtils.basicPropsTabName() : tab;
                        psheet.manager().storeLastSelectedGroup(tab);
                        psheet.adjustForName(tab);
                    }
                }
            }
        }

        public void focusGained(FocusEvent e) {
            ChangeEvent ce = new ChangeEvent(table);
            stateChanged(ce);
        }

        public void focusLost(FocusEvent e) {
            focusGained(e);
        }
    }

    final class HelpAction extends AbstractAction {
        HelpCtx.Provider provider = null;

        //XXX MERGE THIS CLASS WITH PROXYHELPPROVIDER 
        private boolean wasEnabled = false;

        public HelpAction() {
            super(NbBundle.getMessage(PropertySheet.class, "CTL_Help")); //NOI18N
            checkContext();
        }

        public void checkContext() {
            HelpCtx ctx = getContext();
            boolean isEnabled = ctx != null;

            if (isEnabled != wasEnabled) {
                firePropertyChange(
                    "enabled", isEnabled ? Boolean.FALSE : Boolean.TRUE, isEnabled ? Boolean.TRUE : Boolean.FALSE
                ); //NOI18N
            }

            wasEnabled = isEnabled;
            psheet.setHelpEnabled(isEnabled);
        }

        @Override
        public boolean isEnabled() {
            return getContext() != null;
        }

        public void actionPerformed(ActionEvent e) {
            HelpCtx ctx = getContext();

            if (ctx == null || !ctx.display()) {
                Toolkit.getDefaultToolkit().beep();
            }
        }

        public HelpCtx getContext() {
            FeatureDescriptor fd = table.getSelection();
            String id = null;

            //First look on the individual property
            if ((fd != null) && fd instanceof Node.Property) {
                id = (String) fd.getValue("helpID"); //NOI18N
            }

            if (id == null) {
                if ((psheet.getState() & PSheet.STATE_HAS_TABS) != 0) {
                    //If we're in a tabbed pane, we want the first visible
                    //property set's help id
                    Node.PropertySet[] ps = (Node.PropertySet[]) psheet.getTabbedContainerSelection();

                    if ((ps != null) && (ps.length > 0)) {
                        id = (String) ps[0].getValue("helpID"); //NOI18N
                    }
                } else if ((id == null) && (pclistener != null)) {
                    //Otherwise, look for the first property set on the node
                    Node n = pclistener.getNode();

                    if (n == null) {
                        return null;
                    }

                    Node.PropertySet[] ps = n.getPropertySets();

                    if ((fd != null) && (ps != null) && (ps.length > 0)) {
                        for (int i = 0; i < ps.length; i++) {
                            if ((ps[i] == fd) || Arrays.asList(ps[i].getProperties()).contains(fd)) {
                                id = (String) ps[i].getValue("helpID"); //NOI18N

                                break;
                            }
                        }
                    }
                }

                //Then look on the first property set
                if ((id == null) && (pclistener != null)) {
                    Node[] nodes = getCurrentNodes();

                    if ((nodes != null) && (nodes.length > 0)) {
                        for (int i = 0; i < nodes.length; i++) {
                            // Then try to find a property-sheet specific id on 
                            // the Node
                            id = (String) nodes[i].getValue("propertiesHelpID"); //NOI18N

                            if (id != null) {
                                break;
                            }

                            // Then try to find if node doesn't return help 
                            // context directly
                            HelpCtx ctx = nodes[i].getHelpCtx();

                            if ((ctx != null) && (ctx != HelpCtx.DEFAULT_HELP)) {
                                return ctx;
                            }
                        }
                    }
                }
            }

            if ((id != null) && !HelpCtx.DEFAULT_HELP.getHelpID().equals(id)) {
                return new HelpCtx(id);
            } else {
                return null;
            }
        }
    }

    /**
     * Convenience action class to eliminate a few action subclasses.
     */
    private static class MutableAction extends AbstractAction {
        private static final int SORT_NAMES = 0;
        private static final int UNSORT = 1;
        private static final int INVOKE_POPUP = 2;
        private static final int SHOW_DESCRIPTION = 3;
        private static final int SHOW_HELP = 4;
        private static final int RESTORE_DEFAULT = 5;
        private final int id;
        private final PropertySheet sheet;

        public MutableAction(int id, PropertySheet sheet) {
            this.id = id;
            this.sheet = sheet;

            String nameKey = null;

            switch (id) {
            case SORT_NAMES:
                nameKey = "CTL_AlphaSort"; //NOI18N

                break;

            case UNSORT:
                nameKey = "CTL_NoSort"; //NOI18N

                break;

            case INVOKE_POPUP:
                break;

            case SHOW_DESCRIPTION:
                nameKey = "CTL_ShowDescription"; //NOI18N

                break;

            case SHOW_HELP:
                break;

            case RESTORE_DEFAULT:
                nameKey = "CTL_RestoreDefaultValue"; //NOI18N

                break;

            default:
                throw new IllegalArgumentException(Integer.toString(id));
            }

            if (nameKey != null) {
                putValue(Action.NAME, NbBundle.getMessage(PropertySheet.class, nameKey));
            }
        }

        public void actionPerformed(ActionEvent ae) {
            switch (id) {
            case SORT_NAMES:

                try {
                    sheet.setSortingMode(SORTED_BY_NAMES);
                } catch (PropertyVetoException pve) {
                    //can't happen
                }

                break;

            case UNSORT:

                try {
                    sheet.setSortingMode(UNSORTED);
                } catch (PropertyVetoException pve) {
                    //can't happen
                }

                break;

            case INVOKE_POPUP:
                sheet.showPopup(new Point(0, 0));

                break;

            case SHOW_DESCRIPTION:
                sheet.setDescriptionVisible(!sheet.isDescriptionVisible());

                break;

            case SHOW_HELP:
                break;

            case RESTORE_DEFAULT:

                try {
                    // no need to use instanceof check since this action is
                    // not accessible if a selection is not a Node.Property
                    // instance
                    //#122308 - prevent NPE in an exotic scenario
                    if( null != sheet 
                            && null != sheet.table 
                            && null != sheet.table.getSelection() ) {
                        ((Node.Property) sheet.table.getSelection()).restoreDefaultValue();
                    }
                } catch (IllegalAccessException iae) {
                    throw (IllegalStateException) new IllegalStateException("Error restoring default value").initCause(iae);
                } catch (InvocationTargetException ite) {
                    throw (IllegalStateException) new IllegalStateException("Error restoring defaul value").initCause(ite);
                }

                break;

            default:
                throw new IllegalArgumentException(Integer.toString(id));
            }
        }

        @Override
        public boolean isEnabled() {
            if ((id == INVOKE_POPUP) && Boolean.TRUE.equals(sheet.getClientProperty("disablePopup"))) {
                return false;
            }

            return super.isEnabled();
        }
    }

    private final class SheetPCListener extends NodeAdapter {
        private PropertyChangeListener inner;

        /** Cache the current node locally only in the listener */
        private Node currNode;

        public SheetPCListener() {
            inner = new PCL();
        }

        /** Attach to a node, detaching from the last one if non-null.  */
        public void attach(Node n) {
            if (currNode != n) {
                if (currNode != null) {
                    detach();
                }

                if (n != null) {
                    n.addPropertyChangeListener(inner);
                    n.addNodeListener(this);

                    if (PropUtils.isLoggable(PropertySheet.class)) {
                        PropUtils.log(PropertySheet.class, "Now listening for changes on " + n);
                    }
                }

                currNode = n;
            }
        }

        public Node getNode() {
            return currNode;
        }

        public Node detach() {
            Node n = currNode;

            if (n != null) {
                if (PropUtils.isLoggable(PropertySheet.class)) {
                    PropUtils.log(PropertySheet.class, "Detaching listeners from " + n);
                }

                n.removePropertyChangeListener(inner);
                n.removeNodeListener(this);

                //clear the reference
                currNode = null;
            }

            return n;
        }

        /** Receives property change events directed to the NodeListener */
        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            final String nm = evt.getPropertyName();

            if (Node.PROP_PROPERTY_SETS.equals(nm)) {
                final Node n = (Node) evt.getSource();
                Mutex.EVENT.readAccess(
                    new Runnable() {
                        public void run() {
                            if (currNode == n) {
                                setCurrentNode(n);
                            }
                        }
                    }
                );
            } else if (
                Node.PROP_COOKIE.equals(nm) || //weed out uninteresting property changes
                Node.PROP_ICON.equals(nm) || Node.PROP_PARENT_NODE.equals(nm) || Node.PROP_OPENED_ICON.equals(nm) ||
                    Node.PROP_LEAF.equals(nm)
            ) {
                return;
            } else {
                Runnable runnable = new Runnable() {
                    public void run() {
                        //the following must run in EDT to avoid deadlocks, see #91371
                        if (isDescriptionVisible() &&
                            (Node.PROP_DISPLAY_NAME.equals(nm) || Node.PROP_SHORT_DESCRIPTION.equals(nm))) {
                            
                            //XXX SHOULD NOT BE FIRED TO NODELISTENERS
                            Node n = (Node) evt.getSource();
                            if (currNode == n) {
                                String description = (String) n.getValue("nodeDescription"); //NOI18N
                                psheet.setDescription(n.getDisplayName(), (description == null) ? n.getShortDescription() : description);
                                table.setBeanName(n.getDisplayName());
                            }
                        }
                    }
                };
                if( EventQueue.isDispatchThread() ) {
                    runnable.run();
                } else {
                    EventQueue.invokeLater(runnable);
                }
            }
             /*else {
              if (evt.getPropertyName() == null) {
                  //Trigger rebuilding the entire list of properties, probably
                  //one has been added or removed
                  setCurrentNode(currNode);
              }
            }
             */
        }

        @Override
        public void nodeDestroyed(final org.openide.nodes.NodeEvent ev) {
            if (ev.getNode() == currNode) {
                detach();
                Mutex.EVENT.readAccess(
                        new Runnable() {

                            public void run() {
                                if (currNode == null) {
                                    doSetNodes(null);
                                }
                            }
                        });
            }
        }

        private final class PCL implements PropertyChangeListener {
            /** Receives property change events directed to PropertyChangeListeners,
             * not NodeListeners */
            public void propertyChange(final PropertyChangeEvent evt) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        String nm = evt.getPropertyName();
                        /*
                        if (Node.PROP_COOKIE.equals(nm) || // weed out frequently abused property changes
                                Node.PROP_ICON.equals(nm) || Node.PROP_PARENT_NODE.equals(nm) ||
                                Node.PROP_OPENED_ICON.equals(nm) || Node.PROP_LEAF.equals(nm)) {
                            ErrorManager.getDefault().log(
                                    ErrorManager.WARNING,
                                    "Recived bogus property change " + nm + " from " + evt.getSource() + // NOI18N
                                    ".  This should ony be fired to" + // NOI18N
                                    "NodeListeners, not general property change listeners"); // NOI18N
                        } else if (isDescriptionVisible() &&
                                (Node.PROP_DISPLAY_NAME.equals(nm) || Node.PROP_SHORT_DESCRIPTION.equals(nm))) {
                            Node n = (Node) evt.getSource();
                            /*
                            fallbackTitle = n.getDisplayName();
                            fallbackDescription = n.getShortDescription();
                            if (infoPanel != null) {
                                table.fireChange();
                                infoPanel.getBottomComponent().repaint();
                            }
                            
                        } else 
                         */
                        if (nm == null) {
                            if (currNode != null) {
                                setCurrentNode(currNode);
                            }
                        } else {
                            table.repaintProperty(nm);
                        }
                    }
                });
            }
        }
    }
    
    private static final class TabInfo {
        public String[] titles;
        public Object[] sets;

        public TabInfo(String[] titles, Object[] sets) {
            this.titles = titles;
            this.sets = sets;
        }

        public PropertySet[] getSets(int i) {
            return (PropertySet[]) sets[i];
        }
    }
}
