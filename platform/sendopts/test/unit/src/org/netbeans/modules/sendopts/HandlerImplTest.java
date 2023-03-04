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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;

/**
 */
public class HandlerImplTest extends NbTestCase {
    static Object key;
    static Object[] args;
    ByteArrayInputStream is = new ByteArrayInputStream(new byte[0]);
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    static ResourceBundle bundle;
    
    public HandlerImplTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        OP.arr = new Option[] { Option.withoutArgument(Option.NO_SHORT_NAME, "haha") };
        MockServices.setServices(OP.class);
        bundle = ResourceBundle.getBundle("org.netbeans.modules.sendopts.TestBundle");
    }

    public void testErrorMessageIsPrinted() {
        key = "SIMPLEERROR";

        int ret = HandlerImpl.execute(new String[] { "--haha" }, is, os, err, new File("."));

        assertEquals("Execution returns 337", 337, ret);
        assertEquals("No out", 0, os.toByteArray().length);

        String msg = bundle.getString("SIMPLEERROR");
        assertEquals("error is as expected", msg, err.toString().replace("\n", "").replace("\r", ""));
    }
    public void testErrorMessageIsPrintedWithArgs() {
        key = "ARGS";
        args = new Object[] { "Y" };

        int ret = HandlerImpl.execute(new String[] { "--haha" }, is, os, err, new File("."));

        assertEquals("Execution returns 337", 337, ret);
        assertEquals("No out", 0, os.toByteArray().length);

        assertEquals("error is as expected", "XYZ", err.toString().replace("\n", "").replace("\r", ""));
    }
    public void testErrorMessageForInlinedThrowable() {
        key = new Exception() {
            @Override
            public String getLocalizedMessage() {
                return "LOC";
            }
        };

        int ret = HandlerImpl.execute(new String[] { "--haha" }, is, os, err, new File("."));

        assertEquals("Execution returns 221", 221, ret);
        assertEquals("No out", 0, os.toByteArray().length);

        assertEquals("error is as expected", "LOC", err.toString().replace("\n", "").replace("\r", ""));
    }

    public static final class OP extends OptionProcessor {
        static Option[] arr;
        static Map<Option, String[]> values;
        
        protected Set<Option> getOptions() {
            return new HashSet<Option>(Arrays.asList(arr));
        }

        protected void process(Env env, Map<Option, String[]> optionValues) throws CommandException {
            values = optionValues;
            assertNotNull("each test needs to assign a key", key);
            if (key instanceof Throwable) {
                CommandException ex = new CommandException(221);
                ex.initCause((Throwable)key);
                throw ex;
            }

            String locMsg = MessageFormat.format(bundle.getString((String) key), args);
            throw new CommandException(337, locMsg);
        }
        
    }
    
}
