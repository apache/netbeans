/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.common.ui;

import java.util.logging.Logger;
import org.netbeans.modules.glassfish.common.GlassFishLogger;
import org.netbeans.modules.glassfish.common.GlassfishInstance;
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
            = GlassFishLogger.get(CommonPasswordPanel.class);

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

    /** GlassFish server instance used to search for supported platforms. */
    final GlassfishInstance instance;

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
     * Creates new GlassFish password panel.
     * <p/>
     * @param descriptor User notification descriptor.
     * @param instance   GlassFish server instance used to search
     *                   for supported platforms.
     * @param message    Message text.
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public CommonPasswordPanel(final NotifyDescriptor descriptor,
            final GlassfishInstance instance, final String message) {
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
