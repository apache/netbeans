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

package org.netbeans.modules.j2ee.dd.impl.ejb.annotation;

import java.io.IOException;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.impl.common.annotation.CommonTestCase;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;

/**
 *
 * @author Martin Adamek
 */
public class MessageDrivenImplTest extends CommonTestCase  {

    public MessageDrivenImplTest(String testName) {
        super(testName);
    }
    
    public void testMessageDriven() throws IOException, InterruptedException {
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomerMDB.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "import javax.jms.*;" +
                "@MessageDriven(mappedName = \"jms/CustomerMDB\", activationConfig =  {" +
                "        @ActivationConfigProperty(propertyName = \"acknowledgeMode\", propertyValue = \"Auto-acknowledge\")," +
                "        @ActivationConfigProperty(propertyName = \"destinationType\", propertyValue = \"javax.jms.Queue\")" +
                "    })" +
                "public class CustomerMDB implements MessageListener {" +
                "public void onMessage(Message message) {}" +
                "}");

        final String expectedResult = "foo";

        Void result = createEjbJarModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
            public Void run(EjbJarMetadata metadata) throws VersionNotSupportedException {
                // test CustomerMDB
                MessageDriven messageDriven = (MessageDriven) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getMessageDriven(), "CustomerMDB");
                assertEquals("foo.CustomerMDB", messageDriven.getEjbClass());
                assertEquals("CustomerMDB", messageDriven.getEjbName());
                return null;
            }
        });
    }
    
}
