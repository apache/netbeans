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

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.*;

import java.beans.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.UIManager;
import org.openide.awt.Mnemonics;
import org.openide.util.Exceptions;


/**
 * Helper dialog box manager for showing custom property editors.
 *
 * @author Jan Jancura, Dafe Simonek, David Strupl
 */
final class PropertyDialogManager implements VetoableChangeListener, ActionListener {
    /** Name of the custom property that can be passed in PropertyEnv. */
    private static final String PROPERTY_DESCRIPTION = "description"; // NOI18N
    private static Throwable doNotNotify;

    /* JST: Made package private because PropertyPanel should be used instead. */

    /** Listener to editor property changes. */
    private PropertyChangeListener editorListener;
    
    /** Listener to help ctx changes. */
    private PropertyChangeListener componentListener;

    /** Cache for reverting on cancel. */
    private Object oldValue;

    /** Custom property editor. */
    private PropertyEditor editor;

    /** model of the edited property */
    private PropertyModel model;

    /** this is extracted from the model if possible, can be null */
    private Node.Property prop;

    /** Set true when property is changed. */
    private boolean changed = false;

    /** Given component stored for test on Enhance property ed. */
    private Component component;

    /** Dialog instance. */
    private Window dialog;

    /** Ok button can be enabled/disabled*/
    private JButton okButton;

    /** */
    private Runnable errorPerformer;
    private boolean okButtonState = true;
    private Object envStateBeforeDisplay = null;

    /** Environment passed to the property editor. */
    private PropertyEnv env;
    private Object lastValueFromEditor;
    private boolean cancelled = false;
    private boolean ok = false;
    private boolean reset = false;

    public PropertyDialogManager(
        String title, final boolean isModal, final PropertyEditor editor, PropertyModel model,
        final PropertyEnv env
    ) {
        this.editor = editor;

        if (env != null) {
            env.addVetoableChangeListener(this);
        }

        this.component = editor.getCustomEditor();

        if (component == null) {
            throw new NullPointerException(
                "Cannot create a dialog" + " for a null component. Offending property editor class:" +
                editor.getClass().getName()
            );
        }

        this.model = model;
        this.env = env;

        HelpCtx helpCtx = null;
        if (env != null) {
            Object helpID = env.getFeatureDescriptor().getValue(ExPropertyEditor.PROPERTY_HELP_ID);

            if ((helpID != null) && helpID instanceof String && (component != null) && component instanceof JComponent) {
                HelpCtx.setHelpIDString((JComponent) component, (String) helpID);
                helpCtx = new HelpCtx((String) helpID);
            }
        }

        //Docs say this works, but apparently hadn't worked for some time
        if (component instanceof JComponent) {
            Object compTitle = ((JComponent) component).getClientProperty("title");

            if (compTitle instanceof String) {
                title = (String) compTitle;
            }
        }

        // create dialog instance and initialize listeners
        createDialog(isModal, title, helpCtx);
        initializeListeners();
    }

    // public methods ............................................................

    /** Get the created dialog instance.
     * @return the dialog instance managed by this class.
     */
    public Window getDialog() {
        return dialog;
    }

    /**
     * PropertyDialogManager is attached to the PropertyEnv instance by
     * vetoableChangeListener.
     */
    public void vetoableChange(PropertyChangeEvent evt)
    throws PropertyVetoException {
        if ((env != null) && (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()))) {
            okButtonState = evt.getNewValue() != PropertyEnv.STATE_INVALID;

            if (okButton != null) {
                okButton.setEnabled(okButtonState);
            }
        }
    }

    // other methods ............................................................

    /** Creates proper DialogDescriptor and obtain dialog instance
     * via DialogDisplayer.createDialog() call.
     */
    private void createDialog(boolean isModal, String title, HelpCtx helpCtx) {
        if (component instanceof Window) {
            // custom component is already a window --> just return it
            // from getDialog
            dialog = (Window) component;
            dialog.pack();

            return;
        }

        // prepare our options (buttons)
        boolean cannotWrite = false;
        boolean defaultValue = false;

        if (model instanceof ExPropertyModel) {
            FeatureDescriptor fd = ((ExPropertyModel) model).getFeatureDescriptor();

            if (fd instanceof Node.Property) {
                prop = (Node.Property) fd;
                cannotWrite = !prop.canWrite();
                defaultValue = PropUtils.shallBeRDVEnabled(prop);
            }
        }

        Object defaultOption;
        Object[] options;

        if ((editor == null) || (cannotWrite)) {
            JButton closeButton = new JButton(getString("CTL_Close"));
            closeButton.getAccessibleContext().setAccessibleDescription(getString("ACSD_CTL_Close"));
            options = new Object[] { closeButton };
            defaultOption = closeButton;
        } else {
            okButton = new JButton(getString("CTL_OK"));
            okButton.getAccessibleContext().setAccessibleDescription(getString("ACSD_CTL_OK"));

            JButton cancelButton = new JButton(getString("CTL_Cancel"));
            cancelButton.setVerifyInputWhenFocusTarget(false);
            cancelButton.getAccessibleContext().setAccessibleDescription(getString("ACSD_CTL_Cancel"));
            cancelButton.setDefaultCapable(false);

            if (defaultValue) {
                JButton defaultButton = new JButton();
                String name = getString("CTL_Default");
                Mnemonics.setLocalizedText(defaultButton, name);
                defaultButton.setActionCommand(name);
                defaultButton.getAccessibleContext().setAccessibleDescription(getString("ACSD_CTL_Default"));
                defaultButton.setDefaultCapable(false);
                defaultButton.setVerifyInputWhenFocusTarget(false);

                if ("Aqua".equals(UIManager.getLookAndFeel().getID())) {
                    //Support Aqua button ordering
                    options = new Object[] { defaultButton, cancelButton, okButton };
                } else {
                    options = new Object[] { okButton, defaultButton, cancelButton };
                }
            } else {
                if ("Aqua".equals(UIManager.getLookAndFeel().getID())) {
                    options = new Object[] { cancelButton, okButton };
                } else {
                    options = new Object[] { okButton, cancelButton };
                }
            }

            defaultOption = okButton;
        }

        if ((env != null) && (okButton != null)) {
            okButtonState = env.getState() != PropertyEnv.STATE_INVALID;

            if (okButton != null) {
                okButton.setEnabled(okButtonState);
            }
        }

        if (env != null) {
            envStateBeforeDisplay = env.getState();
        }

        
        // create dialog descriptor, create & return the dialog
        // bugfix #24998, set helpCtx obtain from PropertyEnv.getFeatureDescriptor()
        DialogDescriptor descriptor = new DialogDescriptor(component, title,
                isModal, options, defaultOption, DialogDescriptor.DEFAULT_ALIGN,
                helpCtx, this);
        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
    }

    /** Initializes dialog listeners. Must be called after
     * createDialog method call. (dialog variable must not be null)
     */
    private void initializeListeners() {
        // dialog closing reactions
        dialog.addWindowListener(
            new WindowAdapter() {
                /** Ensure that values are reverted when user cancelles dialog
                 * by clicking on x image */
                @Override
                public void windowClosing(WindowEvent e) {
                    if ((editor != null) && !(component instanceof Window)) {
                        // if someone provides a window (s)he has to handle
                        // the cancel him/herself
                        cancelled = true; // XXX shouldn't this be reset otherwise?
                        cancelValue();
                    }

                    // ensure that resources are released
                    if (env != null) {
                        env.removeVetoableChangeListener(PropertyDialogManager.this);
                    }

                    dialog.dispose();
                }

                /** Remove property listener on window close */
                @Override
                public void windowClosed(WindowEvent e) {
                    if (component instanceof Window) {
                        // in this case we have to do similar thing as we do
                        // directly after the Ok button is pressed. The difference
                        // is in the fact that here we do not decide whether the
                        // dialog will be closed or not - it is simply being closed
                        // But we have to take care of propagating the value to
                        // the model
                        if (component instanceof EnhancedCustomPropertyEditor) {
                            try {
                                Object newValue = ((EnhancedCustomPropertyEditor) component).getPropertyValue();
                                model.setValue(newValue);
                            } catch (java.lang.reflect.InvocationTargetException ite) {
                                notifyUser(ite);
                            } catch (IllegalStateException ise) {
                                notifyUser(ise);
                            }
                        } else if ((env != null) && (!env.isChangeImmediate())) {
                            try {
                                model.setValue(lastValueFromEditor);
                            } catch (java.lang.reflect.InvocationTargetException ite) {
                                notifyUser(ite);
                            } catch (IllegalStateException ise) {
                                notifyUser(ise);
                            }
                        }
                    }

                    if (editorListener != null) {
                        editor.removePropertyChangeListener(editorListener);
                    }
                    
                    if (componentListener != null) {
                        component.removePropertyChangeListener(componentListener);
                    }

                    dialog.removeWindowListener(this);
                }
            }
        );

        // reactions to editor property changes
        if (editor != null) {
            try {
                oldValue = model.getValue();
            } catch (Exception e) {
                // Ignored, there can be number of exceptions
                // when asking for old values...
            }

            lastValueFromEditor = editor.getValue();
            editor.addPropertyChangeListener(
                editorListener = new PropertyChangeListener() {
                        /** Notify displayer about property change in editor */
                        public void propertyChange(PropertyChangeEvent e) {
                            changed = true;
                            lastValueFromEditor = editor.getValue();

                            // enabling/disabling the okButton in response to
                            // firing PROP_VALUE_VALID --- usage of this firing
                            // has been deprecated in favor of directly calling
                            // PropertyEnv.setState(...)
                            if (ExPropertyEditor.PROP_VALUE_VALID.equals(e.getPropertyName())) {
                                if (okButton != null) {
                                    if (e.getNewValue() instanceof Boolean) {
                                        Boolean newButtonState = (Boolean) e.getNewValue();
                                        okButtonState = newButtonState.booleanValue();

                                        if (env != null) {
                                            env.setState(
                                                    okButtonState ? PropertyEnv.STATE_VALID : PropertyEnv.STATE_INVALID
                                                );
                                        } else {
                                            okButton.setEnabled(okButtonState);
                                        }

                                        if (e.getOldValue() instanceof Runnable) {
                                            errorPerformer = (Runnable) e.getOldValue();
                                        } else {
                                            errorPerformer = null;
                                        }
                                    }
                                }
                            }
                        }
                    }
            );

            if (component == null) {
                throw new NullPointerException(editor.getClass().getName() + " returned null from getCustomEditor()");
            }

            component.addPropertyChangeListener(
                componentListener = new PropertyChangeListener() {
                        /** forward possible help context change in custom editor */
                        public void propertyChange(PropertyChangeEvent e) {
                            if (DialogDescriptor.PROP_HELP_CTX.equals(e.getPropertyName())) {
                                if (dialog instanceof PropertyChangeListener) {
                                    ((PropertyChangeListener) dialog).propertyChange(e);
                                }
                            }
                        }
                    }
            );
        }
    }

    /**
     * Reverts to old values.
     */
    private void cancelValue() {
        try {
            if( env != null && env.isEditable() )
                editor.setValue(oldValue);
        } catch( Exception e ) {
            // Ignored, there can be number of exceptions
            // when asking for old values...
        }
        if (
            (!changed) || (component instanceof EnhancedCustomPropertyEditor) ||
                ((env != null) && (!env.isChangeImmediate()))
        ) {
            if ((env != null) && (envStateBeforeDisplay != null)) {
                env.setState(envStateBeforeDisplay);
            }

            return;
        }

        try {
            model.setValue(oldValue);
        } catch (Exception e) {
            // Ignored, there can be number of exceptions
            // when asking for old values...
        }
    }

    public boolean wasCancelled() {
        return cancelled;
    }

    public boolean wasOK() {
        return ok;
    }

    public boolean wasReset() {
        return reset;
    }

    /** Called when user presses a button on some option (button) in the
     * dialog.
     * @param evt The button press event.
     */
    public void actionPerformed(ActionEvent evt) {
        String label = evt.getActionCommand();
        if (label.equals(getString("CTL_Cancel"))) {
            cancelled = true; // XXX shouldn't this be reset otherwise?
            cancelValue();
        }

        ok = false;
        reset = false;

        if (label.equals(getString("CTL_Default"))) {
            reset = true;

            if (prop != null) {
                try {
                    prop.restoreDefaultValue();
                } catch (IllegalAccessException iae) {
                    notifyUser(iae);
                } catch (java.lang.reflect.InvocationTargetException ite) {
                    notifyUser(ite);
                }
            }
        }

        if (label.equals(getString("CTL_OK"))) {
            ok = true;

            if ((env != null) && (env.getState() == PropertyEnv.STATE_NEEDS_VALIDATION)) {
                env.setState(PropertyEnv.STATE_VALID);

                if (env.getState() != PropertyEnv.STATE_VALID) {
                    // if the change was vetoed do nothing and return
                    return;
                }
            }

            if (component instanceof EnhancedCustomPropertyEditor) {
                try {
                    Object newValue = ((EnhancedCustomPropertyEditor) component).getPropertyValue();
                    model.setValue(newValue);
                } catch (java.lang.reflect.InvocationTargetException ite) {
                    notifyUser(ite);
                    return;
                } catch (IllegalStateException ise) {
                    notifyUser(ise);
                    return;
                } catch (IllegalArgumentException iae) {
                    notifyUser(iae);
                    return;
                }
            } else if ((env != null) && (!env.isChangeImmediate())) {
                try {
                    model.setValue(lastValueFromEditor);
                } catch (java.lang.reflect.InvocationTargetException ite) {
                    notifyUser(ite);
                    return;
                } catch (IllegalStateException ise) {
                    notifyUser(ise);
                    return;
                }
            }

            // this is an old hack allowing to notify a cached value
            // obtained via propertyChangeEvent from the editor
            if (!okButtonState) {
                if (errorPerformer != null) {
                    errorPerformer.run();
                }

                return;
            }
        }

        // close the dialog
        changed = false;

        if (env != null) {
            env.removeVetoableChangeListener(this);
        }

        dialog.dispose();
    }

    private static String getString(String key) {
        return NbBundle.getMessage(PropertyDialogManager.class, key);
    }

    /** For testing purposes we need to _not_ notify some exceptions.
     * That is why here is a package private method to register an exception
     * that should not be fired.
     */
    static void doNotNotify(Throwable ex) {
        doNotNotify = ex;
    }

    /** Notifies an exception to error manager or prints its it to stderr.
     * @param ex exception to notify
     */
    static void notify(Throwable ex) {
        Throwable d = doNotNotify;
        doNotNotify = null;

        if (d == ex) {
            return;
        }

        if( ex instanceof PropertyVetoException )
            Exceptions.attachLocalizedMessage(ex, ex.getLocalizedMessage());
        
        Exceptions.printStackTrace(ex);
    }

    /**
     * Tries to find an annotation with localized message(primarily) or general
     * message in all exception annotations. If the annotation with the message
     * is found it is notified with original severity. If the message is not
     * found the exception is notified with informational severity.
     *
     * @param e exception to notify
     */
    private static void notifyUser(Exception e) {
        String userMessage = Exceptions.findLocalizedMessage(e);

        if (userMessage != null) {
            Exceptions.printStackTrace(e);
        } else {
            // if there is not user message don't bother user, just log an
            // exception
            Logger.getLogger(PropertyDialogManager.class.getName()).log(Level.INFO, null, e);
        }
    }

    Component getComponent() {
        return component;
    }

    PropertyEditor getEditor() {
        return editor;
    }

}
