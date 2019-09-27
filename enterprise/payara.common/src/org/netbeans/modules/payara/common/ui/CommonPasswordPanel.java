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
package org.netbeans.modules.payara.common.ui;

import java.util.logging.Logger;
import org.netbeans.modules.payara.common.PayaraLogger;
import org.netbeans.modules.payara.common.PayaraInstance;
import org.openide.NotifyDescriptor;
import static org.openide.NotifyDescriptor.CANCEL_OPTION;
import static org.openide.NotifyDescriptor.OK_OPTION;
import org.openide.util.NbBundle;

/**
 * Common panel for password/credentials panels.
 * <p/>
 * @author Tomas Kraus
 */
abstract class CommonPasswordPanel extends javax.swing.JPanel {
    
    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Local logger. */
    private static final Logger LOGGER
            = PayaraLogger.get(CommonPasswordPanel.class);

    /** Buttons for valid values. */
    static final Object[] validButtons
            = new Object[] {OK_OPTION, CANCEL_OPTION};

    /** Buttons for invalid values. */
    static final Object[] invalidButtons
            = new Object[] {CANCEL_OPTION};

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Set buttons in user notification descriptor depending on form validity.
     * <p/>
     * This method is used in constructor so it's better to be static.
     * <p/>
     * @param descriptor    User notification descriptor.
     * @param javaPlatforms Java SE JDK selection content.
     */
    void setDescriptorButtons(
            NotifyDescriptor descriptor, boolean valid) {
        if (valid)
            descriptor.setOptions(validButtons);
        else
            descriptor.setOptions(invalidButtons);
    }
 
    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Payara server instance used to search for supported platforms. */
    final PayaraInstance instance;

    /** Message to be shown in the panel. */
    final String message;

    /** Username label. */
    final String userLabelText;

    /** Password label. */
    final String passwordLabelText;

    /** Validity of form fields. */
    boolean valid;

    /** User notification descriptor. */
    final NotifyDescriptor descriptor;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates new Payara password panel.
     * <p/>
     * @param descriptor User notification descriptor.
     * @param instance   Payara server instance used to search
     *                   for supported platforms.
     * @param message    Message text.
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public CommonPasswordPanel(final NotifyDescriptor descriptor,
            final PayaraInstance instance, final String message) {
        this.descriptor = descriptor;
        this.instance = instance;
        this.message = message;
        this.userLabelText = NbBundle.getMessage(
                CommonPasswordPanel.class, "CommonPasswordPanel.userLabel");
        this.passwordLabelText = NbBundle.getMessage(
                CommonPasswordPanel.class, "CommonPasswordPanel.passwordLabel");
        this.instance.getAdminUser();
        this.instance.getAdminPassword();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructor helper method to finish form initialization after
     * <code>initComponents()</code> was called.
     */
    void initFileds(final boolean valid) {
        this.valid = valid;
        descriptor.setMessage(this);
        setDescriptorButtons(descriptor, valid);
    }

}
