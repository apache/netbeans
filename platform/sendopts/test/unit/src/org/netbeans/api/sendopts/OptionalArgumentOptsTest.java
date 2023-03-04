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

import java.util.Map;
import junit.framework.TestCase;
import org.netbeans.modules.sendopts.OptionImpl;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;

/** The basic test to check semantics of getopts behaviour wrt. one optional argument.
 *
 * @author Jaroslav Tulach
 */
public class OptionalArgumentOptsTest extends TestCase {
    private CommandLine l;
    private OneArgProc proc = new OneArgProc();
    private Option define;

    public OptionalArgumentOptsTest(String s) {
        super(s);
   }

    protected void tearDown() throws Exception {

        super.tearDown();
    }

    protected void setUp() throws Exception {
        Provider.clearAll();
        
        define = Option.optionalArgument('D', "define");
        Provider.add(proc, define);
        l = CommandLine.getDefault();
    }
    
    public void testSingleNoArgOptionIsRecognized() throws Exception {
        l.process(new String[] { "-Dahoj" });
        assertEquals("Processor found", define, proc.option);
        assertEquals("Value is correct", "ahoj", proc.value);
    }
    
    public void testLongOptionRecognized() throws Exception {
        l.process(new String[] { "--define=ahoj" });
        assertEquals("Processor found for long name", define, proc.option);
        assertEquals("Value is correct", "ahoj", proc.value);
    }
    public void testLongOptionRecognized2() throws Exception {
        try {
            l.process(new String[] { "--define", "ahoj" });
            fail("ahoj is not used as optional argument, according to getopts");
        } catch (CommandException ex) {
            // ok
        }
            
        assertNull("Processor found for long name", proc.option);
        assertNull("Value is unset", proc.value);
    }
    
    public void testAbrevatedNameRecognized() throws Exception {
        l.process(new String[] { "--def=ahoj" });
        assertEquals("Processor found for abbrevated name", define, proc.option);
    }
    
    public void testAbrevatedNameRecognized2() throws Exception {
        try {
            l.process(new String[] { "--def", "ahoj" });
            fail("Optional argument must follow the option with = sign, cannot be left out");
        } catch (CommandException ex) {
            // ok
        }
        assertNull("No Processor called", proc.option);
    }

    public void testAbrevatedNameRecognizedWithoutArg() throws Exception {
        l.process(new String[] { "--def" });
        assertEquals("Processor found for abbrevated name", define, proc.option);
        assertNull("Value is null", proc.value);
    }
    public void testShortNameRecognizedWithoutArg() throws Exception {
        l.process(new String[] { "-D" });
        assertEquals("Processor found for abbrevated name", define, proc.option);
        assertNotNull("Value is null", proc.value);
    }


    public void testIncorrectOptionIdentified() throws Exception {
        try {
            l.process(new String[] { "--hell" });
            fail("This option does not exists");
        } catch (CommandException ex) {
            // ok
        }
        assertNull("No processor called", proc.option);
    }

    public void testNoProcessorCalledWhenOneOptionIsNotKnown() throws Exception {
        try {
            l.process(new String[] { "-Dx", "--hell" });
            fail("One option does not exists");
        } catch (CommandException ex) {
            // ok
        }
        assertNull("No processor called", proc.option);
    }
    
    static final class OneArgProc implements Processor {
        Option option;
        String value;
        
        public void process(Env env, Map<Option, String[]> values) throws CommandException {
            assertNull("Not processed yet", option);
            assertEquals("One option", 1, values.size());
            option = values.keySet().iterator().next();
            String[] arr = values.values().iterator().next();
            assertNotNull("Never null", arr);
            if (arr.length == 0) {
                value = null;
            } else {
                assertEquals("Exactly one argument", 1, arr.length);
                value = arr[0];
            }
        }
    }
}
