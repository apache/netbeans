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
package org.netbeans.api.visual.widget;

import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.modules.visual.util.GeomUtil;
import org.netbeans.modules.visual.widget.WidgetAccessibleContext;
import org.openide.util.Lookup;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.visual.laf.DefaultLookFeel;
import javax.swing.SwingUtilities;

/**
 * A scene is a tree of small building blocks called widgets and represented by this class.
 * <p>
 * Each widget has a origin location specified relatively to the location its parent widget
 * and placement is specified be its boundary.
 * <p>
 * The widget is also responsible for rendering the region. The widget is an abstract implementation
 * and does not have render anything except borders and background. There are various built-in widget
 * each for a specific visualization. The widget also holds general properties like foreground, opacity, ...
 * that could be reused by the high-level widgets.
 * <p>
 * The widget has a layout assigned. The layout takes care about resolving the placement of children widgets.
 * For that it can use various properties like preferredLocation, preferredBounds, ...
 * When the widget is resolved (placed) than the read only location and bounds properties contains
 * resolved location and boundary of a widget.
 * <p>
 * Each widget has a chain of actions. Actions defined defines a behaviour of the widget. E.g. MoveAction
 * makes the widget moveable. Also there is possible to create/assign other chains that will be activated
 * based on the active tool of a scene.
 * <p>
 * The widget have its state specified by ObjectState class. When the widget state is change,
 * notifyStateChanged is called to notify about it. The state is automatically updated by high-level scenes
 * and actions. Yherefore you can define your own look and feel directly in the that method.
 *
 * Since version 2.6 Widget class implements Accessible interface.
 *
 * @author David Kaspar
 */
// TODO - Should Widget be an abstract class?
public class Widget implements Accessible, Lookup.Provider {

    static final String MESSAGE_NULL_BOUNDS = "Scene.validate was not called after last change. Widget is not validated. See first Q/A at https://netbeans.apache.org/front/main/projects/graph/faq/ page.";

    private static final HashMap<String, WidgetAction.Chain> EMPTY_HASH_MAP = new HashMap<String, WidgetAction.Chain> (0);

    private Scene scene;
    private Widget parentWidget;

    private List<Widget> children;
    private List<Widget> childrenUm;

    private HashMap<Widget,Object> constraints;

    private WidgetAction.Chain actionsChain;
    private HashMap<String, WidgetAction.Chain> toolsActions = EMPTY_HASH_MAP;

    private ArrayList<Widget.Dependency> dependencies;

    private boolean visible = true;

    private boolean opaque;
    private Paint background;
    private Color foreground;
    private Font font;
    private Border border;
    private Layout layout;
    private Point preferredLocation;
    private Dimension minimumSize;
    private Dimension maximumSize;
    private Dimension preferredSize;
    private Rectangle preferredBounds;
    private boolean checkClipping;
    private boolean enabled;
    
    private String foregroundProperty = null;
    private String backgroundProperty = null;
    private String fontProperties = null;
    
    private PropertyChangeListener foregroundListener = null;
    private PropertyChangeListener backgroundListener = null;
    private PropertyChangeListener fontListener = null;
    

    private ObjectState state = ObjectState.createNormal ();

    private Cursor cursor;
    private String toolTipText;
    private AccessibleContext accessibleContext;

    private Point location;
    private Rectangle bounds;
    private Rectangle calculatedPreferredBounds;

    private boolean requiresFullValidation;
    private boolean requiresPartValidation;

    private boolean requiresFullJustification;
    private boolean requiresPartJustification;
    
    /** 
     * The resource table is created lazily.  A resource is only created when 
     * a property is set by using using resources.  
     */
    private ResourceTable resourceTable = null;
    
    /**
     * Creates a new widget which will be used in a specified scene.
     * @param scene the scene where the widget is going to be used
     */
    public Widget (Scene scene) {
        if (scene == null)
            scene = (Scene) this;
        this.scene = scene;
        children = new ArrayList<Widget> ();
        childrenUm = Collections.unmodifiableList (children);

        actionsChain = new WidgetAction.Chain ();

        opaque = false;
        font = null;
        background = (new DefaultLookFeel()).getBackground();//Color.WHITE;
        foreground = (new DefaultLookFeel()).getForeground();//Color.BLACK;
        border = BorderFactory.createEmptyBorder ();
        layout = LayoutFactory.createAbsoluteLayout ();
        preferredLocation = null;
        preferredBounds = null;
        checkClipping = false;
        enabled = true;

        location = new Point ();
        bounds = null;
        calculatedPreferredBounds = null;
        requiresFullValidation = true;
        requiresPartValidation = true;
    }

    /**
     * Returns a scene where the widget is assigned
     * @return the scene
     */
    public final Scene getScene () {
        return scene;
    }

    /**
     * Returns a Graphics2D instance with is assigned to the scene.
     * Usually used in the calculatedClientArea and paintWidget method.
     * @return the Graphics2D instance; null if the scene view is not created or visible yet
     */
    protected Graphics2D getGraphics () {
        return scene.getGraphics ();
    }

    /**
     * Returns a parent widget.
     * @return the parent widget
     */
    public final Widget getParentWidget () {
        return parentWidget;
    }

    /**
     * Returns a list of children widgets.
     * @return the list of children widgets
     */
    public final List<Widget> getChildren () {
        return childrenUm;
    }

    /**
     * Adds a child widget as the last one.
     * @param child the child widget to be added
     */
    public final void addChild (Widget child) {
        addChild (child, null);
    }

    /**
     * Adds a child widget as the last one.
     * @param child the child widget to be added
     * @param constraint the constraint assigned to the child widget
     */
    public final void addChild (Widget child, Object constraint) {
        assert child.parentWidget == null;
        assert SwingUtilities.isEventDispatchThread();
        Widget widget = this;
        while (widget != null) {
            assert widget != child;
            widget = widget.parentWidget;
        }
        children.add(child);
        child.parentWidget = this;
        setChildConstraint (child, constraint);
        child.updateResources(this, true);
        child.revalidate();
        revalidate ();
        scene.dispatchNotifyAdded (child);
    }

    /**
     * Adds a child at a specified index
     * @param index the index (the child is added before the one that is not the index place)
     * @param child the child widget
     */
    public final void addChild (int index, Widget child) {
        addChild (index, child, null);
    }

    /**
     * Adds a child at a specified index
     * @param index the index (the child is added before the one that is not the index place)
     * @param child the child widget
     * @param constraint the constraint assigned to the child widget
     */
    public final void addChild (int index, Widget child, Object constraint) {
        assert child.parentWidget == null;
        assert SwingUtilities.isEventDispatchThread();
        children.add (index, child);
        child.parentWidget = this;
        setChildConstraint (child, constraint);
        child.updateResources(this, true);
        child.revalidate ();
        revalidate ();
        if (accessibleContext instanceof WidgetAccessibleContext)
            ((WidgetAccessibleContext) accessibleContext).notifyChildAdded (this, child);
        scene.dispatchNotifyAdded (child);
    }

    /**
     * Removes a child widget.
     * @param child the child widget
     */
    public final void removeChild (Widget child) {
        assert child.parentWidget == this;
        assert SwingUtilities.isEventDispatchThread();
        setChildConstraint (child, null);
        child.parentWidget = null;
        children.remove (child);
        child.updateResources(this, false);
        child.revalidate ();
        revalidate ();
        if (accessibleContext instanceof WidgetAccessibleContext)
            ((WidgetAccessibleContext) accessibleContext).notifyChildRemoved (this, child);
        scene.dispatchNotifyRemoved (child);
    }

    /**
     * Removes the widget from its parent.
     */
    public final void removeFromParent () {
        if (parentWidget != null)
            parentWidget.removeChild (this);
    }

    /**
     * Removes all children widgets.
     */
    public final void removeChildren () {
        while (! children.isEmpty ())
            removeChild (children.get (0));
    }

    /**
     * Adds all children in a specified list.
     * @param children the list of children widgets
     */
    public final void addChildren (List<? extends Widget> children) {
        for (Widget child : children)
            addChild (child);
    }

    /**
     * Removes all children widget that are in a specified list.
     * @param widgets the list of children widgets to be removed
     */
    public final void removeChildren (List<Widget> widgets) {
        for (Widget widget : widgets)
            removeChild (widget);
    }

    void dispatchNotifyAddedCore () {
        notifyAdded ();
        for (Widget widget : children)
            widget.dispatchNotifyAddedCore ();
    }

    void dispatchNotifyRemovedCore () {
        notifyRemoved ();
        for (Widget widget : children)
            widget.dispatchNotifyRemovedCore ();
    }

    /**
     * This method is called to notify that the view is shown.
     * Note: You must not modify a tree of widgets from within this method.
     * It means: do not call addChild, removeChild and similar methods.
     */
    protected void notifyAdded () {
    }

    /**
     * This method is called to notify that the view is hidden.
     * Note: You must not modify a tree of widgets from within this method.
     * It means: do not call addChild, removeChild and similar methods.
     */
    protected void notifyRemoved () {
    }

    /**
     * Brings the widget to the front. Means: the widget becomes the last child in the list of children of the parent widget.
     */
    public final void bringToFront () {
        if (parentWidget == null)
            return;
        List<Widget> children = parentWidget.children;
        int i = children.indexOf (this);
        if (i < 0)
            return;
        children.remove (i);
        children.add (children.size (), this);
        revalidate ();
        parentWidget.revalidate ();
    }

    /**
     * Brings the widget to the back. Means: the widget becomes the first child in the list of children of the parent widget.
     */
    public final void bringToBack () {
        if (parentWidget == null)
            return;
        List<Widget> children = parentWidget.children;
        int i = children.indexOf (this);
        if (i <= 0)
            return;
        children.remove (i);
        children.add (0, this);
        revalidate ();
        parentWidget.revalidate ();
    }

    /**
     * Returns constraint assigned to a specified child widget.
     * @param child the child widget
     * @return the constraint
     */
    public final Object getChildConstraint (Widget child) {
        return constraints != null ? constraints.get (child) : null;
    }

    /**
     * Assigns a constraint to a child widget.
     * @param child the child widget
     * @param constraint the constraint
     */
    public final void setChildConstraint (Widget child, Object constraint) {
        assert children.contains (child);
        if (constraint == null) {
            if (constraints != null)
                constraints.remove (child);
            return;
        }
        if (constraints == null)
            constraints = new HashMap<Widget, Object> ();
        constraints.put (child, constraint);
    }

    /**
     * Returns whether the widget is visible.
     * @return true if the widget is visible
     */
    public final boolean isVisible () {
        return visible;
    }

    /**
     * Sets whether the widget is visible.
     * @param visible if true, then the widget is visible
     */
    public final void setVisible (boolean visible) {
        this.visible = visible;
        revalidate ();
    }

    /**
     * Returns whether the widget is enabled.
     * If the widget is disabled then any event is processed by assigned actions.
     * @return true if the widget is enabled.
     */
    public final boolean isEnabled () {
        return enabled;
    }

    /**
     * Sets whether the widget is enabled.
     * If the widget is disabled then any event is processed by assigned actions.
     * @param enabled if true, then the widget is enabled
     */
    public final void setEnabled (boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns a default action chain.
     * @return the default action chain.
     */
    public final WidgetAction.Chain getActions () {
        return actionsChain;
    }

    /**
     * Returns already created action chain for a specified tool.
     * @param tool the tool
     * @return the action chain; null, if no chain for the tool exists
     */
    public final WidgetAction.Chain getActions (String tool) {
        return toolsActions.get (tool);
    }

    /**
     * Creates and returns an action chain for a specified tool.
     * @param tool the tool
     * @return the action chain
     */
    public final WidgetAction.Chain createActions (String tool) {
        if (tool == null)
            return actionsChain;
        if (toolsActions == EMPTY_HASH_MAP) {
            toolsActions = new HashMap<String, WidgetAction.Chain> ();
            toolsActions.put (null, actionsChain);
        }
        WidgetAction.Chain chain = toolsActions.get (tool);
        if (chain == null) {
            chain = new WidgetAction.Chain ();
            toolsActions.put (tool, chain);
        }
        return chain;
    }

    /**
     * Returns a lookup of the widget.
     * @return the lookup
     */
    @Override
    public Lookup getLookup () {
        return Lookup.EMPTY;
    }

    /**
     * Adds a dependency listener which is notified when the widget placement or boundary is going to be changed or similar thing happens to its parent widget.
     * @param dependency the dependency listener
     */
    public final void addDependency (Widget.Dependency dependency) {
        if (dependencies == null)
            dependencies = new ArrayList<Widget.Dependency> ();
        dependencies.add (dependency);
    }

    /**
     * Removes a dependency listener.
     * @param dependency the dependency listener
     */
    public final void removeDependency (Widget.Dependency dependency) {
        if (dependencies == null)
            return;
        dependencies.remove (dependency);
    }

    /**
     * Returns a collection of registered dependencies.
     * @return the unmodifiable collection of dependencies
     * @since 2.6
     */
    public final Collection<Dependency> getDependencies () {
        return dependencies != null ? Collections.unmodifiableCollection (dependencies) : Collections.<Dependency>emptyList ();
    }

    /**
     * Returns whether the widget is opaque.
     * @return true, if the widget is opaque
     */
    public final boolean isOpaque () {
        return opaque;
    }

    /**
     * Sets the widget opacity.
     * @param opaque if true, then the widget is opaque
     */
    public final void setOpaque (boolean opaque) {
        this.opaque = opaque;
        repaint ();
    }

    /**
     * Returns the widget background paint.
     * @return the background paint
     */
    public final Paint getBackground () {
        return background != null ? background : parentWidget != null ? parentWidget.getBackground () : null;
    }

    /**
     * Sets the widget background paint.
     * @param background the background paint
     */
    public final void setBackground (Paint background) {
        this.background = background;
                
        // Since we have a new color set manually, we no longer want to update
        // when the property changes.
        if(backgroundListener != null)
        {
            ResourceTable resourceTable = getResourceTable ();
            if (resourceTable != null)
                resourceTable.removePropertyChangeListener(backgroundProperty, backgroundListener);
            backgroundListener = null;
        }
        
        repaint ();
    }
    
    /**
     * Sets the widget background color to be based on a resource property.
     * @param property the background property name
     */
    public final void setBackgroundFromResource (String property) {
        
        ResourceTable table = getResourceTable();
        String oldPropertyName = backgroundProperty;
        backgroundProperty = property;
        
        if(table != null)
        {
            if((oldPropertyName != null) && (oldPropertyName.length() > 0))
            {
                // Maybe the property name has changed.  Therefore, remove the old 
                // listener.
                table.removePropertyChangeListener(oldPropertyName, backgroundListener);
            }
            
            Object value = table.getProperty(property);
            if(value instanceof Paint)
            {   
                background = (Paint)value;
            }
            
            backgroundListener = new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent event)
                {
                    if(event.getNewValue() instanceof Paint)
                    {   
                        background = (Paint)event.getNewValue();
                        notifyBackgroundChanged(background);
                    }
                }
            };
            table.addPropertyChangeListener(property, backgroundListener);
        }
        repaint ();
    }
    
    protected void notifyBackgroundChanged(Paint paint) {
    }
    
    /**
     * Returns the widget foreground color.
     * @return the foreground color
     */
    public final Color getForeground () {
        return foreground != null ? foreground : parentWidget != null ? parentWidget.getForeground () : null;
    }

    /**
     * Sets the widget foreground color.
     * @param foreground the foreground color
     */
    public final void setForeground (Color foreground) {
        this.foreground = foreground;

        // Since we have a new color set manually, we no longer want to update
        // when the property changes.
        if (foregroundListener != null) {
            ResourceTable resourceTable = getResourceTable();
            if (resourceTable != null) {
                resourceTable.removePropertyChangeListener(foregroundProperty, foregroundListener);
            }
            foregroundListener = null;
        }
        repaint();
    }

    /**
     * Sets the widget foreground color to be based on a resource property.
     * @param property the foreground property name
     */
    public final void setForegroundFromResource (String property) {

        String oldPropertyName = foregroundProperty;

        foregroundProperty = property;

        ResourceTable table = getResourceTable();
        if(table != null)
        {
            if((oldPropertyName != null) && (oldPropertyName.length() > 0))
            {
                // Maybe the property name has changed.  Therefore, remove the old
                // listener.
                table.removePropertyChangeListener(oldPropertyName, foregroundListener);
            }

            Object value = table.getProperty(property);
            if(value instanceof Color)
            {
                this.foreground = (Color)value;
            }
            
            if(foregroundListener == null)
            {
                foregroundListener = new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent event)
                    {
                        if(event.getNewValue() instanceof Color)
                        {   
                            foreground = (Color)event.getNewValue();
                            notifyForegroundChanged(foreground);
                        }
                    }
                };
            }
            table.addPropertyChangeListener(property, foregroundListener);
        }
        repaint ();
    }
    
    protected void notifyForegroundChanged(Color newColor) {
    }
    
    /**
     * Returns the font assigned to the widget. If not set yet, then it returns the font of its parent widget.
     * @return the font
     */
    public final Font getFont () {
                
        Font retVal = font;
        
        if((font == null) && (parentWidget != null))
        {
            retVal = parentWidget.getFont ();
        }
        
        return retVal;
    }

    /**
     * Sets the widget font.
     * @param font the font; if null, then widget unassignes its font.
     */
    public final void setFont (Font font) {
        this.font = font;

        // Since we have a new color set manually, we no longer want to update
        // when the property changes.
        if (fontListener != null) {
            ResourceTable resourceTable = getResourceTable();
            if (resourceTable != null) {
                resourceTable.removePropertyChangeListener(fontProperties, fontListener);
            }
            fontListener = null;
        }

        // Notify others about the change.
        notifyFontChanged(font);

        revalidate();
    }
    /**
     * Sets the widget background color to be based on a resource property.
     * @param property the foreground property name
     */
    public final void setFontFromResource (String property) 
    {
        String oldPropertyName = fontProperties;
        fontProperties = property;
            
        ResourceTable table = getResourceTable();
        if(table != null)
        {
            if((oldPropertyName != null) && (oldPropertyName.length() > 0))
            {
                // Maybe the property name has changed.  Therefore, remove the old 
                // listener.
                table.removePropertyChangeListener(oldPropertyName, fontListener);
            }
            
            Object value = table.getProperty(property);
            if(value instanceof Font)
            {
                this.font = (Font)value;
            }
            
            fontListener = new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent event)
                {
                    Object newValue = event.getNewValue();
                    if(newValue instanceof Font)
                    {
                        if(parentWidget != null)
                        {
                            font = (Font)newValue;
                            notifyFontChanged(font);
                        }
                    }
                }
            };
            
            table.addPropertyChangeListener(property, fontListener);
        }
        repaint ();
    }
    
    protected final void updateResources(Widget parent, boolean added)
    {
        if(added == true)
        {
            ResourceTable table = connectResourceTable();
            if(table != null)
            {
                if((foregroundProperty != null) && (foregroundProperty.length() > 0))
                {
                    setForegroundFromResource(foregroundProperty);
                }

                if((fontProperties != null) && (fontProperties.length() > 0))
                {   
                    setFontFromResource(fontProperties);
                }

                if((backgroundProperty != null) && (backgroundProperty.length() > 0))
                {
                    setBackgroundFromResource(backgroundProperty);
                }
            }
        }
        else
        {
            ResourceTable table = resourceTable;
            if(table == null)
            {
                table = parent.getResourceTable();
            }
            
            if(table != null)
            {
                if(foregroundListener != null)
                {
                    table.removePropertyChangeListener(foregroundListener);
                }

                if(fontListener != null)
                {
                    table.removePropertyChangeListener(fontListener);
                }

                if(backgroundListener != null)
                {
                    table.removePropertyChangeListener(backgroundListener);
                }
            }
            
            disconnectResourceTable();
        }
        
        // Now notify the children to also update, because the parent structure
        // has changed.
        for(Widget child : getChildren())
        {
            child.updateResources(parent, added);
        }
        
        revalidate ();
    }
    
    protected void notifyFontChanged(Font font) {
    }
    
    /**
     * Returns the border of the widget.
     * @return the border
     */
    public final Border getBorder () {
        return border;
    }

    /**
     * Sets the border of the widget.
     * @param border the border
     */
    public final void setBorder (Border border) {
        assert border != null;
        boolean repaintOnly = this.border.getInsets ().equals (border.getInsets ());
        this.border = border;
        revalidate (repaintOnly);
    }

    /**
     * Sets the Swing layout as the border of the widget.
     * @param swingBorder the Swing border
     */
    public final void setBorder (javax.swing.border.Border swingBorder) {
        assert swingBorder != null;
        setBorder (BorderFactory.createSwingBorder (scene, swingBorder));
    }

    /**
     * Returns the layout of the widget.
     * @return the layout
     */
    public final Layout getLayout () {
        return layout;
    }

    /**
     * Sets the layout of the widget.
     * @param layout the layout
     */
    public final void setLayout (Layout layout) {
        this.layout = layout;
        revalidate ();
    }

    /**
     * Returns a minimum size of the widget.
     * @return the minimum size; if null, then no minumum size are set.
     */
    public final Dimension getMinimumSize () {
        return minimumSize != null ? new Dimension (minimumSize) : null;
    }

    /**
     * Sets a minumum size of the widget
     * @param minimumSize the minimum size; if null, then minimum size are unset.
     */
    public final void setMinimumSize (Dimension minimumSize) {
        if (GeomUtil.equals (this.minimumSize, minimumSize))
            return;
        this.minimumSize = minimumSize;
        revalidate ();
    }

    /**
     * Returns a maximum size of the widget.
     * @return the maximum size; if null, then no maximum size are set.
     */
    public final Dimension getMaximumSize () {
        return maximumSize != null ? new Dimension (maximumSize) : null;
    }

    /**
     * Sets a maximum size of the widget
     * @param maximumSize the maximum size; if null, then maximum size are unset.
     */
    public final void setMaximumSize (Dimension maximumSize) {
        if (GeomUtil.equals (this.maximumSize, maximumSize))
            return;
        this.maximumSize = maximumSize;
        revalidate ();
    }

    /**
     * Returns a preferred size of the widget.
     * @return the preferred size; if null, then no preferred size are set.
     */
    public final Dimension getPreferredSize () {
        return preferredSize != null ? new Dimension (preferredSize) : null;
    }

    /**
     * Sets a preferred size of the widget
     * @param preferredSize the preferred size; if null, then preferred size are unset.
     */
    public final void setPreferredSize (Dimension preferredSize) {
        if (GeomUtil.equals (this.preferredSize, preferredSize))
            return;
        this.preferredSize = preferredSize;
        revalidate ();
    }

    /**
     * Returns a preferred location of the widget.
     * @return the preferred location; if null, then no preferred location is set
     */
    public final Point getPreferredLocation () {
        return preferredLocation != null ? new Point (preferredLocation) : null;
    }

    /**
     * Sets a preferred location of the widget.
     * @param preferredLocation the preferred location; if null, then the preferred location is unset
     */
    public final void setPreferredLocation (Point preferredLocation) {
        if (GeomUtil.equals (this.preferredLocation, preferredLocation))
            return;
        this.preferredLocation = preferredLocation;
        revalidate ();
    }

    /**
     * Returns whether a preferred bounds are set.
     * @return true, if preferred bounds are set
     */
    public final boolean isPreferredBoundsSet () {
        return preferredBounds != null;
    }

    /**
     * Returns a preferred bounds relatively to the location of the widget. If no preferred bounds are set, then it returns a preferred bounds
     * that are calculated from the calculateClientArea method of this widget and location and bounds of the children widgets.
     * This calculated bounds are processed by the minimum and maximum bounds too.
     * <p>
     * This method can be called after child widgets are layed out which is assured in method calls of the <code>Layout</code> interface implementation.
     * If preferred bounds are set (check it using <code>isPreferredBoundsSet</code> method), you can call this method at any time.
     * @return the preferred bounds
     */
    public final Rectangle getPreferredBounds () {
        Rectangle rect;
        if (isPreferredBoundsSet ())
            rect = new Rectangle (preferredBounds);
        else {
            if (calculatedPreferredBounds == null)
                calculatedPreferredBounds = calculatePreferredBounds ();
            rect = new Rectangle (calculatedPreferredBounds);
            if (preferredSize != null) {
                rect.width = preferredSize.width;
                rect.height = preferredSize.height;
            }
        }
        if (minimumSize != null) {
            if (rect.width < minimumSize.width)
                rect.width = minimumSize.width;
            if (rect.height < minimumSize.height)
                rect.height = minimumSize.height;
        }
        if (maximumSize != null) {
            if (rect.width > maximumSize.width)
                rect.width = maximumSize.width;
            if (rect.height > maximumSize.height)
                rect.height = maximumSize.height;
        }
        return rect;
    }

    private Rectangle calculatePreferredBounds () {
        Insets insets = border.getInsets ();
        Rectangle clientArea = calculateClientArea ();
        for (Widget child : children) {
            if (! child.isVisible ())
                continue;
            Point location = child.getLocation ();
            Rectangle bounds = child.getBounds ();
            bounds.translate (location.x, location.y);
            clientArea.add (bounds);
        }
        clientArea.x -= insets.left;
        clientArea.y -= insets.top;
        clientArea.width += insets.left + insets.right;
        clientArea.height += insets.top + insets.bottom;
        return clientArea;
    }

    /**
     * Called to calculate the client area required by the widget without the children widgets.
     * @return the calculated client area
     */
    protected Rectangle calculateClientArea () {
        return new Rectangle ();
    }

    /**
     * Sets a preferred bounds that are specified relatively to the location of the widget.
     * @param preferredBounds the preferred bounds; if null, then the preferred bounds are unset
     */
    public final void setPreferredBounds (Rectangle preferredBounds) {
        if (GeomUtil.equals (this.preferredBounds, preferredBounds))
            return;
        this.preferredBounds = preferredBounds;
        revalidate ();
    }

    /**
     * Returns whether clipping is used in the widget.
     * @return true, if the check clipping is used
     */
    public final boolean isCheckClipping () {
        return checkClipping;
    }

    /**
     * Sets a clipping for the widget.
     * @param checkClipping if true, then the clipping is used
     */
    public final void setCheckClipping (boolean checkClipping) {
        this.checkClipping = checkClipping;
        repaint ();
    }

    /**
     * Returns a mouse cursor for a specified local location in the widget.
     * @param localLocation the local location
     * @return the mouse cursor; default implementation return value of cursor property.
     * @since 2.3
     */
    protected Cursor getCursorAt (Point localLocation) {
        return getCursor ();
    }

    /**
     * Returns a mouse cursor for the widget.
     * @return the mouse cursor
     */
    public final Cursor getCursor () {
        return cursor;
    }

    /**
     * Sets a cursor for the widget.
     * @param cursor the mouse cursor; if null, the cursor is unset
     */
    public final void setCursor (Cursor cursor) {
        this.cursor = cursor;
    }

    /**
     * Returns a tool-tip text of the widget.
     * @return the tool-tip text
     */
    public final String getToolTipText () {
        return toolTipText;
    }

    /**
     * Sets a tool-tip of the widget.
     * @param toolTipText the tool tip text
     */
    public final void setToolTipText (String toolTipText) {
        this.toolTipText = toolTipText;
    }

    /**
     * Returns an accessible context of the widget.
     * @return the accessible context
     */
    public final AccessibleContext getAccessibleContext () {
        if (accessibleContext == null)
            accessibleContext = new WidgetAccessibleContext (this);
        return accessibleContext;
    }

    /**
     * Sets a accessible context of the widget.
     * @param accessibleContext the accessible context
     */
    public final void setAccessibleContext (AccessibleContext accessibleContext) {
        this.accessibleContext = accessibleContext;
    }

    /**
     * Returns a state of the widget.
     * @return the widget state
     */
    public final ObjectState getState () {
        return state;
    }

    /**
     * Sets a state of the widget.
     * @param state the widget state
     */
    public final void setState (ObjectState state) {
        ObjectState previousState = this.state;
        this.state = state;
        notifyStateChanged (previousState, state);
    }

    /**
     * Called to notify about the change of the widget state.
     * @param previousState the previous state
     * @param state the new state
     */
    protected void notifyStateChanged (ObjectState previousState, ObjectState state) {
    }

    /**
     * Converts a location in the local coordination system to the scene coordination system.
     * @param localLocation the local location
     * @return the scene location
     */
    public final Point convertLocalToScene (Point localLocation) {
        Point sceneLocation = new Point (localLocation);
        Widget widget = this;
        while (widget != null) {
            if (widget == scene)
                break;
            Point location = widget.getLocation ();
            sceneLocation.x += location.x;
            sceneLocation.y += location.y;
            widget = widget.getParentWidget ();
        }
        return sceneLocation;
    }

    /**
     * Converts a rectangle in the local coordination system to the scene coordination system.
     * @param localRectangle the local rectangle
     * @return the scene rectangle
     */
    public final @NonNull Rectangle convertLocalToScene(@NonNull Rectangle localRectangle) {
        Rectangle sceneRectangle = new Rectangle (localRectangle);
        Widget widget = this;
        while (widget != null) {
            if (widget == scene)
                break;
            Point location = widget.getLocation ();
            sceneRectangle.x += location.x;
            sceneRectangle.y += location.y;
            widget = widget.getParentWidget ();
        }
        return sceneRectangle;
    }

    /**
     * Converts a location in the scene coordination system to the local coordination system.
     * @param sceneLocation the scene location
     * @return the local location
     */
    public final Point convertSceneToLocal (Point sceneLocation) {
        Point localLocation = new Point (sceneLocation);
        Widget widget = this;
        while (widget != null) {
            if (widget == scene)
                break;
            Point location = widget.getLocation ();
            localLocation.x -= location.x;
            localLocation.y -= location.y;
            widget = widget.getParentWidget ();
        }
        return localLocation;
    }

    /**
     * Converts a rectangle in the scene coordination system to the local coordination system.
     * @param sceneRectangle the scene rectangle
     * @return the local rectangle
     */
    public final Rectangle convertSceneToLocal (Rectangle sceneRectangle) {
        Rectangle localRectangle = new Rectangle (sceneRectangle);
        Widget widget = this;
        while (widget != null) {
            if (widget == scene)
                break;
            Point location = widget.getLocation ();
            localRectangle.x -= location.x;
            localRectangle.y -= location.y;
            widget = widget.getParentWidget ();
        }
        return localRectangle;
    }

    /**
     * Returns the resolved location of the widget. The location is specified relatively to the location of the parent widget.
     * <p>
     * The location is resolved/set by calling <code>resolveBounds</code> method which should be called from <code>Layout</code> interface implementation only.
     * Therefore the corrent value is available only after the scene is validated (<code>SceneListener.sceneValidated</code> method).
     * Before validation a previous/obsolete or <code>[0,0]</code> value could be returned.
     * See <strong>Layout</strong> section in documentation.
     * @return the location in the local coordination system of the parent widget
     */
    public final Point getLocation () {
        return location != null ? new Point (location) : null;
    }

    /**
     * Returns the resolved bounds of the widget. The bounds are specified relatively to the location of the widget.
     * <p>
     * The location is resolved/set by calling <code>resolveBounds</code> method which should be called from <code>Layout</code> interface implementation only.
     * Therefore the corrent value is available only after the scene is validated (<code>SceneListener.sceneValidated</code> method).
     * Before validation a previous/obsolete or <code>null</code> value could be returned.
     * See <strong>Layout</strong> section in documentation.
     * @return the bounds in local coordination system
     */
    public final @CheckForNull Rectangle getBounds() {
        return bounds != null ? new Rectangle (bounds) : null;
    }

    /**
     * Sets resolved location and bounds of the widget
     * This method is usually called from implementations of <code>Layout</code> interface.
     * @param location the resolved location; if null then [0,0] point is used instead
     * @param bounds the resolved bounds; if null then the preferred bounds are used instead
     */
    public final void resolveBounds (Point location, Rectangle bounds) {
        this.location = location != null ? location : new Point ();
        this.bounds = bounds != null ? new Rectangle (bounds) : new Rectangle (getPreferredBounds ());
    }

    /**
     * Returns a client area of the widget.
     * @return the client area
     */
    public final Rectangle getClientArea () {
        Rectangle bounds = getBounds ();
        if (bounds == null)
            return null;
        Insets insets = getBorder ().getInsets ();
        return new Rectangle (bounds.x + insets.left, bounds.y + insets.top, bounds.width - insets.left - insets.right, bounds.height - insets.top - insets.bottom);
    }

    /**
     * Called to whether a particular location in local coordination system is controlled (otionally also painted) by the widget.
     * @param localLocation the local location
     * @return true, if the location belong to the widget
     */
    public boolean isHitAt (Point localLocation) {
        return visible  &&  getBounds ().contains (localLocation);
    }

    /**
     * Schedules the widget for repainting.
     */
    // NOTE - has to be called before a change is set into the widget when the change immediatelly affects calculation of the local/scene location/boundary (means any property used in convertLocalToScene) because repaint/revalidate needs to calculate old scene boundaries
    public final void repaint () {
        scene.revalidateWidget (this);
    }

    /**
     * Returns true if the widget is validated (is not scheduled to revalidation).
     * @return true, if is validated
     */
    public boolean isValidated () {
        return ! requiresPartValidation;
    }

    /**
     * Schedules the widget to repaint or revalidation.
     * @param repaintOnly if true, then the widget is scheduled for repainting only;
     *            if false, then widget is scheduled for revalidation (the Scene.validate method has to be called after all changes to invoke validation)
     */
    // NOTE - has to be called before a change is set into the widget when the change affects the local/scene location/boundary because repaint/revalidate needs to calculate old scene boundaries
    public final void revalidate (boolean repaintOnly) {
        if (repaintOnly)
            repaint ();
        else
            revalidate ();
    }

    /**
     * Schedules the widget for revalidation.
     * The Scene.validate method has to be called after all changes to invoke validation. In some cases it is invoked automatically.
     */
    // NOTE - has to be called before a change is set into the widget when the change affects the local/scene location/boundary because repaint/revalidate needs to calculate old scene boundaries
    public final void revalidate () {
        requiresFullValidation = true;
        revalidateUptoRoot ();
    }

    /**
     * Returns whether whole area of the widget has to be repainted after the validation of the widget.
     * Used be LayerWidget for performance optiomalization.
     * @return true, if requires; false, if does not require
     */
    protected boolean isRepaintRequiredForRevalidating () {
        return true;
    }

    private void revalidateUptoRoot () {
        if (requiresPartValidation)
            return;
        if (isRepaintRequiredForRevalidating ())
            repaint ();
        calculatedPreferredBounds = null;
        requiresPartValidation = true;
        if (parentWidget != null)
            parentWidget.revalidateUptoRoot ();
        if (dependencies != null)
            for (Dependency dependency : dependencies)
                dependency.revalidateDependency ();
    }

    void layout (boolean fullValidation) {
        boolean childFullValidation = fullValidation || requiresFullValidation;
        for (Widget widget : children)
            widget.layout (childFullValidation);

        if (fullValidation)
            if (dependencies != null)
                for (Dependency dependency : dependencies)
                    dependency.revalidateDependency ();

        if (childFullValidation  ||  requiresPartValidation) {
            layout.layout (this);
            if (layout.requiresJustification (this)) {
                rejustify ();
            }
        }

        requiresFullValidation = false;
        requiresPartValidation = false;
    }

    private void rejustify () {
        requiresFullJustification = true;
        rejustifyUptoRoot ();
    }

    private void rejustifyUptoRoot () {
        if (requiresPartJustification)
            return;
        requiresPartJustification = true;
        if (parentWidget != null)
            parentWidget.rejustifyUptoRoot ();
    }

    final void justify () {
        if (requiresFullJustification) {
            layout.justify (this);
            if (layout.requiresJustification (this))
                for (Widget child : children)
                    child.rejustify ();
        }

        for (Widget widget : children)
            if (widget.requiresPartJustification)
                widget.justify ();

        requiresFullJustification = false;
        requiresPartJustification = false;
    }

    /**
     * Paints the widget with its children widget into the Graphics2D instance acquired from Scene.getGraphics method.
     */
    public final void paint () {
        if (! visible)
            return;

        assert bounds != null : MESSAGE_NULL_BOUNDS; // NOI18N
        Graphics2D gr = scene.getGraphics ();

        AffineTransform previousTransform = gr.getTransform();
        gr.translate (location.x, location.y);

        Shape tempClip = null;
        if (checkClipping) {
            tempClip = gr.getClip ();
            gr.clip (bounds);
        }

        Rectangle clipBounds;
        if (! checkClipping  ||  (clipBounds = gr.getClipBounds ()) == null   ||  bounds.intersects (clipBounds)) {
            if (opaque)
                paintBackground ();

            paintBorder ();

            if (checkClipping) {
                Insets insets = border.getInsets ();
                gr.clipRect (bounds.x + insets.left, bounds.y + insets.top, bounds.width - insets.left - insets.right, bounds.height - insets.top - insets.bottom);
            }

            paintWidget ();
            paintChildren ();
        }

        if (checkClipping)
            gr.setClip (tempClip);

        gr.setTransform(previousTransform);
    }

    /**
     * Called to paint the widget background itself only using the Graphics2D instance acquired from Scene.getGraphics method.
     */
    protected void paintBackground () {
        Graphics2D gr = scene.getGraphics();
        Insets insets = border.getInsets ();

        gr.setPaint (background);
        if (border.isOpaque ())
            gr.fillRect (bounds.x, bounds.y, bounds.width, bounds.height);
        else
            gr.fillRect (bounds.x + insets.left, bounds.y + insets.top, bounds.width - insets.left - insets.right, bounds.height - insets.top - insets.bottom);
    }

    /**
     * Called to paint the widget border itself only using the Graphics2D instance acquired from Scene.getGraphics method.
     * @since 2.1
     */
    protected void paintBorder () {
        border.paint (getGraphics (), new Rectangle (bounds));
    }

    /**
     * Called to paint the widget itself only using the Graphics2D instance acquired from Scene.getGraphics method.
     * Do not call methods which modify state of the widget (all methods which calls revalidate like setFont(),
     * setVisible(), setLayout(),...). It can cause another repaint and result in infinite paint cycle.
     */
    protected void paintWidget () {
    }

    /**
     * Called to paint the children widgets only using the Graphics2D instance acquired from Scene.getGraphics method.
     */
    protected void paintChildren () {
        if (checkClipping) {
            Rectangle clipBounds = scene.getGraphics ().getClipBounds ();
            for (Widget child : children) {
                Point location = child.getLocation ();
                Rectangle bounds = child.getBounds ();
                bounds.translate (location.x, location.y);
                if (clipBounds == null  ||  bounds.intersects (clipBounds))
                    child.paint ();
            }
        } else
            for (Widget child : children)
                child.paint ();
    }

    /**
     * Returns the object hash code.
     * @return the object hash code
     */
    @Override
    public final int hashCode () {
        return super.hashCode ();
    }

    /**
     * Returns whether a specified object is the same as the widget.
     * @param object the object
     * @return true if the object reference is the same as the widget
     */
    @Override
    public final boolean equals (Object object) {
        return this == object;
    }
    
    /**
     * Retreives the widgets resource table.  If the widgets resource table is 
     * not set then the widgets parent resource table it retrieved.
     * 
     * @return The resource table.
     */
    public ResourceTable getResourceTable() {

        ResourceTable retVal = resourceTable;

        if ((retVal == null) && (getParentWidget() != null)) {
            retVal = getParentWidget().getResourceTable();
        }

        return retVal;
    }

    private ResourceTable connectResourceTable() {
        if ((resourceTable != null) && (getParentWidget() != null)) {
            ResourceTable parentTable = getParentWidget().getResourceTable();
            resourceTable.setParentTable(parentTable);
        }

        return getResourceTable();
    }
    
    private void disconnectResourceTable() {
        if (resourceTable != null) {
            resourceTable.removeParent();
        }
    }
    
    /**
     * Sets the resource table.
     * 
     * @param table The widgets resource table.
     */
    public void setResourceTable(ResourceTable table) {
        resourceTable = table;
    }
    
    /**
     * The dependency listener which is used for notifying dependent widgets, anchor, ...
     * that the widget (or one of its parent widget) location or bounds are going to or were changed.
     */
    public interface Dependency {

        /**
         * Called when the widget (or one of its parent widget) location or bounds are going to or were changed.
         */
        public void revalidateDependency ();

    }

}
