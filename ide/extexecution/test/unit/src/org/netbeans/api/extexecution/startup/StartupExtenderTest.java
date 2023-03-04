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
package org.netbeans.api.extexecution.startup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.extexecution.startup.ProxyStartupExtender;
import org.netbeans.modules.extexecution.startup.StartupExtenderRegistrationProcessor;
import org.netbeans.spi.extexecution.startup.StartupExtenderImplementation;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Petr Hejl
 */
public class StartupExtenderTest extends NbTestCase {

    public StartupExtenderTest(String name) {
        super(name);
    }

    @Override
    protected void tearDown() throws Exception {
        LegacyStartupExtender.enable = false;
        LegacyStartupExtender2.enable = false;
        V2StartupExtender.enable = false;
    }

    public void testLaziness() {
        Lookup lookup = Lookups.forPath(StartupExtenderRegistrationProcessor.PATH);
        assertTrue(lookup.lookup(StartupExtenderImplementation.class) instanceof ProxyStartupExtender);
    }

    public void testArguments() {
        Lookup context = Lookup.EMPTY;

        List<StartupExtender> argsDebug =
                StartupExtender.getExtenders(context, StartupExtender.StartMode.DEBUG);
        assertEquals(4, argsDebug.size());
        assertTrue(argsDebug.get(0).getArguments().isEmpty());

        List<StartupExtender> argsNormal =
                StartupExtender.getExtenders(context, StartupExtender.StartMode.NORMAL);
        assertEquals(4, argsNormal.size());

        StartupExtender args = argsNormal.get(0);
        assertEquals("Test", args.getDescription());
        assertEquals(2, args.getArguments().size());

        assertEquals("arg1", args.getArguments().get(0));
        assertEquals("arg2", args.getArguments().get(1));
    }
    
    /**
     * Checks that legacy extender's output is properly unquoted.
     */
    public void testRawArguments() {
        LegacyStartupExtender.enable = true;
        LegacyStartupExtender2.enable = true;
        V2StartupExtender.enable = true;
        
        List<StartupExtender> argsProfile = StartupExtender.getExtenders(Lookup.EMPTY, StartupExtender.StartMode.PROFILE);
        assertEquals(4, argsProfile.size());
        List<String> args = new ArrayList<>();
        List<String> rawArgs = new ArrayList<>();

        for (StartupExtender e : argsProfile) {
            rawArgs.addAll(e.getRawArguments());
            args.addAll(e.getArguments());
        }
        
        // raw arguments do not contain quoting even for extenders that supply quoted args
        assertEquals(Arrays.asList(
                "Quoted parameter", "Normal-parameter", 
                "Quoted parameter l", "Normal-parameter-l",
                "Quoted parameter V2", "Normal-parameter-v2"
            ), rawArgs);
    }
    
    /**
     * Checks that the new extender's output is quoted for backwards compatibility
     */
    public void testQuotedArguments() {
        LegacyStartupExtender.enable = true;
        LegacyStartupExtender2.enable = true;
        V2StartupExtender.enable = true;
        
        List<StartupExtender> argsProfile = StartupExtender.getExtenders(Lookup.EMPTY, StartupExtender.StartMode.PROFILE);
        assertEquals(4, argsProfile.size());
        List<String> args = new ArrayList<>();
        List<String> rawArgs = new ArrayList<>();

        for (StartupExtender e : argsProfile) {
            rawArgs.addAll(e.getRawArguments());
            args.addAll(e.getArguments());
        }
        
        assertEquals(Arrays.asList(
                "\"Quoted parameter\"", "Normal-parameter", 
                "\"Quoted parameter l\"", "Normal-parameter-l", 
                "\"Quoted parameter V2\"", "Normal-parameter-v2"
            ), args);

    }
}
