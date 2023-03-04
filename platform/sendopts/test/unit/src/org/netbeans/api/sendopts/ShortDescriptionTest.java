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

/** Can we associated short description?
 *
 * @author Jaroslav Tulach
 */
public class ShortDescriptionTest extends TestCase implements Processor {
    private Option help;
    private Option descr;
    
    public ShortDescriptionTest(String s) {
        super(s);
    }

    private void setUpHelp() throws Exception {
        Provider.clearAll();
        help = Option.withoutArgument('h', "help");
        Provider.add(this, help);
    }

    private void setUpShort() {
        Provider.clearAll();
        help = Option.withoutArgument('h', "help");
        descr = Option.shortDescription(help, "org.netbeans.api.sendopts.TestBundle", "HELP");
        assertEquals("Option with description is the same", help, descr);
        assertEquals("Option with description has the same hashCode", help.hashCode(), descr.hashCode());
        Provider.add(this, descr);
    }
    
    public void testPrintedUsage() throws Exception {
        setUpHelp();
        
        StringWriter w = new StringWriter();
        PrintWriter pw = new PrintWriter(w);

        CommandLine.getDefault().usage(pw);

        Matcher m = Pattern.compile("-h.*--help").matcher(w.toString());
        if (!m.find()) {
            fail("-h, --help should be there:\n" + w.toString());
        }

        assertEquals("No help associated", w.toString().indexOf("shorthelp"), -1);
    }
    public void testPrintedUsageEiyhFrdvtipyion() throws Exception {
        setUpShort();
        
        StringWriter w = new StringWriter();
        PrintWriter pw = new PrintWriter(w);

        CommandLine.getDefault().usage(pw);

        Matcher m = Pattern.compile("-h.*--help").matcher(w.toString());
        if (!m.find()) {
            fail("-h, --help should be there:\n" + w.toString());
        }

        if (w.toString().indexOf("shorthelp") == -1) {
            fail("shorthelp associated: " + w.toString());
        }
    }
    public void testProvidedOwnDisplayName() throws Exception {
        Provider.clearAll();
        help = Option.withoutArgument('h', "help");
        Option shor = Option.shortDescription(help, "org.netbeans.api.sendopts.TestBundle", "HELP");
        assertEquals("Option with description is the same", help, shor);
        assertEquals("Option with description has the same hashCode", help.hashCode(), shor.hashCode());
        descr = Option.displayName(shor, "org.netbeans.api.sendopts.TestBundle", "NAMEHELP");
        assertEquals("Option with description is the same", help, descr);
        assertEquals("Option with description has the same hashCode", help.hashCode(), descr.hashCode());
        Provider.add(this, descr);
        
        StringWriter w = new StringWriter();
        PrintWriter pw = new PrintWriter(w);

        CommandLine.getDefault().usage(pw);

        Matcher m = Pattern.compile("-p.*--pomoc").matcher(w.toString());
        if (!m.find()) {
            fail("--pomoc should be there:\n" + w.toString());
        }

        if (w.toString().indexOf("shorthelp") == -1) {
            fail("shorthelp associated: " + w.toString());
        }
    }

    public void process(Env env, Map<Option, String[]> values) throws CommandException {
    }

}

