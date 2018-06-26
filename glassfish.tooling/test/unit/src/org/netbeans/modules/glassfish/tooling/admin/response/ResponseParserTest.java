/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.glassfish.tooling.admin.response;

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

    public static final String PATH = System.getProperty("user.dir") + "/src/test/java/org/netbeans/modules/glassfish/tooling/admin/response/";

    @Test
    public void restXmlParserSimpleMessage() throws FileNotFoundException, IOException {
        File f = new File(PATH + "version.xml");
        RestXMLResponseParser p = new RestXMLResponseParser();
        FileInputStream input = new FileInputStream(f);
        RestActionReport report = p.parse(input);
        input.close();

        assertTrue(report.getExitCode().equals(ActionReport.ExitCode.SUCCESS));
        assertTrue(report.getCommand().equals("version AdminCommand"));
        assertTrue(report.getMessage().equals("GlassFish Server Open Source Edition 3.1.1 (build 12)"));

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
