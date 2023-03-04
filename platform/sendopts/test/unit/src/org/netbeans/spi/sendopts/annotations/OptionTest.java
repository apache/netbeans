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

package org.netbeans.spi.sendopts.annotations;

import org.netbeans.spi.sendopts.ArgsProcessor;
import org.netbeans.spi.sendopts.Description;
import org.netbeans.spi.sendopts.Arg;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.api.sendopts.CommandLine;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.sendopts.Env;
import org.openide.util.NbBundle;
import org.openide.util.test.AnnotationProcessorTestUtils;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class OptionTest extends NbTestCase {
    private CommandLine cmd;
    
    static {
        System.setProperty("org.openide.util.Lookup.paths", "Services");
    }

    public OptionTest(String n) {
        super(n);
    }

    @Override
    protected void setUp() throws Exception {
        cmd = CommandLine.getDefault();
        methodCalled = null;
        methodEnv = null;
    }

    public void testParseEnabled() throws Exception {
        cmd.process("--enabled");
        assertNotNull("options processed", methodCalled);
        assertTrue("enabled set", methodCalled.enabled);
    }

    public void testParseEnabledWithParamFails() {
        try {
            cmd.process("-e", "Param");
            fail("Parse shall not succeed");
        } catch (CommandException ex) {
            // oK
        }
        assertNull("parse not finished, enabled not set", methodCalled);
    }

    public void testParseWithParam() throws CommandException {
        cmd.process("-pParam");
        assertNotNull("Method called", methodCalled);
        assertFalse("enabled not set", methodCalled.enabled);
        assertEquals("Param", methodCalled.withParam);
    }
    public void testParseWithoutParamFails() throws CommandException {
        try {
            cmd.process("-p");
            fail("Missing param for -p");
        } catch (CommandException ex) {
            // OK
            assertNull("No method called", methodCalled);
        }
    }
    public void testParseAdditionalParam() throws CommandException {
        cmd.process("no", "-a", "Param");
        assertNotNull("Called", methodCalled);
        assertFalse("enabled not set", methodCalled.enabled);
        assertNotNull("additionalParams set", methodCalled.additionalParams);
        assertEquals("two", 2, methodCalled.additionalParams.length);
        assertEquals("no", methodCalled.additionalParams[0]);
        assertEquals("Param", methodCalled.additionalParams[1]);
    }
    public void testParseLongAdditional() throws CommandException {
        cmd.process("no", "--additional", "Param");
        assertNotNull("Called", methodCalled);
        assertFalse("enabled not set", methodCalled.enabled);
        assertNotNull("additionalParams set", methodCalled.additionalParams);
        assertEquals("two", 2, methodCalled.additionalParams.length);
        assertEquals("no", methodCalled.additionalParams[0]);
        assertEquals("Param", methodCalled.additionalParams[1]);
        assertNotNull("environment provided", methodEnv);
    }
    public void testParseImplicit() throws CommandException {
        cmd.process("no", "Param");
        assertNotNull("Called", methodCalled);
        assertFalse("enabled not set", methodCalled.enabled);
        assertNotNull("additionalParams set", methodCalled.additionalParams);
        assertEquals("two", 2, methodCalled.additionalParams.length);
        assertEquals("no", methodCalled.additionalParams[0]);
        assertEquals("Param", methodCalled.additionalParams[1]);
        assertNotNull("environment provided", methodEnv);
    }
    public void testHelp() throws CommandException {
        CharSequence log = Log.enable("org.netbeans.modules.sendopts", Level.WARNING);
        StringWriter w = new StringWriter();
        cmd.usage(new PrintWriter(w));
        assertTrue("contains additionalParams:\n" + w, w.toString().contains(("AddOnParams")));
        assertTrue("contains short help:\n" + w, w.toString().contains(("ShortHelp")));
        assertTrue("contains description for p\n" + w, w.toString().contains("Short description for p"));
        assertEquals("No warnings:\n" + log, 0, log.length());
    }
        public void testDefaultValueNotProvided() throws CommandException {
            cmd.process("--default");
            assertNotNull("Options created", methodCalled);
            assertEquals("Set to empty string", "", methodCalled.defaultValue);
        }
        public void testDefaultValueProvided() throws CommandException {
            cmd.process("--default=value");
            assertNotNull("Options created", methodCalled);
            assertEquals("Set to value string", "value", methodCalled.defaultValue);
        }

    public static final class SampleOptions implements ArgsProcessor {
        @Arg(longName="enabled")
        public boolean enabled;

        @Description(shortDescription="Short description for p")
        @Arg(longName="", shortName='p')
        public String withParam;

        @Description(displayName="#NAME", shortDescription="#SHORT")
        @Arg(shortName='a', longName="additional", implicit=true)
        @NbBundle.Messages({
            "NAME=AddOnParams",
            "SHORT=ShortHelp"
        })
        public String[] additionalParams;
        
        @Arg(longName="default", defaultValue="")
        public String defaultValue;
        
        @Override
        public void process(Env env) {
            methodEnv = env;
            methodCalled = this;
        }
    }
    

    private static Env methodEnv;
    private static SampleOptions methodCalled;
    
    public void testCheckForStatic() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.netbeans.spi.sendopts.Arg;\n" +
            "public class A {\n" +
            "  @Arg(longName=\"\", shortName='a')" +
            "  public static String Static;" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
        if (!os.toString().contains("static")) {
            fail(os.toString());
        }
    }
    
    public void testNeedToSpecifyAName() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.netbeans.spi.sendopts.Arg;\n" +
            "public class A {\n" +
            "  @Arg(longName=\"\", implicit=true)" +
            "  public String[] fail;" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
        if (!os.toString().contains("longName")) {
            fail(os.toString());
        }
        if (!os.toString().contains("shortName")) {
            fail(os.toString());
        }
    }

    public void testImplicitNeedsToBeOnStringArray() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.netbeans.spi.sendopts.Arg;\n" +
            "public class A {\n" +
            "  @Arg(longName=\"ahoj\", shortName='a',implicit=true)" +
            "  public String Static;" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
        if (!os.toString().contains("String[]")) {
            fail(os.toString());
        }
    }
    
}


