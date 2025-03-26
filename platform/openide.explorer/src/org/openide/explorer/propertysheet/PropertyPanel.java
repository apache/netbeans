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
package org.openide.explorer.propertysheet;

import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;

import java.beans.FeatureDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.openide.explorer.PropertyPanelBridge;

import org.netbeans.modules.openide.explorer.TTVEnvBridge;
import org.openide.util.Utilities;


/** <p>PropertyPanel is a generic GUI component for displaying and editing a JavaBeans&trade;
 * property or any compatible getter/setter pair for which there is a property editor
 * available, in accordance with the JavaBeans specification.  It makes it possible to
 * instantiate an appropriate GUI component for a property and provides the plumbing
 * between user interation with the gui component and calls to the getter/setter pair
 * to update the value.</p>
 *
 * <p>The simplest way to use PropertyPanel is by driving it from an instance of
 * <code>PropertySupport.Reflection</code>.  To do that, simply pass the name of the
 * property and an object with a getter/setter pair matching that property to the
 * PropertySupport.Reflection's constructor, and pass the resulting instance of
 * PropertySupport.Reflection to the PropertyPanel constructor.</p>
 *
 * <p>A more efficient approach is to implement Node.Property or pass an existing Node.Property
 * object to the PropertyPanel's constructor or PropertyPanel.setProperty - thus
 * bypassing the use of reflection to locate the getter and setter.</p>
 *
 * <p><b>A note on uses of Node.Property and PropertyModel</b>:  PropertyPanel was
 * originally designed to work with instances of PropertyModel, and has since been
 * rewritten to be driven by instances of Node.Property.  The main reason for this
 * is simplification - there is considerable overlap between PropertyModel and
 * Node.Property; particularly, DefaultPropertyModel and PropertySupport.Reflection
 * effectively are two ways of doing exactly the same thing.</p>
 *
 * <p>Use of PropertyModel is still supported, but discouraged except under special
 * circumstances.  The one significant difference between <code>Node.Property</code>
 * and PropertyModel is that PropertyModel permits listening for changes.</p>
 * <p>It is generally accepted that GUI components whose contents unexpectedly change
 * due to events beyond their control does not tend to lead to quality, usable user
 * interfaces.  However, there are cases where a UI will, for example, contain several
 * components and modification to one should immediately be reflected in the other.
 * For such a case, use of PropertyModel is still supported.  For other cases,
 * it makes more sense to use <code>BeanNode</code> and for the designer of the UI
 * to make a design choice as to how to handle (if at all) unexpected changes happening to
 * properties it is displaying.  If all you need to do is display or edit a
 * property, use one of the constructors that takes a Node.Property object or
 * use <code>setProperty</code>.  PropertyModel will be deprecated at some point
 * in the future, when a suitable replacement more consistent with
 * <code>Node.Property</code> is created.</p>
 *
 * PropertyModel and displays an editor component for it.
 * @author Jaroslav Tulach, Petr Hamernik, Jan Jancura, David Strupl, Tim Boudreau
 */
public class PropertyPanel extends JComponent implements javax.accessibility.Accessible {
    /** Constant defining a preference for rendering the value.
     * Value should be displayed in read-only mode.
     */
    public static final int PREF_READ_ONLY = 0x0001;

    /** Constant defining a preference for rendering the value.
     * Value should be displayed in custom editor.
     */
    public static final int PREF_CUSTOM_EDITOR = 0x0002;

    /** Constant defining a preference for rendering the value.
     * Value should be displayed in editor only.
     */
    public static final int PREF_INPUT_STATE = 0x0004;

    /** Constant defining a preference for a borderless UI suitable for
     * use in a table */
    public static final int PREF_TABLEUI = 0x0008;

    /** Name of the 'preferences' property. */
    public static final String PROP_PREFERENCES = "preferences"; // NOI18N

    /** Name of the 'model' property. */
    public static final String PROP_MODEL = "model"; // NOI18N

    /** Name of the read-only property 'propertyEditor'.
     * @deprecated - the property editor is re-fetched from the underlying
     * property object as needed.  It is up to the property object to
     * cache or not cache the property editor.  This property will no longer
     * be fired. */
    public static final @Deprecated String PROP_PROPERTY_EDITOR = "propertyEditor"; // NOI18N

    /** Name of property 'state' that describes the state of the embeded PropertyEditor.
    * @see PropertyEnv#getState
    * @since 2.20  */
    public static final String PROP_STATE = PropertyEnv.PROP_STATE;

    /** Holds value of property preferences. */
    private int preferences;

    /** Holds value of property model. */
    private PropertyModel model;

    /**
     * If this is <code>true</code> the changes made in the property editor
     * are immediately propagated to the value of the property
     * (to the property model).  */
    private boolean changeImmediate = true;

    /** The inner component, either a custom property editor, an
     * InplaceEditor's component or null, depending on the mode and state */
    Component inner = null;

    /** Listener that will listen for changes in model, editor, env */
    private Listener listener = null;

    /** The property which will drive the PropertyPanel */
    private Node.Property prop;

    /** Flag to avoid an endless loop when setProperty is called by setModel */
    private boolean settingModel = false;
    private boolean initializing = false;
    private PropertyDisplayer displayer = null;
    Object[] beans = null;
    private ReusablePropertyEnv reusableEnv = new ReusablePropertyEnv();
    private ReusablePropertyModel reusableModel = new ReusablePropertyModel(reusableEnv);
    private boolean ignoreCommit;

    /** Creates new PropertyPanel backed by a dummy property  */
    public PropertyPanel() {
        this(ModelProperty.toProperty(null), 0, null);
    }

    /**
     * Creates new PropertyPanel with DefaultPropertyModel
     * @param preferences the preferences that affect how this propertypanel
     * will operate
     * @param bean The instance of bean
     * @param propertyName The name of the property to be displayed
     */
    public PropertyPanel(Object bean, String propertyName, int preferences) {
        //XXX inefficient, get DefaultPropertyModel out of the loop
        this(
            ModelProperty.toProperty(new DefaultPropertyModel(bean, propertyName)), preferences,
            
        //XXX can probably subst null for below
        new DefaultPropertyModel(bean, propertyName)
        );
    }

    /** Creates a new PropertyPanel.  While not quite deprecated, do not
     * use this constructor if your intention is to display a Node.Property
     * object; use the constructor that takes a Node.Property object directly
     * instead.
     * @param model The model to display
     * @see org.openide.explorer.propertysheet.PropertyModel
     */
    public PropertyPanel(PropertyModel model, int preferences) {
        this(null, preferences, model);
    }

    /**
     * Create a new property panel for the specified property with the
     * specified preferences.
     * @param p
     * @param preferences
     */
    public PropertyPanel(Node.Property p, int preferences) {
        this(p, preferences, null);
    }

    /**
     * Create a new property panel for displaying/editing the specified
     * property
     * @param p A Property object for this node to represent
     * @see org.openide.nodes.Node.Property
     */
    public PropertyPanel(Node.Property p) {
        this(p, 0, null);
    }

    /** Create a property panel that displays a property belonging to several
     * nodes. This is useful e.g. for TreeTableView.  The property panel will
     * display the standard &quot;different values&quot; condition for cases
     * where the value of various properties does not match.
     * <p>
     * Note that this method assumes that none of the nodes will have two
     * different property sets each containing a property with the requested
     * name.  The behavior of this constructor is undefined in this case,
     * and will likely result in a ClassCastException.
     * <p>
     * This constructor is fail-fast, and will preemptively check that the
     * properties to be proxied indeed exist and are of the same type.
     *
     * @param nodes The nodes that will supply properties
     * @param propertyName The name of the property to look for
     * @throws ClassCastException if the named property for one of the nodes
     *  has a different value type than for others
     * @throws NullPointerException if even one of the nodes does not have
     *  a property of the given name.
     */
    PropertyPanel(Node[] nodes, String propertyName) throws ClassCastException, NullPointerException {
        //Protected for now - TreeTableView can use it
        this(
            (nodes.length == 1) ? ModelProperty.findProperty(nodes[0], propertyName)
                                : ModelProperty.toProperty(nodes, propertyName)
        );
    }

    /** The constructor all the other constructors call */
    private PropertyPanel(Node.Property p, int preferences, PropertyModel mdl) {
        if (p == null) {
            prop = ModelProperty.toProperty(mdl);
        } else {
            prop = p;
        }

        this.preferences = preferences;
        initializing = true;
        setModel(mdl);
        initializing = false;
        setOpaque(true);

        if (!GraphicsEnvironment.isHeadless()) {
            //for debugging, allow CTRL-. to dump the state to stderr
            getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "dump"
            );
        }
        getActionMap().put(
            "dump",
            new AbstractAction() { //NOI18N
                public void actionPerformed(ActionEvent ae) {
                    System.err.println(""); //NOI18N
                    System.err.println(PropertyPanel.this);
                    System.err.println(""); //NOI18N
                }
            }
        );

        //#44226 - Unpretty, but this allows the TreeTableView to invoke a custom editor dialog when
        //necessary - with the TTV rewrite, all cell editor infrastructure will be moved to
        //org.netbeans.modules.openide.explorer, and they will simply share editor classes.  Since that
        //involves an API change (some package private methods of PropertyEnv need to be accessible to
        //the editor classes), this will have to wait for after 4.0 - Tim
        getActionMap().put("invokeCustomEditor", new CustomEditorProxyAction()); //NOI18N

        PropertyPanelBridge.register(this, new BridgeAccessor(this));
    }

    @Override
    public void setBackground(Color c) {
        if (inner != null) {
            inner.setBackground(c);
        }

        super.setBackground(c);
    }

    @Override
    public void setForeground(Color c) {
        if (inner != null) {
            inner.setForeground(c);
        }

        super.setForeground(c);
    }

    /** Returns an appropriate property displayer instance depending on the
     * preferences.  For non-editable modes, will use a lightweight, near-stateless
     * RendererPropertyDisplayer component */
    private PropertyDisplayer findPropertyDisplayer() {
        PropertyDisplayer result;
        Node.Property prop = getProperty();

        if (((preferences & PREF_CUSTOM_EDITOR) == 0) && (((preferences & PREF_READ_ONLY) != 0) || !isEnabled())) {
            //Always use a renderer if we're inline and non-editable
            return getRendererComponent(prop);
        }

        switch (preferences) {
        case 9:
        case 1: //PREF_READ_ONLY
            result = getRendererComponent(prop);

            break;

        case 10:
        case 2: //PREF_CUSTOM_EDITOR
            result = new CustomEditorDisplayer(prop, model);

            break;

        case 11:
        case 3: //PREF_CUSTOM_EDITOR & PREF_READ_ONLY
            result = new CustomEditorDisplayer(prop, model);

            //XXX remember to set enabled on the components
            break;

        case 12:
        case 4: //PREF_INPUT_STATE
            result = new EditablePropertyDisplayer(prop, model);

            break;

        case 13:
        case 5: //PREF_INPUT_STATE & PREF_READ_ONLY
            result = getRendererComponent(prop);

            break;

        case 14:
        case 6: //PREF_INPUT_STATE & PREF_CUSTOM_EDIITOR
            result = new CustomEditorDisplayer(prop, model);

            //Only difference with this combination is it should display
            //an error dialog on commit if the entered value is bad
            break;

        case 15:
        case 7: //PREF_INPUT_STATE & PREF_CUSTOM_EDITOR & PREF_READ_ONLY
            result = new CustomEditorDisplayer(prop, model);

            break;

        case 0:
        case 8:default:
            result = new EditablePropertyDisplayer(prop, model);

            break;
        }

        if (result instanceof PropertyDisplayer_Inline) {
            PropertyDisplayer_Inline inline = (PropertyDisplayer_Inline) result;
            boolean tableUI = ((preferences & PREF_TABLEUI) != 0) || Boolean.TRUE.equals(getClientProperty("flat")); //NOI18N
            inline.setTableUI(tableUI); //NOI18N

            if (inline.isTableUI()) {
                inline.setUseLabels(!tableUI);
            }
        }

        boolean isTableUI = (preferences & PREF_TABLEUI) != 0;

        if (result instanceof CustomEditorDisplayer) {
            ((PropertyDisplayer_Editable) result).setUpdatePolicy(
                changeImmediate ? PropertyDisplayer.UPDATE_ON_FOCUS_LOST : PropertyDisplayer.UPDATE_ON_EXPLICIT_REQUEST
            );
        } else if (result instanceof PropertyDisplayer_Editable) {
            ((PropertyDisplayer_Editable) result).setUpdatePolicy(
                isTableUI ? PropertyDisplayer.UPDATE_ON_CONFIRMATION : PropertyDisplayer.UPDATE_ON_FOCUS_LOST
            );
        }

        if (((preferences & PREF_READ_ONLY) != 0) && result instanceof CustomEditorDisplayer) {
            ((CustomEditorDisplayer) result).setEnabled(false);
        } else if (result instanceof PropertyDisplayer_Editable) {
            if (!isEnabled()) {
                ((PropertyDisplayer_Editable) result).setEnabled(isEnabled());
            }
        }

        return result;
    }

    /** Convenience method to allow reuse of mutable renderer components -
     * will either create a renderer or reuse the current displayer if there
     * is one and it's a renderer */
    private RendererPropertyDisplayer getRendererComponent(Node.Property prop) {
        RendererPropertyDisplayer result;

        if (inner instanceof RendererPropertyDisplayer) {
            //re-use the one we already have if possible
            ((RendererPropertyDisplayer) inner).setProperty(prop);
            result = (RendererPropertyDisplayer) inner;
        } else {
            result = new RendererPropertyDisplayer(prop);
        }

        return result;
    }

    /** Fetch the PropertyDisplayer which will do the actual work of displaying
     * the property.  It will be created if necessary */
    private PropertyDisplayer getPropertyDisplayer() {
        if (displayer == null) {
            setDisplayer(findPropertyDisplayer());
        }

        return displayer;
    }

    /**
     * Writes the edited value to the property.
     * @return <code>true</code> when the value was successfully written, <code>false</code> otherwise.
     */
    private boolean commit() {
        if (ignoreCommit) {
            return true;
        }
        if (displayer instanceof PropertyDisplayer_Editable) {
            try {
                return ((PropertyDisplayer_Editable) displayer).commit();
            } catch (IllegalArgumentException iae) {
                PropertyDialogManager.notify(iae);
                return false;
            }
        } else {
            return false;
        }
    }

    /** Installs the component we will embed to display the property */
    private void installDisplayerComponent() {
        //Fetch or instantiate the component we will embed to display the 
        //property.  Depending on the prefs, it may be a RendererPropertyDisplayer,
        //an EditablePropertyDisplayer or a CustomPropertyDisplayer.
        PropertyDisplayer displayer = getPropertyDisplayer();

        //Find who has focus now, so if we have focus, focus won't end up set
        //to null
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();

        boolean hadFocus = (focusOwner == this) || isAncestorOf(focusOwner);

        if (hadFocus) {
            //If we had focus, clear the global focus owner for now, so that
            //when the existing component is removed, it does not cause
            //focus to get briefly set to a random component
            KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
        }

        //Fetch the new inner component (the custom editor or Inplace editor)
        Component newInner = displayer.getComponent();

        //Set the enabled state appropriately.  For implementations of 
        //PropertyDisplayer_Editable, this will already be handled; for render-
        //only cases, it should be handled explicitly
        if (!(displayer instanceof PropertyDisplayer_Editable)) {
            //only for renderers
            newInner.setEnabled(isEnabled() && getProperty().canWrite());
        }

        newInner.setForeground(getForeground());
        newInner.setBackground(getBackground());

        //Make sure the inner component has really changed
        if (newInner != inner) {
            synchronized (getTreeLock()) {
                //remove the odl component
                if (inner != null) {
                    remove(inner);
                }

                //and add the new one (if any)
                if (newInner != null) {
                    add(newInner);

                    //invalidate its layout so it will be re-laid out
                    newInner.invalidate();
                    inner = newInner;
                }
            }
        }

        //Force a re-layout immediately if visible
        if (isShowing() && !(getParent() instanceof javax.swing.CellRendererPane)) {
            validate();
        }

        //Restore focus if necessary
        if (hadFocus && isEnabled() && ((preferences & PREF_READ_ONLY) == 0)) {
            requestFocus();
        }

        //Simply adding a component to a container can sometimes cause it to be
        //given focus even though it's not focusable.  If this has happened,
        //find the next component in the focus cycle root and force focus to that.
        //Mainly a problem with JFileChooser, but we also have a few property
        //editors that force focus on addNotify which should be fixed
        if (!isEnabled() || ((preferences & PREF_READ_ONLY) != 0)) {
            Component focus = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

            if ((focus == inner) || ((inner instanceof Container) && ((Container) inner).isAncestorOf(focus))) {
                this.transferFocusUpCycle();
            }
        }
    }

    @Override
    public void doLayout() {
        layout();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void layout() {
        if (inner != null) {
            inner.setBounds(0, 0, getWidth(), getHeight());
        }
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension result;

        if (!isDisplayable() && ((preferences & PREF_CUSTOM_EDITOR) == 0)) {
            //XXX use rendererfactory to make this more efficient and just
            //configure a shared renderer instacne with the property & fetch size
            result = getRendererComponent(getProperty()).getComponent().getPreferredSize();
        } else if (inner != null) {
            result = inner.getPreferredSize();
        } else {
            result = PropUtils.getMinimumPanelSize();
        }

        return result;
    }

    /** Sets the property displayer we are using to display the property,
     * detaching listeners, etc */
    private void setDisplayer(PropertyDisplayer nue) {
        if (displayer != null) {
            detachFromDisplayer(displayer);
        }

        displayer = nue;

        if (nue != null) {
            attachToDisplayer(displayer);
        }
    }

    /** Attach any necessary listeners to the property displayer to be used */
    private void attachToDisplayer(PropertyDisplayer displayer) {
        if (displayer instanceof PropertyDisplayer_Inline) {
            updateDisplayerFromClientProps();
        }

        if (displayer instanceof CustomEditorDisplayer) {
            ((CustomEditorDisplayer) displayer).setRemoteEnvListener(getListener());
            ((CustomEditorDisplayer) displayer).setRemoteEnvVetoListener(getListener());
        }

        if (displayer instanceof EditablePropertyDisplayer) {
            ((EditablePropertyDisplayer) displayer).setRemoteEnvListener(getListener());
            ((EditablePropertyDisplayer) displayer).setRemoteEnvVetoListener(getListener());
            ((EditablePropertyDisplayer) displayer).addActionListener(getListener());

            PropertyEnv env = ((EditablePropertyDisplayer) displayer).getPropertyEnv();

            if (env != null) {
                env.setFeatureDescriptor(getProperty());
            }
        }
    }

    /** Remove any listeners and dispose any state relating to a displayer
     * we are no longer interested in */
    private void detachFromDisplayer(PropertyDisplayer displayer) {
        if (displayer instanceof CustomEditorDisplayer) {
            ((CustomEditorDisplayer) displayer).setRemoteEnvVetoListener(null);
        }

        if (displayer instanceof EditablePropertyDisplayer) {
            ((EditablePropertyDisplayer) displayer).setRemoteEnvVetoListener(null);
            ((EditablePropertyDisplayer) displayer).removeActionListener(getListener());
        }
    }

    /** Overridden to catch changes in those client properties that are
     * relevant to PropertyPanel */
    @Override
    protected void firePropertyChange(String nm, Object old, Object nue) {
        if (
            ("flat".equals(nm) || "radioButtonMax".equals(nm) || "suppressCustomEditor".equals(nm) ||
                "useLabels".equals(nm)) && displayer instanceof PropertyDisplayer_Inline
        ) { //NOI18N
            updateDisplayerFromClientProp(nm, nue);
        }

        super.firePropertyChange(nm, old, nue);
    }

    /** Update the current property displayer based on previously set client
     * properties */
    private void updateDisplayerFromClientProp(String nm, Object val) {
        PropertyDisplayer displayer = getPropertyDisplayer();

        if (displayer instanceof PropertyDisplayer_Inline) {
            PropertyDisplayer_Inline inline = (PropertyDisplayer_Inline) displayer;

            if ("flat".equals(nm)) { //NOI18N
                inline.setTableUI(Boolean.TRUE.equals(val));

                if (Boolean.TRUE.equals(val)) {
                    inline.setUseLabels(false);
                } else if (Boolean.FALSE.equals(val) && (getClientProperty("useLabels") == null)) { //NOI18N
                    inline.setUseLabels(true);
                }
            } else if ("radioButtonMax".equals(nm)) { //NOI18N

                int max = (val instanceof Integer) ? ((Integer) val).intValue() : 0;
                inline.setRadioButtonMax(max);
            } else if ("suppressCustomEditor".equals(nm)) { //NOI18N
                inline.setShowCustomEditorButton(!Boolean.TRUE.equals(val));
            } else if ("useLabels".equals(nm)) { //NOI18N
                inline.setUseLabels(Boolean.TRUE.equals(val));
            }
        }
    }

    /** Overridden to return false in cases that the preferences specify a
     * read-only state */
    @Override
    public boolean isFocusable() {
        return super.isFocusable() && isEnabled() && ((preferences & PREF_READ_ONLY) == 0);
    }

    /** Overridden to do
     * nothing in a read only state, since some custom property editors (File
     * chooser) are capable of receiving focus even if they are disabled,
     * effectively making focus disappear */
    @Override
    public void requestFocus() {
        //Do this because even if everything is disabled, JFileChooser's UI
        //*does* supply some focusable components
        if (!isEnabled() || ((preferences & PREF_READ_ONLY) != 0)) {
            return;
        } else if ((inner != null) && inner.isEnabled()) {
            super.requestFocus();
            inner.requestFocus();
        }
    }

    /** In the case that some client properties may have been set before a
     * PropertyRenderer was added, set up its values accordingly.  */
    private void updateDisplayerFromClientProps() {
        String[] props = new String[] { "flat", "radioButtonMax", "suppressCustomEditor", "useLabels" }; //NOI18N

        for (int i = 0; i < props.length; i++) {
            Object o = getClientProperty(props[i]);

            if (o != null) {
                updateDisplayerFromClientProp(props[i], o);
            }
        }
    }

    @Override
    protected void processFocusEvent(FocusEvent fe) {
        super.processFocusEvent(fe);

        if (fe.getID() == FocusEvent.FOCUS_GAINED) {
            if ((inner != null) && inner.isEnabled() && inner.isFocusTraversable()) {
                inner.requestFocus();
            }
        }
    }

    /** Lazily create the listener for listening to the property editor, env
     * and model */
    private Listener getListener() {
        if (listener == null) {
            listener = new Listener();
        }

        return listener;
    }

    /** Overridden to install the inner component that will display the property*/
    @Override
    public void addNotify() {
        attachToModel();

        if (displayer != null) {
            attachToDisplayer(displayer);
        }

        if (inner == null) {
            installDisplayerComponent();
        }

        super.addNotify();
    }

    /** Overridden to dispose the component that actually displays the property
     * and any state information associated with it */
    @Override
    public void removeNotify() {
        super.removeNotify();
        detachFromModel();

        if ((displayer != null) && (!(displayer instanceof RendererPropertyDisplayer))) {
            detachFromDisplayer(displayer);
            displayer = null;
        }

        if (null != inner && !(inner instanceof RendererPropertyDisplayer)) {
            //Renderers hold no references the property panel doesn't, so avoid 
            //creating a new one for performance reasons in TTV - PropertyPanel
            //will be repeatedly added to and removed from a CellRendererPane
            remove(inner);
            inner = null;
        }
    }

    /*
    public Dimension getPreferredSize() {
        Dimension result;
        if (!isDisplayable() && (preferences & PREF_CUSTOM_EDITOR) == 0) {
            //XXX use rendererfactory to make this more efficient and just
            //configure a shared renderer instacne with the property & fetch size
            result = getRendererComponent(
                getProperty()).getComponent().getPreferredSize();
        } else if (inner != null) {
            result = inner.getPreferredSize();
        } else {
            result = super.getPreferredSize();
        }
        return result;
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
     */

    /** Returns the preferences set for this property panel.  The preferences
     * determine such things as read-only mode and whether an inline or custom
     * editor is displayed
     * @return The preferences
     */
    public int getPreferences() {
        return preferences;
    }

    /** Setter for visual preferences in displaying
     * of the value of the property.
     * @param preferences PREF_XXXX constants
     */
    public void setPreferences(int preferences) {
        if (preferences != this.preferences) {
            int oldPreferences = this.preferences;
            this.preferences = preferences;
            hardReset();
            firePropertyChange(PROP_PREFERENCES, oldPreferences, preferences);
        }
    }

    /** Get the property model associated with this property panel.  Note that
     * while the PropertyModel usages of PropertyPanel are not yet deprecated,
     * the preferred and more efficient use of PropertyPanel is directly with
     * a Node.Property instance rather than a PropertyModel. <p><strong><b>Note:</b>
     * This method is primarily here for backward compatibility, and the single
     * use case where it is truly desirable to have a GUI component which reflects
     * changes made by some source other than itself.  If you have used one
     * of the constructors which takes a <code>Node.Property</code> instance
     * or the <code>setProperty</code> method, this method will return a
     * PropertyModel instance generated for the underlying Node.Property object.
     * In such a case, the PropertyModel instance thus obtained <u>will not
     * fire changes</u> due to changes made by calling <code>setValue</code>
     * on the <code>Node.Property</code>.  For details on why this is the case,
     * see <a href="https://bz.apache.org/netbeans/show_bug.cgi?id=37779">issue
     * 37779</a>.</strong>
     *
     * @return Either the PropertyModel set in the constructor or via <code>setModel</code>,
     * or a generated instance of PropertyModel which wraps the <code>Node.Property</code>
     * which was set in the constructor or via <code>setProperty</code>.
     */
    public PropertyModel getModel() {
        if (model == null) {
            return new NodePropertyModel(getProperty(), null);
        }

        return model;
    }

    /** Setter for property model.
     * Note that while the PropertyModel usages of PropertyPanel are not yet deprecated,
     * the preferred and more efficient use of PropertyPanel is directly with
     * a Node.Property instance rather than a PropertyModel.  The PropertyPanel
     * will either construct a wrapper Node.Property instance or find the
     * underlying Node.Property instance, and use that to drive its infrastructure.
     * The only remaining use case for PropertyModel here is if the component
     * needs to listen for changes in the underlying value <i>which do not
     * originate in this PropertyPanel</i>.
     *
     *@param model New model.
     */
    public void setModel(PropertyModel model) {
        if (model != this.model) {
            settingModel = true;

            if ((this.model != null) && (listener != null)) {
                detachFromModel();
            }

            try {
                if (!initializing) {
                    setProperty(ModelProperty.toProperty(model));
                    this.model = model;

                    if (model != null) {
                        if (isDisplayable()) {
                            attachToModel();
                        }
                    }
                } else {
                    this.model = model;
                    attachToModel();
                }
            } finally {
                settingModel = false;
            }
        }
    }

    /** Attach listeners to an instance of PropertyModel */
    private final void attachToModel() {
        if (model != null) {
            model.addPropertyChangeListener(getListener());
        }
    }

    /** Detach listeners from an instance of PropertyModel */
    private final void detachFromModel() {
        if (model != null) {
            model.removePropertyChangeListener(getListener());
        }
    }

    Object[] getBeans() {
        return beans;
    }

    /** Set or change the property this PropertyPanel will display
     * @param p the Property
     */
    public final void setProperty(Node.Property p) {
        Object bridgeID = getClientProperty("beanBridgeIdentifier");

        if (bridgeID != null) {
            TTVEnvBridge bridge = TTVEnvBridge.findInstance(bridgeID);

            if (bridge != null) {
                beans = bridge.getCurrentBeans();
                bridge.clear();
            }
        }

        if (p != prop) {
            prop = p;

            if (!settingModel) {
                //model will be recreated dynamically
                model = null;
            }

            if (displayer != null) {
                if (displayer instanceof PropertyDisplayer_Mutable) {
                    ((PropertyDisplayer_Mutable) displayer).setProperty(p);
                } else {
                    hardReset();
                }
            }
        }
    }

    /** Reset any edits in progress and restore the property's value to the
     * component displaying it.  In custom editor mode, this may cause the
     * entire inner component to be replaced with a new instance of the same
     * thing */
    final void reset() {
        if ((preferences & PREF_CUSTOM_EDITOR) != 0) {
            getPropertyDisplayer().refresh();
        } else {
            hardReset();
        }
    }

    /** Do a full reset, replacing the inner component */
    final void hardReset() {
        setDisplayer(findPropertyDisplayer());

        if (isDisplayable()) { //XXX maybe check for isShowing, and if not just clear the inner component?
            installDisplayerComponent();
        }
    }

    /**
     * Fetch the property that this PropertyPanel displays.  For cases where
     * the PropertyPanel was initialized with an instance of PropertyModel,
     * the return value of this method is not officially defined.
     * @return the property
     */
    public final Node.Property getProperty() {
        if ((prop == null) && (model != null)) {
            prop = ModelProperty.toProperty(model);
        }

        return prop;
    }

    /** Getter for the state of the property editor. The editor can be in
     * not valid states just if it implements the {@link ExPropertyEditor}
     * and changes state by the <code>setState</code> method of the {@link PropertyEnv}
     * environment.
     *
     * @return <code>PropertyEnv.STATE_VALID</code> if the editor is not the <code>ExPropertyEditor</code>
     *    one or other constant from <code>PropertyEnv.STATE_*</code> that was assigned to <code>PropertyEnv</code>
     * @since 2.20
     */
    public final Object getState() {
        if (displayer instanceof PropertyDisplayer_Editable) {
            return ((PropertyDisplayer_Editable) displayer).getPropertyEnv().getState();
        } else {
            PropertyEditor ed = propertyEditor();

            if (ed instanceof ExPropertyEditor) {
                //XXX until we kill ReusablePropertyModel, anyway
                ReusablePropertyEnv env = reusableEnv;
                reusableModel.setProperty(prop);
                ((ExPropertyEditor) ed).attachEnv(env);

                return env.getState();
            }
        }

        return PropertyEnv.STATE_VALID;
    }

    /** If the editor is {@link ExPropertyEditor} it tries to change the
     * <code>getState</code> property to <code>PropertyEnv.STATE_VALID</code>
     * state. This may be vetoed, in such case a warning is presented to the user
     * and the <code>getState</code> will still return the original value
     * (different from STATE_VALID).
     * <P>
     * Also updates the value if
     * <code>org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor</code>
     * is used.
     */
    public void updateValue() {
        if (displayer instanceof PropertyDisplayer_Editable) {
            PropertyEnv env = ((PropertyDisplayer_Editable) displayer).getPropertyEnv();

            if (PropertyEnv.STATE_NEEDS_VALIDATION.equals(env.getState())) {
                env.setState(PropertyEnv.STATE_VALID);
            }

            if (!changeImmediate) {
                commit();
            }
        }
    }

    /**
     * Getter for current property editor depending on the model.
     * It may be <CODE>null</CODE> if it is not possible
     * to obtain a property editor.<p>
     * <strong>Note:  When not in custom editor mode, PropertyPanel does
     * not cache the editor supplied by the property.  If the PropertyPanel
     * was initialized from an instance of (deprecated) DefaultPropertyModel,
     * a different instance of the property editor may be constructed and
     * returned for each call.  Client code must take this into account.
     * </strong>.  For cases of initialization via a Node.Property object,
     * it is up to the supplied Property to cache or not cache the property
     * editor returned from <code>getPropertyEditor()</code> as suits its
     * needs.
     * @deprecated The property panel does not cache the property editor, and
     * depending on its state, it may not consistently return the same property
     * editor instance on repeated calls.  The current implementation will
     * do so for editable states, but there is no guarantee this will remain
     * so in the future.
     * @return the property editor or <CODE>null</CODE>
     */
    public @Deprecated PropertyEditor getPropertyEditor() {
        return propertyEditor();
    }

    /** Internal implementation of getPropertyEditor(). */
    private PropertyEditor propertyEditor() {
        PropertyEditor result = null;

        if (displayer != null) {
            //Use the package private methods to fetch the same editor
            //being used by the displayer, so the state will be appropriate
            if (displayer instanceof CustomEditorDisplayer) {
                result = ((CustomEditorDisplayer) displayer).getPropertyEditor();
            } else if (displayer instanceof EditablePropertyDisplayer) {
                result = ((EditablePropertyDisplayer) displayer).getPropertyEditor();
            }
        }

        if (result == null) {
            //Fetch the property editor using the utility method (which will
            //handle no-property editor and multiple selection states 
            //appropriately)
            result = PropUtils.getPropertyEditor(getProperty());
        }

        return result;
    }

    /** Sets whether or not this component is enabled.
     *
     * all panel components gets disabled when enabled parameter is set false
     * @param enabled flag defining the action.
     */
    @Override
    public void setEnabled(boolean enabled) {
        // bugfix# 10171, explicitly disable components inside the custom editor
        super.setEnabled(enabled);

        if (inner != null) {
            PropertyDisplayer displayer = getPropertyDisplayer();

            if (displayer instanceof PropertyDisplayer_Editable) {
                //Make sure we iterate all components in the custom editor
                //by calling setEnabled on the PropertyDisplayer interface,
                //not the component
                ((PropertyDisplayer_Editable) displayer).setEnabled(enabled);
            } else {
                //We're probably using a renderer, we now need an editable
                //component
                hardReset();
            }
        }
    }

    /** Getter for property changeImmediate.
     * If this is true the changes made in the property editor
     * are immediately propagated to the value of the property
     * (to the property model).
     *
     * @return Value of property changeImmediate.
     */
    public boolean isChangeImmediate() {
        return changeImmediate;
    }

    /** Setter for property changeImmediate.
     * IF this is true the changes made in the property editor
     * are immediately propagated to the value of the property
     * (to the property model).
     * @param changeImmediate New value of property changeImmediate.
     */
    public void setChangeImmediate(boolean changeImmediate) {
        if (this.changeImmediate == changeImmediate) {
            return;
        }

        this.changeImmediate = changeImmediate;

        if (isShowing()) {
            PropertyDisplayer displayer = getPropertyDisplayer();

            if (displayer instanceof PropertyDisplayer_Editable) {
                ((PropertyDisplayer_Editable) displayer).setUpdatePolicy(
                    changeImmediate ? PropertyDisplayer.UPDATE_ON_FOCUS_LOST
                                    : PropertyDisplayer.UPDATE_ON_EXPLICIT_REQUEST
                );
            }
        }

        firePropertyChange(
            PropertyEnv.PROP_CHANGE_IMMEDIATE, changeImmediate ? Boolean.FALSE : Boolean.TRUE,
            changeImmediate ? Boolean.TRUE : Boolean.FALSE
        );
    }

    /** Overridden to provide information from the embedded property renderer
     * if not in custom editor mode */
    @Override
    public String toString() {
        if ((preferences & PREF_CUSTOM_EDITOR) != 0) {
            //custom editor mode, not much useful to say here
            return super.toString() + " - " + prefsToString(getPreferences()); //NOI18N
        } else {
            return getClass().getName() + System.identityHashCode(this) + prefsToString(getPreferences()) +
            " propertyRenderer: " + ((inner == null) ? " null " : inner.toString()); //NOI18N
        }
    }

    /** Utility method to convert PropertyPanel preferences to human-readable
     * form */
    private static String prefsToString(int prefs) {
        StringBuffer sb = new StringBuffer(" prefs:"); //NOI18N
        int[] vals = new int[] { PREF_CUSTOM_EDITOR, PREF_INPUT_STATE, PREF_READ_ONLY };
        String[] s = new String[] { "PREF_CUSTOM_EDITOR", "PREF_INPUT_STATE", "PREF_READ_ONLY" }; //NOI18N
        boolean found = false;

        for (int i = 0; i < vals.length; i++) {
            if ((vals[i] & prefs) != 0) {
                sb.append(s[i]);
            }

            if (found && (i != (vals.length - 1))) {
                sb.append(","); //NOI18N
            }

            found = true;
        }

        return sb.toString();
    }

    /*
     * This method is overridden to customize the painting behavior.
     * It ensures that the background color is filled when setOpaque is true.
     */
    @Override
    public void paint(Graphics g) {
        if (isOpaque()) {
            //Presumably we can get this fixed for JDK 1.5.1
            Color c = getBackground();

            if (c == null) {
                c = UIManager.getColor("control"); //NOI18N
            }

            if (c == null) {
                c = Color.LIGHT_GRAY;
            }

            Color oldC = g.getColor();
            g.setColor(c);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(oldC);
        }

        super.paint(g);
    }

    ////////////////// Accessibility support ///////////////////////////////////
    @Override
    public javax.accessibility.AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessiblePropertyPanel();
        }

        return accessibleContext;
    }

    /**
     * Held in action map to allow TreeTableView to invoke the custom editor over read-only cells
     */
    private class CustomEditorProxyAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            Action wrapped = getWrapped();

            if (wrapped != null) {
                wrapped.actionPerformed(e);
            } else {
                Utilities.disabledActionBeep();
            }
        }

        private Action getWrapped() {
            Node.Property p = getProperty();
            EditablePropertyDisplayer pd = (getPropertyDisplayer() instanceof EditablePropertyDisplayer)
                ? (EditablePropertyDisplayer) getPropertyDisplayer() : new EditablePropertyDisplayer(p);

            return pd.getCustomEditorAction();
        }

        @Override
        public boolean isEnabled() {
            Action wrapped = getWrapped();

            if (wrapped != null) {
                return wrapped.isEnabled();
            } else {
                return getProperty() != null;
            }
        }

        @Override
        public Object getValue(String key) {
            if (SMALL_ICON.equals(key)) {
                // Provide the icon for those who use this action
                return PropUtils.getCustomButtonIcon();
            } else {
                return super.getValue(key);
            }
        }
    }

    private class AccessiblePropertyPanel extends AccessibleJComponent {
        AccessiblePropertyPanel() {
        }

        @Override
        public javax.accessibility.AccessibleRole getAccessibleRole() {
            return javax.accessibility.AccessibleRole.PANEL;
        }

        @Override
        public String getAccessibleName() {
            String name = super.getAccessibleName();

            if ((name == null) && model instanceof ExPropertyModel) {
                FeatureDescriptor fd = ((ExPropertyModel) model).getFeatureDescriptor();
                name = NbBundle.getMessage(PropertyPanel.class, "ACS_PropertyPanel", fd.getDisplayName()); //NOI18N
            }

            return name;
        }

        @Override
        public String getAccessibleDescription() {
            String description = super.getAccessibleDescription();

            if ((description == null) && model instanceof ExPropertyModel) {
                FeatureDescriptor fd = ((ExPropertyModel) model).getFeatureDescriptor();
                description = NbBundle.getMessage(PropertyPanel.class, "ACSD_PropertyPanel", fd.getShortDescription()); //NOI18N
            }

            return description;
        }
    }

    private class Listener implements PropertyChangeListener, VetoableChangeListener, ChangeListener, ActionListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource() instanceof PropertyEnv) {
                firePropertyChange(PropertyPanel.PROP_STATE, evt.getOldValue(), evt.getNewValue());
            }

            if (evt.getSource() instanceof PropertyModel) {
                if ((evt.getOldValue() == null) && (evt.getNewValue() == null)) {
                    //the old hack of firing a null-null change to get the
                    //infrastructure to dump everything and start from scratch
                    hardReset();
                } else {
                    //Just notify the displayer that the value may have changed,
                    //no need for anything radical
                    reset();
                }
            }
        }

        public void vetoableChange(PropertyChangeEvent evt)
        throws PropertyVetoException {
            //do nothing - the displayer will take care of it for us
        }

        public void stateChanged(javax.swing.event.ChangeEvent e) {
            //do nothing
        }

        public void actionPerformed(ActionEvent e) {
            if( inner == e.getSource() && "enterPressed".equals(e.getActionCommand()) ) { //NOI18N
                Object beanBridge = getClientProperty("beanBridgeIdentifier"); //NOI18N
                if( beanBridge instanceof CellEditor ) {
                    boolean wasCommitted = false;
                    if (e instanceof CellEditorActionEvent) {
                        // Prevent from a second commit on stop of cell editing:
                        wasCommitted = ((CellEditorActionEvent) e).isCommitted();
                    }
                    try {
                        ignoreCommit = wasCommitted;
                        ((CellEditor)beanBridge).stopCellEditing();
                    } finally {
                        ignoreCommit = false;
                    }
                }
            }
        }
    }

    private static final class BridgeAccessor implements PropertyPanelBridge.Accessor {

        private final Reference<PropertyPanel> panelRef;

        public BridgeAccessor(PropertyPanel panel) {
            this.panelRef = new WeakReference<PropertyPanel>(panel);
        }

        @Override
        public boolean commit() {
            PropertyPanel panel = panelRef.get();
            if (panel != null) {
                return panel.commit();
            } else {
                return false;
            }
        }

    }
}
