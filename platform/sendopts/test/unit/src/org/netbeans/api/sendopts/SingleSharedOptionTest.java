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
import junit.framework.TestCase;
import org.netbeans.junit.MockServices;
import org.netbeans.spi.sendopts.OptionGroups;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;

/** Can one option be shared inside one processor?
 *
 * @author Jaroslav Tulach
 */
public class SingleSharedOptionTest extends TestCase {
    /** a shared option part of some API */
    static final Option SHARED = Option.requiredArgument(Option.NO_SHORT_NAME, "shared");
    
    public SingleSharedOptionTest(String s) {
        super(s);
    }

    protected void setUp() throws Exception {
        MockServices.setServices(Proc.class);
        Proc.called = null;
    }
    
    public void testP1IsSelected() throws Exception {
        CommandLine.getDefault().process(new String[] { "--shared", "Ahoj", "--p1" });
        
        assertNotNull("Processor called", Proc.called);
        assertTrue(Proc.called.containsKey(Proc.P1));
        assertFalse(Proc.called.containsKey(Proc.P2));
        assertTrue(Proc.called.containsKey(SHARED));
    }

    public void testP2IsSelected() throws Exception {
        CommandLine.getDefault().process(new String[] { "--shared", "Ahoj", "--p2" });

    
        assertNotNull("Processor called", Proc.called);
        assertFalse(Proc.called.containsKey(Proc.P1));
        assertTrue(Proc.called.containsKey(Proc.P2));
        assertTrue(Proc.called.containsKey(SHARED));
    }
    
    public void testBothSelected() throws Exception {
        CommandLine.getDefault().process(new String[] { "--shared", "Ahoj", "--p2", "--p1" });
        
    
        assertNotNull("Processor called", Proc.called);
        assertTrue(Proc.called.containsKey(Proc.P1));
        assertTrue(Proc.called.containsKey(Proc.P2));
        assertTrue(Proc.called.containsKey(SHARED));
    }

    public void testNothingCalled() throws Exception {
        try {
            CommandLine.getDefault().process(new String[] { "--shared", "Ahoj" });
            fail("Just shared is not valid option");
        } catch (CommandException ex) {
            // ok
        }
        
        assertNull("Processor not called", Proc.called);
    }
    
    static final Option createMasterSlaveOption(Option master, Option slave) {
        return OptionGroups.allOf(master, OptionGroups.anyOf(slave));
    }
    
    public static final class Proc extends OptionProcessor {
        static final Option P1 = Option.withoutArgument(Option.NO_SHORT_NAME, "p1");
        static final Option P2 = Option.withoutArgument(Option.NO_SHORT_NAME, "p2");
        static Map<Option,String[]> called;

        protected Set<Option> getOptions() {
            Set<Option> set = new HashSet<Option>();
            set.add(createMasterSlaveOption(P1, SHARED));
            set.add(createMasterSlaveOption(P2, SHARED));
            return set;
        }
        
        protected void process(Env env, Map<Option, String[]> optionValues) throws CommandException {
            assertNull("Not called yet", called);
            called = optionValues;
        }
    }
}

