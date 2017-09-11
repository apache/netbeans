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

package org.netbeans.modules.maven.output;

import java.util.regex.Matcher;
import junit.framework.TestCase;

public class GlobalOutputProcessorTest extends TestCase {

    public GlobalOutputProcessorTest(String testName) {
        super(testName);
    }

    public void testModelProblemPattern() {
        Matcher m = GlobalOutputProcessor.MODEL_PROBLEM.matcher("[WARNING] 'reporting.plugins.plugin.version' for org.apache.maven.plugins:maven-plugin-plugin is missing. @ line 60, column 21");
        assertTrue(m.matches());
        assertEquals(null, m.group(1));
        assertEquals("60", m.group(2));
        assertEquals("21", m.group(3));
        m = GlobalOutputProcessor.MODEL_PROBLEM.matcher("[WARNING] 'build.plugins.plugin.version' for org.apache.maven.plugins:maven-jar-plugin is missing. @ org.glassfish:glassfish-main-parent:3.2-SNAPSHOT, /sources/glassfish/pom.xml, line 574, column 21");
        assertTrue(m.matches());
        assertEquals("/sources/glassfish/pom.xml", m.group(1));
        assertEquals("574", m.group(2));
        assertEquals("21", m.group(3));
    }

    public void testModelProblemPattern2() {
        Matcher m = GlobalOutputProcessor.MODEL_PROBLEM2.matcher( "Non-parseable POM C:\\Users\\mkleint\\AppData\\Local\\Temp\\app1\\pom.xml: Duplicated tag: 'properties' (position: START_TAG seen ...</properties>\\r\\n  <properties>... @16:15)  @ line 16, column 15 -> [Help 2]");
        assertTrue(m.matches());
        assertEquals("C:\\Users\\mkleint\\AppData\\Local\\Temp\\app1\\", m.group(1));
        assertEquals("16", m.group(3));
        assertEquals("15", m.group(4));
        
        m = GlobalOutputProcessor.MODEL_PROBLEM2.matcher("Non-parseable POM C:\\Users\\mkleint\\AppData\\Local\\Temp\\app1\\pom.xml: TEXT must be immediately followed by END_TAG and not START_TAG (position: START_TAG seen ...<dependencies>\\r\\n    <dependency>... @17:17)  @ line 17, column 17 -> [Help 2]");
        assertTrue(m.matches());
        assertEquals("C:\\Users\\mkleint\\AppData\\Local\\Temp\\app1\\", m.group(1));
        assertEquals("17", m.group(3));
        assertEquals("17", m.group(4));
        
        m = GlobalOutputProcessor.MODEL_PROBLEM2.matcher("Non-parseable POM C:\\Users\\mkleint\\src\\maven-3\\pom.xml: expected name start and not \\n (position: TEXT seen ...</modelVersion>\\n</\\n... @21:1)  @ C:\\Users\\mkleint\\src\\maven-3\\pom.xml, line 21, column 1 -> [Help 2]");
        assertTrue(m.matches());
        assertEquals("C:\\Users\\mkleint\\src\\maven-3\\pom.xml", m.group(2));
        assertEquals("21", m.group(3));
        assertEquals("1", m.group(4));
    }
    
  //"Non-readable POM C:\Users\mkleint\AppData\Local\Temp\app1\pom.xml: no more data available - expected end tags </properties></project> to close start tag <properties> from line 13 and start tag <project> from line 1, parser stopped on END_TAG seen ...</project.build.sourceEncoding>\r\n  \r\n  ... @16:3"
 }
