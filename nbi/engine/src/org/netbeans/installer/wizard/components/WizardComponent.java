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

package org.netbeans.installer.wizard.components;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.UiUtils;
import org.netbeans.installer.utils.helper.NbiProperties;
import org.netbeans.installer.utils.helper.swing.NbiButton;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.containers.SwingContainer;
import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.ui.WizardUi;

/**
 * This class represents a single unit of a {@link Wizard} sequence. It is a logical
 * abstraction of a wizard step, should normally be accompanied by a
 * {@link WizardUi} instance.
 *
 * <p>
 * This class provides a bunch of methods which are used by the wizard in order to
 * execute the component, devise whether is can be executed or can be skipped,
 * whether the user is allowed to skip the component, etc.
 *
 * <p>
 * The basic infrastructure used by the concrete implementations of the
 * {@link WizardComponent} class is also defined here: support for child components
 * and properties.
 *
 * @author Kirill Sorokin
 * @since 1.0
 */
public abstract class WizardComponent {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * {@link Wizard} which currently executes this component. This field is
     * populated at the moment the component is executed, thus it is unsafe to
     * assume that it holds a correct value unless it is known that the component
     * is active.
     */
    private Wizard wizard;
    
    /**
     * List of child components. It is not expected that all implementations will
     * make use of this field, thus the presence of  children may simply be ignored
     * by some.
     */
    private List<WizardComponent> children;
    
    /**
     * Component's properties. These should not be mixed up with the properties
     * available through {@link Wizard#getProperty(String)} and
     * {@link Wizard#setProperty(String,String)} - these are internal to the
     * component, while the wizard's ones are expected to keep the user input.
     */
    private NbiProperties properties;
    
    // constructor //////////////////////////////////////////////////////////////////
    /**
     * Creates a new instance of {@link WizardComponent}. This is the default
     * <code>protected</code> constructor which must be called by the concrete
     * implementations. It initializes the fields above and sets some default
     * properties.
     */
    protected WizardComponent() {
        children = new ArrayList<WizardComponent>();
        properties = new NbiProperties();
        
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);
        
        setProperty(HELP_BUTTON_TEXT_PROPERTY,
                DEFAULT_HELP_BUTTON_TEXT);
        setProperty(BACK_BUTTON_TEXT_PROPERTY,
                DEFAULT_BACK_BUTTON_TEXT);
        setProperty(NEXT_BUTTON_TEXT_PROPERTY,
                DEFAULT_NEXT_BUTTON_TEXT);
        setProperty(CANCEL_BUTTON_TEXT_PROPERTY,
                DEFAULT_CANCEL_BUTTON_TEXT);
        setProperty(FINISH_BUTTON_TEXT_PROPERTY,
                DEFAULT_FINISH_BUTTON_TEXT);
    }
    
    // execution flow ///////////////////////////////////////////////////////////////
    /**
     * This method is called when the component was reached with the
     * {@link Wizard#next()} method. It is meant to perform the operations required
     * by the concrete component, such as perform a search of some sort, etc.
     *
     * <p>
     * This method is called after {@link #setWizard(Wizard)}, {@link #initialize()}
     * and {@link #getWizardUi()}, thus it is safe to assume that the wizard field
     * has been correctly initialized and the UI (if it exists) was shown.
     */
    public abstract void executeForward();
    
    /**
     * This method is called when the component was reached with the
     * {@link Wizard#previous()} method. It is meant to perform the operations
     * required by the concrete component, such as perform a search of some sort,
     * etc.
     *
     * <p>
     * This method is called after {@link #setWizard(Wizard)}, {@link #initialize()}
     * and {@link #getWizardUi()}, thus it is safe to assume that the wizard field
     * has been correctly initialized and the UI (if it exists) was shown.
     */
    public abstract void executeBackward();
    
    /**
     * This method is called every time a wizard reaches this component. Unlike the
     * {@link #executeForward()} and {@link #executeBackward()} methods this one is
     * called <b>before</b> the UI is shown and thus is intended to be used to
     * prepare the model or data for use by the UI.
     *
     * <p>
     * This method is called after {@link #setWizard(Wizard)}, thus it is safe to
     * assume that the wizard field has been correctly initialized.
     */
    public abstract void initialize();
    
    /**
     * This method is called by the wizard in order to find out whether this
     * component can be reached via {@link Wizard#next()}.
     *
     * <p>
     * It's important to note that if this method returns <code>false</code>, the
     * wizard will not assume that it cannot proceed, but will <b>skip</b> this
     * component altogether.
     *
     * @return <code>true</code> if the component can be executed,
     *      <code>false</code> if the component should be skipped.
     */
    public boolean canExecuteForward() {
        return true;
    }
    
    /**
     * This method is called by the wizard in order to find out whether this
     * component can be reached via {@link Wizard#previous()}.
     *
     * <p>
     * It's important to note that if this method returns <code>false</code>, the
     * wizard will not assume that it cannot proceed, but will <b>skip</b> this
     * component altogether.
     *
     * @return <code>true</code> if the component can be executed,
     *      <code>false</code> if the component should be skipped.
     */
    public boolean canExecuteBackward() {
        return true;
    }
    
    /**
     * This method is called by the wizard in order to find out whether it is
     * allowed to execute any components before this via the
     * {@link Wizard#previous()} method. If this method returns <code>true</code> it
     * will be illegal to call {@link Wizard#previous()} if the current component
     * is active.
     *
     * @return <code>true</code> is the component is the point of no return,
     *      <code>false</code> otherwise.
     */
    public boolean isPointOfNoReturn() {
        return false;
    }
    
    // ui ///////////////////////////////////////////////////////////////////////////
    /**
     * Returns the {@link WizardUi} object for this component.
     *
     * @return {@link WizardUi} object for this component.
     */
    public abstract WizardUi getWizardUi();
    
    // wizard ///////////////////////////////////////////////////////////////////////
    /**
     * Returns the {@link Wizard} which currently executes this component.
     *
     * @return {@link Wizard} which currently executes this component.
     */
    public final Wizard getWizard() {
        return wizard;
    }
    
    /**
     * Sets the {@link Wizard} which currently executes this component.
     *
     * @param wizard {@link Wizard} which currently executes this component.
     */
    public final void setWizard(final Wizard wizard) {
        this.wizard = wizard;
    }
    
    // children /////////////////////////////////////////////////////////////////////
    /**
     * Registers a new child for this component.
     *
     * @param component New child component which should be registered.
     */
    public final void addChild(final WizardComponent component) {
        children.add(component);
    }
    
    /**
     * Unregisters a child component. If it was not previously registered, no action
     * is taken.
     *
     * @param component Child component which should be unregistered.
     */
    public final void removeChild(final WizardComponent component) {
        children.remove(component);
    }
    
    /**
     * Registers several children for this component.
     *
     * @param components {@link List} of child components which should be
     *      registered.
     */
    public final void addChildren(final List<WizardComponent> components) {
        children.addAll(components);
    }
    
    /**
     * Returns the list of currently registered child components. It is not
     * guaranteed that operating on this list directly will affect the actual
     * children.
     *
     * @return {@link List} of the registered child components.
     */
    public final List<WizardComponent> getChildren() {
        return children;
    }
    
    // properties ///////////////////////////////////////////////////////////////////
    /**
     * Returns the value of the component's property with the specified name. This
     * method attempts to parse the property value using the
     * {@link SystemUtils#parseString(String,ClassLoader)} method and supplying
     * {@link Wizard#getClassLoader()} as the class loader value.
     *
     * @param name Name of the property whose value should be returned.
     * @return Value of the specified property, parsed via
     *      {@link SystemUtils#parseString(String,ClassLoader)}.
     */
    public final String getProperty(final String name) {
        return getProperty(name, true);
    }
    
    /**
     * Returns the value of the specified property. Thsi method can either attempt
     * to resolve the value, or return it as is.
     *
     * @param name Name of the property whose value needs to be returned.
     * @param resolve Whether to resolve the property value or not.
     * @return Value of the specified property, either parsed or not.
     */
    public final String getProperty(final String name, final boolean resolve) {
        final String value = properties.getProperty(name);
        
        if (resolve) {
            return value != null ? resolveString(value) : null;
        } else {
            return value;
        }
    }
    
    /**
     * Sets the specified property to the specified value. If such property does not
     * exist - it is created.
     *
     * @param name Name of the property whose value needs to be set.
     * @param value Value of the property.
     */
    public final void setProperty(final String name, final String value) {
        properties.setProperty(name, value);
    }
    
    /**
     * Returns the properties of this component. The values of the properties will
     * not be parsed. It is not guaranteed that operating on the return value of
     * this method will affect the actual properties of the component.
     *
     * @return Component's properties.
     */
    public final NbiProperties getProperties() {
        return properties;
    }
    
    // helpers //////////////////////////////////////////////////////////////////////
    /**
     * A helper method - calls {@link SystemUtils#resolveString(String,ClassLoader)}
     * supplying {@link Wizard#getClassLoader()} as the class loader value.
     *
     * @param string String to be resolved.
     * @return Resolved string.
     */
    protected final String resolveString(final String string) {
        return SystemUtils.resolveString(string, wizard.getClassLoader());
    }
    
    /**
     * A helper method - calls {@link SystemUtils#resolvePath(String,ClassLoader)}
     * supplying {@link Wizard#getClassLoader()} as the class loader value.
     *
     * @param path Path to be resolved as a {@link String}.
     * @return Resolved path as a {@link File}.
     */
    protected final File resolvePath(final String path) {
        return SystemUtils.resolvePath(path, wizard.getClassLoader());
    }
    
    /**
     * A helper method - calls
     * {@link ResourceUtils#getString(String,String,ClassLoader)}, supplying
     * {@link Wizard#getClassLoader()} as the class loader value.
     *
     * @param baseName Resource bundle base name.
     * @param key Name of the key whose value needs to be obtained.
     * @return Value of the specified key from the specified bundle.
     */
    protected final String getString(final String baseName, final String key) {
        return ResourceUtils.getString(baseName, key, wizard.getClassLoader());
    }
    
    /**
     * A helper method - calls
     * {@link ResourceUtils#getString(String,String,ClassLoader,Object[])},
     * supplying {@link Wizard#getClassLoader()} as the class loader value.
     *
     * @param baseName Resource bundle base name.
     * @param key Name of the key whose value needs to be obtained.
     * @param arguments Objects which should be used to substitute wildcards in the
     *      key value.
     * @return Value of the specified key from the specified bundle with its
     *      wildcards resolved using the supplied arguments.
     */
    protected final String getString(
            final String baseName,
            final String key,
            final Object... arguments) {
        return ResourceUtils.getString(baseName, key, wizard.getClassLoader(), arguments);
    }
    
    /**
     * A helper method - calls {@link ResourceUtils#getResource(String,ClassLoader)}
     * supplying {@link Wizard#getClassLoader()} as the class loader value.
     *
     * @param path Path to the resource which should be obtained.
     * @return {@link InputStream} from the resource, or <code>null</code> if it was
     *      not found.
     */
    protected final InputStream getResource(final String path) {
        return ResourceUtils.getResource(path, wizard.getClassLoader());
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    /**
     * Implementation of the {@link WizardUi} for {@link WizardComponent}.
     *
     * @author Kirill Sorokin
     * @since 1.0
     */
    public static class WizardComponentUi implements WizardUi {
        /**
         * Current {@link WizardComponent} for this UI.
         */
        protected WizardComponent component;
        
        /**
         * UI implementation for Swing environment. This is initialized lazily, i.e.
         * the value of this field will be null, unless the accessor method has been
         * called - {@link #getSwingUi(SwingContainer)}.
         */
        protected WizardComponentSwingUi swingUi;
        
        /**
         * Creates a new instance of {@link WizardComponentUi}, initializing it with
         * the specified instance of {@link WizardComponent}.
         *
         * @param component Instance of {@link WizardComponent} which should be used
         *      by this UI.
         */
        protected WizardComponentUi(final WizardComponent component) {
            this.component = component;
        }
        
        /**
         * {@inheritDoc}
         */
        public SwingUi getSwingUi(final SwingContainer container) {
            if (swingUi == null) {
                swingUi = new WizardComponentSwingUi(component, container);
            }
            
            swingUi.initializeContainer();
            swingUi.initialize();
            
            return swingUi;
        }
    }
    
    /**
     * Implementation of {@link SwingUi} for {@link WizardComponent}.
     *
     * @author Kirill Sorokin
     * @since 1.0
     */
    public static class WizardComponentSwingUi extends SwingUi {
        /**
         * Current {@link WizardComponent} for this UI.
         */
        protected WizardComponent component;
        
        /**
         * Current {@link SwingContainer} for this UI.
         */
        protected SwingContainer container;
        
        /**
         * Creates a new instance of {@link WizardComponentSwingUi}, initializing it
         * with the specified instances of {@link WizardComponent} and
         * {@link SwingContainer}.
         *
         * @param component Instance of {@link WizardComponent} which should be used
         *      by this UI.
         * @param container Instance of {@link SwingContainer} which should be used
         *      by this UI.
         */
        protected WizardComponentSwingUi(
                final WizardComponent component,
                final SwingContainer container) {
            this.component = component;
            this.container = container;
        }
        
        /**
         * {@inheritDoc}
         */
        public String getTitle() {
            return component.getProperty(TITLE_PROPERTY);
        }
        
        /**
         * {@inheritDoc}
         */
        public String getDescription() {
            return component.getProperty(DESCRIPTION_PROPERTY);
        }
        
        /**
         * {@inheritDoc}
         */
        public void evaluateHelpButtonClick() {
            // does nothing
        }
        
        /**
         * {@inheritDoc}
         */
        public void evaluateBackButtonClick() {
            component.getWizard().previous();
        }
        
        /**
         * {@inheritDoc}
         */
        public void evaluateNextButtonClick() {
            component.getWizard().next();
        }
        
        /**
         * {@inheritDoc}
         */
        public void evaluateCancelButtonClick() {
            final String cancelDialogTitle = ResourceUtils.getString(
                    WizardComponent.class,
                    RESOURCE_CANCEL_DIALOG_TITLE);
            final String canceldialogText = ResourceUtils.getString(
                    WizardComponent.class,
                    RESOURCE_CANCEL_DIALOG_TEXT);
            
            if (UiUtils.showYesNoDialog(cancelDialogTitle, canceldialogText)) {
                component.getWizard().getFinishHandler().cancel();
            }
        }
        
        /**
         * {@inheritDoc}
         */
        public NbiButton getDefaultEnterButton() {
            return container.getNextButton();
        }
        
        /**
         * {@inheritDoc}
         */
        public NbiButton getDefaultEscapeButton() {
            return container.getCancelButton();
        }
        
        /**
         * {@inheritDoc}
         */
        public JComponent getDefaultFocusOwner() {
            if (getDefaultEnterButton() != null) {
                return getDefaultEnterButton();
            } else {
                return null;
            }
        }
        
        // protected ////////////////////////////////////////////////////////////////
        /**
         * Initializes the container. This method sets the appropriate texts on the
         * buttons, enables/disables them according to the current position in the
         * wizard, etc.
         *
         * <p>
         * This method is called right before the UI is shown.
         */
        protected void initializeContainer() {
            // set up the help button
            container.getHelpButton().setVisible(false);
            container.getHelpButton().setEnabled(false);
            
            container.getHelpButton().setText(
                    component.getProperty(HELP_BUTTON_TEXT_PROPERTY));
            
            // set up the back button
            container.getBackButton().setVisible(true);
            if (component.getWizard().hasPrevious()) {
                container.getBackButton().setEnabled(true);
            } else {
                container.getBackButton().setEnabled(false);
            }
            
            container.getBackButton().setText(
                    component.getProperty(BACK_BUTTON_TEXT_PROPERTY));
            
            // set up the next (or finish) button
            container.getNextButton().setVisible(true);
            container.getNextButton().setEnabled(true);
            
            if (component.getWizard().hasNext()) {
                container.getNextButton().setText(
                        component.getProperty(NEXT_BUTTON_TEXT_PROPERTY));
            } else {
                container.getNextButton().setText(
                        component.getProperty(FINISH_BUTTON_TEXT_PROPERTY));
            }
            
            // set up the cancel button
            container.getCancelButton().setVisible(true);
            container.getCancelButton().setEnabled(true);
            
            container.getCancelButton().setText(
                    component.getProperty(CANCEL_BUTTON_TEXT_PROPERTY));
        }
        
        /**
         * Initializes the UI. This methods sets the correct texts for the labels,
         * textfields, and initializes other controls.
         *
         * <p>
         * This method is called right before the UI is shown.
         */
        protected void initialize() {
            // does nothing
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    /**
     * Name of the property, which contains the component's title.
     */
    public static final String TITLE_PROPERTY =
            "title"; // NOI18N
    
    /**
     * Name of the property, which contains the component's description.
     */
    public static final String DESCRIPTION_PROPERTY =
            "description"; // NOI18N
    
    /**
     * Name of the property, which contains the text for the standard 'Help'
     * button.
     */
    public static final String HELP_BUTTON_TEXT_PROPERTY =
            "help.button.text"; // NOI18N
    
    /**
     * Name of the property, which contains the text for the standard 'Back'
     * button.
     */
    public static final String BACK_BUTTON_TEXT_PROPERTY =
            "back.button.text"; // NOI18N
    
    /**
     * Name of the property, which contains the text for the standard 'Next'
     * button.
     */
    public static final String NEXT_BUTTON_TEXT_PROPERTY =
            "next.button.text"; // NOI18N
    
    /**
     * Name of the property, which contains the text for the standard 'Cancel'
     * button.
     */
    public static final String CANCEL_BUTTON_TEXT_PROPERTY =
            "cancel.button.text"; // NOI18N
    
    /**
     * Name of the property, which contains the text for the standard 'Finish'
     * button.
     */
    public static final String FINISH_BUTTON_TEXT_PROPERTY =
            "finish.button.text"; // NOI18N
    
    // private //////////////////////////////////////////////////////////////////////
    /**
     * Default value for the component's title.
     */
    private static final String DEFAULT_TITLE =
            ResourceUtils.getString(WizardComponent.class,
            "WC.title"); // NOI18N
    
    /**
     * Default value for the component's description.
     */
    private static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(WizardComponent.class,
            "WC.description"); // NOI18N
    
    /**
     * Default text for the standard 'Help' button.
     */
    private static final String DEFAULT_HELP_BUTTON_TEXT =
            ResourceUtils.getString(WizardComponent.class,
            "WC.help.button.text"); // NOI18N
    
    /**
     * Default text for the standard 'Back' button.
     */
    private static final String DEFAULT_BACK_BUTTON_TEXT =
            ResourceUtils.getString(WizardComponent.class,
            "WC.back.button.text"); // NOI18N
    
    /**
     * Default text for the standard 'Next' button.
     */
    private static final String DEFAULT_NEXT_BUTTON_TEXT =
            ResourceUtils.getString(WizardComponent.class,
            "WC.next.button.text"); // NOI18N
    
    /**
     * Default text for the standard 'Cancel' button.
     */
    private static final String DEFAULT_CANCEL_BUTTON_TEXT =
            ResourceUtils.getString(WizardComponent.class,
            "WC.cancel.button.text"); // NOI18N
    
    /**
     * Default text for the standard 'Finish' button.
     */
    private static final String DEFAULT_FINISH_BUTTON_TEXT =
            ResourceUtils.getString(WizardComponent.class,
            "WC.finish.button.text"); // NOI18N
    
    /**
     * Name of a resource bundle entry.
     */
    public static final String RESOURCE_CANCEL_DIALOG_TITLE =
            "WC.cancel.dialog.title"; // NOI18N
    
    /**
     * Name of a resource bundle entry.
     */
    public static final String RESOURCE_CANCEL_DIALOG_TEXT =
            "WC.cancel.dialog.text"; // NOI18N
}
