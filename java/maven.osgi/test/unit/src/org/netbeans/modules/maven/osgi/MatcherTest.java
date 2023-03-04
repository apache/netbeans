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
package org.netbeans.modules.maven.osgi;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mkleint, folarte
 */
public class MatcherTest {

    public MatcherTest() {
    }

    /**
     * Test of matches method, of class Matcher, simple cases.
     */
    @Test
    public void testMatches() {
        Matcher m = new Matcher("org.foo,!com.bar,org.zaz.*,!org.baz.*");
        assertTrue(m.matches("org.foo"));
        assertFalse(m.matches("org.foo.foo"));
        assertFalse(m.matches("com.bar"));
        assertFalse(m.matches("com.bar.bar")); //not matched I suppose
        assertTrue(m.matches("org.zaz"));
        assertTrue(m.matches("org.zaz.zaz"));
        assertTrue(m.matches("org.zaz.zaz.baz"));
        assertFalse(m.matches("org.zazaz")); // New tests
        assertFalse(m.matches("org.zazaz.zaz")); // New tests
        assertFalse(m.matches("org.baz"));
        assertFalse(m.matches("org.baz.baz"));
    }

    /**
     * Test default value with no patterns.
     */
    @Test
    public void testDefaults() {
        Matcher m = new Matcher("*");
        assertTrue(m.matches("org.foo"));

        m = new Matcher("!*");
        assertFalse(m.matches("org.foo"));

        m = new Matcher("");
        assertFalse(m.matches("org.foo"));

    }


    // Test wether things are truncated on reaching terminal *
    @Test
    public void testListTruncation() {
        Matcher m = new Matcher("org,!*,bar");
        assertTrue(m.matches("org"));
        assertFalse(m.matches("bar"));
        m = new Matcher("!org,*,!bar");
        assertFalse(m.matches("org"));
        assertTrue(m.matches("bar"));
    }

    /**
     * Test partial exclusions of a wider wildcard.
     */

    @Test
    public void testExclusions() {
        Matcher m = new Matcher("!org.foo,org.*");
        assertTrue(m.matches("org.foo.bar"));
        assertFalse(m.matches("org.foo"));
        assertTrue(m.matches("org.foobar"));
        assertTrue(m.matches("org.bar"));
        assertTrue(m.matches("org"));
        assertFalse(m.matches("bar"));
        assertFalse(m.matches("organization"));
    }

    /**
     * Test wildcard exclusion preceding wildcard inclusion.
     */

    @Test
    public void testWildardExclusions() {
        Matcher m = new Matcher("!org.foo.*,org.*");
        assertFalse(m.matches("org.foo.bar"));
        assertFalse(m.matches("org.foo"));
        assertTrue(m.matches("org.foobar")); // No dot...
        assertTrue(m.matches("org.bar"));
        assertTrue(m.matches("org"));
        assertFalse(m.matches("bar"));
        assertFalse(m.matches("organization"));
    }

    @Test
    public void testInclusions() {
        Matcher m = new Matcher("org.foo,!org.*,*"); // Need a positive default to invert.
        assertFalse(m.matches("org.foo.bar"));
        assertTrue(m.matches("org.foo"));
        assertFalse(m.matches("org.foobar"));
        assertFalse(m.matches("org.bar"));
        assertFalse(m.matches("org"));
        assertTrue(m.matches("bar"));
        assertTrue(m.matches("organization"));
    }

    @Test
    public void testWildcardInclusions() {
        Matcher m = new Matcher("org.foo.*,!org.*,*"); // Need a positive default to invert.
        assertTrue(m.matches("org.foo.bar"));
        assertTrue(m.matches("org.foo"));
        assertFalse(m.matches("org.foobar"));
        assertFalse(m.matches("org.bar"));
        assertFalse(m.matches("org"));
        assertTrue(m.matches("bar"));
        assertTrue(m.matches("organization"));
    }


    @Test
    public void testInstructions() {
        Matcher m = new Matcher("org.foo;inst=ss");
        assertTrue(m.matches("org.foo"));
        assertFalse(m.matches("org.foo.foo"));
    }

    @Test
    public void testRegex1() {
        Matcher m = new Matcher("org.*.foo");
        assertFalse(m.matches("org.foo"));
        assertFalse(m.matches("orgx.bar.foo"));
        assertFalse(m.matches("org.bar.foox"));
        assertFalse(m.matches("org.bar.foo.x"));
        assertFalse(m.matches("x.org.bar.foo"));
        assertTrue(m.matches("org.bar.foo"));
        assertTrue(m.matches("org.foo.foo"));
        assertTrue(m.matches("org.foo.foo.foo"));
    }
    @Test
    public void testRegex2() {
        Matcher m = new Matcher("org.b?r");
        assertFalse(m.matches("org.foo"));
        assertFalse(m.matches("org.aar"));
        assertFalse(m.matches("org.baa"));
        assertTrue(m.matches("org.bar"));
        assertTrue(m.matches("org.bAr"));
    }
    @Test
    public void testRegex3() {
        Matcher m = new Matcher("org.b?r.*");
        assertFalse(m.matches("org.foo"));
        assertFalse(m.matches("org.aar"));
        assertFalse(m.matches("org.aar.bar"));
        assertFalse(m.matches("bar.org.bar"));
        assertTrue(m.matches("org.bar"));
        assertTrue(m.matches("org.bar.baz"));
        assertTrue(m.matches("org.bar.baz.boz"));
        assertTrue(m.matches("org.bAr.bar"));
    }
    @Test
    public void testRegex4() {
        Matcher m = new Matcher("org*bar");
        assertFalse(m.matches("org.bar.foo"));
        assertFalse(m.matches("org.aar"));
        assertTrue(m.matches("org.aar.bar"));
        assertFalse(m.matches("bar.org.bar"));
        assertTrue(m.matches("org.bar"));
        assertTrue(m.matches("orgbar"));
        assertTrue(m.matches("organize_the_bar")); // Twisted but correct.
    }

    @Test
    public void testRegex5() {
        Matcher m = new Matcher("*.org.b?r");
        assertFalse(m.matches("org.foo"));
        assertFalse(m.matches("org.aar"));
        assertFalse(m.matches("org.aar.bar"));
        assertTrue(m.matches("bar.org.bar"));
        assertFalse(m.matches("org.bar")); // *. is not magic like .*
        assertTrue(m.matches("x.org.bar"));
        assertFalse(m.matches("xorg.bar"));
        assertTrue(m.matches("x.y.org.bar"));
    }


}
