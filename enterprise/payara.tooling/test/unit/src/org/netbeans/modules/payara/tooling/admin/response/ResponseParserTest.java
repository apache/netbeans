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
package org.netbeans.modules.payara.tooling.admin.response;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * Tests for REST parsers.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
@Test(groups = {"unit-tests"})
public class ResponseParserTest {

    public static final String PATH = System.getProperty("user.dir") + "/src/test/java/org/netbeans/modules/payara/tooling/admin/response/";

    @Test
    public void restXmlParserSimpleMessage() throws FileNotFoundException, IOException {
        File f = new File(PATH + "version.xml");
        RestXMLResponseParser p = new RestXMLResponseParser();
        FileInputStream input = new FileInputStream(f);
        RestActionReport report = p.parse(input);
        input.close();

        assertTrue(report.getExitCode().equals(ActionReport.ExitCode.SUCCESS));
        assertTrue(report.getCommand().equals("version AdminCommand"));
        assertTrue(report.getMessage().equals("Payara Server Open Source Edition 3.1.1 (build 12)"));

        assertNull(report.topMessagePart.getChildren());
    }

    @Test
    public void restXmlParserComplexMessage() throws FileNotFoundException, IOException {
        File f = new File(PATH + "list-jdbc-resources.xml");
        RestXMLResponseParser p = new RestXMLResponseParser();
        FileInputStream input = new FileInputStream(f);
        RestActionReport report = p.parse(input);
        input.close();

        assertTrue(report.getExitCode().equals(ActionReport.ExitCode.SUCCESS));
        assertTrue(report.getCommand().equals("list-jdbc-resources AdminCommand"));
        assertTrue(report.getMessage().equals(""));

        assertNotNull(report.topMessagePart.getChildren());
        assertTrue(report.topMessagePart.getChildren().size() == 3);

        for (MessagePart msg : report.topMessagePart.getChildren()) {
            assertNotNull(msg.getMessage());
            assertFalse("".equals(msg.getMessage()));
            assertTrue((msg.getProperties() == null) || msg.getProperties().isEmpty());
        }
    }
    
    @Test
    public void testJSONParserSimpleMessage() throws FileNotFoundException, IOException {
        File f = new File(PATH + "location.json");
        RestJSONResponseParser p = new RestJSONResponseParser();
        FileInputStream input = new FileInputStream(f);
        RestActionReport report = p.parse(input);
        input.close();
        
        assertTrue(report.getExitCode().equals(ActionReport.ExitCode.SUCCESS));
        assertTrue(report.getCommand().equals("__locations AdminCommand"));
        assertTrue(report.getMessage().equals("/home/piotro/software/ogs/glassfish3/glassfish/domains/domain1"));

        assertNull(report.topMessagePart.getChildren());
    }
    
    @Test
    public void restJSONParserComplexMessage() throws FileNotFoundException, IOException {
        File f = new File(PATH + "list-jdbc-resources.json");
        RestJSONResponseParser p = new RestJSONResponseParser();
        FileInputStream input = new FileInputStream(f);
        RestActionReport report = p.parse(input);
        input.close();

        assertTrue(report.getExitCode().equals(ActionReport.ExitCode.SUCCESS));
        assertTrue(report.getCommand().equals("list-jdbc-resources AdminCommand"));
        assertTrue(report.getMessage().equals(""));

        assertNotNull(report.topMessagePart.getChildren());
        assertTrue(report.topMessagePart.getChildren().size() == 2);

        for (MessagePart msg : report.topMessagePart.getChildren()) {
            assertNotNull(msg.getMessage());
            assertFalse("".equals(msg.getMessage()));
            assertTrue((msg.getProperties() == null) || msg.getProperties().isEmpty());
        }
    }
}
