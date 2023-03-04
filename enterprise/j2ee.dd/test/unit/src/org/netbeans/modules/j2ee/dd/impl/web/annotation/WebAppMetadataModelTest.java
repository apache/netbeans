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

package org.netbeans.modules.j2ee.dd.impl.web.annotation;

import java.io.File;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class WebAppMetadataModelTest extends WebAppTestCase {

    // XXX also test version in testDelegation

    public WebAppMetadataModelTest(String name) {
        super(name);
    }

    public void testDelegation() throws Exception {
        FileObject webXmlFO = TestUtilities.copyStringToFileObject(srcFO, "web.xml",
                "<?xml version='1.0' encoding='UTF-8'?>" +
                "<web-app version='2.5' xmlns='http://java.sun.com/xml/ns/javaee' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd'>" +
                "   <servlet>" +
                "       <servlet-name>Servlet</servlet-name>" +
                "       <servlet-class>org.example.Servlet</servlet-class>" +
                "   </servlet>" +
                "</web-app>");
        File webXmlFile = FileUtil.toFile(webXmlFO);
        MetadataUnit metadataUnit = createMetadataUnit(webXmlFile);
        MetadataModel model = createModel(metadataUnit);
        MetadataModelAction<WebAppMetadata, Integer> action = new MetadataModelAction<WebAppMetadata, Integer>() {
            public Integer run(WebAppMetadata metadata) throws Exception {
                WebApp webApp = metadata.getRoot();
                return (webApp != null) ? webApp.getServlet().length : 0;
            }
        };
        assertEquals(1, model.runReadAction(action));
        metadataUnit.changeDeploymentDescriptor(new File("/foo/bar/baz/web.xml"));
        assertEquals(0, model.runReadAction(action));
        metadataUnit.changeDeploymentDescriptor(null);
        assertEquals(0, model.runReadAction(action));
        metadataUnit.changeDeploymentDescriptor(webXmlFile);
        assertEquals(1, model.runReadAction(action));
    }
}
