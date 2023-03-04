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

/** Default options that work with additonal arguments.
 *
 * @author Jaroslav Tulach
 */
public class DefaultArgumentOptsTest extends TestCase {
    private CommandLine l, wrong;
    private AddArgsProc proc = new AddArgsProc();
    private Option open;
    private Option close;
    private AddArgsProc closeProc = new AddArgsProc();

    public DefaultArgumentOptsTest(String s) {
        super(s);
    }

    protected void tearDown() throws Exception {

        super.tearDown();
    }

    protected void setUpOk() throws Exception {
        Provider.clearAll();
        
        
        open = Option.additionalArguments((char)-1, "open");
        Provider.add(proc, open);
        close = Option.defaultArguments();
        Provider.add(closeProc, close);
        
        l = CommandLine.getDefault();
    }
    
    protected void setUpWrong() throws Exception {
        setUpOk();
        Option def = Option.defaultArguments();
        Provider.add(closeProc, def);
        
        l = null;
        wrong = CommandLine.getDefault();
    }
    
    public void testOptionsPassedToOpen() throws Exception {
        setUpOk();
        
        l.process(new String[] { "1", "--open", "2", "3" });
        assertEquals("Processor found for long name", open, proc.option);
        assertEquals("Three files provided", 3, proc.values.length);
        assertEquals("first", "1", proc.values[0]);
        assertEquals("second", "2", proc.values[1]);
        assertEquals("third", "3", proc.values[2]);
    }
    public void testOptionsPassedToOpenWrong() throws Exception {
        setUpWrong();
        
        wrong.process(new String[] { "1", "--open", "2", "3" });
        assertEquals("Processor found for long name", open, proc.option);
        assertEquals("Three files provided", 3, proc.values.length);
        assertEquals("first", "1", proc.values[0]);
        assertEquals("second", "2", proc.values[1]);
        assertEquals("third", "3", proc.values[2]);
    }
    
    public void testProcessingStopsAtDashDash() throws Exception {
        setUpOk();
        
        l.process(new String[] { "1", "--", "--open", "2" });
        assertEquals("Close Processor found for abbrevated name", close, closeProc.option);
        assertNull("No open called", proc.option);
        assertEquals("three options provided", 3, closeProc.values.length);
        assertEquals("first", "1", closeProc.values[0]);
        assertEquals("second", "--open", closeProc.values[1]);
        assertEquals("third", "2", closeProc.values[2]);
    }
    public void testCannotHaveTwoDefaultOptions() throws Exception {
        setUpWrong();
        
        try {
            wrong.process(new String[] { "1", "--", "--open", "2" });
            fail("Cannot have two default options");
        } catch (CommandException ex) {
            // ok
        }
        assertNull("No close", closeProc.option);
        assertNull("No open", proc.option);
    }
    
    static final class AddArgsProc implements Processor {
        Option option;
        String[] values;


        public void process(Env env, Map<Option, String[]> values) throws CommandException {
            assertNull("Not processed yet", option);
            
            assertEquals("One value", 1, values.size());
            
            option = values.keySet().iterator().next();
            this.values = values.values().iterator().next();

            assertNotNull("An option is provided", option);
            assertNotNull("An array of args is provided", this.values);
        }
    }
}
