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

package org.netbeans.modules.sendopts;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.ArgsProcessor;
import org.netbeans.spi.sendopts.Arg;
import org.netbeans.api.sendopts.CommandLine;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class InstancesTest extends NbTestCase {
    private CommandLine cmd;
    private SampleOptions1 sample1;
    private String[] arr;
    private Options1 options1;
    
    public InstancesTest(String n) {
        super(n);
    }

    @Override
    protected void setUp() throws Exception {
        sample1 = new SampleOptions1();
        arr = new String[1];
        options1 = new Options1(arr);
        cmd = CommandLine.create(sample1, options1, Options2.class, SampleOptions2.class);
        methodCalled = null;
        methodEnv = null;
    }

    public void testParsingWithInstances() throws Exception {
        cmd.process("--with", "value", "--without", "--runMe", "--tellmehow");

        assertTrue(sample1.runCalled);
        assertEquals("value", arr[0]);
        assertNotNull(methodCalled);
        assertTrue(methodCalled.tellMeHow);
        assertTrue(Options2.called);
    }

    private static final class Options1 extends OptionProcessor {
        private static final Option WITH = Option.requiredArgument(Option.NO_SHORT_NAME, "with");

        private final String[] holder;

        Options1(String[] holder) {
            this.holder = holder;
        }

        @Override
        protected Set<Option> getOptions() {
            return Collections.singleton(WITH);
        }

        @Override
        protected void process(Env env, Map<Option, String[]> optionValues) throws CommandException {
            holder[0] = optionValues.get(WITH)[0];
        }
    }

    public static final class Options2 extends OptionProcessor {
        private static final Option WITHOUT = Option.withoutArgument(Option.NO_SHORT_NAME, "without");

        static boolean called;

        @Override
        protected Set<Option> getOptions() {
            return Collections.singleton(WITHOUT);
        }

        @Override
        protected void process(Env env, Map<Option, String[]> optionValues) throws CommandException {
            called = optionValues.containsKey(WITHOUT);
        }
    }


    public static final class SampleOptions1 implements Runnable {
        @Arg(longName = "runMe")
        public boolean runMe;

        public SampleOptions1() {
        }

        boolean runCalled;

        @Override
        public void run() {
            assertTrue(runMe);
            runCalled = true;
        }
    }
    public static final class SampleOptions2 implements ArgsProcessor {
        @Arg(longName = "tellmehow")
        public boolean tellMeHow;
        
        @Override
        public void process(Env env) {
            assertTrue(tellMeHow);
            methodEnv = env;
            methodCalled = this;
        }
    }

    private static Env methodEnv;
    private static SampleOptions2 methodCalled;
}


