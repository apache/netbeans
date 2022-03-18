/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.lsp.pkgconfig;

import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;

public class PkgConfigExpanderTest extends NbTestCase {

    public PkgConfigExpanderTest(String name) {
        super(name);
    }

    @Test
    public void testShouldSplitPkgConfigCorrectly() throws IOException {
        PkgConfigExpander expander = new PkgConfigExpander(PkgConfigExpander.ExpansionStrategy.TESTING);
        String expected = "`a` b `c` d `e`";
        String resulted = expander.expandPkgConfig(expected);
        Assert.assertEquals(expected, resulted);
    }

    // May fail if pkg-config is not installed or gtk+-3.0 is not installed
    @RandomlyFails
    @Test
    public void testShouldExpandPkgConfigIfAvailable() throws IOException {
        PkgConfigExpander expander = new PkgConfigExpander(PkgConfigExpander.ExpansionStrategy.PRODUCTION);
        String expected = "`pkg-config --cflags gtk+-3.0` `pkg-config --cflags gtk+-3.0`";
        String resulted = expander.expandPkgConfig(expected);
        // System.out.format("EXPANSION: [%s]%n", resulted);
        Assert.assertNotEquals(0, resulted.length());
    }

    @Test
    public void testShouldSplitCommandProperly() throws Exception {
        String test = "This is \"a quoted string\" and this a 'single quoted one'";
        String [] parts = PkgConfigExpander.splitCommandIntoArguments(test);
        // System.err.format("SPLITTED: %s%n", String.join("|", parts));
        assertEquals(7, parts.length);
    }
    
}
