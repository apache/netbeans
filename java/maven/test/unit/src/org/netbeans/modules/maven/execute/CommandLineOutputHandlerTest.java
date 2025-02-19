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

package org.netbeans.modules.maven.execute;

import java.util.regex.Matcher;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mkleint
 */
public class CommandLineOutputHandlerTest {

    public CommandLineOutputHandlerTest() {
    }
    
    @Test
    public void testRegExp() throws Exception {
        Matcher m;
        
        m = CommandLineOutputHandler.startPatternM3.matcher("[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ mavenproject3 ---");
        assertTrue(m.matches());
        assertEquals("maven-compiler-plugin", m.group(1));
        assertEquals("compile", m.group(2));
        
        m = CommandLineOutputHandler.startPatternM3.matcher("[INFO] --- surefire:3.2.5:test (default-test) @ mavenproject1 ---");
        assertTrue(m.matches());
        assertEquals("surefire", m.group(1));
        assertEquals("test", m.group(2));
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
        
        m = CommandLineOutputHandler.reactorSummaryLine.matcher("Maven 4 API ........................................ SUCCESS [  1.655 s]");
        assertTrue(m.matches());
        
        m = CommandLineOutputHandler.reactorSummaryLine.matcher("Maven 4 API :: Meta annotations .................... SUCCESS [  0.603 s]");
        assertTrue(m.matches());

    }
}
