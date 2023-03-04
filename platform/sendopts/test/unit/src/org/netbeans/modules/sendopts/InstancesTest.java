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


