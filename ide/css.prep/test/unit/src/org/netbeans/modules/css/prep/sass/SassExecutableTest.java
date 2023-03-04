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
package org.netbeans.modules.css.prep.sass;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.css.prep.util.VersionOutputProcessorFactory;


public class SassExecutableTest extends NbTestCase {

    private static final VersionOutputProcessorFactory VERSION_OUTPUT_PROCESSOR_FACTORY
            = new VersionOutputProcessorFactory(SassExecutable.VERSION_PATTERN);

    public SassExecutableTest(String name) {
        super(name);
    }

    public void testParseValidVersions() {
        assertEquals("3.2.9", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("Sass 3.2.9 (Media Mark)"));
        assertEquals("3.2.9", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("SASS 3.2.9 (Media Mark)"));
        assertEquals("3.3.0", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("Sass 3.3.0 (Media Mark)"));
        assertEquals("3.3.0", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("Sass 3.3.0.alpha.198"));
        assertEquals("3.2.9", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("Sass 3.2.9a (Media Mark)"));
        assertEquals("3.2.9", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("Sass 3.2.9-upd10 (Media Mark)"));
        assertEquals("3.2.9", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("Sass 3.2.9 patch 3 (Media Mark)"));
        assertEquals("3.2.9", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("Sass 3.2.9"));
        assertEquals("3.2.9.1.25", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("Sass 3.2.9.1.25"));
        assertEquals("3.2.9", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("Sass  3.2.9    (Media Mark)"));
        assertEquals("1", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("Sass 1 (Media Mark)"));
        assertEquals("1.0", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("Sass 1.0 (Media Mark)"));
        assertEquals("1.0", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("Sass 1.0,25 (Media Mark)"));
    }

    public void testParseInvalidVersions() {
        assertNull(VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("3.2.9 (Media Mark)"));
        assertNull(VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("Sass-NG 3.2.9 (Media Mark)"));
    }

}
