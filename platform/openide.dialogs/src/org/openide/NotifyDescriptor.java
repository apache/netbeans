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

package org.openide;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.LayoutStyle;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
* This class provides a description of a user notification to be displayed.
* <p>Simple example of usage:
* <pre>
* NotifyDescriptor d =
*     new NotifyDescriptor.Message("Hello...", NotifyDescriptor.INFORMATION_MESSAGE);
* DialogDisplayer.getDefault().notify(d);
* </pre>
* or to get a result:
* <pre>
* NotifyDescriptor d =
*     new NotifyDescriptor.Confirmation("Really do this?!", "Dialog Title",
*                                       NotifyDescriptor.OK_CANCEL_OPTION);
* if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
*     // really do it...
* }
* </pre>
* @see DialogDisplayer#notify
* @author David Peroutka, Jaroslav Tulach
*/
public class NotifyDescriptor extends Object {
    // Property constants

    /** Name of property for the message to be displayed. */
    public static final String PROP_MESSAGE = "message"; // NOI18N

    /** Name of property for the type of message to use. */
    public static final String PROP_MESSAGE_TYPE = "messageType"; // NOI18N

    /** Name of property for the style of options available. */
    public static final String PROP_OPTION_TYPE = "optionType"; // NOI18N

    /** Name of property for the exact list of options. */
    public static final String PROP_OPTIONS = "options"; // NOI18N

    /** Name of property for the value the user selected. */
    public static final String PROP_VALUE = "value"; // NOI18N

    /** Name of property for the dialog title. */
    public static final String PROP_TITLE = "title"; // NOI18N

    /** Name of property for the detail message reported. */
    public static final String PROP_DETAIL = "detail"; // NOI18N

    /** Name of property for the OK button validation. */
    public static final String PROP_VALID = "valid"; // NOI18N

    /**
     * Name of property for toggling the ESC key and title frame button closing.
     * @since 7.35
     */
    public static final String PROP_NO_DEFAULT_CLOSE = "noDefaultClose"; // NOI18N

    /** Name of property for the error message at the bottom of the wizard.
     * To set such message use {@link #createNotificationLineSupport}
     *
     * @since 7.10
     */
    public static final String PROP_ERROR_NOTIFICATION = "errorNotification"; // NOI18N

    /** Name of property for the error message at the bottom of the wizard.
     * To set such message use {@link #createNotificationLineSupport}
     *
     * @since 7.10
     */
    public static final String PROP_WARNING_NOTIFICATION = "warningNotification"; // NOI18N

    /** Name of property for the error message at the bottom of the wizard.
     * To set such message use {@link #createNotificationLineSupport}
     *
     * @since 7.10
     */
    public static final String PROP_INFO_NOTIFICATION = "infoNotification"; // NOI18N

    //
    // Return values
    //

    /** Return value if YES is chosen. */
    public static final Object YES_OPTION = new Integer(JOptionPane.YES_OPTION);

    /** Return value if NO is chosen. */
    public static final Object NO_OPTION = new Integer(JOptionPane.NO_OPTION);

    /** Return value if CANCEL is chosen. */
    public static final Object CANCEL_OPTION = new Integer(JOptionPane.CANCEL_OPTION);

    /** Return value if OK is chosen. */
    public static final Object OK_OPTION = new Integer(JOptionPane.OK_OPTION);

    /** Return value if user closes the window without pressing any button. */
    public static final Object CLOSED_OPTION = new Integer(JOptionPane.CLOSED_OPTION);

    //
    // Option types
    //

    /** Option type used by default. */
    public static final int DEFAULT_OPTION = JOptionPane.DEFAULT_OPTION;

    /** Option type used for negatable confirmations. */
    public static final int YES_NO_OPTION = JOptionPane.YES_NO_OPTION;

    /** Option type used for negatable and cancellable confirmations. */
    public static final int YES_NO_CANCEL_OPTION = JOptionPane.YES_NO_CANCEL_OPTION;

    /** Option type used for cancellable confirmations. */
    public static final int OK_CANCEL_OPTION = JOptionPane.OK_CANCEL_OPTION;

    //
    // Message types
    //

    /** Message type for error messages. */
    public static final int ERROR_MESSAGE = JOptionPane.ERROR_MESSAGE;

    /** Message type for information messages. */
    public static final int INFORMATION_MESSAGE = JOptionPane.INFORMATION_MESSAGE;

    /** Message type for warning messages. */
    public static final int WARNING_MESSAGE = JOptionPane.WARNING_MESSAGE;

    /** Message type for questions. */
    public static final int QUESTION_MESSAGE = JOptionPane.QUESTION_MESSAGE;

    /** Plain message type using no icon. */
    public static final int PLAIN_MESSAGE = JOptionPane.PLAIN_MESSAGE;

    /** Maximum text width to which the text is wrapped */
    private static final int MAXIMUM_TEXT_WIDTH = 100;

    /** preferred width of text area */
    private static final int SIZE_PREFERRED_WIDTH = 350;

    /** preferred height of text area */
    private static final int SIZE_PREFERRED_HEIGHT = 150;
    private Object message;

    /** The message type. */
    private int messageType = PLAIN_MESSAGE;

    /** The option type specifying the user-selectable options. */
    private int optionType;

    /** The option object specifying the user-selectable options. */
    private Object[] options;

    /** The option object specifying the additional user-selectable options. */
    private Object[] adOptions;

    /** The user's choice value object. */
    private Object value;

    /** The default initial value. */
    private Object defaultValue;

    /** The title string for the report. */
    private String title;

    /** Is OK button valid (enabled). */
    private boolean valid = true;

    private NotificationLineSupport notificationLineSupport = null;
    private String infoMsg;
    private String warnMsg;
    private String errMsg;

    /** The object specifying the detail object. */

    //  private Object detail;

    /** Property change support. */
    private PropertyChangeSupport changeSupport;

    /**
     * When true the dialog can be closed only using one of its closing options,
     * ESC key and the close button in its frame will not work.
     */
    private boolean noDefaultClose = false;

    /**
    * Creates a new notify descriptor with specified information to report.
    *
    * If <code>optionType</code> is {@link #YES_NO_OPTION} or {@link #YES_NO_CANCEL_OPTION}
    * and the <code>options</code> parameter is <code>null</code>, then the options are
    * supplied by the look and feel.
    *
    * The <code>messageType</code> parameter is primarily used to supply a
    * default icon from the look and feel.
    *
    * @param message the object to display
    * @param title the title string for the dialog
    * @param optionType indicates which options are available
    * @param messageType indicates what type of message should be displayed
    * @param options an array of objects indicating the possible choices
    * @param initialValue the object that represents the default value
    *
    * @see #getMessage
    * @see #getMessageType
    * @see #getOptions
    * @see #getOptionType
    * @see #getValue
    */
    public NotifyDescriptor(
        Object message, String title, int optionType, int messageType, Object[] options, Object initialValue
    ) {
        checkMessageValidity(message);
        this.message = message;
        this.messageType = messageType;
        this.options = options;
        this.optionType = optionType;
        this.title = title;
        this.value = initialValue;
        this.defaultValue = initialValue;
    }

    /** Method that is called before a value is returned from any of
     * getter methods in this object.
     *
     * Allows subclasses to do some additional initialization actions.
     */
    protected void initialize() {
    }

    /** Checks for initialization.
     */
    final void getterCalled() {
        boolean init = false;

        synchronized (this) {
            if (changeSupport == null) {
                changeSupport = new java.beans.PropertyChangeSupport(this);
                init = true;
            }
        }

        if (init) {
            initialize();
        }
    }

    //
    // Getters/setters for properties.
    //

    /**
    * Return true if OK button is valid (enabled), otherwise return false.
    * @see #setValid
    *
    * @return validity status of OK button.
    */
    public final boolean isValid() {
        getterCalled();

        return valid;
    }

    /** Set validity of OK button.
     * @see #isValid
     * @param newValid validity status of OK button
     */
    public final void setValid(boolean newValid) {
        boolean oldValid = valid;
        valid = newValid;
        firePropertyChange(
            PROP_VALID, oldValid ? Boolean.TRUE : Boolean.FALSE, newValid ? Boolean.TRUE : Boolean.FALSE
        );
    }

    /**
    * Define a descriptive message to be reported.  In the most common
    * usage, the message is just a <code>String</code>.  However, the type
    * of this parameter is actually <code>Object</code>.  Its interpretation depends on
    * its type:
    * <dl compact>
    * <dt><code>Object[]</code><dd> A recursively interpreted series of messages.
    * <dt>{@link Component}<dd> The <code>Component</code> is displayed in the dialog.
    * <dt>{@link javax.swing.Icon}<dd> The <code>Icon</code> is wrapped in a {@link JLabel} and displayed in the dialog.
    * <dt>anything else<dd> The {@link Object#toString string representation} of the object.
    * </dl>
    *
    * @param newMessage the <code>Object</code> to report
    * @see #getMessage
    */
    public void setMessage(Object newMessage) {
        checkMessageValidity(newMessage);
        Object oldMessage = message;

        if (newMessage instanceof String) {
            // bugfix #25457, use JTextArea for word-wrapping
            JTextArea area = new JTextArea((String) newMessage);
            area.setBackground(UIManager.getColor("Label.background")); // NOI18N
            area.setBorder(BorderFactory.createEmptyBorder());
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setEditable(false);
            area.setFocusable(true);
            area.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NotifyDescriptor.class, "ACN_NotifyDescriptor_MessageJTextArea")); // NOI18N
            area.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NotifyDescriptor.class, "ACD_NotifyDescriptor_MessageJTextArea")); // NOI18N
            JScrollPane sp = new JScrollPane(area);
            sp.setBorder(BorderFactory.createEmptyBorder());
            sp.setPreferredSize(new Dimension(SIZE_PREFERRED_WIDTH, SIZE_PREFERRED_HEIGHT));
            newMessage = sp;
        }

        message = newMessage;
        firePropertyChange(PROP_MESSAGE, oldMessage, newMessage);
    }

    private void checkMessageValidity(Object message) {
        if (message instanceof Window) {
            // See https://netbeans.org/bugzilla/show_bug.cgi?id=267337
            throw new IllegalArgumentException("The message must not be a window. message = "+message);
        }
    }

    /**
    * Get the message object.
    * @see #setMessage
    *
    * @return the <code>Object</code> that is to be reported
    */
    public Object getMessage() {
        getterCalled();

        return message;
    }

    /**
    * Define the style of the message.  The look and feel manager may lay out
    * the dialog differently depending on this value, and will often provide
    * a default icon.  The possible values are:
    * <ul>
    * <li>{@link #ERROR_MESSAGE}
    * <li>{@link #INFORMATION_MESSAGE}
    * <li>{@link #WARNING_MESSAGE}
    * <li>{@link #QUESTION_MESSAGE}
    * <li>{@link #PLAIN_MESSAGE}
    * </ul>
    *
    * @param newType the kind of message
    *
    * @see #getMessageType
    */
    public void setMessageType(int newType) {
        if (
            (newType != ERROR_MESSAGE) && (newType != INFORMATION_MESSAGE) && (newType != WARNING_MESSAGE) &&
                (newType != QUESTION_MESSAGE) && (newType != PLAIN_MESSAGE)
        ) {
            throw new IllegalArgumentException(
                "Message type must be one of the following:" // NOI18N
                 +" ERROR_MESSAGE, INFORMATION_MESSAGE," // NOI18N
                 +" WARNING_MESSAGE, QUESTION_MESSAGE or PLAIN_MESSAGE." // NOI18N
                
            );
        }

        int oldType = messageType;
        messageType = newType;
        firePropertyChange(PROP_MESSAGE_TYPE, new Integer(oldType), new Integer(messageType));
    }

    /**
    * Get the message type.
    *
    * @return the message type
    *
    * @see #setMessageType
    */
    public int getMessageType() {
        getterCalled();

        return messageType;
    }

    /**
    * Define the set of options.  The option type is used by the look and
    * feel to determine what options to show (unless explicit options are supplied):
    * <ul>
    * <li>{@link #DEFAULT_OPTION}
    * <li>{@link #YES_NO_OPTION}
    * <li>{@link #YES_NO_CANCEL_OPTION}
    * <li>{@link #OK_CANCEL_OPTION}
    * </ul>
    *
    * @param newType the options the look and feel is to display
    *
    * @see #getOptionType
    * @see #setOptions
    */
    public void setOptionType(int newType) {
        if (
            (newType != DEFAULT_OPTION) && (newType != YES_NO_OPTION) && (newType != YES_NO_CANCEL_OPTION) &&
                (newType != OK_CANCEL_OPTION)
        ) {
            throw new IllegalArgumentException(
                "Option type must be one of the following:" // NOI18N
                 +" DEFAULT_OPTION, YES_NO_OPTION," // NOI18N
                 +" YES_NO_CANCEL_OPTION or OK_CANCEL_OPTION." // NOI18N
                
            );
        }

        int oldType = optionType;
        optionType = newType;
        firePropertyChange(PROP_OPTION_TYPE, new Integer(oldType), new Integer(optionType));
    }

    /**
    * Get the type of options that are to be displayed.
    *
    * @return the option type
    *
    * @see #setOptionType
    */
    public int getOptionType() {
        getterCalled();

        return optionType;
    }

    /**
    * Define an explicit description of the set of user-selectable options.
    * The usual value for the options parameter is an array of
    * <code>String</code>s.  But the parameter type is an array of <code>Object</code>s.  Its
    * interpretation depends on its type:
    * <dl compact>
    * <dt>{@link Component}<dd>The component is added to the button row directly.
    * <dt>{@link javax.swing.Icon}<dd>A {@link javax.swing.JButton} is created with this icon as its label.
    * <dt>anything else<dd>The <code>Object</code> is {@link Object#toString converted} to a string and the result is used to
    *     label a <code>JButton</code>.
    * </dl>
    *
    * @param newOptions an array of user-selectable options
    *
    * @see #getOptions
    */
    public void setOptions(Object[] newOptions) {
        Object[] oldOptions = options;
        options = newOptions;
        firePropertyChange(PROP_OPTIONS, oldOptions, newOptions);
    }

    /**
    * Get the explicit choices the user can make.
    * @return the array of <code>Object</code>s that give the user's choices
    *
    * @see #setOptions
    */
    public Object[] getOptions() {
        getterCalled();

        if (options != null) {
            return options.clone ();
        }

        return options;
    }

    /**
    * Define an explicit description of the set of additional user-selectable options.
    * Additional options are supposed to be used for help button, etc.
    * <P>
    * The usual value for the options parameter is an array of
    * <code>String</code>s.  But the parameter type is an array of <code>Object</code>s.  Its
    * interpretation depends on its type:
    * <dl compact>
    * <dt>{@link Component}<dd>The component is added to the button row directly.
    * <dt>{@link javax.swing.Icon}<dd>A {@link javax.swing.JButton} is created with this icon as its label.
    * <dt>anything else<dd>The <code>Object</code> is {@link Object#toString converted} to a string and the result is used to
    *     label a <code>JButton</code>.
    * </dl>
    *
    * @param newOptions an array of user-selectable options
    *
    * @see #getOptions
    */
    public void setAdditionalOptions(Object[] newOptions) {
        Object[] oldOptions = adOptions;
        adOptions = newOptions;
        firePropertyChange(PROP_OPTIONS, oldOptions, newOptions);
    }

    /**
    * Get the explicit additional choices the user can make.
    * @return the array of <code>Object</code>s that give the user's choices
    *
    * @see #setOptions
    */
    public Object[] getAdditionalOptions() {
        getterCalled();

        if (adOptions != null) {
            return adOptions.clone ();
        }

        return null;
    }

    /**
     * Sets the value w/o firing property change. The caller this is responsible
     * to notify this change.
     */
    void setValueWithoutPCH(Object newValue) {
        value = newValue;
    }

    /**
    * Set the value the user has chosen and fires appropriate property change.
    * You probably do not want to call this yourself, of course.
    *
    * @param newValue the chosen value
    *
    * @see #getValue
    */
    public void setValue(Object newValue) {
        Object oldValue = value;
        setValueWithoutPCH(newValue);
        firePropertyChange(PROP_VALUE, oldValue, newValue);
    }

    /**
    * Get the value the user has selected.
    *
    * @return an <code>Object</code> indicating the option selected by the user
    *
    * @see #setValue
    */
    public Object getValue() {
        getterCalled();

        return value;
    }

    /**
    * Get the default value of descriptor.
    *
    * @return an <code>Object</code> that represents the default value
    * @since 5.9
    */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
    * Set the title string for this report description.
    *
    * @param newTitle the title of this description
    *
    * @see #getTitle
    */
    public void setTitle(String newTitle) {
        Object oldTitle = title;
        title = newTitle;
        firePropertyChange(PROP_TITLE, oldTitle, newTitle);
    }

    /**
    * Get the title string for this report description.
    *
    * @return the title of this description
    *
    * @see #setTitle
    */
    public String getTitle() {
        getterCalled();

        return title;
    }

    /** Create {@link NotificationLineSupport} if you want to notify users
     * using info/warning/error messages in designed line at the bottom
     * of your dialog. These message will be labelled with appropriate icons.
     * <br>
     * Note: Call this method <b>before</b> you call {@link DialogDisplayer#createDialog}
     *
     * @return NotificationLineSupport
     * @since 7.10
     */
    public final NotificationLineSupport createNotificationLineSupport() {
        notificationLineSupport = new NotificationLineSupport (this);
        return notificationLineSupport;
    }

    /** Returns NotificationLineSupport if it was created or <code>null</code> if doesn't.
     * <br>
     * Note: NotificationLineSupport will not be created by default, API client
     * has to create this support purposely with the exception {@link WizardDescriptor}
     * which has such capability longer.
     *
     * @see #createNotificationLineSupport() 
     * @return NotificationLineSupport or null if was not created yet
     * @since 7.10
     */
    public final NotificationLineSupport getNotificationLineSupport() {
        return notificationLineSupport;
    }

    void setInformationMessage (String msg) {
        if (notificationLineSupport == null) {
            throw new IllegalStateException ("NotificationLineSupport wasn't created yet.");
        }
        infoMsg = msg;
        warnMsg = null;
        errMsg = null;
        firePropertyChange (PROP_INFO_NOTIFICATION, null, msg);
    }

    void setWarningMessage (String msg) {
        if (notificationLineSupport == null) {
            throw new IllegalStateException ("NotificationLineSupport wasn't created yet.");
        }
        infoMsg = null;
        warnMsg = msg;
        errMsg = null;
        firePropertyChange (PROP_WARNING_NOTIFICATION, null, msg);
    }

    void setErrorMessage (String msg) {
        if (notificationLineSupport == null) {
            throw new IllegalStateException ("NotificationLineSupport wasn't created yet.");
        }
        infoMsg = null;
        warnMsg = null;
        errMsg = msg;
        firePropertyChange (PROP_ERROR_NOTIFICATION, null, msg);
    }

    String getInformationMessage () {
        if (notificationLineSupport == null) {
            throw new IllegalStateException ("NotificationLineSupport wasn't created yet.");
        }
        return infoMsg;
    }

    String getWarningMessage () {
        if (notificationLineSupport == null) {
            throw new IllegalStateException ("NotificationLineSupport wasn't created yet.");
        }
        return warnMsg;
    }

    String getErrorMessage () {
        if (notificationLineSupport == null) {
            throw new IllegalStateException ("NotificationLineSupport wasn't created yet.");
        }
        return errMsg;
    }

    void clearMessages () {
        if (notificationLineSupport == null) {
            throw new IllegalStateException ("NotificationLineSupport wasn't created yet.");
        }
        infoMsg = null;
        warnMsg = null;
        errMsg = null;
        firePropertyChange (PROP_INFO_NOTIFICATION, null, null);
    }

    /**
    * Define a detail message to be reported.  In the most common usage,
    * this message is just a <code>String</code>.  However, the type of this
    * parameter is actually <code>Object</code>.  Its interpretation depends on its type:
    * <dl compact>
    * <dt><code>Object[]</code><dd> A recursively interpreted series of messages.
    * <dt><code>Throwable</code><dd> A stack trace is displayed.
    * <dt>anything else<dd> The {@link Object#toString string representation} of the object is used.
    * </dl>
    *
    * @param newDetail the detail object of this description
    *
    * @see #getDetail
    *
    public void setDetail(Object newDetail) {
      Object oldDetail = detail;
      detail = newDetail;
      firePropertyChange(PROP_DETAIL, oldDetail, newDetail);
    }

    /**
    * Get the detail object for this description.
    *
    * @return details of this description
    *
    * @see #setTitle
    *
    public Object getDetail() {
      return detail;
      }
      */

    //
    // Support for reporting bound property changes.
    //

    /**
    * Add a {@link PropertyChangeListener} to the listener list.
    *
    * @param listener  the <code>PropertyChangeListener</code> to be added
    */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        getterCalled();
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
    * Remove a {@link PropertyChangeListener} from the listener list.
    *
    * @param listener  the <code>PropertyChangeListener</code> to be removed
    */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (changeSupport != null) {
            changeSupport.removePropertyChangeListener(listener);
        }
    }

    /**
    * Fire a {@link PropertyChangeEvent} to each listener.
    *
    * @param propertyName the programmatic name of the property that was changed
    * @param oldValue the old value of the property
    * @param newValue the new value of the property
    */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (changeSupport != null) {
            changeSupport.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    /**
     * Disable or enabled the closing of dialog window using ESC and the close button
     * in dialog's title bar.
     * @param noDefaultClose True to disabled ESC closing and to disable the close
     * button in dialog's title bar.
     * @since 7.35
     * @see javax.swing.JDialog#DO_NOTHING_ON_CLOSE
     */
    public void setNoDefaultClose( boolean noDefaultClose ) {
        boolean oldValue = this.noDefaultClose;
        this.noDefaultClose = noDefaultClose;
        firePropertyChange(PROP_NO_DEFAULT_CLOSE, oldValue, noDefaultClose);
    }

    /**
     * @return True if ESC key closing and close button in dialog's title bar are disabled,
     * false otherwise.
     * @since 7.35
     */
    public boolean isNoDefaultClose() {
        return noDefaultClose;
    }

    /**
    * Get the title to use for the indicated type.
    * @param messageType the type of message
    * @return the title to use
    */
    protected static String getTitleForType(int messageType) {
        switch (messageType) {
        case ERROR_MESSAGE:
            return NbBundle.getMessage(NotifyDescriptor.class, "NTF_ErrorTitle");

        case WARNING_MESSAGE:
            return NbBundle.getMessage(NotifyDescriptor.class, "NTF_WarningTitle");

        case QUESTION_MESSAGE:
            return NbBundle.getMessage(NotifyDescriptor.class, "NTF_QuestionTitle");

        case INFORMATION_MESSAGE:
            return NbBundle.getMessage(NotifyDescriptor.class, "NTF_InformationTitle");

        case PLAIN_MESSAGE:
            return NbBundle.getMessage(NotifyDescriptor.class, "NTF_PlainTitle");
        }

        return ""; // NOI18N
    }

    /**
    * Provides information about the results of a command.  Offers
    * no user choices; the user can only acknowledge the message.
    */
    public static class Message extends NotifyDescriptor {
        /**
        * Create an informational report about the results of a command.
        *
        * @param message the message object
        * @see NotifyDescriptor#NotifyDescriptor
        */
        public Message(Object message) {
            this(message, INFORMATION_MESSAGE);
        }

        /**
        * Create a report about the results of a command.
        *
        * @param message the message object
        * @param messageType the type of message to be displayed
        * @see NotifyDescriptor#NotifyDescriptor
        */
        public Message(Object message, int messageType) {
            super(
                message, NotifyDescriptor.getTitleForType(messageType), DEFAULT_OPTION, messageType,
                new Object[] { OK_OPTION }, OK_OPTION
            );
        }
    }

    /**
    * Provides a description of a possible action and requests confirmation from the user before proceeding.
    * This should be used to alert the user to a condition
    * or situation that requires the user's decision before proceeding, such
    * as an impending action with potentially destructive or irreversible
    * consequences.  It is conventionally in the form of a question: for example,
    * "Save changes to TestForm?"
    */
    public static class Confirmation extends NotifyDescriptor {
        /**
        * Create a yes/no/cancel question with default title.
        *
        * @param message the message object
        * @see NotifyDescriptor#NotifyDescriptor
        */
        public Confirmation(Object message) {
            this(message, YES_NO_CANCEL_OPTION);
        }

        /**
        * Create a yes/no/cancel question.
        *
        * @param message the message object
        * @param title the dialog title
        * @see NotifyDescriptor#NotifyDescriptor
        */
        public Confirmation(Object message, String title) {
            this(message, title, YES_NO_CANCEL_OPTION);
        }

        /**
        * Create a question with default title.
        *
        * @param message the message object
        * @param optionType the type of options to display to the user
        * @see NotifyDescriptor#NotifyDescriptor
        */
        public Confirmation(Object message, int optionType) {
            this(message, optionType, QUESTION_MESSAGE);
        }

        /**
        * Create a question.
        *
        * @param message the message object
        * @param title the dialog title
        * @param optionType the type of options to display to the user
        * @see NotifyDescriptor#NotifyDescriptor
        */
        public Confirmation(Object message, String title, int optionType) {
            this(message, title, optionType, QUESTION_MESSAGE);
        }

        /**
        * Create a confirmation with default title.
        *
        * @param message the message object
        * @param optionType the type of options to display to the user
        * @param messageType the type of message to use
        * @see NotifyDescriptor#NotifyDescriptor
        */
        public Confirmation(Object message, int optionType, int messageType) {
            super(
                message, NotifyDescriptor.getTitleForType(messageType), optionType, messageType,
                (optionType == DEFAULT_OPTION) ? new Object[] { OK_OPTION } : null, OK_OPTION
            );
        }

        /**
        * Create a confirmation.
        *
        * @param message the message object
        * @param title the dialog title
        * @param optionType the type of options to display to the user
        * @param messageType the type of message to use
        * @see NotifyDescriptor#NotifyDescriptor
        */
        public Confirmation(Object message, String title, int optionType, int messageType) {
            super(
                message, title, optionType, messageType,
                (optionType == DEFAULT_OPTION) ? new Object[] { OK_OPTION } : null, OK_OPTION
            );
        }
    }

    /**
    * Provides a description of an exception that occurred during
    * execution of the IDE. The best is to use this class together with
    * {@link DialogDisplayer#notifyLater} as that allows an exception
    * to be notified from any thread.
    * @deprecated No longer used. Try {@link java.util.logging} or {@link org.openide.util.Exceptions}.
    */
    @Deprecated
    public static final class Exception extends Confirmation {
        static final long serialVersionUID = -3387516993124229948L;

        /**
        * Create an exception report with default message.
        *
        * @param detail the detail object
        */
        public Exception(Throwable detail) {
            this(detail, detail.getMessage());

            // handle InvocationTargetExceptions
            if (detail instanceof InvocationTargetException) {
                Throwable target = ((InvocationTargetException) detail).getTargetException();
                this.setMessage(target);

                Object msgObj = this.getMessage();
                if ((msgObj == null) || "".equals(msgObj)) { // NOI18N

                    String msg = target.getMessage();
                    msg = org.openide.util.Utilities.wrapString(
                            msg, MAXIMUM_TEXT_WIDTH, java.text.BreakIterator.getCharacterInstance(), false
                        );
                    this.setMessage(msg);
                }
            }

            Object obj = this.getMessage();
            // emphasize user-non-friendly exceptions
            if ((obj == null) || "".equals(obj)) { // NOI18N
                this.setMessage(
                    NbBundle.getMessage(
                        NotifyDescriptor.class, "NTF_ExceptionalException", detail.getClass().getName(),
                        System.getProperty("netbeans.user") + java.io.File.separator + "system"
                    )
                ); // NOI18N
                this.setTitle(NbBundle.getMessage(NotifyDescriptor.class, "NTF_ExceptionalExceptionTitle"));
            }
        }

        /**
        * Create an exception report.
        *
        * @param detail the detail object
        * @param message the message object
        */
        public Exception(Throwable detail, Object message) {
            super(message, DEFAULT_OPTION, ERROR_MESSAGE);

            // customize descriptor
            //      this.setDetail(detail);
            this.setTitle(NbBundle.getMessage(NotifyDescriptor.class, "NTF_ExceptionTitle"));
        }
    }

    /**
     * Notification providing a password input.
     */
    public static final class PasswordLine extends InputLine {
        
        /** Construct dialog with the specified title and label text.
        * @param text label text
        * @param title title of the dialog
        */
        public PasswordLine(String text, String title) {
            super(text, title);
        }

        @Override
        JTextField createTextField() {
            return new JPasswordField(25);
        }
        
    }
    
    /** Notification providing for a line of text input.
    * @author Dafe Simonek
    */
    public static class InputLine extends NotifyDescriptor {
        /**
         * Property whose value is the input text, an event is fired
         * when the input text's value changes,
         * if enabled using {@link #setInputTextEventsEnabled(boolean) }.
         *
         * @since 7.70
         */
        public static final String PROP_INPUT_TEXT = "inputText"; // NOI18N

        /**
        * The text field used to enter the input.
        */
        protected JTextField textField;
        private final AtomicBoolean inputTextEventSuppressed = new AtomicBoolean();
        private final AtomicBoolean inputTextEventEnabled = new AtomicBoolean();

        /** Construct dialog with the specified title and label text.
        * @param text label text
        * @param title title of the dialog
        */
            public InputLine(final String text, final String title) {
            this(text, title, OK_CANCEL_OPTION, PLAIN_MESSAGE);
        }

        /** Construct dialog with the specified title, label text, option and
        * message types.
        * @param text label text
        * @param title title of the dialog
        * @param optionType option type (ok, cancel, ...)
        * @param messageType message type (question, ...)
        */
        public InputLine(final String text, final String title, final int optionType, final int messageType) {
            super(null, title, optionType, messageType, null, null);
            super.setMessage(createDesign(text));
        }

        /**
        * Get the text which the user typed into the input line.
        * @return the text entered by the user
        */
        public String getInputText() {
            return textField.getText();
        }

        /**
        * Set the text on the input line.
        * @param text the new text
        */
        public void setInputText(final String text) {
            inputTextEventSuppressed.set(true);
            try {
                textField.setText(text);
                textField.selectAll();
                if (inputTextEventEnabled.get()) {
                    firePropertyChange(PROP_INPUT_TEXT, null, null);
                }
            } finally {
                inputTextEventSuppressed.set(false);
            }
        }

        /**
         * Enable the {@link #PROP_INPUT_TEXT} when the input text is changed.
         *
         * @param value {@code true} if the {@code PROP_INPUT_TEXT} even should be fired
         *              when the input text is modified, {@code false} otherwise.
         *
         * @since 7.70
         */
        public void setInputTextEventEnabled(boolean value) {
            inputTextEventEnabled.set(value);
        }

        /** Make a component representing the input line.
        * @param text a label for the input line
        * @return the component
        */
        protected Component createDesign(final String text) {
            JPanel panel = new JPanel();
            panel.setOpaque (false);

            JLabel textLabel = new JLabel();
            Mnemonics.setLocalizedText(textLabel, text);

            boolean longText = text.length () > 80;
            textField = createTextField(); 
            textField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if (inputTextEventEnabled.get() && !inputTextEventSuppressed.get()) {
                        firePropertyChange(PROP_INPUT_TEXT, null, null);
                    }
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    if (inputTextEventEnabled.get() && !inputTextEventSuppressed.get()) {
                        firePropertyChange(PROP_INPUT_TEXT, null, null);
                    }
                }

                @Override
                public void changedUpdate(DocumentEvent e) {}
            });
            textLabel.setLabelFor(textField);
            
            textField.requestFocus();
            
            GroupLayout layout = new GroupLayout(panel);
            panel.setLayout(layout);
            if (longText) {
                layout.setHorizontalGroup(
                    layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(textLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(32, 32, 32))
                            .addComponent(textField))
                        .addContainerGap())
                );
            } else {
                layout.setHorizontalGroup(
                    layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(textLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textField, GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
                        .addContainerGap())
                );
            }
            if (longText) {
                layout.setVerticalGroup(
                    layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(textLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
            } else {
                layout.setVerticalGroup(
                            layout.createParallelGroup(Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                                    .addComponent(textLabel)
                                    .addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        );
            }

            javax.swing.KeyStroke enter = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0);
            javax.swing.text.Keymap map = textField.getKeymap();

            map.removeKeyStrokeBinding(enter);

            /*

                  textField.addActionListener (new java.awt.event.ActionListener () {
                      public void actionPerformed (java.awt.event.ActionEvent evt) {
            System.out.println("action: " + evt);
                        InputLine.this.setValue (OK_OPTION);
                      }
                    }
                  );
            */
            panel.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(NotifyDescriptor.class, "ACSD_InputPanel")
            );
            textField.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(NotifyDescriptor.class, "ACSD_InputField")
            );
            
            return panel;
        }

        JTextField createTextField() {
            return new JTextField(25);
        }
    }
     // end of InputLine
    
    /** Notification providing a selection list allowing multiple selections.
    * @since 7.60
    */
    public static final class QuickPick extends NotifyDescriptor {

        private final String text;
        private final List<Item> items;
        private final boolean multipleSelection;

        /**
         * Construct dialog with the specified title and label text.
         * @param text label text
         * @param title title of the dialog
         * @param items a list of items
         * @param multipleSelection true if multiple selection allowed
         * @since 7.60
         */
        public QuickPick(final String text, final String title, final List<Item> items, final boolean multipleSelection) {
            super(null, title, OK_CANCEL_OPTION, PLAIN_MESSAGE, null, null);
            this.text = text;
            this.items = items;
            this.multipleSelection = multipleSelection;
        }

        @Override
        public Object getMessage() {
            Object msg = super.getMessage();
            if (msg != null) {
                return msg;
            }
            JPanel panel = new JPanel();
            panel.setOpaque (false);

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(panel);
            panel.setLayout(layout);

            JLabel label = new JLabel();
            Mnemonics.setLocalizedText(label, text);

            GroupLayout.ParallelGroup hGroup = layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(label);
            GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(label);

            final Map<JToggleButton, Item> btn2items = new LinkedHashMap<JToggleButton, Item>();
            ItemListener listener = new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    JToggleButton btn = (JToggleButton) e.getItemSelectable();
                    Item item = btn2items.get(btn);
                    if (item != null) {
                        item.setSelected(btn.isSelected());
                    }
                }
            };

            ButtonGroup buttonGroup = this.multipleSelection ? null : new ButtonGroup();
            for (Item item : items) {
                JToggleButton btn;
                if (buttonGroup != null) {
                    btn = new JRadioButton();
                    buttonGroup.add(btn);
                } else {
                    btn = new JCheckBox();
                }
                btn.setText(item.getLabel());
                btn.setToolTipText(item.getDescription());
                btn.setSelected(item.isSelected());
                hGroup.addComponent(btn);
                vGroup.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btn);
                btn.addItemListener(listener);
                btn2items.put(btn, item);
            }

            layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(hGroup)
                    .addContainerGap())
            );
            layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(vGroup.addContainerGap())
            );

            this.setMessage(panel);
            return panel;
        }

        /**
         * Get label text.
         * @return label text
         * @since 7.63
         */
        public String getLabel() {
            return text;
        }

        /**
         * Get the list of selection items.
         * @return unmodifiable list of items
         * @since 7.60
         */
        public List<Item> getItems() {
            return Collections.unmodifiableList(items);
        }

        /**
         * Check if the picker accepts multiple selections.
         * @return true if multiple selection allowed
         * @since 7.60
         */
        public boolean isMultipleSelection() {
            return multipleSelection;
        }

        /**
         * Item that can be selected from a list of items.
         * @since 7.60
         */
        public static final class Item {

            private final String label;
            private final String description;
            private boolean selected;

            /**
             * Creates item that can be selected from a list of items.
             * @param label item's label
             * @param description item's description
             * @since 7.60
             */
            public Item(String label, String description) {
                this.label = label;
                this.description = description;
            }

            /**
             * Item's label.
             * @since 7.60
             */
            public String getLabel() {
                return label;
            }

            /**
             * Item's description.
             * @since 7.60
             */
            public String getDescription() {
                return description;
            }

            /**
             * Flag indicating if this item is selected.
             * @since 7.60
             */
            public boolean isSelected() {
                return selected;
            }

            /**
             * Marks item as selected.
             * @since 7.60
             */
            public void setSelected(boolean selected) {
                this.selected = selected;
            }
        }
    }

    /** Notification providing a composed input of multiple chained selection lists and/or input lines.
    * @since 7.63
    */
    public static final class ComposedInput extends NotifyDescriptor {

        /** Name of property for the estimated number of chained inputs. */
        public static final String PROP_ESTIMATED_NUMBER_OF_INPUTS = "estimatedNumberOfInputs"; // NOI18N

        private final List<NotifyDescriptor> inputs = new ArrayList<>();
        private final Callback callback;
        private int estimatedNumberOfInputs;

        /** Construct dialog with the specified title and chained inputs.
        * @param title title of the dialog
        * @param estimatedNumberOfInputs estimated number of chained inputs
        * @param callback callback used to create chained inputs
        * @since 7.63
        */
        public ComposedInput(final String title, final int estimatedNumberOfInputs, final Callback callback) {
            super(null, title, OK_CANCEL_OPTION, PLAIN_MESSAGE, null, null);
            this.callback = callback;
            this.estimatedNumberOfInputs = estimatedNumberOfInputs;
        }

        /**
         * Estimated number of chained inputs.
         * @since 7.63
         */
        public int getEstimatedNumberOfInputs() {
            return estimatedNumberOfInputs;
        }

        /**
         * Set estimated number of chained inputs.
         * @param estimatedNumberOfInputs estimated number of chained inputs
         * @since 7.63
         */
        public void setEstimatedNumberOfInputs(int estimatedNumberOfInputs) {
            int oldNumber = this.estimatedNumberOfInputs;
            this.estimatedNumberOfInputs = estimatedNumberOfInputs;
            firePropertyChange(PROP_ESTIMATED_NUMBER_OF_INPUTS, oldNumber, estimatedNumberOfInputs);
        }

        /**
         * Lazy creates chained input of the given ordinal.
         * @param number input number from interval &lt;1, totalInputs+1&gt;
         * @return nested selection list, input line, or null
         * @since 7.63
         */
        public NotifyDescriptor createInput(int number) {
            NotifyDescriptor step = callback.createInput(this, number);
            if (step != null) {
                if (number - 1 < inputs.size()) {
                    inputs.set(number - 1, step);
                } else if (number - 1 == inputs.size()) {
                    inputs.add(step);
                } else {
                    return null;
                }
                if (number >= estimatedNumberOfInputs) {
                    estimatedNumberOfInputs = number;
                }
            }
            return step;
        }

        /**
         * Returns all created chained inputs.
         * @since 7.63
         */
        public NotifyDescriptor[] getInputs() {
            return inputs.toArray(new NotifyDescriptor[0]);
        }

        @Override
        public Object getMessage() {
            Object msg = super.getMessage();
            if (msg != null) {
                return msg;
            }
            JPanel panel = new JPanel();
            panel.setOpaque (false);
            panel.setLayout(new java.awt.GridBagLayout());

            NotifyDescriptor input;
            int i = 0;
            java.awt.GridBagConstraints gridBagConstraints = null;
            while ((input = createInput(++i)) != null) {
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = i - 1;
                gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
                gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
                panel.add((JPanel)input.getMessage(), gridBagConstraints);
            }
            if (gridBagConstraints != null) {
                gridBagConstraints.weighty = 1.0;
            }

            this.setMessage(panel);
            return panel;
        }

        /**
         * Callback used to lazy create chained inputs.
         * @since 7.63
         */
        public static interface Callback {

            /**
             * Lazy creates chained input of the given ordinal.
             * @param input {@link ComposedInput} instance
             * @param number input ordinal from interval &lt;1, totalInputs+1&gt;
             * @return selection list, input line, or null
             * @since 7.63
             */
            public NotifyDescriptor createInput(ComposedInput input, int number);
        }
    }
}
