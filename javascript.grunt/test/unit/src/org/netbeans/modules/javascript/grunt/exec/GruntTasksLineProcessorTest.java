/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.grunt.exec;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.junit.NbTestCase;

public class GruntTasksLineProcessorTest extends NbTestCase {

    public GruntTasksLineProcessorTest(String name) {
        super(name);
    }

    public void testGruntTasks() throws Exception {
        List<String> tasks = readTasks("grunt.txt");
        assertEquals(Arrays.asList(
                "jshint-server",
                "jshint-client",
                "jshint"
        ), tasks);

    }

    public void testEmptyGruntTasks() throws Exception {
        List<String> tasks = readTasks("grunt-empty.txt");
        assertEquals(Collections.emptyList(), tasks);
    }

    public void testGruntTasksWithSpaces() throws Exception {
        List<String> tasks = readTasks("grunt-space.txt");
        assertEquals(Collections.singletonList("co tohle"), tasks);
    }

    public void testGruntTasksIssue250688() throws Exception {
        List<String> tasks = readTasks("grunt-250688.txt");
        assertEquals(Arrays.asList(
                "bower",
                "bowercopy",
                "clean",
                "requirejs",
                "sass",
                "uglify",
                "watch",
                "jasmine_node",
                "karma",
                "madge",
                "_generate_inits",
                "_generate_modules_list",
                "requirejs-lazy",
                "build",
                "cdc",
                "clean-build",
                "development",
                "default",
                "optimize",
                "release",
                "test",
                "test-all",
                "test-chrome",
                "test-backend",
                "test-backend-chrome",
                "test-coverage",
                "test-integrity",
                "test-debug",
                "test-backend-debug"
        ), tasks);

    }

    private List<String> readTasks(String filename) throws IOException {
        GruntExecutable.GruntTasksLineProcessor gruntTasksLineProcessor = new GruntExecutable.GruntTasksLineProcessor();
        for (String line : readLines(new File(getDataDir(), filename))) {
            gruntTasksLineProcessor.processLine(line);
        }
        return gruntTasksLineProcessor.getTasks();
    }

    private List<String> readLines(File file) throws IOException {
        assertTrue(file.isFile());
        return Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
    }

    private void assertEquals(List<String> expected, List<String> actual) {
        assertEquals(actual.toString(), expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i));
        }
    }

}
