/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.spi.editor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.modules.editor.lib2.actions.EditorActionUtilities;
import org.netbeans.modules.editor.lib2.actions.MacroRecording;
import org.netbeans.modules.editor.lib2.actions.PresenterUpdater;
import org.netbeans.modules.editor.lib2.actions.WrapperEditorAction;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;

/**
 * Base class for editor actions that should be used together with
 * {@link EditorActionRegistration} annotation.
 * <br>
 * It may be constructed and used in two ways: direct construction or construction
 * upon invocation by a wrapper action:
 * <ul>
 *   <li> Direct construction - action is created directly when an editor kit
 *        gets constructed (its <code>kit.getActions()</code> gets used).
 *        <br>
 *        Advantages: Action controls all its behavior and properties (including enabled status)
 *        since the begining.
 *        <br>
 *        Disadvantages: Action's class is loaded by classloader at editor kit's construction.
 *        <br>
 *        Example of registration:
 *        <br>
 * <code>
 * public static final class MyAction extends AbstractEditorAction {<br>
 *<br>
 *     &#64;EditorActionRegistration(name = "my-action")<br>
 *     public static MyAction create(Map&lt;String,?&gt; attrs) {<br>
 *         return new MyAction(attrs);<br>
 *     }<br>
 * <br>
 *     private MyAction(Map&lt;String,?&gt; attrs) {<br>
 *         super(attrs);<br>
 *         ...<br>
 *     }<br>
 * <br>
 *     protected void actionPerformed(ActionEvent evt, JTextComponent component) {<br>
 *         ...<br>
 *     }<br>
 * <br>
 * }<br>
 * </code>
 *   </li>
 * 
 *   <li> Construction upon invocation - {@link WrapperEditorAction} is constructed
 *        upon editor kit's construction and the target action only gets
 *        created when the action needs to be executed
 *        (upon {@link Action#actionPerformed(java.awt.event.ActionEvent)} call).
 *        Existing properties of the wrapper action (including <code>Action.NAME</code> property)
 *        get transferred into delegate action.
 *        <br>
 *        Advantages: Action's class is only loaded upon action's execution.
 *        <br>
 *        Disadvantages: Only a limited set of action's properties gets populated
 *        (those defined by {@link EditorActionRegistration}).
 *        <br>
 *        Example of registration:
 *        <br>
 * <code>
 * &#64;EditorActionRegistration(name = "my-action")<br>
 * public static final class MyAction extends AbstractEditorAction {<br>
 *<br>
 *     public MyAction() {<br>
 *         // Here the properties are not yet set.<br>
 *     }<br>
 * <br>
 *     &#64;Override<br>
 *     protected void valuesUpdated() {<br>
 *         // Here the wrapper action has transferred all its properties into this action<br>
 *         // so properties like Action.NAME etc. are now populated.<br>
 *     }<br>
 * <br>
 *     protected void actionPerformed(ActionEvent evt, JTextComponent component) {<br>
 *         ...<br>
 *     }<br>
 * <br>
 * }<br>
 * </code>
 *   </li>
 * </ul>
 *
 * @author Miloslav Metelka
 * @since 1.14
 */
public abstract class AbstractEditorAction extends TextAction implements
        Presenter.Menu, Presenter.Popup, Presenter.Toolbar
{
    
    /**
     * Key of {@link String} property containing a localized display name of the action.
     * <br>
     * It may be passed to {@link #getValue(java.lang.String) } to obtain the property value.
     */
    public static final String DISPLAY_NAME_KEY = "displayName"; // (named in sync with AlwaysEnabledAction) NOI18N

    /**
     * Key of {@link String} property containing a localized text to be displayed in a main menu for this action.
     * <br>
     * It may be passed to {@link #getValue(java.lang.String) } to obtain the property value.
     */
    public static final String MENU_TEXT_KEY = "menuText"; // (named in sync with AlwaysEnabledAction) NOI18N

    /**
     * Key of {@link String} property containing a localized text to be displayed in a popup menu for this action.
     * <br>
     * If this property is not set then {@link #MENU_TEXT_KEY} is attempted.
     * <br>
     * It may be passed to {@link #getValue(java.lang.String) } to obtain the property value.
     */
    public static final String POPUP_TEXT_KEY = "popupText"; // (named in sync with AlwaysEnabledAction) NOI18N

    /**
     * Key of {@link String} property containing a string path to icon.
     */
    public static final String ICON_RESOURCE_KEY = "iconBase"; // (named in sync with AlwaysEnabledAction) NOI18N
    
    /**
     * Key of {@link Boolean} property which determines whether icon of this action should be
     * displayed in menu (false or unset) or not (true).
     * <br>
     * It may be passed to {@link #getValue(java.lang.String) } to obtain the property value.
     * @since 1.74
     */
    public static final String NO_ICON_IN_MENU = "noIconInMenu"; // (named in sync with system actions) NOI18N
    
    /**
     * Key of {@link Boolean} property which determines if this action should be
     * displayed in key binding customizer (false or unset) or not (true).
     * <br>
     * It may be passed to {@link #getValue(java.lang.String) } to obtain the property value.
     * @since 1.74
     */
    public static final String NO_KEY_BINDING = "no-keybinding"; // (named in sync with BaseAction.NO_KEY_BINDING) NOI18N

    /**
     * Key of property containing a <code>List &lt; List &lt; {@link KeyStroke} &gt; &gt;</code>
     * listing all multi-key bindings by which the action may be invoked.
     * <br>
     * There may be multiple multi-key bindings to invoke a single action e.g. a code completion
     * may be invoked by Ctrl+SPACE and also Ctrl+'\'
     * (in fact each of these bindings could also consist of multiple keystrokes).
     * The more straightforward (shorter) bindings should generally precede the longer ones
     * so e.g. tooltip may just show the first binding of the list.
     */
    public static final String MULTI_ACCELERATOR_LIST_KEY = "MultiAcceleratorListKey"; // NOI18N

    /**
     * Key of {@link Boolean} property containing a boolean whether the action should be performed asynchronously or synchronously.
     */
    public static final String ASYNCHRONOUS_KEY = "asynchronous"; // (named in sync with AlwaysEnabledAction) NOI18N

    /**
     * Key of {@link String} property containing a mime type for which this action
     * is registered.
     * <br>
     * Note: action's mime-type is not necessarily the same like <code>EditorKit.getContentType()</code>
     * for which the action was created because the kit may inherit some actions
     * from a global mime-type "".
     * <br>
     * Value of this property is checked at action's initialization
     * (it needs to be passed as part of 'attrs' parameter to constructor).
     * Subsequent modifications of this property should be avoided and they will likely not affect its behavior.
     */
    public static final String MIME_TYPE_KEY = "mimeType"; // (named in sync with doc's property) NOI18N

    /**
     * Key of {@link Preferences} property containing a node in preferences in which this action changes settings.
     */
    public static final String PREFERENCES_NODE_KEY = "preferencesNode"; // (named in sync with AlwaysEnabledAction) NOI18N
    
    /**
     * Key of {@link String} property containing a name of a boolean key in preferences in which this action changes settings
     * (according to {@link #PREFERENCES_NODE_KEY} property).
     * <br>
     * Once this property is set then it's expected that {@link #PREFERENCES_NODE_KEY} is also set
     * to a valid value and checkbox menu presenter will be used automatically.
     */
    public static final String PREFERENCES_KEY_KEY = "preferencesKey"; // (named in sync with AlwaysEnabledAction) NOI18N
    
    /**
     * Key of {@link String} property containing preferences key's default value.
     */
    public static final String PREFERENCES_DEFAULT_KEY = "preferencesDefault"; // (named in sync with AlwaysEnabledAction) NOI18N

    /**
     * Key of {@link Boolean} property determining whether this is just a wrapper action
     * that is being used until the action needs to be executed. Then the target action
     * gets created and run.
     * <br>
     * Value of this property is checked at action's initialization
     * (it needs to be passed as part of 'attrs' parameter to constructor).
     * Subsequent modifications of this property should be avoided and they will likely not affect its behavior.
     */
    public static final String WRAPPER_ACTION_KEY = "WrapperActionKey"; // NOI18N
    
    /** Logger for reporting invoked actions */
    private static final Logger UILOG = Logger.getLogger("org.netbeans.ui.actions.editor"); // NOI18N
    
    /**
     * Whether invoked actions not logged by default, such as caret moves, should be logged too.
     * -J-Dorg.netbeans.editor.ui.actions.logging.detailed=true
     */
    private static final boolean UI_LOG_DETAILED = Boolean.getBoolean("org.netbeans.editor.ui.actions.logging.detailed");
    
    // -J-Dorg.netbeans.spi.editor.AbstractEditorAction.level=FINE
    private static final Logger LOG = Logger.getLogger(AbstractEditorAction.class.getName());

    private static final long serialVersionUID = 1L; // Serialization no longer used (prevent warning)

    private static final Map<String,Boolean> LOGGED_ACTION_NAMES = Collections.synchronizedMap(new HashMap<String, Boolean>());
    
    private Map<String,?> attrs;

    private final Map<String,Object> properties;
    
    /**
     * If this action is a wrapper action around the delegate action which will be constructed
     * upon performing the action then this variable will hold the delegate action instance.
     */
    private Action delegateAction;

    private PreferencesNodeAndListener preferencesNodeAndListener;

    private static final Action UNITIALIZED_ACTION = EditorActionUtilities.createEmptyAction();

    private static final Object MASK_NULL_VALUE = new Object();
    
    /**
     * Constructor that takes a map of attributes that are typically obtained
     * from an xml layer when an action's creation method is annotated with
     * <code>@EditorActionRegistration</code>.
     * <br>
     * Example:
     * <br>
     * <code>
     * public static final class MyAction extends AbstractEditorAction {<br>
     *<br>
     *     &#64;EditorActionRegistration(name = "my-action")<br>
     *     public static MyAction create(Map&lt;String,?&gt; attrs) {<br>
     *         return new MyAction(attrs);<br>
     *     }<br>
     * <br>
     *     private MyAction(Map&lt;String,?&gt; attrs) {<br>
     *         super(attrs);<br>
     *         ...<br>
     *     }<br>
     * <br>
     *     protected void actionPerformed(ActionEvent evt, JTextComponent component) {<br>
     *         ...<br>
     *     }<br>
     * <br>
     * }<br>
     * </code>
     *
     * @param attrs non-null attributes that hold action's properties.
     *  The map is expected to be constant (no key-value changes).
     */
    protected AbstractEditorAction(Map<String,?> attrs) {
        super(null); // Action.NAME property will come from attrs in createValue()
        properties = new HashMap<String,Object>();
        if (attrs != null) {
            setAttrs(attrs);
            delegateAction = Boolean.TRUE.equals(attrs.get(WRAPPER_ACTION_KEY)) ? UNITIALIZED_ACTION : null;
            checkPreferencesKey();
        }
    }
    
    /**
     * Constructor typically used when action is constructed lazily
     * upon its performing (the action is always enabled and its properties
     * are declared in xml layer by annotation processor for <code>@EditorActionRegistration</code>).
     * <br>
     * Example:
     * <br>
     * <code>
     * &#64;EditorActionRegistration(name = "my-action")<br>
     * public static final class MyAction extends AbstractEditorAction {<br>
     *<br>
     *     public MyAction() {<br>
     *         // Here the properties are not yet set.<br>
     *     }<br>
     * <br>
     *     &#64;Override<br>
     *     protected void valuesUpdated() {<br>
     *         // Here the wrapper action has transferred all its properties into this action<br>
     *         // so properties like Action.NAME etc. are now populated.<br>
     *     }<br>
     * <br>
     *     protected void actionPerformed(ActionEvent evt, JTextComponent component) {<br>
     *         ...<br>
     *     }<br>
     * <br>
     * }<br>
     * </code>
     */
    protected AbstractEditorAction() {
        this(null);
    }

    /**
     * Implementation of the action must be defined by descendants.
     *
     * @param evt action event (may be null).
     * @param component "active" text component obtained by {@link TextAction#getFocusedComponent()}.
     *  It may be null.
     */
    protected abstract void actionPerformed(ActionEvent evt, JTextComponent component);

    /**
     * Called when property values from wrapper action were transferred into delegate action (this action)
     * so properties like Action.NAME will start to return correct values.
     *
     * @see AbstractEditorAction()
     */
    protected void valuesUpdated() {
    }

    /**
     * Possibly allow asynchronous execution of this action by returning true.
     *
     * @return Value of {@link #ASYNCHRONOUS_KEY} property is returned
     *  but subclasses may possibly implement some more elaborate algorithm.
     */
    protected boolean asynchronous() {
        return Boolean.TRUE.equals(getValue(ASYNCHRONOUS_KEY));
    }

    /**
     * Reset caret's magic position.
     * <br>
     * Magic caret position is useful when going through empty lines with Down/Up arrow
     * then the caret returns on original horizontal column when a particular line has sufficient
     * number of characters.
     *
     * @param component target text component.
     */
    protected final void resetCaretMagicPosition(JTextComponent component) {
        EditorActionUtilities.resetCaretMagicPosition(component);
    }

    /**
     * Get presenter of this action in main menu.
     * <br>
     * Default implementation uses {@link #MENU_TEXT_KEY} for menu item's text
     * and the presenter is placed in the menu according to rules
     * given in the corresponding {@link EditorActionRegistration}.
     * <br>
     * Moreover the default presenter is sensitive to currently active text component
     * and if the active editor kit has that action redefined it uses the active action's
     * properties for this presenter.
     *
     * @return instance of menu presenter for this action.
     */
    @Override
    public JMenuItem getMenuPresenter() {
        // No reusal (as component it can only be present in a single place in component hierarchy)
        return PresenterUpdater.createMenuPresenter(this);
    }

    /**
     * Get presenter of this action in popup menu.
     * <br>
     * Default implementation uses {@link #POPUP_TEXT_KEY} for popup menu item's text
     * and the presenter is placed in the popup menu according to rules
     * given in the corresponding {@link EditorActionRegistration}.
     *
     * @return instance of popup menu presenter for this action.
     */
    @Override
    public JMenuItem getPopupPresenter() {
        // No reusal (as component it can only be present in a single place in component hierarchy)
        return PresenterUpdater.createPopupPresenter(this);
    }

    /**
     * Get presenter of this action in toolbar.
     *
     * @return instance of toolbar presenter for this action.
     */
    @Override
    public Component getToolbarPresenter() {
        // No reusal (as component it can only be present in a single place in component hierarchy)
        return PresenterUpdater.createToolbarPresenter(this);
    }

    /**
     * @return value of <code>Action.NAME</code> property.
     */
    protected final String actionName() {
        return (String) getValue(Action.NAME);
    }

    @Override
    public final void actionPerformed(final ActionEvent evt) {
        // Possibly delegate to getDelegateAction()
        Action dAction = getDelegateAction();
        if (dAction != null) {
            if (!(dAction instanceof AbstractEditorAction)) {
                checkTogglePreferencesValue();
            }
            dAction.actionPerformed(evt);
            return;
        }

        final JTextComponent component = getTextComponent(evt);
        MacroRecording.get().recordAction(this, evt, component); // Possibly record action in a currently recorded macro

        if (UILOG.isLoggable(Level.FINE)) {
            // TODO [Mila] - Set action's property to disable UI logging
            String actionName = actionName();
            Boolean logged = LOGGED_ACTION_NAMES.get(actionName);
            if (logged == null) {
                logged = isLogged(actionName);
                LOGGED_ACTION_NAMES.put(actionName, logged);
            }
            if (logged) {
                LogRecord r = new LogRecord(Level.FINE, "UI_ACTION_EDITOR"); // NOI18N
                r.setResourceBundle(NbBundle.getBundle(AbstractEditorAction.class));
                if (evt != null) {
                    r.setParameters(new Object[] { evt, evt.toString(), this, toString(), getValue(NAME) });
                } else {
                    r.setParameters(new Object[] { "no-ActionEvent", "no-ActionEvent", this, toString(), getValue(NAME) }); //NOI18N
                }
                r.setLoggerName(UILOG.getName());
                UILOG.log(r);
            }
        }

        checkTogglePreferencesValue();

        if (asynchronous()) {
            RequestProcessor.getDefault().post(new Runnable () {
                public void run() {
                    actionPerformed(evt, component);
                }
            });
        } else {
            actionPerformed(evt, component);
        }
    }
    
    private static boolean isLogged(String actionName) {
        return actionName != null &&
                !"default-typed".equals(actionName) && //NOI18N
                -1 == actionName.indexOf("build-tool-tip") &&//NOI18N
                -1 == actionName.indexOf("build-popup-menu") &&//NOI18N
                -1 == actionName.indexOf("-kit-install") && //NOI18N
                (UI_LOG_DETAILED || (
                    -1 == actionName.indexOf("caret") && //NOI18N
                    -1 == actionName.indexOf("delete") && //NOI18N
                    -1 == actionName.indexOf("undo") &&//NOI18N
                    -1 == actionName.indexOf("redo") &&//NOI18N
                    -1 == actionName.indexOf("selection") && //NOI18N
                    -1 == actionName.indexOf("page-up") &&//NOI18N
                    -1 == actionName.indexOf("page-down") //NOI18N
                ));
    }

    @Override
    public final Object getValue(String key) {
        Action dAction = delegateAction;
        // Delegate whole getValue() if delegateAction already exists
        if (dAction != null && dAction != UNITIALIZED_ACTION) {
            Object value = dAction.getValue(key);
            if (value == null) {
                value = getValueLocal(key);
                if (value != null) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Transfer wrapper action property: key=" + key + ", value=" + value + '\n'); // NOI18N
                    }
                    dAction.putValue(key, value);
                }
            }
            return value;
        }

        return getValueLocal(key);
    }

    private Object getValueLocal(String key) {
        if ("enabled" == key) { // Same == in AbstractAction
            return enabled;
        }
        synchronized (properties) {
            Object value = properties.get(key);
            if (value == null) {
                if ("instanceCreate".equals(key)) { // Return null for this key
                    return null;
                }
                if (value == null) {
                    value = createValue(key);
                    if (value == null) { // Do not query next time
                        value = MASK_NULL_VALUE;
                    }
                    // Do not fire a change since property was not queried yet
                    properties.put(key, value);
                }
            }
            if (value == MASK_NULL_VALUE) {
                value = null;
            }
            return value;
        }
    }
    
    /**
     * This method is called when a value for the given property
     * was not yet populated.
     * <br>
     * This method is only called once for the given property. Even if this method
     * returns null for the given property the infrastructure remembers the
     * returned value and no longer queries this method (the property can still
     * be modified by {@link #putValue(java.lang.String, java.lang.Object) }.)
     * <br>
     * Calling of this method and remembering of the returned value does not trigger
     * {@link #firePropertyChange(java.lang.String, java.lang.Object, java.lang.Object) }.
     *
     * @param key key of the property.
     * @return value of the property or null.
     */
    protected Object createValue(String key) {
        Object value;
        if (Action.SMALL_ICON.equals(key)) {
            value = EditorActionUtilities.createSmallIcon(this);
        } else if (Action.LARGE_ICON_KEY.equals(key)) {
            value = EditorActionUtilities.createLargeIcon(this);
        } else if (attrs != null) {
            value = attrs.get(key);
        } else {
            value = null;
        }
        return value;
    }
    
    @Override
    public final void putValue(String key, Object value) {
        Action dAction = delegateAction;
        // Delegate whole putValue() if delegateAction already exists
        if (dAction != null && dAction != UNITIALIZED_ACTION) {
            dAction.putValue(key, value);
            return;
        }

        if (value == null && properties == null) { // Prevent NPE from super(null) in constructor
            return;
        }
        Object oldValue;
        if ("enabled" == key) { // Same == in AbstractAction
            oldValue = enabled;
            enabled = Boolean.TRUE.equals(value);
        } else {
            synchronized (properties) {
                oldValue = properties.put(key, (value != null) ? value : MASK_NULL_VALUE);
            }
        }
        firePropertyChange(key, oldValue, value); // Checks whether oldValue.equals(value)
    }

    @Override
    public boolean isEnabled() {
        Action dAction = delegateAction;
        if (dAction != null && dAction != UNITIALIZED_ACTION) {
            return dAction.isEnabled();
        } else {
            return super.isEnabled();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        Action dAction = delegateAction;
        if (dAction != null && dAction != UNITIALIZED_ACTION) {
            dAction.setEnabled(enabled);
        } else {
            super.setEnabled(enabled);
        }
    }
    
    @Override
    public Object[] getKeys() {
        Set<String> keys = properties.keySet();
        Object[] keysArray = new Object[keys.size()]; // Do not include "enabled" (same in AbstractAction)
        keys.toArray(keysArray);
        return keysArray;
    }

    private void setAttrs(Map<String,?> attrs) {
        this.attrs = attrs;
    }

    private Action getDelegateAction() {
        Action dAction = delegateAction;
        if (dAction == UNITIALIZED_ACTION) { // Delegate should be created
            dAction = (Action) attrs.get("delegate"); // NOI18N
            if (dAction == null) {
                throw new IllegalStateException("delegate is null for wrapper action");
            }
            if (dAction instanceof AbstractEditorAction) {
                AbstractEditorAction aeAction = (AbstractEditorAction) dAction;
                // Give attributes from wrapper action to its delegate
                aeAction.setAttrs(attrs);
                transferProperties(dAction);
                aeAction.checkPreferencesKey();
                aeAction.valuesUpdated();
                // Note that delegate action will have its delegateAction left to be null
                // so it should not re-delegate (though "delegate" property is set in attrs)
            } else { // Non-AbstractEditorAction
                // Init Action.NAME (existing BaseAction instances registered by EditorActionRegistration
                // would not work properly without this)
                transferProperties(dAction);
            }
            // Sync enabled status of this according to dAction (do it after valuesUpdated())
            boolean dActionEnabled = dAction.isEnabled();
            if (isEnabled() != dActionEnabled) {
                super.setEnabled(dActionEnabled);
            }
            dAction.addPropertyChangeListener(WeakListeners.propertyChange(
                    new DelegateActionPropertyChangeListener(this), dAction));
            delegateAction = dAction;
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Delegate action created: " + dAction + '\n');
            }
        }
        return dAction;
    }
    
    private void transferProperties(Action dAction) {
        boolean log = LOG.isLoggable(Level.FINE);
        if (log) {
            LOG.fine("Transfer properties into " + dAction + '\n'); // NOI18N
        }
        synchronized (properties) {
            for (Map.Entry<String,Object> entry : properties.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value != MASK_NULL_VALUE) { // Allow to call createValue() for the property
                    if (log) {
                        LOG.fine("    key=" + key + ", value=" + value + '\n'); // NOI18N
                    }
                    dAction.putValue(key, value);
                }
            }
        }
        // Enabled status will be handled later in getDelegateAction()
    }

    private void checkPreferencesKey() {
        String preferencesKey = (String) attrs.get(PREFERENCES_KEY_KEY);
        if (preferencesKey != null) {
            preferencesNodeAndListener = new PreferencesNodeAndListener(preferencesKey);
        }
    }

    private void checkTogglePreferencesValue() {
        // Possibly toggle preferences node's value if this is a toggle action
        if (preferencesNodeAndListener != null) {
            preferencesNodeAndListener.togglePreferencesValue();
        }
    }
    
    @Override
    public String toString() {
        String clsName = getClass().getSimpleName();
        return clsName + '@' + System.identityHashCode(this) +
                " mime=\"" + getValue(MIME_TYPE_KEY) +  // NOI18N
                "\" name=\"" + actionName() + "\""; // NOI18N
    }
    
    private static final class DelegateActionPropertyChangeListener implements PropertyChangeListener {
        
        private final AbstractEditorAction wrapper;

        DelegateActionPropertyChangeListener(AbstractEditorAction wrapper) {
            this.wrapper = wrapper;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            wrapper.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            
        }
        
    }

    private final class PreferencesNodeAndListener
    implements PreferenceChangeListener, PropertyChangeListener {

        final Preferences node;

        final String key;

        boolean expectedPropertyChange;

        public PreferencesNodeAndListener(String key) {
            this.key = key;
            node = (Preferences) getValue(AbstractEditorAction.PREFERENCES_NODE_KEY);
            if (node == null) {
                throw new IllegalStateException(
                        "PREFERENCES_KEY_KEY property set but PREFERENCES_NODE_KEY not for action=" + // NOI18N
                        AbstractEditorAction.this);
            }
            node.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, this, node));
            AbstractEditorAction.this.addPropertyChangeListener(this);
            putValue(Action.SELECTED_KEY, preferencesValue());
        }

        private boolean preferencesValue() {
            boolean value = Boolean.TRUE.equals(getValue(AbstractEditorAction.PREFERENCES_DEFAULT_KEY));
            value = node.getBoolean(key, value);
            return value;
        }

        private void togglePreferencesValue() {
            boolean value = preferencesValue();
            setPreferencesValue(!value);
        }

        private void setPreferencesValue(boolean value) {
//            expectedPropertyChange = true;
            try {
                node.putBoolean(key, value);
            } finally {
//                expectedPropertyChange = false;
            }
        }

        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            boolean selected = preferencesValue();
            putValue(Action.SELECTED_KEY, selected);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!expectedPropertyChange && Action.SELECTED_KEY.equals(evt.getPropertyName())) {
                boolean selected = (Boolean) evt.getNewValue();
                setPreferencesValue(selected);
            }
        }

    }


}
