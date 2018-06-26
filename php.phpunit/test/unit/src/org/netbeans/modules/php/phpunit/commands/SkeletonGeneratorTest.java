/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
