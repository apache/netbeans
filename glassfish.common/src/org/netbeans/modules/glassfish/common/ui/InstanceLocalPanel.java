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

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.glassfish.tooling.utils.ServerUtils;
import org.netbeans.modules.glassfish.common.GlassFishLogger;
import org.netbeans.modules.glassfish.common.GlassfishInstance;
import org.netbeans.modules.glassfish.common.PortCollection;
import org.netbeans.modules.glassfish.common.utils.Util;
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
            = GlassFishLogger.get(InstanceLocalPanel.class);

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of local GlassFish server properties editor.
     * <p/>
     * @param instance Local GlassFish server instance to be modified.
     */
    public InstanceLocalPanel(final GlassfishInstance instance) {
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
     * Ports values are initialized with values stored in GlassFish instance
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
     * Get local host field value to be stored into local GlassFish server
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
