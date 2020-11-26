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
package org.netbeans.modules.java.mx.project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ComplianceTest {
    @Test
    public void defaultNull() {
        Compliance eightPlusByDefault = Compliance.parse(null);
        assertMinMax(eightPlusByDefault, 8, null);
    }
    @Test
    public void elevenPlus() {
        Compliance elevenPlus = Compliance.parse("11+");
        assertMinMax(elevenPlus, 11, null);
        assertEquals("11+", elevenPlus.toString());
    }
    
    @Test
    public void elevenTwelve() {
        Compliance elevenTwelve = Compliance.parse("11..12");
        assertMinMax(elevenTwelve, 11, 12);
        assertEquals("11..12", elevenTwelve.toString());
        assertEquals("11", elevenTwelve.getSourceLevel());
    }
    
    @Test
    public void eight() {
        Compliance eight = Compliance.parse("8");
        assertMinMax(eight, 8, 8);
        assertEquals("8", eight.toString());
    }

    @Test
    public void eightPlus() {
        Compliance eightPlus = Compliance.parse("8+");
        assertMinMax(eightPlus, 8, null);
    }

    @Test
    public void oneEight() {
        Compliance eight = Compliance.parse("1.8");
        assertMinMax(eight, 8, 8);
        assertEquals("8", eight.toString());
        assertEquals("1.8", eight.getSourceLevel());
    }

    @Test
    public void oneEightPlus() {
        Compliance eightPlus = Compliance.parse("1.8+");
        assertMinMax(eightPlus, 8, null);
        assertTrue(eightPlus.includes(8));
        assertTrue(eightPlus.includes(9));
        assertTrue(eightPlus.includes(10));
        assertEquals("8+", eightPlus.toString());
        assertEquals("1.8", eightPlus.getSourceLevel());
    }

    @Test
    public void eightThirteen() {
        Compliance eightThirteen = Compliance.parse("8,13");
        assertMinMax(eightThirteen, 8, 8);
        assertTrue(eightThirteen.includes(8));
        assertFalse(eightThirteen.includes(11));
        assertFalse(eightThirteen.includes(13));
        assertFalse(eightThirteen.includes(14));
        assertEquals("8", eightThirteen.toString());
        assertEquals("1.8", eightThirteen.getSourceLevel());
    }

    @Test
    public void eightThirteenPlus() {
        Compliance eightThirteen = Compliance.parse("8,13+");
        assertMinMax(eightThirteen, 8, null);
        assertTrue(eightThirteen.includes(8));
        assertTrue(eightThirteen.includes(11));
        assertTrue(eightThirteen.includes(13));
        assertTrue(eightThirteen.includes(14));
        assertEquals("8+", eightThirteen.toString());
        assertEquals("1.8", eightThirteen.getSourceLevel());
    }

    private void assertMinMax(Compliance c, int min, Integer max) {
        assertNotNull(c);
        assertEquals(min, c.min);
        if (max == null) {
            assertEquals(Integer.MAX_VALUE, c.max);
        } else {
            assertEquals(max.intValue(), c.max);
        }
    }
}
