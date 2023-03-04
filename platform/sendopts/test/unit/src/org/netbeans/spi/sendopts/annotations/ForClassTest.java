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

import java.io.ByteArrayOutputStream;
import org.netbeans.spi.sendopts.ArgsProcessor;
import org.netbeans.spi.sendopts.Description;
import org.netbeans.spi.sendopts.Arg;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.api.sendopts.CommandLine;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.sendopts.Env;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ForClassTest extends NbTestCase {
    private CommandLine cmd;
    
    public ForClassTest(String n) {
        super(n);
    }

    @Override
    protected void setUp() throws Exception {
        cmd = CommandLine.create(SampleOptions2.class);
        methodCalled = null;
        methodEnv = null;
    }

    public void testParseEnabled() throws Exception {
        cmd.process("--on");
        assertNotNull("options processed", methodCalled);
        assertTrue("enabled set", methodCalled.enabled);
    }

    public void testParseEnabledWithParamFails() {
        try {
            cmd.process("--enabled", "Param");
            fail("Parse shall not succeed");
        } catch (CommandException ex) {
            // oK
        }
        assertNull("parse not finished, enabled not set", methodCalled);
    }

    public void testParseWithParam() throws CommandException {
        cmd.process("-qParam");
        assertNotNull("Method called", methodCalled);
        assertFalse("enabled not set", methodCalled.enabled);
        assertEquals("Param", methodCalled.withParam);
    }
    public void testParseWithoutParamFails() throws CommandException {
        try {
            cmd.process("-q");
            fail("Missing param for -q");
        } catch (CommandException ex) {
            // OK
            assertNull("No method called", methodCalled);
        }
    }
    public void testParseAdditionalParam() throws CommandException {
        cmd.process("no", "-m", "Param");
        assertNotNull("Called", methodCalled);
        assertFalse("enabled not set", methodCalled.enabled);
        assertNotNull("additionalParams set", methodCalled.additionalParams);
        assertEquals("two", 2, methodCalled.additionalParams.length);
        assertEquals("no", methodCalled.additionalParams[0]);
        assertEquals("Param", methodCalled.additionalParams[1]);
    }
    public void testParseLongAdditional() throws CommandException {
        cmd.process("no", "--more", "Param");
        assertNotNull("Called", methodCalled);
        assertFalse("enabled not set", methodCalled.enabled);
        assertNotNull("additionalParams set", methodCalled.additionalParams);
        assertEquals("two", 2, methodCalled.additionalParams.length);
        assertEquals("no", methodCalled.additionalParams[0]);
        assertEquals("Param", methodCalled.additionalParams[1]);
        assertNotNull("environment provided", methodEnv);
    }
    public void testHelp() throws CommandException {
        StringWriter w = new StringWriter();
        cmd.usage(new PrintWriter(w));
        assertTrue("contains additionalParams:\n" + w, w.toString().contains(("MyParams")));
        assertTrue("contains short help:\n" + w, w.toString().contains(("ShorterHelp")));
    }
    public void testHelpOnEnv() throws CommandException, UnsupportedEncodingException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        cmd.process(new String[] { "--tellmehow" }, System.in, os, System.err, null);
        String out = new String(os.toByteArray(), StandardCharsets.UTF_8);
        assertTrue("contains additionalParams:\n" + out, out.contains(("MyParams")));
        assertTrue("contains short help:\n" + out, out.contains(("ShorterHelp")));
    }
    public void testDefaultValueNotProvided() throws CommandException {
        cmd.process("--optional");
        assertNotNull("Options created", methodCalled);
        assertEquals("Set to empty string", "", methodCalled.defaultValue);
    }
    public void testDefaultValueProvided() throws CommandException {
        cmd.process("--optional=value");
        assertNotNull("Options created", methodCalled);
        assertEquals("Set to value string", "value", methodCalled.defaultValue);
    }

    public static final class SampleOptions2 implements ArgsProcessor {
        @Arg(longName="on")
        public boolean enabled;

        @Arg(longName="", shortName='q')
        public String withParam;

        @Description(displayName="#NAME2", shortDescription="#SHORT2")
        @Arg(shortName='m', longName="more")
        @NbBundle.Messages({
            "NAME2=MyParams",
            "SHORT2=ShorterHelp"
        })
        public String[] additionalParams;

        @Arg(longName = "optional", defaultValue = "")
        public String defaultValue;
        
        @Arg(longName = "tellmehow")
        public boolean tellMeHow;
        
        @Override
        public void process(Env env) {
            if (tellMeHow) {
                env.usage();
            }
            methodEnv = env;
            methodCalled = this;
        }
    }

    private static Env methodEnv;
    private static SampleOptions2 methodCalled;
}


