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
package org.netbeans.modules.parsing.lucene.support;

import java.lang.reflect.Field;
import java.util.regex.Pattern;
import org.netbeans.modules.parsing.lucene.support.Queries.RegexpFilter;
import static org.junit.Assert.assertEquals;
import org.junit.Test;


public class QueriesTest {

    public QueriesTest() {
    }

    @Test
    public void testRegexpStartText() throws Exception {
        // The first special char of the regexp ends the start text, in this
        // case a group is introduced
        assertEquals("base", getRegexpFilterPrefixForPattern("base(Query)"));
        // Quoted parts of the regexp are extracted verbatim
        assertEquals("base.*", getRegexpFilterPrefixForPattern(Pattern.quote("base.*")));
        // Quantifiers that can become 0 need to remove the character they work
        // on. Quantifiers on groups are handled by the group handling
        assertEquals("base", getRegexpFilterPrefixForPattern("base+"));
        assertEquals("bas", getRegexpFilterPrefixForPattern("base?"));
        assertEquals("bas", getRegexpFilterPrefixForPattern("base*"));
        assertEquals("bas", getRegexpFilterPrefixForPattern("base{0,5}"));
    }

    private String getRegexpFilterPrefixForPattern(String pattern) throws Exception {
        RegexpFilter rf = new RegexpFilter("dummy", pattern, true);
        Field startPrefix = RegexpFilter.class.getDeclaredField("startPrefix");
        startPrefix.setAccessible(true);
        return (String) startPrefix.get(rf);
    }

}
