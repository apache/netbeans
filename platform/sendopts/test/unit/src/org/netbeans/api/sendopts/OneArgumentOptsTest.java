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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.TestCase;
import org.netbeans.modules.sendopts.OptionImpl;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;

/** The basic test to check semantics of getopts behaviour wrt. one argument.
 *
 * @author Jaroslav Tulach
 */
public class OneArgumentOptsTest extends TestCase {
    private CommandLine l;
    private OneArgProc proc = new OneArgProc();
    private Option define;
    
    public OneArgumentOptsTest(String s) {
        super(s);
    }

    protected void tearDown() throws Exception {

        super.tearDown();
    }

    protected void setUp() throws Exception {
        Provider.clearAll();
        
        // so printed help is in english
        Locale.setDefault(Locale.US);

        define = Option.requiredArgument('D', "define");
        Provider.add(proc, define);
        l = CommandLine.getDefault();
    }
    
    public void testNoArrayIndexOutOfBoundsEx() throws Exception {
        try {
            l.process(new String[] { "--define" });
            fail("Should throw an exception");
        } catch (CommandException ex) {
            // ok
        }
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
        l.process(new String[] { "--define", "ahoj" });
        assertEquals("Processor found for long name", define, proc.option);
        assertEquals("Value is correct", "ahoj", proc.value);
    }
    
    public void testAbrevatedNameRecognized() throws Exception {
        l.process(new String[] { "--def=ahoj" });
        assertEquals("Processor found for abbrevated name", define, proc.option);
    }
    
    public void testAbrevatedNameRecognized2() throws Exception {
        l.process(new String[] { "--def", "ahoj" });
        assertEquals("Processor found for abbrevated name", define, proc.option);
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

    public void testMissingArgumentIdentified() throws Exception {
        try {
            l.process(new String[] { "-D" });
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
    
    public void testRequiredArgumentDoesNotSwallowNextOption() throws CommandException {
        CommandLine l;
        
        OneArgProc a = new OneArgProc();
        OneArgProc b = new OneArgProc();
        
        Option one = Option.requiredArgument('1', "one");
        Provider.add(a, one);

        Option two = Option.optionalArgument((char)-1, "two");
        Provider.add(b, two);
        
        l = CommandLine.getDefault();
        try {
            l.process(new String[] { "--one", "--two" });
            fail("Cannot succeed as --one needs an argument");
        } catch (CommandException ex) {
            // ok
        }
        
        assertNull("A not called", a.option);
        assertNull("B not called", b.option);

        assertNull("A not called", a.option);
        assertNull("B not called", b.option);
        
        l.process(new String[] { "--one", "--", "--two" });
        
        assertEquals("A called", one, a.option);
        assertEquals("B not called", null, b.option);
        assertEquals("A value", "--two", a.value);
        
        a.option = null;
        b.option = null;
        a.value = null;
        
        l.process(new String[] { "-1--two" });
        
        assertEquals("A called", one, a.option);
        assertEquals("B not called", null, b.option);
        assertEquals("A value", "--two", a.value);
        
    }
    public void testPrintedUsage() throws Exception {
        StringWriter w = new StringWriter();
        PrintWriter pw = new PrintWriter(w);

        l.usage(pw);

        Matcher m = Pattern.compile("--define <arg>").matcher(w.toString());
        if (!m.find()) {
            fail("--define <arg> should be there:\n" + w.toString());
        }

        int x = w.toString().indexOf('\n');
        if (x == -1) {
            fail("There should be one line: " + w.toString());
        }
        x = w.toString().indexOf('\n', x + 1);
        assertEquals("No next line", -1, x);
    }
    
    static final class OneArgProc implements Processor {
        Option option;
        String value;

        public void process(Env env, Map<Option, String[]> values) throws CommandException {
            assertEquals("One option", 1, values.size());
            assertNull("Not processed yet", option);
            Option o = values.keySet().iterator().next();
            String v = values.values().iterator().next()[0];
            assertNotNull("An option is provided", o);
            option = o;
            value = v;
        }
    }
}
