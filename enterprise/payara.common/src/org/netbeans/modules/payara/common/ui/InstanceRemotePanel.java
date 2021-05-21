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
            = PayaraLogger.get(InstanceRemotePanel.class);

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of remote Payara server properties editor.
     * <p/>
     * @param instance Remote Payara server instance to be modified.
     */
    public InstanceRemotePanel(final PayaraInstance instance) {
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
     * Initialize remote host name field with values stored in Payara
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
     * Initialize port fields with values stored in Payara instance object.
     */
    @Override
    protected void initPorts() {
        dasPortField.setText(Integer.toString(instance.getAdminPort()));
        httpPortField.setText(Integer.toString(instance.getPort()));
    }

    /**
     * Get remote host field value to be stored into local Payara server
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
