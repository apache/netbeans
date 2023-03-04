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

package org.netbeans.api.sendopts;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.api.sendopts.CommandLine;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ForClassWithProcessorTest extends NbTestCase {
    private CommandLine cmd;

    private static SampleOptionProcessor created;
    
    public ForClassWithProcessorTest(String n) {
        super(n);
    }

    @Override
    protected void setUp() throws Exception {
        created = null;
        cmd = CommandLine.create(SampleOptionProcessor.class);
    }

    public void testParseEnabled() throws Exception {
        cmd.process("--on");
        assertNotNull("option processor processed", created);
        assertTrue("on processed", created.onSeen);
    }

    public static final class SampleOptionProcessor extends OptionProcessor {
        private static final Option on = Option.withoutArgument(Option.NO_SHORT_NAME, "on");
        private boolean onSeen;

        public SampleOptionProcessor() {
            created = this;
        }

        @Override
        protected Set<Option> getOptions() {
            Set<Option> options = new HashSet<Option>();
            options.add(on);
            return options;
        }

        @Override
        protected void process(Env env, Map<Option, String[]> optionValues) throws CommandException {
            if (optionValues.containsKey(on)) {
                onSeen = true;
            }
        }
    }
}


