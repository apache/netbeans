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

package org.netbeans.modules.j2ee.jboss4.config;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.spi.MessageDestinationDeployment;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.xml.sax.SAXException;

/**
 *
 * @author Petr Hejl
 */
public final class JBossMessageDestinationManager implements MessageDestinationDeployment {

    private static final Logger LOGGER = Logger.getLogger(JBossMessageDestinationManager.class.getName());

    private final FileObject serverDir;

    private final boolean isAs7;

    public JBossMessageDestinationManager(String serverUrl, boolean isAs7) {
        String serverDirPath = InstanceProperties.getInstanceProperties(serverUrl).getProperty(
                JBPluginProperties.PROPERTY_SERVER_DIR);
        serverDir = FileUtil.toFileObject(new File(serverDirPath));
        this.isAs7 = isAs7;
    }

    @Override
    public Set<MessageDestination> getMessageDestinations() throws ConfigurationException {
        Set<MessageDestination> messageDestinations = new HashSet<MessageDestination>();
        if (isAs7) {
            FileObject config = serverDir.getFileObject("configuration/standalone.xml");
            if (config == null) {
                config = serverDir.getFileObject("configuration/domain.xml");
            }
            if (config == null || !config.isData()) {
                LOGGER.log(Level.WARNING, NbBundle.getMessage(JBossDatasourceManager.class, "ERR_WRONG_JMS_CONFIG_FILE"));
                return messageDestinations;
            }
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();
                JB7MessageDestinationHandler handler = new JB7MessageDestinationHandler();
                InputStream is = new BufferedInputStream(config.getInputStream());
                try {
                    parser.parse(is, handler);
                } finally {
                    is.close();
                }
                messageDestinations.addAll(handler.getMessageDestinations());
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING,
                        NbBundle.getMessage(JBossDatasourceManager.class, "ERR_WRONG_JMS_CONFIG_FILE"), ex);
            } catch (SAXException ex) {
                LOGGER.log(Level.WARNING,
                        NbBundle.getMessage(JBossDatasourceManager.class, "ERR_WRONG_JMS_CONFIG_FILE"), ex);
            } catch (ParserConfigurationException ex) {
                LOGGER.log(Level.WARNING,
                        NbBundle.getMessage(JBossDatasourceManager.class, "ERR_WRONG_JMS_CONFIG_FILE"), ex);
            }

            return messageDestinations;
        }

        return messageDestinations;
    }

    @Override
    public void deployMessageDestinations(Set<MessageDestination> destinations) throws ConfigurationException {
    }

}
