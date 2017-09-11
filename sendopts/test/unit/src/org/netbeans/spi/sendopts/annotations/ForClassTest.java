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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.spi.sendopts.annotations;

import java.io.ByteArrayOutputStream;
import org.netbeans.spi.sendopts.ArgsProcessor;
import org.netbeans.spi.sendopts.Description;
import org.netbeans.spi.sendopts.Arg;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
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
        String out = new String(os.toByteArray(), "UTF-8");
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


