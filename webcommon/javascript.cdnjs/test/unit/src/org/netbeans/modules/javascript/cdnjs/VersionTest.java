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

package org.netbeans.modules.javascript.cdnjs;

import java.util.Comparator;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests of a {@code Version} class.
 *
 * @author Jan Stola
 */
public class VersionTest {

    private int compare(Comparator<Version> comparator, String version1, String version2) {
        Version v1 = Version.parse(version1);
        Version v2 = Version.parse(version2);
        return (int)Math.signum(comparator.compare(v1, v2));
    }

    @Test
    public void testBasic() {
        Comparator<Version> comparator = Version.Comparator.getInstance(false);
        assertEquals(0, compare(comparator, "2", "2"));
        assertEquals(1, compare(comparator, "5", "7"));
        assertEquals(-1, compare(comparator, "7", "5"));
        assertEquals(-1, compare(comparator, "13", "4"));
        assertEquals(1, compare(comparator, "2.3", "2.5"));
        assertEquals(-1, compare(comparator, "2.15", "2.9"));
        assertEquals(-1, compare(comparator, "4.1", "4"));
        assertEquals(1, compare(comparator, "3.1.4.1.5.926", "3.1.4.5.1234"));
    }

    @Test
    public void testAscending() {
        Comparator<Version> comparator = Version.Comparator.getInstance(true);
        assertEquals(0, compare(comparator, "2", "2"));
        assertEquals(-1, compare(comparator, "5", "7"));
        assertEquals(1, compare(comparator, "7", "5"));
        assertEquals(1, compare(comparator, "13", "4"));
        assertEquals(-1, compare(comparator, "2.3", "2.5"));
        assertEquals(1, compare(comparator, "2.15", "2.9"));
        assertEquals(1, compare(comparator, "4.1", "4"));
        assertEquals(-1, compare(comparator, "3.1.4.1.5.926", "3.1.4.5.1234"));
    }

    @Test
    public void testPre() {
        Comparator<Version> comparator = Version.Comparator.getInstance(false);
        assertEquals(1, compare(comparator, "2.3alpha", "2.3"));
        assertEquals(-1, compare(comparator, "3.8", "3.8beta"));
        assertEquals(1, compare(comparator, "0.0.1pre", "0.0.1"));
        assertEquals(1, compare(comparator, "1.2.3beta2", "1.2.3beta3"));
        assertEquals(-1, compare(comparator, "8.0.1", "8.0.1-rc4"));
        assertEquals(1, compare(comparator, "0.7dev", "0.7"));
        assertEquals(1, compare(comparator, "5alpha", "5beta"));
        assertEquals(1, compare(comparator, "5.3alpha", "5.3rc"));
        assertEquals(1, compare(comparator, "5.3beta", "5.3rc"));
        assertEquals(1, compare(comparator, "5.3pre", "5.3rc"));
        assertEquals(1, compare(comparator, "5.3dev", "5.3rc"));
    }

    @Test
    public void testPost() {
        Comparator<Version> comparator = Version.Comparator.getInstance(false);
        assertEquals(-1, compare(comparator, "3.0.1patch", "3.0.1"));
        assertEquals(-1, compare(comparator, "3.0.1fix", "3.0.1"));
        assertEquals(1, compare(comparator, "3.0.1patch2", "3.0.1patch15"));
    }

    @Test
    public void testText() {
        Comparator<Version> comparator = Version.Comparator.getInstance(false);
        assertEquals(1, compare(comparator, "23a", "23c"));        
    }

    @Test
    public void testDate() {
        Comparator<Version> comparator = Version.Comparator.getInstance(false);
        assertEquals(-1, compare(comparator, "2014-11-29", "2014-08-29"));
        assertEquals(1, compare(comparator, "2014-5-13", "2014-10-29"));
    }

    @Test
    public void testOther() {
        Comparator<Version> comparator = Version.Comparator.getInstance(false);
        assertEquals(-1, compare(comparator, "r3", "r2"));
        assertEquals(1, compare(comparator, "r2", "r15"));
        assertEquals(1, compare(comparator, "1.0.0-rc8", "1.0.0-rc.9"));
        assertEquals(-1, compare(comparator, "1.3.0-beta.13", "1.3.0-beta.9"));
        assertEquals(1, compare(comparator, "2.0.0.RC4", "2.1.2"));
    }

    @Test
    public void testLegacy() { // Test-cases from the old implementation of CDNJS libraries
        Comparator<Version> comparator = Version.Comparator.getInstance(false);
        assertEquals(1, compare(comparator, "1.0", "1.0patch1"));
        assertEquals(-1, compare(comparator, "1.0patch1", "1.0"));
        assertEquals(-1, compare(comparator, "1.0", "1.0rc1"));
        assertEquals(1, compare(comparator, "1.0rc1", "1.0"));
        assertEquals(1, compare(comparator, "1.0-pre.1", "1.0-pre.2"));
        assertEquals(-1, compare(comparator, "1.0-rc2", "1.0-pre1"));
        assertEquals(-1, compare(comparator, "1.0-rc1", "1.0-pre7"));
        assertEquals(1, compare(comparator, "1.0-pre7", "1.0-rc1"));
        assertEquals(-1, compare(comparator, "1.0-rc1", "1.0-beta7"));
        assertEquals(-1, compare(comparator, "1.0-pre1", "1.0-beta7"));
        assertEquals(1, compare(comparator, "1.0-beta7", "1.0-pre1"));
        assertEquals(0, compare(comparator, "1.0-rc2", "1.0-rc2"));
        assertEquals(1, compare(comparator, "1.0-rc2", "1.0-patch"));
        assertEquals(-1, compare(comparator, "1.0-patch", "1.0-rc2"));
        assertEquals(-1, compare(comparator, "1.0-patch4", "1.0-patch3"));
        assertEquals(-1, compare(comparator, "1.0-patch1", "1.0-beta.3"));
        assertEquals(1, compare(comparator, "3.0.0-rc1", "3.0.0-rc2"));
        assertEquals(-1, compare(comparator, "3.0.0-rc2", "3.0.0-rc1"));
    }

    @Test
    public void testIssue230467() {
        Comparator<Version> comparator = Version.Comparator.getInstance(false);
        assertEquals(1, compare(comparator, "1.0.0rc6", "1.1.5")); // angular.js
        assertEquals(1, compare(comparator, "0.9.9-amdjs", "1.0.0")); // backbone.js
        assertEquals(1, compare(comparator, "1.0pre", "1.0.0-rc.4")); // ember.js
        assertEquals(1, compare(comparator, "0.97a", "0.99")); // headjs
        assertEquals(1, compare(comparator, "0.5.0-dev", "0.7.2")); // mustache.js
        assertEquals(1, compare(comparator, "1.0.rc.2", "1.0.0-rc.4")); // handlebars.js
        assertEquals(1, compare(comparator, "1.0.0-rc.4", "1.0.0-rc.7")); // ember.js
    }

    @Test
    public void testIssue248426() {
        Comparator<Version> comparator = Version.Comparator.getInstance(false);
        assertEquals(-1, compare(comparator, "2.1.1", "2.1.1-rc2")); // jQuery
    }

}
