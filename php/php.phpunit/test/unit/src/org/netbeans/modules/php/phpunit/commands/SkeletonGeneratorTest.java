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

package org.netbeans.modules.php.phpunit.commands;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.phpunit.util.VersionOutputProcessorFactory;


public class SkeletonGeneratorTest extends NbTestCase {

    private static final VersionOutputProcessorFactory VERSION_OUTPUT_PROCESSOR_FACTORY
            = new VersionOutputProcessorFactory(SkeletonGenerator.VERSION_PATTERN);

    public SkeletonGeneratorTest(String name) {
        super(name);
    }

    public void testParseValidVersions() {
        assertEquals("2.0.1", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("phpunit-skelgen 2.0.1 by Sebastian Bergmann."));
        assertEquals("2.0.15", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("phpunit-skelgen 2.0.15 by Sebastian Bergmann."));
        assertEquals("2.1", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("phpunit-skelgen 2.1 by Sebastian Bergmann."));
        assertEquals("2", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("phpunit-skelgen 2 by Sebastian Bergmann."));
        assertEquals("2.0.1", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("phpunit-skelgen 2.0.1.alpha.198 by Sebastian Bergmann."));
        assertEquals("2.0.1", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("phpunit-skelgen 2.0.1"));
        assertEquals("2.0.1", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("phpunit-skelgen 2.0.1.alpha.198"));
        assertEquals("2.0.1", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("phpunit-skelgen    2.0.1.alpha.198    "));
        assertEquals("1.2.1", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("PHPUnit Skeleton Generator 1.2.1 by Sebastian Bergmann."));
        assertEquals("1.2.18", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("PHPUnit Skeleton Generator 1.2.18 by Sebastian Bergmann."));
        assertEquals("1.2", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("PHPUnit Skeleton Generator 1.2 by Sebastian Bergmann."));
        assertEquals("1", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("PHPUnit Skeleton Generator 1 by Sebastian Bergmann."));
        assertEquals("1.2.1", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("PHPUnit Skeleton Generator 1.2.1-upd10 by Sebastian Bergmann."));
        assertEquals("1.2.1", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("PHPUnit Skeleton Generator 1.2.1 patch 3 by Sebastian Bergmann."));
        assertEquals("1.2.1", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("PHPUnit Skeleton Generator 1.2.1"));
        assertEquals("1.2.1", VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("PHPUnit       Skeleton    Generator    1.2.1"));
    }

    public void testParseInvalidVersions() {
        assertNull(VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("2.0.1 by Sebastian Bergmann."));
        assertNull(VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("phpunit 4.0.17 by Sebastian Bergmann."));
        assertNull(VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("phpunit 4.0.17"));
        assertNull(VERSION_OUTPUT_PROCESSOR_FACTORY.parseVersion("4.0.17"));
    }

}
