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

package org.netbeans.modules.subversion.client.cli.commands;

import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.subversion.client.cli.commands.VersionCommand.Version;

public class VersionCommandTest {

    @Test
    public void testVersionParse() {
        assertNotNull(Version.parse("1.2"));
        assertNotNull(Version.parse("1.10.0 (r1827917)"));
    }
    
    @Test
    public void testSplitting() {
        assertFalse(Version.parse("1.2.3").lowerThan(Version.parse("1.2.3")));
        assertFalse(Version.parse("1.2.3").greaterThan(Version.parse("1.2.3")));
        
        assertFalse(Version.parse("0-dev").lowerThan(Version.parse("0")));
        assertFalse(Version.parse("0-dev").greaterThan(Version.parse("0")));
        
        assertFalse(Version.parse("1.2.3-dev").lowerThan(Version.parse("1.2.3")));
        assertFalse(Version.parse("1.2.3-dev").greaterThan(Version.parse("1.2.3")));
        
        assertFalse(Version.parse("1.2.3-SNAPSHOT").lowerThan(Version.parse("1.2.3")));
        assertFalse(Version.parse("1.2.3-SNAPSHOT").greaterThan(Version.parse("1.2.3")));
        
        assertFalse(Version.parse("1.2-SNAPSHOT").lowerThan(Version.parse("1.2")));
        assertFalse(Version.parse("1.2-SNAPSHOT").greaterThan(Version.parse("1.2")));
        
        assertFalse(Version.parse("1-SNAPSHOT").lowerThan(Version.parse("1")));
        assertFalse(Version.parse("1-SNAPSHOT").greaterThan(Version.parse("1")));
        
        assertFalse(Version.parse("1.9.7-SlikSvn (SlikSvn/1.9.7)").lowerThan(Version.parse("1.9.7")));
        assertFalse(Version.parse("1.9.7-SlikSvn (SlikSvn/1.9.7)").greaterThan(Version.parse("1.9.7")));

        assertFalse(Version.parse("1.10.0 (r1827917)").lowerThan(Version.parse("1.10.0")));
        assertFalse(Version.parse("1.10.0 (r1827917)").greaterThan(Version.parse("1.10.0")));

        assertFalse(Version.parse("1.9.7 (r1800392)").lowerThan(Version.parse("1.9.7")));
        assertFalse(Version.parse("1.9.7 (r1800392)").greaterThan(Version.parse("1.9.7")));
    }

    @Test
    public void testComparison() {
        assertTrue(Version.parse("1.2").lowerThan(Version.parse("1.3")));
        assertTrue(Version.parse("1.2").lowerThan(Version.parse("1.3")));
        assertTrue(Version.parse("1.2").lowerThan(Version.parse("1.2.1")));
        assertTrue(Version.parse("1.2").lowerThan(Version.parse("2")));
        assertTrue(Version.parse("2").greaterThan(Version.parse("1.2")));
        assertTrue(Version.parse("2").greaterThan(Version.parse("1.2.1")));
        assertTrue(Version.parse("1.5").greaterThan(Version.parse("1.2.1")));
        assertTrue(Version.parse("1.10.0 (r1827917)").greaterThan(Version.parse("1.5")));

        assertTrue(Version.parse("1.10.0 (r1827917)").sameMinor(Version.parse("1.10")));
        assertFalse(Version.parse("1.10.0 (r1827917)").sameMinor(Version.parse("1")));
        assertFalse(Version.parse("1.10.0 (r1827917)").sameMinor(Version.parse("1")));
    }

    @Test
    public void testRemainder() {
        assertEquals("(r1827917)", Version.parse("1.10.0 (r1827917)").remainder);
        assertEquals("SNAPSHOT", Version.parse("1.1.1-SNAPSHOT").remainder);
    }

    @Test
    public void testArguments() {
        try {
            Version.parse(null);
            fail("Null argument should not be accepted");
        } catch (IllegalArgumentException ex) {
        }
        try {
            Version.parse(null);
            fail("Null argument should not be accepted");
        } catch (IllegalArgumentException ex) {
        }
        try {
            Version.parse("a.b.c");
            fail("Number parsing exception is expected");
        } catch (NumberFormatException ex) {
        }
        try {
            Version.parse("not a version");
            fail("Number parsing exception is expected");
        } catch (NumberFormatException ex) {
        }
    }

    @Test
    public void testNETBEANS_771() {

        String line = "svn, version 1.10.0 (r1827917)";
        Version v110 = Version.parse(line.substring(line.indexOf(" version ") + 9)); // this similar to what happens in VersionCommand.java checkForErrors()

        assertTrue(VersionCommand.VERSION_15.lowerThan(v110));
        assertFalse(v110.lowerThan(VersionCommand.VERSION_15));
        assertTrue(VersionCommand.VERSION_15.lowerThan(v110));
        assertTrue(VersionCommand.VERSION_16.lowerThan(v110));
        assertFalse(VersionCommand.VERSION_15.sameMinor(v110));
        assertFalse(VersionCommand.VERSION_16.sameMinor(v110));
    }
}
