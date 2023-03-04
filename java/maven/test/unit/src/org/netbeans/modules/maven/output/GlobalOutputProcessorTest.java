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
