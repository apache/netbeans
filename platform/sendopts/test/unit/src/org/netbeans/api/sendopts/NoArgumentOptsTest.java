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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.TestCase;
import org.netbeans.modules.sendopts.OptionImpl;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;

/** The basic test to check semantics of getopts behaviour.
 *
 * @author Jaroslav Tulach
 */
public class NoArgumentOptsTest extends TestCase {
    private CommandLine l;
    private NoArgProc proc = new NoArgProc();
    private Option help;
    private NoArgProc okProc = new NoArgProc();
    private Option ok;
    
    public NoArgumentOptsTest(String s) {
        super(s);
    }

    protected void tearDown() throws Exception {

        super.tearDown();
    }

    protected void setUp() throws Exception {
        
        help = Option.withoutArgument('h', "help");
        Provider.clearAll();
        Provider.add(proc, help);
        ok = Option.withoutArgument('o', "ok");
        Provider.add(okProc, ok);
        
        l = CommandLine.getDefault();
    }
    
    public void testSingleNoArgOptionIsRecognized() throws Exception {
        l.process(new String[] { "-h" });
        assertEquals("Processor found", help, proc.option);
    }
    
    public void testLongOptionRecognized() throws Exception {
        l.process(new String[] { "--help" });
        assertEquals("Processor found for long name", help, proc.option);
    }

    public void testTwoOptionsRecognized() throws Exception {
        l.process(new String[] { "-ho" });
        assertEquals("Processor for help", help, proc.option);
        assertEquals("Processor for ok", ok, okProc.option);
    }
    
    public void testAbrevatedNameRecognized() throws Exception {
        l.process(new String[] { "--he" });
        assertEquals("Processor found for abbrevated name", help, proc.option);
        
        proc.option = null;
        l.process(new String[] { "--he" });
        assertEquals("Processor found for abbrevated name again", help, proc.option);
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
            l.process(new String[] { "-h", "--hell" });
            fail("One option does not exists");
        } catch (CommandException ex) {
            // ok
        }
        assertNull("No processor called", proc.option);
    }

    public void testPrintedUsage() throws Exception {
        StringWriter w = new StringWriter();
        PrintWriter pw = new PrintWriter(w);

        l.usage(pw);

        Matcher m = Pattern.compile("-h.*--help").matcher(w.toString());
        if (!m.find()) {
            fail("-h, --help should be there:\n" + w.toString());
        }
        m = Pattern.compile("-o.*--ok").matcher(w.toString());
        if (!m.find()) {
            fail("-o, --ok should be there:\n" + w.toString());
        }

        int x = w.toString().indexOf('\n');
        if (x == -1) {
            fail("There should be two lines: " + w.toString());
        }
        x = w.toString().indexOf('\n', x + 1);
        if (x == -1) {
            fail("There should be two lines2: " + w.toString());
        }
    }
    public void testPrintedUsageEmpty() throws Exception {
        Provider.clearAll();
        
        help = Option.withoutArgument('h', null);
        Provider.add(proc, help);
        ok = Option.withoutArgument(Option.NO_SHORT_NAME, "ok");
        Provider.add(okProc, ok);
        
        l = CommandLine.getDefault();

        StringWriter w = new StringWriter();
        PrintWriter pw = new PrintWriter(w);

        l.usage(pw);

        Matcher m = Pattern.compile("   *-h    *").matcher(w.toString());
        if (!m.find()) {
            fail("Only -h should be there:\n" + w.toString());
        }
        m = Pattern.compile("  *--ok    *").matcher(w.toString());
        if (!m.find()) {
            fail("  --ok should be there:\n" + w.toString());
        }

        int x = w.toString().indexOf('\n');
        if (x == -1) {
            fail("There should be two lines: " + w.toString());
        }
        x = w.toString().indexOf('\n', x + 1);
        if (x == -1) {
            fail("There should be two lines2: " + w.toString());
        }
    }
    
    static final class NoArgProc implements Processor {
        Option option;
        
        public void process(Env env, Map<Option, String[]> values) throws CommandException {
            assertNull("Not processed yet", option);
            assertEquals(1, values.size());
            option = values.keySet().iterator().next();
            assertNotNull("An option is provided", option);

            String[] args = values.get(option);
            assertNotNull("Values is always [0]", args);
            assertEquals("Values is always [0]", 0, args.length);
        }
    }
}
