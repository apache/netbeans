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
package org.openide.awt;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.actions.Closable;
import org.netbeans.api.actions.Editable;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.actions.Printable;
import org.netbeans.api.actions.Viewable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.BooleanStateAction;
import org.openide.util.actions.SystemAction;


/** Supporting class for manipulation with menu and toolbar presenters.
*
* @author   Jaroslav Tulach
*/
public class Actions {
    /**
     * Action may {@link Action#putValue} this value to indicate that, if not enabled,
     * it should not be visible at all. Presenters may honour the request by removing
     * action's presenter from the UI.
     */
    public static final String ACTION_VALUE_VISIBLE = "openide.awt.actionVisible"; // NOI18N
    
    /**
     * Key for {@link Action#getValue} to indicate that the action should be presented
     * as toggle, if possible. Presenters may create checkbox item, toggle button etc.
     * This is to avoid accessing the {@link Action#SELECTED_KEY} actual value during
     * presenter construction, as evaluation may be expensive.
     * @since 7.71
     */
    public static final String ACTION_VALUE_TOGGLE = "openide.awt.actionToggle"; // NOI18N
    
    /**
     * @deprecated should not be used
     */
    @Deprecated
    public Actions() {}

    /**
     * Make sure an icon is not null, so that e.g. menu items for javax.swing.Action's
     * with no specified icon are correctly aligned. SystemAction already does this so
     * that is not affected.
     */
    private static Icon nonNullIcon(Icon i) {
        return null;

        /*if (i != null) {
            return i;
        } else {
            if (BLANK_ICON == null) {
                BLANK_ICON = new ImageIcon(Utilities.loadImage("org/openide/resources/actions/empty.gif", true)); // NOI18N
            }
            return BLANK_ICON;
        }*/
    }

    /** Method that finds the keydescription assigned to this action.
    * @param action action to find key for
    * @return the text representing the key or null if  there is no text assigned
    */
    public static String findKey(SystemAction action) {
        return findKey((Action) action);
    }

    /** Same method as above, but works just with plain actions.
     */
    private static String findKey(Action action) {
        if (action == null) {
            return null;
        }

        KeyStroke stroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);

        if (stroke == null) {
            return null;
        }

        return keyStrokeToString(stroke);
    }

    // Based on com.formdev.flatlaf.FlatMenuItemRenderer.getMacOSModifiersExText
    private static String getMacOSModifiersExText(int modifiersEx) {
        /* Use the proper MacOS convention, which uses single-character modifier symbols, in the
        order below, without "+" or space separators. */
        StringBuilder buf = new StringBuilder();
        if ((modifiersEx & InputEvent.CTRL_DOWN_MASK) != 0) {
            buf.append('\u2303'); // MacOS "control key" symbol.
        }
        if ((modifiersEx & (InputEvent.ALT_DOWN_MASK | InputEvent.ALT_GRAPH_DOWN_MASK)) != 0) {
            buf.append('\u2325'); // MacOS "option key" symbol.
        }
        if ((modifiersEx & InputEvent.SHIFT_DOWN_MASK) != 0) {
            buf.append('\u21E7'); // MacOS "shift key" symbol.
        }
        if ((modifiersEx & InputEvent.META_DOWN_MASK) != 0) {
            buf.append('\u2318'); // MacOS "command key" symbol.
        }
        return buf.toString();
    }

    /**
     * Creates nice textual representation of KeyStroke.
     * Modifiers and an actual key label are concated per the platform-specific convention
     * @param stroke the KeyStroke to get description of
     * @return String describing the KeyStroke
     */
    public static String keyStrokeToString( KeyStroke stroke ) {
        boolean macOS = Utilities.isMac();
        String modifText = macOS
                ? getMacOSModifiersExText(stroke.getModifiers())
                : InputEvent.getModifiersExText(stroke.getModifiers());
        String keyText = (stroke.getKeyCode() == KeyEvent.VK_UNDEFINED) ?
            String.valueOf(stroke.getKeyChar()) : getKeyText(stroke.getKeyCode());
        if (!modifText.isEmpty()) {
            if (macOS) {
                return modifText + keyText;
            } else {
                return modifText + '+' + keyText;
            }
        } else {
            return keyText;
        }
    }

    /** @return slight modification of what KeyEvent.getKeyText() returns.
     *  The numpad Left, Right, Down, Up get extra result.
     */
    private static String getKeyText(int keyCode) {
        String ret = KeyEvent.getKeyText(keyCode);
        if (ret != null) {
            switch (keyCode) {
                case KeyEvent.VK_KP_DOWN:
                    ret = prefixNumpad(ret, KeyEvent.VK_DOWN);
                    break;
                case KeyEvent.VK_KP_LEFT:
                    ret = prefixNumpad(ret, KeyEvent.VK_LEFT);
                    break;
                case KeyEvent.VK_KP_RIGHT:
                    ret = prefixNumpad(ret, KeyEvent.VK_RIGHT);
                    break;
                case KeyEvent.VK_KP_UP:
                    ret = prefixNumpad(ret, KeyEvent.VK_UP);
                    break;
            }
        }
        return ret;
    }

    private static String prefixNumpad(String key, int nonNumpadCode) {
        final String REPLACABLE_PREFIX = "KP_";
        final String usePrefix = NbBundle.getMessage(Actions.class, "key-prefix-numpad");
        final String nonNumpadName = KeyEvent.getKeyText(nonNumpadCode);
        if (key.equals(nonNumpadName)) {
            /* AWT's name for the key does not distinguish the numpad vs. non-numpad version of the
            key; add our "Numpad-" prefix. */
            return usePrefix + key;
        } else if (key.startsWith(REPLACABLE_PREFIX)) {
            /* AWT's name for the numpad key uses the somewhat confusing "KP_" prefix (e.g.
            "KP_LEFT"); use our own preferred prefix instead (e.g. "Numpad-LEFT"). */
            return usePrefix + key.substring(REPLACABLE_PREFIX.length());
        } else {
            /* AWT is using some other convention to disambiguate the numpad vs. non-numpad version
            of the key. Use AWT's name in this case. */
            return key;
        }
    }

    /** Attaches menu item to an action.
    * @param item menu item
    * @param action action
    * @param popup create popup or menu item
     * @deprecated Use {@link #connect(JMenuItem, Action, boolean)} instead.
    */
    @Deprecated
    public static void connect(JMenuItem item, SystemAction action, boolean popup) {
        connect(item, (Action) action, popup);
    }

    /** Attaches menu item to an action.
     * You can supply an alternative implementation
     * for this method by implementing method
     * {@link ButtonActionConnector#connect(JMenuItem, Action, boolean)} and
     * registering an instance of {@link ButtonActionConnector} in the
     * default lookup. 
     * <p>
     * Since version 7.1 the action can also provide properties
     * "menuText" and "popupText" if one wants to use other text on the JMenuItem
     * than the name
     * of the action taken from Action.NAME. The popupText is checked only if the
     * popup parameter is true and takes the biggest precedence. The menuText is
     * tested everytime and takes precedence over standard <code>Action.NAME</code>
     * <p>
     * By default icons are not visible in popup menus. This can be configured
     * via <a href="@TOP@architecture-summary.html#branding-org.openide.awt.USE_MNEMONICS">branding</a>.
     * 
     * @param item menu item
     * @param action action
     * @param popup create popup or menu item
     * @since 3.29
     */
    public static void connect(JMenuItem item, Action action, boolean popup) {
        for (ButtonActionConnector bac : buttonActionConnectors()) {
            if (bac.connect(item, action, popup)) {
                return;
            }
        }
        Bridge b;
        if ((item instanceof JCheckBoxMenuItem) && (action.getValue(Actions.ACTION_VALUE_TOGGLE) != null)) {
            b = new CheckMenuBridge((JCheckBoxMenuItem)item, action, popup);
        } else {
            b = new MenuBridge(item, action, popup);
        }
        b.prepare();

        if (item instanceof Actions.MenuItem) {
            ((Actions.MenuItem)item).setBridge(b);
        }
        item.putClientProperty(DynamicMenuContent.HIDE_WHEN_DISABLED, action.getValue(DynamicMenuContent.HIDE_WHEN_DISABLED));
    }

    /** Attaches checkbox menu item to boolean state action.
    * @param item menu item
    * @param action action
    * @param popup create popup or menu item
    * @deprecated Please use {@link #connect(javax.swing.JCheckBoxMenuItem, javax.swing.Action, boolean)}. 
    * Have your action to implement properly {@link Action#getValue} for {@link Action#SELECTED_KEY}
    */
    @Deprecated
    public static void connect(JCheckBoxMenuItem item, BooleanStateAction action, boolean popup) {
        Bridge b = new CheckMenuBridge(item, action, popup);
        b.prepare();
    }

    /** Attaches checkbox menu item to boolean state action. The presenter connects to the
     * {@link Action#SELECTED_KEY} action value
     * 
    * @param item menu item
    * @param action action
    * @param popup create popup or menu item
    * @since 7.71
    */
    public static void connect(JCheckBoxMenuItem item, Action action, boolean popup) {
        Bridge b = new CheckMenuBridge(item, action, popup);
        b.prepare();
    }

    /** Connects buttons to action.
    * @param button the button
    * @param action the action
     * @deprecated Use {@link #connect(AbstractButton, Action)} instead.
    */
    @Deprecated
    public static void connect(AbstractButton button, SystemAction action) {
        connect(button, (Action) action);
    }

    /** Connects buttons to action. If the action supplies value for "iconBase"
     * key from getValue(String) with a path to icons, the methods set*Icon
     * will be called on the
     * button with loaded icons using the iconBase. E.g. if the value for "iconBase"
     * is "com/mycompany/myIcon.gif" then the following images are tried
     * <ul>
     *  <li>setIcon with "com/mycompany/myIcon.gif"</li>
     *  <li>setPressedIcon with "com/mycompany/myIcon_pressed.gif"</li>
     *  <li>setDisabledIcon with "com/mycompany/myIcon_disabled.gif"</li>
     *  <li>setRolloverIcon with "com/mycompany/myIcon_rollover.gif"</li>
     *  <li>setSelectedIcon with "com/mycompany/myIcon_selected.gif"</li>
     *  <li>setRolloverSelectedIcon with "com/mycompany/myIcon_rolloverSelected.gif"</li>
     *  <li>setDisabledSelectedIcon with "com/mycompany/myIcon_disabledSelected.gif"</li>
     * </ul>
     * SystemAction has special support for iconBase - please check
     * {@link SystemAction#iconResource} for more details.
     * You can supply an alternative implementation
     * for this method by implementing method
     * {@link ButtonActionConnector#connect(AbstractButton, Action)} and
     * registering an instance of {@link ButtonActionConnector} in the
     * default lookup.
     * @param button the button
     * @param action the action
     * @since 3.29
     * @since 7.32 for set*SelectedIcon
     */
    public static void connect(AbstractButton button, Action action) {
        for (ButtonActionConnector bac : buttonActionConnectors()) {
            if (bac.connect(button, action)) {
                return;
            }
        }
        Bridge b;
        if (action instanceof BooleanStateAction) {
            b = new BooleanButtonBridge(button, (BooleanStateAction)action);
        } else if (action.getValue(Actions.ACTION_VALUE_TOGGLE) != null) {
            b = new BooleanButtonBridge(button, action);
        } else {
            b = new ButtonBridge(button, action);
        }
        b.prepare();
        button.putClientProperty(DynamicMenuContent.HIDE_WHEN_DISABLED, action.getValue(DynamicMenuContent.HIDE_WHEN_DISABLED));
    }

    /** Connects buttons to action.
    * @param button the button
    * @param action the action
    */
    public static void connect(AbstractButton button, BooleanStateAction action) {
        Bridge b = new BooleanButtonBridge(button, action);
        b.prepare();
    }

    /** Sets the text for the menu item or other subclass of AbstractButton.
    * Cut from the name '&amp;' char.
    * @param item AbstractButton
    * @param text new label
    * @param useMnemonic if true and '&amp;' char found in new text, next char is used
    *           as Mnemonic.
    * @deprecated Use either {@link AbstractButton#setText} or {@link Mnemonics#setLocalizedText(AbstractButton, String)} as appropriate.
    */
    @Deprecated
    public static void setMenuText(AbstractButton item, String text, boolean useMnemonic) {
        String msg = NbBundle.getMessage(Actions.class, "USE_MNEMONICS"); // NOI18N
        if ("always".equals(msg)) { // NOI18N
            useMnemonic = true;
        } else if ("never".equals(msg)) { // NOI18N
            useMnemonic = false;
        } else {
            assert "default".equals(msg); // NOI18N 
        }
        if (useMnemonic) {
            Mnemonics.setLocalizedText(item, text);
        } else {
            item.setText(cutAmpersand(text));
        }
    }

    /**
     * Removes an ampersand from a text string; commonly used to strip out unneeded mnemonics.
     * Replaces the first occurence of <code>&amp;?</code> by <code>?</code> or <code>(&amp;??</code> by the empty string
     * where <code>?</code> is a wildcard for any character.
     * <code>&amp;?</code> is a shortcut in English locale.
     * <code>(&amp;?)</code> is a shortcut in Japanese locale.
     * Used to remove shortcuts from workspace names (or similar) when shortcuts are not supported.
     * <p>The current implementation behaves in the same way regardless of locale.
     * In case of a conflict it would be necessary to change the
     * behavior based on the current locale.
     * @param text a localized label that may have mnemonic information in it
     * @return string without first <code>&amp;</code> if there was any
     */
    public static String cutAmpersand(String text) {
        // XXX should this also be deprecated by something in Mnemonics?

        if( null == text )
            return null;

        int i;
        String result = text;

        /* First check of occurence of '(&'. If not found check
          * for '&' itself.
          * If '(&' is found then remove '(&??'.
          */
        i = text.indexOf("(&"); // NOI18N

        if ((i >= 0) && ((i + 3) < text.length()) && /* #31093 */
                (text.charAt(i + 3) == ')')) { // NOI18N
            result = text.substring(0, i) + text.substring(i + 4);
        } else {
            //Sequence '(&?)' not found look for '&' itself
            i = text.indexOf('&');

            if (i < 0) {
                //No ampersand
                result = text;
            } else if (i == (text.length() - 1)) {
                //Ampersand is last character, wrong shortcut but we remove it anyway
                result = text.substring(0, i);
            } else {
                //Remove ampersand from middle of string
                //Is ampersand followed by space? If yes do not remove it.
                if (" ".equals(text.substring(i + 1, i + 2))) {
                    result = text;
                } else {
                    result = text.substring(0, i) + text.substring(i + 1);
                }
            }
        }

        return result;
    }
    
    //
    // Factories 
    //

    
    /** Creates new action which is always enabled. Rather than using this method
     * directly, use {@link ActionRegistration} annotation:
     * <pre>
     * {@link ActionRegistration @ActionRegistration}(displayName="#key")
     * {@link ActionID @ActionID}(id="your.pkg.action.id", category="Tools")
     * public final class Always implements {@link ActionListener} {
     *   public Always() {
     *   }
     *   public void actionPerformed({@link ActionEvent} e) {
     *    // your code
     *   }
     * }
     * </pre>
     * This method can also be used from 
     * <a href="@org-openide-modules@/org/openide/modules/doc-files/api.html#how-layer">XML Layer</a> 
     * directly by following XML definition:
     * <pre>
     * &lt;file name="your-pkg-action-id.instance"&gt;
     *   &lt;attr name="instanceCreate" methodvalue="org.openide.awt.Actions.alwaysEnabled"/&gt;
     *   &lt;attr name="delegate" methodvalue="your.pkg.YourAction.factoryMethod"/&gt;
     *   &lt;attr name="displayName" bundlevalue="your.pkg.Bundle#key"/&gt;
     *   &lt;attr name="iconBase" stringvalue="your/pkg/YourImage.png"/&gt;
     *   &lt;!-- if desired: &lt;attr name="noIconInMenu" boolvalue="true"/&gt; --&gt;
     *   &lt;!-- if desired: &lt;attr name="asynchronous" boolvalue="true"/&gt; --&gt;
     * &lt;/file&gt;
     * </pre>
     * In case the "delegate" is not just {@link ActionListener}, but also
     * {@link Action}, the returned action acts as a lazy proxy - it defers initialization
     * of the action itself, but as soon as it is created, it delegates all queries
     * to it. This way one can create an action that looks statically enabled, and as soon
     * as user really uses it, it becomes active - it can change its name, it can
     * change its enabled state, etc.
     *
     * 
     * @param delegate the task to perform when action is invoked
     * @param displayName the name of the action
     * @param iconBase the location to the actions icon
     * @param noIconInMenu true if this icon shall not have an item in menu
     * @since 7.3
     */
    public static Action alwaysEnabled(
        ActionListener delegate, String displayName, String iconBase, boolean noIconInMenu
    ) {
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("delegate", delegate); // NOI18N
        map.put("displayName", displayName); // NOI18N
        map.put("iconBase", iconBase); // NOI18N
        map.put("noIconInMenu", noIconInMenu); // NOI18N
        return alwaysEnabled(map);
    }
    // for use from layers
    static Action alwaysEnabled(Map map) {
        return AlwaysEnabledAction.create(map);
    }

    /** Creates action which represents a boolean value in {@link java.util.prefs.Preferences}.
     * When added to a menu the action is presented as a JCheckBox.
     * This method can also be used from
     * <a href="@org-openide-modules@/org/openide/modules/doc-files/api.html#how-layer">XML Layer</a>
     * directly by following XML definition:
     * <pre>
     * &lt;file name="your-pkg-action-id.instance"&gt;
     *   &lt;attr name="preferencesNode" methodvalue="method-returning-Preferences-instance" or
     *                                   methodvalue="method-returning-Lookup-that-contains-Preferences-instance" or
     *                                   stringvalue="see below for the preferencesNode parameter description"
     * /&gt;
     *   &lt;attr name="preferencesKey" stringvalue="preferences-key-name"/&gt;
     *   &lt;attr name="instanceCreate" methodvalue="org.openide.awt.Actions.checkbox"/&gt;
     *   &lt;attr name="displayName" bundlevalue="your.pkg.Bundle#key"/&gt;
     *   &lt;attr name="iconBase" stringvalue="your/pkg/YourImage.png"/&gt;
     *   &lt;!-- if desired: &lt;attr name="noIconInMenu" boolvalue="true"/&gt; --&gt;
     *   &lt;!-- if desired: &lt;attr name="asynchronous" boolvalue="true"/&gt; --&gt;
     * &lt;/file&gt;
     * </pre>
     *
     * @param preferencesNode It's one of:
     * <ul>
     *   <li>Absolute path to preferences node under <code>NbPreferences.root()</code>.</li>
     *   <li>"system:" followed by absolute path to preferences node under <code>Preferences.systemRoot()</code>.</li>
     *   <li>"user:" followed by absolute path to preferences node under <code>Preferences.userRoot()</code>.</li>
     * </ul>
     * @param preferencesKey name of the preferences key.
     * @param displayName the name of the action
     * @param iconBase the location to the actions icon
     * @param noIconInMenu true if this icon shall not have an item in menu
     * @since 7.17
     */
    public static Action checkbox(
        String preferencesNode, String preferencesKey,
        String displayName, String iconBase, boolean noIconInMenu
    ) {
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("preferencesNode", preferencesNode); // NOI18N
        map.put("preferencesKey", preferencesKey); // NOI18N
        map.put("displayName", displayName); // NOI18N
        map.put("iconBase", iconBase); // NOI18N
        map.put("noIconInMenu", noIconInMenu); // NOI18N
        return checkbox(map);
    }
    // for use from layers
    static Action checkbox(Map map) {
        return AlwaysEnabledAction.create(map);
    }

    /** Creates new "callback" action. Such action has an assigned key
     * which is used to find proper delegate in {@link ActionMap} of currently
     * active component. You can use {@link ActionRegistration} annotation to 
     * register your action:
     * <pre>
     * {@link ActionRegistration @ActionRegistration}(displayName="#Key", <b>key="KeyInActionMap"</b>)
     * {@link ActionID @ActionID}(category="Tools", id = "action.pkg.ClassName")
     * public final class Fallback implements {@link ActionListener} {
     *   public void actionPerformed({@link ActionEvent} e) {
     *    // your code
     *   }
     * }
     * </pre>
     * If you want to create callback action without any fallback implementation,
     * you can annotate any string constant:
     * <pre>
     * {@link ActionRegistration @ActionRegistration}(displayName = "#Key")
     * {@link ActionID @ActionID}(category = "Edit", id = "my.field.action")
     * public static final String ACTION_MAP_KEY = <b>"KeyInActionMap"</b>;
     * </pre>
     * <p>
     * This action can be lazily declared in a
     * <a href="@org-openide-modules@/org/openide/modules/doc-files/api.html#how-layer">
     * layer file</a> using following XML snippet:
     * <pre>
     * &lt;file name="action-pkg-ClassName.instance"&gt;
     *   &lt;attr name="instanceCreate" methodvalue="org.openide.awt.Actions.callback"/&gt;
     *   &lt;attr name="key" stringvalue="KeyInActionMap"/&gt;
     *   &lt;attr name="surviveFocusChange" boolvalue="false"/&gt; &lt;!-- defaults to false --&gt;
     *   &lt;attr name="fallback" newvalue="action.pkg.DefaultAction"/&gt; &lt;!-- may be missing --&gt;
     *   &lt;attr name="displayName" bundlevalue="your.pkg.Bundle#key"/&gt;
     *   &lt;attr name="iconBase" stringvalue="your/pkg/YourImage.png"/&gt;
     *   &lt;!-- if desired: &lt;attr name="noIconInMenu" boolvalue="true"/&gt; --&gt;
     *   &lt;!-- if desired: &lt;attr name="asynchronous" boolvalue="true"/&gt; --&gt;
     * &lt;/file&gt;
     * </pre>
     * 
     *
     * @param key the key to search for in an {@link ActionMap}
     * @param surviveFocusChange true to remember action provided by previously
     *   active component even some other component is currently active
     * @param fallback action to delegate to when no key found. Use <code>null</code>
     *   to make the action disabled if delegate assigned to key is missing
     * @param displayName localized name of the action (including ampersand)
     * @param iconBase the location to the action icon
     * @param noIconInMenu true if this icon shall not have an item in menu
     * @return creates new action associated with given key
     * @since 7.10
     */
    public static ContextAwareAction callback(
        String key, Action fallback, boolean surviveFocusChange,
        String displayName, String iconBase, boolean noIconInMenu
    ) {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("key", key); // NOI18N
        map.put("surviveFocusChange", surviveFocusChange); // NOI18N
        map.put("fallback", fallback); // NOI18N
        map.put("displayName", displayName); // NOI18N
        map.put("iconBase", iconBase); // NOI18N
        map.put("noIconInMenu", noIconInMenu); // NOI18N
        return callback(map);
    }
    static ContextAwareAction callback(Map fo) {
        return GeneralAction.callback(fo);
    }

    /** Creates new "context" action, an action that observes the current
     * selection for a given type and enables if instances of given type are
     * present. Common interfaces to watch for include {@link Openable},
     * {@link Editable}, {@link Closable}, {@link Viewable}, and any interfaces
     * defined and exposed by various other APIs. Use {@link ActionRegistration} 
     * annotation to register your action::
     * <pre>
     * {@link ActionRegistration @ActionRegistration}(displayName="#Key")
     * {@link ActionID @ActionID}(category="Tools", id = "action.pkg.YourClass")
     * public final class YourClass implements {@link ActionListener} {
     *    Openable context;
     *
     *    public YourClass(Openable context) {
     *      this.context = context;
     *    }
     *
     *    public void actionPerformed({@link ActionEvent} ev) {
     *       // do something with context
     *    }
     * }
     * </pre>
     * In case you are interested in creating multi selection action, just
     * change parameters of your constructor:
     * <pre>
     * {@link ActionRegistration @ActionRegistration}(displayName="#Key")
     * {@link ActionID @ActionID}(category="Tools", id = "action.pkg.YourClass")
     * public final class YourClass implements {@link ActionListener} {
     *    List&lt;Openable&gt; context;
     *
     *    public YourClass(List&lt;Openable&gt; context) {
     *      this.context = context;
     *    }
     *
     *    public void actionPerformed({@link ActionEvent} ev) {
     *       // do something with context
     *    }
     * }
     * </pre>
     * <p>
     * Actions of this kind can be declared in
     * <a href="@org-openide-modules@/org/openide/modules/doc-files/api.html#how-layer">
     * layer file</a> using following XML snippet:
     * <pre>
     * &lt;file name="action-pkg-ClassName.instance"&gt;
     *   &lt;attr name="instanceCreate" methodvalue="org.openide.awt.Actions.context"/&gt;
     *   &lt;attr name="type" stringvalue="org.netbeans.api.actions.Openable"/&gt;
     *   &lt;attr name="selectionType" stringvalue="ANY"/&gt; &lt;-- or EXACTLY_ONE --&gt;
     *   &lt;attr name="delegate" newvalue="action.pkg.YourAction"/&gt;
     * 
     *   &lt;!--
     *      Similar registration like in case of "callback" action.
     *      May be missing completely:
     *   --&gt;
     *   &lt;attr name="key" stringvalue="KeyInActionMap"/&gt;
     *   &lt;attr name="surviveFocusChange" boolvalue="false"/&gt; 
     *   &lt;attr name="displayName" bundlevalue="your.pkg.Bundle#key"/&gt;
     *   &lt;attr name="iconBase" stringvalue="your/pkg/YourImage.png"/&gt;
     *   &lt;!-- if desired: &lt;attr name="noIconInMenu" boolvalue="true"/&gt; --&gt;
     *   &lt;!-- if desired: &lt;attr name="asynchronous" boolvalue="true"/&gt; --&gt;
     * &lt;/file&gt;
     * </pre>
     * In the previous case there has to be a class with public default constructor
     * named <code>action.pkg.YourAction</code>. It has to implement
     * {@link ContextAwareAction} interface. Its {@link ContextAwareAction#createContextAwareInstance(org.openide.util.Lookup)}
     * method is called when the action is invoked. The passed in {@link Lookup}
     * contains instances of the <code>type</code> interface, <code>actionPerformed</code>
     * is then called on the returned clone.
     * <p>
     * Alternatively one can use support for simple dependency injection by
     * using following attributes:
     * <pre>
     * &lt;file name="action-pkg-ClassName.instance"&gt;
     *   &lt;attr name="instanceCreate" methodvalue="org.openide.awt.Actions.context"/&gt;
     *   &lt;attr name="type" stringvalue="org.netbeans.api.actions.Openable"/&gt;
     *   &lt;attr name="delegate" methodvalue="org.openide.awt.Actions.inject"/&gt;
     *   &lt;attr name="selectionType" stringvalue="EXACTLY_ONE"/&gt;
     *   &lt;attr name="injectable" stringvalue="pkg.YourClass"/&gt;
     *   &lt;attr name="displayName" bundlevalue="your.pkg.Bundle#key"/&gt;
     *   &lt;attr name="iconBase" stringvalue="your/pkg/YourImage.png"/&gt;
     *   &lt;!-- if desired: &lt;attr name="noIconInMenu" boolvalue="true"/&gt; --&gt;
     * &lt;/file&gt;
     * </pre>
     * where <code>pkg.YourClass</code> is defined with public constructor taking
     * <code>type</code>:
     * <pre>
     * public final class YourClass implements ActionListener {
     *    Openable context;
     *
     *    public YourClass(Openable context) {
     *      this.context = context;
     *    }
     *
     *    public void actionPerformed(ActionEvent ev) {
     *       // do something with context
     *    }
     * }
     * </pre>
     * The instance of this class is created when the action is invoked and
     * its constructor is fed with the instance of <code>type</code> inside
     * the active context. <code>actionPerformed</code> method is called then.
     * <p>
     * To create action that handled multiselection
     * one can use following XML snippet:
     * <pre>
     * &lt;file name="action-pkg-ClassName.instance"&gt;
     *   &lt;attr name="type" stringvalue="org.netbeans.api.actions.Openable"/&gt;
     *   &lt;attr name="delegate" methodvalue="org.openide.awt.Actions.inject"/&gt;
     *   &lt;attr name="selectionType" stringvalue="ANY"/&gt;
     *   &lt;attr name="injectable" stringvalue="pkg.YourClass"/&gt;
     *   &lt;attr name="displayName" bundlevalue="your.pkg.Bundle#key"/&gt;
     *   &lt;attr name="iconBase" stringvalue="your/pkg/YourImage.png"/&gt;
     *   &lt;!-- if desired: &lt;attr name="noIconInMenu" boolvalue="true"/&gt; --&gt;
     *   &lt;!-- since 7.33: &lt;attr name="context" newvalue="org.my.own.LookupImpl"/&gt; --&gt;
     * &lt;/file&gt;
     * </pre>
     * Now the constructor of <code>YourClass</code> needs to have following
     * form:
     * <pre>
     * public final class YourClass implements ActionListener {
     *    List&lt;Openable&gt; context;
     *
     *    public YourClass(List&lt;Openable&gt; context) {
     *      this.context = context;
     *    }
     *  }
     * </pre>
     * <p>
     * Further attributes are defined to control action's enabled and checked state. 
     * Attributes which control enable state are prefixed by "{@code enableOn}". Attributes
     * controlling checked state have prefix "{@code checkedOn}":
     * <pre><code>
     * &lt;file name="action-pkg-ClassName.instance"&gt;
     *   &lt;!-- Enable on certain type in Lookup --&gt;
     *   &lt;attr name="enableOnType" stringvalue="qualified.type.name"/&gt;
     * 
     *   &lt;!-- Monitor specific property in that type --&gt;
     *   &lt;attr name="enableOnProperty" stringvalue="propertyName"/&gt;
     * 
     *   &lt;!-- The property value, which corresponds to enabled action.
     *           Values "#null" and "#non-null" are treated specially.
     *   --&gt;
     *   &lt;attr name="enableOnValue" stringvalue="propertyName"/&gt;
     * 
     *   &lt;!-- Name of custom listener interface --&gt;
     *   &lt;attr name="enableOnChangeListener" stringvalue="qualifier.listener.interface"/&gt;
     * 
     *   &lt;!-- Name of listener method that triggers state re-evaluation  --&gt;
     *   &lt;attr name="enableOnMethod" stringvalue="methodName"/&gt;
     * 
     *   &lt;!-- Delegate to the action instance for final decision --&gt;
     *   &lt;attr name="enableOnActionProperty" stringvalue="actionPropertyName"/&gt;
     * 
     *   &lt;!-- ... --&gt;
     * 
     * &lt;/file&gt;
     * 
     * </code></pre>
     *
     * @param type the object to seek for in the active context
     * @param single shall there be just one or multiple instances of the object
     * @param surviveFocusChange shall the action remain enabled and act on
     *    previous selection even if no selection is currently in context?
     * @param delegate action to call when this action is invoked
     * @param key alternatively an action can be looked up in action map
     *    (see {@link Actions#callback(java.lang.String, javax.swing.Action, boolean, java.lang.String, java.lang.String, boolean)})
     * @param displayName localized name of the action (including ampersand)
     * @param iconBase the location to the action icon
     * @param noIconInMenu true if this icon shall not have an item in menu
     * @return new instance of context aware action watching for type
     * @since 7.10
     */
    public static ContextAwareAction context(
        Class<?> type,
        boolean single,
        boolean surviveFocusChange,
        ContextAwareAction delegate,
        String key,
        String displayName, String iconBase, boolean noIconInMenu
    ) {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("key", key); // NOI18N
        map.put("surviveFocusChange", surviveFocusChange); // NOI18N
        map.put("delegate", delegate); // NOI18N
        map.put("type", type); // NOI18N
        map.put("selectionType", single ? ContextSelection.EXACTLY_ONE : ContextSelection.ANY);
        map.put("displayName", displayName); // NOI18N
        map.put("iconBase", iconBase); // NOI18N
        map.put("noIconInMenu", noIconInMenu); // NOI18N
        return GeneralAction.context(map, true);
    }
    static Action context(Map fo) {
        Object context = fo.get("context");
        if (context instanceof Lookup) {
            Lookup lkp = (Lookup)context;
            return GeneralAction.bindContext(fo, lkp);
        } else {
            return GeneralAction.context(fo);
        }
    }
    static ContextAction.Performer<?> inject(final Map fo) {
        Object t = fo.get("selectionType"); // NOI18N
        if (ContextSelection.EXACTLY_ONE.toString().equals(t)) {
            return new InjectorExactlyOne(fo);
        }
        if (ContextSelection.ANY.toString().equals(t)) {
            return new InjectorAny(fo);
        }
        throw new IllegalStateException("no selectionType parameter in " + fo); // NOI18N
    }
    static ContextAction.Performer<?> performer(final Map fo) {
        String type = (String)fo.get("type");
        if (type.equals(Openable.class.getName())) {
            return new ActionDefaultPerfomer(0);
        }
        if (type.equals(Viewable.class.getName())) {
            return new ActionDefaultPerfomer(1);
        }
        if (type.equals(Editable.class.getName())) {
            return new ActionDefaultPerfomer(2);
        }
        if (type.equals(Closable.class.getName())) {
            return new ActionDefaultPerfomer(3);
        }
        if (type.equals(Printable.class.getName())) {
            return new ActionDefaultPerfomer(4);
        }
        throw new IllegalStateException(type);
    }

    /**
     * Locates a specific action programmatically.
     * The action will typically have been registered using {@link ActionRegistration}.
     * <p>Normally an {@link ActionReference} will suffice to insert the action
     * into various UI elements (typically using {@link Utilities#actionsForPath}),
     * but in special circumstances you may need to find a single known action.
     * This method is just a shortcut for using {@link FileUtil#getConfigObject}
     * with the correct arguments, plus using {@link AcceleratorBinding#setAccelerator}.
     * @param category as in {@link ActionID#category}
     * @param id as in {@link ActionID#id}
     * @return the action registered under that ID, or null
     * @throws IllegalArgumentException if a corresponding {@link ActionID} would have been rejected
     * @since 7.42
     */
    public static Action forID(String category, String id) throws IllegalArgumentException {
        // copied from ActionProcessor:
        if (category.startsWith("Actions/")) {
            throw new IllegalArgumentException("category should not start with Actions/: " + category);
        }
        if (!FQN.matcher(id).matches()) {
            throw new IllegalArgumentException("id must be valid fully qualified name: " + id);
        }
        String path = "Actions/" + category + "/" + id.replace('.', '-') + ".instance";
        Action a = FileUtil.getConfigObject(path, Action.class);
        if (a == null) {
            return null;
        }
        FileObject def = FileUtil.getConfigFile(path);
        if (def != null) {
            AcceleratorBinding.setAccelerator(a, def);
        }
        return a;
    }
    private static final String IDENTIFIER = "(?:\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)"; // NOI18N
    private static final Pattern FQN = Pattern.compile(IDENTIFIER + "(?:[.]" + IDENTIFIER + ")*"); // NOI18N
    
    /** Extracts help from action.
     */
    private static HelpCtx findHelp(Action a) {
        if (a instanceof HelpCtx.Provider) {
            return ((HelpCtx.Provider) a).getHelpCtx();
        } else {
            return HelpCtx.DEFAULT_HELP;
        }
    }

    // #40824 - when the text changes, it's too late to update in JmenuPlus.popup.show() (which triggers the updateState() in the MenuBridge).
    // check JmenuPlus.setPopupMenuVisible()
    static void prepareMenuBridgeItemsInContainer(Container c) {
        Component[] comps = c.getComponents();

        for (int i = 0; i < comps.length; i++) {
            if (comps[i] instanceof JComponent) {
                JComponent cop = (JComponent) comps[i];
                MenuBridge bridge = (MenuBridge) cop.getClientProperty("menubridgeresizehack");

                if (bridge != null) {
                    bridge.updateState(null);
                }
            }
        }
    }

    //
    // Methods for configuration of MenuItems
    //

    /** Method to prepare the margins and text positions.
    */
    static void prepareMargins(JMenuItem item, Action action) {
        item.setHorizontalTextPosition(JMenuItem.RIGHT);
        item.setHorizontalAlignment(JMenuItem.LEFT);
    }

    /** Updates value of the key
    * @param item item to update
    * @param action the action to update
    */
    static void updateKey(JMenuItem item, Action action) {
        if (!(item instanceof JMenu)) {
            item.setAccelerator((KeyStroke) action.getValue(Action.ACCELERATOR_KEY));
        }
    }

    /** Interface for the creating Actions.SubMenu. It provides the methods for
    * all items in submenu: name shortcut and perform method. Also has methods
    * for notification of changes of the model.
     * @deprecated used by deprecated {@link SubMenu}
     */
    @Deprecated
    public static interface SubMenuModel {
        /** @return count of the submenu items. */
        public int getCount();

        /** Gets label for specific index
        * @param index of the submenu item
        * @return label for this menu item (or <code>null</code> for a separator)
        */
        public String getLabel(int index);

        /** Gets shortcut for specific index
        * @index of the submenu item
        * @return menushortcut for this menu item
        */

        //    public MenuShortcut getMenuShortcut(int index);

        /** Get context help for the specified item.
        * This can be used to associate help with individual items.
        * You may return <code>null</code> to just use the context help for
        * the associated system action (if any).
        * Note that only help IDs will work, not URLs.
        * @return the context help, or <code>null</code>
        */
        public HelpCtx getHelpCtx(int index);

        /** Perform the action on the specific index
        * @param index of the submenu item which should be performed
        */
        public void performActionAt(int index);

        /** Adds change listener for changes of the model.
        */
        public void addChangeListener(ChangeListener l);

        /** Removes change listener for changes of the model.
        */
        public void removeChangeListener(ChangeListener l);
    }

    /** Listener on showing/hiding state of the component.
    * Is attached to menu or toolbar item in prepareXXX methods and
    * method addNotify is called when the item is showing and
    * the method removeNotify is called when the item is hidding.
    * <P>
    * There is a special support listening on changes in the action and
    * if such change occures, updateState method is called to
    * reflect it.
    */
    private abstract static class Bridge extends Object implements PropertyChangeListener {
        /** component to work with */
        protected JComponent comp;

        /** action to associate */
        protected Action action;
        
        private final PropertyChangeListener actionL;
        /** @param comp component
        * @param action the action
        */
        public Bridge(JComponent comp, Action action) {
            if(comp == null || action == null) {
                throw new IllegalArgumentException(
                    "None of the arguments can be null: comp=" + comp + //NOI18N
                    ", action=" + action); // NOI18N
            }
            this.comp = comp;
            this.action = action;

            actionL = WeakListeners.propertyChange(this, action);

            // associate context help, if applicable
            // [PENDING] probably belongs in ButtonBridge.updateState to make it dynamic
            HelpCtx help = findHelp(action);

            if ((help != null) && !help.equals(HelpCtx.DEFAULT_HELP) && (help.getHelpID() != null)) {
                HelpCtx.setHelpIDString(comp, help.getHelpID());
            }
        }

        protected void prepare() {
            comp.addPropertyChangeListener(new VisL());
            if (comp.isShowing()) {
                addNotify();
            } else {
                updateState(null);
            }
        }

        /** Attaches listener to given action */
        final void addNotify() {
            action.addPropertyChangeListener(actionL);
            updateState(null);
        }

        /** Remove the listener */
        final void removeNotify() {
            action.removePropertyChangeListener(actionL);
        }

        /** @param changedProperty the name of property that has changed
        * or null if it is not known
        */
        public abstract void updateState(String changedProperty);

        /** Listener to changes of some properties.
        * Multicast - reacts to keymap changes and ancestor changes
        * together.
        */
        public void propertyChange(final PropertyChangeEvent ev) {
            //assert EventQueue.isDispatchThread();
            if (!EventQueue.isDispatchThread()) {
                new IllegalStateException("This must happen in the event thread!").printStackTrace();
            }

            updateState(ev.getPropertyName());
        }

        // Must be separate from general PCL, because otherwise
        // SystemAction.PROP_ENABLED -> updateState("enabled") ->
        // button.setEnabled(...) -> JButton.PROP_ENABLED ->
        // updateState("enabled") -> button.setEnabled(same)
        private class VisL implements PropertyChangeListener {
            VisL() {
            }

            public void propertyChange(final PropertyChangeEvent ev) {
                if ("ancestor".equals(ev.getPropertyName())) {
                    // ancestor change - decide if parent is null or not
                    if (ev.getNewValue() != null) {
                        addNotify();
                    } else {
                        removeNotify();
                    }
                }
            }
        }
    }

    /** Bridge between an action and button.
    */
    private static class ButtonBridge extends Bridge 
    implements ActionListener {
        /** UI logger to notify about invocation of an action */
        private static Logger UILOG = Logger.getLogger("org.netbeans.ui.actions"); // NOI18N
        
        /** the button */
        protected AbstractButton button;

        public ButtonBridge(AbstractButton button, Action action) {
            super(button, action);
            button.addActionListener(action);
            this.button = button;
            button.addActionListener(this);
        }
        
        public void actionPerformed(ActionEvent ev) {
            LogRecord rec = new LogRecord(Level.FINER, "UI_ACTION_BUTTON_PRESS"); // NOI18N
            rec.setParameters(new Object[] { button, button.getClass().getName(), action, action.getClass().getName(), action.getValue(Action.NAME) });
            rec.setResourceBundle(NbBundle.getBundle(Actions.class));
            rec.setResourceBundleName(Actions.class.getPackage().getName() + ".Bundle"); // NOI18N
            rec.setLoggerName(UILOG.getName());
            UILOG.log(rec);
        }

        protected void updateButtonIcon() {
            Object i = null;
            Object base = action.getValue("iconBase"); // NOI18N
            boolean useSmallIcon = true;
            Object prop = button.getClientProperty("PreferredIconSize"); //NOI18N

            if (prop instanceof Integer) {
                if (((Integer) prop).intValue() == 24) {
                    useSmallIcon = false;
                }
            }

            if (action instanceof SystemAction) {
                if (base instanceof String) {
                    String b = (String) base;
                    ImageIcon imgIcon = loadImage(b, useSmallIcon, null);
                    if (imgIcon != null) {
                        i = imgIcon;
                        button.setIcon(imgIcon);
                        button.setDisabledIcon(ImageUtilities.createDisabledIcon(imgIcon));
                    } else {
                        SystemAction sa = (SystemAction) action;
                        i = sa.getIcon(useTextIcons());
                        button.setIcon((Icon) i);
                        button.setDisabledIcon(ImageUtilities.createDisabledIcon((Icon) i));
                    }
                } else {
                    SystemAction sa = (SystemAction) action;
                    i = sa.getIcon(useTextIcons());
                    button.setIcon((Icon) i);
                    button.setDisabledIcon(ImageUtilities.createDisabledIcon((Icon) i));
                }
            } else {
                //Try to get icon from iconBase for non SystemAction action
                if (base instanceof String) {
                    String b = (String) base;
                    ImageIcon imgIcon = loadImage(b, useSmallIcon, null); // NOI18N
                    if (imgIcon != null) {
                        i = imgIcon;
                        button.setIcon((Icon) i);
                        button.setDisabledIcon(ImageUtilities.createDisabledIcon(imgIcon));
                    } else {
                        i = action.getValue(Action.SMALL_ICON);
                        if (i instanceof Icon) {
                            button.setIcon((Icon) i);
                            button.setDisabledIcon(ImageUtilities.createDisabledIcon((Icon) i));
                        } else {
                            button.setIcon(nonNullIcon(null));
                        }
                    }
                } else {
                    i = action.getValue(Action.SMALL_ICON);
                    if (i instanceof Icon) {
                        button.setIcon((Icon) i);
                        button.setDisabledIcon(ImageUtilities.createDisabledIcon((Icon) i));
                    } else {
                        button.setIcon(nonNullIcon(null));
                    }
                }
            }

            if (base instanceof String) {
                String b = (String) base;

                ImageIcon imgIcon = null;

                if (i == null) {
                    // even for regular icon
                    imgIcon = loadImage(b, useSmallIcon, null);
                    if (imgIcon != null) {
                        button.setIcon(imgIcon);
                    }
                    i = imgIcon;
                }

                ImageIcon pImgIcon = loadImage(b, useSmallIcon, "_pressed"); // NOI18N
                if (pImgIcon != null) {
                    button.setPressedIcon(pImgIcon);
                }

                ImageIcon rImgIcon = loadImage(b, useSmallIcon, "_rollover"); // NOI18N
                if (rImgIcon != null) {
                    button.setRolloverIcon(rImgIcon);
                }


                ImageIcon dImgIcon = loadImage(b, useSmallIcon, "_disabled"); // NOI18N
                if (dImgIcon != null) {
                    button.setDisabledIcon(dImgIcon);
                } else if (imgIcon != null) {
                    button.setDisabledIcon(ImageUtilities.createDisabledIcon(imgIcon));
                }

                ImageIcon sImgIcon = loadImage(b, useSmallIcon, "_selected"); // NOI18N
                if (sImgIcon != null) {
                    button.setSelectedIcon(sImgIcon);
                }

                sImgIcon = loadImage(b, useSmallIcon, "_rolloverSelected"); // NOI18N
                if (sImgIcon != null) {
                    button.setRolloverSelectedIcon(sImgIcon);
                }

                sImgIcon = loadImage(b, useSmallIcon, "_disabledSelected"); // NOI18N
                if (sImgIcon != null) {
                    button.setDisabledSelectedIcon(sImgIcon);
                }
            }
        }

        static ImageIcon loadImage(String iconBase, boolean useSmallIcon, String suffix) {
            if (!useSmallIcon) {
                String bigBase = insertBeforeSuffix(iconBase, "24"); // NOI18N
                ImageIcon icon = ImageUtilities.loadImageIcon(insertBeforeSuffix(bigBase, suffix), true);
                if (icon != null) {
                    return icon;
                }
            }
            return ImageUtilities.loadImageIcon(insertBeforeSuffix(iconBase, suffix), true); // NOI18N
        }
        
        static String insertBeforeSuffix(String path, String toInsert) {
            if (toInsert == null) {
                return path;
            }
            String withoutSuffix = path;
            String suffix = ""; // NOI18N

            if (path.lastIndexOf('.') >= 0) {
                withoutSuffix = path.substring(0, path.lastIndexOf('.'));
                suffix = path.substring(path.lastIndexOf('.'), path.length());
            }

            return withoutSuffix + toInsert + suffix;
        }

        /** @param changedProperty the name of property that has changed
        * or null if it is not known
        */
        public void updateState(String changedProperty) {
            // note: "enabled" (== SA.PROP_ENABLED) hardcoded in AbstractAction
            if ((changedProperty == null) || changedProperty.equals(SystemAction.PROP_ENABLED)) {
                button.setEnabled(action.isEnabled());
            }

            if (
                (changedProperty == null) || changedProperty.equals(SystemAction.PROP_ICON) ||
                    changedProperty.equals(Action.SMALL_ICON) || changedProperty.equals("iconBase")
            ) { // NOI18N
                updateButtonIcon();
            }

            if (
                (changedProperty == null) || changedProperty.equals(Action.ACCELERATOR_KEY) ||
                    (changedProperty.equals(Action.NAME) && (action.getValue(Action.SHORT_DESCRIPTION) == null)) ||
                    changedProperty.equals(Action.SHORT_DESCRIPTION)
            ) {
                String tip = findKey(action);
                String toolTip = (String) action.getValue(Action.SHORT_DESCRIPTION);

                if (toolTip == null) {
                    toolTip = (String) action.getValue(Action.NAME);
                    toolTip = (toolTip == null) ? "" : cutAmpersand(toolTip);
                }

                if ((tip == null) || tip.equals("")) { // NOI18N
                    button.setToolTipText(toolTip);
                } else {
                    button.setToolTipText(
                        org.openide.util.NbBundle.getMessage(Actions.class, "FMT_ButtonHint", toolTip, tip)
                    );
                }
            }
            
            if (
                button instanceof javax.accessibility.Accessible &&
                    ((changedProperty == null) || changedProperty.equals(Action.NAME))
            ) {
                button.getAccessibleContext().setAccessibleName((String) action.getValue(Action.NAME));
            }
        }

        /** Should textual icons be used when lacking a real icon?
        * In the default implementation, <code>true</code>.
        * @return <code>true</code> if so
        */
        protected boolean useTextIcons() {
            return true;
        }
    }

    /** Bridge for button and boolean action.
    */
    private static class BooleanButtonBridge extends ButtonBridge {
        private final BooleanStateAction stateAction;
        private final PropertyChangeListener bsaL;
        
        public BooleanButtonBridge(AbstractButton button, BooleanStateAction bsa) {
            super(button, bsa);
            this.stateAction = bsa;
            if (bsa != null && bsa != action) {
                bsaL = WeakListeners.propertyChange(this, BooleanStateAction.PROP_BOOLEAN_STATE, bsa);
                bsa.addPropertyChangeListener(bsaL);
            } else {
                bsaL = null;
            }
        }

        public BooleanButtonBridge(AbstractButton button, Action action) {
            super(button, action);
            this.stateAction = null;
            this.bsaL = null;
        }

        /** @param changedProperty the name of property that has changed
        * or null if it is not known
        */
        @Override
        public void updateState(String changedProperty) {
            super.updateState(changedProperty);

            if ((changedProperty == null) || 
                    changedProperty.equals(BooleanStateAction.PROP_BOOLEAN_STATE) ||
                    (bsaL == null && changedProperty.equals(Action.SELECTED_KEY))) {
                button.setSelected(getBooleanState());
            }
        }
        
        protected boolean getBooleanState() {
            if (action instanceof AlwaysEnabledAction.CheckBox) {
                return ((AlwaysEnabledAction.CheckBox)action).isPreferencesSelected();
            }
            return stateAction != null ? stateAction.getBooleanState() :
                    Boolean.TRUE.equals(action.getValue(Action.SELECTED_KEY));
        }
    }

    /** Menu item bridge.
    */
    private static class MenuBridge extends ButtonBridge {
        /** behave like menu or popup */
        private boolean popup;

        /** Constructor.
        * @param popup pop-up menu
        */
        public MenuBridge(JMenuItem item, Action action, boolean popup) {
            super(item, action);
            this.popup = popup;
            
            if (popup) {
                prepareMargins(item, action);
            } else {
                // #40824 hack
                item.putClientProperty("menubridgeresizehack", this);

                // #40824 hack end.
            }
        }

        protected @Override void prepare() {
            if (popup) {
                // popups generally get no hierarchy events, yet we need to listen to other changes
                addNotify();
            } else {
                super.prepare();
            }
        }

        /** @param changedProperty the name of property that has changed
        * or null if it is not known
        */
        @Override
        public void updateState(String changedProperty) {
            if (this.button == null) {
                this.button = (AbstractButton) this.comp;
            }
            if ((changedProperty == null) || changedProperty.equals(SystemAction.PROP_ENABLED)) {
                button.setEnabled(action.isEnabled());
            }

            if ((changedProperty == null) || !changedProperty.equals(Action.ACCELERATOR_KEY)) {
                updateKey((JMenuItem) comp, action);
            }

            if (!popup) {
                if (
                    (changedProperty == null) || changedProperty.equals(SystemAction.PROP_ICON) ||
                        changedProperty.equals(Action.SMALL_ICON) || changedProperty.equals("iconBase")
                ) { // NOI18N
                    updateButtonIcon();
                }
            }

            if ((changedProperty == null) || changedProperty.equals(Action.NAME)) {
                Object s = null;
                boolean useMnemonic = true;
                if (popup) {
                    s = action.getValue("popupText"); // NOI18N
                }
                if (s == null) {
                    s = action.getValue("menuText"); // NOI18N
                    useMnemonic = !popup;
                }
                if (s == null) {
                    s = action.getValue(Action.NAME);
                    useMnemonic = !popup;
                }

                if (s instanceof String) {
                    setMenuText(((JMenuItem) comp), (String) s, useMnemonic);

                    //System.out.println("Menu item: " + s);
                    //System.out.println("Action class: " + action.getClass());
                }
            }
        }

        @Override
        protected void updateButtonIcon() {
            Object i = null;
            Object obj = action.getValue("noIconInMenu"); //NOI18N
            Object base = action.getValue("iconBase"); // NOI18N

            if (Boolean.TRUE.equals(obj)) {
                //button.setIcon(nonNullIcon(null));
                return;
            }

            if (action instanceof SystemAction) {
                SystemAction sa = (SystemAction) action;
                i = sa.getIcon(useTextIcons());
                if( i != null ) {
                    button.setIcon((Icon) i);
                    button.setDisabledIcon(ImageUtilities.createDisabledIcon((Icon) i));
                }
            } else {
                if (base == null) {
                    i = action.getValue(Action.SMALL_ICON);
                    if (i instanceof Icon) {
                        button.setIcon((Icon) i);
                        button.setDisabledIcon(ImageUtilities.createDisabledIcon((Icon) i));
                    } else {
                        //button.setIcon(nonNullIcon(null));
                    }
                }
            }

            if (base instanceof String) {
                String b = (String) base;
                ImageIcon imgIcon = null;

                if (i == null) {
                    // even for regular icon
                    imgIcon = ImageUtilities.loadImageIcon(b, true);
                    if (imgIcon != null) {
                        button.setIcon(imgIcon);
                        button.setDisabledIcon(ImageUtilities.createDisabledIcon(imgIcon));
                    }
                }

                ImageIcon pImgIcon = ImageUtilities.loadImageIcon(insertBeforeSuffix(b, "_pressed"), true); // NOI18N
                if (pImgIcon != null) {
                    button.setPressedIcon(pImgIcon);
                }

                ImageIcon rImgIcon = ImageUtilities.loadImageIcon(insertBeforeSuffix(b, "_rollover"), true); // NOI18N
                if (rImgIcon != null) {
                    button.setRolloverIcon(rImgIcon);
                }

                ImageIcon dImgIcon = ImageUtilities.loadImageIcon(insertBeforeSuffix(b, "_disabled"), true); // NOI18N
                if (dImgIcon != null) {
                    button.setDisabledIcon(dImgIcon);
                } else if (imgIcon != null) {
                    button.setDisabledIcon(ImageUtilities.createDisabledIcon(imgIcon));
                }
            }
        }

        @Override
        protected boolean useTextIcons() {
            return false;
        }
    }

    /** Check menu item bridge.
    */
    private static final class CheckMenuBridge extends BooleanButtonBridge {
        /** is popup or menu */
        private boolean popup;
        private boolean hasOwnIcon = false;

        /** Popup menu */
        public CheckMenuBridge(JCheckBoxMenuItem item, BooleanStateAction bsa, boolean popup) {
            super(item, bsa);
            init(item, popup);
        }
        
        public CheckMenuBridge(JCheckBoxMenuItem item, Action action, boolean popup) {
            super(item, action);
            init(item, popup);
        }
        
        private void init(JCheckBoxMenuItem item, boolean popup) {
            this.popup = popup;

            if (popup) {
                prepareMargins(item, action);
            }

            Object base = action.getValue("iconBase"); //NOI18N
            Object i = null;

            if (action instanceof SystemAction) {
                i = action.getValue(SystemAction.PROP_ICON);
            } else {
                i = action.getValue(Action.SMALL_ICON);
            }

            hasOwnIcon = (base != null) || (i != null);
        }

        /** @param changedProperty the name of property that has changed
        * or null if it is not known
        */
        @Override
        public void updateState(String changedProperty) {
            super.updateState(changedProperty);

            if ((changedProperty == null) || !changedProperty.equals(Action.ACCELERATOR_KEY)) {
                updateKey((JMenuItem) comp, action);
            }

            if ((changedProperty == null) || changedProperty.equals(Action.NAME)) {
                Object s = action.getValue(Action.NAME);

                if (s instanceof String) {
                    setMenuText(((JMenuItem) comp), (String) s, true);
                }
            }
        }

        @Override
        protected void updateButtonIcon() {
            if (hasOwnIcon) {
                super.updateButtonIcon();

                return;
            }

            if (!popup) {
                button.setIcon(ImageUtilities.loadImageIcon("org/openide/resources/actions/empty.gif", true)); // NOI18N
            }
        }

        @Override
        protected boolean useTextIcons() {
            return false;
        }
    }


    /** The class that listens to the menu item selections and forwards it to the
     * action class via the performAction() method.
     */
    private static class ISubActionListener implements java.awt.event.ActionListener {
        int index;
        SubMenuModel support;
        
        public ISubActionListener(int index, SubMenuModel support) {
            this.index = index;
            this.support = support;
        }
        
        /** called when a user clicks on this menu item */
        public void actionPerformed(ActionEvent e) {
            support.performActionAt(index);
        }
    }

    /** Sub menu bridge 2.
    */
    @Deprecated
    private static final class SubMenuBridge extends MenuBridge implements ChangeListener, DynamicMenuContent {
        /** model to obtain subitems from */
        private SubMenuModel model;
        private List<JMenuItem> currentOnes;
        private JMenuItem single;
        private JMenu multi;
        /** Constructor.
        */
        public SubMenuBridge(JMenuItem one, JMenu more, Action action, SubMenuModel model, boolean popup) {
            super(one, action, popup);
            single = one;
            multi = more;
            setMenuText(multi, (String)action.getValue(Action.NAME), popup);
            prepareMargins(one, action);
            prepareMargins(more, action);
            currentOnes = new ArrayList<JMenuItem>();
            this.model = model;
        }

        /** Called when model changes. Regenerates the model.
        */
        public void stateChanged(ChangeEvent ev) {
            //assert EventQueue.isDispatchThread();
            if (!EventQueue.isDispatchThread()) {
                new IllegalStateException("This must happen in the event thread!").printStackTrace();
            }
            // change in keys or in submenu model
//            checkVisibility();
        }
        
        @Override
        public void updateState(String changedProperty) {
            super.updateState(changedProperty);
//            checkVisibility();
        }        

        
        
        public JComponent[] getMenuPresenters() {
            return synchMenuPresenters(null);
        }
        
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            currentOnes.clear();
            int cnt = model.getCount();
            
            if (cnt == 0) {
                updateState(null);
                currentOnes.add(single);
                // menu disabled
                single.setEnabled(false);
            } else if (cnt == 1) {
                updateState(null);
                currentOnes.add(single);
                single.setEnabled(action.isEnabled());
                // generate without submenu
                HelpCtx help = model.getHelpCtx(0);
                associateHelp(single, (help == null) ? findHelp(action) : help);
            } else {
                currentOnes.add(multi);
                multi.removeAll();
                //TODO
                Mnemonics.setLocalizedText(multi, (String)action.getValue(Action.NAME));
            
                boolean addSeparator = false;
                int count = model.getCount();
            
                for (int i = 0; i < count; i++) {
                    String label = model.getLabel(i);
                
                    //          MenuShortcut shortcut = support.getMenuShortcut(i);
                    if (label == null) {
                        addSeparator = multi.getItemCount() > 0;
                    } else {
                        if (addSeparator) {
                            multi.addSeparator();
                            addSeparator = false;
                        }
                    
                        //       if (shortcut == null)
                        // (Dafe) changed to support mnemonics in item labels
                        JMenuItem item = new JMenuItem();
                        Mnemonics.setLocalizedText(item, label);
                    
                        // attach the shortcut to the first item
                        if (i == 0) {
                            updateKey(item, action);
                        }
                    
                        item.addActionListener(new ISubActionListener(i, model));
                    
                        HelpCtx help = model.getHelpCtx(i);
                        associateHelp(item, (help == null) ? findHelp(action) : help);
                        multi.add(item);
                    }
                
                    associateHelp(multi, findHelp(action));
                }
                multi.setEnabled(true);
            }
            return currentOnes.toArray(new JMenuItem[0]);
            
        }

        private void associateHelp(JComponent comp, HelpCtx help) {
            if ((help != null) && !help.equals(HelpCtx.DEFAULT_HELP) && (help.getHelpID() != null)) {
                HelpCtx.setHelpIDString(comp, help.getHelpID());
            } else {
                HelpCtx.setHelpIDString(comp, null);
            }
        }
    }
    //
    //
    // The presenter classes
    //
    //

    /**
     * Extension of Swing menu item with connection to
     * system actions.
     */
    public static class MenuItem extends javax.swing.JMenuItem implements DynamicMenuContent {
        static final long serialVersionUID = -21757335363267194L;
        private Actions.Bridge bridge;
        /** Constructs a new menu item with the specified label
        * and no keyboard shortcut and connects it to the given SystemAction.
        * @param aAction the action to which this menu item should be connected
        * @param useMnemonic if true, the menu try to find mnemonic in action label
        */
        public MenuItem(SystemAction aAction, boolean useMnemonic) {
            Actions.connect(this, aAction, !useMnemonic);
        }

        /** Constructs a new menu item with the specified label
        * and no keyboard shortcut and connects it to the given SystemAction.
        * @param aAction the action to which this menu item should be connected
        * @param useMnemonic if true, the menu try to find mnemonic in action label
        */
        public MenuItem(Action aAction, boolean useMnemonic) {
            Actions.connect(this, aAction, !useMnemonic);
        }
        
        void setBridge(Actions.Bridge br) {
            bridge = br;
        }

        public JComponent[] synchMenuPresenters(JComponent[] items) {
            if (bridge != null) {
                bridge.updateState(null);
            }
            return getMenuPresenters();
        }

        public JComponent[] getMenuPresenters() {
            return new JComponent[] {this};
        }
        
    }

    /** CheckboxMenuItem extends the java.awt.CheckboxMenuItem and adds
    * a connection to boolean state actions. The ActCheckboxMenuItem
    * processes the ItemEvents itself and calls the action.seBooleanState() method.
    * It also tracks the enabled and boolean state of the action and reflects it
    * as its visual enabled/check state.
    *
    * @author   Ian Formanek, Jan Jancura
    */
    public static class CheckboxMenuItem extends javax.swing.JCheckBoxMenuItem {
        private static final long serialVersionUID = 6190621106981774043L;

        /** Constructs a new ActCheckboxMenuItem with the specified label
        *  and connects it to the given BooleanStateAction.
        * @param aAction the action to which this menu item should be connected
        * @param useMnemonic if true, the menu try to find mnemonic in action label
        * @deprecated use {@link #CheckboxMenuItem(javax.swing.Action, boolean)}. 
        * Have your action to implement properly {@link Action#getValue} for {@link Action#SELECTED_KEY}
        */
        @Deprecated
        public CheckboxMenuItem(BooleanStateAction aAction, boolean useMnemonic) {
            Actions.connect(this, aAction, !useMnemonic);
        }
        
        
        /** Constructs a new ActCheckboxMenuItem with the specified label
        *  and connects it to the given Action and its {@link Action#SELECTED_KEY}
        * value.
        * 
        * @param aAction the action to which this menu item should be connected
        * @param useMnemonic if true, the menu try to find mnemonic in action label
        * @since 7.71
        */
        public CheckboxMenuItem(Action aAction, boolean useMnemonic) {
            Actions.connect(this, aAction, !useMnemonic);
        }
    }

    /** Component shown in toolbar, representing an action.
    * @deprecated extends deprecated ToolbarButton
    */
    @Deprecated
    public static class ToolbarButton extends org.openide.awt.ToolbarButton {
        private static final long serialVersionUID = 6564434578524381134L;

        public ToolbarButton(SystemAction aAction) {
            super(null);
            Actions.connect(this, aAction);
        }

        public ToolbarButton(Action aAction) {
            super(null);
            Actions.connect(this, aAction);
        }

        /**
         * Gets the maximum size of this component.
         * @return A dimension object indicating this component's maximum size.
         * @see #getMinimumSize
         * @see #getPreferredSize
         * @see java.awt.LayoutManager
         */
        @Override
        public Dimension getMaximumSize() {
            return this.getPreferredSize();
        }

        @Override
        public Dimension getMinimumSize() {
            return this.getPreferredSize();
        }
    }

    /** The Component for BooleeanState action that is to be shown
    * in a toolbar.
    *
    * @deprecated extends deprecated ToolbarToggleButton
    */
    @Deprecated
    public static class ToolbarToggleButton extends org.openide.awt.ToolbarToggleButton {
        private static final long serialVersionUID = -4783163952526348942L;

        /** Constructs a new ActToolbarToggleButton for specified action */
        public ToolbarToggleButton(BooleanStateAction aAction) {
            super(null, false);
            Actions.connect(this, aAction);
        }

        /**
         * Gets the maximum size of this component.
         * @return A dimension object indicating this component's maximum size.
         * @see #getMinimumSize
         * @see #getPreferredSize
         * @see java.awt.LayoutManager
         */
        @Override
        public Dimension getMaximumSize() {
            return this.getPreferredSize();
        }

        @Override
        public Dimension getMinimumSize() {
            return this.getPreferredSize();
        }
    }
    

    /** SubMenu provides easy way of displaying submenu items based on
    * SubMenuModel.
     * @deprecated Extends deprecated {@link JMenuPlus}. Instead create a regular {@link JMenu} and add items to it (or use {@link DynamicMenuContent}).
     */
    @Deprecated
    public static class SubMenu extends JMenuPlus implements DynamicMenuContent {
        private static final long serialVersionUID = -4446966671302959091L;

        private SubMenuBridge bridge;

        /** Constructs a new ActMenuItem with the specified label
        * and no keyboard shortcut and connects it to the given SystemAction.
        * No icon is used by default.
        * @param aAction the action to which this menu item should be connected
        * @param model the support for the menu items
        */
        public SubMenu(SystemAction aAction, SubMenuModel model) {
            this(aAction, model, true);
        }

        /** Constructs a new ActMenuItem with the specified label
        * and no keyboard shortcut and connects it to the given SystemAction.
        * No icon is used by default.
        * @param aAction the action to which this menu item should be connected
        * @param model the support for the menu items
        * @param popup whether this is a popup menu
        */
        public SubMenu(SystemAction aAction, SubMenuModel model, boolean popup) {
            this((Action) aAction, model, popup);
        }

        /** Constructs a new ActMenuItem with the specified label
        * and no keyboard shortcut and connects it to the given SystemAction.
        * No icon is used by default.
        * @param aAction the action to which this menu item should be connected
        * @param model the support for the menu items
        * @param popup whether this is a popup menu
        */
        public SubMenu(Action aAction, SubMenuModel model, boolean popup) {
            bridge = new SubMenuBridge(new JMenuItem(), this, aAction, model, popup);
            bridge.prepare();
        }
        
        public JComponent[] getMenuPresenters() {
            return bridge.getMenuPresenters();
        }
        
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            return bridge.synchMenuPresenters(items);
        }
        
    }

    /**
     * SPI for supplying alternative implementation of connection between actions and presenters.
     * The implementations
     * of this interface are being looked up in the default lookup.
     * If there is no implemenation in the lookup the default implementation
     * is used.
     * @see Lookup#getDefault()
     * @since org.openide.awt 6.9
     */
    public interface ButtonActionConnector {
        /**
         * Connects the action to the supplied button.
         * @return true if the connection was successful and no
         *    further actions are needed. If false is returned the
         *    default connect implementation is called
         */
        boolean connect(AbstractButton button, Action action);
        /**
         * Connects the action to the supplied JMenuItem.
         * @return true if the connection was successful and no
         *    further actions are needed. If false is returned the
         *    default connect implementation is called
         */
        boolean connect(JMenuItem item, Action action, boolean popup);
    }

    private static final ButtonActionConnectorGetter GET = new ButtonActionConnectorGetter();
    private static Collection<? extends ButtonActionConnector> buttonActionConnectors() {
        return GET.all();
    }
    private static final class ButtonActionConnectorGetter implements LookupListener {
        private final Lookup.Result<ButtonActionConnector> result;
        private Collection<? extends ButtonActionConnector> all;

        ButtonActionConnectorGetter() {
            result = Lookup.getDefault().lookupResult(ButtonActionConnector.class);
            result.addLookupListener(this);
            resultChanged(null);
        }

        final Collection<? extends ButtonActionConnector> all() {
            return all;
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            all = result.allInstances();
            all.iterator().hasNext();
        }

    }
}
