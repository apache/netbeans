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

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SingleSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.plaf.ComponentUI;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDisplayer;
import org.netbeans.swing.tabcontrol.TabbedContainer;
import org.netbeans.swing.tabcontrol.TabbedContainerUI;
import org.netbeans.swing.tabcontrol.WinsysInfoForTabbedContainer;
import org.netbeans.swing.tabcontrol.event.ArrayDiff;
import org.netbeans.swing.tabcontrol.event.ComplexListDataEvent;
import org.netbeans.swing.tabcontrol.event.ComplexListDataListener;
import org.netbeans.swing.tabcontrol.event.TabActionEvent;
import org.netbeans.swing.tabcontrol.event.VeryComplexListDataEvent;

/**
 * Default UI implementation for tabbed containers.  Manages installing the tab
 * contentDisplayer and content contentDisplayer components, and the
 * relationship between the data models and selection models of the UI.
 * <p>
 * Note that there is typically little reasons to subclass this - to affect the display
 * or behavior of the tabs, implement {@link org.netbeans.swing.tabcontrol.TabDisplayerUI},
 * the UI delegate for the embedded component which displays the tabs.
 *
 * @author Tim Boudreau
 */
public class DefaultTabbedContainerUI extends TabbedContainerUI {
    /**
     * Action listener which receives actions from the tab displayer and propagates them through
     * action listeners on the tab displayer, to allow clients to consume the events.
     */
    private ActionListener actionListener = null;
    /**
     * Flag to ensure listeners attached, since ComponentShown notifications are not always
     * reliable.
     */
    /** UIManager key for the border of the tab displayer in editor ui type. */
    public static final String KEY_EDITOR_CONTENT_BORDER = "TabbedContainer.editor.contentBorder"; //NOI18N
    /** UIManager key for the border of the tab displayer in editor ui type */
    public static final String KEY_EDITOR_TABS_BORDER = "TabbedContainer.editor.tabsBorder"; //NOI18N
    /** UIManager key for the border of the entire tabbed container in editor ui type*/
    public static final String KEY_EDITOR_OUTER_BORDER = "TabbedContainer.editor.outerBorder"; //NOI18N

    /** UIManager key for the border of the tab displayer in view ui type. */
    public static final String KEY_VIEW_CONTENT_BORDER = "TabbedContainer.view.contentBorder"; //NOI18N
    /** UIManager key for the border of the tab displayer in view ui type. */
    public static final String KEY_VIEW_TABS_BORDER = "TabbedContainer.view.tabsBorder"; //NOI18N
    /** UIManager key for the border of the entire tabbed container in view ui type.*/
    public static final String KEY_VIEW_OUTER_BORDER = "TabbedContainer.view.outerBorder"; //NOI18N

    /** UIManager key for the border of the tab displayer in sliding ui type. */
    public static final String KEY_SLIDING_CONTENT_BORDER = "TabbedContainer.sliding.contentBorder"; //NOI18N
    /** UIManager key for the border of the tab displayer in sliding ui type */
    public static final String KEY_SLIDING_TABS_BORDER = "TabbedContainer.sliding.tabsBorder"; //NOI18N
    /** UIManager key for the border of the entire tabbed container in sliding ui type*/
    public static final String KEY_SLIDING_OUTER_BORDER = "TabbedContainer.sliding.outerBorder"; //NOI18N

    /** UIManager key for the border of the tab displayer in toolbar ui type. */
    public static final String KEY_TOOLBAR_CONTENT_BORDER = "TabbedContainer.toolbar.contentBorder"; //NOI18N
    /** UIManager key for the border of the tab displayer in toolbar ui type */
    public static final String KEY_TOOLBAR_TABS_BORDER = "TabbedContainer.toolbar.tabsBorder"; //NOI18N
    /** UIManager key for the border of the entire tabbed container in toolbar ui type*/
    public static final String KEY_TOOLBAR_OUTER_BORDER = "TabbedContainer.toolbar.outerBorder"; //NOI18N

    /** Component listener which listens on the container, and attaches/detaches listeners.
     * <strong>do not alter the value in this field.  To provide a different implementation,
     * override the appropriate creation method.</strong>
     **/
    protected ComponentListener componentListener = null;
    /** Change listener which tracks changes in the selection model and changes the displayed
     * component to reflect the selected tab
     * <strong>do not alter the value in this field.  To provide a different implementation,
     * override the appropriate creation method.</strong>
     */
    protected ChangeListener selectionListener = null;
    /** Listener on the data model, which handles updating the contained components to keep them
     * in sync with the contents of the data model.
     * <strong>do not alter the value in this field.  To provide a different implementation,
     * override the appropriate creation method.</strong>
     * @see TabbedContainer#setContentPolicy
     */
    protected ComplexListDataListener modelListener = null;
    /**
     * Layout manager which will handle layout of the tabbed container.
     * <strong>do not alter the value in this field.  To provide a different implementation,
     * override the appropriate creation method.</strong>
     */
    protected LayoutManager contentDisplayerLayout = null;
    /** Property change listener which detects changes on the tabbed container, such as its active state, which
     * should be propagated to the tab displayer.
     * <strong>do not alter the value in this field.  To provide a different implementation,
     * override the appropriate creation method.</strong>
     */
    protected PropertyChangeListener propertyChangeListener = null;
    /**
     * FxProvider which will provide transition effects when tabs are changed.  By default, only used for
     * TabbedContainer.TYPE_SLIDE tabs.
     * <strong>do not alter the value in this field.  To provide a different implementation,
     * override the appropriate creation method.</strong>
     */
    protected FxProvider slideEffectManager = null;

    /**
     * The component which displays the selected component in the tabbed container.
     * <strong>do not alter the value in this field.  To provide a different implementation,
     * override the appropriate creation method.</strong>
     */
    protected JComponent contentDisplayer = null;
    /**
     * The Displayer for the tabs.  Normally an instance of <code>TabDisplayer</code>.  To get the actual
     * GUI component that is showing tabs, call its <code>getComponent()</code> method.
     * <strong>do not alter the value in this field.  To provide a different implementation,
     * override the appropriate creation method.</strong>
     */
    protected TabDisplayer tabDisplayer = null;
    
    private HierarchyListener hierarchyListener = null;

    /**
     * Creates a new instance of DefaultTabbedContainerUI
     */
    public DefaultTabbedContainerUI(TabbedContainer c) {
        super(c);
    }

    public static ComponentUI createUI(JComponent c) {
        return new DefaultTabbedContainerUI((TabbedContainer) c);
    }

    /** This method is final.  Subclasses which need to provide additional initialization should override
     * <code>install()</code>
     *
     * @param c A JComponent, which must == the displayer field initialized in the constructor
     */
    @Override
    public final void installUI(JComponent c) {
        assert c == container;
        container.setLayout(createLayout());
        contentDisplayer = createContentDisplayer();
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) //NOI18N
            && (container.getType() == TabbedContainer.TYPE_VIEW
                || container.getType() == TabbedContainer.TYPE_SLIDING)
                && !Boolean.getBoolean("nb.explorerview.aqua.defaultbackground")) { //NOI18N
            contentDisplayer.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
            contentDisplayer.setOpaque(true);
        }
        tabDisplayer = createTabDisplayer();
        selectionListener = createSelectionListener();
        modelListener = createModelListener();
        componentListener = createComponentListener();
        propertyChangeListener = createPropertyChangeListener();
        contentDisplayerLayout = createContentDisplayerLayout();
        slideEffectManager = createFxProvider();
        actionListener = createDisplayerActionListener();
        container.setLayout(createLayout());
        hierarchyListener = new ContainerHierarchyListener();
        forward = new ForwardingMouseListener(container);
        installContentDisplayer();
        installTabDisplayer();
        installBorders();
        installListeners();
        install();
        //#41681, #44278, etc. FOCUS related. the UI needs to be the first listener to be notified
        // that the selection has changed. Otherwise strange focus effects kick in, eg. when the winsys snapshot gets undated beforehand..
        tabDisplayer.getSelectionModel().addChangeListener(selectionListener);

    }
    
    
    /** Used by unit tests */
    TabDisplayer getTabDisplayer() {
        return tabDisplayer;
    }

    private MouseListener forward = null;

    /** This method is final.  Subclasses which need to provide additional initialization should override
     * <code>uninstall()</code>
     *
     * @param c A JComponent, which must == the displayer field initialized in the constructor
     */
    public final void uninstallUI(JComponent c) {
        assert c == container;
        tabDisplayer.getSelectionModel().removeChangeListener(selectionListener);
        uninstall();
        uninstallListeners();
        uninstallDisplayers();

        container = null;
        contentDisplayer = null;
        tabDisplayer = null;
        selectionListener = null;
        modelListener = null;
        componentListener = null;
        propertyChangeListener = null;
        contentDisplayerLayout = null;
        actionListener = null;
        forward = null;
    }

    /** Subclasses may override this method to do anything they need to do on installUI().  It will
     * be called after listeners, the displayer, etc. (all pseudo-final protected fields) have been
     * initialized.
     */
    protected void install() {

    }

    /** Subclasses may override this method to do anything they need to do on uninstallUI().  It will
     * be called before the protected fields of the instance have been nulled.
     */
    protected void uninstall() {

    }
    
    protected boolean uichange() {
        installBorders();
        return false;
    }    

    /**
     * Installs the content displayer component and its layout manager
     */
    protected void installContentDisplayer() {
        contentDisplayer.setLayout(contentDisplayerLayout);
        container.add(contentDisplayer, BorderLayout.CENTER);
    }

    /**
     * Installs the tab displayer component into the container.  By default, installs it using the
     * constraint <code>BorderLayout.NORTH</code>.
     */
    protected void installTabDisplayer() {
        container.add(tabDisplayer, BorderLayout.NORTH);
        tabDisplayer.registerShortcuts(container);
    }

    /**
     * Installs borders on the container, content displayer and tab displayer
     */
    protected void installBorders() {
        String tabsKey;
        String contentKey;
        String outerKey;
        switch (container.getType()) {
            case TabbedContainer.TYPE_EDITOR :
                tabsKey = KEY_EDITOR_TABS_BORDER;
                contentKey = KEY_EDITOR_CONTENT_BORDER;
                outerKey = KEY_EDITOR_OUTER_BORDER;
                break;
            case TabbedContainer.TYPE_VIEW :
                tabsKey = KEY_VIEW_TABS_BORDER;
                contentKey = KEY_VIEW_CONTENT_BORDER;
                outerKey = KEY_VIEW_OUTER_BORDER;
                break;
            case TabbedContainer.TYPE_SLIDING :
                tabsKey = KEY_SLIDING_TABS_BORDER;
                contentKey = KEY_SLIDING_CONTENT_BORDER;
                outerKey = KEY_SLIDING_OUTER_BORDER;
                break;
            case TabbedContainer.TYPE_TOOLBAR :
                tabsKey = KEY_TOOLBAR_TABS_BORDER;
                contentKey = KEY_TOOLBAR_CONTENT_BORDER;
                outerKey = KEY_TOOLBAR_OUTER_BORDER;
                break;
            default :
                throw new IllegalStateException ("Unknown type: "
                    + container.getType());
        }
        try {
            Border b = (Border) UIManager.get (contentKey);
            contentDisplayer.setBorder(b);
            b = (Border) UIManager.get (tabsKey);
            tabDisplayer.setBorder(b);
            b = (Border) UIManager.get (outerKey);
            container.setBorder(b);
        } catch (ClassCastException cce) {
            System.err.println ("Expected a border from UIManager for "
                + tabsKey + "," + contentKey + "," + outerKey);
        }
    }

    /**
     * Installs a component listener on the component.  Listeners on the data
     * model and selection model are installed when the component is shown, as
     * detected by the component listener.
     */
    protected void installListeners() {
        container.addComponentListener(componentListener);
        container.addHierarchyListener (hierarchyListener);
        //Allow mouse events to be forwarded as if they came from the 
        //container
        tabDisplayer.addMouseListener (forward);
        contentDisplayer.addMouseListener (forward);
    }

    /**
     * Begin listening to the model for changes in the selection, which should
     * cause us to update the displayed component in the content
     * contentDisplayer. Listening starts when the component is first shown, and
     * stops when it is hidden;  if you override <code>createComponentListener()</code>,
     * you will need to call this method when the component is shown.
     */
    protected void attachModelAndSelectionListeners() {
        container.getModel().addComplexListDataListener(modelListener);
        container.addPropertyChangeListener(propertyChangeListener);
        tabDisplayer.setActive (container.isActive());
        tabDisplayer.addActionListener (actionListener);
    }

    /**
     * Stop listening to the model for changes in the selection, which should
     * cause us to update the displayed component in the content
     * contentDisplayer, and changes in the data model which can affect the
     * displayed component. Listening starts when the component is first shown,
     * and stops when it is hidden;  if you override <code>createComponentListener()</code>,
     * you will need to call this method when the component is hidden.
     */
    protected void detachModelAndSelectionListeners() {
        container.getModel().removeComplexListDataListener(modelListener);
        container.removePropertyChangeListener(propertyChangeListener);
        tabDisplayer.removeActionListener (actionListener);
    }

    /**
     * Uninstalls the component listener installed in <code>installListeners()</code>
     */
    protected void uninstallListeners() {
        container.removeComponentListener(componentListener);
        container.removeHierarchyListener (hierarchyListener);
        componentListener = null;
        propertyChangeListener = null;
        tabDisplayer.removeMouseListener (forward);
        contentDisplayer.removeMouseListener (forward);
    }

    /**
     * Uninstalls and nulls references to the content contentDisplayer and tab
     * contentDisplayer, and removes all components from the content
     * contentDisplayer.
     */
    protected void uninstallDisplayers() {
        container.remove(contentDisplayer);
        container.remove(tabDisplayer);
        tabDisplayer.unregisterShortcuts(container);
        contentDisplayer.removeAll();
        contentDisplayer = null;
        tabDisplayer = null;
    }

    /**
     * Create the component which will display the tabs.
     */
    protected TabDisplayer createTabDisplayer() {
        TabDisplayer result = null;
        WinsysInfoForTabbedContainer winsysInfo = container.getContainerWinsysInfo();
        if (winsysInfo != null) {
            result = new TabDisplayer(
                    container.getModel(), container.getType(), winsysInfo);
        } else {
            result = new TabDisplayer(
                    container.getModel(), container.getType(), container.getLocationInformer());
        }
        result.setName("Tab Displayer");  //NOI18N
        result.setComponentConverter( container.getComponentConverter() );
        return result;
    }

    /**
     * Create the component which will contain the content (the components which
     * correspond to tabs).  The default implementation simply returns a
     * vanilla, unadorned <code>JPanel</code>.
     */
    protected JPanel createContentDisplayer() {
        JPanel result = new JPanel();
        result.setName ("Content displayer"); //NOI18N
        return result;
    }

    /**
     * Create an FxProvider instance which will provide transition effects when tabs are selected.
     * By default creates a no-op instance for all displayer types except TYPE_SLIDING.
     *
     * @return An instance of FxProvider
     */
    protected FxProvider createFxProvider() {
        if (NO_EFFECTS || (tabDisplayer.getType() != TabDisplayer.TYPE_SLIDING && !EFFECTS_EVERYWHERE)) {
            return new NoOpFxProvider();
        } else {
            if (ADD_TO_GLASSPANE) {
                return new LiveComponentSlideFxProvider();
            } else {
                return new ImageSlideFxProvider();
            }
        }
    }

    /**
     * Creates the content contentDisplayer's layout manager, responsible for
     * ensuring that the correct component is on top and is the only one
     * showing
     */
    protected LayoutManager createContentDisplayerLayout() {
        return new StackLayout();
    }

    /**
     * Create the layout manager that will manage the layout of the
     * TabbedContainer.  A TabbedContainer contains two components - the tabs
     * contentDisplayer, and the component contentDisplayer.
     * <p/>
     * The layout manager determines the position of the tabs relative to the
     * contentDisplayer component which displays the tab contents.
     * <p/>
     * The default implementation uses BorderLayout.  If you override this, you
     * should probably override <code>installDisplayer()</code> as well.
     */
    protected LayoutManager createLayout() {
        if (container.getType() == TabbedContainer.TYPE_SLIDING) {
            return new SlidingTabsLayout();
        } else if (container.getType() == TabbedContainer.TYPE_TOOLBAR) {
            return new ToolbarTabsLayout();
        } else {
            return new BorderLayout();
        }
    }

    /**
     * Create a component listener responsible for initializing the
     * contentDisplayer component when the tabbed container is shown
     */
    protected ComponentListener createComponentListener() {
        return new ContainerComponentListener();
    }

    /**
     * Create a property change listener which will update the tab displayer in
     * accordance with property changes on the container.  Currently the only
     * property change of interest is calls to <code>TabbedContainer.setActive()</code>,
     * which simply cause the active state to be set on the displayer.
     */
    protected PropertyChangeListener createPropertyChangeListener() {
        return new ContainerPropertyChangeListener();
    }

    /** Creates an action listener to catch actions performed on the tab displayer
     * and forward them to listeners on the container for consumption.
     *
     * @return An action listener
     */
    private ActionListener createDisplayerActionListener() {
        return new DisplayerActionListener();
    }

    /**
     * Ensures that the component the selection model says is selected is the
     * one that is showing.
     */
    protected void ensureSelectedComponentIsShowing() {
        int i = tabDisplayer.getSelectionModel().getSelectedIndex();
        if (i != -1) {
            TabData td = container.getModel().getTab(i);
            showComponent(toComp(td));
        }
    }
    
    /** Convenience method for fetching a component from a TabData object
     * via the container's ComponentConverter */
    protected final Component toComp (TabData data) {
        return container.getComponentConverter().getComponent(data);
    }

    /**
     * Shows the passed component. <strong>This method does not communicate with
     * the data model in any way shape or form, it just moves the passed
     * component to the front.  It should only be called in response to an event
     * from the data model or selection model.
     * <p/>
     * If you override <code>createContentDisplayerLayoutModel()</code> to
     * provide your own layout manager to arrange the displayed component, you
     * need to override this to tell the layout (or do whatever is needed) to
     * change the component that is shown.
     *
     * @param c The component to be shown
     * @return The previously showing component, or null if no change was made
     */
    protected Component showComponent(Component c) {
        if (contentDisplayerLayout instanceof StackLayout) {
            StackLayout stack = ((StackLayout) contentDisplayerLayout);
            Component last = stack.getVisibleComponent();
            stack.showComponent(c, contentDisplayer);
            if (c != null) {
                Integer offset = (Integer)((JComponent)c).getClientProperty("MultiViewBorderHack.topOffset");
                contentDisplayer.putClientProperty("MultiViewBorderHack.topOffset", offset);
            } else {
                contentDisplayer.putClientProperty("MultiViewBorderHack.topOffset", null);
            }
            if (last != c) {
                maybeRemoveLastComponent(last);
                return last;
            }
        }
        return null;
    }

    /**
     * Shows a component in the control, using the <code>FxProvider</code> created in
     * <code>createFxProvider()</code> to manage showing it.  Equivalent to calling <code>showComponent</code>,
     * but there may be a delay while the effect is performed.  If no <code>FxProvider</code> is installed,
     * this will simply delegate to <code>showComponent</code>; if not, the <code>FxProvider</code> is expected
     * to do that when its effect is completed.
     *
     * @param c The component to be shown.
     */
    protected final void showComponentWithFxProvider (Component c) {
        if (slideEffectManager == null || !container.isShowing() || (!(c instanceof JComponent))) {
            Component last = showComponent (c);
            maybeRemoveLastComponent (last);
        } else {
            slideEffectManager.start((JComponent) c, container.getRootPane(), 
                tabDisplayer.getClientProperty(TabDisplayer.PROP_ORIENTATION));
        }
    }

    /**
     * Removes the passed component from the AWT hierarchy if the container's content policy is
     * CONTENT_POLICY_ADD_ONLY_SELECTED.
     *
     * @param c The component that should be removed
     */
    private final void maybeRemoveLastComponent (Component c) {
        if (c != null && container.getContentPolicy() == TabbedContainer.CONTENT_POLICY_ADD_ONLY_SELECTED) {
            contentDisplayer.remove (c);
        }
    }

    /**
     * Fills contentDisplayer container with components retrieved from model.
     */
    protected void initDisplayer() {
        if (container.getContentPolicy() == TabbedContainer.CONTENT_POLICY_ADD_ALL) {
            List tabs = container.getModel().getTabs();
            Component curC = null;
            for (Iterator iter = tabs.iterator(); iter.hasNext();) {
                curC = toComp ((TabData) iter.next());
                // string parameter is needed for StackLayout to kick in correctly
                contentDisplayer.add(curC, "");
            }
        } else {
            int i = tabDisplayer.getSelectionModel().getSelectedIndex();
            if (i != -1) {
                TabData td = container.getModel().getTab(i);
                contentDisplayer.add(toComp(td), "");
            }
        }
        updateActiveState();
    }

    /**
     * Create a listener for the TabDataModel.  This listener is responsible for
     * keeping the state of the contained components in the displayer in sync
     * with the contents of the data model.  Note that it is not necessary for
     * this listener to adjust the selection - DefaultTabSelectionModel handles
     * cases such as removing the selected component appropriately, so if such a
     * model change happens, a selection change will be immediately forthcoming
     * to handle it.
     * <p/>
     * Note that it is important that this listener be added to the data model
     * <i>after</i> the DefaultSelectionModel has added its listener. It is
     * important to create the displayer component before adding this listener.
     * Some support for privilged listeners may be added to DefaultTabDataModel
     * in the future to avoid this issue entirely.
     */
    protected ComplexListDataListener createModelListener() {
        return new ModelListener();
    }

    /**
     * Create a ChangeListener which will listen to the selection model of the
     * tab displayer, and update the displayed component in the displayer when
     * the selection model changes.
     */
    protected ChangeListener createSelectionListener() {
        return new SelectionListener();
    }

    /** Sets the active state of the displayer to match that of the container */
    private void updateActiveState() {
        //#45630 - more of a hack than a fix.
        //apparently uninstallUI() was called before the the ContainerPropertyChangeListener instance was removed in 
        //ContainerHierarchyListener's hierarchyChanged method.
        // for such case the property change should be a noop.
        TabDisplayer displ = tabDisplayer;
        TabbedContainer cont = container;
        if (displ != null && cont != null) {
            displ.setActive(cont.isActive());
        }
        
    }

    public Rectangle getTabRect(int tab, Rectangle r) {
        if (r == null) {
            r = new Rectangle();
        }
        tabDisplayer.getTabRect(tab, r);
        Point p = tabDisplayer.getLocation();
        r.x += p.x;
        r.y += p.y;
        return r;
    }
    
    protected void requestAttention (int tab) {
        tabDisplayer.requestAttention (tab);
    }
    
    protected void cancelRequestAttention (int tab) {
        tabDisplayer.cancelRequestAttention(tab);
    }

    @Override
    protected void setAttentionHighlight (int tab, boolean highlight) {
        tabDisplayer.setAttentionHighlight(tab, highlight);
    }
    
    public void setShowCloseButton (boolean val) {
        tabDisplayer.setShowCloseButton(val);
    }
    
    public boolean isShowCloseButton () {
        return tabDisplayer.isShowCloseButton();
    }

    /**
     * Scroll pane-like border, good general border around windows. Used if no
     * border is provided via UIDefaults.
     */
    private static final class DefaultWindowBorder implements Border {
        private static final Insets insets = new Insets(1, 1, 2, 2);

        public void paintBorder(Component c, Graphics g, int x, int y, int w,
                                int h) {
            g.translate(x, y);

            g.setColor(UIManager.getColor("controlShadow")); //NOI18N
            g.drawRect(0, 0, w - 2, h - 2);
            g.setColor(UIManager.getColor("controlHighlight")); //NOI18N
            g.drawLine(w - 1, 1, w - 1, h - 1);
            g.drawLine(1, h - 1, w - 1, h - 1);

            g.translate(-x, -y);
        }

        public Insets getBorderInsets(Component c) {
            return insets;
        }

        public boolean isBorderOpaque() {
            return true;
        }
    } // end of DefaultWindowBorder

    protected class ContainerPropertyChangeListener implements PropertyChangeListener {
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (TabbedContainer.PROP_ACTIVE.equals(evt.getPropertyName())) {
                updateActiveState();
            }
        }
    }
    
    /** Checks the position of the tabbed container relative to its parent
     * window, and potentially updates its orientation client property.
     *
     * @see TabDisplayer#PROP_ORIENTATION
     */
    protected final void updateOrientation() {
        if (!container.isDisplayable()) {
            return;
        }
        if (Boolean.FALSE.equals(container.getClientProperty (TabbedContainer.PROP_MANAGE_TAB_POSITION))) {
            //The client has specified that it does not want automatic management
            //of the displayer orientation
            return;
        }
        Object currOrientation = tabDisplayer.getClientProperty(TabDisplayer.PROP_ORIENTATION);
        Container window = container.getTopLevelAncestor();

        Rectangle containerBounds = container.getBounds();
        containerBounds = SwingUtilities.convertRectangle(container, containerBounds, window);

        boolean longestIsVertical = containerBounds.width < containerBounds.height;

        int distanceToLeft = containerBounds.x;
        int distanceToTop = containerBounds.y;
        int distanceToRight = window.getWidth() - (containerBounds.x + containerBounds.width);
        int distanceToBottom = window.getHeight() - (containerBounds.y + containerBounds.height);

        Object orientation;
        if (!longestIsVertical) {
            if (distanceToBottom > distanceToTop) {
                orientation = TabDisplayer.ORIENTATION_NORTH;
            } else {
                orientation = TabDisplayer.ORIENTATION_SOUTH;
            }
        } else {
            if (distanceToLeft > distanceToRight) {
                orientation = TabDisplayer.ORIENTATION_EAST;
            } else {
                orientation = TabDisplayer.ORIENTATION_WEST;
            }
        }

        if (currOrientation != orientation) {
            tabDisplayer.putClientProperty(
                TabDisplayer.PROP_ORIENTATION, orientation);
            container.validate();
        }
    }

    public int tabForCoordinate(Point p) {
        p = SwingUtilities.convertPoint(container, p, tabDisplayer);
        return tabDisplayer.tabForCoordinate(p);
    }

    public void makeTabVisible(int tab) {
        tabDisplayer.makeTabVisible (tab);
    }

    public SingleSelectionModel getSelectionModel() {
        return tabDisplayer.getSelectionModel();
    }

    public Image createImageOfTab(int idx) {
        return tabDisplayer.getUI().createImageOfTab(idx);
    }

    public Polygon getExactTabIndication(int idx) {
        Polygon result = tabDisplayer.getUI().getExactTabIndication(idx);
        scratchPoint.setLocation(0,0);
        Point p = SwingUtilities.convertPoint(tabDisplayer, scratchPoint, container);
        result.translate (-p.x, -p.y);
        return appendContentBoundsTo(result);
    }

    private Point scratchPoint = new Point();
    public Polygon getInsertTabIndication(int idx) {
        Polygon result = tabDisplayer.getUI().getInsertTabIndication(idx);
        scratchPoint.setLocation(0,0);
        Point p = SwingUtilities.convertPoint(tabDisplayer, scratchPoint, container);
        result.translate (-p.x, -p.y);
        return appendContentBoundsTo(result);
    }

    private Polygon appendContentBoundsTo (Polygon p) {
        int width = contentDisplayer.getWidth();
        int height = contentDisplayer.getHeight();

        int[] xpoints = new int[p.npoints + 4];
        int[] ypoints = new int[xpoints.length];

        //XXX not handling this correctly for non-top orientations

        int pos = 0;
        Object orientation = tabDisplayer.getClientProperty (TabDisplayer.PROP_ORIENTATION);

        int tabsHeight = tabDisplayer.getHeight();
        if (orientation == null || orientation == TabDisplayer.ORIENTATION_NORTH) {

            xpoints[pos] = 0;
            ypoints[pos] = tabsHeight;

            pos++;

            xpoints[pos] = p.xpoints[p.npoints-1];
            ypoints[pos] = tabsHeight;
            pos++;

            for (int i=0; i < p.npoints-2; i++) {
                xpoints [pos] = p.xpoints[i];
                ypoints [pos] = p.ypoints[i];
                pos++;
            }

            xpoints[pos] = xpoints[pos-1];
            ypoints[pos] = tabsHeight;

            pos++;

            xpoints[pos] = width - 1;
            ypoints[pos] = tabsHeight;

            pos++;

            xpoints[pos] = width - 1;
            ypoints[pos] = height -1;

            pos++;

            xpoints[pos] = 0;
            ypoints[pos] = height - 1;
        } else if (orientation == TabDisplayer.ORIENTATION_SOUTH) {
            int yxlate = contentDisplayer.getHeight() * 2;

            xpoints[pos] = 0;
            ypoints[pos] = 0;

            pos++;

            xpoints[pos] = container.getWidth();
            ypoints[pos] = 0;

            pos++;

            xpoints[pos] = container.getWidth();
            ypoints[pos] = container.getHeight() - tabsHeight;

            pos++;

            int upperRight = 0;
            //Search backward for the upper right corner - we only know
            //the location of the left upper corner
            int highestFound = Integer.MIN_VALUE;
            for (int i = p.npoints-2; i >= 0; i--) {
                if (highestFound < p.ypoints[i]) {
                    upperRight = i;
                    highestFound = p.ypoints[i];
                } else if (highestFound == p.ypoints[i]) {
                    break;
                }
            }

            int curr = upperRight-1;
            for (int i=p.npoints-1; i >= 0; i--) {
                xpoints[pos] = p.xpoints[curr];
                if (ypoints[pos] == highestFound) {
                    ypoints[pos] = Math.min (tabDisplayer.getLocation().y, p.ypoints[curr] + yxlate);
                } else {
                    ypoints[pos] = p.ypoints[curr] + yxlate;
                }
                pos++;
                curr++;
                if (curr == p.npoints-1) {
                    curr = 0;
                }
            }

            xpoints[pos] = 0;
            ypoints[pos] = container.getHeight() - tabsHeight;
        } else {
            //Punt on side tabs for now
            xpoints = p.xpoints;
            ypoints = p.ypoints;
        }

        Polygon result = new EqualPolygon (xpoints, ypoints, xpoints.length);
        return result;
    }

    public Rectangle getContentArea() {
        return contentDisplayer.getBounds();
    }

    public Rectangle getTabsArea() {
        return tabDisplayer.getBounds();
    }

    public int dropIndexOfPoint(Point p) {
        Point p2 = SwingUtilities.convertPoint(container, p, tabDisplayer);
        return tabDisplayer.getUI().dropIndexOfPoint (p2);
    }

    public Rectangle getTabsArea(Rectangle dest) {
        return tabDisplayer.getBounds();
    }

    /**
     * A ComponentListener which listens for show/hide to add and remove the
     * selection and model listeners
     */
    protected class ContainerComponentListener extends ComponentAdapter {
        public ContainerComponentListener() {
        }
        
        public void componentMoved (ComponentEvent e) {
            if (container.getType() == TabbedContainer.TYPE_SLIDING) {
                updateOrientation();
            }
        }
        
        public void componentResized (ComponentEvent e) {
            if (container.getType() == TabbedContainer.TYPE_SLIDING) {
                updateOrientation();
            }
        }
    }
    
    private boolean bug4924561knownShowing = false;
    /**
     * Calls <code>initDisplayer()</code>, then <code>attachModelAndSelectionListeners</code>,
     * then <code>ensureSelectedComponentIsShowing</code>
     */
    private class ContainerHierarchyListener implements HierarchyListener {
        public ContainerHierarchyListener() {
        }
        
        public void hierarchyChanged(HierarchyEvent e) {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                boolean showing = container.isShowing();
                if (showing != bug4924561knownShowing) {
                    if (container.isShowing()) {
                        initDisplayer();
                        attachModelAndSelectionListeners();
                        ensureSelectedComponentIsShowing();
                        if (container.getType() == TabbedContainer.TYPE_SLIDING) {
                            updateOrientation();
                        }
                    } else {
                        detachModelAndSelectionListeners();
                        if (container.getType() == TabbedContainer.TYPE_SLIDING) {
                            updateOrientation();
                        }
                    }
                }
                bug4924561knownShowing = showing;
            }
        }
    }
    
    private class ToolbarTabsLayout implements LayoutManager {
        
        public void layoutContainer(Container container) {
            Dimension tabSize = tabDisplayer.getPreferredSize();
            Insets ins = container.getInsets();
            int w = container.getWidth() - (ins.left + ins.right);
            tabDisplayer.setBounds (ins.left, ins.top, 
                w,
                tabSize.height);
            if( tabDisplayer.getModel().size() > 1 ) {
                //#214427 - check the preferred size again, during the first pass
                //the tab displayer may not know the available width
                Dimension newTabSize = tabDisplayer.getPreferredSize();
                if( newTabSize.height != tabSize.height ) {
                    tabSize = newTabSize;
                    tabDisplayer.setBounds (ins.left, ins.top,
                        w,
                        tabSize.height);
                }
            }
            contentDisplayer.setBounds(ins.left, 
                ins.top + tabSize.height, w,
                container.getHeight() - (ins.top + ins.bottom + tabSize.height));
        }
        
        public Dimension minimumLayoutSize(Container container) {
            Dimension tabSize = tabDisplayer.getMinimumSize();
            Dimension contentSize = contentDisplayer.getMinimumSize();
            Insets ins = container.getInsets();
            Dimension result = new Dimension(ins.left + ins.top, ins.right + ins.bottom);
            result.width += Math.max (tabSize.width, contentSize.width);
            result.height += tabSize.height + contentSize.height;
            return result;
        }
        
        public Dimension preferredLayoutSize(Container container) {
            Dimension tabSize = tabDisplayer.getPreferredSize();
            Dimension contentSize = contentDisplayer.getPreferredSize();
            Insets ins = container.getInsets();
            Dimension result = new Dimension(ins.left + ins.top, ins.right + ins.bottom);
            result.width += Math.max (tabSize.width, contentSize.width);
            result.height += tabSize.height + contentSize.height;
            return result;
        }
        
        public void removeLayoutComponent(Component component) {
            //do nothing
        }
        
        public void addLayoutComponent(String str, Component component) {
            //do nothing
        }
    }

    /**
     * A ChangeListener which updates the component displayed in the content
     * displayer with the selected component in the tab displayer's selection
     * model
     */
    protected class SelectionListener implements ChangeListener {
        public SelectionListener() {
        }

        public void stateChanged(ChangeEvent e) {
            if (container.isShowing() 
             //a special case for property sheet dialog window - the selection 
             //change must be processed otherwise the tabbed container may have
             //undefined preferred size so the property window will be too small
             || container.getClientProperty("tc") != null) { //NOI18N
                int idx = tabDisplayer.getSelectionModel().getSelectedIndex();
                if (idx != -1) {
                    Component c = toComp(container.getModel().getTab(idx));
                    c.setBounds(0, 0, contentDisplayer.getWidth(),
                                contentDisplayer.getHeight());
                    showComponentWithFxProvider(c);
                } else {
                    showComponent (null);
                }
            }
        }
    }

    /**
     * This class does the heavy lifting of keeping the content of the content
     * displayer up-to-date with the contents of the data model.
     */
    protected class ModelListener implements ComplexListDataListener {
        public ModelListener() {
        }

        /**
         * DefaultTabDataModel will always call this method with an instance of
         * ComplexListDataEvent.
         */
        public void contentsChanged(ListDataEvent e) {
            //Only need to reread components on setTab (does winsys even use it?)
            if (e instanceof ComplexListDataEvent) {
                ComplexListDataEvent clde = (ComplexListDataEvent) e;
                int index = clde.getIndex0();
                if (clde.isUserObjectChanged() && index != -1) {
                    Component comp = contentDisplayer.getComponent(
                            index);
                    Component nue = toComp(tabDisplayer.getModel().getTab(index));
                    contentDisplayer.remove(comp);
                    
                    boolean add = 
                        container.getContentPolicy() == 
                        TabbedContainer.CONTENT_POLICY_ADD_ALL || 
                        index == 
                        container.getSelectionModel().getSelectedIndex();
                    
                    if (add) {
                        contentDisplayer.add(nue, index);
                    }
                }
                if (clde.isTextChanged()) {
                    maybeMakeSelectedTabVisible(clde);
                }
            }
        }

        /**
         * This method is called to scroll the selected tab into view if its
         * title changes (it may be scrolled offscreen).  NetBeans' editor uses
         * this to ensure that the user can see what file they're editing when
         * the user starts typing (this triggers a * being appended to the tab
         * title, thus triggering this call).
         */
        private void maybeMakeSelectedTabVisible(ComplexListDataEvent clde) {
            if (!container.isShowing() || container.getWidth() < 10) {
                //Java module fires icon changes from badging before the
                //main window has been validated for the first time
                return;
            }
            if (tabDisplayer.getType() == TabDisplayer.TYPE_EDITOR) {
                int idx = tabDisplayer.getSelectionModel().getSelectedIndex();
                //If more than one tab changed, it's probably not an event we want.
                //Only do this if there is only one.
                if ((clde.getIndex0() == clde.getIndex1())
                        && clde.getIndex0() == idx) {
                    (tabDisplayer).makeTabVisible(idx);
                }
            }
        }


        public void intervalAdded(ListDataEvent e) {
            if (container.getContentPolicy() == TabbedContainer.CONTENT_POLICY_ADD_ALL) {
                Component curC = null;
                for (int i = e.getIndex0(); i <= e.getIndex1(); i++) {
                    curC = toComp(container.getModel().getTab(i));
                    contentDisplayer.add(curC, "");
                }
            }
        }

        public void intervalRemoved(ListDataEvent e) {
            //  we know that it must be complex data event
            ComplexListDataEvent clde = (ComplexListDataEvent) e;
            TabData[] removedTabs = clde.getAffectedItems();
            Component curComp;
            for (int i = 0; i < removedTabs.length; i++) {
                curComp = toComp(removedTabs[i]);
                contentDisplayer.remove(curComp);
            }
        }

        public void indicesAdded(ComplexListDataEvent e) {
            Component curC = null;
            if (container.getContentPolicy() == TabbedContainer.CONTENT_POLICY_ADD_ALL) {
                int[] indices = e.getIndices();
                for (int i = 0; i < indices.length; i++) {
                    curC = toComp(container.getModel().getTab(indices[i]));
                    contentDisplayer.add(curC, "");
                }
            }
        }

        public void indicesRemoved(ComplexListDataEvent e) {
            int[] indices = e.getIndices();
            TabData[] removedTabs = e.getAffectedItems();
            Component curComp;
            for (int i = 0; i < indices.length; i++) {
                curComp = toComp(removedTabs[i]);
                // TBD - add assertion curComp.getParent() != contentDisplayer
                contentDisplayer.remove(curComp);
            }
        }

        public void indicesChanged(ComplexListDataEvent e) {
            //XXX - if we keep contentPolicies, this could be simplified for
            //the non ADD_ALL policies
            if (e instanceof VeryComplexListDataEvent) {
                ArrayDiff dif = ((VeryComplexListDataEvent) e).getDiff();

                //Get the deleted and added indices
                Set deleted = dif.getDeletedIndices();
                Set added = dif.getAddedIndices();

                //Get the TabData array from before the change
                TabData[] old = dif.getOldData();
                //Get the TabData array from after the change
                TabData[] nue = dif.getNewData();

                //Now we need to fetch the set of components we should end up
                //displaying.  We need to do this because TabData.equals is only
                //true if the text *and* the component match.  So if the winsys
                //called setTabs just to change the title of a tab, we would
                //end up removing and re-adding the component for no reason.
                Set<Component> components = new HashSet<Component>();
                if (container.getContentPolicy() == TabbedContainer.CONTENT_POLICY_ADD_ALL) {
                    for (int i = 0; i < nue.length; i++) {
                        components.add(toComp(nue[i]));
                    }
                }
                boolean changed = false;

                synchronized (contentDisplayer.getTreeLock()) {
                    //See if we've got anything to delete
                    if (!deleted.isEmpty()) {
                        Iterator i = deleted.iterator();
                        while (i.hasNext()) {
                            //Get the index into the old array of a deleted tab
                            Integer idx = (Integer) i.next();
                            //Find the TabData object for it
                            TabData del = old[idx.intValue()];
                            //Make sure its component is not one we'll be adding
                            if (!components.contains(toComp(del))) {
                                //remove it
                                contentDisplayer.remove(toComp(del));
                                changed = true;
                            }
                        }
                    }
                    
                    if (container.getContentPolicy() == TabbedContainer.CONTENT_POLICY_ADD_ALL) {
                    
                        //See if we've got anything to add
                        if (!added.isEmpty()) {
                            Iterator i = added.iterator();
                            while (i.hasNext()) {
                                //Get the index into the new array of the added tab
                                Integer idx = (Integer) i.next();
                                //Find the TabData object that was added
                                TabData add = nue[idx.intValue()];
                                //Make sure it's not already showing so we don't do
                                //extra work
                                if (!contentDisplayer.isAncestorOf(
                                        toComp(add))) {
                                    contentDisplayer.add(toComp(add), "");
                                    changed = true;
                                }
                            }
                        }
                    }
                }
                //repaint
                if (changed) {
                    contentDisplayer.revalidate();
                    contentDisplayer.repaint();
                }
            }
        }
    }

    /** An action listener which listens on action events from the tab displayer (select, close, etc.)
     * and propagates them to the tabbed container's action posting mechanism, so listeners on it also
     * have an opportunity to veto undesired actions, or handle actions themselves.
     */
    private class DisplayerActionListener implements ActionListener {
        public void actionPerformed (ActionEvent ae) {
            TabActionEvent tae = (TabActionEvent) ae;
            if (!shouldPerformAction(tae.getActionCommand(), tae.getTabIndex(), tae.getMouseEvent())) {
                tae.consume();
            }
        }
    }

    private class SlidingTabsLayout implements LayoutManager {
        
        public void addLayoutComponent(String name, Component comp) {
            //do nothing
        }
        
        public void layoutContainer(Container parent) {
            JComponent c = tabDisplayer;
            
            Object orientation = c.getClientProperty (
                TabDisplayer.PROP_ORIENTATION);
            
            Dimension d = tabDisplayer.getPreferredSize();
            Insets ins = container.getInsets();
            int width = parent.getWidth() - (ins.left + ins.right);
            int height = parent.getHeight() - (ins.top + ins.bottom);
            
            if (orientation == TabDisplayer.ORIENTATION_NORTH) {
                c.setBounds (ins.left, ins.top, 
                    width, d.height);
                
                contentDisplayer.setBounds (ins.left, ins.top + d.height, 
                    width, 
                    parent.getHeight() - (d.height + ins.top + ins.bottom));
                
            } else if (orientation == TabDisplayer.ORIENTATION_SOUTH) {
                contentDisplayer.setBounds (ins.top, ins.left, width, 
                    parent.getHeight() - (d.height + ins.top + ins.bottom));
                
                c.setBounds (ins.left, parent.getHeight() - (d.height + ins.top + ins.bottom),
                    width, d.height);
            } else if (orientation == TabDisplayer.ORIENTATION_EAST) {
                contentDisplayer.setBounds (ins.left, ins.top, width - d.width,
                    height);
                
                c.setBounds (parent.getWidth() - (ins.right + d.width), ins.top, 
                    d.width, height);
                
            } else if (orientation == TabDisplayer.ORIENTATION_WEST) {
                c.setBounds (ins.left, ins.top, d.width, height);
                
                contentDisplayer.setBounds (ins.left + d.width, ins.top, 
                    width - d.width, height);
                
            } else {
                throw new IllegalArgumentException ("Unknown orientation: " + orientation);
            }
        }
        
        public Dimension minimumLayoutSize(Container parent) {
            JComponent c = tabDisplayer;
            
            Object orientation = c.getClientProperty (
                TabDisplayer.PROP_ORIENTATION);
            
            Dimension tabSize = tabDisplayer.getPreferredSize();
            Insets ins = container.getInsets();
            
            Dimension result = new Dimension();
            
            Dimension contentSize = contentDisplayer.getPreferredSize();
            if (tabDisplayer.getSelectionModel().getSelectedIndex() == -1) {
                contentSize.width = 0;
                contentSize.height = 0;
            }
            
            if (orientation == TabDisplayer.ORIENTATION_NORTH || orientation == TabDisplayer.ORIENTATION_SOUTH) {
                result.height = ins.top + ins.bottom + contentSize.height + tabSize.height;
                result.width = ins.left + ins.right + Math.max (contentSize.width, tabSize.width);
            } else {
                result.width = ins.left + ins.right + contentSize.width + tabSize.width;
                result.height = ins.top + ins.bottom + Math.max (contentSize.height, tabSize.height);
            }
            return result;
        }
        
        public Dimension preferredLayoutSize(Container parent) {
            return minimumLayoutSize(parent);
        }
        
        public void removeLayoutComponent(Component comp) {
            //do nothing
        }
    }
    
    /** A FxProvider which simply calls finish() from its start
     * method, providing no effects whatsoever */
    private final class NoOpFxProvider extends FxProvider {
        
        public void cleanup() {
            //Do nothing
        }
        
        protected void doFinish() {
            showComponent (comp);
            
        }
        
        protected void doStart() {
            finish();
        }
    }
    
    private final class ImageSlideFxProvider extends FxProvider implements ActionListener {
        private Timer timer = null;
        private Component prevGlassPane = null;
        private Dimension d = null;
        
        protected void doStart() {
            if (timer == null) {
                timer = new Timer (TIMER, this);
                timer.setRepeats(true);
            }

            prevGlassPane = root.getGlassPane();

            if (prevGlassPane.isVisible() && prevGlassPane.isShowing()) {
                //Probably a drag and drop operation - don't interfere
                doFinish();
                return;
            }

            initSize();
            img = createImageOfComponent();
            

            ImageScalingGlassPane cp = getCustomGlassPane();
            root.setGlassPane (cp);
            cp.setIncrement (0.1f);
            cp.setBounds (root.getBounds());
            cp.setVisible(true);
            cp.revalidate();
            timer.start();            
        }
        
        public void cleanup() {
            timer.stop();
            root.setGlassPane(prevGlassPane);
            prevGlassPane.setVisible(false);
            if (img != null) {
                img.flush();
            }
            img = null;
        }
        
        protected void doFinish() {
            showComponent (comp);
        }
        
        private void initSize() {
            d = comp.getPreferredSize();
            
            Dimension d2 = contentDisplayer.getSize();
            
            d.width = Math.max (d2.width, d.width);
            d.height = Math.max (d2.height, d.height);

            
            boolean flip = orientation == TabDisplayer.ORIENTATION_EAST || 
                orientation == TabDisplayer.ORIENTATION_WEST;
            
            if (d.width == 0 || d.height == 0) {
                if (flip) {
                    d.width = root.getWidth();
                    d.height = tabDisplayer.getHeight();
                } else {
                    d.width = tabDisplayer.getWidth();
                    d.height = root.getHeight();
                }
            } else {
                if (flip) {
                    d.height = Math.max (d.height, tabDisplayer.getHeight());
                } else {
                    d.width = Math.max (d.width, tabDisplayer.getWidth());
                }
            }
        }        

        private BufferedImage img = null;
        private BufferedImage createImageOfComponent() {
            if (USE_SWINGPAINTING) {
                return null;
            }
            if (d.width == 0 || d.height == 0) {
                //Avoid problems in native graphics engine scaling if we should
                //end up with crazy values
                finish();
            }

            BufferedImage img =
                GraphicsEnvironment.getLocalGraphicsEnvironment().
                    getDefaultScreenDevice().getDefaultConfiguration().
                    createCompatibleImage(d.width, d.height);
            
            Graphics2D g2d = img.createGraphics();
            JComponent c = tabDisplayer;
            
            c.setBounds (0, 0, d.width, d.height);
            comp.paint (g2d);

            return img;
        }        
        
        public void actionPerformed(java.awt.event.ActionEvent e) {
            float inc = customGlassPane.getIncrement();
            if (inc >= 1.0f) {
                finish();
            } else {
                customGlassPane.setIncrement (inc + INCREMENT);
            }
        }
        
        private ImageScalingGlassPane customGlassPane = null;
        private ImageScalingGlassPane getCustomGlassPane() {
            if (customGlassPane == null) {
                customGlassPane = new ImageScalingGlassPane();
                customGlassPane.setOpaque(false);
            }
            return customGlassPane;
        }
        
        private class ImageScalingGlassPane extends JPanel {
            private float inc = 0f;
            private Rectangle rect = new Rectangle();
            private Rectangle r2 = new Rectangle();
            private boolean changed = true;
            
            private void setIncrement (float inc) {
                this.inc = inc;
                changed = true;
                if (isShowing()) {
                    Rectangle r = getImageBounds();
                    if (SYNCHRONOUS_PAINTING) {
                        paintImmediately (r.x, r.y, r.width, r.height);
                    } else {
                        repaint(r.x, r.y, r.width, r.height);
                    }
                }
            }
            
            private float getIncrement () {
                return inc;
            }
            
            private Rectangle getImageBounds() {
                if (!changed) {
                    return rect;
                }
                Component c = tabDisplayer;
                r2.setBounds (0, 0, c.getWidth(), c.getHeight());
                
                Rectangle dispBounds = SwingUtilities.convertRectangle(c, r2, 
                    this);
                
                if (orientation == TabDisplayer.ORIENTATION_WEST) {
                    rect.x = dispBounds.x + dispBounds.width;
                    rect.y = dispBounds.y;
                    rect.width = Math.round (inc * d.width);
                    rect.height = dispBounds.height;
                } else if (orientation == TabDisplayer.ORIENTATION_EAST) {
                    rect.width = Math.round (inc * d.width);
                    rect.height = dispBounds.height;
                    rect.x = dispBounds.x - rect.width;
                    rect.y = dispBounds.y;
                } else if (orientation == TabDisplayer.ORIENTATION_SOUTH) {
                    rect.width = dispBounds.width;
                    rect.height = Math.round(inc * d.height);
                    rect.x = dispBounds.x;
                    rect.y = dispBounds.y - rect.height;
                } else if (orientation == TabDisplayer.ORIENTATION_NORTH) {
                    rect.x = dispBounds.x;
                    rect.y = dispBounds.y + dispBounds.height;
                    rect.width = dispBounds.width;
                    rect.height = Math.round(inc * d.height);
                }
                changed = false;
                return rect;
            }
            
            public void paint(Graphics g) {
                try {
                    if (USE_SWINGPAINTING) {
                        SwingUtilities.paintComponent(g, comp, this, getImageBounds());
                    } else {
                        Graphics2D g2d = (Graphics2D) g;
                        Composite comp = null;
                        if (true) {
                            comp = g2d.getComposite();
                            g2d.setComposite (AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(0.99f, inc)));
                        }
                        Rectangle r = getImageBounds();
                        if (NO_SCALE) {
                            AffineTransform at = AffineTransform.getTranslateInstance(r.x, r.y);
                            g2d.drawRenderedImage(img, at);
                        } else {
                            g2d.drawImage (img, r.x, r.y, r.x + r.width,
                                r.y + r.height, 0, 0, d.width, d.height,
                                getBackground(), null);
                        }
                        if (comp != null) {
                            g2d.setComposite(comp);
                        }
                    }
                } catch (Exception e) {
                    //Some problem in Apple's graphics scaling engine
                    e.printStackTrace();
                    finish();
                }
            }
        }
    }
    

    private final class LiveComponentSlideFxProvider extends FxProvider implements ActionListener {
        private Timer timer = null;
        private Component prevGlassPane = null;
        private Dimension d = null;

        protected void doStart() {
            if (timer == null) {
                timer = new Timer (TIMER, this);
                timer.setRepeats(true);
            }

            prevGlassPane = root.getGlassPane();
            if (prevGlassPane.isVisible() && prevGlassPane.isShowing()) {
                //Probably a drag and drop operation - don't interfere
                doFinish();
                return;
            }

            initSize();
            LiveComponentResizingGlassPane cp = getCustomGlassPane();
            root.setGlassPane (cp);
            cp.setIncrement (0.1f);
            cp.setBounds (root.getBounds());
            cp.setVisible(true);
            cp.revalidate();
            timer.start();
        }
        
        private void initSize() {
            d = comp.getPreferredSize();
            
            Dimension d2 = contentDisplayer.getSize();
            
            d.width = Math.max (d2.width, d.width);
            d.height = Math.max (d2.height, d.height);
            
            boolean flip = orientation == TabDisplayer.ORIENTATION_EAST || 
                orientation == TabDisplayer.ORIENTATION_WEST;
            
            if (d.width == 0 || d.height == 0) {
                if (flip) {
                    d.width = root.getWidth();
                    d.height = tabDisplayer.getHeight();
                } else {
                    d.width = tabDisplayer.getWidth();
                    d.height = root.getHeight();
                }
            } else {
                if (flip) {
                    d.height = Math.max (d.height, tabDisplayer.getHeight());
                } else {
                    d.width = Math.max (d.width, tabDisplayer.getWidth());
                }
            }
        }
        
        public void cleanup() {
            timer.stop();
            root.setGlassPane(prevGlassPane);
            prevGlassPane.setVisible(false);
            customGlassPane.remove(comp);
        }
        
        protected void doFinish() {
            showComponent (comp);
        }
        
        public void actionPerformed(java.awt.event.ActionEvent e) {
            float inc = customGlassPane.getIncrement();
            if (inc >= 1.0f) {
                finish();
            } else {
                customGlassPane.setIncrement (inc + INCREMENT);
            }
        }
        
        private LiveComponentResizingGlassPane customGlassPane = null;
        private LiveComponentResizingGlassPane getCustomGlassPane() {
            if (customGlassPane == null) {
                customGlassPane = new LiveComponentResizingGlassPane();
                customGlassPane.setOpaque(false);
            }
            return customGlassPane;
        }
        
        private class LiveComponentResizingGlassPane extends JPanel {
            private float inc = 0f;
            private Rectangle rect = new Rectangle();
            private Rectangle r2 = new Rectangle();
            private boolean changed = true;
            
            private void setIncrement (float inc) {
                this.inc = inc;
                changed = true;
                if (isShowing()) {
                    if (comp.getParent() != this) {
                        add(comp);
                        comp.setVisible(true);
                    }
                }
                doLayout();
            }
            
            public void doLayout() {
                Rectangle r = getImageBounds();
                comp.setBounds (r.x, r.y, r.width, r.height);
            }
            
            private float getIncrement () {
                return inc;
            }
            
            private Rectangle getImageBounds() {
                if (!changed) {
                    return rect;
                }
                Component c = tabDisplayer;
                r2.setBounds (0, 0, c.getWidth(), c.getHeight());
                
                Rectangle dispBounds = SwingUtilities.convertRectangle(c, r2, 
                    this);
                
                if (orientation == TabDisplayer.ORIENTATION_WEST) {
                    rect.x = dispBounds.x + dispBounds.width;
                    rect.y = dispBounds.y;
                    rect.width = Math.round (inc * d.width);
                    rect.height = dispBounds.height;
                } else if (orientation == TabDisplayer.ORIENTATION_EAST) {
                    rect.width = Math.round (inc * d.width);
                    rect.height = dispBounds.height;
                    rect.x = dispBounds.x - rect.width;
                    rect.y = dispBounds.y;
                } else if (orientation == TabDisplayer.ORIENTATION_SOUTH) {
                    rect.width = dispBounds.width;
                    rect.height = Math.round(inc * d.height);
                    rect.x = dispBounds.x;
                    rect.y = dispBounds.y - rect.height;
                } else if (orientation == TabDisplayer.ORIENTATION_NORTH) {
                    rect.x = dispBounds.x;
                    rect.y = dispBounds.y + dispBounds.height;
                    rect.width = dispBounds.width;
                    rect.height = Math.round(inc * d.height);
                }
                changed = false;
                return rect;
            }
        }
    }    
    
//*** A bunch of options for testing
    
    /** Sysprop to turn off all sliding effects */
    static final boolean NO_EFFECTS = Boolean.getBoolean ("nb.tabcontrol.no.fx"); //NOI18N
    /** Sysprop to turn off scaling of the slide image */
    static final boolean NO_SCALE = Boolean.getBoolean ("nb.tabcontrol.fx.no.scaling"); //NOI18N
    /** Sysprop to turn use SwingUtilities.paintComponent() instead of an image buffer for sliding effects */
    static final boolean USE_SWINGPAINTING = Boolean.getBoolean ("nb.tabcontrol.fx.swingpainting"); //NOI18N
    /** Sysprop to turn add the component being scaled to the glasspane and alter its size on a 
     * timer to accomplish growing the component */
    static final boolean ADD_TO_GLASSPANE = Boolean.getBoolean ("nb.tabcontrol.fx.use.resizing"); //NOI18N
    /** For those who <strong>really</strong> love the sliding effect and want to see it on all
     * tab controls of all types */
    static final boolean EFFECTS_EVERYWHERE = Boolean.getBoolean ("nb.tabcontrol.fx.everywhere") ||
        Boolean.getBoolean("nb.tabcontrol.fx.gratuitous"); //NOI18N
    
    /** Also have the scaled image be partially transparent as it's drawn */
    static final boolean USE_ALPHA = Boolean.getBoolean ("nb.tabcontrol.fx.use.alpha") ||
        Boolean.getBoolean("nb.tabcontrol.fx.gratuitous"); //NOI18N
    
    static  boolean SYNCHRONOUS_PAINTING = Boolean.getBoolean ("nb.tabcontrol.fx.synchronous"); //NOI18N
    
    static float INCREMENT = 0.07f;
    
    static int TIMER = 25;
    static {
        boolean gratuitous = Boolean.getBoolean("nb.tabcontrol.fx.gratuitous"); //NOI18N
        String s = System.getProperty ("nb.tabcontrol.fx.increment"); //NOI18N
        if (s != null) {
            try {
                INCREMENT = Float.parseFloat(s);
            } catch (Exception e) {
                System.err.println("Bad float value specified: \"" + s +"\""); //NOI18N
            }
        } else if (gratuitous) {
            INCREMENT = 0.02f;
        }
        
        s = System.getProperty ("nb.tabcontrol.fx.timer"); //NOI18N
        if (s != null) {
            try {
                TIMER = Integer.parseInt (s);
            } catch (Exception e) {
                System.err.println("Bad integer value specified: \"" + s + "\""); //NOI18N
            }
        } else if (gratuitous) {
            TIMER = 7;
        }
        if (gratuitous) {
            SYNCHRONOUS_PAINTING = true;
        }
    }
    
    private static final class ForwardingMouseListener implements MouseListener {
        private final Container c;
        public ForwardingMouseListener (Container c) {
            this.c = c;
        }
        public void mousePressed (MouseEvent me) {
            forward (me);
        }
        
        public void mouseReleased (MouseEvent me) {
            forward (me);
        }
        
        public void mouseClicked (MouseEvent me) {
            forward (me);
        }
        
        public void mouseEntered (MouseEvent me) {
            forward (me);
        }
        
        public void mouseExited (MouseEvent me) {
            forward (me);
        }
        
        private void forward (MouseEvent me) {
            MouseListener[] ml = c.getMouseListeners();
            if (ml.length == 0 || me.isConsumed()) {
                return;
            }
            MouseEvent me2 = SwingUtilities.convertMouseEvent(
                (Component) me.getSource(), me, c);
            
            for (int i=0; i < ml.length; i++) {
                switch (me2.getID()) {
                    case MouseEvent.MOUSE_ENTERED :
                        ml[i].mouseEntered(me2);
                        break;
                    case MouseEvent.MOUSE_EXITED :
                        ml[i].mouseExited(me2);
                        break;
                    case MouseEvent.MOUSE_PRESSED :
                        ml[i].mousePressed(me2);
                        break;
                    case MouseEvent.MOUSE_RELEASED :
                        ml[i].mouseReleased(me2);
                        break;
                    case MouseEvent.MOUSE_CLICKED :
                        ml[i].mouseClicked(me2);
                        break;
                    default :
                        assert false;
                }
            }
        }
        
    }
}
