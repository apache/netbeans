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
package org.openide;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import java.awt.event.ActionListener;


/** A description of a standard dialog.
 * It may be built later using {@link DialogDisplayer#createDialog} or shown with {@link DialogDisplayer#notify}.
* It extends <code>NotifyDescriptor</code>'s capabilities by allowing specification of the
* modal/nonmodal state of the dialog, button behavior and alignment, help, and
* a listener on button presses.
* Anyone who wants to display some kind of dialog with standard
* behavior should use this class to describe it and
* use <code>createDialog(d)</code> to build it.
* When the dialog is closed you may use {@link #getValue} to determine which button
* closed it.
* <p>The property <code>message</code> (inherited from <code>NotifyDescriptor</code>) is primarily used here
* to specify the inner GUI component of the dialog, in contrast to <code>NotifyDescriptor</code>
* which generally uses a <code>String</code> message.
* <P>
* If you want to set one of the custom Options to be the default Option, it
* is possible to call <code>DialogDescriptor.setValue(<i>the button you want to
* have default...</i>)</code>
*
* @author Dafe Simonek
*/
public class DialogDescriptor extends NotifyDescriptor implements HelpCtx.Provider {
    // Property constants

    /** Name of property for alignment of options. */
    public static final String PROP_OPTIONS_ALIGN = "optionsAlign"; // NOI18N

    /** Name of property for modality of dialog. */
    public static final String PROP_MODAL = "modal"; // NOI18N

    /** Name of property whether the dialg is leaf or can be the owner of other one dialog. */
    public static final String PROP_LEAF = "leaf"; // NOI18N

    /** Name of property for the help context. */
    public static final String PROP_HELP_CTX = "helpCtx"; // NOI18N

    /** Name of property for the button listener. */
    public static final String PROP_BUTTON_LISTENER = "buttonListener"; // NOI18N

    /** Name of property for list of closing options. */
    public static final String PROP_CLOSING_OPTIONS = "closingOptions"; // NOI18N
    public static final int BOTTOM_ALIGN = 0;

    /** Alignment to place options vertically
    * in the right part. */
    public static final int RIGHT_ALIGN = 1;

    /** Alignment to place options in the default manner. */
    public static final int DEFAULT_ALIGN = BOTTOM_ALIGN;

    /** default closing options */
    private static final Object[] DEFAULT_CLOSING_OPTIONS = new Object[] { YES_OPTION, NO_OPTION, CANCEL_OPTION, OK_OPTION };

    // Properties

    /** RW property specifies if the dialog can be owner of other dialogs */
    private boolean leaf = false;

    /** RW property specifying modal status of the dialog */
    private boolean modal;

    /** RW property specifying options alignment,
    * possible values today are BOTTOM_ALIGN, RIGHT_ALIGN, DEFAULT_ALIGN */
    private int optionsAlign;

    /** RW property which specifies help context for the dialog */
    private HelpCtx helpCtx;

    /** RW property which specifies button listener for notifying
    * clients about button presses */
    private ActionListener buttonListener;

    /** array of options that close the dialog when pressed */
    private Object[] closingOptions = DEFAULT_CLOSING_OPTIONS;

    /** Create modal dialog descriptor with given title and inner part,
    * with OK/Cancel buttons with default alignment,
    * no help available. All buttons will close the dialog and the getValue ()
    * will provide the pressed option.
    * @param innerPane inner component of the dialog, or String message
    * @param title title of the dialog
    */
    public DialogDescriptor(final Object innerPane, final String title) {
        this(innerPane, title, true, OK_CANCEL_OPTION, OK_OPTION, DEFAULT_ALIGN, null, null);
    }

    /** Create dialog descriptor with given title, inner part and modal status,
    * with OK/Cancel buttons displayed with default alignment, no help available.
    * If <code>bl</code> is not <code>null</code>, then it will receive notifications when the user
    * presses the buttons. (If no listener is specified, it's still possible
    * to retrieve the user-selected button using {@link NotifyDescriptor#getValue}.)
    *
    * @param innerPane inner component of the dialog, or String message
    * @param title title of the dialog
    * @param isModal modal status
    * @param bl listener for user's button presses
    */
    public DialogDescriptor(final Object innerPane, final String title, final boolean isModal, final ActionListener bl) {
        this(innerPane, title, isModal, OK_CANCEL_OPTION, OK_OPTION, DEFAULT_ALIGN, null, bl);
    }

    /** Create dialog descriptor with given title, inner part, modal status,
    * option type and default option. Options have default alignment, no help available.
    * If the action listener is null, all option buttons will close the dialog and the
    * getValue () will provide the pressed option.
    * @param innerPane inner component of the dialog, or String message
    * @param title title of the dialog
    * @param isModal modal status
    * @param optionType one of the standard options (<code>OK_CANCEL_OPTION</code>, ...)
    * @param initialValue default option (default button)
    * @param bl listener for the user's button presses or null for default close action on all options
    */
    public DialogDescriptor(
        final Object innerPane, final String title, final boolean isModal, final int optionType,
        final Object initialValue, final ActionListener bl
    ) {
        this(innerPane, title, isModal, optionType, initialValue, DEFAULT_ALIGN, null, bl);
    }

    /** Create dialog descriptor; possibility of specifying custom
    * array of options and their alignment.  If the action listener is null,
    * all option buttons will close the dialog and the getValue ()
    * will provide the pressed option.
    * When a custom option set is provided, if any of the standard options
    * (OK_OPTION, CLOSE_OPTION or CANCEL_OPTION) are used, the dialog will close when
    * a button for a standard option is pressed; otherwise for custom options, closing the dialog is left
    * to the <code>ActionListener</code> or <code>setClosingOptions</code>.
    * @param innerPane inner component of the dialog, or String message
    * @param title title of the dialog
    * @param modal modal status
    * @param options array of custom options (<code>null</code> means no options at all);
    *                may include strings (for button labels; such buttons then do nothing by default)
    *                or components (such as buttons,
    *                in which case you are responsible for listening to the buttons yourself)
    * @param initialValue default option from custom option array
    * @param optionsAlign specifies where to place
    *   options in the dialog
    * @param helpCtx help context specifying help page
    * @param bl listener for the user's button presses or <code>null</code> for default close action on all options
    *           (unless you specified the options yourself)
    *
    * @see #setClosingOptions
    */
    public DialogDescriptor(
        final Object innerPane, final String title, final boolean modal, final Object[] options,
        final Object initialValue, final int optionsAlign, final HelpCtx helpCtx, final ActionListener bl
    ) {
        super(innerPane, title, DEFAULT_OPTION, PLAIN_MESSAGE, options, initialValue);
        this.modal = modal;
        this.optionsAlign = optionsAlign;
        this.helpCtx = (helpCtx == null) ? HelpCtx.DEFAULT_HELP : helpCtx;
        this.buttonListener = bl;

        if (bl == null) {
            setClosingOptions(options);
        }
    }

    /** Create dialog descriptor; possibility of specifying custom
    * array of options and their alignment.  If the action listener is null,
    * all option buttons will close the dialog and the getValue ()
    * will provide the pressed option.
    * When a custom option set is provided, if any of the standard options
    * (OK_OPTION, CLOSE_OPTION or CANCEL_OPTION) are used, the dialog will close when
    * a button for a standard option is pressed; otherwise for custom options, closing the dialog is left
    * to the <code>ActionListener</code> or <code>setClosingOptions</code>.
    * @param innerPane inner component of the dialog, or String message
    * @param title title of the dialog
    * @param modal modal status
    * @param options array of custom options (<code>null</code> means no options at all);
    *                may include strings (for button labels; such buttons then do nothing by default)
    *                or components (such as buttons,
    *                in which case you are responsible for listening to the buttons yourself)
    * @param initialValue default option from custom option array
    * @param optionsAlign specifies where to place
    *   options in the dialog
    * @param helpCtx help context specifying help page
    * @param bl listener for the user's button presses or <code>null</code> for default close action on all options
    *           (unless you specified the options yourself)
    * @param leaf property specifies whether the dialog can be owner of other dialogs
    *
    * @see #setClosingOptions
    * @since 5.5
    */
    public DialogDescriptor(
        final Object innerPane, final String title, final boolean modal, final Object[] options,
        final Object initialValue, final int optionsAlign, final HelpCtx helpCtx, final ActionListener bl,
        final boolean leaf
    ) {
        super(innerPane, title, DEFAULT_OPTION, PLAIN_MESSAGE, options, initialValue);
        this.modal = modal;
        this.optionsAlign = optionsAlign;
        this.helpCtx = (helpCtx == null) ? HelpCtx.DEFAULT_HELP : helpCtx;
        this.buttonListener = bl;
        this.leaf = leaf;

        if (bl == null) {
            setClosingOptions(options);
        }
    }

    /** Create dialog descriptor.
    * If the action listener is null, all option buttons will close the dialog and the
    * getValue () will provide the pressed option.
    *
    * @param innerPane inner component of the dialog, or String message
    * @param title title of the dialog
    * @param isModal modal status
    * @param optionType one of the standard options (<code>OK_CANCEL_OPTION</code>, ...)
    * @param initialValue default option (default button)
    * @param optionsAlign specifies where to place
    *   options in the dialog
    * @param helpCtx help context specifying help page
    * @param bl listener for the user's button presses or <code>null</code> for default close action on all options
    *           (unless you specified the options yourself)
    */
    public DialogDescriptor(
        final Object innerPane, final String title, final boolean isModal, final int optionType,
        final Object initialValue, final int optionsAlign, final HelpCtx helpCtx, final ActionListener bl
    ) {
        super(innerPane, title, optionType, PLAIN_MESSAGE, null, initialValue);
        this.modal = isModal;
        this.optionsAlign = optionsAlign;
        this.helpCtx = (helpCtx == null) ? HelpCtx.DEFAULT_HELP : helpCtx;
        this.buttonListener = bl;

        if (bl == null) {
            // if the listener is null all options are closing
            setClosingOptions(null);
        }
    }

    /** Get current option alignment.
    * @return current option alignment
    * @see #setOptionsAlign
    */
    public int getOptionsAlign() {
        getterCalled();

        return optionsAlign;
    }

    /** Set new option alignment. See aligment constants for
    * possible values.
    * Fires property change event if successful.
    *
    * @param optionsAlign new options alignment
    * @throws IllegalArgumentException when unknown alignment is given
    * @see #DEFAULT_ALIGN
    */
    public void setOptionsAlign(final int optionsAlign) {
        if ((optionsAlign != BOTTOM_ALIGN) && (optionsAlign != RIGHT_ALIGN)) {
            throw new IllegalArgumentException(
                NbBundle.getBundle(DialogDescriptor.class).getString("EXC_OptionsAlign")
            );
        }

        if (this.optionsAlign == optionsAlign) {
            return;
        }

        int oldValue = this.optionsAlign;
        this.optionsAlign = optionsAlign;
        firePropertyChange(PROP_OPTIONS_ALIGN, new Integer(oldValue), new Integer(optionsAlign));
    }

    /** Get modal status.
    * @return modal status
    * @see #setModal
    */
    public boolean isModal() {
        getterCalled();

        return modal;
    }

    /** Set new modal status.
    * Fires property change event if successful.
    *
    * @param modal new modal status
    * @see #isModal
    */
    public void setModal(final boolean modal) {
        if (this.modal == modal) {
            return;
        }

        boolean oldModal = this.modal;
        this.modal = modal;
        firePropertyChange(PROP_MODAL, Boolean.valueOf(oldModal), Boolean.valueOf(modal));
    }

    /** Get leaf status. If is leaf then cannot be the owner to other one dialog.
    * @return leaf status
    * @see #setLeaf
    * @since 5.5
    */
    public boolean isLeaf() {
        getterCalled();

        return leaf;
    }

    /** Set new leaf status.
    * Fires property change event if successful.
    *
    * @param leaf new leaf status
    * @see #isLeaf
    * @since 5.5
    */
    public void setLeaf(final boolean leaf) {
        if (this.leaf == leaf) {
            return;
        }

        boolean oldLeaf = this.leaf;
        this.leaf = leaf;
        firePropertyChange(PROP_LEAF, Boolean.valueOf(oldLeaf), Boolean.valueOf(leaf));
    }

    /** Setter for list of options that close the dialog.
    * Special values are:
    * <UL>
    *   <LI>null - all options will close the dialog
    *   <LI>empty array - no option will close the dialog
    * </UL>
    * @param arr array of options that should close the dialog when pressed
    *    if null then all options close the dialog
    */
    public void setClosingOptions(Object[] arr) {
        Object[] old = closingOptions;
        closingOptions = arr;

        firePropertyChange(PROP_CLOSING_OPTIONS, old, arr);
    }

    /** Getter for list of closing options.
    * @return array of options or null
    */
    public Object[] getClosingOptions() {
        getterCalled();

        return closingOptions;
    }

    /** Get current help context asociated with this dialog
    * descriptor.
    * @return current help context
    * @see #setHelpCtx
    */
    public HelpCtx getHelpCtx() {
        getterCalled();

        return helpCtx;
    }

    /** Set new help context for this dialog descriptor.
    * Fires property change event if successful.
    * <p>The implementation should automatically display a help
    * button among the secondary options, without your needing to
    * specify it, if the help context on the descriptor is neither
    * <code>null</code> nor {@link HelpCtx#DEFAULT_HELP}. If the
    * descriptor is <code>null</code>, this feature will be disabled
    * (you can still add your own help button manually if you wish,
    * of course). If <code>DEFAULT_HELP</code> (the default), normally the button
    * will also be disabled, however if the inner pane is a component
    * and this component has an {@link HelpCtx#findHelp associated}
    * help ID, that will be used automatically. So most users should never
    * need to manually add a help button: call this method with the correct
    * context, or associate an ID with the displayed component. Note that to
    * set it to <code>null</code> you must explicitly call this method; passing
    * <code>null</code> in the constructor actually sets it to <code>DEFAULT_HELP</code>.
    *
    * @param helpCtx new help context, can be <code>null</code> (no help)
    * @see #getHelpCtx
    */
    public void setHelpCtx(final HelpCtx helpCtx) {
        if ((this.helpCtx != null) && (this.helpCtx.equals(helpCtx))) {
            return;
        }

        HelpCtx oldHelpCtx = this.helpCtx;
        this.helpCtx = helpCtx;
        firePropertyChange(PROP_HELP_CTX, oldHelpCtx, helpCtx);
    }

    /** Get button listener which listens for the user's button presses.
    * @return current button listener instance or null
    * @see #setButtonListener
    */
    public ActionListener getButtonListener() {
        getterCalled();

        return buttonListener;
    }

    /** Set new button listener instance for this dialog descriptor.
    * Fires property change event if successful.
    *
    * @param l new button listener. It may be <code>null</code>, in which case listening is cancelled.
    * @see #getButtonListener
    */
    public void setButtonListener(final ActionListener l) {
        if (this.buttonListener == l) {
            return;
        }

        ActionListener oldButtonListener = this.buttonListener;
        this.buttonListener = l;
        firePropertyChange(PROP_BUTTON_LISTENER, oldButtonListener, l);
    }
}
