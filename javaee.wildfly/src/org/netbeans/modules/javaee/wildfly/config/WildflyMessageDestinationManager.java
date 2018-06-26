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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.javaee.wildfly.config;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.spi.MessageDestinationDeployment;
import org.netbeans.modules.javaee.wildfly.WildflyDeploymentManager;
import org.netbeans.modules.javaee.wildfly.config.xml.jms.WildflyMessageDestinationHandler;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.xml.sax.SAXException;

/**
 *
 * @author Petr Hejl
 * @author Emmanuel Hugonnet (ehsavoie) <ehsavoie@netbeans.org>
 */
public final class WildflyMessageDestinationManager implements MessageDestinationDeployment {

    private static final Logger LOGGER = Logger.getLogger(WildflyMessageDestinationManager.class.getName());

    private final FileObject serverDir;
    private final FileObject configFile;
    private final WildflyDeploymentManager dm;

    public WildflyMessageDestinationManager(WildflyDeploymentManager dm) {
        this.dm = dm;
        InstanceProperties ip = InstanceProperties.getInstanceProperties(dm.getUrl());
        String serverDirPath = ip.getProperty(WildflyPluginProperties.PROPERTY_SERVER_DIR);
        serverDir = FileUtil.toFileObject(new File(serverDirPath));
        FileObject config = null;
        if(ip.getProperty(WildflyPluginProperties.PROPERTY_CONFIG_FILE) != null){
            config = FileUtil.toFileObject(new File(ip.getProperty(WildflyPluginProperties.PROPERTY_CONFIG_FILE)));
        }
        if (config == null) {
            config = serverDir.getFileObject("configuration/standalone.xml");
        }
        if (config == null) {
            config = serverDir.getFileObject("configuration/domain.xml");
        }
        this.configFile = config;
    }

    @Override
    public Set<MessageDestination> getMessageDestinations() throws ConfigurationException {
        Set<MessageDestination> messageDestinations = new HashSet<MessageDestination>();
        if (configFile == null || !configFile.isData()) {
            LOGGER.log(Level.WARNING, NbBundle.getMessage(WildflyDatasourceManager.class, "ERR_WRONG_JMS_CONFIG_FILE"));
            return messageDestinations;
        }
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            WildflyMessageDestinationHandler handler = new WildflyMessageDestinationHandler();
            InputStream is = new BufferedInputStream(configFile.getInputStream());
            try {
                parser.parse(is, handler);
            } finally {
                is.close();
            }
            messageDestinations.addAll(handler.getMessageDestinations());
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING,
                    NbBundle.getMessage(WildflyDatasourceManager.class, "ERR_WRONG_JMS_CONFIG_FILE"), ex);
        } catch (SAXException ex) {
            LOGGER.log(Level.WARNING,
                    NbBundle.getMessage(WildflyDatasourceManager.class, "ERR_WRONG_JMS_CONFIG_FILE"), ex);
        } catch (ParserConfigurationException ex) {
            LOGGER.log(Level.WARNING,
                    NbBundle.getMessage(WildflyDatasourceManager.class, "ERR_WRONG_JMS_CONFIG_FILE"), ex);
        }

        return messageDestinations;
    }

    private Map<String, MessageDestination> createMap(Set<MessageDestination> destinations) {
        if (destinations.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, MessageDestination> map = new HashMap<String, MessageDestination>();
        for (MessageDestination destination : destinations) {
            map.put(destination.getName(), destination);
            WildflyMessageDestination jbossMessageDestination = (WildflyMessageDestination) destination;
            for (String jndiName : jbossMessageDestination.getJndiNames()) {
                map.put(jndiName, destination);
            }
        }
        return map;
    }

    @Override
    public void deployMessageDestinations(Set<MessageDestination> destinations) throws ConfigurationException {
        Set<MessageDestination> deployedDestinations = getMessageDestinations();
        // for faster searching
        Map<String, MessageDestination> deployed = createMap(deployedDestinations);

        // will contain all ds which do not conflict with existing ones
        List<WildflyMessageDestination> toDeploy = new ArrayList<WildflyMessageDestination>();

        // resolve all conflicts
        LinkedList<MessageDestination> conflictJMS = new LinkedList<MessageDestination>();
        for (MessageDestination destination : destinations) {
            if (!(destination instanceof WildflyMessageDestination)) {
                LOGGER.log(Level.INFO, "Unable to deploy {0}", destination);
                continue;
            }

            WildflyMessageDestination jbossMessageDestination = (WildflyMessageDestination) destination;
            String name = jbossMessageDestination.getName();
            Set<String> jndiNames = new HashSet<String>(jbossMessageDestination.getJndiNames());
            jndiNames.retainAll(deployed.keySet());
            if (deployed.keySet().contains(jbossMessageDestination.getName()) || !jndiNames.isEmpty()) { // conflicting destination found
                MessageDestination deployedMessageDestination = deployed.get(name);
                // name is same, but message dest differs
                if (!deployedMessageDestination.equals(jbossMessageDestination)) {
                    conflictJMS.add(deployed.get(name));
                }
            }else {
                toDeploy.add(jbossMessageDestination);
            }
        }

        if (!conflictJMS.isEmpty()) {
            // TODO exception or nothing ?
        }
        ProgressObject po = dm.deployMessageDestinations(toDeploy);

        ProgressObjectSupport.waitFor(po);

        if (po.getDeploymentStatus().isFailed()) {
            String msg = NbBundle.getMessage(WildflyMessageDestinationManager.class, "MSG_FailedToDeployJMS");
            throw new ConfigurationException(msg);
        }
    }

}
