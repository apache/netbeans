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

package org.netbeans.modules.maven.customizer;

import junit.framework.TestCase;

/**
 * @author mkleint
 */
public class RunJarPanelTest extends TestCase {
    
    public RunJarPanelTest(String testName) {
        super(testName);
    }            

    /**
     * Test of split* method, of class RunJarPanel.
     */
    public void testParams() {
        String line = "-Xmx256m org.milos.Main arg1";
        assertEquals("-Xmx256m", RunJarPanel.splitJVMParams(line));
        assertEquals("org.milos.Main", RunJarPanel.splitMainClass(line));
        assertEquals("arg1", RunJarPanel.splitParams(line));
        
        line = "-Xdebug -Djava.compiler=none -Xnoagent -Xrunjdwp:transport=dt_socket,server=n,address=${jpda.address} -classpath %classpath ${packageClassName}";
        assertEquals("-Xdebug -Djava.compiler=none -Xnoagent -Xrunjdwp:transport=dt_socket,server=n,address=${jpda.address} -classpath %classpath", RunJarPanel.splitJVMParams(line));
        assertEquals("${packageClassName}", RunJarPanel.splitMainClass(line));
        assertEquals("", RunJarPanel.splitParams(line));
        
        line = "-classpath %classpath ${packageClassName} %classpath ${packageClassName}";
        assertEquals("-classpath %classpath", RunJarPanel.splitJVMParams(line));
        assertEquals("${packageClassName}", RunJarPanel.splitMainClass(line));
        assertEquals("%classpath ${packageClassName}", RunJarPanel.splitParams(line));
        
        line = "Main arg1 arg2.xsjs.xjsj.MainParam";
        assertEquals("", RunJarPanel.splitJVMParams(line));
        assertEquals("Main", RunJarPanel.splitMainClass(line));
        assertEquals("arg1 arg2.xsjs.xjsj.MainParam", RunJarPanel.splitParams(line));
        
        //non trimmed line
        line = " -classpath %classpath ${packageClassName} %classpath ${packageClassName} ";
        assertEquals("-classpath %classpath", RunJarPanel.splitJVMParams(line));
        assertEquals("${packageClassName}", RunJarPanel.splitMainClass(line));
        assertEquals("%classpath ${packageClassName}", RunJarPanel.splitParams(line));

        //param with quotes and spaces..
        line = "-Dparam1=\"one two three\" -classpath %classpath ${packageClassName} %classpath ${packageClassName} ";
        assertEquals("-Dparam1=\"one two three\" -classpath %classpath", RunJarPanel.splitJVMParams(line));
        assertEquals("${packageClassName}", RunJarPanel.splitMainClass(line));
        assertEquals("%classpath ${packageClassName}", RunJarPanel.splitParams(line));
        line = "-D\"foo bar=baz quux\" -classpath %classpath my.App";
        assertEquals("-D\"foo bar=baz quux\" -classpath %classpath", RunJarPanel.splitJVMParams(line));
        assertEquals("my.App", RunJarPanel.splitMainClass(line));
        assertEquals("", RunJarPanel.splitParams(line));
        line = "\"-Dfoo bar=baz quux\" -classpath %classpath my.App"; // #199411
        assertEquals("\"-Dfoo bar=baz quux\" -classpath %classpath", RunJarPanel.splitJVMParams(line));
        assertEquals("my.App", RunJarPanel.splitMainClass(line));
        assertEquals("", RunJarPanel.splitParams(line));
    }


}
