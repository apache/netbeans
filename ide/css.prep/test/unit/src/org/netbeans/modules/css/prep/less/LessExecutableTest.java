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

package org.netbeans.modules.css.prep.less;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.css.prep.util.VersionOutputProcessorFactory;

public class LessExecutableTest extends NbTestCase {

    private static final VersionOutputProcessorFactory VERSION_OUTPUT_PROCESSOR_FACTORY
            = new VersionOutputProcessorFactory(LessExecutable.VERSION_PATTERN);

    public LessExecutableTest(String name) {
        super(name);
    }

    public void testParseValidVersions() {
        assertEquals("1.5.1", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("lessc 1.5.1 (LESS Compiler) [JavaScript]"));
        assertEquals("1.5.1", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("LESSC 1.5.1 (LESS Compiler) [JavaScript]"));
        assertEquals("1.6", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("lessc 1.6 (LESS Compiler) [JavaScript]"));
        assertEquals("1.5.1", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("lessc 1.5.1.alpha.198 (LESS Compiler) [JavaScript]"));
        assertEquals("1.5.1", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("lessc 1.5.1a (LESS Compiler) [JavaScript]"));
        assertEquals("1.5.1", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("lessc 1.5.1-upd10 (LESS Compiler) [JavaScript]"));
        assertEquals("1.5.1", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("lessc 1.5.1 patch 3 (LESS Compiler) [JavaScript]"));
        assertEquals("1.5.1", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("lessc 1.5.1"));
        assertEquals("1.5.1.1.25", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("lessc 1.5.1.1.25"));
        assertEquals("1.5.1", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("lessc          1.5.1              (LESS Compiler) [JavaScript]"));
        assertEquals("2", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("lessc 2 (LESS Compiler) [JavaScript]"));
        assertEquals("2.0", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("lessc 2.0 (LESS Compiler) [JavaScript]"));
        assertEquals("1.5", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("lessc 1.5,1 (LESS Compiler) [JavaScript]"));
    }

    public void testParseInvalidVersions() {
        assertNull(VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("1.5.1 (LESS Compiler) [JavaScript]"));
        assertNull(VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("lessc-NG 1.5.1 (LESS Compiler) [JavaScript]"));
    }

}
