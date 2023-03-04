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

package org.openide.awt;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JToggleButton;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;
import org.openide.util.WeakSet;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.ActionInvoker;

/** Lazily initialized always enabled action
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
class AlwaysEnabledAction extends AbstractAction
implements PropertyChangeListener, ContextAwareAction {

    // -J-Dorg.openide.awt.AlwaysEnabledAction.level=FINE
    private static final Logger LOG = Logger.getLogger(AlwaysEnabledAction.class.getName());

    private static final String PREFERENCES_NODE = "preferencesNode"; // NOI18N

    private static final String PREFERENCES_KEY = "preferencesKey"; // NOI18N
    private static final String PREFERENCES_DEFAULT = "preferencesDefault"; // NOI18N

    static AlwaysEnabledAction create(Map m) {
        return (m.containsKey(PREFERENCES_KEY)) ? new CheckBox(m) : new AlwaysEnabledAction(m);
    }

    final Map map;
    private final AlwaysEnabledAction parent;
    private PropertyChangeListener weakL;
    ActionListener delegate;
    final Lookup context;
    final Object equals;

    public AlwaysEnabledAction(Map m) {
        super();
        this.map = m;
        this.context = null;
        this.equals = this;
        parent = null;
    }

    AlwaysEnabledAction(Map m, AlwaysEnabledAction parent, Lookup context, Object equals) {
        super();
        this.map = m;
        this.parent = parent;
        this.context = context;
        this.equals = equals;
    }

    private static ActionListener bindToContext(ActionListener a, Lookup context) {
        if (context != null) {
            if (a instanceof ContextAwareAction) {
                return ((ContextAwareAction)a).createContextAwareInstance(context);
            }
        }
        return a;
    }

    protected ActionListener getDelegate() {
        if (delegate == null) {
            ActionListener al;
            if (parent == null) {
                Object listener = map.get("delegate"); // NOI18N
                if (!(listener instanceof ActionListener)) {
                    throw new NullPointerException("No 'delegate' in " + map);
                }
                al = (ActionListener) listener;
            } else {
                al = parent.getDelegate();
            }
            delegate = bindToContext(al, context);
            if (delegate instanceof Action) {
                Action actionDelegate = (Action) delegate;
                if (weakL == null) {
                    weakL = WeakListeners.propertyChange(this, actionDelegate);
                }
                actionDelegate.addPropertyChangeListener(weakL);
                // Ensure display names and other properties are in sync or propagate them
                syncActionDelegateProperty(Action.NAME, actionDelegate);
            }
        }
        return delegate;
    }

    private void syncActionDelegateProperty(String propertyName, Action actionDelegate) {
        Object value = extractCommonAttribute(map, propertyName);
        Object delegateValue = actionDelegate.getValue(propertyName);
        if (value != null) {
            if (delegateValue == null) {
                actionDelegate.putValue(propertyName, value);
            } else {
                if (!delegateValue.equals(value)) { // Values differ
                    LOG.log(Level.FINE, "Value of property \"{0}\" of AlwaysEnabledAction " +
                            "is \"{1}\" but delegate {2} has \"{3}\"",
                            new Object[] {propertyName, value, delegate, delegateValue});
                }
            }
        } // else either both values are null or
        // this has null and delegate has non-null which is probably fine (declarer does not care)
    }

    @Override
    public boolean isEnabled() {
//        assert EventQueue.isDispatchThread();
        if (delegate instanceof Action) {
            return ((Action)delegate).isEnabled();
        }
        return true;
    }

    public void actionPerformed(final ActionEvent e) {
        assert EventQueue.isDispatchThread();
        if (getDelegate() instanceof Action) {
            if (!((Action)getDelegate()).isEnabled()) {
                Utilities.disabledActionBeep();
                // Do not fire newValue == null (see #165838)
                firePropertyChange("enabled", null, isEnabled()); // NOI18N
                return;
            }
        }

        boolean async = Boolean.TRUE.equals(map.get("asynchronous")); // NOI18N
        Runnable ar = new Runnable() {
            public void run() {
                getDelegate().actionPerformed(e);
            }
        };
        ActionInvoker.invokeAction(this, e, async, ar);
    }

    @Override
    public Object getValue(String name) {
        if (delegate instanceof Action) {
            Object ret = ((Action)delegate).getValue(name);
            if (ret != null) {
                return ret;
            }
            if (
                "iconBase".equals(name) && // NOI18N
                ((Action)delegate).getValue(Action.SMALL_ICON) != null
            ) {
                return null;
            }
        }
        Object o = extractCommonAttribute(map, name);
        // cf. #137709 JG18:
        return o != null ? o : super.getValue(name);
    }

    static final Object extractCommonAttribute(Map fo, String name) {
        try {
        if (Action.NAME.equals(name)) {
            String actionName = (String) fo.get("displayName"); // NOI18N
            // NOI18N
            //return Actions.cutAmpersand(actionName);
            return actionName;
        }
        if (Action.MNEMONIC_KEY.equals(name)) {
            String actionName = (String) fo.get("displayName"); // NOI18N
            if( null == actionName )
                return null;
            // NOI18N
            int position = Mnemonics.findMnemonicAmpersand(actionName);
            if (position == -1) {
                return null;
            } else {
                // #167996: copied from AbstractButton.setMnemonic
                int vk = (int) actionName.charAt(position + 1);
                if(vk >= 'a' && vk <='z') { //NOI18N
                    vk -= ('a' - 'A'); //NOI18N
                }
                return vk;
            }
        }
        if (Action.SMALL_ICON.equals(name)) {
            Object icon = fo == null ? null : fo.get("iconBase"); // NOI18N
            if (icon instanceof Icon) {
                return (Icon) icon;
            }
            if (icon instanceof URL) {
                icon = Toolkit.getDefaultToolkit().getImage((URL)icon);
            }
            if (icon instanceof Image) {
                return ImageUtilities.image2Icon((Image)icon);
            }
            if (icon instanceof String) {
                return ImageUtilities.loadImageIcon((String)icon, true);
            }
        }
        if ("iconBase".equals(name)) { // NOI18N
            return fo == null ? null : fo.get("iconBase"); // NOI18N
        }
        if ("noIconInMenu".equals(name)) { // NOI18N
            return fo == null ? null : fo.get("noIconInMenu"); // NOI18N
        }
        // Delegate query to other properties to "fo" ignoring special properties
        if (!"delegate".equals(name) && !"instanceCreate".equals(name)) {
            return fo == null ? null : fo.get(name);
        }
        } catch (RuntimeException x) { // noted in #172103
            LOG.log(Level.WARNING, "Could not get action attribute " + name, x);
        }
        return null;
    }


    @Override
    public int hashCode() {
        if (equals == this) {
            return super.hashCode();
        }
        return equals.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof AlwaysEnabledAction) {
            final AlwaysEnabledAction other = (AlwaysEnabledAction) obj;
            if (this == this.equals && other == other.equals) {
                return (this == other);
            }

            if (this.equals.equals(other.equals)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "AlwaysEnabledAction[" + getValue(Action.NAME) + "]"; // NOI18N
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == delegate) {
            firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        return new AlwaysEnabledAction(map, this, actionContext, equals);
    }

    static final class CheckBox extends AlwaysEnabledAction
            implements Presenter.Menu, Presenter.Popup, Presenter.Toolbar, PreferenceChangeListener, LookupListener
    {

        private static final long serialVersionUID = 1L;

        private static final ActionListener EMPTY = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                // Do nothing
            }
        };

        private JCheckBoxMenuItem menuItem;

        private JCheckBoxMenuItem popupItem;

        private WeakSet<AbstractButton> toolbarItems;

        private Preferences preferencesNode;

        private Lookup.Result<Preferences> preferencesNodeResult;

        private boolean prefsListening;

        CheckBox(Map m) {
            super(m);
        }

        CheckBox(Map m, AlwaysEnabledAction parent, Lookup context, Object equals) {
            super(m, parent, context, equals);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Toggle state in preferences
            togglePreferencesSelected();

            super.actionPerformed(e);
        }

        public JMenuItem getMenuPresenter() {
            if (menuItem == null) {
                menuItem = new JCheckBoxMenuItem();
                menuItem.setSelected(isPreferencesSelected());
                Actions.connect(menuItem, this, false);
            }
            return menuItem;
        }

        public JMenuItem getPopupPresenter() {
            if (popupItem == null) {
                popupItem = new JCheckBoxMenuItem();
                popupItem.setSelected(isPreferencesSelected());
                Actions.connect(popupItem, this, true);
            }
            return popupItem;
        }

        public AbstractButton getToolbarPresenter() {
            if(toolbarItems == null) {
                toolbarItems = new WeakSet<AbstractButton>(4);
            }
            AbstractButton b = new DefaultIconToggleButton();
            toolbarItems.add(b);
            b.setSelected(isPreferencesSelected());
            Actions.connect(b, this);
            return b;
        }

        public void preferenceChange(PreferenceChangeEvent pce) {
            updateItemsSelected();
        }

        @Override
        protected ActionListener getDelegate() {
            return EMPTY;
        }

        @Override
        public Action createContextAwareInstance(Lookup actionContext) {
            return new CheckBox(map, this, actionContext, equals);
        }

        boolean isPreferencesSelected() {
            String key = (String) getValue(PREFERENCES_KEY);
            Preferences prefs = prefs();
            boolean value;
            if (key != null && prefs != null) {
                Object defaultValue = getValue(PREFERENCES_DEFAULT);
                value = prefs.getBoolean(key, defaultValue instanceof Boolean ? (Boolean) defaultValue : false);
                synchronized (this) {
                    if (!prefsListening) {
                        prefsListening = true;
                        prefs.addPreferenceChangeListener(this);
                    }
                }
            } else {
                value = false;
            }
            return value;
        }

        private void updateItemsSelected() {
            boolean selected = isPreferencesSelected();
            if (menuItem != null) {
                menuItem.setSelected(selected);
            }
            if (popupItem != null) {
                popupItem.setSelected(selected);
            }
            if (toolbarItems != null) {
                for(AbstractButton b : toolbarItems) {
                    b.setSelected(selected);
                }
            }
        }

        private synchronized Preferences prefs() {
            if (preferencesNode == null) {
                Object prefsNodeOrLookup = getValue(PREFERENCES_NODE);
                if (prefsNodeOrLookup instanceof String) {
                    String nodeName = (String) prefsNodeOrLookup;
                    if (nodeName.startsWith("system:")) {
                        preferencesNode = Preferences.systemRoot();
                        if (preferencesNode != null) {
                            nodeName = nodeName.substring("system:".length());
                            try {
                                preferencesNode = preferencesNode.nodeExists(nodeName) ? preferencesNode.node(nodeName) : null;
                            } catch (BackingStoreException ex) {
                                preferencesNode = null;
                            }
                        }
                    } else if (nodeName.startsWith("user:")) {
                        preferencesNode = Preferences.userRoot();
                        if (preferencesNode != null) {
                            nodeName = nodeName.substring("user:".length());
                            try {
                                preferencesNode = preferencesNode.nodeExists(nodeName) ? preferencesNode.node(nodeName) : null;
                            } catch (BackingStoreException ex) {
                                preferencesNode = null;
                            }
                        }
                    } else {
                        preferencesNode = NbPreferences.root();
                        if (preferencesNode != null) {
                            try {
                                preferencesNode = preferencesNode.nodeExists(nodeName) ? preferencesNode.node(nodeName) : null;
                            } catch (BackingStoreException ex) {
                                preferencesNode = null;
                            }
                        }
                    }

                } else if (prefsNodeOrLookup instanceof Preferences) {
                    preferencesNode = (Preferences) prefsNodeOrLookup;
                } else if (prefsNodeOrLookup instanceof Lookup) {
                    Lookup prefsLookup = (Lookup) prefsNodeOrLookup;
                    preferencesNodeResult = prefsLookup.lookupResult(Preferences.class);
                    Collection<? extends Preferences> instances = preferencesNodeResult.allInstances();
                    if (instances.size() > 0) {
                        preferencesNode = instances.iterator().next();
                        preferencesNodeResult.addLookupListener(this);
                    }
                    return prefsLookup.lookup(Preferences.class);
                } else {
                    preferencesNode = null;
                }
            }
            return preferencesNode;
        }

        public void resultChanged(LookupEvent ev) {
            preferencesNode = null;
            preferencesNodeResult = null;
            updateItemsSelected();
        }

        private void togglePreferencesSelected() {
            String key = (String) getValue(PREFERENCES_KEY);
            Preferences prefs = prefs();
            if (key != null && prefs != null) {
                Object defaultValue = getValue(PREFERENCES_DEFAULT);
                prefs.putBoolean(key, !prefs.getBoolean(key, defaultValue instanceof Boolean ? (Boolean) defaultValue : false));
            }
        }

    }

    /**
     * A button that provides a default icon when no text and no custom icon have been set.
     * Copied from Toolbar.java and made a toggle button.
     */
    static class DefaultIconToggleButton extends JToggleButton {
        private Icon unknownIcon;

        @Override
        public Icon getIcon() {
            Icon retValue = super.getIcon();
            if( null == retValue && (null == getText() || getText().isEmpty()) ) {
                if (unknownIcon == null) {
                    unknownIcon = ImageUtilities.loadImageIcon("org/openide/awt/resources/unknown.gif", false); //NOI18N
                    //unknownIcon = ImageUtilities.loadImageIcon("org/openide/loaders/unknown.gif", false); //NOI18N
                }
                retValue = unknownIcon;
            }
            return retValue;
        }
    }

}
