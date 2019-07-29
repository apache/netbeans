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

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.payara.tooling.utils.ServerUtils;
import org.netbeans.modules.payara.common.PayaraLogger;
import org.netbeans.modules.payara.common.PayaraInstance;
import org.netbeans.modules.payara.common.PortCollection;
import org.netbeans.modules.payara.common.utils.Util;
import org.openide.util.NbBundle;

/**
 * Local instance properties editor.
 * <p/>
 * @author Tomas Kraus
 */
public class InstanceLocalPanel extends InstancePanel {
    
    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Local logger. */
    private static final Logger LOGGER
            = PayaraLogger.get(InstanceLocalPanel.class);

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of local Payara server properties editor.
     * <p/>
     * @param instance Local Payara server instance to be modified.
     */
    public InstanceLocalPanel(final PayaraInstance instance) {
        super(instance);
        hostRemoteLabel.setVisible(false);
        hostRemoteField.setVisible(false);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented abstract methods                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Local host field initialization.
     * <p/>
     * Attempts to initialize host combo box to existing IP address. Host name
     * string is used as a fallback.
     */
    @Override
    protected void initHost() {
        String hostProperty = instance.getHost();
        InetAddress addr;
        try {
            addr = InetAddress.getByName(hostProperty);
            localIpCB.setSelected(addr.isLoopbackAddress());
        } catch (UnknownHostException uhe) {
            addr = null;
            localIpCB.setSelected(true);
            LOGGER.log(Level.INFO,
                    NbBundle.getMessage(InstancePanel.class,
                    "InstanceLocalPanel.initHost.unknownHost", hostProperty));
        }
        ((IpComboBox)hostLocalField).updateModel(ips, localIpCB.isSelected());
        if (addr != null && ips.contains(addr)) {
            ((IpComboBox)hostLocalField).setSelectedItem(addr);
        } else {
            ((IpComboBox)hostLocalField).getEditor().setItem(hostProperty);
        }
    }

    /**
     * Port fields initialization.
     * <p/>
     * Reads port values from server configuration in <code>domain.xml</code>
     * if exists and updates corresponding fields. Port fields values can't
     * be changed when values are coming from server's <code>domain.xml</code>
     * file.
     * Ports values are initialized with values stored in Payara instance
     * object and can be changed when values from server configuration file
     * are not available.
     */
    @Override
    protected void initPorts() {
        PortCollection ports = new PortCollection();
        String domainPath = ServerUtils.getDomainPath(instance);
        if (configFileParsed
                = Util.readServerConfiguration(new File(domainPath), ports)) {
            dasPortField.setText(Integer.toString(ports.getAdminPort()));
            httpPortField.setText(Integer.toString(ports.getHttpPort()));
        } else {
            dasPortField.setText(Integer.toString(instance.getAdminPort()));
            httpPortField.setText(Integer.toString(instance.getPort()));
        }
    }

    /**
     * Get local host field value to be stored into local Payara server
     * instance object properties.
     * <p/>
     * @return Local host field value converted to {@link String}.
     */
    @Override
    protected String getHost() {
        Object hostValue = hostLocalField.getEditor().getItem();
        if (hostValue instanceof IpComboBox.InetAddr) {
            return ((IpComboBox.InetAddr)hostValue).toString();
        } else if (hostValue instanceof String) {
            return (String)hostValue;
        } else {
            return IpComboBox.IP_4_127_0_0_1_NAME;
        }
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
        hostLocalField.setEnabled(true);
        localIpCB.setEnabled(true);
    }

}
