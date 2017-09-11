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

package org.netbeans.modules.maven.execute;

import java.util.regex.Matcher;
import static junit.framework.TestCase.fail;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mkleint
 */
public class CommandLineOutputHandlerTest {

    public CommandLineOutputHandlerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testDownloadPattern() {
        String[] lines = {
            "51521/?", "11/12K", "11/12M", "51521/120000b",
            "51521/? 12/25K", "34/263M 464/500b",
            "51521/? 13/25K 4034/4640M",
            // #189465: M3 ConsoleMavenTransferListener.doProgress
            "59/101 KB    ", "1/3 B  ", "55 KB", "300 B  ",
            "10/101 KB   48/309 KB   ", // sometimes seems to jam
        };
        for (String line : lines) {
            if (!CommandLineOutputHandler.DOWNLOAD.matcher(line).matches()) {
                fail("Line " + line + " not skipped");
            }
        }
    }
    
    @Test
    public void testRegExp() throws Exception {
        Matcher m = CommandLineOutputHandler.startPatternM2.matcher("[INFO] [surefire:test]");
        assertTrue(m.matches());
        assertEquals("surefire", m.group(1));
        assertEquals("test", m.group(2));
        m = CommandLineOutputHandler.startPatternM2.matcher("[INFO] [compiler:testCompile {execution: default-testCompile}]");
        assertTrue(m.matches());
        assertEquals("compiler", m.group(1));
        assertEquals("testCompile", m.group(2));
        m = CommandLineOutputHandler.startPatternM3.matcher("[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ mavenproject3 ---");
        assertTrue(m.matches());
        assertEquals("maven-compiler-plugin", m.group(1));
        assertEquals("compile", m.group(2));
    }

    @Test
    public void testReactorLine() throws Exception {
        //the non event matching..
        Matcher m = CommandLineOutputHandler.reactorFailure.matcher("[INFO] Maven Core ........................................ FAILURE [1.480s]");
        assertTrue(m.matches());
        
        //
        m = CommandLineOutputHandler.reactorSummaryLine.matcher("Maven Core ........................................ FAILURE [1.480s]");
        assertTrue(m.matches());
        
        m = CommandLineOutputHandler.reactorSummaryLine.matcher("Maven Aether Provider ............................. SUCCESS [1.014s]");
        assertTrue(m.matches());

    }
}
