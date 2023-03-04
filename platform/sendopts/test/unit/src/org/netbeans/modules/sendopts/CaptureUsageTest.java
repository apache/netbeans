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

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import org.junit.Test;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.api.sendopts.CommandLine;
import org.netbeans.spi.sendopts.Arg;
import org.netbeans.spi.sendopts.ArgsProcessor;
import org.netbeans.spi.sendopts.Description;
import org.netbeans.spi.sendopts.Env;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotEquals;

public class CaptureUsageTest implements ArgsProcessor {
    private static String usage;

    @Description(shortDescription = "Use me!")
    @Arg(shortName = 'u', longName = "")
    public boolean use;

    @Test
    public void captureUsage() throws CommandException {
        CommandLine cmd = CommandLine.create(CaptureUsageTest.class);
        usage = null;
        cmd.process("-u");
        assertNotNull("Usage set", usage);
        assertNotEquals("Expecting 'Use me!'", -1, usage.indexOf("Use me!"));
    }

    @Override
    public void process(Env env) throws CommandException {
        assertTrue(use);
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        env.usage(os);
        try {
            usage = os.toString("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new CommandException(1, ex.getMessage());
        }
    }
}
