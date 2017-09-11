/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
