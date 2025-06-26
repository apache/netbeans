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
package org.netbeans.modules.diff.tree;

import java.util.regex.Pattern;
import org.junit.Test;
import org.netbeans.modules.diff.tree.ExclusionPattern.ExclusionType;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class ExclusionPatternTest {

    public ExclusionPatternTest() {
    }

    @Test
    public void testExclusionWildcardUnixStyleSourceUnixStyleTarget() {
        ExclusionPattern exclusionPattern = new ExclusionPattern();
        exclusionPattern.setType(ExclusionType.WILDCARD);
        exclusionPattern.setPattern("node_modules/**/demoFolder/*/hallo");
        Pattern matcherPattern = exclusionPattern.asPattern();
        assertTrue(matcherPattern.matcher("node_modules/x1/x2/demoFolder/y1/hallo").matches());
        assertTrue(matcherPattern.matcher("node_modules/x1/demoFolder/y1/hallo").matches());
        assertFalse(matcherPattern.matcher("node_modules/x1/demoFolder/y1/y2/hallo").matches());
        assertFalse(matcherPattern.matcher("node_modules/demoFolder/y1/hallo").matches());
        assertFalse(matcherPattern.matcher("node_modules/demoFolder/hallo").matches());
    }

    @Test
    public void testExclusionWildcardWindowsStyleSourceUnixStyleTarget() {
        ExclusionPattern exclusionPattern2 = new ExclusionPattern();
        exclusionPattern2.setType(ExclusionType.WILDCARD);
        exclusionPattern2.setPattern("node_modules\\**\\demoFolder\\*\\hallo");
        Pattern matcherPattern2 = exclusionPattern2.asPattern();
        assertTrue(matcherPattern2.matcher("node_modules/x1/x2/demoFolder/y1/hallo").matches());
        assertTrue(matcherPattern2.matcher("node_modules/x1/demoFolder/y1/hallo").matches());
        assertFalse(matcherPattern2.matcher("node_modules/x1/demoFolder/y1/y2/hallo").matches());
        assertFalse(matcherPattern2.matcher("node_modules/demoFolder/y1/hallo").matches());
        assertFalse(matcherPattern2.matcher("node_modules/demoFolder/hallo").matches());
    }

    @Test
    public void testExclusionWildcardUnixStyleSourceWindowsStyleTarget() {
        ExclusionPattern exclusionPattern = new ExclusionPattern();
        exclusionPattern.setType(ExclusionType.WILDCARD);
        exclusionPattern.setPattern("node_modules/**/demoFolder/*/hallo");
        Pattern matcherPattern = exclusionPattern.asPattern();
        assertTrue(matcherPattern.matcher("node_modules\\x1\\x2\\demoFolder\\y1\\hallo").matches());
        assertTrue(matcherPattern.matcher("node_modules\\x1\\demoFolder\\y1\\hallo").matches());
        assertFalse(matcherPattern.matcher("node_modules\\x1\\demoFolder\\y1\\y2\\hallo").matches());
        assertFalse(matcherPattern.matcher("node_modules\\demoFolder\\y1\\hallo").matches());
        assertFalse(matcherPattern.matcher("node_modules\\demoFolder\\hallo").matches());
    }


    @Test
    public void testExclusionWildcardWindowsStyleSourceWindowsStyleTarget() {
        ExclusionPattern exclusionPattern2 = new ExclusionPattern();
        exclusionPattern2.setType(ExclusionType.WILDCARD);
        exclusionPattern2.setPattern("node_modules\\**\\demoFolder\\*\\hallo");
        Pattern matcherPattern2 = exclusionPattern2.asPattern();
        assertTrue(matcherPattern2.matcher("node_modules\\x1\\x2\\demoFolder\\y1\\hallo").matches());
        assertTrue(matcherPattern2.matcher("node_modules\\x1\\demoFolder\\y1\\hallo").matches());
        assertFalse(matcherPattern2.matcher("node_modules\\x1\\demoFolder\\y1\\y2\\hallo").matches());
        assertFalse(matcherPattern2.matcher("node_modules\\demoFolder\\y1\\hallo").matches());
        assertFalse(matcherPattern2.matcher("node_modules\\demoFolder\\hallo").matches());
    }

}
