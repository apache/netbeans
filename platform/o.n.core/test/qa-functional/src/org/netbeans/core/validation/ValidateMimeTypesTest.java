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
package org.netbeans.core.validation;

import java.io.IOException;
import java.nio.file.Files;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;

/** Checks that {@link Files#pr} and {@link FileObject#get} are consistent
 * when running in the NetBeans IDE and platform. Goes thru all
 * the templates and verifies their mime types.
 */
public final class ValidateMimeTypesTest extends NbTestCase {
    public ValidateMimeTypesTest(String name) {
        super(name);
    }


    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(NbModuleSuite.createConfiguration(ValidateMimeTypesTest.class)
            .clusters("(?!ergonomics).*")
            .enableClasspathModules(false)
            .enableModules(".*")
            .gui(false).suite()
        );
        return suite;
    }

    public void testCheckTemplatesMimeTypes() throws IOException {
        var errors = new StringBuilder();

        var root = FileUtil.getConfigFile("Templates");
        assertNotNull("Templates directory found", root);
        var en = root.getChildren(true);
        while (en.hasMoreElements()) {
            var file = en.nextElement();
            if (file.isFolder()) {
                continue;
            }
            var path = FileUtil.toPath(file);
            assertNotNull("Path found for " + file, path);
            var nbMime = file.getMIMEType();
            var jdkMime = Files.probeContentType(path);

            if (nbMime.equals(jdkMime)) {
                continue;
            }

            errors.append("\n")
                .append(file)
                .append(" mime types differ among NetBeans and JDK: ")
                .append(nbMime)
                .append(" != ")
                .append(jdkMime);
        }
        if (!errors.isEmpty()) {
            errors.insert(0, "Found differences in MIME types:");
            fail(errors.toString());
        }
    }
}
