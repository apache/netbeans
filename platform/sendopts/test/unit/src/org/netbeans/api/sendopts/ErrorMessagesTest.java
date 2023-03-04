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

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import junit.framework.TestCase;
import org.netbeans.spi.sendopts.OptionGroups;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;

/** Checks that error reports from the system are sane.
 *
 * @author Jaroslav Tulach
 */
public class ErrorMessagesTest extends TestCase 
implements Processor {
    private CommandLine l;

    private ArrayList<Option> options = new ArrayList<Option>();
    
    public ErrorMessagesTest(String s) {
        super(s);
    }

    protected void tearDown() throws Exception {

        super.tearDown();
    }

    protected void setUp() throws Exception {
        Locale.setDefault(Locale.US);
        
        
        Option no = Option.withoutArgument((char)-1, "no");
        Option open = Option.additionalArguments('o', "open");
        Option close = Option.additionalArguments('c', "close");
        Option one = Option.requiredArgument('1', "one");
        Option two = Option.requiredArgument('2', "two");
        Option optional = Option.optionalArgument((char)-1, "option");
        Option both = OptionGroups.allOf(no, one);
        Option bothDef = OptionGroups.someOf(both, open);
        Option allOf = OptionGroups.oneOf(open, two, both);
        
        Provider.clearAll();
        Provider.add(this, no, open, close, one, two, optional, both, bothDef, allOf);
        
        l = CommandLine.getDefault();
    }
    
    public void testMissingArgument() {
        try {
            l.process(new String[] { "--one" });
            fail("This is going to fail");
        } catch (CommandException ex) {
            if (ex.getLocalizedMessage().indexOf("Option --one") == -1) {
                fail(ex.getLocalizedMessage());
            }
            
            if (ex.getLocalizedMessage().indexOf("needs") == -1) {
                fail(ex.getLocalizedMessage());
            }

            if (ex.getLocalizedMessage().indexOf("argument") == -1) {
                fail(ex.getLocalizedMessage());
            }
        }
    }
    
    public void testShortMissingArgument() {
        try {
            l.process(new String[] { "-1" });
            fail("This is going to fail");
        } catch (CommandException ex) {
            if (ex.getLocalizedMessage().indexOf("Option -1") == -1) {
                fail(ex.getLocalizedMessage());
            }
            
            if (ex.getLocalizedMessage().indexOf("needs") == -1) {
                fail(ex.getLocalizedMessage());
            }

            if (ex.getLocalizedMessage().indexOf("argument") == -1) {
                fail(ex.getLocalizedMessage());
            }
        }
    }
    
    public void testCannotBeUsedAtOnce() {
        try {
            l.process(new String[] { "-c", "-o" });
            fail("Cannot be used at once");
        } catch (CommandException ex) {
            if (ex.getLocalizedMessage().indexOf("-c") == -1) {
                fail("-c should be there: " + ex.getLocalizedMessage());
            }
            if (ex.getLocalizedMessage().indexOf("-o") == -1) {
                fail("-o should be there: " + ex.getLocalizedMessage());
            }
        }
    }
    
    public void testCannotBeUsedAtOnce2() {
        try {
            l.process(new String[] { "--close", "-o" });
            fail("Cannot be used at once");
        } catch (CommandException ex) {
            if (ex.getLocalizedMessage().indexOf("--close") == -1) {
                fail("-c should be there: " + ex.getLocalizedMessage());
            }
            if (ex.getLocalizedMessage().indexOf("-o") == -1) {
                fail("-o should be there: " + ex.getLocalizedMessage());
            }
        }
    }
    public void testCannotBeUsedAtOnce3() {
        try {
            l.process(new String[] { "--close", "--open" });
            fail("Cannot be used at once");
        } catch (CommandException ex) {
            if (ex.getLocalizedMessage().indexOf("--close") == -1) {
                fail("-c should be there: " + ex.getLocalizedMessage());
            }
            if (ex.getLocalizedMessage().indexOf("--open") == -1) {
                fail("-o should be there: " + ex.getLocalizedMessage());
            }
        }
    }
    public void testCannotBeUsedAtOnce4() {
        try {
            l.process(new String[] { "-c", "--open" });
            fail("Cannot be used at once");
        } catch (CommandException ex) {
            if (ex.getLocalizedMessage().indexOf("-c") == -1) {
                fail("-c should be there: " + ex.getLocalizedMessage());
            }
            if (ex.getLocalizedMessage().indexOf("--open") == -1) {
                fail("-o should be there: " + ex.getLocalizedMessage());
            }
        }
    }
    public void testNoOneCannotBeWithTwo() {
        try {
            l.process(new String[] { "--no", "--one", "anArg", "--two", "anotherArg" });
            fail("Cannot be used at once: " + options);
        } catch (CommandException ex) {
            if (ex.getLocalizedMessage().indexOf("--two") == -1) {
                fail("--two should be there: " + ex.getLocalizedMessage());
            }
            if (ex.getLocalizedMessage().indexOf("--no") == -1) {
                fail("--no should be there: " + ex.getLocalizedMessage());
            }
        }
    }
    public void testNoOneCannotBeWithTwo2() {
        try {
            l.process(new String[] { "--no", "--one", "anArg", "-2anotherArg" });
            fail("Cannot be used at once: " + options);
        } catch (CommandException ex) {
            if (ex.getLocalizedMessage().indexOf("-2") == -1) {
                fail("--two should be there: " + ex.getLocalizedMessage());
            }
            if (ex.getLocalizedMessage().indexOf("--no") == -1) {
                fail("--no should be there: " + ex.getLocalizedMessage());
            }
        }
    }

    public void process(Env env, Map<Option, String[]> values) throws CommandException {
        options.addAll(values.keySet());
    }
}
