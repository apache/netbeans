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

/**
 * Remote instance properties editor.
 * <p/>
 * @author Tomas Kraus
 */
public class InstanceRemotePanel extends InstancePanel {
    
    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Local logger. */
    private static final Logger LOGGER
            = GlassFishLogger.get(InstanceRemotePanel.class);

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of remote GlassFish server properties editor.
     * <p/>
     * @param instance Remote GlassFish server instance to be modified.
     */
    public InstanceRemotePanel(final GlassfishInstance instance) {
        super(instance);
        domainsFolderLabel.setVisible(false);
        domainsFolderField.setVisible(false);
        hostLocalLabel.setVisible(false);
        hostLocalField.setVisible(false);
        localIpCB.setVisible(false);
        domainLabel.setVisible(true);
        domainField.setVisible(true);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented abstract methods                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Remote host field initialization.
     * <p/>
     * Initialize remote host name field with values stored in GlassFish
     * instance object.
     */
    @Override
    protected void initHost() {
        String hostProperty = instance.getHost();
        hostRemoteField.setText(hostProperty != null ? hostProperty : "");
    }

    /**
     * Port fields initialization.
     * <p/>
     * Initialize port fields with values stored in GlassFish instance object.
     */
    @Override
    protected void initPorts() {
        dasPortField.setText(Integer.toString(instance.getAdminPort()));
        httpPortField.setText(Integer.toString(instance.getPort()));
    }

    /**
     * Get remote host field value to be stored into local GlassFish server
     * instance object properties.
     * <p/>
     * @return Remote host field value converted to {@link String}.
     */
    @Override
    protected String getHost() {
        return hostRemoteField.getText().trim();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Enable form fields that can be modified by user.
     * <p/>
     * Set those form fields that can be modified by user as enabled. This
     * is usually done after form has been initialized when all form fields
     * are currently disabled.
     */
    @Override
    protected void enableFields() {
        super.enableFields();
        hostRemoteField.setEnabled(true);
    }

}
