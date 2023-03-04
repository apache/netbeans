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
