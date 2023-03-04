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
package org.netbeans.modules.maven.runjar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author sdedic
 */
public class MavenExecuteUtilsTest {
    
    public MavenExecuteUtilsTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of joinParameters method, of class MavenExecuteUtils.
     */
    @Test
    public void testJoinParameters() {
        String cmdLine = MavenExecuteUtils.joinParameters(
            "-classpath", "%classpath",
            "-Dfoo=bar bar",
            "pkg.mainClass",
            "/home/\"foo\"/bar",
            "'"
        );
        assertEquals(
            "-classpath %classpath \"-Dfoo=bar bar\" pkg.mainClass /home/\\\"foo\\\"/bar \\'",
            cmdLine
        );
    }

    /**
     * Test of extractDebugJVMOptions method, of class MavenExecuteUtils.
     */
    @Test
    public void testExtractDebugJVMOptions() throws Exception {
        assertEquals(
                Arrays.asList(
                        "-Dfoo",
                        "-Dbar",
                        "pkg.Main",
                        "bar"
                ), MavenExecuteUtils.extractDebugJVMOptions(
                        "-Djava.compiler=none -Dfoo -Xdebug -Dbar -Xnoagent -Xrunjdwp:whatever -agentlib:jdwp=whatewver pkg.Main bar"
        ));
    }

    /**
     * Test of split* method, of class MavenExecuteUtils.
     */
    @Test
    public void testParams() {
        String line = "-Xmx256m org.milos.Main arg1";
        assertEquals("-Xmx256m", MavenExecuteUtils.splitJVMParams(line));
        assertEquals("org.milos.Main", MavenExecuteUtils.splitMainClass(line));
        assertEquals("arg1", MavenExecuteUtils.splitParams(line));
        
        line = "-Xdebug -Djava.compiler=none -Xnoagent -Xrunjdwp:transport=dt_socket,server=n,address=${jpda.address} -classpath %classpath ${packageClassName}";
        assertEquals("-Xdebug -Djava.compiler=none -Xnoagent -Xrunjdwp:transport=dt_socket,server=n,address=${jpda.address} -classpath %classpath", MavenExecuteUtils.splitJVMParams(line));
        assertEquals("${packageClassName}", MavenExecuteUtils.splitMainClass(line));
        assertEquals("", MavenExecuteUtils.splitParams(line));
        
        line = "-classpath %classpath ${packageClassName} %classpath ${packageClassName}";
        assertEquals("-classpath %classpath", MavenExecuteUtils.splitJVMParams(line));
        assertEquals("${packageClassName}", MavenExecuteUtils.splitMainClass(line));
        assertEquals("%classpath ${packageClassName}", MavenExecuteUtils.splitParams(line));
        
        line = "Main arg1 arg2.xsjs.xjsj.MainParam";
        assertEquals("", MavenExecuteUtils.splitJVMParams(line));
        assertEquals("Main", MavenExecuteUtils.splitMainClass(line));
        assertEquals("arg1 arg2.xsjs.xjsj.MainParam", MavenExecuteUtils.splitParams(line));
        
        //non trimmed line
        line = " -classpath %classpath ${packageClassName} %classpath ${packageClassName} ";
        assertEquals("-classpath %classpath", MavenExecuteUtils.splitJVMParams(line));
        assertEquals("${packageClassName}", MavenExecuteUtils.splitMainClass(line));
        assertEquals("%classpath ${packageClassName}", MavenExecuteUtils.splitParams(line));

        //param with quotes and spaces..
        line = "-Dparam1=\"one two three\" -classpath %classpath ${packageClassName} %classpath ${packageClassName} ";
        assertEquals("-Dparam1=\"one two three\" -classpath %classpath", MavenExecuteUtils.splitJVMParams(line));
        assertEquals("${packageClassName}", MavenExecuteUtils.splitMainClass(line));
        assertEquals("%classpath ${packageClassName}", MavenExecuteUtils.splitParams(line));
        line = "-D\"foo bar=baz quux\" -classpath %classpath my.App";
        assertEquals("-D\"foo bar=baz quux\" -classpath %classpath", MavenExecuteUtils.splitJVMParams(line));
        assertEquals("my.App", MavenExecuteUtils.splitMainClass(line));
        assertEquals("", MavenExecuteUtils.splitParams(line));
        line = "\"-Dfoo bar=baz quux\" -classpath %classpath my.App"; // #199411
        assertEquals("\"-Dfoo bar=baz quux\" -classpath %classpath", MavenExecuteUtils.splitJVMParams(line));
        assertEquals("my.App", MavenExecuteUtils.splitMainClass(line));
        assertEquals("", MavenExecuteUtils.splitParams(line));
    }


    /**
     * Test of splitAll method, of class MavenExecuteUtils.
     */
    @Test
    public void testSplitAndJoin() {
        String s = "-classpath %classpath \"-Dfoo=bar   bar\"   pkg.mainClass /home/\\\"foo\\\"/bar \\'";
        List<String> l = new ArrayList<>();
        MavenExecuteUtils.propertySplitter(s, true).forEach(l::add);
        assertEquals(s, String.join(" ", l));
    }

    /**
     * Test of splitJVMParams method, of class MavenExecuteUtils.
     */
    @Test
    public void testSplitJVMParams() {
        assertEquals("-Dx=y -classpath %classpath", MavenExecuteUtils.splitJVMParams("-Dx=y -classpath %classpath ${packageClassName} param1 param2.param3", false));
        assertEquals("-Dx=y -classpath %classpath", MavenExecuteUtils.splitJVMParams("-Dx=y -classpath %classpath ${exec.mainClass} param1 param2.param3", false));

    }

    /**
     * Test of splitMainClass method, of class MavenExecuteUtils.
     */
    @Test
    public void testSplitMainClass() {
        assertEquals("${packageClassName}", MavenExecuteUtils.splitMainClass("${packageClassName}"));
        assertEquals("${exec.mainClass}", MavenExecuteUtils.splitMainClass("${exec.mainClass}"));

        assertEquals("${packageClassName}", MavenExecuteUtils.splitMainClass("-Dx=y -classpath %classpath ${packageClassName} param1 param2.param3"));
        assertEquals("${exec.mainClass}", MavenExecuteUtils.splitMainClass("-Dx=y -classpath %classpath ${exec.mainClass} param1 param2.param3"));

        assertEquals("com.foo.Bar", MavenExecuteUtils.splitMainClass("-Dx=y -classpath %classpath com.foo.Bar param1 param2.param3"));
        assertEquals("param1", MavenExecuteUtils.splitMainClass("-Dx=y -classpath %classpath ${someOtherProperty} param1 param2.param3"));
    }

    /**
     * Test of splitParams method, of class MavenExecuteUtils.
     */
    @Test
    public void testSplitParams() {
        assertEquals("", MavenExecuteUtils.splitParams("${packageClassName}"));
        assertEquals("first second", MavenExecuteUtils.splitParams("${packageClassName} first second"));
        assertEquals("first \"second\"", MavenExecuteUtils.splitParams("${packageClassName} first \"second\""));
        assertEquals("first \"second\"", MavenExecuteUtils.splitParams("--vm1 --vm2 ${packageClassName} first \"second\""));
        
        assertEquals("", MavenExecuteUtils.splitParams("${exec.mainClass}"));
        assertEquals("first \"second\"", MavenExecuteUtils.splitParams("${exec.mainClass} first \"second\""));

        assertEquals("", MavenExecuteUtils.splitParams("com.foo.Bar"));
        assertEquals("first \"second\"", MavenExecuteUtils.splitParams("com.foo.Bar first \"second\""));
    }

    /**
     * Test of propertySplitter method, of class MavenExecuteUtils.
     */
    @Test
    public void testPropertySplitterRetainQuotes() {
        List<String> parsed = new ArrayList<>();
        Iterable<String> it = 
            MavenExecuteUtils.propertySplitter(
                    "first \"and second\"   or \'third\' and ${pro.perty}"
                );
        it.forEach(parsed::add);
        
        assertEquals(
            Arrays.asList(
                "first",
                "\"and second\"",
                "",
                "",
                "or",
                "'third'",
                "and",
                "${pro.perty}"
            ), parsed
        );
    }

    @Test
    public void testPropertySplitterWithoutQuotes() {
        List<String> parsed = new ArrayList<>();
        Iterable<String> it = 
            MavenExecuteUtils.propertySplitter(
                    "first \"and second\"   or \'third\' and ${pro.perty}", false
                );
        it.forEach(parsed::add);
        
        assertEquals(
            Arrays.asList(
                "first",
                "and second",
                "",
                "",
                "or",
                "third",
                "and",
                "${pro.perty}"
            ), parsed
        );
    }
}
