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
package org.netbeans.modules.javaee.wildfly.config.xml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.netbeans.modules.j2ee.deployment.common.api.SocketBinding;
import org.netbeans.modules.javaee.wildfly.config.SocketContainer;
import org.openide.filesystems.FileUtil;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class ConfigurationParserTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testSocketRetrieval() throws Exception {
        File createdFile = folder.newFile("myfile.txt");
        setupXml(createdFile);
        ConfigurationParser parser = ConfigurationParser.INSTANCE;
        Optional<SocketContainer> sockets = parser.getSockets(FileUtil.toFileObject(createdFile));
        assertEquals(7, sockets.get().getSocketBindings().size());
        Optional<SocketBinding> socketByName = sockets.get().getSocketByName("management-http");
        assertTrue(socketByName.isPresent());
        SocketBinding socketBinding = socketByName.get();
        assertEquals(socketBinding.getInterfaceName(), "management");
        assertEquals(socketBinding.getPort(), 19990);
    }

    private void setupXml(File file) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(getXml());
        }
    }

    private String getXml() {
        return "<server>\n"
                + "<interfaces>\n"
                + "        <interface name=\"management\">\n"
                + "            <inet-address value=\"${jboss.bind.address.management:127.0.0.1}\"/>\n"
                + "        </interface>\n"
                + "        <interface name=\"public\">\n"
                + "            <inet-address value=\"${jboss.bind.address:127.0.0.1}\"/>\n"
                + "        </interface>\n"
                + "    </interfaces>\n"
                + "    <socket-binding-group name=\"standard-sockets\" default-interface=\"public\" port-offset=\"${jboss.socket.binding.port-offset:10000}\">\n"
                + "        <socket-binding name=\"ajp\" port=\"${jboss.ajp.port:8009}\"/>\n"
                + "        <socket-binding name=\"http\" port=\"${jboss.http.port:8080}\"/>\n"
                + "        <socket-binding name=\"https\" port=\"${jboss.https.port:8443}\"/>\n"
                + "        <socket-binding name=\"management-http\" interface=\"management\" port=\"${jboss.management.http.port:9990}\"/>\n"
                + "        <socket-binding name=\"management-https\" interface=\"management\" port=\"${jboss.management.https.port:9993}\"/>\n"
                + "        <socket-binding name=\"txn-recovery-environment\" port=\"4712\"/>\n"
                + "        <socket-binding name=\"txn-status-manager\" port=\"4713\"/>\n"
                + "        <outbound-socket-binding name=\"mail-smtp\">\n"
                + "            <remote-destination host=\"${jboss.mail.server.host:localhost}\" port=\"${jboss.mail.server.port:25}\"/>\n"
                + "        </outbound-socket-binding>\n"
                + "    </socket-binding-group>"
                + "</server>";

    }

}
