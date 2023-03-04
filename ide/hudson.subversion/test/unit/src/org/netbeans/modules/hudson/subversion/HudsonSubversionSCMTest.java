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

package org.netbeans.modules.hudson.subversion;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.hudson.spi.HudsonSCM;
import org.netbeans.modules.hudson.spi.HudsonSCM.ConfigurationStatus;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;

public class HudsonSubversionSCMTest extends NbTestCase {

    public HudsonSubversionSCMTest(String n) {
        super(n);
    }

    @Override protected void setUp() throws Exception {
        clearWorkDir();
    }

    public void testNonSVNDir() throws Exception {
        HudsonSCM scm = new HudsonSubversionSCM();
        assertNull(scm.forFolder(getWorkDir()));
    }

    public void testSVNDir() throws Exception {
        HudsonSCM scm = new HudsonSubversionSCM();
        File dir = getWorkDir();
        File dotSvn = new File(dir, ".svn");
        dotSvn.mkdir();
        OutputStream os = new FileOutputStream(new File(dotSvn, "entries"));
        InputStream is = HudsonSubversionSCMTest.class.getResourceAsStream("sample-entries-file");
        int c;
        while ((c = is.read()) != -1) {
            os.write(c);
        }
        is.close();
        os.close();
        HudsonSCM.Configuration cfg = scm.forFolder(dir);
        assertNotNull(cfg);
        Document doc = XMLUtil.createDocument("root", null, null, null);
        cfg.configure(doc);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(doc, baos, "UTF-8");
        assertEquals("<?xml version='1.0' encoding='UTF-8'?>" +
                "<root>" +
                "<scm class='hudson.scm.SubversionSCM'>" +
                "<locations>" +
                "<hudson.scm.SubversionSCM_-ModuleLocation>" +
                "<remote>https://sezpoz.dev.java.net/svn/sezpoz/trunk</remote>" +
                "<local>.</local>" +
                "</hudson.scm.SubversionSCM_-ModuleLocation>" +
                "</locations>" +
                "<useUpdate>false</useUpdate>" +
                "</scm>" +
                "<triggers>" +
                "<hudson.triggers.SCMTrigger>" +
                "<spec>@hourly</spec>" +
                "</hudson.triggers.SCMTrigger>" +
                "</triggers>" +
                "</root>",
                baos.toString("UTF-8").replace('"', '\'').replaceAll("\n *", "").replaceAll("\r|\n", ""));
    }

    public void testSVN17Dir() throws Exception { // #210884
        HudsonSCM scm = new HudsonSubversionSCM();
        File dir = getWorkDir();
        File dotSvn = new File(dir, ".svn");
        dotSvn.mkdir();
        Writer w = new FileWriter(new File(dotSvn, "entries"));
        w.write("12\n");
        w.flush();
        w.close();
        HudsonSCM.Configuration cfg = scm.forFolder(dir);
        assertNotNull(cfg);
        ConfigurationStatus problems = cfg.problems();
        assertNotNull(problems);
        assertEquals(Bundle.ERR_unsupported(), problems.getErrorMessage());
    }

}
