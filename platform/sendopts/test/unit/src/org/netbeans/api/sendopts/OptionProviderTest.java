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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;
import org.netbeans.junit.MockServices;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;

/** The basic test to check semantics of getopts behaviour.
 *
 * @author Jaroslav Tulach
 */
public class OptionProviderTest extends TestCase {
    private CommandLine l;
    private static Option help;
    private static Option ok;
    
    static {
        help = Option.withoutArgument('h', "help");
        ok = Option.withoutArgument('o', "ok");

        OP.arr = new Option[] { ok, help };
        MockServices.setServices(OP.class);
    }
    
    public OptionProviderTest(String s) {
        super(s);
    }

    protected void setUp() throws Exception {
        
        OP.values = null;
        
        l = CommandLine.getDefault();
    }
    
    public void testSingleNoArgOptionIsRecognized() throws Exception {
        l.process(new String[] { "-h" });
        assertEquals("Processor found", true, OP.values.containsKey(help));
    }
    
    public void testLongOptionRecognized() throws Exception {
        l.process(new String[] { "--help" });
        assertEquals("Processor found for long name", true, OP.values.containsKey(help));
    }

    public void testTwoOptionsRecognized() throws Exception {
        l.process(new String[] { "-ho" });
        assertEquals("Processor for help", true, OP.values.containsKey(help));
        assertEquals("Processor for ok", true, OP.values.containsKey(ok));
    }
    
    public void testAbrevatedNameRecognized() throws Exception {
        l.process(new String[] { "--he" });
        assertEquals("Processor found for abbrevated name", true, OP.values.containsKey(help));
    }
    

    public void testIncorrectOptionIdentified() throws Exception {
        try {
            l.process(new String[] { "--hell" });
            fail("This option does not exists");
        } catch (CommandException ex) {
            // ok
        }
        assertNull("No processor called", OP.values);
    }

    public void testNoProcessorCalledWhenOneOptionIsNotKnown() throws Exception {
        try {
            l.process(new String[] { "-h", "--hell" });
            fail("One option does not exists");
        } catch (CommandException ex) {
            // ok
        }
        assertNull("No processor called", OP.values);
    }
    
    public static final class OP extends OptionProcessor {
        static Option[] arr;
        static Map<Option, String[]> values;
        
        protected Set<Option> getOptions() {
            return new HashSet<Option>(Arrays.asList(arr));
        }

        protected void process(Env env, Map<Option, String[]> optionValues) throws CommandException {
            values = optionValues;
        }
        
    }
}
